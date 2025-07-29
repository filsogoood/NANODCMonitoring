package com.nanodatacenter.nanodcmonitoring_compose.util

import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageConfiguration
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager

/**
 * 이미지 순서 설정을 쉽게 할 수 있는 유틸리티 클래스
 * 기기별 커스텀 설정을 만들 때 사용합니다.
 */
object ImageConfigurationHelper {
    
    /**
     * 기기 A용 커스텀 이미지 순서 생성 예시
     * 실제 요구사항에 맞게 수정하세요.
     */
    fun createDeviceAConfiguration(): ImageConfiguration {
        val customOrder = listOf(
            ImageType.LOGO_ZETACUBE,  // 로고를 첫 번째로
            ImageType.NDP_INFO,
            ImageType.NODE_INFO,
            ImageType.SWITCH_100G,
            ImageType.ONBOARDING,
            ImageType.NODE_MINER,
            ImageType.POSTWORKER,
            ImageType.SUPRA,
            ImageType.DEEPSEEK,
            ImageType.AETHIR,
            ImageType.FILECOIN,
            ImageType.NOT_STORAGE,
            ImageType.UPS_CONTROLLER,
            // 나머지는 제외
        )
        return ImageConfiguration(DeviceType.DEVICE_A, customOrder)
    }
    
    /**
     * 기기 B용 커스텀 이미지 순서 생성 예시
     */
    fun createDeviceBConfiguration(): ImageConfiguration {
        val customOrder = listOf(
            ImageType.UPS_CONTROLLER,  // UPS를 첫 번째로
            ImageType.SWITCH_100G,
            ImageType.NODE_MINER,
            ImageType.SUPRA,
            ImageType.SUPRA_NONE_1,
            ImageType.DEEPSEEK,
            ImageType.AETHIR,
            ImageType.FILECOIN,
            ImageType.LOGO_ZETACUBE,
            // 기타 이미지들은 기본 순서
        )
        return ImageConfiguration(DeviceType.DEVICE_B, customOrder)
    }
    
    /**
     * 기기 C용 커스텀 이미지 순서 생성 예시
     * 로고를 맨 마지막으로 배치하는 경우
     */
    fun createDeviceCConfiguration(): ImageConfiguration {
        val customOrder = listOf(
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
            ImageType.LOGO_ZETACUBE  // 맨 마지막에 로고
        )
        return ImageConfiguration(DeviceType.DEVICE_C, customOrder)
    }
    
    /**
     * 이미지 매니저에 모든 기기별 설정을 적용합니다.
     */
    fun applyAllConfigurations() {
        val manager = ImageOrderManager.getInstance()
        
        // 커스텀 설정들을 매니저에 추가
        manager.addConfiguration(createDeviceAConfiguration())
        manager.addConfiguration(createDeviceBConfiguration())
        manager.addConfiguration(createDeviceCConfiguration())
        
        // 추가 기기 설정이 필요한 경우 여기에 추가
        // manager.addConfiguration(createNewDeviceConfiguration())
    }
    
    /**
     * 특정 기기 타입의 순서를 동적으로 변경합니다.
     */
    fun updateOrderForDevice(deviceType: DeviceType, newOrder: List<ImageType>) {
        val manager = ImageOrderManager.getInstance()
        manager.updateImageOrder(deviceType, newOrder)
    }
    
    /**
     * 현재 설정된 기기의 순서를 출력합니다. (디버깅 용도)
     */
    fun printCurrentOrder(deviceType: DeviceType = DeviceType.DEFAULT): String {
        val manager = ImageOrderManager.getInstance()
        val order = manager.getImageOrder(deviceType)
        
        return buildString {
            appendLine("=== ${deviceType.displayName} 기기의 이미지 순서 ===")
            order.forEachIndexed { index, imageType ->
                appendLine("${index + 1}. ${imageType.description}")
            }
            appendLine("총 ${order.size}개 이미지")
        }
    }
    
    /**
     * 새로운 기기 타입을 위한 커스텀 순서를 쉽게 생성하는 헬퍼 함수
     */
    fun createCustomConfiguration(
        deviceType: DeviceType,
        imageOrder: List<ImageType>
    ): ImageConfiguration {
        return ImageConfiguration(deviceType, imageOrder)
    }
    
    /**
     * 기본 순서에서 특정 이미지들만 제외하는 설정을 생성
     */
    fun createConfigurationWithExclusions(
        deviceType: DeviceType,
        excludeImages: List<ImageType>
    ): ImageConfiguration {
        val filteredOrder = ImageConfiguration.DEFAULT_ORDER.filter { imageType ->
            !excludeImages.contains(imageType)
        }
        return ImageConfiguration(deviceType, filteredOrder)
    }
    
    /**
     * 기본 순서에서 특정 이미지를 맨 앞으로 이동시키는 설정을 생성
     */
    fun createConfigurationWithPriority(
        deviceType: DeviceType,
        priorityImages: List<ImageType>
    ): ImageConfiguration {
        val remainingImages = ImageConfiguration.DEFAULT_ORDER.filter { imageType ->
            !priorityImages.contains(imageType)
        }
        val newOrder = priorityImages + remainingImages
        return ImageConfiguration(deviceType, newOrder)
    }
    
    /**
     * 모든 기기의 설정을 기본값으로 리셋합니다.
     */
    fun resetAllToDefault() {
        val manager = ImageOrderManager.getInstance()
        manager.resetToDefault()
        applyAllConfigurations()
    }
}
