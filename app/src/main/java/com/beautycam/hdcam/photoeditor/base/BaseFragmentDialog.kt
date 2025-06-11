package com.beautycam.hdcam.photoeditor.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragmentDialog<T : ViewBinding>(
) : DialogFragment() {

    private var _binding: T? = null
    protected val binding: T
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        initView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T
    abstract fun initView()
}