
package com.tsu.sudokugame.view


import com.google.android.material.navigation.NavigationView
import com.tsu.sudokugame.model.game.listener.ITimerListener
import com.tsu.sudokugame.controler.IHintDialogFragmentListener
import com.tsu.sudokugame.controler.IResetDialogFragmentListener
import com.tsu.sudokugame.controler.IShareDialogFragmentListener
import com.tsu.sudokugame.controler.GameController
import com.tsu.sudokugame.controler.helper.SudokuFieldLayout
import com.tsu.sudokugame.controler.helper.SudokuKeyboardLayout
import com.tsu.sudokugame.controler.helper.SudokuSpecialButtonLayout
import android.widget.TextView
import android.widget.RatingBar
import com.tsu.sudokugame.controler.SaveLoadStatistics
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import android.view.WindowManager
import com.tsu.sudokugame.model.game.GameType
import com.tsu.sudokugame.model.game.logic.createboard
import com.tsu.sudokugame.model.data.GameInfoContainer
import com.tsu.sudokugame.ui.view.R
import com.tsu.sudokugame.controler.GameStateManager
import android.content.Intent
import android.widget.LinearLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.res.Configuration
import android.graphics.Point
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.tsu.sudokugame.controler.Symbol
import com.tsu.sudokugame.model.game.GameDifficulty
import com.tsu.sudokugame.model.game.listener.IGameSolvedListener
import java.lang.IllegalArgumentException
import java.util.*

class GameActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    IGameSolvedListener, ITimerListener, IHintDialogFragmentListener, IResetDialogFragmentListener,
    IShareDialogFragmentListener {
    var gameController: GameController? = null
    var layout: SudokuFieldLayout? = null
    var keyboard: SudokuKeyboardLayout? = null
    var specialButtonLayout: SudokuSpecialButtonLayout? = null
    var timerView: TextView? = null
    var viewName: TextView? = null
    var ratingBar: RatingBar? = null
    var statistics = SaveLoadStatistics(this)
    var dialog: WinDialog? = null
    private var gameSolved = false
    private var startGame = true
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (gameSolved || !startGame) {
            gameController!!.pauseTimer()
        } else {
            // start the game
            mHandler!!.postDelayed(
                { gameController!!.startTimer() },
                MAIN_CONTENT_FADEIN_DURATION.toLong()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

         if (sharedPref.getBoolean("pref_dark_mode_setting", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (sharedPref.getBoolean("pref_dark_mode_automatically_by_system", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else if (sharedPref.getBoolean("pref_dark_mode_automatically_by_battery", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        if (sharedPref.getBoolean("pref_keep_screen_on", true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        super.onCreate(savedInstanceState)
        var gameType = GameType.Unspecified
        var gameDifficulty = GameDifficulty.Unspecified
        var loadLevelID = 0
        var loadLevel = false
        if (savedInstanceState == null) {
            val extras = intent.extras

            val data = intent.data
            gameController = GameController(sharedPref, applicationContext)

            // Intents coming from the LoadGameActivity and MainActivity can be identified based on the keys the getExtras() bundle contains
            val intentReceivedFromMainActivity = extras != null &&
                    (extras.containsKey("gameType") || extras.containsKey("loadLevel"))

             if (data != null && !intentReceivedFromMainActivity) {
                val input = ""

                var sectionSize = Math.sqrt(input.length.toDouble()).toInt()
                var boardSize = sectionSize * sectionSize
                val difficultyCheck: createboard
                var container =
                    GameInfoContainer(0,
                        GameDifficulty.Unspecified,
                        GameType.Unspecified,
                        IntArray(boardSize),
                        IntArray(boardSize),
                        Array(boardSize) { BooleanArray(sectionSize) })
                container.isCustom = true
                try {
                    container.parseGameType("Default_" + sectionSize + "x" + sectionSize)
                    container.parseFixedValues(input)
                    difficultyCheck =
                        createboard(
                            container.gameType,
                            GameDifficulty.Unspecified
                        )

                    difficultyCheck.setRecordHistory(true)
                    difficultyCheck.puzzle = container.fixedValues
                    difficultyCheck.solve()
                    container.parseDifficulty(difficultyCheck.difficulty.toString())

                    startGame = difficultyCheck.hasUniqueSolution()
                } catch (e: IllegalArgumentException) {
                    startGame = false

                 sectionSize = GameType.Default_9x9.size
                    boardSize = sectionSize * sectionSize
                    container =
                        GameInfoContainer(
                            0,
                            GameDifficulty.Unspecified,
                            GameType.Default_9x9,
                            IntArray(boardSize),
                            IntArray(boardSize),
                            Array(boardSize) { BooleanArray(sectionSize) })
                }

                // Notify the user if the sudoku they tried to import cannot be played and finish the activity
                if (!startGame) {
                    val builder = AlertDialog.Builder(this@GameActivity, R.style.AppTheme_Dialog)
                    builder.setMessage(R.string.impossible_import_notice)
                        .setCancelable(false)
                        .setPositiveButton(R.string.okay) { dialog, id -> finish() }
                    val alert = builder.create()
                    alert.show()
                }
                gameController!!.loadLevel(container)
            } else {
                var isDailySudoku = false
                if (extras != null) {
                    gameType =
                        GameType.valueOf(extras.getString("gameType", GameType.Default_9x9.name))
                    gameDifficulty = GameDifficulty.valueOf(
                        extras.getString(
                            "gameDifficulty",
                            GameDifficulty.Moderate.name
                        )
                    )
                    isDailySudoku = extras.getBoolean("isDailySudoku", false)
                    loadLevel = extras.getBoolean("loadLevel", false)
                    if (loadLevel) {
                        loadLevelID = extras.getInt("loadLevelID")
                    }
                }


                val loadableGames = GameStateManager.loadableGameList
                if (loadLevel) {
                    if (loadableGames.size > loadLevelID) {
                        // load level from GameStateManager
                        gameController!!.loadLevel(loadableGames[loadLevelID])
                    } else if (loadLevelID == GameController.DAILY_SUDOKU_ID) {
                        for (container in loadableGames) {
                            if (container.id == loadLevelID) {
                                gameController!!.loadLevel(container)
                                break
                            }
                        }
                    }
                } else {
                    // load a new level
                    gameController!!.loadNewLevel(gameType, gameDifficulty)
                }
            }
        } else {
            gameController = savedInstanceState.getParcelable("gameController")
            // in case we get the same object back
            // because parceling the Object does not always parcel it. Only if needed.
            if (gameController != null) {
                gameController!!.removeAllListeners()
                gameController!!.setContextAndSettings(applicationContext, sharedPref)
            } else {
                // Error: no game could be restored. Go back to main menu.
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                overridePendingTransition(0, 0)
            }
            gameSolved = savedInstanceState.getBoolean("gameSolved")
        }
        setContentView(R.layout.activity_game_view)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        //toolbar.addView();


        layout = findViewById<View>(R.id.sudokuLayout) as SudokuFieldLayout
        gameController!!.registerGameSolvedListener(this)
        gameController!!.registerTimerListener(this)
        statistics.setGameController(gameController!!)
        layout!!.setSettingsAndGame(sharedPref, gameController)

        keyboard = findViewById<View>(R.id.sudokuKeyboardLayout) as SudokuKeyboardLayout
        keyboard!!.removeAllViews()
        keyboard!!.setGameController(gameController)

        val p = Point()
        windowManager.defaultDisplay.getSize(p)

        // set keyboard orientation
        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        keyboard!!.setKeyBoard(gameController!!.size, p.x, layout!!.height - p.y, orientation)


        //set Special keys
        specialButtonLayout =
            findViewById<View>(R.id.sudokuSpecialLayout) as SudokuSpecialButtonLayout
        specialButtonLayout!!.setButtons(
            p.x,
            gameController,
            keyboard,
            fragmentManager,
            orientation,
            this@GameActivity
        )

        timerView = findViewById<View>(R.id.timerView) as TextView


        viewName = findViewById<View>(R.id.gameModeText) as TextView
        viewName!!.text = getString(gameController!!.gameType.stringResID)

        val difficutyList: List<GameDifficulty> =GameDifficulty.validDifficultyList
        val numberOfStarts = difficutyList.size
        ratingBar = findViewById<View>(R.id.gameModeStar) as RatingBar
        ratingBar!!.max = numberOfStarts
        ratingBar!!.numStars = numberOfStarts
        ratingBar!!.rating = (difficutyList.indexOf(gameController!!.difficulty) + 1).toFloat()
        val diffText = findViewById<View>(R.id.difficultyText) as TextView
        diffText.text = getString(gameController!!.difficulty.stringResID)
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        if (gameSolved) {
            layout!!.isEnabled = false
            keyboard!!.setButtonsEnabled(false)
            specialButtonLayout!!.setButtonsEnabled(false)
        }
        gameController!!.notifyHighlightChangedListeners()
        gameController!!.notifyTimerListener(gameController!!.time)

        // run this so the error list gets build again.
        gameController!!.onModelChange(null)
        overridePendingTransition(0, 0)
    }

    public override fun onPause() {
        super.onPause()

        // Do not save solved or unplayable sudokus
        if (!gameSolved && startGame) {
            gameController!!.saveGame(this)
        }
        gameController!!.deleteTimer()
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val mainContent = findViewById<View>(R.id.main_content)
        if (mainContent != null) {
            mainContent.animate().alpha(1f).duration = MAIN_CONTENT_FADEOUT_DURATION.toLong()
        }
        gameController!!.initTimer()
        if (!gameSolved && startGame) {
            mHandler!!.postDelayed(
                { gameController!!.startTimer() },
                MAIN_CONTENT_FADEIN_DURATION.toLong()
            )
        }
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val s: Symbol
        s = try {
            Symbol.valueOf(sharedPref.getString("pref_symbols", Symbol.Default.name)!!)
        } catch (e: IllegalArgumentException) {
            Symbol.Default
        }
        layout!!.setSymbols(s)
        keyboard!!.setSymbols(s)
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            finish()
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var intent: Intent? = null
        when (id) {
            R.id.nav_highscore ->      {
                intent = Intent(this, StatsActivity::class.java)

            }
            R.id.go_to_home->{
                var intentt=Intent(this,MainActivity::class.java)
                startActivity(intentt)
            }
            else -> {}
        }
        if (intent != null) {
            val i: Intent = intent
            val mainContent = findViewById<View>(R.id.main_content)
            if (mainContent != null) {
                mainContent.animate().alpha(0f).duration = MAIN_CONTENT_FADEOUT_DURATION.toLong()
            }
            mHandler!!.postDelayed({
                startActivity(i)
                overridePendingTransition(0, 0)
            }, NAVDRAWER_LAUNCH_DELAY.toLong())
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSolved() {
        gameSolved = true
        gameController!!.pauseTimer()
        gameController!!.deleteGame(this)


        val isNewBestTime: Boolean
        isNewBestTime = if (!gameController!!.gameIsCustom()) {
            //Show time hints new plus old best time
            statistics.saveGameStats()
            (gameController!!.usedHints == 0
                    && statistics.loadStats(
                gameController!!.gameType,
                gameController!!.difficulty
            ).minTime >= gameController!!.time)
        } else {
            false
        }
        buildWinDialog(
            timeToString(gameController!!.time), gameController!!.usedHints.toString(),
            isNewBestTime
        ).show(supportFragmentManager, "WIN_DIALOG")
        layout!!.isEnabled = false
        keyboard!!.setButtonsEnabled(false)
        specialButtonLayout!!.setButtonsEnabled(false)
    }

    private fun buildWinDialog(
        usedTime: String,
        usedHints: String,
        isNewBestTime: Boolean
    ): WinDialog {
        val dialogArguments = Bundle()
        dialogArguments.putString(WinDialog.ARG_TIME, usedTime)
        dialogArguments.putString(WinDialog.ARG_HINT, usedHints)
        dialogArguments.putBoolean(WinDialog.ARG_BEST, isNewBestTime)
        dialog = WinDialog()
        dialog!!.arguments = dialogArguments
        return dialog as WinDialog
    }


    override fun onTick(time: Int) {

        timerView!!.text = timeToString(time)
        if (gameSolved || !startGame) return
        gameController!!.saveGame(this)
    }

    override fun onHintDialogPositiveClick() {
        gameController!!.hint()
    }

    override fun onResetDialogPositiveClick() {
        gameController!!.resetLevel()
    }

    override fun onShareDialogPositiveClick(input: String?) {
        val sendBoardIntent = Intent()
        sendBoardIntent.action = Intent.ACTION_SEND
        sendBoardIntent.putExtra(Intent.EXTRA_TEXT, input)
        sendBoardIntent.type = "text/plain"
        val shareBoardIntent = Intent.createChooser(sendBoardIntent, null)
        startActivity(shareBoardIntent)
    }

    override fun onDialogNegativeClick() {
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("gameController", gameController)
        savedInstanceState.putBoolean("gameSolved", gameSolved)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        gameController = savedInstanceState.getParcelable("gameController")
        gameSolved = savedInstanceState.getBoolean("gameSolved")
    }

    class ShareBoardDialog : DialogFragment() {
        private val listeners = LinkedList<IShareDialogFragmentListener>()

        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
            // Verify that the host activity implements the callback interface
            if (activity is IShareDialogFragmentListener) {
                listeners.add(activity as IShareDialogFragmentListener)
            }
        }

        class ResetConfirmationDialog : DialogFragment() {
            var listeners = LinkedList<IResetDialogFragmentListener>()
            override fun onAttach(activity: Activity) {
                super.onAttach(activity)
                // Verify that the host activity implements the callback interface
                if (activity is IResetDialogFragmentListener) {
                    listeners.add(activity as IResetDialogFragmentListener)
                }
            }

            override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
                // Use the Builder class for convenient dialog construction
                val builder = AlertDialog.Builder(activity, R.style.AppTheme_Dialog)
                builder.setMessage(R.string.reset_confirmation)
                    .setPositiveButton(R.string.reset_confirmation_confirm) { dialog, id ->
                        for (l in listeners) {
                            l.onResetDialogPositiveClick()
                        }
                    }
                    .setNegativeButton(R.string.cancel) { dialog, id ->
                        // User cancelled the dialog
                    }
                return builder.create()
            }
        }
    }

    companion object {
        fun timeToString(time: Int): String {
            val seconds = time % 60
            val minutes = (time - seconds) / 60 % 60
            val hours = (time - minutes - seconds) / 3600
            val h: String
            val m: String
            val s: String
            s = if (seconds < 10) "0$seconds" else seconds.toString()
            m = if (minutes < 10) "0$minutes" else minutes.toString()
            h = if (hours < 10) "0$hours" else hours.toString()
            return "$h:$m:$s"
        }
    }
}