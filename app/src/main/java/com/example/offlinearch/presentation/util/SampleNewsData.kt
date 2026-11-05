package com.example.offlinearch.presentation.util

import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.domain.model.Sentiment

/**
 * Sample dataset for Compose Previews, UI testing, and component mock data.
 */
object SampleNewsData {

    val sampleArticle1 = NewsArticle(
        id = "1",
        title = "Bitcoin and Ethereum Surge as Market Sentiment Turns Positive Across Major Exchanges",
        description = "Cryptocurrency markets experienced a strong rally today with major digital assets showing significant gains driven by institutional inflows.",
        author = "Jane Doe",
        publisherName = "CoinDesk",
        publishedAt = System.currentTimeMillis() - 3600_000L * 2,
        articleUrl = "https://example.com/news/1",
        imageUrl = "https://picsum.photos/600/300",
        tickers = listOf("BTC", "ETH", "SOL"),
        sentiment = Sentiment.POSITIVE,
    )

    val sampleArticle2 = NewsArticle(
        id = "2",
        title = "Federal Reserve Holds Interest Rates Steady Amid Inflation Concerns",
        description = "The Federal Open Market Committee announced its decision to keep benchmark interest rates unchanged at current levels.",
        author = "John Smith",
        publisherName = "Wall Street Journal",
        publishedAt = System.currentTimeMillis() - 3600_000L * 5,
        articleUrl = "https://example.com/news/2",
        imageUrl = null,
        tickers = listOf("SPY", "QQQ", "TLT"),
        sentiment = Sentiment.NEUTRAL,
    )

    val sampleArticle3 = NewsArticle(
        id = "3",
        title = "Tech Sector Leads Market Pullback Following Quarterly Earnings Reports",
        description = "Major tech equities experienced downward pressure following mixed revenue guidance for the upcoming fiscal quarter.",
        author = "Alice Johnson",
        publisherName = "Bloomberg Financial",
        publishedAt = System.currentTimeMillis() - 3600_000L * 12,
        articleUrl = "https://example.com/news/3",
        imageUrl = null,
        tickers = listOf("NVDA", "AAPL", "MSFT"),
        sentiment = Sentiment.NEGATIVE,
    )

    val sampleArticles = listOf(sampleArticle1, sampleArticle2, sampleArticle3)
}
