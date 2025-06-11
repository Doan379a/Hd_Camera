package com.beautycam.hdcam.photoeditor.ui.vault

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.data.DSDatabase
import com.beautycam.hdcam.photoeditor.data.entity.MediaType
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModel
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModelFactory
import com.beautycam.hdcam.photoeditor.data.viewmodel.PictureViewModel
import com.beautycam.hdcam.photoeditor.databinding.ActivityVaultBinding
import com.beautycam.hdcam.photoeditor.ui.vault.video.VideoVaultFragment
import com.beautycam.hdcam.photoeditor.ui.vault.image.PictureVaultFragment
import com.beautycam.hdcam.photoeditor.widget.tap
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.ui.vault.all.AllVaultFragment
import com.beautycam.hdcam.photoeditor.ui.vault.all.pager.MixedVaultPagerFragment
import com.beautycam.hdcam.photoeditor.ui.vault.image.pager.ImageVaultPagerFragment
import com.beautycam.hdcam.photoeditor.ui.vault.video.pager.VideoPagerVaultFragment
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.buildDeleteIntentSender
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.copyFileToPrivateStorage
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.copyFileToPrivateStorageSuspendVideo
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.getFileFromUri
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.util.ToastUtil.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class MediaProcessType { IMAGE, VIDEO }
data class PendingDelete(val file: File, val type: MediaProcessType,val originUri: Uri)

class VaultActivity : BaseActivity<ActivityVaultBinding>() {

    private lateinit var mDataTabList: List<String>
    private lateinit var mediaViewModel: MediaViewModel
    private val pictureViewModel: PictureViewModel by viewModels()

    private lateinit var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val pendingDeleteList = mutableListOf<PendingDelete>()

