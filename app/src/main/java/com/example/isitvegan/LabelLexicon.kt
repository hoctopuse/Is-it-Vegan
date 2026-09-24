package com.example.isitvegan

enum class HeadingSeparator { COLON, DASH, LINE_BREAK }

/** Shared, bounded label headings used by segmentation and section extraction. */
internal object LabelLexicon {
    data class IngredientHeading(
        val language: LabelLanguage,
        val wordPattern: String,
        val connectorsPattern: String,
        val allowDelimited: Boolean = true
    ) {
        private val extendedTitle =
            "(?:\\s+(?:$connectorsPattern)\\s+[^:\\r\\n,;.()\\[\\]]{1,64})?"
        val titlePattern: String = "(?:$wordPattern)$extendedTitle"
        val boundedPattern: String =
            "(?:^|(?<=\\n))[\\t ]*($titlePattern)[\\t ]*(?:(:)|([\\u2014\\u2013—–-])|(?=(\\r?\\n)))"
        val delimitedPattern: String =
            "(?<![\\p{L}\\d])($titlePattern)[\\t ]*(?:(:)|([\\u2014\\u2013—–-]))"
    }

    data class HeadingMatch(
        val language: LabelLanguage,
        val range: IntRange,
        val contentStart: Int,
        val originalText: String,
        val separator: HeadingSeparator
    )

    val ingredientHeadings = listOf(
        IngredientHeading(LabelLanguage.DUTCH, "(?:ngredi\\u00ebnten?|inoredienten|ingredi\\u00eanten?)", "van\\s+de|van\\s+het|van", allowDelimited = false),
        IngredientHeading(LabelLanguage.FRENCH, "(?:ingr\u00E9dients?|sngredients?|ingr[ée]cients?|ingr\\u00C3\\u2030dients?)", "du|de\\s+la|de\\s+l['’]|des|de"),
        IngredientHeading(LabelLanguage.DUTCH, "(?:ingredi[ëè]nten?|ingedi[ëè]nten?|ingrediënten?|ingredienten?|ingredi\\u00C3\\u2039nten?)", "van\\s+de|van\\s+het|van"),
        IngredientHeading(LabelLanguage.ENGLISH, "ingredients?", "of\\s+the|of"),
        IngredientHeading(LabelLanguage.GERMAN, "(?:zutaten?|ztaten)", "der|des|für"),
        IngredientHeading(LabelLanguage.SPANISH, "ingredientes?", "del|de\\s+la|de\\s+los|de\\s+las|de"),
        IngredientHeading(LabelLanguage.ITALIAN, "ingredienti", "di|del|della"),
        IngredientHeading(LabelLanguage.POLISH, "sk[łl]adniki", "z|do")
    )

    val ingredientWordPattern: String = ingredientHeadings.joinToString("|") { it.wordPattern }

    val tracePrefixes = listOf(
        "p(?:eu|e)t\\s+cont[eé]nir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:éventuelles?\\s+)?de",
        "p(?:eu|e)t\\s+conterir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:éventuelles?\\s+)?de",
        "p(?:eu|e)t\\s+conteir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:éventuelles?\\s+)?de",
        "p(?:eu|e)t\\s+conteuir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:éventuelles?\\s+)?de",
        "p(?:eu|e)t\\s+cont[eé]nir",
        "p(?:eu|e)t\\s+conterir",
        "p(?:eu|e)t\\s+conteir",
        "p(?:eu|e)t\\s+conteuir",
        "p(?:eu|e)t\\s+cont[e\\u00e9]nir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:\\u00e9ventuelles?\\s+)?d['’]",
        "p(?:eu|e)t\\s+conterir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:\\u00e9ventuelles?\\s+)?d['’]",
        "p(?:eu|e)t\\s+conteir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:\\u00e9ventuelles?\\s+)?d['’]",
        "p(?:eu|e)t\\s+conteuir\\s*(?::\\s*)?(?:des\\s+)?traces?\\s+(?:\\u00e9ventuelles?\\s+)?d['’]",
        "traces?\\s+éventuelles?\\s+de",
        "traces?\\s*:",
        "may\\s+contain(?:\\s+traces?\\s+of)?",
        "kan\\s+sporen\\s+bevatten\\s+van",
        "kan(?:\\s+sporen(?:\\s+van)?)?\\s+bevatten",
        "kan(?:\\s+\\p{L}+){1,5}\\s+bevatten",
        "kann(?:\\s+spuren(?:\\s+von)?)?\\s+enthalten",
        "kann\\s+spuren\\s+enthalten\\s+von",
        "kann(?:\\s+\\p{L}+){1,7}\\s+enthalten",
        "pu[òo]\\s+contenere(?:\\s+(?:eventuali\\s+)?tracce\\s+di)?",
        "puede\\s+contener(?:\\s+trazas\\s+de)?"
    )

