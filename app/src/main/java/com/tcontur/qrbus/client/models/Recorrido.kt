package com.tcontur.qrbus.client.models

data class RutaRecor(
    val id: Int,
    val codigo: String,
    val plantilla_actual: Int
)

data class PuntoTrayecto(
    val orden: Int,
    val latitud: Double,
    val longitud: Double
)

data class Recorrido(
    val id: Int,
    val ruta: RutaRecor,
    val lado: Boolean,
    val trayecto: List<PuntoTrayecto>
)
