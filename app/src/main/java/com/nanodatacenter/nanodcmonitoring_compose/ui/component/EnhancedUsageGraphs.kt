package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage

/**
 * 확장된 사용률 그래프 섹션
 * 다양한 레이아웃 패턴으로 그래프를 표시합니다.
 */
@Composable
fun EnhancedUsageGraphSection(
    nodeUsage: NodeUsage,
    nodeIndex: Int,
    nodeName: String,
    showTitle: Boolean = true, // 제목 표시 여부
    modifier: Modifier = Modifier
) {
    val extendedData = nodeUsage.toExtendedUsageData()
    val layoutPattern = getLayoutPatternForNode(nodeIndex, nodeName)
    
    // Card 제거하여 중첩 방지 (상위에서 이미 Card 사용)
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 헤더 (조건부 표시)
        if (showTitle) {
            Text(
                text = "System Metrics",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = UsageMetrics.TEXT_ACCENT,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // 레이아웃 패턴에 따른 그래프 렌더링
        when (layoutPattern) {
            GraphLayoutPattern.GRID_2X2 -> Grid2x2Layout(extendedData)
            GraphLayoutPattern.VERTICAL_BARS -> VerticalBarsLayout(extendedData)
            GraphLayoutPattern.HORIZONTAL_BARS -> HorizontalBarsLayout(extendedData)
            GraphLayoutPattern.MIXED_LAYOUT -> MixedLayout(extendedData)
            GraphLayoutPattern.DASHBOARD -> DashboardLayout(extendedData)
            GraphLayoutPattern.STORAGE_FOCUSED -> StorageFocusedLayout(extendedData)
            GraphLayoutPattern.POST_WORKER_FOCUSED -> PostWorkerFocusedLayout(extendedData)
            GraphLayoutPattern.NAS_FOCUSED -> NasFocusedLayout(extendedData)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 2x2 그리드 레이아웃
 */
@Composable
private fun Grid2x2Layout(data: ExtendedUsageData) {
    // NULL 값만 필터링 (0%는 표시, Storage 제외)
    val mainMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null,
        if (!data.gpuUsage.percentage.isNaN()) data.gpuUsage else null
    )
    
    // Storage 메트릭 분리
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 메인 메트릭 그리드 (2개씩 배치)
        if (mainMetrics.isNotEmpty()) {
            mainMetrics.chunked(2).forEach { rowData ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowData.forEach { metricData ->
                        when (metricData.type) {
                            GraphType.CIRCULAR -> CircularProgressGraph(
                                data = metricData,
                                modifier = Modifier.weight(1f)
                            )
                            GraphType.BAR -> VerticalBarGraph(
                                data = metricData,
                                modifier = Modifier.weight(1f)
                            )
                            else -> CircularProgressGraph(
                                data = metricData,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    // 홀수 개일 경우 빈 공간 채우기
                    if (rowData.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // 추가 메트릭 (온도, SSD Health) - NULL 값 필터링 적용
        AdditionalMetricsRow(data)
        
        // Storage 항목을 하단에 가로 바 그래프로 표시 (제목 제거)
        if (storageMetrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            storageMetrics.forEach { storageMetric ->
                HorizontalBarGraph(
                    data = storageMetric,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 세로 바 그래프 레이아웃
 */
@Composable
private fun VerticalBarsLayout(data: ExtendedUsageData) {
    // NULL 값만 필터링하여 제외 (0%는 표시)
    val validVerticalMetrics = listOfNotNull(
        // CPU Usage는 항상 표시 (기본 메트릭)
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage.copy(type = GraphType.BAR) else null,
        // Memory Usage는 항상 표시 (기본 메트릭)  
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage.copy(type = GraphType.BAR) else null,
        // GPU Usage는 값이 유효할 때만 표시
        if (!data.gpuUsage.percentage.isNaN()) data.gpuUsage.copy(type = GraphType.BAR) else null,
        // SSD Health는 null이 아닐 때만 표시
        data.ssdHealth,
        // GPU VRAM은 값이 유효할 때만 표시
        if (data.cpuVram != null && !data.cpuVram.percentage.isNaN()) data.cpuVram.copy(type = GraphType.BAR) else null
    ).filter { it != null } // null이 아닌 값만 필터링
    
    // GPU 온도 메트릭 분리 (가로 바 그래프용)
    val gpuTempMetrics = listOfNotNull(
        if (data.gpuTemp != null && !data.gpuTemp.percentage.isNaN() && data.gpuTemp.value.isNotEmpty() && data.gpuTemp.value != "null") data.gpuTemp else null
    )
    
    // Storage 메트릭은 별도로 분리 (가로 바 그래프용)
    val storageMetrics = listOfNotNull(
        // Storage Usage는 값이 유효할 때만 표시
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 세로 바 그래프들 (Storage 제외)
        if (validVerticalMetrics.isNotEmpty()) {
            validVerticalMetrics.chunked(4).forEach { rowMetrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    rowMetrics.forEach { metricData ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            VerticalBarGraph(
                                data = metricData,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    // 4개보다 적은 경우 남은 공간 채우기
                    repeat(4 - rowMetrics.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // GPU 온도를 가로 바 그래프로 표시
        if (gpuTempMetrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            gpuTempMetrics.forEach { tempMetric ->
                HorizontalBarGraph(
                    data = tempMetric,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Storage 항목을 하단에 가로 바 그래프로 표시 (제목 제거)
        if (storageMetrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            storageMetrics.forEach { storageMetric ->
                HorizontalBarGraph(
                    data = storageMetric,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 가로 바 그래프 레이아웃
 */
@Composable
private fun HorizontalBarsLayout(data: ExtendedUsageData) {
    // NULL 값만 필터링 (0%는 표시, Storage 제외)
    val validMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null,
        if (!data.gpuUsage.percentage.isNaN()) data.gpuUsage else null,
        // GPU 온도를 가로 바 그래프로 표시
        if (data.gpuTemp != null && !data.gpuTemp.percentage.isNaN() && data.gpuTemp.value.isNotEmpty() && data.gpuTemp.value != "null") data.gpuTemp else null,
        data.ssdHealth?.takeIf { !it.percentage.isNaN() && it.value.isNotEmpty() && it.value != "null" },
        if (data.cpuVram != null && !data.cpuVram.percentage.isNaN()) data.cpuVram else null
    )
    
    // Storage 메트릭 분리
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 일반 메트릭들
        validMetrics.forEach { metricData ->
            HorizontalBarGraph(
                data = metricData,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Storage 항목을 하단에 분리 표시 (제목 제거)
        if (storageMetrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            storageMetrics.forEach { storageMetric ->
                HorizontalBarGraph(
                    data = storageMetric,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 혼합 레이아웃 (원형 + 바)
 */
@Composable
private fun MixedLayout(data: ExtendedUsageData) {
    // 상단 원형 그래프용 메트릭 필터링 (NULL만 제외)
    val circularMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null,
        if (data.cpuVram != null && !data.cpuVram.percentage.isNaN()) data.cpuVram else null
    )
    
    // 중단 바 그래프용 메트릭 필터링 (NULL만 제외, Storage 제외) - GPU 온도 포함
    val barMetrics = listOfNotNull(
        // GPU 온도를 가로 바 그래프로 표시
        if (data.gpuTemp != null && !data.gpuTemp.percentage.isNaN() && data.gpuTemp.value.isNotEmpty() && data.gpuTemp.value != "null") data.gpuTemp else null,
        data.ssdHealth?.takeIf { !it.percentage.isNaN() && it.value.isNotEmpty() && it.value != "null" }
    )
    
    // Storage 메트릭 분리
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 상단: 원형 그래프들
        if (circularMetrics.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                circularMetrics.forEach { metricData ->
                    CircularProgressGraph(
                        data = metricData,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 빈 공간 채우기
                repeat(3 - circularMetrics.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // 중단: 바 그래프들 (온도, SSD Health)
        barMetrics.forEach { metricData ->
            HorizontalBarGraph(
                data = metricData,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Storage 항목을 하단에 분리 표시 (제목 제거)
        if (storageMetrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            storageMetrics.forEach { storageMetric ->
                HorizontalBarGraph(
                    data = storageMetric,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 대시보드 레이아웃
 */
@Composable
private fun DashboardLayout(data: ExtendedUsageData) {
    // 상단 주요 메트릭 필터링 (NULL만 제외)
    val mainMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null
    )
    
    // 온도 메트릭을 가로 바 그래프로 표시 (게이지에서 변경)
    val tempMetrics = listOfNotNull(
        if (data.gpuTemp != null && !data.gpuTemp.percentage.isNaN() && data.gpuTemp.value.isNotEmpty() && data.gpuTemp.value != "null") data.gpuTemp else null
    )
    
    // Health 메트릭 필터링 (NULL만 제외)
    val healthMetrics = listOfNotNull(
        data.ssdHealth?.takeIf { !it.percentage.isNaN() && it.value.isNotEmpty() && it.value != "null" }
    )
    
    // Storage 메트릭 분리
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 상단: 주요 메트릭을 큰 원형 그래프로
        if (mainMetrics.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                mainMetrics.forEach { metricData ->
                    LargeCircularProgressGraph(
                        data = metricData,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 빈 공간 채우기
                repeat(2 - mainMetrics.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // 중단: 온도를 가로 바 그래프로 표시 (게이지에서 변경)
        if (tempMetrics.isNotEmpty()) {
            tempMetrics.forEach { tempData ->
                HorizontalBarGraph(
                    data = tempData,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Health 메트릭 표시
        healthMetrics.forEach { healthMetric ->
            HorizontalBarGraph(
                data = healthMetric,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Storage 항목을 하단에 분리 표시 (제목 제거)
        if (storageMetrics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            storageMetrics.forEach { storageMetric ->
                HorizontalBarGraph(
                    data = storageMetric,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Storage 중심 레이아웃 (BC02 Storage 노드 전용)
 * Storage 메트릭을 강조하고 다른 메트릭들은 간단하게 표시
 */
@Composable
private fun StorageFocusedLayout(data: ExtendedUsageData) {
    // Storage 메트릭을 최우선으로 표시
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    // 기본 시스템 메트릭 (간단하게 표시)
    val basicMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null
    )
    
    // Health 관련 메트릭
    val healthMetrics = listOfNotNull(
        data.ssdHealth?.takeIf { !it.percentage.isNaN() && it.value.isNotEmpty() && it.value != "null" }
    )
    
    // 온도 메트릭
    val tempMetrics = emptyList<UsageGraphData>()
        // CPU 온도는 BC02에서 사용하지 않음
        // if (!data.cpuTemp.percentage.isNaN() && data.cpuTemp.value.isNotEmpty() && data.cpuTemp.value != "null") data.cpuTemp else null
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 상단: Storage 메트릭을 크게 강조
        if (storageMetrics.isNotEmpty()) {
            Text(
                text = "Storage Status",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = UsageMetrics.TEXT_ACCENT,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            storageMetrics.forEach { storageMetric ->
                // Storage를 큰 가로 바 그래프로 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(UsageMetrics.CARD_BACKGROUND.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = storageMetric.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = UsageMetrics.TEXT_PRIMARY
                        )
                        Text(
                            text = if (storageMetric.value.isNotEmpty()) storageMetric.value else "${storageMetric.percentage.toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = storageMetric.color
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 큰 진행률 바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(UsageMetrics.BACKGROUND_DARK)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(storageMetric.percentage / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(storageMetric.color)
                        )
                    }
                }
            }
        }
        
        // 중단: 기본 시스템 메트릭을 작은 원형 그래프로
        if (basicMetrics.isNotEmpty()) {
            Text(
                text = "System Overview",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                basicMetrics.forEach { metricData ->
                    CircularProgressGraph(
                        data = metricData,
                        modifier = Modifier.weight(1f),
                        size = 60.dp
                    )
                }
                // 빈 공간 채우기
                repeat(2 - basicMetrics.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // 하단: Health 및 온도 메트릭
        val additionalMetrics = healthMetrics + tempMetrics
        if (additionalMetrics.isNotEmpty()) {
            Text(
                text = "Health Monitoring",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            additionalMetrics.forEach { metricData ->
                HorizontalBarGraph(
                    data = metricData,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true
                )
            }
        }
    }
}

/**
 * PostWorker 중심 레이아웃 (BC02 PostWorker 노드 전용)
 * CPU, GPU, Memory 등 작업 처리 성능 메트릭을 강조
 */
@Composable
private fun PostWorkerFocusedLayout(data: ExtendedUsageData) {
    // 주요 작업 처리 메트릭 (CPU, Memory, GPU)
    val performanceMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null,
        if (!data.gpuUsage.percentage.isNaN()) data.gpuUsage else null,
        if (data.cpuVram != null && !data.cpuVram.percentage.isNaN()) data.cpuVram else null
    )
    
    // 온도 모니터링
    val thermalMetrics = listOfNotNull(
        // CPU 온도는 BC02에서 사용하지 않음
        // if (!data.cpuTemp.percentage.isNaN() && data.cpuTemp.value.isNotEmpty() && data.cpuTemp.value != "null") data.cpuTemp else null,
        if (data.gpuTemp != null && !data.gpuTemp.percentage.isNaN() && data.gpuTemp.value.isNotEmpty() && data.gpuTemp.value != "null") data.gpuTemp else null
    )
    
    // Health 메트릭
    val healthMetrics = listOfNotNull(
        data.ssdHealth?.takeIf { !it.percentage.isNaN() && it.value.isNotEmpty() && it.value != "null" }
    )
    
    // Storage 메트릭 (간단하게 표시)
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 상단: 작업 처리 성능 메트릭을 큰 원형 그래프로 강조
        if (performanceMetrics.isNotEmpty()) {
            Text(
                text = "Processing Performance",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = UsageMetrics.TEXT_ACCENT,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 메트릭을 2개씩 배치하여 큰 원형 그래프로 표시
            performanceMetrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowMetrics.forEach { metricData ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LargeCircularProgressGraph(
                                data = metricData,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    // 홀수 개일 경우 빈 공간 채우기
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // 중단: 온도 모니터링을 게이지로 표시
        if (thermalMetrics.isNotEmpty()) {
            Text(
                text = "Thermal Monitoring",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                thermalMetrics.forEach { tempData ->
                    GaugeGraph(
                        data = tempData,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 빈 공간 채우기
                repeat(2 - thermalMetrics.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // 하단: Health 및 Storage 메트릭을 간단하게 표시
        val additionalMetrics = healthMetrics + storageMetrics
        if (additionalMetrics.isNotEmpty()) {
            Text(
                text = "System Health",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            additionalMetrics.forEach { metricData ->
                HorizontalBarGraph(
                    data = metricData,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true
                )
            }
        }
    }
}

/**
 * NAS 중심 레이아웃 (BC02 NAS 노드 전용)
 * 스토리지와 네트워크 관련 메트릭을 강조하며 안정적인 저장소 운영에 필요한 정보 제공
 */
@Composable
private fun NasFocusedLayout(data: ExtendedUsageData) {
    // Storage 메트릭을 최우선으로 표시 (NAS의 핵심 기능)
    val storageMetrics = listOfNotNull(
        if (!data.storageUsage.percentage.isNaN() || (data.storageUsage.value.isNotEmpty() && data.storageUsage.value != "null")) data.storageUsage else null
    )
    
    // 시스템 안정성 메트릭 (CPU, Memory - 안정적인 운영을 위해 필요)
    val stabilityMetrics = listOfNotNull(
        if (!data.cpuUsage.percentage.isNaN()) data.cpuUsage else null,
        if (!data.memoryUsage.percentage.isNaN()) data.memoryUsage else null
    )
    
    // Health & 온도 메트릭 (장기간 안정적인 운영을 위한 모니터링)
    val healthMetrics = listOfNotNull(
        data.ssdHealth?.takeIf { !it.percentage.isNaN() && it.value.isNotEmpty() && it.value != "null" },
        // CPU 온도는 BC02에서 사용하지 않음
        // if (!data.cpuTemp.percentage.isNaN() && data.cpuTemp.value.isNotEmpty() && data.cpuTemp.value != "null") data.cpuTemp else null
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 상단: Storage 상태를 가장 크게 강조 표시
        if (storageMetrics.isNotEmpty()) {
            Text(
                text = "Storage Status",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = UsageMetrics.TEXT_ACCENT,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            storageMetrics.forEach { storageMetric ->
                // Storage를 대형 가로 바 그래프로 표시 (가시성 극대화)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(UsageMetrics.CARD_BACKGROUND.copy(alpha = 0.5f))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = storageMetric.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = UsageMetrics.TEXT_PRIMARY
                            )
                            Text(
                                text = "Network Attached Storage",
                                fontSize = 12.sp,
                                color = UsageMetrics.TEXT_SECONDARY
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (storageMetric.value.isNotEmpty()) storageMetric.value else "${storageMetric.percentage.toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = storageMetric.color
                            )
                            Text(
                                text = "${storageMetric.percentage.toInt()}% Used",
                                fontSize = 12.sp,
                                color = UsageMetrics.TEXT_SECONDARY
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 대형 진행률 바 (높이 증가)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(UsageMetrics.BACKGROUND_DARK)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(storageMetric.percentage / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(storageMetric.color)
                        )
                    }
                }
            }
        }
        
        // 중단: 시스템 안정성 메트릭을 원형 그래프로 간결하게 표시
        if (stabilityMetrics.isNotEmpty()) {
            Text(
                text = "System Stability",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                stabilityMetrics.forEach { metricData ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressGraph(
                            data = metricData,
                            modifier = Modifier.fillMaxWidth(),
                            size = 80.dp
                        )
                    }
                }
                // 빈 공간 채우기
                repeat(2 - stabilityMetrics.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // 하단: Health & 온도 모니터링을 컴팩트한 바 그래프로 표시
        if (healthMetrics.isNotEmpty()) {
            Text(
                text = "Health Monitoring",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            healthMetrics.forEach { healthMetric ->
                HorizontalBarGraph(
                    data = healthMetric,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true
                )
            }
        }
        
        // 추가 정보 표시 (NAS 운영 상태)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(UsageMetrics.BACKGROUND_DARK.copy(alpha = 0.3f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Network Storage",
                fontSize = 11.sp,
                color = UsageMetrics.TEXT_SECONDARY
            )
            Text(
                text = "Active",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF10B981) // 초록색으로 활성 상태 표시
            )
        }
    }
}

/**
 * 추가 메트릭 행 (온도, SSD Health 등)
 */
@Composable
private fun AdditionalMetricsRow(data: ExtendedUsageData) {
    // GPU 온도는 가로 바 그래프로 표시
    val tempMetrics = listOfNotNull(
        if (data.gpuTemp != null && !data.gpuTemp.percentage.isNaN() && data.gpuTemp.value.isNotEmpty() && data.gpuTemp.value != "null") data.gpuTemp else null
    )
    
    // 원형 그래프로 표시할 메트릭들
    val circularMetrics = listOfNotNull(
        // SSD Health는 원형 그래프로 유지
        data.ssdHealth,
        // GPU VRAM은 값이 유효할 때만 표시
        if (data.cpuVram != null && !data.cpuVram.percentage.isNaN()) data.cpuVram else null
    )
    
    if (tempMetrics.isNotEmpty() || circularMetrics.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Additional Metrics",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // GPU 온도를 가로 바 그래프로 표시
            tempMetrics.forEach { tempMetric ->
                HorizontalBarGraph(
                    data = tempMetric,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true
                )
            }
            
            // 원형 그래프로 표시할 메트릭들 (2개씩 배치)
            if (circularMetrics.isNotEmpty()) {
                circularMetrics.chunked(2).forEach { rowData ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowData.forEach { metricData ->
                            CircularProgressGraph(
                                data = metricData,
                                modifier = Modifier.weight(1f),
                                size = 60.dp
                            )
                        }
                        // 홀수 개일 경우 빈 공간 채우기
                        if (rowData.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 원형 진행률 그래프 컴포넌트
 */
@Composable
fun CircularProgressGraph(
    data: UsageGraphData,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = data.percentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 그래프 제목
        Text(
            text = data.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = UsageMetrics.TEXT_SECONDARY,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 원형 진행률 그래프
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 6.dp.toPx()
                val radius = (this.size.minDimension - strokeWidth) / 2
                val center = Offset(this.size.width / 2, this.size.height / 2)
                
                // 배경 원
                drawCircle(
                    color = UsageMetrics.BACKGROUND_DARK,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
                
                // 진행률 호
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    color = data.color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2)
                )
            }
            
            // 퍼센트 텍스트
            Text(
                text = if (data.value.isNotEmpty()) data.value else "${data.percentage.toInt()}%",
                fontSize = if (size > 70.dp) 12.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                color = UsageMetrics.TEXT_PRIMARY,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 큰 원형 진행률 그래프 (대시보드용)
 */
@Composable
fun LargeCircularProgressGraph(
    data: UsageGraphData,
    modifier: Modifier = Modifier
) {
    CircularProgressGraph(
        data = data,
        modifier = modifier,
        size = 120.dp
    )
}

/**
 * 세로 바 그래프 컴포넌트
 */
@Composable
fun VerticalBarGraph(
    data: UsageGraphData,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 80.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = data.percentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 그래프 제목 - 높이 고정으로 일관성 유지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp), // 고정 높이로 일관성 유지
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        
        // 값 표시 - 높이 고정으로 일관성 유지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp), // 고정 높이로 일관성 유지
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (data.value.isNotEmpty()) data.value else "${data.percentage.toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = UsageMetrics.TEXT_PRIMARY,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 바 그래프 - 중앙에 정확히 위치
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp) // 약간 더 넓게 하여 시각적 안정성 증대
                    .height(height)
                    .clip(RoundedCornerShape(6.dp)) // 더 둥근 모서리
                    .background(UsageMetrics.BACKGROUND_DARK),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(data.color)
                )
            }
        }
    }
}

/**
 * 가로 바 그래프 컴포넌트
 */
@Composable
fun HorizontalBarGraph(
    data: UsageGraphData,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = data.percentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    val barHeight = if (compact) 16.dp else 20.dp
    val fontSize = if (compact) 10.sp else 12.sp
    
    Column(
        modifier = modifier
    ) {
        // 헤더 (이름과 값)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.name,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = UsageMetrics.TEXT_SECONDARY
            )
            
            // 값을 더 명확하게 표시
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // 메인 값 표시 (Storage의 경우 GB와 %를 모두 표시)
                Text(
                    text = if (data.value.isNotEmpty()) data.value else "${data.percentage.toInt()}%",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = data.color
                )
                
                // Storage의 경우 추가 정보 표시
                if (data.name.contains("Storage", ignoreCase = true) && data.percentage > 0f) {
                    Text(
                        text = "${data.percentage.toInt()}% used",
                        fontSize = (fontSize.value - 1).sp,
                        color = UsageMetrics.TEXT_SECONDARY
                    )
                }
                
                // 온도의 경우 상태 표시
                if (data.name.contains("Temperature", ignoreCase = true) && data.percentage > 0f) {
                    val tempStatus = when {
                        data.percentage < 30f -> "Cool"
                        data.percentage < 60f -> "Normal"
                        data.percentage < 80f -> "Warm"
                        else -> "Hot"
                    }
                    Text(
                        text = tempStatus,
                        fontSize = (fontSize.value - 1).sp,
                        color = when {
                            data.percentage < 30f -> Color(0xFF10B981) // 초록색
                            data.percentage < 60f -> Color(0xFF3B82F6) // 파란색
                            data.percentage < 80f -> Color(0xFFF59E0B) // 주황색
                            else -> Color(0xFFEF4444) // 빨간색
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(if (compact) 3.dp else 4.dp))
        
        // 바 그래프 - 그라데이션 효과 추가
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .background(UsageMetrics.BACKGROUND_DARK)
        ) {
            // 진행률 바 - 색상 강도 조절
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(barHeight / 2))
                    .background(
                        when {
                            data.name.contains("Temperature", ignoreCase = true) -> {
                                // 온도에 따른 색상 변화
                                when {
                                    data.percentage < 30f -> Color(0xFF10B981)
                                    data.percentage < 60f -> Color(0xFF3B82F6)
                                    data.percentage < 80f -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                            }
                            data.name.contains("Storage", ignoreCase = true) -> {
                                // Storage 사용률에 따른 색상 변화
                                when {
                                    data.percentage < 50f -> Color(0xFF10B981)
                                    data.percentage < 80f -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                            }
                            else -> data.color
                        }
                    )
            )
            
            // 진행률이 매우 낮을 때 최소 표시 바
            if (animatedProgress > 0f && animatedProgress < 0.05f) {
                Box(
                    modifier = Modifier
                        .width(barHeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(barHeight / 2))
                        .background(data.color.copy(alpha = 0.8f))
                )
            }
        }
    }
}

/**
 * 게이지 그래프 컴포넌트 (온도용)
 */
@Composable
fun GaugeGraph(
    data: UsageGraphData,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = data.percentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 그래프 제목
        Text(
            text = data.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = UsageMetrics.TEXT_SECONDARY,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 게이지 그래프
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawGauge(this, animatedProgress, data.color)
            }
            
            // 중앙 값 표시
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = data.value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UsageMetrics.TEXT_PRIMARY,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 게이지 그리기 함수
 */
private fun drawGauge(
    drawScope: DrawScope,
    progress: Float,
    color: Color
) {
    with(drawScope) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        
        // 배경 호 (반원)
        drawArc(
            color = UsageMetrics.BACKGROUND_DARK,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
        
        // 진행률 호 (반원)
        val sweepAngle = progress * 180f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
    }
}


