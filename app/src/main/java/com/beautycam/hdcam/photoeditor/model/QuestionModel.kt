package com.beautycam.hdcam.photoeditor.model

import android.content.Context
import com.beautycam.hdcam.photoeditor.R

data class QuestionModel(val id : Int, val question : String){
    override fun toString(): String = question
}
fun getListOfQuestions(context: Context): List<QuestionModel> {
    return listOf(
        QuestionModel(1, context.getString(R.string.question_1)),
        QuestionModel(2, context.getString(R.string.question_2)),
        QuestionModel(3, context.getString(R.string.question_3)),
        QuestionModel(4, context.getString(R.string.question_4)),
        QuestionModel(5, context.getString(R.string.question_5))
    )
}
