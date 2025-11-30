package com.api.ruletaeuropea.data.db

import com.api.ruletaeuropea.data.dao.HistorialDao
import com.api.ruletaeuropea.data.dao.JugadorDao
import com.api.ruletaeuropea.data.dao.NumeroDao
import com.api.ruletaeuropea.data.dao.RankingDao
import com.api.ruletaeuropea.data.dao.RuletaDao
import com.api.ruletaeuropea.data.dao.ApuestaDao
import com.api.ruletaeuropea.data.dao.UbicacionDao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.api.ruletaeuropea.data.*
import com.api.ruletaeuropea.data.entity.Apuesta
import com.api.ruletaeuropea.data.entity.Historial
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.data.entity.Numero
import com.api.ruletaeuropea.data.entity.Ranking
import com.api.ruletaeuropea.data.entity.Ruleta
import com.api.ruletaeuropea.data.entity.Ubicacion
import android.content.Context
import androidx.room.Room


@Database(
    entities = [
        Jugador::class,
        Numero::class,
        Ruleta::class,
        Apuesta::class,
        Historial::class,
        Ranking::class,
        Ubicacion::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RuletaDatabase : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao
    abstract fun numeroDao(): NumeroDao
    abstract fun ruletaDao(): RuletaDao
    abstract fun apuestaDao(): ApuestaDao
    abstract fun historialDao(): HistorialDao
    abstract fun rankingDao(): RankingDao
    abstract fun ubicacionDao(): UbicacionDao
    companion object {
        @Volatile
        private var INSTANCE: RuletaDatabase? = null

        fun getDatabase(context: Context): RuletaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RuletaDatabase::class.java,
                    "ruleta_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

