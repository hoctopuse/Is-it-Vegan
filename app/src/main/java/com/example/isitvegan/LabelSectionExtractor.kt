package com.example.isitvegan

data class LabelSections(
    val language: LabelLanguage,
    val ingredientsText: String?,
    val declaredContainsText: String?,
    val tracesText: String?,
    val ignoredSections: List<String>,
    val rawText: String,
    val hasIngredientHeading: Boolean
) {
    val analysisText: String get() = ingredientsText ?: declaredContainsText.orEmpty()
}

internal object LabelSectionExtractor {
    private enum class SectionKind { INGREDIENTS, CONTAINS, TRACES, IGNORED }
    private data class SectionMarker(val kind: SectionKind, val range: IntRange, val contentStart: Int)

    private val headings = mapOf(
        SectionKind.CONTAINS to listOf("contient", "bevat", "contains", "enthält"),
        SectionKind.TRACES to listOf("peut\\s+contenir", "traces?\\s+éventuelles?\\s+de", "traces?\\s*:", "kan\\s+(?:sporen\\s+bevatten\\s+van|bevatten)", "may\\s+contain(?:\\s+traces?\\s+of)?", "kann\\s+spuren\\s+von", "kann\\s+enthalten"),
        SectionKind.IGNORED to listOf(
            "valeurs?\\s+nutritionnelles?", "nutrition(?:al)?\\s+(?:values?|declaration)",
            "nährwert(?:angaben)?", "préparation", "preparation", "bereiding", "preparación",
            "mode\\s+d['’]emploi", "conservation", "conseils?\\s+de\\s+conservation",
            "bewaring", "conservación", "storage", "zubereitung", "quantité\\s+nette",
            "net\\s+weight", "[àÀ]\\s+consommer\\s+de\\s+préférence\\s+avant",
            "best\\s+before", "mindestens\\s+haltbar", "fabricant", "distributeur",
            "manufacturer", "hersteller", "fabriqué\\s+en", "lot", "origine", "origin"
        )
    )

    fun extract(block: LanguageBlock): LabelSections = extract(block.language, block.rawText)

    fun extract(language: LabelLanguage, text: String): LabelSections {
        val markers = findMarkers(text)
        val ingredient = markers.firstOrNull { it.kind == SectionKind.INGREDIENTS }
        val contains = markers.firstOrNull { it.kind == SectionKind.CONTAINS }
        val ingredientsText = when {
            ingredient != null -> contentUntil(text, ingredient, markers) {
                it.kind == SectionKind.TRACES || it.kind == SectionKind.IGNORED ||
                    it.kind == SectionKind.INGREDIENTS
            }
            contains != null -> null
            else -> text.substring(0, markers.firstOrNull { it.kind == SectionKind.IGNORED }?.range?.first ?: text.length)
        }?.trimStart()?.takeIf { it.isNotBlank() }
        val declaredContains = contains?.let { contentUntil(text, it, markers) { next -> next.kind == SectionKind.TRACES || next.kind == SectionKind.IGNORED || next.kind == SectionKind.INGREDIENTS }.trim().takeIf(String::isNotBlank) }
        val traces = if (ingredient != null || contains != null) {
            markers.filter { it.kind == SectionKind.TRACES }.map { marker ->
                text.substring(marker.range.first, markers.firstOrNull { it.range.first > marker.range.first && (it.kind == SectionKind.IGNORED || it.kind == SectionKind.INGREDIENTS) }?.range?.first ?: text.length).trim()
            }.filter(String::isNotBlank).joinToString("\n").takeIf(String::isNotBlank)
        } else null
        val ignored = markers.filter { it.kind == SectionKind.IGNORED }.map { marker ->
            text.substring(marker.range.first, markers.firstOrNull { it.range.first > marker.range.first && it.kind == SectionKind.INGREDIENTS }?.range?.first ?: text.length).trim()
        }.filter(String::isNotBlank)
        return LabelSections(language, ingredientsText, declaredContains, traces, ignored, text, ingredient != null)
    }

    private fun findMarkers(text: String): List<SectionMarker> {
        val ingredientMarkers = LabelLexicon.ingredientHeadings.flatMap { heading ->
            Regex("(?i)(?<![\\p{L}\\d])${heading.pattern}").findAll(text)
                .map { SectionMarker(SectionKind.INGREDIENTS, it.range, it.range.last + 1) }
                .toList()
        }
        val otherMarkers = headings.flatMap { (kind, values) -> values.flatMap { heading ->
            val suffix = if (kind == SectionKind.TRACES) "(?:\\s*:\\s*|(?=\\s))" else "\\s*:"
            Regex("(?i)(?<![\\p{L}\\d])(?:$heading)$suffix").findAll(text)
                .map { SectionMarker(kind, it.range, it.range.last + 1) }.toList()
        } }
        return (ingredientMarkers + otherMarkers)
            .filter { depthAt(text, it.range.first) == 0 }.sortedBy { it.range.first }
            .fold(mutableListOf()) { result, marker ->
                if (result.lastOrNull()?.range?.contains(marker.range.first) != true) result += marker
                result
            }
    }

    private fun contentUntil(text: String, marker: SectionMarker, markers: List<SectionMarker>, stop: (SectionMarker) -> Boolean): String {
        val end = markers.firstOrNull { it.range.first > marker.range.first && stop(it) }?.range?.first ?: text.length
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
