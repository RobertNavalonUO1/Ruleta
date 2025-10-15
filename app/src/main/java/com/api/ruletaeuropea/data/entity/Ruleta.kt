package com.api.ruletaeuropea.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "com.api.ruletaeuropea.data.entity.Ruleta")
data class Ruleta(
    @PrimaryKey(autoGenerate = true) val IDRuleta: Long = 0,
    val NumeroGanador: Int,
    val Timestamp: Long = System.currentTimeMillis()
)

