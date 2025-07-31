package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.AdminAccessManager
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Node
import com.nanodatacenter.nanodcmonitoring_compose.network.model.HardwareSpec
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil
import kotlinx.coroutines.launch

/**
 * 클릭 가능한 이미지 아이템 컴포넌트
 * 첫 번째 이미지(index 0) 클릭 시 스코어 카드를 표시합니다.
 * LOGO_ZETACUBE 클릭 시 관리자 접근 기능을 제공합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller는 클릭해도 카드가 나오지 않습니다.
 * SUPRA, POSTWORKER는 전체 노드 정보를 표시하고, FILECOIN은 하드디스크 사용량 그래프, NODE_MINER는 FILECOIN 데이터로 전체 정보를 표시합니다.
 */
@Composable
fun ClickableImageItem(
    imageType: ImageType,
    imageIndex: Int = -1,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    apiResponse: ApiResponse? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var scoreData by remember { mutableStateOf<Score?>(null) }
    
    val repository = remember { NanoDcRepository() }
    val adminManager = remember { AdminAccessManager.getInstance() }
    val context = LocalContext.current
    
    // 토스트 메시지 표시
    LaunchedEffect(adminManager.shouldShowToast) {
        if (adminManager.shouldShowToast) {
            Toast.makeText(context, adminManager.toastMessage, Toast.LENGTH_SHORT).show()
            adminManager.onToastShown()
        }
    }
    
    Column(modifier = modifier) {
        // 이미지 표시 (클릭 가능 여부에 따라 동작 분기)
        when {
            imageType.isAdminAccess -> {
                // 관리자 접근 이미지: LOGO_ZETACUBE 8번 클릭 기능
                SeamlessImageItem(
                    imageType = imageType,
                    modifier = Modifier.clickable { 
                        adminManager.handleLogoClick()
                    },
                    contentScale = contentScale
                )
            }
            imageType.showsInfoCard -> {
                // 일반 클릭 가능한 이미지: 기존 로직 유지
                SeamlessImageItem(
                    imageType = imageType,
                    modifier = Modifier.clickable { 
                        isExpanded = !isExpanded
                    },
                    contentScale = contentScale
                )
            }
            else -> {
                // 클릭 불가능한 이미지: 클릭 이벤트 없이 이미지만 표시
                SeamlessImageItem(
                    imageType = imageType,
                    modifier = Modifier,  // clickable 없음
                    contentScale = contentScale
                )
            }
        }
        
        // 확장 정보 카드 (일반 클릭 가능한 이미지에만 표시)
        if (imageType.showsInfoCard) {
            // 커스텀 스케일 이미지의 경우 카드 겹침 방지를 위한 여백 추가
            if (ImageScaleUtil.hasCustomScale(imageType)) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                when {
                    imageIndex == 0 -> {
                        // 첫 번째 이미지인 경우 스코어 카드 표시
                        LaunchedEffect(Unit) {
                            // 스코어 데이터 로드
                            try {
                                scoreData = repository.getScoreForFirstImage()
                            } catch (e: Exception) {
                                // API 실패 시에도 기본값으로 표시
                                scoreData = null
                            }
                        }
                        ExpandedScoreCard(score = scoreData)
                    }
                    // SUPRA, POSTWORKER, FILECOIN, NODE_MINER 이미지의 경우 노드 정보 표시
                    imageType == ImageType.SUPRA || imageType == ImageType.POSTWORKER || imageType == ImageType.FILECOIN || imageType == ImageType.NODE_MINER -> {
                        apiResponse?.let { response ->
                            // 이미지 타입에 따라 해당 노드 찾기
                            val targetNode = when (imageType) {
                                ImageType.SUPRA -> response.nodes.find { it.nodeName.contains("Supra", ignoreCase = true) }
                                ImageType.POSTWORKER -> response.nodes.find { it.nodeName.contains("PostWorker", ignoreCase = true) }
                                ImageType.FILECOIN -> response.nodes.find { it.nodeName.contains("Filecoin", ignoreCase = true) }
                                ImageType.NODE_MINER -> response.nodes.find { it.nodeName.contains("Filecoin", ignoreCase = true) } // FILECOIN과 동일한 데이터 사용
                                else -> null
                            }
                            
                            targetNode?.let { node ->
                                when (imageType) {
                                    ImageType.FILECOIN -> {
                                        // FILECOIN은 하드디스크 사용량 그래프 표시
                                        val hardwareSpec = response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val nodeUsage = response.nodeUsage.find { it.nodeId == node.nodeId }
                                        
                                        FilecoinDiskUsageCard(
                                            node = node,
                                            hardwareSpec = hardwareSpec,
                                            nodeUsage = nodeUsage
                                        )
                                    }
                                    ImageType.NODE_MINER -> {
                                        // NODE_MINER는 전체 정보 표시 (CY01-Node Miner로 표기)
                                        val hardwareSpec = response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val score = response.scores.find { it.nodeId == node.nodeId }
                                        val nodeUsage = response.nodeUsage.find { it.nodeId == node.nodeId }
                                        
                                        NodeInfoCard(
                                            node = node,
                                            hardwareSpec = hardwareSpec,
                                            score = score,
                                            nodeUsage = nodeUsage,
                                            displayName = "CY01-Node Miner"
                                        )
                                    }
                                    else -> {
                                        // SUPRA, POSTWORKER는 전체 정보 표시
                                        val hardwareSpec = response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val score = response.scores.find { it.nodeId == node.nodeId }
                                        val nodeUsage = response.nodeUsage.find { it.nodeId == node.nodeId }
                                        
                                        NodeInfoCard(
                                            node = node,
                                            hardwareSpec = hardwareSpec,
                                            score = score,
                                            nodeUsage = nodeUsage
                                        )
                                    }
                                }
                            } ?: ExpandedInfoCard(imageType = imageType) // 노드를 찾지 못한 경우 기본 카드 표시
                        } ?: ExpandedInfoCard(imageType = imageType) // API 데이터가 없는 경우 기본 카드 표시
                    }
                    else -> {
                        // 다른 이미지는 기존 확장 정보 표시
                        ExpandedInfoCard(imageType = imageType)
                    }
                }
            }
        }
    }
    
    // 관리자 다이얼로그 표시
    if (imageType.isAdminAccess) {
        AdminAccessDialog(
            isVisible = adminManager.showAdminDialog,
            onDismiss = { adminManager.dismissAdminDialog() },
            onAdminAccess = {
                // 추후 관리자 메뉴 확장 시 사용
                Toast.makeText(context, "관리자 메뉴 접근", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * FILECOIN 하드디스크 사용량을 그래프로 표시하는 카드 컴포넌트
 */
@Composable
fun FilecoinDiskUsageCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 노드 이름
            Text(
                text = node.nodeName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // 하드디스크 사용량 그래프
            if (hardwareSpec != null && nodeUsage != null) {
                val totalHarddiskGb = hardwareSpec.totalHarddiskGb?.toLongOrNull() ?: 0L
                val usagePercent = nodeUsage.harddiskUsedPercent?.toFloatOrNull() ?: 0f
                
                DiskUsageChart(
                    totalCapacityGb = totalHarddiskGb,
                    usagePercent = usagePercent
                )
            } else {
                Text(
                    text = "Data not available",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 디스크 사용량 차트 (바 형태)
 */
@Composable
fun DiskUsageChart(
    totalCapacityGb: Long,
    usagePercent: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 프로그레스 바 (더 큰 크기로)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Color(0xFF374151),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((usagePercent / 100f).coerceIn(0f, 1f))
                    .background(
                        when {
                            usagePercent > 90f -> Color(0xFFEF4444) // 빨간색
                            usagePercent > 70f -> Color(0xFFF59E0B) // 주황색
                            else -> Color(0xFF10B981) // 초록색
                        },
                        RoundedCornerShape(20.dp)
                    )
            )
            
            // 바 안에 퍼센테이지 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${String.format("%.1f", usagePercent)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 추가 정보 행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total: ${formatCapacity(totalCapacityGb)}",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = when {
                    usagePercent > 90f -> "Critical"
                    usagePercent > 70f -> "Warning"
                    else -> "Normal"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    usagePercent > 90f -> Color(0xFFEF4444)
                    usagePercent > 70f -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
            )
        }
    }
}

/**
 * 용량을 읽기 쉬운 형태로 포맷
 */
private fun formatCapacity(capacityGb: Long): String {
    return when {
        capacityGb >= 1024 * 1024 -> "${String.format("%.1f", capacityGb / (1024.0 * 1024.0))} PB"
        capacityGb >= 1024 -> "${String.format("%.1f", capacityGb / 1024.0)} TB"
        else -> "${capacityGb} GB"
    }
}

/**
 * 확장 정보를 보여주는 카드 컴포넌트
 */
@Composable
fun ExpandedInfoCard(
    imageType: ImageType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 제목
            Text(
                text = imageType.description ?: "Equipment Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 상태 정보 (추후 API 연동 시 실제 값으로 대체)
            InfoRow(label = "Status", value = "Normal")
            InfoRow(label = "Temperature", value = "23°C")
            InfoRow(label = "CPU Usage", value = "45%")
            InfoRow(label = "Memory Usage", value = "67%")
            InfoRow(label = "Network", value = "Connected")
            InfoRow(label = "Last Update", value = "2 minutes ago")
        }
    }
}

/**
 * 정보 행을 표시하는 컴포넌트
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 기본적으로 모든 이미지가 간격 없이 붙어서 표시되는 이미지 컴포넌트
 * 모든 이미지가 동일한 방식으로 표시되어 카드 레이아웃 문제를 방지
 * 특정 이미지 타입들에 대해서는 위아래만 90% 크기로 조정하면서 레이아웃 공간도 함께 조정하여 간격 제거
 */
@Composable
fun SeamlessImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    // 커스텀 스케일이 적용된 이미지는 scale과 layout modifier를 결합하여 간격 완전 제거
    val imageModifier = if (ImageScaleUtil.hasCustomScale(imageType)) {
        val scaleFactor = ImageScaleUtil.getImageScaleFactor(imageType)
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .scale(scaleY = scaleFactor, scaleX = 1f)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val newHeight = (placeable.height * scaleFactor).toInt()
                layout(placeable.width, newHeight) {
                    placeable.place(0, 0)
                }
            }
    } else {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
    }
    
    // 모든 이미지를 동일한 방식으로 처리하여 레이아웃 일관성 보장
    Image(
        painter = painterResource(id = imageType.drawableRes),
        contentDescription = imageType.description,
        modifier = imageModifier,
        contentScale = contentScale
    )
}

