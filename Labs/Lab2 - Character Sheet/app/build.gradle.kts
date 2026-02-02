// =============================================================================
// APP/BUILD.GRADLE.KTS - Configuración del Módulo de Aplicación
// =============================================================================
// Este archivo configura el módulo :app, que es nuestra aplicación Android.
// Aquí definimos:
//   1. Plugins necesarios para compilar la app
//   2. Configuración de Android (SDK versions, package name, etc.)
//   3. Configuración del compilador de Kotlin
//   4. Dependencias (bibliotecas que usamos)
// =============================================================================

// Importaciones necesarias para la configuración del compilador Kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// -----------------------------------------------------------------------------
// BLOQUE DE PLUGINS
// -----------------------------------------------------------------------------
// Aplicamos los plugins que declaramos en el build.gradle.kts raíz.
// Ahora SÍ los aplicamos (sin "apply false") porque este módulo los necesita.
// -----------------------------------------------------------------------------
plugins {
    // Plugin para crear una aplicación Android
    // Habilita tareas como: assembleDebug, installDebug, etc.
    alias(libs.plugins.android.application)

    // Plugin de Kotlin para Android
    // Permite compilar código Kotlin a bytecode compatible con Android
    alias(libs.plugins.kotlin.android)

    // Plugin del compilador de Compose (OBLIGATORIO desde Kotlin 2.0)
    // Procesa las anotaciones @Composable y genera el código necesario
    alias(libs.plugins.kotlin.compose)
}

