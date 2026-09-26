package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelLanguageSegmentationHotfixTest {
    @Test fun everySupportedDashMarkerUsesTheSameRegionalGrammar() {
        listOf(
            Triple("FR/BE/LUX", LabelLanguage.FRENCH, setOf("BE", "LUX")),
            Triple("NL/BE/LUX", LabelLanguage.DUTCH, setOf("BE", "LUX")),
            Triple("EN", LabelLanguage.ENGLISH, emptySet()),
            Triple("DE", LabelLanguage.GERMAN, emptySet())
        ).forEach { (marker, language, markets) ->
            val segmentation = LabelLanguageSegmenter.segment("$marker — composition")
            val block = segmentation.blocks.single()

            assertEquals(marker, language, block.language)
            assertEquals(marker, markets, block.marketTags)
            assertEquals(marker, marker, block.detectedMarker)
            assertFalse(marker, segmentation.usedFallback)
        }
    }

    @Test fun fourRegionalBlocksAreReturnedExactlyInTextOrder() {
        val segmentation = LabelLanguageSegmenter.segment(
            """FR/BE/LUX — français
                NL/BE/LUX — nederlands
                EN — english
                DE — deutsch""".trimIndent()
        )

        assertEquals(
            listOf(
                LabelLanguage.FRENCH,
                LabelLanguage.DUTCH,
                LabelLanguage.ENGLISH,
                LabelLanguage.GERMAN
            ),
            segmentation.blocks.map { it.language }
        )
        assertEquals(setOf("BE", "LUX"), segmentation.blocks[0].marketTags)
        assertEquals(setOf("BE", "LUX"), segmentation.blocks[1].marketTags)
    }

    @Test fun multilingualRegressionLabelStopsFrenchBeforeDutchBlock() {
        val text = """FR/BE/LUX — Ingrédients : pâtes 48 % [semoule de BLÉ dur, ŒUFS], sauce aux légumes 39 % [tomates, oignons, carottes, huile d’olive, herbes], fromage râpé 8 % [LAIT, sel, présure], eau, sel.

            NL/BE/LUX — Ingrediënten: pasta 48 % [griesmeel van harde TARWE, EI], groentesaus 39 % [tomaten, ui, wortelen, olijfolie, kruiden], geraspte kaas 8 % [MELK, zout, stremsel], water, zout.

            EN — Ingredients: pasta 48% [durum WHEAT semolina, EGG], vegetable sauce 39% [tomatoes, onions, carrots, olive oil, herbs], grated cheese 8% [MILK, salt, rennet], water, salt.

            DE — Zutaten: Nudeln 48 % [HARTWEIZENGRIEß, EI], Gemüsesoße 39 %, geriebener Käse 8 % [MILCH, Salz, Lab], Wasser, Salz.""".trimIndent()
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, emptyList())
        val segmentation = diagnostics.languageSegmentation
        val frenchOnly = text.substringBefore("\n\nNL/BE/LUX")
        val frenchReference = VeganAnalyzer.analyzeWithDiagnostics(frenchOnly, emptyList())
        val report = DiagnosticReport.build(diagnostics, "0.5.8.3")

        assertEquals(LabelLanguage.FRENCH, segmentation.selectedLanguage)
        assertEquals(
            listOf(
                LabelLanguage.FRENCH,
                LabelLanguage.DUTCH,
                LabelLanguage.ENGLISH,
                LabelLanguage.GERMAN
            ),
            segmentation.blocks.map { it.language }
        )
        assertEquals("FR/BE/LUX", segmentation.detectedMarker)
        assertEquals(
            listOf(LabelLanguage.DUTCH, LabelLanguage.ENGLISH, LabelLanguage.GERMAN),
            segmentation.ignoredLanguages
        )
        assertFalse(segmentation.usedFallback)
        assertEquals(setOf("BE", "LUX"), segmentation.blocks[0].marketTags)
        assertEquals(setOf("BE", "LUX"), segmentation.blocks[1].marketTags)
        listOf(
            "Langue sélectionnée : FR",
            "Blocs détectés : FR | NL | EN | DE",
            "Marqueur sélectionné : FR/BE/LUX",
            "Autres blocs ignorés : NL | EN | DE",
            "Fallback texte complet : non"
        ).forEach { expected -> assertTrue(expected, report.contains(expected)) }

        val frenchBlock = segmentation.blocks.first().rawText
        val ingredients = diagnostics.labelSections.ingredientsText.orEmpty()
        val preprocessed = diagnostics.preprocessedInput
        listOf("pâtes", "sauce aux légumes", "fromage râpé").forEach { expected ->
            assertTrue(expected, preprocessed.contains(expected, ignoreCase = true))
        }
        listOf(
            "NL/BE/LUX", "Ingrediënten", "pasta", "griesmeel", "MELK", "stremsel",
            "WHEAT", "tomatoes", "MILCH", "HARTWEIZENGRIEß"
        ).forEach { forbidden ->
            assertFalse("bloc FR: $forbidden", frenchBlock.contains(forbidden, ignoreCase = true))
            assertFalse("ingrédients FR: $forbidden", ingredients.contains(forbidden, ignoreCase = true))
            assertFalse("prétraitement FR: $forbidden", preprocessed.contains(forbidden, ignoreCase = true))
            assertFalse(
                "tokens FR: $forbidden",
                diagnostics.tokens.any { it.text.contains(forbidden, ignoreCase = true) }
            )
        }
        assertEquals(frenchReference.tokens.map { it.text }, diagnostics.tokens.map { it.text })
    }

    @Test fun ordinaryWordsContainingLanguageFragmentsDoNotOpenBlocks() {
        val text = "Une enluminure de confrérie accompagne une amande et un mélange belge."
        val segmentation = LabelLanguageSegmenter.segment(text)

        assertTrue(segmentation.usedFallback)
        assertEquals(LabelLanguage.UNKNOWN, segmentation.selectedLanguage)
        assertEquals(text, segmentation.selectedText)
    }
}
