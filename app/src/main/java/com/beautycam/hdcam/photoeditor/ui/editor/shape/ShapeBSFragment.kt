package com.beautycam.hdcam.photoeditor.ui.editor.shape

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.ui.editor.BottomSheetDismissListener
import com.beautycam.hdcam.photoeditor.ui.editor.ColorPickerAdapter
import com.beautycam.hdcam.photoeditor.utils.showColorPicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.shape.ShapeType

class ShapeBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onShapeSizeChanged(shapeSize: Int)
        fun onShapePicked(shapeType: ShapeType)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_shapes_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvColor: RecyclerView = view.findViewById(R.id.shapeColors)
        val sbOpacity = view.findViewById<SeekBar>(R.id.shapeOpacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.shapeSize)
        val shapeGroup = view.findViewById<RadioGroup>(R.id.shapeRadioGroup)
        val doneShape = view.findViewById<TextView>(R.id.tv_done)
        view.setBackgroundResource(R.drawable.bg_sticker)

        // shape picker
        shapeGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.lineRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Line)
                }

                R.id.arrowRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Arrow())
                }

                R.id.ovalRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Oval)
                }

                R.id.rectRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Rectangle)
                }

                else -> {
                    mProperties!!.onShapePicked(ShapeType.Brush)
                }
            }
        }
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)

        val activity = requireActivity()


        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)
        colorPickerAdapter.setOnColorPickerClickListener(object :
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
        doneShape.setOnClickListener {
            dismiss()
        }
    }

    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.shapeOpacity -> if (mProperties != null) {
                mProperties!!.onOpacityChanged(i)
            }

            R.id.shapeSize -> if (mProperties != null) {
                mProperties!!.onShapeSizeChanged(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    private var dismissListener: BottomSheetDismissListener? = null

    fun setDismissListener(listener: BottomSheetDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onBottomSheetDismissed()
    }
}