/**
 * 순수 이미지만 표시하는 컴포넌트 (카드, 박스 없음)
 * 원본 크기 및 다양한 스케일링 모드 지원
 * 클릭 가능한 이미지의 경우 첫 번째 이미지 클릭 시 스코어 모달을 표시합니다.
 * LOGO_ZETACUBE 클릭 시 관리자 접근 기능을 제공합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller는 클릭해도 카드가 나오지 않습니다.
 * SUPRA, POSTWORKER는 전체 노드 정보를 표시하고, FILECOIN은 하드디스크 사용량 그래프, NODE_MINER는 FILECOIN 데이터로 전체 정보를 표시합니다.
 */
@Composable
fun PureImageItem(
    imageType: ImageType,
    imageIndex: Int = -1,
    modifier: Modifier = Modifier,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH,
    apiResponse: ApiResponse? = null
) {
    val contentScale = ImageScaleUtil.getContentScale(scaleMode)

    ClickableImageItem(
        imageType = imageType,
        imageIndex = imageIndex,
        modifier = modifier,
        contentScale = contentScale,
        apiResponse = apiResponse
    )
}

/**
 * 스크롤 없이 모든 이미지가 한 화면에 보이도록 하는 컴포넌트
 * 이미지들이 간격 없이 연속적으로 표시됨
 * API 데이터를 로드하여 SUPRA, POSTWORKER는 전체 노드 정보를 표시하고, FILECOIN은 하드디스크 사용량 그래프, NODE_MINER는 FILECOIN 데이터로 전체 정보를 표시합니다.
 */
