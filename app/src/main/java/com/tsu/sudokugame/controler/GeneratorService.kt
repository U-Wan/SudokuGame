package com.tsu.sudokugame.controler

import com.tsu.sudokugame.model.game.GameType.Companion.validGameTypes

import android.app.IntentService
import com.tsu.sudokugame.model.game.GameType
import com.tsu.sudokugame.model.game.GameDifficulty
import com.tsu.sudokugame.model.data.DatabaseHelper
import android.content.Intent
import com.tsu.sudokugame.model.game.logic.Symmetry
import com.tsu.sudokugame.model.game.logic.createboard
import androidx.core.app.NotificationCompat
import com.tsu.sudokugame.ui.view.R
import android.app.PendingIntent
import android.util.Log
import android.util.Pair
import com.tsu.sudokugame.view.MainActivity
import androidx.core.content.ContextCompat
import com.tsu.sudokugame.model.data.Level
import com.tsu.sudokugame.model.game.logic.Action
import com.tsu.sudokugame.model.game.logic.PrintStyle
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class GeneratorService : IntentService {
    private val opts = QQWingOptions()
    private val generationList: MutableList<Pair<GameType, GameDifficulty>> = LinkedList()
    private val dbHelper = DatabaseHelper(this)

    //private Handler mHandler = new Handler();
    constructor() : super("Generator Service") {}
    constructor(name: String?) : super(name) {}

    private fun buildGenerationList() {
        generationList.clear()
        for (validType in validGameTypes) {
            for (validDifficulty in GameDifficulty.validDifficultyList) {
                val levelCount = dbHelper.getLevels(validDifficulty, validType).size
                Log.d(
                    TAG,
                    "\tType: " + validType.name + " Difficulty: " + validDifficulty.name + "\t: " + levelCount
                )
                // add the missing levels to the list
                for (i in levelCount until NewLevelManager.PRE_SAVES_MIN) {
                    generationList.add(Pair(validType, validDifficulty))
                }
            }
        }

        // PrintGenerationList
        Log.d(TAG, "### Missing Levels: ###")
        var i = 0
        for (dataPair in generationList) {
            Log.d(
                TAG,
                "\t" + i++ + ":\tType: " + dataPair.first.name + " Difficulty: " + dataPair.second.name
            )
        }
    }

    private fun handleGenerationStop() {
        stopForeground(true)
        //mHandler.removeCallbacksAndMessages(null);
    }

    private fun handleGenerationStart(intent: Intent) {
        var gameType: GameType?
        var gameDifficulty: GameDifficulty?
        try {
            gameType = GameType.valueOf(
                intent.extras!!.getString(EXTRA_GAMETYPE, "")
            )
            gameDifficulty = GameDifficulty.valueOf(
                intent.extras!!.getString(EXTRA_DIFFICULTY, "")
            )
        } catch (e: IllegalArgumentException) {
            gameType = null
            gameDifficulty = null
        } catch (e: NullPointerException) {
            gameType = null
            gameDifficulty = null
        }
        if (gameType == null) {
            generateLevels()
        } else {
            generateLevel(gameType, gameDifficulty)
        }
    }

    private fun generateLevels() {
        // if we start this service multiple times while we are already generating...
        // we ignore this call and just keep generating
        buildGenerationList()

        // generate from the list
        if (generationList.size > 0) {

            // generate 1 level and wait for it to be done.
            val dataPair = generationList[0]
            val type = dataPair.first
            val diff = dataPair.second
            generateLevel(type, diff)
        }
    }

    private fun generateLevel(gameType: GameType, gameDifficulty: GameDifficulty?) {
        generated.clear()
        opts.gameDifficulty = gameDifficulty
        opts.action = Action.GENERATE
        opts.needNow = true
        opts.printSolution = false
        opts.gameType = gameType
        if (gameDifficulty == GameDifficulty.Easy && gameType === GameType.Default_9x9) {
            opts.symmetry = Symmetry.ROTATE90
        } else {
            opts.symmetry = Symmetry.NONE
        }
        if (gameType === GameType.Default_12x12 && gameDifficulty != GameDifficulty.Challenge) {
            opts.symmetry = Symmetry.ROTATE90
        }
        val puzzleCount = AtomicInteger(0)
        val done = AtomicBoolean(false)
        val generationRunnable: Runnable = object : Runnable {
            // Create a new puzzle board
            // and set the options
            private val ss = createQQWing()
            private fun createQQWing(): createboard {
                val ss = createboard(
                    opts.gameType,
                    opts.gameDifficulty!!
                )
                ss.setRecordHistory(opts.printHistory || opts.printInstructions || opts.printStats || opts.gameDifficulty != GameDifficulty.Unspecified)
                ss.setLogHistory(opts.logHistory)
                ss.setPrintStyle(opts.printStyle)
                return ss
            }

            override fun run() {
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                try {

                    // Solve puzzle or generate puzzles
                    // until end of input for solving, or
                    // until we have generated the specified number.
                    while (!done.get()) {

                        // Record whether the puzzle was possible or
                        // not,
                        // so that we don't try to solve impossible
                        // givens.
                        var havePuzzle: Boolean
                        var solveImpossible: Boolean
                        if (opts.action === Action.GENERATE) {
                            // Generate a puzzle
                            havePuzzle = ss.generatePuzzleSymmetry(opts.symmetry)
                        } else {
                            // Read the next puzzle on STDIN
                            var puzzle: IntArray? = IntArray(createboard.BOARD_SIZE)
                            if (getPuzzleToSolve(puzzle)) {
                                havePuzzle = ss.setPuzzlee(puzzle)
                                if (havePuzzle) {
                                    puzzleCount.getAndDecrement()
                                } else {
                                    // Puzzle to solve is impossible.
                                    solveImpossible = true
                                }
                            } else {
                                // Set loop to terminate when nothing is
                                // left on STDIN
                                havePuzzle = false
                                done.set(true)
                            }
                            puzzle = null
                        }
                        if (opts.gameDifficulty != GameDifficulty.Unspecified) {
                            ss.solve()
                        }
                        if (havePuzzle) {
                            // Bail out if it didn't meet the difficulty
                            // standards for generation
                            if (opts.action === Action.GENERATE) {

                                // save the level anyways but keep going if the desired level is not yet generated
                                val level = Level()
                                level.gameType = opts.gameType
                                level.difficulty = ss.difficulty
                                level.puzzle = ss.puzzle
                                dbHelper.addLevel(level)
                                Log.d(
                                    TAG,
                                    "Generated: " + level.gameType.name + ",\t" + level.difficulty.name
                                )
                                if (opts.gameDifficulty != GameDifficulty.Unspecified && opts.gameDifficulty != ss.difficulty) {
                                    havePuzzle = false
                                    // check if other threads have finished the job
                                    if (puzzleCount.get() >= opts.numberToGenerate) done.set(true)
                                } else {
                                    val numDone = puzzleCount.incrementAndGet()
                                    if (numDone >= opts.numberToGenerate) done.set(true)
                                    if (numDone > opts.numberToGenerate) havePuzzle = false
                                }
                            }
                            if (havePuzzle) {
                                generated.add(ss.puzzle)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QQWing", "Exception Occured", e)
                    return
                }
                generationDone()
            }
        }
        generationRunnable.run()
    }

    // this is called whenever a generation is done..
    private fun generationDone() {
        // check if more can be generated
        if (generationList.size > 0) {
            generateLevels()
        } else {
            // we are done and can close this service
            handleGenerationStop()
        }
    }



    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_GENERATE == action) handleGenerationStart(intent) else if (ACTION_STOP == action) handleGenerationStop()
        }
    }

    private var level: IntArray? = null
    private val generated = LinkedList<IntArray>()

    private class QQWingOptions {
        // defaults for options
        var needNow = false
        var printPuzzle = false
        var printSolution = false
        var printHistory = false
        var printInstructions = false
        var timer = false
        var countSolutions = false
        var action = Action.NONE
        var logHistory = false
        var printStyle = PrintStyle.READABLE
        var numberToGenerate = 1
        var printStats = false
        var gameDifficulty: GameDifficulty? = GameDifficulty.Unspecified
        var gameType = GameType.Unspecified
        var symmetry = Symmetry.NONE
        var threads = Runtime.getRuntime().availableProcessors()
    }

    private fun getPuzzleToSolve(puzzle: IntArray?): Boolean {
        if (level != null) {
            if (puzzle!!.size == level!!.size) {
                for (i in level!!.indices) {
                    puzzle[i] = level!![i]
                }
            }
            level = null
            return true
        }
        return false
    }

    companion object {
        private val TAG = GeneratorService::class.java.simpleName
        val ACTION_GENERATE = TAG + " ACTION_GENERATE"
        val ACTION_STOP = TAG + " ACTION_STOP"
        val EXTRA_GAMETYPE = TAG + " EXTRA_GAMETYPE"
        val EXTRA_DIFFICULTY = TAG + " EXTRA_DIFFICULTY"
    }
}