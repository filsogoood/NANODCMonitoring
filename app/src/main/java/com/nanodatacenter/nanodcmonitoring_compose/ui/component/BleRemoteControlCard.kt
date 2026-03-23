package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nanodatacenter.nanodcmonitoring_compose.ble.*

// Colors
private val CardBg = Color(0xFF1F2937)
private val CardBgLight = Color(0xFF374151)
private val CoolingBlue = Color(0xFF2196F3)
private val HeatingOrange = Color(0xFFFF5722)
private val FanGreen = Color(0xFF4CAF50)
private val PowerOnBlue = Color(0xFF1565C0)
private val PowerOffGray = Color(0xFF424242)
private val ActiveGreen = Color(0xFF10B981)
private val ErrorRed = Color(0xFFEF4444)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFF9CA3AF)
private val FilterCyan = Color(0xFF00B0FF)

/**
 * BLE 리모컨 카드 - DANGSAN 데이터센터 최상단에 표시
 * 이미지 클릭 시 아래에 펼쳐지는 형태
 */
@Composable
fun BleRemoteControlCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bleManager = remember { BleManager.getInstance(context) }

    val connectionState by bleManager.connectionState.collectAsState()
    val scanResults by bleManager.scanResults.collectAsState()
    val isScanning by bleManager.isScanning.collectAsState()
    val deviceStatus by bleManager.deviceStatus.collectAsState()
    val connectedDeviceName by bleManager.connectedDeviceName.collectAsState()

    // Permission handling
    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        hasPermissions = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "공조기 리모컨",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                ConnectionBadge(connectionState, connectedDeviceName)
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                !hasPermissions -> {
                    Text(
                        text = "BLE 권한이 필요합니다.",
                        color = ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                connectionState == ConnectionState.DISCONNECTED -> {
                    ScanSection(
                        scanResults = scanResults,
                        isScanning = isScanning,
                        onStartScan = { bleManager.startScan() },
                        onStopScan = { bleManager.stopScan() },
                        onConnect = { bleManager.connect(it.device) }
                    )
                }

                connectionState == ConnectionState.CONNECTING ||
                connectionState == ConnectionState.CONNECTED ||
                connectionState == ConnectionState.DISCOVERING_SERVICES -> {
                    ConnectingSection()
                }

                connectionState == ConnectionState.READY -> {
                    RemoteControlSection(
                        deviceStatus = deviceStatus,
                        onPower = { on -> bleManager.sendCommand(JtProtocol.buildPowerCommand(on)) },
                        onMode = { mode -> bleManager.sendCommand(JtProtocol.buildModeCommand(mode)) },
                        onFanSpeed = { speed -> bleManager.sendCommand(JtProtocol.buildFanSpeedCommand(speed)) },
                        onColdTemp = { temp -> bleManager.sendCommand(JtProtocol.buildColdTempCommand(temp)) },
                        onHeatTemp = { temp -> bleManager.sendCommand(JtProtocol.buildHeatTempCommand(temp)) },
                        onTimeSet = { bleManager.sendCommand(JtProtocol.buildTimeSetCommand()) },
                        onFilterReset = { bleManager.sendCommand(JtProtocol.buildFilterResetCommand()) },
                        onDisconnect = { bleManager.disconnect() },
                        onSendWeekdayReservation = { settings -> bleManager.sendCommand(JtProtocol.buildWeekdayReservationCommand(settings)) },
                        onSendWeekendReservation = { settings -> bleManager.sendCommand(JtProtocol.buildWeekendReservationCommand(settings)) },
                        onSendDayReservation = { dayBits -> bleManager.sendCommand(JtProtocol.buildDayReservationCommand(dayBits)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionBadge(state: ConnectionState, deviceName: String?) {
    val (text, color) = when (state) {
        ConnectionState.DISCONNECTED -> "연결 안됨" to PowerOffGray
        ConnectionState.CONNECTING -> "연결 중..." to CoolingBlue
        ConnectionState.CONNECTED, ConnectionState.DISCOVERING_SERVICES -> "서비스 탐색..." to CoolingBlue
        ConnectionState.READY -> (deviceName ?: "연결됨") to ActiveGreen
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun ScanSection(
    scanResults: List<BleDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (BleDevice) -> Unit
) {
    Button(
        onClick = { if (isScanning) onStopScan() else onStartScan() },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) ErrorRed else CoolingBlue
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = if (isScanning) "스캔 중지" else "장비 스캔",
            color = Color.White
        )
    }

    if (scanResults.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            scanResults.forEach { device ->
                DeviceItem(device = device, onConnect = onConnect)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    } else if (isScanning) {
        Text(
            text = "JT 장비를 검색 중...",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun DeviceItem(device: BleDevice, onConnect: (BleDevice) -> Unit) {
    val rssiColor = when {
        device.rssi > -60 -> ActiveGreen
        device.rssi > -80 -> Color(0xFFF59E0B)
        else -> ErrorRed
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBgLight)
            .clickable { onConnect(device) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name ?: "Unknown",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = device.address,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${device.rssi} dBm",
                fontSize = 12.sp,
                color = rssiColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "연결",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CoolingBlue
            )
        }
    }
}

@Composable
private fun ConnectingSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = CoolingBlue,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "장비에 연결 중...", color = TextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun RemoteControlSection(
    deviceStatus: DeviceStatus?,
    onPower: (Boolean) -> Unit,
    onMode: (OperationMode) -> Unit,
    onFanSpeed: (FanSpeed) -> Unit,
    onColdTemp: (Int) -> Unit,
    onHeatTemp: (Int) -> Unit,
    onTimeSet: () -> Unit,
    onFilterReset: () -> Unit,
    onDisconnect: () -> Unit,
    onSendWeekdayReservation: (ReservationSettings) -> Unit,
    onSendWeekendReservation: (ReservationSettings) -> Unit,
    onSendDayReservation: (Int) -> Unit
) {
    val status = deviceStatus

    // Status info
    if (status != null) {
        StatusRow(status)
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Power button
    PowerButton(
        isPowerOn = status?.isPowerOn ?: false,
        mode = status?.mode,
        fanSpeed = status?.fanSpeed,
        onPower = onPower
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Temperature
    if (status != null) {
        TemperatureControl(status, onColdTemp, onHeatTemp)
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Mode buttons
    ModeButtons(currentMode = status?.mode, onMode = onMode)
    Spacer(modifier = Modifier.height(8.dp))

    // Fan speed buttons
    FanSpeedButtons(currentFanSpeed = status?.fanSpeed, onFanSpeed = onFanSpeed)
    Spacer(modifier = Modifier.height(12.dp))

    // Reservation section
    ReservationSection(
        deviceStatus = status,
        onSendWeekdayReservation = onSendWeekdayReservation,
        onSendWeekendReservation = onSendWeekendReservation,
        onSendDayReservation = onSendDayReservation
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Utility row
    UtilityRow(
        status = status,
        onTimeSet = onTimeSet,
        onFilterReset = onFilterReset,
        onDisconnect = onDisconnect
    )
}

@Composable
private fun StatusRow(status: DeviceStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBgLight)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Board #${status.boardId}", fontSize = 11.sp, color = TextSecondary)
            Text(text = "v${status.version}", fontSize = 11.sp, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = status.deviceTime, fontSize = 11.sp, color = TextSecondary)
            if (status.errors.isNotEmpty()) {
                Text(
                    text = status.errors.first(),
                    fontSize = 11.sp,
                    color = ErrorRed
                )
            }
            if (status.filterAlarm) {
                Text(text = "필터 교체 필요", fontSize = 11.sp, color = FilterCyan)
            }
        }
    }
}

@Composable
private fun PowerButton(
    isPowerOn: Boolean,
    mode: OperationMode?,
    fanSpeed: FanSpeed?,
    onPower: (Boolean) -> Unit
) {
    val bgColor = if (isPowerOn) {
        when (mode) {
            OperationMode.COOLING -> CoolingBlue
            OperationMode.HEATING -> HeatingOrange
            OperationMode.FAN_ONLY -> FanGreen
            null -> PowerOnBlue
        }
    } else {
        PowerOffGray
    }

    Button(
        onClick = { onPower(!isPowerOn) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isPowerOn) "ON" else "OFF",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (isPowerOn && mode != null) {
                Text(
                    text = "${mode.label} ${fanSpeed?.label ?: ""}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TemperatureControl(
    status: DeviceStatus,
    onColdTemp: (Int) -> Unit,
    onHeatTemp: (Int) -> Unit
) {
    val isCooling = status.mode == OperationMode.COOLING
    val isHeating = status.mode == OperationMode.HEATING
    val isFanOnly = status.mode == OperationMode.FAN_ONLY
    val currentSettingTemp = status.settingTemp ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBgLight)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current temperature
        Text(text = "현재 온도", fontSize = 12.sp, color = TextSecondary)
        Text(
            text = if (status.currentTemp != null) "${status.currentTemp}°C" else "--°C",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = when (status.mode) {
                OperationMode.COOLING -> CoolingBlue
                OperationMode.HEATING -> HeatingOrange
                OperationMode.FAN_ONLY -> FanGreen
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!isFanOnly) {
            // Setting temperature with +/- buttons
            Text(text = "설정 온도", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                TempButton(text = "-") {
                    val newTemp = (currentSettingTemp - 1).coerceIn(20, 30)
                    if (isCooling) onColdTemp(newTemp) else onHeatTemp(newTemp)
                }

                Text(
                    text = "${currentSettingTemp}°C",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                TempButton(text = "+") {
                    val newTemp = (currentSettingTemp + 1).coerceIn(20, 30)
                    if (isCooling) onColdTemp(newTemp) else onHeatTemp(newTemp)
                }
            }
        }

        // Cold / Heat setting temp display
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "냉방", fontSize = 11.sp, color = CoolingBlue)
                Text(
                    text = "${status.coldSettingTemp}°C",
                    fontSize = 14.sp,
                    color = if (isCooling) CoolingBlue else TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "난방", fontSize = 11.sp, color = HeatingOrange)
                Text(
                    text = "${status.heatSettingTemp}°C",
                    fontSize = 14.sp,
                    color = if (isHeating) HeatingOrange else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TempButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF4B5563))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ModeButtons(currentMode: OperationMode?, onMode: (OperationMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OperationMode.entries.forEach { mode ->
            val isActive = currentMode == mode
            val color = when (mode) {
                OperationMode.COOLING -> CoolingBlue
                OperationMode.HEATING -> HeatingOrange
                OperationMode.FAN_ONLY -> FanGreen
            }

            Button(
                onClick = { onMode(mode) },
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) color else CardBgLight
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = mode.label,
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FanSpeedButtons(currentFanSpeed: FanSpeed?, onFanSpeed: (FanSpeed) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FanSpeed.entries.forEach { speed ->
            val isActive = currentFanSpeed == speed

            OutlinedButton(
                onClick = { onFanSpeed(speed) },
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isActive) Color(0xFF4B5563) else Color.Transparent
                ),
                border = BorderStroke(
                    1.dp,
                    if (isActive) ActiveGreen else Color(0xFF4B5563)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = speed.label,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) ActiveGreen else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun UtilityRow(
    status: DeviceStatus?,
    onTimeSet: () -> Unit,
    onFilterReset: () -> Unit,
    onDisconnect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Time sync
        SmallActionButton(
            text = "시간 동기화",
            color = CoolingBlue,
            modifier = Modifier.weight(1f),
            onClick = onTimeSet
        )

        // Filter info
        SmallActionButton(
            text = if (status?.filterAlarm == true) "필터 리셋" else "필터 ${status?.filterTime ?: 0}h",
            color = if (status?.filterAlarm == true) FilterCyan else TextSecondary,
            modifier = Modifier.weight(1f),
            onClick = onFilterReset
        )

        // Disconnect
        SmallActionButton(
            text = "연결 해제",
            color = ErrorRed,
            modifier = Modifier.weight(1f),
            onClick = onDisconnect
        )
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            maxLines = 1
        )
    }
}

// =============================================
// 예약 설정 섹션
// =============================================

private val DayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
// dayReservationRaw bit: bit5=월, bit4=화, bit3=수, bit2=목, bit1=금, bit0=토, bit6=일
private val DayBitPositions = listOf(5, 4, 3, 2, 1, 0, 6)

@Composable
private fun ReservationSection(
    deviceStatus: DeviceStatus?,
    onSendWeekdayReservation: (ReservationSettings) -> Unit,
    onSendWeekendReservation: (ReservationSettings) -> Unit,
    onSendDayReservation: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 요일 선택 상태 (장비에서 읽은 값으로 초기화)
    var dayBits by remember(deviceStatus?.dayReservationRaw) {
        mutableIntStateOf(deviceStatus?.dayReservationRaw ?: 0)
    }

    // 평일 예약 상태
    var weekdaySettings by remember(deviceStatus?.weekdayReservation) {
        mutableStateOf(deviceStatus?.weekdayReservation ?: ReservationSettings())
    }

    // 주말 예약 상태
    var weekendSettings by remember(deviceStatus?.weekendReservation) {
        mutableStateOf(deviceStatus?.weekendReservation ?: ReservationSettings())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBgLight)
    ) {
        // Header - 클릭하면 펼침/접힘
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "예약 설정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (deviceStatus?.reservationActive == true) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ON", fontSize = 11.sp, color = ActiveGreen)
                }
            }
            Text(
                text = if (isExpanded) "▲" else "▼",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {

                // 요일 선택
                Text(text = "예약 요일", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                DaySelector(
                    dayBits = dayBits,
                    onDayToggle = { index ->
                        val bitPos = DayBitPositions[index]
                        dayBits = dayBits xor (1 shl bitPos)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        // 요일이 하나라도 선택되면 bit7(전체ON) 설정
                        val finalBits = if ((dayBits and 0x7F) != 0) dayBits or 0x80 else dayBits and 0x7F
                        onSendDayReservation(finalBits)
                    },
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoolingBlue),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "요일 전송", fontSize = 12.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 평일 예약
                ReservationCard(
                    title = "평일 예약",
                    settings = weekdaySettings,
                    onSettingsChange = { weekdaySettings = it },
                    onSend = { onSendWeekdayReservation(weekdaySettings) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 주말 예약
                ReservationCard(
                    title = "주말 예약",
                    settings = weekendSettings,
                    onSettingsChange = { weekendSettings = it },
                    onSend = { onSendWeekendReservation(weekendSettings) }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DaySelector(dayBits: Int, onDayToggle: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DayLabels.forEachIndexed { index, label ->
            val bitPos = DayBitPositions[index]
            val isSelected = (dayBits and (1 shl bitPos)) != 0
            val isWeekend = index >= 5

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) {
                            if (isWeekend) HeatingOrange else CoolingBlue
                        } else Color(0xFF4B5563)
                    )
                    .clickable { onDayToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ReservationCard(
    title: String,
    settings: ReservationSettings,
    onSettingsChange: (ReservationSettings) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBg)
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))

        // 모드 선택
        Text(text = "모드", fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OperationMode.entries.forEachIndexed { modeIndex, mode ->
                val isActive = settings.mode == modeIndex
                val color = when (mode) {
                    OperationMode.COOLING -> CoolingBlue
                    OperationMode.HEATING -> HeatingOrange
                    OperationMode.FAN_ONLY -> FanGreen
                }
                Button(
                    onClick = {
                        onSettingsChange(settings.copy(mode = modeIndex))
                    },
                    modifier = Modifier.weight(1f).height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) color else Color(0xFF4B5563)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = mode.label, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3개 타임 슬롯
        settings.slots.forEachIndexed { slotIndex, slot ->
            val isEnabled = settings.slotEnabled[slotIndex]
            TimeSlotRow(
                slotIndex = slotIndex,
                slot = slot,
                isEnabled = isEnabled,
                onEnabledToggle = {
                    val newEnabled = settings.slotEnabled.toMutableList()
                    newEnabled[slotIndex] = !isEnabled
                    onSettingsChange(settings.copy(slotEnabled = newEnabled))
                },
                onSlotChange = { newSlot ->
                    val newSlots = settings.slots.toMutableList()
                    newSlots[slotIndex] = newSlot
                    onSettingsChange(settings.copy(slots = newSlots))
                }
            )
            if (slotIndex < 2) Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 전송 버튼
        Button(
            onClick = onSend,
            modifier = Modifier.fillMaxWidth().height(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = "$title 전송", fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
private fun TimeSlotRow(
    slotIndex: Int,
    slot: ReservationSlot,
    isEnabled: Boolean,
    onEnabledToggle: () -> Unit,
    onSlotChange: (ReservationSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isEnabled) Color(0xFF374151) else Color(0xFF2D3748))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // 헤더: Switch + 예약 번호
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isEnabled,
                onCheckedChange = { onEnabledToggle() },
                modifier = Modifier.height(28.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ActiveGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = PowerOffGray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "예약 ${slotIndex + 1}",
                fontSize = 13.sp,
                fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                color = if (isEnabled) TextPrimary else TextSecondary
            )
        }

        // 시간 설정 (활성화 시에만 표시)
        if (isEnabled) {
            Spacer(modifier = Modifier.height(6.dp))
            TimeRow(
                label = "시작",
                hour = slot.startHour,
                minute = slot.startMin,
                onHourChange = { onSlotChange(slot.copy(startHour = it.coerceIn(0, 23))) },
                onMinuteChange = { onSlotChange(slot.copy(startMin = it.coerceIn(0, 59))) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            TimeRow(
                label = "종료",
                hour = slot.endHour,
                minute = slot.endMin,
                onHourChange = { onSlotChange(slot.copy(endHour = it.coerceIn(0, 23))) },
                onMinuteChange = { onSlotChange(slot.copy(endMin = it.coerceIn(0, 59))) }
            )
        }
    }
}

/**
 * 시간 조절 한 줄 - [label] [시−] HH [시+] : [분−] MM [분+]
 */
@Composable
private fun TimeRow(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.width(30.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // 시 조절
        TimeAdjustButton("−") { onHourChange(hour - 1) }
        Text(
            text = String.format("%02d", hour),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        TimeAdjustButton("+") { onHourChange(hour + 1) }

        Text(
            text = ":",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 분 조절
        TimeAdjustButton("−") { onMinuteChange(minute - 1) }
        Text(
            text = String.format("%02d", minute),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        TimeAdjustButton("+") { onMinuteChange(minute + 1) }
    }
}

@Composable
private fun TimeAdjustButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF4B5563))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
