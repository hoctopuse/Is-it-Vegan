package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Version066TraceLanguageTest {
    private val database = listOf(
        Ingredient("wheat_flour", "farine", listOf("farine", "flour"), null, VeganStatus.VEGAN, "test"),
        Ingredient("sugar", "sucre", listOf("sucre", "sugar"), null, VeganStatus.VEGAN, "test"),
        Ingredient("oil", "huile", listOf("huile", "oil"), null, VeganStatus.VEGAN, "test"),
        Ingredient("milk", "lait", listOf("lait", "milk", "Milch", "melk", "latte", "leche"), null, VeganStatus.VEGETARIAN, "test"),
        Ingredient("egg", "œuf", listOf("œuf", "oeuf", "egg", "Eiern", "eieren", "uova", "huevos"), null, VeganStatus.VEGETARIAN, "test")
    )

    @Test fun multilingualTraceStatementsAreExcludedExactlyOnce() {
        listOf(
            "Kan sporen van melk en eieren bevatten",
            "Kann Spuren von Milch und Eiern enthalten",
            "Può contenere tracce di latte e uova",
            "Può contenere eventuali tracce di latte e uova",
            "Puede contener trazas de leche y huevos",
            "May contain traces of milk and eggs"
        ).forEach { trace ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                "Ingrédients: farine, sucre, huile. $trace.", database, InputMode.FULL_LABEL
            )
            assertEquals(trace, VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
            assertTrue(trace, diagnostics.result.veganBlockers.isEmpty())
            assertEquals(trace, 1, diagnostics.crossContactWarnings.size)
            val normalizedTrace = diagnostics.labelSections.traceSection?.normalizedText.orEmpty()
            assertTrue(trace, normalizedTrace.isNotBlank())
            assertTrue(trace, !normalizedTrace.contains("contenere", true))
            assertTrue(trace, !normalizedTrace.contains("contain", true))
            assertTrue(trace, !normalizedTrace.contains("sporen", true))
            assertTrue(trace, !normalizedTrace.contains("spuren", true))
        }
    }
}
