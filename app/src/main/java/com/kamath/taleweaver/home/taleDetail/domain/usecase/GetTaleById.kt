package com.kamath.taleweaver.home.taleDetail.domain.usecase

import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import com.kamath.taleweaver.home.taleDetail.domain.repository.TaleDetailRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTaleById @Inject constructor(
    private val repository: TaleDetailRepository
) {
    operator fun invoke(taleId: String): Flow<Resource<Tale>> {
        if (taleId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Error("Tale ID cannot be empty."))
        }
        return repository.getTaleById(taleId)
    }
}