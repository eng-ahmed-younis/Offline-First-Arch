package com.example.offlinearch.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.offlinearch.data.data_source.local.NewsEntity
import com.example.offlinearch.data.dto.NewsDto
import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.domain.model.Sentiment
import java.time.Instant

private val UNKNOWN_AUTHORS = setOf("na", "n/a", "not specified", "unknown", "")

@RequiresApi(Build.VERSION_CODES.O)
fun NewsDto.toEntity(position: Int) = NewsEntity(
    id = id,
    title = title,
    description = description,
    author = author?.trim()?.takeUnless { it.lowercase() in UNKNOWN_AUTHORS },
    publisherName = publisher.name,
    publisherLogoUrl = publisher.logoUrl,
    publishedAt = runCatching { Instant.parse(publishedUtc).toEpochMilli() }.getOrDefault(0L),
    articleUrl = articleUrl,
    imageUrl = imageUrl,
    tickers = tickers,
    sentiment = insights.firstOrNull()?.sentiment,
    position = position,
)

fun NewsEntity.toDomain() = NewsArticle(
    id = id,
    title = title,
    description = description,
    author = author,
    publisherName = publisherName,
    publishedAt = publishedAt,
    articleUrl = articleUrl,
    imageUrl = imageUrl,
    tickers = tickers,
    sentiment = when (sentiment?.lowercase()) {
        "positive" -> Sentiment.POSITIVE
        "negative" -> Sentiment.NEGATIVE
        "neutral" -> Sentiment.NEUTRAL
        else -> null
    },
)
