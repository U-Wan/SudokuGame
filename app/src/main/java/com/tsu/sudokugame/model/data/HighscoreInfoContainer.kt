package com.tsu.sudokugame.model.data

import com.tsu.sudokugame.model.game.GameType
import com.tsu.sudokugame.controler.GameController
import com.tsu.sudokugame.model.game.GameDifficulty
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class HighscoreInfoContainer {
    var gameType: GameType? = null
        private set
    var difficulty: GameDifficulty? = null
        private set
    var minTime = Int.MAX_VALUE
        private set
    var time = 0
        private set
    var numberOfHintsUsed = 0
        private set
    var numberOfGames = 0
        private set
    var numberOfGamesNoHints = 0
        private set
    var timeNoHints = 0
        private set
    private val amountOfSavedArguments = 8

    constructor() {}
    constructor(t: GameType?, diff: GameDifficulty?) {
        gameType = if (gameType == null) t else gameType
        difficulty = if (difficulty == null) diff else difficulty
    }

    fun add(gc: GameController) {
        difficulty = if (difficulty == null) gc.difficulty else difficulty
        gameType = if (gameType == null) gc.gameType else gameType
        numberOfGames++
        minTime = if (gc.usedHints == 0 && gc.time < minTime) gc.time else minTime
        numberOfGamesNoHints =
            if (gc.usedHints == 0) numberOfGamesNoHints + 1 else numberOfGamesNoHints
        timeNoHints = if (gc.usedHints == 0) timeNoHints + gc.time else timeNoHints
    }

    fun incHints() {
        numberOfHintsUsed++
    }

    fun incTime() {
        time++
    }

    fun setInfosFromFile(s: String) {
        if (s.isEmpty()) return
        val strings = s.split("/").toTypedArray()
        require(strings.size == amountOfSavedArguments) { "Argument Exception" }
        try {
            time = parseTime(strings[0])
            numberOfHintsUsed = parseHints(strings[1])
            numberOfGames = parseNumberOfGames(strings[2])
            minTime = parseTime(strings[3])
            gameType = parseGameType(strings[4])
            difficulty = parsDifficulty(strings[5])
            numberOfGamesNoHints = parseNumberOfGames(strings[6])
            timeNoHints = parseTime(strings[7])
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Could not set Infoprmation illegal Arguments")
        }
    }

    private fun parseGameType(s: String): GameType {
        return GameType.valueOf(s)
    }

    private fun parsDifficulty(s: String): GameDifficulty {
        return GameDifficulty.valueOf(s)
    }

    private fun parseTime(s: String): Int {
        val ret = Integer.valueOf(s)
        require(ret >= 0) { "Parser Exception wrong Integer" }
        return ret
    }

    private fun parseHints(s: String): Int {
        val ret = Integer.valueOf(s)
        require(ret >= 0) { "Parser Exception wrong Integer" }
        return ret
    }

    private fun parseNumberOfGames(s: String): Int {
        val ret = Integer.valueOf(s)
        require(ret >= 0) { "Parser Exception wrong Integer" }
        return ret
    }

    val actualStats: String
        get() {
            val sb = StringBuilder()
            sb.append(time)
            sb.append("/")
            sb.append(numberOfHintsUsed)
            sb.append("/")
            sb.append(numberOfGames)
            sb.append("/")
            sb.append(minTime)
            sb.append("/")
            sb.append(gameType!!.name)
            sb.append("/")
            sb.append(difficulty!!.name)
            sb.append("/")
            sb.append(numberOfGamesNoHints)
            sb.append("/")
            sb.append(timeNoHints)
            return sb.toString()
        }
}