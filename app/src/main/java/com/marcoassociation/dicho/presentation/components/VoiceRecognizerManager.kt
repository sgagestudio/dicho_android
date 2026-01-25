package com.marcoassociation.dicho.presentation.components

import android.content.Context
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

class VoiceRecognizerManager(
    private val context: Context,
    private val listener: RecognitionListener
) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    init {
        speechRecognizer.setRecognitionListener(listener)
    }

    fun startListening(intent: android.content.Intent) {
        // TODO: Provide SpeechRecognizer intent configuration based on language and offline mode.
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun release() {
        speechRecognizer.destroy()
    }
}
