package com.example.isitvegan

import java.text.Normalizer

/** Assigns a block language from its content; the printed heading remains only a hint. */
object OcrBlockLanguageClassifier {
    private val vocabulary = mapOf(
        LabelLanguage.FRENCH to setOf(
            "eau", "huile", "coco", "raffinee", "hydrogenee", "amidons", "amidon", "pomme",
            "terre", "sel", "modifie", "correcteur", "acidite", "proteines", "proteine",
            "epaississant", "arome", "naturel", "extrait", "olive", "sucre", "farine"
        ),
        LabelLanguage.ENGLISH to setOf(
            "water", "oil", "coconut", "refined", "hydrogenated", "starch", "starches", "corn",
            "potato", "salt", "modified", "acidity", "regulator", "protein", "thickener",
            "natural", "flavour", "flavor", "extract", "sugar", "flour"
        ),
        LabelLanguage.DUTCH to setOf(
            "geraffineerde", "ongeharde", "kokosolie", "zetmelen", "aardappel", "zout",
            "gemodificeerd", "tapiocazetmeel", "zuurteregelaar", "aardappeleiwit", "verdikkingsmiddel",
            "natuurlijk", "olijfextract", "suiker", "bloem"
        ),
        LabelLanguage.GERMAN to setOf(
            "wasser", "raffiniertes", "ungehartetes", "kokosnussol", "starke", "kartoffel", "salz",
            "modifizierte", "tapiokastarke", "saureregulator", "kartoffeleiweiss", "verdickungsmittel",
            "naturliches", "olivenextrakt", "zucker", "mehl"
        ),
        LabelLanguage.ITALIAN to setOf(
            "zucchero", "farina", "grano", "sale", "acqua", "olio", "amido", "estratto"
        ),
        LabelLanguage.POLISH to setOf(
            "cukier", "maka", "pszenna", "sol", "woda", "olej", "skrobia"
        )
    )

    fun classify(block: LanguageBlock): LanguageBlock {
        val heading = LabelLexicon.findIngredientHeadings(block.rawText).firstOrNull()
        val content = heading?.let {
            block.rawText.substring((it.range.last + 1).coerceAtMost(block.rawText.length))
        } ?: block.rawText
        val tokens = tokenize(content)
        val ranked = vocabulary.mapValues { (_, words) -> tokens.count(words::contains) }
            .entries.sortedByDescending { it.value }
        val winner = ranked.firstOrNull()
        val runnerUp = ranked.getOrNull(1)?.value ?: 0
        val contentLanguage = winner?.takeIf { it.value >= 2 && it.value > runnerUp }?.key
        // Distinct printed headings are stronger evidence than noisy vocabulary. Generic
        // "Ingredients" remains eligible for content classification because it occurs on
        // multilingual labels without reliably identifying the following language.
        val explicitHeadingLanguage = heading?.language?.takeIf {
            it in setOf(LabelLanguage.DUTCH, LabelLanguage.GERMAN, LabelLanguage.SPANISH, LabelLanguage.ITALIAN, LabelLanguage.POLISH)
        }
        val finalLanguage = explicitHeadingLanguage ?: contentLanguage ?: block.language
        val correction = if (explicitHeadingLanguage == null && contentLanguage != null && contentLanguage != block.language) {
            "Langue corrigée de ${block.language.displayName} vers ${contentLanguage.displayName} selon le vocabulaire dominant du contenu."
        } else null
        val usefulLength = content.count { it.isLetterOrDigit() }
        val truncated = usefulLength < 8 ||
            Regex("(?m)[\\p{L}]{2,}-\\s*$").containsMatchIn(content) ||
            parenthesisBalance(content) != 0
        return block.copy(
            language = finalLanguage,
            headingLanguage = heading?.language,
            languageCorrectionReason = correction,
            hasIngredientHeading = heading != null,
            hasHeadingSeparator = heading?.separator != null,
            usefulLength = usefulLength,
            manifestlyTruncated = truncated
        )
    }

    private fun tokenize(text: String): List<String> = Normalizer
        .normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .split(Regex("[^\\p{L}]+"))
        .filter { it.length >= 2 }

    private fun parenthesisBalance(text: String): Int = text.fold(0) { balance, char ->
        when (char) {
            '(', '[' -> balance + 1
            ')', ']' -> balance - 1
            else -> balance
        }
    }
}
