package com.example.isitvegan

/**
 * Language aliases deliberately stay separate from ingredients.json: this file only maps a
 * reviewed spelling to a proposed canonical ingredient id. The id is usable only when the
 * canonical database contains it; classification always remains in that database.
 */
internal data class MultilingualIngredientResolution(
    val correctedText: String,
    val alias: String? = null,
    val canonicalId: String? = null,
    val canonicalAvailable: Boolean? = null,
    val language: LabelLanguage
)

internal data class MultilingualLexiconValidation(
    val errors: List<String>,
    val unavailableCanonicalIds: List<String>
) {
    val isValid: Boolean get() = errors.isEmpty()
}

internal class MultilingualIngredientLexicon private constructor(
    private val entries: List<Entry>,
    private val corrections: List<Correction>
) {
    private data class Entry(
        val canonicalId: String,
        val language: LabelLanguage,
        val aliases: List<String>,
        val ocrVariants: List<String>
    )

    private data class Correction(
        val language: LabelLanguage,
        val from: String,
        val to: String
    )

    fun resolve(
        value: String,
        language: LabelLanguage,
        database: List<Ingredient>
    ): MultilingualIngredientResolution {
        val parenthesisStart = value.indexOf('(').takeIf { it >= 0 } ?: value.length
        val base = value.substring(0, parenthesisStart).trim()
        val suffix = value.substring(parenthesisStart)
        val corrected = corrections.firstOrNull {
            it.language == language && TextNormalizer.normalize(it.from) == TextNormalizer.normalize(base)
        }?.to ?: base
        val correctedValue = (corrected + suffix).trim()
        val normalized = TextNormalizer.normalize(corrected)
        val candidate = entries.firstOrNull { entry ->
            entry.language == language && normalized in (entry.aliases + entry.ocrVariants)
                .map(TextNormalizer::normalize)
        } ?: return MultilingualIngredientResolution(correctedValue, language = language)
        val alias = (candidate.aliases + candidate.ocrVariants).firstOrNull {
            TextNormalizer.normalize(it) == normalized
        }
        val correctedFromVariant = candidate.ocrVariants.any {
            TextNormalizer.normalize(it) == normalized
        }
        return MultilingualIngredientResolution(
            correctedText = if (correctedFromVariant) {
                candidate.aliases.firstOrNull().orEmpty() + suffix
            } else correctedValue,
            alias = if (correctedFromVariant) candidate.aliases.firstOrNull() else alias,
            canonicalId = candidate.canonicalId,
            canonicalAvailable = database.any { it.id == candidate.canonicalId },
            language = language
        )
    }

    fun validateAgainst(database: List<Ingredient>): MultilingualLexiconValidation {
        val availableIds = database.mapTo(hashSetOf()) { it.id }
        return MultilingualLexiconValidation(
            errors = structuralErrors(entries, corrections),
            unavailableCanonicalIds = entries.map { it.canonicalId }
                .filterNot(availableIds::contains)
                .distinct()
                .sorted()
        )
    }

    companion object {
        fun empty() = MultilingualIngredientLexicon(emptyList(), emptyList())

        fun load(json: String): MultilingualIngredientLexicon {
            val root = MiniJson.parse(json) as? Map<*, *>
                ?: throw IllegalArgumentException("La racine du lexique multilingue doit être un objet.")
            require((root["schemaVersion"] as? Number)?.toInt() == 1) {
                "Version de schéma du lexique multilingue absente ou non prise en charge."
            }
            val aliases = (root["aliases"] as? List<*>).orEmpty().toEntries()
            val corrections = (root["ocrCorrections"] as? List<*>).orEmpty().toCorrections()
            val errors = structuralErrors(aliases, corrections)
            require(errors.isEmpty()) { errors.joinToString("; ") }
            return MultilingualIngredientLexicon(aliases, corrections)
        }

        private fun List<*>.toEntries(): List<Entry> = mapIndexed { index, value ->
            val item = value as? Map<*, *>
                ?: throw IllegalArgumentException("aliases[$index] doit être un objet")
            val language = labelLanguage(item["language"] as? String)
            val canonicalId = item["canonicalId"] as? String ?: ""
            Entry(
                canonicalId = canonicalId,
                language = language,
                aliases = (item["aliases"] as? List<*>).strings(),
                ocrVariants = (item["ocrVariants"] as? List<*>).strings()
            )
        }

        private fun List<*>.toCorrections(): List<Correction> = mapIndexed { index, value ->
            val item = value as? Map<*, *>
                ?: throw IllegalArgumentException("ocrCorrections[$index] doit être un objet")
            val language = labelLanguage(item["language"] as? String)
            val from = item["from"] as? String ?: ""
            val to = item["to"] as? String ?: ""
            Correction(language, from, to)
        }

        private fun List<*>?.strings(): List<String> = this.orEmpty().filterIsInstance<String>()

        private fun labelLanguage(value: String?): LabelLanguage = when (value?.uppercase()) {
            "FR", "FRENCH" -> LabelLanguage.FRENCH
            "NL", "DUTCH" -> LabelLanguage.DUTCH
            "DE", "GERMAN" -> LabelLanguage.GERMAN
            "EN", "ENGLISH" -> LabelLanguage.ENGLISH
            "IT", "ITALIAN" -> LabelLanguage.ITALIAN
            "ES", "SPANISH" -> LabelLanguage.SPANISH
            "PL", "POLISH" -> LabelLanguage.POLISH
            else -> LabelLanguage.UNKNOWN
        }

        private fun structuralErrors(
            entries: List<Entry>,
            corrections: List<Correction>
        ): List<String> = buildList {
            entries.forEachIndexed { index, entry ->
                if (entry.canonicalId.isBlank()) add("aliases[$index].canonicalId est vide")
                if (entry.language == LabelLanguage.UNKNOWN) add("aliases[$index].language est invalide")
                if (entry.aliases.isEmpty()) add("aliases[$index].aliases est vide")
                val terms = entry.aliases + entry.ocrVariants
                if (terms.any(String::isBlank)) add("aliases[$index] contient un terme vide")
                val duplicates = terms.groupBy(TextNormalizer::normalize)
                    .filter { (term, values) -> term.isNotBlank() && values.size > 1 }
                    .keys
                if (duplicates.isNotEmpty()) {
                    add("aliases[$index] contient des termes équivalents dupliqués: ${duplicates.joinToString()}")
                }
            }
            val indexedTerms = entries.flatMap { entry ->
                (entry.aliases + entry.ocrVariants).map { term ->
                    Triple(entry.language, TextNormalizer.normalize(term), entry.canonicalId)
                }
            }
            indexedTerms.groupBy { it.first to it.second }
                .filterValues { values -> values.size > 1 }
                .forEach { (key, values) ->
                    add("Alias dupliqué pour ${key.first.displayName}: ${key.second} (${values.map { it.third }.distinct().joinToString()})")
                }
            indexedTerms.groupBy { it.second }
                .filterValues { values -> values.map { it.third }.distinct().size > 1 }
                .forEach { (term, values) ->
                    add("Alias multilingue conflictuel: $term (${values.map { it.third }.distinct().joinToString()})")
                }
            corrections.forEachIndexed { index, correction ->
                if (correction.language == LabelLanguage.UNKNOWN) add("ocrCorrections[$index].language est invalide")
                if (correction.from.isBlank() || correction.to.isBlank()) {
                    add("ocrCorrections[$index] contient une correction vide")
                }
            }
            corrections.groupBy { it.language to TextNormalizer.normalize(it.from) }
                .filterValues { it.size > 1 }
                .forEach { (key, _) ->
                    add("Correction OCR dupliquée pour ${key.first.displayName}: ${key.second}")
                }
        }
    }
}
