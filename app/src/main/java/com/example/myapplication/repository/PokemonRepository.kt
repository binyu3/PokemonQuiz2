package com.example.myapplication.repository

import com.apollographql.apollo3.ApolloClient
import com.example.myapplication.SearchPokemonSpeciesQuery
import com.example.myapplication.GetPokemonDetailsQuery
import com.example.myapplication.model.Pokemon
import com.example.myapplication.model.PokemonAbility
import com.example.myapplication.model.PokemonColor
import com.example.myapplication.model.PokemonSpecies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository {
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://beta.pokeapi.co/graphql/v1beta")
        .build()

    suspend fun searchPokemonSpecies(
        name: String,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<PokemonSpecies>> = withContext(Dispatchers.IO) {
        try {
            val query = SearchPokemonSpeciesQuery(
                name = "%$name%",
                limit = limit,
                offset = offset
            )
            val response = apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message))
            } else {
                // 将 Apollo 生成的 Data 转换为自定义的 PokemonSpecies 列表
                val speciesList = response.data?.pokemon_v2_pokemonspecies?.map { species ->
                    PokemonSpecies(
                        id = species.id,
                        name = species.name,
                        captureRate = species.capture_rate,
                        color = species.pokemon_v2_pokemoncolor?.let {
                            PokemonColor(it.name)
                        },
                        pokemons = species.pokemon_v2_pokemons.map { pokemon ->
                            Pokemon(
                                id = pokemon.id,
                                name = pokemon.name,
                                abilities = pokemon.pokemon_v2_pokemonabilities.map { ability ->
                                    PokemonAbility(
                                        ability.pokemon_v2_ability.name
                                    )
                                }
                            )
                        }
                    )
                } ?: emptyList()

                Result.success(speciesList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPokemonDetails(id: Int): Result<Pokemon> = withContext(Dispatchers.IO) {
        try {
            val query = GetPokemonDetailsQuery(id = id)
            val response = apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message))
            } else {
                val pokemonData = response.data?.pokemon_v2_pokemon_by_pk
                    ?: return@withContext Result.failure(Exception("Pokemon not found"))

                // 将 Apollo 生成的 Data 转换为自定义的 Pokemon 对象
                val pokemon = Pokemon(
                    id = pokemonData.id,
                    name = pokemonData.name,
                    abilities = pokemonData.pokemon_v2_pokemonabilities.map { ability ->
                        PokemonAbility(
                            ability.pokemon_v2_ability.name
                        )
                    }
                )

                Result.success(pokemon)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}