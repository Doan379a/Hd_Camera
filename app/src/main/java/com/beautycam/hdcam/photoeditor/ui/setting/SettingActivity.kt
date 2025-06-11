package com.beautycam.hdcam.photoeditor.ui.setting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivitySettingBinding
import com.beautycam.hdcam.photoeditor.sharePreferent.PreferenceManager
import com.beautycam.hdcam.photoeditor.sharePreferent.SharePrefUtils
import com.beautycam.hdcam.photoeditor.ui.language.LanguageActivity
import com.beautycam.hdcam.photoeditor.ui.pass.EnterPassActivity
import com.beautycam.hdcam.photoeditor.ui.pass.SecurityQuestionActivity
import com.beautycam.hdcam.photoeditor.utils.HelperMenu
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.visible

class SettingActivity : BaseActivity<ActivitySettingBinding>(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: PreferenceManager

    private var helperMenu: HelperMenu? = null

    override fun setViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        prefs = PreferenceManager(this)
        if (SharePrefUtils.isRated(this))
            binding.tvRate.gone()
        val passCode = prefs.getPassCode()
        if (passCode == null || passCode.isEmpty() || passCode.isBlank()){
            binding.tvChangePassword.gone()
        }else{
            binding.tvChangePassword.visible()
        }
//        checkSwitch()
    }

    override fun viewListener() {
//        binding.swVibration.setOnCheckedChangeListener { _, isChecked ->
//            prefs.saveCheckVibration(isChecked)
//            checkSwitch()
//        }
//        binding.swSound.setOnCheckedChangeListener { _, isChecked ->
//            prefs.saveCheckSound(isChecked)
//            checkSwitch()
//        }
        binding.apply {
            tvRate.tap { helperMenu?.showDialogRate(false) }
            tvFeedback.tap { helperMenu?.showDialogFeedback() }
            tvShare.tap { helperMenu?.showShareApp() }
            tvPolicy.tap { helperMenu?.showPolicy() }
            tvLanguage.tap { showActivity(LanguageActivity::class.java) }
            ivBack.tap { finish() }
            tvChangePassword.tap { showChangePassCode() }
        }
    }

//    private fun checkSwitch() {
//        binding.swVibration.alpha = if (prefs.getCheckVibration()) 1f else 0.5f
//        binding.swSound.alpha = if (prefs.getCheckSound()) 1f else 0.5f
//    }

    override fun dataObservable() {
        helperMenu = HelperMenu(this)

        val prefs = getSharedPreferences("data", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null)
            return

        if (SharePrefUtils.isRated(this))
            binding.tvRate.gone()
    }

    private fun showChangePassCode(){
        val intent = Intent(this, EnterPassActivity::class.java)
        intent.putExtra("changePass", true)
        startActivity(intent)
    }
}