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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
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
    // 디버깅 로그 추가
    android.util.Log.d("BC02PostWorkerSectorGraph", "🎯 PostWorker Debug Info:")
    android.util.Log.d("BC02PostWorkerSectorGraph", "   Node: ${node.nodeName}")
    android.util.Log.d("BC02PostWorkerSectorGraph", "   DisplayName: $displayName")
    android.util.Log.d("BC02PostWorkerSectorGraph", "   Score: ${score?.averageScore ?: "NULL"}")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 타이틀 카드 (Last update 제거)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                PostWorkerHeader(displayName = displayName)
            }
        }
        
        // 2. 스코어 카드
        if (score != null) {
            ExpandedScoreCard(score = score)
        }
        
        // 3. 하드웨어 스펙 카드
        if (hardwareSpec != null) {
            BC02HardwareSpecCard(hardwareSpec = hardwareSpec)
        }
        
        // 4. 사용량 카드 (Last update를 오른쪽 구석에 포함)
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
                // 사용량 헤더 (Last update 포함)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Usage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA)
                    )
                    
                    // 오른쪽 구석에 Last update 정보
                    PostWorkerTimeDisplay(
                        nodeUsage = nodeUsage,
                        lastRefreshTime = lastRefreshTime
                    )
                }
                
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
    // 디버깅 로그 추가
    android.util.Log.d("BC02NodeMinerSectorGraph", "🎯 NodeMiner Debug Info:")
    android.util.Log.d("BC02NodeMinerSectorGraph", "   Node: ${node.nodeName}")
    android.util.Log.d("BC02NodeMinerSectorGraph", "   DisplayName: $displayName")
    android.util.Log.d("BC02NodeMinerSectorGraph", "   Score: ${score?.averageScore ?: "NULL"}")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 타이틀 카드 (Last update 제거)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                NodeMinerHeader(displayName = displayName)
            }
        }
        
        // 2. 스코어 카드
        if (score != null) {
            ExpandedScoreCard(score = score)
        }
        
        // 3. 하드웨어 스펙 카드
        if (hardwareSpec != null) {
            BC02HardwareSpecCard(hardwareSpec = hardwareSpec)
        }
        
        // 4. 사용량 카드 (Last update를 오른쪽 구석에 포함)
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
                // 사용량 헤더 (Last update 포함)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Usage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA)
                    )
                    
                    // 오른쪽 구석에 Last update 정보
                    NodeMinerTimeDisplay(
                        nodeUsage = nodeUsage,
                        lastRefreshTime = lastRefreshTime
                    )
                }
                
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
            }
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
    // 디버깅 로그 추가
    android.util.Log.d("BC02NASSectorGraph", "🎯 NAS Debug Info:")
    android.util.Log.d("BC02NASSectorGraph", "   Node: ${node.nodeName}")
    android.util.Log.d("BC02NASSectorGraph", "   DisplayName: $displayName")
    android.util.Log.d("BC02NASSectorGraph", "   Score: ${score?.averageScore ?: "NULL"}")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 타이틀 카드 (Last update 제거)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                NASHeader(displayName = displayName)
            }
        }
        
        // 2. 스코어 카드
        if (score != null) {
            ExpandedScoreCard(score = score)
        }
        
        // 3. 하드웨어 스펙 카드
        if (hardwareSpec != null) {
            BC02HardwareSpecCard(hardwareSpec = hardwareSpec)
        }
        
        // 4. 사용량 카드 (Last update를 오른쪽 구석에 포함)
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
                // 사용량 헤더 (Last update 포함)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Usage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA)
                    )
                    
                    // 오른쪽 구석에 Last update 정보
                    NASTimeDisplay(
                        nodeUsage = nodeUsage,
                        lastRefreshTime = lastRefreshTime
                    )
                }
                
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
}

/**
 * BC02용 독립적인 하드웨어 스펙 카드
 * GY01의 NodeHardwareSpecCard와 동일한 스타일로 별도 카드 구현
 */
@Composable
fun BC02HardwareSpecCard(
    hardwareSpec: HardwareSpec,
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
            Text(
                text = "Hardware Specifications",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF60A5FA),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            BC02HardwareSpecSection(hardwareSpec = hardwareSpec)
        }
    }
}

// ===== Post Worker 섹션 컴포넌트들 =====

@Composable
private fun PostWorkerHeader(displayName: String) {
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
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "Last Update: ${nodeUsage?.timestamp ?: "N/A"}",
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
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "Last Update: ${nodeUsage?.timestamp ?: "N/A"}",
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
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "Last Update: ${nodeUsage?.timestamp ?: "N/A"}",
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
private fun NodeMinerHeader(displayName: String) {
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
        
        // 스코어를 항상 표시하도록 수정
        MiningStatItem(
            label = "Performance",
            value = "${score?.averageScore?.let { String.format("%.1f", it.toFloatOrNull() ?: 0f) } ?: "N/A"}/100"
        )
    }
}

// ===== NAS 섹션 컴포넌트들 =====

