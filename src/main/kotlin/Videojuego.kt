import kotlinx.serialization.Serializable

// Anotamos la data class indicando que es serializable para el compilador de Kotlin
@Serializable
data class Videojuego(
    val id: Int,
    val titulo: String,
    val genero: String,
    val anio: Int,
    val nota: Double
)
