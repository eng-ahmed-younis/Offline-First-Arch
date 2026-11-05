package com.example.offlinearch.presentation.screens.mvi

import com.example.offlinearch.presentation.base.MviEffect

sealed interface NewsEffect : MviEffect {
    data class ShowError(val message: String) : NewsEffect
}