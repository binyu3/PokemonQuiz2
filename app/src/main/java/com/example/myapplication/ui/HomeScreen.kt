package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.PokemonSpecies
import com.example.myapplication.viewmodel.PokemonViewModel
import com.example.myapplication.viewmodel.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    viewModel: PokemonViewModel,
    sharedViewModel: SharedViewModel,
    onPokemonClick: (Int) -> Unit
) {

    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val lastSearchQuery by sharedViewModel.lastSearchQuery.collectAsStateWithLifecycle()
    val lastSearchResults by sharedViewModel.searchResults.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf(lastSearchQuery) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 恢复上次的搜索结果
    LaunchedEffect(Unit) {
        if (lastSearchQuery.isNotEmpty() && lastSearchResults.isNotEmpty()) {
            viewModel.updateSearchState(ApiResponse(data = lastSearchResults))
        }
    }

    // 监听滚动到底部
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() &&
                    visibleItems.last().index >= (searchState.data?.size ?: 0) - 5
                ) {
                    viewModel.loadMore(searchQuery)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                coroutineScope.launch {
                    delay(300)
                    if (searchQuery == newValue && newValue.isNotEmpty()) {
                        viewModel.searchPokemon(newValue)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Pokémon species...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    if (searchQuery.isNotEmpty()) {
                        viewModel.searchPokemon(searchQuery)
                    }
                }
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            searchState.loading && searchState.data.isNullOrEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            searchState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${searchState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                viewModel.searchPokemon(searchQuery)
                            }
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }

            searchState.data.isNullOrEmpty() -> {
                if (searchQuery.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Search for Pokémon species",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Try searching for 'Pikachu', 'Charizard', etc.",
                            color = Color.LightGray
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No results found for '$searchQuery'")
                    }
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    items(searchState.data ?: emptyList()) { species ->
                        SpeciesCard(
                            species = species,
                            onPokemonClick = onPokemonClick
                        )
                    }

                    item {
                        if (searchState.loading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // 保存搜索结果到 SharedViewModel
    LaunchedEffect(searchState.data) {
        if (searchState.data != null && searchQuery.isNotEmpty()) {
            sharedViewModel.setLastSearch(searchQuery, searchState.data!!)
        }
    }
}

@Composable
fun SpeciesCard(
    species: PokemonSpecies,
    onPokemonClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onPokemonClick(species.id)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = when (species.color?.name?.lowercase()) {
                        "red" -> Color(0xFFEF5350)
                        "blue" -> Color(0xFF42A5F5)
                        "green" -> Color(0xFF66BB6A)
                        "yellow" -> Color(0xFFFFEE58)
                        "black" -> Color(0xFF424242)
                        "white" -> Color(0xFFFAFAFA)
                        "brown" -> Color(0xFF8D6E63)
                        "purple" -> Color(0xFFAB47BC)
                        "pink" -> Color(0xFFEC407A)
                        else -> MaterialTheme.colorScheme.surfaceContainer
                    }
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = species.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (species.color?.name?.lowercase() == "black")
                        Color.White else Color.Black
                )

                Text(
                    text = "Capture Rate: ${species.captureRate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (species.color?.name?.lowercase() == "black")
                        Color.White.copy(alpha = 0.8f)
                    else Color.Black.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pokémon in this species:",
                style = MaterialTheme.typography.bodyMedium,
                color = if (species.color?.name?.lowercase() == "black")
                    Color.White.copy(alpha = 0.8f)
                else Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                species.pokemons.forEach { pokemon ->
                    Text(
                        text = "• ${pokemon.name.replaceFirstChar { it.uppercase() }}",
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (species.color?.name?.lowercase() == "black")
                                    Color.White.copy(alpha = 0.1f)
                                else Color.Black.copy(alpha = 0.05f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable {
                                onPokemonClick(pokemon.id)
                            },
                        color = if (species.color?.name?.lowercase() == "black")
                            Color.White else Color.Black
                    )
                }
            }
        }
    }
}