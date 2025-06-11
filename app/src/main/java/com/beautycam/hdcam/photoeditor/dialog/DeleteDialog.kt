package com.beautycam.hdcam.photoeditor.dialog

import android.app.Activity
import android.view.LayoutInflater
import com.beautycam.hdcam.photoeditor.base.BaseDialog
import com.beautycam.hdcam.photoeditor.databinding.DialogDeleteBinding
import com.beautycam.hdcam.photoeditor.widget.tap

class DeleteDialog (
    activity1: Activity,
    val content: String? = null,
    private var action: () -> Unit,
) : BaseDialog<DialogDeleteBinding>(activity1, true) {


    override fun getContentView(): DialogDeleteBinding {
        return DialogDeleteBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {
    }

    override fun bindView() {
        binding.root.tap { dismiss() }
        binding.apply {
            imgClose.tap {
//                action.invoke()
                dismiss()
            }
            tvNo.tap {
                dismiss()
            }

            tvYes.tap {
                action.invoke()
                dismiss()
            }
        }
    }
}