package com.example.gpstracker.feature.history.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.routes.EncodedPolyline

@Composable
fun MiniRouteCanvas(
    encodedPolyline: String,
    modifier: Modifier = Modifier,
) {
    val points = remember(encodedPolyline) { EncodedPolyline.decode(encodedPolyline) }
    val routeColor = ColorTokens.Primary500
    val startColor = ColorTokens.MapManeuverAccent
    val endColor = ColorTokens.PinDestinationA
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.size < 2) return@Canvas
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLng = points.minOf { it.longitude }
        val maxLng = points.maxOf { it.longitude }
        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)
        val pad = 16.dp.toPx()
        val usableW = (size.width - pad * 2).coerceAtLeast(1f)
        val usableH = (size.height - pad * 2).coerceAtLeast(1f)
        fun project(point: LatLngPoint): Offset {
            val x = pad + ((point.longitude - minLng) / lngSpan * usableW).toFloat()
            val y = pad + ((maxLat - point.latitude) / latSpan * usableH).toFloat()
            return Offset(x, y)
        }
        val path = Path()
        val first = project(points.first())
        path.moveTo(first.x, first.y)
        for (i in 1 until points.size) {
            val p = project(points[i])
            path.lineTo(p.x, p.y)
        }
        drawPath(
            path = path,
            color = routeColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        val start = project(points.first())
        val end = project(points.last())
        drawCircle(color = startColor, radius = 5.dp.toPx(), center = start)
        drawCircle(color = endColor, radius = 5.dp.toPx(), center = end)
    }
}
