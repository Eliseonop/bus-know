package com.tcontur.qrbus.client.services
import com.tcontur.qrbus.client.models.EmpresaModel
import retrofit2.Call
import retrofit2.http.GET

interface EmpresaService {
    @GET("/tracker/empresas") // Asegúrate de que este endpoint sea correcto
    fun getEmpresas(): Call<List<EmpresaModel>>
}