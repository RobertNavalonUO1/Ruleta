package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.R

@Composable
fun PantallaResultados(
    navController: NavController,
    numeroGanador: Int,
    totalApostado: Int,
    premio: Int,
    neto: Int,
    expGanada: Int,
    nivelAntes: Int,
    nivelDespues: Int
) {
    val dorado = Color(0xFFFFD700)
    val positivo = neto >= 0
    val label = if (positivo) "Has ganado" else "Has perdido"
    val colorEstado = if (positivo) Color(0xFF69F0AE) else Color(0xFFFF8A80)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fondo reutilizando la imagen general del juego
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Capa de oscurecido suave
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xCC000000))
        )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212).copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Resultado de la ronda",
                        color = dorado,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    // Chip grande con el número ganador
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1D1D1D), Color(0xFF303030))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Número ganador", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = numeroGanador.toString(),
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Resumen de ganancias / pérdidas + experiencia
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            color = colorEstado,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Balance: ${if (neto >= 0) "+" else "-"}${kotlin.math.abs(neto)}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Apostado: $totalApostado  •  Premio: $premio",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        // Línea de experiencia ganada
                        Text(
                            text = "EXP: +$expGanada",
                            color = dorado,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (nivelDespues > nivelAntes) {
                            Spacer(Modifier.height(4.dp))
                            // Mostrar subida de nivel
                            Text(
                                text = "Nivel $nivelAntes → $nivelDespues",
                                color = Color(0xFF69F0AE),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                // Volver al menú principal limpiando el backstack hasta menú
                                navController.navigate("menu") {
                                    popUpTo("menu") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = dorado, contentColor = Color.Black),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Volver al menú", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                // Volver a la pantalla de apuestas para jugar otra vez
                                navController.navigate("apuestas") {
                                    popUpTo("apuestas") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303030), contentColor = dorado),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Seguir apostando", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
