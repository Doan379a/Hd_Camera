package com.beautycam.hdcam.photoeditor.ui.editor.sticker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.databinding.FragmentBottomStickerEmojiDialogBinding
import com.beautycam.hdcam.photoeditor.databinding.RowStickerBinding
import com.beautycam.hdcam.photoeditor.ui.editor.BottomSheetDismissListener
import com.beautycam.hdcam.photoeditor.utils.getBitmapFromAsset
import com.beautycam.hdcam.photoeditor.widget.tap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class StickerBSFragment : BottomSheetDialogFragment() {
    private var mStickerListener: StickerListener? = null
    private var dismissListener: BottomSheetDismissListener? = null
    private lateinit var binding: FragmentBottomStickerEmojiDialogBinding

    fun setStickerListener(stickerListener: StickerListener?) {
        mStickerListener = stickerListener
    }

    fun setDismissListener(listener: BottomSheetDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onBottomSheetDismissed()
    }

    interface StickerListener {
        fun onStickerClick(bitmap: Bitmap)
    }

    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding = FragmentBottomStickerEmojiDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        val params = (binding.root.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if (behavior is BottomSheetBehavior<*>) {
            behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        }

        (binding.root.parent as View).setBackgroundColor(resources.getColor(android.R.color.white))
        binding.root.setBackgroundResource(R.drawable.bg_sticker)

        val gridLayoutManager = GridLayoutManager(activity, 4)
        binding.rvEmoji.layoutManager = gridLayoutManager
        val stickerAdapter = StickerAdapter()
        binding.rvEmoji.adapter = stickerAdapter
        binding.rvEmoji.setHasFixedSize(true)
        binding.rvEmoji.setItemViewCacheSize(stickerPathList.size)
        binding.tvTitle.text=getString(R.string.sticker)
        binding.tvTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
          R.drawable.ic_sticker_2,
            0,
            0,
            0
        )
        binding.tvDone.tap {
            dismiss()
        }
    }

    inner class StickerAdapter : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RowStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sticker = stickerPathList[position]
            val fromAsset = requireActivity().getBitmapFromAsset(requireActivity(), sticker)
            Glide.with(requireActivity())
                .asBitmap()
                .load(fromAsset)
                .into(holder.binding.imgSticker)
        }

        override fun getItemCount(): Int = stickerPathList.size

        inner class ViewHolder(val binding: RowStickerBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    val sticker = stickerPathList[adapterPosition]
                    val fromAsset = requireActivity().getBitmapFromAsset(requireActivity(), sticker)
                    Glide.with(requireActivity())
                        .asBitmap()
                        .load(fromAsset)
                        .into(object : CustomTarget<Bitmap?>(256, 256) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                                mStickerListener?.onStickerClick(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                    dismiss()
                }
            }
        }
    }

    companion object {
        private val stickerPathList: List<String> by lazy {
            val baseStickers = listOf(
                "sticker/animal/animal_cat1.png",
            "sticker/animal/animal_deer1.png",
            "sticker/animal/animal_dog1.png",
            "sticker/animal/animal_fox1.png",
            "sticker/animal/animal_bee1.png",
            "sticker/animal/animal_frog1.png",
            "sticker/animal/animal_hippo1.png",
            "sticker/animal/animal_fox2.png",
            "sticker/animal/animal_hen.png",
            "sticker/animal/animal_donkey.png",
            /////
            "sticker/facial/facial_1.webp",
            "sticker/facial/facial_2.webp",
            "sticker/facial/facial_3.webp",
            "sticker/facial/facial_4.webp",
            "sticker/facial/facial_5.webp",
            "sticker/facial/facial_6.webp",
            "sticker/facial/facial_7.webp",
            "sticker/facial/facial_8.webp",
            "sticker/facial/facial_9.webp",
            "sticker/facial/facial_10.webp",
            "sticker/facial/facial_11.webp",
            "sticker/facial/facial_12.png",
            "sticker/facial/facial_13.png",
            ////
            "sticker/food/food_birthday_cake_1.png",
            "sticker/food/food_birthday_cake_2.png",
            "sticker/food/food_birthday_cake_3.png",
            "sticker/food/food_cupcake.png",
            "sticker/food/food_cupcake_with_cherry.png",
            "sticker/food/food_ice_cream_1.png",
            "sticker/food/food_ice_cream2.png",
            "sticker/food/food_ice_cream3.png",
            "sticker/food/food_ice_cream_4.png",
            "sticker/food/food_pizza1.png",
            "sticker/food/food_pizza2.png",
            ////
            "sticker/fun/fun_rainbow.png",
            "sticker/fun/fun_star.png",
            "sticker/fun/fun_star2.png",
            "sticker/fun/fun_stool.png",
            ////
            "sticker/words/words_bang.png",
            "sticker/words/words_yeah.png",
            "sticker/words/words_blog.png",
            "sticker/words/words_boom.png",
            "sticker/words/words_happy_birthday1.png",
            "sticker/words/words_happy_birthday2.png",
            "sticker/words/words_happy_birthday3.png",
            "sticker/words/words_legacy.png",
            "sticker/words/words_win1.png",
            "sticker/words/words_wow.png")
            //stickerv2
            val generated = (1..236).map { i ->
                "sticker/stickerv1/sticker_$i.png"
            }
            baseStickers + generated
        }
    }
}
