import java.nio.file.Files
import java.nio.file.Path

import kotlinx.serialization.*
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

fun escribirVideojuegosJSON(ruta: Path, videojuegos: List<Videojuego>) {
    // Creamos las carpetas necesarias si todavía no existen
    Files.createDirectories(ruta.parent)

    // Configuramos el formateador con la opción 'prettyPrint' activa
    val jsonConfigurador = Json { prettyPrint = true }
    val jsonString = jsonConfigurador.encodeToString(videojuegos)

    Files.writeString(ruta, jsonString)
}