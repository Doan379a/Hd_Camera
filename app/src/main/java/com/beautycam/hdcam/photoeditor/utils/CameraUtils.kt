package com.beautycam.hdcam.photoeditor.utils

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.beautycam.hdcam.photoeditor.ui.vault.VaultActivity
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.generateTimestampedFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CameraUtils {
    val Foder_Key = "Beauty_Camera_HD"
    const val DELETE_REQUEST_CODE = 1001

    fun copyFileToPrivateStorage(
        context: Context,
        uri: Uri,
        index: Int,
        onResult: (File?) -> Unit
    ) {
        try {
            val fileName = generateTimestampedFileName("IMAGE_LOCK_(${index + 1})", ".jpg")
            val targetDir = File(context.getExternalFilesDir("locked_videos"), "")
            if (!targetDir.exists()) targetDir.mkdirs()
            val targetFile = File(targetDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            onResult(targetFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("LOII", "$e")
            onResult(null)
        }
    }


    fun copyAnyFileToPrivate(
        context: Context,
        filePath: String,
        onResult: (File?, Boolean) -> Unit
    ) {
        val fileName = generateTimestampedFileName("IMAGE_LOCK", ".jpg")
        try {
            val file = File(filePath)
            if (file.exists()) {
                Log.d("TAO_NE", "__00000")
                val targetDir = File(context.getExternalFilesDir("locked_videos"), "")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                file.copyTo(targetFile, overwrite = true)
                onResult(targetFile, true)
                return
            }
        } catch (e: Exception) {
            Log.d("TAO_NE", "___111$e")
            try {
                val uri = getMediaStoreUriFromPath(context, filePath)
                if (uri != null) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val targetDir = File(context.getExternalFilesDir("locked_videos"), "")
                    if (!targetDir.exists()) targetDir.mkdirs()
                    val targetFile = File(targetDir, fileName)
                    val outputStream = FileOutputStream(targetFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    onResult(targetFile, true)
                    return
                }


            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.d("TAO_NE", "___222$ex")
            }

        }
        onResult(null, false)
    }

    fun getMediaStoreUriFromPath(context: Context, filePath: String): Uri? {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DATA}=?"
        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            arrayOf(filePath),
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(0)
                return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            }
        }
        return null
    }

    suspend fun copyFileToPrivateStorageSuspendVideo(
        context: Context,
        uri: Uri,
        index: Int
    ): File? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = generateTimestampedFileName("VIDEO_LOCK_(${index + 1})", ".mp4")
                val targetDir = File(context.getExternalFilesDir("locked_videos"), "")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                targetFile
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("LOII", "$e")
                null
            }
        }
    }

    fun buildDeleteIntentSender(
        context: Context,
        file: File,
        onResult: (IntentSenderRequest?, Boolean) -> Unit
    ) {
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _, uri ->
            if (uri == null) {
                // Không tìm thấy trong MediaStore, có thể xóa vật lý nếu muốn
                val deleted = file.delete()
                onResult(null, deleted)
                return@scanFile
            }
            // Đã có uri trong MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intentSender = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        listOf(uri)
                    ).intentSender
                    val request = IntentSenderRequest.Builder(intentSender).build()
                    onResult(request, false) // Cần xác nhận từ user
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(null, false)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val rows = context.contentResolver.delete(uri, null, null)
                    val dummyIntent = Intent()
                    val dummySender = PendingIntent.getActivity(
                        context,
                        0,
                        dummyIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    ).intentSender
                    val request = IntentSenderRequest.Builder(dummySender).build()
                    onResult(request, rows > 0) // Đã xóa xong
                } catch (e: SecurityException) {
                    // RecoverableSecurityException sẽ trả về yêu cầu xác nhận của user
                    if (e is RecoverableSecurityException) {
                        val intentSender = e.userAction.actionIntent.intentSender
                        val request = IntentSenderRequest.Builder(intentSender).build()
                        onResult(request, false)
                    } else {
                        onResult(null, false)
                    }
                }
            } else {
                // Android thấp hơn Q: Xóa luôn
                val rows = context.contentResolver.delete(uri, null, null)
                onResult(null, rows > 0)
            }
        }
    }

    fun buildDeleteIntentSender(
        context: Context,
        uri: Uri,
        onResult: (IntentSenderRequest?, Boolean) -> Unit
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 trở lên
                val intentSender = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    listOf(uri)
                ).intentSender
                val request = IntentSenderRequest.Builder(intentSender).build()
                onResult(request, false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10
                try {
                    context.contentResolver.delete(uri, null, null)
                    val dummyIntent = Intent()
                    val dummySender = PendingIntent.getActivity(
                        context,
                        0,
                        dummyIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    ).intentSender
                    val request = IntentSenderRequest.Builder(dummySender).build()
                    onResult(request, true)
                    Log.d("TAO_NE", "Thanh cong ??")
                } catch (e: SecurityException) {
                    if (e is RecoverableSecurityException) {
                        val intentSender = e.userAction.actionIntent.intentSender
                        val request = IntentSenderRequest.Builder(intentSender).build()
                        onResult(request, false)
                    } else {
                        e.printStackTrace()
                        onResult(null, false)
                    }
                }
            } else {
                // Android 9 trở xuống
                context.contentResolver.delete(uri, null, null)
                onResult(null, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(null, false)
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteMediaFileFromMediaStore(context: Context, file: File) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            null
        ) { path, uri ->
            try {
                val rows = context.contentResolver.delete(uri, null, null)
                Log.d("DEBUG", "Deleted via contentResolver: $rows rows for $uri")
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    val intentSender = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        listOf(uri)
                    ).intentSender

                    if (context is Activity) {
                        context.startIntentSenderForResult(
                            intentSender,
                            DELETE_REQUEST_CODE,
                            null,
                            0,
                            0,
                            0
                        )
                    } else {
                        Log.e("DEBUG", "Context is not Activity, cannot request delete permission")
                    }
                } else {
                    e.printStackTrace()
                }
            }
        }
    }


    fun restoreLockedFileToGallery(context: Context, lockedFile: File): String? {
        if (!lockedFile.exists()) return null
        val fileName = generateTimestampedFileName("IMAGE", ".jpg")
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$Foder_Key")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    lockedFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }


                val deleted = lockedFile.delete()
                Log.d("DEBUG", "Deleted cache file: $deleted")

                MediaScannerConnection.scanFile(context, arrayOf(fileName), null, null)

                return it.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("DEBUG", "Error restoring file: ${e.message}")
            }
        }

        return null
    }

    fun restoreLockedFileToVideo(context: Context, lockedFile: File): Uri? {
        if (!lockedFile.exists()) return null
        val fileName = generateTimestampedFileName("VIDEO", ".mp4")
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$Foder_Key")
        }

        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    lockedFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val deleted = lockedFile.delete()
                Log.d("DEBUG", "Deleted cache file: $deleted")

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(lockedFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )

                return it
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("DEBUG", "Error restoring file: ${e.message}")
            }
        }

        return null
    }

    fun deleteLocalFileCache(file: File): Boolean {
        return if (file.exists()) {
            val deleted = file.delete()
            Log.d("DEBUG", "Deleted file: ${file.absolutePath}, success=$deleted")
            deleted
        } else {
            Log.d("DEBUG", "File not found for deletion: ${file.absolutePath}")
            false
        }
    }


    fun shareVideo(context: Context, videoPath: String) {
        val videoFile = File(videoPath)

        if (!videoFile.exists()) {
            Log.e("VideoPagerFragment", "File not found: $videoPath")
            return
        }

        val videoUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.applicationContext.packageName}.provider",
            videoFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, videoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
    }

    fun shareImage(context: Context, imagePath: String) {
        val imageFile = File(imagePath)

        if (!imageFile.exists()) {
            Log.e("ShareImage", "File not found: $imagePath")
            return
        }

        val imageUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.applicationContext.packageName}.provider",
            imageFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ hình ảnh"))
    }

    data class VideoItem(
        val id: Long,
        val title: String,
        val uri: Uri,
        val duration: Long
    )

    fun getAllVideos(context: Context): List<VideoItem> {
        val videoList = mutableListOf<VideoItem>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Không tên"
                val duration = cursor.getLong(durationColumn)

                val contentUri = Uri.withAppendedPath(collection, id.toString())

                videoList.add(VideoItem(id, title, contentUri, duration))
            }
        }

        return videoList
    }

    fun getCorrectlyOrientedBitmap(context: Context, imageUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStream.close()

        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    fun resizeBitmapToView(bitmap: Bitmap, view: ImageView): Bitmap {
        val width = view.width
        val height = view.height
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

}