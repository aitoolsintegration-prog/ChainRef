package com.safety.chainreference.data.model

data class ChainVerse(
    val order: Int,
    val reference: String,
    val text: String,
    val linkingPhrase: String,
    val nextVerse: String?,
    val crossThemeConnections: List<CrossThemeConnection> = emptyList()
)

data class CrossThemeConnection(
    val theme: String,
    val reference: String,
    val text: String
)
