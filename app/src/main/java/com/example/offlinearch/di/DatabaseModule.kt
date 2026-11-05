package com.example.offlinearch.di

import android.content.Context
import androidx.room.Room
import com.example.offlinearch.data.data_source.local.AppDatabase
import com.example.offlinearch.data.data_source.local.NewsDao
import com.example.offlinearch.data.data_source.local.RemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "offline_news.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideNewsDao(db: AppDatabase): NewsDao = db.newsDao()
    @Provides fun provideRemoteKeysDao(db: AppDatabase): RemoteKeysDao = db.remoteKeysDao()
}
