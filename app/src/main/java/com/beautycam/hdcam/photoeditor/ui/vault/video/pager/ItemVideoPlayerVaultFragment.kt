package com.beautycam.hdcam.photoeditor.ui.vault.video.pager

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.databinding.ItemVideoPlayerBinding
import com.beautycam.hdcam.photoeditor.ui.gallery.video.pager.ItemVideoPlayerGalleryFragment
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.getMediaStoreUri
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.visible
import java.io.File

class ItemVideoPlayerVaultFragment : Fragment() {

    private var _binding: ItemVideoPlayerBinding? = null
    private val binding get() = _binding!!


    private var videoPath: String? = null
    private var isPlaying = false
    private val hideHandler = Handler(Looper.getMainLooper())
    private val handler = Handler(Looper.getMainLooper())
    private var isTracking = false

    companion object {
        private const val VIDEO_PATH_KEY = "video_path"

        fun newInstance(videoPath: String): ItemVideoPlayerVaultFragment {
            Log.d("ItemVideoPlayerFragment", "newInstance() called with path: $videoPath")
            val fragment = ItemVideoPlayerVaultFragment()
            fragment.arguments = Bundle().apply {
                putString(VIDEO_PATH_KEY, videoPath)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPath = arguments?.getString(VIDEO_PATH_KEY)

        if (videoPath != null) {
            setupVideoView(videoPath!!)
        }

        binding.parent.setOnClickListener {
            togglePlayPause()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && binding.videoView.duration > 0) {
                    val position = (binding.videoView.duration * progress) / 100
                    binding.videoView.seekTo(position)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTracking = false
            }
        })
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (binding.videoView.isPlaying && !isTracking) {
                val position = binding.videoView.currentPosition
                val duration = binding.videoView.duration
                if (duration > 0) {
                    val progress = (position * 100) / duration
                    binding.seekBar.progress = progress
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    private fun updateSeekBar() {
        handler.post(updateRunnable)
    }

    private fun setupVideoView(videoPath: String) {
        Log.d("ItemVideoPlayerFragment", "Initializing video: $videoPath")
        val uri =
            getMediaStoreUri(requireContext(), File(videoPath)) ?: Uri.fromFile(File(videoPath))
        binding.videoView.setVideoURI(uri)

        binding.videoView.setOnPreparedListener { mp ->
            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight
            val containerWidth = binding.root.width
            if (videoWidth > 0 && videoHeight > 0) {
                val calculatedHeight = containerWidth * videoHeight / videoWidth
                binding.videoView.layoutParams = binding.videoView.layoutParams.apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = calculatedHeight
                }
            }

            binding.videoView.start()
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            isPlaying = true
            updateSeekBar()
            hideHandler.postDelayed({ binding.btnPlayPause.gone() }, 3000)
        }

        binding.videoView.setOnCompletionListener {
            isPlaying = false
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            binding.btnPlayPause.visible()
            binding.seekBar.progress = 0
            handler.removeCallbacks(updateRunnable)
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    private fun playVideo() {
        if (!isAdded || _binding == null) {
            Log.w(
                "ItemVideoPlayerFragment",
                "Fragment is not added or binding is null, cannot play video"
            )
            return
        }
        binding.videoView.start()
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        isPlaying = true

        binding.btnPlayPause.visible()
        hideHandler.removeCallbacksAndMessages(null)
        hideHandler.postDelayed({
            if (isPlaying) {
                binding.btnPlayPause.gone()
            }
        }, 3000)
        updateSeekBar()
    }

    private fun pauseVideo() {
        if (!isAdded || _binding == null) {
            Log.w(
                "ItemVideoPlayerFragment",
                "Fragment is not added or binding is null, cannot pause video"
            )
            return
        }
        binding.videoView.pause()
        binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        binding.btnPlayPause.visibility = View.VISIBLE
        isPlaying = false

        hideHandler.removeCallbacksAndMessages(null)

        handler.removeCallbacks(updateRunnable)
    }

    fun startVideo() {
        playVideo()
    }

    fun stopVideo() {
        pauseVideo()
    }


    override fun onPause() {
        super.onPause()
        pauseVideo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideHandler.removeCallbacksAndMessages(null)
        handler.removeCallbacks(updateRunnable)
        _binding = null
    }
}