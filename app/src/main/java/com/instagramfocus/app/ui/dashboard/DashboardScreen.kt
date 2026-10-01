package com.instagramfocus.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.ui.components.FocusCard
import com.instagramfocus.app.ui.components.MetricRow
import com.instagramfocus.app.ui.components.PrimaryActionButton
import com.instagramfocus.app.ui.components.SecondaryActionButton
import com.instagramfocus.app.ui.components.StatusBadge
import com.instagramfocus.app.ui.theme.AllowedGreen
import com.instagramfocus.app.ui.theme.BlockedRed
import com.instagramfocus.app.ui.theme.FocusBlue
import com.instagramfocus.app.ui.theme.WarningAmber
import com.instagramfocus.app.util.InstagramIntents
import com.instagramfocus.app.util.PermissionUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToDebug: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
        viewModel.refreshStatistics()
    }

    if (uiState.showFrictionDialog) {
        FrictionDialog(
            onDismiss = { viewModel.dismissFrictionDialog() },
            onConfirmTurnOff = { reason -> viewModel.confirmDisableFocusMode(reason) },
            onStartBypassInstead = { minutes -> viewModel.startTemporaryBypass(minutes) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // App Title & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INSTAGRAM FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = androidx.compose.ui.unit.sp(1.2)
                    )
                    Text(
                        text = "Protection Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToDebug) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug Inspector",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permission Warning Banner if Accessibility is disabled
            if (!uiState.isAccessibilityEnabled) {
                FocusCard(
                    backgroundColor = WarningAmber.copy(alpha = 0.1f),
                    borderColor = WarningAmber.copy(alpha = 0.4f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Focus protection is currently disabled.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enable Accessibility Service so Instagram Focus can filter distractions.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryActionButton(
                        text = "Enable Accessibility Service",
                        onClick = { PermissionUtils.openAccessibilitySettings(context) },
                        containerColor = WarningAmber,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Master Switch Focus Card
            FocusCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Focus Mode",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            StatusBadge(
                                isActive = uiState.settings.isFocusModeEnabled && !uiState.bypassState.isActive,
                                activeText = if (uiState.bypassState.isActive) "PAUSED" else "ON",
                                inactiveText = "OFF"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.bypassState.isActive)
                                "Bypass active: ${uiState.bypassState.remainingSeconds}s remaining"
                            else if (uiState.settings.isFocusModeEnabled)
                                "Distraction surfaces are restricted."
                            else
                                "Instagram is unrestricted.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = uiState.settings.isFocusModeEnabled,
                        onCheckedChange = { viewModel.onToggleFocusMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = FocusBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Allowed vs Blocked Feature Overview Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Allowed Card
                FocusCard(
                    modifier = Modifier.weight(1f),
                    borderColor = AllowedGreen.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AllowedGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = AllowedGreen, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Allowed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AllowedGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("✓ DMs", "✓ Stories", "✓ Follow Requests", "✓ Notifications").forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // Blocked Card
                FocusCard(
                    modifier = Modifier.weight(1f),
                    borderColor = BlockedRed.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BlockedRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, null, tint = BlockedRed, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Blocked",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BlockedRed
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("✗ Reels", "✗ Explore", "✗ Feed", "✗ Suggestions").forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today's Distraction Protection Statistics
            FocusCard {
                Text(
                    text = "Today's Distraction Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distraction attempts prevented automatically",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                MetricRow(
                    label = "Reels blocked",
                    value = "${uiState.statistics.reelsBlockedToday} attempts",
                    valueColor = BlockedRed
                )
                MetricRow(
                    label = "Explore blocked",
                    value = "${uiState.statistics.exploreBlockedToday} attempts",
                    valueColor = WarningAmber
                )
                MetricRow(
                    label = "Feed scrolling blocked",
                    value = "${uiState.statistics.feedBlockedToday} attempts",
                    valueColor = FocusBlue
                )
                MetricRow(
                    label = "DM sessions protected",
                    value = "${uiState.statistics.dmSessionsCount} intentional",
                    valueColor = AllowedGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temporary Bypass Section
            FocusCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Temporary Bypass",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.bypassState.isActive)
                                "Active: expires in ${uiState.bypassState.remainingSeconds}s"
                            else
                                "Temporarily pause restrictions for quick tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.bypassState.isActive) {
                        androidx.compose.material3.TextButton(onClick = { viewModel.cancelTemporaryBypass() }) {
                            Text("Resume Focus", color = FocusBlue)
                        }
                    }
                }

                if (!uiState.bypassState.isActive) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 30).forEach { mins ->
                            SecondaryActionButton(
                                text = "$mins min",
                                onClick = { viewModel.startTemporaryBypass(mins) },
                                modifier = Modifier.weight(1f).height(44.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Launch Actions
            PrimaryActionButton(
                text = "Open Direct Messages Directly",
                onClick = { InstagramIntents.openDirectMessages(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryActionButton(
                text = "Open Instagram",
                onClick = { InstagramIntents.openInstagram(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
