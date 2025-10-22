package com.api.ruletaeuropea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.navegacion.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val jugador = remember {
                    mutableStateOf(Jugador(NombreJugador = "Ingrid", NumMonedas = 1000))
                }
                val apuestas = remember {
                    mutableStateOf(listOf<Apuesta>())
                }

                AppNavigation(
                    navController = navController,
                    jugador = jugador,
                    apuestas = apuestas
                )
            }
        }
    }
}


