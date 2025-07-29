package com.nanodatacenter.nanodcmonitoring_compose.manager

import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageConfiguration
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType

/**
 * 이미지 순서와 설정을 관리하는 싱글톤 클래스
 * 확장성을 위해 설계되었으며, 추후 다양한 기기별 설정을 지원합니다.
 */
class ImageOrderManager private constructor() {
    
    private val configurations = mutableMapOf<DeviceType, ImageConfiguration>()
    private var currentDeviceType: DeviceType = DeviceType.DEFAULT
    
    companion object {
        @Volatile
        private var INSTANCE: ImageOrderManager? = null
        
        fun getInstance(): ImageOrderManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageOrderManager().also { INSTANCE = it }
            }
        }
    }
    
    init {
        // 기본 설정 초기화
        initializeDefaultConfigurations()
    }
    
    /**
     * 기본 설정들을 초기화합니다.
     */
    private fun initializeDefaultConfigurations() {
        // 기본 설정
        configurations[DeviceType.DEFAULT] = ImageConfiguration.createDefault(DeviceType.DEFAULT)
        
        // 추후 다른 기기별 설정 예시 (필요시 수정)
        // configurations[DeviceType.DEVICE_A] = createDeviceAConfiguration()
        // configurations[DeviceType.DEVICE_B] = createDeviceBConfiguration()
    }
    
    /**
     * 현재 기기 타입을 설정합니다.
     */
    fun setCurrentDeviceType(deviceType: DeviceType) {
        currentDeviceType = deviceType
    }
    
    /**
     * 현재 기기 타입을 반환합니다.
     */
    fun getCurrentDeviceType(): DeviceType = currentDeviceType
    
    /**
     * 현재 설정된 이미지 순서를 반환합니다.
     */
    fun getCurrentImageOrder(): List<ImageType> {
        return configurations[currentDeviceType]?.imageOrder 
            ?: ImageConfiguration.DEFAULT_ORDER
    }
    
    /**
     * 특정 기기 타입의 이미지 순서를 반환합니다.
     */
    fun getImageOrder(deviceType: DeviceType): List<ImageType> {
        return configurations[deviceType]?.imageOrder 
            ?: ImageConfiguration.DEFAULT_ORDER
    }
    
    /**
     * 새로운 기기 설정을 추가합니다.
     */
    fun addConfiguration(configuration: ImageConfiguration) {
        configurations[configuration.deviceType] = configuration
    }
    
    /**
     * 특정 기기의 이미지 순서를 업데이트합니다.
     */
    fun updateImageOrder(deviceType: DeviceType, newOrder: List<ImageType>) {
        val currentConfig = configurations[deviceType]
        if (currentConfig != null) {
            configurations[deviceType] = currentConfig.copy(imageOrder = newOrder)
        } else {
            configurations[deviceType] = ImageConfiguration(deviceType, newOrder)
        }
    }
    
    /**
     * 지원되는 모든 기기 타입을 반환합니다.
     */
    fun getSupportedDeviceTypes(): List<DeviceType> {
        return configurations.keys.toList()
    }
    
    /**
     * 특정 인덱스의 이미지 타입을 반환합니다.
     */
    fun getImageTypeAt(index: Int, deviceType: DeviceType = currentDeviceType): ImageType? {
        val order = getImageOrder(deviceType)
        return if (index in order.indices) order[index] else null
    }
    
    /**
     * 이미지 순서에서 특정 이미지 타입의 인덱스를 찾습니다.
     */
    fun getIndexOfImageType(imageType: ImageType, deviceType: DeviceType = currentDeviceType): Int {
        return getImageOrder(deviceType).indexOf(imageType)
    }
    
    /**
     * 현재 설정의 총 이미지 개수를 반환합니다.
     */
    fun getTotalImageCount(deviceType: DeviceType = currentDeviceType): Int {
        return getImageOrder(deviceType).size
    }
    
    /**
     * 설정을 초기화합니다. (테스트 또는 리셋 용도)
     */
    fun resetToDefault() {
        configurations.clear()
        initializeDefaultConfigurations()
        currentDeviceType = DeviceType.DEFAULT
    }
    
    // 추후 다른 기기별 설정 예시 메서드들
    // private fun createDeviceAConfiguration(): ImageConfiguration { ... }
    // private fun createDeviceBConfiguration(): ImageConfiguration { ... }
}
