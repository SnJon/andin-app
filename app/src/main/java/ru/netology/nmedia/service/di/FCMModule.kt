package ru.netology.nmedia.service.di

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FCMModule {
    @Singleton
    @Provides
    fun provideFirebaseMessagingProvider(): FirebaseMessagingProvider {
        return object : FirebaseMessagingProvider {
            override fun getToken(): Task<String> {
                return FirebaseMessaging.getInstance().token
            }
        }
    }
}
