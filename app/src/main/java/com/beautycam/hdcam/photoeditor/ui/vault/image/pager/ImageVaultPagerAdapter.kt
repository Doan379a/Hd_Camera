package com.beautycam.hdcam.photoeditor.ui.vault.image.pager


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.getMediaStoreUri
import java.io.File

class ImageVaultPagerAdapter(private val imageList: List<String>) : RecyclerView.Adapter<ImageVaultPagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imagePath: String) {
            val file = File(imagePath)
            val contentUri = getMediaStoreUri(itemView.context, file)
            Glide.with(itemView.context)
                .load(contentUri ?: file)
                .placeholder(R.drawable.img_loadding)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_pager, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size
}
