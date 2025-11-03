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
import com.api.ruletaeuropea.R
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun PantallaLogin(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val nombreState = remember { mutableStateOf("") }
    val contrasenaState = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val dorado = Color(0xFFFFD700)
    val fondo = painterResource(id = R.drawable.fondo)
    val logo = painterResource(id = R.drawable.logoinico)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = fondo,
            contentDescription = "Fondo decorativo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🖼️ Lado izquierdo: logo
            Image(
                painter = logo,
                contentDescription = "Logo de inicio",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 24.dp),
                contentScale = ContentScale.Fit
            )

            // Lado derecho: formulario
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Log in or create your account",
                    color = dorado,
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = nombreState.value,
                    onValueChange = { nombreState.value = it },
                    label = { Text("User name", color = dorado) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dorado,
                        unfocusedTextColor = dorado,
                        focusedBorderColor = dorado,
                        unfocusedBorderColor = dorado,
                        cursorColor = dorado
                    )
                )

                OutlinedTextField(
                    value = contrasenaState.value,
                    onValueChange = { contrasenaState.value = it },
                    label = { Text("Password (optional)", color = dorado) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dorado,
                        unfocusedTextColor = dorado,
                        focusedBorderColor = dorado,
                        unfocusedBorderColor = dorado,
                        cursorColor = dorado
                    )
                )

                Button(
                    onClick = {
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dorado)
                ) {
                    Text("Save and enter", color = Color.Black)
                }

                Button(
                    onClick = {
                        jugador.value = Jugador(NombreJugador = "Invitado", NumMonedas = 1000)
                        navController.navigate("menu") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dorado)
                ) {
                    Text("Log in without a username", color = Color.Black)
                }
            }
        }
    }
}
