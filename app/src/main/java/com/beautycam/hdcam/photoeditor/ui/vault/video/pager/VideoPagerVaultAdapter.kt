package com.beautycam.hdcam.photoeditor.ui.vault.video.pager

import android.util.Log
import android.util.SparseArray
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VideoPagerVaultAdapter(
    fragmentActivity: FragmentActivity,
    private val videoList: List<String>
) : FragmentStateAdapter(fragmentActivity) {
    private val fragmentMap = SparseArray<ItemVideoPlayerVaultFragment>()
    override fun getItemCount(): Int = videoList.size

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        Log.d("VideoPagerAdapter", "createFragment() - Position: $position")
        val fragment = ItemVideoPlayerVaultFragment.newInstance(videoList[position])
        fragmentMap.put(position, fragment)
        return fragment
    }
    fun getFragment(position: Int): ItemVideoPlayerVaultFragment? {
        return fragmentMap[position]
    }
    override fun getItemId(position: Int): Long {
        return videoList[position].hashCode().toLong()
    }
    override fun containsItem(itemId: Long): Boolean {
        return videoList.any { it.hashCode().toLong() == itemId }
    }
}