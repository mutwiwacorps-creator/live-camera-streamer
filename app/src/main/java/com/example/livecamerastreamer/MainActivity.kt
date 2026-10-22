
package com.example.livecamerastreamer

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.token.TokenRequestOptions
import io.livekit.android.token.TokenSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var roomInput: EditText
    private lateinit var videoView: SurfaceViewRenderer

    private var room: Room? = null
    private var eventJob: Job? = null
    private var pendingBroadcast = false

    companion object {
        private const val PERMISSION_REQUEST = 1001
        private const val TOKEN_SERVER_ID = "bfhk-2lq926"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Live Camera Streamer"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        status = TextView(this).apply {
            text = "Enter the same room name on both phones."
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
        }
        layout.addView(status)

        roomInput = EditText(this).apply {
            hint = "Room name"
            setText("infinix-room")
            singleLine = true
        }
        layout.addView(
            roomInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        videoView = SurfaceViewRenderer(this)
        layout.addView(
            videoView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        layout.addView(makeButton("Broadcast camera and microphone") {
            pendingBroadcast = true
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    ),
                    PERMISSION_REQUEST
                )
            } else {
                startRoom(true)
            }
        })

        layout.addView(makeButton("Watch stream") {
            startRoom(false)
        })

        layout.addView(makeButton("Stop streaming / watching") {
            stopRoom()
        })

        setContentView(layout)
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { action() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == PERMISSION_REQUEST) {
            val granted = grantResults.isNotEmpty() &&
                grantResults.all {
                    it == PackageManager.PERMISSION_GRANTED
                }

            if (granted && pendingBroadcast) {
                startRoom(true)
            } else {
                status.text =
                    "Camera and microphone permissions are required to broadcast."
            }
        }
    }

    private fun startRoom(broadcast: Boolean) {
        val roomName = roomInput.text.toString().trim()

        if (roomName.isBlank()) {
            status.text = "Enter a room name first."
            return
        }

        lifecycleScope.launch {
            stopRoom()
            status.text = "Connecting to LiveKit..."

            try {
                val newRoom = LiveKit.create(applicationContext)
                room = newRoom
                newRoom.initVideoRenderer(videoView)

                eventJob = launch {
                    newRoom.events.collect { event ->
                        when (event) {
                            is RoomEvent.TrackSubscribed -> {
                                val track = event.track
                                if (track is VideoTrack) {
                                    track.addRenderer(videoView)
                                    status.text =
                                        "Connected. Receiving live video."
                                } else {
                                    status.text =
                                        "Connected. Receiving live stream."
                                }
                            }
                            else -> Unit
                        }
                    }
                }

                val tokenSource =
                    TokenSource.fromDevelopmentTokenServer(
                        TOKEN_SERVER_ID
                    )

                val response = tokenSource.fetch(
                    TokenRequestOptions(
                        roomName = roomName,
                        participantName = if (broadcast) {
                            "broadcaster"
                        } else {
                            "viewer"
                        }
                    )
                ).getOrThrow()

                newRoom.connect(
                    response.serverUrl,
                    response.participantToken
                )

                if (broadcast) {
                    newRoom.localParticipant.setCameraEnabled(true)
                    newRoom.localParticipant.setMicrophoneEnabled(true)
                    status.text =
                        "Broadcasting camera and microphone in '$roomName'."
                } else {
                    status.text =
                        "Watching room '$roomName'. Waiting for broadcaster..."
                }
            } catch (e: Exception) {
                status.text =
                    "Connection failed: ${e.message ?: "Please try again."}"
                eventJob?.cancel()
                eventJob = null
                room = null
            }
        }
    }

    private fun stopRoom() {
        eventJob?.cancel()
        eventJob = null

        val oldRoom = room
        room = null

        if (oldRoom != null) {
            lifecycleScope.launch {
                try {
                    oldRoom.disconnect()
                } catch (_: Exception) {
                    // Ignore disconnect errors while stopping.
                }
            }
        }

        status.text = "Stopped."
    }

    override fun onDestroy() {
        eventJob?.cancel()
        room?.let { currentRoom ->
            lifecycleScope.launch {
                try {
                    currentRoom.disconnect()
                } catch (_: Exception) {
                    // Ignore disconnect errors during shutdown.
                }
            }
        }
        super.onDestroy()
    }
}
