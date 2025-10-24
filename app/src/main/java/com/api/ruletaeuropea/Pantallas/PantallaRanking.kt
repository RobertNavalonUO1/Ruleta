package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Jugador
import kotlinx.coroutines.flow.map

@Composable
fun PantallaRanking() {
    val dao = App.database.jugadorDao()
    val rankingFlow = dao.verRanking(50)
    val lista by rankingFlow.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Ranking de jugadores")
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            itemsIndexed(lista) { index, jugador: Jugador ->
                Text(text = "${index + 1}. ${jugador.NombreJugador} - ${jugador.NumMonedas} monedas")
            }
        }
    }
}

