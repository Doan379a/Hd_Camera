package com.beautycam.hdcam.photoeditor.ui.camera.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beautycam.hdcam.photoeditor.base.BaseAdapter
import com.beautycam.hdcam.photoeditor.databinding.ItemFilterBinding
import com.beautycam.hdcam.photoeditor.model.FilterModel

class FilterAdapter (
    val context: Context,
    val onClick: (fil: FilterModel) -> Unit
) : BaseAdapter<ItemFilterBinding, FilterModel>() {
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemFilterBinding {
        return ItemFilterBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ItemFilterBinding): RecyclerView.ViewHolder = FilterVH(binding)

    inner class FilterVH(binding: ItemFilterBinding) : BaseVH<FilterModel>(binding){
        override fun onItemClickListener(data: FilterModel) {
            super.onItemClickListener(data)
            onClick.invoke(data)
        }

        override fun bind(data: FilterModel) {
            super.bind(data)
            binding.imgFilter.setImageResource(data.img)
        }

    }
}