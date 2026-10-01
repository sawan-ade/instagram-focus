package com.instagramfocus.app.ui.intervention

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.ui.components.FocusCard
import com.instagramfocus.app.ui.components.PrimaryActionButton
import com.instagramfocus.app.ui.components.SecondaryActionButton
import com.instagramfocus.app.ui.theme.BlockedRed
import com.instagramfocus.app.ui.theme.FocusBlue

@Composable
fun InterventionScreen(
    screenType: ScreenType,
    reason: String,
    onGoToDms: () -> Unit,
    onGoToNotifications: () -> Unit,
    onClose: () -> Unit
) {
    val surfaceName = when (screenType) {
        ScreenType.REELS -> "Reels are"
        ScreenType.EXPLORE -> "Explore is"
        ScreenType.HOME_FEED -> "Home Feed is"
        else -> "This section is"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        FocusCard(
            modifier = Modifier.fillMaxWidth(0.92f),
            borderColor = BlockedRed.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calm shield icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(BlockedRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = BlockedRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Instagram Focus",
                    style = MaterialTheme.typography.labelMedium,
                    color = FocusBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.sp(1.2)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This section is blocked.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$surfaceName disabled during Focus Mode.\n\nYour messages and stories are still available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Go to DMs",
                    onClick = onGoToDms
                )

                Spacer(modifier = Modifier.height(10.dp))

                SecondaryActionButton(
                    text = "Go to Notifications",
                    onClick = onGoToNotifications
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onClose) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
