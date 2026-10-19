import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun leerDatosCSV(ruta: Path): MutableList<Videojuego> {

    // Lista donde se guardan los videojuegos leídos del CSV
    var videojuegos = mutableListOf<Videojuego>()

    if (!Files.isReadable(ruta)) {
        println("Error: No se puede leer el fichero en la ruta: $ruta")
        return videojuegos
    }

    val reader = csvReader {
        // Configuración CSV indicando que el separador es ';'
        delimiter = ';'
    }

    // Leemos todas las filas del fichero CSV
    val filas: List<List<String>> = reader.readAll(ruta.toFile())

    // Convertimos cada fila en un objeto Videojuego
    videojuegos = filas.mapNotNull { columnas ->

        if (columnas.size >= 5) {
            try {
                // Convertimos las columnas de texto a los tipos correspondientes
                val id = columnas[0].toInt()
                val titulo = columnas[1]
                val genero = columnas[2]
                val anio = columnas[3].toInt()
                val nota = columnas[4].toDouble()

                Videojuego(id, titulo, genero, anio, nota)

            } catch (e: Exception) {
                println("Fila inválida ignorada: $columnas -> Error: ${e.message}")
                null
            }

        } else {
            println("Fila con formato incompleto ignorada: $columnas")
            null
        }
    }.toMutableList()

    println("--- Información leída de: $ruta")
    return videojuegos
}

fun guardarCSV(ruta: Path, videojuegos: List<Videojuego>) {
    try {
        // Creamos las carpetas necesarias si todavía no existen
        Files.createDirectories(ruta.parent)

        val fichero: File = ruta.toFile()

        csvWriter {
            delimiter = ';'
        }.writeAll(
            videojuegos.map { videojuego ->
                listOf(
                    videojuego.id.toString(),
                    videojuego.titulo,
                    videojuego.genero,
                    videojuego.anio.toString(),
                    videojuego.nota.toString()
                )
            },
            fichero // Fichero donde se escribirán los datos
        )

        println("--- Información guardada con éxito en: $fichero")

    } catch (e: Exception) {
        println("Error al escribir el fichero CSV: ${e.message}")
    }
}