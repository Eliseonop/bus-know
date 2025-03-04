package com.tcontur.qrbus.client

import ApiService
import android.util.Log
import com.tcontur.qrbus.client.services.AuthService
import com.tcontur.qrbus.client.services.EmpresaService
import com.tcontur.qrbus.core.login.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceClient {
    private const val BASE_URL = "https://urbanito-23lnu3rcea-uc.a.run.app"

//    val sessionManager = SessionManager(context)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log completo del request/response
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

//    private val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

    private fun initRetrofit(newBaseUrl: String = BASE_URL): Retrofit {
        return Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getAuthService(company: String): AuthService? {
        val baseUrl = "https://${company}-23lnu3rcea-uc.a.run.app"
        val retrofit = initRetrofit(baseUrl)

        try {
            return retrofit.create(AuthService::class.java)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }
    }

    fun getEmpresaService(): EmpresaService? {
        val retrofit = initRetrofit()
        try {
            return retrofit.create(EmpresaService::class.java)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }
    }

    fun getApiService(sm: SessionManager): ApiService? {
        val company = sm.getEmpresa()?.codigo
        val baseUrl = "https://${company}-23lnu3rcea-uc.a.run.app"
        Log.e("URL", baseUrl)
        val retrofit = initRetrofit(baseUrl)
        try {
            return retrofit.create(ApiService::class.java)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }
    }

}