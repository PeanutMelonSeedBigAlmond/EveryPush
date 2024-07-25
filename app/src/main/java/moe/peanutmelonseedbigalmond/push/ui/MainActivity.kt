package moe.peanutmelonseedbigalmond.push.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import moe.peanutmelonseedbigalmond.push.BuildConfig
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.config.ServerConfig
import moe.peanutmelonseedbigalmond.push.ui.page.AllMessagesListPage
import moe.peanutmelonseedbigalmond.push.ui.page.LoggedInDeviceManagePage
import moe.peanutmelonseedbigalmond.push.ui.page.LoginFollowingPage
import moe.peanutmelonseedbigalmond.push.ui.page.MainPage
import moe.peanutmelonseedbigalmond.push.ui.page.MessageGroupDetailPage
import moe.peanutmelonseedbigalmond.push.ui.page.MessageShowPage
import moe.peanutmelonseedbigalmond.push.ui.theme.EveryPushTheme

class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        if (!it) {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            }
            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.data = uri
            startActivity(intent)
            Toast.makeText(this, "请打开通知权限", Toast.LENGTH_SHORT).show()
        }
    }

    private val intentChannel= Channel<Intent>(4,BufferOverflow.DROP_OLDEST)
    private val intentFlow=intentChannel.receiveAsFlow()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentChannel.trySend(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EveryPushTheme {
                val navController = rememberNavController()

                LaunchedEffect(key1 = Unit) {
                    intentFlow.collect{
                        if (it.action==Intent.ACTION_VIEW&&it.data?.scheme=="everypush"){
                            val uri=it.data?:return@collect
                            if (uri.path=="/pages/message"){
                                val messageId=uri.getQueryParameter("mesageId")?.toLongOrNull()?:return@collect
                                navController.navigate(
                                    navController.graph.findNode(NavRoutes.messageShowPage)!!.id,
                                    args = bundleOf("messageId" to messageId)
                                )
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (ServerConfig.isConfigValid()) {
                        NavRoutes.mainPage
                    } else {
                        NavRoutes.loginFollowingPage
                    }
                ) {
                    composable(NavRoutes.mainPage) {
                        MainPage(
                            modifier = Modifier.fillMaxSize(),
                            parentNavHostController = navController
                        )
                    }
                    composable(NavRoutes.messageGroupDetailPage, arguments = listOf(
                        navArgument("groupId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )) {
                        val args = it.arguments
                        MessageGroupDetailPage(
                            messageGroupId = args?.getString("groupId"),
                            modifier = Modifier.fillMaxSize(),
                            parentNavHostController = navController
                        )
                    }
                    composable(NavRoutes.loginFollowingPage) {
                        LoginFollowingPage(
                            modifier = Modifier.fillMaxSize(),
                            parentNavHostController = navController
                        )
                    }
                    composable(
                        NavRoutes.messageShowPage,
                        arguments = listOf(
                            navArgument("messageId") {
                                type = NavType.LongType
                            }
                        ),
                    ) {
                        val messageId = it.arguments!!.getLong("messageId")
                        MessageShowPage(
                            messageId = messageId,
                            modifier = Modifier.fillMaxSize(),
                            parentNavHostController = navController
                        )
                    }

                    composable(NavRoutes.allMessageListPage) {
                        AllMessagesListPage(
                            parentNavHostController = navController,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable(NavRoutes.loggedInDeviceManagePage){
                        LoggedInDeviceManagePage(modifier = Modifier.fillMaxSize(),parentNavHostController = navController)
                    }
                }
                LaunchedEffect(key1 = Unit) {
                    Client.userToken = ServerConfig.token
                    Client.serverAddress = ServerConfig.serverUrl
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        askNotificationPermission()
    }

    private fun askNotificationPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // ignore
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // ignore
        } else {
            // Directly ask for the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Toast.makeText(this, "请打开通知权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}