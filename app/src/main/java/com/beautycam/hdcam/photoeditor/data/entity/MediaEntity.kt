package com.beautycam.hdcam.photoeditor.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var filePath: String,
    val mediaType: MediaType
)

enum class MediaType {
    IMAGE,
    VIDEO
}