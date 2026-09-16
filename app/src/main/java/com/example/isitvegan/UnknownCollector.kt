package com.example.isitvegan

/** Keeps meaningful unmatched text and ignores a small, reviewed vocabulary of presentation words. */
internal object UnknownCollector {
    private val ignorablePhrases = listOf(
        "preparation a base de",
        "partiellement degraisse",
        "partiellement degraissee",
        "non hydrogene",
        "non hydrogenee",
        "proteine de",
        "poudre de",
        "extrait de",
        "flocons de",
        "flocon de",
        "base de",
        "feves de",
        "feve de",
        "decortiquees",
        "decortiquee"
    ).sortedByDescending { it.length }

    private val glueWords = setOf("de", "d", "du", "des", "a", "au", "aux", "et", "en")

    fun collect(match: IngredientMatch): String? {
        if (match.ingredients.isEmpty()) {
            return match.token.text.trim().takeIf {
                TextNormalizer.normalize(it).isNotBlank()
            }
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
        return meaningfulWords.joinToString(" ").takeIf { it.isNotBlank() }
    }
}
