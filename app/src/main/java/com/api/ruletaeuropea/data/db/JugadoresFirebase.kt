package com.api.ruletaeuropea.data.db

import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.api.ruletaeuropea.data.entity.Jugador

data class JugadorTop(
    val nombre: String = "",
    val saldo: Int = 0
)

object JugadoresFirebase {

    private val firestore = FirebaseFirestore.getInstance()

    //Guarda o actualiza un jugador en Firestore.
    suspend fun guardarJugador(jugador: Jugador) {
        firestore.collection("jugadores")
            .document(jugador.NombreJugador) // Puedes usar un ID único si quieres
            .set(
                mapOf(
                    "nombre" to jugador.NombreJugador,
                    "saldo" to jugador.NumMonedas,
                    "ultimaActualizacion" to System.currentTimeMillis()
                )
            )
            .await()
    }

    //Top 10 de jugadores ordenados por saldo.
    //Se actualiza en tiempo real.
    @Composable
    fun rememberTop10(): State<List<JugadorTop>> {
        val topState = remember { mutableStateOf(emptyList<JugadorTop>()) }

        DisposableEffect(Unit) {
            val listener: ListenerRegistration = firestore
                .collection("jugadores")
                .orderBy("saldo", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    topState.value = snapshot.documents.mapNotNull { doc ->
                        JugadorTop(
                            nombre = doc.getString("nombre") ?: return@mapNotNull null,
                            saldo = doc.getLong("saldo")?.toInt() ?: 0
                        )
                    }
                }

            onDispose { listener.remove() }
        }

        return topState
    }
}
// Obtener el Top 10 de jugadores de forma suspendida
suspend fun obtenerTop10Suspend(): List<JugadorTop> {
    val firestore = FirebaseFirestore.getInstance()
    val snapshot = firestore.collection("jugadores")
        .orderBy("saldo", Query.Direction.DESCENDING)
        .limit(10)
        .get()
        .await()

    return snapshot.documents.mapNotNull { doc ->
        JugadorTop(
            nombre = doc.getString("nombre") ?: return@mapNotNull null,
            saldo = doc.getLong("saldo")?.toInt() ?: 0
        )
    }
}
