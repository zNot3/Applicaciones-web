package com.curso.android.module4.cityspots.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.curso.android.module4.cityspots.data.dao.SpotDao
import com.curso.android.module4.cityspots.data.entity.SpotEntity

@Database(
    entities = [SpotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpotDatabase : RoomDatabase() {

    abstract fun spotDao(): SpotDao

    companion object {
        private const val DATABASE_NAME = "cityspots_database"

        @Volatile
        private var INSTANCE: SpotDatabase? = null

        fun getInstance(context: Context): SpotDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): SpotDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SpotDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
