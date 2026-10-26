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

// Convierte un texto en un array de bytes de longitud fija (ISO_8859_1, relleno con espacios)
fun textoABytes(texto: String, tamano: Int): ByteArray {
    val bytes = ByteArray(tamano) { ' '.code.toByte() }
    val original = texto.toByteArray(Charsets.ISO_8859_1)
    original.copyInto(bytes, 0, 0, minOf(original.size, tamano))
    return bytes
}

// Comprueba que el texto cabe en los bytes reservados y que se puede guardar en ISO_8859_1
fun textoValido(texto: String, tamano: Int): Boolean {
    val bytes = texto.toByteArray(Charsets.ISO_8859_1)
    return bytes.size <= tamano && String(bytes, Charsets.ISO_8859_1) == texto
}

// Prepara un buffer con los 76 bytes de un registro, listo para escribirlo en el canal
fun registroABuffer(videojuego: Videojuego): ByteBuffer {
    val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

    buffer.putInt(videojuego.id)
    buffer.put(textoABytes(videojuego.titulo, TAMANO_TITULO))
    buffer.put(textoABytes(videojuego.genero, TAMANO_GENERO))
    buffer.putInt(videojuego.anio)
    buffer.putDouble(videojuego.nota)

    // Preparamos el buffer para volcar la información al canal
    buffer.flip()
    return buffer
}

// Reconstruye un videojuego a partir de un buffer con un registro leído
fun bufferARegistro(buffer: ByteBuffer): Videojuego {
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

    return Videojuego(id, titulo, genero, anio, nota)
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
            videojuegos.add(bufferARegistro(buffer))
            buffer.clear()
        }
    }
    return videojuegos
}

// Recorre el fichero y devuelve la posición (en bytes) donde empieza el registro con ese ID, o -1 si no existe
fun buscarPosicionBIN(id: Int): Long {
    var posicion = -1L

    if (!Files.isReadable(rutaBIN)) return posicion

    FileChannel.open(rutaBIN, StandardOpenOption.READ).use { canal ->
        val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

        while (posicion == -1L && canal.read(buffer) > 0) {
            // Al terminar de leer un registro, el puntero está al final de este
            val inicioRegistro = canal.position() - TAMANO_REGISTRO
            buffer.flip()

            if (buffer.getInt() == id) {
                posicion = inicioRegistro
            }
            buffer.clear()
        }
    }
    return posicion
}

// Lee el registro que empieza en una posición concreta del fichero
fun leerRegistroEn(posicion: Long): Videojuego {
    return FileChannel.open(rutaBIN, StandardOpenOption.READ).use { canal ->
        // Saltamos directamente a la posición del registro
        canal.position(posicion)

        val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)
        canal.read(buffer)
        buffer.flip()

        bufferARegistro(buffer)
    }
}

// Pide un ID hasta que sea un número entero
fun pedirId(): Int {
    while (true) {
        print("Introduce el ID: ")
        val id = readlnOrNull()?.toIntOrNull()

        if (id != null) return id

        println("El ID debe ser un número entero.")
    }
}

// Pide un ID hasta que sea un número entero que no exista ya en el fichero binario
fun pedirIdNuevo(): Int {
    while (true) {
        val id = pedirId()

        if (buscarPosicionBIN(id) != -1L) {
            println("Ya existe un videojuego con ID $id.")
        } else {
            return id
        }
    }
}

// Pide un texto hasta que no esté vacío y quepa en los bytes reservados del registro
fun pedirTexto(mensaje: String, tamano: Int): String {
    while (true) {
        print("$mensaje (máximo $tamano caracteres): ")
        val texto = readlnOrNull()?.trim()

        if (texto.isNullOrBlank()) {
            println("El campo no puede estar vacío.")
        } else if (!textoValido(texto, tamano)) {
            println("El texto es demasiado largo o contiene caracteres no permitidos.")
        } else {
            return texto
        }
    }
}

// Pide el año hasta que sea un número entero
fun pedirAnio(): Int {
    while (true) {
        print("Introduce el año: ")
        val anio = readlnOrNull()?.toIntOrNull()

        if (anio != null) return anio

        println("El año debe ser un número entero.")
    }
}

