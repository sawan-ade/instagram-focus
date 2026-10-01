package com.instagramfocus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.instagramfocus.app.ui.theme.AllowedGreen
import com.instagramfocus.app.ui.theme.BlockedRed

@Composable
fun StatusBadge(
    isActive: Boolean,
    activeText: String = "ACTIVE",
    inactiveText: String = "PAUSED",
    modifier: Modifier = Modifier
) {
    val indicatorColor = if (isActive) AllowedGreen else BlockedRed
    val bgColor = indicatorColor.copy(alpha = 0.15f)
    val text = if (isActive) activeText else inactiveText

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = indicatorColor
        )
    }
}
