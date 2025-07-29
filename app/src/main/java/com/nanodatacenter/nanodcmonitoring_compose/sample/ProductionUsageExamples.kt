package com.nanodatacenter.nanodcmonitoring_compose.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.ui.theme.NANODCMonitoring_ComposeTheme
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageConfigurationHelper

/**
 * 실제 프로덕션에서 사용하는 방법들을 보여주는 예시 코드들
 * 
 * 이 파일은 다음과 같은 사용 사례들을 다룹니다:
 * 1. 앱 시작 시 기기 타입 자동 감지 및 설정
 * 2. 런타임에 기기 타입 변경
 * 3. 커스텀 이미지 순서 동적 생성
 * 4. 설정 저장 및 복원
 */

/**
 * 1. 앱 시작 시 기기별 설정 초기화 예시
 */
object DeviceSetupExample {
    
    /**
     * 앱 시작 시 호출되는 초기화 함수
     * MainActivity.onCreate()에서 호출
     */
    fun initializeApp() {
        // 모든 기기별 설정을 로드
        ImageConfigurationHelper.applyAllConfigurations()
        
        // 기기 타입 자동 감지 (실제로는 하드웨어 정보나 설정 파일에서 읽어옴)
        val detectedDeviceType = detectDeviceType()
        
        // 감지된 기기 타입으로 설정
        val manager = ImageOrderManager.getInstance()
        manager.setCurrentDeviceType(detectedDeviceType)
        
        println("앱 초기화 완료 - 기기 타입: ${detectedDeviceType.displayName}")
    }
    
    /**
     * 기기 타입 자동 감지 로직 (예시)
     * 실제로는 시스템 정보, 설정 파일, 또는 네트워크 설정 등을 기반으로 판단
     */
    private fun detectDeviceType(): DeviceType {
        // 예시: 시스템 속성이나 설정 파일을 읽어서 기기 타입 결정
        val deviceId = getDeviceIdentifier() // 가상의 함수
        
        return when {
            deviceId.contains("RACK_A") -> DeviceType.DEVICE_A
            deviceId.contains("RACK_B") -> DeviceType.DEVICE_B
            deviceId.contains("RACK_C") -> DeviceType.DEVICE_C
            else -> DeviceType.DEFAULT
        }
    }
    
    private fun getDeviceIdentifier(): String {
        // 실제로는 시스템 정보나 설정을 읽어옴
        return "DEFAULT_DEVICE"
    }
}

/**
 * 2. 런타임에 기기 타입 변경 예시
 */
object RuntimeConfigurationExample {
    
    /**
     * 설정 화면에서 기기 타입을 변경할 때 사용
     */
    fun changeDeviceType(newDeviceType: DeviceType) {
        val manager = ImageOrderManager.getInstance()
        manager.setCurrentDeviceType(newDeviceType)
        
        // 변경사항을 로그로 출력
        println("기기 타입 변경됨: ${newDeviceType.displayName}")
        printCurrentConfiguration(newDeviceType)
    }
    
    /**
     * 현재 설정 정보를 출력 (디버깅용)
     */
    private fun printCurrentConfiguration(deviceType: DeviceType) {
        val configInfo = ImageConfigurationHelper.printCurrentOrder(deviceType)
        println(configInfo)
    }
}

/**
 * 3. 커스텀 이미지 순서 동적 생성 예시
 */
object CustomOrderExample {
    
    /**
     * 특정 상황에 따라 동적으로 이미지 순서를 변경하는 예시
     */
    fun createSituationalOrder() {
        // 예시 1: 긴급 상황 시 UPS와 스위치를 맨 앞으로
        val emergencyOrder = ImageConfigurationHelper.createConfigurationWithPriority(
            deviceType = DeviceType.DEFAULT,
            priorityImages = listOf(
                ImageType.UPS_CONTROLLER,
                ImageType.SWITCH_100G
            )
        )
        
        // 예시 2: 스토리지 관련 이미지만 제외
        val noStorageOrder = ImageConfigurationHelper.createConfigurationWithExclusions(
            deviceType = DeviceType.DEFAULT,
            excludeImages = listOf(
                ImageType.FILECOIN,
                ImageType.FILECOIN_NONE_1,
                ImageType.FILECOIN_NONE_2,
                ImageType.NOT_STORAGE
            )
        )
        
        // 예시 3: 완전 커스텀 순서
        val customOrder = listOf(
            ImageType.LOGO_ZETACUBE,
            ImageType.NDP_INFO,
            ImageType.UPS_CONTROLLER,
            ImageType.SWITCH_100G
        )
        val completelyCustom = ImageConfigurationHelper.createCustomConfiguration(
            deviceType = DeviceType.DEFAULT,
            imageOrder = customOrder
        )
        
        // 설정 적용 예시
        val manager = ImageOrderManager.getInstance()
        manager.addConfiguration(emergencyOrder)
        manager.addConfiguration(noStorageOrder)
        manager.addConfiguration(completelyCustom)
    }
}

/**
 * 4. 실제 사용 시나리오 시뮬레이션
 */
@Composable
fun ProductionUsageExample() {
    var currentDevice by remember { mutableStateOf(DeviceType.DEFAULT) }
    var statusMessage by remember { mutableStateOf("기본 설정") }
    
    // 앱 시작 시 초기화
    LaunchedEffect(Unit) {
        DeviceSetupExample.initializeApp()
        statusMessage = "앱 초기화 완료"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "실제 프로덕션 사용 예시",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "상태: $statusMessage",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // 기기 타입 변경 버튼들
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "기기 타입 변경",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeviceType.values().forEach { deviceType ->
                        Button(
                            onClick = {
                                currentDevice = deviceType
                                RuntimeConfigurationExample.changeDeviceType(deviceType)
                                statusMessage = "${deviceType.displayName}로 변경됨"
                            },
                            enabled = currentDevice != deviceType
                        ) {
                            Text(deviceType.displayName)
                        }
                    }
                }
            }
        }
        
        // 커스텀 순서 생성 예시
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "커스텀 순서 생성",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = {
                        CustomOrderExample.createSituationalOrder()
                        statusMessage = "커스텀 순서 설정 완료"
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("상황별 커스텀 순서 적용")
                }
            }
        }
        
        // 현재 이미지 개수 정보
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "현재 설정 정보",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val manager = ImageOrderManager.getInstance()
                val imageCount = manager.getTotalImageCount(currentDevice)
                val supportedDevices = manager.getSupportedDeviceTypes()
                
                Text(
                    text = "현재 기기: ${currentDevice.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "이미지 개수: ${imageCount}개",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "지원 기기: ${supportedDevices.size}개",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "프로덕션 사용 예시")
@Composable
fun ProductionUsagePreview() {
    NANODCMonitoring_ComposeTheme {
        ProductionUsageExample()
    }
}
