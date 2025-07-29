package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil

/**
 * 기본적으로 모든 이미지가 간격 없이 붙어서 표시되는 이미지 컴포넌트
 * 간단하고 일관성 있는 표시 방식
 */
@Composable
fun SeamlessImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    Image(
        painter = painterResource(id = imageType.drawableRes),
        contentDescription = imageType.description,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),  // 높이는 이미지 내용에 맞게
        contentScale = contentScale
    )
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

/**
 * 특별한 경우에만 사용하는 커스텀 간격 이미지 컴포넌트
 * 기본적으로는 사용하지 않고, 특수한 요구사항이 있을 때만 사용
 */
@Composable
fun CustomSpacedImageItem(
    imageType: ImageType,
    topSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    bottomSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    Column(
        modifier = modifier
    ) {
        if (topSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(topSpacing))
        }
        
        SeamlessImageItem(
            imageType = imageType,
            contentScale = contentScale
        )
        
        if (bottomSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(bottomSpacing))
        }
    }
}
