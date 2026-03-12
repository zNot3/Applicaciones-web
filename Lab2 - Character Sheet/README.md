# RPG Dice Roller App

Una aplicación Android educativa que simula un dado de 20 caras (d20), diseñada como módulo 1 de un curso introductorio de 4 módulos para aprender desarrollo Android con Jetpack Compose.

## Funcionalidad

- Simula un dado de 20 caras usado en juegos de rol (RPG)
- Animación visual durante el lanzamiento (15 iteraciones a 80ms cada una)
- Feedback visual especial:
  - **20 (Critical Hit)**: Texto en color dorado
  - **1 (Critical Miss)**: Texto en color rojo
  - Otros valores: Texto en gris estándar
- Botón deshabilitado durante la animación para evitar múltiples lanzamientos

![Critical Hit Screenshot](assets/critical_hit.png)

## Demo

Mira el funcionamiento final del módulo:

[▶️ Ver Video Demo (MP4)](assets/module1.mp4)

## Material de Clase

El contenido teórico y práctico para el módulo se encuentra aquí:
*   [Diapositivas (Marp)](slides/slides.md)

## Conceptos Cubiertos

### Kotlin & Corrutinas
- `rememberCoroutineScope`: Scope para lanzar corrutinas en Compose
- `launch`: Iniciar corrutinas
- `delay`: Función de suspensión para pausas sin bloquear

### Android Activities & Logcat
- Ciclo de vida de Activity (`onCreate`)
- `Log.d()` para mensajes de depuración
- `ComponentActivity` como base moderna

### Jetpack Compose
- `remember` y `mutableStateOf` para gestión de estado
- `rememberSaveable` para preservar estado durante cambios de configuración
- Recomposición automática
- Layouts: `Column`, `Box`, `Spacer`
- Componentes: `Button`, `Text`, `Icon`, `Scaffold`, `TopAppBar`
- Material 3 theming con soporte DayNight (modo oscuro automático)

## Requisitos del Sistema

- **Android Studio**: Ladybug (2024.2.1) o superior
- **JDK**: 17 o superior
- **Android SDK**: API 35 (Android 15)
- **Gradle**: 8.12 (descargado automáticamente por el wrapper)

## Estructura del Proyecto

```
RPGDiceRollerApp/
├── build.gradle.kts              # Build raíz (plugins)
├── settings.gradle.kts           # Configuración del proyecto
├── gradle.properties             # Propiedades de Gradle
├── gradlew / gradlew.bat         # Gradle Wrapper scripts
├── gradle/
│   ├── libs.versions.toml        # Version Catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts          # Build del módulo app
    ├── proguard-rules.pro        # Reglas de ofuscación
    └── src/main/
        ├── AndroidManifest.xml   # Manifiesto de la app
        ├── java/.../MainActivity.kt  # Código principal
        └── res/                  # Recursos (iconos, strings, themes)
```

## Comandos de Compilación

### Desde la Terminal

```bash
# Navegar al directorio del proyecto
cd RPGDiceRollerApp

# Compilar la versión debug
./gradlew assembleDebug

# Instalar en dispositivo/emulador conectado
./gradlew installDebug

# Compilar versión release
./gradlew assembleRelease

# Limpiar el proyecto
./gradlew clean

# Ver todas las tareas disponibles
./gradlew tasks
```

### Desde Android Studio

1. **Abrir el proyecto**: File → Open → Seleccionar carpeta `RPGDiceRollerApp`
2. **Sincronizar Gradle**: Android Studio lo hará automáticamente
3. **Ejecutar**: Click en el botón ▶️ (Run) o Shift+F10
4. **Build APK**: Build → Build Bundle(s) / APK(s) → Build APK(s)

## Versiones de Dependencias

| Componente | Versión |
|------------|---------|
| Android Gradle Plugin | 8.10.0 |
| Kotlin | 2.1.20 |
| Gradle Wrapper | 8.12 |
| Compose BOM | 2025.05.01 |
| Target SDK | 35 |
| Min SDK | 24 |
| JDK | 17 |

## Notas Educativas

### `remember` vs `rememberSaveable`

En Jetpack Compose existen dos formas principales de recordar estado:

| Característica | `remember` | `rememberSaveable` |
|----------------|------------|---------------------|
| Sobrevive recomposición | ✅ | ✅ |
| Sobrevive rotación de pantalla | ❌ | ✅ |
| Sobrevive proceso terminado | ❌ | ✅ |
| Uso de memoria | Menor | Mayor |

**¿Cuándo usar cada uno?**
- `remember`: Para estados transitorios (animaciones, hover, estados de carga)
- `rememberSaveable`: Para datos que el usuario espera conservar (formularios, selecciones, resultados)

En esta app, usamos `rememberSaveable` para el valor del dado y el mensaje de resultado, pero `remember` para el estado de animación (es transitorio y aceptable que se pierda).

### Material Icons: Core vs Extended

Compose ofrece dos paquetes de íconos:

| Paquete | Tamaño | Íconos incluidos |
|---------|--------|------------------|
| `material-icons-core` | ~2 MB | ~300 íconos más usados (Refresh, Add, Menu, etc.) |
| `material-icons-extended` | ~36 MB | +2000 íconos de Material Design |

**Recomendación**: Usa `material-icons-core` si solo necesitas íconos comunes. En producción, considera importar íconos individuales como recursos drawable para minimizar el tamaño del APK.

### Theme.Material3.DayNight

El tema `Theme.Material3.DayNight.NoActionBar` proporciona:
- **Material 3**: Diseño moderno con Material You
- **DayNight**: Soporte automático para modo oscuro según la configuración del sistema
- **NoActionBar**: Sin ActionBar tradicional (usamos TopAppBar de Compose)

Aunque Compose maneja su propio theming con `MaterialTheme`, el tema XML sigue siendo necesario para el splash screen, ciertas APIs del sistema, y como base de `ComponentActivity`.

## Notas para Estudiantes

### Ver Logs de Depuración

1. Abre Android Studio
2. View → Tool Windows → Logcat
3. Filtra por el tag `MainActivity`
4. Observa los mensajes durante cada lanzamiento del dado

### Experimentos Sugeridos

1. Cambia `MAX_DICE_VALUE` a 6 para un dado normal
2. Modifica `ANIMATION_DELAY_MS` para una animación más rápida/lenta
3. Agrega más tipos de dados (d4, d6, d8, d10, d12)
4. Implementa un historial de lanzamientos
5. Agrega sonidos para críticos

## Licencia

Este proyecto es material educativo para el curso de desarrollo Android.
