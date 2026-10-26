import java.nio.file.Files
import java.nio.file.Path

fun convertir(
    origen: Path,
    destino: Path,
    leer: (Path) -> List<Videojuego>,
    escribir: (Path, List<Videojuego>) -> Unit
) {
    // Comprobamos que el fichero de origen existe antes de leerlo
    if (Files.notExists(origen)) {
        println("Error: no existe el fichero de origen: $origen")
        return
    }

    try {
        // Origen -> objetos Videojuego en memoria
        val videojuegos = leer(origen)

        if (videojuegos.isEmpty()) {
            println("No se han leído videojuegos de $origen, no se genera el fichero.")
            return
        }

        // Objetos Videojuego en memoria -> formato destino
        escribir(destino, videojuegos)

        println("Fichero generado: $destino")
        println("Registros convertidos: ${videojuegos.size}")

    } catch (e: Exception) {
        println("Error durante la conversión: ${e.message}")
    }
}

fun menuConversiones(rutaDatosIni: Path, rutaDatosFin: Path) {

    // Ficheros de origen (datos_ini) de cada formato
    val csvIni = rutaDatosIni.resolve("videojuegos.csv")
    val jsonIni = rutaDatosIni.resolve("videojuegos.json")
    val xmlIni = rutaDatosIni.resolve("videojuegos.xml")

    // Ficheros de destino (datos_fin) de cada formato
    val csvFin = rutaDatosFin.resolve("videojuegos.csv")
    val jsonFin = rutaDatosFin.resolve("videojuegos.json")
    val xmlFin = rutaDatosFin.resolve("videojuegos.xml")

    var volver = false

    // Submenú de conversiones: se repite hasta volver al menú principal
    while (!volver) {
        println()
        println("--- Conversiones ---")
        println("1. CSV  → JSON")
        println("2. CSV  → XML")
        println("3. JSON → CSV")
        println("4. JSON → XML")
        println("5. XML  → CSV")
        println("6. XML  → JSON")
        println("0. Volver")
        print("Elige una opción: ")

        when (readlnOrNull()?.toIntOrNull()) {

            1 -> convertir(csvIni, jsonFin, ::leerDatosCSV, ::escribirVideojuegosJSON)
            2 -> convertir(csvIni, xmlFin, ::leerDatosCSV, ::escribirVideojuegosXML)
            3 -> convertir(jsonIni, csvFin, ::leerVideojuegosJSON, ::guardarCSV)
            4 -> convertir(jsonIni, xmlFin, ::leerVideojuegosJSON, ::escribirVideojuegosXML)
            5 -> convertir(xmlIni, csvFin, ::leerVideojuegosXML, ::guardarCSV)
            6 -> convertir(xmlIni, jsonFin, ::leerVideojuegosXML, ::escribirVideojuegosJSON)

            // Volvemos al menú principal sin cerrar el programa
            0 -> volver = true

            // Avisamos si se introduce una opción que no existe
            else -> println("Opción no válida.")
        }
    }
}