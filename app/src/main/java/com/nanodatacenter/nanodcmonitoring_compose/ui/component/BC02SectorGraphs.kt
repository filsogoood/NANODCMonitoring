package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.network.model.HardwareSpec
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Node
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
import com.nanodatacenter.nanodcmonitoring_compose.util.BC02DataMapper
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Line
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BC02 섹터별 그래프 컴포넌트
 * 각 섹터(POST_WORKER, NODE_MINER, NAS)별로 다른 그래프 레이아웃을 제공
 * CPU 온도는 BC02에서 제공되지 않으므로 표시하지 않음
 */

/**
 * BC02 Post Worker 섹터 그래프
 * 라인 차트와 성능 메트릭을 표시
 */
@Composable
fun BC02PostWorkerSectorGraph(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?,
    displayName: String,
    lastRefreshTime: Long = 0, // Last update 시간 추가
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더 섹션 (Last update 포함)
            PostWorkerHeader(
                displayName = displayName,
                nodeUsage = nodeUsage,
                lastRefreshTime = lastRefreshTime
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 메인 메트릭 섹션 (라인 차트 형태)
            PostWorkerMetricsChart(
                hardwareSpec = hardwareSpec,
                nodeUsage = nodeUsage
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 성능 인디케이터
            PostWorkerPerformanceIndicators(
                nodeUsage = nodeUsage,
                score = score
            )
        }
    }
}

/**
 * BC02 Node Miner 섹터 그래프
 * 원형 차트와 마이닝 통계를 표시
 */
@Composable
fun BC02NodeMinerSectorGraph(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?,
    displayName: String,
    lastRefreshTime: Long = 0, // Last update 시간 추가
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더 섹션 (Last update 포함)
            NodeMinerHeader(
                displayName = displayName,
                nodeUsage = nodeUsage,
                lastRefreshTime = lastRefreshTime
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 메인 섹션 - 원형 차트와 마이닝 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 왼쪽: 리소스 사용량 원형 차트
                NodeMinerResourceChart(
                    hardwareSpec = hardwareSpec,
                    nodeUsage = nodeUsage,
                    modifier = Modifier.weight(1f)
                )
                
                // 오른쪽: 마이닝 통계
                NodeMinerStats(
                    hardwareSpec = hardwareSpec,
                    nodeUsage = nodeUsage,
                    score = score,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 하드웨어 상세 정보
            NodeMinerHardwareDetails(
                hardwareSpec = hardwareSpec,
                nodeUsage = nodeUsage
            )
        }
    }
}

/**
 * BC02 NAS 섹터 그래프
 * 세로 막대 차트와 스토리지 정보를 표시
 */
@Composable
fun BC02NASSectorGraph(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?,
    displayName: String,
    lastRefreshTime: Long = 0, // Last update 시간 추가
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더 섹션 (Last update 포함)
            NASHeader(
                displayName = displayName,
                nodeUsage = nodeUsage,
                lastRefreshTime = lastRefreshTime
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 메인 섹션 - 스토리지 사용량 세로 막대 차트
            NASStorageChart(
                hardwareSpec = hardwareSpec,
                nodeUsage = nodeUsage
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 스토리지 상세 정보
            NASStorageDetails(
                hardwareSpec = hardwareSpec,
                nodeUsage = nodeUsage,
                score = score
            )
        }
    }
}

// ===== Post Worker 섹션 컴포넌트들 =====

@Composable
private fun PostWorkerHeader(
    displayName: String,
    nodeUsage: NodeUsage?,
    lastRefreshTime: Long = 0
) {
    Column {
        // 첫 번째 줄: 아이콘과 제목
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Computer,
                contentDescription = "Post Worker",
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // 두 번째 줄: 시간 정보 (타이틀 아래)
        if (nodeUsage != null || lastRefreshTime > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            PostWorkerTimeDisplay(
                nodeUsage = nodeUsage,
                lastRefreshTime = lastRefreshTime
            )
        }
    }
}

/**
 * Post Worker용 시간 표시 컴포넌트 (두 줄로 표시)
 * "Last update : [timestamp]", "refreshed  : [time]" 형태
 */
@Composable
private fun PostWorkerTimeDisplay(
    nodeUsage: NodeUsage?,
    lastRefreshTime: Long
) {
    val refreshTime = if (lastRefreshTime > 0) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastRefreshTime))
    } else {
        "00:00:00"
    }
    
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Last update : ${nodeUsage?.timestamp ?: "N/A"}",
            fontSize = 10.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = "refreshed  : $refreshTime",
            fontSize = 10.sp,
            color = Color(0xFF60A5FA) // 파란색
        )
    }
}

/**
 * Node Miner용 시간 표시 컴포넌트 (두 줄로 표시)
 * "Last update : [timestamp]", "refreshed  : [time]" 형태
 */
