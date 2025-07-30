package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * 노드 정보 데이터 클래스
 */
data class Node(
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
