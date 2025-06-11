package com.beautycam.hdcam.photoeditor.ui.editor.filters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.databinding.RowFilterViewBinding
import ja.burhanrashid52.photoeditor.PhotoFilter
import java.io.IOException
import java.util.ArrayList


class FilterViewAdapter(private val mFilterListener: FilterListener) :
    RecyclerView.Adapter<FilterViewAdapter.ViewHolder>() {

    private val mPairList: MutableList<Pair<String, PhotoFilter>> = ArrayList()
    private var selectedItemPosition = -1
    private var isItem0Modified = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowFilterViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterPair = mPairList[position]
        val fromAsset = getBitmapFromAsset(holder.binding.root.context, filterPair.first)
        val isSelected = position == selectedItemPosition
        val isNoneFilter = filterPair.second == PhotoFilter.NONE
        if (position == 0 && isItem0Modified) {
            holder.binding.imgFilterView.setImageResource(R.drawable.img_none_selected) // Ảnh thay đổi
            holder.binding.txtFilterName.setTextColor(Color.parseColor("#FF8594"))
        } else if (isNoneFilter) {
            val drawableResId =
                if (isSelected) R.drawable.img_none_selected else R.drawable.img_none
            holder.binding.imgFilterView.setImageResource(drawableResId)
            holder.binding.txtFilterName.setTextColor(Color.parseColor("#FF8594"))
        } else {
            if (fromAsset != null) {
                holder.binding.imgFilterView.setImageBitmap(fromAsset)
            } else {
                holder.binding.imgFilterView.setImageResource(R.drawable.img_none)
            }
        }
        val textColor = if (isSelected) Color.parseColor("#FF8594") else Color.parseColor("#585858")
        holder.binding.txtFilterName.setTextColor(textColor)

        holder.binding.txtFilterName.text =
            filterPair.second.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

        holder.binding.cardBoder.setCardBackgroundColor(
            if (isSelected && !isNoneFilter) Color.parseColor("#FF8594") else Color.TRANSPARENT
        )

    }

    override fun getItemCount(): Int {
        return mPairList.size
    }

    inner class ViewHolder(val binding: RowFilterViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (layoutPosition == 0) {
                    // Nếu click vào item 0, thay đổi trạng thái của item 0
                    isItem0Modified = !isItem0Modified
                } else {
                    // Nếu click vào item khác, reset item 0 về trạng thái ban đầu
                    if (isItem0Modified) {
                        isItem0Modified = false
                    }
                }
                selectedItemPosition = layoutPosition
                notifyDataSetChanged()
                mFilterListener.onFilterSelected(mPairList[layoutPosition].second)
            }
        }
    }

    private fun getBitmapFromAsset(context: Context, strName: String): Bitmap? {
        val assetManager = context.assets
        return try {
            val istr = assetManager.open(strName)
            BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setupFilters() {
        mPairList.add(Pair("filters/original.jpg", PhotoFilter.NONE))
        mPairList.add(Pair("filters/auto_fix.png", PhotoFilter.AUTO_FIX))
        mPairList.add(Pair("filters/brightness.png", PhotoFilter.BRIGHTNESS))
        mPairList.add(Pair("filters/contrast.png", PhotoFilter.CONTRAST))
        mPairList.add(Pair("filters/documentary.png", PhotoFilter.DOCUMENTARY))
        mPairList.add(Pair("filters/dual_tone.png", PhotoFilter.DUE_TONE))
        mPairList.add(Pair("filters/fill_light.png", PhotoFilter.FILL_LIGHT))
        mPairList.add(Pair("filters/fish_eye.png", PhotoFilter.FISH_EYE))
        mPairList.add(Pair("filters/grain.png", PhotoFilter.GRAIN))
        mPairList.add(Pair("filters/gray_scale.png", PhotoFilter.GRAY_SCALE))
        mPairList.add(Pair("filters/lomish.png", PhotoFilter.LOMISH))
        mPairList.add(Pair("filters/negative.png", PhotoFilter.NEGATIVE))
        mPairList.add(Pair("filters/posterize.png", PhotoFilter.POSTERIZE))
        mPairList.add(Pair("filters/saturate.png", PhotoFilter.SATURATE))
        mPairList.add(Pair("filters/sepia.png", PhotoFilter.SEPIA))
        mPairList.add(Pair("filters/sharpen.png", PhotoFilter.SHARPEN))
        mPairList.add(Pair("filters/temprature.png", PhotoFilter.TEMPERATURE))
        mPairList.add(Pair("filters/tint.png", PhotoFilter.TINT))
        mPairList.add(Pair("filters/vignette.png", PhotoFilter.VIGNETTE))
        mPairList.add(Pair("filters/cross_process.png", PhotoFilter.CROSS_PROCESS))
        mPairList.add(Pair("filters/b_n_w.png", PhotoFilter.BLACK_WHITE))
        mPairList.add(Pair("filters/flip_horizental.png", PhotoFilter.FLIP_HORIZONTAL))
        mPairList.add(Pair("filters/flip_vertical.png", PhotoFilter.FLIP_VERTICAL))
        mPairList.add(Pair("filters/rotate.png", PhotoFilter.ROTATE))
    }

    init {
        setupFilters()
    }
}