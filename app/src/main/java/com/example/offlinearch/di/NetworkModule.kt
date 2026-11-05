package com.example.offlinearch.di

import android.util.Log
import com.example.offlinearch.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Singleton

/**
 * Hilt module that configures and provides network-related dependencies
 * across the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured [Json] parser instance for KotlinX Serialization.
     * Configured with unknown key ignoring, input coercion, and lenient parsing.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true      // Ignore extra API fields not mapped in DTOs
        coerceInputValues = true       // Fallback to defaults when API returns null for non-nullable primitive fields
        isLenient = true               // Allow lenient JSON formatting
    }

    /**
     * Custom Ktor plugin that appends the `apiKey` query parameter to every outgoing HTTP request.
     * This ensures both base URL endpoints and absolute pagination URLs (`next_url`) carry the API key.
     *
     * @param apiKey The API key string to attach to requests.
     */
    private fun apiKeyPlugin(apiKey: String) = createClientPlugin("ApiKey") {
        onRequest { request, _ -> request.url.parameters["apiKey"] = apiKey }
    }

    /**
     * Factory function to instantiate and configure a Ktor [HttpClient].
     *
     * @param baseUrl Base URL for API requests.
     * @param apiKey API key attached to requests.
     * @param debug Flag enabling logging with sensitive API key masking when true.
     * @param json Configured [Json] instance for response deserialization.
     * @param engine Optional custom [HttpClientEngine] (useful for testing with MockEngine).
     * @return Fully configured [HttpClient] instance.
     */
    fun createHttpClient(
        baseUrl: String,
        apiKey: String,
        debug: Boolean,
        json: Json = provideJson(),
        engine: HttpClientEngine? = null,
    ): HttpClient {
        val config: HttpClientConfig<*>.() -> Unit = {
            expectSuccess = true                       // Throws ResponseException on non-2xx HTTP status codes

            // Content negotiation plugin using KotlinX Serialization
            install(ContentNegotiation) { json(json) }

            // Configures connect and request timeouts
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
            }

            // Automatic retry policy for network failures and 5xx server errors
            install(HttpRequestRetry) {
                // Retry network failures and 5xx server errors, but skip 4xx client errors
                retryOnExceptionIf(maxRetries = 2) { _, cause ->
                    cause is IOException || cause is ServerResponseException
                }
                exponentialDelay()
            }

            // Sets default base URL and installs custom API Key plugin
            defaultRequest {
                url(baseUrl)
            }
            install(apiKeyPlugin(apiKey))

            // Enables request/response logging in debug builds with API key masking for security
            if (debug) {
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("Ktor", message.replace(Regex("apiKey=[^&\\s]+"), "apiKey=***"))
                        }
                    }
                }
            }
        }
        return if (engine != null) HttpClient(engine, config) else HttpClient(OkHttp, config)
    }

    /**
     * Provides a singleton [HttpClient] injected into data sources / API implementations.
     *
     * @param json The [Json] serializer provided by [provideJson].
     * @return The singleton Ktor [HttpClient].
     */
    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient {
        return createHttpClient(
            baseUrl = BuildConfig.BASE_URL,
            apiKey = BuildConfig.NEWS_API_KEY,
            debug = BuildConfig.DEBUG,
            json = json
        )
    }
}

/*
 =========================================================================================
 EXAMPLES: How to add an Authorization/Bearer Token using Ktor Plugins
 =========================================================================================

 --- EXAMPLE 1: Custom Ktor Client Plugin (Lightweight Token Header) ---

 private fun bearerAuthPlugin(tokenProvider: suspend () -> String?) = createClientPlugin("BearerAuth") {
     onRequest { request, _ ->
         val token = tokenProvider()
         if (!token.isNullOrBlank()) {
             request.header("Authorization", "Bearer $token")
         }
     }
 }

 // Usage in HttpClient configuration:
 // install(bearerAuthPlugin { tokenRepository.getAccessToken() })


 --- EXAMPLE 2: Ktor's Built-In Auth Plugin (With Automatic Token Refresh on 401) ---
 // Requires library: io.ktor:ktor-client-auth

 install(Auth) {
     bearer {
         loadTokens {
             val accessToken = tokenRepository.getAccessToken()
             val refreshToken = tokenRepository.getRefreshToken()
             if (!accessToken.isNullOrEmpty()) {
                 BearerTokens(accessToken, refreshToken ?: "")
             } else {
                 null
             }
         }
         refreshTokens {
             val newTokens = tokenRepository.refreshTokens()
             newTokens?.let {
                 BearerTokens(it.accessToken, it.refreshToken)
             }
         }
     }
 }
 =========================================================================================
*/
