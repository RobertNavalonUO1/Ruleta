package com.api.ruletaeuropea

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.data.entity.Ubicacion
import com.api.ruletaeuropea.data.db.RuletaDatabase
import com.api.ruletaeuropea.logica.obtenerUbicacion
import com.api.ruletaeuropea.navegacion.AppNavigation
import com.api.ruletaeuropea.Modelo.Apuesta
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat




class MainActivity : ComponentActivity() {

    private var isMuted = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Inicializamos Firebase
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Obtenemos usuario actual
        val user = auth.currentUser
        val uid = user?.uid

        // Lanzamos UI principal
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var mutedState by remember { mutableStateOf(isMuted) }
                val jugador = remember { mutableStateOf(Jugador(NombreJugador = "", NumMonedas = 1000)) }
                val apuestas = remember { mutableStateOf(listOf<Apuesta>()) }

                Box(modifier = Modifier.fillMaxSize()) {
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
                            this@MainActivity.isMuted = mutedState
                            val action = if (mutedState) "STOP" else "PLAY"
                            Intent(this@MainActivity, MusicService::class.java).also {
                                it.putExtra("action", action)
                                startService(it)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = if (mutedState) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = "Toggle sound",
                            tint = Color.White
                        )
                    }

                    val context = LocalContext.current
                    val pickAudioLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let {
                            val intent = Intent(context, MusicService::class.java).apply {
                                putExtra("action", "SET_MUSIC")
                                putExtra("audioUri", uri.toString())
                            }
                            context.startService(intent)
                        }
                    }
                }
            }
        }

        // Solicitar permisos de ubicación
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

        // Iniciar servicio de música
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)

        // Crear jugador en Firestore si existe UID
        uid?.let { crearJugadorSiNoExiste(db, uid) }
    }

    private fun crearJugadorSiNoExiste(db: FirebaseFirestore, uid: String) {
        val jugadorRef = db.collection("jugadores").document(uid)
        jugadorRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val jugador = hashMapOf(
                    "nombre" to "Jugador",
                    "saldo" to 1000,
                    "totalApostado" to 0,
                    "gananciasTotales" to 0,
                    "creadoEn" to FieldValue.serverTimestamp()
                )
                jugadorRef.set(jugador)
            }
        }
    }

    private fun getLastLocationAndSave() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val ubicacion = Ubicacion(latitude = it.latitude, longitude = it.longitude)
                    CoroutineScope(Dispatchers.IO).launch {
                        RuletaDatabase.getDatabase(this@MainActivity).ubicacionDao().insert(ubicacion)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        val action = if (isMuted) "STOP" else "PLAY"
        Intent(this, MusicService::class.java).also {
            it.putExtra("action", action)
            startService(it)
        }
        Intent(this, MusicService::class.java).also {
            it.putExtra("action", "RESUME_BG")
            startService(it)
        }
    }

    override fun onPause() {
        super.onPause()
        Intent(this, MusicService::class.java).also {
            it.putExtra("action", "STOP")
            startService(it)
        }
        Intent(this, MusicService::class.java).also {
            it.putExtra("action", "PAUSE_BG")
            startService(it)
        }
    }
}

// Función helper para Google Sign-In
fun getGoogleSignInClient(activity: Activity): GoogleSignInClient =
    GoogleSignIn.getClient(
        activity,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

