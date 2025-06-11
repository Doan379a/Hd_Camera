package com.beautycam.hdcam.photoeditor.ui.editor

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.beautycam.hdcam.photoeditor.R
import java.util.ArrayList

class ColorPickerAdapter internal constructor(
    private var context: Context,
    colorPickerColors: List<Int>,
) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    private var selectedPosition: Int = -1
    private var inflater: LayoutInflater
    private val colorPickerColors: List<Int>
    private lateinit var onColorPickerClickListener: OnColorPickerClickListener

    internal constructor(context: Context) : this(context, getDefaultColors(context)) {
        this.context = context
        inflater = LayoutInflater.from(context)
    }

    fun getSelectedPosition(): Int = selectedPosition
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.color_picker_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colorPickerColors[position]
        if (position == 0) {
            holder.colorPickerView.setBackgroundResource(R.drawable.img_default)
        } else {
            holder.colorPickerView.setBackgroundColor(color)
        }
        holder.cardColor.setCardBackgroundColor(
            if (selectedPosition == position) ContextCompat.getColor(
                context,
                R.color.white
            ) else Color.TRANSPARENT
        )
    }

    override fun getItemCount(): Int {
        return colorPickerColors.size
    }

    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView: View = itemView.findViewById(R.id.color_picker_view)
        var cardColor: CardView = itemView.findViewById(R.id.card_color)

        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onColorPickerClickListener.onColorPickerClickListener(colorPickerColors[adapterPosition])
            }
        }
    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int)
    }

    companion object {
        fun getDefaultColors(context: Context): List<Int> {
            val colorPickerColors = ArrayList<Int>()
            colorPickerColors.add(ContextCompat.getColor((context), R.color.blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.brown_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.green_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.red_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.black))
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.red_orange_color_picker
                )
            )
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.sky_blue_color_picker
                )
            )
            colorPickerColors.add(ContextCompat.getColor((context), R.color.violet_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.white))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.yellow_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.yellow_green_color_picker
                )
            )
            return colorPickerColors
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        this.colorPickerColors = colorPickerColors
    }
}