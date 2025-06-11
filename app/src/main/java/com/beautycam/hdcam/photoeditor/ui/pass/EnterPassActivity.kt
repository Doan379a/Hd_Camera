package com.beautycam.hdcam.photoeditor.ui.pass

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivityEnterPassBinding
import com.beautycam.hdcam.photoeditor.sharePreferent.PreferenceManager
import com.beautycam.hdcam.photoeditor.ui.vault.VaultActivity
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.tap2
import com.beautycam.hdcam.photoeditor.widget.visible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EnterPassActivity : BaseActivity<ActivityEnterPassBinding>() {
    private var currentPos = 1
    private lateinit var pref: PreferenceManager
    private var enterPassCode = ""
    private var checkPass = 0

    private var isChangePass: Boolean = false

    override fun setViewBinding(): ActivityEnterPassBinding {
        return ActivityEnterPassBinding.inflate(layoutInflater)
    }

    override fun initView() {
        pref = PreferenceManager(this)
        setupKeyboardListeners()

        isChangePass = intent.getBooleanExtra("changePass", false)
        Log.d("isChangePass>>", "isChangePass = $isChangePass")
    }

    override fun viewListener() {
        binding.btnClose.tap { finish() }
        binding.btnReset.tap {
            val intent = Intent(this, SecurityQuestionActivity::class.java)
            intent.putExtra("resetPass", true)
            intent.putExtra("changePass", isChangePass)
            startActivity(intent)
            finish()
        }
    }

    override fun dataObservable() {
    }

    private fun setupKeyboardListeners() {
        val pins = listOf(
            binding.tvNumber1,
            binding.tvNumber2,
            binding.tvNumber3,
            binding.tvNumber4
        )

        val numberButtons = listOf(
            binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6,
            binding.btn7, binding.btn8, binding.btn9,
            binding.btn0
        )

        numberButtons.forEach { btn ->
            btn.tap2 {
                if (currentPos <= 4) {
                    pins[currentPos - 1].text = btn.text
                    currentPos++
                    updatePinStyles(pins)

                    if (currentPos > 4) {
                        enterPassCode = pins.joinToString("") { it.text.toString() }
                        val passCode = pref.getPassCode()
                        if (enterPassCode.equals(passCode)){
                            if (isChangePass){
                                val intent = Intent(this, PassCodeActivity::class.java)
                                intent.putExtra("changePass", isChangePass)
                                startActivity(intent)
                            }else{
                                showActivity(VaultActivity::class.java)
                            }
                            finish()
                        }else{

                            Toast.makeText(this,R.string.errorr_pass,Toast.LENGTH_SHORT).show()
                            checkPass++
                            if (checkPass == 3){
                                binding.btnReset.visible()
                            }
                            lifecycleScope.launch {
                                delay(500)
                                clearPassCode()
                            }
                        }
                    }
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            if (currentPos > 1) {
                currentPos--
                pins[currentPos - 1].text = ""
                updatePinStyles(pins)
            }
        }

        // lần đầu khởi tạo
        updatePinStyles(pins)
    }

    private fun clearPassCode(){

        val pins = listOf(
            binding.tvNumber1,
            binding.tvNumber2,
            binding.tvNumber3,
            binding.tvNumber4
        )
        binding.apply {
            tvNumber1.text = ""
            tvNumber2.text = ""
            tvNumber3.text = ""
            tvNumber4.text = ""
        }
        currentPos = 1
        updatePinStyles(pins)
    }


    private fun updatePinStyles(pins: List<TextView>) {
        pins.forEachIndexed { idx, tv ->
            val pos = idx + 1
            when {
                pos < currentPos -> setBackgroundPin(tv, State.OK)
//                pos == currentPos -> setBackgroundPin(tv, State.CURRENT)
                else -> setBackgroundPin(tv, State.EMPTY)
            }
        }
    }

    private fun setBackgroundPin(tv: TextView, state: State) {
        // cập nhật background
        val bgRes = when (state) {
            State.OK -> R.drawable.bg_pin_ok
//            State.CURRENT -> R.drawable.bg_pin_ok
            else -> R.drawable.bg_pin_non
        }
        tv.setBackgroundResource(bgRes)

//        // cập nhật kích thước
//        val sizeDp = when (state) {
//            State.CURRENT -> 18
//            else -> 12
//        }
//        val lp = tv.layoutParams as ViewGroup.MarginLayoutParams
//        lp.width = dpToPx(sizeDp)
//        lp.height = dpToPx(sizeDp)
//        tv.layoutParams = lp
    }

    private fun Context.dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private enum class State { OK, EMPTY }
}