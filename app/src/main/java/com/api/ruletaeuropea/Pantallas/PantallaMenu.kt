package com.api.ruletaeuropea.pantallas

// PantallaMenu.kt
// ---------------------------------------------------------------
// Esta pantalla compone el menú principal de la Ruleta Europea.
// Objetivos clave del archivo:
// 1. Mostrar saludo al jugador y su saldo con nivel/progreso.
// 2. Presentar información del juego (RTP, tipo de ruleta, La Partage) en forma de pills adaptables.
// 3. Brindar controles rápidos para configurar límites y velocidad de la partida (Segment components).
// 4. Ofrecer accesos rápidos: Mesa privada, Modo práctica y Multijugador.
// 5. Visualizar los últimos resultados de la ruleta (ejemplo estático).
// 6. Proporcionar botones principales de navegación: Jugar, Ranking, Historial.
// 7. Mostrar diálogo emergente con reglas de la ruleta europea.
// 8. Adaptar el layout de forma responsive según orientación y altura disponible SIN usar scroll.
// 9. Proveer feedback háptico en interacciones clave y snackbars informativos.
// 10. Permitir densificación (compactar alturas y paddings) cuando el espacio vertical es limitado o en landscape.
// ---------------------------------------------------------------
// NOTA: No se emplea scroll; el contenido se reorganiza en una o dos columnas según altura y orientación.
// ---------------------------------------------------------------

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.data.entity.Jugador
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.LaunchedEffect
import com.api.ruletaeuropea.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
// Imports para diseño responsive (orientación y adaptación de ancho/alto)
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

/**
 * Pantalla principal del menú. Se adapta automáticamente:
 * - Portrait con altura suficiente: una sola columna vertical.
 * - Landscape o altura compacta: dos columnas para optimizar espacio horizontal.
 * Usa BoxWithConstraints para conocer dimensiones disponibles y ajustar distribución.
 */
