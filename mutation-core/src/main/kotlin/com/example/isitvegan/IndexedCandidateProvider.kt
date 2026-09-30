package com.example.isitvegan

/** The lookup operation used by the phase 3 experiment. */
enum class IndexedCandidateQuery {
    EXACT_ALIAS,
    MULTILINGUAL_ALIAS,
    E_NUMBER,
    TEXT_CANDIDATES
}

/** A candidate carries searchable form metadata; it never decides a final status or verdict. */
data class IndexedCandidate(
    val canonicalId: String,
    val concept: RuntimeConcept?,
    val form: RuntimeForm,
    val canonicalAvailable: Boolean,
    val status: VeganStatus?
)

data class IndexedCandidateResult(
    val query: IndexedCandidateQuery,
    val originalText: String,
    val normalizedText: String,
    val language: LabelLanguage?,
    val candidates: List<IndexedCandidate>,
    val collision: Boolean,
    val invalidCanonicalIds: List<String>
)

/**
 * Experimental read-only façade over KnowledgeIndex.
 *
 * This class enumerates candidates and provenance only. Selection, contextual protections,
 * resolution, unknown collection and verdict calculation remain in IngredientMatcher and the
 * existing analysis service.
 */
class IndexedCandidateProvider(
    private val index: KnowledgeIndex
) {
    private val conceptsById = index.concepts.associateBy { it.id }

    fun findExactAlias(value: String): IndexedCandidateResult =
        result(IndexedCandidateQuery.EXACT_ALIAS, value, null, index.findByAlias(value))

    fun findMultilingualAlias(
        value: String,
        language: LabelLanguage
    ): IndexedCandidateResult =
        result(
            IndexedCandidateQuery.MULTILINGUAL_ALIAS,
            value,
            language,
            index.findByAlias(value, language)
        )

    fun findENumber(value: String): IndexedCandidateResult =
        result(IndexedCandidateQuery.E_NUMBER, value, null, index.findByENumber(value))

    /** Enumerates lexical candidates in a phrase; it does not apply matcher selection rules. */
    fun findTextCandidates(
        value: String,
        language: LabelLanguage? = null
    ): IndexedCandidateResult {
        val forms = index.findCandidatesInText(value, language)
        return result(IndexedCandidateQuery.TEXT_CANDIDATES, value, language, forms)
    }

    private fun result(
        query: IndexedCandidateQuery,
        originalText: String,
        language: LabelLanguage?,
        forms: List<RuntimeForm>
    ): IndexedCandidateResult {
        val candidates = forms.map { form ->
            val concept = conceptsById[form.canonicalId]
            IndexedCandidate(
                canonicalId = form.canonicalId,
                concept = concept,
                form = form,
                canonicalAvailable = form.canonicalAvailable && concept != null,
                status = concept?.status
            )
        }
        val invalid = candidates.asSequence()
            .filterNot { it.canonicalAvailable }
            .map { it.canonicalId }
            .distinct()
            .sorted()
            .toList()
        return IndexedCandidateResult(
            query = query,
            originalText = originalText,
            normalizedText = TextNormalizer.normalize(originalText),
            language = language,
            candidates = candidates,
            collision = candidates.map { it.canonicalId }.distinct().size > 1,
            invalidCanonicalIds = invalid
        )
    }
}
