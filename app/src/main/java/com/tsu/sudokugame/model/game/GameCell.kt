package com.tsu.sudokugame.model.game

import android.os.Parcelable
import com.tsu.sudokugame.model.game.listener.IModelChangedListener
import kotlin.jvm.JvmOverloads
import kotlin.Throws
import android.os.Parcel
import android.os.Parcelable.Creator
import java.lang.StringBuilder
import java.util.*

class GameCell : Cloneable, Parcelable {
    var row = 0
        private set
    var col = 0
        private set
    var value = 0
    var isFixed = false
        private set
    var noteCount = 0
        private set
    var notes: BooleanArray?=null
        private set
    private var size = 0
    private var modelChangedListeners: MutableList<IModelChangedListener> = LinkedList()

    @JvmOverloads
    constructor(row: Int, col: Int, size: Int, `val`: Int = 0) {
        this.row = row
        this.col = col
        this.size = size
        if (0 < `val` && `val` <= size) {
            setValuee(`val`)
            isFixed = true
        } else {
            setValuee(0)
            isFixed = false
        }
    }

    fun setValuee(`val`: Int) {
        if (!isFixed) {
            deleteNotes()
            value = `val`
            notifyListeners()
        }
    }


    fun toggleNote(`val`: Int) {
        if (!isFixed) {
            noteCount = if (notes!![`val` - 1]) noteCount - 1 else noteCount + 1
            notes!![`val` - 1] = !notes!![`val` - 1]
            notifyListeners()
        }
    }

    fun setNote(`val`: Int) {
        if (!isFixed) {
            noteCount = if (notes!![`val` - 1]) noteCount else noteCount + 1
            notes!![`val` - 1] = true
            notifyListeners()
        }
    }

    fun deleteNote(`val`: Int) {
        if (!isFixed) {
            noteCount = if (notes!![`val` - 1]) noteCount - 1 else noteCount
            notes!![`val` - 1] = false
            notifyListeners()
        }
    }

    fun deleteNotes() {
        noteCount = 0
        notes = BooleanArray(size)
        notifyListeners()
    }

    fun hasValue(): Boolean {
        return value > 0
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GameCell) return false
        if (other.col != col) return false
        if (other.row != row) return false
        if (other.isFixed != isFixed) return false
        if (other.value != this.value) return false
        if (other.notes!!.size != notes!!.size) return false
        for (i in notes!!.indices) {
            if (other.notes!![i] != notes!![i]) return false
        }
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[")
        if (value == 0) {
            sb.append("{")
            var addedNotes = false
            for (i in 0 until size) {
                if (notes!![i]) {
                    if (addedNotes) {
                        sb.append(" ,")
                    }
                    sb.append(i + 1)
                    addedNotes = true
                }
            }
            sb.append("}")
        } else {
            sb.append(value)
        }
        sb.append(" ")
        sb.append("(")
        sb.append(row)
        sb.append("|")
        sb.append(col)
        sb.append(")")
        sb.append("]")
        return sb.toString()
    }

    fun reset(): Boolean {
        if (isFixed) {
            return false
        }
        setValuee(0)
        return true
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): GameCell {
        val clone = super.clone() as GameCell
        // keep listeners .. so we can just replace the board and still have the listeners
        clone.notes = if (notes == null) null else Arrays.copyOf(notes, notes!!.size)
        return clone
    }

    fun registerOnModelChangeListener(listener: IModelChangedListener) {
        if (!modelChangedListeners.contains(listener)) {
            modelChangedListeners.add(listener)
        }
    }

    fun removeOnModelChangeListener(listener: IModelChangedListener) {
        if (modelChangedListeners.contains(listener)) {
            modelChangedListeners.remove(listener)
        }
    }

    fun notifyListeners() {
        for (m in modelChangedListeners) {
            m.onModelChange(this)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(row)
        dest.writeInt(col)
        dest.writeInt(value)
        dest.writeInt(size)
        dest.writeInt(if (isFixed) 1 else 0)
        dest.writeInt(noteCount)
        dest.writeBooleanArray(notes)
    }

    private constructor(`in`: Parcel) {
        row = `in`.readInt()
        col = `in`.readInt()
        value = `in`.readInt()
        size = `in`.readInt()
        isFixed = `in`.readInt() == 1
        noteCount = `in`.readInt()
        notes = BooleanArray(size)
        `in`.readBooleanArray(notes!!)
        removeAllListeners()
    }

    fun removeAllListeners() {
        modelChangedListeners = LinkedList()
    }

    companion object {
        @JvmField
        val CREATOR: Creator<GameCell?> = object : Creator<GameCell?> {
            override fun createFromParcel(`in`: Parcel): GameCell? {
                return GameCell(`in`)
            }

            override fun newArray(size: Int): Array<GameCell?> {
                return arrayOfNulls(size)
            }
        }
    }
}