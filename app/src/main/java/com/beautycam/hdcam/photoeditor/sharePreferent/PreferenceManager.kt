package com.beautycam.hdcam.photoeditor.sharePreferent

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "spin_wheel_prefs"
        private const val KEY_CHECK_SOUND = "KEY_CHECK_SOUND"
        private const val STYLE_GRID = "STYLE_GRID"
        private const val PASS_CODE = "PASS_CODE"
        private const val SECURITY_QUESTION = "SECURITY_QUESTION"
        private const val SECURITY_RESULT = "SECURITY_RESULT"
        private const val URI_PHOTO = "URI_PHOTO"
    }

    fun saveCheckSound(value: Boolean) {
        sharedPref.edit().putBoolean(KEY_CHECK_SOUND, value).apply()
    }

    fun getCheckSound(): Boolean {
        return sharedPref.getBoolean(KEY_CHECK_SOUND, false)
    }

    fun clearCheckSound() {
        sharedPref.edit().remove(KEY_CHECK_SOUND).apply()
    }

    fun saveStyleGrid(value: Int) {
        sharedPref.edit().putInt(STYLE_GRID, value).apply()
    }

    fun getStyleGrid(): Int {
        return sharedPref.getInt(STYLE_GRID, 0)
    }

    fun savePassCode(value: String){
        sharedPref.edit().putString(PASS_CODE,value).apply()
    }

    fun getPassCode(): String? {
        return sharedPref.getString(PASS_CODE, "")
    }

    fun saveSecurityQuestion(value : Int){
        sharedPref.edit().putInt(SECURITY_QUESTION,value).apply()
    }

    fun getSecurityQuestion() : Int{
        return sharedPref.getInt(SECURITY_QUESTION, 0)
    }

    fun saveSecurityResult(value: String){
        sharedPref.edit().putString(SECURITY_RESULT,value).apply()
    }

    fun getSecurityResult(): String? {
        return sharedPref.getString(SECURITY_RESULT, "")
    }

    fun saveUriPhoto(value: String){
        sharedPref.edit().putString(URI_PHOTO,value).apply()
    }

    fun getUriPhoto(): String? {
        return sharedPref.getString(URI_PHOTO, null)
    }
}

