package com.example.domain.repository

import com.example.core.error.ResultWrapper
import com.example.domain.model.AnalyticsData
import com.example.domain.model.AnalyticsFilters

interface AnalyticsRepository {
    suspend fun getAnalytics(filters: AnalyticsFilters): ResultWrapper<AnalyticsData>
}
