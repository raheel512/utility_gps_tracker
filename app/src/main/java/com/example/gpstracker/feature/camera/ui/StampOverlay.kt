package com.example.gpstracker.feature.camera.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.feature.camera.stamp.StampRenderModel

private val StampShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(6.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StampOverlay(
    model: StampRenderModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .clip(StampShape)
            .background(ColorTokens.CameraStampScrim)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StampIcon(R.drawable.ic_gps_cam_clock)
                Text(
                    text = model.dateTimeLine,
                    color = ColorTokens.CameraOnScrim,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.4.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .background(ColorTokens.Primary500)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = model.gmtBadge.uppercase(),
                    color = ColorTokens.CameraBadgeText,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.55.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        model.addressLine?.let { address ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StampIcon(R.drawable.ic_gps_cam_pin)
                Text(
                    text = address,
                    color = ColorTokens.CameraAddressText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        val chips = listOfNotNull(
            model.coordinatesLine?.let { R.drawable.ic_gps_cam_coords to it },
            model.altitudeLine?.let { R.drawable.ic_gps_cam_altitude to it },
            model.speedLine?.let { R.drawable.ic_gps_cam_speed to it },
            model.pressureLine?.let { R.drawable.ic_gps_cam_weather to it },
        )
        if (chips.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                chips.forEach { (icon, text) -> StampChip(icon, text) }
            }
        }
    }
}

@Composable
private fun StampIcon(@DrawableRes res: Int) {
    // Figma icons carry their own fill colours.
    Icon(painter = painterResource(res), contentDescription = null, tint = Color.Unspecified)
}

@Composable
private fun StampChip(@DrawableRes icon: Int, text: String) {
    Row(
        modifier = Modifier
            .clip(ChipShape)
            .background(ColorTokens.CameraChipScrim)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StampIcon(icon)
        Text(
            text = text,
            color = ColorTokens.CameraOnScrim,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.33.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
