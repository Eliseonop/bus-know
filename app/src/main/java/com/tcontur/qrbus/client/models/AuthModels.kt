package com.tcontur.qrbus.client.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Int,
    val message: String
)