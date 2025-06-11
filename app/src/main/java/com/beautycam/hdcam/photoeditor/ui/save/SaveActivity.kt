package com.beautycam.hdcam.photoeditor.ui.save

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.base.extensions.showToast
import com.beautycam.hdcam.photoeditor.databinding.ActivitySaveBinding
import com.beautycam.hdcam.photoeditor.ui.collage.CollageLayoutsActivity
import com.beautycam.hdcam.photoeditor.ui.editor.EditImageActivity
import com.beautycam.hdcam.photoeditor.ui.main.MainActivity
import com.beautycam.hdcam.photoeditor.widget.tap
import gun0912.tedimagepicker.builder.TedImagePicker

class SaveActivity : BaseActivity<ActivitySaveBinding>() {
    private var activity: String? = null
    private var filePath: String? = null

    override fun setViewBinding(): ActivitySaveBinding {
        return ActivitySaveBinding.inflate(layoutInflater)
    }

    override fun initView() {
        filePath = intent.getStringExtra("linkPath") ?: ""
        activity = intent.getStringExtra("keyActivity") ?: ""
        Glide.with(this).load(filePath).into(binding.image)
    }

    override fun viewListener() {
        binding.tvEditOneMore.tap {
            when (activity) {
                "CollageLayoutsActivity" -> {
                    clickCollage()
                }
                "EditImageActivity" -> {
                    selectEditor()
                    finishAffinity()
                }
            }
        }

        binding.tvBackHome.tap {
            showActivity(MainActivity::class.java)
            finish()
        }

        binding.imgClose.tap {
            finish()
//            when (activity) {
//                "CollageLayoutsActivity" -> {
//                    showActivity(CollageLayoutsActivity::class.java)
//                }
//                "EditImageActivity" -> {
//                    showActivity(EditImageActivity::class.java)
//                }
//            }
        }
        binding.tvWhatsApp.tap {
            filePath?.let { it1 ->     shareImage(Uri.parse(it1), "whatsapp") }
        }
        binding.tvFacebook.tap {
            filePath?.let { it1 ->     shareImage(Uri.parse(it1), "facebook") }
        }
        binding.tvInstagram.tap {
            filePath?.let { it1 ->     shareImage(Uri.parse(it1), "instagram") }
        }
    }

    override fun dataObservable() {
    }

    private fun clickCollage() {
        TedImagePicker.with(this)
            .min(2, getString(R.string.img_min_9))
            .max(9, getString(R.string.img_max_9))
            .errorListener {
                Log.d("TedImagePicker", "Lỗi khi chọn ảnh!")
            }
            .cancelListener {
                Log.d("TedImagePicker", "Người dùng đã hủy chọn ảnh")
                showActivity(MainActivity::class.java)
                finish()
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
                finishAffinity()
            }
    }

    private fun selectEditor() {
        TedImagePicker.with(this)
            .cancelListener {
                showActivity(MainActivity::class.java)
                finish()
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
                finishAffinity()
            }

    }
    private fun shareImage(imageUri: Uri, target: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        when (target) {
            "facebook" -> {
                if (isAppInstalled("com.facebook.katana")) {
                    shareIntent.setPackage("com.facebook.katana")
                } else {
                    openPlayStore("com.facebook.katana")
                    return
                }
            }

            "instagram" -> {
                if (isAppInstalled("com.instagram.android")) {
                    shareIntent.setPackage("com.instagram.android")
                } else {
                    openPlayStore("com.instagram.android")
                    return
                }
            }

            "whatsapp" -> {
                if (isAppInstalled("com.whatsapp")) {
                    shareIntent.setPackage("com.whatsapp")
                } else {
                    openPlayStore("com.whatsapp")
                    return
                }
            }

            "messenger" -> {
                if (isAppInstalled("com.facebook.orca")) {
                    shareIntent.setPackage("com.facebook.orca")
                } else {
                    openPlayStore("com.facebook.orca")
                    return
                }
            }

            "email" -> {
                shareIntent.setType("message/rfc822")
            }

            "share_more" -> {
            }
        }


        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
        } else {
            Log.e("ShareActivity", "No app available for $target")
//            Toast.makeText(this, "R.string.app_not_installed", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    private fun openPlayStore(packageName: String) {
        val playStoreIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(playStoreIntent)
    }
}