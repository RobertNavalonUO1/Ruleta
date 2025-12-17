package com.api.ruletaeuropea.data.dao
import androidx.room.*
import com.api.ruletaeuropea.data.entity.PremioAcumulado

@Dao
interface PremioAcumuladoDao {
    @Query("SELECT * FROM PremioAcumulado WHERE id = 0")
    suspend fun obtener(): PremioAcumulado?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(fondo: PremioAcumulado)
}