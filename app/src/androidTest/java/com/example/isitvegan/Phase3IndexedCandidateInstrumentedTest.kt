package com.example.isitvegan

import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.math.roundToLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Phase3IndexedCandidateInstrumentedTest {
    private data class JsonText(val ingredients: String, val mappings: String, val originRules: String)
    private data class Timing(val min: Long, val median: Long, val max: Long, val n: Int)

    @Test
    fun indexedCandidateProbePreservesCurrentAnalysisOnDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val json = readAssets(context)
        val knowledge = IngredientKnowledge.fromJson(json.ingredients, json.mappings, json.originRules)
        val index = KnowledgeIndex.from(knowledge)
        val provider = IndexedCandidateProvider(index)
        val matcher = IngredientMatcher(knowledge.ingredients)
        val service = IngredientAnalysisService(knowledge)

        assertTrue(provider.findExactAlias("huile de colza").candidates.any { it.canonicalId == "rapeseed_oil" })
        assertTrue(provider.findMultilingualAlias("Rapsöl", LabelLanguage.GERMAN).candidates.any { it.canonicalId == "rapeseed_oil" })
        assertTrue(provider.findENumber("E 471").candidates.any { it.canonicalId == "e471" })
        assertTrue(provider.findENumber("471").candidates.isEmpty())
        assertEquals(479, knowledge.ingredients.size)

        val label = "Ingrédients : parent (eau, chocolat, lait, ingrédient inconnu). Peut contenir : œuf."
        val baseline = service.analyzeWithDiagnostics(label, InputMode.FULL_LABEL)
        val probe = service.analyzeWithDiagnostics(label, InputMode.FULL_LABEL).also { diagnostics ->
            diagnostics.tokens.filter { it.kind != NodeKind.COMPOSITE_INGREDIENT }.forEach { token ->
                provider.findTextCandidates(token.matcherText ?: token.text, token.matchingLanguage)
                provider.findTextCandidates(token.matcherText ?: token.text)
            }
        }
        assertEquals(baseline.result.verdict, probe.result.verdict)
        assertEquals(baseline.result.veganAssessment, probe.result.veganAssessment)
        assertEquals(baseline.result.unknown, probe.result.unknown)
        assertEquals(baseline.result.crossContactWarnings, probe.result.crossContactWarnings)
        assertEquals(baseline.tokens.map { it.parentOrder }, probe.tokens.map { it.parentOrder })
        assertEquals(baseline.tokens.map { it.quantityPercent }, probe.tokens.map { it.quantityPercent })
        assertTrue(probe.crossContactWarnings.isNotEmpty())
        assertTrue(probe.tokens.none { it.text.contains("lait", true) && it.isDeclaredPresence.not() && it.order > 0 && it.parentOrder == null })
        assertTrue(matcher.match(IngredientToken("eau", depth = 0, order = 0)).ingredients.any { it.id == "water" })

        val exact = measure { provider.findExactAlias("huile de colza") }
        val multilingual = measure { provider.findMultilingualAlias("Rapsöl", LabelLanguage.GERMAN) }
        val eNumber = measure { provider.findENumber("E471") }
        val candidateRetrieval = measure { provider.findTextCandidates("arôme chocolat") }
        val candidateMatcher = measure {
            provider.findTextCandidates("arôme chocolat")
            matcher.match(IngredientToken("arôme chocolat", depth = 0, order = 0))
        }
        val currentAnalysis = measure { service.analyzeWithDiagnostics(label, InputMode.FULL_LABEL) }
        val experimentalProbe = measure {
            service.analyzeWithDiagnostics(label, InputMode.FULL_LABEL).also { diagnostics ->
                diagnostics.tokens.filter { it.kind != NodeKind.COMPOSITE_INGREDIENT }.forEach { token ->
                    provider.findTextCandidates(token.matcherText ?: token.text)
                }
            }
        }
        val diagnostics = measure {
            service.analyzeWithDiagnostics(label, InputMode.FULL_LABEL).decision
        }

        Log.i(TAG, "PHASE3_ANDROID_LOGICAL|concepts=${index.metrics.conceptCount}|forms=${index.metrics.formCount}|collisions=${index.metrics.collisionKeyCount}|invalidTargets=${index.metrics.invalidCanonicalIdCount}")
        log("exact", exact)
        log("multilingual", multilingual)
        log("enumber", eNumber)
        log("candidate-retrieval", candidateRetrieval)
        log("candidate-plus-matcher", candidateMatcher)
        log("current-analysis", currentAnalysis)
        log("experimental-probe", experimentalProbe)
        log("diagnostics", diagnostics)
        Log.i(TAG, "PHASE3_ANDROID_MEMORY|pss=${pssKb()}|caveat=process PSS approximation")
    }

    private fun readAssets(context: android.content.Context): JsonText = JsonText(
        context.assets.open("ingredients.json").bufferedReader().use { it.readText() },
        context.assets.open("ingredient_aliases_multilingual.json").bufferedReader().use { it.readText() },
        context.assets.open("origin_qualifier_rules.json").bufferedReader().use { it.readText() }
    )

    private fun measure(operation: () -> Any?): Timing {
        repeat(5) { operation() }
        val values = (1..10).map {
            val start = System.nanoTime()
            operation()
            System.nanoTime() - start
        }.sorted()
        return Timing(values.first(), values[values.lastIndex / 2], values.last(), values.size)
    }

    private fun log(name: String, timing: Timing) {
        Log.i(TAG, "PHASE3_ANDROID_TIMING|$name|min=${micros(timing.min)}us|median=${micros(timing.median)}us|max=${micros(timing.max)}us|n=${timing.n}")
    }

    private fun pssKb(): Int {
        val info = Debug.MemoryInfo()
        Debug.getMemoryInfo(info)
        return info.totalPss
    }

    private companion object {
        const val TAG = "Phase3Benchmark"
    }
}

private fun micros(nanos: Long): Long = (nanos / 1_000.0).roundToLong()
