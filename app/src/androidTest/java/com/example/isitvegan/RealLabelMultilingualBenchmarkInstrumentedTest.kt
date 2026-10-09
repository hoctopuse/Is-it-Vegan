package com.example.isitvegan

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Text-only benchmark for the supplied English block; OCR is deliberately outside these timings. */
@RunWith(AndroidJUnit4::class)
class RealLabelMultilingualBenchmarkInstrumentedTest {
    @Test
    fun profilesRealLabelTextAndDeterministicOcrVariants() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assetRead = timed { readAssets(context) }
        val assets = assetRead.value
        assertTrue(assets.ingredients.isNotEmpty())
        assertTrue(assets.mappings.isNotEmpty())
        assertTrue(assets.rules.isNotEmpty())
        val coldKnowledge = timed { knowledge(assets) }
        val loadedKnowledge = coldKnowledge.value
        assertKnowledge(loadedKnowledge)
        val matcherBuild = timed { IngredientMatcher(loadedKnowledge.ingredients) }

        val corpus = MiniJson.parse(assets.ingredients) as List<*>
        val mappings = MiniJson.parse(assets.mappings) as Map<*, *>
        val mappingCount = (mappings["mappings"] as? List<*>)?.size ?: 0
        val aliasGroupCount = (mappings["aliases"] as? List<*>)?.size ?: 0
        Log.i(TAG, "REAL_LABEL_CORPUS|concepts=${corpus.size}|mappings=$mappingCount|aliasGroups=$aliasGroupCount|ingredientsBytes=${assets.ingredients.toByteArray().size}|mappingsBytes=${assets.mappings.toByteArray().size}|rulesBytes=${assets.rules.toByteArray().size}")
        log("asset-read-cold", listOf(assetRead.nanos))
        log("knowledge-load-cold", listOf(coldKnowledge.nanos))
        log("matcher-build-cold", listOf(matcherBuild.nanos))
        log("knowledge-from-json", repeated({ knowledge(assets) }, ::assertKnowledge).nanos)
        log("matcher-build", repeated({ IngredientMatcher(loadedKnowledge.ingredients) }) { }.nanos)

        val scenarios = linkedMapOf(
            "clean-flavouring" to CLEAN_LABEL,
            "ocr-glucose-fructose-syna" to CLEAN_LABEL.replace("Glucose-fructose syrup", "Glucose-fructose syna"),
            "ocr-wheat-lour" to CLEAN_LABEL.replace("WHEAT flour", "WHEAT lour"),
            "ocr-citric-aid" to CLEAN_LABEL.replace("Citric acid", "CTitric aid"),
            "ocr-favouring" to CLEAN_LABEL.replace("Flavouring", "Favouring")
        )

        scenarios.forEach { (name, text) ->
            val service = IngredientAnalysisService(loadedKnowledge)
            val cold = timed { service.analyzeWithDiagnostics(text, InputMode.FULL_LABEL, UiLanguage.EN) }
            log("$name-cold-first-analysis", listOf(cold.nanos))
            assertValidResult(text, cold.value)

            repeat(WARMUPS) {
                val result = service.analyzeWithDiagnostics(text, InputMode.FULL_LABEL, UiLanguage.EN)
                assertValidResult(text, result)
            }
            val samples = mutableListOf<Long>()
            repeat(SAMPLES) {
                val measured = timed {
                    service.analyzeWithDiagnostics(text, InputMode.FULL_LABEL, UiLanguage.EN)
                }
                assertValidResult(text, measured.value)
                samples += measured.nanos
            }
            log(name, samples)
        }

        val englishSection = LabelSectionExtractor.extract(LabelLanguage.ENGLISH, CLEAN_LABEL)
        assertEquals(INGREDIENTS, englishSection.ingredientsText)
        val parseCold = timed { IngredientTreeParser.parse(englishSection.ingredientsText.orEmpty(), loadedKnowledge.originRules) }
        assertTrue(parseCold.value.isNotEmpty())
        log("clean-ingredient-tree-parse-cold", listOf(parseCold.nanos))
        val parseSamples = repeated({
            IngredientTreeParser.parse(englishSection.ingredientsText.orEmpty(), loadedKnowledge.originRules)
        }) { assertTrue(it.isNotEmpty()) }
        log("clean-ingredient-tree-parse", parseSamples.nanos)

