package com.example.isitvegan

import org.junit.Assert.*
import org.junit.Test

class CrossContactTest {
    private val database = listOf(
        Ingredient("sugar", "sucre", listOf("sucre"), null, VeganStatus.VEGAN, "Test"),
        Ingredient("milk", "lait", listOf("lait"), null, VeganStatus.VEGETARIAN, "Test"),
        Ingredient("gelatin", "gélatine", listOf("gélatine"), null, VeganStatus.NON_VEGAN, "Test"),
        Ingredient("e471", "E471", listOf("E471"), "E471", VeganStatus.UNCERTAIN, "Test")
    )
    private val notices = listOf(
        "Peut contenir du lait et des œufs.",
        "Traces : lait, gélatine",
        "Peut contenir des traces de soja",
        "Fabriqué dans un atelier utilisant du lait",
        "Traces éventuelles de fruits à coque"
    )

    @Test fun eachWarningIsPreservedAndNeverAnalyzed() {
        notices.forEach { notice ->
            listOf("sucre. ", "sucre\n").forEach { prefix ->
                val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(prefix + notice, database)
                assertEquals(listOf(notice), diagnostics.crossContactWarnings)
                assertEquals(listOf("sugar"), diagnostics.result.matched.map { it.id })
                assertTrue(diagnostics.result.unknown.isEmpty())
                assertFalse(diagnostics.result.stoppedAtNonVegetarian)
                assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
                assertEquals(1, diagnostics.tokens.size)
                assertFalse(diagnostics.preprocessedInput.contains(notice))
            }
        }
    }

    @Test fun multipleWarningsKeepOrderAndDeduplicateExactCopies() {
        val result = LabelPreprocessor.preprocess(
            "sucre. ${notices[0]} ${notices[2]}; ${notices[3]}\n${notices[0]}"
        )
        assertEquals(listOf(notices[0], notices[2], notices[3]), result.crossContactWarnings)
    }

    @Test fun containsRemainsComposition() {
        val result = VeganAnalyzer.analyze("sucre (contient : lait)", database)
        assertEquals(AnalysisVerdict.VEGETARIAN, result.verdict)
        assertEquals(listOf("milk"), result.matched.map { it.id })
        assertTrue(result.crossContactWarnings.isEmpty())
    }

    @Test fun actualAnimalIngredientStillDeterminesVerdict() {
        val result = VeganAnalyzer.analyze("gélatine. Peut contenir du lait.", database)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertEquals(listOf("gelatin"), result.matched.map { it.id })
        assertEquals(listOf("Peut contenir du lait."), result.crossContactWarnings)
    }

    @Test fun warningAloneHasNoComposition() {
        val result = VeganAnalyzer.analyze("Peut contenir du lait.", database)
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, result.availability)
        assertEquals(null, result.verdict)
        assertTrue(result.matched.isEmpty())
        assertTrue(result.unknown.isEmpty())
        assertEquals(listOf("Peut contenir du lait."), result.crossContactWarnings)
    }

    @Test fun allVerdictsAndEarlyStopAreInvariant() {
        listOf("sucre", "sucre, lait", "sucre, E471", "sucre, mystère", "gélatine, sucre", "")
            .forEach { composition ->
                val baseline = VeganAnalyzer.analyze(composition, database)
                val result = VeganAnalyzer.analyze("$composition\nTraces : lait, gélatine, mystère", database)
                assertEquals(baseline, result.copy(crossContactWarnings = emptyList()))
                assertEquals(baseline.verdict, result.verdict)
                assertEquals(baseline.verdictWithoutUncertain, result.verdictWithoutUncertain)
            }
    }

    @Test fun allergenDeclarationIsOnlyADiagnosticNote() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics("sucre\n**Allergènes :** lait, gélatine", database)
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertTrue(diagnostics.crossContactWarnings.isEmpty())
        assertEquals(listOf("Allergènes : lait, gélatine"), diagnostics.excludedNotes)
        assertEquals(listOf("sugar"), diagnostics.result.matched.map { it.id })
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun completeRealTofuLabelKeepsNotesSeparateFromTokens() {
        val label = """Eau, graines de soja*, présure (nigari), chlorure de calcium. *Agriculture biologique.
            **Allergènes :** soja
            **Traces :** céleri, gluten, lupin, moutarde, fruits à coque, graines de sésame""".trimIndent()
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(label, database)
        assertEquals(listOf("Agriculture biologique.", "Allergènes : soja"), diagnostics.excludedNotes)
        assertEquals(listOf("Traces : céleri, gluten, lupin, moutarde, fruits à coque, graines de sésame"), diagnostics.crossContactWarnings)
        assertFalse(diagnostics.tokens.any { it.text.contains("lupin") || it.text.contains("biologique") })
        val report = DiagnosticReport.build(diagnostics, "0.5.6")
        assertTrue(report.contains("COMPOSITION APRÈS PRÉTRAITEMENT\n"))
        assertTrue(report.contains("TRACES / CONTAMINATION CROISÉE\n${diagnostics.crossContactWarnings.single()}"))
        assertTrue(report.contains("NOTES EXCLUES\nAgriculture biologique.\nAllergènes : soja"))
    }

    @Test fun certificationAndMarketingFooterAreRetainedAfterTraces() {
        val result = LabelPreprocessor.preprocess(
            "sucre. Peut contenir du lait.¹Rainforest Alliance Certified. Find out more at ra.org"
        )
        assertEquals(listOf("Peut contenir du lait."), result.crossContactWarnings)
        assertEquals(listOf("Rainforest Alliance Certified. Find out more at ra.org"), result.excludedNotes)
        assertFalse(result.compositionText.contains("Rainforest"))
    }

    @Test fun compositionFollowingAWarningIsStillAnalyzed() {
        val result = VeganAnalyzer.analyze("sucre. Peut contenir du lait. gélatine", database)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertEquals(listOf("Peut contenir du lait."), result.crossContactWarnings)
    }

    @Test fun displayIsInformationalAndAbsentWithoutWarnings() {
        assertEquals("", CrossContactNotice.format(emptyList()))
        val display = CrossContactNotice.format(notices)
        assertTrue(display.contains("⚠️ TRACES SIGNALÉES"))
        notices.forEach { assertTrue(display.contains("• $it")) }
        assertTrue(display.endsWith("Cette information n’est pas utilisée pour calculer le verdict."))
    }

    @Test fun parenthesesInsideWarningDoNotLeakIngredients() {
        val warning = "Peut contenir des fruits à coque (amandes, noix), lait, gélatine."
        val result = VeganAnalyzer.analyze("sucre. $warning", database)
        assertEquals(listOf(warning), result.crossContactWarnings)
        assertEquals(AnalysisVerdict.VEGAN, result.verdict)
        assertTrue(result.unknown.isEmpty())
        assertEquals(listOf("sugar"), result.matched.map { it.id })
    }

    @Test fun standaloneOrganicNoteAndConcentrateFootnoteArePreserved() {
        val result = LabelPreprocessor.preprocess("sucre\nAgriculture biologique.\n^concentré.")
        assertEquals(listOf("Agriculture biologique.", "^concentré."), result.excludedNotes)
        assertEquals("sucre\n\n", result.compositionText)
    }
}
