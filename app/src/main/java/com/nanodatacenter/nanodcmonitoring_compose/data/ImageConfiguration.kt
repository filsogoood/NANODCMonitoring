package com.nanodatacenter.nanodcmonitoring_compose.data

/**
 * 기기별 이미지 순서 설정을 관리하는 데이터 클래스
 */
data class ImageConfiguration(
    val deviceType: DeviceType,
    val imageOrder: List<ImageType>
) {
    companion object {
        /**
         * 기본 이미지 순서 (요청사항 기준)
         */
        val DEFAULT_ORDER = listOf(
            ImageType.NDP_INFO,
            ImageType.NODE_INFO,
            ImageType.ONBOARDING,
            ImageType.SWITCH_100G,
            ImageType.NODE_MINER,
            ImageType.POSTWORKER,
            ImageType.SUPRA,
            ImageType.SUPRA_NONE_1,
            ImageType.SUPRA_NONE_2,
            ImageType.SUPRA_NONE_3,
            ImageType.DEEPSEEK,
            ImageType.DEEPSEEK_NONE,
            ImageType.AETHIR,
            ImageType.AETHIR_NONE,
            ImageType.FILECOIN,
            ImageType.FILECOIN_NONE_1,
            ImageType.FILECOIN_NONE_2,
            ImageType.NOT_STORAGE,
            ImageType.UPS_CONTROLLER,
            ImageType.LOGO_ZETACUBE
        )

        /**
         * 기본 설정을 생성합니다.
         */
        fun createDefault(deviceType: DeviceType = DeviceType.DEFAULT): ImageConfiguration {
            return ImageConfiguration(deviceType, DEFAULT_ORDER)
        }
    }
}

/**
 * 기기 타입을 정의하는 enum
 * 추후 다른 기기별로 다른 순서가 필요할 때 사용
 */
enum class DeviceType(val displayName: String) {
    DEFAULT("기본"),
    DEVICE_A("기기 A"),
    DEVICE_B("기기 B"),
    DEVICE_C("기기 C");
    
    companion object {
        fun fromString(value: String): DeviceType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: DEFAULT
        }
    }
}
