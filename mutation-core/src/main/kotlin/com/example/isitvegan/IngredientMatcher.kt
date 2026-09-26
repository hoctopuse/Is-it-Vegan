package com.example.isitvegan

enum class MatchResolution { NONE, EXACT, COVERED, PARTIAL_CONTEXTUAL, BLOCKED_CONFLICT }

data class IngredientMatch(
    val token: IngredientToken,
    val ingredients: List<Ingredient>,
    val residualNormalized: String,
    val resolution: MatchResolution,
    val blockedIngredientIds: List<String> = emptyList()
)

/** Matches the longest aliases first so a precise phrase masks shorter overlapping aliases. */
class IngredientMatcher(private val database: List<Ingredient>) {
    private data class AliasEntry(
        val ingredient: Ingredient,
        val value: String,
        val pattern: Regex,
        val hasLongerLinkedAlias: Boolean
    )

    private data class Candidate(
        val ingredient: Ingredient,
        val start: Int,
        val endExclusive: Int,
        val aliasLength: Int
    )

    private val normalizedAliases = database.flatMap { ingredient ->
        listOf(ingredient.name) + ingredient.aliases + listOfNotNull(ingredient.eNumber)
    }.map(TextNormalizer::normalize).filter(String::isNotBlank).distinct()

    private val aliases: List<AliasEntry> = database.flatMap { ingredient ->
        (listOf(ingredient.name) + ingredient.aliases + listOfNotNull(ingredient.eNumber))
            .map { TextNormalizer.normalize(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .map { normalizedAlias ->
                AliasEntry(
                    ingredient = ingredient,
                    value = normalizedAlias,
                    pattern = Regex(
                        "(?<![a-z0-9])${Regex.escape(normalizedAlias)}(?![a-z0-9])"
                    ),
                    hasLongerLinkedAlias = normalizedAliases.any {
                        it.length > normalizedAlias.length &&
                            it.startsWith("$normalizedAlias ") &&
                            linkedSuffix.matches(it.removePrefix("$normalizedAlias "))
                    }
                )
            }
    }.sortedByDescending { it.value.length }

    fun match(token: IngredientToken): IngredientMatch {
        val normalized = TextNormalizer.normalize(token.text)
        coveredGlucoseFructose(token, normalized)?.let { return it }
        val candidates = buildList {
            aliases.forEach { alias ->
                alias.pattern.findAll(normalized).forEach { occurrence ->
                    val suffix = normalized.substring(occurrence.range.last + 1).trimStart()
                    if (alias.hasLongerLinkedAlias && linkedSuffix.matches(suffix)) return@forEach
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

        val blocked = candidates.filter { animal ->
            animal.ingredient.id in protectedAnimalIds && candidates.any { source ->
                source.ingredient.status == VeganStatus.VEGAN && protectedPair(normalized, animal, source)
            }
        }
        val eligible = candidates.filterNot { it in blocked }
        val covered = BooleanArray(normalized.length)
        val selected = mutableListOf<Candidate>()
        eligible.forEach { candidate ->
            if ((candidate.start until candidate.endExclusive).none { covered[it] }) {
                (candidate.start until candidate.endExclusive).forEach { covered[it] = true }
                selected += candidate
            }
        }

        val ordered = selected.sortedBy { it.start }
        val ingredients = linkedMapOf<String, Ingredient>()
        ordered.forEach { ingredients[it.ingredient.id] = it.ingredient }
        var residual = normalized.mapIndexed { index, character ->
            if (covered[index]) ' ' else character
        }.joinToString("")
            .replace(Regex("\\s+"), " ")
            .trim()

        val protectedExpressionCovered = blocked.isNotEmpty() && blocked.all { animal ->
            ordered.any { source ->
                source.ingredient.status == VeganStatus.VEGAN &&
                    protectedPair(normalized, animal, source) &&
                    hasNoSemanticRemainder(normalized, animal, source)
            }
        }
        if (protectedExpressionCovered) residual = ""
        val resolution = when {
            ingredients.isEmpty() && blocked.isEmpty() -> MatchResolution.NONE
            protectedExpressionCovered -> MatchResolution.COVERED
            residual.isBlank() -> MatchResolution.EXACT
            isCovered(normalized, residual, ordered) -> MatchResolution.COVERED
            blocked.isNotEmpty() -> MatchResolution.BLOCKED_CONFLICT
            else -> MatchResolution.PARTIAL_CONTEXTUAL
        }
        return IngredientMatch(
            token,
            ingredients.values.toList(),
            residual,
            resolution,
            blocked.map { it.ingredient.id }.distinct()
        )
    }

    private fun protectedPair(normalized: String, animal: Candidate, source: Candidate): Boolean {
        if (source.endExclusive <= animal.start) {
            return normalized.substring(source.endExclusive, animal.start).isBlank()
        }
        if (source.start >= animal.endExclusive) {
            return normalized.substring(animal.endExclusive, source.start).trim() in sourceConnectors
        }
        return false
    }

    private fun hasNoSemanticRemainder(
        normalized: String,
        animal: Candidate,
        source: Candidate
    ): Boolean {
        val explained = BooleanArray(normalized.length)
        (animal.start until animal.endExclusive).forEach { explained[it] = true }
        (source.start until source.endExclusive).forEach { explained[it] = true }
        val connectorStart = minOf(animal.endExclusive, source.endExclusive)
        val connectorEnd = maxOf(animal.start, source.start)
        (connectorStart until connectorEnd).forEach { explained[it] = true }
        return normalized.filterIndexed { index, _ -> !explained[index] }
            .all { it.isWhitespace() || it in punctuation }
    }

    private fun coveredGlucoseFructose(
        token: IngredientToken,
        normalized: String
    ): IngredientMatch? {
        if (normalized !in glucoseFructoseForms) return null
        val ingredient = database.firstOrNull { it.id == "glucose_syrup" } ?: return null
        return IngredientMatch(token, listOf(ingredient), "", MatchResolution.COVERED)
    }

    private fun isCovered(
        normalized: String,
        residual: String,
        selected: List<Candidate>
    ): Boolean {
        if (selected.isEmpty()) return false
        var remaining = residual
        reviewedPhrases.forEach { phrase ->
            remaining = remaining.replace(
                Regex("(?<![a-z0-9])${Regex.escape(phrase)}(?![a-z0-9])"),
                " "
            )
        }
        remaining = remaining.replace(
            Regex("\\b(?:origine|origin|provenance)\\s+[a-z]+\\b"),
            " "
        )
        if (remaining.split(Regex("\\s+")).filter(String::isNotBlank).all { it in glueWords }) {
            return true
        }
        if (selected.any { it.ingredient.id == "natural_flavouring" } &&
            residual.matches(Regex("^(?:de|d) [a-z]+$"))
        ) return true

        if (selected.size == 1) {
            val source = selected.single()
            val prefix = normalized.substring(0, source.start).trim()
            val suffix = normalized.substring(source.endExclusive).trim()
            if (derivedSourcePrefix.matches(prefix) &&
                (suffix.isBlank() || derivedSourceSuffix.matches(suffix))
            ) return true
        }
        return false
    }

    private companion object {
        val linkedSuffix = Regex("^(?:de|d|du|des|a|au)(?:\\s|$).*")
        val protectedAnimalIds = setOf("butter", "milk", "cream")
        val sourceConnectors = setOf("de", "d", "van", "of")
        val punctuation = setOf('(', ')', '[', ']', ',', ';', ':', '.', '-')
        val glueWords = setOf("de", "d", "du", "des", "a", "au", "aux", "et", "en")
        val reviewedPhrases = listOf(
            "preparation a base de", "partiellement degraisse", "partiellement degraissee",
            "non hydrogene", "non hydrogenee", "proteine de", "proteines de", "poudre de",
            "extrait de", "flocons de", "flocon de", "base de", "feves de", "feve de",
            "ecreme", "ecremee", "entier", "entiere", "demi ecreme", "demi ecremee",
            "en poudre", "decortiquees", "decortiquee", "rehydrate", "rehydratees",
            "rehydrates", "rehydratee", "concentre", "concentree", "concentres",
            "concentrees", "depellicule", "depelliculee", "depellicules", "depelliculees",
            "fume", "fumee", "fumes", "fumees", "au bois de hetre",
            "rouge", "seche au soleil", "sechee au soleil"
        ).sortedByDescending(String::length)
        val derivedSourcePrefix = Regex("^(?:puree|pate|graines?|huile|graisse) (?:de|d)$")
        val derivedSourceSuffix = Regex("^(?:moulu|moulue|moulus|moulues)$")
        val glucoseFructoseForms = setOf(
            "sirop de glucose fructose",
            "glucose fructose syrup",
            "glucose fructosestroop"
        )
    }
}
