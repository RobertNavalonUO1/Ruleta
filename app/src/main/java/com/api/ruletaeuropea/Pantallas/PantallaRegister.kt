package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.api.ruletaeuropea.data.entity.Jugador

@Composable
fun PantallaRegister(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val dorado = Color(0xFFFFD700)
    val fondo = painterResource(id = R.drawable.fondo)

    var nombre by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var pass2 by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = fondo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .widthIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create account", color = dorado, style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("User name", color = dorado) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = dorado, unfocusedTextColor = dorado,
                    focusedBorderColor = dorado, unfocusedBorderColor = dorado, cursorColor = dorado
                )
            )

            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password", color = dorado) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = dorado, unfocusedTextColor = dorado,
                    focusedBorderColor = dorado, unfocusedBorderColor = dorado, cursorColor = dorado
                )
            )

            OutlinedTextField(
                value = pass2, onValueChange = { pass2 = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Repeat password", color = dorado) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = dorado, unfocusedTextColor = dorado,
                    focusedBorderColor = dorado, unfocusedBorderColor = dorado, cursorColor = dorado
                )
            )

            if (error != null) Text(error!!, color = Color.White)

            Button(
                onClick = {
                    val n = nombre.trim()
                    val p1 = pass.trim()
                    val p2 = pass2.trim()

                    when {
                        n.isEmpty() || p1.isEmpty() || p2.isEmpty() ->
                            error = "All fields are required"
                        p1.length < 4 ->
                            error = "Password must have at least 4 characters"
                        p1 != p2 ->
                            error = "Passwords do not match"
                        else -> {
                            scope.launch {
                                val dao = App.database.jugadorDao()
                                val existe = withContext(Dispatchers.IO) { dao.obtenerPorNombre(n) }
                                if (existe != null) {
                                    error = "User name already exists"
                                } else {
                                    val nuevo = Jugador(NombreJugador = n, Contrasena = p1, NumMonedas = 1000)
                                    withContext(Dispatchers.IO) { dao.insertar(nuevo) }
                                    jugador.value = nuevo
                                    navController.navigate("menu") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = dorado)
            ) {
                Text("Create account", color = Color.Black)
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado),
                border = BorderStroke(1.dp, dorado)
            ) {
                Text("Back to login")
            }
        }
    }
}