// -----------------------------------------------------------------------------
// BLOQUE ANDROID
// -----------------------------------------------------------------------------
// Configuración específica de Android proporcionada por el AGP
// (Android Gradle Plugin).
// -----------------------------------------------------------------------------
android {
    // --- Namespace ---
    // Define el package name para los recursos generados (R class, BuildConfig).
    // IMPORTANTE: Debe coincidir con el package de tu código fuente.
    namespace = "com.curso.android.module1.dice"

    // --- Compile SDK ---
    // Versión del SDK de Android contra la que se COMPILA la app.
    // Esto determina qué APIs de Android puedes usar en tu código.
    // Usamos toInt() porque en libs.versions.toml está como String.
    compileSdk = libs.versions.compileSdk.get().toInt()

    // --- Default Config ---
    // Configuración por defecto que aplica a todas las variantes de build.
    defaultConfig {
        // Application ID: Identificador ÚNICO de tu app en Google Play.
        // Puede ser diferente al namespace (aunque aquí son iguales).
        applicationId = "com.curso.android.module1.dice"

        // Minimum SDK: Versión mínima de Android donde la app puede instalarse.
        // API 24 = Android 7.0 (Nougat), lanzado en 2016.
        // Esto cubre aproximadamente el 99% de dispositivos activos.
        minSdk = libs.versions.minSdk.get().toInt()

        // Target SDK: Versión de Android para la que OPTIMIZAS la app.
        // Debería ser la última versión estable para aprovechar mejoras.
        targetSdk = libs.versions.targetSdk.get().toInt()

        // Version Code: Número entero que DEBE incrementarse en cada release.
        // Google Play usa este número para determinar si una versión es más nueva.
        versionCode = 1

        // Version Name: String visible para los usuarios (ej: "1.0.0").
        // Puede seguir cualquier formato (semver, fecha, etc.)
        versionName = "1.0.0"

        // Test Instrumentation Runner: Framework para tests de UI (Espresso).
        // AndroidJUnitRunner es el runner estándar de AndroidX Test.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // --- Build Types ---
    // Define variantes de compilación (debug, release, etc.)
    // Cada tipo puede tener configuraciones diferentes.
    buildTypes {
        // Configuración para builds de RELEASE (producción)
        release {
            // isMinifyEnabled: Activa R8/ProGuard para:
            //   - Ofuscar código (seguridad)
            //   - Eliminar código no usado (reduce tamaño del APK)
            //   - Optimizar bytecode (mejor rendimiento)
            // NOTA: Para desarrollo/debug, lo dejamos en false.
            isMinifyEnabled = false

            // ProGuard Rules: Archivo con reglas para R8/ProGuard.
            // getDefaultProguardFile(): Obtiene reglas por defecto de Android.
            // "proguard-rules.pro": Tus reglas personalizadas (si las necesitas).
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // --- Compile Options ---
    // Configuración del compilador de Java.
    // Aunque escribimos Kotlin, algunas partes usan el compilador de Java.
    compileOptions {
        // Source Compatibility: Versión de Java del código fuente.
        // Target Compatibility: Versión de Java del bytecode generado.
        // Java 17 es requerido por AGP 8.x
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // --- Build Features ---
    // Habilita/deshabilita características opcionales del build.
    buildFeatures {
        // Compose: Habilita Jetpack Compose para este módulo.
        // Esto activa el procesamiento de anotaciones @Composable.
        compose = true
    }
}

// -----------------------------------------------------------------------------
// CONFIGURACIÓN DEL COMPILADOR DE KOTLIN
// -----------------------------------------------------------------------------
// Usamos el DSL moderno `compilerOptions` en lugar del deprecated `kotlinOptions`.
// Esto es IMPORTANTE para evitar warnings de deprecation en Kotlin 2.x
//
// NOTA: Esta configuración se aplica a nivel de proyecto Kotlin, no dentro
// del bloque android {}, lo cual es la forma recomendada en Kotlin 2.x
// -----------------------------------------------------------------------------
kotlin {
    // compilerOptions: DSL moderno que reemplaza a kotlinOptions
    compilerOptions {
        // jvmTarget: Versión de la JVM para el bytecode generado.
        // Debe coincidir con sourceCompatibility/targetCompatibility de Java.
        // Usamos JvmTarget.JVM_17 (enum) en lugar de "17" (String deprecated)
        jvmTarget.set(JvmTarget.JVM_17)

        // freeCompilerArgs: Argumentos adicionales para el compilador.
        // Aquí puedes agregar flags experimentales si los necesitas.
        // Ejemplo: freeCompilerArgs.add("-Xcontext-receivers")
    }
}

// -----------------------------------------------------------------------------
// BLOQUE DE DEPENDENCIAS
// -----------------------------------------------------------------------------
// Aquí declaramos las bibliotecas que nuestra app necesita.
//
// Tipos de dependencias:
//   implementation: Disponible en compile-time y runtime
//   debugImplementation: Solo en builds de debug
//   testImplementation: Solo para unit tests
//   androidTestImplementation: Solo para tests de instrumentación
//
// NOTA: Usamos "libs.xxx" del Version Catalog (gradle/libs.versions.toml)
// -----------------------------------------------------------------------------
dependencies {

    // -------------------------------------------------------------------------
    // BIBLIOTECAS CORE DE ANDROIDX
    // -------------------------------------------------------------------------

    // Core KTX: Extensiones de Kotlin para APIs de Android
    // Proporciona funciones como: context.toast(), bundle.getParcelableCompat(), etc.
    implementation(libs.androidx.core.ktx)

    // Lifecycle Runtime KTX: Componentes de ciclo de vida con corrutinas
    // Proporciona: lifecycleScope, repeatOnLifecycle, etc.
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Activity Compose: Integración entre Activity y Compose
    // Proporciona: setContent { }, enableEdgeToEdge(), ComponentActivity
    implementation(libs.androidx.activity.compose)

    // -------------------------------------------------------------------------
    // JETPACK COMPOSE (usando BOM)
    // -------------------------------------------------------------------------
    // El BOM (Bill of Materials) es un archivo especial que define versiones
    // compatibles de TODAS las bibliotecas de Compose.
    //
    // Cómo funciona:
    //   1. Declaramos el BOM con platform()
    //   2. Las demás dependencias de Compose NO especifican versión
    //   3. El BOM proporciona automáticamente versiones compatibles
    //
    // Beneficios:
    //   - Garantiza compatibilidad entre bibliotecas de Compose
    //   - Solo actualizas UNA versión (el BOM) para actualizar todo
    //   - Evita conflictos de versiones
    // -------------------------------------------------------------------------

    // Compose BOM: Define versiones para todas las bibliotecas de Compose
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI: Core del framework de UI declarativa
    // Contiene: Modifier, Layout, Canvas, Input, etc.
    implementation(libs.androidx.compose.ui)

    // Compose UI Graphics: Utilidades gráficas
    // Contiene: Color, Brush, ImageBitmap, etc.
    implementation(libs.androidx.compose.ui.graphics)

    // Compose UI Tooling Preview: Soporte para @Preview
    // Permite ver previews de Composables en Android Studio
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Material 3: Componentes de Material Design 3
    // Contiene: Button, Text, Card, TopAppBar, etc.
    implementation(libs.androidx.compose.material3)

    // Material Icons Core: Colección de íconos más comunes
    // Contiene: Icons.Default.Refresh, Icons.Filled.Star, Icons.Default.Add, etc.
    //
    // NOTA EDUCATIVA: Usamos material-icons-core (~2MB) en lugar de
    // material-icons-extended (~36MB) porque solo necesitamos el ícono Refresh.
    // Ver gradle/libs.versions.toml para más información sobre la diferencia.
    implementation(libs.androidx.compose.material.icons.core)

    // -------------------------------------------------------------------------
    // HERRAMIENTAS DE DEBUG (solo en builds debug)
    // -------------------------------------------------------------------------

    // Compose UI Tooling: Herramientas de desarrollo
    // Incluye el Layout Inspector y otras utilidades de debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// =============================================================================
// NOTAS EDUCATIVAS ADICIONALES:
// =============================================================================
//
// ¿QUÉ ES platform() EN GRADLE?
// ------------------------------
// platform() indica que una dependencia es un BOM (Bill of Materials).
// Un BOM no contiene código, solo define versiones de otras dependencias.
// Cuando usas platform(), Gradle:
//   1. Descarga el BOM
//   2. Lee las versiones que define
//   3. Aplica esas versiones a las dependencias que no especifican versión
//
// ¿POR QUÉ JvmTarget.JVM_17 EN LUGAR DE "17"?
// --------------------------------------------
// En versiones antiguas de Kotlin (< 2.0), se usaba:
//   kotlinOptions { jvmTarget = "17" }  // String - DEPRECATED
//
// En Kotlin 2.x, se recomienda:
//   compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }  // Enum - MODERNO
//
// Beneficios del enfoque moderno:
//   - Type-safe (el IDE te ayuda a elegir valores válidos)
//   - Sin warnings de deprecation
//   - Mejor integración con Gradle's lazy configuration
//
// ¿QUÉ HACE enableEdgeToEdge()?
// ------------------------------
// Esta función (que llamamos en MainActivity) configura la app para
// que su contenido se dibuje detrás de las barras del sistema
// (status bar y navigation bar). Esto permite:
//   - UI más inmersiva y moderna
//   - Control total sobre el espacio de pantalla
//   - Consistencia con las guías de Material 3
//
// =============================================================================
