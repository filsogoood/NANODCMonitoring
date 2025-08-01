package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.R
import com.nanodatacenter.nanodcmonitoring_compose.config.DeviceConfigurationManager
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.network.model.*
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.MetricData
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.UsageGraphData
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.UsageMetrics
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * 노드 정보와 함께 이미지를 표시하는 컴포넌트
 * 노드 이름에 따라 적절한 이미지를 선택하여 표시합니다.
 */
@Composable
fun NodeImageWithInfo(
    node: Node,
    hardwareSpec: HardwareSpec?,
    score: Score?,
    nodeUsage: NodeUsage?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 노드 이름에 따라 이미지 타입 결정
    val imageType = when {
        node.nodeName.contains("Supra", ignoreCase = true) -> ImageType.SUPRA
        node.nodeName.contains("PostWorker", ignoreCase = true) -> ImageType.POSTWORKER
        node.nodeName.contains("Filecoin", ignoreCase = true) -> ImageType.FILECOIN
        else -> ImageType.NODE_INFO // 기본 이미지
    }
    
    Column(modifier = modifier) {
        // 이미지 표시 (클릭 가능)
        Image(
            painter = painterResource(id = imageType.drawableRes),
            contentDescription = node.nodeName,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            contentScale = ContentScale.FillWidth
        )
        
        // 확장 정보 카드
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            NodeInfoCard(
                node = node,
                hardwareSpec = hardwareSpec,
                score = score,
                nodeUsage = nodeUsage
            )
        }
    }
}

/**
 * 노드 정보를 표시하는 카드 컴포넌트
 * 각 정보를 개별 카드로 분리하여 표시합니다.
 */
@Composable
fun NodeInfoCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    score: Score?,
    nodeUsage: NodeUsage?,
    displayName: String? = null, // 커스텀 표시 이름
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 노드 이름 카드
        NodeNameCard(node = node, displayName = displayName)
        
        // 스코어 카드
        if (score != null) {
            NodeScoreCard(score = score)
        }
        
        // 하드웨어 스펙 카드
        if (hardwareSpec != null) {
            NodeHardwareSpecCard(hardwareSpec = hardwareSpec)
        }
        
        // 사용률 카드
        if (nodeUsage != null) {
            NodeUsageCard(nodeUsage = nodeUsage)
        }
    }
}

/**
 * 노드 이름을 표시하는 개별 카드
 */
@Composable
private fun NodeNameCard(
    node: Node,
    displayName: String? = null, // 커스텀 표시 이름
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName ?: node.nodeName, // displayName이 있으면 사용, 없으면 기본 이름
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 스코어를 표시하는 개별 카드 (ScoreComponents.kt와 동일한 UI 스타일)
 */
@Composable
private fun NodeScoreCard(
    score: Score,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더 (ScoreComponents.kt와 동일한 스타일)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .background(
                            Color(0xFF3B82F6),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Shield,
                    contentDescription = "Score",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 점수 표시 부분 (ScoreComponents.kt와 동일한 스타일)
            ScoreDisplaySection(score = score)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 육각형 차트
            HexagonChart(score = score)
        }
    }
}

/**
 * 하드웨어 스펙을 표시하는 개별 카드
 */
@Composable
private fun NodeHardwareSpecCard(
    hardwareSpec: HardwareSpec,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Hardware Specifications",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF60A5FA),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            HardwareSpecSection(hardwareSpec = hardwareSpec)
        }
    }
}

/**
 * 사용률을 표시하는 개별 카드
 */
@Composable
private fun NodeUsageCard(
    nodeUsage: NodeUsage,
    modifier: Modifier = Modifier
) {
    // Repository에서 실제 데이터 갱신 시간 가져오기
    val repository = remember { NanoDcRepository.getInstance() }
    val lastRefreshTime by repository.lastRefreshTime.collectAsState()
    val refreshTime = if (lastRefreshTime > 0) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastRefreshTime))
    } else {
        "00:00:00"
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 헤더와 시간 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Current Usage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Last Update: ${nodeUsage.timestamp}",
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = "Refreshed: $refreshTime",
                            fontSize = 10.sp,
                            color = Color(0xFF60A5FA)
                        )
                    }
                }
                
                // 그래프 섹션
                UsageGraphSection(nodeUsage = nodeUsage)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 상세 정보 섹션
                UsageSection(nodeUsage = nodeUsage)
            }
        }
    }
}

/**
 * 스코어 섹션 - 평균 스코어와 개별 스코어 표시
 */
