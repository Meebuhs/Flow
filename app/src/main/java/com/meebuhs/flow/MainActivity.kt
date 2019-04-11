package com.meebuhs.flow

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.meebuhs.flow.Prefs.setEnum

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        setDefaultPrefs()
    }

    private fun setDefaultPrefs() {
        val prefs = Prefs.getPrefs(this)
        prefs.setEnum(Constants.PREF_MOVEMENT, Movement.STANDARD)
    }
}
