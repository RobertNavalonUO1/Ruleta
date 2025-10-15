package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Ranking

@Dao
interface RankingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(registros: List<Ranking>)

    @Query("SELECT * FROM Ranking ORDER BY Posicion ASC")
    suspend fun verRanking(): List<Ranking>

    @Query("DELETE FROM Ranking")
    suspend fun limpiar()
}

