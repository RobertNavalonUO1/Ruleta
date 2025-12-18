package com.api.ruletaeuropea.data.db

import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await


@Composable
fun rememberPremioAcumulado(): State<Int> {
    val premioState = remember { mutableStateOf(0) }
    val firestore = FirebaseFirestore.getInstance()

    DisposableEffect(Unit) {
        val listener: ListenerRegistration =
            firestore.collection("ruleta")
                .document("global")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    val premio = snapshot?.getLong("premioAcumulado")?.toInt() ?: 0
                    premioState.value = premio
                }

        onDispose {
            listener.remove()
        }
    }

    return premioState
}

//Leer el premio acumulado
suspend fun obtenerPremioAcumuladoFirestore(): Int {
    val firestore = FirebaseFirestore.getInstance()

    val snapshot = firestore
        .collection("ruleta")
        .document("global")
        .get()
        .await()

    return snapshot.getLong("premioAcumulado")?.toInt() ?: 0
}

//Actualizar el premio acumulado
suspend fun resetearPremioAcumuladoFirestore() {
    FirebaseFirestore.getInstance()
        .collection("ruleta")
        .document("global")
        .update("premioAcumulado", 0)
        .await()
}
