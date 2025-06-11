package com.beautycam.hdcam.photoeditor.ui.vault.all

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.beautycam.hdcam.photoeditor.base.BaseFragment
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.data.DSDatabase
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModel
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModelFactory
import com.beautycam.hdcam.photoeditor.data.viewmodel.PictureViewModel
import com.beautycam.hdcam.photoeditor.databinding.FragmentPictureBinding
import com.beautycam.hdcam.photoeditor.ui.gallery.MediaAdapter
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.visible

class AllVaultFragment : BaseFragment<FragmentPictureBinding>() {
    private lateinit var mediaAdapter: VaultMediaAdapter
    private val mediaViewModel: MediaViewModel by activityViewModels {
        MediaViewModelFactory(
            MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        )
    }

    private val pictureViewModel: PictureViewModel by activityViewModels()
    private var mediaList = mutableListOf<MediaEntity>()

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPictureBinding {
        return FragmentPictureBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(),3)
        mediaAdapter = VaultMediaAdapter(mutableListOf()) { media ->
            val index = mediaList.indexOfFirst {
                Log.d("DOAN4", "item=${it.filePath}, clicked=${media.filePath}")
                it.filePath == media.filePath
            }
            if (index != -1) {
                pictureViewModel.setClickItemALL(mediaList, index)
            }
            Log.d(getTagDebug(), "initView: $index")
        }

        binding.recyclerView.adapter = mediaAdapter
        mediaViewModel.lockedMedia.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                Log.d(getTagDebug(), "1 = ${list.size}")
                binding.recyclerView.visible()
                binding.imgNodata.gone()
                mediaList = list.toMutableList()
                mediaAdapter.updateData(list)
            } else {
                Log.d(getTagDebug(), "2 = ${list.size}")
                binding.recyclerView.gone()
                binding.imgNodata.visible()
            }
        }

    }

    override fun viewListener() {

    }

    override fun dataObservable() {

    }

}