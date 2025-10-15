package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Jugador
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadorDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(jugador: Jugador)

    @Update
    suspend fun actualizar(jugador: Jugador)

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Jugador WHERE NombreJugador = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Jugador?

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Jugador ORDER BY NombreJugador ASC")
    fun listarTodos(): Flow<List<Jugador>>

    @Query("UPDATE com.api.ruletaeuropea.data.entity.Jugador SET NumMonedas = NumMonedas + :delta WHERE NombreJugador = :nombre")
    suspend fun aplicarDeltaMonedas(nombre: String, delta: Int)

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Jugador ORDER BY NumMonedas DESC LIMIT :limite")
    fun verRanking(limite: Int = 50): Flow<List<Jugador>>

    @Delete
    suspend fun eliminar(jugador: Jugador)
}

