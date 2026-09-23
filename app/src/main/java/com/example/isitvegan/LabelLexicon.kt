package com.example.isitvegan

enum class HeadingSeparator { COLON, DASH, LINE_BREAK }

/** Shared, bounded label headings used by segmentation and section extraction. */
internal object LabelLexicon {
    data class IngredientHeading(
        val language: LabelLanguage,
        val wordPattern: String,
        val connectorsPattern: String
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
        IngredientHeading(LabelLanguage.FRENCH, "(?:ingr\u00E9dients?|sngredients?|ingr[ée]cients?|ingr\\u00C3\\u2030dients?)", "du|de\\s+la|de\\s+l['’]|des|de"),
        IngredientHeading(LabelLanguage.DUTCH, "(?:ingredi[ëè]nten?|ingrediënten?|ingredienten?|ingredi\\u00C3\\u2039nten?)", "van\\s+de|van\\s+het|van"),
        IngredientHeading(LabelLanguage.ENGLISH, "ingredients?", "of\\s+the|of"),
        IngredientHeading(LabelLanguage.GERMAN, "zutaten?", "der|des|für"),
        IngredientHeading(LabelLanguage.SPANISH, "ingredientes?", "del|de\\s+la|de\\s+los|de\\s+las|de")
    )

    val ingredientWordPattern: String = ingredientHeadings.joinToString("|") { it.wordPattern }

    val tracePrefixes = listOf(
        "p(?:eu|e)t\\s+cont(?:e|é)nir(?:\\s+(?:des?\\s+)?traces?\\s+de)?",
        "p(?:eu|e)t\\s+conterir(?:\\s+(?:des?\\s+)?traces?\\s+de)?",
        "p(?:eu|e)t\\s+conteir(?:\\s+(?:des?\\s+)?traces?\\s+(?:éventuelles?\\s+)?de)?",
        "p(?:eu|e)t\\s+conteuir(?:\\s+(?:des?\\s+)?traces?\\s+(?:éventuelles?\\s+)?de)?",
        "traces?\\s+éventuelles?\\s+de",
        "traces?\\s*:",
        "may\\s+contain(?:\\s+traces?\\s+of)?",
        "kan(?:\\s+sporen(?:\\s+van)?)?\\s+bevatten",
        "kan(?:\\s+\\p{L}+){1,5}\\s+bevatten",
        "kann(?:\\s+spuren(?:\\s+von)?)?\\s+enthalten",
        "kann(?:\\s+\\p{L}+){1,7}\\s+enthalten",
        "puede\\s+contener(?:\\s+trazas\\s+de)?"
    )

    val declaredPresenceWords = listOf("contient", "contains", "bevat", "enth(?:\\u00E4lt|Ã¤lt|Ält)", "contiene")

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
        (bounded + delimited).distinctBy { it.range }.toList()
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
