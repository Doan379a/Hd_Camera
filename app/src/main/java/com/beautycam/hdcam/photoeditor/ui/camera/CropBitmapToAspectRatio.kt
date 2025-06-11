package com.beautycam.hdcam.photoeditor.ui.camera

import android.graphics.Bitmap

fun cropBitmapToAspectRatio(bitmap: Bitmap, aspectRatio: Float): Bitmap {
    val srcWidth = bitmap.width
    val srcHeight = bitmap.height

    val targetWidth: Int
    val targetHeight: Int

    if (srcWidth.toFloat() / srcHeight > aspectRatio) {
        // Width quá lớn, crop ngang
        targetHeight = srcHeight
        targetWidth = (targetHeight * aspectRatio).toInt()
    } else {
        // Height quá lớn, crop dọc
        targetWidth = srcWidth
        targetHeight = (targetWidth / aspectRatio).toInt()
    }

    val xOffset = (srcWidth - targetWidth) / 2
    val yOffset = (srcHeight - targetHeight) / 2

    return Bitmap.createBitmap(bitmap, xOffset, yOffset, targetWidth, targetHeight)
}
