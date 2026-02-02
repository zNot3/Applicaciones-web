# =============================================================================
# PROGUARD RULES - Reglas de Ofuscación y Optimización
# =============================================================================
# ProGuard (ahora R8) es una herramienta que procesa el código compilado para:
#   1. OFUSCAR: Renombra clases/métodos a nombres cortos (a, b, c...)
#   2. SHRINK: Elimina código no utilizado
#   3. OPTIMIZE: Mejora el bytecode para mejor rendimiento
#
# Este archivo permite definir reglas personalizadas cuando el comportamiento
# por defecto de R8 causa problemas.
#
# CUÁNDO AGREGAR REGLAS:
# - Si usas reflexión (la ofuscación rompe los nombres)
# - Si usas serialización (JSON, Parcelable, etc.)
# - Si una biblioteca de terceros lo requiere
# =============================================================================

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see:
#   http://developer.android.com/guide/developing/tools/proguard.html

# -----------------------------------------------------------------------------
# REGLAS COMUNES (Descomentarlas si las necesitas)
# -----------------------------------------------------------------------------

# Si tu proyecto usa WebView con JavaScript, descomenta esto:
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
# }

# Si usas @Keep de AndroidX, ya está incluida por defecto.
# Si necesitas mantener logs de depuración en release (no recomendado):
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
# }

# -----------------------------------------------------------------------------
# REGLAS PARA COMPOSE (generalmente no necesarias, pero útiles de conocer)
# -----------------------------------------------------------------------------
# Compose funciona bien con R8 por defecto, pero si tienes problemas:
# -keep class androidx.compose.** { *; }
# -dontwarn androidx.compose.**
