package com.yogeshj.myFitness

import androidx.room.Dao
import androidx.room.Query


@Dao
interface Dao {
    @Query("SELECT * FROM fitness")
    fun getAll(): List<Fitness>
}