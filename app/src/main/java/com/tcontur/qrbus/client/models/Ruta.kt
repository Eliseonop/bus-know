package com.tcontur.qrbus.client.models


data class Tipo(
    val id: Int,
    val orden: Int,
    val nombre: String
)

data class Plantilla(
    val id: Int,
    val nombre: String
)

data class Ruta(
    val id: Int,
    val activo: Boolean,
    val codigo: String,
    val fin: String,
    val inicio: String,
    val tipo: Tipo,
    val plantilla: Plantilla
)
