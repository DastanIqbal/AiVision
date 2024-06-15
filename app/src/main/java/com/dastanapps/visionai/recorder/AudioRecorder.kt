package com.dastanapps.visionai.recorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

/**
 *
 * Created by Iqbal Ahmed on 15/06/2024
 *
 */

class AudioRecorder(
    private val context: ComponentActivity
) {
    private var mRecorder: WavAudioRecorder? = null
    var isRecording = mutableStateOf(false)
    val outputFile: String = "${context.externalCacheDir?.absolutePath}/recording.wav"

    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    fun registerPermissionLauncher() {
        requestPermissionLauncher = context.registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startRecording()
            } else {
                Toast.makeText(context, "Allow Mic Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun recordAudio() {
        File(outputFile).delete()
        if (isRecording.value) {
            stopRecording()
        } else {
            if (checkPermissions()) {
                startRecording()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecording() {
        if (mRecorder == null) {
            mRecorder = WavAudioRecorder.instanse
        }
        if (WavAudioRecorder.State.INITIALIZING == mRecorder?.state) {
            Log.e(TAG, "INITIALIZING $outputFile")
            mRecorder?.setOutputFile(outputFile)
            mRecorder?.prepare()
            mRecorder?.start()
            isRecording.value = true
        } else if (WavAudioRecorder.State.ERROR == mRecorder?.state) {
            Log.e(TAG, "ERROR")
            mRecorder?.release()
            mRecorder = WavAudioRecorder.instanse
            mRecorder?.setOutputFile(outputFile)
            isRecording.value = false
        } else {
            Log.d(TAG, "On Record Stop Click")
            stopRecording()
        }
    }

    fun stopRecording() {
        isRecording.value = false

        mRecorder?.stop()
        mRecorder?.reset()
        mRecorder?.release()
        mRecorder = WavAudioRecorder.instanse
    }

    fun onDestroy() {
        if (isRecording.value) {
            stopRecording()
        }
    }

    companion object{
        private val TAG = AudioRecorder::class.java.simpleName
    }
}