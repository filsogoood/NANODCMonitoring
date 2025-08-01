package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.config.DeviceConfigurationManager
import com.nanodatacenter.nanodcmonitoring_compose.network.model.*
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * BC01 데이터센터 전용 스토리지 기반 모니터링 화면
 * 각 스토리지 노드를 카드 형태로 표시하여 직관적인 모니터링 제공
 */
@Composable
fun StorageBasedMonitoringScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }
    val currentDataCenter = deviceConfigManager.getSelectedDataCenter()
    
    // Repository에서 데이터 구독
    val repository = remember { NanoDcRepository.getInstance() }
    val apiResponse by repository.apiResponseState.collectAsState()
    val isLoading by repository.isLoading.collectAsState()
    val lastRefreshTime by repository.lastRefreshTime.collectAsState()
    
    // BC01 노드들을 카테고리별로 분류
    val storageNodes = remember(apiResponse) {
        apiResponse?.nodes?.filter { node ->
            node.nodeName.contains("NAS", ignoreCase = true)
        } ?: emptyList()
    }
    
    val minerNodes = remember(apiResponse) {
        apiResponse?.nodes?.filter { node ->
            node.nodeName.contains("Filecoin-Miner", ignoreCase = true) || 
            node.nodeName.contains("SAI Server", ignoreCase = true)
        } ?: emptyList()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // 헤더
        BC01Header(
            currentDataCenter = currentDataCenter,
            lastRefreshTime = lastRefreshTime,
            isLoading = isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading && apiResponse == null) {
            // 초기 로딩 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                    Text(
                        text = "Loading BC01 Storage Data...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            // 스토리지 노드와 마이너 노드 표시
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 스토리지 노드들
                items(storageNodes) { node ->
                    val hardwareSpec = apiResponse?.hardwareSpecs?.find { it.nodeId == node.nodeId }
                    val nodeUsage = apiResponse?.nodeUsage?.find { it.nodeId == node.nodeId }
                    val score = apiResponse?.scores?.find { it.nodeId == node.nodeId }
                    
                    StorageNodeCard(
                        node = node,
                        hardwareSpec = hardwareSpec,
                        nodeUsage = nodeUsage,
                        score = score
                    )
                }
                
                // 마이너 노드들
                items(minerNodes) { node ->
                    val hardwareSpec = apiResponse?.hardwareSpecs?.find { it.nodeId == node.nodeId }
                    val nodeUsage = apiResponse?.nodeUsage?.find { it.nodeId == node.nodeId }
                    val score = apiResponse?.scores?.find { it.nodeId == node.nodeId }
                    
                    MinerNodeCard(
                        node = node,
                        hardwareSpec = hardwareSpec,
                        nodeUsage = nodeUsage,
                        score = score
                    )
                }
            }
        }
    }
}

/**
 * BC01 헤더 컴포넌트
 */
@Composable
private fun BC01Header(
    currentDataCenter: com.nanodatacenter.nanodcmonitoring_compose.data.DataCenterType,
    lastRefreshTime: Long,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentDataCenter.displayName} Storage Monitoring",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (lastRefreshTime > 0) {
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                Text(
                    text = "Last Updated: ${formatter.format(Date(lastRefreshTime))}",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

/**
 * 스토리지 노드 카드 (NAS 전용)
 */
@Composable
private fun StorageNodeCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 노드 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = node.nodeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "BC01 Data Center",
                        fontSize = 14.sp,
                        color = Color(0xFF10B981)
                    )
                }
                
                StatusBadge(status = node.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 스토리지 정보
            hardwareSpec?.let { spec ->
                StorageInfoSection(hardwareSpec = spec)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 사용량 정보
            nodeUsage?.let { usage ->
                UsageSection(nodeUsage = usage)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 성능 점수
            score?.let { scoreData ->
                ScoreSection(score = scoreData)
            }
        }
    }
}

/**
 * 마이너 노드 카드 (Filecoin-Miner, SAI Server 전용)
 */
@Composable
private fun MinerNodeCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    score: Score?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B69)), // 보라색 계열로 차별화
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 노드 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = node.nodeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (node.nodeName.contains("SAI")) "AI Computing Node" else "Mining Node",
                        fontSize = 14.sp,
                        color = Color(0xFF8B5CF6)
                    )
                }
                
                StatusBadge(status = node.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 하드웨어 정보
            hardwareSpec?.let { spec ->
                MinerHardwareSection(hardwareSpec = spec)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 사용량 정보
            nodeUsage?.let { usage ->
                UsageSection(nodeUsage = usage)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 성능 점수
            score?.let { scoreData ->
                ScoreSection(score = scoreData)
            }
        }
    }
}

