package com.example.isitvegan

import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

@OptIn(ExperimentalTestApi::class)
class MainScreenInstrumentedTest {
    @Test fun freshOcrSessionCannotEraseTypedTextThroughFullTextChoice() {
        assertTypedTextSurvivesFullTextChoice(false)
    }

    @Test fun manualOcrSessionCannotEraseTypedTextThroughFullTextChoice() {
        assertTypedTextSurvivesFullTextChoice(true)
    }

    private fun assertTypedTextSurvivesFullTextChoice(manual: Boolean) {
        composeRule.onNodeWithText("OCR").performClick()
        if (manual) composeRule.onNodeWithTag("ocr-manual-entry").performScrollTo().performClick()
        val text = "INGREDIENTS: eau, sucre"
        composeRule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(text)
        val label = localizedContext(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
            .getString(R.string.full_ocr_text)
        if (composeRule.onAllNodes(hasText(label)).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText(label).performScrollTo().performClick()
        }
        composeRule.onNodeWithTag("ocr-editor").assertTextContains(text)
        composeRule.onAllNodes(hasText(label)).assertCountEquals(0)
    }
    private val wakeDeviceRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            UiLanguagePreferences.write(instrumentation.targetContext, UiLanguage.FR)
            val automation = instrumentation.uiAutomation
            listOf("svc power stayon true", "input keyevent 224", "wm dismiss-keyguard", "input keyevent 82")
                .forEach { command ->
                    ParcelFileDescriptor.AutoCloseInputStream(
                        automation.executeShellCommand(command)
                    ).use { it.readBytes() }
                }
            Thread.sleep(250)
        }
    }

    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(wakeDeviceRule).around(composeRule)

    @Test fun extractedZeroOptionsCanReturnToFullText() {
        val full = "[FR] eau, sucre\n[NL] water, suiker"
        extractInjectedText(full, 0)
        assertReturnToFullText(full)
    }

    @Test fun zeroOptionsFullTextCanBeRestoredAfterEditingAndAnalysis() {
        assertZeroOptionRestoration("[FR] eau, sucre\n[NL] water, suiker", selectFullTextFirst = true)
    }

    @Test fun fallbackFullTextCanBeRestoredAfterEditingAndAnalysis() {
        assertZeroOptionRestoration("eau, sucre", selectFullTextFirst = false)
    }

    private fun assertZeroOptionRestoration(full: String, selectFullTextFirst: Boolean) {
        val submitted = java.util.Collections.synchronizedList(mutableListOf<String>())
        extractInjectedText(full, 0, submitted)
        if (selectFullTextFirst) assertReturnToFullText(full)
        val edited = "INGREDIENTS: lait"
        composeRule.onNodeWithTag("ocr-editor").performScrollTo().performTextClearance()
        composeRule.onNodeWithTag("ocr-editor").performTextInput(edited)
        val resources = localizedContext(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        composeRule.onNodeWithText(resources.getString(R.string.full_ocr_text)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("VÉGÉTARIEN", substring = true), 30_000)
        assertEquals(listOf(edited), submitted.toList())
        assertReturnToFullText(full)
        composeRule.onAllNodes(hasText("VÉGÉTARIEN", substring = true)).assertCountEquals(0)
        composeRule.onAllNodes(hasText(resources.getString(R.string.multilingual_stale_result))).assertCountEquals(0)
    }

    @Test fun extractedOneOptionCanReturnToFullText() {
        val full = "FR Ingrédients: eau, sucre, sel, tomate, farine.\n[NL] water, suiker"
        extractInjectedText(full, 1)
        composeRule.onNodeWithText("FR").assertIsSelected()
        assertReturnToFullText(full)
    }

    @Test fun extractedRepeatedOccurrencesAnalyzeCurrentChoiceAndInvalidateOldVerdict() {
        val full = "FR Ingrédients: eau, sucre, sel, tomate, farine.\nFR Ingrédients: lait, sucre, sel, tomate, farine."
        val submitted = java.util.Collections.synchronizedList(mutableListOf<String>())
        extractInjectedText(full, 2, submitted)
        composeRule.onNodeWithText("FR · 1").performScrollTo().performClick().assertIsSelected()
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("VEGAN", substring = true), 30_000)
        composeRule.onNodeWithText("FR · 2").performScrollTo().performClick()
        composeRule.onNodeWithText("FR · 2").assertIsSelected()
        composeRule.onAllNodes(hasText("VEGAN", substring = true)).assertCountEquals(0)
        composeRule.onNodeWithTag("ocr-editor").assertTextContains("Ingrédients : lait, sucre, sel, tomate, farine.")
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("VÉGÉTARIEN", substring = true), 30_000)
        assertEquals(listOf("Ingrédients : eau, sucre, sel, tomate, farine.",
            "Ingrédients : lait, sucre, sel, tomate, farine."), submitted.toList())
        assertReturnToFullText(full)
        composeRule.onAllNodes(hasText("VÉGÉTARIEN", substring = true)).assertCountEquals(0)
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntil(30_000) { submitted.size == 3 }
        composeRule.waitUntilAtLeastOneExists(hasText("Sélectionnez", substring = true), 30_000)
        assertEquals(full, submitted.last())
    }

    @Test fun extractedIdenticalOccurrencesKeepDistinctSelectedChips() {
        val full = "FR Ingrédients: eau, sucre\nFR Ingrédients: eau, sucre"
        extractInjectedText(full, 2)
        composeRule.onNodeWithText("FR · 1").assertIsSelected()
        composeRule.onNodeWithText("FR · 2").performScrollTo().performClick()
        composeRule.onNodeWithText("FR · 2").assertIsSelected()
        composeRule.onNodeWithTag("ocr-editor").assertTextContains("Ingrédients : eau, sucre")
        assertReturnToFullText(full)
    }

    private fun extractInjectedText(full: String, optionCount: Int, submitted: MutableList<String> = mutableListOf()) {
        composeRule.waitUntil(30_000) { VeganAnalyzer.isDatabaseLoaded() }
        val segmentation = LabelLanguageSegmenter.segment(full, UiLanguage.FR)
        val selection = OcrTextSelection.from(segmentation, full)
        assertEquals(optionCount, selection.options.size)
        val processing = OcrProcessingResult(full, selection.editableText,
            selection.updateDiagnostics(OcrDiagnostics(0, 1, 2, emptyList(), emptyList(),
                languageSegmentCount = segmentation.blocks.size)), fullText = full,
            textOptions = selection.options, selectedOptionLanguage = selection.selectedLanguage,
            selectedOptionBlockId = selection.selectedBlockId, selectedOptionSegmentId = selection.selectedSegmentId,
            languageBlocks = segmentation.blocks)
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                OcrFirstScreenContent(true,
                    pickPhoto = { it(android.net.Uri.parse("content://test/b2")) },
                    decodePreview = { android.graphics.Bitmap.createBitmap(2, 2, android.graphics.Bitmap.Config.ARGB_8888) },
                    processOcr = { _, _, success, _, _ -> success(processing) },
                    analyzeText = { text, mode, language, block ->
                        submitted.add(text)
                        VeganAnalyzer.analyzeWithDiagnostics(text, mode, language, block)
                    })
            }
        }
        composeRule.onNodeWithText("OCR").performClick()
        val resources = localizedContext(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        composeRule.onNodeWithText(resources.getString(R.string.choose_photo)).performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("EXTRAIRE LE TEXTE").and(isEnabled()), 30_000)
        composeRule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        composeRule.onNodeWithTag("ocr-editor").assertTextContains(selection.editableText)
    }

    private fun assertReturnToFullText(full: String) {
        val label = localizedContext(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
            .getString(R.string.full_ocr_text)
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        composeRule.onNodeWithTag("ocr-editor").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(full))
        )
    }

    @Test fun damagedHeadingDoesNotOfferAProbableBlock() {
        val text = "INGREDIENTE.\nSuiker, tarwebloem, fructosestroop.\nZUTATEN: Zucker, Milch\nINGREDIENTS: sugar, milk"
        val options = OcrTextSelection.from(LabelLanguageSegmenter.segment(text), text).options
        var selected: String? = null
        val resources = localizedContext(
            InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR
        ).resources
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                OcrTextBlockChoices(
                    options, options.single { it.language == LabelLanguage.GERMAN }.segmentId, false,
                    choiceLabel = resources.getString(R.string.ocr_block_choice),
                    probableLabel = resources.getString(R.string.ocr_probable),
                    probableHint = resources.getString(R.string.ocr_probable_hint),
                    fullTextLabel = resources.getString(R.string.full_ocr_text),
                    onSelectOption = { selected = it }, onSelectFullText = {}
                )
            }
        }
        composeRule.onAllNodes(hasText("NL · probable")).assertCountEquals(0)
        val german = options.single { it.language == LabelLanguage.GERMAN }
        composeRule.onNodeWithText("DE").assertIsDisplayed().performClick()
        composeRule.onNodeWithText(resources.getString(R.string.full_ocr_text)).assertIsDisplayed()
        assertEquals(german.segmentId, selected)
    }

    @Test fun oneOcrOptionKeepsLanguageAndFullTextChoicesVisible() {
        val text = "INGREDIENTS: water, sugar, salt, tomato, flour"
        val options = OcrTextSelection.from(LabelLanguageSegmenter.segment(text), text).options
        assertEquals(1, options.size)
        var selectedFullText = false
        val resources = localizedContext(
            InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR
        ).resources
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                OcrTextBlockChoices(
                    options, options.single().segmentId, false,
                    choiceLabel = resources.getString(R.string.ocr_block_choice),
                    probableLabel = resources.getString(R.string.ocr_probable),
                    probableHint = resources.getString(R.string.ocr_probable_hint),
                    fullTextLabel = resources.getString(R.string.full_ocr_text),
                    onSelectOption = {}, onSelectFullText = { selectedFullText = true }
                )
            }
        }
        composeRule.onNodeWithText("EN").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText(resources.getString(R.string.full_ocr_text)).assertIsDisplayed().performClick()
        assertEquals(true, selectedFullText)
    }

    @Test fun noSelectableOcrOptionsStillAllowsChoosingTheFullText() {
        var selectedFullText = false
        val resources = localizedContext(
            InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR
        ).resources
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                OcrTextBlockChoices(
                    emptyList(), null, false,
                    choiceLabel = resources.getString(R.string.ocr_block_choice),
                    probableLabel = resources.getString(R.string.ocr_probable),
                    probableHint = resources.getString(R.string.ocr_probable_hint),
                    fullTextLabel = resources.getString(R.string.full_ocr_text),
                    onSelectOption = {}, onSelectFullText = { selectedFullText = true }
                )
            }
        }
        composeRule.onNodeWithText(resources.getString(R.string.full_ocr_text)).assertIsDisplayed().performClick()
        assertEquals(true, selectedFullText)
    }

    @Test fun repeatedSameLanguageOcrOptionsAreIndividuallyVisible() {
        val text = "FR Ingrédients: eau\nFR Ingrédients: eau, sucre, sel, tomate, farine de blé."
        val options = OcrTextSelection.from(LabelLanguageSegmenter.segment(text, UiLanguage.FR), text).options
        assertEquals(2, options.size)
        var selected: String? = null
        val resources = localizedContext(
            InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR
        ).resources
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                OcrTextBlockChoices(
                    options, options.first().segmentId, false,
                    choiceLabel = resources.getString(R.string.ocr_block_choice),
                    probableLabel = resources.getString(R.string.ocr_probable),
                    probableHint = resources.getString(R.string.ocr_probable_hint),
                    fullTextLabel = resources.getString(R.string.full_ocr_text),
                    onSelectOption = { selected = it }, onSelectFullText = {}
                )
            }
        }
        composeRule.onNodeWithText("FR · 1").assertIsDisplayed().assertIsSelected().performClick()
        composeRule.onNodeWithText("FR · 2").assertIsDisplayed().performClick()
        assertEquals(options.last().segmentId, selected)
        composeRule.onNodeWithText(resources.getString(R.string.full_ocr_text)).assertIsDisplayed()
    }

    @Test fun multilingualCorrectionReachesTheCallerOnlyAfterExplicitAccept() {
        val unknown = IngredientOccurrence("en:1", "en-block", LabelLanguage.ENGLISH, "WHEAT lour", 1)
        val known = IngredientOccurrence(
            "nl:1", "nl-block", LabelLanguage.DUTCH, "tarwebloem", 1,
            canonicalId = "wheat_flour", status = VeganStatus.VEGAN
        )
        val proposal = IngredientAlignmentResult(
            unknown, known, AlignmentKind.CORRECTION_PROPOSAL, "wheat_flour", "Farine de blé"
        )
        var acceptedText: String? = null
        var dismissed = false
        composeRule.runOnUiThread {
            composeRule.activity.setContent { MultilingualAlignmentReview(
                MultilingualAlignmentEvaluation(listOf(unknown, known), listOf(proposal)),
                "Ingredients: WHEAT lour",
                emptySet(),
                InstrumentationRegistry.getInstrumentation().targetContext.resources,
                onAccept = { _, text -> acceptedText = text },
                onDismiss = { dismissed = true },
                applicableProposalKeys = setOf(proposal.proposalKey)
            ) }
        }

        composeRule.onNodeWithTag("multilingual-correction-text").performTextClearance()
        composeRule.onNodeWithTag("multilingual-correction-text").performTextInput("WHEAT flour")
        assertEquals(null, acceptedText)
        assertEquals(false, dismissed)
        composeRule.onNodeWithTag("multilingual-correction-accept").performClick()
        assertEquals("WHEAT flour", acceptedText)
        assertEquals(false, dismissed)
    }

    @Test fun ambiguousReviewIsCollapsedAndCanBeExpandedWithoutAcceptingAnything() {
        val links = (1..30).map { index ->
            IngredientAlignmentResult(
                IngredientOccurrence("de:$index", "de", LabelLanguage.GERMAN, "DETERM$index", index),
                IngredientOccurrence("en:$index", "en", LabelLanguage.ENGLISH, "ENTERM$index", index),
                AlignmentKind.AMBIGUOUS
            )
        }
        var accepted = false
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MultilingualAlignmentReview(
                        MultilingualAlignmentEvaluation(links.flatMap { listOf(it.first, it.second) }, links),
                        "texte", emptySet(), InstrumentationRegistry.getInstrumentation().targetContext.resources,
                        onAccept = { _, _ -> accepted = true }, onDismiss = {}
                    )
                }
            }
        }
        composeRule.waitUntilAtLeastOneExists(androidx.compose.ui.test.hasTestTag("multilingual-ambiguous-toggle"), 30_000)
        val showLabel = InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getString(R.string.multilingual_ambiguous_show, 30)
        composeRule.onNodeWithText(showLabel).performScrollTo().assertIsDisplayed()
        composeRule.onAllNodes(hasText("DETERM1", substring = true)).assertCountEquals(0)
        composeRule.onNodeWithTag("multilingual-ambiguous-toggle").performClick()
        composeRule.onNodeWithText("DETERM1 (DE) ↔ ENTERM1 (EN)").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("multilingual-ambiguous-toggle").performScrollTo().performClick()
        composeRule.onAllNodes(hasText("DETERM1", substring = true)).assertCountEquals(0)
        assertEquals(false, accepted)
    }

    @Test fun unmatchedTermsCanBeReviewedWithoutChangingText() {
        val unknown = IngredientOccurrence("en:1", "en", LabelLanguage.ENGLISH, "mystery", 1)
        val known = IngredientOccurrence("nl:1", "nl", LabelLanguage.DUTCH, "suiker", 1, canonicalId = "sugar")
        var accepted = false
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MultilingualAlignmentReview(MultilingualAlignmentEvaluation(listOf(unknown, known), emptyList()),
                        "mystery", emptySet(), resources, onAccept = { _, _ -> accepted = true }, onDismiss = {})
                }
            }
        }
        composeRule.waitUntilAtLeastOneExists(androidx.compose.ui.test.hasTestTag("multilingual-unpaired-toggle"), 30_000)
        composeRule.onAllNodes(hasText("mystery", substring = true)).assertCountEquals(0)
        composeRule.onNodeWithTag("multilingual-unpaired-toggle").performClick()
        composeRule.onNodeWithText(resources.getString(R.string.multilingual_unpaired_unknown, "mystery", "EN")).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(resources.getString(R.string.multilingual_unpaired_known, "suiker", "NL")).performScrollTo().assertIsDisplayed()
        assertEquals(false, accepted)
    }

    @Test fun manualEntryClearsPreviousOcrVerdict() {
        composeRule.onNodeWithText("OCR").performClick()
        composeRule.onNodeWithTag("ocr-editor").performTextInput("INGREDIENTS: eau, sucre")
        composeRule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 30_000)
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("VEGAN", substring = true), 30_000)
        composeRule.onNodeWithTag("ocr-manual-entry").performScrollTo().performClick()
        composeRule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput("INGREDIENTS: lait")
        composeRule.onAllNodes(hasText("VEGAN", substring = true)).assertCountEquals(0)
    }

    @Test fun failedReanalysisShowsErrorAndHidesOldVerdict() {
        composeRule.waitUntil(30_000) { VeganAnalyzer.isDatabaseLoaded() }
        composeRule.runOnUiThread {
            composeRule.activity.setContent {
                OcrFirstScreenContent(true) { text, mode, language, block ->
                    if (text.contains("FAILTEST")) throw IllegalStateException("technical detail")
                    VeganAnalyzer.analyzeWithDiagnostics(text, mode, language, block)
                }
            }
        }
        composeRule.onNodeWithText("OCR").performClick()
        composeRule.onNodeWithTag("ocr-editor").performTextInput("INGREDIENTS: eau, sucre")
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("VEGAN", substring = true), 30_000)
        composeRule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(", FAILTEST")
        composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(androidx.compose.ui.test.hasTestTag("analysis-error"), 30_000)
        composeRule.onNodeWithTag("analysis-error").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodes(hasText("VEGAN", substring = true)).assertCountEquals(0)
        composeRule.onAllNodes(hasText("technical detail", substring = true)).assertCountEquals(0)
    }

    @Test fun pageCanScrollToItsFooter() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        composeRule.onNodeWithText("Version $versionName")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test fun analysisAutomaticallyBringsTheResultIntoView() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\neau, sucre")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 10_000)
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
    }

    @Test fun analysisClickUsesTheEditedOcrFieldValue() {
        val initialOcrText = "INGRÉDIENTS\nZZZRAWONLYQZX"
        val editedText = "INGRÉDIENTS\neau, sucre"

        composeRule.onNodeWithText("OCR").performClick()
        composeRule.onNodeWithText("Texte éditable à analyser").performTextInput(initialOcrText)
        composeRule.onNodeWithText("Texte éditable à analyser").performTextClearance()
        composeRule.onNodeWithText("Texte éditable à analyser").performTextInput(editedText)

        // One real UI click; the displayed result must reflect the edited known ingredients.
        composeRule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 30_000)
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
        composeRule.onAllNodes(hasText("ZZZRAWONLYQZX", substring = true)).assertCountEquals(0)
    }

    @Test fun crossContactWarningIsDisplayedAtTheEndOfTheResult() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\nsucre. Peut contenir du lait.")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("⚠️ TRACES SIGNALÉES", substring = true), 10_000)
        composeRule.onNodeWithText("⚠️ TRACES SIGNALÉES", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("• Peut contenir du lait.", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
    }

    @Test fun interleavedDeclaredPresenceAndTracesReachTheRenderedResult() {
        val editor = composeRule.onNodeWithText("Texte de l’étiquette")
        for ((term, verdict) in listOf("lait" to "VÉGÉTARIEN", "gelatine" to "NON VEGAN", "mystere" to "INCONCLUS")) {
            editor.performScrollTo().performTextClearance()
            editor.performTextInput("Ingredients: eau. Contient: soja. Peut contenir: noix Contient: $term.")
            composeRule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 30_000)
            composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            composeRule.waitUntilAtLeastOneExists(hasText(verdict, substring = true), 30_000)
            composeRule.onNodeWithText(verdict, substring = true).performScrollTo().assertIsDisplayed()
            val explanation = if (term == "mystere") "Ingrédients non identifiés" else "Causes connues incompatibles"
            composeRule.onNode(hasText(explanation, substring = true).and(hasText(term, substring = true, ignoreCase = true)))
                .performScrollTo().assertIsDisplayed()
            composeRule.onNode(hasText(verdict, substring = true).and(hasText("Peut contenir: noix", substring = true)))
                .performScrollTo().assertIsDisplayed()
            composeRule.onAllNodes(hasText(verdict, substring = true).and(hasText("Peut contenir: noix Contient:", substring = true))).assertCountEquals(0)
        }
    }

    @Test fun embeddedTraceResumptionKeepsMilkInRenderedVerdict() {
        val editor = composeRule.onNodeWithText("Texte de l’étiquette")
        for (text in listOf(
            "Ingredients: eau [Peut contenir: noix], lait.",
            "Ingredients: eau (Peut contenir: noix) - lait."
        )) {
            editor.performScrollTo().performTextClearance()
            editor.performTextInput(text)
            composeRule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 30_000)
            composeRule.onNodeWithText("ANALYSER").performScrollTo().performClick()
            composeRule.waitUntilAtLeastOneExists(hasText("VÉGÉTARIEN", substring = true), 30_000)
            val result = hasText("VÉGÉTARIEN", substring = true).and(hasText("Peut contenir: noix", substring = true))
            composeRule.onNode(result.and(hasText("lait", substring = true))).performScrollTo().assertIsDisplayed()
            composeRule.onAllNodes(result.and(hasText("Peut contenir: noix], lait", substring = true))).assertCountEquals(0)
        }
    }

    @Test fun fullLabelModeDoesNotAnalyzeAProductNameAsIngredients() {
        composeRule.onNodeWithText("Étiquette").assertIsSelected()
        composeRule.onNodeWithText("Texte de l’étiquette").performTextInput("Pommes")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(
            hasText("AUCUNE LISTE D’INGRÉDIENTS DÉTECTÉE", substring = true),
            10_000
        )

        composeRule.onNodeWithText("AUCUNE LISTE D’INGRÉDIENTS DÉTECTÉE", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Aucune conclusion vegan n’est produite.", substring = true)
            .assertIsDisplayed()
    }

    @Test fun inputModeDefaultsToFullLabelAndUpdatesItsHelp() {
        composeRule.onNodeWithText("Étiquette").assertIsSelected()
        composeRule.onNodeWithText("Texte de l’étiquette").performClick()
        composeRule.onNodeWithText("Collez le texte complet de l’étiquette")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Liste seule").performClick()
        composeRule.onNodeWithText("Liste seule").assertIsSelected()
        composeRule.onNodeWithText("Collez uniquement la liste des ingrédients")
            .assertIsDisplayed()

        composeRule.onNodeWithText("OCR").performClick()
        composeRule.onNodeWithText("Texte éditable à analyser")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Corrigez ou saisissez le texte de l’étiquette")
            .assertIsDisplayed()
    }

    @Test fun fullLabelAcceptsAnIngredientHeadingWithoutAColon() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\neau, sucre, sel")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 10_000)
        composeRule.onNodeWithText("✅ VEGAN", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test fun uncertainVerdictShowsTheConditionalResultWithoutChangingTheMainVerdict() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\nsucre, E471")
        composeRule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 10_000)
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("⚠️ INCERTAIN", substring = true), 10_000)
        composeRule.onNodeWithText("⚠️ INCERTAIN", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Selon les ingrédients au statut établi : VEGAN", substring = true)
            .assertIsDisplayed()
    }

    @Test fun nonVeganVerdictAlsoShowsUncertainIngredientsWithoutConditionalResult() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\ngélatine, E471")
        composeRule.waitUntilAtLeastOneExists(hasText("ANALYSER").and(isEnabled()), 10_000)
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("❌ NON VEGAN", substring = true), 10_000)

        composeRule.onNodeWithText("❌ NON VEGAN", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Causes connues incompatibles", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Ingrédients incertains", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Selon les ingrédients au statut établi : VEGAN", substring = true)
            .assertDoesNotExist()
    }

    @Test fun changingModeInvalidatesThePreviousResult() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\neau, sucre")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 10_000)
        composeRule.onNodeWithText("Liste seule").performClick()
        composeRule.onNodeWithText("⚪ En attente d'analyse").assertIsDisplayed()
    }
}
