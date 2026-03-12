// =============================================================================
// SETTINGS.GRADLE.KTS - Configuración del Proyecto Multi-Módulo
// =============================================================================
// Este archivo es el PRIMER archivo que Gradle lee al iniciar.
// Define:
//   1. Qué módulos forman parte del proyecto
//   2. Dónde buscar plugins y dependencias (repositorios)
//   3. El nombre del proyecto raíz
//
// IMPORTANTE: Este archivo usa Kotlin DSL (.kts), que es type-safe y
// proporciona autocompletado en el IDE.
// =============================================================================

// -----------------------------------------------------------------------------
// PLUGIN MANAGEMENT - Configuración de Plugins de Gradle
// -----------------------------------------------------------------------------
// Define DÓNDE Gradle debe buscar los plugins que usamos en build.gradle.kts
// Los plugins son extensiones que agregan funcionalidad a Gradle.
// -----------------------------------------------------------------------------
pluginManagement {
    // Lista de repositorios donde buscar plugins, EN ORDEN de prioridad
    repositories {
        // --- Google's Maven Repository ---
        // Contiene plugins de Android (AGP), bibliotecas de AndroidX, etc.
        // Es el repositorio principal para desarrollo Android.
        google {
            // Filtro de contenido: solo buscar grupos que empiecen con estos prefijos
            // Esto optimiza la resolución de dependencias.
            content {
                includeGroupByRegex("com\\.android.*")      // Plugins de Android
                includeGroupByRegex("com\\.google.*")       // Bibliotecas de Google
                includeGroupByRegex("androidx.*")           // Bibliotecas AndroidX
            }
        }

        // --- Maven Central ---
        // El repositorio más grande de bibliotecas Java/Kotlin.
        // Contiene la mayoría de bibliotecas de terceros.
        mavenCentral()

        // --- Gradle Plugin Portal ---
        // Repositorio oficial de plugins de Gradle.
        // Contiene plugins como kotlin-android, kotlin-compose, etc.
        gradlePluginPortal()
    }
}

// -----------------------------------------------------------------------------
// DEPENDENCY RESOLUTION MANAGEMENT - Resolución de Dependencias
// -----------------------------------------------------------------------------
// Define DÓNDE Gradle debe buscar las bibliotecas/dependencias declaradas
// en los archivos build.gradle.kts de cada módulo.
// -----------------------------------------------------------------------------
dependencyResolutionManagement {
    // --- Modo de Repositorios ---
    // FAIL_ON_PROJECT_REPOS: Falla si algún módulo intenta definir sus propios
    // repositorios. Esto fuerza a que TODOS los repositorios se definan aquí,
    // garantizando consistencia en todo el proyecto.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // Lista de repositorios para dependencias (similar a pluginManagement)
    repositories {
        // Google's Maven Repository (AndroidX, Material, etc.)
        google()

        // Maven Central (bibliotecas de terceros)
        mavenCentral()
    }
}

// -----------------------------------------------------------------------------
// NOMBRE DEL PROYECTO RAÍZ
// -----------------------------------------------------------------------------
// Este es el nombre que aparece en Android Studio y en los reportes de Gradle.
// Por convención, debe coincidir con el nombre de la carpeta del proyecto.
// -----------------------------------------------------------------------------
rootProject.name = "RPGDiceRollerApp"

// -----------------------------------------------------------------------------
// INCLUSIÓN DE MÓDULOS
// -----------------------------------------------------------------------------
// Cada línea include(":modulo") agrega un módulo al proyecto.
// El prefijo ":" indica que es relativo al proyecto raíz.
//
// En este proyecto simple solo tenemos un módulo: "app"
// En proyectos más grandes podrías tener:
//   include(":app")
//   include(":core:data")
//   include(":core:domain")
//   include(":feature:login")
//   etc.
// -----------------------------------------------------------------------------
include(":app")
