package com.beautycam.hdcam.photoeditor.utils

import android.content.Context
import android.util.Log
import com.beautycam.hdcam.photoeditor.library.colorpicker.ColorPickerPopUp

fun Context.showColorPicker(defaultColor: Int, onColorPicked: (String) -> Unit, onDismiss: (() -> Unit) ?=null) {
    val colorPickerPopUp = ColorPickerPopUp(this)
    colorPickerPopUp.setShowAlpha(true)
        .setDefaultColor(defaultColor)
        .setDialogTitle("Pick a Color")
        .setOnPickColorListener(object : ColorPickerPopUp.OnPickColorListener {
            override fun onColorPicked(color: Int) {
                val colorString = String.format("#%06X", (0xFFFFFF and color))
                Log.d("ColorPicker", colorString)
                onColorPicked(colorString)
                onDismiss?.invoke()
            }

            override fun onCancel() {
                colorPickerPopUp.dismissDialog()
                onDismiss?.invoke()
            }
        })
    colorPickerPopUp.show()
}
