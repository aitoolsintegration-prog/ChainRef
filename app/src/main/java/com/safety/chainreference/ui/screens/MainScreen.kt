package com.safety.chainreference.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safety.chainreference.ui.viewmodel.MainViewModel
import com.safety.chainreference.data.model.ChainVerse
import com.safety.chainreference.data.model.GeminiResponse

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var question by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf("Sabbath") }
    var inputError by remember { mutableStateOf<String?>(null) }

    // Explicitly specify types for collectAsState with initial values
    val response: GeminiResponse? by viewModel.response.collectAsState(initial = null)
    val isLoading: Boolean by viewModel.isLoading.collectAsState(initial = false)
    val errorMessage: String? by viewModel.error.collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Link-A-Verse", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = question,
            onValueChange = {
                question = it
                inputError = null
            },
            label = { Text("Ask a Bible question") },
            isError = inputError != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (inputError != null) {
            Text(
                text = inputError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (question.isBlank()) {
                    inputError = "Please enter a question"
                } else {
                    viewModel.askQuestion(question, theme)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading spinner
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Backend error
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Empty state feedback
        if (!isLoading && response?.chain.isNullOrEmpty() && errorMessage == null) {
            Text(
                text = "No results yet. Ask a question above.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Show response chain
        response?.chain?.let { chain ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chain) { verse: ChainVerse ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(verse.reference, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(verse.text)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                verse.linkingPhrase,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
