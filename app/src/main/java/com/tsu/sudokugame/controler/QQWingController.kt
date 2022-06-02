package com.tsu.sudokugame.controler

import android.os.Process
import android.util.Log
import com.tsu.sudokugame.model.game.logic.Action
import com.tsu.sudokugame.model.game.GameType
import com.tsu.sudokugame.model.game.logic.createboard
import com.tsu.sudokugame.model.game.GameBoard
import com.tsu.sudokugame.model.game.logic.PrintStyle
import com.tsu.sudokugame.model.game.logic.Symmetry
import com.tsu.sudokugame.model.game.GameDifficulty
import java.lang.Exception
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class QQWingController {
    val opts = QQWingOptions()
    private var level: IntArray? = null
    private lateinit var solution: IntArray
    private val generated = LinkedList<IntArray>()
    private var solveImpossible = false
    fun generate(type: GameType, difficulty: GameDifficulty): IntArray {
        generated.clear()
        opts.gameDifficulty = difficulty
        opts.action = Action.GENERATE
        opts.needNow = true
        opts.printSolution = false
        opts.threads = Runtime.getRuntime().availableProcessors()
        opts.gameType = type
        doAction()
        return generated.poll()
    }

    fun generateMultiple(
        type: GameType,
        difficulty: GameDifficulty,
        amount: Int
    ): LinkedList<IntArray> {
        generated.clear()
        opts.numberToGenerate = amount
        opts.gameDifficulty = difficulty
        opts.needNow = true
        opts.action = Action.GENERATE
        opts.printSolution = false
        opts.threads = Runtime.getRuntime().availableProcessors()
        opts.gameType = type
        doAction()
        return generated
    }

    fun generateFromSeed(
        seed: Int,
        challengePermission: Double,
        challengeIterations: Int
    ): IntArray {
        var seed = seed
        var challengeIterations = challengeIterations
        generated.clear()
        val generator = createboard(
            GameType.Default_9x9,
            GameDifficulty.Unspecified
        )
        var continueSearch = true
        val random = Random(seed.toLong())
        val seedFactor = 2
        while (continueSearch && challengeIterations > 0) {
            seed *= seedFactor
            generator.setRandom(seed)
            generator.setRecordHistory(true)
            generator.generatePuzzle()
            if (generator.difficulty != GameDifficulty.Challenge || random.nextDouble() < challengePermission) {
                continueSearch = false
            } else {
                challengeIterations--
            }
        }
        generated.add(generator.puzzle)
        opts.gameType = GameType.Default_9x9
        opts.gameDifficulty = generator.difficulty
        return generated.poll()
    }

    fun solve(gameBoard: GameBoard): IntArray {
        level = IntArray(gameBoard.size * gameBoard.size)
        solveImpossible = false
        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard.size) {
                if (gameBoard.getCell(i, j).isFixed) {
                    level!![gameBoard.size * i + j] = gameBoard.getCell(i, j).value
                }
            }
        }
        opts.needNow = true
        opts.action = Action.SOLVE
        opts.printSolution = true
        opts.threads = 1
        opts.gameType = gameBoard.gameType
        doAction()
        //if(solveImpossible) {
        // Can not occur with normal use of the app.
        //}
        return solution
    }

    private fun doAction() {
        // The number of puzzles solved or generated.
        val puzzleCount = AtomicInteger(0)
        val done = AtomicBoolean(false)
        val threads = arrayOfNulls<Thread>(opts.threads)
        for (threadCount in threads.indices) {
            threads[threadCount] = Thread(
                object : Runnable {
                    // Create a new puzzle board
                    // and set the options
                    private val ss = createQQWing()
                    private fun createQQWing(): createboard {
                        val ss =
                            createboard(
                                opts.gameType,
                                opts.gameDifficulty
                            )
                        ss.setRecordHistory(opts.printHistory || opts.printInstructions || opts.printStats || opts.gameDifficulty != GameDifficulty.Unspecified)
                        ss.setLogHistory(opts.logHistory)
                        ss.setPrintStyle(opts.printStyle)
                        return ss
                    }

                    override fun run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
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
                                val solutions = 0
                                if (havePuzzle) {

                                    // Count the solutions if requested.
                                    // (Must be done before solving, as it would
                                    // mess up the stats.)
                                    //if (opts.countSolutions) {
                                    //    solutions = ss.countSolutions();
                                    //}

                                    // Solve the puzzle
                                    if (opts.printSolution || opts.printHistory || opts.printStats || opts.printInstructions || opts.gameDifficulty != GameDifficulty.Unspecified) {
                                        ss.solve()
                                        solution = ss.solution
                                    }

                                    // Bail out if it didn't meet the difficulty
                                    // standards for generation
                                    if (opts.action === Action.GENERATE) {
                                        if (opts.gameDifficulty != GameDifficulty.Unspecified && opts.gameDifficulty != ss.difficulty) {
                                            havePuzzle = false
                                            // check if other threads have
                                            // finished the job
                                            if (puzzleCount.get() >= opts.numberToGenerate) done.set(
                                                true
                                            )
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
                    }
                }
            )
            threads[threadCount]!!.start()
        }
        if (opts.needNow) {
            for (i in threads.indices) {
                try {
                    threads[i]!!.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    class QQWingOptions {
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
        var gameDifficulty = GameDifficulty.Unspecified
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
}