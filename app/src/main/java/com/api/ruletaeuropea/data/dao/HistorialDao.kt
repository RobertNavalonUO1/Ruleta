package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Historial
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {
    @Insert
    suspend fun insertar(reg: Historial): Long

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Historial WHERE NombreJugador = :nombre ORDER BY Fecha DESC")
    fun verHistorial(nombre: String): Flow<List<Historial>>

    @Query("DELETE FROM com.api.ruletaeuropea.data.entity.Historial")
    suspend fun limpiar()
}

