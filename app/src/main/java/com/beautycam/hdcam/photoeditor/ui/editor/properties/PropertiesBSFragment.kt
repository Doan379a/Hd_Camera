package com.beautycam.hdcam.photoeditor.ui.editor.properties

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.ui.editor.BottomSheetDismissListener
import com.beautycam.hdcam.photoeditor.ui.editor.ColorPickerAdapter
import com.beautycam.hdcam.photoeditor.utils.showColorPicker

class PropertiesBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onShapeSizeChanged(shapeSize: Int)
    }
    private var dismissListener: BottomSheetDismissListener? = null

    fun setDismissListener(listener: BottomSheetDismissListener) {
        dismissListener = listener
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_properties_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvColor: RecyclerView = view.findViewById(R.id.rvColors)
        val sbOpacity = view.findViewById<SeekBar>(R.id.sbOpacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.sbSize)
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)
        view.setBackgroundResource(R.drawable.bg_sticker)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = activity?.let { ColorPickerAdapter(it) }
        colorPickerAdapter?.setOnColorPickerClickListener(object :
            ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                if (colorPickerAdapter.getSelectedPosition() == 0) {
                    val defaultColor =
                        ContextCompat.getColor(requireActivity(), R.color.default_color)
                    requireActivity().showColorPicker(defaultColor, onColorPicked = { colorString ->
                        if (mProperties != null) {
                            val pickedColor = Color.parseColor(colorString)
                            mProperties!!.onColorChanged(pickedColor)
                        }
                    }, onDismiss = { dismiss() })
                } else {
                    if (mProperties != null) {
                        dismiss()
                        mProperties!!.onColorChanged(colorCode)
                    }
                }
            }
        })
        rvColor.adapter = colorPickerAdapter
    }

    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sbOpacity -> if (mProperties != null) {
                mProperties?.onOpacityChanged(i)
            }
            R.id.sbSize -> if (mProperties != null) {
                mProperties?.onShapeSizeChanged(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onBottomSheetDismissed()
    }
}