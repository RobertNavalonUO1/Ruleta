package com.api.ruletaeuropea.pantallas.data.remote

import com.api.ruletaeuropea.data.db.JugadorTop


suspend fun obtenerTop10Rest(): List<JugadorTop> {
    val response = RetrofitClient.api.getJugadores()

    if (!response.isSuccessful) return emptyList()

    val body = response.body()
    val documents = body?.documents ?: emptyList()

    val jugadores = documents.mapNotNull { doc ->
        val fields = doc.fields
        val nombre = fields?.nombre?.stringValue
        val saldoStr = fields?.saldo?.integerValue
        val saldo = saldoStr?.toIntOrNull()

        if (nombre != null && saldo != null) {
            JugadorTop(nombre = nombre, saldo = saldo)
        } else {
            null
        }
    }

    return jugadores
        .sortedByDescending { it.saldo }
        .take(10)
}
