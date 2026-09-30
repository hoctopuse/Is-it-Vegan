package com.example.isitvegan

import android.content.Context
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
class Phase4MatcherProfileInstrumentedTest {
    @Test
    fun profilesCurrentMatcherOnDeviceWithoutChangingAnalysisPath() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assets = readAssets(context)
        val readIngredients = measure { context.assets.open("ingredients.json").bufferedReader().use { it.readText() } }
        val readMappings = measure { context.assets.open("ingredient_aliases_multilingual.json").bufferedReader().use { it.readText() } }
        val readRules = measure { context.assets.open("origin_qualifier_rules.json").bufferedReader().use { it.readText() } }
        val parseIngredients = measure { MiniJson.parse(assets.ingredients) }
        val parseMappings = measure { MiniJson.parse(assets.mappings) }
        val parseRules = measure { MiniJson.parse(assets.rules) }
        val deserializeKnowledge = measure {
            IngredientKnowledge.fromJson(assets.ingredients, assets.mappings, assets.rules)
        }
        val knowledge = IngredientKnowledge.fromJson(assets.ingredients, assets.mappings, assets.rules)
        val matcherBuild = measure { IngredientMatcher(knowledge.ingredients) }
        val profileCollector = MatcherProfileCollector()
        val profiledMatcher = IngredientMatcher(knowledge.ingredients, profileCollector)
        val referenceMatcher = IngredientMatcher(knowledge.ingredients)
        val matcherCases = listOf("eau", "E 471", "arôme de chocolat", "Schokolade", longText(), "ingrédient inconnu")
        matcherCases.forEachIndexed { order, text ->
            assertEquals(referenceMatcher.match(IngredientToken(text, 0, order)), profiledMatcher.match(IngredientToken(text, 0, order)))
        }
        val exact = measure { profiledMatcher.match(IngredientToken("eau", 0, 0)) }
        val multilingual = measure { profiledMatcher.match(IngredientToken("Schokolade", 0, 0)) }
        val eNumber = measure { profiledMatcher.match(IngredientToken("E 471", 0, 0)) }
        val contained = measure { profiledMatcher.match(IngredientToken(longText(), 0, 0)) }
        val protectedContext = measure { profiledMatcher.match(IngredientToken("arôme de chocolat", 0, 0)) }
        val noMatch = measure { profiledMatcher.match(IngredientToken("ingrédient inconnu sans correspondance", 0, 0)) }
        val parsing = measure { IngredientTreeParser.parse("parent (enfant (eau, chocolat), lait) 12,5 %", knowledge.originRules) }
        val lexical = measure { knowledge.multilingualLexicon.resolve("Schokolade", LabelLanguage.GERMAN, knowledge.ingredients) }
        val origin = measure { knowledge.originRules.extractAttached("gélatine (origine bovine)") }
        val service = IngredientAnalysisService(knowledge)
        val pssBefore = pssKb()
        val simple = measure { service.analyzeWithDiagnostics("eau, sucre", InputMode.MANUAL_INGREDIENT_LIST) }
        val nested = measure { service.analyzeWithDiagnostics("parent (enfant (eau, chocolat), lait) 12,5 %", InputMode.MANUAL_INGREDIENT_LIST) }
        val multi = measure { service.analyzeWithDiagnostics("Ingredients: water, chocolate flavour, coffee extract", InputMode.FULL_LABEL) }
        val traces = measure { service.analyzeWithDiagnostics("Ingrédients : eau, sucre. Peut contenir : lait, œuf.", InputMode.FULL_LABEL) }
        val long = measure { service.analyzeWithDiagnostics(longText(), InputMode.MANUAL_INGREDIENT_LIST) }
        val unknown = measure { service.analyzeWithDiagnostics("ingrédient inconnu sans correspondance", InputMode.MANUAL_INGREDIENT_LIST) }
        val diagnostics = measure { service.analyzeWithDiagnostics("sauce (eau, lait, ingrédient inconnu). Peut contenir : œuf.", InputMode.FULL_LABEL).decision }
        val verdict = measure { service.analyze("eau, sucre", InputMode.MANUAL_INGREDIENT_LIST).verdict }
        val pssAfter = pssKb()

