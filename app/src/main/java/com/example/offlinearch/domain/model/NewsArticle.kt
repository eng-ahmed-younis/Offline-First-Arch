package com.example.offlinearch.domain.model

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String?,
    val author: String?,
    val publisherName: String,
    val publishedAt: Long,
    val articleUrl: String,
    val imageUrl: String?,
    val tickers: List<String>,
    val sentiment: Sentiment?,
)

enum class Sentiment { POSITIVE, NEUTRAL, NEGATIVE }
