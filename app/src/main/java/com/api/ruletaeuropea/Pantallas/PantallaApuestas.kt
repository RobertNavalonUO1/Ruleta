package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.componentes.SelectorMonedas
import com.api.ruletaeuropea.componentes.RuletaGrid

@Composable
fun PantallaApuestas() {
    var monedaSeleccionada by remember { mutableStateOf(1) }
    var apuestas by remember { mutableStateOf(listOf<Apuesta>()) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo), // 👈 tu imagen de fondo
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Rectángulo gris semitransparente que contiene tablero, monedas y botón
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(45.dp)
                .background(Color.Gray.copy(alpha = 0.6f)) // 👈 gris translúcido
                .padding(6.dp), // padding interno

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            SelectorMonedas(
                monedaSeleccionada = monedaSeleccionada,
                onMonedaSeleccionada = { monedaSeleccionada = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            RuletaGrid(
                monedaSeleccionada = monedaSeleccionada,
                apuestas = apuestas,
                onApuestaRealizada = { numero ->
                    apuestas = apuestas + Apuesta(numero, monedaSeleccionada)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Image(
                painter = painterResource(id = R.drawable.confirm_bet),
                contentDescription = "Confirmar apuesta",
                modifier = Modifier
                    .width(600.dp)   // 👈 prueba con tamaño fijo
                    .height(300.dp)
                    .clickable {
                        println("✅ Apuestas confirmadas: $apuestas")
                    },
                contentScale = ContentScale.Fit
            )

        }
    }
}

