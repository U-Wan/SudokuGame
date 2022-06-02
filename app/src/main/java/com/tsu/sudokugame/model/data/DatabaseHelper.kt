package com.tsu.sudokugame.model.data

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.tsu.sudokugame.model.game.GameDifficulty
import kotlin.jvm.Synchronized
import com.tsu.sudokugame.model.game.GameType
import java.util.*

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(LevelColumns.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Synchronized
    fun getLevels(difficulty: GameDifficulty?, gameType: GameType?): List<Level> {
        require(!(difficulty == null || gameType == null)) { "Arguments may not be null" }
        val levelList: MutableList<Level> = LinkedList()
        val database = writableDatabase
        val selection = LevelColumns.DIFFICULTY + " = ? AND " + LevelColumns.GAMETYPE + " = ?"
        val selectionArgs = arrayOf(difficulty.name, gameType.name)

        val c = database.query(
            LevelColumns.TABLE_NAME,
            LevelColumns.PROJECTION,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        if (c != null) {
            while (c.moveToNext()) {
                levelList.add(LevelColumns.getLevel(c))
            }
        }
        c!!.close()
        return levelList
    }

    @Synchronized
    fun getLevel(difficulty: GameDifficulty?, gameType: GameType?): Level {
        val levelList = getLevels(difficulty, gameType)
        require(levelList.size != 0) { "There is no level" }
        return levelList[0]
    }

    @Synchronized
    fun deleteLevel(id: Int) {
        val database = writableDatabase
        val selection = LevelColumns.DIFFICULTY + " = ?"
        val selectionArgs = arrayOf(id.toString() + "")
        database.delete(LevelColumns.TABLE_NAME, selection, selectionArgs)
    }

    @Synchronized
    fun addLevel(level: Level?): Long {
        val database = writableDatabase
        return database.insert(LevelColumns.TABLE_NAME, null, LevelColumns.getValues(level!!))
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Database.db"
    }
}