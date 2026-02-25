package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    viewModel: PokemonViewModel,
    onBack: () -> Unit
) {
    val detailState by viewModel.pokemonDetailState.collectAsState()

    // 加载宝可梦详情
    LaunchedEffect(pokemonId) {
        if (pokemonId != -1) {
            viewModel.getPokemonDetails(pokemonId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pokémon Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            detailState.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            detailState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${detailState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.getPokemonDetails(pokemonId) }) {
                        Text("Retry")
                    }
                }
            }

            detailState.data != null -> {
                val pokemon = detailState.data!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // 宝可梦名称
                        Text(
                            text = pokemon.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFDC0A2D) // 宝可梦红色
                        )
                    }

                    item {
                        // 能力列表
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(horizontal = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Abilities",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pokemon.abilities.forEach { ability ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                            )
                                        ) {
                                            Text(
                                                text = ability.name.replaceFirstChar { it.uppercase() },
                                                modifier = Modifier.padding(12.dp),
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }

                                if (pokemon.abilities.isEmpty()) {
                                    Text(
                                        text = "No abilities listed",
                                        color = Color.Gray,
                                        fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}