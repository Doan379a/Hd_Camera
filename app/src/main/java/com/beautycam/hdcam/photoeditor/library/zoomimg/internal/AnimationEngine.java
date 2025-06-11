package com.beautycam.hdcam.photoeditor.library.zoomimg.internal;

import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;

import com.beautycam.hdcam.photoeditor.library.zoomimg.internal.Fps;
import com.beautycam.hdcam.photoeditor.library.zoomimg.internal.GestureDebug;

public abstract class AnimationEngine implements Runnable {

    private static final long FRAME_TIME = 10L;

    private final View view;
    private final com.beautycam.hdcam.photoeditor.library.zoomimg.internal.Fps fps;

    public AnimationEngine(@NonNull View view) {
        this.view = view;
        this.fps = GestureDebug.isDebugFps() ? new com.beautycam.hdcam.photoeditor.library.zoomimg.internal.Fps() : null;
    }

    @Override
    public final void run() {
        boolean continueAnimation = onStep();

        if (fps != null) {
            fps.step();
            if (!continueAnimation) {
                fps.stop();
            }
        }

        if (continueAnimation) {
            scheduleNextStep();
        }
    }

    public abstract boolean onStep();

    private void scheduleNextStep() {
        view.removeCallbacks(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.postOnAnimationDelayed(this, FRAME_TIME);
        } else {
            view.postDelayed(this, FRAME_TIME);
        }
    }

    public void start() {
        if (fps != null) {
            fps.start();
        }

        scheduleNextStep();
    }

}