@Composable
private fun NASHeader(displayName: String) {
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
}

@Composable
private fun NASStorageChart(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?
) {
    // 스토리지 및 시스템 사용량을 각각 별개의 가로형 막대 차트로 표시
    val ssdHealth = nodeUsage?.ssdHealthPercent?.toFloatOrNull() ?: 100f
    val hddUsage = nodeUsage?.harddiskUsedPercent?.toFloatOrNull() ?: 0f
    val memUsage = nodeUsage?.memUsagePercent?.toFloatOrNull() ?: 0f
    val cpuUsage = nodeUsage?.cpuUsagePercent?.toFloatOrNull() ?: 0f
    
    Column {
        Text(
            text = "Storage Usage Overview",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // SSD Health - 별개의 가로형 막대 차트
        HorizontalMetricBar(
            label = "SSD Health",
            value = ssdHealth,
            maxValue = 100f,
            color = Color(0xFF3B82F6),
            backgroundColor = Color(0xFF374151),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // HDD Usage - 별개의 가로형 막대 차트
        HorizontalMetricBar(
            label = "HDD Usage",
            value = hddUsage,
            maxValue = 100f,
            color = Color(0xFF10B981),
            backgroundColor = Color(0xFF374151),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Memory Usage - 별개의 가로형 막대 차트
        HorizontalMetricBar(
            label = "Memory Usage",
            value = memUsage,
            maxValue = 100f,
            color = Color(0xFFF59E0B),
            backgroundColor = Color(0xFF374151),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // CPU Usage - 별개의 가로형 막대 차트
        HorizontalMetricBar(
            label = "CPU Usage",
            value = cpuUsage,
            maxValue = 100f,
            color = Color(0xFF8B5CF6),
            backgroundColor = Color(0xFF374151),
            modifier = Modifier.fillMaxWidth()
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp), // 텍스트가 잘리지 않도록 높이를 더 증가
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 총 용량
            StorageDetailCard(
                title = "Total Capacity",
                value = "${hardwareSpec?.totalHarddiskGb ?: "N/A"} GB",
                color = Color(0xFF3B82F6),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight() // Row의 전체 높이를 채우도록 설정
            )
            
            // 사용량
            StorageDetailCard(
                title = "Used Space",
                value = "${nodeUsage?.harddiskUsedPercent ?: "0"}%",
                color = Color(0xFF10B981),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight() // Row의 전체 높이를 채우도록 설정
            )
            
            // SSD 상태
            StorageDetailCard(
                title = "SSD Health",
                value = "${nodeUsage?.ssdHealthPercent ?: "N/A"}%",
                color = Color(0xFFF59E0B),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight() // Row의 전체 높이를 채우도록 설정
            )
            
            // 스코어 추가
            StorageDetailCard(
                title = "Score",
                value = "${score?.averageScore?.let { String.format("%.1f", it.toFloatOrNull() ?: 0f) } ?: "N/A"}",
                color = Color(0xFF8B5CF6),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight() // Row의 전체 높이를 채우도록 설정
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
                .padding(8.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 9.sp, // 제목 폰트 크기 더 줄임
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2 // 제목이 2줄까지 표시되도록 설정
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 10.sp, // 값 폰트 크기도 더 줄임
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2 // 값이 2줄까지 표시되도록 설정
            )
        }
    }
}

/**
 * BC02용 하드웨어 스펙 섹션
 * GY01의 HardwareSpecSection과 동일한 방식으로 정보 표시
 */
@Composable
private fun BC02HardwareSpecSection(hardwareSpec: HardwareSpec) {
    Column {
        BC02InfoRow("CPU", "${hardwareSpec.cpuModel} (${hardwareSpec.cpuCores} cores)")
        BC02InfoRow("GPU", "${hardwareSpec.gpuModel} (${hardwareSpec.gpuVramGb}GB VRAM)")
        BC02InfoRow("RAM", "${hardwareSpec.totalRamGb}GB")
        BC02InfoRow("NVMe Count", hardwareSpec.nvmeCount)
    }
}

/**
 * BC02용 정보 행 표시 컴포넌트
 * GY01의 NodeInfoRow와 동일한 스타일
 */
@Composable
private fun BC02InfoRow(
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
 * BC02용 육각형 스코어 카드
 * NodeComponents.kt의 NodeScoreCard와 동일한 육각형 차트를 표시
 */
@Composable
fun BC02HexagonScoreCard(
    score: Score,
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
            // 헤더 (NodeComponents.kt와 동일한 스타일)
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
            BC02ScoreDisplaySection(score = score)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 육각형 차트
            BC02HexagonChart(score = score)
        }
    }
}

/**
 * BC02용 스코어 표시 섹션
 */
@Composable
private fun BC02ScoreDisplaySection(score: Score) {
    val averageScore = score.averageScore.toFloatOrNull() ?: 0f
    
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
 * BC02용 육각형 차트
 */
@Composable
private fun BC02HexagonChart(score: Score?) {
    val metrics = extractBC02Metrics(score)
    
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
            drawBC02HexagonChart(this, metrics)
        }
        
        // 메트릭 라벨들
        BC02HexagonLabels(metrics = metrics)
    }
}

/**
 * BC02용 Score 객체에서 메트릭 데이터 추출
 */
private fun extractBC02Metrics(score: Score?): List<BC02MetricData> {
    return if (score != null) {
        listOf(
            BC02MetricData("CPU", score.cpuScore.toFloatOrNull() ?: 80f, Color(0xFF3B82F6)),
            BC02MetricData("GPU", score.gpuScore.toFloatOrNull() ?: 80f, Color(0xFF8B5CF6)),
            BC02MetricData("RAM", score.ramScore.toFloatOrNull() ?: 80f, Color(0xFF06B6D4)),
            BC02MetricData("STORAGE", score.ssdScore.toFloatOrNull() ?: 80f, Color(0xFF10B981)),
            BC02MetricData("NETWORK", score.networkScore.toFloatOrNull() ?: 80f, Color(0xFFF59E0B)),
            BC02MetricData("HEALTH", score.hardwareHealthScore.toFloatOrNull() ?: 80f, Color(0xFFEF4444))
        )
    } else {
        // 기본값 (모든 값 80점)
        listOf(
            BC02MetricData("CPU", 80f, Color(0xFF3B82F6)),
            BC02MetricData("GPU", 80f, Color(0xFF8B5CF6)),
            BC02MetricData("RAM", 80f, Color(0xFF06B6D4)),
            BC02MetricData("STORAGE", 80f, Color(0xFF10B981)),
            BC02MetricData("NETWORK", 80f, Color(0xFFF59E0B)),
            BC02MetricData("HEALTH", 80f, Color(0xFFEF4444))
        )
    }
}

/**
 * BC02용 메트릭 데이터 클래스
 */
data class BC02MetricData(
    val name: String,
    val value: Float,
    val color: Color
)

/**
 * BC02용 육각형 차트 그리기 함수
 */
private fun drawBC02HexagonChart(drawScope: androidx.compose.ui.graphics.drawscope.DrawScope, metrics: List<BC02MetricData>) {
    with(drawScope) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.8f
        val vertices = mutableListOf<Offset>()
        
        // 육각형 꼭짓점 계산
        for (i in 0 until 6) {
            val angle = (i * 60 - 90) * (kotlin.math.PI / 180).toFloat()
            val x = center.x + radius * kotlin.math.cos(angle.toDouble()).toFloat()
            val y = center.y + radius * kotlin.math.sin(angle.toDouble()).toFloat()
            vertices.add(Offset(x, y))
        }
        
        // 기준선 그리기 (회색 육각형들)
        for (scale in listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)) {
            val scaledVertices = vertices.map { vertex ->
                val dx = vertex.x - center.x
                val dy = vertex.y - center.y
                Offset(center.x + dx * scale, center.y + dy * scale)
            }
            
            // 육각형 그리기
            for (i in scaledVertices.indices) {
                val start = scaledVertices[i]
                val end = scaledVertices[(i + 1) % scaledVertices.size]
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFF374151),
                    start = start,
                    end = end,
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        
        // 중심에서 꼭짓점으로 가는 선 그리기
        vertices.forEach { vertex ->
            drawLine(
                color = androidx.compose.ui.graphics.Color(0xFF374151),
                start = center,
                end = vertex,
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // 실제 데이터 육각형 그리기
        val dataVertices = mutableListOf<Offset>()
        for (i in metrics.indices) {
            val angle = (i * 60 - 90) * (kotlin.math.PI / 180).toFloat()
            val normalizedValue = (metrics[i].value / 100f).coerceIn(0f, 1f)
            val dataRadius = radius * normalizedValue
            val x = center.x + dataRadius * kotlin.math.cos(angle.toDouble()).toFloat()
            val y = center.y + dataRadius * kotlin.math.sin(angle.toDouble()).toFloat()
            dataVertices.add(Offset(x, y))
        }
        
        // 데이터 육각형의 면 채우기
        if (dataVertices.size >= 3) {
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(dataVertices[0].x, dataVertices[0].y)
            for (i in 1 until dataVertices.size) {
                path.lineTo(dataVertices[i].x, dataVertices[i].y)
            }
            path.close()
            
            drawPath(
                path = path,
                color = androidx.compose.ui.graphics.Color(0xFF3B82F6).copy(alpha = 0.3f)
            )
        }
        
        // 데이터 육각형의 선 그리기
        for (i in dataVertices.indices) {
            val start = dataVertices[i]
            val end = dataVertices[(i + 1) % dataVertices.size]
            drawLine(
                color = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }
        
        // 꼭짓점에 원 그리기
        dataVertices.forEachIndexed { index, vertex ->
            drawCircle(
                color = metrics[index].color,
                radius = 4.dp.toPx(),
                center = vertex
            )
        }
    }
}

/**
 * BC02용 육각형 라벨 컴포넌트
 */
@Composable
private fun BC02HexagonLabels(metrics: List<BC02MetricData>) {
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
