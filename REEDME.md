# Catálogo de Videojuegos

Programa de consola en Kotlin para gestionar un catálogo de videojuegos.
Permite gestionar el catálogo en un fichero CSV (listar, buscar, añadir, modificar la nota y eliminar), importar videojuegos desde ficheros XML y JSON, convertir datos entre CSV, JSON y XML, y mantener un fichero binario de acceso aleatorio con registros de tamaño fijo.

## 1. Estructura de datos

### Data class

```kotlin
@Serializable
data class Videojuego(
    val id: Int,
    val titulo: String,
    val genero: String,
    val anio: Int,
    val nota: Double
)
```

### Estructura del proyecto

```
src/
├── Main.kt                    ← menús principales y submenú CSV
├── Videojuego.kt              ← data class
├── VideojuegoRepository.kt    ← leer y guardar el CSV
├── VideojuegoService.kt       ← listar, buscar, añadir, modificar, eliminar e integrar
├── VideojuegoXML.kt           ← leer y escribir XML (Jackson)
├── VideojuegoJSON.kt          ← leer y escribir JSON (kotlinx.serialization)
├── VideojuegoConversor.kt     ← conversiones entre formatos
└── VideojuegoBIN.kt           ← CRUD del fichero binario de acceso aleatorio

proyecto/datos/
├── datos_ini/                 ← ficheros de partida (videojuegos.csv, .xml y .json)
└── datos_fin/                 ← ficheros generados (CSV modificado, conversiones y .bin)
```

### Ficheros de intercambio

- **CSV**: sin cabecera y con `;` como separador. Cada línea es un videojuego: `id;titulo;genero;anio;nota`.
- **XML**: etiqueta raíz `<videojuegos>` y un `<videojuego>` por cada juego, con las etiquetas `<id>`, `<titulo>`, `<genero>`, `<anio>` y `<nota>`.
- **JSON**: lista de objetos con las claves `id`, `titulo`, `genero`, `anio` y `nota`.

### Estructura del registro binario

El fichero `datos_fin/videojuegos.bin` guarda los videojuegos uno detrás de otro, y cada registro ocupa siempre el mismo número de bytes:

| Campo  | Tipo   | Tamaño               | Bytes dentro del registro |
|--------|--------|----------------------|---------------------------|
| id     | Int    | 4 bytes              | 0 – 3                     |
| titulo | String | 40 bytes (fijo)      | 4 – 43                    |
| genero | String | 20 bytes (fijo)      | 44 – 63                   |
| anio   | Int    | 4 bytes              | 64 – 67                   |
| nota   | Double | 8 bytes              | 68 – 75                   |

**Tamaño total del registro: 4 + 40 + 20 + 4 + 8 = 76 bytes.**

Los textos se rellenan con espacios hasta completar su tamaño y se guardan con la codificación `ISO_8859_1`.

## 2. Instrucciones de ejecución

- **Requisitos previos**: JDK 24.
- **Compilación**: abre el proyecto en IntelliJ IDEA y deja que Gradle sincronice las dependencias. En `build.gradle.kts` deben estar el plugin y las dependencias siguientes:

```kotlin
plugins {
    kotlin("plugin.serialization") version "1.9.0" // misma versión que el plugin de Kotlin del proyecto
}

dependencies {
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

- **Ficheros de datos iniciales**: coloca `videojuegos.csv`, `videojuegos.xml` y `videojuegos.json` en la carpeta `proyecto/datos/datos_ini` antes de ejecutar. El CSV es imprescindible para arrancar; el XML y el JSON solo se usan en las opciones 2, 3 y 4 del menú.
- **Ejecución**: ejecuta `Main.kt`. Las carpetas `datos_ini` y `datos_fin` se crean automáticamente si no existen.
- **Menú principal**:

| Opción | Qué hace |
|--------|----------|
| 1. Gestión CSV | Listar, buscar por ID, añadir, modificar la nota y eliminar videojuegos. Cada cambio se guarda en `datos_fin/videojuegos.csv`. |
| 2. Leer catálogo desde XML | Importa `datos_ini/videojuegos.xml` al catálogo sin duplicar IDs e informa de cuántos son nuevos. |
| 3. Leer catálogo desde JSON | Igual que la opción 2, desde `datos_ini/videojuegos.json`. |
| 4. Conversiones entre formatos | Convierte entre CSV, JSON y XML leyendo de `datos_ini` y escribiendo en `datos_fin`. |
| 5. Gestión fichero BIN | Importar desde el CSV, leer, añadir, modificar la nota y eliminar registros del fichero binario. |
| 0. Salir | Cierra el programa. |

## 3. Decisiones de diseño

- Elegí `;` como separador del CSV y no usé cabecera, porque así lo exigen los datos de partida y evita tener que descartar la primera línea al leer.
- Separé `datos_ini` (datos originales) de `datos_fin` (datos modificados) para no perder nunca el fichero de partida. Al arrancar, el programa carga `datos_fin/videojuegos.csv` si ya existe, para conservar los cambios de ejecuciones anteriores, y si no, carga `datos_ini`.
- Las conversiones y la importación al binario leen siempre de `datos_ini`. Las conversiones a CSV escriben en `datos_fin/videojuegos.csv`, el mismo fichero donde se guarda el catálogo, así que sobrescriben los cambios guardados.
- La data class `Videojuego` se reutiliza para CSV, XML, JSON y binario. Sus propiedades se llaman igual que las etiquetas y claves de los ficheros, por lo que no hacen falta anotaciones `@SerialName` ni `@JacksonXmlProperty` en ella.
- La integración de XML y JSON evita duplicados comparando los ID con los que ya existen en el catálogo, y la función `integrarCatalogo` devuelve cuántos videojuegos nuevos se han añadido.
- Todas las conversiones usan una única función genérica, `convertir(origen, destino, leer, escribir)`. Los objetos `Videojuego` en memoria hacen de paso intermedio entre cualquier formato de origen y de destino.
- Reservé 40 bytes para el título porque el más largo del catálogo, "The Legend of Zelda: Breath of the Wild", tiene 39 caracteres. Reservé 20 bytes para el género porque el más largo ("Plataformas") tiene 11 caracteres y quedan márgenes para géneros futuros.
- Usé `ISO_8859_1` para que 1 carácter ocupe siempre 1 byte (también las tildes y las eñes) y que el tamaño del registro sea siempre de 76 bytes. Por eso, al añadir un videojuego, se rechazan los títulos o géneros demasiado largos o con caracteres que esa codificación no admite.
- Al modificar un registro binario salto directamente al campo `nota` con `FileChannel.position()`, sin leer ni reescribir el resto del fichero.
- Para eliminar un registro copio todos los demás a un fichero temporal `.tmp` y después lo muevo sobre el original, porque borrar un registro de en medio obligaría a desplazar todos los bytes siguientes.
- Todas las entradas por teclado se validan: el ID debe ser un número entero, la nota debe estar entre 0.0 y 10.0 (se admite la coma decimal) y las opciones de menú no válidas se avisan sin cerrar el programa.
