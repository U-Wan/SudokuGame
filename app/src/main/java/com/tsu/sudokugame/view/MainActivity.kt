package com.tsu.sudokugame.view

import com.tsu.sudokugame.model.game.GameType.Companion.validGameTypes

import com.google.android.material.navigation.NavigationView
import android.content.SharedPreferences
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.tsu.sudokugame.controler.NewLevelManager
import com.tsu.sudokugame.ui.view.R
import com.tsu.sudokugame.model.game.GameType
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.tsu.sudokugame.model.game.GameDifficulty
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.ActionBarDrawerToggle
import android.content.Intent
import com.tsu.sudokugame.controler.GameStateManager
import com.tsu.sudokugame.controler.GameController
import androidx.core.view.GravityCompat
import android.content.res.Configuration
import android.preference.PreferenceManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.lang.Enum

class MainActivity() : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    var difficultyBar: RatingBar? = null
    var difficultyText: TextView? = null
    lateinit var settings: SharedPreferences
    var arrowLeft: ImageView? = null
    var arrowRight: ImageView? = null
    var drawer: DrawerLayout? = null
    var mNavigationView: NavigationView? = null
    private var mViewPager: ViewPager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        settings = PreferenceManager.getDefaultSharedPreferences(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        val newLevelManager = NewLevelManager.getInstance(applicationContext, settings)

        // sheamowmebs tu aris sawiro level-is tavidan generireba
        newLevelManager?.checkAndRestock()
        setContentView(R.layout.activity_main_menu)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val mSectionsPagerAdapter= SectionsPagerAdapter(supportFragmentManager)

        //levelis scroleri
        mViewPager = findViewById<View>(R.id.scroller) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val validGameTypes: List<GameType> = validGameTypes
        val lastChosenGameType = settings!!.getString("lastChosenGameType", GameType.Default_9x9.name)
        val index = validGameTypes.indexOf(Enum.valueOf(GameType::class.java, lastChosenGameType))
        mViewPager!!.currentItem = index
        arrowLeft = findViewById<View>(R.id.arrow_left) as ImageView
        arrowRight = findViewById<View>(R.id.arrow_right) as ImageView

        arrowLeft!!.visibility = if ((index == 0)) View.INVISIBLE else View.VISIBLE
        arrowRight!!.visibility =
            if ((index == mSectionsPagerAdapter.count - 1)) View.INVISIBLE else View.VISIBLE

        mViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ){}
            override fun onPageSelected(position: Int) {
                arrowLeft!!.visibility = if ((position == 0)) View.INVISIBLE else View.VISIBLE
                arrowRight!!.visibility =
                    if ((position == mSectionsPagerAdapter.count - 1)) View.INVISIBLE else View.VISIBLE
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })


        difficultyBar = findViewById<View>(R.id.difficultyBar) as RatingBar
        difficultyText = findViewById<View>(R.id.difficultyText) as TextView
        val difficultyList = GameDifficulty.validDifficultyList
        difficultyBar!!.numStars = difficultyList.size
        difficultyBar!!.max = difficultyList.size

        difficultyBar!!.onRatingBarChangeListener = object : OnRatingBarChangeListener {
            override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
                (findViewById<View>(R.id.playButton) as Button).setText(R.string.new_game)
                if (rating >= 1) {
                    difficultyText!!.text =
                        getString(difficultyList.get(ratingBar.rating.toInt() - 1).stringResID)
                } else {
                    difficultyText!!.setText(R.string.difficulty_custom)
                    (findViewById<View>(R.id.playButton) as Button).setText(R.string.create_game)
                }
            }
        }

        val retrievedDifficulty = settings!!.getString("lastChosenDifficulty", "Moderate")
        val lastChosenDifficulty = GameDifficulty.valueOf(
            (if ((retrievedDifficulty == "Custom")) GameDifficulty.Unspecified.toString() else retrievedDifficulty)!!
        )
        if (lastChosenDifficulty == GameDifficulty.Unspecified) {
            difficultyBar!!.rating = 0f
        } else {
            difficultyBar!!.rating = (GameDifficulty.validDifficultyList
                .indexOf(lastChosenDifficulty) + 1).toFloat()
        }
        if (Configuration.SCREENLAYOUT_SIZE_SMALL == (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK)) {
            difficultyBar!!.scaleX = 0.75f
            difficultyBar!!.scaleY = 0.75f
        }

        val editor = settings!!.edit()
        editor.putBoolean("savesChanged", true)
        editor.apply()
        refreshContinueButton()


        drawer = findViewById<View>(R.id.drawer_layout_main) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.setDrawerListener(toggle)
        toggle.syncState()
        mNavigationView = findViewById<View>(R.id.nav_view_main) as NavigationView
        mNavigationView!!.setNavigationItemSelectedListener(this)

        overridePendingTransition(0, 0)
    }

    fun onClick(view: View) {
        var i: Intent? = null
        when (view.id) {
            R.id.arrow_left -> mViewPager!!.arrowScroll(View.FOCUS_LEFT)
            R.id.arrow_right -> mViewPager!!.arrowScroll(View.FOCUS_RIGHT)
            R.id.continueButton -> i = Intent(this, LoadGameActivity::class.java)
            R.id.playButton -> {
                val gameType = validGameTypes[mViewPager!!.currentItem]
                val index = difficultyBar!!.progress - 1
                val gameDifficulty =
                    GameDifficulty.validDifficultyList[if (index < 0) 0 else index]
                val newLevelManager = NewLevelManager.getInstance(applicationContext, settings!!)
                if (newLevelManager!!.isLevelLoadable(gameType, gameDifficulty)) {
                    val editor = settings!!.edit()
                    editor.putString("lastChosenGameType", gameType.name)
                    editor.putString("lastChosenDifficulty", gameDifficulty.name)
                    editor.apply()


                    i = Intent(this, GameActivity::class.java)
                    i.putExtra("gameType", gameType.name)
                    i.putExtra("gameDifficulty", gameDifficulty.name)
                } else {
                    newLevelManager.checkAndRestock()
                    val t =
                        Toast.makeText(applicationContext, R.string.generating, Toast.LENGTH_SHORT)
                    t.show()
                    return
                }
            }
            else -> {}
        }
        val intent = i
        if (intent != null) {
            val mainContent = findViewById<View>(R.id.main_content)
            if (mainContent != null) {
                mainContent.animate().alpha(0f).duration = MAIN_CONTENT_FADEOUT_DURATION.toLong()
            }
            mHandler!!.postDelayed(object : Runnable {
                override fun run() {
                    startActivity(intent)
                }
            }, MAIN_CONTENT_FADEOUT_DURATION.toLong())
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContinueButton()
    }

    private fun refreshContinueButton() {
        val continueButton = findViewById<View>(R.id.continueButton) as Button
        val fm = GameStateManager(baseContext, settings)
        val gic = fm.loadGameStateInfo()
        if (gic.size > 0 && !(gic.size == 1 && gic[0].id == GameController.DAILY_SUDOKU_ID)) {
            continueButton.isEnabled = true
            continueButton.setBackgroundResource(R.drawable.button_standalone)
        } else {
            continueButton.isEnabled = false
            continueButton.setBackgroundResource(R.drawable.button_inactive)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        drawer!!.closeDrawer(GravityCompat.START)

        mHandler!!.postDelayed(object : Runnable {
            override fun run() {
                goToNavigationItem(id)
            }
        }, NAVDRAWER_LAUNCH_DELAY.toLong())
        return true
    }

    private fun goToNavigationItem(id: Int): Boolean {
        val intent: Intent
        when (id) {
            R.id.nav_highscore_main -> {
                intent = Intent(this, StatsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            else -> {}
        }
        return true
    }


    inner class SectionsPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        (fm)!!
    ) {
        override fun getItem(position: Int): Fragment {
            return GameTypeFragment.newInstance(position)
        }

        override fun getCount(): Int {
            return validGameTypes.size
        }
    }


    class GameTypeFragment() : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_main_menu, container, false)
            val gameType = validGameTypes[requireArguments().getInt(ARG_SECTION_NUMBER)]
            val imageView = rootView.findViewById<View>(R.id.gameTypeImage) as ImageView
            imageView.setImageResource(gameType.resIDImage)
            val textView = rootView.findViewById<View>(R.id.section_label) as TextView
            textView.text = getString(gameType.stringResID)
            return rootView
        }

        companion object {

            private val ARG_SECTION_NUMBER = "section_number"


            fun newInstance(sectionNumber: Int): GameTypeFragment {
                val fragment = GameTypeFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}