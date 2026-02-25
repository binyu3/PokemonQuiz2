package com.example.myapplication.model

data class PokemonSpecies(
    val id: Int,
    val name: String,
    val captureRate: Int,
    val color: PokemonColor?,
    val pokemons: List<Pokemon>
)

data class Pokemon(
    val id: Int,
    val name: String,
    val abilities: List<PokemonAbility>
)

data class PokemonAbility(
    val name: String
)

data class PokemonColor(
    val name: String
)

data class ApiResponse<T>(
    val data: T? = null,
    val loading: Boolean = false,
    val error: String? = null
)

data class PokemonListResponse(
    val pokemon_v2_pokemonspecies: List<PokemonSpecies>
)