@Composable
private fun NodeMinerTimeDisplay(
    nodeUsage: NodeUsage?,
    lastRefreshTime: Long
) {
    val refreshTime = if (lastRefreshTime > 0) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastRefreshTime))
    } else {
        "00:00:00"
    }
    
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Last update : ${nodeUsage?.timestamp ?: "N/A"}",
            fontSize = 10.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = "refreshed  : $refreshTime",
            fontSize = 10.sp,
            color = Color(0xFF60A5FA) // 파란색
        )
    }
}

/**
 * NAS용 시간 표시 컴포넌트 (두 줄로 표시)
 * "Last update : [timestamp]", "refreshed  : [time]" 형태
 */
@Composable
private fun NASTimeDisplay(
    nodeUsage: NodeUsage?,
    lastRefreshTime: Long
) {
    val refreshTime = if (lastRefreshTime > 0) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastRefreshTime))
    } else {
        "00:00:00"
    }
    
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Last update : ${nodeUsage?.timestamp ?: "N/A"}",
            fontSize = 10.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = "refreshed  : $refreshTime",
            fontSize = 10.sp,
            color = Color(0xFF60A5FA) // 파란색
        )
    }
}

/**
 * Last update 시간을 표시하는 컴포넌트
 */
@Composable
private fun LastUpdateDisplay(lastRefreshTime: Long) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        val formatter = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }
        
        Text(
            text = "Last Update",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formatter.format(java.util.Date(lastRefreshTime)),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF60A5FA) // 밝은 파란색
        )
    }
}

