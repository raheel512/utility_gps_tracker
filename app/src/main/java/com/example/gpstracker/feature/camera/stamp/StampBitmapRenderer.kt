package com.example.gpstracker.feature.camera.stamp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.feature.camera.stamp.StampLayoutSpec as Spec
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Draws [StampRenderModel] onto a capture bitmap so it matches the on-screen stamp.
 * The panel is sized to its content. Call off the main thread. Does not recycle [bitmap].
 */
@Singleton
class StampBitmapRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun drawOnto(bitmap: Bitmap, model: StampRenderModel) {
        val s = Spec.scaleForImageWidth(bitmap.width)
        val canvas = Canvas(bitmap)

        val panelW = bitmap.width - 2 * Spec.SCREEN_MARGIN * s
        val left = Spec.SCREEN_MARGIN * s
        val contentLeft = left + Spec.PADDING * s
        val contentW = panelW - 2 * Spec.PADDING * s

        val titlePaint = textPaint(Spec.TITLE_TEXT * s, semiBold, ColorTokens.CameraOnScrim.toArgb(), 0.4f / Spec.TITLE_TEXT)
        val badgePaint = textPaint(Spec.BADGE_TEXT * s, semiBold, ColorTokens.CameraBadgeText.toArgb(), 0.55f / Spec.BADGE_TEXT)
        val addressPaint = textPaint(Spec.ADDRESS_TEXT * s, medium, ColorTokens.CameraAddressText.toArgb(), 0.14f / Spec.ADDRESS_TEXT)
        val chipPaint = textPaint(Spec.CHIP_TEXT * s, medium, ColorTokens.CameraOnScrim.toArgb(), 0.33f / Spec.CHIP_TEXT)

        // Row 1 measurements
        val badgeText = model.gmtBadge.uppercase()
        val badgeW = badgePaint.measureText(badgeText) + 2 * Spec.BADGE_PADDING_H * s
        val badgeH = lineHeight(badgePaint) + 2 * Spec.BADGE_PADDING_V * s
        val clock = icon(R.drawable.ic_gps_cam_clock, s)
        val titleRowH = max(Spec.TITLE_LINE * s, badgeH)
        val titleMaxW = contentW - badgeW - Spec.BADGE_GAP * s - clock.w - Spec.ICON_TEXT_GAP * s
        val title = TextUtils.ellipsize(model.dateTimeLine, titlePaint, titleMaxW, TextUtils.TruncateAt.END).toString()

        // Row 2 measurements
        val pin = icon(R.drawable.ic_gps_cam_pin, s)
        val addressLayout = model.addressLine?.let { address ->
            staticLayout(address, addressPaint, (contentW - pin.w - Spec.ICON_TEXT_GAP * s).roundToInt())
        }

        // Chip rows (wrap like FlowRow)
        val chips = listOfNotNull(
            model.coordinatesLine?.let { R.drawable.ic_gps_cam_coords to it },
            model.altitudeLine?.let { R.drawable.ic_gps_cam_altitude to it },
            model.speedLine?.let { R.drawable.ic_gps_cam_speed to it },
            model.pressureLine?.let { R.drawable.ic_gps_cam_weather to it },
        ).map { (res, text) ->
            val ic = icon(res, s)
            val w = 2 * Spec.CHIP_PADDING_H * s + ic.w + Spec.CHIP_ICON_GAP * s + chipPaint.measureText(text)
            val h = 2 * Spec.CHIP_PADDING_V * s + max(Spec.CHIP_LINE * s, ic.h)
            Chip(ic, text, w, h)
        }
        val chipRows = mutableListOf<MutableList<Chip>>()
        var rowW = 0f
        chips.forEach { chip ->
            val needed = if (chipRows.isEmpty() || chipRows.last().isEmpty()) chip.w else rowW + Spec.CHIP_GAP * s + chip.w
            if (chipRows.isEmpty() || needed > contentW) {
                chipRows += mutableListOf(chip)
                rowW = chip.w
            } else {
                chipRows.last() += chip
                rowW = needed
            }
        }
        val chipRowH = chips.maxOfOrNull { it.h } ?: 0f
        val chipsH = if (chipRows.isEmpty()) 0f else chipRows.size * chipRowH + (chipRows.size - 1) * Spec.CHIP_GAP * s

        // Panel height from content
        val sections = listOfNotNull(
            titleRowH,
            addressLayout?.let { max(it.height.toFloat(), pin.h) },
            chipsH.takeIf { it > 0f },
        )
        val panelH = 2 * Spec.PADDING * s + sections.sum() + (sections.size - 1) * Spec.ROW_GAP * s
        val top = bitmap.height - Spec.SCREEN_MARGIN * s - panelH
        val radius = Spec.CORNER_RADIUS * s
        canvas.drawRoundRect(
            RectF(left, top, left + panelW, top + panelH),
            radius,
            radius,
            fill(ColorTokens.CameraStampScrim.toArgb()),
        )

