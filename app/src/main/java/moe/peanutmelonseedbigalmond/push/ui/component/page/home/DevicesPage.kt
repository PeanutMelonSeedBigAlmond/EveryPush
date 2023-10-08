package moe.peanutmelonseedbigalmond.push.ui.component.page.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.network.response.DeviceResponse
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageSnackBarHostState
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.widget.DeviceListItem
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MyAlertDialog
import moe.peanutmelonseedbigalmond.push.ui.data.DeviceData
import moe.peanutmelonseedbigalmond.push.utils.DeviceUtil

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DevicesPage() {
    //region 变量
    val globalViewModel = LocalGlobalViewModel.current
    val homePageViewModel = LocalHomePageViewModel.current
    val snackBarHostState = LocalHomePageSnackBarHostState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var devices by remember(globalViewModel) { globalViewModel.deviceList }
    var isRefreshing by remember { mutableStateOf(false) }
    var currentRenamingDevice by remember { mutableStateOf<DeviceData?>(null) }
    //endregion

    //region 函数
    /**
     * 显示提示
     * @param message String
     * @return Job
     */
    fun showMessageWithSnackBar(message: String) = coroutineScope.launch {
        snackBarHostState.showSnackbar(message)
    }

    /**
     * 获取已经注册的设备列表
     * @return Job
     */
    suspend fun listDevices() {
        isRefreshing = true
        try {
            val deviceList = globalViewModel.client.listDevices()
            devices = deviceList.map {
                return@map DeviceData(it.id, it.name, it.deviceId)
            }
        } catch (_: CancellationException) {
            devices = emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            showMessageWithSnackBar(e.toString())
        } finally {
            isRefreshing = false
        }
    }

    /**
     * 注册设备
     * @return DeviceRegisterResponse
     */
    suspend fun registerDevice(): DeviceResponse {
        if (globalViewModel.fcmToken == null) {
            throw Exception(context.getString(R.string.get_firebase_token_failed))
        } else {
            return globalViewModel.client.registerDevice(
                globalViewModel.fcmToken!!,
                DeviceUtil.getDeviceName()
            )
        }
    }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                listDevices()
            }
        }
    )

    // fab 回调：注册设备
    homePageViewModel.devicePageOnFabClick = {
        isRefreshing = true
        coroutineScope.launch {
            try {
                registerDevice()
                listDevices()
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                e.printStackTrace()
                showMessageWithSnackBar(e.toString())
            } finally {
                isRefreshing = false
            }
        }
    }
    //endregion

    LaunchedEffect(Unit) {
        listDevices()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState),
        contentAlignment = Alignment.Center
    ) {
        homePageViewModel.appBarTitle = stringResource(id = R.string.title_devices)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (devices.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.tip_no_device),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(devices.size, key = { it }) {
                DeviceListItem(
                    thisDeviceFcmToken = globalViewModel.fcmToken ?: "",
                    device = devices[it],
                    onRenameActionSelected = { deviceData -> currentRenamingDevice = deviceData },
                    onDeleteActionSelected = {
                        isRefreshing = true
                        coroutineScope.launch {
                            try {
                                globalViewModel.client.removeDevice(it.id)
                                listDevices()
                            } catch (_: CancellationException) {
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showMessageWithSnackBar(e.toString())
                            } finally {
                                isRefreshing = false
                            }
                        }
                    })
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing, state = refreshState, modifier = Modifier.align(
                Alignment.TopCenter
            )
        )
    }

    if (currentRenamingDevice != null) {
        RenameDeviceDialog(
            device = currentRenamingDevice!!,
            onCancel = {
                currentRenamingDevice = null
            },
            onConfirm = { device, newName ->
                currentRenamingDevice = null
                isRefreshing = true
                coroutineScope.launch {
                    try {
                        globalViewModel.client.renameDevice(device.id, newName)
                        listDevices()
                    } catch (_: CancellationException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showMessageWithSnackBar(e.toString())
                    } finally {
                        isRefreshing = false
                    }
                }
            })
    }
}

@Composable
private fun RenameDeviceDialog(
    device: DeviceData,
    onCancel: () -> Unit,
    onConfirm: (DeviceData, String) -> Unit,
) {
    val context = LocalContext.current
    var newDeviceName by remember { mutableStateOf(device.name) }
    var inputError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    MyAlertDialog(
        title = { Text(text = stringResource(id = R.string.rename)) },
        content = {
            OutlinedTextField(
                value = newDeviceName,
                onValueChange = { newDeviceName = it },
                isError = inputError,
                supportingText = { if (inputError) Text(text = errorMessage) }
            )
        },
        onDismiss = { onCancel() },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newDeviceName.isNotEmpty()) {
                    inputError = false
                    onConfirm(device, newDeviceName)
                } else {
                    errorMessage = context.getString(R.string.field_is_required)
                    inputError = true
                }
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        })
}

