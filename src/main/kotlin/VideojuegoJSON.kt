import java.nio.file.Files
import java.nio.file.Path

import kotlinx.serialization.json.*

fun leerVideojuegosJSON(ruta: Path): List<Videojuego> {
    var videojuegos: List<Videojuego> = emptyList()

    if (!Files.isReadable(ruta)) {
        println("Error: No se puede leer el fichero en la ruta: $ruta")
    } else {
        try {
            // Leemos el contenido completo del JSON como String
            val jsonString = Files.readString(ruta)

            // Convertimos de texto JSON a una lista de objetos Videojuego
            videojuegos = Json.decodeFromString<List<Videojuego>>(jsonString)
            println("--- Información leída con éxito de: $ruta")
        } catch (e: Exception) {
            println("Error al leer el fichero JSON: ${e.message}")
        }
    }
    return videojuegos
}
