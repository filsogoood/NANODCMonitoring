package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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
                        onStartScan = {
                            if (!bleManager.isBluetoothEnabled) {
                                Toast.makeText(context, "블루투스를 켜주세요", Toast.LENGTH_SHORT).show()
                            } else {
                                bleManager.startScan()
                            }
                        },
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
                        onTimeSet = {
                            bleManager.sendCommand(JtProtocol.buildTimeSetCommand())
                            Toast.makeText(context, "시간 동기화 전송 완료", Toast.LENGTH_SHORT).show()
                        },
                        onFilterReset = {
                            bleManager.sendCommand(JtProtocol.buildFilterResetCommand())
                            Toast.makeText(context, "필터 리셋 전송 완료", Toast.LENGTH_SHORT).show()
                        },
                        onDisconnect = { bleManager.disconnect() },
                        onSendWeekdayReservation = { settings -> bleManager.sendCommand(JtProtocol.buildWeekdayReservationCommand(settings)) },
                        onSendWeekendReservation = { settings -> bleManager.sendCommand(JtProtocol.buildWeekendReservationCommand(settings)) },
                        onSendDayReservation = { dayBits -> bleManager.sendCommand(JtProtocol.buildDayReservationCommand(dayBits)) },
                        onSendFilterSetting = { settings ->
                            bleManager.sendCommand(JtProtocol.buildDeviceSettingCommand(settings))
                            Toast.makeText(context, "필터 설정 전송 완료", Toast.LENGTH_SHORT).show()
                        },
                        onSendCycleOperateSetting = { settings ->
                            bleManager.sendCommand(JtProtocol.buildDeviceSettingCommand(settings))
                            Toast.makeText(context, "주기운전 설정 전송 완료", Toast.LENGTH_SHORT).show()
                        },
                        onSendTempCorrection = { settings ->
                            bleManager.sendCommand(JtProtocol.buildDeviceSettingCommand(settings))
                            Toast.makeText(context, "온도보정 설정 전송 완료", Toast.LENGTH_SHORT).show()
                        }
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
    onSendDayReservation: (Int) -> Unit,
    onSendFilterSetting: (DeviceSettings) -> Unit,
    onSendCycleOperateSetting: (DeviceSettings) -> Unit,
    onSendTempCorrection: (DeviceSettings) -> Unit
) {
    val context = LocalContext.current
    val status = deviceStatus
    var isAdminMenuExpanded by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // 과열/과냉 경보
    if (status != null) {
        TemperatureAlarmBanner(status)
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

    // 연결 해제 + 관리자 버튼
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onDisconnect,
            modifier = Modifier.weight(1f).height(38.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "연결 해제", fontSize = 13.sp, color = ErrorRed)
        }

        Button(
            onClick = {
                if (isAdminMenuExpanded) {
                    isAdminMenuExpanded = false
                } else {
                    showPasswordDialog = true
                }
            },
            modifier = Modifier.weight(1f).height(38.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAdminMenuExpanded) AdminOrange else CardBgLight
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "관리자",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAdminMenuExpanded) Color.White else AdminOrange
            )
        }
    }

    // ─── 관리자 메뉴 (버튼 클릭 시 표시) ───
    if (isAdminMenuExpanded) {
        Spacer(modifier = Modifier.height(12.dp))
        AdminMenuContent(
            deviceStatus = status,
            onTimeSet = onTimeSet,
            onFilterReset = onFilterReset,
            onSendWeekdayReservation = onSendWeekdayReservation,
            onSendWeekendReservation = onSendWeekendReservation,
            onSendDayReservation = onSendDayReservation,
            onSendFilterSetting = onSendFilterSetting,
            onSendCycleOperateSetting = onSendCycleOperateSetting,
            onSendTempCorrection = onSendTempCorrection
        )
    }

    // 비밀번호 입력 다이얼로그
    if (showPasswordDialog) {
        AdminPasswordDialog(
            onSuccess = {
                showPasswordDialog = false
                isAdminMenuExpanded = true
                Toast.makeText(context, "관리자 모드 진입", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPasswordDialog = false }
        )
    }
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
            Text(text = "필터 ${status.filterTime}h / ${status.filterTimeSetting}h", fontSize = 11.sp, color = TextSecondary)
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
        OperationMode.entries.filter { it != OperationMode.HEATING }.forEach { mode ->
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

// =============================================
// 관리자 비밀번호 다이얼로그
// =============================================

private const val ADMIN_PASSWORD = "1004"
private val AdminOrange = Color(0xFFFF9800)

@Composable
private fun AdminPasswordDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "관리자 인증",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminOrange
                )

                Text(
                    text = "비밀번호를 입력해주세요",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("비밀번호", color = TextSecondary) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                    ),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("비밀번호가 틀렸습니다", color = ErrorRed, fontSize = 12.sp) }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AdminOrange,
                        focusedBorderColor = AdminOrange,
                        unfocusedBorderColor = CardBgLight,
                        errorBorderColor = ErrorRed
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "취소", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (password == ADMIN_PASSWORD) {
                                onSuccess()
                            } else {
                                isError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "확인", color = Color.White)
                    }
                }
            }
        }
    }
}

