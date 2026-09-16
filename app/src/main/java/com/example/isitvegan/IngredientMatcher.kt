package com.example.isitvegan

internal data class IngredientMatch(
    val token: IngredientToken,
    val ingredients: List<Ingredient>,
    val residualNormalized: String
)

/** Matches the longest aliases first so a precise phrase masks shorter overlapping aliases. */
internal class IngredientMatcher(database: List<Ingredient>) {
    private data class AliasEntry(
        val ingredient: Ingredient,
        val value: String,
        val pattern: Regex
    )

    private data class Candidate(
        val ingredient: Ingredient,
        val start: Int,
        val endExclusive: Int,
        val aliasLength: Int
    )

    private val aliases: List<AliasEntry> = database.flatMap { ingredient ->
        (ingredient.aliases + listOfNotNull(ingredient.eNumber))
            .map { TextNormalizer.normalize(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .map { normalizedAlias ->
                AliasEntry(
                    ingredient = ingredient,
                    value = normalizedAlias,
                    pattern = Regex(
                        "(?<![a-z0-9])${Regex.escape(normalizedAlias)}(?![a-z0-9])"
                    )
                )
            }
    }.sortedByDescending { it.value.length }

    fun match(token: IngredientToken): IngredientMatch {
        val normalized = TextNormalizer.normalize(token.text)
        val candidates = buildList {
            aliases.forEach { alias ->
                alias.pattern.findAll(normalized).forEach { occurrence ->
                    add(Candidate(
                        ingredient = alias.ingredient,
                        start = occurrence.range.first,
                        endExclusive = occurrence.range.last + 1,
                        aliasLength = alias.value.length
                    ))
                }
            }
        }.sortedWith(
            compareByDescending<Candidate> { it.aliasLength }
                .thenBy { it.start }
        )

        val covered = BooleanArray(normalized.length)
        val selected = mutableListOf<Candidate>()
        candidates.forEach { candidate ->
            if ((candidate.start until candidate.endExclusive).none { covered[it] }) {
                (candidate.start until candidate.endExclusive).forEach { covered[it] = true }
                selected += candidate
            }
        }

        val ordered = selected.sortedBy { it.start }
        val ingredients = linkedMapOf<String, Ingredient>()
        ordered.forEach { ingredients[it.ingredient.id] = it.ingredient }
        val residual = normalized.mapIndexed { index, character ->
            if (covered[index]) ' ' else character
        }.joinToString("")
            .replace(Regex("\\s+"), " ")
            .trim()

        return IngredientMatch(token, ingredients.values.toList(), residual)
    }
}
