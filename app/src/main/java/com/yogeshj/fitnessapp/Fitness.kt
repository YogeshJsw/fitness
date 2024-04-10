package com.yogeshj.fitnessapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness")
class Fitness(
    var videoId: String,
    var name: String,
    var steps: String,
    var ing: String,
    var category: String
) {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var uid = 0
}