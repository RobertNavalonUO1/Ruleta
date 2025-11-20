package com.api.ruletaeuropea.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.pantallas.*

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    jugador: MutableState<Jugador>,
    apuestas: MutableState<List<Apuesta>>,
    startDestinationOverride: String? = null
) {
    val startDestination = startDestinationOverride ?: "intro"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("intro") { PantallaIntro(navController) }
        composable("login") { PantallaLogin(navController = navController, jugador = jugador) }
        composable("register") { PantallaRegister(navController = navController, jugador = jugador) }
        composable("menu") { PantallaMenu(navController = navController, jugador = jugador) }
        composable("apuestas") { PantallaApuestas(navController = navController, jugador = jugador, apuestas = apuestas) }
        composable("ruleta") {
            PantallaRuletaGirando(
                navController = navController,
                jugador = jugador.value,
                apuestas = apuestas,
                onActualizarSaldo = { delta -> jugador.value = jugador.value.copy(NumMonedas = jugador.value.NumMonedas + delta) },
                onActualizarJugador = { actualizado -> jugador.value = actualizado }
            )
        }
        composable(
            route = "resultados/{numero}/{apostado}/{premio}/{neto}/{exp}/{nivAntes}/{nivDesp}") { backStackEntry ->
            val args = backStackEntry.arguments
            val numero = args?.getString("numero")?.toIntOrNull() ?: 0
            val apostado = args?.getString("apostado")?.toIntOrNull() ?: 0
            val premio = args?.getString("premio")?.toIntOrNull() ?: 0
            val neto = args?.getString("neto")?.toIntOrNull() ?: (premio - apostado)
            val exp = args?.getString("exp")?.toIntOrNull() ?: 0
            val nivAntes = args?.getString("nivAntes")?.toIntOrNull() ?: jugador.value.Nivel
            val nivDesp = args?.getString("nivDesp")?.toIntOrNull() ?: jugador.value.Nivel
            PantallaResultados(
                navController = navController,
                numeroGanador = numero,
                totalApostado = apostado,
                premio = premio,
                neto = neto,
                expGanada = exp,
                nivelAntes = nivAntes,
                nivelDespues = nivDesp
            )
        }
        composable("ranking") { PantallaRanking(navController) }
        composable("historial") { PantallaHistorial(jugadorNombre = jugador.value.NombreJugador, navController = navController) }
    }
}