/**
 * 상태 배지
 */
@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, displayText) = when (status.lowercase()) {
        "active" -> Triple(Color(0xFF10B981), Color.White, "Active")
        "pre" -> Triple(Color(0xFFF59E0B), Color.White, "Pre")
        else -> Triple(Color(0xFF6B7280), Color.White, status)
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * 스토리지 정보 섹션
 */
@Composable
private fun StorageInfoSection(hardwareSpec: HardwareSpec) {
    Column {
        Text(
            text = "Storage Capacity",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                title = "NVMe",
                value = "${hardwareSpec.storageTotalGb} GB",
                icon = "💾"
            )
            
            InfoCard(
                title = "HDD",
                value = "${(hardwareSpec.totalHarddiskGb?.toDoubleOrNull()?.div(1000) ?: 0.0).let { "%.1f TB".format(it) }}",
                icon = "🗄️"
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                title = "CPU",
                value = "${hardwareSpec.cpuCores} cores",
                icon = "🔧"
            )
            
            InfoCard(
                title = "RAM",
                value = "${hardwareSpec.totalRamGb} GB",
                icon = "⚡"
            )
        }
    }
}

/**
 * 마이너 하드웨어 섹션
 */
@Composable
private fun MinerHardwareSection(hardwareSpec: HardwareSpec) {
    Column {
        Text(
            text = "Hardware Specifications",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                title = "CPU",
                value = "${hardwareSpec.cpuCores} cores",
                icon = "🔧"
            )
            
            InfoCard(
                title = "GPU",
                value = "${hardwareSpec.gpuCount} x ${hardwareSpec.gpuVramGb}GB",
                icon = "🎮"
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                title = "RAM",
                value = "${hardwareSpec.totalRamGb} GB",
                icon = "⚡"
            )
            
            InfoCard(
                title = "Storage",
                value = "${hardwareSpec.storageTotalGb} GB",
                icon = "💾"
            )
        }
    }
}

/**
 * 사용량 섹션
 */
@Composable
private fun UsageSection(nodeUsage: NodeUsage) {
    Column {
        Text(
            text = "Real-time Usage",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // CPU 사용률
        UsageBar(
            label = "CPU",
            percentage = nodeUsage.cpuUsagePercent.toFloatOrNull() ?: 0f,
            color = Color(0xFF06B6D4)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 메모리 사용률
        UsageBar(
            label = "Memory",
            percentage = nodeUsage.memUsagePercent.toFloatOrNull() ?: 0f,
            color = Color(0xFF10B981)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 디스크 사용률
        nodeUsage.harddiskUsedPercent?.let { diskUsage ->
            UsageBar(
                label = "Disk",
                percentage = diskUsage.toFloatOrNull() ?: 0f,
                color = Color(0xFFF59E0B)
            )
        }
        
        // SSD 건강도
        nodeUsage.ssdHealthPercent?.let { ssdHealth ->
            if (ssdHealth != "null") {
                Spacer(modifier = Modifier.height(8.dp))
                UsageBar(
                    label = "SSD Health",
                    percentage = ssdHealth.toFloatOrNull() ?: 0f,
                    color = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

/**
 * 성능 점수 섹션
 */
@Composable
private fun ScoreSection(score: Score) {
    Column {
        Text(
            text = "Performance Score",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 평균 점수
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF059669)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Overall: ${score.averageScore}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 개별 점수들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreCard("CPU", score.cpuScore)
            ScoreCard("SSD", score.ssdScore)
            ScoreCard("RAM", score.ramScore)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreCard("Network", score.networkScore)
            ScoreCard("Health", score.hardwareHealthScore)
            Spacer(modifier = Modifier.weight(1f)) // 빈 공간
        }
    }
}

/**
 * 정보 카드
 */
@Composable
private fun RowScope.InfoCard(
    title: String,
    value: String,
    icon: String
) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF374151)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
    
    if (title != "RAM" && title != "Storage") {
        Spacer(modifier = Modifier.width(8.dp))
    }
}

/**
 * 사용률 바
 */
@Composable
private fun UsageBar(
    label: String,
    percentage: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.width(60.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    Color(0xFF374151),
                    RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                    .background(
                        color,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Text(
            text = "${"%.1f".format(percentage)}%",
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 점수 카드
 */
@Composable
private fun RowScope.ScoreCard(
    label: String,
    score: String
) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF374151)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = score,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
    
    if (label != "Health") {
        Spacer(modifier = Modifier.width(4.dp))
    }
} 