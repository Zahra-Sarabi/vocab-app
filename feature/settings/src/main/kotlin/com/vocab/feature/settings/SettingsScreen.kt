package com.vocab.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vocab.core.designsystem.component.Art
import com.vocab.core.designsystem.component.ClayBox
import com.vocab.core.designsystem.component.ClayButton
import com.vocab.core.designsystem.component.ClayButtonStyle
import com.vocab.core.designsystem.component.ClayCard
import com.vocab.core.designsystem.component.ClayIconButton
import com.vocab.core.designsystem.component.EnText
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.model.Accent
import com.vocab.core.model.Illustration
import com.vocab.core.model.ThemeMode
import com.vocab.core.model.UserSettings
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data object SettingsRoute

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsRoute> { SettingsScreen(onBack) }
}

@Composable
internal fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val pending by viewModel.pendingRestore.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.export(it.toString()) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.inspect(it.toString()) }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setReminder(granted)
    }

    val messageText = message?.let {
        stringResource(
            when (it) {
                SettingsMessage.BACKUP_SAVED -> R.string.backup_saved
                SettingsMessage.BACKUP_FAILED -> R.string.backup_failed
                SettingsMessage.RESTORE_DONE -> R.string.restore_done
                SettingsMessage.RESTORE_INVALID -> R.string.restore_invalid
            },
        )
    }
    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbar.showSnackbar(messageText)
            viewModel.messageShown()
        }
    }

    val current = settings ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.space4),
            verticalArrangement = Arrangement.spacedBy(Dimens.space4),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
                ClayIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.settings_back), onBack)
                Text(stringResource(R.string.settings_title), style = VocabTheme.type.displayFa, color = VocabTheme.colors.ink)
            }
            BackupCard(
                current,
                viewModel,
                onExport = { exportLauncher.launch(viewModel.backupFileName()) },
                onImport = { importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) },
            )
            GoalCard(current.dailyGoal, viewModel::setDailyGoal)
            ReminderCard(
                current,
                onReminder = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else viewModel.setReminder(enabled)
                },
                onTime = viewModel::setReminderTime,
                onStreak = viewModel::setStreakAlert,
            )
            ChoiceCard(
                Illustration.HEADPHONE,
                stringResource(R.string.accent_title),
                listOf(Accent.US to stringResource(R.string.accent_us), Accent.UK to stringResource(R.string.accent_uk)),
                current.accent,
                viewModel::setAccent,
            )
            ChoiceCard(
                Illustration.SPARKLES,
                stringResource(R.string.theme_title),
                listOf(ThemeMode.SYSTEM to stringResource(R.string.theme_system), ThemeMode.LIGHT to stringResource(R.string.theme_light), ThemeMode.DARK to stringResource(R.string.theme_dark)),
                current.themeMode,
                viewModel::setTheme,
            )
            Text(stringResource(R.string.licenses), style = VocabTheme.type.captionFa, color = VocabTheme.colors.inkMuted)
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    pending?.let { restore ->
        val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(restore.preview.exportedAt))
        AlertDialog(
            onDismissRequest = viewModel::cancelRestore,
            title = { Text(stringResource(R.string.restore_title), style = VocabTheme.type.titleFa) },
            text = {
                Text(
                    stringResource(R.string.restore_body, date, restore.preview.learnedWords, restore.preview.passedUnits, restore.preview.streakDays),
                    style = VocabTheme.type.bodyFa,
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmRestore) { Text(stringResource(R.string.restore_confirm), style = VocabTheme.type.labelFa) } },
            dismissButton = { TextButton(onClick = viewModel::cancelRestore) { Text(stringResource(R.string.restore_cancel), style = VocabTheme.type.labelFa) } },
        )
    }
}

