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

    // 🔹 Usa el nombre de la tabla, no la ruta completa
    @Query("SELECT * FROM Jugador WHERE NombreJugador = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Jugador?

    @Query("SELECT * FROM Jugador ORDER BY NombreJugador ASC")
    fun listarTodos(): Flow<List<Jugador>>

    // 🔹 También aquí, solo 'Jugador'
    @Query("UPDATE Jugador SET NumMonedas = NumMonedas + :delta WHERE NombreJugador = :nombre")
    suspend fun aplicarDeltaMonedas(nombre: String, delta: Int)

    @Query("SELECT * FROM Jugador ORDER BY NumMonedas DESC LIMIT :limite")
    fun verRanking(limite: Int = 50): Flow<List<Jugador>>

    @Delete
    suspend fun eliminar(jugador: Jugador)

    @Query("DELETE FROM Jugador")
    suspend fun borrarTodos()
}
