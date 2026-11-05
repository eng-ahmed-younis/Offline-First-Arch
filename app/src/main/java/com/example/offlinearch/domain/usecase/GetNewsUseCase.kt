package com.example.offlinearch.domain.usecase

import androidx.paging.PagingData
import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<NewsArticle>> = repository.news(query)
}
