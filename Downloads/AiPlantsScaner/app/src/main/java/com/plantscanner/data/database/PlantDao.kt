package com.plantscanner.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.plantscanner.data.model.PlantAnalysis
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plant_analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<PlantAnalysis>>
    
    @Query("SELECT * FROM plant_analyses WHERE id = :id")
    suspend fun getAnalysisById(id: Long): PlantAnalysis?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: PlantAnalysis): Long
    
    @Delete
    suspend fun deleteAnalysis(analysis: PlantAnalysis)
    
    @Query("DELETE FROM plant_analyses")
    suspend fun deleteAllAnalyses()
}
