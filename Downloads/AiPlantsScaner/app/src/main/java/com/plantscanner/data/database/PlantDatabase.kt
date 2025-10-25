package com.plantscanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.plantscanner.data.model.Converters
import com.plantscanner.data.model.PlantAnalysis
import com.plantscanner.util.Constants

@Database(
    entities = [PlantAnalysis::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlantDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    
    companion object {
        @Volatile
        private var INSTANCE: PlantDatabase? = null
        
        fun getDatabase(context: Context): PlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For dev: recreate DB on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
