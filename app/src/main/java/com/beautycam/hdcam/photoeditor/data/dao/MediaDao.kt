package com.beautycam.hdcam.photoeditor.data.dao

import androidx.room.*
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity

@Dao
interface MediaDao {

    @Insert
    suspend fun insertMedia(media: MediaEntity)

    @Query("SELECT * FROM media")
    suspend fun getAllLockedMedia(): List<MediaEntity>

    @Query("SELECT * FROM media WHERE filePath = :filePath")
    suspend fun getMediaByPath(filePath: String): MediaEntity?

    @Delete
    suspend fun deleteMedia(media: MediaEntity)
}
