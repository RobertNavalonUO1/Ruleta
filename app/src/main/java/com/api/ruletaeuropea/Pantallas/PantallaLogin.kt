package com.api.ruletaeuropea.pantallas

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.data.entity.Jugador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.NavController
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun PantallaLogin(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val nombreState = rememberSaveable { mutableStateOf("") }
    val contrasenaState = rememberSaveable { mutableStateOf("") }
    val mensajeError = remember { mutableStateOf<String?>(null) }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dorado = Color(0xFFFFD700)
    val fondo = painterResource(id = R.drawable.fondo)
    val logo = painterResource(id = R.drawable.logoinico)

    // Acción de login reutilizable (botón y tecla Done)
    val onLogin: () -> Unit = {
        val nombre = nombreState.value.trim()
        val contrasena = contrasenaState.value.trim()

        if (nombre.isEmpty() || contrasena.isEmpty()) {
            mensajeError.value = "You must enter your username and password"
        } else {
            scope.launch {
                isLoading.value = true
                try {
                    val dao = App.database.jugadorDao()
                    val existente = withContext(Dispatchers.IO) { dao.obtenerPorNombre(nombre) }

                    if (existente == null) {
                        val nuevo = Jugador(
                            NombreJugador = nombre,
                            Contrasena = contrasenaState.value.takeIf { it.isNotBlank() },
                            NumMonedas = 1000,
                            Nivel = 1,
                            ExpActual = 0
                        )
                        withContext(Dispatchers.IO) { dao.insertar(nuevo) }
                        jugador.value = nuevo
                        navController.navigate("menu") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        if (existente.Contrasena == contrasena) {
                            jugador.value = existente
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            mensajeError.value = "Incorrect password"
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PantallaLogin", "Error en login", e)
                    mensajeError.value = e.message ?: "An unexpected error occurred. Please try again."
                } finally {
                    isLoading.value = false
                }
            }
        }
    }

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
            // Lado izquierdo: Logo
            Image(
                painter = logo,
                contentDescription = "Logo de inicio",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 24.dp),
                contentScale = ContentScale.Fit
            )

            // Formulario dentro de una tarjeta elevada con fondo translúcido
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Log in or create your account",
                        color = dorado,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    OutlinedTextField(
                        value = nombreState.value,
                        onValueChange = {
                            nombreState.value = it
                            if (!mensajeError.value.isNullOrEmpty()) mensajeError.value = null
                        },
                        label = { Text("User name", color = dorado) },
                        placeholder = { Text("Enter your user name", color = dorado.copy(alpha = 0.7f)) },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = dorado) },
                        isError = mensajeError.value != null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Text),
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
                        onValueChange = {
                            contrasenaState.value = it
                            if (!mensajeError.value.isNullOrEmpty()) mensajeError.value = null
                        },
                        label = { Text("Password", color = dorado) },
                        placeholder = { Text("Enter your password", color = dorado.copy(alpha = 0.7f)) },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = dorado) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                                val icon = if (passwordVisible.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                                val desc = if (passwordVisible.value) "Hide password" else "Show password"
                                Icon(imageVector = icon, contentDescription = desc, tint = dorado)
                            }
                        },
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = mensajeError.value != null,
                        supportingText = {
                            mensajeError.value?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
                        keyboardActions = KeyboardActions(onDone = { onLogin() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = dorado,
                            unfocusedTextColor = dorado,
                            focusedBorderColor = dorado,
                            unfocusedBorderColor = dorado,
                            cursorColor = dorado
                        )
                    )

                    // Botón principal con estado de carga
                    Button(
                        onClick = onLogin,
                        enabled = !isLoading.value,
                        colors = ButtonDefaults.buttonColors(containerColor = dorado)
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Signing in...", color = Color.Black)
                        } else {
                            Text("Save and enter", color = Color.Black)
                        }
                    }

                    // Enlace para crear cuenta con menor jerarquía visual
                    TextButton(onClick = { navController.navigate("register") }) {
                        Text("Create account", color = dorado)
                    }

                    // Botón alternativo delineado para invitado
                    OutlinedButton(
                        onClick = {
                            jugador.value = Jugador(NombreJugador = "Guest", NumMonedas = 1000, Nivel = 1, ExpActual = 0)
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        enabled = !isLoading.value,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = dorado),
                        border = BorderStroke(1.dp, dorado)
                    ) {
                        Text("Log as guest")
                    }
                }
            }
        }
    }
}
