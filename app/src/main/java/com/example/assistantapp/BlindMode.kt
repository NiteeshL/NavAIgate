package com.example.assistantapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File

@Composable
fun ReadingModeCamera(
    onImageCaptured: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Capture image once when reading mode is activated
        val file = createTempFile(context.toString()) // Create a temporary file
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    onImageCaptured(bitmap) // Pass the captured bitmap to the callback
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle error
                    exception.printStackTrace()
                }
            }
        )
    }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}
@Composable
fun BlindModeScreen() {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val scope = rememberCoroutineScope()

    // Haptic feedback patterns
    val assistantModeVibrationPattern = longArrayOf(0, 200, 100, 200) // Short vibration twice
    val readingModeVibrationPattern = longArrayOf(0, 500) // Long vibration once

    // Function to trigger vibrations
    fun triggerVibration(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            vibrator.vibrate(pattern, -1)
        }
    }

    // Text-to-Speech setup
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(context) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(1.5f) // Increase the speech rate

                // Set a specific voice (e.g., female voice)
                val availableVoices = tts.value?.voices
                val desiredVoice = availableVoices?.find { voice ->
                    voice.name.contains("female", ignoreCase = true)
                }
                if (desiredVoice != null) {
                    tts.value?.voice = desiredVoice
                }
            }
        }
    }

    // Camera and permissions
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Modes and states
    var currentMode by remember { mutableStateOf("navigation") }
    var isAssistantMode by remember { mutableStateOf(false) }
    var isReadingMode by remember { mutableStateOf(false) }
    var navigationPaused by remember { mutableStateOf(false) }
    var isMicActive by remember { mutableStateOf(false) }
    var chatResponse by remember { mutableStateOf("") }
    var readingModeResult by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf("") }
    var lastSpokenIndex by remember { mutableStateOf(0) }
    var lastProcessedTimestamp by remember { mutableStateOf(0L) }
    val frameInterval = 12000 // Process a frame every 6.5 seconds

    // Speech recognition
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    // Speech recognition listener
    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    scope.launch {
                        chatResponse = sendMessageToGeminiAI(spokenText, analysisResult)
                        tts.value?.speak(chatResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                if (navigationPaused) {
                    speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onError(error: Int) {
                if (navigationPaused) {
                    speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // Handle microphone activation when navigation is paused
    LaunchedEffect(navigationPaused) {
        if (navigationPaused) {
            isMicActive = true
            speechRecognizer.startListening(speechIntent)
        } else {
            isMicActive = false
            speechRecognizer.stopListening()
            chatResponse = ""
        }
    }

    // Request permissions if not granted
    if (!hasPermission) {
        ActivityCompat.requestPermissions(
            (context as Activity),
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            1
        )
    }

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double tap for assistant mode
                        triggerVibration(assistantModeVibrationPattern)
                        if (!isReadingMode) {
                            navigationPaused = !navigationPaused
                            isAssistantMode = navigationPaused
                            if (navigationPaused) {
                                tts.value?.stop()
                                currentMode = "assistant"
                                tts.value?.speak("Assistant mode activated.", TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                tts.value?.stop()
                                currentMode = "navigation"
                                chatResponse = ""
                                tts.value?.speak("Assistant mode deactivated.", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    },
                    onLongPress = {
                        // Long press for reading mode
                        triggerVibration(readingModeVibrationPattern)
                        if (!isAssistantMode) {
                            isReadingMode = !isReadingMode
                            if (isReadingMode) {
                                tts.value?.stop()
                                currentMode = "reading"
                                navigationPaused = true
                                tts.value?.speak("Entering reading mode", TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                tts.value?.stop()
                                currentMode = "navigation"
                                readingModeResult = ""
                                navigationPaused = false
                                tts.value?.speak("Exiting reading mode", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        } else {
                            // Exit assistant mode and enter navigation mode
                            tts.value?.stop()
                            isAssistantMode = false
                            navigationPaused = false
                            isReadingMode = false
                            currentMode = "navigation"
                            chatResponse = ""
                            tts.value?.speak("Exiting assistant mode, entering navigation mode", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isReadingMode) {
                ReadingModeCamera(
                    onImageCaptured = { bitmap ->
                        capturedImage = bitmap
                        scope.launch {
                            readingModeResult = ""
                            sendFrameToGemini2AI(bitmap, { partialResult ->
                                readingModeResult += partialResult
                                tts.value?.speak(partialResult, TextToSpeech.QUEUE_ADD, null, null)
                            }, { error -> /* Handle error */ })
                        }
                    },
                    cameraExecutor = cameraExecutor
                )
            } else if (!navigationPaused) {
                CameraPreviewWithAnalysis { imageProxy ->
                    val currentTimestamp = System.currentTimeMillis()
                    if (currentTimestamp - lastProcessedTimestamp >= frameInterval) {
                        scope.launch {
                            val bitmap = imageProxy.toBitmap()
                            if (bitmap != null) {
                                sendFrameToGeminiAI(bitmap, { partialResult ->
                                    analysisResult += " $partialResult"
                                    val newText = analysisResult.substring(lastSpokenIndex)
                                    tts.value?.speak(newText, TextToSpeech.QUEUE_ADD, null, null)
                                    lastSpokenIndex = analysisResult.length
                                }, { error -> /* Handle error */ })
                                lastProcessedTimestamp = currentTimestamp
                            }
                            imageProxy.close()
                        }
                    } else {
                        imageProxy.close()
                    }
                }
            }

            // Overlay for AI responses
            AIResponseOverlay(
                currentMode = currentMode,
                navigationResponse = analysisResult,
                response = analysisResult,
                chatResponse = chatResponse,
                readingModeResult = readingModeResult,
                tts = tts.value,
                lastSpokenIndex = lastSpokenIndex
            )
        }
    }
}