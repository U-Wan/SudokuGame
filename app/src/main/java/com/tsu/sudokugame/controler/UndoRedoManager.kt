package com.tsu.sudokugame.controler

import android.os.Parcelable
import com.tsu.sudokugame.model.game.GameBoard
import android.os.Parcel
import android.os.Parcelable.Creator
import java.util.*

class UndoRedoManager : Parcelable {
    private var activeState = 0
    private val states = LinkedList<GameBoard>()

    constructor(initState: GameBoard) {
        try {
            states.addLast(initState.clone())
            activeState = 0
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }

    val isUnDoAvailable: Boolean
        get() = activeState > 0 && states.size > 1
    val isRedoAvailable: Boolean
        get() = activeState < states.size - 1

    fun UnDo(): GameBoard? {
        return if (isUnDoAvailable) {
            states[--activeState]
        } else {
            null
        }
    }

    fun ReDo(): GameBoard? {
        return if (isRedoAvailable) {
            states[++activeState]
        } else {
            null
        }
    }

    fun addState(gameBoard: GameBoard) {
        if (gameBoard == states[activeState]) {
            return
        }
        val deleteList = LinkedList<GameBoard>()
        for (i in states.indices) {
            if (i > activeState) {                  // 3 states // state 1 is active // means 0,[1],2
                // delete rest of the list          // i > activeState // i > 1 // so i = 2 will be deleted // 0 can not be deleted
                deleteList.add(states[i])
            }
        }
        for (g in deleteList) {
            states.removeLastOccurrence(g)
        }

        // then append the current state
        try {
            val board = gameBoard.clone()
            states.addLast(board)
            activeState = states.size - 1
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(activeState)
        out.writeTypedList(states)
    }

    private constructor(`in`: Parcel) {
        activeState = `in`.readInt()
        `in`.readTypedList(states, GameBoard.CREATOR)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<UndoRedoManager?> = object : Creator<UndoRedoManager?> {
            override fun createFromParcel(`in`: Parcel): UndoRedoManager? {
                return UndoRedoManager(`in`)
            }

            override fun newArray(size: Int): Array<UndoRedoManager?> {
                return arrayOfNulls(size)
            }
        }
    }
}