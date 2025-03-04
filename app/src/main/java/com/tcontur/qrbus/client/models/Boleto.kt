package com.tcontur.qrbus.client.models


data class Boleto(
    val id: Int,
    val orden: Int,
    val nombre: String,
    val serie: String,
    val tarifa: Float,
    val color: String,
    val activo: Boolean,
)