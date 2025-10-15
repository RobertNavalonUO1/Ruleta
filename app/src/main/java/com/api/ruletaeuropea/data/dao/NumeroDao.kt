package com.api.ruletaeuropea.data.dao

import androidx.room.*
import com.api.ruletaeuropea.data.entity.Numero

@Dao
interface NumeroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(lista: List<Numero>)

    @Query("SELECT * FROM Numero ORDER BY Numero ASC")
    suspend fun obtenerTodos(): List<Numero>

    @Query("SELECT * FROM Numero WHERE Numero = :n LIMIT 1")
    suspend fun obtener(n: Int): Numero?
}
