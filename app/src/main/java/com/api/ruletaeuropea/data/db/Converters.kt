package com.api.ruletaeuropea.data.db

import com.api.ruletaeuropea.data.model.CategoriaApostada
import com.api.ruletaeuropea.data.model.ColorEnum
import com.api.ruletaeuropea.data.model.ParEnum
import com.api.ruletaeuropea.data.model.MitadEnum

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromCategoria(v: CategoriaApostada) = v.name
    @TypeConverter fun toCategoria(v: String) = CategoriaApostada.valueOf(v)

    @TypeConverter fun fromColor(v: ColorEnum?) = v?.name
    @TypeConverter fun toColor(v: String?) = v?.let { ColorEnum.valueOf(it) }

    @TypeConverter fun fromPar(v: ParEnum?) = v?.name
    @TypeConverter fun toPar(v: String?) = v?.let { ParEnum.valueOf(it) }

    @TypeConverter fun fromMitad(v: MitadEnum?) = v?.name
    @TypeConverter fun toMitad(v: String?) = v?.let { MitadEnum.valueOf(it) }

    @TypeConverter fun fromIntList(list: ArrayList<Int>?): String? =
        list?.joinToString(",")

    @TypeConverter fun toIntList(data: String?): ArrayList<Int>? =
        data?.takeIf { it.isNotBlank() }?.split(",")?.map { it.toInt() }?.let { ArrayList(it) }
}

