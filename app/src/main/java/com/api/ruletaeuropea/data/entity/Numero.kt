package com.api.ruletaeuropea.data.entity

import ColorEnum
import MitadEnum
import ParEnum
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Numero")
data class Numero(
    @PrimaryKey val Numero: Int,
    val Color: ColorEnum,
    val Par: ParEnum,
    val MitadSup: MitadEnum
)

