package com.tsu.sudokugame.model.game.logic

import java.lang.StringBuilder

class LogItem {
    var round = 0
        private set


    var type: LogType? = null
        private set

    private var value = 0
    var position = 0
        private set

    constructor(r: Int, t: LogType) {
        init(r, t, 0, -1)
    }

    constructor(r: Int, t: LogType, v: Int, p: Int) {
        init(r, t, v, p)
    }

    private fun init(r: Int, t: LogType, v: Int, p: Int) {
        round = r
        type = t
        value = v
        position = p
    }

    fun print() {
        print(toString())
    }

    val row: Int
        get() = if (position <= -1) -1 else createboard.cellToRow(position) + 1

    val column: Int
        get() = if (position <= -1) -1 else createboard.cellToColumn(position) + 1

    fun getValue(): Int {
        return if (value <= 0) -1 else value
    }
    val description: String
        get() {
            val sb = StringBuilder()
            sb.append("Round: ").append(round)
            sb.append(" - ")
            sb.append(type!!.description)
            if (value > 0 || position > -1) {
                sb.append(" (")
                if (position > -1) {
                    sb.append("Row: ").append(row).append(" - Column: ").append(column)
                }
                if (value > 0) {
                    if (position > -1) sb.append(" - ")
                    sb.append("Value: ").append(getValue())
                }
                sb.append(")")
            }
            return sb.toString()
        }

    override fun toString(): String {
        return description
    }
}