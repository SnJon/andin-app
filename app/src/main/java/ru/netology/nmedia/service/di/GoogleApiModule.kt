package ru.netology.nmedia.service.di

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GoogleApiModule {
    @Singleton
    @Provides
    fun provideGoogleApiAvailabilityProvider(): GoogleApiAvailabilityProvider {
        return object : GoogleApiAvailabilityProvider {
            override fun isGooglePlayServicesAvailable(context: Context): Int {
                return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            }

            override fun isUserResolvableError(errorCode: Int): Boolean {
                return GoogleApiAvailability.getInstance().isUserResolvableError(errorCode)
            }

            override fun getErrorDialog(activity: Activity, errorCode: Int, requestCode: Int): Dialog? {
                return GoogleApiAvailability.getInstance().getErrorDialog(activity, errorCode, requestCode)
            }
        }
    }
}
