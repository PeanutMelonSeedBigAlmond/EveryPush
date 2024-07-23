package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.bean.DeviceInfo
import moe.peanutmelonseedbigalmond.push.ui.component.DeviceItem
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.DevicesPageViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DevicesPage(modifier: Modifier = Modifier) {
    val viewModel = viewModel(modelClass = DevicesPageViewModel::class.java)
    var refreshing by remember { viewModel.refreshing }
    var currentDeviceToken by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val devices = viewModel.devices.collectAsLazyPagingItems()
    val refreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = { devices.refresh() })
    val snackBarHostState = LocalSnackBarHostState.current
    var currentRenameDevice by remember { viewModel.currentRenameDevice }

    LaunchedEffect(key1 = Unit) {
        currentDeviceToken=viewModel.getDeviceToken()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设备") }, actions = {
                IconButton(onClick = {
                    coroutineScope.launch {
                        try {
                            refreshing = true
                            viewModel.registerDevice()
                            devices.refresh()
                            refreshing = false
                        } catch (_: CancellationException) {
                        } catch (e: Exception) {
                            e.printStackTrace()
                            launch {
                                snackBarHostState.showSnackbar(e.localizedMessage ?: e.toString())
                            }
                            refreshing = false
                        }
                    }
                }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            })
        }
    ) {
        Box(
            modifier = modifier
                .padding(it)
                .pullRefresh(refreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    devices.itemCount,
                    key = { devices[it]!!.id }
                ) {
                    DeviceItem(
                        deviceInfo = devices[it]!!,
                        isCurrentDevice = devices[it]!!.deviceToken == currentDeviceToken,
                        onRenameRequest = { currentRenameDevice = it },
                        onRemoveRequest = {
                            coroutineScope.launch {
                                refreshing = true
                                try {
                                    viewModel.removeDevice(it.id)
                                    devices.refresh()
                                    refreshing = false
                                } catch (_: CancellationException) {
                                    refreshing = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    launch {
                                        snackBarHostState.showSnackbar(
                                            e.localizedMessage ?: e.toString()
                                        )
                                    }
                                    refreshing = false
                                }
                            }
                        }
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            LaunchedEffect(key1 = devices.loadState.refresh) {
                when (val state = devices.loadState.refresh) {
                    is LoadState.Loading -> {
                        refreshing = true
                    }

                    is LoadState.NotLoading -> {
                        refreshing = false
                    }

                    is LoadState.Error -> {
                        refreshing = false
                        val err = state.error
                        coroutineScope.launch {
                            val snackBarResult = snackBarHostState.showSnackbar(
                                err.localizedMessage ?: err.toString(),
                                actionLabel = "重试",
                                withDismissAction = true
                            )
                            if (snackBarResult == SnackbarResult.ActionPerformed) {
                                devices.retry()
                            }
                        }
                    }
                }
            }
        }
    }
    if (currentRenameDevice != null) {
        RenameDeviceDialog(
            deviceInfo = currentRenameDevice!!,
            onDismissRequest = { currentRenameDevice = null },
            onConfirm = { device, newName ->
                currentRenameDevice = null
                coroutineScope.launch {
                    refreshing = true
                    try {
                        viewModel.renameDevice(device.id, newName)
                        devices.refresh()
                        refreshing = false
                    } catch (_: CancellationException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                        launch {
                            snackBarHostState.showSnackbar(
                                e.localizedMessage ?: e.toString()
                            )
                        }
                        refreshing = false
                    }
                }
            })
    }
}

@Composable
fun RenameDeviceDialog(
    deviceInfo: DeviceInfo,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onConfirm: (DeviceInfo, String) -> Unit = { _, _ -> }
) {
    var name by remember { mutableStateOf(deviceInfo.name) }
    val nameValid by remember { derivedStateOf { name.isNotBlank() } }

    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = { onConfirm(deviceInfo, name.trim()) }, enabled = nameValid) {
            Text(text = "确定")
        }
    }, text = {
        TextField(
            value = name,
            onValueChange = { name = it },
            supportingText = if (!nameValid) {
                {
                    Text(text = "不能为空")
                }
            } else null,
            singleLine = true,
            label = { Text(text = "名称") },
            isError = !nameValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }, title = { Text(text = "重命名") }, modifier = modifier)
}