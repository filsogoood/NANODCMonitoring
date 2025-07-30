package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil
import kotlinx.coroutines.launch

/**
 * 클릭 가능한 이미지 아이템 컴포넌트
 * 첫 번째 이미지(index 0) 클릭 시 스코어 카드를 표시합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller, ZetaCube Logo는 클릭해도 카드가 나오지 않습니다.
 */
@Composable
fun ClickableImageItem(
    imageType: ImageType,
    imageIndex: Int = -1,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    var isExpanded by remember { mutableStateOf(false) }
    var scoreData by remember { mutableStateOf<Score?>(null) }
    
    val repository = remember { NanoDcRepository() }
    
    Column(modifier = modifier) {
        // 이미지 표시 (클릭 가능 여부에 따라 동작 분기)
        if (imageType.isClickable) {
            // 클릭 가능한 이미지: 기존 로직 유지
            SeamlessImageItem(
                imageType = imageType,
                modifier = Modifier.clickable { 
                    isExpanded = !isExpanded
                },
                contentScale = contentScale
            )
        } else {
            // 클릭 불가능한 이미지: 클릭 이벤트 없이 이미지만 표시
            SeamlessImageItem(
                imageType = imageType,
                modifier = Modifier,  // clickable 없음
                contentScale = contentScale
            )
        }
        
        // 확장 정보 카드 (클릭 가능한 이미지에만 표시)
        if (imageType.isClickable) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (imageIndex == 0) {
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
                } else {
                    // 다른 이미지는 기존 확장 정보 표시
                    ExpandedInfoCard(imageType = imageType)
                }
            }
        }
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
 */
@Composable
fun SeamlessImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    // 모든 이미지를 동일한 방식으로 처리하여 레이아웃 일관성 보장
    Image(
        painter = painterResource(id = imageType.drawableRes),
        contentDescription = imageType.description,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentScale = contentScale
    )
}

/**
 * 순수 이미지만 표시하는 컴포넌트 (카드, 박스 없음)
 * 원본 크기 및 다양한 스케일링 모드 지원
 * 클릭 가능한 이미지의 경우 첫 번째 이미지 클릭 시 스코어 모달을 표시합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller, ZetaCube Logo는 클릭해도 카드가 나오지 않습니다.
 */
@Composable
fun PureImageItem(
    imageType: ImageType,
    imageIndex: Int = -1,
    modifier: Modifier = Modifier,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH
) {
    val contentScale = ImageScaleUtil.getContentScale(scaleMode)

    ClickableImageItem(
        imageType = imageType,
        imageIndex = imageIndex,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * 스크롤 없이 모든 이미지가 한 화면에 보이도록 하는 컴포넌트
 * 이미지들이 간격 없이 연속적으로 표시됨
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

    if (useOriginalSize) {
        // 원본 크기 모드: 각 이미지를 원본 크기로 표시하고 스크롤 가능
        SeamlessOriginalSizeContent(
            imageOrder = imageOrder,
            modifier = modifier
        )
    } else {
        // 기존 방식: 화면에 맞춰 이미지 크기 조정
        SeamlessFitScreenContent(
            imageOrder = imageOrder,
            scaleMode = scaleMode,
            modifier = modifier
        )
    }
}

/**
 * 원본 크기로 이미지를 표시하는 컴포넌트 (간격 없음)
 * 스크롤 가능하며 모든 이미지가 완전히 붙어서 표시됨
 * 클릭 가능한 이미지의 경우 첫 번째 이미지 클릭 시 스코어 모달을 표시합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller, ZetaCube Logo는 클릭해도 카드가 나오지 않습니다.
 */
@Composable
private fun SeamlessOriginalSizeContent(
    imageOrder: List<ImageType>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top  // 간격 없이 위부터 차례로 배치
    ) {
        imageOrder.forEachIndexed { index, imageType ->
            ClickableImageItem(
                imageType = imageType,
                imageIndex = index,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

/**
 * 화면에 맞춰 이미지 크기를 조정하는 컴포넌트 (간격 없음)
 * 모든 이미지가 한 화면에 표시됨
 */
@Composable
private fun SeamlessFitScreenContent(
    imageOrder: List<ImageType>,
    scaleMode: ImageScaleUtil.ScaleMode,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    // 각 이미지의 높이 계산 (화면 높이를 이미지 개수로 나누기)
    val imageHeight = screenHeight / imageOrder.size

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top  // 간격 없이 위부터 차례로 배치
    ) {
        imageOrder.forEachIndexed { index, imageType ->
            PureImageItem(
                imageType = imageType,
                imageIndex = index,
                modifier = Modifier.height(imageHeight.dp),
                scaleMode = scaleMode
            )
        }
    }
}

/**
 * 원본 크기 이미지들을 연속으로 표시하는 전체 화면 모니터링 컴포넌트
 * LazyColumn 사용으로 성능 최적화하면서 간격 없이 표시
 * 클릭 가능한 이미지의 경우 첫 번째 이미지 클릭 시 스코어 모달을 표시합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller, ZetaCube Logo는 클릭해도 카드가 나오지 않습니다.
 */
@Composable
fun OriginalSizeDataCenterScreen(
    modifier: Modifier = Modifier,
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),  // 명시적으로 간격 0 설정
        contentPadding = PaddingValues(0.dp)  // 패딩도 0으로 설정
    ) {
        itemsIndexed(
            items = imageOrder,
            key = { _, imageType -> imageType.name }  // 성능 최적화를 위한 key 설정
        ) { index, imageType ->
            ClickableImageItem(
                imageType = imageType,
                imageIndex = index,
                modifier = Modifier.fillParentMaxWidth(),  // LazyColumn 내에서 전체 너비 사용
                contentScale = ContentScale.FillWidth
            )
        }
    }
}
