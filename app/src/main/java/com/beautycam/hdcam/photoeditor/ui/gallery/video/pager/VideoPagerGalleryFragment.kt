package com.beautycam.hdcam.photoeditor.ui.gallery.video.pager

import android.app.Activity
import android.app.ProgressDialog
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
import com.beautycam.hdcam.photoeditor.databinding.FragmentVideoPagerBinding
import com.beautycam.hdcam.photoeditor.dialog.DeleteDialog
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.buildDeleteIntentSender
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareVideo
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.generateTimestampedFileName
import java.io.File
import com.beautycam.hdcam.photoeditor.widget.tap
import gun0912.tedimagepicker.util.ToastUtil.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoPagerGalleryFragment(
    private val videoList: MutableList<String>,
    private val startIndex: Int
) :
    Fragment() {

    private var _binding: FragmentVideoPagerBinding? = null
    private val binding get() = _binding!!
    private var filePath: String? = null
    private var nameFile: String? = null
    private lateinit var adapter: VideoPagerGalleryAdapter
    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var copiedFile: File? = null
    private lateinit var progressDialog: ProgressDialog
    private var checkRemove: Boolean = false
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(getTagDebug(), "onPageSelected() - Position: $position")
            filePath = videoList[position]
            nameFile = File(filePath).name
            binding.tvTitle.text = nameFile
            for (i in 0 until adapter.itemCount) {
                val fragment = adapter.getFragment(i)
                if (fragment != null && i != position) {
                    fragment.stopVideo()
                }
            }

            val currentFragment = adapter.getFragment(position)
            currentFragment?.startVideo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deleteLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (checkRemove) {
                        removeCurrentVideo {
                            showToast(getString(R.string.delete_success))
                            progressDialog.dismiss()
                        }
                    } else {
                        val entity = MediaEntity(
                            filePath = copiedFile!!.absolutePath,
                            mediaType = MediaType.VIDEO
                        )
                        mediaViewModel.insertMedia(entity)
                        showToast(getString(R.string.restored_to_library))
                        removeCurrentVideo {
                            progressDialog.dismiss()
                        }
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
        _binding = FragmentVideoPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        val factory = MediaViewModelFactory(repository)
        mediaViewModel = ViewModelProvider(this, factory)[MediaViewModel::class.java]
        adapter = VideoPagerGalleryAdapter(requireActivity(), videoList)
        binding.viewPager2.adapter = adapter

        Log.d(getTagDebug(), "onViewCreated() - Start Index: $startIndex")
        binding.viewPager2.registerOnPageChangeCallback(pageChangeCallback)
        binding.viewPager2.post {
            Log.d("VideoPagerFragment", "Setting currentItem to $startIndex")
            binding.viewPager2.setCurrentItem(startIndex, false)
        }

        binding.viewPager2.postDelayed({
            val currentFragment = adapter.getFragment(startIndex)
            if (currentFragment != null) {
                Log.d("VideoPagerFragment", "Starting video immediately at $startIndex")
                currentFragment.startVideo()
                filePath = videoList[startIndex]
                nameFile = File(filePath).name
                binding.tvTitle.text = nameFile
            } else {
                Log.e("VideoPagerFragment", "Fragment at $startIndex is not ready")
            }
        }, 100)

        binding.tvShare.tap {
            filePath?.let { it1 -> shareVideo(requireActivity(), it1) }
        }

        binding.tvDelete.tap {
            val deleteDialog = DeleteDialog(requireActivity()) {
                filePath?.let {
                    checkRemove = true
                    val path = File(it)
                    progressDialog = ProgressDialog(requireActivity()).apply {
                        setMessage(getString(R.string.loading))
                        setCancelable(false)
                        show()
                    }
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            buildDeleteIntentSender(
                                requireActivity(),
                                path
                            ) { request, deletedImmediately ->
                                progressDialog.dismiss()
                                if (deletedImmediately) {
                                    removeCurrentVideo {
                                        showToast(getString(R.string.delete_success))
                                        progressDialog.dismiss()
                                    }
                                } else if (request != null) {
                                    deleteLauncher.launch(request)
                                } else {
//                                    showToast("Không thể tạo yêu cầu xoá")
                                    showToast(getString(R.string.failed))
                                    Log.d("LOCK_FLOW", "Không thể tạo yêu cầu xoá")
                                }
                            }
                        }
                    }
                }
            }
            deleteDialog.show()
        }

        binding.tvLock.tap {
            lockVideo()
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
        binding.imgBack.tap {
            back()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager2.unregisterOnPageChangeCallback(pageChangeCallback)
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun lockVideo() {
        checkRemove = false
        filePath?.let { path ->
            val originalFile = File(path)

            progressDialog = ProgressDialog(requireActivity()).apply {
                setMessage(getString(R.string.loading))
                setCancelable(false)
                show()
            }

            lifecycleScope.launch {
                val newFile = withContext(Dispatchers.IO) {
                    try {
                        val targetDir =
                            File(requireActivity().getExternalFilesDir("locked_videos"), "")
                        if (!targetDir.exists()) targetDir.mkdirs()
                        val fileName = generateTimestampedFileName("VIDEO_LOCK", ".mp4")
                        val targetFile = File(targetDir, fileName)
                        originalFile.copyTo(targetFile, overwrite = true)
                        targetFile
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                if (newFile != null) {
                    withContext(Dispatchers.IO) {
                        buildDeleteIntentSender(
                            requireActivity(),
                            originalFile
                        ) { request, deletedImmediately ->
                            progressDialog.dismiss()
                            if (deletedImmediately) {
                                val entity = MediaEntity(
                                    filePath = newFile.absolutePath,
                                    mediaType = MediaType.VIDEO
                                )
                                lifecycleScope.launch {
                                    mediaViewModel.insertMedia(entity)
                                    showToast(getString(R.string.restored_to_library))
                                    removeCurrentVideo {
                                        progressDialog.dismiss()
                                    }
                                }
                            } else if (request != null) {
                                copiedFile = newFile
                                deleteLauncher.launch(request)
                            } else {
//                                showToast("Không thể tạo yêu cầu xoá")
                                Log.d("LOCK_FLOW", "Không thể tạo yêu cầu xoá")
                                showToast(getString(R.string.failed))
                                newFile.delete()
                            }
                        }
                    }
                } else {
                    progressDialog.dismiss()
//                    showToast("Không thể sao chép file")
                }
            }
        }
    }

    private fun back() {
        val result = Bundle().apply {
            putStringArrayList("list", ArrayList(videoList))
        }
        parentFragmentManager.setFragmentResult("video_reload_request", result)
    }

    private fun removeCurrentVideo(onFinish: (() -> Unit)? = null) {
        val currentIndex = binding.viewPager2.currentItem

        if (videoList.isEmpty() || currentIndex !in videoList.indices) return

        adapter.getFragment(currentIndex)?.stopVideo()
        videoList.removeAt(currentIndex)
        adapter.notifyItemRemoved(currentIndex)
        adapter.notifyDataSetChanged()

        if (videoList.isEmpty()) {
            onFinish?.invoke()
            back()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        val safeIndex = currentIndex.coerceAtMost(videoList.lastIndex)

        binding.viewPager2.post {
            binding.viewPager2.setCurrentItem(safeIndex, false)

            filePath = videoList[safeIndex]
            binding.tvTitle.text = File(filePath).name
            binding.viewPager2.postDelayed({
                adapter.getFragment(safeIndex)?.startVideo()
                onFinish?.invoke()
            }, 1000)
        }
    }


}