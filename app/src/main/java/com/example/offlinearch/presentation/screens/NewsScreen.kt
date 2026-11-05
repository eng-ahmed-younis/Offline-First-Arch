package com.example.offlinearch.presentation.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.presentation.screens.components.NewsRow
import com.example.offlinearch.presentation.screens.mvi.NewsIntent
import com.example.offlinearch.presentation.screens.mvi.NewsState
import com.example.offlinearch.presentation.theme.OfflineArchTheme
import com.example.offlinearch.presentation.util.SampleNewsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel = hiltViewModel()) {
    val state = viewModel.viewState
    val items = viewModel.news.collectAsLazyPagingItems()

    NewsScreenContent(
        state = state,
        items = items,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreenContent(
    state: NewsState,
    items: LazyPagingItems<NewsArticle>,
    onIntent: (NewsIntent) -> Unit,
) {
    val refresh = items.loadState.refresh
    val append = items.loadState.append

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(NewsIntent.Search(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search title or ticker (e.g. PLTR)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onIntent(NewsIntent.Search("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )

            if (refresh is LoadState.Error && items.itemCount > 0) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "Showing saved news. Couldn't refresh.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = { items.refresh() }) { Text("Retry") }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = refresh is LoadState.Loading && items.itemCount > 0,
                onRefresh = { items.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    refresh is LoadState.Loading && items.itemCount == 0 ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }

                    refresh is LoadState.Error && items.itemCount == 0 ->
                        FullScreenMessage(
                            text = "No connection and no saved news yet.",
                            actionLabel = "Retry",
                            onAction = { items.retry() },
                        )

                    refresh is LoadState.NotLoading && items.itemCount == 0 ->
                        FullScreenMessage(
                            text = if (state.query.isBlank()) "No news available."
                            else "No saved news matches your search.",
                        )

                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                            items[index]?.let { article ->
                                NewsRow(article = article)
                                HorizontalDivider()
                            }
                        }

                        when (append) {
                            is LoadState.Loading -> item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is LoadState.Error -> item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { items.retry() }) { Text("Couldn't load more. Retry") }
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenMessage(
    text: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) { Text(actionLabel) }
        }
    }
}

@Preview(showBackground = true, name = "News List Preview - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "News List Preview - Dark")
@Composable
private fun SampleNewsListPreview() {
    OfflineArchTheme {
        Surface {
            Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Search title or ticker (e.g. PLTR)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                )
                LazyColumn(Modifier.fillMaxSize()) {
                    items(SampleNewsData.sampleArticles, key = { it.id }) { article ->
                        NewsRow(article = article)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
