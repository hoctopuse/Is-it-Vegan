package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrNativeOrderRegressionTest {
    @Test fun rotatedNestleLabelKeepsItsFrenchIngredientSection() {
        val mlKitText = """
            FR
            BE
            CH
            Gaufrette croustillante enrobée de chocolat au lait.
            ingrédlents:
            sucre,
            matières grasses végétales (palme, karité),
            LAIT écrémé en poudre,
            farine de BLÉ,
            beurre de cacao,
            PETIT-LAIT filtré en poudre,
            matière grasse de LAIT anhydre,
            BEURRE.
            Peut contenir : ARACHIDES et FRUITS À COQUE.
            SE
            Ingredienser: socker, vegetabiliskt fett.
        """.trimIndent()

        val editable = OcrTextCleaner.clean(mlKitText)
        val segmentation = LabelLanguageSegmenter.segment(editable)
        val sections = LabelSectionExtractor.extract(
            segmentation.selectedLanguage,
            segmentation.selectedText
        )

        assertEquals(LabelLanguage.FRENCH, segmentation.selectedLanguage)
        assertTrue(sections.hasIngredientHeading)
        assertTrue(sections.ingredientsText.orEmpty().contains("LAIT écrémé"))
        assertTrue(sections.tracesText.orEmpty().contains("ARACHIDES"))
    }
}
