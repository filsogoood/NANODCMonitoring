package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * 하드웨어 사양 정보 데이터 클래스
 */
data class HardwareSpec(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("node_id")
    val nodeId: String,
    
    @SerializedName("cpu_model")
    val cpuModel: String,
    
    @SerializedName("cpucores")
    val cpuCores: String,
    
    @SerializedName("gpu_model")
    val gpuModel: String,
    
    @SerializedName("gpu_vram_gb")
    val gpuVramGb: String,
    
    @SerializedName("total_ram_gb")
    val totalRamGb: String,
    
    @SerializedName("storage_type")
    val storageType: String,
    
    @SerializedName("storage_total_gb")
    val storageTotalGb: String,
    
    @SerializedName("cpu_count")
    val cpuCount: String,
    
    @SerializedName("gpu_count")
    val gpuCount: String,
    
    @SerializedName("nvme_count")
    val nvmeCount: String,
    
    @SerializedName("nanodc_id")
    val nanodcId: String,
    
    @SerializedName("total_harddisk_gb")
    val totalHarddiskGb: String?
)
