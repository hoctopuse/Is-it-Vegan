package com.example.isitvegan

import java.math.BigDecimal
import java.util.Collections
import kotlin.system.measureNanoTime

/** The classified editorial identity used by the v0.7 runtime contract. */
data class RuntimeConcept(
    val id: String,
    val name: String,
    val status: VeganStatus,
    val reason: String,
    val eNumber: String?,
    val source: String?,
    val possibleOriginNote: PossibleOriginNote?,
    val historicalAliases: List<String>
)

enum class RuntimeFormType {
    CANONICAL_NAME,
    HISTORICAL_ALIAS,
    E_NUMBER,
    MULTILINGUAL_ALIAS,
    OCR_VARIANT,
    STRUCTURED_MAPPING
}

/** A searchable surface. It never carries a classification independent of its concept. */
data class RuntimeForm(
    val surfaceForm: String,
    val normalizedForm: String,
    val language: LabelLanguage,
    val type: RuntimeFormType,
    val canonicalId: String,
    val canonicalAvailable: Boolean,
    val provenance: List<String> = emptyList(),
    val confidence: String? = null,
    val mappingGroup: String? = null,
    val relation: String? = null
)

enum class RuntimeRecognitionState {
    RECOGNIZED,
    RECOGNIZED_UNCERTAIN,
    UNKNOWN
}

/** One analyzed label occurrence. Its identity is positional, never a concept identity. */
data class RuntimeOccurrence(
    val occurrenceId: String,
    val originalText: String,
    val normalizedText: String,
    val correctedText: String? = null,
    val order: Int,
    val language: LabelLanguage = LabelLanguage.UNKNOWN,
    val conceptId: String? = null,
    val matchedForm: RuntimeForm? = null,
    val depth: Int = 0,
    val parentOrder: Int? = null,
    val compositionPath: List<String> = emptyList(),
    val percentage: BigDecimal? = null,
    val context: String? = null,
    val confidence: String? = null,
    val state: RuntimeRecognitionState,
    val declaredPresence: Boolean = false,
    val trace: Boolean = false
)

data class KnowledgeIndexMetrics(
    val buildDurationNanos: Long,
    val conceptCount: Int,
    val formCount: Int,
    val aliasKeyCount: Int,
    val collisionKeyCount: Int,
    val invalidCanonicalIdCount: Int
)

/**
 * Immutable experimental lookup structure. It is deliberately an adapter beside the current
 * matcher; production analysis does not call it in this phase.
 */
