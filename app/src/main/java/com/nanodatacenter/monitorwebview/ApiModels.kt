package com.nanodatacenter.monitorwebview

import com.google.gson.annotations.SerializedName

/**
 * 로그인 요청 데이터 클래스
 */
data class LoginRequest(
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * 로그인 응답 데이터 클래스
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String
)

/**
 * API 응답의 기본 구조
 */
data class ApiResponse(
    @SerializedName("hardware_specs")
    val hardwareSpecs: List<ApiHardwareSpec>? = null,
    
    @SerializedName("nodes")
    val nodes: List<ApiNode>? = null,
    
    @SerializedName("node_usage")
    val nodeUsage: List<ApiNodeUsage>? = null,
    
    @SerializedName("nanodc")
    val nanodc: List<ApiNanoDC>? = null,
    
    @SerializedName("ndp_list")
    val ndpList: List<ApiNdpList>? = null,
    
    @SerializedName("all_users")
    val allUsers: List<ApiUser>? = null,
    
    @SerializedName("all_scores")
    val allScores: List<ApiScore>? = null
)

/**
 * 하드웨어 스펙 정보
 */
data class ApiHardwareSpec(
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
    val nanodcId: String
)

/**
 * 노드 정보
 */
data class ApiNode(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("node_id")
    val nodeId: String,
    
    @SerializedName("user_uuid")
    val userUuid: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("create_at")
    val createAt: String,
    
    @SerializedName("update_at")
    val updateAt: String,
    
    @SerializedName("node_name")
    val nodeName: String,
    
    @SerializedName("nanodc_id")
    val nanodcId: String
)

/**
 * 노드 사용량 정보
 */
data class ApiNodeUsage(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("node_id")
    val nodeId: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("cpu_usage_percent")
    val cpuUsagePercent: String,
    
    @SerializedName("mem_usage_percent")
    val memUsagePercent: String,
    
    @SerializedName("gpu_usage_percent")
    val gpuUsagePercent: String,
    
    @SerializedName("gpu_temp")
    val gpuTemp: String,
    
    @SerializedName("used_storage_gb")
    val usedStorageGb: String,
    
    @SerializedName("ssd_health_percent")
    val ssdHealthPercent: String,
    
    @SerializedName("gpu_vram_percent")
    val gpuVramPercent: String?
)

/**
 * 나노 데이터센터 정보
 */
data class ApiNanoDC(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nanodc_id")
    val nanodcId: String,
    
    @SerializedName("country")
    val country: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("ip")
    val ip: String,
    
    @SerializedName("latitude")
    val latitude: String,
    
    @SerializedName("longtitude")
    val longitude: String,
    
    @SerializedName("name")
    val name: String
)

/**
 * NDP 리스트 정보
 */
data class ApiNdpList(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("node_id")
    val nodeId: String,
    
    @SerializedName("from")
    val from: String,
    
    @SerializedName("to")
    val to: String,
    
    @SerializedName("amount")
    val amount: String,
    
    @SerializedName("tx_hash")
    val txHash: String,
    
    @SerializedName("date")
    val date: String
)

/**
 * 사용자 정보
 */
data class ApiUser(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userUuid")
    val userUuid: String?,
    
    @SerializedName("userId")
    val userId: String?,
    
    @SerializedName("password")
    val password: String?,
    
    @SerializedName("user_name")
    val userName: String,
    
    @SerializedName("ndp_address")
    val ndpAddress: String?
)

/**
 * NDP 점수 정보
 */
data class ApiScore(
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
