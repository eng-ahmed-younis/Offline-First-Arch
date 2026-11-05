package com.example.offlinearch.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val results: List<NewsDto> = emptyList(),
    @SerialName("next_url") val nextUrl: String? = null,
)


@Serializable
data class NewsDto(
    val id: String,
    val title: String,
    val author: String? = null,
    @SerialName("published_utc") val publishedUtc: String,
    @SerialName("article_url") val articleUrl: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val description: String? = null,
    val tickers: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val publisher: PublisherDto,
    val insights: List<InsightDto> = emptyList(),
)


@Serializable
data class PublisherDto(
    val name: String,
    @SerialName("logo_url")
    val logoUrl: String? = null,
)

@Serializable
data class InsightDto(
    val ticker: String,
    val sentiment: String? = null,
)