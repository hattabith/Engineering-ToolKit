package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CalculationHistoryEntity): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT calculatorId FROM favorite_calculators")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_calculators WHERE calculatorId = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorite_calculators WHERE calculatorId = :id")
    suspend fun removeFavorite(id: String)
}
