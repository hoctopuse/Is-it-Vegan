package com.example.isitvegan

enum class LabelLanguage(val displayName: String) {
    FRENCH("FR"), DUTCH("NL"), ENGLISH("EN"), GERMAN("DE"), SPANISH("ES"), UNKNOWN("inconnue")
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
    val usedFallback: Boolean,
    val selectionReason: String = "",
    val rejectedUntitledLanguages: List<LabelLanguage> = emptyList()
)

internal object LabelLanguageSegmenter {
    private data class Marker(val language: LabelLanguage, val range: IntRange, val contentStart: Int, val marker: String)
    private data class MarkerPattern(val language: LabelLanguage, val pattern: Regex, val keepMarkerInBlock: Boolean = false)

    private val markerPatterns = buildList {
        add(MarkerPattern(LabelLanguage.FRENCH, markerRegex("Ingredients\\s+FR\\s*:")))
        addAll(
            listOf(
                MarkerPattern(LabelLanguage.FRENCH, languageNameRegex("Français")),
                MarkerPattern(LabelLanguage.DUTCH, languageNameRegex("Nederlands")),
                MarkerPattern(LabelLanguage.ENGLISH, languageNameRegex("English")),
                MarkerPattern(LabelLanguage.GERMAN, languageNameRegex("Deutsch")),
                MarkerPattern(LabelLanguage.SPANISH, languageNameRegex("Español")),
                MarkerPattern(LabelLanguage.FRENCH, markerRegex("\\[FR\\]")),
                MarkerPattern(LabelLanguage.DUTCH, markerRegex("\\[NL\\]")),
                MarkerPattern(LabelLanguage.ENGLISH, markerRegex("\\[EN\\]")),
                MarkerPattern(LabelLanguage.GERMAN, markerRegex("\\[DE\\]")),
                MarkerPattern(LabelLanguage.SPANISH, markerRegex("\\[ES\\]")),
                MarkerPattern(LabelLanguage.FRENCH, codeRegex(languageCodePattern("FR", listOf("BE", "LU", "LUX"), listOf("F")))),
                MarkerPattern(LabelLanguage.DUTCH, codeRegex(languageCodePattern("NL", listOf("BE", "LU", "LUX")))),
                MarkerPattern(LabelLanguage.ENGLISH, codeRegex(languageCodePattern("EN", listOf("GB"), listOf("GB")))),
                MarkerPattern(LabelLanguage.GERMAN, codeRegex(languageCodePattern("DE", emptyList()))),
                MarkerPattern(LabelLanguage.SPANISH, codeRegex(languageCodePattern("ES", emptyList())))
            )
        )
    }

    fun segment(text: String): LanguageSegmentation {
        val markers = findMarkers(text)
        if (markers.isEmpty()) return fallback(text)
        val blocks = markers.mapIndexed { index, marker ->
            val end = markers.getOrNull(index + 1)?.range?.first ?: text.length
            LanguageBlock(marker.language, marketTags(marker.marker), text.substring(marker.contentStart, end).trim(), marker.contentStart, end, marker.marker)
        }.filter { it.rawText.isNotBlank() }
        if (blocks.isEmpty()) return fallback(text)
        val titledBlocks = blocks.filter { LabelLexicon.findIngredientHeadings(it.rawText).isNotEmpty() }
        val selected = preferredBlock(titledBlocks.ifEmpty { blocks })
        return LanguageSegmentation(text, blocks, selected.rawText, selected.language, selected.detectedMarker,
            blocks.map { it.language }.filter { it != selected.language }.distinct(), false,
            selectionReason = if (titledBlocks.isNotEmpty()) {
                "Bloc avec un titre d’ingrédients, puis préférence linguistique."
            } else {
                "Aucun bloc avec titre d’ingrédients ; bloc conservé pour un éventuel mode manuel."
            },
            rejectedUntitledLanguages = if (titledBlocks.isNotEmpty()) {
                blocks.filterNot { it in titledBlocks }.map { it.language }.distinct()
            } else emptyList()
        )
    }

    private fun findMarkers(text: String): List<Marker> = (markerPatterns.flatMap { definition ->
        definition.pattern.findAll(text).map { match ->
            Marker(definition.language, match.range, if (definition.keepMarkerInBlock) match.range.first else match.range.last + 1,
                match.value.trim().trimEnd(':', '—', '–', '-', ' ').trim())
        }.toList()
    } + LabelLexicon.findIngredientHeadings(text).map { heading ->
        Marker(
            heading.language,
            heading.range,
            heading.range.first,
            heading.originalText
        )
    }).sortedBy { it.range.first }.fold(mutableListOf()) { markers, candidate ->
        val previous = markers.lastOrNull()
        val overlaps = previous?.range?.contains(candidate.range.first) == true
        val repeatedTitle = previous != null && previous.language == candidate.language &&
            text.substring(previous.range.last + 1, candidate.range.first).isBlank()
        if (!overlaps && !repeatedTitle) markers += candidate
        markers
    }

    private fun preferredBlock(blocks: List<LanguageBlock>): LanguageBlock = listOf(
        LabelLanguage.FRENCH, LabelLanguage.DUTCH, LabelLanguage.ENGLISH,
        LabelLanguage.GERMAN, LabelLanguage.SPANISH
    ).firstNotNullOfOrNull { language -> blocks.firstOrNull { it.language == language } } ?: blocks.first()

    private fun fallback(text: String): LanguageSegmentation {
        val block = LanguageBlock(LabelLanguage.UNKNOWN, emptySet(), text, 0, text.length, null)
        return LanguageSegmentation(
            text, listOf(block), text, LabelLanguage.UNKNOWN, null, emptyList(), true,
            selectionReason = "Aucun marqueur de langue ; texte complet conservé pour extraction bornée."
        )
    }

    private fun marketTags(marker: String): Set<String> = Regex("\\b(?:BE|LU|LUX|GB)\\b", RegexOption.IGNORE_CASE)
        .findAll(marker).map { it.value.uppercase() }.toCollection(linkedSetOf())

    private fun markerRegex(marker: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*(?:$marker)",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private fun languageNameRegex(name: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$name(?:\\s*:\\s*|(?=\\s*(?:$|\\r?\\n|(?:${LabelLexicon.ingredientWordPattern})\\b)))",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private fun languageCodePattern(
        languageCode: String,
        marketCodes: List<String>,
        aliases: List<String> = emptyList()
    ): String {
        val languageWithMarkets = if (marketCodes.isEmpty()) {
            languageCode
        } else {
            val market = marketCodes.joinToString("|")
            "$languageCode(?:\\s*(?:[-/]\\s*|\\s+)(?:$market)){0,${marketCodes.size}}"
        }
        val reversedCodes = marketCodes.map { "$it-$languageCode" }
        return (listOf(languageWithMarkets) + reversedCodes +
            listOf("\\[$languageCode]", "\\($languageCode\\)") + aliases)
            .joinToString(prefix = "(?:", postfix = ")", separator = "|")
    }
    private fun codeRegex(code: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$code(?:\\s*(?::|[—–-])\\s*|(?=\\s*(?:$|\\r?\\n|(?:${LabelLexicon.ingredientWordPattern})\\b)))",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
}
