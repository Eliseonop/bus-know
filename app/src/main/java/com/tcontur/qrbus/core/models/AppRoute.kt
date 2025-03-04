package com.tcontur.qrbus.core.models
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute {
    @Serializable
    object LoginRoute : AppRoute()

    @Serializable
    object HomeRoute : AppRoute()

    @Serializable
    object MapRoute : AppRoute()

    @Serializable
    data class Detail(val name: String) : AppRoute()

    @Serializable
    object ReportRoute : AppRoute()
}