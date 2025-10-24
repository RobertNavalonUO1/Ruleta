package com.api.ruletaeuropea.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.api.ruletaeuropea.data.entity.Jugador

@Composable
fun PantallaMenu(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Hola, ${jugador.value.NombreJugador}")

        Spacer(Modifier.height(16.dp))

        Button(onClick = { navController.navigate("apuestas") }) {
            Text("Jugar")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { navController.navigate("ranking") }) {
            Text("Ranking")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { navController.navigate("historial") }) {
            Text("Historial")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            // Limpiar y volver a login
            jugador.value = Jugador(NombreJugador = "Invitado", NumMonedas = 1000)
            navController.navigate("login") {
                popUpTo("menu") { inclusive = true }
            }
        }) {
            Text("Salir")
        }
    }
}

