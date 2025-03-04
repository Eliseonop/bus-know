package com.tcontur.qrbus.client.services

import com.tcontur.qrbus.client.models.LoginRequest
import com.tcontur.qrbus.client.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/api/token-auth")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}