package com.example.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateGeminiContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @POST
    suspend fun generateOpenAiContent(
        @Url url: String = "https://api.openai.com/v1/chat/completions",
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}
