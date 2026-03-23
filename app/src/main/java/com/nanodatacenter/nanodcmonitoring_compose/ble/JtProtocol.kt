package com.nanodatacenter.nanodcmonitoring_compose.ble

import android.util.Log
import java.util.Calendar

object JtProtocol {

    private const val TAG = "JtProtocol"
    private const val STX: Byte = 0xFA.toByte()
    private const val DEVICE_ID: Byte = 0x50
    private const val BOARD_ID: Byte = 0x01

    private const val CMD_STATUS_REQUEST: Byte = 0x01
    private const val CMD_POWER: Byte = 0x40
    private const val CMD_MODE: Byte = 0x41
    private const val CMD_FAN_SPEED: Byte = 0x42
    private const val CMD_COLD_TEMP: Byte = 0x43
    private const val CMD_HEAT_TEMP: Byte = 0x44
    private const val CMD_DEVICE_SETTING: Byte = 0x45
    private const val CMD_WEEKDAY_RESERVATION: Byte = 0x46
    private const val CMD_WEEKEND_RESERVATION: Byte = 0x47
    private const val CMD_DAY_RESERVATION: Byte = 0x48
    private const val CMD_TIME_SET: Byte = 0x49
    private const val CMD_FILTER_RESET: Byte = 0x4A.toByte()

    fun buildStatusRequest(): ByteArray = buildPacket(CMD_STATUS_REQUEST, byteArrayOf())

    fun buildPowerCommand(turnOn: Boolean): ByteArray {
        return buildPacket(CMD_POWER, byteArrayOf(if (turnOn) 0x01 else 0x00))
    }

    fun buildModeCommand(mode: OperationMode): ByteArray {
        val data = when (mode) {
            OperationMode.COOLING -> 0x00.toByte()
            OperationMode.HEATING -> 0x01.toByte()
            OperationMode.FAN_ONLY -> 0x02.toByte()
        }
        return buildPacket(CMD_MODE, byteArrayOf(data))
    }

    fun buildFanSpeedCommand(speed: FanSpeed): ByteArray {
        val data = when (speed) {
            FanSpeed.LOW -> 0x01.toByte()
            FanSpeed.MEDIUM -> 0x02.toByte()
            FanSpeed.HIGH -> 0x03.toByte()
        }
        return buildPacket(CMD_FAN_SPEED, byteArrayOf(data))
    }

    fun buildColdTempCommand(temp: Int): ByteArray {
        return buildPacket(CMD_COLD_TEMP, byteArrayOf(temp.coerceIn(20, 30).toByte()))
    }

    fun buildHeatTempCommand(temp: Int): ByteArray {
        return buildPacket(CMD_HEAT_TEMP, byteArrayOf(temp.coerceIn(20, 30).toByte()))
    }

