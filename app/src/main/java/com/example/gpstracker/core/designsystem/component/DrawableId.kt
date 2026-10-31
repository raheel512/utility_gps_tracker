package com.example.gpstracker.core.designsystem.component

import android.content.Context
import androidx.annotation.DrawableRes

/**
 * Resolves a drawable by name so missing assets do not break compilation.
 * Returns null when the resource is not yet added under res/drawable.
 */
@DrawableRes
fun Context.drawableIdOrNull(name: String): Int? {
    val id = resources.getIdentifier(name, "drawable", packageName)
    return id.takeIf { it != 0 }
}
