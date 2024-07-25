package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.bean.MessageGroup
import moe.peanutmelonseedbigalmond.push.ui.NavRoutes
import moe.peanutmelonseedbigalmond.push.ui.component.MessageGroupItem
import moe.peanutmelonseedbigalmond.push.ui.component.MessageGroupItemWithOptions
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.MessageGroupPageViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun MessageGroupPage(parentNavHostController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel = viewModel<MessageGroupPageViewModel>()
    val messageGroupList = viewModel.messageGroupList.collectAsLazyPagingItems()
    var refreshing by remember { viewModel.refreshing }
    val coroutineScope = rememberCoroutineScope()
    val snackBarStateHost = LocalSnackBarHostState.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            coroutineScope.launch {
                refreshing = true
                try {
                    messageGroupList.refresh()
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
    var currentRenamingMessageGroup by remember { viewModel.currentRenamingMessageGroup }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = "消息组") })
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
                .padding(it)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 全部消息
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        onClick = {
                            parentNavHostController.navigate(
                                parentNavHostController.graph.findNode(NavRoutes.allMessageListPage)!!.id,
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                top = 24.dp,
                                bottom = 24.dp,
                                start = 12.dp,
                                end = 12.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                            )
                            Text(
                                text = "全部消息",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                            )
                        }
                    }
                }

                stickyHeader {
                    Text(
                        text = "分组",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    MessageGroupItem(
                        groupId = null,
                        name = "默认",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        onClick = {
                            parentNavHostController.navigate(
                                parentNavHostController.graph.findNode(NavRoutes.messageGroupDetailPage)!!.id,
                                args = bundleOf("groupId" to it)
                            )
                        }
                    )
                }

                items(messageGroupList.itemCount, key = {
                    messageGroupList[it]!!.groupId
                }) {
                    MessageGroupItemWithOptions(
                        messageGroup = messageGroupList[it]!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        onDelete = {
                            refreshing = true
                            coroutineScope.launch {
                                try {
                                    viewModel.deleteMessageGroup(it.groupId)
                                    messageGroupList.refresh()
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
                                    }
                                    refreshing = false
                                }
                            }
                        },
                        onRename = { currentRenamingMessageGroup = it },
                        onClick = {
                            parentNavHostController.navigate(
                                parentNavHostController.graph.findNode(NavRoutes.messageGroupDetailPage)!!.id,
                                args = bundleOf("groupId" to it.groupId)
                            )
                        }
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            LaunchedEffect(key1 = messageGroupList.loadState.refresh) {
                when (val state = messageGroupList.loadState.refresh) {
                    is LoadState.Loading -> {
                        refreshing = true
                    }

                    is LoadState.NotLoading -> {
                        refreshing = false
                    }

                    is LoadState.Error -> {
                        refreshing = false
                        val err = state.error
                        launch {
                            val snackBarResult = snackBarStateHost.showSnackbar(
                                err.localizedMessage ?: err.toString(),
                                actionLabel = "重试",
                                withDismissAction = true
                            )
                            if (snackBarResult == SnackbarResult.ActionPerformed) {
                                messageGroupList.retry()
                            }
                        }
                    }
                }
            }
        }
    }

    if (currentRenamingMessageGroup != null) {
        RenameMessageGroupDialog(messageGroup = currentRenamingMessageGroup!!, onDismissRequest = {
            currentRenamingMessageGroup = null
        }, onConfirm = { messageGroup, name ->
            currentRenamingMessageGroup = null
            coroutineScope.launch {
                refreshing = true
                try {
                    viewModel.renameMessageGroup(messageGroup.groupId, name)
                    messageGroupList.refresh()
                    refreshing = false
                } catch (_: CancellationException) {
                    refreshing = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch {
                        snackBarStateHost.showSnackbar(e.localizedMessage ?: e.toString())
                    }
                    refreshing = false
                }
            }
        })
    }

    LaunchedEffect(key1 = Unit) {
        try {
            viewModel.syncMessageGroups()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
private fun RenameMessageGroupDialog(
    messageGroup: MessageGroup,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onConfirm: (messageGroup: MessageGroup, name: String) -> Unit = { _, _ -> }
) {
    var name by remember { mutableStateOf(messageGroup.name) }
    val nameValid by remember { derivedStateOf { name.isNotBlank() } }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(messageGroup, name) },
                enabled = nameValid
            ) {
                Text(text = "确定")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "名称") },
                    maxLines = 1
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = messageGroup.groupId,
                    label = { Text(text = "ID") },
                    maxLines = 1,
                    onValueChange = {},
                    enabled = false
                )
            }
        },
        title = {
            Text(text = "重命名消息组")
        },
        modifier = modifier
    )
}