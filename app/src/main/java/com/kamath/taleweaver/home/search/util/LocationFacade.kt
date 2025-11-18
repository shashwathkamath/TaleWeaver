package com.kamath.taleweaver.home.search.util

import android.content.Context
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.StateFlow

interface LocationFacade {
    val hasLocationPermission: StateFlow<Boolean>//state of permission
    fun checkPermissionStatus(context: Context)//permission status

    //get last known location
    fun getLastKnownLocation(
        context: Context,
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun requestPermission(permissionLauncher: ActivityResultLauncher<String>)
}