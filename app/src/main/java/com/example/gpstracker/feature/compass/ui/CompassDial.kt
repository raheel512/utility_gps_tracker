package com.example.gpstracker.feature.compass.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.appShadow
import kotlin.math.cos
import kotlin.math.sin

private val DialBorder = Color(0xE6E2E8F0)
private val TickBlue = Color(0x592563EB)
private val RingBlue = Color(0x99BFDBFE)
private val DashedRing = Color(0xFFDBEAFE)
private val CardinalBlue = Color(0xFF2563EB)
private val Ink = Color(0xFF0F172A)
private val DegreeInk = Color(0xFF94A3B8)
private val NorthLight = Color(0xFFEF4444)
private val NorthDark = Color(0xFFDC2626)
private val SouthLight = Color(0xFF2563EB)
private val SouthDark = Color(0xFF1D4ED8)
private val HubRing = Color(0xFFE2E8F0)
private val HubStroke = Color(0xFF94A3B8)
private val IndexRed = Color(0xFFDC2626)

private val DegreeLabels = intArrayOf(30, 60, 120, 150, 210, 240, 300, 330)

@Composable
fun CompassDial(
    headingDegrees: Float?,
    theme: CompassTheme,
    modifier: Modifier = Modifier,
) {
    val rotation = rememberDialRotation(headingDegrees)
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .appShadow(AppShadow.Hero, CircleShape)
                .clip(CircleShape)
                .background(ColorTokens.Surface)
                .border(1.dp, DialBorder, CircleShape),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
                .clip(CircleShape),
        ) {
            if (theme == CompassTheme.Classic) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val scale = size.minDimension / 304.dp.toPx()
                    fun px(designDp: Float) = designDp.dp.toPx() * scale
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    drawCircle(
                        color = RingBlue,
                        radius = radius - px(20f),
                        center = center,
                        style = Stroke(width = px(1f)),
                    )
                    drawCircle(
                        color = DashedRing,
                        radius = radius - px(40f),
                        center = center,
                        style = Stroke(
                            width = px(1f),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(px(3f), px(4f)),
                            ),
                        ),
                    )

                    val tickOuter = radius - px(10f)
                    val tickInner = tickOuter - px(12f)
                    for (degrees in 0 until 360 step 5) {
                        val angle = Math.toRadians(degrees - 90.0)
                        val cosA = cos(angle).toFloat()
                        val sinA = sin(angle).toFloat()
                        drawLine(
                            color = TickBlue,
                            start = Offset(center.x + cosA * tickInner, center.y + sinA * tickInner),
                            end = Offset(center.x + cosA * tickOuter, center.y + sinA * tickOuter),
                            strokeWidth = px(1.1f),
                            cap = StrokeCap.Butt,
                        )
                    }

                    drawCardinalMarks(center, px(6f), px(12f), px(2f))
                    drawRoseLabels(center, radius, scale)
                    drawNeedle(center, px(17.28f), px(94.08f), px(5f))
                    drawHub(center, px(14f), px(7f), px(3f), px(2f), px(1f))
                }
            } else {
                Image(
                    painter = painterResource(theme.faceRes()),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                )
            }
        }
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.minDimension / 304.dp.toPx()
            val width = 12.dp.toPx() * scale
            val height = 9.dp.toPx() * scale
            val cx = size.width / 2f
            val top = 1.dp.toPx() * scale
            val path = Path().apply {
                moveTo(cx, top + height)
                lineTo(cx - width / 2f, top)
                lineTo(cx + width / 2f, top)
                close()
            }
            drawPath(path, IndexRed)
        }
    }
}

