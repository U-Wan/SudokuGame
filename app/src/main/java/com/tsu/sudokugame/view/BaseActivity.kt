package com.tsu.sudokugame.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.tsu.sudokugame.ui.view.R

open class BaseActivity : AppCompatActivity() {
    @JvmField
    protected var mHandler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = Handler()
        overridePendingTransition(0, 0)
    }

    public override fun onResume() {
        super.onResume()
        val mainContent = findViewById<View>(R.id.main_content)
        if (mainContent != null) {
            mainContent.animate().alpha(1f).duration =
                MAIN_CONTENT_FADEIN_DURATION.toLong()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val mainContent = findViewById<View>(R.id.main_content)
        if (mainContent != null) {
            mainContent.alpha = 0f
            mainContent.animate().alpha(1f).duration =
                MAIN_CONTENT_FADEIN_DURATION.toLong()
        }
    }

    companion object {
        const val NAVDRAWER_LAUNCH_DELAY = 250
        const val MAIN_CONTENT_FADEOUT_DURATION = 150
        const val MAIN_CONTENT_FADEIN_DURATION = 250
    }
}