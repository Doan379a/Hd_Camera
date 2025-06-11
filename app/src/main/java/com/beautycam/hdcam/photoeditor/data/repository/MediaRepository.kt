package com.beautycam.hdcam.photoeditor.data.repository

import com.beautycam.hdcam.photoeditor.data.dao.MediaDao
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val mediaDao: MediaDao) {

    suspend fun insertMedia(media: MediaEntity) {
        mediaDao.insertMedia(media)
    }

    suspend fun getAllLockedMedia(): List<MediaEntity> {
        return mediaDao.getAllLockedMedia()
    }

    suspend fun getMediaByPath(filePath: String): MediaEntity? {
        return mediaDao.getMediaByPath(filePath)
    }

    suspend fun deleteMedia(media: MediaEntity) {
        mediaDao.deleteMedia(media)
    }
}