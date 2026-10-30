package com.example.isitvegan

import androidx.activity.compose.setContent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class OcrChosenTextInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val original = "Ingrédients: eau, sucre"
    private val id = stableBlockId(original)
    private val full = "$original\nZUTATEN: MILCH"
    private val service = IngredientAnalysisService(IngredientKnowledge(listOf(
        Ingredient("water", "eau", listOf("eau"), status = VeganStatus.VEGAN, reason = "test"),
        Ingredient("sugar", "sucre", listOf("sucre"), status = VeganStatus.VEGAN, reason = "test"),
        Ingredient("milk", "MILCH", listOf("MILCH"), status = VeganStatus.NON_VEGAN, reason = "test")
    )))

    private fun showScreen(onResult: (AnalysisDiagnostics) -> Unit) {
        val options = listOf(
            OcrTextOption(LabelLanguage.FRENCH, original, "FR", blockId = id, segmentId = "fr"),
            OcrTextOption(LabelLanguage.GERMAN, "ZUTATEN: MILCH", "DE", blockId = "de", segmentId = "de")
        )
        val session = OcrSession(editableText = original, fullOcrText = full, textOptions = options,
            selectedOptionLanguage = LabelLanguage.FRENCH, selectedOptionBlockId = id)
        UiLanguagePreferences.write(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        rule.runOnUiThread {
            rule.activity.setContent {
                OcrFirstScreenContent(true, session) { text, mode, language, block ->
                    service.analyzeWithDiagnostics(text, mode, language, block).also(onResult)
                }
            }
        }
        rule.onNodeWithText("OCR").performClick()
    }

    @Test fun replacingChosenBlockWithSeveralListsShowsWarning() {
        var received: AnalysisDiagnostics? = null
        showScreen { received = it }
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("VEGAN", substring = true), 30_000)
        rule.onNodeWithTag("ocr-editor").performScrollTo().performTextClearance()
        rule.onNodeWithTag("ocr-editor").performTextInput("$original, mystère\nZUTATEN: MILCH")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("Le texte contient plusieurs listes", substring = true), 30_000)
        rule.onNodeWithText("Le texte contient plusieurs listes", substring = true).performScrollTo().assertIsDisplayed()
        val result = requireNotNull(received)
        assertEquals("$original, mystère\nZUTATEN: MILCH", result.input)
        assertEquals(result.input, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.ambiguousOcrText)
        assertNull(result.result.verdict)
    }

    @Test fun analyzeEditAndReanalyzeOneListUsesCurrentText() {
        var received: AnalysisDiagnostics? = null
        showScreen { received = it }
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("VEGAN", substring = true), 30_000)
        rule.onNodeWithTag("ocr-editor").performScrollTo().performTextClearance()
        rule.onNodeWithTag("ocr-editor").performTextInput("$original, MILCH, mystère")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("NON VEGAN", substring = true), 30_000)
        rule.onNodeWithText("NON VEGAN", substring = true).performScrollTo().assertIsDisplayed()
        val result = requireNotNull(received)
        assertEquals("$original, MILCH, mystère", result.input)
        assertEquals(result.input, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.result.unknown.any { it.contains("mystère") })
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.result.verdict)
    }

    @Test fun fullTextShowsAmbiguityInsteadOfVeganVerdict() {
        var received: AnalysisDiagnostics? = null
        showScreen { received = it }
        rule.onNodeWithText("Texte complet").performScrollTo().performClick()
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("Le texte contient plusieurs listes", substring = true), 30_000)
        rule.onNodeWithText("Le texte contient plusieurs listes", substring = true).performScrollTo().assertIsDisplayed()
        val result = requireNotNull(received)
        assertEquals(full, result.input)
        assertEquals(full, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertNull(result.result.verdict)
    }
}
