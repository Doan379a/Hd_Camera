package com.beautycam.hdcam.photoeditor.dialog

import android.app.Activity
import android.view.LayoutInflater
import com.beautycam.hdcam.photoeditor.base.BaseDialog
import com.beautycam.hdcam.photoeditor.databinding.DialogPermissionBinding
import com.beautycam.hdcam.photoeditor.widget.tap

class PermissionDialog(
    activity1: Activity,
    private var action: () -> Unit
) : BaseDialog<DialogPermissionBinding>(activity1, true) {


    override fun getContentView(): DialogPermissionBinding {
        return DialogPermissionBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {
    }

    override fun bindView() {
        binding.apply {
            txtGo.tap {
                action.invoke()
                dismiss()
            }
        }
    }
}