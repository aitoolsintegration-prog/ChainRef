package com.safety.chainreference

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.safety.chainreference.ui.screens.MainScreen
import com.safety.chainreference.ui.theme.ChainReferenceTheme
import com.safety.chainreference.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Provide Material3 theme
            ChainReferenceTheme {

                // Surface for background color
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    // Provide the ViewModel
                    val mainViewModel: MainViewModel = viewModel()

                    // Main app screen
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}
