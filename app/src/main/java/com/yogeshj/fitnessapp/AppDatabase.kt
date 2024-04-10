package com.yogeshj.fitnessapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Fitness::class], exportSchema = false, version = 1)
abstract class AppDatabase :RoomDatabase() {
    abstract fun getDao():Dao
}