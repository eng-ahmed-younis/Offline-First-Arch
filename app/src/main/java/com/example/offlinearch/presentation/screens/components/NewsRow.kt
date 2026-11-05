package com.example.offlinearch.presentation.screens.components

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.offlinearch.domain.model.NewsArticle
import com.example.offlinearch.domain.model.Sentiment
import com.example.offlinearch.presentation.theme.OfflineArchTheme
import com.example.offlinearch.presentation.util.SampleNewsData

@Composable
fun NewsRow(
    article: NewsArticle,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier
            .fillMaxWidth()
            .clickable { runCatching { uriHandler.openUri(article.articleUrl) } }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = buildString {
                append(article.publisherName)
                append(" · ")
                append(DateUtils.getRelativeTimeSpanString(article.publishedAt))
                article.author?.let { append(" · "); append(it) }
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        article.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            Text(text = it, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }

        article.imageUrl?.let { url ->
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.medium),
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            article.tickers.take(3).forEach { ticker ->
                SuggestionChip(onClick = {}, label = { Text(ticker) })
            }
            article.sentiment?.let { SentimentLabel(it) }
        }
    }
}

@Composable
private fun SentimentLabel(sentiment: Sentiment) {
    val (label, color) = when (sentiment) {
        Sentiment.POSITIVE -> "Positive" to Color(0xFF2E7D32)
        Sentiment.NEGATIVE -> "Negative" to Color(0xFFC62828)
        Sentiment.NEUTRAL -> "Neutral" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(label, style = MaterialTheme.typography.labelMedium, color = color)
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun NewsRowPreview() {
    OfflineArchTheme {
        Surface {
            NewsRow(article = SampleNewsData.sampleArticle1)
        }
    }
}
