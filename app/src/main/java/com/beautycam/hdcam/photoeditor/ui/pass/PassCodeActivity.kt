package com.beautycam.hdcam.photoeditor.ui.pass

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivityPassCodeBinding
import com.beautycam.hdcam.photoeditor.sharePreferent.PreferenceManager
import com.beautycam.hdcam.photoeditor.ui.main.MainActivity
import com.beautycam.hdcam.photoeditor.ui.vault.VaultActivity
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.tap2

class PassCodeActivity : BaseActivity<ActivityPassCodeBinding>() {
    private var currentPos = 1
    private lateinit var pref: PreferenceManager
    private var createPassCode = ""
    private var confirmPassCode = ""
    private var isChangePass: Boolean = false

    override fun setViewBinding() = ActivityPassCodeBinding.inflate(layoutInflater)

    override fun initView() {
        pref = PreferenceManager(this)
        setupKeyboardListeners()

        isChangePass = intent.getBooleanExtra("changePass", false)
        Log.d("isChangePass>>", "isChangePass = $isChangePass")
    }

    override fun viewListener() {
        binding.btnClose.tap { showActivity(MainActivity::class.java) }
    }

    override fun dataObservable() = Unit

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
                        if (createPassCode.equals("") || createPassCode == ""){
                            createPassCode = pins.joinToString("") { it.text.toString() }
                            setConfirmPassCode()
                        }else{
                            confirmPassCode = pins.joinToString("") { it.text.toString() }
                            if (createPassCode.equals(confirmPassCode)){

                                val intent = Intent(this, SecurityQuestionActivity::class.java)
                                intent.putExtra("passCode", confirmPassCode)
                                if (isChangePass) intent.putExtra("changePass", isChangePass)
                                startActivity(intent)

                                finish()
                            }else{
                                Toast.makeText(this,R.string.errorr_pass,Toast.LENGTH_SHORT).show()
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

    private fun setConfirmPassCode(){

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
            tvCreate.setText(getText(R.string.confirm_pass))
            tvStep2.setBackgroundResource(R.drawable.bg_pin_ok)
            tvStep2.setTextColor(Color.WHITE)
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
