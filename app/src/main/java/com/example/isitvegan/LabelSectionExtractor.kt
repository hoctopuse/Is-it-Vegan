package com.example.isitvegan

data class LabelSections(
    val language: LabelLanguage,
    val ingredientsText: String?,
    val declaredContainsText: String?,
    val tracesText: String?,
    val ignoredSections: List<String>,
    val rawText: String,
    val hasIngredientHeading: Boolean,
    val ingredientHeadingText: String? = null,
    val ingredientHeadingSeparator: HeadingSeparator? = null,
    val declaredContainsSyntax: String? = null
) {
    val analysisText: String get() = ingredientsText ?: declaredContainsText.orEmpty()
}

internal object LabelSectionExtractor {
    private enum class SectionKind { INGREDIENTS, CONTAINS, TRACES, IGNORED }
    private data class SectionMarker(
        val kind: SectionKind,
        val range: IntRange,
        val contentStart: Int,
        val originalText: String? = null,
        val separator: HeadingSeparator? = null
    )

    private val ignoredHeadings = listOf(
        "valeurs?\\s+nutritionnelles?", "nutrition(?:al)?\\s+(?:values?|declaration)",
        "nährwert(?:angaben)?", "préparation", "preparation", "bereiding", "preparación",
        "mode\\s+d['’]emploi", "conservation", "conseils?\\s+de\\s+conservation",
        "bewaring", "conservación", "storage", "zubereitung", "quantité\\s+nette",
        "net\\s+weight", "[àÀ]\\s+consommer\\s+de\\s+préférence\\s+avant",
        "best\\s+before", "mindestens\\s+haltbar", "fabricant", "distributeur",
        "manufacturer", "hersteller", "fabriqué\\s+en", "lot", "origine", "origin"
    )

    fun extract(block: LanguageBlock): LabelSections = extract(block.language, block.rawText)

    fun extract(language: LabelLanguage, text: String): LabelSections {
        val markers = findMarkers(text)
        val ingredient = markers.firstOrNull { it.kind == SectionKind.INGREDIENTS }
        val contains = markers.firstOrNull { it.kind == SectionKind.CONTAINS }
        val ingredientsText = when {
            ingredient != null -> contentUntil(text, ingredient, markers) {
                it.kind == SectionKind.TRACES || it.kind == SectionKind.IGNORED ||
                    it.kind == SectionKind.INGREDIENTS || it.kind == SectionKind.CONTAINS
            }
            contains != null -> null
            else -> text.substring(
                0,
                markers.firstOrNull {
                    it.kind == SectionKind.TRACES || it.kind == SectionKind.IGNORED
                }?.range?.first ?: text.length
            )
        }?.trimStart()?.takeIf { it.isNotBlank() }
        val declaredContains = contains?.let {
            contentUntil(text, it, markers) { next ->
                next.kind == SectionKind.TRACES || next.kind == SectionKind.IGNORED ||
                    next.kind == SectionKind.INGREDIENTS
            }.trim()
                .replace(Regex("(?i)^(?:du|des|de\\s+la|de\\s+l['’])\\s+"), "")
                .takeIf(String::isNotBlank)
        }
        val traces = markers.filter { it.kind == SectionKind.TRACES }.map { marker ->
            text.substring(
                marker.range.first,
                markers.firstOrNull {
                    it.range.first > marker.range.first &&
                        (it.kind == SectionKind.IGNORED || it.kind == SectionKind.INGREDIENTS)
                }?.range?.first ?: text.length
            ).trim()
        }.filter(String::isNotBlank).joinToString("\n").takeIf(String::isNotBlank)
        val ignored = markers.filter { it.kind == SectionKind.IGNORED }.map { marker ->
            text.substring(
                marker.range.first,
                markers.firstOrNull {
                    it.range.first > marker.range.first && it.kind == SectionKind.INGREDIENTS
                }?.range?.first ?: text.length
            ).trim()
        }.filter(String::isNotBlank)
        return LabelSections(
            language = ingredient?.let { marker ->
                LabelLexicon.findIngredientHeadings(text)
                    .firstOrNull { it.range == marker.range }?.language
            } ?: language,
            ingredientsText = ingredientsText,
            declaredContainsText = declaredContains,
            tracesText = traces,
            ignoredSections = ignored,
            rawText = text,
            hasIngredientHeading = ingredient != null,
            ingredientHeadingText = ingredient?.originalText,
            ingredientHeadingSeparator = ingredient?.separator,
            declaredContainsSyntax = contains?.originalText
        )
    }

    private fun findMarkers(text: String): List<SectionMarker> {
        val ingredientMarkers = LabelLexicon.findIngredientHeadings(text).map {
            SectionMarker(
                SectionKind.INGREDIENTS,
                it.range,
                it.contentStart,
                it.originalText,
                it.separator
            )
        }
        val traceMarkers = LabelLexicon.tracePrefixes.flatMap { prefix ->
            Regex("(?i)(?<![\\p{L}\\d])(?:$prefix)(?:\\s*:\\s*|(?=\\s))")
                .findAll(text)
                .map {
                    SectionMarker(SectionKind.TRACES, it.range, it.range.last + 1, it.value.trim())
                }.toList()
        }
        val containsMarkers = LabelLexicon.declaredPresenceWords.flatMap { word ->
            Regex("(?i)(?<![\\p{L}\\d])(?:$word)(?:\\s*:\\s*|(?=\\s))")
                .findAll(text)
                .map {
                    SectionMarker(SectionKind.CONTAINS, it.range, it.range.last + 1, it.value.trim())
                }.toList()
        }
        val ignoredMarkers = ignoredHeadings.flatMap { heading ->
            Regex("(?i)(?<![\\p{L}\\d])(?:$heading)\\s*:")
                .findAll(text)
                .map { SectionMarker(SectionKind.IGNORED, it.range, it.range.last + 1) }
                .toList()
        }
        return (ingredientMarkers + traceMarkers + containsMarkers + ignoredMarkers)
            .filter { depthAt(text, it.range.first) == 0 }
            .sortedWith(compareBy<SectionMarker> { it.range.first }.thenBy { markerPriority(it.kind) })
            .fold(mutableListOf()) { result, marker ->
                if (result.lastOrNull()?.range?.contains(marker.range.first) != true) result += marker
                result
            }
    }

    private fun markerPriority(kind: SectionKind): Int = when (kind) {
        SectionKind.TRACES -> 0
        SectionKind.INGREDIENTS -> 1
        SectionKind.CONTAINS -> 2
        SectionKind.IGNORED -> 3
    }

    private fun contentUntil(
        text: String,
        marker: SectionMarker,
        markers: List<SectionMarker>,
        stop: (SectionMarker) -> Boolean
    ): String {
        val end = markers.firstOrNull { it.range.first > marker.range.first && stop(it) }
            ?.range?.first ?: text.length
        return text.substring(marker.contentStart, end)
    }

    private fun depthAt(text: String, position: Int): Int {
        var depth = 0
        for (index in 0 until position) when (text[index]) {
            '(', '[' -> depth++
            ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
        }
        return depth
    }
}
