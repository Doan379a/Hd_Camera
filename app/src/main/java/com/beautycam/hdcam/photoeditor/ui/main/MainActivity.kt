package com.beautycam.hdcam.photoeditor.ui.main

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.databinding.ActivityMainBinding
import com.beautycam.hdcam.photoeditor.sharePreferent.PreferenceManager
import com.beautycam.hdcam.photoeditor.ui.camera.CameraActivity
import com.beautycam.hdcam.photoeditor.ui.collage.CollageLayoutsActivity
import com.beautycam.hdcam.photoeditor.ui.editor.EditImageActivity
import com.beautycam.hdcam.photoeditor.ui.gallery.GalleryActivity
import com.beautycam.hdcam.photoeditor.ui.pass.EnterPassActivity
import com.beautycam.hdcam.photoeditor.ui.pass.PassCodeActivity
import com.beautycam.hdcam.photoeditor.ui.setting.SettingActivity
import com.beautycam.hdcam.photoeditor.ui.vault.VaultActivity
import com.beautycam.hdcam.photoeditor.utils.Default.CAMERA_PERMISSION
import com.beautycam.hdcam.photoeditor.utils.Default.STORAGE_PERMISSION
import com.beautycam.hdcam.photoeditor.utils.Default.VIDEO_RECORD_PERMISSIONS
import com.beautycam.hdcam.photoeditor.widget.tap
import gun0912.tedimagepicker.builder.TedImagePicker
import java.io.File

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var pref: PreferenceManager
    private var passCode = ""

    override fun setViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun viewListener() {
        binding.imgCamera.setOnClickListener {
            if (checkPermission(CAMERA_PERMISSION+VIDEO_RECORD_PERMISSIONS)){
                showActivity( CameraActivity::class.java)
            }else{
                showDialogPermission(CAMERA_PERMISSION+VIDEO_RECORD_PERMISSIONS)
            }
        }
        binding.layoutCollage.tap {
            if (checkPermission(STORAGE_PERMISSION+CAMERA_PERMISSION)) {
                clickCollage()
            }else{
                showDialogPermission(STORAGE_PERMISSION+CAMERA_PERMISSION)
            }
        }
        binding.imgSetting.tap {
            showActivity(SettingActivity::class.java)
        }
        binding.layoutEditor.tap {
            if (checkPermission(STORAGE_PERMISSION+CAMERA_PERMISSION)){
                selectEditor()
            }else{
                showDialogPermission(STORAGE_PERMISSION+CAMERA_PERMISSION)
            }
        }
        binding.layoutGallery.tap{
            if (checkPermission(STORAGE_PERMISSION)) {
                showActivity(GalleryActivity::class.java)
            }else{
                showDialogPermission(STORAGE_PERMISSION)
            }
        }
        binding.layoutVault.tap {
            if (checkPermission(STORAGE_PERMISSION)) {
                pref = PreferenceManager(this)
                passCode = pref.getPassCode()!!
                if (passCode.equals("") || passCode == ""){
                    showActivity(PassCodeActivity::class.java)
                }else{
                    showActivity(EnterPassActivity::class.java)
                }
            }else{
                showDialogPermission(STORAGE_PERMISSION)
            }
        }



    }

    override fun dataObservable() {

    }

    private fun clickCollage() {
        TedImagePicker.with(this)
            .min(2,  getString(R.string.img_min_9))
            .max(9, getString(R.string.img_max_9))
            .errorListener {
                Log.d("TedImagePicker", "Lỗi khi chọn ảnh!")
            }
            .cancelListener {
                Log.d("TedImagePicker", "Người dùng đã hủy chọn ảnh")
            }
            .startMultiImage { uris ->
                if (uris.isEmpty()) {
                    Log.d("TedImagePicker", "Không có ảnh nào được chọn")
                    return@startMultiImage
                }


                val intent = Intent(this, CollageLayoutsActivity::class.java).apply {
                    putParcelableArrayListExtra("LIST_IMAGE", ArrayList(uris))
                    putExtra("theme", 1)
                }
                startActivity(intent)
            }
    }
    private fun selectEditor() {
        TedImagePicker.with(this)
            .cancelListener {
                Log.d("TedImagePicker", "Người dùng đã hủy chọn ảnh")
            }
            .errorListener {
                Log.d("TedImagePicker", "Lỗi khi chọn ảnh!")
            }
            .start { uri ->
                val intent = Intent(this, EditImageActivity::class.java).apply {
                    putExtra(
                        "URI_IMAGE",
                        uri
                    )
                }

                startActivity(intent)
            }

    }
}
