package com.beautycam.hdcam.photoeditor.ui.gallery.video.pager

import android.util.Log
import android.util.SparseArray
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.beautycam.hdcam.photoeditor.ui.vault.video.pager.ItemVideoPlayerVaultFragment

class VideoPagerGalleryAdapter(
    fragmentActivity: FragmentActivity,
    private val videoList: MutableList<String>
) : FragmentStateAdapter(fragmentActivity) {
    private val fragmentMap = SparseArray<ItemVideoPlayerGalleryFragment>()
    override fun getItemCount(): Int = videoList.size

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        Log.d("VideoPagerAdapter", "createFragment() - Position: $position")
        val fragment = ItemVideoPlayerGalleryFragment.newInstance(videoList[position])
        fragmentMap.put(position, fragment)
        return fragment
    }
    fun getFragment(position: Int): ItemVideoPlayerGalleryFragment? {
        return fragmentMap[position]
    }
    override fun getItemId(position: Int): Long {
        return videoList[position].hashCode().toLong()
    }
    override fun containsItem(itemId: Long): Boolean {
        return videoList.any { it.hashCode().toLong() == itemId }
    }
}