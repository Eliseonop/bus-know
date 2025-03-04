//package com.tcontur.qrbus.client
//import com.tcontur.login_tcontur.client.models.Boleto
import com.tcontur.qrbus.client.models.Boleto
import com.tcontur.qrbus.client.models.Recorrido
import com.tcontur.qrbus.client.models.Ruta
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("/api/boletos") // Asegúrate de que este endpoint sea correcto
    fun getBoletos(
        @Header("Authorization") authToken: String
    ): Call<List<Boleto>>


    @GET("/api/rutas")
    fun getRutas(
        @Header("Authorization") authToken: String
    ): Call<List<Ruta>>


    @GET("/api/recorridos")
    fun getRecorrido(
        @Header("Authorization") authToken: String
    ): Call<List<Recorrido>>
}