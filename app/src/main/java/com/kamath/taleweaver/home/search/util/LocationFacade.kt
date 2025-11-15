package com.kamath.taleweaver.home.search.util

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.StateFlow

interface LocationFacade {
    val hasLocationPermission: StateFlow<Boolean>//state of permission
    fun checkPermissionStatus(context: Context)//permission status
    fun requestPermission(permissionLauncher: ActivityResultLauncher<String>)
}