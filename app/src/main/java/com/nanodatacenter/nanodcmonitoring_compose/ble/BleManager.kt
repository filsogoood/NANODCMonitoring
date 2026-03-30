package com.nanodatacenter.nanodcmonitoring_compose.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice
)

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, DISCOVERING_SERVICES, READY
}

@SuppressLint("MissingPermission")
class BleManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "BleManager"
        private const val POLL_INTERVAL = 500L
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        @Volatile
        private var instance: BleManager? = null

        fun getInstance(context: Context): BleManager {
            return instance ?: synchronized(this) {
                instance ?: BleManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val bleScanner: BluetoothLeScanner?
        get() = bluetoothAdapter?.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null

    private val handler = Handler(Looper.getMainLooper())

    private val writeQueue = ConcurrentLinkedQueue<ByteArray>()
    @Volatile
    private var isWriting = false

    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    val scanResults: StateFlow<List<BleDevice>> = _scanResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    val deviceStatus: StateFlow<DeviceStatus?> = _deviceStatus.asStateFlow()

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val statusPollRunnable = object : Runnable {
        override fun run() {
            if (_connectionState.value == ConnectionState.READY) {
                enqueueWrite(JtProtocol.buildStatusRequest())
                handler.postDelayed(this, POLL_INTERVAL)
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name
            Log.d(TAG, "onScanResult: name=$deviceName addr=${result.device.address} rssi=${result.rssi}")
            if (deviceName == null || !deviceName.contains("JT", ignoreCase = true)) {
                Log.d(TAG, "onScanResult: 필터링됨 (JT 미포함) name=$deviceName")
                return
            }

            val device = BleDevice(
                name = deviceName,
                address = result.device.address,
                rssi = result.rssi,
                device = result.device
            )
            val currentList = _scanResults.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == device.address }
            if (existingIndex >= 0) {
                currentList[existingIndex] = device
                Log.d(TAG, "onScanResult: 기존 장비 업데이트 $deviceName (${device.address})")
            } else {
                currentList.add(device)
                Log.i(TAG, "onScanResult: 새 장비 발견 $deviceName (${device.address}) rssi=${device.rssi}")
            }
            _scanResults.value = currentList.sortedByDescending { it.rssi }
            Log.d(TAG, "onScanResult: 현재 장비 수=${_scanResults.value.size}")
        }

        override fun onScanFailed(errorCode: Int) {
            val reason = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "ALREADY_STARTED"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "APP_REGISTRATION_FAILED"
                SCAN_FAILED_INTERNAL_ERROR -> "INTERNAL_ERROR"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "FEATURE_UNSUPPORTED"
                else -> "UNKNOWN($errorCode)"
            }
            Log.e(TAG, "onScanFailed: errorCode=$errorCode reason=$reason")
            _isScanning.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val stateStr = when (newState) {
                BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
                BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
                BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
                BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
                else -> "UNKNOWN($newState)"
            }
            Log.i(TAG, "onConnectionStateChange: status=$status newState=$stateStr device=${gatt.device.address}")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "GATT 연결 성공 → MTU 요청 (247)")
                    _connectionState.value = ConnectionState.DISCOVERING_SERVICES
                    _connectedDeviceName.value = gatt.device.name ?: gatt.device.address
                    gatt.requestMtu(247)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w(TAG, "GATT 연결 해제 (status=$status) → 정리 시작")
                    handler.removeCallbacks(statusPollRunnable)
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _connectedDeviceName.value = null
                    _deviceStatus.value = null
                    writeCharacteristic = null
                    notifyCharacteristic = null
                    writeQueue.clear()
                    isWriting = false
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                    Log.i(TAG, "GATT 정리 완료")
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "onMtuChanged: mtu=$mtu status=$status → 서비스 탐색 시작")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "서비스 탐색 실패: status=$status")
                return
            }

            Log.i(TAG, "서비스 탐색 완료: ${gatt.services.size}개 서비스 발견")

            writeCharacteristic = null
            notifyCharacteristic = null

            for (service in gatt.services) {
                Log.d(TAG, "  서비스: ${service.uuid}")
                for (char in service.characteristics) {
                    val props = char.properties
                    val propStr = mutableListOf<String>().apply {
                        if (props and BluetoothGattCharacteristic.PROPERTY_READ != 0) add("READ")
                        if (props and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) add("WRITE")
                        if (props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) add("WRITE_NO_RESP")
                        if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) add("NOTIFY")
                        if (props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) add("INDICATE")
                    }.joinToString("|")
                    Log.d(TAG, "    특성: ${char.uuid} [$propStr]")

                    if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 && notifyCharacteristic == null) {
                        notifyCharacteristic = char
                        Log.i(TAG, "    → NOTIFY 특성 선택: ${char.uuid}")
                    }
                    if (props and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 && writeCharacteristic == null) {
                        writeCharacteristic = char
                        Log.i(TAG, "    → WRITE 특성 선택: ${char.uuid}")
                    }
                }
            }

            Log.i(TAG, "특성 탐색 결과: write=${writeCharacteristic?.uuid} notify=${notifyCharacteristic?.uuid}")

            val notifyChar = notifyCharacteristic
            if (notifyChar != null) {
                gatt.setCharacteristicNotification(notifyChar, true)
                val cccd = notifyChar.getDescriptor(CCCD_UUID)
                if (cccd != null) {
                    Log.i(TAG, "CCCD 디스크립터 발견 → 알림 활성화 쓰기")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        @Suppress("DEPRECATION")
                        cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        gatt.writeDescriptor(cccd)
                    }
                } else {
                    Log.w(TAG, "CCCD 디스크립터 없음 → 바로 폴링 시작")
                    startPolling()
                }
            } else {
                Log.w(TAG, "NOTIFY 특성 없음")
                if (writeCharacteristic != null) {
                    Log.i(TAG, "WRITE 특성만 있음 → 바로 폴링 시작")
                    startPolling()
                } else {
                    Log.e(TAG, "WRITE/NOTIFY 특성 모두 없음! 통신 불가")
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Log.d(TAG, "onDescriptorWrite: uuid=${descriptor.uuid} status=$status")
            if (descriptor.uuid == CCCD_UUID) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "CCCD 쓰기 성공 → 폴링 시작")
                } else {
                    Log.e(TAG, "CCCD 쓰기 실패: status=$status")
                }
                startPolling()
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value ?: return
            Log.d(TAG, "onCharacteristicChanged(deprecated): ${value.size}bytes")
            handleNotification(value)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            Log.d(TAG, "onCharacteristicChanged: ${value.size}bytes")
            handleNotification(value)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onCharacteristicWrite 실패: status=$status")
            }
            isWriting = false
            processWriteQueue()
        }
    }

    private fun startPolling() {
        Log.i(TAG, "startPolling: 상태 폴링 시작 (${POLL_INTERVAL}ms 간격) → READY")
        _connectionState.value = ConnectionState.READY
        handler.postDelayed(statusPollRunnable, 500)
    }

    private fun handleNotification(data: ByteArray) {
        Log.d(TAG, "handleNotification: ${data.size}bytes → ${data.joinToString(" ") { "%02X".format(it) }}")
        val status = JtProtocol.parseStatusResponse(data)
        if (status != null) {
            Log.d(TAG, "handleNotification: 파싱 성공 → $status")
            _deviceStatus.value = status
        } else {
            Log.w(TAG, "handleNotification: 파싱 실패")
        }
    }

    private fun enqueueWrite(data: ByteArray) {
        writeQueue.add(data)
        if (!isWriting) {
            processWriteQueue()
        }
    }

    private fun processWriteQueue() {
        val gatt = bluetoothGatt ?: return
        val char = writeCharacteristic ?: return
        val data = writeQueue.poll() ?: return

        isWriting = true

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val writeType = if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            } else {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            val result = gatt.writeCharacteristic(char, data, writeType)
            if (result != 0) {
                isWriting = false
            }
        } else {
            @Suppress("DEPRECATION")
            char.value = data
            char.writeType = if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            } else {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            @Suppress("DEPRECATION")
            val result = gatt.writeCharacteristic(char)
            if (!result) {
                isWriting = false
            }
        }
    }

    fun startScan() {
        if (_isScanning.value) {
            Log.w(TAG, "startScan: 이미 스캔 중, 무시")
            return
        }
        Log.i(TAG, "startScan: 스캔 시작")
        Log.d(TAG, "startScan: bluetoothAdapter=${bluetoothAdapter != null} enabled=${bluetoothAdapter?.isEnabled} scanner=${bleScanner != null}")

        if (bluetoothAdapter?.isEnabled != true) {
            Log.e(TAG, "startScan: 블루투스가 꺼져있음! 스캔 불가")
            _isScanning.value = false
            return
        }

        _scanResults.value = emptyList()
        _isScanning.value = true

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanner = bleScanner
        if (scanner == null) {
            Log.e(TAG, "startScan: BLE Scanner가 null!")
            _isScanning.value = false
            return
        }

        try {
            scanner.startScan(null, settings, scanCallback)
            Log.i(TAG, "startScan: BLE 스캔 시작됨 (LOW_LATENCY)")
        } catch (e: SecurityException) {
            Log.e(TAG, "startScan: 권한 오류 - ${e.message}")
            _isScanning.value = false
        } catch (e: Exception) {
            Log.e(TAG, "startScan: 스캔 시작 실패 - ${e.message}")
            _isScanning.value = false
        }
    }

    fun stopScan() {
        if (!_isScanning.value) return
        Log.i(TAG, "stopScan: 스캔 중지")
        bleScanner?.stopScan(scanCallback)
        _isScanning.value = false
        Log.d(TAG, "stopScan: 발견된 장비 수=${_scanResults.value.size}")
    }

    fun connect(device: BluetoothDevice) {
        Log.i(TAG, "connect: 연결 시도 name=${device.name} addr=${device.address}")
        stopScan()
        _connectionState.value = ConnectionState.CONNECTING
        writeQueue.clear()
        isWriting = false
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        Log.d(TAG, "connect: connectGatt 호출 완료")
    }

    fun disconnect() {
        Log.i(TAG, "disconnect: 연결 해제 요청")
        handler.removeCallbacks(statusPollRunnable)
        writeQueue.clear()
        isWriting = false
        bluetoothGatt?.disconnect()
    }

    fun sendCommand(packet: ByteArray) {
        enqueueWrite(packet)
    }

    fun cleanup() {
        handler.removeCallbacks(statusPollRunnable)
        writeQueue.clear()
        stopScan()
        bluetoothGatt?.let {
            it.disconnect()
            it.close()
        }
        bluetoothGatt = null
    }
}
