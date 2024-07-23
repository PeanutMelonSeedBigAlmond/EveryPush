package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.utils.DateUtils
import moe.peanutmelonseedbigalmond.push.viewmodel.LoggedInDeviceManagePageViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun LoggedInDeviceManagePage(
    parentNavHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewmodel = viewModel(modelClass = LoggedInDeviceManagePageViewModel::class.java)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var refreshing by remember { viewmodel.refreshing }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            coroutineScope.launch {
                try {
                    refreshing = true
                    viewmodel.refreshLoggedInDevices()
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch {
                        snackbarHostState.showSnackbar("加载设备列表失败")
                    }
                } finally {
                    refreshing = false
                }
            }
        })
    val deviceList = remember { viewmodel.loggedInDevices }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = "登录设备管理") }, navigationIcon = {
                IconButton(onClick = { parentNavHostController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (deviceList.any { it.current }) {
                    stickyHeader {
                        Text(text = "当前设备", color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    val items = deviceList.filter { it.current }
                    items(count = items.size) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "${items[it].platform} · ${items[it].name}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = DateUtils.timestampToString(items[it].updatedAt))
                            HorizontalDivider()
                        }
                    }
                }

                if (deviceList.any { !it.current }) {
                    stickyHeader {
                        Text(text = "其他设备", color = MaterialTheme.colorScheme.primary)
                    }
                    val items = deviceList.filter { !it.current }
                    items(count = items.size) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${items[it].platform} · ${items[it].name}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(text = DateUtils.timestampToString(items[it].updatedAt))
                            }
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        refreshing = true
                                        viewmodel.logout(items[it].token)
                                    } catch (_: CancellationException) {
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        launch {
                                            snackbarHostState.showSnackbar(
                                                e.localizedMessage ?: e.toString()
                                            )
                                        }
                                    } finally {
                                        refreshing = false
                                    }
                                }

                            }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                    }
                }

                if (deviceList.any { !it.current }) {
                    stickyHeader {
                        Text(text = "登出", color = MaterialTheme.colorScheme.primary)
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        refreshing = true
                                        viewmodel.logoutOthers()
                                    } catch (_: CancellationException) {
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        launch {
                                            snackbarHostState.showSnackbar(
                                                e.localizedMessage ?: e.toString()
                                            )
                                        }
                                    } finally {
                                        refreshing = false
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null
                                )
                                Text(text = "登出除本机外所有设备")
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        try {
            refreshing = true
            viewmodel.refreshLoggedInDevices()
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            launch {
                snackbarHostState.showSnackbar("加载设备列表失败")
            }
        } finally {
            refreshing = false
        }
    }
}