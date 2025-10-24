package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.data.entity.Historial

@Composable
fun PantallaHistorial(jugadorNombre: String) {
    val dao = App.database.historialDao()
    val historialFlow = dao.verHistorial(jugadorNombre)
    val lista by historialFlow.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Historial de ${jugadorNombre}")
        if (lista.isEmpty()) {
            Text("No hay registros")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(lista) { item: Historial ->
                    Text(text = "Apuesta #${item.NumApuesta} - Resultado: ${item.Resultado} - Saldo: ${item.SaldoDespues}")
                }
            }
        }
    }
}

