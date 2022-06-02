package com.tsu.sudokugame.model.game

import java.lang.StringBuilder
import java.util.ArrayList

class CellConflictList : ArrayList<CellConflict?>() {
    override fun add(`object`: CellConflict?): Boolean {
        return if (!contains(`object`)) {
            super.add(`object`)
        } else false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[List ")
        for (i in 0 until size) {
            sb.append(get(i).toString())
            if (i + 1 < size) {
                sb.append(", ")
            }
        }
        sb.append("]")
        return sb.toString()
    }
}