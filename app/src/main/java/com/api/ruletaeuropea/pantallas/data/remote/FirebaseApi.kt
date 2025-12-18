package com.api.ruletaeuropea.pantallas.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface FirebaseApi {

    @GET("projects/ruletaeuropea-bcb84/databases/(default)/documents/jugadores")
    suspend fun getJugadores(): Response<FirestoreJugadoresResponse>
}
