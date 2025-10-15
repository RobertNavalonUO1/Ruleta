package com.api.ruletaeuropea.data.dao

import com.api.ruletaeuropea.data.entity.Ruleta

interface RuletaDao {
    @Insert
    suspend fun insertar(ruleta: Ruleta): Long

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Ruleta WHERE IDRuleta = :id")
    suspend fun obtenerPorId(id: Long): Ruleta?

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Ruleta ORDER BY IDRuleta DESC LIMIT :limite")
    fun ultimasTiradas(limite: Int = 50): Flow<List<Ruleta>>

    @Query("DELETE FROM com.api.ruletaeuropea.data.entity.Ruleta")
    suspend fun limpiar()
}
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao

