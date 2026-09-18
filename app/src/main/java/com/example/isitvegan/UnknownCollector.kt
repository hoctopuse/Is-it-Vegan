package com.example.isitvegan

enum class MatchKind { NONE, EXACT, PARTIAL_CONTEXTUAL }

internal data class UnknownAssessment(
    val matchKind: MatchKind,
    val unknown: String?
)

/** Keeps meaningful unmatched text and ignores a small, reviewed vocabulary of presentation words. */
internal object UnknownCollector {
    private val ignorablePhrases = listOf(
        "preparation a base de",
        "partiellement degraisse",
        "partiellement degraissee",
        "non hydrogene",
        "non hydrogenee",
        "proteine de",
        "proteines de",
        "poudre de",
        "extrait de",
        "flocons de",
        "flocon de",
        "base de",
        "feves de",
        "feve de",
        "decortiquees",
        "decortiquee",
        "rehydrate", "rehydratees", "rehydrates", "rehydratee",
        "concentre", "concentree", "concentres", "concentrees",
        "depellicule", "depelliculee", "depellicules", "depelliculees",
        "fume", "fumee", "fumes", "fumees",
        "au bois de hetre"
    ).sortedByDescending { it.length }

    private val glueWords = setOf("de", "d", "du", "des", "a", "au", "aux", "et", "en")

    fun assess(match: IngredientMatch): UnknownAssessment {
        if (match.ingredients.isEmpty()) {
            val unknown = match.token.text.trim().takeIf {
                TextNormalizer.normalize(it).isNotBlank()
            }
            return UnknownAssessment(MatchKind.NONE, unknown)
        }

        var residual = match.residualNormalized
        ignorablePhrases.forEach { phrase ->
            residual = residual.replace(
                Regex("(?<![a-z0-9])${Regex.escape(phrase)}(?![a-z0-9])"),
                " "
            )
        }
        val meaningfulWords = residual.split(Regex("\\s+"))
            .filter { it.isNotBlank() && it !in glueWords }
        if (meaningfulWords.isEmpty()) {
            return UnknownAssessment(MatchKind.EXACT, null)
        }

        // A match inside a longer expression is useful context, but it does not
        // resolve the rest of that expression. Keep its original wording intact.
        return UnknownAssessment(MatchKind.PARTIAL_CONTEXTUAL, match.token.text.trim())
    }

    fun collect(match: IngredientMatch): String? = assess(match).unknown
}
