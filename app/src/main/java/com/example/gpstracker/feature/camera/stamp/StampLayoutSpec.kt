package com.example.gpstracker.feature.camera.stamp

/**
 * Stamp layout in design units (dp on the 393-wide Figma frame, GPS Camera 55505:1835).
 * Mirrors the on-screen `StampOverlay`; renderers multiply by [scaleForImageWidth].
 */
object StampLayoutSpec {
    const val DESIGN_FRAME_WIDTH = 393f
    const val SCREEN_MARGIN = 16f
    const val CORNER_RADIUS = 12f
    const val PADDING = 12f
    const val ROW_GAP = 8f
    const val ICON_TEXT_GAP = 6f

    const val TITLE_TEXT = 16f
    const val TITLE_LINE = 24f
    const val BADGE_TEXT = 11f
    const val BADGE_PADDING_H = 8f
    const val BADGE_PADDING_V = 2f
    const val BADGE_GAP = 8f

    const val ADDRESS_TEXT = 14f
    const val ADDRESS_LINE = 20f
    const val ADDRESS_MAX_LINES = 2

    const val CHIP_TEXT = 11f
    const val CHIP_LINE = 14f
    const val CHIP_PADDING_H = 8f
    const val CHIP_PADDING_V = 4f
    const val CHIP_RADIUS = 6f
    const val CHIP_GAP = 6f
    const val CHIP_ICON_GAP = 4f

    /** Pixels per design unit, so the stamp keeps the same proportion of the photo as on screen. */
    fun scaleForImageWidth(imageWidthPx: Int): Float = imageWidthPx / DESIGN_FRAME_WIDTH
}
