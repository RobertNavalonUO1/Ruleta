package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Numero

@Dao
interface NumeroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(lista: List<Numero>)

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Numero ORDER BY com.api.ruletaeuropea.data.entity.Numero ASC")
    suspend fun obtenerTodos(): List<Numero>

    @Query("SELECT * FROM com.api.ruletaeuropea.data.entity.Numero WHERE com.api.ruletaeuropea.data.entity.Numero = :n LIMIT 1")
    suspend fun obtener(n: Int): Numero?
}

