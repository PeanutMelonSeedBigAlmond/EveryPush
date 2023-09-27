package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.statusBarsPadding
import moe.peanutmelonseedbigalmond.push.ui.component.page.ConfigureServerPage
import moe.peanutmelonseedbigalmond.push.ui.component.page.GoogleLoginPage
import moe.peanutmelonseedbigalmond.push.ui.component.page.HomePage
import moe.peanutmelonseedbigalmond.push.ui.component.page.Page
import moe.peanutmelonseedbigalmond.push.ui.theme.MyAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    //region 变量
    val navController = rememberNavController()
    val globalViewModel = LocalGlobalViewModel.current
    var showLoadingDialog by remember { mutableStateOf(false) }
    //endregion

    //region 函数
    /**
     * 判断是否需要登录
     * @return Boolean
     */
    @Composable
    fun needLogin(): Boolean = globalViewModel.url.isBlank()
            || globalViewModel.token.isBlank()
            || globalViewModel.tokenExpiredAt <= System.currentTimeMillis()
    //endregion

    MyAppTheme {
        CompositionLocalProvider(
            LocalNavHostController provides navController
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = if (needLogin()) {
                        Page.Guide.route
                    } else {
                        Page.Main.route
                    },
                    modifier = Modifier
                        .statusBarsPadding(),
                ) {
                    navigation(
                        startDestination = Page.ConfigureServer.route,
                        route = Page.Guide.route
                    ) {
                        composable(Page.ConfigureServer.route) { ConfigureServerPage() }
                        composable(Page.GoogleLogin.route) { GoogleLoginPage() }
                    }
                    composable(Page.Main.route) {
                        HomePage()
                    }
                }
            }
        }
    }
}
