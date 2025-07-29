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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil

/**
 * 이미지 간격 설정을 위한 데이터 클래스
 * 확장성을 위해 별도로 분리
 */
data class ImageSpacing(
    val topOffset: Dp = 0.dp,
    val bottomOffset: Dp = 0.dp
)

/**
 * 이미지 타입별 간격 설정을 관리하는 객체
 * 새로운 이미지 타입이 추가되어도 쉽게 확장 가능
 */
object ImageSpacingConfig {
    
    /**
     * 이미지 타입별 간격 설정 맵
     * 음수 마진을 offset으로 변환하여 처리
     * 16-17번(FILECOIN_NONE_2-NOT_STORAGE), 18-19번(UPS_CONTROLLER-LOGO_ZETACUBE) 이미지들이 붙도록 조정
     */
    private val spacingMap = mapOf(
        ImageType.DEEPSEEK to ImageSpacing(
            topOffset = (-10).dp,
            bottomOffset = (-10).dp
        ),
        ImageType.DEEPSEEK_NONE to ImageSpacing(
            topOffset = (-19).dp,
            bottomOffset = (-10).dp
        ),
        ImageType.AETHIR to ImageSpacing(
            topOffset = (-19).dp,
            bottomOffset = (-10).dp
        ),
        ImageType.AETHIR_NONE to ImageSpacing(
            topOffset = (-19).dp,
            bottomOffset = (-10).dp
        ),
        ImageType.FILECOIN to ImageSpacing(
            topOffset = (-19).dp,
            bottomOffset = (-10).dp
        ),
        ImageType.FILECOIN_NONE_1 to ImageSpacing(
            topOffset = (-19).dp,
            bottomOffset = (-10).dp
        ),
        // 16번 이미지: 17번과 완전히 붙도록 bottomOffset 강화
        ImageType.FILECOIN_NONE_2 to ImageSpacing(
            topOffset = (-19).dp,
            bottomOffset = (-30).dp  // 더 강한 음수로 조정
        ),
        // 17번 이미지: 16번과 18번 모두 붙도록 양쪽 조정
        ImageType.NOT_STORAGE to ImageSpacing(
            topOffset = (-25).dp,    // 더 강한 음수로 이전 이미지와 붙게 함
            bottomOffset = (-25).dp  // 다음 이미지와도 붙도록 음수 설정
        ),
        // 18번 이미지: 17번과 19번 모두 붙도록 양쪽 조정  
        ImageType.UPS_CONTROLLER to ImageSpacing(
            topOffset = (-20).dp,    // 더 강한 음수로 이전 이미지와 붙게 함
            bottomOffset = (-20).dp  // 다음 이미지와도 붙도록 강화
        ),
        // 19번 이미지: 18번과 붙도록 topOffset 조정
        ImageType.LOGO_ZETACUBE to ImageSpacing(
            topOffset = (-10).dp,    // 더 음수로 조정하여 이전 이미지와 붙게 함
            bottomOffset = 0.dp
        )
    )
    
    /**
     * 특정 이미지 타입의 간격 설정을 반환
     * 설정되지 않은 타입은 기본값(0.dp) 반환
     */
    fun getSpacing(imageType: ImageType): ImageSpacing {
        return spacingMap[imageType] ?: ImageSpacing()
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

    Image(
        painter = painterResource(id = imageType.drawableRes),
        contentDescription = imageType.description,
        modifier = modifier.fillMaxWidth(),
        contentScale = contentScale
    )
}

/**
 * 간격이 적용된 이미지 컴포넌트
 * offset을 사용하여 음수 마진 효과 구현
 */
@Composable
fun SpacedImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    val spacing = ImageSpacingConfig.getSpacing(imageType)
    
    Image(
        painter = painterResource(id = imageType.drawableRes),
        contentDescription = imageType.description,
        modifier = modifier
            .fillMaxWidth()
            .offset(y = spacing.topOffset), // offset을 사용하여 음수 마진 효과
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
        OriginalSizeMonitoringContent(
            imageOrder = imageOrder,
            modifier = modifier
        )
    } else {
        // 기존 방식: 화면에 맞춰 이미지 크기 조정
        FitScreenMonitoringContent(
            imageOrder = imageOrder,
            scaleMode = scaleMode,
            modifier = modifier
        )
    }
}

/**
 * 원본 크기로 이미지를 표시하는 컴포넌트
 * 스크롤 가능하며 간격 설정 적용
 */
@Composable
private fun OriginalSizeMonitoringContent(
    imageOrder: List<ImageType>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        imageOrder.forEach { imageType ->
            SpacedImageItem(
                imageType = imageType,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

/**
 * 화면에 맞춰 이미지 크기를 조정하는 컴포넌트
 * 모든 이미지가 한 화면에 표시됨
 */
@Composable
private fun FitScreenMonitoringContent(
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
        verticalArrangement = Arrangement.Top
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
 * 간격 없이 이미지들이 붙어서 표시됨 (LazyColumn 사용)
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
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(0.dp) // 음수가 아닌 0으로 설정
    ) {
        items(imageOrder) { imageType ->
            SpacedImageItem(
                imageType = imageType,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}
