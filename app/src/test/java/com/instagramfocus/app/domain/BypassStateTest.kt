package com.instagramfocus.app.domain

import com.instagramfocus.app.domain.model.BypassState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BypassStateTest {

    @Test
    fun testBypassRemainingSeconds() {
        val now = System.currentTimeMillis()
        val future = now + 120_000L // 2 minutes

        val state = BypassState(
            isActive = true,
            expiresAtTimestamp = future,
            totalDurationMinutes = 2
        )

        assertTrue(state.remainingSeconds in 118..120)
        assertFalse(state.isExpired)
    }

    @Test
    fun testBypassExpired() {
        val now = System.currentTimeMillis()
        val past = now - 5000L

        val state = BypassState(
            isActive = true,
            expiresAtTimestamp = past,
            totalDurationMinutes = 5
        )

        assertEquals(0L, state.remainingSeconds)
        assertTrue(state.isExpired)
    }
}