@Composable
private fun ScoreSection(score: Score) {
    Column {
        // 평균 스코어 표시
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF111827),
            border = BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f", score.averageScore.toFloatOrNull() ?: 0f),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
            }
        }
        
        // 개별 스코어
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreItem("CPU", score.cpuScore, Color(0xFF3B82F6))
            ScoreItem("GPU", score.gpuScore, Color(0xFF8B5CF6))
            ScoreItem("RAM", score.ramScore, Color(0xFF06B6D4))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreItem("SSD", score.ssdScore, Color(0xFF10B981))
            ScoreItem("Network", score.networkScore, Color(0xFFF59E0B))
            ScoreItem("Health", score.hardwareHealthScore, Color(0xFFEF4444))
        }
    }
}

/**
 * 점수 표시 섹션 (ScoreComponents.kt와 동일한 스타일)
 */
@Composable
private fun ScoreDisplaySection(score: Score) {
    val averageScore = score.averageScore.toFloatOrNull() ?: 80f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827), // 웹과 동일한 더 어두운 배경
        border = BorderStroke(
            1.dp, 
            Color(0xFF374151).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%.2f", averageScore),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24) // 웹과 동일한 황색
            )
        }
    }
}

/**
 * 개별 스코어 섹션
 */
@Composable
private fun IndividualScoreSection(score: Score) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreItem("CPU", score.cpuScore, Color(0xFF3B82F6))
        ScoreItem("GPU", score.gpuScore, Color(0xFF8B5CF6))
        ScoreItem("RAM", score.ramScore, Color(0xFF06B6D4))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreItem("SSD", score.ssdScore, Color(0xFF10B981))
        ScoreItem("Network", score.networkScore, Color(0xFFF59E0B))
        ScoreItem("Health", score.hardwareHealthScore, Color(0xFFEF4444))
    }
}

/**
 * 육각형 차트 컴포넌트 (ScoreComponents.kt와 동일한 스타일)
 */
@Composable
private fun HexagonChart(score: Score?) {
    val metrics = extractMetrics(score)
    
    Box(
        modifier = Modifier
            .size(320.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
        ) {
            drawHexagonChart(this, metrics)
        }
        
        // 메트릭 라벨들
        HexagonLabels(metrics = metrics)
    }
}

/**
 * Score 객체에서 메트릭 데이터 추출
 */
private fun extractMetrics(score: Score?): List<MetricData> {
    return if (score != null) {
        listOf(
            MetricData("CPU", score.cpuScore.toFloatOrNull() ?: 80f, Color(0xFF3B82F6)),
            MetricData("GPU", score.gpuScore.toFloatOrNull() ?: 80f, Color(0xFF8B5CF6)),
            MetricData("RAM", score.ramScore.toFloatOrNull() ?: 80f, Color(0xFF06B6D4)),
            MetricData("STORAGE", score.ssdScore.toFloatOrNull() ?: 80f, Color(0xFF10B981)),
            MetricData("NETWORK", score.networkScore.toFloatOrNull() ?: 80f, Color(0xFFF59E0B)),
            MetricData("HEALTH", score.hardwareHealthScore.toFloatOrNull() ?: 80f, Color(0xFFEF4444))
        )
    } else {
        // 기본값 (모든 값 80점)
        listOf(
            MetricData("CPU", 80f, Color(0xFF3B82F6)),
            MetricData("GPU", 80f, Color(0xFF8B5CF6)),
            MetricData("RAM", 80f, Color(0xFF06B6D4)),
            MetricData("STORAGE", 80f, Color(0xFF10B981)),
            MetricData("NETWORK", 80f, Color(0xFFF59E0B)),
            MetricData("HEALTH", 80f, Color(0xFFEF4444))
        )
    }
}

/**
 * 육각형 차트 그리기 함수
 */
private fun drawHexagonChart(drawScope: DrawScope, metrics: List<MetricData>) {
    val center = Offset(drawScope.size.width / 2, drawScope.size.height / 2)
    val maxRadius = (drawScope.size.width.coerceAtMost(drawScope.size.height) / 2) * 0.8f
    
    // 육각형 격자선 그리기 (4개 레이어)
    val gridLayers = listOf(0.25f, 0.5f, 0.75f, 1.0f)
    gridLayers.forEach { scale ->
        val radius = maxRadius * scale
        drawHexagonGrid(drawScope, center, radius, Color(0xFF4A4A4A))
    }
    
    // 중심에서 각 꼭짓점으로 연결선 그리기
    for (i in 0 until 6) {
        val angle = (i * 60 - 90) * PI / 180.0
        val endPoint = Offset(
            center.x + (maxRadius * cos(angle)).toFloat(),
            center.y + (maxRadius * sin(angle)).toFloat()
        )
        drawScope.drawLine(
            color = Color(0xFF4A4A4A),
            start = center,
            end = endPoint,
            strokeWidth = 2f
        )
    }
    
    // 데이터 섹션 그리기
    drawDataSections(drawScope, center, maxRadius, metrics)
}

/**
 * 육각형 격자 그리기
 */
