package com.curso.android.module2.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.curso.android.module2.stream.data.repository.MusicRepository
import com.curso.android.module2.stream.ui.navigation.HomeDestination
import com.curso.android.module2.stream.ui.navigation.LibraryDestination
import com.curso.android.module2.stream.ui.navigation.PlayerDestination
import com.curso.android.module2.stream.ui.navigation.SearchDestination
import com.curso.android.module2.stream.ui.screens.HomeScreen
import com.curso.android.module2.stream.ui.screens.LibraryScreen
import com.curso.android.module2.stream.ui.screens.PlayerScreen
import com.curso.android.module2.stream.ui.screens.SearchScreen
import com.curso.android.module2.stream.ui.theme.StreamUITheme
import org.koin.compose.koinInject
import kotlin.reflect.KClass

/**
 * ================================================================================
 * MAIN ACTIVITY - Punto de Entrada de la UI
 * ================================================================================
 *
 * SINGLE ACTIVITY ARCHITECTURE
 * ----------------------------
 * En apps Compose modernas, típicamente usamos UNA sola Activity.
 * Toda la navegación se maneja internamente con Navigation Compose.
 *
 * Ventajas:
 * - Navegación más fluida (sin recrear Activities)
 * - Estado compartido más fácil
 * - Transiciones personalizables
 * - Mejor integración con Compose
 *
 * COMPONENTES CLAVE:
 * ------------------
 * 1. ComponentActivity: Base moderna para Compose
 * 2. setContent { }: Establece la raíz del árbol de Compose
 * 3. NavHost: Contenedor de destinos de navegación
 * 4. NavController: Controla la navegación (back stack)
 * 5. NavigationBar: Barra de navegación inferior (Bottom Navigation)
 *
 * EDGE TO EDGE:
 * -------------
 * enableEdgeToEdge() hace que la app dibuje detrás de las barras
 * del sistema (status bar, navigation bar). Esto permite UIs
 * más inmersivas con colores personalizados en las barras.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita dibujo edge-to-edge (detrás de barras del sistema)
        enableEdgeToEdge()

        /**
         * setContent { }
         * --------------
         * Establece el contenido de la Activity usando Compose.
         * Todo lo que está dentro es un árbol de Composables.
         *
         * Este es el ÚNICO lugar donde conectamos el mundo tradicional
         * de Android (Activities) con el mundo de Compose.
         */
        setContent {
            StreamUITheme {
                StreamUIApp()
            }
        }
    }
}

/**
 * ================================================================================
 * BOTTOM NAVIGATION ITEM
 * ================================================================================
 *
 * Define los elementos del BottomNavigation con sus propiedades.
 *
 * PATRÓN: Cada item tiene:
 * - route: La clase de destino para navegación type-safe
 * - label: Texto que se muestra debajo del ícono
 * - selectedIcon: Ícono cuando el tab está seleccionado (filled)
 * - unselectedIcon: Ícono cuando el tab no está seleccionado (outlined)
 *
 * ICONOS FILLED vs OUTLINED:
 * -------------------------
 * Es una convención de Material Design usar iconos filled para
 * el estado seleccionado y outlined para el no seleccionado.
 * Esto proporciona feedback visual claro al usuario.
 */
data class BottomNavItem(
    val route: KClass<*>,
    val label: String,
    val selectedIcon: @Composable () -> ImageVector,
    val unselectedIcon: @Composable () -> ImageVector
)

/**
 * Lista de items del BottomNavigation.
 *
 * Nota: Para el ícono de Library usamos un recurso drawable personalizado
 * ya que Icons.Default no incluye un ícono de biblioteca de música apropiado.
 * Se usa el mismo ícono para ambos estados (selected/unselected) como fallback.
 */
@Composable
fun getBottomNavItems(): List<BottomNavItem> {
    val libraryIcon = ImageVector.vectorResource(R.drawable.ic_library)
    return listOf(
        BottomNavItem(
            route = HomeDestination::class,
            label = "Home",
            selectedIcon = { Icons.Filled.Home },
            unselectedIcon = { Icons.Outlined.Home }
        ),
        BottomNavItem(
            route = SearchDestination::class,
            label = "Search",
            selectedIcon = { Icons.Filled.Search },
            unselectedIcon = { Icons.Outlined.Search }
        ),
        BottomNavItem(
            route = LibraryDestination::class,
            label = "Library",
            selectedIcon = { libraryIcon },
            unselectedIcon = { libraryIcon }
        )
    )
}

