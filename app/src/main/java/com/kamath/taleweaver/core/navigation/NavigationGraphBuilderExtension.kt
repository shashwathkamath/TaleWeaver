import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.login.presentation.LoginScreen
import com.kamath.taleweaver.login.presentation.LoginScreenViewmodel
import com.kamath.taleweaver.registration.presentation.RegistrationScreen
import com.kamath.taleweaver.registration.presentation.RegistrationViewModel
import kotlinx.coroutines.flow.collectLatest

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        route = AppDestination.AUTH_FLOW,
        startDestination = AppDestination.LOGIN_SCREEN
    ) {
        composable(AppDestination.LOGIN_SCREEN) {
            val viewModel: LoginScreenViewmodel = hiltViewModel()
            LaunchedEffect(key1 = Unit) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is NavigationEvent.NavigateToHome -> {
                            navController.navigate(AppDestination.HOME_SCREEN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }

                        else -> {}
                    }
                }
            }
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToSignUp = {
                    navController
                        .navigate(AppDestination.REGISTRATION_SCREEN) {
                            launchSingleTop = true
                        }
                }
            )
        }
        composable(AppDestination.REGISTRATION_SCREEN) {
            val viewmodel: RegistrationViewModel = hiltViewModel()
            LaunchedEffect(viewmodel.navigationEvent) {
                viewmodel.navigationEvent.collectLatest { event ->
                    when (event) {
                        is NavigationEvent.NavigateToLogin -> {
                            navController.navigate(AppDestination.LOGIN_SCREEN) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                        else -> {}
                    }
                }
            }
            RegistrationScreen(
                viewmodel = viewmodel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}