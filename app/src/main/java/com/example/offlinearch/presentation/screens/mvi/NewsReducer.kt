package com.example.offlinearch.presentation.screens.mvi

import com.example.offlinearch.presentation.base.Reducer

class NewsReducer : Reducer<NewsAction, NewsState> {
    override fun reduce(action: NewsAction, state: NewsState): NewsState {
        return when (action) {
            is NewsAction.UpdateQuery -> state.copy(query = action.query)
        }
    }
}