package com.example.isitvegan

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class OcrProgressInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val uri = Uri.parse("content://test/photo")
    private val service = IngredientAnalysisService(IngredientKnowledge(listOf(
        Ingredient("water", "eau", listOf("eau"), status = VeganStatus.VEGAN, reason = "test")
    )))
    private val text = "Ingrédients: eau"
    private val options = listOf(
        OcrTextOption(LabelLanguage.FRENCH, text, "FR", blockId = "fr", segmentId = "fr"),
        OcrTextOption(LabelLanguage.ENGLISH, text, "EN", blockId = "en", segmentId = "en")
    )

    private fun show(
        initialUri: Uri? = uri,
        pick: (((Uri?) -> Unit) -> Unit)? = null,
        session: OcrSession = OcrSession(editableText = text, textOptions = options,
            selectedOptionLanguage = LabelLanguage.FRENCH, selectedOptionBlockId = "fr"),
        crop: ((Bitmap, OcrCropRect) -> Bitmap)? = null,
        cropSettled: (() -> Unit)? = null,
        ocr: (Uri, OcrCropRect?, (OcrProcessingResult) -> Unit, (String) -> Unit, UiLanguage) -> Unit = { _, _, _, _, _ -> },
        loads: AtomicInteger = AtomicInteger(),
        previewDecode: ((Uri) -> Bitmap)? = null,
        realPreview: Boolean = false
    ) {
        UiLanguagePreferences.write(InstrumentationRegistry.getInstrumentation().targetContext, UiLanguage.FR)
        rule.runOnUiThread {
            rule.activity.setContent {
                OcrFirstScreenContent(true, session, initialSourceUri = initialUri,
                    pickPhoto = pick, decodePreview = if (realPreview) null else { value -> loads.incrementAndGet(); previewDecode?.invoke(value) ?: Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888) },
                    prepareCropPreview = crop, onCropSettled = cropSettled, processOcr = ocr,
                    analyzeText = service::analyzeWithDiagnostics)
            }
        }
        rule.onNodeWithText("OCR").performClick()
        if (initialUri != null) rule.waitUntilAtLeastOneExists(hasText("EXTRAIRE LE TEXTE").and(isEnabled()), 30_000)
    }

    @Test fun editInvalidatesExtractionProgress() {
        show()
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.onNodeWithText("Extraction du texte en cours…").assertExists()
        rule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(", sel")
        rule.onAllNodes(hasText("Extraction du texte en cours…")).assertCountEquals(0)
    }

    @Test fun languageChangeInvalidatesExtractionProgress() {
        show()
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.onNodeWithText("English").performScrollTo().performClick()
        rule.onAllNodes(hasText("Extraction du texte en cours…")).assertCountEquals(0)
    }

    @Test fun blockChangeInvalidatesExtractionProgress() {
        show()
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.onNodeWithText("EN").performScrollTo().performClick()
        rule.onAllNodes(hasText("Extraction du texte en cours…")).assertCountEquals(0)
    }

    @Test fun obsoleteOcrCallbackDoesNotClearNewExtractionProgress() {
        val callbacks = mutableListOf<(OcrProcessingResult) -> Unit>()
        show(ocr = { _, _, success, _, _ -> callbacks += success })
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(", sel")
        rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
        rule.runOnUiThread { callbacks[0](OcrProcessingResult(text, text, OcrDiagnostics(0, 1, 1, listOf("FR"), emptyList()))) }
        rule.onNodeWithText("Extraction du texte en cours…").performScrollTo().assertIsDisplayed()
        rule.runOnUiThread { callbacks[1](OcrProcessingResult(text, text, OcrDiagnostics(0, 1, 1, listOf("FR"), emptyList()))) }
        rule.onNodeWithText("Texte reconstruit. Vérifiez-le avant l’analyse.").performScrollTo().assertIsDisplayed()
    }

    @Test fun cropInvalidationClearsProgressWithoutOverwritingNewOcr() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val settled = CountDownLatch(1)
        show(crop = { bitmap, _ -> started.countDown(); check(release.await(90, TimeUnit.SECONDS)); bitmap.copy(Bitmap.Config.ARGB_8888, false) },
            cropSettled = { settled.countDown() })
        try {
            rule.onNodeWithText("RECADRER POUR L’OCR").performScrollTo().performClick()
            rule.onNodeWithText("UTILISER CE CADRE").performClick()
            assertTrue(started.await(10, TimeUnit.SECONDS))
            rule.onNodeWithText("Préparation de la zone recadrée…").assertExists()
            rule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(", sel")
            rule.onAllNodes(hasText("Préparation de la zone recadrée…")).assertCountEquals(0)
            rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
            release.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.onNodeWithText("Extraction du texte en cours…").performScrollTo().assertIsDisplayed()
        } finally { release.countDown() }
    }

    @Test fun obsoleteOcrCallbackDoesNotClearNewCropProgress() {
        val callbacks = mutableListOf<(OcrProcessingResult) -> Unit>()
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val settled = CountDownLatch(1)
        show(ocr = { _, _, success, _, _ -> callbacks += success },
            crop = { bitmap, _ -> started.countDown(); check(release.await(90, TimeUnit.SECONDS)); bitmap.copy(Bitmap.Config.ARGB_8888, false) },
            cropSettled = { settled.countDown() })
        try {
            rule.onNodeWithText("EXTRAIRE LE TEXTE").performScrollTo().performClick()
            rule.onNodeWithTag("ocr-editor").performScrollTo().performTextInput(", sel")
            rule.onNodeWithText("RECADRER POUR L’OCR").performScrollTo().performClick()
            rule.onNodeWithText("UTILISER CE CADRE").performClick()
            assertTrue(started.await(10, TimeUnit.SECONDS))
            rule.runOnUiThread { callbacks[0](OcrProcessingResult(text, text, OcrDiagnostics(0, 1, 1, listOf("FR"), emptyList()))) }
            rule.onNodeWithText("Préparation de la zone recadrée…").performScrollTo().assertIsDisplayed()
            release.countDown()
            rule.waitUntil(30_000) { settled.count == 0L }
            rule.onNodeWithText("Cadre validé. Appuyez sur EXTRAIRE LE TEXTE.").performScrollTo().assertIsDisplayed()
        } finally { release.countDown() }
    }

    @Test fun selectingSamePhotoTwiceKeepsPhotoControlsUsable() {
        val loads = AtomicInteger()
        show(initialUri = null, pick = { callback -> callback(uri) }, loads = loads)
        rule.onNodeWithText("CHOISIR UNE PHOTO").performClick()
        rule.waitUntilAtLeastOneExists(hasText("EXTRAIRE LE TEXTE").and(isEnabled()), 30_000)
        rule.onNodeWithText("RECADRER POUR L’OCR").assertIsEnabled()
        rule.onNodeWithText("CHOISIR UNE PHOTO").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("CHOISIR UNE PHOTO").and(isEnabled()), 30_000)
        rule.onNodeWithText("CHOISIR UNE PHOTO").assertIsEnabled()
        rule.onNodeWithText("EXTRAIRE LE TEXTE").assertIsEnabled()
        rule.onNodeWithText("RECADRER POUR L’OCR").assertExists()
        assertTrue(loads.get() >= 1)
    }

    @Test fun selectingSameFixtureTwiceReloadsRealPreview() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val image = File(instrumentation.targetContext.cacheDir, "session-preview-fixture.png")
        instrumentation.context.assets.open("ocr/simple.png").use { input -> image.outputStream().use(input::copyTo) }
        val imageUri = Uri.fromFile(image)
        show(initialUri = null, pick = { callback -> callback(imageUri) }, realPreview = true)
        rule.onNodeWithText("CHOISIR UNE PHOTO").performClick()
        rule.waitUntilAtLeastOneExists(hasText("RECADRER POUR L’OCR").and(isEnabled()), 30_000)
        rule.onNodeWithText("CHOISIR UNE PHOTO").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("RECADRER POUR L’OCR").and(isEnabled()), 30_000)
        rule.onNodeWithText("EXTRAIRE LE TEXTE").assertIsEnabled()
        rule.onNodeWithText("CHOISIR UNE PHOTO").assertIsEnabled()
    }

    @Test fun oldPreviewCannotOverwriteNewSelectionOfSameUri() {
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val firstFinished = CountDownLatch(1)
        val order = AtomicInteger()
        show(initialUri = null, pick = { callback -> callback(uri) }, previewDecode = {
            if (order.incrementAndGet() == 1) {
                firstStarted.countDown()
                try { check(releaseFirst.await(90, TimeUnit.SECONDS)) } finally { firstFinished.countDown() }
            }
            Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        })
        try {
            rule.onNodeWithText("CHOISIR UNE PHOTO").performClick()
            rule.waitUntil(30_000) { firstStarted.count == 0L }
            rule.onNodeWithText("EFFACER").performScrollTo().performClick()
            rule.onNodeWithText("CHOISIR UNE PHOTO").performScrollTo().performClick()
            rule.waitUntilAtLeastOneExists(hasText("EXTRAIRE LE TEXTE").and(isEnabled()), 30_000)
            releaseFirst.countDown()
            rule.waitUntil(30_000) { firstFinished.count == 0L }
            rule.onNodeWithText("Photo sélectionnée. Appuyez sur EXTRAIRE LE TEXTE.").performScrollTo().assertIsDisplayed()
            rule.onNodeWithText("EXTRAIRE LE TEXTE").assertIsEnabled()
        } finally { releaseFirst.countDown() }
    }

    @Test fun manualTextRoundTripRequiresFreshAnalysis() {
        show(initialUri = null)
        rule.onNodeWithText("Étiquette").performClick()
        rule.onNodeWithText("Texte de l’étiquette").performTextInput(text)
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
        rule.onNodeWithText("Texte de l’étiquette").performScrollTo().performTextInput(", lait")
        rule.onNodeWithText("Texte de l’étiquette").performTextClearance()
        rule.onNodeWithText("Texte de l’étiquette").performTextInput(text)
        rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
    }

    @Test fun manualListRoundTripRequiresFreshAnalysis() {
        show(initialUri = null)
        rule.onNodeWithText("Liste seule").performClick()
        rule.onNodeWithText("Liste d’ingrédients").performTextInput("eau")
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
        rule.onNodeWithText("Liste d’ingrédients").performScrollTo().performTextInput(", lait")
        rule.onNodeWithText("Liste d’ingrédients").performTextClearance()
        rule.onNodeWithText("Liste d’ingrédients").performTextInput("eau")
        rule.onAllNodes(hasText("✅ VEGAN", substring = true)).assertCountEquals(0)
        rule.onNodeWithText("ANALYSER").performScrollTo().performClick()
        rule.waitUntilAtLeastOneExists(hasText("✅ VEGAN", substring = true), 30_000)
    }
}
