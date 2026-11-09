package com.vocab.core.data.device

import android.content.Context
import android.net.Uri
import android.speech.tts.TextToSpeech
import com.vocab.core.datastore.SettingsDataSource
import com.vocab.core.domain.repository.DocumentStore
import com.vocab.core.domain.repository.Pronouncer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Reads and writes files the user picked through the system file picker. */
class SafDocumentStore @Inject constructor(@ApplicationContext private val context: Context) : DocumentStore {
    override suspend fun read(uri: String): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
            ?: throw IOException("Can't open $uri")
    }

    override suspend fun write(uri: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openOutputStream(Uri.parse(uri), "wt") ?: throw IOException("Can't open $uri")
        stream.use { it.write(bytes) }
    }
}

/** Offline pronunciation with the phone's own text-to-speech engine. */
@Singleton
class TtsPronouncer @Inject constructor(
    @ApplicationContext context: Context,
    settings: SettingsDataSource,
) : Pronouncer {
    private var ready = false
    private var locale: Locale = Locale.US
    private var rate = 1f
    private val tts = TextToSpeech(context) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) applyVoice()
    }

    init {
        settings.settings.onEach {
            locale = Locale.forLanguageTag(it.accent.localeTag)
            rate = it.speechRate
            if (ready) applyVoice()
        }.launchIn(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))
    }

    override fun speak(text: String) {
        if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    private fun applyVoice() {
        tts.language = locale
        tts.setSpeechRate(rate)
    }
}