@Composable
fun DataCenterMonitoringScreen(
    modifier: Modifier = Modifier,
    deviceType: DeviceType = DeviceType.DEFAULT,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH,
    useOriginalSize: Boolean = false
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)
    
    // API 데이터 로드
    val repository = remember { NanoDcRepository() }
    var apiResponse by remember { mutableStateOf<ApiResponse?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            apiResponse = repository.getUserData("c236ea9c-3d7e-430b-98b8-1e22d0d6cf01")
        } catch (e: Exception) {
            // 에러 처리 - 로그만 남기고 계속 진행
            android.util.Log.e("DataCenterMonitoringScreen", "Failed to load API data", e)
        }
    }

    if (useOriginalSize) {
        // 원본 크기 모드: 각 이미지를 원본 크기로 표시하고 스크롤 가능
        SeamlessOriginalSizeContent(
            imageOrder = imageOrder,
            modifier = modifier,
            apiResponse = apiResponse
        )
    } else {
        // 기존 방식: 화면에 맞춰 이미지 크기 조정
        SeamlessFitScreenContent(
            imageOrder = imageOrder,
            scaleMode = scaleMode,
            modifier = modifier,
            apiResponse = apiResponse
        )
    }
}

/**
 * 원본 크기로 이미지를 표시하는 컴포넌트 (간격 없음)
 */
