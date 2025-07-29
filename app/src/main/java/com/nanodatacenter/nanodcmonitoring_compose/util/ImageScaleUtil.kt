package com.nanodatacenter.nanodcmonitoring_compose.util

import androidx.compose.ui.layout.ContentScale

/**
 * 이미지 스케일링 관련 유틸리티
 * 다양한 디바이스에서 일관된 이미지 표시를 위한 스케일링 옵션을 제공
 */
object ImageScaleUtil {
    
    /**
     * 이미지 스케일링 모드 정의
     */
    enum class ScaleMode {
        /** 원본 크기 유지 (잘림 가능) */
        ORIGINAL,
        /** 원본 비율 유지하면서 컨테이너에 맞추기 */
        FIT_ASPECT_RATIO,
        /** 컨테이너 폭에 맞추고 비율 유지 */
        FIT_WIDTH,
        /** 컨테이너 높이에 맞추고 비율 유지 */
        FIT_HEIGHT,
        /** 컨테이너를 완전히 채우기 (비율 무시) */
        FILL_BOUNDS,
        /** 컨테이너보다 클 때만 축소 */
        INSIDE
    }
    
    /**
     * ScaleMode를 ContentScale로 변환
     */
    fun getContentScale(scaleMode: ScaleMode): ContentScale {
        return when (scaleMode) {
            ScaleMode.ORIGINAL -> ContentScale.None
            ScaleMode.FIT_ASPECT_RATIO -> ContentScale.Fit
            ScaleMode.FIT_WIDTH -> ContentScale.FillWidth
            ScaleMode.FIT_HEIGHT -> ContentScale.FillHeight
            ScaleMode.FILL_BOUNDS -> ContentScale.FillBounds
            ScaleMode.INSIDE -> ContentScale.Inside
        }
    }
    
    /**
     * 디바이스 타입과 화면 크기에 따른 권장 스케일링 모드 반환
     */
    fun getRecommendedScaleMode(
        isTablet: Boolean = false,
        isLandscape: Boolean = false
    ): ScaleMode {
        return when {
            isTablet && isLandscape -> ScaleMode.ORIGINAL
            isTablet -> ScaleMode.FIT_WIDTH
            isLandscape -> ScaleMode.FIT_HEIGHT
            else -> ScaleMode.ORIGINAL
        }
    }
}
