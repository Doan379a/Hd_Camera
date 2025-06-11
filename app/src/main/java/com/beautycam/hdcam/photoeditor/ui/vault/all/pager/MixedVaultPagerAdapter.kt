package com.beautycam.hdcam.photoeditor.ui.vault.all.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.beautycam.hdcam.photoeditor.data.entity.MediaEntity
import com.beautycam.hdcam.photoeditor.data.entity.MediaType
import com.beautycam.hdcam.photoeditor.ui.vault.video.pager.ItemVideoPlayerVaultFragment

class MixedVaultPagerAdapter(
    fragment: Fragment,
    private val mediaList: MutableList<MediaEntity>
) : FragmentStateAdapter(fragment) {
    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = mediaList.size

    override fun createFragment(position: Int): Fragment {
        val media = mediaList[position]
        val fragment = when (media.mediaType) {
            MediaType.IMAGE -> ItemImageVaultFragment.newInstance(media.filePath)
            MediaType.VIDEO -> ItemVideoPlayerVaultFragment.newInstance(media.filePath)
        }
        fragmentMap[position] = fragment
        return fragment
    }

    fun getFragment(position: Int): Fragment? {
        return fragmentMap[position]
    }

    override fun getItemId(position: Int): Long {
        return mediaList[position].filePath.hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return mediaList.any { it.filePath.hashCode().toLong() == itemId }
    }
}
