package com.tsu.sudokugame.controler

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tsu.sudokugame.model.data.GameInfoContainer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException
import java.util.*

class GameStateManager(var context: Context, private val settings: SharedPreferences) {
    private var includesDaily = false
    fun loadGameStateInfo(): List<GameInfoContainer> {
        if (!settings.getBoolean("savesChanged", false)) {
            return list
        }
        val dir = context.getDir(SAVES_DIR, 0)
        val result = LinkedList<GameInfoContainer>()
        for (file in dir.listFiles()) {
            if (file.isFile) {
                val gic = GameInfoContainer()

                // load file
                val bytes = ByteArray(file.length().toInt())
                try {
                    val stream = FileInputStream(file)
                    try {
                        stream.read(bytes)
                    } finally {
                        stream.close()
                    }
                } catch (e: IOException) {
                    Log.e("File Manager", "Could not load game. IOException occured.")
                }
                val gameString = String(bytes)
                val values = gameString.split("/").toTypedArray()
                try {
                    require(values.size >= 4) { "Can not load game info. File seems to be damaged or incomplete." }
                    val id = file.name.substring(5, file.name.lastIndexOf("."))
                    var i = 0
                    gic.id = Integer.valueOf(id) // save_x.txt
                    gic.parseGameType(values[i++])
                    gic.parseTime(values[i++])
                    gic.parseDate(values[i++])
                    gic.parseDifficulty(values[i++])
                    gic.parseFixedValues(values[i++])
                    gic.parseSetValues(values[i++])
                    gic.parseNotes(values[i++])
                    gic.parseHintsUsed(values[i++])
                    if (values.size > i) {
                        gic.isCustom = true
                    }
                    if (gic.id == GameController.DAILY_SUDOKU_ID) {
                        includesDaily = true
                    }
                } catch (e: IllegalArgumentException) {
                    file.delete()
                    continue
                } catch (e: IndexOutOfBoundsException) {
                    file.delete()
                    continue
                }

                // davamatot listshi
                result.add(gic)
            }
        }
        val editor = settings.edit()
        editor.putBoolean("savesChanged", false)
        editor.commit()
        list = sortListByLastPlayedDate(result)
        val removeList = LinkedList<GameInfoContainer>()
        for (i in list.indices) {
            if (i >= MAX_NUM_OF_SAVED_GAMES && !includesDaily || i > MAX_NUM_OF_SAVED_GAMES) {
                deleteGameStateFile(list[i])
                removeList.add(list[i])
            }
        }
        for (gic in removeList) {
            list.remove(gic)
        }
        return list
    }

    fun deleteGameStateFile(gic: GameInfoContainer) {
        val dir = context.getDir(SAVES_DIR, 0)
        val file = File(dir, SAVE_PREFIX + gic.id + FILE_EXTENSION)
        file.delete()
        val editor = settings.edit()
        editor.putBoolean("savesChanged", true)
        editor.commit()
    }

    fun sortListByLastPlayedDate(list: LinkedList<GameInfoContainer>): LinkedList<GameInfoContainer> {
        if (list.size < 2) {
            return list
        }
        var listL = LinkedList<GameInfoContainer>()
        var listR = LinkedList<GameInfoContainer>()
        val middle = list.size / 2
        for (i in list.indices) {
            if (i < middle) {
                listL.add(list[i])
            } else {
                listR.add(list[i])
            }
        }
        listL = sortListByLastPlayedDate(listL)
        listR = sortListByLastPlayedDate(listR)
        return sortListByLastPlayedDateMerge(listL, listR)
    }

    fun sortListByLastPlayedDateMerge(
        list1: LinkedList<GameInfoContainer>,
        list2: LinkedList<GameInfoContainer>
    ): LinkedList<GameInfoContainer> {
        val result = LinkedList<GameInfoContainer>()
        while (!(list1.isEmpty() && list2.isEmpty())) {
            val gic1 = list1.peek()
            val gic2 = list2.peek()
            if (gic1 == null) {
                result.add(list2.pop())
            } else if (gic2 == null) {
                result.add(list1.pop())
            } else if (gic1.lastTimePlayed.after(gic2.lastTimePlayed)) {
                result.add(list1.pop())
            } else {
                result.add(list2.pop())
            }
        }
        return result
    }

    fun saveGameState(controller: GameController) {
        val level = GameInfoContainer.getGameInfo(controller)
        val dir = context.getDir(SAVES_DIR, 0)
        val file = File(dir, SAVE_PREFIX + controller.gameID.toString() + FILE_EXTENSION)
        try {
            val stream = FileOutputStream(file)
            try {
                stream.write(level.toByteArray())
            } finally {
                stream.close()
            }
        } catch (e: IOException) {
            Log.e("File Manager", "Could not save game. IOException occured.")
        }
        val editor = settings.edit()
        editor.putBoolean("savesChanged", true)
        editor.commit()
    }

    companion object {
        private const val FILE_EXTENSION = ".txt"
        private const val SAVE_PREFIX = "save_"
        private const val SAVES_DIR = "saves"
        private const val MAX_NUM_OF_SAVED_GAMES = 10
        private var list: MutableList<GameInfoContainer> = LinkedList()
        val loadableGameList: List<GameInfoContainer>
            get() = list
    }
}