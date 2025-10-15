package com.api.ruletaeuropea

import android.app.Application
import androidx.room.Room
import com.api.ruletaeuropea.data.RuletaDatabase

class App : Application() {
    companion object {
        lateinit var database: RuletaDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            RuletaDatabase::class.java,
            "ruleta_db"
        ).build()
    }
}