private fun drawHexagonGrid(drawScope: DrawScope, center: Offset, radius: Float, color: Color) {
    val path = Path()
    for (i in 0 until 6) {
        val angle = (i * 60 - 90) * PI / 180.0
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    
    drawScope.drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2f)
    )
}

/**
 * 데이터 섹션 그리기 (각 메트릭별로 색상 구분)
 */
private fun drawDataSections(
    drawScope: DrawScope, 
    center: Offset, 
    maxRadius: Float, 
    metrics: List<MetricData>
) {
    val layers = listOf(25f, 50f, 75f, 100f)
    
    metrics.forEachIndexed { sectionIndex, metric ->
        layers.forEach { layerThreshold ->
            if (metric.value >= layerThreshold) {
                val layerRadius = maxRadius * (layerThreshold / 100f)
                val innerRadius = if (layerThreshold > 25f) {
                    maxRadius * ((layerThreshold - 25f) / 100f)
                } else {
                    0f
                }
                
                drawSectionSegment(
                    drawScope, 
                    center, 
                    innerRadius, 
                    layerRadius, 
                    sectionIndex, 
                    metric.color,
                    0.8f - ((layerThreshold / 100f - 0.25f) * 0.15f)
                )
            }
        }
    }
}

/**
 * 섹션 세그먼트 그리기
 */
private fun drawSectionSegment(
    drawScope: DrawScope,
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    sectionIndex: Int,
    color: Color,
    alpha: Float
) {
    val path = Path()
    val startAngle = (sectionIndex * 60 - 90) * PI / 180.0
    val endAngle = ((sectionIndex + 1) * 60 - 90) * PI / 180.0
    
    if (innerRadius == 0f) {
        // 중심에서 시작하는 삼각형
        path.moveTo(center.x, center.y)
        path.lineTo(
            center.x + (outerRadius * cos(startAngle)).toFloat(),
            center.y + (outerRadius * sin(startAngle)).toFloat()
        )
        path.lineTo(
            center.x + (outerRadius * cos(endAngle)).toFloat(),
            center.y + (outerRadius * sin(endAngle)).toFloat()
        )
        path.close()
    } else {
        // 링 형태의 사다리꼴
        path.moveTo(
            center.x + (innerRadius * cos(startAngle)).toFloat(),
            center.y + (innerRadius * sin(startAngle)).toFloat()
        )
        path.lineTo(
            center.x + (outerRadius * cos(startAngle)).toFloat(),
            center.y + (outerRadius * sin(startAngle)).toFloat()
        )
        path.lineTo(
            center.x + (outerRadius * cos(endAngle)).toFloat(),
            center.y + (outerRadius * sin(endAngle)).toFloat()
        )
        path.lineTo(
            center.x + (innerRadius * cos(endAngle)).toFloat(),
            center.y + (innerRadius * sin(endAngle)).toFloat()
        )
        path.close()
    }
    
    drawScope.drawPath(
        path = path,
        color = color.copy(alpha = alpha),
        style = androidx.compose.ui.graphics.drawscope.Fill
    )
    
    // 경계선
    drawScope.drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 1f)
    )
}

/**
 * 육각형 라벨 컴포넌트
 */
