package com.api.ruletaeuropea.data.db

import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun rememberPremioAcumulado(): State<Int> {
    val premioState = remember { mutableStateOf(0) }
    val firestore = FirebaseFirestore.getInstance()

    DisposableEffect(Unit) {
        val listener: ListenerRegistration =
            firestore.collection("configuracion")
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
