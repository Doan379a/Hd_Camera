package com.beautycam.hdcam.photoeditor.ui.intro.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.databinding.LayoutItemIntroBinding
import com.beautycam.hdcam.photoeditor.model.IntroModel
import com.beautycam.hdcam.photoeditor.widget.visible


@SuppressLint("UseCompatLoadingForDrawables")
open class Fragment1 : Fragment() {

    private lateinit var binding: LayoutItemIntroBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LayoutItemIntroBinding.inflate(inflater, container, false)
        try {
            initView()
            viewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }

    private fun initView() {
        try {
            val data = IntroModel(
                R.drawable.img_intro1,
                R.string.title_intro_1,
                R.string.content_intro_1, type = 0
            )
            if (data.type == 0) {
                binding.llContent.visible()
//                binding.frAds.gone()
                binding.ivIntro.setImageResource(data.image)
                binding.tvTitle.setText(data.title)
                binding.tvContent.setText(data.content)
            } else {
//                binding.llContent.gone()
//                binding.frAds.visibility = View.VISIBLE
//                val nativeBuilder = NativeBuilder(
//                    activity,
//                    binding.frAds,
//                    R.layout.ads_native_large_full_shimer,
//                    R.layout.layout_native_large_full,
//                    R.layout.layout_native_large_full
//                )
//                nativeBuilder.setListIdAd(arrayListOf(requireActivity().getString(R.string.native_introfull)))
//                val nativeManager = NativeManager(requireActivity(), this, nativeBuilder)

            }
        } catch (_: Exception) {

        }
    }

    private fun viewListener() {

    }
}