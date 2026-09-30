package com.example.isitvegan

import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import kotlin.math.roundToLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Phase2RuntimeBenchmarkInstrumentedTest {
    private data class Timing(val min: Long, val median: Long, val max: Long, val n: Int)

    @Test
    fun measureAssetsKnowledgeIndexMatcherAndAnalysisOnDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val memoryBefore = pssKb()
        val loadTiming = measure {
            readAssets(context)
        }
        val json = readAssets(context)
        val deserializeTiming = measure {
            IngredientKnowledge.fromJson(json.ingredients, json.mappings, json.originRules)
        }
        val knowledge = IngredientKnowledge.fromJson(json.ingredients, json.mappings, json.originRules)
        val memoryAfterKnowledge = pssKb()
        val indexTiming = measure { KnowledgeIndex.from(knowledge) }
        val index = KnowledgeIndex.from(knowledge)
        val memoryAfterIndex = pssKb()
        val matcherTiming = measure { IngredientMatcher(knowledge.ingredients) }
        val matcher = IngredientMatcher(knowledge.ingredients)
        val service = IngredientAnalysisService(knowledge)
        val lookupTiming = measure { index.findByAlias("eau") }
        val multilingualTiming = measure {
            index.findByAlias("chocoladearoma", LabelLanguage.DUTCH)
        }
        val eNumberTiming = measure { index.findByENumber("E 471") }
        val analysisTiming = measure {
            service.analyzeWithDiagnostics(
                "Ingrédients : sauce (eau, lait, ingrédient inconnu). Peut contenir : œuf.",
                InputMode.FULL_LABEL
            )
        }
        val matcherResult = matcher.match(IngredientToken("eau", depth = 0, order = 0))
        assertTrue(matcherResult.ingredients.any { it.id == "water" })
        assertTrue(index.findByAlias("eau").any { it.canonicalId == "water" })
        assertEquals(479, knowledge.ingredients.size)

        Log.i(TAG, "PHASE2_ANDROID_FILES|assets=three-json-assets")
        Log.i(TAG, "PHASE2_ANDROID_LOGICAL|concepts=${index.metrics.conceptCount}|forms=${index.metrics.formCount}|aliasKeys=${index.metrics.aliasKeyCount}|collisions=${index.metrics.collisionKeyCount}|invalidTargets=${index.metrics.invalidCanonicalIdCount}")
        logTiming("load-assets", loadTiming)
        logTiming("deserialize-knowledge", deserializeTiming)
        logTiming("build-index", indexTiming)
        logTiming("build-matcher", matcherTiming)
        logTiming("lookup-exact", lookupTiming)
        logTiming("lookup-multilingual", multilingualTiming)
        logTiming("lookup-enumber", eNumberTiming)
        logTiming("analyze-diagnostics", analysisTiming)
        Log.i(TAG, "PHASE2_ANDROID_MEMORY|pssBeforeKb=$memoryBefore|pssAfterKnowledgeKb=$memoryAfterKnowledge|pssAfterIndexKb=$memoryAfterIndex|caveat=process PSS approximation")
    }

    private data class JsonText(val ingredients: String, val mappings: String, val originRules: String)

    private fun readAssets(context: android.content.Context): JsonText = JsonText(
        context.assets.open("ingredients.json").bufferedReader().use { it.readText() },
        context.assets.open("ingredient_aliases_multilingual.json").bufferedReader().use { it.readText() },
        context.assets.open("origin_qualifier_rules.json").bufferedReader().use { it.readText() }
    )

    private fun measure(operation: () -> Any?): Timing {
        repeat(3) { operation() }
        val values = (1..10).map {
            val start = System.nanoTime()
            operation()
            System.nanoTime() - start
        }.sorted()
        return Timing(values.first(), values[values.lastIndex / 2], values.last(), values.size)
    }

    private fun pssKb(): Int {
        val info = Debug.MemoryInfo()
        Debug.getMemoryInfo(info)
        return info.totalPss
    }

    private fun logTiming(name: String, timing: Timing) {
        Log.i(TAG, "PHASE2_ANDROID_TIMING|$name|min=${micros(timing.min)}us|median=${micros(timing.median)}us|max=${micros(timing.max)}us|n=${timing.n}")
    }

    private fun micros(nanos: Long): Long = (nanos / 1_000.0).roundToLong()

    private companion object {
        const val TAG = "Phase2Benchmark"
    }
}
