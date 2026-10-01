package com.instagramfocus.app.ui.debug

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.ui.components.FocusCard
import com.instagramfocus.app.ui.components.MetricRow
import com.instagramfocus.app.ui.theme.AllowedGreen
import com.instagramfocus.app.ui.theme.BlockedRed
import com.instagramfocus.app.ui.theme.FocusBlue
import com.instagramfocus.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: DebugViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val simulationOptions = listOf(
        "REELS" to "Reels",
        "FEED" to "Home Feed",
        "EXPLORE" to "Explore Grid",
        "DM_INBOX" to "DM Inbox",
        "DM_CHAT" to "DM Chat",
        "STORY" to "Story Viewer",
        "PROFILE" to "User Profile",
        "FOLLOW_REQUESTS" to "Follow Requests",
        "UNKNOWN" to "Unknown Screen"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Classifier Live Inspector",
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
                .padding(24.dp)
        ) {
            Text(
                text = "SIMULATE INSTAGRAM STATE",
                style = MaterialTheme.typography.labelSmall,
                color = FocusBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Simulation selector pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                simulationOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = (uiState.simulatedScreen == key),
                        onClick = { viewModel.simulateScreen(key) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Classifier Output Card
            Text(
                text = "CLASSIFICATION ENGINE OUTPUT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                MetricRow(
                    label = "Target Package",
                    value = uiState.currentPackage,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                MetricRow(
                    label = "Detected Screen",
                    value = uiState.classification?.screenType?.name ?: "N/A",
                    valueColor = if (uiState.classification?.screenType?.isDistractionSurface == true) BlockedRed else AllowedGreen
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                MetricRow(
                    label = "Confidence Score",
                    value = String.format("%.2f", uiState.classification?.confidence ?: 0f),
                    valueColor = FocusBlue
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                val decisionText = when (uiState.decision) {
                    is RestrictionDecision.Block -> "BLOCK"
                    is RestrictionDecision.Allow -> "ALLOW"
                    is RestrictionDecision.Warn -> "WARN"
                    null -> "N/A"
                }
                val decisionColor = when (uiState.decision) {
                    is RestrictionDecision.Block -> BlockedRed
                    is RestrictionDecision.Allow -> AllowedGreen
                    is RestrictionDecision.Warn -> WarningAmber
                    null -> MaterialTheme.colorScheme.onSurface
                }

                MetricRow(
                    label = "Restriction Decision",
                    value = decisionText,
                    valueColor = decisionColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Matched Rules Card
            Text(
                text = "MATCHED RULES & SIGNALS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                Text(
                    text = "Matched Rule Names:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                uiState.classification?.matchedRules?.forEach { rule ->
                    Text(
                        text = "• $rule",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!uiState.classification?.detectedSignals.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Detected UI Signals:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    uiState.classification?.detectedSignals?.forEach { signal ->
                        Text(
                            text = "• $signal",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