class KnowledgeIndex private constructor(
    val concepts: List<RuntimeConcept>,
    private val formsByNormalizedAlias: Map<String, List<RuntimeForm>>,
    private val formsByLanguageAndAlias: Map<Pair<LabelLanguage, String>, List<RuntimeForm>>,
    private val formsByCanonicalId: Map<String, List<RuntimeForm>>,
    private val formsByENumber: Map<String, List<RuntimeForm>>,
    val invalidCanonicalIds: List<String>,
    val metrics: KnowledgeIndexMetrics
) {
    fun findByAlias(value: String, language: LabelLanguage? = null): List<RuntimeForm> {
        val normalized = TextNormalizer.normalize(value)
        return if (language == null) {
            formsByNormalizedAlias[normalized].orEmpty()
        } else {
            formsByLanguageAndAlias[language to normalized].orEmpty()
        }
    }

    /** Candidate enumeration for parity probes; selection and contextual rules remain in the matcher. */
    fun findCandidatesInText(value: String, language: LabelLanguage? = null): List<RuntimeForm> {
        val normalized = TextNormalizer.normalize(value)
        val candidates = if (language == null) {
            formsByNormalizedAlias.values.flatten()
        } else {
            formsByLanguageAndAlias
                .filterKeys { it.first == language }
                .values.flatten()
        }
        return candidates.filter { form ->
            Regex("(?<![a-z0-9])${Regex.escape(form.normalizedForm)}(?![a-z0-9])")
                .containsMatchIn(normalized)
        }
    }

    fun findByENumber(value: String): List<RuntimeForm> =
        formsByENumber[TextNormalizer.normalize(value)].orEmpty()

    fun findByConcept(canonicalId: String): List<RuntimeForm> =
        formsByCanonicalId[canonicalId].orEmpty()

    fun collisionKeys(): Set<String> = formsByNormalizedAlias
        .filterValues { forms -> forms.map { it.canonicalId }.distinct().size > 1 }
        .keys

    companion object {
        fun from(knowledge: IngredientKnowledge): KnowledgeIndex {
            lateinit var result: KnowledgeIndex
            val duration = measureNanoTime {
                result = build(knowledge)
            }
            return result.withBuildDuration(duration)
        }

        private fun build(knowledge: IngredientKnowledge): KnowledgeIndex {
            val concepts = knowledge.ingredients.map { ingredient ->
                RuntimeConcept(
                    id = ingredient.id,
                    name = ingredient.name,
                    status = ingredient.status,
                    reason = ingredient.reason,
                    eNumber = ingredient.eNumber,
                    source = ingredient.source,
                    possibleOriginNote = ingredient.possibleOriginNote,
                    historicalAliases = ingredient.aliases.toList()
                )
            }.toList()
            val knownIds = concepts.mapTo(hashSetOf()) { it.id }
            val forms = buildList {
                knowledge.ingredients.forEach { ingredient ->
                    add(form(ingredient.name, ingredient.id, RuntimeFormType.CANONICAL_NAME, knownIds))
                    ingredient.aliases.forEach { alias ->
                        add(form(alias, ingredient.id, RuntimeFormType.HISTORICAL_ALIAS, knownIds))
                    }
                    ingredient.eNumber?.let {
                        add(form(it, ingredient.id, RuntimeFormType.E_NUMBER, knownIds))
                    }
                }
                // Keep the old lexicon path visible, including OCR variants, without changing its
                // resolution rules or merging entries that happen to share a normalized surface.
                knowledge.multilingualLexicon.legacyRuntimeForms(knownIds).forEach(::add)
                knowledge.multilingualLexicon.runtimeMappings().forEach { mapping ->
                    add(
                        RuntimeForm(
                            surfaceForm = mapping.surfaceForm,
                            normalizedForm = TextNormalizer.normalize(mapping.normalizedForm),
                            language = mapping.language,
                            type = RuntimeFormType.STRUCTURED_MAPPING,
                            canonicalId = mapping.canonicalId,
                            canonicalAvailable = mapping.canonicalId in knownIds,
                            provenance = listOfNotNull(mapping.source),
                            confidence = mapping.confidence,
                            mappingGroup = mapping.mappingGroup,
                            relation = mapping.relation
                        )
                    )
                }
            }
            val invalid = forms.asSequence()
                .filterNot { it.canonicalAvailable }
                .map { it.canonicalId }
                .distinct()
                .sorted()
                .toList()
            val byAlias = immutableIndex(forms.groupBy { it.normalizedForm })
            val byLanguage = immutableIndex(forms.groupBy { it.language to it.normalizedForm })
            val byConcept = immutableIndex(forms.groupBy { it.canonicalId })
            val byENumber = immutableIndex(
                forms.filter { it.type == RuntimeFormType.E_NUMBER }
                    .groupBy { it.normalizedForm }
            )
            return KnowledgeIndex(
                concepts = concepts,
                formsByNormalizedAlias = byAlias,
                formsByLanguageAndAlias = byLanguage,
                formsByCanonicalId = byConcept,
                formsByENumber = byENumber,
                invalidCanonicalIds = invalid,
                metrics = KnowledgeIndexMetrics(
                    buildDurationNanos = 0,
                    conceptCount = concepts.size,
                    formCount = forms.size,
                    aliasKeyCount = byAlias.size,
                    collisionKeyCount = byAlias.count { (_, values) ->
                        values.map { it.canonicalId }.distinct().size > 1
                    },
                    invalidCanonicalIdCount = invalid.size
                )
            )
        }

        private fun form(
            surface: String,
            canonicalId: String,
            type: RuntimeFormType,
            knownIds: Set<String>
        ) = RuntimeForm(
            surfaceForm = surface,
            normalizedForm = TextNormalizer.normalize(surface),
            language = LabelLanguage.UNKNOWN,
            type = type,
            canonicalId = canonicalId,
            canonicalAvailable = canonicalId in knownIds
        )

        private fun <K> immutableIndex(values: Map<K, List<RuntimeForm>>): Map<K, List<RuntimeForm>> =
            Collections.unmodifiableMap(values.mapValues { (_, forms) -> forms.toList() })
    }

    private fun withBuildDuration(duration: Long): KnowledgeIndex = KnowledgeIndex(
        concepts = concepts,
        formsByNormalizedAlias = formsByNormalizedAlias,
        formsByLanguageAndAlias = formsByLanguageAndAlias,
        formsByCanonicalId = formsByCanonicalId,
        formsByENumber = formsByENumber,
        invalidCanonicalIds = invalidCanonicalIds,
        metrics = metrics.copy(buildDurationNanos = duration)
    )
}

private fun MultilingualIngredientLexicon.legacyRuntimeForms(
    knownIds: Set<String>
): List<RuntimeForm> = runtimeLegacyEntries().flatMap { entry ->
    val aliases = entry.aliases.map { it to RuntimeFormType.MULTILINGUAL_ALIAS } +
        entry.ocrVariants.map { it to RuntimeFormType.OCR_VARIANT }
    aliases.map { (surface, type) ->
        RuntimeForm(
            surfaceForm = surface,
            normalizedForm = TextNormalizer.normalize(surface),
            language = entry.language,
            type = type,
            canonicalId = entry.canonicalId,
            canonicalAvailable = entry.canonicalId in knownIds,
            provenance = listOf("legacy-multilingual-aliases")
        )
    }
}
