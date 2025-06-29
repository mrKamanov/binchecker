package com.binchecker.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.binchecker.domain.model.BinInfo

@Database(entities = [BinInfo::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BinDatabase : RoomDatabase() {
    abstract fun binDao(): BinDao

    companion object {
        @Volatile
        private var INSTANCE: BinDatabase? = null

        fun getDatabase(context: Context): BinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BinDatabase::class.java,
                    "bin_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 