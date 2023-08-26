package com.tambapps.p2p.peer_transfer.android.configuration

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityComponent::class, SingletonComponent::class, ServiceComponent::class)
class FandemAndroidConfiguration {

  @Provides
  fun workManager(@ApplicationContext context: Context): WorkManager {
    return WorkManager.getInstance(context)
  }

  @Provides fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
  @Provides fun notificationManager(@ApplicationContext context: Context): NotificationManager = context.getSystemService(NotificationManager::class.java)
}