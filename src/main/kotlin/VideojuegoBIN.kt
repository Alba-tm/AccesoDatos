import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

// Definimos los tamaños en bytes de cada campo del registro binario
const val TAMANO_ID = Int.SIZE_BYTES // 4 bytes
const val TAMANO_TITULO = 40 // 40 bytes para la cadena de texto
const val TAMANO_GENERO = 20 // 20 bytes para la cadena de texto
const val TAMANO_ANIO = Int.SIZE_BYTES // 4 bytes
const val TAMANO_NOTA = Double.SIZE_BYTES // 8 bytes
const val TAMANO_REGISTRO = TAMANO_ID + TAMANO_TITULO + TAMANO_GENERO + TAMANO_ANIO + TAMANO_NOTA // 76 bytes en total

// Fichero binario donde se guardan los videojuegos
val rutaBIN: Path = Path.of("proyecto", "datos", "datos_fin", "videojuegos.bin")

fun menuBIN(rutaImportacion: Path) {
    var volver = false

    // Submenú del fichero binario: se repite hasta volver al menú principal
    while (!volver) {
        println()
        println("===== CRUD fichero BIN =====")
        println("1. Importar información de un fichero de intercambio.")
        println("2. Leer información del fichero binario.")
        println("3. Añadir un registro nuevo")
        println("4. Modificar un registro existente (por ID)")
        println("5. Eliminar un registro existente (por ID)")
        println("0. Volver")
        print("Elige una opción: ")

        when (readlnOrNull()?.toIntOrNull()) {

            1 -> importarABIN(rutaImportacion)
            2 -> mostrarRegistrosBIN()
            3 -> añadirRegistroBIN()
            4 -> modificarRegistroBIN()
            5 -> eliminarRegistroBIN()

            // Volvemos al menú principal sin cerrar el programa
            0 -> volver = true

            // Avisamos si se introduce una opción que no existe
            else -> println("Opción no válida.")
        }
    }
}

// Escribe un registro de 76 bytes en la posición actual del canal
fun escribirRegistro(canal: FileChannel, videojuego: Videojuego) {
    val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

    buffer.putInt(videojuego.id)

    // Los textos se rellenan con espacios hasta completar su tamaño fijo
    buffer.put(videojuego.titulo.padEnd(TAMANO_TITULO, ' ').toByteArray(Charsets.ISO_8859_1), 0, TAMANO_TITULO)
    buffer.put(videojuego.genero.padEnd(TAMANO_GENERO, ' ').toByteArray(Charsets.ISO_8859_1), 0, TAMANO_GENERO)

    buffer.putInt(videojuego.anio)
    buffer.putDouble(videojuego.nota)

    // Preparamos el buffer para volcar la información al canal
    buffer.flip()
    while (buffer.hasRemaining()) {
        canal.write(buffer)
    }
}

// Lee todos los registros de manera secuencial de inicio a fin
fun leerRegistrosBIN(): List<Videojuego> {
    val videojuegos = mutableListOf<Videojuego>()

    if (!Files.isReadable(rutaBIN)) return videojuegos

    FileChannel.open(rutaBIN, StandardOpenOption.READ).use { canal ->
        val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

        // Cada lectura llena exactamente un registro de 76 bytes
        while (canal.read(buffer) > 0) {
            buffer.flip()

            val id = buffer.getInt()

            // Leemos los bytes de los textos y los decodificamos limpiando los espacios sobrantes
            val tituloBytes = ByteArray(TAMANO_TITULO)
            buffer.get(tituloBytes)
            val titulo = String(tituloBytes, Charsets.ISO_8859_1).trim()

            val generoBytes = ByteArray(TAMANO_GENERO)
            buffer.get(generoBytes)
            val genero = String(generoBytes, Charsets.ISO_8859_1).trim()

            val anio = buffer.getInt()
            val nota = buffer.getDouble()

            videojuegos.add(Videojuego(id, titulo, genero, anio, nota))
            buffer.clear()
        }
    }
    return videojuegos
}

// Pide un número entero hasta que se introduzca uno válido
fun pedirEntero(mensaje: String): Int {
    while (true) {
        print(mensaje)
        val numero = readlnOrNull()?.toIntOrNull()

        if (numero != null) return numero

        println("Debe ser un número entero.")
    }
}

// Pide un texto hasta que no esté vacío y quepa en el tamaño reservado
fun pedirTexto(mensaje: String, maximo: Int): String {
    while (true) {
        print("$mensaje (máximo $maximo caracteres): ")
        val texto = readlnOrNull()?.trim()

        if (!texto.isNullOrEmpty() && texto.length <= maximo) return texto

        println("El texto no puede estar vacío ni superar $maximo caracteres.")
    }
}

// Pide una nota hasta que sea un número entre 0.0 y 10.0
fun pedirNota(mensaje: String): Double {
    while (true) {
        print(mensaje)
        val nota = readlnOrNull()?.toDoubleOrNull()

        if (nota != null && nota >= 0.0 && nota <= 10.0) return nota

        println("La nota debe ser un número entre 0.0 y 10.0.")
    }
}

