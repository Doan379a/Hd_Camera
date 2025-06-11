package com.beautycam.hdcam.photoeditor.ui.intro

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.beautycam.hdcam.photoeditor.ui.intro.fragment.Fragment1
import com.beautycam.hdcam.photoeditor.ui.intro.fragment.Fragment2
import com.beautycam.hdcam.photoeditor.ui.intro.fragment.Fragment3
import com.beautycam.hdcam.photoeditor.ui.intro.fragment.Fragment4


class IntroAdapter(fragmentManager: FragmentManager, private val activity: Activity) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
//        if (SharePrefRemote.get_config(
//                activity, SharePrefRemote.native_introfull
//            ) && AdsConsentManager.getConsentResult(activity) && SharePrefRemote.get_config(
//                activity, SharePrefRemote.ads_visibility
//            ) && isNetworkAvailable()
//        ) {
//            return when (position) {
//                0 -> com.beautycam.hdcam.photoeditor.ui.intro.fragment.Fragment1()
//                1 -> Fragment2()
//                2 -> FragmentAds()
//                3 -> Fragment3()
//                4 -> Fragment4()
//                else -> {
//                    com.beautycam.hdcam.photoeditor.ui.intro.fragment.Fragment1()
//                }
//            }
//        } else {
            return when (position) {
                0 -> Fragment1()
                1 -> Fragment2()
                2 -> Fragment3()
                3 -> Fragment4()
                else -> {
                    Fragment1()
                }
//            }
        }
    }


    override fun getCount(): Int {
        return 4
        //        if (SharePrefRemote.get_config(
//                activity, SharePrefRemote.native_introfull
//            ) && AdsConsentManager.getConsentResult(activity) && SharePrefRemote.get_config(
//                activity, SharePrefRemote.ads_visibility
//            ) && isNetworkAvailable()
//        ) 5 else 4

    }

//    private fun isNetworkAvailable(): Boolean {
//        val connectivityManager =
//            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork = connectivityManager.activeNetworkInfo
//        return activeNetwork != null && activeNetwork.isConnected
//    }

}
