package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Ruleta
import kotlinx.coroutines.flow.Flow

@Dao
interface RuletaDao {

    @Insert
    suspend fun insertar(ruleta: Ruleta): Long

    @Query("SELECT * FROM Ruleta WHERE IDRuleta = :id")
    suspend fun obtenerPorId(id: Long): Ruleta?

    @Query("SELECT * FROM Ruleta ORDER BY IDRuleta DESC LIMIT :limite")
    fun ultimasTiradas(limite: Int = 50): Flow<List<Ruleta>>

    @Query("DELETE FROM Ruleta")
    suspend fun limpiar()
}
