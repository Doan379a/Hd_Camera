package com.beautycam.hdcam.photoeditor.ui.intro

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.beautycam.hdcam.photoeditor.ui.main.MainActivity
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivityIntroBinding
import com.beautycam.hdcam.photoeditor.sharePreferent.SharePrefUtils
import com.beautycam.hdcam.photoeditor.ui.permission.PermissionActivity

class IntroActivity : BaseActivity<ActivityIntroBinding>() {


    private lateinit var dots: Array<ImageView?>
    private val countPageIntro = 4


    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(layoutInflater)
    }

    override fun initView() {

        val pagerAdapter = IntroAdapter(supportFragmentManager, this)
        binding.viewPager2.adapter = pagerAdapter
        binding.viewPager2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                addBottomDots(position)
                when (position) {
                    0, 1, 2 -> binding.btnNextTutorial.text = getString(R.string.next)
                    else -> binding.btnNextTutorial.text = getString(R.string.start)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        addBottomDots(0)
    }

    override fun viewListener() {
        binding.btnNextTutorial.setOnClickListener {
            if (binding.viewPager2.currentItem == countPageIntro - 1) {
                startNextScreen()
            } else
                binding.viewPager2.currentItem += 1
        }
    }

    override fun dataObservable() {
        addBottomDots(0)
    }

    private fun startNextScreen() {
        if (SharePrefUtils.isGoToMain(this))
            showActivity(MainActivity::class.java)
        else
            showActivity(PermissionActivity::class.java)
        finishAffinity()
    }

    private fun addBottomDots(currentPage: Int) {
        binding.linearDots.removeAllViews()
        dots = arrayOfNulls(countPageIntro)
        for (i in 0 until countPageIntro) {
            dots[i] = ImageView(this)
            if (i == currentPage)
                dots[i]!!.setImageResource(R.drawable.ic_intro_selected)
            else
                dots[i]!!.setImageResource(R.drawable.ic_intro_not_select)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.linearDots.addView(dots[i], params)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finishAffinity()
    }

}