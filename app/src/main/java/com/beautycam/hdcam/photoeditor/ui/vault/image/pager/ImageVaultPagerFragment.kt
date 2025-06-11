package com.beautycam.hdcam.photoeditor.ui.vault.image.pager

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.beautycam.hdcam.photoeditor.ui.editor.EditImageActivity
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.DELETE_REQUEST_CODE
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.deleteLocalFileCache
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.restoreLockedFileToGallery
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareImage
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.shareVideo
import com.beautycam.hdcam.photoeditor.widget.tap
import gun0912.tedimagepicker.util.ToastUtil.showToast
import java.io.File

class ImageVaultPagerFragment(
    private val imageList: MutableList<String>,
    private val startIndex: Int
) : Fragment() {

    private var filePath: String? = null
    private var nameFile: String? = null
    private var _binding: FragmentImagePagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ImageVaultPagerAdapter
    private var data: List<MediaEntity>? = null
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(getTagDebug(), "onPageSelected() - Position: $position")
            filePath = imageList[position]
            nameFile = File(filePath).name
            binding.tvTitle.text = nameFile
        }
    }
    private val mediaViewModel: MediaViewModel by activityViewModels {
        MediaViewModelFactory(
            MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        )
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

        adapter = ImageVaultPagerAdapter(imageList)
        binding.tvLock.text = getString(R.string.unlock)//titile
        binding.viewPager2.adapter = adapter
        binding.viewPager2.setCurrentItem(startIndex, false)
        binding.viewPager2.registerOnPageChangeCallback(pageChangeCallback)

        mediaViewModel.lockedMedia.observe(viewLifecycleOwner) {
            Log.d("Doan_2", "$it")
            data = it
        }

        filePath = imageList[startIndex]
        nameFile = File(filePath).name

        binding.tvTitle.text = nameFile
        binding.tvDelete.tap {
            val deleteDialog = DeleteDialog(requireActivity()) {
                filePath?.let { path ->
                    val file = File(path)
                    deleteLocalFileCache(file)
                    data?.find {
                        Log.d("Doan_2", "${it.filePath}----$path")
                        it.filePath == path
                    }?.let { entity ->
                        mediaViewModel.deleteMedia(entity)
                        mediaViewModel.notifyReloadMedia()
                        showToast(getString(R.string.delete_success))
                        removeCurrentImage()
                    }
                }
            }
            deleteDialog.show()
        }
        binding.tvLock.tap {
            filePath?.let { path ->
                val deleteDialog = DeleteDialog(requireActivity()) {
                    val file = File(path)
                    val uri = restoreLockedFileToGallery(requireActivity(), file)
                    uri?.let {
                        data?.find {
                            Log.d("Doan_2", "${it.filePath}----$path")
                            it.filePath == path
                        }?.let { entity ->
                            mediaViewModel.deleteMedia(entity)
                            mediaViewModel.notifyReloadMedia()
                            showToast(getString(R.string.restored_to_library))
                            removeCurrentImage()
                        }

                    } ?:  showToast(getString(R.string.failed))
                }
                deleteDialog.show()
            }
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
        binding.tvShare.tap {
            filePath?.let { it1 ->
                shareImage(requireActivity(), it1)
            }
        }
        binding.tvEdit.tap{
            filePath?.let { path ->
                val imageUri = Uri.fromFile(File(path))
                val intent = Intent(requireActivity(), EditImageActivity::class.java).apply {
                    putExtra("URI_IMAGE", imageUri)
                }
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun removeCurrentImage() {
        val currentIndex = binding.viewPager2.currentItem

        if (imageList.isEmpty() || currentIndex !in imageList.indices) return

        val nextIndex = if (currentIndex == imageList.lastIndex) {
            currentIndex - 1
        } else {
            currentIndex + 1
        }

        imageList.removeAt(currentIndex)
        adapter.notifyDataSetChanged()
        if (imageList.isEmpty()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }
        val safeIndex = nextIndex.coerceIn(imageList.indices)
        binding.viewPager2.setCurrentItem(safeIndex, false)

        binding.viewPager2.post {
            filePath = imageList[safeIndex]
            nameFile = File(filePath).name
            binding.tvTitle.text = nameFile
        }


    }


}
