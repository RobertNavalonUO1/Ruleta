package com.api.ruletaeuropea.pantallas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.componentes.SelectorMonedas
import com.api.ruletaeuropea.componentes.CoinsDisplay
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.componentes.RuletaGrid as RuletaGridComp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import com.api.ruletaeuropea.App


@Composable
fun PantallaApuestas(
    navController: NavController,
    jugador: MutableState<Jugador>,
    apuestas: MutableState<List<Apuesta>>
) {
    var monedaSeleccionada by remember { mutableStateOf(1) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selector de moneda arriba
            SelectorMonedas(
                monedaSeleccionada = monedaSeleccionada,
                onMonedaSeleccionada = { monedaSeleccionada = it }
            )

            Spacer(Modifier.height(6.dp))

            // Zona central ocupada por el tablero (responsive desde el propio componente)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                RuletaGridComp(
                    monedaSeleccionada = monedaSeleccionada,
                    apuestas = apuestas.value,
                    onApuestaRealizada = { numero ->
                        val saldo = jugador.value.NumMonedas
                        if (saldo >= monedaSeleccionada) {
                            // Actualizar numMonedas en la base de datos.
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    App.database.jugadorDao()
                                        .aplicarDeltaMonedas(jugador.value.NombreJugador, -monedaSeleccionada)
                                }
                                jugador.value = jugador.value.copy(NumMonedas = saldo - monedaSeleccionada)
                            }


                            // Actualizar jugador en memoria.
                            jugador.value = jugador.value.copy(NumMonedas = saldo - monedaSeleccionada)

                            // Guardar apuesta en memoria.
                            apuestas.value = apuestas.value + Apuesta(numero, monedaSeleccionada)
                        } else {
                            Toast.makeText(
                                context,
                                "No tienes suficientes monedas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Barra de información inferior: monedas + botón Girar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total de monedas del jugador en la barra
                CoinsDisplay(
                    cantidad = jugador.value.NumMonedas,
                    modifier = Modifier
                        .padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Botón circular pequeño: Girar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(6.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700))
                        .clickable {
                            if (apuestas.value.isNotEmpty()) {
                                navController.navigate("ruleta")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Añade alguna apuesta antes de confirmar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "Girar",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