// =============================================
// 관리자 메뉴 컨텐츠
// =============================================

@Composable
private fun AdminMenuContent(
    deviceStatus: DeviceStatus?,
    onTimeSet: () -> Unit,
    onFilterReset: () -> Unit,
    onSendWeekdayReservation: (ReservationSettings) -> Unit,
    onSendWeekendReservation: (ReservationSettings) -> Unit,
    onSendDayReservation: (Int) -> Unit,
    onSendFilterSetting: (DeviceSettings) -> Unit,
    onSendCycleOperateSetting: (DeviceSettings) -> Unit,
    onSendTempCorrection: (DeviceSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 관리자 메뉴 제목
        Text(
            text = "관리자 메뉴",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AdminOrange
        )

        // 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AdminOrange.copy(alpha = 0.3f))
        )

        // 장비 상태 정보
        if (deviceStatus != null) {
            StatusRow(deviceStatus)
        }

        // 시간 동기화
        Button(
            onClick = onTimeSet,
            modifier = Modifier.fillMaxWidth().height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoolingBlue),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = "시간 동기화", fontSize = 13.sp, color = Color.White)
        }

        // 온도보정 설정
        TempCorrectionSection(
            deviceStatus = deviceStatus,
            onSendTempCorrection = onSendTempCorrection
        )

        // 예약 설정
        ReservationSection(
            deviceStatus = deviceStatus,
            onSendWeekdayReservation = onSendWeekdayReservation,
            onSendWeekendReservation = onSendWeekendReservation,
            onSendDayReservation = onSendDayReservation
        )

        // 필터 설정
        FilterSettingSection(
            deviceStatus = deviceStatus,
            onSendFilterSetting = onSendFilterSetting,
            onFilterReset = onFilterReset
        )

        // 주기운전 설정
        CycleOperateSection(
            deviceStatus = deviceStatus,
            onSendCycleOperateSetting = onSendCycleOperateSetting
        )
    }
}

// =============================================
// 온도보정 설정 섹션
// =============================================

private val TempCorrectionColor = Color(0xFF26A69A)