    fun buildTimeSetCommand(): ByteArray {
        val cal = Calendar.getInstance()
        val year = (cal.get(Calendar.YEAR) % 100).toByte()
        val month = (cal.get(Calendar.MONTH) + 1).toByte()
        val day = cal.get(Calendar.DAY_OF_MONTH).toByte()
        val hour = cal.get(Calendar.HOUR_OF_DAY).toByte()
        val minute = cal.get(Calendar.MINUTE).toByte()
        val second = cal.get(Calendar.SECOND).toByte()
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7; Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3; Calendar.THURSDAY -> 4; Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6; else -> 1
        }.toByte()
        return buildPacket(CMD_TIME_SET, byteArrayOf(year, month, day, hour, minute, second, dayOfWeek))
    }

    fun buildFilterResetCommand(): ByteArray = buildPacket(CMD_FILTER_RESET, byteArrayOf())

    fun buildDeviceSettingCommand(settings: DeviceSettings): ByteArray {
        val filterHigh = ((settings.filterTimeSetting shr 8) and 0xFF).toByte()
        val filterLow = (settings.filterTimeSetting and 0xFF).toByte()
        val data = byteArrayOf(
            settings.tempCorrection.toByte(), settings.heatSetting.toByte(),
            settings.superColdSetting.toByte(), settings.weakWindSpeedSet.toByte(),
            settings.moderateWindSpeedSet.toByte(), settings.strongWindSpeedSet.toByte(),
            settings.fanSpeedSet.toByte(), settings.compOnDelay.toByte(),
            settings.fanOffDelay.toByte(), settings.weakWindFilterAlarm.toByte(),
            settings.moderateWindFilterAlarm.toByte(), settings.strongWindFilterAlarm.toByte(),
            filterHigh, filterLow, settings.cycleOperateSetting.toByte(),
            settings.cycleOperateSettingOnTime.toByte(), settings.cycleOperateSettingOffTime.toByte(),
            settings.optionState.toByte()
        )
        return buildPacket(CMD_DEVICE_SETTING, data)
    }

    fun buildWeekdayReservationCommand(settings: ReservationSettings): ByteArray =
        buildPacket(CMD_WEEKDAY_RESERVATION, settings.toBytes())

    fun buildWeekendReservationCommand(settings: ReservationSettings): ByteArray =
        buildPacket(CMD_WEEKEND_RESERVATION, settings.toBytes())

    fun buildDayReservationCommand(dayBits: Int): ByteArray =
        buildPacket(CMD_DAY_RESERVATION, byteArrayOf(dayBits.toByte()))

    private fun buildPacket(cmd: Byte, data: ByteArray): ByteArray {
        val len = data.size.toByte()
        val dataWithoutCheck = byteArrayOf(STX, DEVICE_ID, BOARD_ID, cmd, len) + data
        val xorValue = calculateXOR(dataWithoutCheck)
        val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
        val sumValue = calculateSUM(dataWithXOR)
        return dataWithXOR + byteArrayOf(sumValue)
    }

    private fun calculateXOR(data: ByteArray): Byte {
        var result: Byte = 0
        for (b in data) result = (result.toInt() xor b.toInt()).toByte()
        return result
    }

    private fun calculateSUM(data: ByteArray): Byte {
        var sum = 0
        for (b in data) sum = (sum + b.toUByte().toInt()) % 256
        return sum.toByte()
    }

    fun parseStatusResponse(rawData: ByteArray): DeviceStatus? {
        if (rawData.size < 7) return null
        if (rawData[3].toInt() and 0xFF != 0x01) return null

        var offset = 5
        val endIndex = rawData.size - 2

        fun readByte(): Int {
            if (offset >= endIndex) return 0
            val value = rawData[offset].toInt() and 0xFF
            offset += 1
            return value
        }

        fun readTwoBytes(): Int {
            if (offset + 1 >= endIndex) return 0
            val high = rawData[offset].toInt() and 0xFF
            val low = rawData[offset + 1].toInt() and 0xFF
            offset += 2
            return (high shl 8) or low
        }

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
        val filterTimeSetting = readTwoBytes()
        val filterTime = readTwoBytes()
        val weakWindFilterAlarm = readByte()
        val moderateWindFilterAlarm = readByte()
        val strongWindFilterAlarm = readByte()
        val cycleOperateSetting = readByte()
        val cycleOperateSettingOnTime = readByte()
        val cycleOperateSettingOffTime = readByte()

        val weekdayModeByte = readByte()
        val weekdaySlots = List(3) { ReservationSlot(readByte(), readByte(), readByte(), readByte()) }
        val weekdayReservation = ReservationSettings.fromByte(weekdayModeByte, weekdaySlots)

        val weekendModeByte = readByte()
        val weekendSlots = List(3) { ReservationSlot(readByte(), readByte(), readByte(), readByte()) }
        val weekendReservation = ReservationSettings.fromByte(weekendModeByte, weekendSlots)

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
        readByte() // tempOption
        val boardID = readByte()
        val optionState = readByte()
        readByte()
        val error1 = readByte()
        val error2 = readByte()
        readByte()
        val version = readByte()

        val powerStatus = (operation shr 6) and 0x03
        val modeValue = (operation shr 4) and 0x03
        val fanSpeedValue = operation and 0x0F

        val isPowerOn = powerStatus == 1 || powerStatus == 3
        val mode = when (modeValue) {
            0 -> OperationMode.COOLING; 1 -> OperationMode.HEATING; else -> OperationMode.FAN_ONLY
        }
        val fanSpeed = when (fanSpeedValue) {
            1 -> FanSpeed.LOW; 2 -> FanSpeed.MEDIUM; 3 -> FanSpeed.HIGH; else -> null
        }

        val currentTemp = if ((nowTemp and 0xFF) == 0xFF) null
        else {
            val isNegative = (nowTemp and 0x80) != 0
            val tempValue = nowTemp and 0x7F
            if (isNegative) -tempValue else tempValue
        }

        val settingTemp = when (mode) {
            OperationMode.COOLING -> coldSettingTemp
            OperationMode.HEATING -> heatSettingTemp
            OperationMode.FAN_ONLY -> null
        }

        val dayOfWeekStr = when (nowTimeDays) {
            1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; 7 -> "일"; else -> "?"
        }

        val deviceTime = String.format(
            "%02d-%02d-%02d (%s) %02d:%02d:%02d",
            nowTimeYear, nowTimeMonth, nowTimeDay, dayOfWeekStr,
            nowTimeHour, nowTimeMin, nowTimeSec
        )

        val errors = parseErrors(error1, error2)
        val filterAlarm = filterTime >= filterTimeSetting && filterTimeSetting > 0
        val reservationActive = (dayReservation and 0x7F) != 0

        // 과열/과냉 경보 판정
        val overheatAlarm = (error1 and 0x01) != 0  // Error1 bit0: 과열
        val overcoldAlarm = (error1 and 0x02) != 0  // Error1 bit1: 과냉

        return DeviceStatus(
            isPowerOn = isPowerOn, mode = mode, fanSpeed = fanSpeed,
            currentTemp = currentTemp, settingTemp = settingTemp,
            coldSettingTemp = coldSettingTemp, heatSettingTemp = heatSettingTemp,
            heaterTemp = nowHeaterTemp, compTemp = compTemp,
            overheatAlarm = overheatAlarm, overcoldAlarm = overcoldAlarm,
            deviceTime = deviceTime, boardId = boardID, version = version,
            errors = errors, filterAlarm = filterAlarm, filterTime = filterTime,
            filterTimeSetting = filterTimeSetting, reservationActive = reservationActive,
            fanSpeedRaw = fanSpeedValue, weekdayReservation = weekdayReservation,
            weekendReservation = weekendReservation, dayReservationRaw = dayReservation,
            deviceSettings = DeviceSettings(
                tempCorrection = tempCorrection, heatSetting = heatSetting,
                superColdSetting = superColdSetting, weakWindSpeedSet = weakWindSpeedSet,
                moderateWindSpeedSet = moderateWindSpeedSet, strongWindSpeedSet = strongWindSpeedSet,
                fanSpeedSet = fanSpeedSet, compOnDelay = compOnDelay, fanOffDelay = fanOffDelay,
                weakWindFilterAlarm = weakWindFilterAlarm, moderateWindFilterAlarm = moderateWindFilterAlarm,
                strongWindFilterAlarm = strongWindFilterAlarm, filterTimeSetting = filterTimeSetting,
                cycleOperateSetting = cycleOperateSetting, cycleOperateSettingOnTime = cycleOperateSettingOnTime,
                cycleOperateSettingOffTime = cycleOperateSettingOffTime, optionState = optionState
            )
        )
    }

    private fun parseErrors(error1: Int, error2: Int): List<String> {
        val errors = mutableListOf<String>()
        if ((error1 and 0x01) != 0) errors.add("과열 경보")
        if ((error1 and 0x02) != 0) errors.add("과냉 경보")
        if ((error1 and 0x04) != 0) errors.add("과전류 에러")
        if ((error1 and 0x08) != 0) errors.add("히터 온도센서 에러")
        if ((error1 and 0x10) != 0) errors.add("Comp 온도센서 에러")
        if ((error1 and 0x20) != 0) errors.add("온도센서 에러")
        if ((error1 and 0x40) != 0) errors.add("응축팬 에러")
        if ((error1 and 0x80) != 0) errors.add("급기팬 에러")
        if ((error2 and 0x01) != 0) errors.add("RTC 에러")
        if ((error2 and 0x02) != 0) errors.add("히터 에러")
        if ((error2 and 0x04) != 0) errors.add("COMP 에러")
        if ((error2 and 0x08) != 0) errors.add("RF 통신 에러")
        if ((error2 and 0x10) != 0) errors.add("BLE 통신 에러")
        return errors
    }
}

