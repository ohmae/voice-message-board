/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.Toaster

private const val RMS_DB_MAX = 10.0f
private const val RMS_DB_MIN = -2.12f
private const val HISTORY_SIZE = 10
private val RECOGNIZER_ICON_AREA_SIZE = 192.dp
private val RECOGNIZER_ICON_CIRCLE_SIZE = 72.dp

fun normalizeRms(
    rms: Float,
): Float = ((rms - RMS_DB_MIN) / (RMS_DB_MAX - RMS_DB_MIN)).coerceIn(0f, 1f)

@Composable
fun RecognizerDialog(
    onResult: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var volume by remember { mutableFloatStateOf(0f) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    DisposableEffect(context) {
        val applicationContext = context.applicationContext
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(
                params: Bundle?,
            ) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(
                buffer: ByteArray?,
            ) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(
                eventType: Int,
                params: Bundle?,
            ) = Unit

            override fun onRmsChanged(
                rms: Float,
            ) {
                volume = normalizeRms(rms)
            }

            override fun onError(
                error: Int,
            ) {
                Toaster.show(context, R.string.toast_voice_input_fail)
                currentOnDismiss()
            }

            override fun onPartialResults(
                results: Bundle?,
            ) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
                if (list.isNotEmpty() && list[0].isNotEmpty()) {
                    text = list[0]
                }
            }

            override fun onResults(
                results: Bundle?,
            ) {
                currentOnDismiss()
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
                if (list.isNotEmpty()) {
                    currentOnResult(list)
                }
            }
        }

        val speechRecognizer = runCatching {
            SpeechRecognizer.createSpeechRecognizer(applicationContext)?.apply {
                setRecognitionListener(listener)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }
                startListening(intent)
            }
        }.getOrNull()

        if (speechRecognizer == null) {
            Toaster.show(context, R.string.toast_fail_to_start_voice_input)
            currentOnDismiss()
        } else {
            recognizer = speechRecognizer
        }

        onDispose {
            runCatching { speechRecognizer?.destroy() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        RecognizerDialogContent(
            text = text.ifEmpty { stringResource(R.string.recognizer_title) },
            volume = volume,
            onClickBeatingView = { recognizer?.stopListening() },
        )
    }
}

@Composable
fun RecognizerDialogContent(
    text: String,
    volume: Float,
    onClickBeatingView: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            WaveEffect(
                volume = volume,
                modifier = Modifier.fillMaxSize(),
            )
            BeatingEffect(
                volume = volume,
                onClick = onClickBeatingView,
                modifier = Modifier
                    .size(RECOGNIZER_ICON_AREA_SIZE)
                    .align(Alignment.Center),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            )
        }
    }
}

@Composable
private fun BeatingEffect(
    volume: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val beatingColor = Color(0x202962FF)
    val density = LocalDensity.current

    val radiusMinPx = with(density) { RECOGNIZER_ICON_CIRCLE_SIZE.toPx() / 2f }
    val radiusMaxPx = with(density) { RECOGNIZER_ICON_AREA_SIZE.toPx() / 2f }
    val radiusRangePx = radiusMaxPx - radiusMinPx

    val animators = remember {
        listOf(
            Animatable(0f),
            Animatable(0f),
            Animatable(0f),
        )
    }
    val radiusState = remember { mutableStateListOf(0f, 0f, 0f) }
    val radiusTargets = remember { mutableStateListOf(0f, 0f, 0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(volume) {
        for (i in 0 until 3) {
            if (radiusState[i] < volume) {
                radiusState[i] = volume
            }
            if (!animators[i].isRunning) {
                val vol = radiusState[i]
                radiusState[i] = 0f
                val target = radiusRangePx * vol / 6f * (1 + i)
                val duration = 20 + 200 * (i + 1)
                scope.launch {
                    radiusTargets[i] = target
                    animators[i].snapTo(0f)
                    animators[i].animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = duration,
                            easing = LinearEasing,
                        ),
                    )
                    animators[i].snapTo(0f)
                    radiusTargets[i] = 0f
                }
            }
        }
    }

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            var r = radiusMinPx
            for (i in 0 until 3) {
                val progress = animators[i].value
                if (progress > 0f) {
                    val easeFactor = 1f - (progress * 2f - 1f).let { it * it }
                    r += radiusTargets[i] * easeFactor
                }
                drawCircle(
                    color = beatingColor,
                    radius = r,
                    center = Offset(cx, cy),
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_circle),
            contentDescription = null,
            modifier = Modifier.size(RECOGNIZER_ICON_CIRCLE_SIZE),
        )
        Image(
            painter = painterResource(R.drawable.ic_voice),
            contentDescription = null,
        )
    }
}

