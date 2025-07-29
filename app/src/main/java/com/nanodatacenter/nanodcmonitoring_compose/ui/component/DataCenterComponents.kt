package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil

/**
 * 기본적으로 모든 이미지가 간격 없이 붙어서 표시되는 이미지 컴포넌트
 * 특정 이미지들은 높이를 90%로 조정하여 간격 없이 표시
 */
@Composable
fun SeamlessImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    // 높이를 조정할 이미지 타입들 정의
    val shouldReduceHeight = when (imageType) {
        ImageType.DEEPSEEK,
        ImageType.DEEPSEEK_NONE,
        ImageType.AETHIR,
        ImageType.AETHIR_NONE,
        ImageType.FILECOIN,
        ImageType.FILECOIN_NONE_1,
        ImageType.FILECOIN_NONE_2 -> true
        else -> false
    }
    
    if (shouldReduceHeight) {
        // scale과 layout을 결합하여 이미지를 90%로 축소하고 레이아웃 공간도 함께 조정
        Image(
            painter = painterResource(id = imageType.drawableRes),
            contentDescription = imageType.description,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .scale(scaleY = 0.9f, scaleX = 1f)  // 세로만 90%로 축소
                .layout { measurable, constraints ->
                    // 축소된 이미지를 측정
                    val placeable = measurable.measure(constraints)
                    // 레이아웃 높이를 90%로 조정하여 빈 공간 제거
                    val adjustedHeight = (placeable.height * 0.9f).toInt()
                    layout(placeable.width, adjustedHeight) {
                        // 축소된 이미지를 중앙에 배치
                        placeable.placeRelative(0, 0)
                    }
                },
            contentScale = contentScale
        )
    } else {
        // 일반 이미지는 원본 크기
        Image(
            painter = painterResource(id = imageType.drawableRes),
            contentDescription = imageType.description,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = contentScale
        )
    }
}

/**
 * 순수 이미지만 표시하는 컴포넌트 (카드, 박스 없음)
 * 원본 크기 및 다양한 스케일링 모드 지원
 */
@Composable
fun PureImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH
) {
    val contentScale = ImageScaleUtil.getContentScale(scaleMode)

    SeamlessImageItem(
        imageType = imageType,
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
        imageOrder.forEach { imageType ->
            SeamlessImageItem(
                imageType = imageType,
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
        imageOrder.forEach { imageType ->
            PureImageItem(
                imageType = imageType,
                modifier = Modifier.height(imageHeight.dp),
                scaleMode = scaleMode
            )
        }
    }
}

/**
 * 원본 크기 이미지들을 연속으로 표시하는 전체 화면 모니터링 컴포넌트
 * LazyColumn 사용으로 성능 최적화하면서 간격 없이 표시
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
        items(
            items = imageOrder,
            key = { imageType -> imageType.name }  // 성능 최적화를 위한 key 설정
        ) { imageType ->
            SeamlessImageItem(
                imageType = imageType,
                modifier = Modifier.fillParentMaxWidth(),  // LazyColumn 내에서 전체 너비 사용
                contentScale = ContentScale.FillWidth
            )
        }
    }
}
