package com.example.offlinearch.presentation.screens.mvi

import com.example.offlinearch.presentation.base.MviState

data class NewsState(
    val query: String = ""
) : MviState