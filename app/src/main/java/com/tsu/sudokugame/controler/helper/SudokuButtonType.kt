package com.tsu.sudokugame.controler.helper

import androidx.annotation.DrawableRes
import com.tsu.sudokugame.ui.view.R
import java.util.ArrayList

enum class SudokuButtonType(@param:DrawableRes val resID: Int) {
    Unspecified(R.drawable.ic_accessibility_black_48dp),
    Value(R.drawable.ic_accessibility_black_48dp),
    Do(R.drawable.ic_redo_black_48dp), Undo(R.drawable.ic_undo_black_48dp),
    Hint(R.drawable.ic_lightbulb_outline_black_48dp),
    Spacer(R.drawable.ic_accessibility_black_48dp),
    Delete(R.drawable.ic_delete_black_48dp);

    companion object {
        @JvmStatic
        val specialButtons: List<SudokuButtonType>
            get() {
                val result = ArrayList<SudokuButtonType>()
                result.add(Undo)
                result.add(Do)
                result.add(Hint)
                result.add(Delete)
                return result
            }

        fun getName(type: SudokuButtonType?): String {
            return when (type) {
                Do -> "Do"
                Undo -> "Un"
                Hint -> "Hnt"
                Spacer -> ""
                Delete -> "Del"
                else -> "NotSet"
            }
        }
    }
}