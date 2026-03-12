package com.curso.android.module3.amiibo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.curso.android.module3.amiibo.data.local.dao.AmiiboDao
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity

@Database(
    entities = [AmiiboEntity::class, AmiiboDetailEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AmiiboDatabase : RoomDatabase() {

    abstract fun amiiboDao(): AmiiboDao

    companion object {
        const val DATABASE_NAME = "amiibo_database"
    }
}