enum class OperationMode(val label: String) {
    COOLING("냉방"), HEATING("난방"), FAN_ONLY("송풍")
}

enum class FanSpeed(val label: String) {
    LOW("약풍"), MEDIUM("중풍"), HIGH("강풍")
}

data class ReservationSlot(
    val startHour: Int = 0, val startMin: Int = 0,
    val endHour: Int = 0, val endMin: Int = 0
)

data class ReservationSettings(
    val mode: Int = 0,
    val slotEnabled: List<Boolean> = listOf(false, false, false),
    val slots: List<ReservationSlot> = listOf(ReservationSlot(), ReservationSlot(), ReservationSlot())
) {
    fun toBytes(): ByteArray {
        val modeByte = (mode and 0x03) shl 6 or
                ((if (slotEnabled[0]) 1 else 0) shl 4) or
                ((if (slotEnabled[1]) 1 else 0) shl 2) or
                (if (slotEnabled[2]) 1 else 0)
        val data = ByteArray(13)
        data[0] = modeByte.toByte()
        for (i in 0..2) {
            val s = slots[i]
            data[1 + i * 4] = s.startHour.toByte()
            data[2 + i * 4] = s.startMin.toByte()
            data[3 + i * 4] = s.endHour.toByte()
            data[4 + i * 4] = s.endMin.toByte()
        }
        return data
    }

    companion object {
        fun fromByte(modeByte: Int, slots: List<ReservationSlot>): ReservationSettings {
            return ReservationSettings(
                mode = (modeByte shr 6) and 0x03,
                slotEnabled = listOf(
                    ((modeByte shr 4) and 0x03) > 0,
                    ((modeByte shr 2) and 0x03) > 0,
                    (modeByte and 0x03) > 0
                ),
                slots = slots
            )
        }
    }
}

