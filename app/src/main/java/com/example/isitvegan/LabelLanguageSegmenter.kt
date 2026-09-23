package com.example.isitvegan

enum class LabelLanguage(val displayName: String) {
    FRENCH("FR"), DUTCH("NL"), ENGLISH("EN"), GERMAN("DE"), ITALIAN("IT"),
    SPANISH("ES"), PORTUGUESE("PT"), SWEDISH("SE/SV"), DANISH("DK/DA"),
    NORWEGIAN("NO"), FINNISH("FI"), UNKNOWN("inconnue")
}

data class LanguageBlock(
    val language: LabelLanguage,
    val marketTags: Set<String>,
    val rawText: String,
    val startIndex: Int,
    val endIndex: Int,
    val detectedMarker: String?,
    val headingLanguage: LabelLanguage? = null,
    val languageCorrectionReason: String? = null,
    val hasIngredientHeading: Boolean = false,
    val hasHeadingSeparator: Boolean = false,
    val usefulLength: Int = 0,
    val manifestlyTruncated: Boolean = false,
    /** Stable identity shared by OCR selection, diagnostics and analysis. */
    val id: String = "block-$startIndex-$endIndex",
    val selectionScore: Int = 0,
    val selectionSignals: List<String> = emptyList()
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
    val rejectedUntitledLanguages: List<LabelLanguage> = emptyList(),
    val selectedBlockId: String? = null
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
                MarkerPattern(LabelLanguage.FRENCH, codeRegex(languageCodePattern("FR", listOf("BE", "CH", "LU", "LUX"), listOf("F")))),
                MarkerPattern(LabelLanguage.DUTCH, codeRegex(languageCodePattern("NL", listOf("BE", "LU", "LUX")))),
                MarkerPattern(LabelLanguage.ENGLISH, codeRegex(languageCodePattern("EN", listOf("GB"), listOf("GB")))),
                MarkerPattern(LabelLanguage.GERMAN, codeRegex(languageCodePattern("DE", emptyList()))),
                MarkerPattern(LabelLanguage.ITALIAN, codeRegex(languageCodePattern("IT", emptyList()))),
                MarkerPattern(LabelLanguage.SPANISH, codeRegex(languageCodePattern("ES", emptyList()))),
                MarkerPattern(LabelLanguage.PORTUGUESE, codeRegex(languageCodePattern("PT", emptyList()))),
                MarkerPattern(LabelLanguage.SWEDISH, codeRegex("(?:SE|SV)(?:\\s*(?:[-/]\\s*|\\s+)(?:DK|DA|NO)){0,3}")),
                MarkerPattern(LabelLanguage.DANISH, codeRegex("(?:DK|DA)")),
                MarkerPattern(LabelLanguage.NORWEGIAN, codeRegex("NO")),
                MarkerPattern(LabelLanguage.FINNISH, codeRegex("FI"))
            )
        )
    }

    fun segment(text: String, preferredUiLanguage: UiLanguage? = null): LanguageSegmentation {
        val markers = findMarkers(text)
        if (markers.isEmpty()) return fallback(text)
        val blocks = markers.mapIndexed { index, marker ->
            val end = markers.getOrNull(index + 1)?.range?.first ?: text.length
            LanguageBlock(marker.language, marketTags(marker.marker), text.substring(marker.contentStart, end).trim(), marker.contentStart, end, marker.marker)
                .let(OcrBlockLanguageClassifier::classify)
        }.filter { it.rawText.isNotBlank() }.map(::scoreBlock)
        if (blocks.isEmpty()) return fallback(text)
        val titledBlocks = blocks.filter { it.hasIngredientHeading }
        val selected = preferredBlock(titledBlocks.ifEmpty { blocks }, preferredUiLanguage)
        val correctionReason = selected.languageCorrectionReason?.let { " $it" }.orEmpty()
        return LanguageSegmentation(text, blocks, selected.rawText, selected.language, selected.detectedMarker,
            blocks.map { it.language }.filter { it != selected.language }.distinct(), false,
            selectionReason = if (titledBlocks.isNotEmpty()) {
                "Qualité du bloc évaluée avant la préférence linguistique.$correctionReason"
            } else {
                "Aucun bloc avec titre d’ingrédients ; bloc conservé pour un éventuel mode manuel."
            },
            rejectedUntitledLanguages = if (titledBlocks.isNotEmpty()) {
                blocks.filterNot { it.hasIngredientHeading }.map { it.language }.distinct()
            } else emptyList(),
            selectedBlockId = selected.id
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

    private fun preferredBlock(blocks: List<LanguageBlock>, uiLanguage: UiLanguage?): LanguageBlock {
        val order = when (uiLanguage) {
            UiLanguage.NL -> listOf(LabelLanguage.DUTCH, LabelLanguage.ENGLISH, LabelLanguage.FRENCH)
            UiLanguage.EN -> listOf(LabelLanguage.ENGLISH, LabelLanguage.FRENCH, LabelLanguage.DUTCH)
            UiLanguage.FR -> listOf(LabelLanguage.FRENCH, LabelLanguage.ENGLISH, LabelLanguage.DUTCH)
            null -> listOf(LabelLanguage.FRENCH, LabelLanguage.DUTCH, LabelLanguage.ENGLISH)
        } + listOf(
        LabelLanguage.GERMAN, LabelLanguage.ITALIAN, LabelLanguage.SPANISH,
        LabelLanguage.PORTUGUESE, LabelLanguage.SWEDISH, LabelLanguage.DANISH,
        LabelLanguage.NORWEGIAN, LabelLanguage.FINNISH
        )
        val ranked = blocks.withIndex().sortedWith(
            compareByDescending<IndexedValue<LanguageBlock>> { it.value.selectionScore }
                .thenBy { index -> order.indexOf(index.value.language).let { if (it < 0) Int.MAX_VALUE else it } }
                .thenByDescending { it.value.usefulLength }
                .thenBy { it.index }
        )
        return ranked.firstOrNull()?.value ?: blocks.first()
    }

    private fun scoreBlock(block: LanguageBlock): LanguageBlock {
        val text = block.rawText
        val signals = mutableListOf<String>()
        var score = 0
        if (block.hasIngredientHeading) { score += 40; signals += "titre d’ingrédients" }
        if (block.hasHeadingSeparator) { score += 8; signals += "séparateur de titre" }
        if (block.headingLanguage != null && block.headingLanguage == block.language) {
            signals += "langue explicitement identifiée"
        }
        // Without an ingredient heading, retain the explicit UI-language preference rather
        // than letting a longer arbitrary paragraph displace the user's language.
        val lengthScore = if (block.hasIngredientHeading) {
            (block.usefulLength / 80).coerceAtMost(15)
        } else 0
        score += lengthScore
        signals += "longueur utile=${block.usefulLength}"
        if (block.hasIngredientHeading && block.usefulLength < 100) {
            score -= 20
            signals += "fragment court"
        }
        val itemCount = text.count { it == ',' || it == ';' }.coerceAtMost(12)
        if (block.hasIngredientHeading) {
            score += itemCount * 2
            if (itemCount >= 2) signals += "liste structurée=$itemCount"
        }
        if (text.trimEnd().lastOrNull() in setOf('.', ')', ']')) { score += 3; signals += "fin de liste cohérente" }
        if (LabelLexicon.tracePrefixes.any { Regex("(?i)$it").containsMatchIn(text) }) {
            score += 2; signals += "section traces"
        }
        if (LabelSectionExtractor.hasEndOrMarketingMarker(text)) {
            score -= 12; signals += "conservation ou marketing"
        }
        if (block.manifestlyTruncated) { score -= 50; signals += "bloc tronqué" }
        return block.copy(selectionScore = score, selectionSignals = signals)
    }

    private fun fallback(text: String): LanguageSegmentation {
        val block = LanguageBlock(LabelLanguage.UNKNOWN, emptySet(), text, 0, text.length, null,
            usefulLength = text.count { it.isLetterOrDigit() })
        return LanguageSegmentation(
            text, listOf(block), text, LabelLanguage.UNKNOWN, null, emptyList(), true,
            selectionReason = "Aucun marqueur de langue ; texte complet conservé pour extraction bornée.",
            selectedBlockId = block.id
        )
    }

    private fun marketTags(marker: String): Set<String> = Regex("\\b(?:BE|CH|LU|LUX|GB|DK|DA|NO)\\b", RegexOption.IGNORE_CASE)
        .findAll(marker).map { it.value.uppercase() }.toCollection(linkedSetOf())

    private fun markerRegex(marker: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$marker",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private fun languageNameRegex(name: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$name(?:\\s*:\\s*|(?=\\s*(?:$|\\r?\\n|${LabelLexicon.ingredientWordPattern}\\b)))",
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
    private fun codeRegex(code: String) = Regex("(?:^|(?<=[\\n.;|]))[\\t ]*$code(?:\\s*(?::|[—–-])\\s*|(?=\\s*(?:$|\\r?\\n|${LabelLexicon.ingredientWordPattern}\\b)))",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
}
