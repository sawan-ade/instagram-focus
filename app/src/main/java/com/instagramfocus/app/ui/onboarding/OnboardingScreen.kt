package com.instagramfocus.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.ui.components.FocusCard
import com.instagramfocus.app.ui.components.PrimaryActionButton
import com.instagramfocus.app.ui.components.SecondaryActionButton
import com.instagramfocus.app.ui.theme.AllowedGreen
import com.instagramfocus.app.ui.theme.BlockedRed
import com.instagramfocus.app.ui.theme.FocusBlue
import com.instagramfocus.app.util.PermissionUtils

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions(context)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Step Indicator Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { index ->
                        val isSelected = index == uiState.currentStep
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) FocusBlue
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.currentStep > 0) {
                        SecondaryActionButton(
                            text = "Back",
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    PrimaryActionButton(
                        text = if (uiState.currentStep == 3) "Enter Focus Mode" else "Continue",
                        onClick = {
                            if (uiState.currentStep == 3) {
                                viewModel.completeOnboarding(onFinishOnboarding)
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "OnboardingTransition"
            ) { step ->
                when (step) {
                    0 -> StepOnePhilosophy()
                    1 -> StepTwoAllowedFeatures()
                    2 -> StepThreeBlockedFeatures()
                    3 -> StepFourPermissions(
                        uiState = uiState,
                        onEnableAccessibility = {
                            PermissionUtils.openAccessibilitySettings(context)
                        },
                        onEnableNotificationListener = {
                            PermissionUtils.openNotificationListenerSettings(context)
                        },
                        onEnableOverlay = {
                            PermissionUtils.openOverlaySettings(context)
                        },
                        onRefresh = { viewModel.refreshPermissions(context) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepOnePhilosophy() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(FocusBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = FocusBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Instagram, without the rabbit hole.",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Keep Instagram for the people you care about.\nRemove the endless scrolling.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        FocusCard {
            Text(
                text = "The Philosophy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"Instagram as a communication tool, NOT an entertainment app.\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepTwoAllowedFeatures() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "What Remains Available",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "All genuine personal and social communication is protected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        val allowedList = listOf(
            "Direct Messages (DMs)" to "Chat, send photos, voice notes, and group messages with friends.",
            "Instagram Stories" to "View updates from people you follow without entering algorithmic video feeds.",
            "Follow Requests" to "Approve or decline connection requests from people.",
            "Social Notifications" to "Stay informed about messages, mentions, and friend requests.",
            "User Profiles" to "View accounts and profiles when intentionally searching."
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            allowedList.forEach { (title, description) ->
                FocusCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AllowedGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AllowedGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepThreeBlockedFeatures() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "What Gets Blocked",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Addictive algorithmic loops designed for infinite scrolling.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        val blockedList = listOf(
            "Instagram Reels" to "Infinite vertical short-form video feed.",
            "Explore Grid" to "Endless discovery algorithm optimized to capture attention.",
            "Endless Main Feed" to "Algorithmic posts and suggested accounts below caught up.",
            "Suggested Content" to "Recommendation rabbit holes unrelated to your direct network."
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            blockedList.forEach { (title, description) ->
                FocusCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(BlockedRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = BlockedRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepFourPermissions(
    uiState: OnboardingUiState,
    onEnableAccessibility: () -> Unit,
    onEnableNotificationListener: () -> Unit,
    onEnableOverlay: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Permissions & Privacy",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "This app operates entirely on your phone. No data ever leaves your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // 1. Accessibility Service
        FocusCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "1. Accessibility Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Needed to detect which Instagram screen is open (e.g. DMs vs Reels) and prevent algorithmic distraction.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (uiState.isAccessibilityEnabled) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AllowedGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = AllowedGreen)
                    }
                } else {
                    androidx.compose.material3.TextButton(onClick = onEnableAccessibility) {
                        Text("Enable")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Overlay / Draw Over Apps
        FocusCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "2. Draw Over Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Allows displaying the calm intervention card when a blocked section is accessed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (uiState.canDrawOverlays) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AllowedGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = AllowedGreen)
                    }
                } else {
                    androidx.compose.material3.TextButton(onClick = onEnableOverlay) {
                        Text("Enable")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Notification Filter
        FocusCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "3. Notification Filter (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Filters out promotional reel recommendations while preserving all direct messages and follow requests.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (uiState.isNotificationListenerEnabled) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AllowedGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = AllowedGreen)
                    }
                } else {
                    androidx.compose.material3.TextButton(onClick = onEnableNotificationListener) {
                        Text("Enable")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.TextButton(
            onClick = onRefresh,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Re-check Permissions")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
