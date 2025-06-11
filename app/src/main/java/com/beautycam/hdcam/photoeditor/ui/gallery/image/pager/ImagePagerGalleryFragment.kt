package com.beautycam.hdcam.photoeditor.ui.gallery.image.pager

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.data.DSDatabase
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.entity.MediaType
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModel
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModelFactory
import com.beautycam.hdcam.photoeditor.databinding.FragmentImagePagerBinding
import com.beautycam.hdcam.photoeditor.dialog.DeleteDialog
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.buildDeleteIntentSender
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.copyAnyFileToPrivate
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.copyFileToPrivateStorage
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.deleteMediaFileFromMediaStore
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareImage
import com.beautycam.hdcam.photoeditor.widget.tap
import gun0912.tedimagepicker.util.ToastUtil.showToast
import kotlinx.coroutines.launch
import java.io.File

class ImagePagerGalleryFragment(
    private val imageList: MutableList<String>,
    private val startIndex: Int
) : Fragment() {

    private var filePath: String? = null
    private var nameFile: String? = null
    private var _binding: FragmentImagePagerBinding? = null
    private lateinit var mediaViewModel: MediaViewModel
    private val binding get() = _binding!!
    private lateinit var adapter: ImagePagerGalleryAdapter
    private lateinit var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var copiedFile: File? = null
    private var checkRemove: Boolean = false

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(getTagDebug(), "onPageSelected() - Position: $position")
            filePath = imageList[position]
            nameFile = File(filePath).name
            binding.tvTitle.text = nameFile
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deleteLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (checkRemove) {
                        showToast(getString(R.string.delete_success))
                        removeCurrentImage()
                    } else {
                        val entity = MediaEntity(
                            filePath = copiedFile!!.absolutePath,
                            mediaType = MediaType.IMAGE
                        )
                        mediaViewModel.insertMedia(entity)
                        showToast(getString(R.string.restored_to_library))
                        removeCurrentImage()
                        copiedFile = null
                    }

                } else {
                    copiedFile?.delete()
                    copiedFile = null
//                    showToast("Bạn đã huỷ thao tác xoá")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        val factory = MediaViewModelFactory(repository)
        mediaViewModel = ViewModelProvider(this, factory)[MediaViewModel::class.java]
        adapter = ImagePagerGalleryAdapter(imageList)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.setCurrentItem(startIndex, false)
        binding.viewPager2.registerOnPageChangeCallback(pageChangeCallback)
        Log.d("LOCK_FLOW", "$imageList")
        filePath = imageList[startIndex]
        nameFile = File(filePath).name

        binding.tvTitle.text = nameFile
        binding.tvLock.tap {
            checkRemove = false
            filePath?.let { path ->
                val originalFile = File(path)
                copyAnyFileToPrivate(requireActivity(), path) { newFile, checkAndroi10 ->
                    if (newFile != null) {
                        buildDeleteIntentSender(
                            requireActivity(),
                            originalFile
                        ) { request, deletedImmediately ->
                            if (deletedImmediately) {
                                Log.d("LOCK_FLOW", "1. Insert media")
                                val entity = MediaEntity(
                                    filePath = newFile.absolutePath,
                                    mediaType = MediaType.IMAGE
                                )
                                lifecycleScope.launch {
                                    mediaViewModel.insertMedia(entity)
                                    showToast(getString(R.string.restored_to_library))
                                    removeCurrentImage()
                                }
                            } else if (request != null) {
                                copiedFile = newFile
                                deleteLauncher.launch(request)
                            } else {
//                                showToast("Không thể tạo yêu cầu xoá")
                                showToast(getString(R.string.failed))
                            }
                        }
                    } else {
//                        showToast("Không thể sao chép file")
                        showToast(getString(R.string.failed))
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    back()
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            })

        binding.tvDelete.tap {
            filePath?.let {
                    checkRemove = true
                    val path = File(it)
                    buildDeleteIntentSender(
                        requireActivity(),
                        path
                    ) { request,deletedImmediately ->
                        if (deletedImmediately) {
                            lifecycleScope.launch {
                                showToast(getString(R.string.delete_success))
                                removeCurrentImage()
                            }
                        } else if (request != null) {
                            deleteLauncher.launch(request)
                        } else {
//                            showToast("Không thể tạo yêu cầu xoá")
                            Log.d("LOCK_FLOW", "Không thể tạo yêu cầu xoá")
                        }
                    }
            }
        }
        binding.imgBack.tap {
            back()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.tvShare.tap {
            filePath?.let {
                shareImage(requireActivity(), it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun removeCurrentImage() {
        Log.d("LOCK_FLOW", "4. Đã vào removeCurrentImage")
        val currentIndex = binding.viewPager2.currentItem

        if (imageList.isEmpty() || currentIndex !in imageList.indices) {
            Log.d("LOCK_FLOW", "5. List rỗng hoặc index lỗi: $currentIndex/${imageList.size}")
            return
        }

        val nextIndex = if (currentIndex == imageList.lastIndex) {
            currentIndex - 1
        } else {
            currentIndex + 1
        }

        imageList.removeAt(currentIndex)
        adapter.notifyDataSetChanged()
        if (imageList.isEmpty()) {
            Log.d("LOCK_FLOW", "6. List rỗng sau remove, sẽ back")
            back()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }
        val safeIndex = nextIndex.coerceIn(imageList.indices)
        binding.viewPager2.currentItem = safeIndex
        binding.viewPager2.post {
            filePath = imageList[safeIndex]
            nameFile = File(filePath).name
            binding.tvTitle.text = nameFile
            Log.d("LOCK_FLOW", "7. Đã update UI title: $nameFile")
        }
    }


    private fun back() {
        val result = Bundle().apply {
            putStringArrayList("listImage", ArrayList(imageList))
        }
        parentFragmentManager.setFragmentResult("picture_reload_request", result)
    }

}
