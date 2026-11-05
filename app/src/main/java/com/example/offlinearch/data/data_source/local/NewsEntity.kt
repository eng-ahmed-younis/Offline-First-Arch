package com.example.offlinearch.data.data_source.local
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter



/** Local mirror of one remote article (same fields as the API). */
@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val author: String?,
    val publisherName: String,
    val publisherLogoUrl: String?,
    val publishedAt: Long,          // epoch millis
    val articleUrl: String,
    val imageUrl: String?,
    val tickers: List<String>,
    val sentiment: String?,
    val position: Int,              // keeps the server order
)


/** Stores the cursor (next_url) for pagination + last refresh time. */
@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val label: String,
    val nextUrl: String?,
    val lastRefreshed: Long,
)


class Converters {
    @TypeConverter fun fromList(value: List<String>): String = value.joinToString(",")
    @TypeConverter fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")
}