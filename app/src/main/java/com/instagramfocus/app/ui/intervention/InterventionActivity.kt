package com.instagramfocus.app.ui.intervention

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.util.InstagramIntents

class InterventionActivity : Activity() {

    companion object {
        const val EXTRA_SCREEN_TYPE = "extra_screen_type"
        const val EXTRA_REASON = "extra_reason"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenTypeName = intent.getStringExtra(EXTRA_SCREEN_TYPE) ?: ScreenType.UNKNOWN.name
        val screenType = try { ScreenType.valueOf(screenTypeName) } catch (_: Exception) { ScreenType.UNKNOWN }

        val surfaceName = when (screenType) {
            ScreenType.REELS -> "Reels are"
            ScreenType.EXPLORE -> "Explore is"
            ScreenType.HOME_FEED -> "Home Feed is"
            else -> "This section is"
        }

        // Translucent dim background
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E6090D16"))
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        // Calm Intervention Card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            gravity = Gravity.CENTER_HORIZONTAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#131B2E"))
                setStroke(dp(1), Color.parseColor("#EF4444"))
                cornerRadius = dp(20).toFloat()
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Icon
        card.addView(TextView(this).apply {
            text = "🛡️"
            textSize = 36f
            gravity = Gravity.CENTER
        })
        card.addView(createSpacer(12))

        // Title
        card.addView(TextView(this).apply {
            text = "Instagram Focus"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#3B82F6"))
            letterSpacing = 0.1f
            gravity = Gravity.CENTER
        })
        card.addView(createSpacer(4))
        card.addView(TextView(this).apply {
            text = "This section is blocked."
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F8FAFC"))
            gravity = Gravity.CENTER
        })
        card.addView(createSpacer(12))

        // Message
        card.addView(TextView(this).apply {
            text = "$surfaceName disabled during Focus Mode.\n\nYour messages and stories are still available."
            textSize = 15f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setLineSpacing(0f, 1.2f)
        })
        card.addView(createSpacer(24))

        // [ Go to DMs ]
        card.addView(Button(this).apply {
            text = "Go to DMs"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#3B82F6"))
                cornerRadius = dp(12).toFloat()
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48))
            setOnClickListener {
                InstagramIntents.openDirectMessages(this@InterventionActivity)
                finish()
            }
        })
        card.addView(createSpacer(10))

        // [ Go to Notifications ]
        card.addView(Button(this).apply {
            text = "Go to Notifications"
            textSize = 15f
            setTextColor(Color.parseColor("#CBD5E1"))
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                setStroke(dp(1), Color.parseColor("#475569"))
                cornerRadius = dp(12).toFloat()
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48))
            setOnClickListener {
                InstagramIntents.openNotifications(this@InterventionActivity)
                finish()
            }
        })
        card.addView(createSpacer(10))

        // [ Close ]
        card.addView(Button(this).apply {
            text = "Close"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        })

        root.addView(card)
        setContentView(root)
    }

    private fun createSpacer(dp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(dp))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
