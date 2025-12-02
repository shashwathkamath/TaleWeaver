package com.kamath.taleweaver.home.search.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationPermissionHandler @Inject constructor() : LocationFacade {

    private val _hasLocationPermission = MutableStateFlow(false)
    override val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    private lateinit var locationClient: FusedLocationProviderClient


    override fun checkPermissionStatus(context: Context) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _hasLocationPermission.value = hasFineLocation || hasCoarseLocation
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getLastKnownLocation(
        context: Context,
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!_hasLocationPermission.value) {
            onFailure(SecurityException("Location permission has not been granted."))
            return
        }
        if (!::locationClient.isInitialized) {
            locationClient = LocationServices.getFusedLocationProviderClient(context)
        }
        locationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onFailure(Exception("Could not retrieve location. It might be disabled."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    override fun requestPermission(permissionLauncher: ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}