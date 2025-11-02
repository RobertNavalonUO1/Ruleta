package com.api.ruletaeuropea.pantallas

import com.api.ruletaeuropea.App
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.componentes.CoinsDisplay
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.logica.calcularPago
import com.api.ruletaeuropea.logica.evaluarApuesta
import com.api.ruletaeuropea.logica.tipoApuesta
import com.api.ruletaeuropea.logica.construirApuestaCompleta
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.api.ruletaeuropea.data.entity.Ruleta
import com.api.ruletaeuropea.data.entity.Historial

@Composable
fun PantallaRuletaGirando(
    navController: NavController,
    jugador: Jugador,
    apuestas: MutableState<List<Apuesta>>,
    onActualizarSaldo: (Int) -> Unit
) {
    var resultado by remember { mutableStateOf<Int?>(null) }
    var mostrarResultado by remember { mutableStateOf(false) }

    // Simula el giro de la ruleta
    LaunchedEffect(Unit) {
        delay(1500) // tiempo de giro
        resultado = (0..36).random()
        delay(2000) // tiempo antes de mostrar resultado
        mostrarResultado = true
    }


    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo visual
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        CoinsDisplay(
            cantidad = jugador.NumMonedas,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        if (resultado == null || !mostrarResultado) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(26.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen de ruleta girando
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("ruleta_animada.json"))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )

                LottieAnimation(
                    composition = composition,
                    progress = progress,
                    modifier = Modifier.size(600.dp)
                )


                // Panel de resumen de apuesta
                Column(
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                        .padding(26.dp)
                        .width(500.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("YOUR BET:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    apuestas.value.forEach {
                        Text(
                            text = "${tipoApuesta(it.numero)}: ${it.valorMoneda}",
                            fontSize = 16.sp,
                            color = Color(0xFFFFA500) // naranja suave
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val total = apuestas.value.sumOf { it.valorMoneda }
                    Text("TOTAL: $total C", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        else {
            Image(
                painter = painterResource(id = R.drawable.fondo_1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            val apuestasGanadoras = apuestas.value.filter { evaluarApuesta(it, resultado!!) }
            val pagoTotal = calcularPago(apuestas.value, resultado!!)

            // Actualiza el saldo del jugador
            LaunchedEffect(pagoTotal) {
                val nuevoSaldo = jugador.NumMonedas + pagoTotal

                // Actualiza la variable de estado
                onActualizarSaldo(pagoTotal)

                val daoRuleta = App.database.ruletaDao()
                val daoApuesta = App.database.apuestaDao()
                val daoJugador = App.database.jugadorDao()
                val daoHistorial = App.database.historialDao()

                //Inserta el resultado de la ruleta
                val idRuleta = withContext(Dispatchers.IO) {
                    daoRuleta.insertar(Ruleta(NumeroGanador = resultado!!))
                }

                // Actualiza el saldo del jugador en la base de datos
                withContext(Dispatchers.IO) {
                    val jugadorActualizado = jugador.copy(NumMonedas = nuevoSaldo)
                    daoJugador.actualizar(jugadorActualizado)
                }

                // Guarda la apuesta en el historial
                withContext(Dispatchers.IO) {
                    apuestas.value.forEach { apuesta ->
                        // Inserta la apuesta y obtiene su ID
                        val apuestaCompleta = construirApuestaCompleta(apuesta, jugador, resultado!!, idRuleta)
                        val idApuesta = daoApuesta.insertar(apuestaCompleta) // solo aquí se inserta

                        // Inserta el registro en historial usando el ID de la apuesta
                        val registroHistorial = Historial(
                            NombreJugador = jugador.NombreJugador,
                            NumApuesta = idApuesta,
                            Resultado = resultado.toString(),
                            SaldoDespues = nuevoSaldo
                        )
                        daoHistorial.insertar(registroHistorial)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Número ganador con fondo de color
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = when (resultado) {
                                0 -> Color.Green
                                in listOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36) -> Color.Red
                                else -> Color.Black
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$resultado",
                        fontSize = 64.sp, // más grande
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700) // dorado
                    )

                }
                Spacer(modifier = Modifier.height(8.dp))
                // Panel de resultados
                Column(
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .width(400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (pagoTotal > 0) {
                        Text(
                            text = "YOU WON",
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    } else {
                        Text(
                            text = "YOU LOSE",
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    apuestasGanadoras.forEach {
                        Text(
                            text = "${tipoApuesta(it.numero)}: ${it.valorMoneda}",
                            fontSize = 16.sp,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TOTAL: $pagoTotal C", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Botones
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.play_again),
                        contentDescription = "Jugar de nuevo",
                        modifier = Modifier
                            .size(200.dp)
                            .clickable {
                                apuestas.value = emptyList()
                                navController.popBackStack()
                            }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.exit),
                        contentDescription = "Salir",
                        modifier = Modifier
                            .size(200.dp)
                            .clickable {
                                // Limpia las apuestas y vuelve al menú principal
                                apuestas.value = emptyList()
                                navController.navigate("menu") {
                                    popUpTo("menu") { inclusive = true } // limpia el historial
                                    launchSingleTop = true // evita duplicar pantallas
                                }
                            }
                    )
                }

            }
        }
    }
}
