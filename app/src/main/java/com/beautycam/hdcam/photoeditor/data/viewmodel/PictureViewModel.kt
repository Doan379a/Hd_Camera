package com.beautycam.hdcam.photoeditor.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity

class PictureViewModel : ViewModel() {
    private val _listPicture = MutableLiveData<List<String>>()
    val listPicture: LiveData<List<String>> get() = _listPicture

    private val _listVideo = MutableLiveData<List<String>>()
    val listVideo: LiveData<List<String>> get() = _listVideo


    private val _clickItemImage = MutableLiveData<Pair<List<String>, Int>>()
    val clickItemImage: LiveData<Pair<List<String>, Int>> get() = _clickItemImage

    private val _clickItemVideo = MutableLiveData<Pair<List<String>, Int>>()
    val clickItemVideo: LiveData<Pair<List<String>, Int>> get() = _clickItemVideo
    private val _clickItemALL = MutableLiveData<Pair<List<MediaEntity>, Int>>()
    val clickItemALL: LiveData<Pair<List<MediaEntity>, Int>> get() = _clickItemALL


    fun setClickItemALL(path: List<MediaEntity>, position: Int) {
        Log.d("KK", "setClickItem: $path")
        _clickItemALL.value = Pair(path, position)
    }
    fun setClickItemVideo(path: List<String>, position: Int) {
        Log.d("KK", "setClickItem: $path")
        _clickItemVideo.value = Pair(path, position)
    }

    fun setClickItemImage(path: List<String>, position: Int) {
        Log.d("KK", "setClickItem: $path")
        _clickItemImage.value = Pair(path, position)
    }


    fun setListPicture(list: List<String>) {
        Log.d("KK", "setListPicture called with size = ${list.size}")
        _listPicture.value = list.toList()
    }

    fun setListVideo(list: List<String>) {
        _listVideo.value = list
    }
}