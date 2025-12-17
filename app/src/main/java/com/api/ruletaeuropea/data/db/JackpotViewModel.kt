package com.api.ruletaeuropea.data.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JackpotViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _premioAcumulado = MutableStateFlow(0)
    val premioAcumulado: StateFlow<Int> get() = _premioAcumulado

    init {
        // Escucha cambios en Firestore en tiempo real
        firestore.collection("jackpot")
            .document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val value = snapshot?.getLong("premio")?.toInt() ?: 0
                _premioAcumulado.value = value
            }
    }

    // Función para aumentar el jackpot
    fun incrementarPremio(monto: Int) {
        viewModelScope.launch {
            val docRef = firestore.collection("jackpot").document("current")
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val actual = snapshot.getLong("premio")?.toInt() ?: 0
                transaction.update(docRef, "premio", actual + monto)
            }
        }
    }

    // Función para resetear el jackpot
    fun resetPremio() {
        viewModelScope.launch {
            firestore.collection("jackpot")
                .document("current")
                .set(mapOf("premio" to 0))
        }
    }
}
