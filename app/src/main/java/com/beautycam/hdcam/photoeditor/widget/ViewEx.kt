package com.beautycam.hdcam.photoeditor.widget

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.tap(action: (view: View?) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        it.postDelayed({ it.isEnabled = true }, 1500)
        action(it)
    }
}

fun View.tap2(action: (view: View?) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        it.postDelayed({ it.isEnabled = true }, 200)
        action(it)
    }
}
fun View.tap3(action: (view: View?) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        it.postDelayed({ it.isEnabled = true }, 1000)
        action(it)
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun handleKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = (context as Activity).currentFocus
    currentFocus?.let {
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}


fun View.visibleIf(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.GONE
}
