package com.example.offlinearch.data.data_source.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY position ASC")
    fun pagingSource(): PagingSource<Int, NewsEntity>

    @Query(
        """SELECT * FROM news
           WHERE title LIKE '%' || :query || '%' OR tickers LIKE '%' || :query || '%'
           ORDER BY position ASC"""
    )
    fun search(query: String): PagingSource<Int, NewsEntity>

    @Upsert suspend fun upsertAll(items: List<NewsEntity>)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM news")
    suspend fun nextPosition(): Int

    @Query("DELETE FROM news") suspend fun clearAll()

    /**
     * Cache Eviction / Storage Bloat Protection (LRU / TTL):
     * Keeps only the top 500 most recent items ordered by position and deletes older/excess rows.
     */
    @Query("DELETE FROM news WHERE id NOT IN (SELECT id FROM news ORDER BY position ASC LIMIT 500)")
    suspend fun pruneExcessNews()
}

@Dao
interface RemoteKeysDao {
    @Upsert suspend fun save(keys: RemoteKeys)
    @Query("SELECT * FROM remote_keys WHERE label = :label") suspend fun get(label: String): RemoteKeys?
    @Query("DELETE FROM remote_keys") suspend fun clear()
}
