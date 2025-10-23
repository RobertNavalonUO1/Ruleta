package com.api.ruletaeuropea.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.api.ruletaeuropea.pantallas.PantallaApuestas
import com.api.ruletaeuropea.pantallas.PantallaRuletaGirando
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.data.entity.Jugador

@Composable
fun AppNavigation(
    navController: NavHostController,
    jugador: MutableState<Jugador>,
    apuestas: MutableState<List<Apuesta>>
) {
    NavHost(navController = navController, startDestination = "apuestas") {

        composable("apuestas") {
            PantallaApuestas(
                navController = navController,
                jugador = jugador,
                apuestas = apuestas
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
    }
}
