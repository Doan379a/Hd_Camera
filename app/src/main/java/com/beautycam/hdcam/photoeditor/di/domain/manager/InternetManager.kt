package com.beautycam.hdcam.photoeditor.di.domain.manager

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.beautycam.hdcam.photoeditor.utils.ConstantUtils.TAG


class InternetManager(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isInternetConnected: Boolean
        @SuppressLint("MissingPermission")
        get() {
            try {
                val network = connectivityManager.activeNetwork ?: return false
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                    else -> false
                }
            } catch (ex: Exception) {
                Log.e(TAG, "isInternetConnected: ", ex)
                return false
            }
        }
}