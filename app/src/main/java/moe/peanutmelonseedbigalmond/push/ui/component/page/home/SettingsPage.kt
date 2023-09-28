package moe.peanutmelonseedbigalmond.push.ui.component.page.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivity
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivityCoroutineScope
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.LocalNavHostController
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageSnackBarHostState
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.Page
import moe.peanutmelonseedbigalmond.push.ui.component.widget.preference.MenuPreferences
import moe.peanutmelonseedbigalmond.push.ui.component.widget.preference.PreferenceGroup
import moe.peanutmelonseedbigalmond.push.ui.component.widget.preference.TextPreferences

@Composable
fun SettingsPage() {
    //region 变量
    val appNavHostController = LocalNavHostController.current
    val globalViewModel = LocalGlobalViewModel.current
    val homePageViewModel = LocalHomePageViewModel.current
    val activityCoroutineScope = LocalActivityCoroutineScope.current
    val deviceList by remember(homePageViewModel) { homePageViewModel.deviceList }
    var username by remember { mutableStateOf("") }
    //endregion

    //region 函数
    suspend fun getUserInfo() {
        try {
            val userInfo = globalViewModel.client.getUserInfo()
            username = userInfo.username
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            Log.w("SettingsPage", "getUserInfo: 获取用户信息失败")
            e.printStackTrace()
        }
    }
    //endregion

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        homePageViewModel.appBarTitle = stringResource(id = R.string.title_settings)
        UserInfoWidget(
            username = username,
            onLogoutAction = {
                val localClient = Client(globalViewModel.url)
                localClient.token = globalViewModel.token
                localClient.tokenExpiredAt = globalViewModel.tokenExpiredAt
                deviceList
                    .filter { it.deviceId == AppConfigurationRepository.fcmPushToken }
                    .forEach {
                        activityCoroutineScope.launch {
                            try {
                                localClient.removeDevice(it.id)
                            } catch (e: Exception) {
                                Log.w(
                                    "SettingsPage",
                                    "SettingsPage: 移除设备失败, device=$it"
                                )
                                e.printStackTrace()
                            }
                        }
                    }

                globalViewModel.clearUserConfig()
                appNavHostController.navigate(Page.Guide.route) {
                    popUpTo(
                        appNavHostController.currentBackStackEntry?.destination?.route ?: ""
                    ) {
                        inclusive = true
                    }
                }
            }
        )
        CleanImageCachePref()

        PreferenceGroup(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(id = R.string.title_other_settings)
        ) {
            FCMDiagnosticsPerf()
        }
    }

    LaunchedEffect(Unit) {
        getUserInfo()
    }
}

@Composable
private fun UserInfoWidget(username: String, onLogoutAction: () -> Unit) {
    PreferenceGroup(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        title = stringResource(id = R.string.title_account_settings)
    ) {
        MenuPreferences(
            modifier = Modifier
                .fillMaxWidth(),
            optionsText = arrayOf(
                stringResource(id = R.string.confirm),
                stringResource(id = R.string.cancel)
            ),
            optionsId = arrayOf("confirm", "cancel"),
            title = { Text(text = stringResource(id = R.string.account)) },
            summary = { Text(text = username) },
            menuTitle = stringResource(id = R.string.logout),
        ) {
            when (it) {
                "confirm" -> onLogoutAction()
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
@Preview
fun CleanImageCachePref() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageCacheSize by remember { mutableLongStateOf(-1) }

    suspend fun calculateCacheSize() = withContext(Dispatchers.IO) {
        imageCacheSize = context.imageLoader.diskCache?.size ?: 0L
    }

    PreferenceGroup(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        title = stringResource(id = R.string.title_cache_settings)
    ) {
        TextPreferences(
            modifier = Modifier.fillMaxWidth(),
            title = { Text(text = stringResource(id = R.string.clear_images_cache)) },
            summary = { if (imageCacheSize > 0) Text(fileSizeToString(imageCacheSize)) },
            enabled = imageCacheSize > 0,
        ) {
            context.imageLoader.diskCache?.clear()
            coroutineScope.launch { calculateCacheSize() }
        }
    }

    LaunchedEffect(Unit) {
        calculateCacheSize()
    }
}

@Composable
fun FCMDiagnosticsPerf() {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val snackBarHostState = LocalHomePageSnackBarHostState.current
    val coroutineScope = rememberCoroutineScope()
    TextPreferences(
        modifier = Modifier.fillMaxWidth(),
        title = { Text(text = stringResource(id = R.string.fcm_diagnostics_perf)) },
        summary = {
            Text(
                text = stringResource(id = R.string.fcm_diagnostics_pref_summary)
            )
        }) {
        val intent = Intent()
        intent.setClassName("com.google.android.gms", "com.google.android.gms.gcm.GcmDiagnostics")
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                snackBarHostState.showSnackbar(context.getString(R.string.error_cannot_start_activity))
            }
        }
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