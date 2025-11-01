package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Jugador
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale



@Composable
fun PantallaRanking(navController: NavController) {
    val dao = App.database.jugadorDao()
    val rankingFlow = dao.verRanking(50)
    val lista by rankingFlow.collectAsState(initial = emptyList())

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
                text = "Ranking de jugadores",
                fontSize = 26.sp,
                color = Color(0xFFFFE97F),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(lista) { index, jugador ->
                    RankingCard(index = index, jugador = jugador)
                }
            }
        }

        // Botón Exit fijo arriba a la derecha
        BotonExit(
            onClick = {
                navController.navigate("menu") {
                    popUpTo("ranking") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(bottom = 24.dp)
                .width(140.dp)
                .height(60.dp)
        )
    }
}

@Composable
private fun RankingCard(index: Int, jugador: Jugador) {
    val amarilloBase = Color(0xFFFFE97F)
    val amarilloClaro = Color(0xFFFFF3B0)
    val amarilloOscuro = Color(0xFFE6C95C)

    Card(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(amarilloClaro, amarilloBase, amarilloOscuro)
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                    text = "${index + 1}. ${jugador.NombreJugador}",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "${jugador.NumMonedas} C",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}

// Botón Exit independiente
@Composable
fun BotonExit(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "press-scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFB300))
                )
            )
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Exit",
            fontSize = 20.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
