package com.safety.chainreference.data.model

import com.safety.chainreference.data.model.ChainVerse

data class GeminiResponse(
    val theme: String,
    val summary: String,
    val chain: List<ChainVerse> = emptyList()
)
