package com.beautycam.hdcam.photoeditor.dialog

import android.app.Activity
import android.view.LayoutInflater
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseDialog
import com.beautycam.hdcam.photoeditor.databinding.DialogAlertBinding
import com.beautycam.hdcam.photoeditor.widget.tap


class AlertDialog(
    activity1: Activity,
    val title: String? = null,
    val content: String? = null,
    val iconBack: Int? = null,
//    private var action: () -> Unit,
) : BaseDialog<DialogAlertBinding>(activity1, true) {


    override fun getContentView(): DialogAlertBinding {
        return DialogAlertBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {
        binding.tvTitle.text = title ?: ""
        binding.tvContent.text = content ?: ""
        binding.imgClose.setImageResource(iconBack ?: R.drawable.ic_back)
    }

    override fun bindView() {
        binding.apply {
            imgClose.tap {
//                action.invoke()
                dismiss()
            }

        }
    }
}