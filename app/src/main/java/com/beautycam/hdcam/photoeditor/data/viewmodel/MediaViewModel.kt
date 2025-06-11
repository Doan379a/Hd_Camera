package com.beautycam.hdcam.photoeditor.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository

class MediaViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _lockedMedia = MutableLiveData<List<MediaEntity>>()
    val lockedMedia: LiveData<List<MediaEntity>> get() = _lockedMedia

    private val _shouldReloadMedia = MutableLiveData<Boolean>()
    val shouldReloadMedia: LiveData<Boolean> get() = _shouldReloadMedia

    fun notifyReloadMedia() {
        _shouldReloadMedia.value = true
    }



fun fetchLockedMedia() {
    viewModelScope.launch {
        val mediaList = repository.getAllLockedMedia()
        _lockedMedia.postValue(mediaList)
    }
}

fun insertMedia(media: MediaEntity) {
    viewModelScope.launch {
        repository.insertMedia(media)
        fetchLockedMedia()
    }
}

fun deleteMedia(media: MediaEntity) {
    viewModelScope.launch {
        repository.deleteMedia(media)
        fetchLockedMedia()
    }
}
}
