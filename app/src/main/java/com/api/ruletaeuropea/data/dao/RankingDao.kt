package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Ranking

@Dao
interface RankingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(registros: List<Ranking>)

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Ranking ORDER BY Posicion ASC")
    suspend fun verRanking(): List<Ranking>

    @Query("DELETE FROM com.api.ruletaeuropea.data.entity.Ranking")
    suspend fun limpiar()
}