@Composable
private fun BackupCard(settings: UserSettings, vm: SettingsViewModel, onExport: () -> Unit, onImport: () -> Unit) {
    val colors = VocabTheme.colors
    ClayCard(color = colors.lilacSoft) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.space3)) {
            Art(Illustration.BACKUP, 60.dp)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.backup_title), style = VocabTheme.type.titleFa, color = colors.ink)
                Text(stringResource(R.string.backup_subtitle), style = VocabTheme.type.captionFa, color = colors.inkMuted)
            }
        }
        val last = settings.lastBackupAt
        val lastText = when {
            last == null -> stringResource(R.string.backup_never)
            vm.daysSince(last) == 0L -> stringResource(R.string.backup_today)
            else -> stringResource(R.string.backup_last, vm.daysSince(last).toInt())
        }
        ClayBox(Modifier.fillMaxWidth(), color = colors.butterSoft, radius = 14.dp, shadow = VocabTheme.shadows.pill, contentAlignment = Alignment.CenterStart) {
            Text(lastText, style = VocabTheme.type.captionFa, color = colors.butterInk, modifier = Modifier.padding(horizontal = Dimens.space3, vertical = Dimens.space2))
        }
        ClayButton(stringResource(R.string.backup_export), onExport, Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Upload)
        ClayButton(stringResource(R.string.backup_import), onImport, Modifier.fillMaxWidth(), style = ClayButtonStyle.SECONDARY, leadingIcon = Icons.Rounded.Download)
    }
}

@Composable
private fun GoalCard(goal: Int, onGoal: (Int) -> Unit) {
    val colors = VocabTheme.colors
    ClayCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Art(Illustration.BULLSEYE, 32.dp)
            Text(stringResource(R.string.goal_title), style = VocabTheme.type.labelFa, color = colors.ink, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.goal_subtitle), style = VocabTheme.type.captionFa, color = colors.inkMuted)
        }
        ClayBox(Modifier.fillMaxWidth(), color = colors.surfaceSunk, radius = Dimens.radiusMd, shadow = VocabTheme.shadows.sunk) {
            Row(Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                UserSettings.DAILY_GOAL_OPTIONS.forEach { option ->
                    val selected = option == goal
                    ClayBox(
                        Modifier.weight(1f).height(40.dp).clickable { onGoal(option) },
                        color = if (selected) colors.primaryStrong else colors.surfaceSunk,
                        radius = 14.dp,
                        shadow = if (selected) VocabTheme.shadows.button else com.vocab.core.designsystem.theme.ClayShadow(emptyList()),
                    ) {
                        EnText("$option", style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito), color = if (selected) colors.onPrimary else colors.inkMuted)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCard(settings: UserSettings, onReminder: (Boolean) -> Unit, onTime: (Int, Int) -> Unit, onStreak: (Boolean) -> Unit) {
    val colors = VocabTheme.colors
    var picking by remember { mutableStateOf(false) }
    val hour = settings.reminderMinuteOfDay / 60
    val minute = settings.reminderMinuteOfDay % 60
    ClayCard(padding = 0.dp) {
        SwitchRow(Illustration.BELL, stringResource(R.string.reminder_title), "%02d:%02d".format(hour, minute), settings.reminderEnabled, onReminder) { picking = true }
        HorizontalDivider(color = colors.surfaceSunk)
        SwitchRow(Illustration.FIRE, stringResource(R.string.streak_alert_title), stringResource(R.string.streak_alert_subtitle), settings.streakAlertEnabled, onStreak, null)
    }
    if (picking) {
        val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { picking = false },
            title = { Text(stringResource(R.string.reminder_time_pick), style = VocabTheme.type.titleFa) },
            text = { TimePicker(state) },
            confirmButton = {
                TextButton(onClick = {
                    onTime(state.hour, state.minute)
                    picking = false
                }) { Text(stringResource(R.string.reminder_time_ok), style = VocabTheme.type.labelFa) }
            },
        )
    }
}

@Composable
private fun SwitchRow(art: Illustration, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit, onSubtitle: (() -> Unit)?) {
    val colors = VocabTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Dimens.space4, vertical = Dimens.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space3),
    ) {
        Art(art, 30.dp)
        Column(Modifier.weight(1f).then(if (onSubtitle != null) Modifier.clickable(onClick = onSubtitle) else Modifier)) {
            Text(title, style = VocabTheme.type.labelFa, color = colors.ink)
            Text(subtitle, style = VocabTheme.type.captionFa, color = if (onSubtitle != null) colors.primaryText else colors.inkMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedTrackColor = colors.go, checkedThumbColor = colors.surface),
        )
    }
}

@Composable
private fun <T> ChoiceCard(art: Illustration, title: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    val colors = VocabTheme.colors
    ClayCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Art(art, 30.dp)
            Text(title, style = VocabTheme.type.labelFa, color = colors.ink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space2)) {
            options.forEach { (value, label) ->
                ClayButton(
                    label,
                    { onSelect(value) },
                    Modifier.weight(1f),
                    style = if (value == selected) ClayButtonStyle.PRIMARY else ClayButtonStyle.SECONDARY,
                )
            }
        }
    }
}
