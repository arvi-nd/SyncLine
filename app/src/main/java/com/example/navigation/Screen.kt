package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Main : Screen("main")

    // Sub-screens
    object Chat : Screen("chat/{clientId}") {
        fun createRoute(clientId: String) = "chat/$clientId"
    }

    object ClientDetails : Screen("client_details/{clientId}") {
        fun createRoute(clientId: String) = "client_details/$clientId"
    }

    object ScheduleMessage : Screen("schedule_message/{clientId}") {
        fun createRoute(clientId: String) = "schedule_message/$clientId"
    }

    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }

    object Timezones : Screen("timezones")
    object Calendar : Screen("calendar")
    object Files : Screen("files")
    object Notifications : Screen("notifications")
    object Search : Screen("search")
    object Assistant : Screen("assistant")
    object EmptyState : Screen("empty_state")
    object Settings : Screen("settings")
}
