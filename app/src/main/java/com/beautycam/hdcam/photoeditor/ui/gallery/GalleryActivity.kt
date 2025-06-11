package com.beautycam.hdcam.photoeditor.ui.gallery

import android.graphics.Color
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.data.viewmodel.PictureViewModel
import com.beautycam.hdcam.photoeditor.databinding.ActivityGalleryBinding
import androidx.activity.viewModels
import com.beautycam.hdcam.photoeditor.data.entity.MediaType
import com.beautycam.hdcam.photoeditor.ui.gallery.image.PictureGalleryFragment
import com.beautycam.hdcam.photoeditor.ui.gallery.image.pager.ImagePagerGalleryFragment
import com.beautycam.hdcam.photoeditor.ui.gallery.video.VideoGalleryFragment
import com.beautycam.hdcam.photoeditor.ui.gallery.video.pager.VideoPagerGalleryFragment
import com.beautycam.hdcam.photoeditor.utils.LoadImageUtils.getMediaFromHD
import com.beautycam.hdcam.photoeditor.widget.tap

class GalleryActivity : BaseActivity<ActivityGalleryBinding>() {

    private lateinit var mDataTabList: List<String>
    private val pictureViewModel: PictureViewModel by viewModels()

    private var myPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(getTagDebug(), "onPageSelected: $position")
                checkTab(position)
            }
        }

    override fun setViewBinding(): ActivityGalleryBinding {
        return ActivityGalleryBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.imgBack.tap {
            finish()
        }
        mDataTabList = listOf(getString(R.string.picture), getString(R.string.video))
        binding.viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = mDataTabList.size
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> PictureGalleryFragment()
                    1 -> VideoGalleryFragment()
                    else-> PictureGalleryFragment()
                }
            }
        }
        binding.tabTitle1.text = mDataTabList[0]
        binding.tabTitle2.text = mDataTabList[1]
        val mediaList = getMediaFromHD(this)
        Log.d(getTagDebug(),"${mediaList.size}-mediaList, $mediaList")
        val pictureMedia = mediaList.filter { it.mediaType == MediaType.IMAGE }.map { it.filePath }
        val videoMedia = mediaList.filter { it.mediaType == MediaType.VIDEO }.map { it.filePath }

        Log.d(getTagDebug(),"${pictureMedia.size}---pictureMedia, $pictureMedia")
        Log.d(getTagDebug()," ${videoMedia.size}---videoMedia, $videoMedia")

        pictureViewModel.setListPicture(pictureMedia)
        pictureViewModel.setListVideo(videoMedia)
        binding.viewPager2.registerOnPageChangeCallback(myPageChangeCallback)

        pictureViewModel.clickItemImage.observe(this) {
            if (it.first != null && it.second != null){
                openMediaPagerFragment(it.first, it.second,false)
            }
        }

        pictureViewModel.clickItemVideo.observe(this) {
            if (it.first != null && it.second != null){
                openMediaPagerFragment(it.first, it.second,true)
            }

        }
    }

    override fun viewListener() {
        binding.tabTitle1.setOnClickListener{
            checkTab(0)
            binding.viewPager2.currentItem = 0
        }
        binding.tabTitle2.setOnClickListener{
            checkTab(1)
            binding.viewPager2.currentItem = 1
        }
    }
    fun checkTab(position: Int){
        when(position){
            0->{
                binding.tabTitle1.setTextColor(Color.parseColor("#FFFFFF"))
                binding.tabTitle2.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_selected_photo, 0, 0, 0)
                binding.tabTitle2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video2, 0, 0, 0)
                binding.tabTitle1.setBackgroundResource(R.drawable.bg_select_tab)
                binding.tabTitle2.setBackgroundResource(R.drawable.bg_tab)
            }
            1->{
                binding.tabTitle1.setTextColor(Color.parseColor("#666666"))
                binding.tabTitle2.setTextColor(Color.parseColor("#FFFFFF"))
                binding.tabTitle1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo, 0, 0, 0)
                binding.tabTitle2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_selected_video2, 0, 0, 0)
                binding.tabTitle1.setBackgroundResource(R.drawable.bg_tab)
                binding.tabTitle2.setBackgroundResource(R.drawable.bg_select_tab)
            }
        }
    }
    override fun dataObservable() {

    }

    private fun openMediaPagerFragment(mediaList: List<String>, startIndex: Int, isVideo: Boolean) {
        val fragment = if (isVideo) {
            VideoPagerGalleryFragment(mediaList.toMutableList(), startIndex)
        } else {
            ImagePagerGalleryFragment(mediaList.toMutableList(), startIndex)
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

}
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_media)
//
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)

//        mediaViewModel.unlockedMedia.observe(this) { mediaList ->
//            mediaAdapter = MediaAdapter(mediaList, ::lockMedia, ::unlockMedia)
//            recyclerView.adapter = mediaAdapter
//        }
//
//        mediaViewModel.fetchUnlockedMedia()
//    }
//    private fun lockMedia(media: MediaEntity) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val cacheDir = getCacheDirectory(this@GalleryActivity)
//            val destinationPath = "$cacheDir/${File(media.filePath).name}"
//            if (moveFile(media.filePath, destinationPath)) {
//                media.filePath = destinationPath
////                mediaViewModel.lockMedia(media)
//            }
//        }
//    }
//
//    private fun unlockMedia(media: MediaEntity) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val saveDir = getSaveDirectory(this@GalleryActivity)
//            val destinationPath = "$saveDir/${File(media.filePath).name}"
//            if (moveFile(media.filePath, destinationPath)) {
//                media.filePath = destinationPath
////                mediaViewModel.unlockMedia(media)
//            }
//        }
//    }

//}
