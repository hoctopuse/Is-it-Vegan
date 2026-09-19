package com.example.isitvegan

typealias MatchKind = MatchResolution

internal data class UnknownAssessment(
    val matchKind: MatchResolution,
    val unknown: String?
)

/** Keeps unresolved leaves in their complete, readable context. */
internal object UnknownCollector {
    fun assess(match: IngredientMatch): UnknownAssessment {
        val unknown = when (match.resolution) {
            MatchResolution.EXACT, MatchResolution.COVERED -> null
            MatchResolution.NONE,
            MatchResolution.PARTIAL_CONTEXTUAL,
            MatchResolution.BLOCKED_CONFLICT -> match.token.text.trim().takeIf {
                TextNormalizer.normalize(it).isNotBlank()
            }
        }
        return UnknownAssessment(match.resolution, unknown)
    }

    fun collect(match: IngredientMatch): String? = assess(match).unknown
}
