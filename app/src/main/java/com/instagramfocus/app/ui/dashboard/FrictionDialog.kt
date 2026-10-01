package com.instagramfocus.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.ui.components.PrimaryActionButton
import com.instagramfocus.app.ui.theme.FocusBlue

@Composable
fun FrictionDialog(
    onDismiss: () -> Unit,
    onConfirmTurnOff: (reason: String) -> Unit,
    onStartBypassInstead: (minutes: Int) -> Unit
) {
    val reasons = listOf(
        "I need something specific on Instagram",
        "I'm finished with focused work for today",
        "Just a quick check (I'll take a 10m pause)",
        "Other personal reason"
    )

    var selectedReason by remember { mutableStateOf(reasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Pause Distraction Protection?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Focus Mode is currently protecting your attention from endless scrolling.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Why are you turning Focus Mode off?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedReason == reason),
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Suggest gentle temporary bypass instead of permanent turn off
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusBlue.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Tip: Temporary Bypass",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = FocusBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Take a 10-minute break. Restrictions will automatically restore when the timer ends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrimaryActionButton(
                        text = "Allow Instagram for 10 Mins",
                        onClick = { onStartBypassInstead(10) },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmTurnOff(selectedReason) }
            ) {
                Text(
                    text = "Turn Off Completely",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Focus ON")
            }
        }
    )
}
