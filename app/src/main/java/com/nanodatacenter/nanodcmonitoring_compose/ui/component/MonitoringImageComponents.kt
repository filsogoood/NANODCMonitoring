package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil

/**
 * 단일 모니터링 이미지를 표시하는 컴포넌트
 * 원본 크기 유지 및 다양한 스케일링 모드를 지원
 * 이미지 크기에 따라 Card 높이가 동적으로 조정됨
 */
@Composable
fun MonitoringImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    showDescription: Boolean = false,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
    useFixedHeight: Boolean = false,  // 기본값을 false로 변경
    fixedHeight: Int = 180,
    onClick: ((ImageType) -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    // 권장 스케일링 모드 적용 (원본 크기 우선)
    val finalScaleMode = if (scaleMode == ImageScaleUtil.ScaleMode.ORIGINAL) {
        ImageScaleUtil.getRecommendedScaleMode(isTablet, isLandscape).let {
            // 원본 크기를 최우선으로 설정
            ImageScaleUtil.ScaleMode.ORIGINAL
        }
    } else scaleMode
    
    val contentScale = ImageScaleUtil.getContentScale(finalScaleMode)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()  // 항상 컨텐츠 높이에 맞게 조정
            .let { mod ->
                onClick?.let { clickHandler ->
                    mod.clickable { clickHandler(imageType) }
                } ?: mod
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),  // Column도 컨텐츠 높이에 맞게 조정
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 이미지 컨테이너
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // 높이를 컨텐츠에 맞게 조정
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (finalScaleMode == ImageScaleUtil.ScaleMode.ORIGINAL) {
                    // 원본 크기일 때는 스크롤 가능하게 처리
                    if (useFixedHeight) {
                        // 고정 높이를 원하는 경우에만 스크롤 컨테이너 사용
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fixedHeight.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = imageType.drawableRes),
                                    contentDescription = imageType.description,
                                    modifier = Modifier.wrapContentSize(),
                                    contentScale = contentScale
                                )
                            }
                        }
                    } else {
                        // 고정 높이를 사용하지 않는 경우 이미지 크기에 맞게 표시
                        Image(
                            painter = painterResource(id = imageType.drawableRes),
                            contentDescription = imageType.description,
                            modifier = Modifier.wrapContentSize(),
                            contentScale = contentScale
                        )
                    }
                } else {
                    // 다른 스케일링 모드일 때
                    Image(
                        painter = painterResource(id = imageType.drawableRes),
                        contentDescription = imageType.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),  // 높이는 컨텐츠에 맞게
                        contentScale = contentScale
                    )
                }
            }
            
            if (showDescription) {
                Text(
                    text = imageType.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 모니터링 이미지들을 세로 목록으로 표시하는 컴포넌트
 * 원본 크기 이미지 표시 지원
 */
@Composable
fun MonitoringImageList(
    deviceType: DeviceType = DeviceType.DEFAULT,
    modifier: Modifier = Modifier,
    showDescriptions: Boolean = false,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
    useFixedHeight: Boolean = false,  // 기본값을 false로 변경
    fixedHeight: Int = 180,
    onImageClick: ((ImageType) -> Unit)? = null
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(imageOrder) { imageType ->
            MonitoringImageItem(
                imageType = imageType,
                showDescription = showDescriptions,
                scaleMode = scaleMode,
                useFixedHeight = useFixedHeight,
                fixedHeight = fixedHeight,
                onClick = onImageClick
            )
        }
    }
}

/**
 * 모니터링 이미지들을 가로 스크롤 목록으로 표시하는 컴포넌트
 * 원본 크기 이미지 표시 지원
 */
@Composable
fun MonitoringImageRow(
    deviceType: DeviceType = DeviceType.DEFAULT,
    modifier: Modifier = Modifier,
    itemWidth: Int = 250,
    showDescriptions: Boolean = false,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
    useFixedHeight: Boolean = false,  // 기본값을 false로 변경
    fixedHeight: Int = 180,
    onImageClick: ((ImageType) -> Unit)? = null
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(imageOrder) { imageType ->
            MonitoringImageItem(
                imageType = imageType,
                modifier = Modifier.width(itemWidth.dp),
                showDescription = showDescriptions,
                scaleMode = scaleMode,
                useFixedHeight = useFixedHeight,
                fixedHeight = fixedHeight,
                onClick = onImageClick
            )
        }
    }
}

/**
 * 모니터링 이미지들을 그리드로 표시하는 컴포넌트
 * 원본 크기 이미지 표시 지원
 */
@Composable
fun MonitoringImageGrid(
    deviceType: DeviceType = DeviceType.DEFAULT,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    showDescriptions: Boolean = false,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.ORIGINAL,
    useFixedHeight: Boolean = false,  // 기본값을 false로 변경
    fixedHeight: Int = 180,
    onImageClick: ((ImageType) -> Unit)? = null
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(imageOrder) { imageType ->
            MonitoringImageItem(
                imageType = imageType,
                showDescription = showDescriptions,
                scaleMode = scaleMode,
                useFixedHeight = useFixedHeight,
                fixedHeight = fixedHeight,
                onClick = onImageClick
            )
        }
    }
}

/**
 * 원본 크기 이미지를 완전히 스크롤 가능하게 표시하는 컴포넌트
 * 확대/축소 없이 이미지 원본 해상도로 표시
 */
@Composable
fun OriginalSizeImageViewer(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    showDescription: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (showDescription) {
            Text(
                text = imageType.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        // 완전히 스크롤 가능한 이미지 컨테이너
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = imageType.drawableRes),
                    contentDescription = imageType.description,
                    modifier = Modifier.wrapContentSize(),
                    contentScale = ContentScale.None // 원본 크기 유지
                )
            }
        }
    }
}

/**
 * 여러 이미지를 원본 크기로 연속 표시하는 컴포넌트
 * 각 이미지가 원본 크기로 표시되며 세로로 스크롤 가능
 */
@Composable
fun OriginalSizeImageList(
    deviceType: DeviceType = DeviceType.DEFAULT,
    modifier: Modifier = Modifier,
    showDescriptions: Boolean = true,
    onImageClick: ((ImageType) -> Unit)? = null
) {
    val imageOrderManager = ImageOrderManager.getInstance()
    val imageOrder = imageOrderManager.getImageOrder(deviceType)
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(imageOrder) { imageType ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .let { mod ->
                        onImageClick?.let { clickHandler ->
                            mod.clickable { clickHandler(imageType) }
                        } ?: mod
                    },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showDescriptions) {
                        Text(
                            text = imageType.description,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // 원본 크기 이미지 (스크롤 가능)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp) // 최대 높이 제한
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = imageType.drawableRes),
                                contentDescription = imageType.description,
                                modifier = Modifier.wrapContentSize(),
                                contentScale = ContentScale.None
                            )
                        }
                    }
                }
            }
        }
    }
}
