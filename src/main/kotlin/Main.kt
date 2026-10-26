import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

fun main() {

    println("CREACIÓN DE RUTAS PROYECTO")
    println("Creando rutas...")

    val rutaDatos = Path.of("proyecto", "datos")
    val rutaDatosIni = Path.of("proyecto", "datos", "datos_ini")
    val rutaDatosFin = Path.of("proyecto", "datos", "datos_fin")

    // Rutas de los ficheros CSV de entrada y salida
    val rutaEntrada = rutaDatosIni.resolve("videojuegos.csv")
    val rutaSalida = rutaDatosFin.resolve("videojuegos.csv")

    // Ruta del fichero XML desde el que se puede importar el catálogo
    val rutaXML = rutaDatosIni.resolve("videojuegos.xml")

    // Ruta del fichero JSON desde el que se puede importar el catálogo
    val rutaJSON = rutaDatosIni.resolve("videojuegos.json")

    try {

        // Creamos la carpeta de datos iniciales si no existe
        if (Files.notExists(rutaDatosIni)) {
            println("Creación de ruta para DATOS_INI")
            Files.createDirectories(rutaDatosIni)
        }

        // Creamos la carpeta donde guardaremos los datos modificados
        if (Files.notExists(rutaDatosFin)) {
            println("Creación de ruta para DATOS_FIN")
            Files.createDirectories(rutaDatosFin)
        }

        println()
        println("MOSTRANDO ESTRUCTURA DE DIRECTORIOS Y FICHEROS")

        Files.walk(rutaDatos).use { stream -> // Recorre lo que hay dentro de una carpeta
            stream.sorted().forEach { path -> // Ordena las rutas
                val profundidad = path.nameCount - rutaDatos.nameCount // Calcular profundidad
                val indentacion = "\t".repeat(profundidad) // Crear sangrado

                val prefijo =
                    if (Files.isDirectory(path))
                        "[DIR]"
                    else
                        "[FILE]"

                if (profundidad == 0) {
                    println("[DIR] ${path.fileName}")
                } else {
                    println("$indentacion$prefijo ${path.fileName}")
                }
            }
        }

    } catch (e: IOException) {
        println("Error: ${e.message}")
        return
    }

    // Elegimos el fichero a leer: el de datos_fin si ya existe, si no el de datos_ini
    val rutaLectura = when {
        Files.exists(rutaSalida) -> rutaSalida
        Files.exists(rutaEntrada) -> rutaEntrada
        else -> {
            println("Error: no existe el fichero inicial: $rutaEntrada")
            println("Coloca videojuegos.csv dentro de la carpeta datos_ini.")
            return
        }
    }

    // Leemos los videojuegos del fichero CSV
    var videojuegos = leerDatosCSV(rutaLectura)

    // Menú principal: se repite hasta que el usuario elija salir
    while (true) {
        println()
        println("===== CATÁLOGO DE VIDEOJUEGOS =====")
        println("1. Gestión CSV")
        println("2. Leer catálogo desde XML")
        println("3. Leer catálogo desde JSON")
        println("4. Conversiones entre formatos")
        println("5. Gestión fichero BIN")
        println("0. Salir")
        println("====================================")
        print("Elige una opción: ")

        when (readlnOrNull()?.toIntOrNull()) {

            1 -> {
                var volver = false

                // Submenú de gestión CSV: se repite hasta volver al menú principal
                while (!volver) {
                    println()
                    println("--- Gestión CSV ---")
                    println("1. Listar todos los videojuegos")
                    println("2. Buscar videojuego por ID")
                    println("3. Añadir nuevo videojuego")
                    println("4. Modificar nota de un videojuego")
                    println("5. Eliminar videojuego")
                    println("0. Volver al menú principal")
                    print("Elige una opción: ")

                    when (readlnOrNull()?.toIntOrNull()) {

                        // Mostramos todos los videojuegos de la lista
                        1 -> listarVideojuegos(videojuegos)

                        // Buscamos un videojuego utilizando su ID
                        2 -> {
                            print("Introduce el ID: ")
                            val id = readlnOrNull()?.toIntOrNull()

                            if (id == null) {
                                println("El ID debe ser un número entero.")
                            } else {
                                buscarPorId(videojuegos, id)
                            }
                        }

                        // Añadimos un videojuego y guardamos la lista en el CSV
                        3 -> {
                            // Guardamos el tamaño para detectar si se ha añadido un videojuego
                            val cantidadAnterior = videojuegos.size

                            añadirVideojuego(videojuegos)

                            // Solo guardamos si la lista ha cambiado
                            if (videojuegos.size > cantidadAnterior) {
                                guardarCSV(rutaSalida, videojuegos)
                            }
                        }

                        // Modificamos la nota de un videojuego
                        4 -> {
                            print("Introduce el ID: ")
                            val id = readlnOrNull()?.toIntOrNull()

                            if (id == null) {
                                println("El ID debe ser un número entero.")
                            } else {
                                print("Introduce la nueva nota: ")
                                // Cambiamos la coma por un punto para aceptar notas como 8,5
                                val nota = readlnOrNull()?.replace(',', '.')?.toDoubleOrNull()

                                if (nota == null) {
                                    println("La nota debe ser un número válido.")
                                } else {
                                    // La función devuelve la lista con la nota actualizada
                                    val actualizada = modificarNota(videojuegos, id, nota)

                                    // Guardamos solo si la lista ha cambiado
                                    if (actualizada != videojuegos) {
                                        videojuegos = actualizada
                                        guardarCSV(rutaSalida, videojuegos)
                                    }
                                }
                            }
                        }

                        // Eliminamos un videojuego después de confirmar la operación
                        5 -> {
                            print("Introduce el ID que quieres eliminar: ")
                            val id = readlnOrNull()?.toIntOrNull()

                            if (id == null) {
                                println("El ID debe ser un número entero.")
                            } else {
                                // La función pedirá confirmación antes de eliminar
                                val cantidadAnterior = videojuegos.size

                                videojuegos = eliminarVideojuego(videojuegos, id)

                                // Guardamos solo si realmente se ha eliminado
                                if (videojuegos.size < cantidadAnterior) {
                                    guardarCSV(rutaSalida, videojuegos)
                                }
                            }
                        }

                        // Volvemos al menú principal sin cerrar el programa
                        0 -> volver = true

                        // Avisamos si se introduce una opción que no existe
                        else -> println("Opción no válida.")
                    }
                }
            }

            // Leemos los videojuegos del XML y los integramos en el catálogo
            2 -> {
                val importados = leerVideojuegosXML(rutaXML)

                if (importados.isEmpty()) {
                    println("No se han leído videojuegos del fichero XML.")
                } else {
                    // Mostramos los videojuegos leídos del XML
                    listarVideojuegos(importados)

                    // Añadimos solo los que no existen ya en el catálogo (por ID)
                    val nuevos = integrarCatalogo(videojuegos, importados)
                    println("Videojuegos nuevos integrados: $nuevos")

                    // Guardamos solo si se ha integrado algún videojuego
                    if (nuevos > 0) {
                        guardarCSV(rutaSalida, videojuegos)
                    }
                }
            }

            // Leemos los videojuegos del JSON y los integramos en el catálogo
            3 -> {
                val importados = leerVideojuegosJSON(rutaJSON)

                if (importados.isEmpty()) {
                    println("No se han leído videojuegos del fichero JSON.")
                } else {
                    // Mostramos los videojuegos leídos del JSON
                    listarVideojuegos(importados)

                    // Añadimos solo los que no existen ya en el catálogo (por ID)
                    val nuevos = integrarCatalogo(videojuegos, importados)
                    println("Videojuegos nuevos integrados: $nuevos")

                    // Guardamos solo si se ha integrado algún videojuego
                    if (nuevos > 0) {
                        guardarCSV(rutaSalida, videojuegos)
                    }
                }
            }

            // Abrimos el submenú de conversiones entre formatos
            4 -> menuConversiones(rutaDatosIni, rutaDatosFin)

            // Abrimos el submenú de gestión del fichero binario (se importa desde el CSV inicial)
            5 -> menuBIN(rutaEntrada)

            // Cerramos el programa cuando el usuario elige 0
            0 -> {
                println("Saliendo del programa.")
                return
            }

            // Avisamos si la opción del menú principal no es válida
            else -> println("Opción no válida.")
        }
    }
}