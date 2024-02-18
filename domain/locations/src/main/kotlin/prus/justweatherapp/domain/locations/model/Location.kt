package prus.justweatherapp.domain.locations.model

data class Location(
    val id: String,
    val city: String,
    val adminName: String? = null,
    val country: String? = null,
    val displayName: String? = null,
    var orderIndex: Int? = null,
    val lng: Double,
    val lat: Double,
)
