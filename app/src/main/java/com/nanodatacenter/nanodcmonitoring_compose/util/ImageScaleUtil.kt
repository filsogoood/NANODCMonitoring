package com.nanodatacenter.nanodcmonitoring_compose.util

import androidx.compose.ui.layout.ContentScale
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType

/**
 * 이미지 스케일링 관련 유틸리티
 * 다양한 디바이스에서 일관된 이미지 표시를 위한 스케일링 옵션을 제공
 * 
 * 사용 예시:
 * ```kotlin
 * // 특정 이미지 타입의 스케일 팩터 가져오기
 * val scale = ImageScaleUtil.getImageScaleFactor(ImageType.DEEPSEEK) // returns 0.95f
 * 
 * // 이미지 타입이 커스텀 스케일을 사용하는지 확인
 * if (ImageScaleUtil.hasCustomScale(imageType)) {
 *     // 커스텀 스케일 적용 로직
 * }
 * ```
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
     * 특정 이미지 타입들에 대한 스케일 설정
     * 확장성을 위해 Map으로 관리하여 추후 다른 이미지 타입 추가 가능
     * 
     * 새로운 이미지 타입에 커스텀 스케일을 추가하려면:
     * 1. 아래 Map에 ImageType과 스케일 팩터(Float) 추가
     * 2. 0.95f = 95% 크기, 1.2f = 120% 크기 등으로 설정
     * 
     * 현재 95% 스케일 적용 이미지들:
     * - DEEPSEEK 계열 (DEEPSEEK, DEEPSEEK_NONE)
     * - AETHIR 계열 (AETHIR, AETHIR_NONE)
     * - FILECOIN 계열 (FILECOIN, FILECOIN_NONE_1, FILECOIN_NONE_2)
     */
    private val CUSTOM_SCALE_MAP = mapOf(
        ImageType.DEEPSEEK to 0.95f,
        ImageType.DEEPSEEK_NONE to 0.95f,
        ImageType.AETHIR to 0.95f,
        ImageType.AETHIR_NONE to 0.95f,
        ImageType.FILECOIN to 0.95f,
        ImageType.FILECOIN_NONE_1 to 0.95f,
        ImageType.FILECOIN_NONE_2 to 0.95f
    )
    
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
     * 특정 이미지 타입에 대한 스케일 팩터 반환
     * @param imageType 이미지 타입
     * @return 해당 이미지 타입의 스케일 팩터 (기본값: 1.0f)
     */
    fun getImageScaleFactor(imageType: ImageType): Float {
        return CUSTOM_SCALE_MAP[imageType] ?: 1.0f
    }
    
    /**
     * 이미지 타입이 커스텀 스케일을 사용하는지 확인
     * @param imageType 확인할 이미지 타입
     * @return 커스텀 스케일 사용 여부
     */
    fun hasCustomScale(imageType: ImageType): Boolean {
        return CUSTOM_SCALE_MAP.containsKey(imageType)
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