    private var myPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("KKK", "onPageSelected: $position")
                checkTab(position)
            }
        }


    override fun setViewBinding(): ActivityVaultBinding {
        return ActivityVaultBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val repository = MediaRepository(DSDatabase.getDatabase(this).mediaDao())
        mediaViewModel =
            ViewModelProvider(this, MediaViewModelFactory(repository))[MediaViewModel::class.java]
        mediaViewModel.fetchLockedMedia()
        deleteLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (pendingDeleteList.isNotEmpty()) {
                        val item = pendingDeleteList.removeAt(0)
                        val fileGoc = getFileFromUri(this,item.originUri) // bạn tự viết hàm này
                        if (fileGoc == null || !fileGoc.exists()) {
                            // File gốc đã xóa thành công, giờ mới insert DB
                            val entity = MediaEntity(
                                filePath = item.file.absolutePath,
                                mediaType = if (item.type == MediaProcessType.IMAGE) MediaType.IMAGE else MediaType.VIDEO
                            )
                            mediaViewModel.insertMedia(entity)
                            showToast(getString(R.string.photos_and_videos_hidden_successfully))
                        }
                    } else {
                        showToast("Lỗi: Không tìm thấy file pendingDeleteList đã copy")
                    }
                } else {
                    showToast("RESULT_OK")
                }
            }


        mDataTabList =
            listOf(getString(R.string.all), getString(R.string.picture), getString(R.string.video))
        binding.viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = mDataTabList.size
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> AllVaultFragment()
                    1 -> PictureVaultFragment()
                    2 -> VideoVaultFragment()
                    else -> AllVaultFragment()
                }
            }
        }
        binding.tabTitleAll.text = mDataTabList[0]
        binding.tabTitle1.text = mDataTabList[1]
        binding.tabTitle2.text = mDataTabList[2]
        binding.viewPager2.registerOnPageChangeCallback(myPageChangeCallback)


        mediaViewModel.lockedMedia.observe(this) { mediaList ->
            Log.d("DOO", "lockedMedia: $mediaList")
            val pictureMedia =
                mediaList.filter { it.mediaType == MediaType.IMAGE }.map { it.filePath }
            val videoMedia =
                mediaList.filter { it.mediaType == MediaType.VIDEO }.map { it.filePath }
            pictureViewModel.setListPicture(pictureMedia)
            pictureViewModel.setListVideo(videoMedia)
        }
        pictureViewModel.clickItemImage.observe(this) {
            if (it.first != null && it.second != null) {
                openMediaPagerFragment(it.first, it.second, false)
            }
        }

        pictureViewModel.clickItemVideo.observe(this) {
            if (it.first != null && it.second != null) {
                openMediaPagerFragment(it.first, it.second, true)
            }
        }
        pictureViewModel.clickItemALL.observe(this) {
            if (it.first != null && it.second != null) {
                Log.d("DOAN_3", "clickItemALL triggered: ${it.second}")
                openMixedPager(it.first, it.second)
            }
        }
        mediaViewModel.shouldReloadMedia.observe(this) { shouldReload ->
            if (shouldReload == true) {
                Log.d("VaultActivity", "LiveData: shouldReloadMedia = true")
                mediaViewModel.fetchLockedMedia()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun viewListener() {
        binding.imgBack.tap {
            finish()
        }
        binding.tabTitleAll.tap {
            checkTab(0)
            binding.viewPager2.currentItem = 0
        }
        binding.tabTitle1.tap {
            checkTab(1)
            binding.viewPager2.currentItem = 1
        }
        binding.tabTitle2.tap {
            checkTab(2)
            binding.viewPager2.currentItem = 2
        }
        binding.tvImport.tap {

            importImageVideo()
        }
    }

    override fun dataObservable() {
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun importImageVideo() {
        TedImagePicker.with(this)
            .imageAndVideo()
            .cancelListener {
                Log.d("TedImagePicker", "Người dùng đã hủy chọn ảnh")
            }
            .errorListener {
                Log.d("TedImagePicker", "Lỗi khi chọn ảnh!")
            }
            .startMultiImage { uriList ->
                val imageUris = mutableListOf<Uri>()
                val videoUris = mutableListOf<Uri>()

                uriList.forEach { uri ->
                    val mimeType = contentResolver.getType(uri) ?: ""
                    if (mimeType.startsWith("image")) {
                        imageUris.add(uri)
                    } else if (mimeType.startsWith("video")) {
                        videoUris.add(uri)
                    }
                }

                val uniqueImageUris = imageUris.distinct()
                val uniqueVideoUris = videoUris.distinct()

                importImage(uniqueImageUris.toMutableList())
                importVideo(uniqueVideoUris.toMutableList())
                Log.d("Doan2", "Ảnh: $imageUris")
                Log.d("Doan2", "Video: $videoUris")
            }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun importImage(uri: MutableList<Uri>? = mutableListOf()) {
        uri?.forEachIndexed { index, uri ->
            copyFileToPrivateStorage(this, uri, index) { newFile ->
                if (newFile != null) {
                    buildDeleteIntentSender(this, uri) { request, deletedImmediately ->
                        Log.d("TAO_NE2", "777 $request")
                        if (deletedImmediately) {
                            // ĐÃ XÓA LUÔN, không cần chờ callback launcher
                            val entity = MediaEntity(
                                filePath = newFile.absolutePath,
                                mediaType = MediaType.IMAGE
                            )
                            mediaViewModel.insertMedia(entity)
                            showToast(getString(R.string.photos_and_videos_hidden_successfully))
                        } else if (request != null) {
                            // Cần xác nhận từ user (Android 11+ hoặc bị recoverable trên Android 10)
                            pendingDeleteList.add(PendingDelete(newFile, MediaProcessType.IMAGE,uri))
                            deleteLauncher.launch(request)
                        } else {
//                            showToast("Không thể tạo yêu cầu xoá")
                        }
                    }
                } else {
//                    showToast("Không thể sao chép file")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun importVideo(uriList: MutableList<Uri>? = mutableListOf()) {
        uriList?.forEachIndexed { index, uri ->
            lifecycleScope.launch {
                val newFile = copyFileToPrivateStorageSuspendVideo(this@VaultActivity, uri, index)
                withContext(Dispatchers.Main) {
                    if (newFile != null) {
                        buildDeleteIntentSender(
                            this@VaultActivity,
                            uri
                        ) { request, deletedImmediately ->
                            if (deletedImmediately) {
                                val entity = MediaEntity(
                                    filePath = newFile.absolutePath,
                                    mediaType = MediaType.VIDEO
                                )
                                mediaViewModel.insertMedia(entity)
                                showToast(getString(R.string.photos_and_videos_hidden_successfully))
                            } else if (request != null) {
                                pendingDeleteList.add(
                                    PendingDelete(
                                        newFile,
                                        MediaProcessType.VIDEO,uri
                                    )
                                )
                                deleteLauncher.launch(request)
                            } else {
//                                showToast("Không thể tạo yêu cầu xoá")
                            }
                        }
                    } else {
//                        showToast("Không thể sao chép file")
                    }
                }
            }
        }
    }

    private fun openMixedPager(list: List<MediaEntity>, index: Int) {
        val fragment = MixedVaultPagerFragment(list.toMutableList(), index)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openMediaPagerFragment(mediaList: List<String>, startIndex: Int, isVideo: Boolean) {
        val fragment = if (isVideo) {
            VideoPagerVaultFragment(mediaList.toMutableList(), startIndex)
        } else {
            ImageVaultPagerFragment(mediaList.toMutableList(), startIndex)
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun checkTab(position: Int) {
        when (position) {
            0 -> {
                binding.tabTitleAll.setTextColor(Color.parseColor("#FFFFFF"))
                binding.tabTitle1.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle2.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_photo,
                    0,
                    0,
                    0
                )
                binding.tabTitle2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_video2,
                    0,
                    0,
                    0
                )
                binding.tabTitleAll.setBackgroundResource(R.drawable.bg_select_tab)
                binding.tabTitle1.setBackgroundResource(R.drawable.bg_tab)
                binding.tabTitle2.setBackgroundResource(R.drawable.bg_tab)
            }

            1 -> {
                binding.tabTitle1.setTextColor(Color.parseColor("#FFFFFF"))
                binding.tabTitle2.setTextColor(Color.parseColor("#666666"))
                binding.tabTitleAll.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_selected_photo,
                    0,
                    0,
                    0
                )
                binding.tabTitle2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_video2,
                    0,
                    0,
                    0
                )
                binding.tabTitle1.setBackgroundResource(R.drawable.bg_select_tab)
                binding.tabTitle2.setBackgroundResource(R.drawable.bg_tab)
                binding.tabTitleAll.setBackgroundResource(R.drawable.bg_tab)
            }

            2 -> {
                binding.tabTitleAll.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle1.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle2.setTextColor(Color.parseColor("#FFFFFF"))
                binding.tabTitle1.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_photo,
                    0,
                    0,
                    0
                )
                binding.tabTitle2.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_selected_video2,
                    0,
                    0,
                    0
                )
                binding.tabTitle1.setBackgroundResource(R.drawable.bg_tab)
                binding.tabTitleAll.setBackgroundResource(R.drawable.bg_tab)
                binding.tabTitle2.setBackgroundResource(R.drawable.bg_select_tab)
            }
        }
    }
}