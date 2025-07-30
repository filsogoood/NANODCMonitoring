package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * API 응답의 최상위 데이터 클래스
 */
data class ApiResponse(
    @SerializedName("hardware_specs")
    val hardwareSpecs: List<HardwareSpec>,
    
    @SerializedName("nodes")
    val nodes: List<Node>,
    
    @SerializedName("scores")
    val scores: List<Score>,
    
    @SerializedName("ndpListFiltered")
    val ndpListFiltered: List<NdpTransaction>,
    
    @SerializedName("nanodc")
    val nanodc: List<NanoDc>,
    
    @SerializedName("node_usage")
    val nodeUsage: List<NodeUsage>
)
