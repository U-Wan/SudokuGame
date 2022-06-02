package com.tsu.sudokugame.controler.helper

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

class CreateSudokuSpecialButton(context: Context?, attrs: AttributeSet?) : AppCompatImageButton(
    context!!, attrs
) {
    var value = -1
    var type = CreateSudokuButtonType.Unspecified
}