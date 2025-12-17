package com.api.ruletaeuropea.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.api.ruletaeuropea.pantallas.PantallaRuletaGirando
import com.api.ruletaeuropea.pantallas.PantallaApuestas
import com.api.ruletaeuropea.pantallas.PantallaIntro
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.pantallas.PantallaLogin
import com.api.ruletaeuropea.pantallas.PantallaMenu
import com.api.ruletaeuropea.pantallas.PantallaRanking
import com.api.ruletaeuropea.pantallas.PantallaHistorial
import com.api.ruletaeuropea.pantallas.PantallaRegister
import com.api.ruletaeuropea.pantallas.PantallaInfo
import com.api.ruletaeuropea.data.entity.Jugador



@Composable
fun AppNavigation(
    navController: NavHostController,
    jugador: MutableState<Jugador>,
    apuestas: MutableState<List<Apuesta>>,
    // startDestinationOverride permite a la Activity indicar una ruta distinta (p. ej. "ruleta")
    startDestinationOverride: String? = null
) {
    // Si la Activity pasa una ruta, úsala; si no, usar "intro"
    val start = startDestinationOverride ?: "intro"

    NavHost(navController = navController, startDestination = "intro") {

        composable("intro") {
            PantallaIntro(navController)
        }

        composable("login") {
            PantallaLogin(navController = navController, jugador = jugador)
        }

        composable("menu") {
            PantallaMenu(navController = navController, jugador = jugador)
        }

        composable("apuestas") {
            PantallaApuestas(
                navController = navController,
                jugador = jugador,
                apuestas = apuestas
            )
        }

        composable("ranking") {
            PantallaRanking(navController = navController)
        }

        composable("historial") {
            PantallaHistorial(
                jugadorNombre = jugador.value.NombreJugador,
                navController = navController
            )
        }

        composable("ruleta") {
            PantallaRuletaGirando(
                navController = navController,
                jugador = jugador.value,
                apuestas = apuestas,
                onActualizarSaldo = { ganancia ->
                    jugador.value = jugador.value.copy(
                        NumMonedas = jugador.value.NumMonedas + ganancia
                    )
                }
            )
        }

        composable("register") {
            // Pantalla vacía o de error
            PantallaRegister(navController = navController, jugador = jugador)
        }

        composable("info") { PantallaInfo(navController) }
    }
}