@Composable
fun PantallaMenu(
    navController: NavController, // Controlador de navegación para cambiar a otras pantallas
    jugador: MutableState<Jugador> // Estado del jugador actual (nombre y monedas)
) {
    val dorado = Color(0xFFFFD700) // Color dorado corporativo reutilizado
    val snackbarHostState = remember { SnackbarHostState() } // Host para snackbars informativos
    val haptics = LocalHapticFeedback.current // Haptic feedback para mejorar interacción
    val scope = rememberCoroutineScope() // Alcance de corrutinas para lanzar snackbars de forma segura

    // Estados persistentes entre recomposiciones y cambios de configuración
    val limitesIndex = rememberSaveable { mutableStateOf(1) } // Índice seleccionado de límites (Estándar por defecto)
    val velocidadIndex = rememberSaveable { mutableStateOf(1) } // Índice seleccionado de velocidad (Normal por defecto)
    val isDark = rememberSaveable { mutableStateOf(false) } // Estado local de switch tema (no aplica tema real, solo demostrativo)
    val showReglas = rememberSaveable { mutableStateOf(false) } // Control de visibilidad del diálogo de reglas

    // Efecto: dispara snackbar cuando cambian cualquiera de los segmentos
    LaunchedEffect(limitesIndex.value, velocidadIndex.value) {
        snackbarHostState.showSnackbar("Preferencia guardada")
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .background(Color.Black) // Fondo base negro bajo la imagen
            .statusBarsPadding() // Ajuste para no cubrir la barra de estado
            .navigationBarsPadding() // Ajuste para no cubrir barra de navegación
    ) {
        // Fondo con imagen (misma que login) - purely decorativa
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null, // Sin descripción al ser decorativa
            contentScale = ContentScale.Crop, // Recorte para llenar área
            modifier = Modifier.fillMaxSize()
        )

        // Determinar orientación actual del dispositivo
        val cfg = LocalConfiguration.current
        val isPortrait = cfg.orientation == Configuration.ORIENTATION_PORTRAIT

        // BoxWithConstraints: ofrece maxWidth / maxHeight para lógica responsive
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current // (Reservado para futuras conversiones px->dp si hiciera falta)
            val maxW = maxWidth // Ancho disponible en dp
            val maxH = maxHeight // Alto disponible en dp
            val isCompactH = maxH < 520.dp // Marca alto compacto (ej: pantallas pequeñas o teclado visible)
            val isVeryWide = maxW > 900.dp // Disposición muy amplia (tablets, desktop emulador)

            // Tarjeta elevada que contiene todo el contenido del menú, centrada y con ancho respon.
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center) // Centra la tarjeta en el espacio disponible
                    .padding(horizontal = if (isPortrait) 20.dp else 28.dp) // Menor padding en portrait
                    .fillMaxWidth(if (isPortrait) 0.96f else 0.90f) // Ajuste de ancho según orientación
                    .wrapContentHeight(), // Altura solo según contenido, sin scroll
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)), // Fondo translúcido
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                val innerPad = if (isPortrait && !isCompactH) 20.dp else 16.dp // Padding interno adaptado

                // Box para colocar snackbar host sobre el contenido sin desbordarlo
                Box {
                    if (isPortrait && !isCompactH) {
                        // MODO UNA COLUMNA (Portrait con altura suficiente)
                        Column(
                            modifier = Modifier.padding(all = innerPad),
                            verticalArrangement = Arrangement.spacedBy(16.dp), // Espaciado vertical reducido
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Saludo al jugador utilizando su nombre
                            Text(
                                text = "Hola, ${jugador.value.NombreJugador}",
                                color = dorado,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Sección de saldo y nivel del jugador
                            CardSaldo(jugador = jugador.value, dorado = dorado)

                            // Sección de información del juego (RTP, tipo de ruleta, La Partage)
                            PillsSection()

                            // Controles rápidos para límites y velocidad de juego
                            Segment(
                                title = "Límites",
                                options = listOf("Estándar", "Altos", "Bajos"),
                                selectedIndex = limitesIndex.value,
                                onSelect = { newIndex ->
                                    limitesIndex.value = newIndex
                                    // Efecto háptico al cambiar opción
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            Segment(
                                title = "Velocidad",
                                options = listOf("Lenta", "Normal", "Rápida"),
                                selectedIndex = velocidadIndex.value,
                                onSelect = { newIndex ->
                                    velocidadIndex.value = newIndex
                                    // Efecto háptico al cambiar opción
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )

                            // Accesos rápidos a otras funciones
                            AccesosRapidos(navController = navController)

                            // Últimos resultados de la ruleta (ejemplo estático)
                            UltimosResultados()

                            // Botones principales de navegación
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Botón Jugar
                                Button(
                                    onClick = {
                                        // Navegar a la pantalla de juego
                                        navController.navigate("juego")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = dorado,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Jugar")
                                }

                                // Botón Ranking
                                Button(
                                    onClick = {
                                        // Navegar a la pantalla de ranking
                                        navController.navigate("ranking")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = dorado,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Ranking")
                                }
                            }

                            // Botón para mostrar reglas del juego
                            Button(
                                onClick = { showReglas.value = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = dorado
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text("¿Cómo jugar?")
                            }
                        }
                    } else {
                        // MODO DOS COLUMNAS (Landscape o altura compacta)
                        Row(
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Columna izquierda: saludo, saldo y nivel
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Saludo al jugador utilizando su nombre
                                Text(
                                    text = "Hola, ${jugador.value.NombreJugador}",
                                    color = dorado,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Sección de saldo y nivel del jugador
                                CardSaldo(jugador = jugador.value, dorado = dorado)
                            }

                            // Columna derecha: información del juego y controles
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Sección de información del juego (RTP, tipo de ruleta, La Partage)
                                PillsSection()

                                // Controles rápidos para límites y velocidad de juego
                                Segment(
                                    title = "Límites",
                                    options = listOf("Estándar", "Altos", "Bajos"),
                                    selectedIndex = limitesIndex.value,
                                    onSelect = { newIndex ->
                                        limitesIndex.value = newIndex
                                        // Efecto háptico al cambiar opción
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )

                                Segment(
                                    title = "Velocidad",
                                    options = listOf("Lenta", "Normal", "Rápida"),
                                    selectedIndex = velocidadIndex.value,
                                    onSelect = { newIndex ->
                                        velocidadIndex.value = newIndex
                                        // Efecto háptico al cambiar opción
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        }
                    }
                    // Host visual del snackbar anclado al fondo de la tarjeta
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
        // Botón de salida fuera de la tarjeta (no ocupa espacio interno)
        OutlinedButton(
            onClick = {
                // Reset estado jugador a invitado y navegar a login borrando menú del back stack
                jugador.value = Jugador(NombreJugador = "Guest", NumMonedas = 1000)
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                }
            },
            enabled = true,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado),
            border = BorderStroke(1.dp, dorado),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 35.dp)
                .height(48.dp)
        ) { Text("Exit") }
        // Diálogo de reglas (visible según showReglas)
        DialogReglas(show = showReglas.value) { showReglas.value = false }
    }
}

/**
 * Componente que muestra el saldo y nivel del jugador en una tarjeta.
 * Muestra el saldo en monedas y un indicador del nivel/progreso hacia el siguiente nivel.
 */
@Composable
fun CardSaldo(jugador: Jugador, dorado: Color) {
    // ... Código existente de CardSaldo ...
}

/**
 * Sección que muestra información del juego en forma de pills adaptables.
 * Incluye RTP, tipo de ruleta y regla La Partage.
 */
@Composable
fun PillsSection() {
    // ... Código existente de PillsSection ...
}

/**
 * Componente de segmento para seleccionar opciones como límites y velocidad.
 * Permite seleccionar entre varias opciones con feedback háptico.
 */
@Composable
fun Segment(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    // ... Código existente de Segment ...
}

/**
 * Componente que muestra accesos rápidos a funciones como mesa privada y multijugador.
 */
@Composable
fun AccesosRapidos(navController: NavController) {
    // ... Código existente de AccesosRapidos ...
}

/**
 * Componente que muestra los últimos resultados de la ruleta en un formato adaptativo.
 */
@Composable
fun UltimosResultados() {
    // ... Código existente de UltimosResultados ...
}

/**
 * Diálogo que muestra las reglas de la ruleta europea.
 * Se muestra u oculta mediante el estado [show].
 */
@Composable
fun DialogReglas(show: Boolean, onDismiss: () -> Unit) {
    // ... Código existente de DialogReglas ...
}

/**
 * Función que permite alternar entre tema claro y oscuro.
 * Modifica el estado [isDark] y reinicia la actividad para aplicar cambios.
 */
fun ToggleTema(isDark: Boolean) {
    // ... Código existente de ToggleTema ...
}
