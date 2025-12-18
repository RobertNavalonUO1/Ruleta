package com.api.ruletaeuropea.Pantallas.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FirestoreJugadoresResponse(
    val documents: List<FirestoreJugadorDocument>?
)

@JsonClass(generateAdapter = true)
data class FirestoreJugadorDocument(
    val fields: FirestoreJugadorFields?
)

@JsonClass(generateAdapter = true)
data class FirestoreJugadorFields(
    val nombre: FirestoreStringValue?,
    val saldo: FirestoreIntValue?
)

@JsonClass(generateAdapter = true)
data class FirestoreStringValue(
    val stringValue: String?
)

@JsonClass(generateAdapter = true)
data class FirestoreIntValue(
    val integerValue: String?
)
