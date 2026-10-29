package com.example.isitvegan

import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
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
