package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.Pokemon
import com.example.myapplication.model.PokemonSpecies
import com.example.myapplication.repository.PokemonRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonViewModel(private val repository: PokemonRepository = PokemonRepository()) : ViewModel() {
    private val _searchState = MutableStateFlow<ApiResponse<List<PokemonSpecies>>>(ApiResponse())
    val searchState: StateFlow<ApiResponse<List<PokemonSpecies>>> = _searchState.asStateFlow()

    private val _pokemonDetailState = MutableStateFlow<ApiResponse<Pokemon>>(ApiResponse())
    val pokemonDetailState: StateFlow<ApiResponse<Pokemon>> = _pokemonDetailState.asStateFlow()

    private var searchJob: Job? = null
    private var currentPage = 0
    private var hasMore = true

    fun updateSearchState(response: ApiResponse<List<PokemonSpecies>>) {
        _searchState.update { response }
    }

    fun searchPokemon(name: String, isNewSearch: Boolean = true) {
        searchJob?.cancel()

        if (isNewSearch) {
            currentPage = 0
            hasMore = true
            _searchState.update { ApiResponse(loading = true) }
        }

        searchJob = viewModelScope.launch {
            // 添加防抖延迟
            if (isNewSearch) {
                delay(300)
            }

            val offset = currentPage * PAGE_SIZE

            repository.searchPokemonSpecies(name, PAGE_SIZE, offset).fold(
                onSuccess = { speciesList ->
                    hasMore = speciesList.size == PAGE_SIZE
                    if (isNewSearch) {
                        _searchState.update {
                            ApiResponse(data = speciesList)
                        }
                    } else {
                        val currentList = _searchState.value.data ?: emptyList()
                        _searchState.update {
                            ApiResponse(data = currentList + speciesList)
                        }
                    }
                    currentPage++
                },
                onFailure = { error ->
                    _searchState.update {
                        ApiResponse(error = error.message ?: "Unknown error")
                    }
                }
            )
        }
    }

    fun loadMore(name: String) {
        if (!hasMore || _searchState.value.loading) return

        _searchState.update { it.copy(loading = true) }
        searchPokemon(name, isNewSearch = false)
    }

    fun getPokemonDetails(id: Int) {
        viewModelScope.launch {
            _pokemonDetailState.update { ApiResponse(loading = true) }

            repository.getPokemonDetails(id).fold(
                onSuccess = { pokemon ->
                    _pokemonDetailState.update { ApiResponse(data = pokemon) }
                },
                onFailure = { error ->
                    _pokemonDetailState.update {
                        ApiResponse(error = error.message ?: "Unknown error")
                    }
                }
            )
        }
    }

    fun clearSearch() {
        _searchState.update { ApiResponse() }
        currentPage = 0
        hasMore = true
    }

    fun clearDetail() {
        _pokemonDetailState.update { ApiResponse() }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}