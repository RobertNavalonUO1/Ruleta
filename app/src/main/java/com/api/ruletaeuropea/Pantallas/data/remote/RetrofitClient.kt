package com.api.ruletaeuropea.Pantallas.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://firestore.googleapis.com/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: FirebaseApi = retrofit.create(FirebaseApi::class.java)
}
