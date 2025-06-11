package com.hypersoft.pzlayout.slant

import android.graphics.PointF

class CrossoverPointF : PointF {

    // Reference to the horizontal slant line associated with this crossover point.
    @JvmField
    var horizontal: SlantLine? = null

    // Reference to the vertical slant line associated with this crossover point.
    @JvmField
    var vertical: SlantLine? = null

    // Default constructor that initializes a CrossoverPointF instance.
    constructor()

    // Constructor that initializes a CrossoverPointF with specific x and y coordinates.
    constructor(x: Float, y: Float) {
        this.x = x // Set the x coordinate
        this.y = y // Set the y coordinate
    }

    // Constructor that initializes a CrossoverPointF with specific slant lines.
    constructor(horizontal: SlantLine?, vertical: SlantLine?) {
        this.horizontal = horizontal // Set the horizontal slant line
        this.vertical = vertical // Set the vertical slant line
    }

    /**
     * Updates the coordinates of this crossover point by calculating the
     * intersection of the horizontal and vertical slant lines.
     * If either slant line is null, the update is not performed.
     */
    fun update() {
        SlantUtils.intersectionOfLines(this, horizontal ?: return, vertical ?: return)
    }
}

