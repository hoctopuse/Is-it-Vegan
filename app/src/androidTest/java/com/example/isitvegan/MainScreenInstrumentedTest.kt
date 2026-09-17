package com.example.isitvegan

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class MainScreenInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun pageCanScrollToItsFooter() {
        val versionName = InstrumentationRegistry.getInstrumentation()
            .targetContext.packageManager
            .getPackageInfo(InstrumentationRegistry.getInstrumentation().targetContext.packageName, 0)
            .versionName
        composeRule.onNodeWithText("Version $versionName")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test fun analysisAutomaticallyBringsTheResultIntoView() {
        composeRule.onNodeWithText("Ingrédients").performTextInput("eau, sucre")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
    }

    @Test fun crossContactWarningIsDisplayedAtTheEndOfTheResult() {
        composeRule.onNodeWithText("Ingrédients")
            .performTextInput("sucre. Peut contenir du lait.")
        composeRule.onNodeWithText("ANALYSER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("⚠️ TRACES SIGNALÉES", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("• Peut contenir du lait.", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("✅ VEGAN", substring = true).assertIsDisplayed()
    }
}
