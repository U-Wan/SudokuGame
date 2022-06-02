package com.tsu.sudokugame.view

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.tsu.sudokugame.ui.view.R

class WinDialog : DialogFragment() {
    private var timeString: String? = ""
    private var hintString: String? = ""
    private var isNewBestTime = false
    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        setParam(args!!.getString(ARG_TIME), args.getString(ARG_HINT), args.getBoolean(ARG_BEST))
    }

    fun setParam(timeString: String?, hintString: String?, isNewBestTime: Boolean) {
        this.timeString = timeString
        this.hintString = hintString
        this.isNewBestTime = isNewBestTime
    }

    override fun onCreateDialog(saved: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.WinDialog)
        dialog.window!!.setContentView(R.layout.win_screen_layout)
        dialog.window!!.setGravity(Gravity.CENTER_HORIZONTAL)
        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        if (isNewBestTime) {
        }
        (dialog.findViewById<View>(R.id.moveinhome) as Button).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            if (activity != null) {
                requireActivity().overridePendingTransition(0, 0)
                requireActivity().finish()
            }
        }
        return dialog
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putString(ARG_TIME, timeString)
        out.putString(ARG_HINT, hintString)
        out.putBoolean(ARG_BEST, isNewBestTime)
    }

    companion object {
        const val ARG_TIME = "WinDialog.ARG_TIME"
        const val ARG_HINT = "WinDialog.ARG_HINT"
        const val ARG_BEST = "WinDialog.ARG_BEST"
    }
}