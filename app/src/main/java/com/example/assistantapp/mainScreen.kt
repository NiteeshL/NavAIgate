package com.example.assistantapp

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPage(navController: NavHostController) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var tapCount by remember { mutableStateOf(0) }
    var currentSpeechRate by remember { mutableStateOf(1.0f) }
    val vibrator = context.getSystemService(Vibrator::class.java)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    // Initialize TextToSpeech
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.speak(
                    "Tap anywhere on the screen for instructions. Tap twice for navigation. Long press for current time.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun playConfirmationTone(pitch: Float, speed: Float) {
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0.0f)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)
        tts?.speak(" ", TextToSpeech.QUEUE_ADD, params, "tone_id")
    }

    fun handleSingleTap() {
        playConfirmationTone(1.2f, 1.0f)
        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        tts?.speak(
            "To Master the navigation through this App, follow these instructions below. " +
                    "Double Tap on the Navigation screen to stop the Navigation or vice versa. " +
                    "Right after, speak any query about anything or the environment around you. " +
                    "You also can use your earbuds or airpods by double tapping to stop or resume the navigation. " +
                    "Long press the screen to know the time. " +
                    "Now Double tap on the screen to begin",
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }

    fun handleDoubleTap() {
        playConfirmationTone(0.8f, 1.0f)
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(100, 50), -1))
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        navController.navigate("blindMode")
    }


    LaunchedEffect(tapCount) {
        when (tapCount) {
            1 -> handleSingleTap()
            2 -> handleDoubleTap()
        }
        if (tapCount > 0) {
            delay(500)
            tapCount = 0
        }
    }

    fun announceTime() {
        val currentTime = LocalDateTime.now().format(timeFormatter)
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(100, 50, 100), -1))
        tts?.speak("The current time is $currentTime", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = LocalTime.now()
            val minute = currentTime.minute
            val hour = currentTime.hour

            if (minute == 0) {
                tts?.speak("It's $hour o'clock", TextToSpeech.QUEUE_ADD, null, "chime")
            } else if (minute == 30) {
                tts?.speak("It's half past $hour", TextToSpeech.QUEUE_ADD, null, "chime")
            }

            delay(60000)
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize() // Ensures full-screen usage
        .background(Color(0xFFF4F4F4)) // Set background to #f4f4f4
    ) {
        // Background image with full viewport height
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.mainscreen)
                .decoderFactory(GifDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = "Animated blind man",
            modifier = Modifier
                .fillMaxHeight(0.8f), // Ensure the background fills the entire screen
            contentScale = ContentScale.Crop // Crop to fill the viewport properly
        )

        // Application logo at the top
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.nav)
                .crossfade(true)
                .build(),
            contentDescription = "Application logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 0.dp)
                .offset(y = (-80).dp)
                .fillMaxWidth()
                .size(400.dp)
                .alpha(animateFloatAsState(targetValue = 1f, animationSpec = tween(1000)).value),
            contentScale = ContentScale.Fit
        )

        // Clickable overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { tapCount++ },
                    onLongClick = {
                        announceTime()
                        tapCount = 0
                    }
                )
        )

        // Text at the bottom of the screen
        Text(
            text = "Made with ❤️ by Niteesh and Adityan",
            color = Color.Black,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter) // Correctly place at the bottom
                .padding(bottom = 16.dp) // Optional padding
        )
    }
}