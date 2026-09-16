package com.example.isitvegan

internal object LabelPreprocessor {
    private val crossContactMarker = Regex(
        "(?i)\\b(?:peut\\s+contenir|traces?\\s*(?:éventuelles?\\s*)?(?:de|d['’]|:)|" +
            "fabriqu[ée]\\s+dans\\s+un\\s+atelier)"
    )
    private val percentageSectionHeading = Regex(
        "(?i)(?:^|[.;]\\s*)[\\p{L}][\\p{L}'’ -]{0,40}\\s*" +
            "\\(\\s*\\d+(?:[.,]\\d+)?\\s*%\\s*\\)\\s*:\\s*"
    )
    private val functionalClassHeading = Regex(
        "(?i)\\b(?:stabilisants?|correcteurs?\\s+d['’]acidit[ée]|" +
            "r[ée]gulateurs?\\s+d['’]acidit[ée]|antioxydants?|acidifiants?|" +
            "conservateurs?|[ée]mulsifiants?|[ée]paississants?|g[ée]lifiants?)" +
            "\\s*(?=[(\\[])"
    )
    private val trailingConcentrateFootnote =
        Regex("(?i)\\s*\\.?\\s*\\^\\s*concentr[ée]\\.?\\s*$")
    private val rainforestFootnote =
        Regex("(?is)\\.?\\s*Rainforest\\s+Alliance\\s+Certified.*$")
    private val agricultureFootnote =
        Regex("(?i)\\s*\\*\\s*Agriculture biologique\\.?\\s*$")
    private val ingredientHeading = Regex("(?i)^\\s*ingr[ée]dients?\\s*:\\s*")
    private val containsHeading = Regex("(?i)\\bcontient\\s*:\\s*")
    private val simpleOcrCorrections = listOf(
        Regex("(?i)\\bformage(?=\\s+grana\\b)") to "fromage"
    )

    fun preprocess(text: String): String {
        var cleaned = text.replace('\u00A0', ' ')
            .lines()
            .mapNotNull { line ->
                val heading = TextNormalizer.normalize(line.trim().trim('*').substringBefore(':'))
                if (heading == "traces" || heading == "allergenes") null
                else line.substringBeforeCrossContactNote()
            }
            .joinToString("\n")
            .replace(agricultureFootnote, "")
            .replace(trailingConcentrateFootnote, "")
            .replace(rainforestFootnote, "")
            // Section labels describe groups, not ingredients.
            .replace(percentageSectionHeading, ";")
            .replace(functionalClassHeading, "")
            .replace(ingredientHeading, "")
            .replace(containsHeading, "")

        simpleOcrCorrections.forEach { (pattern, replacement) ->
            cleaned = cleaned.replace(pattern, replacement)
        }
        return QuantityCleaner.clean(cleaned)
    }

    private fun String.substringBeforeCrossContactNote(): String {
        val marker = crossContactMarker.find(this) ?: return this
        return substring(0, marker.range.first)
    }
}
