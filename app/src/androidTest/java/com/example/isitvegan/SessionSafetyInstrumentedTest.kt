package com.example.isitvegan

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import android.net.Uri
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SessionSafetyInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val service = IngredientAnalysisService(IngredientKnowledge(listOf(
        Ingredient("water", "eau", listOf("eau"), status = VeganStatus.VEGAN, reason = "test"),
        Ingredient("tomato", "tomate", listOf("tomate"), status = VeganStatus.VEGAN, reason = "test")
    )))

    private fun screen(session: OcrSession = OcrSession(), onSettled: (() -> Unit)? = null, analyzer: (String, InputMode, UiLanguage, String?) -> AnalysisDiagnostics = service::analyzeWithDiagnostics) {
        UiLanguagePreferences.write(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        rule.runOnUiThread { rule.activity.setContent { OcrFirstScreenContent(true, session, onAnalysisSettled = onSettled, analyzeText = analyzer) } }
    }

    @Test fun manualEditHidesOldVerdictUntilReanalysis() {
        screen()
        rule.onNodeWithText("Texte de l’étiquette").performTextInput("Ingrédients: eau")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
        rule.onNodeWithText("Texte de l’étiquette").performScrollTo().performTextInput(", mystère")
        rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("INCONCLUS", substring = true), 30_000)
    }

    @Test fun manualListEditHidesOldVerdictUntilReanalysis() {
        screen()
        rule.onNodeWithText("Liste seule").performClick()
        rule.onNodeWithText("Liste d’ingrédients").performTextInput("eau")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
        rule.onNodeWithText("Liste d’ingrédients").performScrollTo().performTextInput(", mystère")
        rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("INCONCLUS", substring = true), 30_000)
    }

    @Test fun oldAnalysisCannotPublishAfterClearAndIdenticalNewText() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val settled = CountDownLatch(1)
        screen(onSettled = { settled.countDown() }) { text, mode, language, block ->
            started.countDown()
            check(release.await(90, TimeUnit.SECONDS))
            service.analyzeWithDiagnostics(text, mode, language, block)
        }
        try {
            rule.onNodeWithText("Texte de l’étiquette").performTextInput("Ingrédients: eau")
            rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            assertTrue(started.await(10, TimeUnit.SECONDS))
            rule.onNodeWithText("EFFACER").performScrollTo().performClick()
            rule.onNodeWithText("Texte de l’étiquette").performScrollTo().performTextInput("Ingrédients: eau")
            release.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 30_000)
            rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        } finally { release.countDown() }
    }

    @Test fun newerAnalysisRemainsWhenOlderAnalysisFinishesLast() {
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val settled = CountDownLatch(2)
        val calls = AtomicInteger()
        screen(onSettled = { settled.countDown() }) { text, mode, language, block ->
            if (calls.incrementAndGet() == 1) {
                firstStarted.countDown()
                check(releaseFirst.await(90, TimeUnit.SECONDS))
                service.analyzeWithDiagnostics(text, mode, language, block)
            } else {
                // A distinct synthetic response makes publication order observable even with identical text.
                service.analyzeWithDiagnostics("Ingrédients: tomate inhabituelle", mode, language, block)
            }
        }
        try {
            rule.onNodeWithText("Texte de l’étiquette").performTextInput("Ingrédients: eau")
            rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            assertTrue(firstStarted.await(10, TimeUnit.SECONDS))
            rule.onNodeWithText("EFFACER").performScrollTo().performClick()
            rule.onNodeWithText("Texte de l’étiquette").performScrollTo().performTextInput("Ingrédients: eau")
            rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            rule.waitUntilAtLeastOneExists(hasText("INCONCLUS", substring = true), 30_000)
            releaseFirst.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.onNodeWithText("INCONCLUS", substring = true).performScrollTo().assertIsDisplayed()
            rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        } finally { releaseFirst.countDown() }
    }

    @Test fun lateAnalysisFailureDoesNotShowAnErrorInNewSession() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val settled = CountDownLatch(1)
        screen(onSettled = { settled.countDown() }) { _, _, _, _ ->
            started.countDown()
            check(release.await(90, TimeUnit.SECONDS))
            throw IllegalStateException("old failure")
        }
        try {
            rule.onNodeWithText("Texte de l’étiquette").performTextInput("Ingrédients: eau")
            rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            assertTrue(started.await(10, TimeUnit.SECONDS))
            rule.onNodeWithText("EFFACER").performScrollTo().performClick()
            rule.onNodeWithText("Texte de l’étiquette").performScrollTo().performTextInput("Ingrédients: eau")
            release.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.onAllNodes(hasTestTag("analysis-error")).assertCountEquals(0)
        } finally { release.countDown() }
    }

    @Test fun unresolvedPartialIngredientIsVisibleInRenderedVerdict() {
        screen()
        rule.onNodeWithText("Liste seule").performClick()
        rule.onNodeWithText("Liste d’ingrédients").performTextInput("tomate inhabituelle. Peut contenir : lait.")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("INCONCLUS", substring = true), 30_000)
        rule.onNode(hasText("Ingrédients non identifiés", substring = true).and(hasText("inhabituelle", substring = true)))
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun unknownDeclaredPresenceRemainsVisibleWithoutIngredientList() {
        screen()
        rule.onNodeWithText("Texte de l’étiquette").performTextInput("Contient: mystère. Peut contenir: lait.")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("AUCUNE LISTE", substring = true), 30_000)
        rule.onNode(hasText("Ingrédients non identifiés", substring = true).and(hasText("mystère", substring = true)))
            .performScrollTo().assertIsDisplayed()
        rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
    }

    @Test fun revertingOcrEditKeepsVerdictStaleUntilReanalysis() {
        screen(OcrSession(editableText = "Ingrédients: eau"))
        rule.onNodeWithText("OCR").performClick()
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
        rule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(", lait")
        rule.onNodeWithTag("ocr-editor").performTextClearance()
        rule.onNodeWithTag("ocr-editor").performTextInput("Ingrédients: eau")
        rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        val resources = localizedContext(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR).resources
        rule.onNodeWithText(resources.getString(R.string.multilingual_stale_result)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
    }

    @Test fun oldAnalysisCannotPublishAfterLanguageChange() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val settled = CountDownLatch(1)
        screen(onSettled = { settled.countDown() }) { text, mode, language, block ->
            started.countDown()
            check(release.await(90, TimeUnit.SECONDS))
            service.analyzeWithDiagnostics(text, mode, language, block)
        }
        try {
            rule.onNodeWithText("Texte de l’étiquette").performTextInput("Ingrédients: eau")
            rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            assertTrue(started.await(10, TimeUnit.SECONDS))
            rule.onNodeWithText("English").performScrollTo().performClick()
            release.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.waitUntilAtLeastOneExists(hasText("ANALYZE").and(isEnabled()), 30_000)
            rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        } finally { release.countDown() }
    }

    @Test fun oldAnalysisCannotPublishForAnotherBlockWithIdenticalText() {
        val text = "Ingrédients: eau"
        val options = listOf(
            OcrTextOption(LabelLanguage.FRENCH, text, "FR", blockId = "fr", segmentId = "fr"),
            OcrTextOption(LabelLanguage.ENGLISH, text, "EN", blockId = "en", segmentId = "en")
        )
        val session = OcrSession(editableText = text, textOptions = options,
            selectedOptionLanguage = LabelLanguage.FRENCH, selectedOptionBlockId = "fr")
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val settled = CountDownLatch(1)
        screen(session, onSettled = { settled.countDown() }) { submitted, mode, language, block ->
            started.countDown()
            check(release.await(90, TimeUnit.SECONDS))
            service.analyzeWithDiagnostics(submitted, mode, language, block)
        }
        try {
            rule.onNodeWithText("OCR").performClick()
            rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            assertTrue(started.await(10, TimeUnit.SECONDS))
            rule.onNodeWithText("EN").performScrollTo().performClick()
            release.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 30_000)
            rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        } finally { release.countDown() }
    }

    @Test fun previousAnalysisErrorDoesNotFollowBlockSelection() {
        val text = "Ingrédients: eau"
        val options = listOf(
            OcrTextOption(LabelLanguage.FRENCH, text, "FR", blockId = "fr", segmentId = "fr"),
            OcrTextOption(LabelLanguage.ENGLISH, text, "EN", blockId = "en", segmentId = "en")
        )
        val session = OcrSession(editableText = text, textOptions = options,
            selectedOptionLanguage = LabelLanguage.FRENCH, selectedOptionBlockId = "fr")
        screen(session) { _, _, _, _ -> throw IllegalStateException("failed") }
        rule.onNodeWithText("OCR").performClick()
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasTestTag("analysis-error"), 30_000)
        rule.onNodeWithText("EN").performScrollTo().performClick()
        rule.onAllNodes(hasTestTag("analysis-error")).assertCountEquals(0)
    }

    @Test fun lateOcrCannotReplaceClearedSession() {
        lateinit var deliver: (OcrProcessingResult) -> Unit
        UiLanguagePreferences.write(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        rule.runOnUiThread {
            rule.activity.setContent {
                OcrFirstScreenContent(true, initialSourceUri = Uri.parse("content://missing/test"),
                    processOcr = { _, _, success, _, _ -> deliver = success },
                    analyzeText = service::analyzeWithDiagnostics)
            }
        }
        rule.onNodeWithText("OCR").performClick()
        rule.waitUntilAtLeastOneExists(hasText("EXTRAIRE LE TEXTE").and(isEnabled()), 30_000)
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.onNodeWithText("EFFACER").performScrollTo().performClick()
        rule.runOnUiThread {
            deliver(OcrProcessingResult("Ingrédients: eau", "Ingrédients: eau", OcrDiagnostics(0, 1, 1, listOf("FR"), emptyList())))
        }
        rule.onNodeWithTag("ocr-editor").performScrollTo().assert(!hasText("Ingrédients: eau", substring = true))
    }

    @Test fun newerOcrWinsWhenCallbacksArriveOutOfOrder() {
        val callbacks = mutableListOf<Pair<(OcrProcessingResult) -> Unit, (String) -> Unit>>()
        UiLanguagePreferences.write(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        rule.runOnUiThread {
            rule.activity.setContent {
                OcrFirstScreenContent(true, initialSourceUri = Uri.parse("content://missing/test"),
                    processOcr = { _, _, success, failure, _ -> callbacks += success to failure },
                    analyzeText = service::analyzeWithDiagnostics)
            }
        }
        rule.onNodeWithText("OCR").performClick()
        rule.waitUntilAtLeastOneExists(hasText("EXTRAIRE LE TEXTE").and(isEnabled()), 30_000)
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput("édition")
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.runOnUiThread {
            fun result(text: String) = OcrProcessingResult(text, text, OcrDiagnostics(0, 1, 1, listOf("FR"), emptyList()))
            callbacks[1].first(result("Ingrédients: nouveau"))
            callbacks[0].first(result("Ingrédients: ancien"))
            callbacks[0].second("Ancienne erreur OCR")
        }
        rule.onNodeWithTag("ocr-editor").performScrollTo().assert(hasText("Ingrédients: nouveau", substring = true))
        rule.onNodeWithTag("ocr-editor").assert(!hasText("Ingrédients: ancien", substring = true))
        rule.onAllNodes(hasText("Ancienne erreur OCR")).assertCountEquals(0)
    }
}
