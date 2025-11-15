package com.kamath.taleweaver.home.search.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationPermissionHandler @Inject constructor() : LocationFacade {

    private val _hasLocationPermission = MutableStateFlow(false)
    override val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    override fun checkPermissionStatus(context: Context) {
        _hasLocationPermission.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(permissionLauncher: ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}