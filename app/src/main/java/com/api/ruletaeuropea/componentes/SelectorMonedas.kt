package com.api.ruletaeuropea.componentes

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.api.ruletaeuropea.R

@Composable
fun SelectorMonedas(
    monedaSeleccionada: Int,
    onMonedaSeleccionada: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            1 to R.drawable.coin1,
            5 to R.drawable.coin5,
            10 to R.drawable.coin10,
            20 to R.drawable.coin20,
            50 to R.drawable.coin50,
            100 to R.drawable.coin100
        ).forEachIndexed { index, (valor, imagenRes) ->
            MonedaChip(
                valor = valor,
                imagenRes = imagenRes,
                seleccionado = valor == monedaSeleccionada,
                onClick = { onMonedaSeleccionada(valor) }
            )
            if (index < 5) Spacer(Modifier.width(10.dp))
        }
    }
}

@Composable
fun MonedaChip(
    valor: Int,
    imagenRes: Int,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val bordeColor = if (seleccionado) Color(0xFFFFD700) else Color.Transparent

    val transition = rememberInfiniteTransition(label = "moneda-pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val scale = if (seleccionado) pulse else 1f

    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(if (seleccionado) 12.dp else 6.dp, CircleShape, clip = false)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .border(4.dp, bordeColor, CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (seleccionado) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.35f }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66FFD700), Color.Transparent),
                            center = center,
                            radius = radius
                        ),
                        radius = radius
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = imagenRes),
            contentDescription = "Moneda $valor",
            modifier = Modifier.size(72.dp)
        )
    }
}
