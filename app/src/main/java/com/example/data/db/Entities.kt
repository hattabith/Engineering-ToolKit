package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val calculatorId: String,
    val calculatorTitle: String,
    val inputSummary: String,
    val resultSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_calculators")
data class FavoriteEntity(
    @PrimaryKey
    val calculatorId: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)
