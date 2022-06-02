package com.tsu.sudokugame.controler.helper

import androidx.annotation.DrawableRes
import com.tsu.sudokugame.ui.view.R
import java.util.ArrayList

enum class CreateSudokuButtonType(@param:DrawableRes val resID: Int) {
    Unspecified(R.drawable.ic_accessibility_black_48dp),
    Do(R.drawable.ic_redo_black_48dp), Undo(R.drawable.ic_undo_black_48dp), Spacer(
        R.drawable.ic_accessibility_black_48dp
    ),
    Delete(R.drawable.ic_delete_black_48dp),
    Finalize(R.drawable.ic_finalize);

    companion object {
        @JvmStatic
        val specialButtons: List<CreateSudokuButtonType>
            get() {
                val result = ArrayList<CreateSudokuButtonType>()
                result.add(Undo)
                result.add(Do)
                result.add(Finalize)
                result.add(Delete)
                return result
            }


    }
}