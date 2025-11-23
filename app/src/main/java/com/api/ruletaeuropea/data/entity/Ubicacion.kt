package com.api.ruletaeuropea.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ubicacion")
data class Ubicacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double
)
