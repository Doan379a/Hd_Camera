package  com.beautycam.hdcam.photoeditor.ui.permission

import android.os.Bundle
import android.view.View
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivityPermissionBinding
import com.beautycam.hdcam.photoeditor.sharePreferent.SharePrefUtils
import com.beautycam.hdcam.photoeditor.ui.main.MainActivity
import com.beautycam.hdcam.photoeditor.utils.Default.CAMERA_PERMISSION
import com.beautycam.hdcam.photoeditor.utils.Default.STORAGE_PERMISSION
import com.beautycam.hdcam.photoeditor.utils.Default.VIDEO_RECORD_PERMISSIONS
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.visible


class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {


    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        if (checkPermission(STORAGE_PERMISSION + VIDEO_RECORD_PERMISSIONS + CAMERA_PERMISSION)) {
            allowCameraPermission()
        }

    }

    override fun viewListener() {
        binding.apply {
            ivSetCameraPermission.tap {
                showDialogPermission(STORAGE_PERMISSION + VIDEO_RECORD_PERMISSIONS + CAMERA_PERMISSION)
            }
            tvContinue.tap {
                SharePrefUtils.forceGoToMain(this@PermissionActivity)
                showActivity(MainActivity::class.java)
                finishAffinity()
            }
        }

    }

    override fun dataObservable() {
    }

    override fun onPermissionGranted() {
        if (checkPermission(STORAGE_PERMISSION + VIDEO_RECORD_PERMISSIONS + CAMERA_PERMISSION)) {
            allowCameraPermission()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        if (checkPermission(STORAGE_PERMISSION + VIDEO_RECORD_PERMISSIONS + CAMERA_PERMISSION)) {
            allowCameraPermission()
        }
        super.onResume()
    }

    private fun allowCameraPermission() {
        binding.ivSetCameraPermission.gone()
        binding.ivSelectCameraPermission.visible()
    }

}