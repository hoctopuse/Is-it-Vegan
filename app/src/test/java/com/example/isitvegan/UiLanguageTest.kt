package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class UiLanguageTest {
    @Test fun supportedDeviceLanguagesUseTheirMatchingInterface() {
        assertEquals(UiLanguage.FR, UiLanguage.fromDeviceLanguage("fr"))
        assertEquals(UiLanguage.EN, UiLanguage.fromDeviceLanguage("en"))
        assertEquals(UiLanguage.NL, UiLanguage.fromDeviceLanguage("nl"))
    }

    @Test fun unsupportedOrMissingDeviceLanguageFallsBackToFrench() {
        assertEquals(UiLanguage.FR, UiLanguage.fromDeviceLanguage("de"))
        assertEquals(UiLanguage.FR, UiLanguage.fromDeviceLanguage(null))
    }
}
