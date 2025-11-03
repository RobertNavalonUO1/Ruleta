package com.api.ruletaeuropea.pantallas
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Historial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.navigation.NavController


@Composable
fun PantallaHistorial(jugadorNombre: String, navController: NavController) {
    val dao = App.database.historialDao()
    val historialFlow = dao.verHistorial(jugadorNombre)
    val lista by historialFlow.collectAsState(initial = emptyList())


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
            // Encabezado
            Text(
                "History of $jugadorNombre",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (lista.isEmpty()) {
                Text(
                    "There are no records",
                    fontSize = 18.sp,
                    color = Color.LightGray
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    items(lista) { item: Historial ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bet #${item.NumApuesta}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Result: ${item.Resultado}",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Balace: ${item.SaldoDespues} C",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
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


