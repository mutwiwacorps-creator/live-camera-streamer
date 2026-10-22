package com.example.livecamerastreamer

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Live Camera Streamer"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = "Ready to configure streaming"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        layout.addView(title)
        layout.addView(status)

        layout.addView(Button(this).apply {
            text = "Start Streaming"
            setOnClickListener {
                status.text =
                    "Streaming is not configured yet. Please wait for setup."
            }
        })

        layout.addView(Button(this).apply {
            text = "Stop Streaming"
            setOnClickListener {
                status.text = "Streaming stopped"
            }
        })

        setContentView(layout)
    }
}
