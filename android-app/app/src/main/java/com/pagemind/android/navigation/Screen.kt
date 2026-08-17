package com.pagemind.android.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPasswordEmail : Screen("forgot_password_email")
    object ForgotPasswordCode : Screen("forgot_password_code")
    object ForgotPasswordNewPassword : Screen("forgot_password_new_password")
    object LanguageSelection : Screen("language_selection")
    object Home : Screen("home")
    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: Long) = "book_details/$bookId"
    }
    object Search : Screen("search")
    object Chatbot : Screen("chatbot")
    object ChatHistory : Screen("chat_history")
    object LikedBooks : Screen("liked_books")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
}
