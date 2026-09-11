package com.example.data.repository

import com.example.data.db.CalculationHistoryDao
import com.example.data.db.CalculationHistoryEntity
import com.example.data.db.FavoriteDao
import com.example.data.db.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationHistoryDao) {
    val allHistory: Flow<List<CalculationHistoryEntity>> = dao.getAllHistory()

    suspend fun recordCalculation(
        calculatorId: String,
        calculatorTitle: String,
        inputSummary: String,
        resultSummary: String
    ) {
        dao.insert(
            CalculationHistoryEntity(
                calculatorId = calculatorId,
                calculatorTitle = calculatorTitle,
                inputSummary = inputSummary,
                resultSummary = resultSummary
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = dao.deleteById(id)

    suspend fun clearHistory() = dao.clearAll()
}

class FavoriteRepository(private val dao: FavoriteDao) {
    val favoriteCalculatorIds: Flow<List<String>> = dao.getAllFavoriteIds()

    fun isFavorite(calculatorId: String): Flow<Boolean> = dao.isFavorite(calculatorId)

    suspend fun toggleFavorite(calculatorId: String, currentIsFav: Boolean) {
        if (currentIsFav) {
            dao.removeFavorite(calculatorId)
        } else {
            dao.addFavorite(FavoriteEntity(calculatorId = calculatorId))
        }
    }
}