@Composable
private fun PostWorkerMetricsChart(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?
) {
    // CPU와 메모리 사용량을 가로형 막대 차트로 표시
    val cpuUsage = nodeUsage?.cpuUsagePercent?.toFloatOrNull() ?: 0f
    val memUsage = nodeUsage?.memUsagePercent?.toFloatOrNull() ?: 0f
    
    Column {
        Text(
            text = "System Performance Metrics",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 개선된 가로형 막대 차트 - CPU Usage
        HorizontalMetricBar(
            label = "CPU Usage",
            value = cpuUsage,
            maxValue = 100f,
            color = Color(0xFF3B82F6),
            backgroundColor = Color(0xFF374151),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 개선된 가로형 막대 차트 - Memory Usage
        HorizontalMetricBar(
            label = "Memory Usage",
            value = memUsage,
            maxValue = 100f,
            color = Color(0xFF10B981),
            backgroundColor = Color(0xFF374151),
            modifier = Modifier.fillMaxWidth()
        )
        

    }
}

/**
 * 향상된 가로형 메트릭 바 컴포넌트
 * 더 시각적으로 매력적이고 정보가 풍부한 가로형 막대 차트
 */
@Composable
private fun HorizontalMetricBar(
    label: String,
    value: Float,
    maxValue: Float,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value / maxValue,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000),
        label = "progress"
    )
    
    Column(
        modifier = modifier
    ) {
        // 라벨과 수치 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", value)}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 가로형 진행률 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
        ) {
            // 진행률 바
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color
                            )
                        )
                    )
            )
            
            // 가로 선 패턴 (시각적 효과)
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineCount = 3
                    val lineSpacing = size.height / (lineCount + 1)
                    
                    for (i in 1..lineCount) {
                        drawLine(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                            start = Offset(0f, lineSpacing * i),
                            end = Offset(size.width, lineSpacing * i),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
            
            // 퍼센트 텍스트 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (animatedProgress > 0.15f) { // 진행률이 충분할 때만 표시
                    Text(
                        text = "${String.format("%.0f", value)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun PostWorkerPerformanceIndicators(
    nodeUsage: NodeUsage?,
    score: Score?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CPU 사용량 인디케이터
        PerformanceIndicator(
            title = "CPU",
            value = "${nodeUsage?.cpuUsagePercent ?: "0"}%",
            color = Color(0xFF3B82F6),
            modifier = Modifier.weight(1f)
        )
        
        // 메모리 사용량 인디케이터
        PerformanceIndicator(
            title = "Memory",
            value = "${nodeUsage?.memUsagePercent ?: "0"}%",
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        
        // 스코어 인디케이터
        PerformanceIndicator(
            title = "Score",
            value = "${score?.averageScore?.toIntOrNull() ?: "N/A"}",
            color = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
    }
}

// ===== Node Miner 섹션 컴포넌트들 =====

@Composable
private fun NodeMinerHeader(
    displayName: String,
    nodeUsage: NodeUsage?,
    lastRefreshTime: Long = 0
) {
    Column {
        // 첫 번째 줄: 아이콘과 제목
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = "Node Miner",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // 두 번째 줄: 시간 정보 (타이틀 아래)
        if (nodeUsage != null || lastRefreshTime > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            NodeMinerTimeDisplay(
                nodeUsage = nodeUsage,
                lastRefreshTime = lastRefreshTime
            )
        }
    }
}

@Composable
private fun NodeMinerResourceChart(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resource Usage",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 원형 차트 (Canvas로 직접 구현)
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            val cpuUsage = nodeUsage?.cpuUsagePercent?.toFloatOrNull() ?: 0f
            val memUsage = nodeUsage?.memUsagePercent?.toFloatOrNull() ?: 0f
            val avgUsage = (cpuUsage + memUsage) / 2
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.8f
                
                // 배경 원
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFF374151),
                    radius = radius,
                    center = center,
                    style = Stroke(12.dp.toPx())
                )
                
                // 사용량 표시 원호
                val sweepAngle = (avgUsage / 100f) * 360f
                drawArc(
                    color = androidx.compose.ui.graphics.Color(0xFF8B5CF6),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(12.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
            
            // 중앙 텍스트
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${String.format("%.1f", (cpuUsage + memUsage) / 2)}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Avg Usage",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun NodeMinerStats(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Mining Statistics",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        MiningStatItem(
            label = "CPU Cores",
            value = "${hardwareSpec?.cpuCores ?: "N/A"}"
        )
        
        MiningStatItem(
            label = "Total RAM",
            value = "${hardwareSpec?.totalRamGb ?: "N/A"} GB"
        )
        
        MiningStatItem(
            label = "Storage",
            value = "${hardwareSpec?.storageTotalGb ?: "N/A"} GB"
        )
        
        score?.averageScore?.let { avgScore ->
            MiningStatItem(
                label = "Performance",
                value = "${avgScore.toIntOrNull() ?: "N/A"}/100"
            )
        }
    }
}

@Composable
private fun NodeMinerHardwareDetails(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?
) {
    Column {
        Text(
            text = "Hardware Details",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        hardwareSpec?.let { spec ->
            Text(
                text = "CPU: ${spec.cpuModel}",
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            spec.gpuModel?.takeIf { it != "N/A" }?.let { gpu ->
                Text(
                    text = "GPU: $gpu",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            // BC02는 CPU 온도를 제공하지 않으므로 표시하지 않음
            nodeUsage?.let { usage ->
                Text(
                    text = "Note: CPU temperature not available for BC02",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ===== NAS 섹션 컴포넌트들 =====

@Composable
private fun NASHeader(
    displayName: String,
    nodeUsage: NodeUsage?,
    lastRefreshTime: Long = 0
) {
    Column {
        // 첫 번째 줄: 아이콘과 제목
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Storage,
                contentDescription = "NAS Storage",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // 두 번째 줄: 시간 정보 (타이틀 아래)
        if (nodeUsage != null || lastRefreshTime > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            NASTimeDisplay(
                nodeUsage = nodeUsage,
                lastRefreshTime = lastRefreshTime
            )
        }
    }
}

@Composable
private fun NASStorageChart(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?
) {
    Column {
        Text(
            text = "Storage Usage Overview",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 세로 막대 차트를 위한 데이터
        val storageData = listOf(
            "SSD" to (nodeUsage?.ssdHealthPercent?.toFloatOrNull() ?: 100f),
            "HDD" to (nodeUsage?.harddiskUsedPercent?.toFloatOrNull() ?: 0f),
            "Memory" to (nodeUsage?.memUsagePercent?.toFloatOrNull() ?: 0f),
            "CPU" to (nodeUsage?.cpuUsagePercent?.toFloatOrNull() ?: 0f)
        )
        
        val chartData = storageData.map { (label, value) ->
            Bars(
                label = label,
                values = listOf(
                    Bars.Data(
                        value = value.toDouble(),
                        color = SolidColor(
                            when (label) {
                                "SSD" -> Color(0xFF3B82F6)
                                "HDD" -> Color(0xFF10B981)
                                "Memory" -> Color(0xFFF59E0B)
                                "CPU" -> Color(0xFF8B5CF6)
                                else -> Color(0xFF6B7280)
                            }
                        )
                    )
                )
            )
        }
        
        ColumnChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            data = chartData,
            barProperties = BarProperties(
                spacing = 8.dp,
                thickness = 20.dp
            )
        )
        
        // 범례
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            storageData.forEach { (label, _) ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NASStorageDetails(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Storage Details",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 총 용량
            StorageDetailCard(
                title = "Total Capacity",
                value = "${hardwareSpec?.totalHarddiskGb ?: "N/A"} GB",
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            
            // 사용량
            StorageDetailCard(
                title = "Used Space",
                value = "${nodeUsage?.harddiskUsedPercent ?: "0"}%",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            
            // SSD 상태
            StorageDetailCard(
                title = "SSD Health",
                value = "${nodeUsage?.ssdHealthPercent ?: "N/A"}%",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ===== 공통 컴포넌트들 =====

/**
 * 사용량 수치 표시 컴포넌트
 */
@Composable
private fun UsageValueDisplay(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PerformanceIndicator(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF374151)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MiningStatItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun StorageDetailCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF374151)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
