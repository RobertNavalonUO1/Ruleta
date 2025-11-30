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
import com.api.ruletaeuropea.data.entity.Ubicacion
import com.api.ruletaeuropea.data.db.RuletaDatabase
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.api.ruletaeuropea.logica.obtenerUbicacion
import androidx.compose.ui.platform.LocalContext




class MainActivity : ComponentActivity() {

    private var isMuted = true
    private val LOCATION_PERMISSION_REQUEST_CODE = 2001


    companion object {
        const val REQUEST_CODE_PICK_AUDIO = 1001
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion(this)
        }
    }

    private fun getLastLocationAndSave() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Comprobamos permiso antes de acceder a la ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, salimos o pedimos de nuevo
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val ubicacion = Ubicacion(latitude = lat, longitude = lon)

                    // Guardamos en la base de datos con Coroutines
                    CoroutineScope(Dispatchers.IO).launch {
                        RuletaDatabase.getDatabase(this@MainActivity).ubicacionDao().insert(ubicacion)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar el servicio al arrancar la app
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            obtenerUbicacion(this)
        }

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

                    val context = LocalContext.current

                    // Launcher para seleccionar archivo MP3
                    val pickAudioLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri: Uri? ->
                        uri?.let {
                            val intent = Intent(context, MusicService::class.java).apply {
                                putExtra("action", "SET_MUSIC")
                                putExtra("audioUri", uri.toString())
                            }
                            context.startService(intent)
                        }
                    }
                    // Botón Musica arriba derecha (Abre selector de audio)
                    IconButton(
                        onClick = {
                            pickAudioLauncher.launch(arrayOf("audio/mpeg"))
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd) // Arriba a la derecha del Box padre
                            .padding(end = 45.dp, top = 26.dp) // Ajusta separación respecto al botón de volumen
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icselectmusic), // tu ícono de música
                            contentDescription = "Cambiar música",
                            tint = Color.White // o cambia según fondo
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

    override fun onPause() {
        super.onPause()
        Intent(this, MusicService::class.java).also {
            it.putExtra("action", "PAUSE_BG")
            startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        Intent(this, MusicService::class.java).also {
            it.putExtra("action", "RESUME_BG")
            startService(it)
        }
    }
}
