package com.example.offlinearch.domain.repository

import androidx.paging.PagingData
import com.example.offlinearch.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun news(query: String): Flow<PagingData<NewsArticle>>
}
