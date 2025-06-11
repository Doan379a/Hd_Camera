package com.beautycam.hdcam.photoeditor.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivitySplashBinding
import com.beautycam.hdcam.photoeditor.ui.language_start.LanguageStartActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default);

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initView() {
        coroutineScope.launch {
            delay(3000)
            showActivity(LanguageStartActivity::class.java)
        }
    }

    override fun viewListener() {

    }

    override fun dataObservable() {

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
    }

}