@Composable
private fun WaveEffect(
    volume: Float,
    modifier: Modifier = Modifier,
) {
    val wave1Color = Color(0xC0448AFF)
    val wave2Color = Color(0x80448AFF)
    val wave3Color = Color(0x40448AFF)

    val density = LocalDensity.current
    val wave1ScalePx = with(density) { 20.0f.dp.toPx() }
    val wave2ScalePx = with(density) { 17.5f.dp.toPx() }
    val wave3ScalePx = with(density) { 15.0f.dp.toPx() }

    val wave1CenterPx = with(density) { 50.dp.toPx() }
    val wave2CenterPx = with(density) { 60.dp.toPx() }
    val wave3CenterPx = with(density) { 70.dp.toPx() }

    val queue = remember {
        mutableStateListOf<Float>().also { q ->
            if (q.isEmpty()) {
                repeat(HISTORY_SIZE) { q.add(0f) }
            }
        }
    }
    var sign by remember { mutableIntStateOf(1) }
    var maxAmplitude by remember { mutableFloatStateOf(0f) }

    val phaseAnimatable = remember { Animatable(0f) }

    LaunchedEffect(volume) {
        if (volume > maxAmplitude) {
            maxAmplitude = volume
        }
    }

    LaunchedEffect(Unit) {
        var prevValue: Float
        while (true) {
            phaseAnimatable.snapTo(0f)
            prevValue = 0f
            phaseAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = LinearEasing),
            ) {
                if (value < prevValue) {
                    sign *= -1
                    if (queue.isNotEmpty()) {
                        queue.removeAt(queue.size - 1)
                    }
                    queue.add(0, maxAmplitude * sign)
                    maxAmplitude = 0f
                }
                prevValue = value
            }
            sign *= -1
            if (queue.isNotEmpty()) {
                queue.removeAt(queue.size - 1)
            }
            queue.add(0, maxAmplitude * sign)
            maxAmplitude = 0f
        }
    }

    val phase = phaseAnimatable.value

    Canvas(modifier = modifier) {
        fun drawWave(
            color: Color,
            cy: Float,
            offset: Float,
            scale: Float,
        ) {
            val width = size.width
            val height = size.height
            val waveLength = width / (2f + offset * 2f)
            val handleLength = waveLength / 3f
            var xp = width + (1.0f - phase + offset) * waveLength
            var yp = (queue.getOrNull(0) ?: 0f) * scale + cy

            val path = Path()
            path.moveTo(xp, yp)
            for (i in 1 until HISTORY_SIZE) {
                val xn = xp - waveLength
                val x1 = xp - handleLength
                val x2 = xn + handleLength
                val yn = (queue.getOrNull(i) ?: 0f) * scale + cy
                path.cubicTo(x1, yp, x2, yn, xn, yn)
                if (xn < 0f) break
                xp = xn
                yp = yn
            }
            path.lineTo(0f, height)
            path.lineTo(width, height)
            path.close()

            drawPath(path = path, color = color)
        }

        val height = size.height
        drawWave(wave3Color, height - wave3CenterPx, 1.0f, wave3ScalePx)
        drawWave(wave2Color, height - wave2CenterPx, 0.5f, wave2ScalePx)
        drawWave(wave1Color, height - wave1CenterPx, 0.0f, wave1ScalePx)
    }
}

@Preview(showBackground = true)
@Composable
private fun RecognizerDialogContentPreview() {
    MaterialTheme {
        RecognizerDialogContent(
            text = "音声認識中...",
            volume = 0.5f,
            onClickBeatingView = {},
        )
    }
}