        var y = top + Spec.PADDING * s

        // Row 1: clock + date/time, GMT badge on the right
        val rowCenter = y + titleRowH / 2f
        clock.draw(canvas, contentLeft, rowCenter - clock.h / 2f)
        canvas.drawText(title, contentLeft + clock.w + Spec.ICON_TEXT_GAP * s, baseline(titlePaint, rowCenter), titlePaint)
        val badgeRight = contentLeft + contentW
        val badgeRect = RectF(badgeRight - badgeW, rowCenter - badgeH / 2f, badgeRight, rowCenter + badgeH / 2f)
        canvas.drawRoundRect(badgeRect, badgeH / 2f, badgeH / 2f, fill(ColorTokens.Primary500.toArgb()))
        canvas.drawText(badgeText, badgeRect.left + Spec.BADGE_PADDING_H * s, baseline(badgePaint, rowCenter), badgePaint)
        y += titleRowH + Spec.ROW_GAP * s

        // Row 2: pin + address (up to two lines)
        if (addressLayout != null) {
            val rowH = max(addressLayout.height.toFloat(), pin.h)
            val firstLineCenter = y + addressLayout.getLineBottom(0) / 2f
            pin.draw(canvas, contentLeft, firstLineCenter - pin.h / 2f)
            canvas.save()
            canvas.translate(contentLeft + pin.w + Spec.ICON_TEXT_GAP * s, y)
            addressLayout.draw(canvas)
            canvas.restore()
            y += rowH + Spec.ROW_GAP * s
        }

        // Row 3: metric chips
        val chipFill = fill(ColorTokens.CameraChipScrim.toArgb())
        val chipRadius = Spec.CHIP_RADIUS * s
        chipRows.forEach { row ->
            var x = contentLeft
            row.forEach { chip ->
                val rect = RectF(x, y, x + chip.w, y + chipRowH)
                canvas.drawRoundRect(rect, chipRadius, chipRadius, chipFill)
                val cy = rect.centerY()
                val iconX = x + Spec.CHIP_PADDING_H * s
                chip.icon.draw(canvas, iconX, cy - chip.icon.h / 2f)
                canvas.drawText(
                    chip.text,
                    iconX + chip.icon.w + Spec.CHIP_ICON_GAP * s,
                    baseline(chipPaint, cy),
                    chipPaint,
                )
                x += chip.w + Spec.CHIP_GAP * s
            }
            y += chipRowH + Spec.CHIP_GAP * s
        }
    }

    private class Chip(val icon: ScaledIcon, val text: String, val w: Float, val h: Float)

    private class ScaledIcon(private val drawable: Drawable, val w: Float, val h: Float) {
        fun draw(canvas: Canvas, x: Float, y: Float) {
            drawable.setBounds(0, 0, w.roundToInt(), h.roundToInt())
            canvas.save()
            canvas.translate(x, y)
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    /** Vector drawables use their dp size as design units, scaled to the photo. */
    private fun icon(@DrawableRes res: Int, scale: Float): ScaledIcon {
        val drawable = requireNotNull(ContextCompat.getDrawable(context, res)).mutate()
        val density = context.resources.displayMetrics.density
        return ScaledIcon(
            drawable,
            drawable.intrinsicWidth / density * scale,
            drawable.intrinsicHeight / density * scale,
        )
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(Spec.ADDRESS_MAX_LINES)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setLineSpacing(Spec.ADDRESS_LINE / Spec.ADDRESS_TEXT * paint.textSize - lineHeight(paint), 1f)
            .setIncludePad(false)
            .build()

    private fun textPaint(sizePx: Float, typeface: Typeface, color: Int, letterSpacingEm: Float) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = sizePx
            this.typeface = typeface
            letterSpacing = letterSpacingEm
        }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private fun lineHeight(paint: Paint): Float = paint.descent() - paint.ascent()

    private fun baseline(paint: Paint, centerY: Float): Float = centerY - (paint.descent() + paint.ascent()) / 2f

    private val semiBold: Typeface = weighted(600, Typeface.DEFAULT_BOLD)
    private val medium: Typeface = weighted(500, Typeface.DEFAULT)

    private fun weighted(weight: Int, fallback: Typeface): Typeface =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface.create(Typeface.DEFAULT, weight, false)
        } else {
            fallback
        }
}
