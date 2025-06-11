package com.beautycam.hdcam.photoeditor.utils;

import android.Manifest;
import android.os.Build;

public class Default {
    //about app
    public static final String EMAIL = "russellwilkey062969@gmail.com";
    public static final String SUBJECT = "Feedback: Beauty Camera HD: Photo Editor";
    public static final String PRIVACY_POLICY = "https://sites.google.com/view/beauty-camera-hd-photo-editor/home";
    //Name permission
    public static final String[] STORAGE_PERMISSION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}
            : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String[] VIDEO_RECORD_PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO
    };
    public static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};

}
