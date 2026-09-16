package com.example.isitvegan

/** Removes recipe quantities while preserving meaningful identifiers such as E471 and B12. */
internal object QuantityCleaner {
    private val eNumberWithSpace = Regex("(?i)\\bE\\s+(?=\\d)")
    private val insNumberWithSpace = Regex("(?i)\\bINS\\s+(?=\\d)")
    private val vitaminWithSpace = Regex("(?i)\\b[BDK]\\s+(?=\\d)")
    private val omegaWithSpace = Regex("(?i)\\bom[ée]ga\\s+(?=\\d)")
    private val quantityWithUnit = Regex(
        "(?i)(?<![\\p{L}\\d-])\\d+(?:[.,]\\d+)?\\s*" +
            "(?:%|kg|mg|µg|ug|g|ml|cl|dl|l)(?![\\p{L}])"
    )
    private val standaloneNumber = Regex(
        "(?<![\\p{L}\\d-])\\d+(?:[.,]\\d+)?(?![\\p{L}\\d])"
    )
    private val footnoteMarker = Regex("[¹²³⁴⁵⁶⁷⁸⁹⁰]")

    fun clean(text: String): String = text
        .replace(eNumberWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(insNumberWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(vitaminWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(omegaWithSpace) { it.value.trimEnd() + "-" }
        .replace(quantityWithUnit, "")
        .replace(standaloneNumber, "")
        .replace(footnoteMarker, "")
        .replace(Regex("[ \\t]{2,}"), " ")
}
