package com.api.ruletaeuropea.data.entity

import com.api.ruletaeuropea.data.model.ColorEnum
import com.api.ruletaeuropea.data.model.ParEnum
import com.api.ruletaeuropea.data.model.MitadEnum
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Numero")
data class Numero(
    @PrimaryKey val Numero: Int,
    val Color: ColorEnum?,
    val Par: ParEnum?,
    val MitadSup: MitadEnum?
)

