package com.api.ruletaeuropea.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.api.ruletaeuropea.R
import com.nativecoders.brand.NativeCodersGlitchLogo
import kotlinx.coroutines.delay

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
            enter = fadeIn(animationSpec = tween(1200)) + scaleIn(initialScale = 0.85f, animationSpec = tween(1200)),
            exit = fadeOut(animationSpec = tween(700)) + scaleOut(targetScale = 0.95f, animationSpec = tween(700))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp)
            ) {
                val maxW = maxWidth
                val imageSize = when {
                    maxW < 300.dp -> 120.dp
                    maxW < 360.dp -> 150.dp
                    else -> 180.dp
                }
                val glitchScale = when {
                    maxW < 300.dp -> 0.60f
                    maxW < 340.dp -> 0.70f
                    maxW < 380.dp -> 0.78f
                    maxW < 420.dp -> 0.85f
                    else -> 0.95f
                }
                Row(
                    modifier = Modifier.wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo3d),
                        contentDescription = "Logo 3D",
                        modifier = Modifier.size(imageSize)
                    )
                    // Contenedor escalado para evitar salto de línea
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(imageSize * 0.6f) // proporcional a imagen
                            .graphicsLayer { scaleX = glitchScale; scaleY = glitchScale }
                    ) {
                        NativeCodersGlitchLogo(
                            modifier = Modifier.fillMaxWidth(),
                            showTopLogo = false,
                            intensity = 0.6f,
                            line1 = "NATIVE",
                            line2 = "CODERS",
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaIntroPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp)
        ) {
            val maxW = maxWidth
            val imageSize = when {
                maxW < 300.dp -> 120.dp
                maxW < 360.dp -> 150.dp
                else -> 180.dp
            }
            val glitchScale = when {
                maxW < 300.dp -> 0.60f
                maxW < 340.dp -> 0.70f
                maxW < 380.dp -> 0.78f
                maxW < 420.dp -> 0.85f
                else -> 0.95f
            }
            Row(
                modifier = Modifier.wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo3d),
                    contentDescription = "Logo 3D",
                    modifier = Modifier.size(imageSize)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(imageSize * 0.6f)
                        .graphicsLayer { scaleX = glitchScale; scaleY = glitchScale }
                ) {
                    NativeCodersGlitchLogo(
                        showTopLogo = false,
                        intensity = 0.6f,
                        line1 = "NATIVE",
                        line2 = "CODERS",
                        singleLine = true
                    )
                }
            }
        }
    }
}
