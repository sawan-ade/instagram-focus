package com.instagramfocus.app.ui.statistics

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.instagramfocus.app.ui.components.MetricRow
import com.instagramfocus.app.ui.theme.AllowedGreen
import com.instagramfocus.app.ui.theme.BlockedRed
import com.instagramfocus.app.ui.theme.FocusBlue
import com.instagramfocus.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.statistics.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attention Protection Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            // Weekly Highlight Card
            FocusCard(
                backgroundColor = FocusBlue.copy(alpha = 0.12f),
                borderColor = FocusBlue.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "Weekly Protection Impact",
                    style = MaterialTheme.typography.labelSmall,
                    color = FocusBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.sp(1.1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stats.weeklyBlockedTotal}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Distraction attempts prevented over the last 7 days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Today's Breakdown
            Text(
                text = "TODAY'S BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                MetricRow(
                    label = "Reels blocked",
                    value = "${stats.reelsBlockedToday}",
                    valueColor = BlockedRed
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                MetricRow(
                    label = "Explore blocked",
                    value = "${stats.exploreBlockedToday}",
                    valueColor = WarningAmber
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                MetricRow(
                    label = "Feed scrolling blocked",
                    value = "${stats.feedBlockedToday}",
                    valueColor = FocusBlue
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                MetricRow(
                    label = "Total distractions stopped today",
                    value = "${stats.totalBlockedToday}",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Intentional Sessions
            Text(
                text = "COMMUNICATION SESSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = AllowedGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.sp(1.1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FocusCard {
                MetricRow(
                    label = "Direct Message sessions",
                    value = "${stats.dmSessionsCount}",
                    valueColor = AllowedGreen
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                MetricRow(
                    label = "Instagram launches",
                    value = "${stats.instagramSessionsCount}",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Assurance Note
            FocusCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Privacy Guarantee",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "These numbers are calculated locally from on-device logs. No message content, contacts, or network data is ever tracked or transmitted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { viewModel.clearStatistics() }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = BlockedRed)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Clear Statistics", color = BlockedRed)
                }
            }
        }
    }
}
