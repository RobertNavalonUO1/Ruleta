package com.api.ruletaeuropea.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

const val premioAcumuladoId = 0
@Entity(tableName = "PremioAcumulado")
data class PremioAcumulado(
    @PrimaryKey val id: Int = premioAcumuladoId, // Siempre habrá solo un registro
    val premioAcumulado: Int
)