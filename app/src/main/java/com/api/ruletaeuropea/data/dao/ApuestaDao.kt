package com.api.ruletaeuropea.data.dao
import androidx.room.*
import com.api.ruletaeuropea.data.entity.Apuesta

@Dao
interface ApuestaDao {
    @Insert
    suspend fun insertar(apuesta: Apuesta): Long

    @Insert
    suspend fun insertarTodas(apuestas: List<Apuesta>): List<Long>

    @Query("SELECT * FROM Apuesta WHERE IDRuleta = :idRuleta ORDER BY NumeroApuesta ASC")
    suspend fun obtenerPorRuleta(idRuleta: Long): List<Apuesta>

    @Query("SELECT * FROM Apuesta WHERE NombreJugador = :nombre ORDER BY CreadaEn DESC LIMIT :limite")
    suspend fun ultimasPorJugador(nombre: String, limite: Int = 100): List<Apuesta>

    @Query("SELECT COALESCE(SUM(MonedasApostadas), 0) FROM Apuesta WHERE IDRuleta = :idRuleta")
    suspend fun sumaApostadoEnRuleta(idRuleta: Long): Int

    @Query("SELECT COALESCE(SUM(Pago), 0) FROM Apuesta WHERE IDRuleta = :idRuleta")
    suspend fun sumaPagadoEnRuleta(idRuleta: Long): Int

    @Query("UPDATE Apuesta SET Ganada = :ganada, Pago = :pago WHERE NumeroApuesta = :numApuesta")
    suspend fun actualizarResultado(numApuesta: Long, ganada: Boolean, pago: Int)

    @Query("DELETE FROM Apuesta WHERE IDRuleta = :idRuleta")
    suspend fun borrarPorRuleta(idRuleta: Long)

    @Query("DELETE FROM Apuesta")
    suspend fun limpiar()
}

