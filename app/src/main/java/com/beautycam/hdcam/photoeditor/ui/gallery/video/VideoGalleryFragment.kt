package com.beautycam.hdcam.photoeditor.ui.gallery.video

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.beautycam.hdcam.photoeditor.base.BaseFragment
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.data.viewmodel.PictureViewModel
import com.beautycam.hdcam.photoeditor.databinding.FragmentVideoBinding
import com.beautycam.hdcam.photoeditor.ui.gallery.MediaAdapter
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.visible

class VideoGalleryFragment : BaseFragment<FragmentVideoBinding>() {
    private lateinit var mediaAdapter: MediaAdapter
    private val pictureViewModel: PictureViewModel by activityViewModels()
    private var mediaList = mutableListOf<String>()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoBinding {
        return FragmentVideoBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        parentFragmentManager.setFragmentResultListener("video_reload_request", this) { _, bundle ->
            val list = bundle.getStringArrayList("list") ?: emptyList()
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
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        mediaAdapter = MediaAdapter(mutableListOf()) { media ->
            val index = mediaList.indexOf(media)
            if (index != -1) {
                pictureViewModel.setClickItemVideo(mediaList, index)
            }
            Log.d(getTagDebug(), "initView: $index")
        }
        binding.recyclerView.adapter = mediaAdapter
        pictureViewModel.listVideo.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.recyclerView.visible()
                mediaList= list.toMutableList()
                binding.imgNodata.gone()
                mediaAdapter.updateData(list)
            } else {
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