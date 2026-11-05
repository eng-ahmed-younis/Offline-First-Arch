package com.example.offlinearch.presentation.screens.mvi

import com.example.offlinearch.presentation.base.MviAction

sealed interface NewsAction : MviAction {
    data class UpdateQuery(val query: String) : NewsAction
}