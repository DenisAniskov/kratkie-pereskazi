package com.plantscanner.data.api

import com.plantscanner.data.model.LMStudioRequest
import com.plantscanner.data.model.LMStudioResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LMStudioApiService {
    @POST("v1/chat/completions")
    suspend fun analyzePlant(@Body request: LMStudioRequest): LMStudioResponse
}
