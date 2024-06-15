package com.dastanapps.visionai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.dastanapps.visionai.chat.ChatRoute
import com.dastanapps.visionai.recorder.AudioRecorder
import com.dastanapps.visionai.ui.theme.AiVisionTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val audioRecorder by lazy { AudioRecorder(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioRecorder.registerPermissionLauncher()

        setContent {
            AiVisionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ChatRoute(audioRecorder)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.onDestroy()
    }
}