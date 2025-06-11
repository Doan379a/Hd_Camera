package com.beautycam.hdcam.photoeditor.ui.pass

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.databinding.ActivitySecurityQuestionBinding
import com.beautycam.hdcam.photoeditor.databinding.ItemSpinnerBinding
import com.beautycam.hdcam.photoeditor.databinding.RowStickerBinding
import com.beautycam.hdcam.photoeditor.model.QuestionModel
import com.beautycam.hdcam.photoeditor.model.getListOfQuestions
import com.beautycam.hdcam.photoeditor.sharePreferent.PreferenceManager
import com.beautycam.hdcam.photoeditor.ui.vault.VaultActivity
import com.beautycam.hdcam.photoeditor.utils.getBitmapFromAsset
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.visible

class SecurityQuestionActivity : BaseActivity<ActivitySecurityQuestionBinding>() {

    private lateinit var pref: PreferenceManager
    private var incomingPasscode: String? = null
    private var isResetPass: Boolean = false
    private var isChangePass: Boolean = false
    private lateinit var adapter: SpinnerAdapter
    private var selected: Int? = 1
    private var isDropdownVisible = false
    override fun setViewBinding(): ActivitySecurityQuestionBinding {
        return ActivitySecurityQuestionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        isResetPass = intent.getBooleanExtra("resetPass", false)
        incomingPasscode = intent.extras?.getString("passCode")
        isChangePass = intent.getBooleanExtra("changePass", false)

        Log.d("isChangePass>>", "isChangePass = $isChangePass")
        Log.d("SECQ>>", "incomingPasscode = $incomingPasscode")
        Log.d("ResetPass>>", "isResetPass = $isResetPass")
        pref = PreferenceManager(this)

        binding.rcvQuestion.layoutManager = LinearLayoutManager(this)
        adapter = SpinnerAdapter(getListOfQuestions(this), 1) {
            selected = it.id
            Log.d("selected", "$selected")
            binding.spnQuestion.text = it.question
            setSpinner(false)
        }
        binding.rcvQuestion.adapter = adapter
    }

    override fun viewListener() {
        binding.spnQuestion.setOnClickListener {
            isDropdownVisible = !isDropdownVisible
            setSpinner(isDropdownVisible)
        }
        binding.edtResult.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.tvCounter.text = "$length/50"

                // Cắt nếu người dùng dán quá 50 ký tự
                if (length > 50) {
                    binding.edtResult.setText(s?.substring(0, 50))
                    binding.edtResult.setSelection(50) // Đặt con trỏ cuối chuỗi
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnOk.tap {
            if (isResetPass) {
                resetPass()
            } else {
                savePinAndQuestion()
            }
        }
        binding.btnClose.tap { finish() }
        binding.root.setOnClickListener {
            setSpinner(false)
        }
    }

    private fun setSpinner(isDropdownVisible: Boolean) {
        selected?.let { adapter.updateID(it) }
        if (isDropdownVisible) {
            binding.cardRcv.visible()
        } else {
            binding.cardRcv.gone()
        }

    }

    private fun savePinAndQuestion() {

        val answer = binding.edtResult.text.toString()
        Log.d("selected", selected.toString())
        Log.d("answer", answer)
        Log.d("passcode", incomingPasscode.toString())
        if (incomingPasscode != null && selected != null && answer.isNotBlank()) {
            pref.savePassCode(incomingPasscode!!)
            selected?.let { pref.saveSecurityQuestion(it) }
            pref.saveSecurityResult(answer)
            if (isChangePass) finish()
            else startActivity(Intent(this, VaultActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, R.string.please_enter_the_answer, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetPass() {

        val oldQuestion = pref.getSecurityQuestion()
        val oldAnswer = pref.getSecurityResult()
        Log.d("oldQuestion", oldQuestion.toString())
        Log.d("oldAnswer", oldAnswer.toString())
        val answer = binding.edtResult.text.toString()
        Log.d("answer", answer)
        Log.d("selected", selected.toString())

        if (oldQuestion == selected && oldAnswer.equals(answer)) {
            val intent = Intent(this, PassCodeActivity::class.java)
            intent.putExtra("changePass", isChangePass)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, R.string.errorr_the_answer, Toast.LENGTH_SHORT).show()
        }

    }

    override fun dataObservable() {
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { view ->
                if (view is EditText) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        // Ẩn bàn phím
                        view.clearFocus()
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    inner class SpinnerAdapter(
        val list: List<QuestionModel>,
        private var selectedId: Int?,
        private val onItemClick: (QuestionModel) -> Unit
    ) : RecyclerView.Adapter<SpinnerAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSpinnerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sticker = list[position]
            holder.bind(sticker, sticker.id == selectedId)
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(val binding: ItemSpinnerBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(sticker: QuestionModel, isCheck: Boolean) {
                binding.tvSpinnerItem.text = sticker.question
                binding.tvSpinnerItem.setTextColor(
                    if (isCheck) {
                        Color.parseColor("#FFFFFF")
                    } else Color.parseColor("#000000")
                )
                binding.root.setBackgroundColor(
                    if (isCheck) {
                        Color.parseColor("#FF8594")
                    } else 0
                )
                binding.root.setOnClickListener {
                    onItemClick.invoke(sticker)
                }
            }
        }

        fun updateID(int: Int) {
            selectedId = int
            notifyDataSetChanged()
        }
    }
}
