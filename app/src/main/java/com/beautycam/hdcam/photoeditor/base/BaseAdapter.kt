package com.beautycam.hdcam.photoeditor.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.widget.tap


abstract class BaseAdapter<VB : ViewBinding, M : Any> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected var listData: MutableList<M> = arrayListOf()
    protected abstract fun createBinding(
        inflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ): VB

    protected abstract fun createVH(binding: VB): RecyclerView.ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: VB = createBinding(LayoutInflater.from(parent.context), parent, viewType)
        return createVH(binding)
    }

    override fun getItemCount(): Int = if (listData.size > 0) listData.size else 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (listData.size == 0) {
            holder.itemView.tap {
                Toast.makeText(
                    holder.itemView.context,
                    R.string.this_is_item_sample,
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        (holder as BaseVH<M>).bind(listData[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addList(newList: MutableList<M>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    fun insertList(fromIdx: Int, insertedList: MutableList<M>) {
        listData.addAll(fromIdx, insertedList)
        notifyItemRangeInserted(fromIdx, insertedList.size)
    }

    abstract inner class BaseVH<M>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {
        open fun onItemClickListener(data: M) = Unit
        open fun bind(data: M) {
            binding.root.tap { onItemClickListener(data) }
        }
    }
}