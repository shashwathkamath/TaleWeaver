package com.kamath.taleweaver.home.search.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.home.search.presentation.components.PermissionDeniedContent
import com.kamath.taleweaver.home.search.presentation.components.PermissionGrantedContent
import com.kamath.taleweaver.home.search.util.LocationFacade
import timber.log.Timber

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onListingClick: (String) -> Unit = {}
) {
    val locationFacade: LocationFacade = viewModel.getFacade()
    val hasPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.checkPermission()
    }
    if (hasPermission) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        PermissionGrantedContent(
            state = uiState,
            onEvent = { event ->
                viewModel.onEvent(event)
            },
            onListingClick = onListingClick
        )
    } else {
        PermissionDeniedContent {
            locationFacade.requestPermission(permissionLauncher)
        }
    }
}