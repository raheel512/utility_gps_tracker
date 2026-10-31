package com.example.gpstracker.feature.camera.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

/** Camera settings — copy and layout not in design. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSettingsSheet(
    uiState: GpsCameraUiState,
    onEvent: (GpsCameraEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onEvent(GpsCameraEvent.SettingsDismissed) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.md)
                .padding(bottom = spacing.md),
        ) {
            Text(
                text = stringResource(R.string.gps_camera_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = spacing.sm),
            )
            SwitchRow(
                title = stringResource(R.string.gps_camera_settings_embed_gps),
                body = stringResource(R.string.gps_camera_settings_embed_gps_body),
                checked = uiState.embedGpsExif,
                onCheckedChange = { onEvent(GpsCameraEvent.EmbedGpsExifChanged(it)) },
            )
            SwitchRow(
                title = stringResource(R.string.gps_camera_settings_grid),
                body = null,
                checked = uiState.gridEnabled,
                onCheckedChange = { onEvent(GpsCameraEvent.ToggleGrid) },
            )
            Text(
                text = stringResource(R.string.gps_camera_settings_units),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = spacing.sm, bottom = spacing.xs),
            )
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                UnitOption(
                    label = stringResource(R.string.gps_camera_settings_units_metric),
                    selected = uiState.distanceUnit == DistanceUnit.KM,
                    onClick = { onEvent(GpsCameraEvent.DistanceUnitSelected(DistanceUnit.KM)) },
                )
                UnitOption(
                    label = stringResource(R.string.gps_camera_settings_units_imperial),
                    selected = uiState.distanceUnit == DistanceUnit.MI,
                    onClick = { onEvent(GpsCameraEvent.DistanceUnitSelected(DistanceUnit.MI)) },
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    body: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(spacing.sm))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun UnitOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(LocalSpacing.current.xs))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
