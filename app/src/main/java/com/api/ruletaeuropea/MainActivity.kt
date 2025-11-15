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

        // Determinar si la app es debugeable sin usar BuildConfig (se mantiene por compatibilidad pero ya no fuerza la ruta)
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // Permitir override mediante extra de intent: --es startRoute ruleta
        val routeFromIntent = intent?.getStringExtra("startRoute")

        // Usar únicamente el valor pasado en el intent si existe; NO forzar "ruleta" en modo debug
        val startOverride = routeFromIntent?.takeIf { it.isNotBlank() }

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
