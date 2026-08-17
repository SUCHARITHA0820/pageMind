package com.pagemind.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pagemind.android.ui.screens.BookDetailsScreen
import com.pagemind.android.ui.screens.ChatHistoryScreen
import com.pagemind.android.ui.screens.ChatbotScreen
import com.pagemind.android.ui.screens.EditProfileScreen
import com.pagemind.android.ui.screens.ForgotPasswordCodeScreen
import com.pagemind.android.ui.screens.ForgotPasswordEmailScreen
import com.pagemind.android.ui.screens.ForgotPasswordNewPasswordScreen
import com.pagemind.android.ui.screens.HomeScreen
import com.pagemind.android.ui.screens.LanguageSelectionScreen
import com.pagemind.android.ui.screens.LikedBooksScreen
import com.pagemind.android.ui.screens.LoginScreen
import com.pagemind.android.ui.screens.OnboardingScreen
import com.pagemind.android.ui.screens.ProfileScreen
import com.pagemind.android.ui.screens.SearchScreen
import com.pagemind.android.ui.screens.SettingsScreen
import com.pagemind.android.ui.screens.SignupScreen
import com.pagemind.android.ui.screens.SplashScreen

@Composable
fun PageMindNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Screen.Signup.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        // 3. Login Screen (Returning User -> HomeScreen)
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateForgotPassword = { navController.navigate(Screen.ForgotPasswordEmail.route) },
                onGuestLogin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Signup Screen (First-time User -> LanguageSelectionScreen)
        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.LanguageSelection.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        // 5. Forgot Password Email Screen
        composable(Screen.ForgotPasswordEmail.route) {
            ForgotPasswordEmailScreen(
                onSendCode = { navController.navigate(Screen.ForgotPasswordCode.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 6. Forgot Password Code Screen
        composable(Screen.ForgotPasswordCode.route) {
            ForgotPasswordCodeScreen(
                onVerifyCode = { navController.navigate(Screen.ForgotPasswordNewPassword.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 7. Forgot Password New Password Screen
        composable(Screen.ForgotPasswordNewPassword.route) {
            ForgotPasswordNewPasswordScreen(
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 8. Language Selection Screen
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 9. Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                },
                onNavigateSearch = { navController.navigate(Screen.Search.route) },
                onNavigateChatbot = { navController.navigate(Screen.Chatbot.route) },
                onNavigateLiked = { navController.navigate(Screen.LikedBooks.route) },
                onNavigateProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        // 10. Book Details Screen
        composable(
            route = Screen.BookDetails.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 1L
            BookDetailsScreen(
                bookId = bookId,
                onBackClick = { navController.popBackStack() },
                onAskAiClick = { bookTitle ->
                    navController.navigate(Screen.Chatbot.route)
                }
            )
        }

        // 11. Search Screen
        composable(Screen.Search.route) {
            SearchScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 12. Chatbot Screen
        composable(Screen.Chatbot.route) {
            ChatbotScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                },
                onNavigateHistory = { navController.navigate(Screen.ChatHistory.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 13. Chat History Screen
        composable(Screen.ChatHistory.route) {
            ChatHistoryScreen(
                onThreadClick = { navController.navigate(Screen.Chatbot.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 14. Liked Books Screen
        composable(Screen.LikedBooks.route) {
            LikedBooksScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 15. Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateLanguage = { navController.navigate(Screen.LanguageSelection.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                }
            )
        }

        // 16. Edit Profile Screen
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 17. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
