package com.example.isitvegan

enum class VeganStatus {
    VEGAN,
    NON_VEGAN,
    UNCERTAIN
}

data class Ingredient(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val eNumber: String? = null,
    val status: VeganStatus,
    val reason: String,
    val source: String? = null
)