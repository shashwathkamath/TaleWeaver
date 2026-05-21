import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.login.presentation.LoginScreen
import com.kamath.taleweaver.login.presentation.OtpScreen
import com.kamath.taleweaver.login.presentation.OtpViewModel
import com.kamath.taleweaver.registration.presentation.RegistrationScreen
import com.kamath.taleweaver.registration.presentation.RegistrationViewModel

private val OTP_ROUTE = "${AppDestination.OTP_SCREEN}/{${AppDestination.ARG_OTP_EMAIL}}" +
        "?${AppDestination.ARG_OTP_USERNAME}={${AppDestination.ARG_OTP_USERNAME}}"

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        route = AppDestination.AUTH_FLOW,
        startDestination = AppDestination.LOGIN_SCREEN
    ) {
        composable(AppDestination.LOGIN_SCREEN) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(AppDestination.REGISTRATION_SCREEN) {
                        launchSingleTop = true
                    }
                },
                onNavigateToOtp = { email ->
                    navController.navigate(buildOtpRoute(email, null))
                }
            )
        }

        composable(AppDestination.REGISTRATION_SCREEN) {
            val viewmodel: RegistrationViewModel = hiltViewModel()
            RegistrationScreen(
                viewmodel = viewmodel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = { email, username ->
                    navController.navigate(buildOtpRoute(email, username)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = OTP_ROUTE,
            arguments = listOf(
                navArgument(AppDestination.ARG_OTP_EMAIL) { type = NavType.StringType },
                navArgument(AppDestination.ARG_OTP_USERNAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: OtpViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                viewModel.navigationEvent.collect { event ->
                    if (event is NavigationEvent.NavigateToHome) {
                        navController.navigate(AppDestination.HOME_SCREEN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            }
            OtpScreen(viewModel = viewModel, onNavigateToHome = {
                navController.navigate(AppDestination.HOME_SCREEN) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            })
        }
    }
}

private fun buildOtpRoute(email: String, username: String?): String {
    val encodedEmail = Uri.encode(email)
    return if (username.isNullOrBlank()) {
        "${AppDestination.OTP_SCREEN}/$encodedEmail"
    } else {
        "${AppDestination.OTP_SCREEN}/$encodedEmail?${AppDestination.ARG_OTP_USERNAME}=${Uri.encode(username)}"
    }
}
