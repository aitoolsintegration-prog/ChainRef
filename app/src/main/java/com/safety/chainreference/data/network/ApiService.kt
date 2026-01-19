package com.safety.chainreference.data.network

import com.safety.chainreference.data.model.GeminiRequest
import com.safety.chainreference.data.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("askGemini")
    suspend fun askGemini(@Body request: GeminiRequest): GeminiResponse

    companion object {
        // Factory method
        val retrofitService: ApiService
            get() = RetrofitClient.apiService
    }
}
