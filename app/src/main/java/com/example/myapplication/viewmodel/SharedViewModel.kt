package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _lastSearchQuery = MutableStateFlow("")
    val lastSearchQuery: StateFlow<String> = _lastSearchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.example.myapplication.model.PokemonSpecies>>(emptyList())
    val searchResults: StateFlow<List<com.example.myapplication.model.PokemonSpecies>> = _searchResults.asStateFlow()

    fun setLastSearch(query: String, results: List<com.example.myapplication.model.PokemonSpecies>) {
        _lastSearchQuery.value = query
        _searchResults.value = results
    }
}