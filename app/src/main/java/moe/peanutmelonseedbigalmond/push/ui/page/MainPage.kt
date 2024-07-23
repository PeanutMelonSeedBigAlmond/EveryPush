package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.config.ServerConfig
import moe.peanutmelonseedbigalmond.push.ui.NavRoutes
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState

@Composable
fun MainPage(parentNavHostController: NavHostController, modifier: Modifier = Modifier) {
    val navHostController = rememberNavController()
    val pages = remember {
        listOf<Triple<String, String, ImageVector>>(
            Triple(NavRoutes.devicesPage, "设备", Icons.Filled.PhoneAndroid),
            Triple(NavRoutes.keysPage, "密钥", Icons.Filled.Key),
            Triple(NavRoutes.messageGroupPage, "消息组", Icons.Filled.Category),
            Triple(NavRoutes.settingsPage, "设置", Icons.Filled.Settings)
        )
    }
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                pages.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                        onClick = {
                            navHostController.navigate(route) {
                                popUpTo(navHostController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(text = label)
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) {
        CompositionLocalProvider(LocalSnackBarHostState provides snackBarHostState) {
            NavHost(
                navController = navHostController,
                startDestination = NavRoutes.devicesPage,
                modifier = Modifier.padding(it)
            ) {
                composable(NavRoutes.devicesPage) {
                    DevicesPage(modifier = Modifier.fillMaxSize())
                }
                composable(NavRoutes.keysPage) {
                    KeysPage(modifier = Modifier.fillMaxSize())
                }
                composable(NavRoutes.messageGroupPage) {
                    MessageGroupPage(
                        modifier = Modifier.fillMaxSize(),
                        parentNavHostController = parentNavHostController
                    )
                }
                composable(NavRoutes.settingsPage) {
                    SettingsPage(
                        modifier = Modifier.fillMaxSize(),
                        parentNavHostController = parentNavHostController
                    )
                }
            }
        }
        LaunchedEffect(key1 = Unit) {
            Client.userToken = ServerConfig.token
            Client.serverAddress = ServerConfig.serverUrl
        }
    }
}