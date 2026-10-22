package com.example.primecheck.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primecheck.model.AppTheme
import com.example.primecheck.model.UserSettings

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onSettingsChange: (UserSettings) -> Unit,
    onClearHistory: () -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Preferences and appearance options",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance Section
        SettingsCard(title = "APPEARANCE") {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = settings.theme == theme,
                        onClick = { onSettingsChange(settings.copy(theme = theme)) },
                        label = {
                            Text(
                                when (theme) {
                                    AppTheme.SYSTEM -> "System"
                                    AppTheme.LIGHT -> "Light"
                                    AppTheme.DARK -> "Dark"
                                }
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingSwitchRow(
                title = "Dynamic Color",
                subtitle = "Apply wallpaper accent colors (Android 12+)",
                checked = settings.dynamicColor,
                onCheckedChange = { onSettingsChange(settings.copy(dynamicColor = it)) }
            )
        }

        // Behavior Section
        SettingsCard(title = "BEHAVIOR") {
            SettingSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Vibrate softly on calculations and button presses",
                checked = settings.hapticFeedback,
                onCheckedChange = { onSettingsChange(settings.copy(hapticFeedback = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchRow(
                title = "Animations",
                subtitle = "Smooth transitions and result reveals",
                checked = settings.animations,
                onCheckedChange = { onSettingsChange(settings.copy(animations = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchRow(
                title = "Confirm Delete",
                subtitle = "Ask for confirmation before deleting calculations",
                checked = settings.confirmDelete,
                onCheckedChange = { onSettingsChange(settings.copy(confirmDelete = it)) }
            )
        }

        // Results Section
        SettingsCard(title = "RESULTS DISPLAY") {
            SettingSwitchRow(
                title = "Show Factors",
                subtitle = "Display full list of positive divisors",
                checked = settings.showFactors,
                onCheckedChange = { onSettingsChange(settings.copy(showFactors = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchRow(
                title = "Show Factor Pairs",
                subtitle = "Display all multiplicative factor pairs",
                checked = settings.showFactorPairs,
                onCheckedChange = { onSettingsChange(settings.copy(showFactorPairs = it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingSwitchRow(
                title = "Show Calculation Steps",
                subtitle = "Display step-by-step mathematical reasoning",
                checked = settings.showCalculationSteps,
                onCheckedChange = { onSettingsChange(settings.copy(showCalculationSteps = it)) }
            )
        }

        // Data Section
        SettingsCard(title = "DATA") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showClearHistoryDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear History")
                }

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("This will restore default preferences. Your calculation history will remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear all saved calculations?") },
            text = { Text("Are you sure you want to remove all saved calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