@Composable
private fun HexagonLabels(metrics: List<MetricData>) {
    val labelPositions = listOf(
        Pair(0.7f, 0.15f),   // CPU (위)
        Pair(0.95f, 0.5f),   // GPU (오른쪽 위)
        Pair(0.75f, 0.85f),  // RAM (오른쪽 아래)
        Pair(0.25f, 0.85f),  // STORAGE (아래)
        Pair(0.05f, 0.5f),   // NETWORK (왼쪽 아래)
        Pair(0.25f, 0.15f),  // HEALTH (왼쪽 위)
    )
    
    metrics.forEachIndexed { index, metric ->
        val position = labelPositions[index]
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopStart)
                .offset(
                    x = (320.dp * position.first) - 40.dp,
                    y = (320.dp * position.second) - 20.dp
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = metric.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = String.format("%.2f", metric.value),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 개별 스코어 아이템
 */
@Composable
private fun ScoreItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * 하드웨어 스펙 섹션 - 하드웨어 정보 표시
 */
@Composable
private fun HardwareSpecSection(hardwareSpec: HardwareSpec) {
    Column {
        NodeInfoRow("CPU", "${hardwareSpec.cpuModel} (${hardwareSpec.cpuCores} cores)")
        NodeInfoRow("GPU", "${hardwareSpec.gpuModel} (${hardwareSpec.gpuVramGb}GB VRAM)")
        NodeInfoRow("RAM", "${hardwareSpec.totalRamGb}GB")
        NodeInfoRow("Storage", "${hardwareSpec.storageType} ${hardwareSpec.storageTotalGb}GB")
        NodeInfoRow("NVMe Count", hardwareSpec.nvmeCount)
    }
}

/**
 * 사용률 그래프 섹션 - CPU, Memory, GPU 사용률을 그래프로 표시
 */
@Composable
private fun UsageGraphSection(nodeUsage: NodeUsage) {
    val usageData = listOf(
        UsageGraphData(
            name = "CPU",
            percentage = nodeUsage.cpuUsagePercent.toFloatOrNull() ?: 0f,
            color = UsageMetrics.CPU_COLOR
        ),
        UsageGraphData(
            name = "Memory",
            percentage = nodeUsage.memUsagePercent.toFloatOrNull() ?: 0f,
            color = UsageMetrics.MEMORY_COLOR
        ),
        UsageGraphData(
            name = "GPU",
            percentage = nodeUsage.gpuUsagePercent?.toFloatOrNull() ?: 0f,
            color = UsageMetrics.GPU_COLOR
        ),
        UsageGraphData(
            name = "Storage",
            percentage = nodeUsage.harddiskUsedPercent?.toFloatOrNull() ?: 0f,
            color = UsageMetrics.STORAGE_COLOR
        )
    )
    
    Column {
        // 그래프 그리드
        usageData.chunked(2).forEach { rowData ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowData.forEach { data ->
                    UsageGraph(
                        data = data,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 홀수 개일 경우 빈 공간 채우기
                if (rowData.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 개별 사용률 그래프 컴포넌트
 */
@Composable
private fun UsageGraph(
    data: UsageGraphData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 그래프 제목
        Text(
            text = data.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 원형 진행률 그래프
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                
                // 배경 원
                drawCircle(
                    color = Color(0xFF374151),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
                
                // 진행률 호
                val sweepAngle = (data.percentage / 100f) * 360f
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
                    size = androidx.compose.ui.geometry.Size(
                        radius * 2,
                        radius * 2
                    )
                )
            }
            
            // 퍼센트 텍스트
            Text(
                text = "${data.percentage.toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 사용률 섹션 - 현재 사용률 정보 표시 (Last Update 제거)
 */
@Composable
private fun UsageSection(nodeUsage: NodeUsage) {
    Column {
        if (!nodeUsage.gpuUsagePercent.isNullOrEmpty()) {
            NodeInfoRow("GPU Temperature", "${nodeUsage.gpuTemp}°C")
            NodeInfoRow("GPU VRAM", "${nodeUsage.gpuVramPercent}%")
        }
        
        NodeInfoRow("Storage Used", "${nodeUsage.usedStorageGb}GB")
        NodeInfoRow("SSD Health", "${nodeUsage.ssdHealthPercent}%")
    }
}

/**
 * 노드 정보 행을 표시하는 재사용 가능한 컴포넌트
 */
@Composable
private fun NodeInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 모든 노드 이미지를 표시하는 화면
 * API 데이터를 기반으로 노드별 이미지와 정보를 표시합니다.
 */
@Composable
fun NodeBasedMonitoringScreen(
    modifier: Modifier = Modifier,
    nanoDcId: String? = null
) {
    val repository = remember { NanoDcRepository.getInstance() }
    val apiResponse by repository.apiResponseState.collectAsState()
    val isLoading by repository.isLoading.collectAsState()
    
    // 현재 nanoDcId 결정 (매개변수로 받거나 DeviceConfigurationManager에서 가져오기)
    val context = LocalContext.current
    val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }
    val currentNanoDcId = nanoDcId ?: deviceConfigManager.getSelectedDataCenter().nanoDcId
    
    // Repository가 아직 자동 갱신을 시작하지 않았다면 시작
    LaunchedEffect(currentNanoDcId) {
        // MainActivity에서 이미 시작했지만, 혹시 모를 상황을 대비한 안전장치
        if (repository.apiResponseState.value == null) {
            android.util.Log.d("NodeBasedMonitoringScreen", "🔄 Ensuring auto refresh is active...")
            repository.startAutoRefresh(currentNanoDcId)
        }
    }
    
    if (isLoading) {
        // 로딩 표시
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF60A5FA))
        }
    } else {
        apiResponse?.let { response ->
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                // 노드별로 이미지와 정보 표시
                items(response.nodes) { node ->
                    val hardwareSpec = response.hardwareSpecs.find { it.nodeId == node.nodeId }
                    val score = response.scores.find { it.nodeId == node.nodeId }
                    val nodeUsage = response.nodeUsage.find { it.nodeId == node.nodeId }
                    
                    NodeImageWithInfo(
                        node = node,
                        hardwareSpec = hardwareSpec,
                        score = score,
                        nodeUsage = nodeUsage,
                        modifier = Modifier.fillParentMaxWidth()
                    )
                }
            }
        }
    }
}
