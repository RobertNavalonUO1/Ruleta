package com.api.ruletaeuropea.pantallas


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Historial
import com.api.ruletaeuropea.data.entity.Apuesta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign




@Composable
fun PantallaHistorial(jugadorNombre: String, navController: NavController) {

    val daoHistorial = App.database.historialDao()
    val daoApuesta = App.database.apuestaDao()

    val historialFlow = daoHistorial.verHistorial(jugadorNombre)
    val listaHistorial by historialFlow.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "History of $jugadorNombre",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (listaHistorial.isEmpty()) {
                Text("There are no records", fontSize = 18.sp, color = Color.LightGray)
            } else {

                //Cabecera
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .background(Color.DarkGray.copy(alpha = 0.8f))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bet", modifier = Modifier.weight(1f), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Date", modifier = Modifier.weight(1.6f), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Coins Bet", modifier = Modifier.weight(1.2f), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Result", modifier = Modifier.weight(1f), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Coins Won", modifier = Modifier.weight(1.2f), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Balance", modifier = Modifier.weight(1f), color = Color.White, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de registros
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(0.65f)
                ) {
                    items(listaHistorial) { historial ->
                        var apuesta by remember { mutableStateOf<Apuesta?>(null) }

                        LaunchedEffect(historial.NumApuesta) {
                            apuesta = withContext(Dispatchers.IO) {
                                daoApuesta.obtenerPorId(historial.NumApuesta)
                            }
                        }

                        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(historial.Fecha))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${historial.NumApuesta}", color = Color.White, fontSize = 14.sp,
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(fecha, color = Color.White, fontSize = 14.sp,
                                modifier = Modifier.weight(1.6f), textAlign = TextAlign.Center)
                            Text(apuesta?.MonedasApostadas?.toString() ?: "-", color = Color.White, fontSize = 14.sp,
                                modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                            Text(
                                if (apuesta?.Ganada == true) "Won" else "Lost",
                                color = when (apuesta?.Ganada) {
                                    true -> Color.Green
                                    false -> Color.Red
                                    else -> Color.White
                                },
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(apuesta?.Pago?.toString() ?: "-", color = Color.White, fontSize = 14.sp,
                                modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                            Text("${historial.SaldoDespues}", color = Color.White, fontSize = 14.sp,
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        //Boton Exit
        PlantillaBoton(
            text = "Exit",
            onClick = { navController.navigate("menu") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .width(120.dp)
                .height(50.dp),
            colors = listOf(Color.White, Color(0xFFAAAAAA)),
            textColor = Color.Black
        )
    }
}

