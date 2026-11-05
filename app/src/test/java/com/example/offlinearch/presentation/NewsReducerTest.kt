package com.example.offlinearch.presentation

import com.example.offlinearch.presentation.screens.mvi.NewsAction
import com.example.offlinearch.presentation.screens.mvi.NewsState
import com.example.offlinearch.presentation.screens.mvi.NewsReducer
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsReducerTest {

    private val reducer = NewsReducer()

    @Test
    fun `reduce UpdateQuery action updates query state`() {
        val initialState = NewsState(query = "")
        val action = NewsAction.UpdateQuery(query = "PLTR")

        val newState = reducer.reduce(action, initialState)

        assertEquals("PLTR", newState.query)
    }
}
