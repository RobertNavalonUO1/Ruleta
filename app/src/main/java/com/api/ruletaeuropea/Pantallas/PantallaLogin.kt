package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Jugador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaLogin(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val nombreState = remember { mutableStateOf("") }
    val contrasenaState = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inicia sesión o crea tu usuario")
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = nombreState.value,
            onValueChange = { nombreState.value = it },
            label = { Text("Nombre de usuario") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasenaState.value,
            onValueChange = { contrasenaState.value = it },
            label = { Text("Contraseña (opcional)") },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {

            jugador.value = Jugador(NombreJugador = "Invitado", NumMonedas = 1000)
            navController.navigate("menu") {
                popUpTo("login") { inclusive = true }
            }
        }) {
            Text("Entrar sin usuario")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            val nombre = nombreState.value.trim()
            if (nombre.isNotEmpty()) {
                scope.launch {
                    val user = withContext(Dispatchers.IO) {
                        val dao = App.database.jugadorDao()
                        val existente = dao.obtenerPorNombre(nombre)
                        if (existente == null) {
                            val nuevo = Jugador(
                                NombreJugador = nombre,
                                Contrasena = contrasenaState.value.takeIf { it.isNotBlank() },
                                NumMonedas = 1000
                            )
                            dao.insertar(nuevo)
                            nuevo
                        } else {
                            existente
                        }
                    }
                    jugador.value = user
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }) {
            Text("Guardar y entrar")
        }
    }
}
