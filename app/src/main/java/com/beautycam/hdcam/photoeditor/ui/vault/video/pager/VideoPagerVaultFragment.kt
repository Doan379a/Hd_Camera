package com.beautycam.hdcam.photoeditor.ui.vault.video.pager

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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.DELETE_REQUEST_CODE
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.deleteMediaFileFromMediaStore
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.restoreLockedFileToGallery
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.restoreLockedFileToVideo
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareVideo
import java.io.File
import com.beautycam.hdcam.photoeditor.widget.tap
import gun0912.tedimagepicker.util.ToastUtil.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoPagerVaultFragment(
    private val videoList: MutableList<String>,
    private val startIndex: Int
) :
    Fragment() {

    private var _binding: FragmentVideoPagerBinding? = null
    private val binding get() = _binding!!
    private var filePath: String? = null
    private var nameFile: String? = null
    private lateinit var adapter: VideoPagerVaultAdapter
    private val mediaViewModel: MediaViewModel by activityViewModels {
        MediaViewModelFactory(
            MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        )
    }
    private var data: List<MediaEntity>? = null
    private lateinit var progressDialog: ProgressDialog


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
        adapter = VideoPagerVaultAdapter(requireActivity(), videoList)
        binding.viewPager2.adapter = adapter
        binding.tvLock.text = getString(R.string.unlock)//titile
        Log.d(getTagDebug(), "onViewCreated() - Start Index: $startIndex")
        binding.viewPager2.registerOnPageChangeCallback(pageChangeCallback)
        binding.viewPager2.post {
            Log.d("VideoPagerFragment", "Setting currentItem to $startIndex")
            binding.viewPager2.setCurrentItem(startIndex, false)
        }
        mediaViewModel.lockedMedia.observe(viewLifecycleOwner) {
            Log.d("Doan_2", "$it")
            data = it
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
                filePath?.let { path ->
                    val file = File(path)
                    progressDialog = ProgressDialog(requireActivity()).apply {
                        setMessage(getString(R.string.loading))
                        setCancelable(false)
                        show()
                    }
                    lifecycleScope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            deleteMediaFileFromMediaStore(requireActivity(), file)
                        }
                        uri.let {
                            data?.find {
                                Log.d("Doan_2", "${it.filePath}----$file")
                                it.filePath == path
                            }?.let { entity ->
                                mediaViewModel.deleteMedia(entity)
                                mediaViewModel.notifyReloadMedia()
                                showToast(getString(R.string.delete_success))
                                removeCurrentVideo {
                                    progressDialog.dismiss()
                                }

                            }
                        }?: showToast(getString(R.string.failed))
                    }
                }
            }
            deleteDialog.show()
        }

        binding.tvLock.tap {
            val deleteDialog = DeleteDialog(requireActivity()) {
                progressDialog = ProgressDialog(requireActivity()).apply {
                    setMessage(getString(R.string.loading))
                    setCancelable(false)
                    show()
                }
                lifecycleScope.launch {
                    filePath?.let { path ->
                        val file = File(path)
                        val uri = withContext(Dispatchers.IO) {
                            restoreLockedFileToVideo(requireActivity(), file)
                        }
                        uri?.let {
                            data?.find {
                                Log.d("Doan_2", "${it.filePath}----$path")
                                it.filePath == path
                            }?.let { entity ->
                                mediaViewModel.deleteMedia(entity)
                                mediaViewModel.notifyReloadMedia()
                                showToast(getString(R.string.restored_to_library))
                                removeCurrentVideo {
                                    progressDialog.dismiss()
                                }

                            }

                        } ?: showToast(getString(R.string.failed))
                    }
                }
            }
            deleteDialog.show()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()

                }
            })
        binding.imgBack.tap {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager2.unregisterOnPageChangeCallback(pageChangeCallback)
        _binding = null
    }


    private fun removeCurrentVideo(onFinish: (() -> Unit)? = null) {
        val currentIndex = binding.viewPager2.currentItem

        if (videoList.isEmpty() || currentIndex !in videoList.indices) return

        adapter.getFragment(currentIndex)?.stopVideo()
        videoList.removeAt(currentIndex)
        adapter.notifyItemRemoved(currentIndex)

        if (videoList.isEmpty()) {
            onFinish?.invoke()
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
            }, 100)
        }
    }


}