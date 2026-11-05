# 📱 Offline-First Android Architecture (OfflineNews)

A production-grade, robust Android application built with modern Jetpack libraries and an **Offline-First Architecture**. The app demonstrates advanced data synchronization, cursor-based pagination, resilient networking with Ktor, local caching with Room, and reactive UI updates using Jetpack Compose and MVI principles.

---

## 🏗️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3 with edge-to-edge support.
- **Architecture Pattern**: Clean Architecture (Data, Domain, Presentation layers) + MVI (Model-View-Intent).
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) (Dagger).
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room) with TypeConverters and Transaction management.
- **Pagination**: [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) (`RemoteMediator` + `PagingSource`).
- **Networking**: [Ktor Client](https://ktor.io/) (OkHttp engine) with Content Negotiation, Logging, Timeout, and Retry plugins.
- **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).
- **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/compose/).
- **Concurrency**: Kotlin Coroutines & Flows.

---

## 🔄 Offline-First Data Flow

```text
 ┌───────────┐      Ktor Client      ┌───────────────┐
 │ Remote API│ ────────────────────► │NewsRemoteMediator│
 └───────────┘                       └───────┬───────┘
                                             │  Upsert & Prune (Max 500)
                                             ▼
                                     ┌───────────────┐
                                     │ Room Database │
                                     └───────┬───────┘
                                             │  PagingSource
                                             ▼
                                     ┌───────────────┐
                                     │   ViewModel   │
                                     └───────┬───────┘
                                             │  Flow<PagingData>
                                             ▼
                                     ┌───────────────┐
                                     │ Jetpack UI    │
                                     └───────────────┘
```
> **Key Principle**: The UI **never** reads directly from the network. It always reads from Room. `RemoteMediator` synchronizes remote data in the background and caches it locally.

---

## ✨ Key Features

1. **Robust Cursor Pagination (`RemoteMediator`)**:
   - Handles `REFRESH`, `APPEND`, and `PREPEND` load types.
   - Persists absolute pagination cursors (`next_url`) in `RemoteKeys`.
   - Preserves old cache if offline or if network requests fail.

2. **Storage Bloat Protection (LRU / TTL Cache Eviction)**:
   - Automatically prunes database records beyond the 500 most recent items during background syncs to prevent unbounded local storage growth.

3. **Secure API Key Management**:
   - Injects `NEWS_API_KEY` securely from `local.properties` via Gradle `buildConfigField`.
   - Uses a custom Ktor client plugin (`apiKeyPlugin`) to automatically attach the API key to every request, including absolute `next_url` pagination links.

4. **Offline Resilience & Error Handling**:
   - Gracefully handles `IOException` (offline/timeouts), `ResponseException` (4xx/5xx errors), and `SerializationException` (unexpected JSON schemas).
   - Provides pull-to-refresh, retry buttons, and cached fallback banners.

---

## 📂 Project Structure

```text
com.example.offlinearch/
│
├── data/
│   ├── data_source/
│   │   ├── local/          # Room AppDatabase, NewsDao, NewsEntity, RemoteKeys, Converters
│   │   └── remote/         # Ktor NewsApi, HttpClientFactory
│   ├── dto/                # Kotlinx Serialization DTOs
│   ├── mapper/             # Entity <-> Domain mappers
│   └── repository/         # NewsRepositoryImpl & NewsRemoteMediator
│
├── domain/
│   ├── model/              # Clean Domain Models (NewsArticle, Sentiment)
│   ├── repository/         # NewsRepository interface
│   └── usecase/            # GetNewsUseCase
│
├── di/                     # Hilt Modules (DatabaseModule, NetworkModule, RepositoryModule)
│
└── presentation/
    ├── screens/            # NewsScreen, NewsViewModel, MVI contracts
    ├── components/         # Reusable UI components (NewsRow)
    ├── theme/              # Material 3 Theme and Color schemes
    └── util/               # Sample data providers
```

---

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/offline-news-android.git
   ```

2. **Configure your API Key**:
   Create or open `local.properties` in the root directory and add your API key:
   ```properties
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   NEWS_API_KEY=your_actual_news_api_key_here
   ```

3. **Build and Run**:
   Sync project with Gradle files and run the app on an emulator or physical device.
