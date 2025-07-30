package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * NDP 트랜잭션 정보 데이터 클래스
 */
data class NdpTransaction(
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
