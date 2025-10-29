package com.kamath.taleweaver.home.taleDetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import com.kamath.taleweaver.home.taleDetail.domain.usecase.GetTaleById
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class TaleDetailState(
    val tale: Tale? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TaleDetailViewModel @Inject constructor(
    private val getTaleById: GetTaleById,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaleDetailState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("taleId")?.let { taleId ->
            fetchTaleDetails(taleId)
        }
    }

    private fun fetchTaleDetails(taleId: String) {
        getTaleById(taleId).onEach { result ->
            _uiState.update { currentState ->
                when (result) {
                    is Resource.Loading -> {
                        currentState.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        currentState.copy(isLoading = false, tale = result.data)
                    }

                    is Resource.Error -> {
                        currentState.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}