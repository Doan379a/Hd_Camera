package com.beautycam.hdcam.photoeditor.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.dialog.DialogFeedback
import com.beautycam.hdcam.photoeditor.dialog.RatingDialog
import com.beautycam.hdcam.photoeditor.ui.pass.SecurityQuestionActivity


class HelperMenu(private val activity: Activity) {

    fun showDialogRate(isFinishActivity: Boolean) {
        RatingDialog(activity, isFinishActivity).show()
    }

    fun showShareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name))
        var shareMessage = activity.getString(R.string.app_name)
        shareMessage =
            "$shareMessage \nhttps://play.google.com/store/apps/details?id=${activity.packageName}"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        activity.startActivity(Intent.createChooser(shareIntent, "Share to"))
    }

    fun showDialogFeedback() {
        DialogFeedback(activity).show()
    }

    fun showPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Default.PRIVACY_POLICY))
        activity.startActivity(intent)
    }

}