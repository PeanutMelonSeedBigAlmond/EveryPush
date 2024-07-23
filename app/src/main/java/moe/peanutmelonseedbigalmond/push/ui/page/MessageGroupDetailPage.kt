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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.ui.NavRoutes
import moe.peanutmelonseedbigalmond.push.ui.component.MessageItem
import moe.peanutmelonseedbigalmond.push.viewmodel.MessageGroupDetailPageViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MessageGroupDetailPage(
    messageGroupId: String?,
    parentNavHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<MessageGroupDetailPageViewModel>(factory = viewModelFactory {
        initializer {
            MessageGroupDetailPageViewModel(messageGroupId)
        }
    })
    val title by remember { viewModel.title }
    val messageList = viewModel.messageExcerptList.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var refreshing by remember { viewModel.refreshing }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            coroutineScope.launch {
                try {
                    refreshing = true
                    messageList.refresh()
                    refreshing = false
                } catch (_: CancellationException) {
                    refreshing = false
                } catch (e: Exception) {
                    launch { snackBarHostState.showSnackbar(e.localizedMessage ?: e.toString()) }
                    e.printStackTrace()
                    refreshing = false
                }
            }
        }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        parentNavHostController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) {
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
                .padding(it)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(messageList.itemCount, key = {
                    messageList[it]!!.id
                }) {
                    MessageItem(
                        messageExcerptInfo = messageList[it]!!,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            parentNavHostController.navigate(
                                parentNavHostController.graph.findNode(
                                    NavRoutes.messageShowPage
                                )!!.id,
                                args = bundleOf("messageId" to it.id)
                            )
                        },
                        onDelete = {
                            coroutineScope.launch {
                                try {
                                    refreshing = true
                                    viewModel.deleteMessage(it.id)
                                    messageList.refresh()
                                } catch (_: CancellationException) {
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    launch {
                                        snackBarHostState.showSnackbar(
                                            e.localizedMessage ?: e.toString()
                                        )
                                    }
                                } finally {
                                    refreshing = false
                                }
                            }
                        }
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            LaunchedEffect(key1 = messageList.loadState.refresh) {
                when (val state = messageList.loadState.refresh) {
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
                            val snackBarResult = snackBarHostState.showSnackbar(
                                err.localizedMessage ?: err.toString(),
                                actionLabel = "重试",
                                withDismissAction = true
                            )
                            if (snackBarResult == SnackbarResult.ActionPerformed) {
                                messageList.retry()
                            }
                        }
                    }
                }
            }

            LaunchedEffect(key1 = messageList.loadState.append) {
                if (messageList.loadState.append is LoadState.NotLoading && messageList.itemCount == 0) {
                    launch {
                        snackBarHostState.showSnackbar("没有内容")
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        launch {
            try {
                viewModel.updateMessageGroupInfo(messageGroupId)
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}