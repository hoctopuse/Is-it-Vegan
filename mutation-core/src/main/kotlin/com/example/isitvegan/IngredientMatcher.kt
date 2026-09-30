package com.example.isitvegan

enum class MatchResolution { NONE, EXACT, COVERED, PARTIAL_CONTEXTUAL, BLOCKED_CONFLICT }

data class IngredientMatch(
    val token: IngredientToken,
    val ingredients: List<Ingredient>,
    val residualNormalized: String,
    val resolution: MatchResolution,
    val blockedIngredientIds: List<String> = emptyList()
)

/** Optional, test-facing measurements. It has no logging and is inactive by default. */
data class MatcherProfileSnapshot(
    val normalizationPreparationNanos: Long,
    val aliasPreparationNanos: Long,
    val regexPreparationNanos: Long,
    val longerLinkedAliasCheckNanos: Long,
    val matcherConstructionNanos: Long,
    val matchNormalizationNanos: Long,
    val candidateSearchNanos: Long,
    val contextFilteringNanos: Long,
    val selectionAndResolutionNanos: Long,
    val aliasEntries: Int,
    val normalizedAliasCount: Int,
    val regexTests: Long,
    val candidatesFound: Long,
    val candidatesBlocked: Long,
    val candidatesSelected: Long,
    val matchCalls: Long
)

class MatcherProfileCollector {
    private var normalizationPreparationNanos = 0L
    private var aliasPreparationNanos = 0L
    private var regexPreparationNanos = 0L
    private var longerLinkedAliasCheckNanos = 0L
    private var matcherConstructionNanos = 0L
    private var matchNormalizationNanos = 0L
    private var candidateSearchNanos = 0L
    private var contextFilteringNanos = 0L
    private var selectionAndResolutionNanos = 0L
    private var aliasEntries = 0
    private var normalizedAliasCount = 0
    private var regexTests = 0L
    private var candidatesFound = 0L
    private var candidatesBlocked = 0L
    private var candidatesSelected = 0L
    private var matchCalls = 0L

    internal fun recordConstruction(
        normalizationNanos: Long,
        aliasNanos: Long,
        regexNanos: Long,
        linkedAliasNanos: Long,
        totalNanos: Long,
        entryCount: Int,
        normalizedCount: Int
    ) {
        normalizationPreparationNanos += normalizationNanos
        aliasPreparationNanos += aliasNanos
        regexPreparationNanos += regexNanos
        longerLinkedAliasCheckNanos += linkedAliasNanos
        matcherConstructionNanos += totalNanos
        aliasEntries += entryCount
        normalizedAliasCount += normalizedCount
    }

    internal fun recordMatch(
        normalizationNanos: Long,
        searchNanos: Long,
        contextNanos: Long,
        selectionNanos: Long,
        testedRegexes: Int,
        foundCandidates: Int,
        blockedCandidates: Int,
        selectedCandidates: Int
    ) {
        matchNormalizationNanos += normalizationNanos
        candidateSearchNanos += searchNanos
        contextFilteringNanos += contextNanos
        selectionAndResolutionNanos += selectionNanos
        regexTests += testedRegexes
        candidatesFound += foundCandidates
        candidatesBlocked += blockedCandidates
        candidatesSelected += selectedCandidates
        matchCalls++
    }

    fun snapshot(): MatcherProfileSnapshot = MatcherProfileSnapshot(
        normalizationPreparationNanos,
        aliasPreparationNanos,
        regexPreparationNanos,
        longerLinkedAliasCheckNanos,
        matcherConstructionNanos,
        matchNormalizationNanos,
        candidateSearchNanos,
        contextFilteringNanos,
        selectionAndResolutionNanos,
        aliasEntries,
        normalizedAliasCount,
        regexTests,
        candidatesFound,
        candidatesBlocked,
        candidatesSelected,
        matchCalls
    )
}

