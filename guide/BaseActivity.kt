package com.blejt.jtcontrol.Activity

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat


open class BaseActivity : AppCompatActivity(), BluetoothConnectionCallback,
    BluetoothNotificationCallback, BluetoothDeviceCallback {

    protected var bluetoothService: BluetoothService? = null
    private var bound = false
    private lateinit var parsed: DataMap

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("DEBUG","BASE ServiceConnected")
            val binder = service as BluetoothService.BluetoothBinder
            bluetoothService = binder.getService()
            bluetoothService?.setConnectionCallback(this@BaseActivity)  // 콜백 설정
            bluetoothService?.setNotificationCallback(this@BaseActivity)
            bound = true
            onBluetoothServiceConnected()  // 서비스 연결 시 호출
            Log.i("BaseActivity","onServiceConnected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
            bound = false
            Log.i("BaseActivity","onServiceDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.i("BaseActivity","onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        Log.i("BaseActivity","onDestroy")
    }

    // 서비스가 연결되었을 때 처리할 작업 (자식 클래스에서 오버라이드 가능)
    protected open fun onBluetoothServiceConnected() {
        // 서비스 연결 후 작업을 처리할 수 있도록 제공
        Log.i("BaseActivity","onBluetoothServiceConnected")
    }
    // BluetoothConnectionCallback 콜백 구현
    override fun onConnectionStateChanged(connected: Boolean) {
        // 자식 클래스에서 오버라이드 가능
        Log.i("BaseActivity","onConnectionStateChanged")
    }

    // Bluetooth 연결 종료 (closeBluetooth)
    protected fun closeBluetoothConnection() {
        bluetoothService?.closeBluetooth()
        Log.i("BaseActivity","closeBluetoothConnection")
    }

    override fun onNotificationReceived(data: ByteArray) {
        Log.i("BaseActivity","onNotificationReceived")
        TODO("Not yet implemented")
    }

    override fun onDeviceFound(device: BluetoothDevice, rssi: Int) {
        Log.i("BaseActivity","onDeviceFound")
        TODO("Not yet implemented")
    }

    fun calculateXOR(data: ByteArray): Byte {
        var xorResult: Byte = 0
        for (b in data) {
            xorResult = (xorResult.toInt() xor b.toInt()).toByte()
        }
        return xorResult
    }

    fun calculateSUM(data: ByteArray): Byte {
        var sum = 0
        for (b in data) {
            sum = (sum + b.toUByte().toInt()) % 256
        }
        return sum.toByte()
    }

    open fun parseReceiveData(rawData: ByteArray): DataMap? {
        // 최소 길이 검사
//        if (rawData[3].toInt() != 0x81) {
        if (rawData[3].toInt() != 0x01) {                                                           /*TODO 상태정보 0x81 데이터맵 확인 필요*/
            Log.i("DataParsing", "수신된 데이터가 상태정보가 아닙니다")
            return null
        }

        // 파싱 시작 인덱스 (0~5는 제외)
        var offset = 5
        // 실제 유효한 끝 인덱스 (마지막 2바이트 xor,sum 제외)
        val endIndex = rawData.size - 2

        // 바이트 -> Int 변환 헬퍼 함수(1바이트)
        fun readByte(): Int {
            val value = rawData[offset].toInt() and 0xFF
            offset += 1
            return value
        }

        // 2바이트 정수 읽기 (high byte, low byte)
        fun readTwoBytes(): Int {
            val high = rawData[offset].toInt() and 0xFF
            val low = rawData[offset + 1].toInt() and 0xFF
            offset += 2
            return (high shl 8) or low
        }

        // 순서대로 필드 파싱 (가상의 인덱스 순서, 실제 프로토콜 스펙에 맞게 조정 필요)
        val operation = readByte()
        val coldSettingTemp = readByte()
        val heatSettingTemp = readByte()
        val tempCorrection = readByte()
        val heatSetting = readByte()
        val superColdSetting = readByte()
        val weakWindSpeedSet = readByte()
        val moderateWindSpeedSet = readByte()
        val strongWindSpeedSet = readByte()
        val fanSpeedSet = readByte()
        val compOnDelay = readByte()
        val fanOffDelay = readByte()

        val filterTimeSetting_2byte = readTwoBytes()  // 2바이트 필드 예
        val filterTime_2byte = readTwoBytes()         // 또다른 2바이트 필드

        val weakWindFilterAlarm = readByte()
        val moderateWindFilterAlarm = readByte()
        val strongFilterWindFilterAlarm = readByte()
        val cycleOperateSetting = readByte()
        val cycleOperateSettingOnTime = readByte()
        val cycleOperateSettingOffTime = readByte()

        val weekDaysReserveSettingMode = readByte()
        val weekDaysSettingStartHour1 = readByte()
        val weekDaysSettingStartMin1 = readByte()
        val weekDaysSettingEndHour1 = readByte()
        val weekDaysSettingEndMin1 = readByte()
        val weekDaysSettingStartHour2 = readByte()
        val weekDaysSettingStartMin2 = readByte()
        val weekDaysSettingEndHour2 = readByte()
        val weekDaysSettingEndMin2 = readByte()
        val weekDaysSettingStartHour3 = readByte()
        val weekDaysSettingStartMin3 = readByte()
        val weekDaysSettingEndHour3 = readByte()
        val weekDaysSettingEndMin3 = readByte()

        val weekEndsReserveSettingMode = readByte()
        val weekEndsSettingStartHour1 = readByte()
        val weekEndsSettingStartMin1 = readByte()
        val weekEndsSettingEndHour1 = readByte()
        val weekEndsSettingEndMin1 = readByte()
        val weekEndsSettingStartHour2 = readByte()
        val weekEndsSettingStartMin2 = readByte()
        val weekEndsSettingEndHour2 = readByte()
        val weekEndsSettingEndMin2 = readByte()
        val weekEndsSettingStartHour3 = readByte()
        val weekEndsSettingStartMin3 = readByte()
        val weekEndsSettingEndHour3 = readByte()
        val weekEndsSettingEndMin3 = readByte()

        val dayReservation = readByte()
        val nowTimeYear = readByte()
        val nowTimeMonth = readByte()
        val nowTimeDay = readByte()
        val nowTimeHour = readByte()
        val nowTimeMin = readByte()
        val nowTimeSec = readByte()
        val nowTimeDays = readByte()
        val nowTemp = readByte()
        val nowHeaterTemp = readByte()
        val compTemp = readByte()
        val tempOption = readByte()
        val boardID = readByte()
        val optionState = readByte()
        val deviceStateInfo = readByte()
        val error1 = readByte()
        val error2 = readByte()
        val compCurrent = readByte()
        val version = readByte()

        // 남은 부분은 ModelName으로 처리 (가정)
        // offset부터 endIndex 까지가 ModelName 바이트라 가정
        val modelNameBytes = rawData.copyOfRange(offset, endIndex)
        val modelName = String(modelNameBytes, Charsets.US_ASCII)

        parsed = DataMap(
            Operation = operation,
            ColdSettingTemp = coldSettingTemp,
            HeatSettingTemp = heatSettingTemp,
            TempCorrection = tempCorrection,
            HeatSetting = heatSetting,
            SuperColdSetting = superColdSetting,
            WeakWindSpeedSet = weakWindSpeedSet,
            ModerateWindSpeedSet = moderateWindSpeedSet,
            StrongWindSpeedSet = strongWindSpeedSet,
            FanSpeedSet = fanSpeedSet,
            CompOnDelay = compOnDelay,
            FanOffDelay = fanOffDelay,
            FilterTimeSetting_2byte = filterTimeSetting_2byte,
            FilterTime_2byte = filterTime_2byte,
            WeakWindFilterAlarm = weakWindFilterAlarm,
            ModerateWindFilterAlarm = moderateWindFilterAlarm,
            StrongFilterWindFilterAlarm = strongFilterWindFilterAlarm,
            CycleOperateSetting = cycleOperateSetting,
            CycleOperateSettingOnTime = cycleOperateSettingOnTime,
            CycleOperateSettingOffTime = cycleOperateSettingOffTime,
            WeekDaysReserveSettingMode = weekDaysReserveSettingMode,
            WeekDaysSettingStartHour1 = weekDaysSettingStartHour1,
            WeekDaysSettingStartMin1 = weekDaysSettingStartMin1,
            WeekDaysSettingEndHour1 = weekDaysSettingEndHour1,
            WeekDaysSettingEndMin1 = weekDaysSettingEndMin1,
            WeekDaysSettingStartHour2 = weekDaysSettingStartHour2,
            WeekDaysSettingStartMin2 = weekDaysSettingStartMin2,
            WeekDaysSettingEndHour2 = weekDaysSettingEndHour2,
            WeekDaysSettingEndMin2 = weekDaysSettingEndMin2,
            WeekDaysSettingStartHour3 = weekDaysSettingStartHour3,
            WeekDaysSettingStartMin3 = weekDaysSettingStartMin3,
            WeekDaysSettingEndHour3 = weekDaysSettingEndHour3,
            WeekDaysSettingEndMin3 = weekDaysSettingEndMin3,
            WeekEndsReserveSettingMode = weekEndsReserveSettingMode,
            WeekEndsSettingStartHour1 = weekEndsSettingStartHour1,
            WeekEndsSettingStartMin1 = weekEndsSettingStartMin1,
            WeekEndsSettingEndHour1 = weekEndsSettingEndHour1,
            WeekEndsSettingEndMin1 = weekEndsSettingEndMin1,
            WeekEndsSettingStartHour2 = weekEndsSettingStartHour2,
            WeekEndsSettingStartMin2 = weekEndsSettingStartMin2,
            WeekEndsSettingEndHour2 = weekEndsSettingEndHour2,
            WeekEndsSettingEndMin2 = weekEndsSettingEndMin2,
            WeekEndsSettingStartHour3 = weekEndsSettingStartHour3,
            WeekEndsSettingStartMin3 = weekEndsSettingStartMin3,
            WeekEndsSettingEndHour3 = weekEndsSettingEndHour3,
            WeekEndsSettingEndMin3 = weekEndsSettingEndMin3,
            DayReservation = dayReservation,
            NowTimeYear = nowTimeYear,
            NowTimeMonth = nowTimeMonth,
            NowTimeDay = nowTimeDay,
            NowTimeHour = nowTimeHour,
            NowTimeMin = nowTimeMin,
            NowTimeSec = nowTimeSec,
            NowTimeDays = nowTimeDays,
            NowTemp = nowTemp,
            NowHeaterTemp = nowHeaterTemp,
            CompTemp = compTemp,
            TempOption = tempOption,
            BoardID = boardID,
            OptionState = optionState,
            DeviceStateInfo = deviceStateInfo,
            Error1 = error1,
            Error2 = error2,
            CompCurrent = compCurrent,
            Version = version,
            ModelName = modelName
        )
        return parsed
    }


}