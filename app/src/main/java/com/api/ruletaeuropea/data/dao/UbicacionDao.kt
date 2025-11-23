package com.api.ruletaeuropea.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.api.ruletaeuropea.data.entity.Ubicacion

@Dao
interface UbicacionDao {
    @Insert
    fun insert(ubicacion: Ubicacion)

    @Query("SELECT * FROM ubicacion")
    fun getAll(): List<Ubicacion>
}
