package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoIngredientList094Test {
    private val database = listOf(
        Ingredient("water", "eau", listOf("eau", "water"), null, VeganStatus.VEGAN, "test"),
        Ingredient("milk", "lait", listOf("lait", "milk"), null, VeganStatus.NON_VEGAN, "test"),
        Ingredient("sugar", "sucre", listOf("sucre", "sugar"), null, VeganStatus.VEGAN, "test")
    )

    @Test fun emptyTextIsExplicitlyNotEvaluated() {
        val diagnostics = analyze("")

        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
        assertEquals(null, diagnostics.result.verdict)
        assertEquals(DecisionReason.NO_INGREDIENT_LIST, diagnostics.decision.reason)
        assertTrue(diagnostics.result.matched.isEmpty())
        assertTrue(diagnostics.result.unknown.isEmpty())
        val report = DiagnosticReport.build(diagnostics, "0.6.9.4")
        assertTrue(report.contains("Résultat : non évalué"))
        assertFalse(report.contains("Classification détaillée : INCONCLUSIVE"))
    }

    @Test fun textWithoutIngredientHeadingDoesNotProduceAVerdict() {
        val diagnostics = analyze("Valeurs nutritionnelles : énergie 200 kJ. Fabriqué en France.")

        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
        assertEquals(null, diagnostics.result.verdict)
        assertTrue(diagnostics.ingredientTree.isEmpty())
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun traceOnlyTextKeepsTraceSeparateAndNotEvaluated() {
        val diagnostics = analyze("Peut contenir : lait, œuf.")

        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
        assertEquals(null, diagnostics.result.verdict)
        assertTrue(diagnostics.result.matched.isEmpty())
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertTrue(diagnostics.crossContactWarnings.any { it.contains("lait", ignoreCase = true) })
        val report = DiagnosticReport.build(diagnostics, "0.6.9.4")
        assertTrue(report.contains("Résultat : non évalué"))
        assertTrue(report.contains("TRACES / CONTAMINATION"))
        assertFalse(report.substringAfter("Inconnus :").substringBefore("Éléments responsables").contains("lait"))
    }

    @Test fun declaredPresenceIsNotDisplayedAsAVerdictWithoutAList() {
        val diagnostics = analyze("Contient : lait")

        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
        assertEquals(null, diagnostics.result.verdict)
        assertEquals(DecisionReason.NO_INGREDIENT_LIST, diagnostics.decision.reason)
        assertTrue(diagnostics.verdictExplanation.knownBlockingIngredients.isEmpty())
        val report = DiagnosticReport.build(diagnostics, "0.6.9.4")
        assertTrue(report.contains("Résultat : non évalué"))
        assertFalse(report.contains("NON VEGAN"))
        assertTrue(report.contains("Ingrédients non végétariens : non calculés"))
    }

    @Test fun realIngredientListStillUsesExistingVerdictStates() {
        assertEquals(AnalysisVerdict.VEGAN, analyze("Ingrédients : eau, sucre").result.verdict)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, analyze("Ingrédients : lait").result.verdict)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, analyze("Ingrédients : eau, arôme").result.verdict)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, analyze("Ingrédients : eau, mystère").result.verdict)
    }

    private fun analyze(text: String) = IngredientAnalysisService(IngredientKnowledge(database))
        .analyzeWithDiagnostics(text, InputMode.FULL_LABEL)
}
