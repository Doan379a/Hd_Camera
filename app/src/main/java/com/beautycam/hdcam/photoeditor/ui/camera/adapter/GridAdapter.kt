package com.beautycam.hdcam.photoeditor.ui.camera.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseAdapter
import com.beautycam.hdcam.photoeditor.databinding.ItemGridBinding
import com.beautycam.hdcam.photoeditor.model.GridModel
import com.beautycam.hdcam.photoeditor.widget.tap

class GridAdapter(
    val context: Context,
    val onClick: (grid: GridModel) -> Unit
) : BaseAdapter<ItemGridBinding, GridModel>(){

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemGridBinding {
        return ItemGridBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ItemGridBinding): RecyclerView.ViewHolder = GridVH(binding)

    inner class GridVH(binding: ItemGridBinding) : BaseVH<GridModel>(binding){
        override fun onItemClickListener(data: GridModel) {
            super.onItemClickListener(data)
            onClick.invoke(data)
        }
        override fun bind(data: GridModel) {
            super.bind(data)
            binding.tvGridName.text = data.name

            if (data.active) binding.tvGridName.setBackgroundResource(R.drawable.bg_choose)
            else binding.tvGridName.setBackgroundColor(Color.TRANSPARENT)

            binding.root.tap {
                notifyDataSetChanged()
                onClick(data)
            }
        }
    }

    fun setCheck(position: Int){
        for (item in listData){
            item.active = item.id == position
        }
        notifyDataSetChanged()
    }
}