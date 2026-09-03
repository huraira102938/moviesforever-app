package com.moviesforever.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.moviesforever.app.ui.navigation.MoviesForeverNavHost
import com.moviesforever.app.ui.theme.MoviesForeverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoviesForeverTheme {
                MoviesForeverApp()
            }
        }
    }
}

@Composable
fun MoviesForeverApp(
    viewModel: com.moviesforever.app.ui.viewmodel.AppViewModel = hiltViewModel()
) {
    MoviesForeverNavHost(viewModel = viewModel)
}
