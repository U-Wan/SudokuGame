package com.tsu.sudokugame.controler.helper

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class SudokuButton(context: Context?, attrs: AttributeSet?) : AppCompatButton(
    context!!, attrs
) {
    var value = -1
    var type = SudokuButtonType.Unspecified
}