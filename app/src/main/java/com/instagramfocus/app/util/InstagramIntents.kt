package com.instagramfocus.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.instagramfocus.app.domain.classifier.InstagramAdapter

object InstagramIntents {

    fun openInstagram(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(InstagramAdapter.PACKAGE_NAME)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            openWebFallback(context, "https://www.instagram.com/")
        }
    }

    fun openDirectMessages(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(InstagramAdapter.DEEP_LINK_DIRECT_INBOX)).apply {
            setPackage(InstagramAdapter.PACKAGE_NAME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to web direct inbox or standard launch
            openWebFallback(context, InstagramAdapter.WEB_DIRECT_FALLBACK)
        }
    }

    fun openNotifications(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(InstagramAdapter.DEEP_LINK_NOTIFICATIONS)).apply {
            setPackage(InstagramAdapter.PACKAGE_NAME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            openInstagram(context)
        }
    }

    private fun openWebFallback(context: Context, url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open Instagram destination.", Toast.LENGTH_SHORT).show()
        }
    }
}
