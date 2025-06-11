package com.beautycam.hdcam.photoeditor.ui.vault.all.pager

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.data.DSDatabase
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.repository.MediaRepository
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModel
import com.beautycam.hdcam.photoeditor.data.viewmodel.MediaViewModelFactory
import com.beautycam.hdcam.photoeditor.databinding.ItemImagePagerBinding
import java.io.File

class ItemImageVaultFragment : Fragment() {

    private var _binding: ItemImagePagerBinding? = null
    private val binding get() = _binding!!

    private var imagePath: String? = null
    private var mediaData: List<MediaEntity>? = null

    private val mediaViewModel: MediaViewModel by activityViewModels {
        MediaViewModelFactory(
            MediaRepository(DSDatabase.getDatabase(requireActivity()).mediaDao())
        )
    }

    companion object {
        fun newInstance(imagePath: String): ItemImageVaultFragment {
            val fragment = ItemImageVaultFragment()
            fragment.arguments = Bundle().apply {
                putString("path", imagePath)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePath = arguments?.getString("path")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemImagePagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePath?.let { path ->
            Glide.with(this)
                .load(File(path))
                .placeholder(R.drawable.img_loadding)
                .into(binding.imageView)

        }

        // Observe media list
        mediaViewModel.lockedMedia.observe(viewLifecycleOwner) {
            mediaData = it
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
