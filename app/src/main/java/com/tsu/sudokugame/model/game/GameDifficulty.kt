package com.tsu.sudokugame.model.game

import androidx.annotation.StringRes
import android.os.Parcelable
import com.tsu.sudokugame.ui.view.R
import android.os.Parcel
import android.os.Parcelable.Creator
import java.util.*

enum class GameDifficulty
    (@param:StringRes var stringResID: Int) : Parcelable {
    Unspecified(R.string.gametype_unspecified), Easy(R.string.difficulty_easy), Moderate(R.string.difficulty_moderate), Hard(
        R.string.difficulty_hard
    ),
    Challenge(R.string.difficulty_challenge);

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
        dest.writeInt(stringResID)
    }

    companion object {
        @JvmStatic
        val validDifficultyList: LinkedList<GameDifficulty>
            get() {
                val validList = LinkedList<GameDifficulty>()
                validList.add(Easy)
                validList.add(Moderate)
                validList.add(Hard)
                validList.add(Challenge)
                return validList
            }
        @JvmField
        val CREATOR: Creator<GameDifficulty?> = object : Creator<GameDifficulty?> {
            override fun createFromParcel(`in`: Parcel): GameDifficulty? {
                val g = values()[`in`.readInt()]
                g.stringResID = `in`.readInt()
                return g
            }

            override fun newArray(size: Int): Array<GameDifficulty?> {
                return arrayOfNulls(size)
            }
        }
    }
}