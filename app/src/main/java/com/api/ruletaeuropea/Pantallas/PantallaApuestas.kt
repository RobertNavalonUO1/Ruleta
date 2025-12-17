package com.api.ruletaeuropea.pantallas

import android.media.MediaPlayer
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
import com.api.ruletaeuropea.componentes.RuletaGrid as RuletaGridComp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Jugador
import androidx.compose.material3.Text



@Composable
fun PantallaApuestas(
    navController: NavController,
    jugador: MutableState<Jugador>,
    apuestas: MutableState<List<Apuesta>>
) {
    var monedaSeleccionada by remember { mutableStateOf(1) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sonidoNuevaFicha: MediaPlayer = remember { MediaPlayer.create(context, R.raw.fichasobremesa) }
    val sonidoSobreFicha: MediaPlayer = remember { MediaPlayer.create(context, R.raw.fichasobreficha) }
    val premioAcumulado = remember { mutableStateOf(1220) } //Provisional mientras no se lea de firebase
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
                            // Comprobar si ya hay fichas en la casilla
                            val casillaOcupada = apuestas.value.any { it.numero == numero }

                            // Reproducir el sonido correspondiente
                            if (casillaOcupada) {
                                sonidoSobreFicha.start()
                            } else {
                                sonidoNuevaFicha.start()
                            }

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
                            apuestas.value = apuestas.value + Apuesta(jugador.value, numero, monedaSeleccionada)
                        } else {
                            Toast.makeText(
                                context,
                                "You don't have enough coins.",
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

                Spacer(modifier = Modifier.width(26.dp))

                // Premio acumulado
                val dorado = Color(0xFFFFD700)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_premio),
                        contentDescription = "Premio acumulado",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "${premioAcumulado.value}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // empuja los botones a la derecha

                Box(){
                    PlantillaBoton(
                        text = "Exit",
                        onClick = { navController.navigate("menu") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 0.dp)
                            .width(120.dp)
                            .height(50.dp),
                        colors = listOf(Color.White, Color(0xFFAAAAAA)),
                        textColor = Color.Black
                    )
                }

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
                                    "Add a bet before confirming",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "Spin",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            }
        }
    }
    // Liberar recursos al salir del Composable
    DisposableEffect(Unit) {
        onDispose {
            sonidoNuevaFicha.release()
            sonidoSobreFicha.release()
        }
    }
}
