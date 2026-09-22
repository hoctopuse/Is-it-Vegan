package com.example.isitvegan

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.Rule
import org.junit.Test

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
        composeRule.waitForIdle()
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
    }

    @Test fun crossContactWarningIsDisplayedAtTheEndOfTheResult() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\nsucre. Peut contenir du lait.")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("⚠️ TRACES SIGNALÉES", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("• Peut contenir du lait.", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
    }

    @Test fun fullLabelModeDoesNotAnalyzeAProductNameAsIngredients() {
        composeRule.onNodeWithText("Étiquette").assertIsSelected()
        composeRule.onNodeWithText("Texte de l’étiquette").performTextInput("Pommes")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitForIdle()

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
        composeRule.waitForIdle()
        composeRule.onNodeWithText("✅ VEGAN", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test fun changingModeInvalidatesThePreviousResult() {
        composeRule.onNodeWithText("Texte de l’étiquette")
            .performTextInput("INGRÉDIENTS\neau, sucre")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Liste seule").performClick()
        composeRule.onNodeWithText("⚪ En attente d'analyse").assertIsDisplayed()
    }
}