@Composable
private fun SeamlessOriginalSizeContent(
    imageOrder: List<ImageType>,
    modifier: Modifier = Modifier,
    apiResponse: ApiResponse? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        imageOrder.forEachIndexed { index, imageType ->
            ClickableImageItem(
                imageType = imageType,
                imageIndex = index,
                contentScale = ContentScale.FillWidth,
                apiResponse = apiResponse
            )
        }
    }
}

/**
 * 화면에 맞춰 이미지 크기를 조정하는 컴포넌트 (간격 없음)
 */
@Composable
private fun SeamlessFitScreenContent(
    imageOrder: List<ImageType>,
    scaleMode: ImageScaleUtil.ScaleMode,
    modifier: Modifier = Modifier,
    apiResponse: ApiResponse? = null
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    // 각 이미지의 높이 계산 (화면 높이를 이미지 개수로 나누기)
    val baseImageHeight = screenHeight / imageOrder.size

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        imageOrder.forEachIndexed { index, imageType ->
            // 특정 이미지 타입들에 대해서는 90% 높이 적용하되 레이아웃 공간도 함께 조정
            val adjustedHeight = if (ImageScaleUtil.hasCustomScale(imageType)) {
                (baseImageHeight * ImageScaleUtil.getImageScaleFactor(imageType)).toInt()
            } else {
                baseImageHeight
            }
            
            PureImageItem(
                imageType = imageType,
                imageIndex = index,
                modifier = Modifier.height(adjustedHeight.dp),
                scaleMode = scaleMode,
                apiResponse = apiResponse
            )
        }
    }
}

/**
 * 원본 크기 이미지들을 연속으로 표시하는 전체 화면 모니터링 컴포넌트
 */
@Composable
fun OriginalSizeDataCenterScreen(
    modifier: Modifier = Modifier,
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)
    
    // API 데이터 로드
    val repository = remember { NanoDcRepository() }
    var apiResponse by remember { mutableStateOf<ApiResponse?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            apiResponse = repository.getUserData("c236ea9c-3d7e-430b-98b8-1e22d0d6cf01")
        } catch (e: Exception) {
            // 에러 처리 - 로그만 남기고 계속 진행
            android.util.Log.e("OriginalSizeDataCenterScreen", "Failed to load API data", e)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(
            items = imageOrder,
            key = { _, imageType -> imageType.name }
        ) { index, imageType ->
            ClickableImageItem(
                imageType = imageType,
                imageIndex = index,
                modifier = Modifier.fillParentMaxWidth(),
                contentScale = ContentScale.FillWidth,
                apiResponse = apiResponse
            )
        }
    }
}
