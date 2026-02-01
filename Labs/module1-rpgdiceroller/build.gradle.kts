// =============================================================================
// BUILD.GRADLE.KTS (RAÍZ) - Configuración del Proyecto Principal
// =============================================================================
// Este es el archivo de build de nivel RAÍZ (root-level).
// A diferencia del build.gradle.kts del módulo :app, este archivo:
//   1. NO compila código directamente
//   2. Define plugins disponibles para todos los sub-módulos
//   3. Puede contener configuraciones compartidas entre módulos
//
// En proyectos modernos con Version Catalogs, este archivo es minimalista.
// =============================================================================

// -----------------------------------------------------------------------------
// BLOQUE DE PLUGINS
// -----------------------------------------------------------------------------
// Aquí declaramos los plugins que PUEDEN ser usados por los módulos del proyecto.
// El modificador "apply false" significa:
//   "Registra este plugin pero NO lo apliques aquí"
//
// Esto es necesario porque estos plugins deben aplicarse en cada módulo
// que los necesite (en app/build.gradle.kts), no en el proyecto raíz.
//
// NOTA: Usamos "alias(libs.plugins.xxx)" que referencia el Version Catalog
// definido en gradle/libs.versions.toml
// -----------------------------------------------------------------------------
plugins {
    // --- Android Application Plugin ---
    // Plugin para compilar aplicaciones Android (genera APK/AAB).
    // Se aplicará en el módulo :app
    alias(libs.plugins.android.application) apply false

    // --- Kotlin Android Plugin ---
    // Habilita el compilador de Kotlin para proyectos Android.
    // Necesario para escribir código Kotlin que se compile a bytecode Android.
    alias(libs.plugins.kotlin.android) apply false

    // --- Kotlin Compose Compiler Plugin ---
    // A partir de Kotlin 2.0, el compilador de Compose es un plugin SEPARADO.
    // Este plugin transforma las funciones @Composable en código ejecutable.
    // Sin este plugin, las anotaciones @Composable no funcionarían.
    alias(libs.plugins.kotlin.compose) apply false
}

// =============================================================================
// NOTAS EDUCATIVAS:
// =============================================================================
//
// ¿POR QUÉ "apply false"?
// ------------------------
// En Gradle, los plugins modifican el comportamiento del build.
// Si aplicáramos el plugin "android-application" aquí (sin "apply false"),
// Gradle intentaría tratar el proyecto RAÍZ como una aplicación Android,
// lo cual fallaría porque no hay código fuente aquí.
//
// En cambio, usamos "apply false" para:
//   1. Descargar el plugin y hacerlo disponible
//   2. Permitir que cada módulo decida si lo aplica o no
//
// ¿QUÉ ES UN VERSION CATALOG?
// ----------------------------
// El objeto "libs" que usamos (libs.plugins.xxx) viene del archivo
// gradle/libs.versions.toml. Gradle lo procesa automáticamente y genera
// un objeto type-safe que podemos usar en los scripts de build.
//
// Beneficios:
//   - Versiones centralizadas en un solo archivo
//   - Autocompletado en el IDE
//   - Fácil actualización de dependencias
//   - Consistencia entre módulos
//
// ESTRUCTURA DE UN PROYECTO ANDROID MULTI-MÓDULO:
// ------------------------------------------------
//   RPGDiceRollerApp/
//   ├── build.gradle.kts          <- Este archivo (raíz)
//   ├── settings.gradle.kts       <- Configuración de módulos
//   ├── gradle.properties         <- Propiedades de Gradle
//   ├── gradle/
//   │   ├── libs.versions.toml    <- Version Catalog
//   │   └── wrapper/
//   │       └── gradle-wrapper.properties
//   └── app/
//       ├── build.gradle.kts      <- Build del módulo :app
//       └── src/main/
//           ├── AndroidManifest.xml
//           └── java/...          <- Código fuente
//
// =============================================================================
