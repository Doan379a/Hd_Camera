package com.beautycam.hdcam.photoeditor.ui.language_start

import android.annotation.SuppressLint
import android.content.Intent
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivityLanguageStartBinding
import com.beautycam.hdcam.photoeditor.model.LanguageModel
import com.beautycam.hdcam.photoeditor.ui.intro.IntroActivity
import com.beautycam.hdcam.photoeditor.utils.SystemUtil
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.visible
import java.util.Locale

class LanguageStartActivity : BaseActivity<ActivityLanguageStartBinding>() {

    private lateinit var adapter: LanguageStartAdapter
    private var listLanguage: ArrayList<LanguageModel> = ArrayList()
    private var codeLang = ""


    override fun setViewBinding(): ActivityLanguageStartBinding {
        return ActivityLanguageStartBinding.inflate(layoutInflater)
    }

    override fun initView() {
        adapter = LanguageStartAdapter(this, onClick = {
            codeLang = it.code
            adapter.setCheck(it.code)
            binding.ivDone.visible()
        })
        binding.recyclerView.adapter = adapter
    }

    override fun viewListener() {
        binding.ivDone.tap {
            SystemUtil.saveLocale(baseContext, codeLang)
            startActivity(Intent(this@LanguageStartActivity, IntroActivity::class.java))
            finish()
        }
    }

    override fun dataObservable() {
        setCodeLanguage()
        initData()
    }

    private fun setCodeLanguage() {
        //language
        val codeLangDefault = Locale.getDefault().language
        val langDefault = arrayOf("fr", "pt", "es", "de", "in", "en", "hi", "vi", "ja")
        codeLang =
            if (SystemUtil.getPreLanguage(this).equals(""))
                if (!mutableListOf(*langDefault)
                        .contains(codeLangDefault)
                ) {
                    "en"
                } else {
                    codeLangDefault
                } else {
                SystemUtil.getPreLanguage(this)
            }
    }

    private fun initData() {
        var pos = 0
        listLanguage.clear()
        listLanguage.addAll(SystemUtil.listLanguage())
//        listLanguage.forEachIndexed { index, languageModel ->
//            if (languageModel.code == codeLang) {
//                pos = index
//                return@forEachIndexed
//            }
//        }
//        val temp = listLanguage[pos]
//        temp.active = true
//        listLanguage.removeAt(pos)
//        listLanguage.add(0, temp)
        adapter.addList(listLanguage)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finishAffinity()
    }

}