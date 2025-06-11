package com.beautycam.hdcam.photoeditor.ui.vault.all.pager

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.data.DSDatabase
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.entity.MediaType
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModel
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModelFactory
import com.beautycam.hdcam.photoeditor.databinding.FragmentImagePagerBinding
import com.beautycam.hdcam.photoeditor.dialog.DeleteDialog
import com.beautycam.hdcam.photoeditor.ui.editor.EditImageActivity
import com.beautycam.hdcam.photoeditor.ui.vault.video.pager.ItemVideoPlayerVaultFragment
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.buildDeleteIntentSender
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.deleteLocalFileCache
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.deleteMediaFileFromMediaStore
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.restoreLockedFileToGallery
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.restoreLockedFileToVideo
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareImage
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareVideo
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.visible
import gun0912.tedimagepicker.util.ToastUtil.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MixedVaultPagerFragment(
    private val mediaList: MutableList<MediaEntity>,
    private val startIndex: Int
) : Fragment() {

    private lateinit var binding: FragmentImagePagerBinding // hoặc layout mới nếu cần
    private lateinit var adapter: MixedVaultPagerAdapter
    private var clickVideo: Boolean = false
    private var filePath: String? = null
    private val mediaViewModel: MediaViewModel by activityViewModels {
        MediaViewModelFactory(
            MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        )
    }
    private var data: List<MediaEntity>? = null
    private lateinit var progressDialog: ProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MixedVaultPagerAdapter(this, mediaList)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.setCurrentItem(startIndex, false)
        mediaViewModel.lockedMedia.observe(viewLifecycleOwner) {
            Log.d("Doan_2", "$it")
            data = it
        }
        binding.tvLock.text = getString(R.string.unlock)//titile
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                filePath = mediaList[position].filePath
                binding.tvTitle.text = File(filePath).name
                val mediaType = mediaList[position].mediaType
                if (mediaType == MediaType.VIDEO) {
                    clickVideo = true
                    binding.tvEdit.gone()
                } else {
                    clickVideo = false
                    binding.tvEdit.visible()
                }
            }
        })
        filePath = mediaList[startIndex].filePath
        binding.tvTitle.text = File(filePath).name
        binding.tvShare.tap {
            filePath?.let { it1 ->
                if (clickVideo) {
                    shareVideo(requireActivity(), it1)
                } else {
                    shareImage(requireActivity(), it1)
                }
            }
        }
        binding.tvLock.tap {
            val currentPos = binding.viewPager2.currentItem
            val fragment = adapter.getFragment(currentPos)
            if (fragment is ItemVideoPlayerVaultFragment) {
                fragment.stopVideo()
            }
            filePath?.let { path ->
                val saveDialog = DeleteDialog(requireActivity()) {
                    val currentIndex = binding.viewPager2.currentItem
                    val currentMedia = mediaList.getOrNull(currentIndex)
                    progressDialog = ProgressDialog(requireActivity()).apply {
                        setMessage(getString(R.string.loading))
                        setCancelable(false)
                        show()
                    }

                    lifecycleScope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            if (currentMedia?.mediaType == MediaType.VIDEO) {
                                restoreLockedFileToVideo(
                                    requireActivity(),
                                    File(currentMedia.filePath)
                                )
                            } else {
                                restoreLockedFileToGallery(
                                    requireActivity(),
                                    File(currentMedia?.filePath)
                                )
                            }
                        }
                        uri?.let {
                            data?.find { it.filePath == currentMedia?.filePath }?.let { entity ->
                                mediaViewModel.deleteMedia(entity)
                                mediaViewModel.notifyReloadMedia()
                                showToast(getString(R.string.restored_to_library))
                                removeCurrent {
                                    progressDialog.dismiss()
                                }
                            }
                        } ?: { Log.d("DOAN_1", "Failed to delete")
                            showToast(getString(R.string.failed))}
                        progressDialog.dismiss()
                    }
                }
                saveDialog.show()
            }
        }

        binding.tvDelete.tap {
            val saveDialog = DeleteDialog(requireActivity()) {
                val currentPos = binding.viewPager2.currentItem
                val fragment = adapter.getFragment(currentPos)
                if (fragment is ItemVideoPlayerVaultFragment) {
                    fragment.stopVideo()
                }
                filePath?.let { path ->
                    val file = File(path)
                    progressDialog = ProgressDialog(requireActivity()).apply {
                        setMessage(getString(R.string.loading))
                        setCancelable(false)
                        show()
                    }
                    lifecycleScope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            deleteLocalFileCache(file)
                        }
                        uri.let {
                            data?.find {
                                Log.d("Doan_2", "${it.filePath}----$path")
                                it.filePath == path
                            }?.let { entity ->
                                lifecycleScope.launch {
                                    mediaViewModel.deleteMedia(entity)
                                    mediaViewModel.notifyReloadMedia()
                                    showToast(getString(R.string.delete_success))
                                    removeCurrent {
                                        progressDialog.dismiss()
                                    }
                                }
                            }
                        } ?: { Log.d("DOAN_1", "Failed to delete")
                            showToast(getString(R.string.failed))}
                    }
                }
            }
            saveDialog.show()
        }
        binding.imgBack.tap {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.tvEdit.tap {
            filePath?.let { path ->
                val imageUri = Uri.fromFile(File(path))
                val intent = Intent(requireActivity(), EditImageActivity::class.java).apply {
                    putExtra("URI_IMAGE", imageUri)
                }
                startActivity(intent)
            }
        }
    }

    private fun removeCurrent(onFinish: (() -> Unit)? = null) {
        val currentIndex = binding.viewPager2.currentItem
        if (mediaList.isEmpty() || currentIndex !in mediaList.indices) return

        mediaList.removeAt(currentIndex)
        adapter.notifyDataSetChanged()

        if (mediaList.isEmpty()) {
            onFinish?.invoke()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        val safeIndex = currentIndex.coerceAtMost(mediaList.lastIndex)
        binding.viewPager2.setCurrentItem(safeIndex, false)

        binding.viewPager2.post {
            filePath = mediaList[safeIndex].filePath
            binding.tvTitle.text = File(filePath).name
            clickVideo = mediaList[safeIndex].mediaType == MediaType.VIDEO
            onFinish?.invoke()
        }
    }
}