        val profile = profileCollector.snapshot()
        assertTrue(profile.aliasEntries > 0)
        assertTrue(profile.regexTests >= profile.aliasEntries.toLong())
        Log.i(TAG, "PHASE4_ANDROID_PROFILE|aliases=${profile.aliasEntries}|normalizedAliases=${profile.normalizedAliasCount}|matchCalls=${profile.matchCalls}|regexTests=${profile.regexTests}|candidates=${profile.candidatesFound}|blocked=${profile.candidatesBlocked}|selected=${profile.candidatesSelected}")
        Log.i(TAG, "PHASE4_ANDROID_MATCHER_STAGES|construction=${micros(profile.matcherConstructionNanos)}us|normalizationPreparation=${micros(profile.normalizationPreparationNanos)}us|aliasPreparation=${micros(profile.aliasPreparationNanos)}us|regexPreparation=${micros(profile.regexPreparationNanos)}us|linkedAliasChecks=${micros(profile.longerLinkedAliasCheckNanos)}us|matchNormalization=${micros(profile.matchNormalizationNanos)}us|candidateSearch=${micros(profile.candidateSearchNanos)}us|contextFiltering=${micros(profile.contextFilteringNanos)}us|selectionResolution=${micros(profile.selectionAndResolutionNanos)}us")
        listOf(
            "asset-read-ingredients" to readIngredients,
            "asset-read-mappings" to readMappings,
            "asset-read-rules" to readRules,
            "parse-ingredients" to parseIngredients,
            "parse-mappings" to parseMappings,
            "parse-rules" to parseRules,
            "deserialize-knowledge" to deserializeKnowledge,
            "build-matcher" to matcherBuild,
            "match-exact" to exact,
            "match-multilingual" to multilingual,
            "match-enumber" to eNumber,
            "match-contained" to contained,
            "match-protected-context" to protectedContext,
            "match-no-match" to noMatch,
            "parse-composition" to parsing,
            "lexical-resolution" to lexical,
            "origin-rule" to origin,
            "analysis-simple" to simple,
            "analysis-nested" to nested,
            "analysis-multilingual" to multi,
            "analysis-traces" to traces,
            "analysis-long" to long,
            "analysis-no-match" to unknown,
            "diagnostics" to diagnostics,
            "verdict" to verdict
        ).forEach { (name, timing) -> log(name, timing) }
        Log.i(TAG, "PHASE4_ANDROID_MEMORY|pssBefore=${pssBefore}KB|pssAfter=${pssAfter}KB|caveat=process-wide PSS, GC dependent")
    }

    private fun longText() = listOf(
        "eau", "sucre", "sel", "farine de blé", "lait végétal", "arôme chocolat", "extrait de café",
        "E471", "E470b", "E572", "chocolade", "Schokolade", "ingrédient totalement inconnu", "miel", "gélatine", "fraise", "café"
    ).joinToString(", ")

    private fun readAssets(context: Context) = AssetTexts(
        context.assets.open("ingredients.json").bufferedReader().use { it.readText() },
        context.assets.open("ingredient_aliases_multilingual.json").bufferedReader().use { it.readText() },
        context.assets.open("origin_qualifier_rules.json").bufferedReader().use { it.readText() }
    )

    private fun measure(block: () -> Any?): Timing {
        repeat(5) { block() }
        val values = (1..10).map {
            val start = System.nanoTime()
            block()
            System.nanoTime() - start
        }.sorted()
        return Timing(values.first(), values[values.lastIndex / 2], values.last())
    }

    private fun log(name: String, timing: Timing) = Log.i(
        TAG,
        "PHASE4_ANDROID_TIMING|$name|min=${micros(timing.minimum)}us|median=${micros(timing.median)}us|max=${micros(timing.maximum)}us|n=10|warmup=5"
    )

    private fun pssKb(): Int = Debug.MemoryInfo().also(Debug::getMemoryInfo).totalPss

    private data class AssetTexts(val ingredients: String, val mappings: String, val rules: String)
    private data class Timing(val minimum: Long, val median: Long, val maximum: Long)

    private companion object { const val TAG = "Phase4MatcherProfile" }
}

private fun micros(nanos: Long): Long = (nanos / 1_000.0).roundToLong()
