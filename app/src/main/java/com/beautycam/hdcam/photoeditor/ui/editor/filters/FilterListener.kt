package com.beautycam.hdcam.photoeditor.ui.editor.filters

import ja.burhanrashid52.photoeditor.PhotoFilter

interface FilterListener {
    fun onFilterSelected(photoFilter: PhotoFilter)
}