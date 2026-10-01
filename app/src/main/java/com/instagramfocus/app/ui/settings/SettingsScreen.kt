package com.instagramfocus.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.ui.components.FocusCard
import com.instagramfocus.app.ui.theme.AllowedGreen
import com.instagramfocus.app.ui.theme.BlockedRed
import com.instagramfocus.app.ui.theme.FocusBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Focus Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Blocked Distraction Surfaces
            Text(
                text = "BLOCKED SURFACES",
                style = MaterialTheme.typography.labelSmall,
                color = BlockedRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                SettingSwitchRow(
                    title = "Block Reels",
                    subtitle = "Prevents opening the vertical short-video feed.",
                    checked = settings.blockReels,
                    onCheckedChange = { viewModel.setBlockReels(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Block Explore Grid",
                    subtitle = "Blocks the discovery and recommendation grid.",
                    checked = settings.blockExplore,
                    onCheckedChange = { viewModel.setBlockExplore(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Block Endless Feed",
                    subtitle = "Restricts the endless algorithmic timeline.",
                    checked = settings.blockFeed,
                    onCheckedChange = { viewModel.setBlockFeed(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Block Suggested Content",
                    subtitle = "Prevents rabbit holes from accounts you do not follow.",
                    checked = settings.blockSuggestedContent,
                    onCheckedChange = { viewModel.setBlockSuggestedContent(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Allowed Communication Surfaces
            Text(
                text = "ALLOWED COMMUNICATION",
                style = MaterialTheme.typography.labelSmall,
                color = AllowedGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                SettingSwitchRow(
                    title = "Allow Direct Messages (DMs)",
                    subtitle = "Keep inbox, chats, photos, voice messages active.",
                    checked = settings.allowDms,
                    onCheckedChange = { viewModel.setAllowDms(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Allow Stories",
                    subtitle = "Permit checking updates from close friends.",
                    checked = settings.allowStories,
                    onCheckedChange = { viewModel.setAllowStories(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Allow Follow Requests",
                    subtitle = "Permit approving and managing connection requests.",
                    checked = settings.allowFollowRequests,
                    onCheckedChange = { viewModel.setAllowFollowRequests(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Allow Profiles",
                    subtitle = "Permit viewing profile bios and info.",
                    checked = settings.allowProfiles,
                    onCheckedChange = { viewModel.setAllowProfiles(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Strictness & Anti-Bypass Friction
            Text(
                text = "STRICTNESS & FRICTION",
                style = MaterialTheme.typography.labelSmall,
                color = FocusBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                SettingSwitchRow(
                    title = "Strict Mode",
                    subtitle = "When enabled, unrecognized screens are actively monitored. When disabled, safe fallback protects communication.",
                    checked = settings.isStrictModeEnabled,
                    onCheckedChange = { viewModel.setStrictMode(it) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingSwitchRow(
                    title = "Anti-Bypass Friction Survey",
                    subtitle = "Asks for intention when attempting to disable Focus Mode.",
                    checked = settings.enableFrictionSurvey,
                    onCheckedChange = { viewModel.setFrictionSurvey(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = FocusBlue
            )
        )
    }
}
