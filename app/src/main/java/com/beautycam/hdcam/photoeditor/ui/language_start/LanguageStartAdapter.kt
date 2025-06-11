package com.beautycam.hdcam.photoeditor.ui.language_start

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseAdapter
import com.beautycam.hdcam.photoeditor.databinding.ItemLanguageBinding
import com.beautycam.hdcam.photoeditor.model.LanguageModel

class LanguageStartAdapter(
    val context: Context,
    val onClick: (lang: LanguageModel) -> Unit
) : BaseAdapter<ItemLanguageBinding, LanguageModel>() {

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemLanguageBinding {
        return ItemLanguageBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ItemLanguageBinding): RecyclerView.ViewHolder =
        LanguageVH(binding)


    inner class LanguageVH(binding: ItemLanguageBinding) : BaseVH<LanguageModel>(binding) {
        override fun onItemClickListener(data: LanguageModel) {
            super.onItemClickListener(data)
            onClick.invoke(data)
        }

        override fun bind(data: LanguageModel) {
            super.bind(data)
            binding.txtName.text = data.name
            if (data.active) {
                binding.layoutItem.setBackgroundResource(R.drawable.border_item_language_select)
                binding.txtName.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                binding.layoutItem.setBackgroundResource(R.drawable.border_item_language)
                binding.txtName.setTextColor(Color.parseColor("#292B2B"))
            }
            when (data.code) {

                "en" -> binding.icLang.setImageResource(R.drawable.flag_en)
                "de" -> binding.icLang.setImageResource(R.drawable.flag_ger)
                "es" -> binding.icLang.setImageResource(R.drawable.flag_span)
                "fr" -> binding.icLang.setImageResource(R.drawable.flag_fra)
                "hi" -> binding.icLang.setImageResource(R.drawable.flag_hindi)
                "in" -> binding.icLang.setImageResource(R.drawable.flag_indonesia)
                "pt" -> binding.icLang.setImageResource(R.drawable.flag_port)
                "vi" -> binding.icLang.setImageResource(R.drawable.flag_vi)
                "ja" -> binding.icLang.setImageResource(R.drawable.flag_japan)
            }
        }
    }


    fun setCheck(code: String) {
        for (item in listData) {
            item.active = item.code == code
        }
        notifyDataSetChanged()
    }
}