package moe.peanutmelonseedbigalmond.push.ui.page

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.repository.config.ServerConfig
import moe.peanutmelonseedbigalmond.push.ui.MainActivity
import moe.peanutmelonseedbigalmond.push.ui.NavRoutes
import moe.peanutmelonseedbigalmond.push.ui.component.TextPreferences
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.SettingsPageViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun SettingsPage(parentNavHostController: NavHostController, modifier: Modifier = Modifier) {
    val viewmodel = viewModel(modelClass = SettingsPageViewModel::class.java)
    val userInfo by viewmodel.userInfo.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = LocalSnackBarHostState.current
    var cacheSize by remember { mutableLongStateOf(0L) }
    var confirmLogout by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "设置") })
        },
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        confirmLogout = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = if (userInfo == null) {
                                    "用户名正在加载"
                                } else {
                                    if (userInfo!!.name.isNullOrEmpty()) {
                                        "无法获取用户名"
                                    } else {
                                        userInfo!!.name!!
                                    }
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (userInfo != null && userInfo!!.name != null) {
                                Text(
                                    text = userInfo!!.email!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else if (userInfo == null) {
                                Text(
                                    text = "邮箱正在加载",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                    }
                }
            }

            item {
                TextPreferences(
                    modifier = Modifier
                        .height(64.dp)
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                    title = { Text("登录设备管理") }
                ) {
                    parentNavHostController.navigate(NavRoutes.loggedInDeviceManagePage)
                }
            }

            item { HorizontalDivider() }

            item {
                TextPreferences(
                    modifier = Modifier
                        .height(64.dp)
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                    title = { Text(text = "清除图片缓存") },
                    summary = {
                        Text(text = fileSizeToString(cacheSize))
                    }
                ) {
                    context.imageLoader.diskCache?.clear()
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            cacheSize = context.imageLoader.diskCache?.size ?: 0L
                        }
                        snackBarHostState.showSnackbar("已清除缓存")
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                TextPreferences(
                    modifier = Modifier
                        .height(64.dp)
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                    title = { Text("FCM 诊断页面") }
                ) {
                    val activity = context as MainActivity
                    val intent = Intent()
                    intent.setClassName(
                        "com.google.android.gms",
                        "com.google.android.gms.gcm.GcmDiagnostics"
                    )
                    try {
                        activity.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(e.message ?: e.toString())
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        launch {
            try {
                viewmodel.refreshUserInfo()
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                e.printStackTrace()
                snackBarHostState.showSnackbar("用户信息获取失败")
            }
        }
        launch {
            withContext(Dispatchers.IO) {
                cacheSize = context.imageLoader.diskCache?.size ?: 0L
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(onDismissRequest = { confirmLogout = false }, confirmButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        viewmodel.logout()
                    }
                    confirmLogout = false
                    ServerConfig.serverUrl = ""
                    ServerConfig.token = ""
                    parentNavHostController.navigate(NavRoutes.loginFollowingPage) {
                        popUpTo(parentNavHostController.currentBackStackEntry?.destination?.route!!) {
                            inclusive = true
                        }
                    }
                }) {
                Text(text = "确定")
            }
        }, text = {
            Text(text = "确定要退出登录吗？")
        })
    }
}

private fun fileSizeToString(size: Long): String {
    return when (size) {
        in 0L..1024L -> "%d B".format(size)
        in 1025L..1024L * 1024 -> "%.2f KB".format(size / 1024.0)
        in 1024L * 1024 + 1..1024L * 1024 * 1024 -> "%.2f MB".format(size / 1024 / 1024.0)
        else -> "%.2f GB".format(size / 1024 / 1024 / 1024.0)
    }
}