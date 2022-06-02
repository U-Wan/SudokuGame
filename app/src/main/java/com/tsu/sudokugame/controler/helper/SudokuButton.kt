package com.tsu.sudokugame.controler.helper

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton

class SudokuButton(context: Context?, attrs: AttributeSet?) : AppCompatButton(
    context!!, attrs
) {
    var value = -1
    var type = SudokuButtonType.Unspecified
}
class CreateSudokuSpecialButton(context: Context?, attrs: AttributeSet?) : AppCompatImageButton(
    context!!, attrs
) {
    var value = -1
    var type = CreateSudokuButtonType.Unspecified
}
class SudokuSpecialButton(context: Context?, attrs: AttributeSet?) : AppCompatImageButton(
    context!!, attrs
) {
    var value = -1
    var type = SudokuButtonType.Unspecified
}