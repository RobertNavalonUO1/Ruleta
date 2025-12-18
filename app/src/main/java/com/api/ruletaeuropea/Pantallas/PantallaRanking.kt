package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import com.api.ruletaeuropea.data.db.JugadorTop
import com.api.ruletaeuropea.data.db.obtenerTop10Suspend




@Composable
fun PantallaRanking(navController: NavController) {
    val scope = rememberCoroutineScope()
    var top10 by remember { mutableStateOf<List<JugadorTop>>(emptyList()) } // Estado reactivo

    // 🔹 Cargar jugadores desde Firestore al componer la pantalla
    LaunchedEffect(Unit) {
        top10 = try {
            obtenerTop10Suspend() // suspend fun que hace la query a Firestore
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151515))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Ranking scrollable
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ranking of players",
                fontSize = 26.sp,
                color = Color(0xFFFFE97F),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(top10) { index, jugador ->
                    RankingCardTop(index = index, jugador = jugador)
                }
            }
        }

        // Boton Exit
        PlantillaBoton(
            text = "Exit",
            onClick = {
                navController.navigate("menu") {
                    popUpTo("ranking") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 35.dp)
                .width(140.dp)
                .height(60.dp)
        )
    }
}

@Composable
private fun RankingCardTop(index: Int, jugador: JugadorTop) {
    val amarilloBase = Color(0xFFFFE97F)
    val amarilloClaro = Color(0xFFFFF3B0)
    val amarilloOscuro = Color(0xFFE6C95C)

    Card(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(35.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(6.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(amarilloClaro, amarilloBase, amarilloOscuro)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${index + 1}. ${jugador.nombre}",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "${jugador.saldo} C",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
