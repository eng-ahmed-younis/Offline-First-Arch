package com.example.offlinearch.presentation.screens.mvi

import com.example.offlinearch.presentation.base.MviIntent

sealed interface NewsIntent : MviIntent {
    data class Search(val query: String) : NewsIntent
}