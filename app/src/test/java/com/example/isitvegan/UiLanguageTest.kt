package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiLanguageTest {
    @Test fun supportedDeviceLanguagesUseTheirMatchingInterface() {
        assertEquals(UiLanguage.FR, UiLanguage.fromDeviceLanguage("fr"))
        assertEquals(UiLanguage.EN, UiLanguage.fromDeviceLanguage("en"))
        assertEquals(UiLanguage.NL, UiLanguage.fromDeviceLanguage("nl"))
        assertEquals(UiLanguage.DE, UiLanguage.fromDeviceLanguage("de"))
    }

    @Test fun unsupportedOrMissingDeviceLanguageFallsBackToFrench() {
        assertEquals(UiLanguage.FR, UiLanguage.fromDeviceLanguage("es"))
        assertEquals(UiLanguage.FR, UiLanguage.fromDeviceLanguage(null))
    }

    @Test fun verdictAndTraceResourcesHaveTheSameKeysInEverySupportedLanguage() {
        val files = listOf(
            File("src/main/res/values/strings.xml"),
            File("src/main/res/values-en/strings.xml"),
            File("src/main/res/values-nl/strings.xml"),
            File("src/main/res/values-de/strings.xml")
        )
        val keyPattern = Regex("""<string\s+name="([^"]+)"""")
        val keysByFile = files.associateWith { file ->
            keyPattern.findAll(file.readText()).map { it.groupValues[1] }.toSet()
        }
        val expectedKeys = setOf(
            "verdict_vegan",
            "verdict_non_vegan",
            "verdict_uncertain",
            "verdict_vegetarian",
            "uncertain_ingredients_title",
            "unidentified_ingredients_title",
            "conditional_vegan",
            "conditional_vegetarian",
            "traces_title",
            "traces_excluded_notice"
        )

        keysByFile.forEach { (file, keys) ->
            assertTrue("${file.path}: ${expectedKeys - keys}", keys.containsAll(expectedKeys))
            assertEquals(file.path, keysByFile.getValue(files.first()), keys)
        }
    }

    @Test fun bothVerdictDisplayPathsUseTheSharedLocalizedFormatter() {
        val ocrScreen = File("src/main/java/com/example/isitvegan/OcrFirstScreen.kt").readText()
        val legacyScreen = File("src/main/java/com/example/isitvegan/MainActivity.kt").readText()

        assertTrue(ocrScreen.contains("VerdictExplanationFormatter.render(resources, text, diagnostics)"))
        assertTrue(legacyScreen.contains("VerdictExplanationFormatter.render("))
        assertFalse(ocrScreen.contains("CrossContactNotice.format"))
        assertFalse(legacyScreen.contains("CrossContactNotice.format"))
    }
}
