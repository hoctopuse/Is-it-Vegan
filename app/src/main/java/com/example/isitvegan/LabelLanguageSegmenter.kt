package com.example.isitvegan

enum class LabelLanguage(val displayName: String) {
    FRENCH("FR"), DUTCH("NL"), ENGLISH("EN"), GERMAN("DE"), UNKNOWN("inconnue")
}

data class LanguageBlock(
    val language: LabelLanguage,
    val marketTags: Set<String>,
    val rawText: String,
    val startIndex: Int,
    val endIndex: Int,
    val detectedMarker: String?
)

data class LanguageSegmentation(
    val originalText: String,
    val blocks: List<LanguageBlock>,
    val selectedText: String,
    val selectedLanguage: LabelLanguage,
    val detectedMarker: String?,
    val ignoredLanguages: List<LabelLanguage>,
    val usedFallback: Boolean
)

internal object LabelLanguageSegmenter {
    private data class Marker(val language: LabelLanguage, val range: IntRange, val contentStart: Int, val marker: String)
    private data class MarkerPattern(val language: LabelLanguage, val pattern: Regex, val keepMarkerInBlock: Boolean = false)

    private val markerPatterns = listOf(
        MarkerPattern(LabelLanguage.FRENCH, markerRegex("Ingredients\\s+FR\\s*:")),
        MarkerPattern(LabelLanguage.FRENCH, markerRegex("Ingrédients?\\s*:"), true),
        MarkerPattern(LabelLanguage.DUTCH, markerRegex("Ingrediënten?\\s*:"), true),
        MarkerPattern(LabelLanguage.ENGLISH, markerRegex("Ingredients?\\s*:"), true),
        MarkerPattern(LabelLanguage.GERMAN, markerRegex("Zutaten?\\s*:"), true),
        MarkerPattern(LabelLanguage.FRENCH, languageNameRegex("Français")),
        MarkerPattern(LabelLanguage.DUTCH, languageNameRegex("Nederlands")),
        MarkerPattern(LabelLanguage.ENGLISH, languageNameRegex("English")),
        MarkerPattern(LabelLanguage.GERMAN, languageNameRegex("Deutsch")),
        MarkerPattern(LabelLanguage.FRENCH, markerRegex("\\[FR\\]")),
        MarkerPattern(LabelLanguage.DUTCH, markerRegex("\\[NL\\]")),
        MarkerPattern(LabelLanguage.ENGLISH, markerRegex("\\[EN\\]")),
        MarkerPattern(LabelLanguage.GERMAN, markerRegex("\\[DE\\]")),
        MarkerPattern(LabelLanguage.FRENCH, codeRegex("(?:FR(?:\\s*(?:[-/]\\s*|\\s+)(?:BE|LU|LUX)){0,2}|BE-FR|\\[FR]|\\(FR\\)|F)")),
        MarkerPattern(LabelLanguage.DUTCH, codeRegex("(?:NL(?:\\s*(?:[-/]\\s*|\\s+)BE)?|BE-NL|\\[NL]|\\(NL\\))")),
        MarkerPattern(LabelLanguage.ENGLISH, codeRegex("(?:EN(?:\\s*[-/]\\s*GB)?|GB-EN|\\[EN]|\\(EN\\)|GB)")),
        MarkerPattern(LabelLanguage.GERMAN, codeRegex("(?:DE|\\[DE]|\\(DE\\))"))
    )

    fun segment(text: String): LanguageSegmentation {
        val markers = findMarkers(text)
        if (markers.isEmpty()) return fallback(text)
        val blocks = markers.mapIndexed { index, marker ->
            val end = markers.getOrNull(index + 1)?.range?.first ?: text.length
            LanguageBlock(marker.language, marketTags(marker.marker), text.substring(marker.contentStart, end).trim(), marker.contentStart, end, marker.marker)
        }.filter { it.rawText.isNotBlank() }
        if (blocks.isEmpty()) return fallback(text)
        val selected = preferredBlock(blocks)
        return LanguageSegmentation(text, blocks, selected.rawText, selected.language, selected.detectedMarker,
            blocks.map { it.language }.filter { it != selected.language }.distinct(), false)
    }

    private fun findMarkers(text: String): List<Marker> = markerPatterns.flatMap { definition ->
        definition.pattern.findAll(text).map { match ->
            Marker(definition.language, match.range, if (definition.keepMarkerInBlock) match.range.first else match.range.last + 1,
                match.value.trim().trimEnd(':').trim())
        }.toList()
    }.sortedBy { it.range.first }.fold(mutableListOf()) { markers, candidate ->
        val previous = markers.lastOrNull()
        val overlaps = previous?.range?.contains(candidate.range.first) == true
        val repeatedTitle = previous != null && previous.language == candidate.language &&
            text.substring(previous.range.last + 1, candidate.range.first).isBlank()
        if (!overlaps && !repeatedTitle) markers += candidate
        markers
    }

    private fun preferredBlock(blocks: List<LanguageBlock>): LanguageBlock = listOf(
        LabelLanguage.FRENCH, LabelLanguage.DUTCH, LabelLanguage.ENGLISH, LabelLanguage.GERMAN
    ).firstNotNullOfOrNull { language -> blocks.firstOrNull { it.language == language } } ?: blocks.first()

    private fun fallback(text: String): LanguageSegmentation {
        val block = LanguageBlock(LabelLanguage.UNKNOWN, emptySet(), text, 0, text.length, null)
        return LanguageSegmentation(text, listOf(block), text, LabelLanguage.UNKNOWN, null, emptyList(), true)
    }

    private fun marketTags(marker: String): Set<String> = Regex("\\b(?:BE|LU|LUX|GB)\\b", RegexOption.IGNORE_CASE)
        .findAll(marker).map { it.value.uppercase() }.toCollection(linkedSetOf())

    private fun markerRegex(marker: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*(?:$marker)",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private fun languageNameRegex(name: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$name(?:\\s*:\\s*|(?=\\s*(?:$|\\r?\\n|(?:Ingrédients?|Ingrediënten?|Ingredients?|Zutaten?)\\s*:)))",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private fun codeRegex(code: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$code(?:\\s*:\\s*|(?=\\s*(?:$|\\r?\\n|(?:Ingrédients?|Ingrediënten?|Ingredients?|Zutaten?)\\s*:)))",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
}
