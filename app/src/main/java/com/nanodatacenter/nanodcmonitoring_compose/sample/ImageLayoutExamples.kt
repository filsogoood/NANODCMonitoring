package com.nanodatacenter.nanodcmonitoring_compose.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.*
import com.nanodatacenter.nanodcmonitoring_compose.ui.theme.NANODCMonitoring_ComposeTheme
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageConfigurationHelper

/**
 * 다양한 이미지 레이아웃 옵션들을 보여주는 예시 화면
 * 실제 프로덕션에서는 필요한 레이아웃만 선택해서 사용하세요.
 */
@Composable
fun ImageLayoutExamplesScreen(modifier: Modifier = Modifier) {
    var currentDeviceType by remember { mutableStateOf(DeviceType.DEFAULT) }
    
    LaunchedEffect(Unit) {
        // 초기 설정 적용
        ImageConfigurationHelper.applyAllConfigurations()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 기기 타입 선택
        DeviceTypeSelector(
            currentDeviceType = currentDeviceType,
            onDeviceTypeChange = { newType ->
                currentDeviceType = newType
                ImageOrderManager.getInstance().setCurrentDeviceType(newType)
            }
        )
        
        // 세로 목록 예시
        LayoutExampleSection(
            title = "세로 목록 (기본 사용법)",
            description = "가장 기본적인 형태로, 전체 화면을 채우며 이미지들을 세로로 나열합니다."
        ) {
            MonitoringImageList(
                deviceType = currentDeviceType,
                modifier = Modifier.height(400.dp), // 프리뷰용 높이 제한
                showDescriptions = false
            )
        }
        
        // 가로 스크롤 예시
        LayoutExampleSection(
            title = "가로 스크롤",
            description = "화면 상단이나 특정 섹션에서 가로로 스크롤하며 이미지를 보여줍니다."
        ) {
            MonitoringImageRow(
                deviceType = currentDeviceType,
                itemWidth = 250,
                showDescriptions = false
            )
        }
        
        // 그리드 예시
        LayoutExampleSection(
            title = "그리드 (2열)",
            description = "제한된 공간에서 여러 이미지를 효율적으로 보여주는 격자 형태입니다."
        ) {
            MonitoringImageGrid(
                deviceType = currentDeviceType,
                modifier = Modifier.height(600.dp), // 프리뷰용 높이 제한
                columns = 2,
                showDescriptions = false
            )
        }
        
        // 설명 포함 예시
        LayoutExampleSection(
            title = "설명 포함 세로 목록",
            description = "각 이미지 하단에 설명 텍스트가 포함된 형태입니다."
        ) {
            MonitoringImageList(
                deviceType = currentDeviceType,
                modifier = Modifier.height(400.dp), // 프리뷰용 높이 제한
                showDescriptions = true
            )
        }
    }
}

/**
 * 기기 타입 선택 UI
 */
@Composable
private fun DeviceTypeSelector(
    currentDeviceType: DeviceType,
    onDeviceTypeChange: (DeviceType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "기기 타입 선택",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceType.values().forEach { deviceType ->
                    FilterChip(
                        onClick = { onDeviceTypeChange(deviceType) },
                        label = { Text(deviceType.displayName) },
                        selected = currentDeviceType == deviceType
                    )
                }
            }
            
            Text(
                text = "현재 선택: ${currentDeviceType.displayName}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * 레이아웃 예시 섹션
 */
@Composable
private fun LayoutExampleSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            content()
        }
    }
}

@Preview(showBackground = true, name = "이미지 레이아웃 예시들")
@Composable
fun ImageLayoutExamplesPreview() {
    NANODCMonitoring_ComposeTheme {
        ImageLayoutExamplesScreen()
    }
}
