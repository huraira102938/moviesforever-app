package com.moviesforever.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RedeemRequest(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    // Tells the Worker this app saves the corrected `id` it returns (see RedeemResponse.id).
    @SerializedName("useServerId") val useServerId: Boolean = true
)

data class RedeemResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("username") val username: String? = null,
    // The code as stored, after the Worker fixes capitals and hidden characters.
    @SerializedName("id") val id: String? = null
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
