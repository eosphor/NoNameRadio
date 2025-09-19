package com.eosphor.nonameradio.compose.components

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eosphor.nonameradio.LegacySettingsActivity
import com.eosphor.nonameradio.R

private data class SettingsCategory(
    @StringRes val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val preferenceRoot: String?
)

@Composable
fun ExactOriginalSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories = listOf(
        SettingsCategory(R.string.settings_appearance, Icons.Filled.Palette, "pref_category_ui"),
        SettingsCategory(R.string.settings_startup_behaviour, Icons.Filled.PlayArrow, "pref_category_startup"),
        SettingsCategory(R.string.settings_interaction, Icons.Filled.List, "pref_category_interaction"),
        SettingsCategory(R.string.settings_play, Icons.Filled.QueueMusic, "pref_category_player"),
        SettingsCategory(R.string.settings_alarm, Icons.Filled.Alarm, "pref_category_alarm"),
        SettingsCategory(R.string.settings_recordings, Icons.Filled.Settings, "pref_category_recordings"),
        SettingsCategory(R.string.settings_connectivity, Icons.Filled.NetworkCheck, "pref_category_connectivity"),
        SettingsCategory(R.string.settings_mpd, Icons.Filled.SettingsInputComponent, "pref_category_mpd"),
        SettingsCategory(R.string.settings_other, Icons.Filled.Info, "pref_category_other")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_item_settings),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(categories) { category ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, LegacySettingsActivity::class.java)
                        if (!category.preferenceRoot.isNullOrEmpty()) {
                            intent.putExtra(
                                LegacySettingsActivity.EXTRA_PREFERENCE_ROOT,
                                category.preferenceRoot
                            )
                        }
                        context.startActivity(intent)
                    }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(category.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
            }
        }
    }
}
