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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

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

    val cfg = LocalConfiguration.current
    val widthDp = cfg.screenWidthDp
    val heightDp = cfg.screenHeightDp
    val isLandscape = widthDp > heightDp
    val sizeClass = when {
        widthDp >= 840 -> "expanded"
        widthDp >= 600 -> "medium"
        else -> "compact"
    }
    val isLowHeight = heightDp < 520
    val scrollState = rememberScrollState()

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
        // Fondo base
        Image(
            painter = fondo,
            contentDescription = "Fondo decorativo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Overlay sutil para legibilidad
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC000000), Color(0x99000000), Color(0x66000000))
                    )
                )
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val maxW = maxWidth
            val cardMaxWidth = when (sizeClass) {
                "expanded" -> 600.dp
                "medium" -> 500.dp
                else -> 420.dp
            }

            val horizontalPadding = when (sizeClass) {
                "expanded" -> 48.dp
                "medium" -> 40.dp
                else -> 24.dp
            }
            val verticalPadding = if (isLowHeight) 8.dp else 24.dp
            val formSpacing = if (isLowHeight) 12.dp else 16.dp
            val headlineStyle = if (sizeClass == "compact") MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium

            // Layout adaptativo
            if (isLandscape && sizeClass != "compact") {
                // Fila: logo + formulario
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo escalado y centrado
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = logo,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    // Formulario en card
                    LoginCard(
                        nombreState = nombreState,
                        contrasenaState = contrasenaState,
                        mensajeError = mensajeError,
                        passwordVisible = passwordVisible,
                        isLoading = isLoading,
                        onLogin = onLogin,
                        navController = navController,
                        jugador = jugador,
                        dorado = dorado,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = cardMaxWidth)
                            .verticalScroll(scrollState),
                        spacing = formSpacing,
                        headlineStyle = headlineStyle
                    )
                }
            } else {
                // Columna apilada (portrait o pantalla compacta)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = logo,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .fillMaxWidth(0.6f)
                            .padding(top = if (isLowHeight) 8.dp else 24.dp),
                        contentScale = ContentScale.Fit
                    )
                    LoginCard(
                        nombreState = nombreState,
                        contrasenaState = contrasenaState,
                        mensajeError = mensajeError,
                        passwordVisible = passwordVisible,
                        isLoading = isLoading,
                        onLogin = onLogin,
                        navController = navController,
                        jugador = jugador,
                        dorado = dorado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = cardMaxWidth),
                        spacing = formSpacing,
                        headlineStyle = headlineStyle
                    )
                }
            }
        }
    }
}

// Nuevo composable para la tarjeta de login reutilizable y responsive
@Composable
private fun LoginCard(
    nombreState: MutableState<String>,
    contrasenaState: MutableState<String>,
    mensajeError: MutableState<String?>,
    passwordVisible: MutableState<Boolean>,
    isLoading: MutableState<Boolean>,
    onLogin: () -> Unit,
    navController: NavController,
    jugador: MutableState<Jugador>,
    dorado: Color,
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    headlineStyle: androidx.compose.ui.text.TextStyle
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xCC000000)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = spacing),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Text(
                text = "Log in or create your account",
                color = dorado,
                style = headlineStyle,
                fontWeight = FontWeight.SemiBold
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
                supportingText = { mensajeError.value?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
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

            Button(
                onClick = onLogin,
                enabled = !isLoading.value,
                colors = ButtonDefaults.buttonColors(containerColor = dorado),
                modifier = Modifier.height(52.dp)
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Signing in...", color = Color.Black)
                } else Text("Save and enter", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.navigate("register") }) { Text("Create account", color = dorado) }
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
                ) { Text("Log as guest") }
            }
        }
    }
}
