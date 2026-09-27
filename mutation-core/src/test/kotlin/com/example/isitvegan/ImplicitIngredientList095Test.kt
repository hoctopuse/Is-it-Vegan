package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImplicitIngredientList095Test {
    private val database = listOf(
        ingredient("wheat_flour", "farine de blé"),
        ingredient("sugar", "sucre"),
        ingredient("rapeseed_oil", "huile de colza"),
        ingredient("cocoa", "cacao"),
        ingredient("salt", "sel"),
        ingredient("milk", "lait", VeganStatus.NON_VEGAN)
    )

    @Test fun realUntitledCompositionIsDetectedAndKeepsTracesSeparate() {
        val diagnostics = analyze(
            """Céréales 93,2% (farine de blé, farine de blé complet,
            farine d'avoine complète, d'orge, de seigle, de maïs, blé
            épeautre, de riz, triticale), cacao 3,5%, huiles végétales
            (huile de colza faible en acide érucique, huile de tournesol),
            sels minéraux (carbonate de calcium, fumarate de fer, sulfate
            de zinc, iodure de potassium), vitamines (C, E, niacine, B1, A,
            B6, acide folique, D), arôme (vanilline). Peut contenir : du lait.""".trimIndent()
        )

        assertEquals(AnalysisAvailability.INGREDIENT_LIST_ANALYZED, diagnostics.result.availability)
        assertTrue(diagnostics.labelSections.hasIngredientHeading)
        assertEquals(null, diagnostics.labelSections.ingredientHeadingText)
        assertEquals(ImplicitIngredientListConfidence.HIGH, diagnostics.labelSections.implicitIngredientList?.confidence)
        assertTrue(diagnostics.labelSections.implicitIngredientList!!.structuralReasons.isNotEmpty())
        assertTrue(diagnostics.crossContactWarnings.single().contains("lait", ignoreCase = true))
        assertFalse(diagnostics.tokens.any { it.text.contains("Peut contenir", true) || it.text.equals("lait", true) })
        assertFalse(diagnostics.result.matched.any { it.id == "milk" })
        val report = DiagnosticReport.build(diagnostics, "0.6.9.5")
        assertTrue(report.contains("Section ingrédients : détectée sans titre explicite"))
        assertTrue(report.contains("Confiance liste implicite"))
    }

    @Test fun simpleUntitledIngredientSequenceNeedsAndMeetsConvergingSignals() {
        val diagnostics = analyze("Farine de blé, sucre, huile de colza, cacao, sel.")

        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertTrue(diagnostics.labelSections.implicitIngredientList != null)
        assertEquals(null, diagnostics.labelSections.ingredientHeadingText)
    }

    @Test fun nutritionMarketingTraceOnlyAndAmbiguousTextStayNotEvaluated() {
        listOf(
            "Valeurs nutritionnelles : énergie 200 kJ, matières grasses 12 g, sel 0,3 g.",
            "Une recette savoureuse avec cacao, sucre et céréales pour toute la famille.",
            "Peut contenir : lait, œuf.",
            "Farine, sucre."
        ).forEach { text ->
            val diagnostics = analyze(text)
            assertEquals(text, AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
            assertNull(text, diagnostics.result.verdict)
            assertTrue(text, diagnostics.result.matched.isEmpty())
            assertTrue(text, diagnostics.result.unknown.isEmpty())
        }
    }

    @Test fun explicitListAndExistingBioAndLineBreakNormalizationsRemainAvailable() {
        val diagnostics = analyze("Ingrédients : farine de blé bio, huile de\ncolza, cacao, sel.")

        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertEquals(null, diagnostics.labelSections.implicitIngredientList)
        assertTrue(diagnostics.result.matched.any { it.id == "wheat_flour" })
        assertTrue(diagnostics.result.matched.any { it.id == "rapeseed_oil" })
    }

    private fun analyze(text: String) = IngredientAnalysisService(IngredientKnowledge(database))
        .analyzeWithDiagnostics(text, InputMode.FULL_LABEL)

    private fun ingredient(id: String, alias: String, status: VeganStatus = VeganStatus.VEGAN) = Ingredient(
        id, alias, listOf(alias), null, status, "test"
    )
}
