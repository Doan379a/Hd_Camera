package com.beautycam.hdcam.photoeditor.data

import android.content.Context
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.model.FilterModel
import com.beautycam.hdcam.photoeditor.model.GridModel

object DataApp {
    fun getFilterList(context: Context) : List<FilterModel> {
        return listOf(
            FilterModel(0, R.drawable.ic_none, 1f,1f),
            FilterModel(1, R.drawable.img_frame_1, 0.78f,0.65f),
            FilterModel(2, R.drawable.img_frame_2, 0.78f,0.78f),
            FilterModel(3, R.drawable.img_frame_3, 0.54f,0.45f),
            FilterModel(4, R.drawable.img_frame_4, 0.7f,0.68f),
            FilterModel(5, R.drawable.img_frame_5, 0.76f,0.8f),
            FilterModel(6, R.drawable.img_frame_6, 0.78f,0.91f),
            FilterModel(7, R.drawable.img_frame_7, 0.52f,0.6f),
            FilterModel(8, R.drawable.img_frame_8, 0.62f,0.4f),
            FilterModel(9, R.drawable.img_frame_9, 0.78f,0.7f),
            FilterModel(10, R.drawable.img_frame_10, 0.8f,0.69f),
        )
    }

    fun getGridList(context: Context) : List<GridModel> {
        return listOf(
            GridModel(0, "None", R.drawable.img_grid_3x3),
            GridModel(1, "3x3", R.drawable.img_grid_3x3),
            GridModel(2, "Phi 3x3", R.drawable.img_grid_phi3x3),
            GridModel(3, "4x2", R.drawable.img_grid_4x2),
            GridModel(4, "Cross", R.drawable.img_grid_cross),
            GridModel(5, "Diagonal", R.drawable.img_grid_diagonal),
            GridModel(6, "Tria.1", R.drawable.img_grid_triangle1),
            GridModel(7, "Tria.2", R.drawable.img_grid_triangle2),

        )
    }
}