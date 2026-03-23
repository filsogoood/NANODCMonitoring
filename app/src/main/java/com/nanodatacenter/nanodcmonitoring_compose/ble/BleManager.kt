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
            if (deviceName == null || !deviceName.contains("JT", ignoreCase = true)) return

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
            } else {
                currentList.add(device)
            }
            _scanResults.value = currentList.sortedByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            _isScanning.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i(TAG, "onConnectionStateChange: status=$status newState=$newState")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server")
                    _connectionState.value = ConnectionState.DISCOVERING_SERVICES
                    _connectedDeviceName.value = gatt.device.name ?: gatt.device.address
                    gatt.requestMtu(247)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server")
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
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "MTU changed: mtu=$mtu status=$status")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service discovery failed: $status")
                return
            }

            Log.i(TAG, "Services discovered: ${gatt.services.size}")

            writeCharacteristic = null
            notifyCharacteristic = null

            for (service in gatt.services) {
                for (char in service.characteristics) {
                    val props = char.properties
                    if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 && notifyCharacteristic == null) {
                        notifyCharacteristic = char
                    }
                    if (props and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 && writeCharacteristic == null) {
                        writeCharacteristic = char
                    }
                }
            }

            val notifyChar = notifyCharacteristic
            if (notifyChar != null) {
                gatt.setCharacteristicNotification(notifyChar, true)
                val cccd = notifyChar.getDescriptor(CCCD_UUID)
                if (cccd != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        @Suppress("DEPRECATION")
                        cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        gatt.writeDescriptor(cccd)
                    }
                } else {
                    startPolling()
                }
            } else {
                if (writeCharacteristic != null) startPolling()
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (descriptor.uuid == CCCD_UUID) {
                startPolling()
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value ?: return
            handleNotification(value)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleNotification(value)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            isWriting = false
            processWriteQueue()
        }
    }

    private fun startPolling() {
        _connectionState.value = ConnectionState.READY
        handler.postDelayed(statusPollRunnable, 500)
    }

    private fun handleNotification(data: ByteArray) {
        val status = JtProtocol.parseStatusResponse(data)
        if (status != null) {
            _deviceStatus.value = status
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
        if (_isScanning.value) return
        _scanResults.value = emptyList()
        _isScanning.value = true

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner?.startScan(null, settings, scanCallback)
    }

    fun stopScan() {
        if (!_isScanning.value) return
        bleScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    fun connect(device: BluetoothDevice) {
        stopScan()
        _connectionState.value = ConnectionState.CONNECTING
        writeQueue.clear()
        isWriting = false
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
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
