// Muestra los datos de un videojuego en una sola línea
fun mostrarVideojuego(videojuego: Videojuego) {
    println(
        "ID: ${videojuego.id}, " +
                "Título: ${videojuego.titulo}, " +
                "Género: ${videojuego.genero}, " +
                "Año: ${videojuego.anio}, " +
                "Nota: ${videojuego.nota}"
    )
}

fun listarVideojuegos(lista: List<Videojuego>) {
    if (lista.isEmpty()) {
        println("No hay videojuegos.")
    } else {
        lista.forEach { videojuego ->
            mostrarVideojuego(videojuego)
        }
    }
}

fun buscarPorId(lista: List<Videojuego>, id: Int) {
    // Buscamos el primer videojuego que tenga el ID indicado
    val videojuego = lista.find { it.id == id }

    if (videojuego == null) {
        println("No existe un videojuego con ID $id.")
    } else {
        mostrarVideojuego(videojuego)
    }
}

fun anadirVideojuego(lista: MutableList<Videojuego>) {
    print("Introduce el título: ")
    val titulo = readlnOrNull()?.trim() ?: return

    if (titulo.isBlank()) {
        println("El título no puede estar vacío.")
        return
    }

    print("Introduce el género: ")
    val genero = readlnOrNull()?.trim() ?: return

    if (genero.isBlank()) {
        println("El género no puede estar vacío.")
        return
    }

    print("Introduce el año: ")
    val anio = readlnOrNull()?.toIntOrNull()

    if (anio == null) {
        println("El año debe ser un número entero.")
        return
    }

    print("Introduce la nota (0.0 - 10.0): ")
    val nota = readlnOrNull()?.toDoubleOrNull()

    if (nota == null || nota < 0.0 || nota > 10.0) {
        println("La nota debe estar entre 0.0 y 10.0.")
        return
    }

    // Busca el ID mayor y le suma 1; si la lista está vacía, comienza en 1
    val id = (lista.maxOfOrNull { it.id } ?: 0) + 1

    val videojuego = Videojuego(id, titulo, genero, anio, nota)

    // Añadimos el videojuego a la lista
    lista.add(videojuego)

    println("Videojuego añadido con ID: $id")
}

fun modificarNota(lista: List<Videojuego>,id: Int, nuevaNota: Double): MutableList<Videojuego> {

    // Comprobamos que la nota esté dentro del rango permitido
    if (nuevaNota < 0.0 || nuevaNota > 10.0) {
        println("La nota debe estar entre 0.0 y 10.0.")
        return lista.toMutableList()
    }

    // Buscamos el videojuego que queremos modificar
    val videojuego = lista.find { it.id == id }

    if (videojuego == null) {
        println("No existe un videojuego con ID $id.")
        return lista.toMutableList()
    }

    println("Nota modificada.")

    // Creamos una lista nueva con la nota modificada
    return lista.map { juego ->
        if (juego.id == id) {
            juego.copy(nota = nuevaNota)
        } else {
            juego
        }
    }.toMutableList()
}

fun eliminarVideojuego(lista: List<Videojuego>, id: Int): MutableList<Videojuego> {

    // Buscamos el videojuego que queremos eliminar
    val videojuego = lista.find { it.id == id }

    if (videojuego == null) {
        println("No existe un videojuego con ID $id.")
        return lista.toMutableList()
    }

    // Pedimos confirmación antes de eliminarlo
    print("¿Seguro que quieres eliminar ${videojuego.titulo}? (s/n): ")
    val confirmacion = readlnOrNull()

    if (confirmacion.equals("s", ignoreCase = true)) {
        // Devolvemos una lista que no contiene el videojuego eliminado
        println("Videojuego eliminado.")
        return lista.filter { it.id != id }.toMutableList()
    }

    println("Eliminación cancelada.")
    return lista.toMutableList()
}

fun integrarCatalogo(catalogo: MutableList<Videojuego>,importados: List<Videojuego>): Int {
    var nuevos = 0

    for (videojuego in importados) {
        // Solo añadimos el videojuego si su ID todavía no existe en el catálogo
        if (catalogo.none { it.id == videojuego.id }) {
            catalogo.add(videojuego)
            nuevos++
        }
    }

    // Devolvemos cuántos videojuegos nuevos se han añadido
    return nuevos
}
