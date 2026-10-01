package com.instagramfocus.app.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")
    data object Statistics : Screen("statistics")
    data object Debug : Screen("debug")
}
