package com.beautycam.hdcam.photoeditor.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository

class MediaViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            return MediaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
