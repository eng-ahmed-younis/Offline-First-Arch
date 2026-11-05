package com.example.offlinearch.presentation.screens

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.domain.usecase.GetNewsUseCase
import com.example.offlinearch.presentation.base.MviBaseViewModel
import com.example.offlinearch.presentation.screens.mvi.NewsAction
import com.example.offlinearch.presentation.screens.mvi.NewsIntent
import com.example.offlinearch.presentation.screens.mvi.NewsReducer
import com.example.offlinearch.presentation.screens.mvi.NewsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class NewsViewModel @Inject constructor(
    getNews: GetNewsUseCase,
) : MviBaseViewModel<NewsState, NewsAction, NewsIntent>(
    NewsState(),
    NewsReducer()
) {

    override fun handleIntent(intent: NewsIntent) {
        when (intent) {
            is NewsIntent.Search -> {
                onAction(NewsAction.UpdateQuery(intent.query))
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val news: Flow<PagingData<NewsArticle>> = snapshotFlow { viewState.query }
        .debounce(300.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { getNews(it) }
        .cachedIn(viewModelScope)
}
