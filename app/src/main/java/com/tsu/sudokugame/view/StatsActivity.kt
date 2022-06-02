package com.tsu.sudokugame.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tsu.sudokugame.model.data.HighscoreInfoContainer
import com.tsu.sudokugame.model.game.GameDifficulty
import com.tsu.sudokugame.model.game.GameType.Companion.validGameTypes
import com.tsu.sudokugame.ui.view.R
import com.tsu.sudokugame.controler.SaveLoadStatistics


class StatsActivity : BaseActivity() {
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setTitle(R.string.menu_highscore)
        actionBar.setDisplayHomeAsUpEnabled(true)


        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = findViewById<View>(R.id.main_content) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_stats, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                SaveLoadStatistics.resetStats(this)
                mSectionsPagerAdapter!!.refresh(this)
                return true
            }

            R.id.go_home -> {
                finish()
                val i = Intent(applicationContext, MainActivity::class.java)
                startActivity(i)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(private val fm: FragmentManager) : FragmentPagerAdapter(
        fm
    ) {
        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position)
        }

        override fun getCount(): Int {
            return validGameTypes.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return getString(validGameTypes[position].stringResID)
        }

        fun refresh(context: Context?) {
            for (f in fm.fragments) {
                if (f is PlaceholderFragment) {
                    f.refresh(context)
                }
            }
        }
    }

    class PlaceholderFragment : Fragment() {
        private var rootView: View? = null
        private var difficultyView: TextView? = null
        private var averageTimeView: TextView? = null
        private var minTimeView: TextView? = null
        private var difficultyBarView: RatingBar? = null
        private val s: String? = null
        private var t = 0
        private var totalTime = 0
        private var totalGames = 0
        private var totalHints = 0
        fun refresh(context: Context?) {
            resetGeneral()
            val s = SaveLoadStatistics(requireContext())
            val stats = s.loadStats(validGameTypes[requireArguments().getInt(ARG_SECTION_NUMBER)])
            var j = 0
            for (i in stats) {
                updateGeneralInfo(i.time, i.numberOfGames, i.numberOfHintsUsed)
                setStats(i, j++)
            }
            setGeneralInfo()
        }

        private fun resetGeneral() {
            totalTime = 0
            totalHints = 0
            totalGames = 0
        }

        private fun formatTime(totalTime: Int): String {
            if (totalTime == 0) return "-"
            val seconds = totalTime % 60
            val minutes = (totalTime - seconds) / 60 % 60
            val hours = (totalTime - minutes - seconds) / 3600
            val h: String
            val m: String
            val s: String
            s = if (seconds < 10) "0$seconds" else seconds.toString()
            m = if (minutes < 10) "0$minutes" else minutes.toString()
            h = if (hours < 10) "0$hours" else hours.toString()
            return "$h:$m:$s"
        }

        private fun updateGeneralInfo(time: Int, games: Int, hints: Int) {
            totalHints += hints
            totalGames += games
            totalTime += time
        }

        private fun setGeneralInfo() {
            var generalInfoView: TextView
            generalInfoView = rootView!!.findViewById<View>(R.id.numb_of_hints) as TextView
            generalInfoView.text = totalHints.toString()
            generalInfoView = rootView!!.findViewById<View>(R.id.numb_of_total_games) as TextView
            generalInfoView.text = totalGames.toString()
            generalInfoView = rootView!!.findViewById<View>(R.id.numb_of_total_time) as TextView
            generalInfoView.text = formatTime(totalTime)
        }

        private fun setStats(infos: HighscoreInfoContainer, pos: Int) {
            when (pos) {
                0 -> {
                    difficultyBarView =
                        rootView!!.findViewById<View>(R.id.first_diff_bar) as RatingBar
                    difficultyView = rootView!!.findViewById<View>(R.id.first_diff_text) as TextView
                    averageTimeView = rootView!!.findViewById<View>(R.id.first_ava_time) as TextView
                    minTimeView = rootView!!.findViewById<View>(R.id.first_min_time) as TextView
                }
                1 -> {
                    difficultyBarView =
                        rootView!!.findViewById<View>(R.id.second_diff_bar) as RatingBar
                    difficultyView =
                        rootView!!.findViewById<View>(R.id.second_diff_text) as TextView
                    averageTimeView =
                        rootView!!.findViewById<View>(R.id.second_ava_time) as TextView
                    minTimeView = rootView!!.findViewById<View>(R.id.second_min_time) as TextView
                }
                2 -> {
                    difficultyBarView =
                        rootView!!.findViewById<View>(R.id.third_diff_bar) as RatingBar
                    difficultyView = rootView!!.findViewById<View>(R.id.third_diff_text) as TextView
                    averageTimeView = rootView!!.findViewById<View>(R.id.third_ava_time) as TextView
                    minTimeView = rootView!!.findViewById<View>(R.id.third_min_time) as TextView
                }
                3 -> {
                    difficultyBarView =
                        rootView!!.findViewById<View>(R.id.fourth_diff_bar) as RatingBar
                    difficultyView =
                        rootView!!.findViewById<View>(R.id.fourth_diff_text) as TextView
                    averageTimeView =
                        rootView!!.findViewById<View>(R.id.fourth_ava_time) as TextView
                    minTimeView = rootView!!.findViewById<View>(R.id.fourth_min_time) as TextView
                }
                else -> return
            }
            difficultyBarView!!.max = GameDifficulty.validDifficultyList.size
            difficultyBarView!!.numStars = GameDifficulty.validDifficultyList.size
            difficultyBarView!!.rating = infos.difficulty!!.ordinal.toFloat()
            difficultyView!!.text = rootView!!.resources.getString(infos.difficulty!!.stringResID)
            t = if (infos.timeNoHints == 0) 0 else infos.timeNoHints / infos.numberOfGamesNoHints
            averageTimeView!!.text = formatTime(t)
            t = if (infos.minTime == Int.MAX_VALUE) 0 else infos.minTime
            minTimeView!!.text = formatTime(t)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_stats, container, false)
            this.rootView = rootView
            resetGeneral()
            val s = SaveLoadStatistics(this.requireContext())
            val stats = s.loadStats(validGameTypes[requireArguments().getInt(ARG_SECTION_NUMBER)])
            var j = 0
            for (i in stats) {
                updateGeneralInfo(i.time, i.numberOfGames, i.numberOfHintsUsed)
                setStats(i, j++)
            }
            setGeneralInfo()
            val imageView = rootView.findViewById<View>(R.id.statistic_image) as ImageView
            imageView.setImageResource(validGameTypes[requireArguments().getInt(ARG_SECTION_NUMBER)].resIDImage)
            return rootView
        }

        companion object {
            private const val ARG_SECTION_NUMBER = "section_number"
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}