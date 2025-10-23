package com.api.ruletaeuropea.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.api.ruletaeuropea.R

@Composable
    fun SelectorMonedas(
        monedaSeleccionada: Int,
        onMonedaSeleccionada: (Int) -> Unit
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center, // 👈 centradas
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Pair(1, R.drawable.coin1),
            Pair(5, R.drawable.coin5),
            Pair(10, R.drawable.coin10),
            Pair(20, R.drawable.coin20),
            Pair(50, R.drawable.coin50),
            Pair(100, R.drawable.coin100)
        ).forEachIndexed { index, (valor, imagenRes) ->
            MonedaChip(
                valor = valor,
                imagenRes = imagenRes,
                seleccionado = valor == monedaSeleccionada,
                onClick = { onMonedaSeleccionada(valor) }
            )
            if (index < 5) Spacer(Modifier.width(4.dp)) // un pelín más de separación
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

    Box(
        modifier = Modifier
            .size(48.dp)
            .border(2.dp, bordeColor, CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imagenRes),
            contentDescription = "Moneda $valor",
            modifier = Modifier.size(48.dp)
        )
    }
}
