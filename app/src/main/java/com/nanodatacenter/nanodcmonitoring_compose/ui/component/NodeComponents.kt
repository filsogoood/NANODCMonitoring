package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.R
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.network.model.*
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import kotlinx.coroutines.launch

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
 * 스코어, 하드웨어 스펙, 사용률 정보를 포함합니다.
 */
@Composable
fun NodeInfoCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    score: Score?,
    nodeUsage: NodeUsage?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937) // 어두운 배경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 노드 이름 헤더
            Text(
                text = node.nodeName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 스코어 섹션
            if (score != null) {
                ScoreSection(score = score)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 하드웨어 스펙 섹션
            if (hardwareSpec != null) {
                HardwareSpecSection(hardwareSpec = hardwareSpec)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 사용률 섹션
            if (nodeUsage != null) {
                UsageSection(nodeUsage = nodeUsage)
            }
        }
    }
}

/**
 * 스코어 섹션 - 기존 최상단 레이아웃 재활용
 */
@Composable
private fun ScoreSection(score: Score) {
    Column {
        Text(
            text = "Score",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF60A5FA),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
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
 * 하드웨어 스펙 섹션
 */
@Composable
private fun HardwareSpecSection(hardwareSpec: HardwareSpec) {
    Column {
        Text(
            text = "Hardware Specifications",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF60A5FA),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        InfoRow("CPU", "${hardwareSpec.cpuModel} (${hardwareSpec.cpuCores} cores)")
        InfoRow("GPU", "${hardwareSpec.gpuModel} (${hardwareSpec.gpuVramGb}GB VRAM)")
        InfoRow("RAM", "${hardwareSpec.totalRamGb}GB")
        InfoRow("Storage", "${hardwareSpec.storageType} ${hardwareSpec.storageTotalGb}GB")
        InfoRow("NVMe Count", hardwareSpec.nvmeCount)
    }
}

/**
 * 사용률 섹션
 */
@Composable
private fun UsageSection(nodeUsage: NodeUsage) {
    Column {
        Text(
            text = "Current Usage",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF60A5FA),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        InfoRow("CPU Usage", "${nodeUsage.cpuUsagePercent}%")
        InfoRow("Memory Usage", "${nodeUsage.memUsagePercent}%")
        
        if (!nodeUsage.gpuUsagePercent.isNullOrEmpty()) {
            InfoRow("GPU Usage", "${nodeUsage.gpuUsagePercent}%")
            InfoRow("GPU Temperature", "${nodeUsage.gpuTemp}°C")
            InfoRow("GPU VRAM", "${nodeUsage.gpuVramPercent}%")
        }
        
        InfoRow("Storage Used", "${nodeUsage.usedStorageGb}GB")
        InfoRow("SSD Health", "${nodeUsage.ssdHealthPercent}%")
        InfoRow("Last Update", nodeUsage.timestamp)
    }
}

/**
 * 모든 노드 이미지를 표시하는 화면
 * API 데이터를 기반으로 노드별 이미지와 정보를 표시합니다.
 */
@Composable
fun NodeBasedMonitoringScreen(
    modifier: Modifier = Modifier,
    nanoDcId: String = "c236ea9c-3d7e-430b-98b8-1e22d0d6cf01"
) {
    val repository = remember { NanoDcRepository() }
    var apiResponse by remember { mutableStateOf<ApiResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // API 데이터 로드
    LaunchedEffect(nanoDcId) {
        launch {
            try {
                apiResponse = repository.getUserData(nanoDcId)
            } catch (e: Exception) {
                // 에러 처리
            } finally {
                isLoading = false
            }
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
