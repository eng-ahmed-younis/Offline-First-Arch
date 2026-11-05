package com.example.offlinearch.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.offlinearch.data.data_source.local.AppDatabase
import com.example.offlinearch.data.data_source.remote.NewsApi
import com.example.offlinearch.data.mapper.toDomain
import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [NewsRepositoryImpl] is the concrete implementation of [NewsRepository].
 * It coordinates data flow between the remote API (`NewsApi`) and local Room database (`AppDatabase`),
 * providing a unified Paging stream to the domain and presentation layers.
 */
@OptIn(ExperimentalPagingApi::class)
class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val db: AppDatabase,
) : NewsRepository {

    /**
     * Returns a reactive stream ([Flow]) of [PagingData] containing [NewsArticle] items.
     *
     * **Strategy:**
     * - **Normal Feed (`query` is blank):** Configures Paging 3 `Pager` with [NewsRemoteMediator]
     *   to automatically fetch remote pages, cache them in Room, and emit items from `db.newsDao().pagingSource()`.
     * - **Search Mode (`query` is active):** Disables [NewsRemoteMediator] (`remoteMediator = null`)
     *   and queries local cached items using `db.newsDao().search(query)`, delivering instant local search
     *   over offline-first cached data.
     *
     * @param query Search term matching article titles or ticker symbols.
     * @return Flow of paged domain models ([NewsArticle]).
     */
    override fun news(query: String): Flow<PagingData<NewsArticle>> {
        val searching = query.isNotBlank()
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
            // RemoteMediator is only active when not searching (network pagination vs local search filter).
            remoteMediator = if (searching) null else NewsRemoteMediator(api, db),
            pagingSourceFactory = {
                if (searching) {
                    db.newsDao().search(query.trim())
                } else {
                    db.newsDao().pagingSource()
                }
            },
        ).flow.map { pagingData ->
            // Map Room Entity objects (`NewsEntity`) to clean domain models (`NewsArticle`).
            pagingData.map { entity -> entity.toDomain() }
        }
    }
}
