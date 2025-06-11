package com.beautycam.hdcam.photoeditor.library.zoomimg.views.interfaces;

import androidx.annotation.NonNull;

import com.beautycam.hdcam.photoeditor.library.zoomimg.animation.ViewPositionAnimator;

/**
 * Common interface for views supporting position animation.
 */
public interface AnimatorView {

    /**
     * @return {@link ViewPositionAnimator} instance to control animation from other view position.
     */
    @NonNull
    ViewPositionAnimator getPositionAnimator();

}
