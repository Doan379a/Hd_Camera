package com.beautycam.hdcam.photoeditor.ui.gallery.image

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.beautycam.hdcam.photoeditor.base.BaseFragment
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.data.viewmodel.PictureViewModel
import com.beautycam.hdcam.photoeditor.databinding.FragmentPictureBinding
import com.beautycam.hdcam.photoeditor.ui.gallery.MediaAdapter
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.visible

class PictureGalleryFragment : BaseFragment<FragmentPictureBinding>() {
    private lateinit var mediaAdapter: MediaAdapter
    private val pictureViewModel: PictureViewModel by activityViewModels()
    private var mediaList = mutableListOf<String>()

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPictureBinding {
        return FragmentPictureBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        parentFragmentManager.setFragmentResultListener("picture_reload_request", this) { _, bundle ->
            val list = bundle.getStringArrayList("listImage") ?: emptyList()
            if (list.isNotEmpty()) {
                Log.d(getTagDebug(), "parentFragmentManager = ${list.size}")
                binding.recyclerView.visible()
                mediaList= list.toMutableList()
                binding.imgNodata.gone()
                mediaAdapter.updateData(list)
            } else {
                Log.d(getTagDebug(), "parentFragmentManager = ${list.size}")
                binding.recyclerView.gone()
                binding.imgNodata.visible()
            }
        }
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(),3)
        mediaAdapter = MediaAdapter(mutableListOf()) { media ->
            val index = mediaList.indexOf(media)
            if (index != -1) {
                pictureViewModel.setClickItemImage(mediaList, index)
            }
            Log.d(getTagDebug(), "initView: $index")
        }

        binding.recyclerView.adapter = mediaAdapter
        pictureViewModel.listPicture.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                Log.d(getTagDebug(), "1 = ${list.size}")
                binding.recyclerView.visible()
                mediaList= list.toMutableList()
                binding.imgNodata.gone()
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