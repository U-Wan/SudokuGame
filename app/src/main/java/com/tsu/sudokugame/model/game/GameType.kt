/*
 This file is part of Privacy Friendly Sudoku.

 Privacy Friendly Sudoku is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Sudoku is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Sudoku. If not, see <http://www.gnu.org/licenses/>.
 */
package com.tsu.sudokugame.model.game

import android.os.Parcelable
import com.tsu.sudokugame.ui.view.R
import android.os.Parcel
import android.os.Parcelable.Creator
import java.util.*

enum class GameType(
    var size: Int,
    var sectionHeight: Int,
    var sectionWidth: Int, // change pictures for unsepc x9x9 and hyper 9x9 as soon as available
    var stringResID: Int,
    var resIDImage: Int
) : Parcelable {
    Unspecified(1, 1, 1, R.string.gametype_unspecified, R.drawable.icon_default_6x6), Default_9x9(
        9,
        3,
        3,
        R.string.gametype_default_9x9,
        R.drawable.icon_default_9x9
    ),
    Default_12x12(
        12,
        3,
        4,
        R.string.gametype_default_12x12,
        R.drawable.icon_default_12x12
    ),
    Default_6x6(6, 2, 3, R.string.gametype_default_6x6, R.drawable.icon_default_6x6), X_9x9(
        9,
        3,
        3,
        R.string.gametype_x_9x9,
        R.drawable.icon_default_9x9
    ),
    Hyper_9x9(9, 3, 3, R.string.gametype_hyper_9x9, R.drawable.icon_default_9x9);

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
        dest.writeInt(stringResID)
        dest.writeInt(sectionWidth)
        dest.writeInt(sectionHeight)
        dest.writeInt(size)
        dest.writeInt(resIDImage)
    }

    companion object {
        @JvmStatic
        val validGameTypes: LinkedList<GameType>
            get() {
                val result = LinkedList<GameType>()
                result.add(Default_6x6)
                result.add(Default_9x9)
                result.add(Default_12x12)
                return result
            }
        @JvmField
        val CREATOR: Creator<GameType?> = object : Creator<GameType?> {
            override fun createFromParcel(`in`: Parcel): GameType {
                val g = values()[`in`.readInt()]
                g.stringResID = `in`.readInt()
                g.sectionWidth = `in`.readInt()
                g.sectionHeight = `in`.readInt()
                g.size = `in`.readInt()
                g.resIDImage = `in`.readInt()
                return g
            }

            override fun newArray(size: Int): Array<GameType?> {
                return arrayOfNulls(size)
            }
        }
    }
}