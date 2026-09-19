package com.example.isitvegan

enum class SourceClaim { VEGETAL, ANIMAL, MICROBIAL, UNSPECIFIED }

internal data class SourceClaimExtraction(
    val text: String,
    val claim: SourceClaim,
    val detectedPhrase: String? = null
)

/** Explicit origin statements only; ordinary uses of “végétal” never match. */
internal object SourceClaimLexicon {
    private data class Entry(val claim: SourceClaim, val pattern: Regex)

    private val entries = listOf(
        entry(SourceClaim.VEGETAL,
            "d['’]origine\\s+végétale|de\\s+origine\\s+végétale|van\\s+plantaardige\\s+oorsprong|" +
                "plant[- ]based|of\\s+vegetable\\s+origin|pflanzlichen\\s+ursprungs|de\\s+origen\\s+vegetal"),
        entry(SourceClaim.ANIMAL,
            "d['’]origine\\s+animale|de\\s+origine\\s+animale|van\\s+dierlijke\\s+oorsprong|" +
                "animal\\s+origin|tierischen\\s+ursprungs|de\\s+origen\\s+animal"),
        entry(SourceClaim.MICROBIAL,
            "origine\\s+microbienne|microbial\\s+origin|microbiële\\s+oorsprong|" +
                "mikrobiellen\\s+ursprungs|origen\\s+microbiano")
    )

    fun extract(value: String): SourceClaimExtraction {
        val match = entries.asSequence().mapNotNull { entry ->
            entry.pattern.find(value)?.let { entry to it }
        }.minByOrNull { it.second.range.first } ?: return SourceClaimExtraction(
            value,
            SourceClaim.UNSPECIFIED
        )
        val cleaned = value.removeRange(match.second.range)
            .replace(Regex("\\s+"), " ")
            .trim()
        return SourceClaimExtraction(cleaned, match.first.claim, match.second.value.trim())
    }

    private fun entry(claim: SourceClaim, pattern: String) = Entry(
        claim,
        Regex("(?i)(?<![\\p{L}\\d])(?:$pattern)(?![\\p{L}\\d])")
    )
}

internal data class OriginResolution(
    val baseStatus: VeganStatus,
    val effectiveStatus: VeganStatus,
    val explanation: String?
)

/** IDs whose existing uncertainty can be resolved by an explicit origin statement. */
internal object SourceClaimResolver {
    private val resolvable = setOf("e471")

    fun resolve(ingredient: Ingredient, claim: SourceClaim): OriginResolution {
        val effective = when {
            ingredient.id !in resolvable -> ingredient.status
            claim == SourceClaim.VEGETAL -> VeganStatus.VEGAN
            claim == SourceClaim.ANIMAL -> VeganStatus.NON_VEGAN
            else -> ingredient.status
        }
        val explanation = when {
            ingredient.id !in resolvable || effective == ingredient.status -> null
            claim == SourceClaim.VEGETAL -> "origine végétale déclarée"
            claim == SourceClaim.ANIMAL -> "origine animale déclarée"
            else -> null
        }
        return OriginResolution(ingredient.status, effective, explanation)
    }
}
