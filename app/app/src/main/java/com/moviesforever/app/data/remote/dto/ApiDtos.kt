package com.moviesforever.app.data.remote.dto

import com.moviesforever.app.data.remote.Market
import com.google.gson.annotations.SerializedName

data class RedeemRequest(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("market") val market: String = Market.ID
)

data class RedeemResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("username") val username: String? = null
)

data class SignedUrlRequest(
    @SerializedName("movieId") val movieId: String,
    @SerializedName("id") val id: String?,
    @SerializedName("username") val username: String?
)

data class SignedUrlResponse(
    @SerializedName("url") val url: String?,
    @SerializedName("allowed") val allowed: Boolean,
    @SerializedName("message") val message: String? = null
)
