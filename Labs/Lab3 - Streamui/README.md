# StreamUI - Módulo 2: Arquitectura y Navegación

Proyecto educativo de Android que demuestra la implementación de arquitectura MVVM, inyección de dependencias con Koin, y navegación tipada con Navigation Compose.

## Screenshots

<p align="center">
  <img src="assets/screenshot_1.png" width="30%" />
  <img src="assets/screenshot_2.png" width="30%" />
  <img src="assets/screenshot_3.png" width="30%" />
</p>

### Demo Video
Puedes ver el funcionamiento de la aplicación en el siguiente video: [StreamUI Demo](assets/module2.webm)

## Presentación del Módulo

Todos los conceptos teóricos, diagramas de arquitectura y explicaciones detalladas (MVVM, UDF, DI, Navigation Type-Safe) se encuentran en los slides:

📄 [**Ver Presentación (Slides)**](slides/slides.md)


## Estructura del Proyecto

```
com.curso.android.module2.stream/
├── StreamApplication.kt      # Inicialización de Koin
├── MainActivity.kt           # NavHost y navegación
├── data/
│   ├── model/
│   │   └── Models.kt         # Song, Category (@Serializable)
│   └── repository/
│       ├── MusicRepository.kt      # Interface (abstracción)
│       └── MockMusicRepository.kt  # Implementación con datos mock
├── di/
│   └── AppModule.kt          # Módulo de Koin (interface binding)
└── ui/
    ├── components/
    │   └── SongCoverMock.kt  # Cover generado por código
    ├── navigation/
    │   └── Destinations.kt   # Rutas type-safe (Home, Search, Player)
    ├── screens/
    │   ├── HomeScreen.kt     # Grid de categorías (LazyColumn + LazyRow)
    │   ├── SearchScreen.kt   # Búsqueda con estados Loading/Success/Error
    │   └── PlayerScreen.kt   # Reproductor con controles
    ├── theme/
    │   └── Theme.kt
    └── viewmodel/
        ├── HomeViewModel.kt   # sealed interface UiState
        └── SearchViewModel.kt # sealed interface UiState (consistente)
```

---

## Notas Educativas

### Interface para Repository (Testabilidad)

El proyecto implementa el **Principio de Inversión de Dependencias (DIP)** usando interfaces:

```kotlin
// Interface (abstracción)
interface MusicRepository {
    fun getCategories(): List<Category>
    fun getSongById(songId: String): Song?
    fun getAllSongs(): List<Song>
}

// Implementación concreta
class MockMusicRepository : MusicRepository { ... }
```

**¿Por qué usar interfaces?**

| Sin Interface | Con Interface |
|---------------|---------------|
| ViewModel depende de `MockMusicRepository` | ViewModel depende de `MusicRepository` |
| Difícil de testear (acoplamiento fuerte) | Fácil de testear (inyectar fakes/mocks) |
| Cambiar implementación requiere modificar ViewModel | Cambiar implementación solo requiere cambiar binding en Koin |

En Koin, el binding se hace así:
```kotlin
singleOf(::MockMusicRepository) bind MusicRepository::class
```

> **Nota**: Los tests unitarios están fuera del alcance de este módulo educativo, pero la arquitectura está preparada para agregarlos fácilmente.

### Sealed Interface para UI States

Ambos ViewModels usan `sealed interface` para representar estados:

```kotlin
sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(...) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
```

**Beneficios:**
1. **Exhaustividad**: El compilador verifica que manejes todos los estados en `when`
2. **Type-safety**: Cada estado tiene sus propios datos
3. **Consistencia**: Mismo patrón en todos los ViewModels del proyecto
4. **Preparación**: Listo para operaciones asíncronas (APIs, bases de datos)

---

## Versiones de Dependencias

| Dependencia | Versión |
|-------------|---------|
| Android Gradle Plugin | 8.8.0 |
| Compose BOM | 2025.12.00 |
| Navigation Compose | 2.9.6 |
| Koin BOM | 4.1.1 |
| Kotlinx Serialization | 1.9.0 |
| Kotlin | 2.2.0 |
| Target SDK | 36 |

---

## Cómo Ejecutar

1. Abrir el proyecto en Android Studio
2. Sincronizar Gradle
3. Ejecutar en un emulador o dispositivo (API 24+)

No se requieren assets externos: todas las imágenes son generadas por código usando gradientes y íconos de Material.

---

## Créditos

Proyecto generado usando [Claude Code](https://claude.com/code) y adaptado por **Adrián Catalán**.

---

## Recursos Adicionales

- [Type-Safe Navigation - Android Developers](https://developer.android.com/guide/navigation/design/type-safety)
- [Koin Documentation](https://insert-koin.io/docs/quickstart/android-compose/)
- [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state)
- [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
