package com.api.ruletaeuropea.data.db

import com.api.ruletaeuropea.data.dao.HistorialDao
import com.api.ruletaeuropea.data.dao.JugadorDao
import com.api.ruletaeuropea.data.dao.NumeroDao
import com.api.ruletaeuropea.data.dao.RankingDao
import com.api.ruletaeuropea.data.dao.RuletaDao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.api.ruletaeuropea.data.*
import com.api.ruletaeuropea.data.dao.ApuestaDao
import com.api.ruletaeuropea.data.entity.Apuesta
import com.api.ruletaeuropea.data.entity.Historial
import com.api.ruletaeuropea.data.entity.Jugador
import com.api.ruletaeuropea.data.entity.Numero
import com.api.ruletaeuropea.data.entity.Ranking
import com.api.ruletaeuropea.data.entity.Ruleta

@Database(
    entities = [
        Jugador::class,
        Numero::class,
        Ruleta::class,
        Apuesta::class,
        Historial::class,
        Ranking::class
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
}
