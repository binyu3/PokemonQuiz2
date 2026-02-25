package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.HomeScreen
import com.example.myapplication.ui.PokemonDetailScreen
import com.example.myapplication.ui.WelcomeScreen
import com.example.myapplication.viewmodel.PokemonViewModel
import com.example.myapplication.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PokemonViewModel by viewModels()
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        setContent {
            PokemonQuizAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel, sharedViewModel)
                }
            }
        }
    }
}

@Composable
fun PokemonQuizAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFFDC0A2D),
            secondary = Color(0xFF2A75BB),
            tertiary = Color(0xFFFFCC00)
        ),
        content = content
    )
}

@Composable
fun AppNavigation(
    viewModel: PokemonViewModel,
    sharedViewModel: SharedViewModel
) {
    var showWelcome by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("home") }
    var selectedPokemonId by remember { mutableIntStateOf(-1) }

    if (showWelcome) {
        WelcomeScreen {
            showWelcome = false
            currentScreen = "home"
        }
    } else {
        when (currentScreen) {
            "home" -> {
                HomeScreen(
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel,
                    onPokemonClick = { pokemonId ->
                        selectedPokemonId = pokemonId
                        currentScreen = "detail"
                    }
                )
            }
            "detail" -> {
                PokemonDetailScreen(
                    pokemonId = selectedPokemonId,
                    viewModel = viewModel,
                    onBack = {
                        currentScreen = "home"
                    }
                )
            }
        }
    }
}