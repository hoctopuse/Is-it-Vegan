package com.example.isitvegan

/** Shared, bounded label headings used by segmentation and section extraction. */
internal object LabelLexicon {
    data class IngredientHeading(
        val language: LabelLanguage,
        val wordPattern: String,
        val connectorsPattern: String
    ) {
        val pattern: String =
            "(?:$wordPattern)(?:\\s+(?:$connectorsPattern)\\s+[^:\\r\\n,;.()\\[\\]]{1,64})?\\s*:"
    }

    val ingredientHeadings = listOf(
        IngredientHeading(
            LabelLanguage.FRENCH,
            "ingrédients?",
            "du|de\\s+la|de\\s+l['’]|des|de"
        ),
        IngredientHeading(
            LabelLanguage.DUTCH,
            "ingrediënten?",
            "van\\s+de|van\\s+het|van"
        ),
        IngredientHeading(
            LabelLanguage.ENGLISH,
            "ingredients?",
            "of\\s+the|of"
        ),
        IngredientHeading(
            LabelLanguage.GERMAN,
            "zutaten?",
            "der|des|für"
        ),
        IngredientHeading(
            LabelLanguage.SPANISH,
            "ingredientes?",
            "del|de\\s+la|de\\s+los|de\\s+las|de"
        )
    )

    val ingredientWordPattern: String = ingredientHeadings
        .joinToString("|") { it.wordPattern }

    fun headingFor(language: LabelLanguage): IngredientHeading? =
        ingredientHeadings.firstOrNull { it.language == language }
}
