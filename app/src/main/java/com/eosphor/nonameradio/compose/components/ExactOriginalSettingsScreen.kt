package com.eosphor.nonameradio.compose.components

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eosphor.nonameradio.LegacySettingsActivity
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.compose.viewmodels.SettingsViewModel

private data class SettingsCategory(
    @StringRes val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val preferenceRoot: String?
)

@Composable
fun ExactOriginalSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Инициализация ViewModel
    LaunchedEffect(Unit) {
        viewModel.initializeSettings(context)
    }

    if (selectedCategory == null) {
        // Главный экран настроек
        SettingsMainScreen(
            modifier = modifier,
            onCategorySelected = { category -> selectedCategory = category }
        )
    } else {
        // Экран конкретной категории настроек
        SettingsCategoryScreen(
            modifier = modifier,
            category = selectedCategory!!,
            uiState = uiState,
            viewModel = viewModel,
            onBack = { selectedCategory = null }
        )
    }
}

@Composable
private fun SettingsMainScreen(
    modifier: Modifier = Modifier,
    onCategorySelected: (String) -> Unit
) {
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
                        onCategorySelected(category.preferenceRoot ?: "")
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
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SettingsCategoryScreen(
    modifier: Modifier = Modifier,
    category: String,
    uiState: com.eosphor.nonameradio.compose.viewmodels.SettingsUiState,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = getCategoryTitle(category),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        when (category) {
            "pref_category_ui" -> {
                appearanceSettings(uiState, viewModel)
            }
            "pref_category_startup" -> {
                startupSettings(uiState, viewModel)
            }
            "pref_category_interaction" -> {
                interactionSettings(uiState, viewModel)
            }
            "pref_category_player" -> {
                playerSettings(uiState, viewModel)
            }
            "pref_category_alarm" -> {
                alarmSettings(uiState, viewModel)
            }
            "pref_category_recordings" -> {
                recordingsSettings(uiState, viewModel)
            }
            "pref_category_connectivity" -> {
                connectivitySettings(uiState, viewModel)
            }
            "pref_category_mpd" -> {
                mpdSettings(uiState, viewModel)
            }
            "pref_category_other" -> {
                otherSettings(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun getCategoryTitle(category: String): String {
    return when (category) {
        "pref_category_ui" -> "Appearance"
        "pref_category_startup" -> "Startup Behavior"
        "pref_category_interaction" -> "Interaction"
        "pref_category_player" -> "Playback"
        "pref_category_alarm" -> "Alarm"
        "pref_category_recordings" -> "Recordings"
        "pref_category_connectivity" -> "Connectivity"
        "pref_category_mpd" -> "MPD"
        "pref_category_other" -> "Other"
        else -> "Settings"
    }
}
