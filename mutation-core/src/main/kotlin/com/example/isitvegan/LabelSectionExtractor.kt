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
    val traceSection: TraceSection? = null
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
        val traceSections = markers.filter { it.kind == SectionKind.TRACES }.map { marker ->
            val nextSection = markers.firstOrNull {
                    it.range.first > marker.range.first &&
                        (it.kind == SectionKind.IGNORED || it.kind == SectionKind.INGREDIENTS)
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
                if (result.none { it.start <= candidate.end && candidate.start <= it.end }) result += candidate
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
            declaredContainsSyntax = contains?.originalText,
            traceSection = uniqueTraceSections.firstOrNull()?.let {
                it.copy(normalizedText = uniqueTraceSections.joinToString("\n") { section -> section.normalizedText })
            }
        )
    }

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
        val end = markers.firstOrNull { it.range.first > marker.range.first && stop(it) }
            ?.range?.first ?: text.length
        return trimTrailingProductTitle(text.substring(marker.contentStart, end))
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
                ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
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
