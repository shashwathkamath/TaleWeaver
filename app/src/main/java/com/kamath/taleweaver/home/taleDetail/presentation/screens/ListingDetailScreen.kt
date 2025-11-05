package com.kamath.taleweaver.home.taleDetail.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.home.taleDetail.presentation.TaleDetailViewModel

@Composable
fun ListingDetailScreen(
    //viewModel: TaleDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    //val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Detail Screen for Listing ID:\n")
    }
}