@Composable
private fun rememberDialRotation(headingDegrees: Float?): Float {
    var rotation by remember { mutableFloatStateOf(0f) }
    var hasHeading by remember { mutableStateOf(false) }
    LaunchedEffect(headingDegrees) {
        val heading = headingDegrees ?: return@LaunchedEffect
        val desired = -heading
        if (!hasHeading) {
            rotation = desired
            hasHeading = true
        } else {
            var delta = ((desired - rotation) % 360f + 360f) % 360f
            if (delta > 180f) delta -= 360f
            rotation += delta
        }
    }
    return rotation
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCardinalMarks(
    center: Offset,
    inset: Float,
    length: Float,
    thickness: Float,
) {
    drawLine(
        CardinalBlue,
        Offset(center.x, inset),
        Offset(center.x, inset + length),
        strokeWidth = thickness,
    )
    drawLine(
        CardinalBlue,
        Offset(center.x, size.height - inset),
        Offset(center.x, size.height - inset - length),
        strokeWidth = thickness,
    )
    drawLine(
        CardinalBlue,
        Offset(inset, center.y),
        Offset(inset + length, center.y),
        strokeWidth = thickness,
    )
    drawLine(
        CardinalBlue,
        Offset(size.width - inset, center.y),
        Offset(size.width - inset - length, center.y),
        strokeWidth = thickness,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoseLabels(
    center: Offset,
    radius: Float,
    scale: Float,
) {
    val cardinalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 18.sp.toPx() * scale
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
        letterSpacing = 0.05f
    }
    val degreePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#94A3B8")
        textSize = 11.sp.toPx() * scale
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val letters = arrayOf(0 to "N", 90 to "E", 180 to "S", 270 to "W")
        val letterRadius = radius - 36.dp.toPx() * scale
        for ((degrees, letter) in letters) {
            val angle = Math.toRadians(degrees - 90.0)
            val x = center.x + cos(angle).toFloat() * letterRadius
            val y = center.y + sin(angle).toFloat() * letterRadius
            val baseline = y - (cardinalPaint.ascent() + cardinalPaint.descent()) / 2f
            native.drawText(letter, x, baseline, cardinalPaint)
        }
        val numberRadius = radius - 56.dp.toPx() * scale
        for (degrees in DegreeLabels) {
            val angle = Math.toRadians(degrees - 90.0)
            val x = center.x + cos(angle).toFloat() * numberRadius
            val y = center.y + sin(angle).toFloat() * numberRadius
            val baseline = y - (degreePaint.ascent() + degreePaint.descent()) / 2f
            native.drawText(degrees.toString(), x, baseline, degreePaint)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeedle(
    center: Offset,
    width: Float,
    height: Float,
    gap: Float,
) {
    val left = center.x - width / 2f
    val right = center.x + width / 2f
    val northBase = center.y - gap
    val northTip = northBase - height
    drawPath(halfNeedle(center.x, northTip, left, northBase), NorthLight)
    drawPath(halfNeedle(center.x, northTip, right, northBase), NorthDark)
    val southBase = center.y + gap
    val southTip = southBase + height
    drawPath(halfNeedle(center.x, southTip, left, southBase), SouthLight)
    drawPath(halfNeedle(center.x, southTip, right, southBase), SouthDark)
}

private fun halfNeedle(tipX: Float, tipY: Float, sideX: Float, baseY: Float): Path =
    Path().apply {
        moveTo(tipX, tipY)
        lineTo(sideX, baseY)
        lineTo(tipX, baseY)
        close()
    }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHub(
    center: Offset,
    outer: Float,
    middle: Float,
    dot: Float,
    outerStroke: Float,
    middleStroke: Float,
) {
    drawCircle(Ink, outer, center)
    drawCircle(Color.White, outer, center, style = Stroke(outerStroke))
    drawCircle(HubRing, middle, center)
    drawCircle(HubStroke, middle, center, style = Stroke(middleStroke))
    drawCircle(Ink, dot, center)
}

private fun CompassTheme.faceRes(): Int = when (this) {
    CompassTheme.Classic -> R.drawable.compass_face_classic
    CompassTheme.Night -> R.drawable.compass_face_night
    CompassTheme.Terrain -> R.drawable.compass_face_terrain
    CompassTheme.Nautical -> R.drawable.compass_face_nautical
}
