package com.api.ruletaeuropea.pantallas

import androidx.navigation.NavController

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
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.componentes.CoinsDisplay
import com.airbnb.lottie.compose.*


@Composable
fun PantallaApuestas(
    navController: NavController,
    jugador: MutableState<Jugador>,
    apuestas: MutableState<List<Apuesta>>
) {
    var monedaSeleccionada by remember { mutableStateOf(1) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Monedas arriba a la izquierda
        CoinsDisplay(
            cantidad = jugador.value.NumMonedas,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Contenedor gris translúcido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(45.dp)
                .background(Color.Gray.copy(alpha = 0.6f))
                .padding(6.dp),
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
                apuestas = apuestas.value,
                onApuestaRealizada = { numero ->
                    apuestas.value = apuestas.value + Apuesta(numero, monedaSeleccionada)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Image(
                painter = painterResource(id = R.drawable.confirm_bet),
                contentDescription = "Confirmar apuesta",
                modifier = Modifier
                    .width(600.dp)
                    .height(300.dp)
                    .clickable {
                        val totalApostado = apuestas.value.sumOf { it.valorMoneda }
                        if (totalApostado <= jugador.value.NumMonedas) {
                            jugador.value = jugador.value.copy(
                                NumMonedas = jugador.value.NumMonedas - totalApostado
                            )
                            navController.navigate("ruleta")
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}