data class DeviceStatus(
    val isPowerOn: Boolean, val mode: OperationMode, val fanSpeed: FanSpeed?,
    val currentTemp: Int?, val settingTemp: Int?,
    val coldSettingTemp: Int, val heatSettingTemp: Int,
    val heaterTemp: Int = 0, val compTemp: Int = 0,
    val overheatAlarm: Boolean = false, val overcoldAlarm: Boolean = false,
    val deviceTime: String, val boardId: Int, val version: Int,
    val errors: List<String>, val filterAlarm: Boolean,
    val filterTime: Int, val filterTimeSetting: Int,
    val reservationActive: Boolean, val fanSpeedRaw: Int,
    val weekdayReservation: ReservationSettings = ReservationSettings(),
    val weekendReservation: ReservationSettings = ReservationSettings(),
    val dayReservationRaw: Int = 0,
    val deviceSettings: DeviceSettings = DeviceSettings()
)

data class DeviceSettings(
    val tempCorrection: Int = 0, val heatSetting: Int = 100,
    val superColdSetting: Int = 5, val weakWindSpeedSet: Int = 50,
    val moderateWindSpeedSet: Int = 100, val strongWindSpeedSet: Int = 150,
    val fanSpeedSet: Int = 100, val compOnDelay: Int = 180,
    val fanOffDelay: Int = 120, val weakWindFilterAlarm: Int = 30,
    val moderateWindFilterAlarm: Int = 30, val strongWindFilterAlarm: Int = 30,
    val filterTimeSetting: Int = 6000, val cycleOperateSetting: Int = 0,
    val cycleOperateSettingOnTime: Int = 40, val cycleOperateSettingOffTime: Int = 10,
    val optionState: Int = 0
)
