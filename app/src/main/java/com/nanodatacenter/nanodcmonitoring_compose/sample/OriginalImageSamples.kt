package com.nanodatacenter.nanodcmonitoring_compose.sample

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.*
import com.nanodatacenter.nanodcmonitoring_compose.ui.theme.DataCenterTheme
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil

/**
 * 원본 크기 이미지 표시 방법들을 보여주는 샘플 컴포저블 모음
 * 다양한 디바이스와 화면 크기에서 이미지를 원본 크기로 표시하는 방법을 제시
 */

/**
 * 샘플 1: 기본 원본 크기 이미지 리스트
 */
@Composable
fun OriginalImageListSample(
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    // 원본 크기로 이미지를 표시하는 기본 방법
    MonitoringImageList(
        deviceType = deviceType,
        scaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
        showDescriptions = true,
        useFixedHeight = false // 높이를 고정하지 않아 원본 크기 유지
    )
}

/**
 * 샘플 2: 완전한 원본 크기 이미지 뷰어
 */
@Composable
fun FullOriginalImageListSample(
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    // 완전히 원본 크기로 이미지를 표시하는 방법
    OriginalSizeImageList(
        deviceType = deviceType,
        showDescriptions = true,
        onImageClick = { imageType ->
            // 이미지 클릭 시 처리 로직
            println("Clicked on: ${imageType.description}")
        }
    )
}

/**
 * 샘플 3: 전체 화면 원본 크기 모니터링
 */
@Composable
fun FullScreenOriginalSample(
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    // 전체 화면에서 원본 크기 이미지들을 연속으로 표시
    OriginalSizeDataCenterScreen(
        deviceType = deviceType
    )
}

/**
 * 샘플 4: 스케일링 모드 선택 가능한 이미지 리스트
 */
@Composable
fun ScaleModeSelectableSample(
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    var selectedScaleMode by remember { mutableStateOf(ImageScaleUtil.ScaleMode.ORIGINAL) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 스케일링 모드 선택 UI
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "이미지 스케일링 모드 선택",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedScaleMode = ImageScaleUtil.ScaleMode.ORIGINAL },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("원본 크기")
                    }
                    
                    Button(
                        onClick = { selectedScaleMode = ImageScaleUtil.ScaleMode.FIT_ASPECT_RATIO },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("비율 맞춤")
                    }
                    
                    Button(
                        onClick = { selectedScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("폭 맞춤")
                    }
                }
            }
        }
        
        // 선택된 스케일링 모드로 이미지 표시
        MonitoringImageList(
            deviceType = deviceType,
            modifier = Modifier.weight(1f),
            scaleMode = selectedScaleMode,
            showDescriptions = true,
            useFixedHeight = selectedScaleMode != ImageScaleUtil.ScaleMode.ORIGINAL
        )
    }
}

/**
 * 샘플 5: 하이브리드 모드 (기본 뷰 + 원본 크기 옵션)
 */
@Composable
fun HybridModeSample(
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    var showOriginalSize by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 모드 전환 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "원본 크기 보기:",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Switch(
                checked = showOriginalSize,
                onCheckedChange = { showOriginalSize = it }
            )
        }
        
        // 선택된 모드에 따라 다른 컴포넌트 표시
        if (showOriginalSize) {
            OriginalSizeImageList(
                deviceType = deviceType,
                modifier = Modifier.weight(1f),
                showDescriptions = true
            )
        } else {
            MonitoringImageList(
                deviceType = deviceType,
                modifier = Modifier.weight(1f),
                scaleMode = ImageScaleUtil.ScaleMode.FIT_ASPECT_RATIO,
                showDescriptions = true,
                useFixedHeight = true,
                fixedHeight = 200
            )
        }
    }
}

// 프리뷰들
@Preview(showBackground = true, name = "원본 크기 리스트")
@Composable
fun OriginalImageListPreview() {
    DataCenterTheme {
        OriginalImageListSample()
    }
}

@Preview(showBackground = true, name = "스케일 모드 선택")
@Composable
fun ScaleModeSelectablePreview() {
    DataCenterTheme {
        ScaleModeSelectableSample()
    }
}

@Preview(showBackground = true, name = "하이브리드 모드")
@Composable
fun HybridModePreview() {
    DataCenterTheme {
        HybridModeSample()
    }
}
