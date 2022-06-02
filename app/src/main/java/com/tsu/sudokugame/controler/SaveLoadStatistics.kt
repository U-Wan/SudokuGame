package com.tsu.sudokugame.controler

import android.content.Context
import android.util.Log
import com.tsu.sudokugame.model.game.GameType.Companion.validGameTypes
import com.tsu.sudokugame.model.game.listener.ITimerListener
import com.tsu.sudokugame.model.game.listener.IHintListener
import com.tsu.sudokugame.model.game.GameType
import com.tsu.sudokugame.model.game.GameDifficulty
import com.tsu.sudokugame.model.data.HighscoreInfoContainer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.ArrayList

class SaveLoadStatistics(var context: Context) : ITimerListener, IHintListener {
    private var gc: GameController? = null
    private val numberOfArguents = 2
    fun setGameController(gc: GameController) {
        this.gc = gc
        gc.registerTimerListener(this)
        gc.registerHintListener(this)
    }

    fun loadStats(t: GameType, gd: GameDifficulty): HighscoreInfoContainer {
        val dir = context.getDir(SAVES_DIR, 0)
        val infos: HighscoreInfoContainer
        val bytes: ByteArray
        val inputStream: FileInputStream
        val file: File
        file = File(dir, SAVE_PREFIX + t.name + "_" + gd.name + FILE_EXTENSION)
        bytes = ByteArray(file.length().toInt())
        try {
            inputStream = FileInputStream(file)
            try {
                inputStream.read(bytes)
            } finally {
                inputStream.close()
            }
        } catch (e: IOException) {
            Log.e("Failed to read file", "File could not be read")
        }
        infos = HighscoreInfoContainer(t, gd)
        try {
            infos.setInfosFromFile(String(bytes))
        } catch (e: IllegalArgumentException) {
            file.delete()
        }
        return infos
    }

    fun loadStats(t: GameType): List<HighscoreInfoContainer> {
        val dir = context.getDir(SAVES_DIR, 0)
        val difficulties: List<GameDifficulty> = GameDifficulty.validDifficultyList
        val result: MutableList<HighscoreInfoContainer> = ArrayList()
        var infos: HighscoreInfoContainer
        var bytes: ByteArray
        var inputStream: FileInputStream
        var file: File
        for (dif in difficulties) {
            file = File(dir, SAVE_PREFIX + t.name + "_" + dif.name + FILE_EXTENSION)
            bytes = ByteArray(file.length().toInt())
            try {
                inputStream = FileInputStream(file)
                try {
                    inputStream.read(bytes)
                } finally {
                    inputStream.close()
                }
            } catch (e: IOException) {
                Log.e("Failed to read file", "File could not be read")
            }
            infos = HighscoreInfoContainer(t, dif)
            try {
                infos.setInfosFromFile(String(bytes))
            } catch (e: IllegalArgumentException) {
                file.delete()
            }
            result.add(infos)
        }
        return result
    }

    fun incTime(gd: GameDifficulty, gameType: GameType) {
        val infos = loadStats(gameType, gd)
        infos.incTime()
        saveContainer(infos, gd, gameType)
    }

    fun incHints(gd: GameDifficulty, gameType: GameType) {
        val infos = loadStats(gameType, gd)
        infos.incHints()
        saveContainer(infos, gd, gameType)
    }

    fun saveContainer(infos: HighscoreInfoContainer, gd: GameDifficulty, t: GameType) {
        val dir = context.getDir(SAVES_DIR, 0)
        val file = File(dir, SAVE_PREFIX + t.name + "_" + gd.name + FILE_EXTENSION)
        val stats = infos.actualStats
        try {
            val stream = FileOutputStream(file)
            try {
                stream.write(stats.toByteArray())
            } finally {
                stream.close()
            }
        } catch (e: IOException) {
            Log.e("File Manager", "Could not save game. IOException occured.")
        }
    }

    fun saveGameStats() {
        if (gc == null) return
        val infoContainer = HighscoreInfoContainer()

        // Read existing stats
        val dir = context.getDir(SAVES_DIR, 0)
        val file = File(
            dir,
            SAVE_PREFIX + gc!!.gameType.name + "_" + gc!!.difficulty.name + FILE_EXTENSION
        )
        if (file.isFile) {
            val bytes = ByteArray(file.length().toInt())
            try {
                val stream = FileInputStream(file)
                try {
                    stream.read(bytes)
                } finally {
                    stream.close()
                }
            } catch (e: IOException) {
                Log.e("Stats load to save game", "error while load old game Stats")
            }
            val fileStats = String(bytes)
            if (!fileStats.isEmpty()) {
                try {
                    infoContainer.setInfosFromFile(fileStats)
                } catch (e: IllegalArgumentException) {
                    Log.e("Parse Error", "Illegal Atgumanet")
                }
            }
        }
        infoContainer.add(gc!!)
        val stats = infoContainer.actualStats
        try {
            val stream = FileOutputStream(file)
            try {
                stream.write(stats.toByteArray())
            } finally {
                stream.close()
            }
        } catch (e: IOException) {
            Log.e("File Manager", "Could not save game. IOException occured.")
        }
    }

    override fun onTick(time: Int) {
        if (!gc!!.gameIsCustom()) incTime(gc!!.difficulty, gc!!.gameType)
    }

    override fun onHintUsed() {
        if (!gc!!.gameIsCustom()) incHints(gc!!.difficulty, gc!!.gameType)
    }

    companion object {
        private const val FILE_EXTENSION = ".txt"
        private const val SAVE_PREFIX = "stat"
        private const val SAVES_DIR = "stats"
        fun resetStats(context: Context) {
            val dir = context.getDir(SAVES_DIR, 0)
            var file: File
            for (t in validGameTypes) {
                for (d in GameDifficulty.validDifficultyList) {
                    file = File(dir, SAVE_PREFIX + t.name + "_" + d.name + FILE_EXTENSION)
                    file.delete()
                }
            }
        }
    }
}