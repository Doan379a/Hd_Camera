package com.beautycam.hdcam.photoeditor.ui.vault.all

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.getMediaStoreUri
import java.io.File
import java.util.concurrent.TimeUnit


class VaultMediaAdapter(
    private var mediaList: List<MediaEntity>,
    private val onItemClick: (MediaEntity) -> Unit
) : RecyclerView.Adapter<VaultMediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        holder.bind(media)
    }

    override fun getItemCount(): Int = mediaList.size

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
        private val mediaTypeTextView: TextView = itemView.findViewById(R.id.mediaTypeTextView)

        fun bind(media: MediaEntity) {
            val file = File(media.filePath)
            val contentUri = getMediaStoreUri(itemView.context, file)
            Glide.with(itemView.context)
                .load(contentUri ?: file)
                .placeholder(R.drawable.img_loadding)
                .into(mediaImageView)

            val extension = file.extension.lowercase()

            if (extension == "mp4" || extension == "avi" || extension == "mkv") {
                val retriever = MediaMetadataRetriever()
                try {
                    mediaTypeTextView.visibility = View.VISIBLE

                    retriever.setDataSource(media.filePath)
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = durationStr?.toLongOrNull() ?: 0L
                    mediaTypeTextView.text = formatDuration(durationMs)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mediaTypeTextView.text = ""
                } finally {
                    retriever.release()
                }
            } else {
                mediaTypeTextView.text = ""
                mediaTypeTextView.visibility = View.GONE
            }
            itemView.setOnClickListener {
                onItemClick(media)
            }
        }
        private fun formatDuration(durationMs: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
    fun updateData(newList: List<MediaEntity>) {
        mediaList=newList
        notifyDataSetChanged()
    }
}