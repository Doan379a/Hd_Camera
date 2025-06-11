package com.beautycam.hdcam.photoeditor.sharePreferent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SharePrefUtils {
    private static SharedPreferences mSharePref;

    public static void init(Context context) {
        if (mSharePref == null) {
            mSharePref = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
    public static boolean isRated(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("rated", false);
    }

    public static void forceRated(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("rated", true);
        editor.commit();
    }
    public static boolean isGoToMain(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("GoToMain", false);
    }

    public static void forceGoToMain(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("GoToMain", true);
        editor.commit();
    }
    public static int getCountOpenApp(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getInt("counts", 1);
    }

    public static void increaseCountOpenApp(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putInt("counts", pre.getInt("counts", 1) + 1);
        editor.commit();
    }
    public static boolean isSound(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("Sound", true);
    }
    public static boolean isVibration(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("Vibration", true);
    }
    public static boolean isTutorial(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("Tutorial", false);
    }

    public static void forceTutorial(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("Tutorial", true);
        editor.commit();
    }

}
