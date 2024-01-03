package moe.peanutmelonseedbigalmond.push.ui.component.page.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.datasource.DevicesDataSource
import moe.peanutmelonseedbigalmond.push.network.response.DeviceResponse
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageSnackBarHostState
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.widget.DeviceListItem
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MyAlertDialog
import moe.peanutmelonseedbigalmond.push.ui.data.DeviceData
import moe.peanutmelonseedbigalmond.push.ui.viewmodel.GlobalViewModel
import moe.peanutmelonseedbigalmond.push.utils.DeviceUtil

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DevicesPage() {
    //region 变量
    val globalViewModel = LocalGlobalViewModel.current
    val homePageViewModel = LocalHomePageViewModel.current
    val vm = viewModel<DevicePageViewModel>(factory = DevicePageViewModel.Factory(globalViewModel))
    val snackBarHostState = LocalHomePageSnackBarHostState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentRenamingDevice by remember { mutableStateOf<DeviceData?>(null) }
    val data = vm.data.collectAsLazyPagingItems()
    val lazyColumnState = rememberLazyListState()
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

    fun refreshDevices(){
        data.refresh()
    }

    val refreshState = rememberPullRefreshState(
        refreshing = data.loadState.refresh is LoadState.Loading,
        onRefresh = {
            refreshDevices()
        }
    )

    // fab 回调：注册设备
    homePageViewModel.devicePageOnFabClick = {
        coroutineScope.launch {
            try {
                registerDevice()
                refreshDevices()
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                e.printStackTrace()
                showMessageWithSnackBar(e.toString())
            }
        }
    }
    //endregion

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = lazyColumnState,
        ) {
            items(items = data, key = { it.id }) {
                DeviceListItem(
                    thisDeviceFcmToken = globalViewModel.fcmToken ?: "",
                    device = it!!.let { item -> DeviceData(item.id, item.name, item.deviceId) },
                    onRenameActionSelected = { deviceData ->
                        currentRenamingDevice = deviceData
                    },
                    onDeleteActionSelected = {
                        coroutineScope.launch {
                            try {
                                globalViewModel.client.removeDevice(it.id)
                                data.refresh()
                            } catch (_: CancellationException) {
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showMessageWithSnackBar(e.toString())
                            }
                        }
                    })
            }
            if (data.loadState.refresh is LoadState.Error) {
                item {
                    val err = (data.loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = err.message ?: err.toString())
                        TextButton(onClick = { data.retry() }) {
                            Text(text = "Retry")
                        }
                    }
                }
            } else if (data.loadState.refresh is LoadState.NotLoading && data.itemCount <= 0) {
                item {
                    Text(
                        text = stringResource(id = R.string.tip_no_device),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing = true,
            state = refreshState,
            modifier = Modifier.align(
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
                coroutineScope.launch {
                    try {
                        globalViewModel.client.renameDevice(device.id, newName)
                        refreshDevices()
                    } catch (_: CancellationException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showMessageWithSnackBar(e.toString())
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

class DevicePageViewModel(private val globalViewModel: GlobalViewModel) : ViewModel() {
    val data=Pager(PagingConfig(pageSize = 20)) {
        DevicesDataSource(globalViewModel.client)
    }.flow.cachedIn(viewModelScope)

    class Factory(private val globalViewModel: GlobalViewModel) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DevicePageViewModel(globalViewModel) as T
        }
    }
}