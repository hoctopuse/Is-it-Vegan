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
    val declaredContainsSyntax: String? = null,
    val selectedBlockId: String? = null,
    val traceSection: TraceSection? = null,
    val ingredientSection: IngredientSection? = null,
    val traceSections: List<TraceSection> = listOfNotNull(traceSection),
    /** Present only when a composition is accepted without an explicit label heading. */
    val implicitIngredientList: ImplicitIngredientList? = null,
    val ambiguousProductBoundary: Boolean = false,
    val ambiguousTraceContinuation: Boolean = false
)

data class ImplicitIngredientList(
    val structuralReasons: List<String>,
    val confidence: ImplicitIngredientListConfidence
)

enum class ImplicitIngredientListConfidence { HIGH }

/** Start is inclusive and end is exclusive in LabelSections.rawText. */
data class IngredientSection(
    val start: Int,
    val end: Int,
    val rawText: String
)

/** Single source of truth for detection, boundaries and displayed trace text. */
data class TraceSection(
    val detected: Boolean = true,
    val start: Int,
    val end: Int,
    val rawText: String,
    val normalizedText: String
)

object LabelSectionExtractor {
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
        "mode\\s+d['’]emploi", "zubereitung", "fabricant", "distributeur",
        "manufacturer", "hersteller", "fabriqué\\s+en", "origine", "origin",
        "importateur", "importer", "imported\\s+by", "certification", "certified", "certifi[ée]", "zertifiziert", "gecertificeerd",
        "conditionn[ée]\\s+sous\\s+atmosph[èe]re\\s+protectrice",
        "en\\s+d[ée]pit\\s+des\\s+contr[ôo]les\\s+effectu[ée]s",
        "open\\s+here", "ouvrir\\s+ici"
    )
    private val boundaryHeadings = listOf(
        "non\\s+ouvert", "conservation", "conseils?\\s+de\\s+conservation",
        "bewaring(?:sadvies)?", "te\\s+bewaren", "na\\s+openen", "ongeopend", "conservación", "consejos?\\s+de\\s+conservación", "storage(?:\\s+instructions?)?", "store\\s+in",
        "aufbewahrung(?:shinweise)?", "quantité\\s+nette", "netto(?:hoeveelheid|gewicht)",
        "net\\s+(?:quantity|weight)", "nettogewicht", "cantidad\\s+neta", "nettomasse",
        "[àÀ]\\s+consommer\\s+de\\s+préférence\\s+avant(?:\\s+(?:le|fin))?",
        "best\\s+before(?:\\s+end)?", "ten\\s+minste\\s+houdbaar\\s+tot", "mindestens\\s+haltbar(?:\\s+bis)?",
        "consumir\\s+preferentemente\\s+antes(?:\\s+del\\s+fin)?", "[AaÀà]\\s+conserver", "après\\s+ouverture",
        "na\\s+opening", "after\\s+opening", "nach\\s+dem\\s+[Öö]ffnen", "una\\s+vez\\s+abierto",
        "vor\\s+wärme\\s+schützen", "zu\\s+verbrauchen\\s+bis", "después\\s+de\\s+abrir",
        "rainforest\\s+alliance", "ra\\.org", "mehr\\s+unter", "informations?\\s+nutritionnelles?", "voedingswaarden",
        "lot", "importateur", "importer", "imported\\s+by", "certification", "certified", "certifi[ée]", "zertifiziert", "gecertificeerd",
        "conditionn[ée]\\s+sous\\s+atmosph[èe]re\\s+protectrice",
        "en\\s+d[ée]pit\\s+des\\s+contr[ôo]les\\s+effectu[ée]s",
        "open\\s+here", "ouvrir\\s+ici"
    )

    private val organicCertificationClaims = listOf(
        "ingr\\u00e9dients?\\s+(?:(?:issus?|provenant)\\s+de\\s+l['’]agriculture\\s+biologique|d['’]origine\\s+biologique)",
        "ingredi\\u00ebnten\\s+uit\\s+de\\s+biologische\\s+landbouw",
        "ingredients?\\s+from\\s+organic\\s+farming",
        "zutaten\\s+aus\\s+\\u00f6kologischem\\s+landbau"
    )

    fun extract(block: LanguageBlock): LabelSections =
        extract(block.language, block.rawText).copy(language = block.language, selectedBlockId = block.id)

    fun hasEndOrMarketingMarker(text: String): Boolean =
        (boundaryHeadings + ignoredHeadings).any { Regex("(?i)$it").containsMatchIn(text) }

    fun extract(language: LabelLanguage, text: String): LabelSections {
        val markers = findMarkers(text)
        val ambiguousTraceContinuation = markers.any { marker ->
            if (marker.kind != SectionKind.TRACES) false else {
                val group = enclosingTraceGroup(text, marker)
                if (group == null) depthAt(text, marker.range.first) > 0
                else traceContinuation(text, group, markers).ambiguous
            }
        }
        val ingredient = markers.firstOrNull { it.kind == SectionKind.INGREDIENTS }
        val contains = markers.firstOrNull { it.kind == SectionKind.CONTAINS }
        val ingredientSectionText = ingredient?.let { marker ->
            contentUntil(text, marker, markers) {
                it.kind == SectionKind.TRACES || it.kind == SectionKind.IGNORED ||
                    it.kind == SectionKind.INGREDIENTS || it.kind == SectionKind.CONTAINS
            }
        }
        val untrimmedIngredientsText = when {
            ingredient != null -> ingredientSectionText
            contains != null -> null
            else -> contentUntil(text, SectionMarker(SectionKind.INGREDIENTS, -1..-1, 0), markers) {
                it.kind == SectionKind.TRACES || it.kind == SectionKind.IGNORED
            }
        }
        val ingredientsText = untrimmedIngredientsText?.trimStart()?.takeIf { it.isNotBlank() }
        val scopeStart = contains?.range?.first?.let { first ->
            if (ingredient != null && first < ingredient.range.first && isSimplePresencePrefix(text.substring(0, ingredient.range.first))) first
            else ingredient?.range?.first ?: first
        } ?: ingredient?.range?.first ?: 0
        val scopeEnd = markers.firstOrNull {
            it.range.first > (ingredient?.range?.first ?: scopeStart) &&
                (it.kind == SectionKind.IGNORED || it.kind == SectionKind.INGREDIENTS)
        }?.range?.first ?: text.length
        val possibleProductBoundary = Regex("(?m)[.!?][ \\t]*\\r?\\n[ \\t]*[^\\r\\n:,.!?]+\\r?\\n[ \\t]*(?=(?:contient|contains|bevat|enth\\u00e4lt)\\s*:)", RegexOption.IGNORE_CASE)
            .find(text, scopeStart)?.range?.first?.takeIf { it < scopeEnd }
        val declaredContains = markers.filter {
            it.kind == SectionKind.CONTAINS && it.range.first in scopeStart until (possibleProductBoundary ?: scopeEnd)
        }.mapNotNull {
            contentUntil(text, it, markers) { next ->
                next.kind == SectionKind.TRACES || next.kind == SectionKind.IGNORED ||
                    next.kind == SectionKind.INGREDIENTS || next.kind == SectionKind.CONTAINS
            }.trim().trimEnd('.', ';')
                .replace(Regex("(?i)^(?:du|des|de\\s+la|de\\s+l['’])\\s+"), "")
                .takeIf(String::isNotBlank)
        }.joinToString(", ").takeIf(String::isNotBlank)
        val traceSections = markers.filter { it.kind == SectionKind.TRACES }.map { marker ->
            val nextSection = markers.firstOrNull {
                    it.range.first > marker.range.first &&
                        (it.kind == SectionKind.IGNORED || it.kind == SectionKind.INGREDIENTS ||
                            it.kind == SectionKind.CONTAINS)
                }?.range?.first ?: text.length
            val end = traceEnd(text, marker.contentStart, nextSection)
            val raw = text.substring(marker.range.first, end).trim()
            TraceSection(
                start = marker.range.first,
                end = end,
                rawText = raw,
                normalizedText = normalizeTraceText(raw)
            )
        }.filter { it.rawText.isNotBlank() }
            .fold(mutableListOf<TraceSection>()) { result, candidate ->
                if (result.none { it.start <= candidate.end && candidate.start < it.end }) result += candidate
                result
            }
        val uniqueTraceSections = traceSections.distinctBy {
            TextNormalizer.normalize(it.normalizedText).ifBlank { TextNormalizer.normalize(it.rawText) }
        }
        val traces = uniqueTraceSections.map { it.rawText }
            .joinToString("\n").takeIf(String::isNotBlank)
        val ignored = markers.filter { it.kind == SectionKind.IGNORED }.map { marker ->
            text.substring(
                marker.range.first,
                markers.firstOrNull {
                    it.range.first > marker.range.first &&
                        (it.kind == SectionKind.IGNORED || it.kind == SectionKind.INGREDIENTS ||
                            it.kind == SectionKind.TRACES)
                }?.range?.first ?: text.length
            ).trim()
        }.filter(String::isNotBlank)
        val implicitIngredients = if (ingredient == null && contains == null) {
            detectImplicitIngredientList(ingredientsText)
        } else {
            null
        }
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
            // Existing callers use this flag as the authorization to parse an extracted list.
            // An accepted implicit list meets the same high-confidence extraction contract.
            hasIngredientHeading = ingredient != null || implicitIngredients != null,
            ingredientHeadingText = ingredient?.originalText,
            ingredientHeadingSeparator = ingredient?.separator,
            declaredContainsSyntax = contains?.originalText,
            traceSection = uniqueTraceSections.firstOrNull()?.let {
                it.copy(normalizedText = uniqueTraceSections.joinToString("\n") { section -> section.normalizedText })
            },
            ingredientSection = ingredient?.let { marker ->
                val end = marker.contentStart + ingredientSectionText.orEmpty().length
                val raw = text.substring(marker.contentStart, end)
                IngredientSection(marker.contentStart, marker.contentStart + raw.length, raw)
            } ?: implicitIngredients?.let {
                val end = untrimmedIngredientsText.orEmpty().length
                val start = end - ingredientsText.orEmpty().length
                IngredientSection(start, end, text.substring(start, end))
            },
            traceSections = uniqueTraceSections,
            implicitIngredientList = implicitIngredients,
            ambiguousProductBoundary = possibleProductBoundary != null,
            ambiguousTraceContinuation = ambiguousTraceContinuation
        )
    }

    internal fun isSimplePresencePrefix(prefix: String): Boolean = Regex(
        "(?i)^\\s*(?:contient|contains|bevat|enth\\u00e4lt)\\s*:\\s*[\\p{L}\\d, -]+[.]\\s*$"
    ).matches(prefix)

    internal data class ScopedManualInput(val text: String, val embeddedTraces: List<String>)

    /** Preserve the existing manual preprocessor's treatment of standalone notices. Only
     * embedded groups use the same scope and separator rules as titled compositions. */
    internal fun prepareManualInput(sections: LabelSections): ScopedManualInput {
        val text = sections.rawText
        val markers = findMarkers(text)
        val chars = text.toCharArray()
        val warnings = mutableListOf<String>()
        var coveredUntil = 0
        for (marker in markers.filter { it.kind == SectionKind.TRACES }) {
            val group = enclosingTraceGroup(text, marker) ?: continue
            if (group.first < coveredUntil) continue
            val continuation = traceContinuation(text, group, markers)
            val mask = maskedTraceGroup(group, continuation)
            mask.copyInto(chars, group.first)
            warnings += sections.traceSections.filter { it.start in group }.map { it.rawText }
            coveredUntil = continuation.resumeAt
        }
        return ScopedManualInput(chars.concatToString(), warnings.distinct())
    }

    /**
     * A heading-free composition is accepted only when independent structural signals agree.
     * This deliberately keeps ordinary prose, nutrition panels and a trace-only statement out.
     */
    private fun detectImplicitIngredientList(candidate: String?): ImplicitIngredientList? {
        val text = candidate?.trim()?.takeIf(String::isNotBlank) ?: return null
        val normalized = TextNormalizer.normalize(text)
        if (implicitListExclusions.any { normalized.contains(it) }) return null

        val topLevelSeparators = topLevelSeparatorCount(text)
        val nodes = IngredientTreeParser.parse(text)
        val parsedElements = nodeCount(nodes)
        val knownFoodTerms = implicitIngredientTerms.count { term ->
            Regex("(?<![a-z0-9])${Regex.escape(term)}(?![a-z0-9])").containsMatchIn(normalized)
        }
        val hasPercentage = Regex("\\d+(?:[,.]\\d+)?\\s*%").containsMatchIn(text)
        val hasStructuredGroup = text.any { it == '(' || it == '[' } &&
            IngredientTreeParser.inspectParentheses(text).balanced
        val hasDenseDelimitedSequence = topLevelSeparators >= 4

        // Delimiters, parser output and ingredient vocabulary are all required. A fourth
        // structural signal prevents a short comma-separated sentence from being accepted.
        if (topLevelSeparators < 3 || parsedElements < 4 || knownFoodTerms < 2 ||
            !(hasPercentage || hasStructuredGroup || hasDenseDelimitedSequence)
        ) return null

        return ImplicitIngredientList(
            structuralReasons = buildList {
                add("au moins $topLevelSeparators séparateurs d’éléments")
                add("$parsedElements éléments compatibles avec le parseur")
                add("$knownFoodTerms termes alimentaires reconnus")
                if (hasPercentage) add("pourcentage alimentaire")
                if (hasStructuredGroup) add("structure parenthésée équilibrée")
                if (hasDenseDelimitedSequence) add("séquence d’éléments dense")
            },
            confidence = ImplicitIngredientListConfidence.HIGH
        )
    }

    private fun topLevelSeparatorCount(text: String): Int {
        var depth = 0
        var count = 0
        text.forEach { character ->
            when (character) {
                '(', '[' -> depth++
                ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
                ',', ';' -> if (depth == 0) count++
            }
        }
        return count
    }

    private fun nodeCount(nodes: List<IngredientNode>): Int =
        nodes.sumOf { 1 + nodeCount(it.children) }

    private val implicitListExclusions = listOf(
        "valeurs nutritionnelles", "nutrition", "energie", "energy", "kcal", "kj",
        "peut contenir", "may contain", "kan sporen", "kann spuren"
    )

    // Bounded hints support structural extraction only; they never identify or classify an ingredient.
    private val implicitIngredientTerms = listOf(
        "farine", "ble", "avoine", "orge", "seigle", "mais", "riz", "triticale",
        "sucre", "cacao", "huile", "sel", "eau", "tomate", "poivron", "epices",
        "lait", "oeuf", "soja", "vinaigre", "amidon", "wheat", "flour", "oats",
        "barley", "rye", "corn", "rice", "sugar", "cocoa", "oil", "salt", "water"
    )

    private fun normalizeTraceText(rawText: String): String {
        val text = rawText.trim()
        val enclosed = listOf(
            Regex("(?is)^kan(?:\\s+sporen)?\\s+bevatten(?:\\s+van)?\\s*:?\\s*(.+?)[.!?]?$") ,
            Regex("(?is)^kann\\s+spuren\\s+enthalten(?:\\s+von)?\\s*:?\\s*(.+?)[.!?]?$") ,
            Regex("(?is)^kann\\s+spuren\\s+enthalten\\s+von\\s+(.+?)[.!?]?$") ,
            Regex("(?is)^kan\\s+sporen\\s+bevatten\\s+van\\s+(.+?)[.!?]?$") ,
            Regex("(?is)^kann(?:\\s+spuren(?:\\s+von)?)?\\s+(.+?)\\s+enthalten[.!?]?$"),
            Regex("(?is)^kan(?:\\s+sporen(?:\\s+van)?)?\\s+(.+?)\\s+bevatten[.!?]?$"),
            Regex("(?is)^pu[òo]\\s+contenere(?:\\s+(?:eventuali\\s+)?tracce\\s+di)?\\s*:?\\s*(.+?)[.!?]?$"),
            Regex("(?is)^puede\\s+contener(?:\\s+trazas\\s+de)?\\s*:?\\s*(.+?)[.!?]?$"),
            Regex("(?is)^may\\s+contain(?:\\s+traces?\\s+of)?\\s*:?\\s*(.+?)[.!?]?$"),
            Regex("(?is)^p(?:eu|e)t\\s+(?:cont[eé]nir|conterir|conteir|conteuir)" +
                "\\s*(?::\\s*)?(?:des\\s+)?(?:traces?\\s+(?:éventuelles?\\s+)?d(?:e|['’])\\s*:?\\s*)?(.+?)[.!?]?$"),
            Regex("(?is)^traces?\\s+(?:éventuelles?\\s+)?de\\s*:?\\s*(.+?)[.!?]?$"),
            Regex("(?is)^traces?\\s*:\\s*(.+?)[.!?]?$")
        )
        val content = enclosed.firstNotNullOfOrNull { pattern ->
            pattern.matchEntire(text)?.groupValues?.getOrNull(1)
        } ?: text
        return content.trim().trim(':', ';', '.', '!', '?').trim()
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
        val traceMarkers = LabelLexicon.tracePrefixes.sortedByDescending(String::length).flatMap { prefix ->
            Regex("(?i)(?<![\\p{L}\\d])$prefix(?:\\s*:\\s*|(?=\\s|[.,;)\\]]|$))")
                .findAll(text)
                .map {
                    SectionMarker(SectionKind.TRACES, it.range, it.range.last + 1, it.value.trim())
                }.toList()
        }
        val containsMarkers = LabelLexicon.declaredPresenceWords.flatMap { word ->
            Regex("(?i)(?<![\\p{L}\\d])$word(?:\\s*:\\s*|(?=\\s))")
                .findAll(text)
                .map {
                    SectionMarker(SectionKind.CONTAINS, it.range, it.range.last + 1, it.value.trim())
                }.toList()
        }
        val colonDelimitedIgnoredMarkers = ignoredHeadings.flatMap { heading ->
            Regex("(?i)(?<![\\p{L}\\d])$heading\\s*:")
                .findAll(text)
                .map { SectionMarker(SectionKind.IGNORED, it.range, it.range.last + 1) }
                .toList()
        }
        val boundaryIgnoredMarkers = boundaryHeadings.flatMap { heading ->
            Regex(
                "(?im)(?:^[\\t ]*|(?<=[.!?])[\\t ]+)($heading)(?:[\\t ]*:[\\t ]*|(?=[\\t ,.?!;]|$))"
            )
                .findAll(text)
                .map { match ->
                    val headingRange = match.groups[1]!!.range
                    SectionMarker(SectionKind.IGNORED, headingRange, match.range.last + 1)
                }
                .toList()
        }
        val organicClaimMarkers = organicCertificationClaims.flatMap { claim ->
            Regex("(?im)(?:^[\\t ]*|(?<=[.!?])[\\t ]+)($claim)(?=[\\t .,!?:;]|$)")
                .findAll(text)
                .map { match ->
                    val range = match.groups[1]!!.range
                    SectionMarker(SectionKind.IGNORED, range, range.last + 1)
                }.toList()
        }
        val productLanguageMarkers = LabelLexicon.findProductLanguageBoundaries(text).map { boundary ->
            SectionMarker(SectionKind.IGNORED, boundary.range, boundary.range.first)
        }
        val cocoaSolidsMarkers = Regex(
            "(?im)(?:^|(?<=[.!?]\\s)|(?<=\\n)\\s*)((?:cacao|cocoa\\s+solids|milk\\s+solids|kakao)\\s*:\\s*\\d+(?:[,.]\\d+)?\\s*%\\s*(?:minimum|mindestens|ten\\s+minste)\\b[^\\n.]*(?:\\.|$))"
        ).findAll(text).map { match ->
            val range = match.groups[1]!!.range
            SectionMarker(SectionKind.IGNORED, range, range.first)
        }.toList()
        return (ingredientMarkers + traceMarkers + containsMarkers +
            colonDelimitedIgnoredMarkers + boundaryIgnoredMarkers + organicClaimMarkers +
            productLanguageMarkers + cocoaSolidsMarkers)
            .filter { marker ->
                depthAt(text, marker.range.first) == 0 ||
                    marker.kind == SectionKind.TRACES || marker.kind == SectionKind.IGNORED
            }
            .sortedWith(compareBy<SectionMarker> { it.range.first }.thenBy { markerPriority(it.kind) })
            .fold(mutableListOf()) { result, marker ->
                if (result.none { existing ->
                        existing.range.first <= marker.range.last && marker.range.first <= existing.range.last
                    }) result += marker
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
        val output = StringBuilder()
        var cursor = marker.contentStart
        while (true) {
            val next = markers.firstOrNull { it.range.first >= cursor && it.range.first > marker.range.first && stop(it) }
            val end = next?.range?.first ?: text.length
            if (next?.kind == SectionKind.TRACES) {
                val group = enclosingTraceGroup(text, next)?.takeIf { it.first >= cursor }
                if (group != null) {
                    val continuation = traceContinuation(text, group, markers)
                    output.append(text, cursor, group.first)
                    // Keep the original offsets while removing only the scoped trace. A known
                    // separator becomes a parser delimiter; raw source text stays in rawText.
                    output.append(maskedTraceGroup(group, continuation).concatToString())
                    cursor = continuation.resumeAt
                    continue
                } else if (depthAt(text, next.range.first) > 0) {
                    // An unclosed or unsupported embedded notice has no safe endpoint.
                    // Retain its remainder for review; the ambiguity guard prevents parsing.
                    output.append(text, cursor, text.length)
                    return output.toString()
                }
            }
            output.append(text, cursor, end)
            return trimTrailingProductTitle(output.toString())
        }
    }

    private data class TraceContinuation(val resumeAt: Int, val separated: Boolean = false, val ambiguous: Boolean = false)

    private fun maskedTraceGroup(group: IntRange, continuation: TraceContinuation): CharArray =
        CharArray(continuation.resumeAt - group.first) { ' ' }.also {
            if (continuation.separated) it[it.lastIndex] = ','
        }

    /** A group belongs to the trace only when its opening is immediately before the marker.
     * Pure outer wrappers are included, but a group containing preceding ingredients is not. */
    private fun enclosingTraceGroup(text: String, marker: SectionMarker): IntRange? {
        var opening = text.lastNonWhitespaceBefore(marker.range.first) ?: return null
        if (text[opening] !in "([") return null
        while (true) {
            val previous = text.lastNonWhitespaceBefore(opening) ?: break
            if (text[previous] !in "([") break
            opening = previous
        }
        val stack = ArrayDeque<Char>()
        for (i in opening until text.length) when (val char = text[i]) {
            '(', '[' -> stack.addLast(char)
            ')', ']' -> {
                val expected = if (char == ')') '(' else '['
                if (stack.removeLastOrNull() != expected) return null
                if (stack.isEmpty()) return opening..i
            }
        }
        return null
    }

    private fun String.lastNonWhitespaceBefore(end: Int): Int? =
        (end - 1 downTo 0).firstOrNull { !this[it].isWhitespace() }

    private fun traceContinuation(text: String, group: IntRange, markers: List<SectionMarker>): TraceContinuation {
        val start = group.last + 1
        val following = (start until text.length).firstOrNull { !text[it].isWhitespace() }
            ?: return TraceContinuation(start)
        if (markers.any { it.range.first == following } || text[following] in ")]") {
            return TraceContinuation(start)
        }
        val char = text[following]
        if (char in ",;.-\u2013\u2014") return TraceContinuation(following + 1, separated = true)
        if (text.substring(start, following).any { it == '\n' || it == '\r' }) {
            return TraceContinuation(following, separated = true)
        }
        return TraceContinuation(start, ambiguous = true)
    }

    /** A completed ingredient sentence followed by a fresh label line is commonly the product title. */
    private fun trimTrailingProductTitle(content: String): String {
        val titleStart = Regex(
            "(?im)(?<=[.!?])\\s*\\r?\\n\\s*(?=(?:[A-Z0-9]{2,}[ A-Z0-9-]{2,}\\s+)?(?:nouilles|muesli|mélange|barre)\\b)"
        ).find(content)?.range?.first ?: return content
        return content.substring(0, titleStart).trimEnd()
    }

    /** A trace list may span lines, but a completed allergen sentence is its strongest boundary. */
    private fun traceEnd(text: String, contentStart: Int, maximumEnd: Int): Int {
        var depth = 0
        for (index in contentStart until maximumEnd) {
            when (text[index]) {
                '(', '[' -> depth++
                ')', ']' -> if (depth == 0) return index else depth--
                '.', '!', '?' -> if (depth == 0) return index + 1
            }
        }
        return maximumEnd
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
