package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.bean.KeyInfo
import moe.peanutmelonseedbigalmond.push.ui.component.KeyItem
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.KeysPageViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun KeysPage(modifier: Modifier = Modifier) {
    val viewModel = viewModel<KeysPageViewModel>()
    var refreshing by remember { viewModel.refreshing }
    val keysList = viewModel.keyList.collectAsLazyPagingItems()
    val snackBarStateHost = LocalSnackBarHostState.current
    val coroutineScope = rememberCoroutineScope()
    var currentRenamingKey by remember { viewModel.currentRenamingKey }
    val swipeRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            coroutineScope.launch {
                refreshing = true
                try {
                    keysList.refresh()
                    refreshing = false
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch {
                        snackBarStateHost.showSnackbar(e.localizedMessage ?: e.toString())
                    }
                    refreshing = false
                }
            }
        }
    )

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = { Text(text = "密钥") },
            actions = {
                IconButton(onClick = {
                    coroutineScope.launch {
                        refreshing = true
                        try {
                            viewModel.generateKey()
                            keysList.refresh()
                            refreshing = false
                        } catch (_: CancellationException) {
                        } catch (e: Exception) {
                            e.printStackTrace()
                            launch {
                                snackBarStateHost.showSnackbar(e.localizedMessage ?: e.toString())
                            }
                            refreshing = false
                        }
                    }
                }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            }
        )
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(swipeRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(keysList.itemCount, key = { keysList[it]!!.id }) {
                    KeyItem(
                        keyInfo = keysList[it]!!,
                        modifier = Modifier.fillMaxWidth(),
                        onRename = {
                            currentRenamingKey = it
                        },
                        onCopy = {
                            viewModel.copyKey(it)
                            coroutineScope.launch {
                                snackBarStateHost.showSnackbar("复制成功")
                            }
                        },
                        onDelete = {
                            coroutineScope.launch {
                                refreshing = true
                                try {
                                    viewModel.deleteKey(it.id)
                                    keysList.refresh()
                                    refreshing = false
                                    launch {
                                        snackBarStateHost.showSnackbar("已删除")
                                    }
                                } catch (_: CancellationException) {
                                    refreshing = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    launch {
                                        snackBarStateHost.showSnackbar(
                                            e.localizedMessage ?: e.toString()
                                        )
                                        refreshing = false
                                    }
                                }
                            }
                        },
                        onReset = {
                            coroutineScope.launch {
                                refreshing = true
                                try {
                                    viewModel.resetKey(it.id)
                                    keysList.refresh()
                                    refreshing = false
                                    snackBarStateHost.showSnackbar("已重置密钥")
                                } catch (_: CancellationException) {
                                    refreshing = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    launch {
                                        snackBarStateHost.showSnackbar(
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
                state = swipeRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            LaunchedEffect(key1 = keysList.loadState.refresh) {
                when (val state = keysList.loadState.refresh) {
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
                            val snackBarResult = snackBarStateHost.showSnackbar(
                                err.localizedMessage ?: err.toString(),
                                actionLabel = "重试",
                                withDismissAction = true
                            )
                            if (snackBarResult == SnackbarResult.ActionPerformed) {
                                keysList.retry()
                            }
                        }
                    }
                }
            }
        }
    }
    if (currentRenamingKey != null) {
        RenameDialog(
            modifier = Modifier.fillMaxWidth(),
            keyInfo = currentRenamingKey!!,
            onDismiss = { currentRenamingKey = null },
            onRename = { keyInfo, name ->
                coroutineScope.launch {
                    refreshing = true
                    try {
                        viewModel.renameKey(keyInfo.id, name)
                        keysList.refresh()
                        refreshing = false
                        currentRenamingKey = null
                    } catch (_: CancellationException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                        launch {
                            snackBarStateHost.showSnackbar(e.localizedMessage ?: e.toString())
                        }
                    }
                    currentRenamingKey = null
                }
            }
        )
    }

}

@Composable
fun RenameDialog(
    keyInfo: KeyInfo,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onRename: (KeyInfo, String) -> Unit = { _, _ -> }
) {
    var name by remember { mutableStateOf(keyInfo.name) }
    val nameValid by remember { derivedStateOf { name.isNotBlank() } }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = { onRename(keyInfo, name) }) {
            Text(text = "确定")
        }
    }, title = {
        Text(text = "重命名")
    }, text = {
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !nameValid,
            supportingText = if (!nameValid) {
                { Text(text = "名称不能为空") }
            } else null,
            label = { Text(text = "名称") }
        )
    }, modifier = modifier)
}