/** Matches the longest aliases first so a precise phrase masks shorter overlapping aliases. */
class IngredientMatcher(
    private val database: List<Ingredient>,
    private val profileCollector: MatcherProfileCollector? = null
) {
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

    private val normalizedAliases: List<String>
    private val aliases: List<AliasEntry>

    init {
        val collector = profileCollector
        val constructionStarted = if (collector != null) System.nanoTime() else 0L
        val normalizationStarted = if (collector != null) System.nanoTime() else 0L
        normalizedAliases = database.flatMap { ingredient ->
            listOf(ingredient.name) + ingredient.aliases + listOfNotNull(ingredient.eNumber)
        }.map(TextNormalizer::normalize).filter(String::isNotBlank).distinct()
        val normalizationNanos = if (collector != null) System.nanoTime() - normalizationStarted else 0L
        val longerLinkedAliasBases = longerLinkedAliasBases(normalizedAliases)
        var regexNanos = 0L
        var linkedAliasNanos = 0L
        val aliasStarted = if (collector != null) System.nanoTime() else 0L
        aliases = database.flatMap { ingredient ->
            (listOf(ingredient.name) + ingredient.aliases + listOfNotNull(ingredient.eNumber))
                .map { TextNormalizer.normalize(it) }
                .filter { it.isNotBlank() }
                .distinct()
                .map { normalizedAlias ->
                    val regexStarted = if (collector != null) System.nanoTime() else 0L
                    val pattern = Regex(
                        "(?<![a-z0-9])${Regex.escape(normalizedAlias)}(?![a-z0-9])"
                    )
                    if (collector != null) regexNanos += System.nanoTime() - regexStarted
                    val linkedAliasStarted = if (collector != null) System.nanoTime() else 0L
                    val hasLongerLinkedAlias = normalizedAlias in longerLinkedAliasBases
                    if (collector != null) linkedAliasNanos += System.nanoTime() - linkedAliasStarted
                    AliasEntry(ingredient, normalizedAlias, pattern, hasLongerLinkedAlias)
                }
        }.sortedByDescending { it.value.length }
        collector?.recordConstruction(
            normalizationNanos = normalizationNanos,
            aliasNanos = System.nanoTime() - aliasStarted,
            regexNanos = regexNanos,
            linkedAliasNanos = linkedAliasNanos,
            totalNanos = System.nanoTime() - constructionStarted,
            entryCount = aliases.size,
            normalizedCount = normalizedAliases.size
        )
    }

    fun match(token: IngredientToken): IngredientMatch {
        val collector = profileCollector
        val normalizationStarted = if (collector != null) System.nanoTime() else 0L
        val rawNormalized = TextNormalizer.normalize(token.text)
        val normalized = knownIngredientCompounds[rawNormalized] ?: rawNormalized
        val normalizationNanos = if (collector != null) System.nanoTime() - normalizationStarted else 0L
        coveredGlucoseFructose(token, normalized)?.let {
            collector?.recordMatch(normalizationNanos, 0L, 0L, 0L, 0, 0, 0, 1)
            return it
        }
        val searchStarted = if (collector != null) System.nanoTime() else 0L
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
        val searchNanos = if (collector != null) System.nanoTime() - searchStarted else 0L

        val contextStarted = if (collector != null) System.nanoTime() else 0L
        val blocked = candidates.filter { animal ->
            animal.ingredient.id in protectedAnimalIds && candidates.any { source ->
                source.ingredient.status == VeganStatus.VEGAN && protectedPair(normalized, animal, source)
            }
        }
        val eligible = candidates.filterNot { it in blocked ||
            (it.ingredient.id == "honey" && honeyFlavourContext.matches(normalized)) ||
            (it.ingredient.id == "milk" && milkNonIngredientContext.matches(normalized)) ||
            (it.ingredient.id == "apple" && fruitFlavourContext.matches(normalized)) ||
            (it.ingredient.id in flavourQualifiedIngredientIds && isFlavourContext(normalized, it)) ||
            (it.ingredient.id == "coffee" && isExtractContext(normalized, it))
        }
        val contextNanos = if (collector != null) System.nanoTime() - contextStarted else 0L
        val selectionStarted = if (collector != null) System.nanoTime() else 0L
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
        val result = IngredientMatch(
            token,
            ingredients.values.toList(),
            residual,
            resolution,
            blocked.map { it.ingredient.id }.distinct()
        )
        collector?.recordMatch(
            normalizationNanos,
            searchNanos,
            contextNanos,
            System.nanoTime() - selectionStarted,
            aliases.size,
            candidates.size,
            blocked.size,
            selected.size
        )
        return result
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

    private fun isFlavourContext(normalized: String, candidate: Candidate): Boolean {
        val prefix = normalized.substring(0, candidate.start).trim()
        val suffix = normalized.substring(candidate.endExclusive).trim()
        return flavourPrefixes.matches(prefix) || flavourSuffixes.matches(suffix)
    }

    private fun isExtractContext(normalized: String, candidate: Candidate): Boolean =
        extractPrefixes.matches(normalized.substring(0, candidate.start).trim())

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

    companion object {
        /**
         * Precomputes the exact bases that the former pairwise search identified.
         * Kept internal so JVM tests can compare it with the former predicate over every alias.
         */
        internal fun longerLinkedAliasBases(normalizedAliases: Collection<String>): Set<String> = buildSet {
            val aliases = normalizedAliases.toHashSet()
            normalizedAliases.forEach { longerAlias ->
                var separator = longerAlias.indexOf(' ')
                while (separator >= 0) {
                    val base = longerAlias.substring(0, separator)
                    val suffix = longerAlias.substring(separator + 1)
                    if (base in aliases && linkedSuffix.matches(suffix)) add(base)
                    separator = longerAlias.indexOf(' ', separator + 1)
                }
            }
        }

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
        val honeyFlavourContext = Regex(
            "^(?:(?:arome de|gout de|gout) (?:miel|honey|honing|honig)|(?:miel|honey|honing|honig) (?:flavour|flavor|aroma))$"
        )
        val milkNonIngredientContext = Regex(
            "^(?:(?:arome de|gout de|gout) (?:lait|milk|melk|milch)|" +
                "(?:lait|milk|melk|milch) (?:flavour|flavor|aroma)|" +
                "(?:lait vegetal|plant milk|plantaardige melk|pflanzliche milch)|" +
                "(?:contient(?: du)? lait|contains milk|bevat melk|enthalt milch))$"
        )
        val fruitFlavourContext = Regex("^(?:arome de|gout de) pomme$")
        val chocolateConceptIds = setOf(
            "chocolate", "milk_chocolate", "white_chocolate", "filled_chocolate",
            "chocolate_confection", "powdered_chocolate"
        )
        val flavourPrefixes = Regex(
            "^(?:arome(?: naturel)?(?: de)?|arome gout|gout(?: de)?)$"
        )
        val flavourSuffixes = Regex("^(?:flavour|flavor|aroma)$")
        val extractPrefixes = Regex("^(?:extrait de|extract of|extract van|extrakt aus)$")
        val flavourQualifiedIngredientIds = chocolateConceptIds + setOf("strawberry", "coffee")
        val knownIngredientCompounds = mapOf("bitterschokolade" to "bitter schokolade")
    }
}
