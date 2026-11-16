package com.transitmate.bd.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedCardEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TransitMateDatabase : RoomDatabase() {
    abstract fun transitCardDao(): TransitCardDao

    companion object {
        @Volatile
        private var instance: TransitMateDatabase? = null

        fun getInstance(context: Context): TransitMateDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TransitMateDatabase::class.java,
                    "transitmate_local.db"
                ).build().also { instance = it }
            }
        }
    }
}
