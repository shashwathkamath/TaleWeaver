package com.kamath.taleweaver.home.taleDetail.domain.repository

import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import kotlinx.coroutines.flow.Flow

interface TaleDetailRepository {
    fun getTaleById(taleId:String): Flow<Resource<Tale>>
}