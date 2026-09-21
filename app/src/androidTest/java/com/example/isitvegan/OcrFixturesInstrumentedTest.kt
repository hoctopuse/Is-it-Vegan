package com.example.isitvegan

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OcrFixturesInstrumentedTest {
    @Test fun bundledLatinRecognizerReadsControlledFixtureEssentials() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val processor = OcrProcessor(context)
        try {
            listOf(
                "simple" to listOf("water", "sugar"),
                "multilingual" to listOf("suiker"),
                "nested" to listOf("sauce", "huile"),
                "may_contain" to listOf("CONTENIR")
            ).forEach { (name, expected) ->
                val file = File(context.cacheDir, "$name.png")
                context.assets.open("ocr/$name.png").use { input -> file.outputStream().use(input::copyTo) }
                val expectedText = context.assets.open("ocr/$name.txt").bufferedReader().use { it.readText() }
                val latch = CountDownLatch(1)
                var recognized = ""
                var error: String? = null
                processor.process(Uri.fromFile(file), { text -> recognized = text; latch.countDown() }, { message -> error = message; latch.countDown() })
                assertTrue("OCR did not finish for $name", latch.await(20, TimeUnit.SECONDS))
                assertFalse("OCR failed for $name: $error", error != null)
                assertFalse("No raw OCR text for $name", recognized.isBlank())
                expected.forEach { token -> assertTrue("Missing '$token' in $name: $recognized", recognized.contains(token, ignoreCase = true)) }
                assertTrue("Fixture reference should document $name", expectedText.isNotBlank())
            }
        } finally {
            processor.close()
        }
    }
}
