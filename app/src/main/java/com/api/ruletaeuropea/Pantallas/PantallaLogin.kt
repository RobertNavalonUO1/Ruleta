package com.api.ruletaeuropea.pantallas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.data.entity.Jugador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.animateDpAsState

@Composable
fun PantallaLogin(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val nombreState = remember { mutableStateOf("") }
    val contrasenaState = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Fondo profesional con leve viñeta
    val background = Brush.radialGradient(
        colors = listOf(Color(0xFF151515), Color(0xFF0E0E0E)),
        center = Offset(0.3f, 0.3f),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoImage(size = 120.dp)

            Text(
                text = "Ruleta Europea",
                color = Color(0xFFFFE97F),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoldOutlinedTextField(
                    value = nombreState.value,
                    onValueChange = { nombreState.value = it },
                    label = "Nombre de usuario",
                    modifier = Modifier.fillMaxWidth()
                )

                GoldOutlinedTextField(
                    value = contrasenaState.value,
                    onValueChange = { contrasenaState.value = it },
                    label = "Contraseña (opcional)",
                    modifier = Modifier.fillMaxWidth(),
                    isPassword = true
                )
            }

            Spacer(Modifier.height(6.dp))

            GoldButton(
                text = "Guardar y entrar",
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(56.dp)
            ) {
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
            }

            GoldOutlineButton(
                text = "Entrar sin usuario",
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(52.dp)
            ) {
                jugador.value = Jugador(NombreJugador = "Invitado", NumMonedas = 1000)
                navController.navigate("menu") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }
}

@Composable
private fun LogoImage(size: Dp) {
    Image(
        painter = painterResource(id = R.drawable.logo_ruleta_transparente),
        contentDescription = "Logo ruleta dorada",
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun GoldOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    val gold = Color(0xFFFFD54F)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = gold) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = gold,
            unfocusedBorderColor = gold.copy(alpha = 0.6f),
            focusedLabelColor = gold,
            cursorColor = gold,
            focusedTextColor = Color(0xFFFFF8E1),
            unfocusedTextColor = Color(0xFFECECEC)
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        modifier = modifier
    )
}

@Composable
private fun GoldButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "press-scale")

    // Efecto de brillo animado (sheen)
    val transition = rememberInfiniteTransition(label = "sheen")
    val shimmerX by transition.animateFloat(
        initialValue = -120f,
        targetValue = 720f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen-x"
    )

    val fillGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFB300)
        )
    )
    val borderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFF2B2), Color(0xFFFFC107)),
        start = Offset(0f, 0f),
        end = Offset(300f, 120f)
    )

    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val animatedElevation by animateDpAsState(targetValue = if (pressed) 6.dp else 12.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(brush = fillGradient)
            .border(2.dp, brush = borderGradient, shape = RoundedCornerShape(14.dp))
            .shadow(animatedElevation, RoundedCornerShape(14.dp), clip = false)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF1A1A1A),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // capa de brillo diagonal que se desplaza
        val xDp = with(density) { shimmerX.dp }
        Box(
            modifier = Modifier
                .matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .offset(x = xDp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 200f)
                        )
                    )
            )
        }
    }
}

@Composable
private fun GoldOutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.98f else 1f, label = "press-scale-outline")

    val borderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFF2B2), Color(0xFFFFC107)),
        start = Offset(0f, 0f),
        end = Offset(300f, 120f)
    )
    val haptics = LocalHapticFeedback.current
    val animatedElevation by animateDpAsState(targetValue = if (pressed) 2.dp else 6.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, brush = borderGradient, shape = RoundedCornerShape(14.dp))
            .shadow(animatedElevation, RoundedCornerShape(14.dp), clip = false)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFFFFE97F),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