    private val organicCertificationClaim = Regex(
        "(?i)\\bingr\\u00e9dients?\\s+(?:(?:issus?|provenant)\\s+de\\s+l['’]agriculture\\s+biologique|d['’]origine\\s+biologique)\\b|" +
            "\\bingredi\\u00ebnten\\s+uit\\s+de\\s+biologische\\s+landbouw\\b|" +
            "\\bingredients?\\s+from\\s+organic\\s+farming\\b|" +
            "\\bzutaten\\s+aus\\s+\\u00f6kologischem\\s+landbau\\b"
    )

    fun isOrganicCertificationClaim(value: String): Boolean = organicCertificationClaim.containsMatchIn(value)

    val declaredPresenceWords = listOf("contient", "contains", "bevat", "enth(?:\\u00E4lt|Ã¤lt|Ält)", "contiene")

    data class ProductLanguageBoundary(val language: LabelLanguage, val range: IntRange)

    /** Reviewed product-title starts seen after a completed multilingual ingredient list. */
    fun findProductLanguageBoundaries(text: String): List<ProductLanguageBoundary> = productLanguageTitles.flatMap { (language, title) ->
        Regex("(?im)(?:^|(?<=[.!?]\\s)|(?<=\\n)\\s*)($title)").findAll(text).map { match ->
            ProductLanguageBoundary(language, match.groups[1]!!.range)
        }.toList()
    }.sortedBy { it.range.first }

    private val productLanguageTitles = listOf(
        LabelLanguage.DUTCH to "(?:NL\\s+)?BISCUITS\\s+BEDEKT\\s+MET\\s+MELKCHOCOLADE\\b",
        LabelLanguage.GERMAN to "E\\)\\s*KEKSE\\s+ÜBERZOGEN\\s+MIT\\b",
        LabelLanguage.ITALIAN to "ENIKEDAO\\s+AL\\s+LATTE\\b"
    )

    fun findIngredientHeadings(text: String): List<HeadingMatch> = ingredientHeadings.flatMap { heading ->
        val options = setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
        val bounded = Regex(heading.boundedPattern, options).findAll(text).map { match ->
            val separator = when {
                match.groups[2] != null -> HeadingSeparator.COLON
                match.groups[3] != null -> HeadingSeparator.DASH
                else -> HeadingSeparator.LINE_BREAK
            }
            headingMatch(heading.language, match, separator)
        }
        val delimited = Regex(heading.delimitedPattern, options).findAll(text).map { match ->
            headingMatch(
                heading.language,
                match,
                if (match.groups[2] != null) HeadingSeparator.COLON else HeadingSeparator.DASH
            )
        }
        (bounded + if (heading.allowDelimited) delimited else emptySequence()).filter { match ->
            !match.originalText.equals("ztaten", true) && !match.originalText.equals("ingediënten", true) ||
                text.substring(match.contentStart).take(160).let { content ->
                    content.count { it.isLetter() } >= 12 && Regex("\\p{L}+\\s*[,;]\\s*\\p{L}+").containsMatchIn(content)
                }
        }.filterNot { match -> isOrganicCertificationClaim(match.originalText) }
            .filter { match ->
                val degradedDutchTitle = match.originalText.equals("ngrediënten", true) ||
                    match.originalText.equals("inoredienten", true) ||
                    match.originalText.equals("ingrediênten", true)
                !degradedDutchTitle || text.substring(match.contentStart).take(160).substringBefore('.').let { content ->
                    content.count { it == ',' || it == ';' } >= 1 && content.count(Char::isLetter) >= 12
                }
            }
            .distinctBy { it.range }.toList()
    }.sortedBy { it.range.first }

    private fun headingMatch(
        language: LabelLanguage,
        match: MatchResult,
        separator: HeadingSeparator
    ) = HeadingMatch(
        language = language,
        range = match.range,
        contentStart = match.range.last + 1,
        originalText = match.groups[1]!!.value.trim(),
        separator = separator
    )

}
