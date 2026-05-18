package com.example.meetings.presentation.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.meetings.MainActivity
import com.example.meetings.R
import com.example.meetings.data.repository.MeetingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class RecordingService : Service() {

    private var isRecording = false
    private var chunkOrder = 1
    private var currentChunkDuration = 0
    private var meetingId = ""
    private var mediaRecorder: MediaRecorder? = null
    private var chunkTimer: CountDownTimer? = null
    private var isLastChunk = false
    private var currentChunkFile: File? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        meetingId = intent?.getStringExtra("meetingId") ?: ""
        isLastChunk = intent?.getBooleanExtra("isLast", false) ?: false
        val lastChunkOrder = intent?.getIntExtra("lastChunkOrder", chunkOrder) ?: chunkOrder

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("RecordingService", "No RECORD_AUDIO permission in service!")
            stopSelf()
            return START_NOT_STICKY
        }

        if (isLastChunk) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null

            sendLastChunk()
            stopSelf()
            return START_NOT_STICKY
        }

        if (!isRecording) {
            startRecording()
        }
        return START_STICKY
    }

    private fun startRecording() {
        isRecording = true
        setupMediaRecorder()
        mediaRecorder?.start()

        chunkTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentChunkDuration = (10000 - millisUntilFinished).toInt() / 1000
            }

            override fun onFinish() {
                sendChunk()
                chunkOrder++
                currentChunkDuration = 0
                startChunkTimer()
            }
        }.start()

        startForeground(1, createNotification())
    }

    private fun setupMediaRecorder(): File {
        val file = getChunkFile()
        file.parentFile?.mkdirs()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)

            try {
                prepare()
                Log.d("RecordingService", "MediaRecorder prepared for: ${file.name}")
            } catch (e: Exception) {
                Log.e("RecordingService", "Prepare failed for ${file.absolutePath}", e)
                release()
                throw e
            }
        }

        currentChunkFile = file
        return file
    }

    private fun getChunkFile(): File {
        val fileName = "meeting_${meetingId}_chunk_${chunkOrder}_${System.currentTimeMillis()}.mp4"
        return File(getExternalFilesDir(null), fileName)
    }

    private fun sendChunk() {
        Log.d("RecordingService", "Stopping recording for chunk $chunkOrder...")
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

        val file = currentChunkFile
        if (file != null) {
            Log.d("RecordingService", "Chunk file path: ${file.absolutePath}")
            Log.d("RecordingService", "File exists: ${file.exists()}, Size: ${file.length()} bytes")

            if (file.exists() && file.length() > 0) {
                try {
                    Log.d("RecordingService", "Uploading chunk $chunkOrder (duration: ${currentChunkDuration}s)")
                    scope.launch {
                        MeetingRepository().uploadAudioFile(
                            this@RecordingService,
                            meetingId,
                            chunkOrder,
                            false,
                            currentChunkDuration,
                            Uri.fromFile(file)
                        )
                        Log.d("RecordingService", "Chunk $chunkOrder uploaded successfully")
                        file.delete()
                        Log.d("RecordingService", "Chunk file deleted")
                    }
                } catch (e: Exception) {
                    Log.e("RecordingService", "Error sending chunk $chunkOrder", e)
                }
            } else {
                Log.e("RecordingService", "Chunk file is empty or missing: ${file.absolutePath}")
            }
        } else {
            Log.e("RecordingService", "No currentChunkFile reference!")
        }

        Log.d("RecordingService", "Starting new recording for next chunk...")
        currentChunkFile = setupMediaRecorder()
        mediaRecorder?.start()
    }

    private fun sendLastChunk() {
        Log.d("RecordingService", "Sending LAST chunk (order: $chunkOrder, duration: ${currentChunkDuration}s)")

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        val file = currentChunkFile
        if (file != null) {
            Log.d("RecordingService", "Last chunk file: ${file.absolutePath}, exists: ${file.exists()}, size: ${file.length()}")

            if (file.exists() && file.length() > 0) {
                scope.launch {
                    try {
                        MeetingRepository().uploadAudioFile(
                            this@RecordingService,
                            meetingId,
                            chunkOrder,
                            true,
                            currentChunkDuration,
                            Uri.fromFile(file)
                        )
                        Log.d("RecordingService", "LAST chunk uploaded successfully")
                        file.delete()
                    } catch (e: Exception) {
                        Log.e("RecordingService", "Error sending LAST chunk", e)
                    }
                }
            } else {
                Log.w("RecordingService", "Last chunk file is missing or empty")
            }
        } else {
            Log.w("RecordingService", "No currentChunkFile to send as last chunk!")
        }
    }
    private fun startChunkTimer() {
        chunkTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentChunkDuration = (10000 - millisUntilFinished).toInt() / 1000
            }

            override fun onFinish() {
                sendChunk()
                chunkOrder++
                currentChunkDuration = 0
                startChunkTimer()
            }
        }.start()
    }

    private fun createNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "recording_channel",
                "Recording Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "recording_channel")
            .setContentTitle("Запись встречи")
            .setContentText("Идет запись...")
            .setSmallIcon(R.drawable.ic_record)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        chunkTimer?.cancel()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}