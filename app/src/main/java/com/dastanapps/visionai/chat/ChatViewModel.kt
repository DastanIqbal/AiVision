package com.dastanapps.visionai.chat

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dastanapps.openai.whisper.Loader
import com.dastanapps.visionai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import darren.googlecloudtts.GoogleCloudTTSFactory
import darren.googlecloudtts.parameter.AudioConfig
import darren.googlecloudtts.parameter.AudioEncoding
import darren.googlecloudtts.parameter.VoiceSelectionParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = Participant.MODEL.name) { text("Your name is Vision AI, and you always give short answer") },
            content(role = Participant.MODEL.name) { text("Hi, I am Vision AI.\nHow can I help you?") }
        )
    )

    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.drop(1).map { content ->
            // Map the initial messages
            ChatMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == Participant.MODEL.name) Participant.MODEL else Participant.USER,
                isPending = false
            )
        }))
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()


    private val googleCloudTTS by lazy {
        GoogleCloudTTSFactory.create(BuildConfig.GoogleCloudApiKey)
    }

    var audioFilePath: String = ""

    fun sendMessage(userMessage: String) {
        // Add a pending message
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                participant = Participant.USER,
                isPending = true
            )
        )

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)
                response.functionCalls.let { functionCalls ->
                    functionCalls.forEach {  functionCall ->
                        val matchedFunction = generativeModel.tools?.flatMap { it.functionDeclarations }
                            ?.first { it.name ==  functionCall.name }
                        functionCalling(functionCall, matchedFunction)
                        return@launch
                    }
                }

                _uiState.value.replaceLastPendingMessage()

                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        ChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                            isPending = false
                        )
                    )

                    viewModelScope.launch(Dispatchers.IO) {
                        try {
//                            playVoice(modelResponse)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            postError(e)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                postError(e)
            }
        }
    }

    private suspend fun functionCalling(functionCall: FunctionCallPart, function: FunctionDeclaration?){
        val apiResponse: JSONObject =
            function?.execute(functionCall) ?: JSONObject()
        val functionResponse = chat.sendMessage(
            content(role = Participant.FUNCTION.name.lowercase()) {
                part(FunctionResponsePart( functionCall.name, apiResponse))
            }
        )
        functionResponse.text?.run {
            _uiState.value.replaceLastPendingMessage()
            _uiState.value.addMessage(
                ChatMessage(
                    text = functionResponse.text ?: "",
                    participant = Participant.FUNCTION,
                    isPending = false
                )
            )
        }
    }

    fun postError(e: Exception) {
        Log.d("ChatViewModel", "playVoice: ${e.localizedMessage}")
        _uiState.value.replaceLastPendingMessage()
        _uiState.value.addMessage(
            ChatMessage(
                text = e.localizedMessage,
                participant = Participant.ERROR
            )
        )
    }

    fun transcribeVoiceToText(assets: AssetManager, outputFile: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = Loader.loadModelJNI(assets, outputFile, 1)
                _uiState.value.replaceLastPendingMessage()
                response?.let { modelResponse ->
                    sendMessage(modelResponse)
                }
            } catch (e: Exception) {
                Log.d("ChatViewModel", "playVoice: ${e.localizedMessage}")
                postError(e)
            }
        }
    }

    private fun playVoice(text: String) {
        Log.d("ChatViewModel", "playVoice: $text")
//        val voicesList = googleCloudTTS.load()
//        val languageCode = voicesList.languageCodes[0]
//        val voiceName = voicesList.getVoiceNames(languageCode)[0]

        val languageCode = "en-US"
        val voiceName = "en-US-Journey-F"

        // Set languageCode and voiceName, Rate and pitch parameter.
        googleCloudTTS.setVoiceSelectionParams(
            VoiceSelectionParams(
                languageCode,
                voiceName
            )
        ).setAudioConfig(AudioConfig(AudioEncoding.MP3, 1f, 0f))

        // start speak
        googleCloudTTS.start(text, audioFilePath)


//        // stop speak
//        googleCloudTTS.stop()
//
//
//        // pause speak
//        googleCloudTTS.pause()
//
//
//        // resume speak
//        googleCloudTTS.resume()
    }
}
