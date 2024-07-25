package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import moe.peanutmelonseedbigalmond.push.repository.config.ServerConfig
import moe.peanutmelonseedbigalmond.push.ui.NavRoutes
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.LoginFollowingPageViewModel

@Composable
fun LoginFollowingPage(
    parentNavHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val viewModel = viewModel(modelClass = LoginFollowingPageViewModel::class.java)
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        modifier = modifier,
    ) {
        CompositionLocalProvider(
            LocalSnackBarHostState provides snackBarHostState
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.chooseServerPage,
                modifier = Modifier.padding(it)
            ) {
                composable(NavRoutes.chooseServerPage) {
                    ChooseServerPage(modifier = Modifier.fillMaxSize()) {
                        viewModel.serverAddress = it
                        navController.navigate(NavRoutes.loginPage)
                    }
                }
                composable(NavRoutes.loginPage) {
                    LoginPage(
                        serverAddress = viewModel.serverAddress,
                        modifier = Modifier.fillMaxSize(),
                        onBack = {
                            navController.popBackStack()
                        }, onLoginSuccess = {
                            ServerConfig.saveServerConfig(it, viewModel.serverAddress)
                            parentNavHostController.navigate(NavRoutes.mainPage) {
                                popUpTo(NavRoutes.loginFollowingPage) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}