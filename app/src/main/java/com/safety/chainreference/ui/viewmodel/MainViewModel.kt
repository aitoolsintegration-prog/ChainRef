package com.safety.chainreference.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safety.chainreference.data.model.GeminiRequest
import com.safety.chainreference.data.model.GeminiResponse
import com.safety.chainreference.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainViewModel : ViewModel() {

    private val _response = MutableStateFlow<GeminiResponse?>(null)
    val response: StateFlow<GeminiResponse?> = _response

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ✅ Retrofit service instance
    private val apiService: ApiService = ApiService.retrofitService

    /**
     * Ask a question to the backend
     */
    fun askQuestion(question: String, selectedTheme: String) {
        _isLoading.value = true
        _error.value = null
        _response.value = null

        viewModelScope.launch {
            try {
                val request = GeminiRequest(
                    question = question,
                    selectedTheme = selectedTheme
                )

                val result = apiService.askGemini(request)
                _response.value = result
            } catch (e: IOException) {
                _error.value = "Network error: ${e.localizedMessage}"
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.code()} ${e.message()}"
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
