package com.api.ruletaeuropea.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.api.ruletaeuropea.R
import com.nativecoders.brand.NativeCodersGlitchLogo
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun PantallaIntro(navController: NavController) {
    val visible = remember { mutableStateOf(false) }
    // Estado separado para mostrar el logo de texto con un pequeño delay (stagger)
    val showGlitch = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // animación de entrada
        visible.value = true
        // esperar a que termine la animación de entrada para luego mostrar el logo de texto
        delay(1200) // coincide con el durationMillis del enter
        showGlitch.value = true

        // mantener la pantalla visible un poco más para que se aprecien los efectos
        delay(4500)

        // iniciar animación de salida
        showGlitch.value = false
        visible.value = false
        // esperar a que termine la animación de salida antes de navegar
        delay(700)

        // navegar a login y eliminar la intro del backstack
        navController.navigate("login") {
            popUpTo("intro") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(animationSpec = tween(durationMillis = 1200)) + scaleIn(initialScale = 0.85f, animationSpec = tween(1200)),
            exit = fadeOut(animationSpec = tween(700)) + scaleOut(targetScale = 0.95f, animationSpec = tween(700))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.wrapContentSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logo3d),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(220.dp)
                        .zIndex(0f) // aseguramos que el 3D esté por debajo
                )

                Spacer(modifier = Modifier.height(40.dp)) // aumentado para evitar solapamientos durante las animaciones

                // Mostrar el logo de texto Native Coders con efecto glitch con un pequeño delay
                AnimatedVisibility(
                    visible = showGlitch.value,
                    enter = fadeIn(animationSpec = tween(durationMillis = 900)) + scaleIn(initialScale = 0.92f, animationSpec = tween(900), transformOrigin = TransformOrigin(0.5f, 0f)),
                    exit = fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.96f, animationSpec = tween(600), transformOrigin = TransformOrigin(0.5f, 0f))
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 6.dp)
                            .zIndex(1f) // forzamos que el glitch se dibuje por encima del logo3d
                    ) {
                        val scale = when {
                            maxWidth < 300.dp -> 0.70f
                            maxWidth < 340.dp -> 0.78f
                            maxWidth < 380.dp -> 0.85f
                            else -> 0.92f
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                        ) {
                            NativeCodersGlitchLogo(
                                modifier = Modifier.fillMaxWidth(),
                                enableBackground = false,
                                intensity = 0.6f
                            )
                        }
                    }
                }
            }
        }
    }
}
