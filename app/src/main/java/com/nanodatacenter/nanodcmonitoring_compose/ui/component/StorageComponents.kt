package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.config.DeviceConfigurationManager
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.network.model.*
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.NodeScoreCard
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.NodeHardwareSpecCard
import java.text.SimpleDateFormat
import java.util.*

/**
 * BC01 데이터센터 전용 스토리지 기반 모니터링 화면
 * GY01과 동일한 UI 구조로 표시
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

    // BC01 노드들을 필터링
    val bc01Nodes = remember(apiResponse) {
        apiResponse?.nodes?.filter { node ->
            node.nodeName.contains("NAS", ignoreCase = true) ||
                    node.nodeName.contains("Filecoin-Miner", ignoreCase = true) ||
                    node.nodeName.contains("SAI Server", ignoreCase = true)
        } ?: emptyList()
    }

    if (isLoading && apiResponse == null) {
        // 초기 로딩 상태
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF60A5FA))
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            // BC01 노드들을 GY01과 동일한 형식으로 표시
            items(bc01Nodes.size) { index ->
                val node = bc01Nodes[index]
                val hardwareSpec = apiResponse?.hardwareSpecs?.find { it.nodeId == node.nodeId }
                val score = apiResponse?.scores?.find { it.nodeId == node.nodeId }
                val nodeUsage = apiResponse?.nodeUsage?.find { it.nodeId == node.nodeId }

                BC01NodeImageWithInfo(
                    node = node,
                    hardwareSpec = hardwareSpec,
                    score = score,
                    nodeUsage = nodeUsage,
                    nodeIndex = index,
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
        }
    }
}

/**
 * BC01 노드 이미지와 정보를 표시하는 컴포넌트
 * GY01의 NodeImageWithInfo와 동일한 구조
 */
@Composable
fun BC01NodeImageWithInfo(
    node: Node,
    hardwareSpec: HardwareSpec?,
    score: Score?,
    nodeUsage: NodeUsage?,
    nodeIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 노드 이름에 따라 이미지 타입 결정
    val imageType = when {
        node.nodeName.contains("NAS", ignoreCase = true) -> ImageType.STORAGE_1
        node.nodeName.contains("Filecoin-Miner", ignoreCase = true) -> ImageType.FILECOIN
        node.nodeName.contains("SAI Server", ignoreCase = true) -> ImageType.POSTWORKER
        else -> ImageType.STORAGE_1 // 기본 이미지
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

        // 확장 정보 카드 (BC01 전용 UI 사용)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            // BC01 전용 사용량 카드 (기존 StorageStatusCard 형태 유지)
            BC01StorageInfoCard(
                node = node,
                hardwareSpec = hardwareSpec,
                score = score,
                nodeUsage = nodeUsage,
                nodeIndex = nodeIndex
            )
        }
    }
}

/**
 * BC01 스토리지 전용 정보 카드 컴포넌트
 * 기존 BC01의 StorageStatusCard 방식을 유지하여 사용량 UI를 제공
 */
@Composable
fun BC01StorageInfoCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    score: Score?,
    nodeUsage: NodeUsage?,
    nodeIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 노드 이름 카드
        NodeNameCard(
            node = node,
            displayName = when {
                node.nodeName.contains("NAS5", ignoreCase = true) -> "BC01 Storage 1"
                node.nodeName.contains("NAS3", ignoreCase = true) ||
                        node.nodeName.contains("NAS4", ignoreCase = true) -> "BC01 Storage 2"
                node.nodeName.contains("NAS2", ignoreCase = true) -> "BC01 Storage 3"
                node.nodeName.contains("NAS1", ignoreCase = true) -> "BC01 Storage 4"
                node.nodeName.contains("SAI Server", ignoreCase = true) -> "BC01 Storage 5"
                else -> "BC01 Storage ${nodeIndex + 1}"
            }
        )

        // 스코어 카드
        if (score != null) {
            NodeScoreCard(score = score)
        }

        // 스토리지 상태 카드 (기존 BC01 방식 유지)
        if (hardwareSpec != null && nodeUsage != null) {
            StorageStatusCard(
                hardwareSpec = hardwareSpec,
                nodeUsage = nodeUsage
            )
        }

        // 하드웨어 스펙 카드 (GY01과 동일한 NodeHardwareSpecCard 사용)
        if (hardwareSpec != null) {
            NodeHardwareSpecCard(
                hardwareSpec = hardwareSpec
            )
        }
    }
}

