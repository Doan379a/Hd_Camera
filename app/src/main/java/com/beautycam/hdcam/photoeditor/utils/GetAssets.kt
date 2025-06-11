package com.beautycam.hdcam.photoeditor.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException

fun Context.getBitmapFromAsset(context: Context, strName: String): Bitmap? {
    val assetManager = context.assets
    return try {
        val istr = assetManager.open(strName)
        BitmapFactory.decodeStream(istr)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Context.getAssetAudioFileDescriptor(fileName: String): AssetFileDescriptor? {
    return try {
        this.assets.openFd(fileName)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
