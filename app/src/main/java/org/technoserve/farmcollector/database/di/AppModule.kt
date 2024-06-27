package org.technoserve.farmcollector.database.di
/*
import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.technoserve.farmcollector.database.AppDatabase
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmDAO
import org.technoserve.farmcollector.database.FarmRepository
import org.technoserve.farmcollector.database.remote.SyncRepository
import org.technoserve.farmcollector.database.remote.SyncRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFarmDao(database: AppDatabase): FarmDAO {
        return database.farmsDAO()
    }

    @Provides
    @Singleton
    fun provideFarmRepository(dao: FarmDAO): FarmRepository {
        return FarmRepository(dao)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(@ApplicationContext context: Context): SyncRepository {
        return SyncRepositoryImpl(context)
    }

}

 */