/**
 * BC01 노드 정보를 표시하는 카드 컴포넌트
 * GY01의 NodeInfoCard와 동일한 구조로 각 정보를 개별 카드로 분리하여 표시
 */
@Composable
fun BC01NodeInfoCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    score: Score?,
    nodeUsage: NodeUsage?,
    nodeIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 노드 이름 카드 (GY01과 동일한 스타일)
        NodeNameCard(
            node = node,
            displayName = when {
                node.nodeName.contains("NAS5", ignoreCase = true) -> "BC01 Storage 1"
                node.nodeName.contains("NAS3", ignoreCase = true) ||
                        node.nodeName.contains("NAS4", ignoreCase = true) -> "BC01 Storage 2"

                node.nodeName.contains("NAS2", ignoreCase = true) -> "BC01 Storage 3"
                node.nodeName.contains("NAS1", ignoreCase = true) -> "BC01 Storage 4"
                node.nodeName.contains("SAI Server", ignoreCase = true) -> "BC01 Storage 5"
                node.nodeName.contains("Filecoin-Miner", ignoreCase = true) -> "BC01 Filecoin Miner"
                else -> node.nodeName
            }
        )

        // 스코어 카드 (GY01과 동일한 NodeScoreCard 사용)
        if (score != null) {
            NodeScoreCard(score = score)
        }

        // 하드웨어 스펙 카드 (GY01과 동일한 스타일)
        if (hardwareSpec != null) {
            // 하드웨어 스펙 카드 (GY01과 동일한 NodeHardwareSpecCard 사용)
            NodeHardwareSpecCard(
                hardwareSpec = hardwareSpec
            )

            // 스토리지 노드와 FileCoin Miner인 경우 스토리지 상태 카드 표시 (사용량)
            if (node.nodeName.contains("NAS", ignoreCase = true) ||
                node.nodeName.contains("SAI Server", ignoreCase = true) ||
                node.nodeName.contains("Filecoin-Miner", ignoreCase = true)
            ) {
                // 스토리지 상태 카드 (GY01 스타일) - 사용량으로 분류
                StorageStatusCard(
                    hardwareSpec = hardwareSpec,
                    nodeUsage = nodeUsage
                )
            }
        }

        // Current Usage 카드 삭제 - 갱신시간 정보는 Storage Status에 통합됨
    }
}

/**
 * 노드 이름을 표시하는 개별 카드 (GY01/BC01 공통)
 */
