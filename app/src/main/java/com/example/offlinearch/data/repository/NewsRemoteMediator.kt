package com.example.offlinearch.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.offlinearch.data.data_source.local.AppDatabase
import com.example.offlinearch.data.data_source.local.NewsEntity
import com.example.offlinearch.data.data_source.local.RemoteKeys
import com.example.offlinearch.data.data_source.remote.NewsApi
import com.example.offlinearch.data.mapper.toEntity
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * [NewsRemoteMediator] implements Paging 3's [RemoteMediator] for an Offline-First architecture.
 *
 * How it works:
 * 1. **Offline-First Flow**: The UI/ViewModel only queries Room via `PagingSource`. It never talks directly to the network.
 * 2. **Synchronization**: When Room needs more data (or triggers a refresh), Paging 3 invokes [load].
 * 3. **Cursor Pagination**: The API returns an absolute pagination URL (`next_url`), which is stored in [RemoteKeys] to fetch subsequent pages (`APPEND`).
 */
@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val api: NewsApi,
    private val db: AppDatabase,
) : RemoteMediator<Int, NewsEntity>() {

    private companion object {
        const val LABEL = "news"
        // Cache considered fresh for 15 minutes. Avoids redundant network calls on rapid screen rotation/re-navigation.
        const val STALE_AFTER_MS = 15 * 60 * 1000L
    }

    /**
     * Determines whether an initial refresh should be launched when Paging starts.
     * - If cache is recent (< 15 mins), it skips initial refresh (`SKIP_INITIAL_REFRESH`) and displays cached items instantly.
     * - If cache is stale or empty, it triggers a refresh (`LAUNCH_INITIAL_REFRESH`).
     *
     *  * يحدد ما إذا كان يجب بدء عملية تحديث أولية (Initial Refresh) عند بدء تشغيل Paging.
     *      * - إذا كان التخزين المؤقت (Cache) حديثاً (أقل من 15 دقيقة)، يتم تخطي التحديث الأول (`SKIP_INITIAL_REFRESH`) وعرض العناصر المخزنة فوراً.
     *      * - إذا كان التخزين المؤقت قديماً أو فارغاً، يتم تشغيل التحديث (`LAUNCH_INITIAL_REFRESH`).
     */
    override suspend fun initialize(): InitializeAction {
        val last = db.remoteKeysDao().get(LABEL)?.lastRefreshed ?: 0L
        return if (System.currentTimeMillis() - last < STALE_AFTER_MS)
            InitializeAction.SKIP_INITIAL_REFRESH
        else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    /**
     * Called by Paging 3 whenever data needs to be fetched from the remote source.
     *
     * @param loadType Indicates whether we are loading the initial data ([LoadType.REFRESH]),
     *                 prepending items ([LoadType.PREPEND]), or appending next page items ([LoadType.APPEND]).
     * @param state Provides pagination configuration and current list state.
     */
    override suspend fun load(loadType: LoadType, state: PagingState<Int, NewsEntity>): MediatorResult {
        // Retrieve stored pagination cursor (next_url) from Room
        val keys = db.remoteKeysDao().get(LABEL)

        return try {
            // 1. Fetch data from remote API based on load type
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    // Fetch first page
                    api.getNews(limit = state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    // News streams only append downwards; no prepending required.
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // Fetch next page using absolute `next_url` cursor stored in remote keys.
                    val next = keys?.nextUrl
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    api.getNextPage(next)
                }
            }

            // 2. Persist fetched data and pagination state atomically in a Room database transaction.
            db.withTransaction {
                // On REFRESH, clear old cache so fresh data replaces outdated records.
                if (loadType == LoadType.REFRESH) {
                    db.newsDao().clearAll()
                    db.remoteKeysDao().clear()
                }

                // Determine starting position index to maintain correct server ordering.
                val start = db.newsDao().nextPosition()

                // Map DTOs to Room Entities and upsert into database.
                db.newsDao().upsertAll(response.results.mapIndexed { i, dto -> dto.toEntity(start + i) })

                // Evict excess rows beyond 500 items to prevent storage bloat (LRU Cache Policy).
                db.newsDao().pruneExcessNews()

                // Save the new pagination cursor (`next_url`) and refresh timestamp.
                db.remoteKeysDao().save(
                    RemoteKeys(
                        label = LABEL,
                        nextUrl = response.nextUrl,
                        lastRefreshed = if (loadType == LoadType.REFRESH) System.currentTimeMillis()
                        else keys?.lastRefreshed ?: System.currentTimeMillis(),
                    )
                )
            }

            // 3. Return success and inform Paging 3 whether pagination has reached the end.
            MediatorResult.Success(endOfPaginationReached = response.nextUrl == null)

        } catch (e: IOException) {
            // Handled when offline or connection times out. UI falls back to displaying cached Room data.
            MediatorResult.Error(e)
        } catch (e: ResponseException) {
            // Handled on HTTP errors (e.g. 401 unauthorized, 429 rate limit, 5xx server errors).
            MediatorResult.Error(e)
        } catch (e: SerializationException) {
            // Handled if JSON parsing fails due to unexpected API schema changes.
            MediatorResult.Error(e)
        }
    }
}
