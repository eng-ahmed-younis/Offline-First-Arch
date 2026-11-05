package com.example.offlinearch.data.data_source.remote

import com.example.offlinearch.data.dto.NewsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class NewsApi @Inject constructor(
    private val client: HttpClient,
) {
    /** First page (REFRESH). Base URL comes from the client's defaultRequest. */

    suspend fun getNews(limit: Int): NewsResponse =
        client.get("v2/reference/news") {
            parameter("limit", limit)
            parameter("order", "descending")
            parameter("sort", "published_utc")
        }.body()


    /** Next page: [url] is the absolute `next_url` from the previous response (no api key in it). */
    suspend fun getNextPage(url: String): NewsResponse =
        client.get(url).body()


}