package com.tsu.sudokugame.model.data

import android.provider.BaseColumns

import com.tsu.sudokugame.model.game.GameType
import android.content.ContentValues
import android.database.Cursor

import android.provider.BaseColumns._ID
import com.tsu.sudokugame.controler.Symbol
import com.tsu.sudokugame.model.game.GameDifficulty
import java.lang.StringBuilder

object LevelColumns : BaseColumns {
    const val TABLE_NAME = "levels"
    const val DIFFICULTY = "level_difficulty"
    const val GAMETYPE = "level_gametype"
    const val PUZZLE = "level_puzzle"
    private const val TEXT_TYPE = " TEXT "
    private const val INTEGER_TYPE = " INTEGER "
    private const val COMMA_SEP = ","
    val PROJECTION = arrayOf(
        _ID,
        DIFFICULTY,
        GAMETYPE,
        PUZZLE
    )
    var SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
            BaseColumns._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            DIFFICULTY + TEXT_TYPE + COMMA_SEP +
            GAMETYPE + TEXT_TYPE + COMMA_SEP +
            PUZZLE + TEXT_TYPE + " )"
    var SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME
    fun getLevel(c: Cursor): Level {
        val level = Level()

        // *** ID ***
        level.id = c.getInt(c.getColumnIndexOrThrow(BaseColumns._ID))

        // *** GAME TYPE ***
        val gameTypeString = c.getString(c.getColumnIndexOrThrow(GAMETYPE))
        val gameType = GameType.valueOf(gameTypeString)
        level.gameType = gameType

        // *** DIFFICULTY ***
        val difficultyString = c.getString(c.getColumnIndexOrThrow(DIFFICULTY))
        level.difficulty = GameDifficulty.valueOf(difficultyString)

        val puzzleString = c.getString(c.getColumnIndexOrThrow(PUZZLE))
        val puzzle = IntArray(gameType.size * gameType.size)
        require(puzzle.size == puzzleString.length) { "Saved level does not have the correct size." }
        for (i in 0 until puzzleString.length) {
            puzzle[i] = Symbol.getValue(Symbol.SaveFormat, puzzleString[i].toString()) + 1
        }
        level.puzzle = puzzle
        return level
    }

    fun getValues(record: Level): ContentValues {
        val values = ContentValues()
        if (record.id != -1) {
            values.put(BaseColumns._ID, record.id)
        }
        values.put(GAMETYPE, record.gameType.name)
        values.put(DIFFICULTY, record.difficulty.name)
        val sb = StringBuilder()
        for (i in 0 until record.puzzle.size) {
            if (record.puzzle[i] == 0) {
                sb.append(0)
            } else {
                sb.append(Symbol.getSymbol(Symbol.SaveFormat, record.puzzle[i] - 1))
            }
        }
        values.put(PUZZLE, sb.toString())
        return values
    }
}