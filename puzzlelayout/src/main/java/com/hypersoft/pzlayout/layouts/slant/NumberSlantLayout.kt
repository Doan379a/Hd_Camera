package com.hypersoft.pzlayout.layouts.slant

import android.util.Log
import com.hypersoft.pzlayout.slant.SlantPuzzleLayout


abstract class NumberSlantLayout(theme: Int) : SlantPuzzleLayout() {

    companion object {
        const val TAG = "NumberSlantLayout" // Tag for logging purposes
    }

    val theme: Int // Holds the validated theme index

    init {
        // Validate the provided theme index and log an error if it's out of bounds
        this.theme = if (theme >= safeGetThemeCount()) {
            Log.e(
                TAG, "NumberSlantLayout: the most theme count is " +
                        safeGetThemeCount() +
                        " ,you should let theme from 0 to " +
                        (safeGetThemeCount() - 1) + " ."
            )
            // If the theme index is out of bounds, default to the last available theme
            safeGetThemeCount() - 1
        } else {
            theme // Use the provided theme index if valid
        }
    }


    private fun safeGetThemeCount(): Int {
        return getThemeCount()
    }


    abstract fun getThemeCount(): Int
}

