package com.anujsinghdev.anujtodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Import this
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.anujsinghdev.anujtodo.ui.Navigation
import com.anujsinghdev.anujtodo.ui.theme.AnujToDoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject the MainViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnujToDoTheme {
                // Check if we are still fetching the user state
                if (viewModel.isLoading.value) {
                    // Show a simple loading spinner while checking
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Loading finished, set up navigation with the correct start screen
                    val navController = rememberNavController()
                    val startScreen = viewModel.startDestination.value

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            Navigation(
                                navController = navController,
                                startDestination = startScreen
                            )
                        }
                    }
                }
            }
        }
    }
}