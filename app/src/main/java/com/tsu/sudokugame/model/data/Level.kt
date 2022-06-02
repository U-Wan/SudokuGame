package com.tsu.sudokugame.model.data

import com.tsu.sudokugame.controler.Symbol
import com.tsu.sudokugame.model.game.GameDifficulty
import com.tsu.sudokugame.model.game.GameType

class Level {
    var id = -1
    var difficulty = GameDifficulty.Unspecified
    var gameType = GameType.Unspecified
    lateinit var puzzle: IntArray


 /*   fun setPuzzle(puzzleString: String) {
        val puzzle = IntArray(gameType.size * gameType.size)
        require(puzzle.size == puzzleString.length) { "Saved level does not have the correct size." }
        for (i in 0 until puzzleString.length) {
            puzzle[i] = Symbol.getValue(Symbol.SaveFormat, puzzleString[i].toString()) + 1
        }
        this.puzzle = puzzle
    }*/
}