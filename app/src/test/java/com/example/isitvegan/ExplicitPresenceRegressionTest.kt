package com.example.isitvegan

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ExplicitPresenceRegressionTest {
    private val service = IngredientAnalysisService(IngredientKnowledge.fromJson(
        File("src/main/assets/ingredients.json").readText(),
        File("src/main/assets/ingredient_aliases_multilingual.json").readText(),
        File("src/main/assets/origin_qualifier_rules.json").readText()
    ))

    @Test fun titlelessManualListsUseTheSameTraceScope() {
        for (text in listOf("eau [Peut contenir: noix], lait.", "eau (Peut contenir: noix) - lait.")) {
            val d = service.analyzeWithDiagnostics(text, InputMode.MANUAL_INGREDIENT_LIST)
            assertTrue(text, d.result.matched.any { it.id == "milk" })
            assertEquals(text, VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
            assertTrue(d.crossContactWarnings.any { it.contains("noix") })
            assertFalse(d.crossContactWarnings.any { it.contains("lait") })
        }
    }

    @Test fun tracesAloneAndCompositeScopeControls() {
        for (mode in InputMode.values()) {
            for (text in listOf("[Peut contenir: noix]", "Ingredients: (Peut contenir: noix)", "Contient: [Peut contenir: noix]")) {
                val d = service.analyzeWithDiagnostics(text, mode)
                assertNotEquals("$mode $text", VeganAssessment.VEGAN, d.result.veganAssessment)
                assertTrue("$mode $text", d.result.matched.isEmpty())
                assertTrue("$mode $text trace must not become an unknown ingredient", d.visibleUnknownIngredients.isEmpty())
                assertTrue("$mode $text", d.crossContactWarnings.any { it.contains("noix") })
            }
            val composite = service.analyzeWithDiagnostics("Ingredients: preparation (eau [Peut contenir: noix], lait).", mode)
            assertTrue(mode.toString(), composite.result.matched.any { it.id == "milk" })
            assertNotEquals(mode.toString(), VeganAssessment.VEGAN, composite.result.veganAssessment)
            val unsupported = service.analyzeWithDiagnostics("Ingredients: eau (sucre, Peut contenir: noix), lait.", mode)
            assertTrue(mode.toString(), unsupported.ambiguousOcrText)
            assertEquals("Ingredients: eau (sucre, Peut contenir: noix), lait.", unsupported.labelSections.rawText)
            assertNotEquals(mode.toString(), VeganAssessment.VEGAN, unsupported.result.veganAssessment)
        }
    }

    @Test fun bracketAndDashRegressionsWithCurrentRealKnowledge() {
        val probes = listOf(
            "Ingredients: eau [Peut contenir: noix], lait.",
            "Ingredients: eau (Peut contenir: noix) - lait."
        ).flatMap { text -> InputMode.values().map { mode ->
            val d = service.analyzeWithDiagnostics(text, mode)
            println("B1 probe $mode | $text | section=${d.labelSections.ingredientsText} | presence=${d.declaredPresenceText} | matched=${d.result.matched.map { it.id }} | unknown=${d.result.unknown} | traces=${d.crossContactWarnings} | detailed=${d.result.verdict} | vegan=${d.result.veganAssessment} | ambiguous=${d.ambiguousOcrText}")
            Triple(text, mode, d)
        } }
        for ((text, mode, d) in probes) {
            assertTrue("$mode $text extracted milk", d.labelSections.ingredientsText.orEmpty().contains("lait"))
            assertTrue("$mode $text recognized milk", d.result.matched.any { it.id == "milk" })
            assertEquals("$mode $text", AnalysisVerdict.VEGETARIAN, d.result.verdict)
            assertEquals("$mode $text", VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
            assertTrue("$mode $text nuts trace", d.crossContactWarnings.any { it.contains("noix") })
            assertFalse("$mode $text trace scope", d.crossContactWarnings.any { it.contains("lait") })
        }
    }

    @Test fun compositionResumesAfterParenthesizedTrace() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Ingredients: eau (Peut contenir: noix), lait.", mode)
            assertTrue("$mode section", d.labelSections.ingredientsText.orEmpty().contains("lait"))
            assertTrue("$mode milk", d.result.matched.any { it.id == "milk" })
            assertEquals("$mode", AnalysisVerdict.VEGETARIAN, d.result.verdict)
            assertEquals("$mode", VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
            assertTrue("$mode trace", d.crossContactWarnings.any { it.contains("noix") })
        }
    }

    @Test fun presenceResumesAfterParenthesizedTrace() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja (Peut contenir: noix), E471.", mode)
            assertTrue("$mode presence", d.declaredPresenceText.orEmpty().contains("E471"))
            assertTrue("$mode E471", d.result.matched.any { it.id == "e471" })
            assertEquals("$mode", AnalysisVerdict.UNCERTAIN, d.result.verdict)
            assertEquals("$mode", VeganAssessment.UNCERTAIN, d.result.veganAssessment)
            assertTrue("$mode trace", d.crossContactWarnings.any { it.contains("noix") })
        }
    }

    @Test fun productBoundaryIsNotSilentlyMerged() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja.\nBarre chocolat\nContient: lait.", mode)
            assertTrue("$mode ambiguity", d.ambiguousOcrText)
            assertNotEquals("$mode", VeganAssessment.VEGAN, d.result.veganAssessment)
            assertFalse("$mode milk", d.result.matched.any { it.id == "milk" })
        }
    }

    @Test fun resumedCompositionAndLineBreakControls() {
        for (mode in InputMode.values()) {
            for (text in listOf(
                "Ingredients: eau (Peut contenir: noix), lait.",
                "Ingredients: eau (Peut contenir: noix (amandes)); lait."
            )) {
                val d = service.analyzeWithDiagnostics(text, mode)
                assertTrue("$mode $text milk", d.result.matched.any { it.id == "milk" })
                assertTrue("$mode $text trace", d.crossContactWarnings.any { it.contains("noix") })
                assertFalse("$mode $text trace out", d.preprocessedInput.contains("noix"))
                assertNotEquals("$mode $text", VeganAssessment.VEGAN, d.result.veganAssessment)
                val section = requireNotNull(d.labelSections.ingredientSection)
                assertEquals(section.rawText, d.labelSections.rawText.substring(section.start, section.end))
            }
            val undelimited = service.analyzeWithDiagnostics("Ingredients: eau (Peut contenir: noix) lait.", mode)
            assertTrue(mode.toString(), undelimited.labelSections.ingredientsText.orEmpty().contains("lait"))
            assertTrue(mode.toString(), undelimited.ambiguousOcrText)
            assertNotEquals(mode.toString(), VeganAssessment.VEGAN, undelimited.result.veganAssessment)
            val newline = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja.\nContient: lait.", mode)
            assertFalse(mode.toString(), newline.ambiguousOcrText)
            assertTrue(mode.toString(), newline.result.matched.any { it.id == "milk" })
            val traces = service.analyzeWithDiagnostics("Peut contenir: noix (amandes).", mode)
            assertNotEquals(mode.toString(), VeganAssessment.VEGAN, traces.result.veganAssessment)
            assertTrue(mode.toString(), traces.crossContactWarnings.any { it.contains("noix") })
        }
    }

    @Test fun presenceBeforeHeadingReachesVerdictInAllModes() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Contient: lait. Ingredients: eau.", mode)
            assertTrue("$mode presence", d.labelSections.declaredContainsText.orEmpty().contains("lait"))
            assertTrue("$mode analyzed", d.preprocessedInput.contains("eau"))
            assertTrue("$mode analyzed presence", d.declaredPresenceText.orEmpty().contains("lait"))
            assertTrue("$mode milk", d.result.matched.any { it.id == "milk" })
            assertEquals("$mode verdict", VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
        }
    }

    @Test fun ambiguousPresencePreambleDoesNotConfirmVegan() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Autre produit. Contient: lait. Ingredients: eau.", mode)
            assertNotEquals(mode.toString(), VeganAssessment.VEGAN, d.result.veganAssessment)
            assertTrue(mode.toString(), d.ambiguousOcrText)
        }
    }

    @Test fun multilingualFormsReachVerdictWithRealAssets() {
        val texts = listOf(
            "Ingrédients: eau. Contient: soja. Peut contenir: noix Contient: lait.",
            "Ingrediënten: water. Bevat: soja. Kan bevatten: noten Bevat: melk.",
            "Ingredients: water. Contains: soy. May contain: nuts Contains: milk.",
            "Zutaten: Wasser. Enthält: Soja. Kann enthalten: Nüsse Enthält: Milch."
        )
        for (text in texts) for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics(text, mode)
            assertTrue("$mode $text milk", d.result.matched.any { it.id == "milk" })
            assertEquals("$mode $text", VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
            assertTrue("$mode $text", d.crossContactWarnings.isNotEmpty())
        }
    }

    @Test fun nestedTraceAndOutsideAnimalStayDistinct() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja (Peut contenir: noix (Contient: lait)) Contient: gelatine", mode)
            assertTrue("$mode", d.result.matched.any { it.id == "gelatin" })
            assertFalse("$mode", d.result.matched.any { it.id == "milk" })
            assertEquals("$mode", AnalysisVerdict.NON_VEGETARIAN, d.result.verdict)
            assertTrue("$mode", d.crossContactWarnings.any { it.contains("lait") })
            assertFalse("$mode", d.labelSections.declaredContainsText.orEmpty().contains("("))
        }
    }

    @Test fun unbalancedTraceScopePreventsConfirmation() {
        for (mode in InputMode.values()) {
            val d = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja (Peut contenir: noix Contient: lait", mode)
            assertNotEquals(mode.toString(), VeganAssessment.VEGAN, d.result.veganAssessment)
            assertTrue(mode.toString(), d.ambiguousOcrText)
        }
    }

    @Test fun presenceInsideTraceIsNotAnIngredient() {
        val d = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja. Peut contenir: noix (Contient: lait).", InputMode.FULL_LABEL)
        assertTrue(d.crossContactWarnings.any { it.contains("Contient: lait") })
        assertFalse(d.result.matched.any { it.id == "milk" })
        assertEquals(AnalysisVerdict.VEGAN, d.result.verdict)
    }

    @Test fun parenthesizedTraceDoesNotConsumeFollowingPresence() {
        val d = service.analyzeWithDiagnostics("Contient: soja (Peut contenir: noix) Contient: lait", InputMode.FULL_LABEL)
        assertTrue(d.labelSections.declaredContainsText.orEmpty().contains("lait"))
        assertFalse(d.labelSections.declaredContainsText.orEmpty().contains("("))
        assertTrue(d.crossContactWarnings.any { it.contains("noix") })
        assertEquals(VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
    }

    @Test fun interleavedDeclarationsAndUnpunctuatedBoundary() {
        for (mode in InputMode.values()) {
            for ((term, verdict) in listOf("lait" to AnalysisVerdict.VEGETARIAN, "gelatine" to AnalysisVerdict.NON_VEGETARIAN, "mystere" to AnalysisVerdict.INCONCLUSIVE)) {
                val d = service.analyzeWithDiagnostics("Ingredients: eau. Contient: soja. Peut contenir: noix. Contient: $term.", mode)
                assertTrue("$mode $term extracted", d.labelSections.declaredContainsText.orEmpty().contains(term))
                assertTrue("$mode $term soy", d.result.matched.any { it.id == "soy" })
                assertTrue("$mode $term water", d.result.matched.any { it.id == "water" })
                assertEquals("$mode $term detailed", verdict, d.result.verdict)
                if (term != "mystere") assertEquals(VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
                else {
                    assertEquals(VeganAssessment.UNCERTAIN, d.result.veganAssessment)
                    assertTrue(d.visibleUnknownIngredients.any { it.contains(term) })
                }
                assertTrue(d.crossContactWarnings.any { it.contains("noix") })
                assertFalse(d.crossContactWarnings.any { it.contains("Contient: $term", true) })
                assertFalse(d.result.matched.any { it.id == "walnut" })
            }
            val d = service.analyzeWithDiagnostics("Ingredients: eau. Peut contenir: noix Contient: lait.", mode)
            assertEquals(AnalysisVerdict.VEGETARIAN, d.result.verdict)
            assertEquals(VeganAssessment.NOT_VEGAN, d.result.veganAssessment)
            assertTrue(d.result.matched.any { it.id == "milk" })
            assertFalse(d.crossContactWarnings.any { it.contains("Contient: lait", true) })
        }
    }
}
