package com.example.isitvegan

enum class VeganStatus {
    VEGAN,
    VEGETARIAN,
    NON_VEGAN,
    UNCERTAIN
}

/** Stable editorial origin categories rendered by the Android resources. */
enum class PossibleOrigin {
    PLANT,
    ANIMAL,
    EGG,
    SYNTHETIC,
    MICROBIAL,
    MARINE
}

enum class OriginVariability {
    RAW_MATERIAL_AND_PROCESS,
    PRODUCTION_METHOD,
    MANUFACTURER
}

/** Informational evidence for an uncertain concept; it never participates in verdict calculation. */
data class PossibleOriginNote(
    val origins: List<PossibleOrigin>,
    val variability: OriginVariability,
    val sourceIds: List<String>,
    val confidence: String
)

data class Ingredient(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val eNumber: String? = null,
    val status: VeganStatus,
    val reason: String,
    val source: String? = null,
    val possibleOriginNote: PossibleOriginNote? = null
)
