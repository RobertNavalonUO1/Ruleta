package com.api.ruletaeuropea.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.api.ruletaeuropea.pantallas.PantallaRuletaGirando
import com.api.ruletaeuropea.pantallas.PantallaApuestas
import com.api.ruletaeuropea.pantallas.PantallaIntro
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.pantallas.PantallaLogin
import com.api.ruletaeuropea.pantallas.PantallaMenu
import com.api.ruletaeuropea.pantallas.PantallaRanking
import com.api.ruletaeuropea.pantallas.PantallaHistorial
import com.api.ruletaeuropea.pantallas.PantallaRegister
import com.api.ruletaeuropea.pantallas.PantallaResultados

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

    NavHost(navController = navController, startDestination = start) {

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
                onActualizarSaldo = { ganancia: Int ->
                    jugador.value = jugador.value.copy(
                        NumMonedas = jugador.value.NumMonedas + ganancia
                    )
                },
                onActualizarJugador = { actualizado ->
                    jugador.value = actualizado
                }
            )
        }

        composable(
            route = "resultados/{numero}/{apostado}/{premio}/{neto}/{exp}/{nivelAntes}/{nivelDespues}",
            arguments = listOf(
                navArgument("numero") { type = NavType.IntType },
                navArgument("apostado") { type = NavType.IntType },
                navArgument("premio") { type = NavType.IntType },
                navArgument("neto") { type = NavType.IntType },
                navArgument("exp") { type = NavType.IntType },
                navArgument("nivelAntes") { type = NavType.IntType },
                navArgument("nivelDespues") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val numero = backStackEntry.arguments?.getInt("numero") ?: 0
            val apostado = backStackEntry.arguments?.getInt("apostado") ?: 0
            val premio = backStackEntry.arguments?.getInt("premio") ?: 0
            val neto = backStackEntry.arguments?.getInt("neto") ?: (premio - apostado)
            val exp = backStackEntry.arguments?.getInt("exp") ?: 0
            val nivelAntes = backStackEntry.arguments?.getInt("nivelAntes") ?: jugador.value.Nivel
            val nivelDespues = backStackEntry.arguments?.getInt("nivelDespues") ?: jugador.value.Nivel

            PantallaResultados(
                navController = navController,
                numeroGanador = numero,
                totalApostado = apostado,
                premio = premio,
                neto = neto,
                expGanada = exp,
                nivelAntes = nivelAntes,
                nivelDespues = nivelDespues
            )
        }

        composable("register") {
            // Pantalla vacía o de error
            PantallaRegister(navController = navController, jugador = jugador)
        }
    }
}
