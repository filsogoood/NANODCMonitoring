package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * 노드 사용량 정보 데이터 클래스
 */
data class NodeUsage(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("node_id")
    val nodeId: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("cpu_usage_percent")
    val cpuUsagePercent: String?,
    
    @SerializedName("mem_usage_percent")
    val memUsagePercent: String?,
    
    @SerializedName("cpu_temp")
    val cpuTemp: String?,
    
    @SerializedName("gpu_usage_percent")
    val gpuUsagePercent: String?,
    
    @SerializedName("gpu_temp")
    val gpuTemp: String?,
    
    @SerializedName("used_storage_gb")
    val usedStorageGb: String?,
    
    @SerializedName("ssd_health_percent")
    val ssdHealthPercent: String?, // API에서 null이 올 수 있으므로 nullable로 변경
    
    @SerializedName("gpu_vram_percent")
    val gpuVramPercent: String?,
    
    @SerializedName("harddisk_used_percent")
    val harddiskUsedPercent: String?,
    
    @SerializedName("stage_used")
    val stageUsed: String?
)
