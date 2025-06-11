package com.beautycam.hdcam.photoeditor.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.entity.MediaType
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.Foder_Key
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LoadImageUtils {
    fun getMediaFromHD(context: Context): List<MediaEntity> {
        val mediaList = mutableListOf<MediaEntity>()
        val resolver = context.applicationContext.contentResolver

        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND (${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)"
        val selectionArgs = arrayOf(
            "DCIM/$Foder_Key%",
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        val cursor = resolver.query(uri, projection, selection, selectionArgs, "${MediaStore.Files.FileColumns.DATE_ADDED} DESC")

        cursor?.use {
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val mediaTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataIndex)
                val mimeType = cursor.getString(mimeTypeIndex)
                val mediaTypeValue = cursor.getInt(mediaTypeIndex)

                val mediaType = when (mediaTypeValue) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaType.IMAGE
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.VIDEO
                    else -> null
                }

                if (mediaType != null) {
                    mediaList.add(
                        MediaEntity(
                            filePath = filePath,
                            mediaType = mediaType
                        )
                    )
                }
            }
        }

        return mediaList
    }

     fun getMediaStoreUri(context: Context, file: File): Uri? {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = MediaStore.Files.FileColumns.DATA + "=?"
        val selectionArgs = arrayOf(file.absolutePath)

        val uriExternal = MediaStore.Files.getContentUri("external")
        context.contentResolver.query(uriExternal, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                return ContentUris.withAppendedId(uriExternal, id)
            }
        }
        return null
    }

    fun generateTimestampedFileName(baseName: String, extension: String = ".mp4"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
        return "${baseName}_$timestamp$extension"
    }
    fun getFileFromUri(context: Context, uri: Uri): File? {
        // Trường hợp scheme là "file"
        if ("file" == uri.scheme) {
            return File(uri.path!!)
        }
        // Trường hợp scheme là "content"
        if ("content" == uri.scheme) {
            var cursor: Cursor? = null
            try {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val filePath = cursor.getString(columnIndex)
                    if (filePath != null) {
                        return File(filePath)
                    }
                }
            } catch (e: Exception) {
                // Một số hệ máy mới sẽ không còn MediaStore.Images.Media.DATA
                // Dưới đây là cách fallback
                try {
                    val fileName = getFileName(context, uri)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, fileName ?: "temp_file")
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    return tempFile
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } finally {
                cursor?.close()
            }
        }
        return null
    }

    // Hàm phụ lấy file name từ Uri
    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}