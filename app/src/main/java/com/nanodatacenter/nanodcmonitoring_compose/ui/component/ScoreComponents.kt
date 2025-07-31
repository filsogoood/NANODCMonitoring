package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * 확장 가능한 스코어 카드 컴포넌트 (다른 이미지들과 동일한 스타일)
 */
@Composable
fun ExpandedScoreCard(
    score: Score?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937) // 웹과 동일한 어두운 배경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        ScoreCardContent(score = score)
    }
}

/**
 * 스코어 카드 내용 (카드 내부)
 */
@Composable
private fun ScoreCardContent(
    score: Score?
) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        // 헤더
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
        
        // 점수 표시 부분
        ScoreDisplaySection(score = score)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 육각형 차트
        HexagonChart(score = score)
    }
}

/**
 * 스코어 정보를 표시하는 모달 다이얼로그
 */
@Composable
fun ScoreModal(
    score: Score?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1F2937), // 웹과 동일한 어두운 배경
            tonalElevation = 8.dp
        ) {
            ScoreContent(
                score = score,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * 스코어 콘텐츠 (모달 내부)
 */
@Composable
private fun ScoreContent(
    score: Score?,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
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
            
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 점수 표시 부분
        ScoreDisplaySection(score = score)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 육각형 차트
        HexagonChart(score = score)
    }
}

/**
 * 점수 표시 섹션 (웹 UI와 동일한 스타일)
 */
@Composable
private fun ScoreDisplaySection(score: Score?) {
    val averageScore = score?.averageScore?.toFloatOrNull() ?: 80f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827), // 웹과 동일한 더 어두운 배경
        border = androidx.compose.foundation.BorderStroke(
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
 * 육각형 차트 컴포넌트 (웹 UI와 동일한 스타일)
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
 * 메트릭 데이터 클래스 (내부 모듈에서 공유 가능)
 */
internal data class MetricData(
    val name: String,
    val value: Float,
    val color: Color
)

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
