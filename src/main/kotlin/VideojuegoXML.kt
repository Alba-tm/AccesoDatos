import java.nio.file.Files
import java.nio.file.Path

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// Clase contenedora que representa la etiqueta raíz <videojuegos>
@JacksonXmlRootElement(localName = "videojuegos")
data class VideojuegosXML(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "videojuego")
    val listaVideojuegos: List<Videojuego> = emptyList()
)

fun leerVideojuegosXML(ruta: Path): List<Videojuego> {
    var contenedor = VideojuegosXML(emptyList())

    if (!Files.isReadable(ruta)) {
        println("Error: No se puede leer el fichero en la ruta: $ruta")
    } else {
        try {
            val xmlMapper = XmlMapper().registerKotlinModule()

            // Leemos el XML directamente sobre la clase contenedora
            contenedor = xmlMapper.readValue(ruta.toFile())
            println("--- Información leída con éxito de: $ruta")
        } catch (e: Exception) {
            println("Error al leer el fichero XML: ${e.message}")
        }
    }
    return contenedor.listaVideojuegos
}