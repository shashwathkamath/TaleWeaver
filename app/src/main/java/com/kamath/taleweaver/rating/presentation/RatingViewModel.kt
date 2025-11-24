package com.kamath.taleweaver.rating.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.rating.domain.model.Rating
import com.kamath.taleweaver.rating.domain.usecase.SubmitRatingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val submitRatingUseCase: SubmitRatingUseCase
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<RatingEvent>()
    val eventFlow: SharedFlow<RatingEvent> = _eventFlow

    fun submitRating(sellerId: String, rating: Float, comment: String, transactionId: String = "") {
        viewModelScope.launch {
            val ratingModel = Rating(
                sellerId = sellerId,
                rating = rating,
                comment = comment,
                transactionId = transactionId
            )

            submitRatingUseCase(ratingModel)
                .onSuccess {
                    _eventFlow.emit(RatingEvent.RatingSubmitted)
                }
                .onFailure { error ->
                    _eventFlow.emit(RatingEvent.Error(error.message ?: "Failed to submit rating"))
                }
        }
    }
}

sealed class RatingEvent {
    object RatingSubmitted : RatingEvent()
    data class Error(val message: String) : RatingEvent()
}