/**
 * Composable raíz de la aplicación.
 *
 * Configura:
 * 1. Surface con el color de fondo del tema
 * 2. NavController para manejar navegación
 * 3. Scaffold con TopAppBar y BottomNavigation
 * 4. NavHost con los destinos de la app
 *
 * ================================================================================
 * BOTTOM NAVIGATION ARCHITECTURE
 * ================================================================================
 *
 * ESTRUCTURA:
 * -----------
 * La app tiene 3 tabs principales (Home, Search, Library) accesibles
 * desde el BottomNavigation. El Player es una pantalla de detalle
 * que se abre sobre cualquier tab.
 *
 * ```
 *     ┌─────────────────────────────────┐
 *     │          TopAppBar              │
 *     ├─────────────────────────────────┤
 *     │                                 │
 *     │     Content (Home/Search/       │
 *     │     Library/Player)             │
 *     │                                 │
 *     ├─────────────────────────────────┤
 *     │  Home  │  Search  │  Library    │  ← BottomNavigation
 *     └─────────────────────────────────┘
 * ```
 *
 * NAVEGACIÓN ENTRE TABS:
 * ----------------------
 * Usamos navigate() con opciones especiales para tabs:
 * - popUpTo(findStartDestination): Evita acumular back stack
 * - saveState/restoreState: Preserva el estado de cada tab
 * - launchSingleTop: Evita múltiples instancias del mismo destino
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamUIApp() {
    /**
     * rememberNavController()
     * -----------------------
     * Crea y recuerda un NavController.
     *
     * "Remember" significa que sobrevive recomposiciones.
     * El NavController mantiene el back stack de navegación.
     */
    val navController = rememberNavController()

    /**
     * koinInject()
     * ------------
     * Obtiene una dependencia del contenedor de Koin.
     * Aquí inyectamos el repository para buscar canciones por ID.
     */
    val repository: MusicRepository = koinInject()

    /**
     * currentBackStackEntryAsState()
     * ------------------------------
     * Observa el estado actual del back stack como State.
     * Se recompone automáticamente cuando cambia el destino.
     *
     * Lo usamos para determinar qué tab está seleccionado
     * y si debemos mostrar el BottomNavigation.
     */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    /**
     * Determinar si mostrar el BottomNavigation
     * -----------------------------------------
     * El BottomNavigation solo se muestra en los tabs principales.
     * Se oculta en pantallas de detalle como Player.
     */
    val bottomNavItems = getBottomNavItems()
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route) == true
    }

    /**
     * Título dinámico del TopAppBar
     * -----------------------------
     * Cambia según la pantalla actual para dar contexto al usuario.
     */
    val topBarTitle = when {
        currentDestination?.hasRoute(HomeDestination::class) == true -> "StreamUI"
        currentDestination?.hasRoute(SearchDestination::class) == true -> "Search"
        currentDestination?.hasRoute(LibraryDestination::class) == true -> "Your Library"
        currentDestination?.hasRoute(PlayerDestination::class) == true -> "Now Playing"
        else -> "StreamUI"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            /**
             * TOP APP BAR
             * -----------
             * Barra superior con el título de la pantalla actual.
             */
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            /**
             * BOTTOM NAVIGATION BAR
             * ---------------------
             * NavigationBar es el componente Material 3 para bottom navigation.
             *
             * Diferencias con Material 2:
             * - Material 2: BottomNavigation
             * - Material 3: NavigationBar
             *
             * Solo se muestra en los tabs principales (Home, Search, Library).
             */
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            /**
                             * ESTADO SELECCIONADO
                             * -------------------
                             * Usamos hierarchy para verificar si el destino actual
                             * está en la jerarquía del item. Esto maneja correctamente
                             * el caso de destinos anidados.
                             */
                            val selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(item.route)
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    /**
                                     * NAVEGACIÓN DE TABS
                                     * ------------------
                                     * La navegación entre tabs requiere opciones especiales:
                                     *
                                     * popUpTo(findStartDestination):
                                     * - Limpia el back stack hasta el destino inicial
                                     * - Evita que se acumulen múltiples screens
                                     *
                                     * saveState = true:
                                     * - Guarda el estado del tab actual antes de salir
                                     * - Preserva scroll position, campos de texto, etc.
                                     *
                                     * restoreState = true:
                                     * - Restaura el estado del tab al que navegamos
                                     * - El usuario vuelve donde estaba en ese tab
                                     *
                                     * launchSingleTop = true:
                                     * - Evita crear múltiples instancias del mismo destino
                                     * - Si ya estás en Home, tocar Home no crea otro Home
                                     */
                                    navController.navigate(
                                        when (item.route) {
                                            HomeDestination::class -> HomeDestination
                                            SearchDestination::class -> SearchDestination
                                            LibraryDestination::class -> LibraryDestination
                                            else -> HomeDestination
                                        }
                                    ) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon() else item.unselectedIcon(),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            /**
             * NAVHOST: Contenedor de Navegación
             * ----------------------------------
             * NavHost define el grafo de navegación de la app.
             *
             * Parámetros:
             * - navController: Controla la navegación
             * - startDestination: Destino inicial (HomeDestination)
             *
             * TYPE-SAFE NAVIGATION (Navigation 2.8+):
             * ---------------------------------------
             * En lugar de strings para las rutas, usamos tipos:
             * - composable<HomeDestination> { } en lugar de composable("home") { }
             * - navController.navigate(PlayerDestination(id)) en lugar de navigate("player/$id")
             */
            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                /**
                 * DESTINO: Home Screen
                 * --------------------
                 * composable<T> define un destino para el tipo T.
                 *
                 * HomeDestination es un object (sin argumentos),
                 * por lo que el lambda no necesita extraer nada.
                 */
                composable<HomeDestination> {
                    HomeScreen(
                        onSongClick = { song ->
                            /**
                             * NAVEGACIÓN TYPE-SAFE
                             * --------------------
                             * Navegamos a PlayerDestination pasando el songId.
                             *
                             * El compilador verifica que:
                             * - PlayerDestination existe
                             * - songId es del tipo correcto (String)
                             */
                            navController.navigate(PlayerDestination(songId = song.id))
                        }
                    )
                }

                /**
                 * DESTINO: Search Screen
                 * ----------------------
                 * SearchDestination es un object (sin argumentos).
                 *
                 * Esta pantalla es parte del BottomNavigation.
                 * Permite buscar canciones y navegar al Player.
                 */
                composable<SearchDestination> {
                    SearchScreen(
                        onSongClick = { song ->
                            /**
                             * REUTILIZACIÓN DE DESTINOS
                             * -------------------------
                             * Usamos el MISMO PlayerDestination que usa HomeScreen.
                             *
                             * Esto demuestra que los destinos son reutilizables:
                             * - No importa DESDE DÓNDE navegas
                             * - Solo importa A DÓNDE vas y con qué datos
                             */
                            navController.navigate(PlayerDestination(songId = song.id))
                        },
                        onBackClick = {
                            // En BottomNavigation, Search es un tab principal
                            // No necesita back manual, el usuario usa los tabs
                        }
                    )
                }

                /**
                 * DESTINO: Library Screen
                 * -----------------------
                 * LibraryDestination muestra las playlists del usuario.
                 *
                 * Es el tercer tab del BottomNavigation.
                 * Actualmente solo muestra playlists sin navegación adicional.
                 */
                composable<LibraryDestination> {
                    LibraryScreen(
                        onPlaylistClick = { playlist ->
                            // TODO: Navegar al detalle de la playlist
                            // Por ahora no hace nada
                        }
                    )
                }

                /**
                 * DESTINO: Player Screen
                 * ----------------------
                 * PlayerDestination es una data class con argumentos.
                 *
                 * toRoute<T>() extrae los argumentos de forma type-safe.
                 *
                 * El Player es una pantalla de detalle que se abre sobre
                 * cualquier tab. El BottomNavigation se oculta cuando
                 * estamos en esta pantalla.
                 */
                composable<PlayerDestination> { backStackEntry ->
                    // Extrae los argumentos de navegación de forma type-safe
                    val destination = backStackEntry.toRoute<PlayerDestination>()

                    // Busca la canción en el repository
                    val song = repository.getSongById(destination.songId)

                    PlayerScreen(
                        song = song,
                        onBackClick = {
                            /**
                             * popBackStack()
                             * --------------
                             * Navega hacia atrás en el back stack.
                             * Vuelve al tab desde donde se abrió el Player.
                             */
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