        val sectionCold = timed { LabelSectionExtractor.extract(LabelLanguage.ENGLISH, CLEAN_LABEL) }
        assertEquals(INGREDIENTS, sectionCold.value.ingredientsText)
        log("clean-section-extraction-cold", listOf(sectionCold.nanos))
        val sectionSamples = repeated({ LabelSectionExtractor.extract(LabelLanguage.ENGLISH, CLEAN_LABEL) }) {
            assertEquals(INGREDIENTS, it.ingredientsText)
        }
        log("clean-section-extraction", sectionSamples.nanos)
    }

    private fun assertValidResult(input: String, diagnostics: AnalysisDiagnostics) {
        assertEquals(input, diagnostics.input)
        assertEquals(InputMode.FULL_LABEL, diagnostics.inputMode)
        assertEquals(LabelLanguage.ENGLISH, diagnostics.labelSections.language)
        assertEquals(input.substringAfter("INGREDIENTS: "), diagnostics.labelSections.ingredientsText)
        assertTrue(diagnostics.ingredientTree.isNotEmpty())
        val ids = diagnostics.result.matched.map { it.id }.toSet()
        assertTrue("Expected sugar mapping in $ids", "sugar" in ids)
        if (input == CLEAN_LABEL) {
            assertTrue("Expected wheat flour mapping in $ids", "wheat_flour" in ids)
        }
        assertTrue("Expected protected vegetable oil designation in $ids", "vegetable_oil" in ids)
        if (diagnostics.result.unknown.isNotEmpty()) {
            assertFalse("Unknown ingredients must not produce a vegan verdict", diagnostics.result.verdict == AnalysisVerdict.VEGAN)
        }
    }

    private fun assertKnowledge(knowledge: IngredientKnowledge) {
        assertTrue(knowledge.ingredients.isNotEmpty())
        assertTrue(knowledge.originRuleErrors.isEmpty())
    }

    private fun readAssets(context: Context) = AssetTexts(
        context.assets.open("ingredients.json").bufferedReader().use { it.readText() },
        context.assets.open("ingredient_aliases_multilingual.json").bufferedReader().use { it.readText() },
        context.assets.open("origin_qualifier_rules.json").bufferedReader().use { it.readText() }
    )

    private fun knowledge(assets: AssetTexts) = IngredientKnowledge.fromJson(
        assets.ingredients, assets.mappings, assets.rules
    )

    private fun <T> repeated(block: () -> T, verify: (T) -> Unit): Samples<T> {
        repeat(WARMUPS) { verify(block()) }
        val measured = mutableListOf<Long>()
        var last: T? = null
        repeat(SAMPLES) {
            val result = timed(block)
            measured += result.nanos
            verify(result.value)
            last = result.value
        }
        @Suppress("UNCHECKED_CAST")
        return Samples(measured, last as T)
    }

    private fun <T> timed(block: () -> T): Timed<T> {
        val start = SystemClock.elapsedRealtimeNanos()
        val value = block()
        return Timed(value, SystemClock.elapsedRealtimeNanos() - start)
    }

    private fun log(name: String, values: List<Long>) {
        val sorted = values.sorted()
        val lowerMedian = sorted[(sorted.size - 1) / 2]
        val median = if (sorted.size % 2 == 1) {
            sorted[sorted.size / 2]
        } else {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
        }
        Log.i(TAG, "REAL_LABEL_TIMING|$name|rawNs=${values.joinToString(",")}|minNs=${sorted.first()}|lowerMedianNs=$lowerMedian|medianNs=$median|maxNs=${sorted.last()}|n=${values.size}|warmup=${if (values.size == 1) 0 else WARMUPS}")
    }

    private data class AssetTexts(val ingredients: String, val mappings: String, val rules: String)
    private data class Timed<T>(val value: T, val nanos: Long)
    private data class Samples<T>(val nanos: List<Long>, val value: T)

    private companion object {
        const val TAG = "RealLabelBenchmark"
        const val WARMUPS = 5
        const val SAMPLES = 10
        const val INGREDIENTS = "Sugar, Glucose-fructose syrup,\nWHEAT flour, Acids (Malic acid, Citric acid), Dextrose,\nVegetable oil (Palm), Flavouring,\nColours (E100, E131, E163),\nCaramelised sugar syrup,\nColouring food (Elderberry extract, Safflower, Sweet potato),\nAntioxidants (Ascorbic acid, E306)."
        const val CLEAN_LABEL = "FLAVOURED JELLY GUMS. INGREDIENTS: $INGREDIENTS"
    }
}