@Composable
private fun NodeNameCard(
    node: Node,
    displayName: String? = null,
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
                text = displayName ?: node.nodeName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 스토리지 상태를 표시하는 개별 카드 (GY01 스타일)
 * BC01에서 Stage Status와 갱신시간 정보 2개를 함께 표시
 */
@Composable
private fun StorageStatusCard(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
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
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Stage Status 제목과 갱신시간 정보 (GY01 Current Usage와 동일한 스타일)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stage Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF60A5FA)
                )
                
                // 갱신시간 정보 (GY01과 동일한 스타일)
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Last Update: ${formatTimestamp(nodeUsage?.timestamp)}",
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
            
            Spacer(modifier = Modifier.height(16.dp))

            if (hardwareSpec != null && nodeUsage != null) {
                // HDD 사용량
                val totalHddGb = hardwareSpec.totalHarddiskGb?.toLongOrNull() ?: 0L
                val hddUsagePercent = nodeUsage.harddiskUsedPercent?.toFloatOrNull() ?: 0f
                val usedHddGb = (totalHddGb * hddUsagePercent / 100).toLong()

                if (totalHddGb > 0) {
                    StorageProgressBar(
                        label = "HDD Storage",
                        usedGb = usedHddGb,
                        totalGb = totalHddGb,
                        usagePercent = hddUsagePercent,
                        primaryColor = Color(0xFF3B82F6),
                        backgroundColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // NVMe 사용량
                val totalNvmeGb = hardwareSpec.storageTotalGb?.toLongOrNull() ?: 0L
                val usedNvmeGb = nodeUsage.usedStorageGb?.toLongOrNull() ?: 0L
                val nvmeUsagePercent = if (totalNvmeGb > 0) {
                    ((usedNvmeGb.toFloat() / totalNvmeGb) * 100).coerceIn(0f, 100f)
                } else 0f

                if (totalNvmeGb > 0) {
                    StorageProgressBar(
                        label = "NVMe Storage",
                        usedGb = usedNvmeGb,
                        totalGb = totalNvmeGb,
                        usagePercent = nvmeUsagePercent,
                        primaryColor = Color(0xFF10B981),
                        backgroundColor = Color(0xFF064E3B).copy(alpha = 0.3f)
                    )
                }
            } else {
                Text(
                    text = "Storage data not available",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 개선된 스토리지 사용량 막대 그래프
 */
@Composable
private fun StorageProgressBar(
    label: String,
    usedGb: Long,
    totalGb: Long,
    usagePercent: Float,
    primaryColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 라벨
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 막대 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            // 배경 막대
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        backgroundColor,
                        RoundedCornerShape(16.dp)
                    )
            )

            // 사용량 막대
            val animatedProgress by animateFloatAsState(
                targetValue = (usagePercent / 100f).coerceIn(0f, 1f),
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.9f),
                                when {
                                    usagePercent > 90f -> Color(0xFFEF4444)
                                    usagePercent > 70f -> Color(0xFFF59E0B)
                                    else -> primaryColor
                                }
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                // 그라데이션 오버레이 효과
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        )
                )
            }

            // 중앙에 텍스트 표시
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 사용량 정보
                Text(
                    text = "${formatCapacity(usedGb)} / ${formatCapacity(totalGb)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // 퍼센트
                Text(
                    text = "${String.format(Locale.US, "%.1f", usagePercent)}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // 추가 정보 (Free space)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Free",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = formatCapacity(totalGb - usedGb),
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 하드웨어 스펙을 표시하는 개별 카드 (GY01 스타일)
 */
@Composable
private fun HardwareSpecCard(
    hardwareSpec: HardwareSpec,
    isStorageNode: Boolean,
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

            // 주요 스펙 박스들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecInfoBox(
                    title = "CPU",
                    value = "${hardwareSpec.cpuCores ?: 0} cores",
                    color = Color(0xFF8B5CF6)
                )

                Spacer(modifier = Modifier.width(8.dp))

                SpecInfoBox(
                    title = "RAM",
                    value = "${hardwareSpec.totalRamGb ?: 0} GB",
                    color = Color(0xFFF59E0B)
                )
            }

            if (!isStorageNode && !hardwareSpec.gpuCount.isNullOrEmpty() && (hardwareSpec.gpuCount?.toIntOrNull()
                    ?: 0) > 0
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpecInfoBox(
                        title = "GPU",
                        value = "${hardwareSpec.gpuCount} x ${hardwareSpec.gpuVramGb ?: 0}GB",
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 추가 정보
            StorageNodeInfoRow("CPU Model", hardwareSpec.cpuModel)
            if (!hardwareSpec.gpuModel.isNullOrEmpty() && !isStorageNode) {
                StorageNodeInfoRow("GPU Model", hardwareSpec.gpuModel)
            }
            if (isStorageNode) {
                StorageNodeInfoRow("NVMe Count", hardwareSpec.nvmeCount)
            }
        }
    }
}

/**
 * 스펙 정보 박스
 */
@Composable
private fun RowScope.SpecInfoBox(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
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
 * 노드 정보 행을 표시하는 재사용 가능한 컴포넌트 (StorageComponents 전용)
 */
@Composable
private fun StorageNodeInfoRow(
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
 * 용량을 읽기 쉬운 형태로 포맷
 */
private fun formatCapacity(capacityGb: Long): String {
    return when {
        capacityGb >= 1024 * 1024 -> "${
            String.format(
                Locale.US,
                "%.1f",
                capacityGb / (1024.0 * 1024.0)
            )
        } PB"

        capacityGb >= 1024 -> "${String.format(Locale.US, "%.1f", capacityGb / 1024.0)} TB"
        else -> "$capacityGb GB"
    }
}

/**
 * 타임스탬프를 포맷하는 함수
 */
private fun formatTimestamp(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "N/A"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: "N/A"
    } catch (e: Exception) {
        "N/A"
    }
}