@Composable
private fun TempCorrectionSection(
    deviceStatus: DeviceStatus?,
    onSendTempCorrection: (DeviceSettings) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val currentSettings = deviceStatus?.deviceSettings
    var tempCorrection by remember(currentSettings?.tempCorrection) {
        mutableIntStateOf(currentSettings?.tempCorrection ?: 0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBgLight)
    ) {
        // Header
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
                    text = "온도보정 설정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${currentSettings?.tempCorrection ?: 0}",
                    fontSize = 12.sp,
                    color = TempCorrectionColor
                )
            }
            Text(
                text = if (isExpanded) "▲" else "▼",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {

                // 현재 보정값 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "현재 보정값", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = "${currentSettings?.tempCorrection ?: 0}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TempCorrectionColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 보정값 조절
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "설정값", fontSize = 13.sp, color = TextSecondary)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterAdjustButton("−10") { tempCorrection = (tempCorrection - 10).coerceIn(0, 255) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("−1") { tempCorrection = (tempCorrection - 1).coerceIn(0, 255) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$tempCorrection",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterAdjustButton("+1") { tempCorrection = (tempCorrection + 1).coerceIn(0, 255) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("+10") { tempCorrection = (tempCorrection + 10).coerceIn(0, 255) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 전송 버튼
                Button(
                    onClick = {
                        val settings = currentSettings?.copy(
                            tempCorrection = tempCorrection
                        ) ?: DeviceSettings(tempCorrection = tempCorrection)
                        onSendTempCorrection(settings)
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TempCorrectionColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "온도보정 전송", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
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

// =============================================
// 과열/과냉 경보 배너
// =============================================

@Composable
private fun TemperatureAlarmBanner(status: DeviceStatus) {
    val hasAlarm = status.overheatAlarm || status.overcoldAlarm || status.filterAlarm
    if (!hasAlarm) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        if (status.overheatAlarm) {
            AlarmRow(
                label = "과열 경보",
                detail = "콤프레셔 온도: ${status.compTemp}°C",
                bgColor = Color(0x33FF0000),
                textColor = ErrorRed
            )
        }
        if (status.overcoldAlarm) {
            if (status.overheatAlarm) Spacer(modifier = Modifier.height(4.dp))
            AlarmRow(
                label = "과냉 경보",
                detail = "현재 온도: ${status.currentTemp ?: "--"}°C",
                bgColor = Color(0x330066FF),
                textColor = CoolingBlue
            )
        }
        if (status.filterAlarm) {
            if (status.overheatAlarm || status.overcoldAlarm) Spacer(modifier = Modifier.height(4.dp))
            AlarmRow(
                label = "필터 교체 필요",
                detail = "${status.filterTime}h / ${status.filterTimeSetting}h",
                bgColor = Color(0x3300B0FF),
                textColor = FilterCyan
            )
        }
    }
}

@Composable
private fun AlarmRow(
    label: String,
    detail: String,
    bgColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "⚠ $label",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = detail,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.8f)
        )
    }
}

// =============================================
// 필터 설정 섹션
// =============================================

@Composable
private fun FilterSettingSection(
    deviceStatus: DeviceStatus?,
    onSendFilterSetting: (DeviceSettings) -> Unit,
    onFilterReset: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 필터 알람 시간 설정값 (장비에서 읽은 값으로 초기화)
    var filterTimeSetting by remember(deviceStatus?.filterTimeSetting) {
        mutableIntStateOf(deviceStatus?.filterTimeSetting ?: 6000)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBgLight)
    ) {
        // 헤더
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
                    text = "필터 설정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${deviceStatus?.filterTime ?: 0}h 사용",
                    fontSize = 12.sp,
                    color = if (deviceStatus?.filterAlarm == true) FilterCyan else TextSecondary
                )
            }
            Text(
                text = if (isExpanded) "▲" else "▼",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {

                // 현재 필터 사용 시간
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "사용 시간", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = "${deviceStatus?.filterTime ?: 0}h",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (deviceStatus?.filterAlarm == true) FilterCyan else TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 알람 설정 시간 조절
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "알람 시간", fontSize = 13.sp, color = TextSecondary)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterAdjustButton("−100") { filterTimeSetting = (filterTimeSetting - 100).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("−10") { filterTimeSetting = (filterTimeSetting - 10).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("−1") { filterTimeSetting = (filterTimeSetting - 1).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${filterTimeSetting}h",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterAdjustButton("+1") { filterTimeSetting = (filterTimeSetting + 1).coerceAtMost(60000) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("+10") { filterTimeSetting = (filterTimeSetting + 10).coerceAtMost(60000) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("+100") { filterTimeSetting = (filterTimeSetting + 100).coerceAtMost(60000) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 버튼 행
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 설정 전송
                    Button(
                        onClick = {
                            val settings = deviceStatus?.deviceSettings?.copy(
                                filterTimeSetting = filterTimeSetting
                            ) ?: DeviceSettings(filterTimeSetting = filterTimeSetting)
                            onSendFilterSetting(settings)
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoolingBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "알람 시간 전송", fontSize = 12.sp, color = Color.White)
                    }

                    // 필터 리셋
                    Button(
                        onClick = onFilterReset,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FilterCyan),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "필터 시간 리셋", fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FilterAdjustButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .widthIn(min = 36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF4B5563))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
    }
}

private val CycleYellow = Color(0xFFFFC107)

@Composable
private fun CycleOperateSection(
    deviceStatus: DeviceStatus?,
    onSendCycleOperateSetting: (DeviceSettings) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val currentSettings = deviceStatus?.deviceSettings

    var cycleEnabled by remember(currentSettings?.cycleOperateSetting) {
        mutableStateOf((currentSettings?.cycleOperateSetting ?: 0) != 0)
    }
    var onTime by remember(currentSettings?.cycleOperateSettingOnTime) {
        mutableIntStateOf(currentSettings?.cycleOperateSettingOnTime ?: 40)
    }
    var offTime by remember(currentSettings?.cycleOperateSettingOffTime) {
        mutableIntStateOf(currentSettings?.cycleOperateSettingOffTime ?: 10)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBgLight)
    ) {
        // Header
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
                    text = "주기운전 설정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if ((currentSettings?.cycleOperateSetting ?: 0) != 0) "ON" else "OFF",
                    fontSize = 12.sp,
                    color = if ((currentSettings?.cycleOperateSetting ?: 0) != 0) CycleYellow else TextSecondary
                )
            }
            Text(
                text = if (isExpanded) "▲" else "▼",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {

                // ON/OFF toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "주기운전", fontSize = 13.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (cycleEnabled) "ON" else "OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cycleEnabled) CycleYellow else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = cycleEnabled,
                            onCheckedChange = { cycleEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CycleYellow,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF4B5563)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ON time setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "가동 시간", fontSize = 13.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterAdjustButton("−10") { onTime = (onTime - 10).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("−1") { onTime = (onTime - 1).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${onTime}분",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterAdjustButton("+1") { onTime = (onTime + 1).coerceAtMost(255) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("+10") { onTime = (onTime + 10).coerceAtMost(255) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // OFF time setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardBg)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "정지 시간", fontSize = 13.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterAdjustButton("−10") { offTime = (offTime - 10).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("−1") { offTime = (offTime - 1).coerceAtLeast(1) }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${offTime}분",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterAdjustButton("+1") { offTime = (offTime + 1).coerceAtMost(255) }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterAdjustButton("+10") { offTime = (offTime + 10).coerceAtMost(255) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Send button
                Button(
                    onClick = {
                        val settings = currentSettings?.copy(
                            cycleOperateSetting = if (cycleEnabled) 1 else 0,
                            cycleOperateSettingOnTime = onTime,
                            cycleOperateSettingOffTime = offTime
                        ) ?: DeviceSettings(
                            cycleOperateSetting = if (cycleEnabled) 1 else 0,
                            cycleOperateSettingOnTime = onTime,
                            cycleOperateSettingOffTime = offTime
                        )
                        onSendCycleOperateSetting(settings)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CycleYellow),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "주기운전 설정 전송", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
