package com.nanodatacenter.nanodcmonitoring_compose.network.model

import com.google.gson.annotations.SerializedName

/**
 * 나노 데이터 센터 정보 데이터 클래스
 */
data class NanoDc(
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
