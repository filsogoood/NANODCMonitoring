package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * 성능 점수 정보 데이터 클래스
 */
data class Score(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("node_id")
    val nodeId: String,
    
    @SerializedName("cpu_score")
    val cpuScore: String,
    
    @SerializedName("gpu_score")
    val gpuScore: String,
    
    @SerializedName("ssd_score")
    val ssdScore: String,
    
    @SerializedName("ram_score")
    val ramScore: String,
    
    @SerializedName("network_score")
    val networkScore: String,
    
    @SerializedName("hardware_health_score")
    val hardwareHealthScore: String,
    
    @SerializedName("total_score")
    val totalScore: String,
    
    @SerializedName("average_score")
    val averageScore: String
)