// Pide una nota hasta que sea un número entre 0.0 y 10.0
fun pedirNota(mensaje: String): Double {
    while (true) {
        print(mensaje)
        // Cambiamos la coma por un punto para aceptar notas como 8,5
        val nota = readlnOrNull()?.replace(',', '.')?.toDoubleOrNull()

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

        var importados = 0

        // Abrimos el canal vaciando el fichero si existe o creándolo si no existe
        FileChannel.open(
            rutaBIN,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { canal ->
            for (videojuego in videojuegos) {
                // Solo escribimos los registros cuyos textos caben en el tamaño fijo
                if (textoValido(videojuego.titulo, TAMANO_TITULO) &&
                    textoValido(videojuego.genero, TAMANO_GENERO)
                ) {
                    val buffer = registroABuffer(videojuego)
                    while (buffer.hasRemaining()) {
                        canal.write(buffer)
                    }
                    importados++
                } else {
                    println("Videojuego ignorado (título o género no válidos): ${videojuego.titulo}")
                }
            }
        }

        println("--- Videojuegos importados al fichero binario: $importados")

    } catch (e: Exception) {
        println("Error al importar al fichero binario: ${e.message}")
    }
}

fun mostrarRegistrosBIN() {
    if (Files.notExists(rutaBIN)) {
        println("El fichero binario todavía no existe. Impórtalo o añade un registro.")
        return
    }

    try {
        println("--- Información leída secuencialmente del fichero binario: ---")
        listarVideojuegos(leerRegistrosBIN())
    } catch (e: Exception) {
        println("Error al leer el fichero binario: ${e.message}")
    }
}

fun añadirRegistroBIN() {
    try {
        // Pedimos los datos validando cada uno hasta que sea correcto
        val id = pedirIdNuevo()
        val titulo = pedirTexto("Introduce el título", TAMANO_TITULO)
        val genero = pedirTexto("Introduce el género", TAMANO_GENERO)
        val anio = pedirAnio()
        val nota = pedirNota("Introduce la nota (0.0 - 10.0): ")

        val videojuego = Videojuego(id, titulo, genero, anio, nota)

        Files.createDirectories(rutaBIN.parent)

        // Añadimos el registro al final del fichero (se crea si no existe)
        FileChannel.open(
            rutaBIN,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        ).use { canal ->
            val buffer = registroABuffer(videojuego)
            while (buffer.hasRemaining()) {
                canal.write(buffer)
            }
        }

        println("Videojuego añadido con ID: $id")

    } catch (e: Exception) {
        println("Error al añadir el registro: ${e.message}")
    }
}

fun modificarRegistroBIN() {
    if (Files.notExists(rutaBIN)) {
        println("El fichero binario todavía no existe. Impórtalo o añade un registro.")
        return
    }

    try {
        val id = pedirId()

        // Recorremos el fichero para ver si existe el ID
        val posicion = buscarPosicionBIN(id)

        if (posicion == -1L) {
            println("No existe un videojuego con ID $id.")
            return
        }

        val videojuego = leerRegistroEn(posicion)
        println("Videojuego encontrado: ${videojuego.titulo}")

        val nuevaNota = pedirNota("Introduce la nueva nota (0.0 - 10.0): ")

        FileChannel.open(rutaBIN, StandardOpenOption.WRITE).use { canal ->
            // Nos situamos exactamente sobre el campo nota del registro
            canal.position(posicion + TAMANO_ID + TAMANO_TITULO + TAMANO_GENERO + TAMANO_ANIO)

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
    if (Files.notExists(rutaBIN)) {
        println("El fichero binario todavía no existe. Impórtalo o añade un registro.")
        return
    }

    val pathTemporal = Path.of(rutaBIN.toString() + ".tmp")

    try {
        val id = pedirId()

        // Recorremos el fichero para ver si existe el ID
        val posicion = buscarPosicionBIN(id)

        if (posicion == -1L) {
            println("No existe un videojuego con ID $id.")
            return
        }

        val videojuego = leerRegistroEn(posicion)

        // Pedimos confirmación antes de eliminarlo
        print("¿Seguro que quieres eliminar ${videojuego.titulo}? (s/n): ")
        val confirmacion = readlnOrNull()

        if (!confirmacion.equals("s", ignoreCase = true)) {
            println("Eliminación cancelada.")
            return
        }

        // Copiamos al fichero temporal todos los registros menos el que se elimina
        FileChannel.open(rutaBIN, StandardOpenOption.READ).use { canalLectura ->
            FileChannel.open(
                pathTemporal,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use { canalEscritura ->
                val buffer = ByteBuffer.allocate(TAMANO_REGISTRO)

                while (canalLectura.read(buffer) > 0) {
                    buffer.flip()
                    val idLeido = buffer.getInt()

                    if (idLeido != id) {
                        // Rebobinamos el puntero del buffer para escribir el registro completo original
                        buffer.rewind()
                        while (buffer.hasRemaining()) {
                            canalEscritura.write(buffer)
                        }
                    }
                    buffer.clear()
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