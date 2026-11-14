package com.niktype.keyboard.voice

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niktype.keyboard.R
import com.niktype.keyboard.keyboard.drawMicIcon

/**
 * اکتیویتی شفاف تایپ صوتی: از تشخیص گفتار خود دستگاه استفاده می‌کند.
 * متن نتیجه فقط به‌صورت محلی از طریق VoiceResultBus به کیبورد می‌رسد.
 */
class VoiceInputActivity : ComponentActivity() {

    private var speech: SpeechRecognizer? = null
    private var language: String = "fa-IR"

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening() else {
                Toast.makeText(this, getString(R.string.voice_no_permission), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VoiceResultBus.reset()
        language = intent?.getStringExtra("lang") ?: "fa-IR"

        setContent { VoiceOverlay() }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onDestroy() {
        speech?.destroy()
        speech = null
        super.onDestroy()
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, getString(R.string.voice_failed), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) VoiceResultBus.post(text)
                finish()
            }

            override fun onError(error: Int) {
                Toast.makeText(this@VoiceInputActivity, getString(R.string.voice_failed), Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        speech?.startListening(intent)
    }

    @Composable
    private fun VoiceOverlay() {
        val transition = rememberInfiniteTransition(label = "mic")
        val pulse by transition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
            label = "pulse",
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(110.dp)
                        .scale(pulse)
                        .background(
                            Color(0xFF7C4DFF).copy(alpha = 0.9f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val density = LocalDensity.current
                    Canvas(Modifier.size(46.dp)) {
                        val px = with(density) { 46.dp.toPx() }
                        drawMicIcon(
                            center = androidx.compose.ui.geometry.Offset(px / 2f, px / 2f),
                            px,
                            Color.White,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.voice_listen),
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }
        }
    }
}
