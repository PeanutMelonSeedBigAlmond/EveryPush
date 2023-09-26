package moe.peanutmelonseedbigalmond.push.ui.component.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.page.home.DevicesPage
import moe.peanutmelonseedbigalmond.push.ui.component.page.home.MessagesPage
import moe.peanutmelonseedbigalmond.push.ui.component.page.home.SettingsPage
import moe.peanutmelonseedbigalmond.push.ui.component.page.home.TokenPage
import moe.peanutmelonseedbigalmond.push.ui.viewmodel.HomePageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    //region 变量
    val viewModel = viewModel(modelClass = HomePageViewModel::class.java)
    val navController = rememberNavController()
    val context= LocalContext.current
    val snackBarState = remember { SnackbarHostState() }
    var showFloatingAction by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var appBarTitle by remember { mutableStateOf("") }
    val navigationItems = remember(Unit) {
        listOf(
            Triple(Icons.Rounded.Devices, context.getString(R.string.title_devices), Page.MainPage.Device.route),
            Triple(Icons.Rounded.Key, context.getString(R.string.title_keys), Page.MainPage.Keys.route),
            Triple(Icons.Rounded.Message, context.getString(R.string.title_messages), Page.MainPage.Message.route),
            Triple(Icons.Rounded.Settings, context.getString(R.string.title_settings), Page.MainPage.Setting.route)
        )
    }
    //endregion

    CompositionLocalProvider(
        LocalHomePageViewModel provides viewModel,
        LocalHomePageSnackBarHostState provides snackBarState
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(text = appBarTitle) }) },
            floatingActionButton = {
               AnimatedVisibility(visible = showFloatingAction, enter = scaleIn(), exit = scaleOut()){
                    FloatingActionButton(onClick = {
                        viewModel.onFabClick(navigationItems[currentIndex].third)
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    navigationItems.forEachIndexed { index, (icon, text, route) ->
                        NavigationBarItem(selected = currentIndex == index,
                            onClick = {
                                currentIndex = index
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id)
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon, contentDescription = text
                                )
                            },
                            label = { Text(text = text) })
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackBarState) }
        ) {
            Box(modifier = Modifier.padding(it)) {
                NavHost(
                    startDestination = Page.MainPage.Device.route,
                    navController = navController,
                ) {
                    composable(Page.MainPage.Device.route) {
                        showFloatingAction=true
                        appBarTitle = stringResource(id = R.string.title_devices)
                        DevicesPage()
                    }
                    composable(Page.MainPage.Keys.route) {
                        showFloatingAction=true
                        appBarTitle = stringResource(id = R.string.title_keys)
                        TokenPage()
                    }
                    composable(Page.MainPage.Message.route) {
                        showFloatingAction=true
                        appBarTitle = stringResource(id = R.string.title_messages)
                        MessagesPage()
                    }
                    composable(Page.MainPage.Setting.route) {
                        showFloatingAction=false
                        appBarTitle = stringResource(id = R.string.title_settings)
                        SettingsPage()
                    }
                }
            }
        }
    }
}

val LocalHomePageViewModel = compositionLocalOf<HomePageViewModel> { error("Not initialized") }
val LocalHomePageSnackBarHostState = compositionLocalOf<SnackbarHostState> { error("Not initialized") }
