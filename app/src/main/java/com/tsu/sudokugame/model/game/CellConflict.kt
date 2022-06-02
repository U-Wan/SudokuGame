package com.tsu.sudokugame.model.game

import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator
import java.lang.StringBuilder

class CellConflict : Parcelable {

    var cell1: GameCell? = null
        private set
    var cell2: GameCell? = null
        private set
    val rowCell1: Int
        get() = cell1!!.row
    val rowCell2: Int
        get() = cell2!!.row
    val colCell1: Int
        get() = cell1!!.col
    val colCell2: Int
        get() = cell2!!.col

    constructor(first: GameCell?, second: GameCell?) {
        cell1 = first
        cell2 = second
    }

    operator fun contains(c: GameCell): Boolean {
        return cell1 == c || cell2 == c
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CellConflict) {
            return false
        }
        return if (!(cell1 == other.cell1 && cell2 == other.cell2
                    || cell1 == other.cell2 && cell2 == other.cell1)
        ) {
            false
        } else true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[Conflict ")
        sb.append(cell1.toString())
        sb.append(" ")
        sb.append(cell2.toString())
        sb.append("]")
        return sb.toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(cell1, 0)
        dest.writeParcelable(cell2, 0)
    }

    private constructor(`in`: Parcel) {
        cell1 = `in`.readParcelable(GameCell::class.java.classLoader)
        cell2 = `in`.readParcelable(GameCell::class.java.classLoader)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<CellConflict?> = object : Creator<CellConflict?> {
            override fun createFromParcel(`in`: Parcel): CellConflict? {
                return CellConflict(`in`)
            }

            override fun newArray(size: Int): Array<CellConflict?> {
                return arrayOfNulls(size)
            }
        }
    }
}