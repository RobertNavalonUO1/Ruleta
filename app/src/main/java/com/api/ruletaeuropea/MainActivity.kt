package com.api.ruletaeuropea

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.navegacion.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determinar si la app es debugeable sin usar BuildConfig
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // Permitir override mediante extra de intent: --es startRoute ruleta
        val routeFromIntent = intent?.getStringExtra("startRoute")

        // Si se pasa startRoute en el intent, se usa; si no, en modo debugeable arrancamos en "ruleta"
        val startOverride = when {
            !routeFromIntent.isNullOrBlank() -> routeFromIntent
            isDebuggable -> "ruleta"
            else -> null
        }

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
                    apuestas = apuestas,
                    startDestinationOverride = startOverride
                )
            }
        }
    }
}
