package com.beautycam.hdcam.photoeditor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.beautycam.hdcam.photoeditor.data.dao.MediaDao
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity


@Database(entities = [MediaEntity::class], version = 1, exportSchema = false)
abstract class DSDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: DSDatabase? = null

        fun getDatabase(context: Context): DSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DSDatabase::class.java,
                    "media_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
