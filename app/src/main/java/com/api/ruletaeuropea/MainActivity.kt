package com.api.ruletaeuropea

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import android.content.pm.ApplicationInfo
import android.content.Intent
import androidx.navigation.compose.rememberNavController
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.navegacion.AppNavigation
import android.os.Bundle

class MainActivity : ComponentActivity() {

    private var isMuted = true

    companion object {
        const val REQUEST_CODE_PICK_AUDIO = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar el servicio al arrancar la app
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var mutedState by remember { mutableStateOf(isMuted) }
                val jugador = remember {
                    mutableStateOf(Jugador(NombreJugador = "Ingrid", NumMonedas = 1000))
                }
                val apuestas = remember {
                    mutableStateOf(listOf<Apuesta>())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Navegación principal
                    AppNavigation(
                        navController = navController,
                        jugador = jugador,
                        apuestas = apuestas,
                        startDestinationOverride = intent?.getStringExtra("startRoute")?.takeIf { it.isNotBlank() }
                    )

                    // Botón de volumen flotante
                    IconButton(
                        onClick = {
                            mutedState = !mutedState
                            val action = if (mutedState) "STOP" else "PLAY"
                            Intent(this@MainActivity, MusicService::class.java).also {
                                it.putExtra("action", action)
                                startService(it)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .padding(top = 10.dp)
                    ) {
                        val isBackgroundDark = true
                        Icon(
                            painter = painterResource(
                                id = if (mutedState) R.drawable.icsoundoff else R.drawable.icsoundon
                            ),
                            contentDescription = if (mutedState) "Mute" else "Sound",
                            tint = if (isBackgroundDark) Color.White else Color.Black
                        )

                    }
                }
            }
        }


        // Determinar si la app es debugeable sin usar BuildConfig (se mantiene por compatibilidad pero ya no fuerza la ruta)
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // Permitir override mediante extra de intent: --es startRoute ruleta
        val routeFromIntent = intent?.getStringExtra("startRoute")

        // Usar únicamente el valor pasado en el intent si existe; NO forzar "ruleta" en modo debug
        val startOverride = routeFromIntent?.takeIf { it.isNotBlank() }

    }
}