fun importarABIN(rutaOrigen: Path) {
    // Comprobamos que el fichero de intercambio existe antes de leerlo
    if (Files.notExists(rutaOrigen)) {
        println("Error: no existe el fichero de origen: $rutaOrigen")
        return
    }

    val videojuegos = leerDatosCSV(rutaOrigen)

    if (videojuegos.isEmpty()) {
        println("No hay videojuegos para importar.")
        return
    }

    try {
        Files.createDirectories(rutaBIN.parent)

        // Abrimos el canal vaciando el fichero si existe o creándolo si no existe
        FileChannel.open(
            rutaBIN,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { canal ->
            for (videojuego in videojuegos) {
                escribirRegistro(canal, videojuego)
            }
        }
        println("--- Videojuegos importados al fichero binario: ${videojuegos.size}")

    } catch (e: Exception) {
        println("Error al importar al fichero binario: ${e.message}")
    }
}

fun mostrarRegistrosBIN() {
    try {
        println("--- Información leída secuencialmente del fichero binario: ---")
        listarVideojuegos(leerRegistrosBIN())
    } catch (e: Exception) {
        println("Error al leer el fichero binario: ${e.message}")
    }
}

fun anadirRegistroBIN() {
    try {
        // Pedimos el ID hasta que sea un número que no exista ya en el fichero
        var id = pedirEntero("Introduce el ID: ")
        while (leerRegistrosBIN().any { it.id == id }) {
            println("Ya existe un videojuego con ID $id.")
            id = pedirEntero("Introduce el ID: ")
        }

        val titulo = pedirTexto("Introduce el título", TAMANO_TITULO)
        val genero = pedirTexto("Introduce el género", TAMANO_GENERO)
        val anio = pedirEntero("Introduce el año: ")
        val nota = pedirNota("Introduce la nota (0.0 - 10.0): ")

        Files.createDirectories(rutaBIN.parent)

        // Añadimos el registro al final del fichero (se crea si no existe)
        FileChannel.open(
            rutaBIN,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        ).use { canal ->
            escribirRegistro(canal, Videojuego(id, titulo, genero, anio, nota))
        }
        println("Videojuego añadido con ID: $id")

    } catch (e: Exception) {
        println("Error al añadir el registro: ${e.message}")
    }
}

fun modificarRegistroBIN() {
    try {
        // Recorremos el fichero para ver si existe el ID
        val lista = leerRegistrosBIN()
        val id = pedirEntero("Introduce el ID: ")
        val videojuego = lista.firstOrNull { it.id == id }

        if (videojuego == null) {
            println("No existe un videojuego con ID $id.")
            return
        }

        println("Videojuego encontrado: ${videojuego.titulo}")
        val nuevaNota = pedirNota("Introduce la nueva nota (0.0 - 10.0): ")

        FileChannel.open(rutaBIN, StandardOpenOption.WRITE).use { canal ->
            // Posición del registro = posición en la lista × tamaño del registro
            val inicioRegistro = lista.indexOf(videojuego).toLong() * TAMANO_REGISTRO

            // Nos situamos en el canal exactamente sobre el campo nota
            canal.position(inicioRegistro + TAMANO_ID + TAMANO_TITULO + TAMANO_GENERO + TAMANO_ANIO)

            val buffer = ByteBuffer.allocate(TAMANO_NOTA)
            buffer.putDouble(nuevaNota)
            buffer.flip()

            while (buffer.hasRemaining()) {
                canal.write(buffer)
            }
        }
        println("Nota de ${videojuego.titulo} modificada a $nuevaNota.")

    } catch (e: Exception) {
        println("Error al modificar el registro: ${e.message}")
    }
}

fun eliminarRegistroBIN() {
    val pathTemporal = Path.of(rutaBIN.toString() + ".tmp")

    try {
        // Recorremos el fichero para ver si existe el ID
        val lista = leerRegistrosBIN()
        val id = pedirEntero("Introduce el ID: ")
        val videojuego = lista.firstOrNull { it.id == id }

        if (videojuego == null) {
            println("No existe un videojuego con ID $id.")
            return
        }

        // Pedimos confirmación antes de eliminarlo
        print("¿Seguro que quieres eliminar ${videojuego.titulo}? (s/n): ")
        if (!readlnOrNull().equals("s", ignoreCase = true)) {
            println("Eliminación cancelada.")
            return
        }

        // Escribimos en el fichero temporal todos los registros menos el eliminado
        FileChannel.open(
            pathTemporal,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { canal ->
            for (juego in lista) {
                if (juego.id != id) {
                    escribirRegistro(canal, juego)
                }
            }
        }

        // Reemplazamos el fichero original por el temporal
        Files.move(pathTemporal, rutaBIN, StandardCopyOption.REPLACE_EXISTING)
        println("Videojuego eliminado.")

    } catch (e: Exception) {
        println("Error durante la eliminación: ${e.message}")
    }
}
