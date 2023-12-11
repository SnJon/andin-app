package ru.netology.nmedia.service.di

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.google.android.gms.tasks.Task

interface GoogleApiAvailabilityProvider {
    fun isGooglePlayServicesAvailable(context: Context): Int
    fun isUserResolvableError(errorCode: Int): Boolean
    fun getErrorDialog(activity: Activity, errorCode: Int, requestCode: Int): Dialog?
}

interface FirebaseMessagingProvider {
    fun getToken(): Task<String>
}
