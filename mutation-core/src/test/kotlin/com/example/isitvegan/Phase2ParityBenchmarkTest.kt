package com.example.isitvegan

import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.math.roundToLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase2ParityBenchmarkTest {
    private data class CorpusCase(
        val id: String,
        val text: String,
        val language: LabelLanguage,
        val analysis: Boolean = false,
        val inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    )

    private data class Timing(
        val minNanos: Long,
        val medianNanos: Long,
        val maxNanos: Long,
        val repetitions: Int
    ) {
        fun rendered(): String = "min=${timingMicros(minNanos)}us median=${timingMicros(medianNanos)}us max=${timingMicros(maxNanos)}us n=$repetitions"
    }

    private val knowledge by lazy { loadCurrentKnowledge() }
    private val index by lazy { KnowledgeIndex.from(knowledge) }
    private val matcher by lazy { IngredientMatcher(knowledge.ingredients) }
    private val service by lazy { IngredientAnalysisService(knowledge) }

    @Test
    fun controlledCorpusComparesCandidatesConceptsStatusesAndDiagnostics() {
        val cases = corpus()
        val conceptCases = cases.filterNot { it.analysis }
        val compositionCases = cases.filter { it.analysis }
        assertTrue(conceptCases.size >= 24)
        assertTrue(compositionCases.size >= 8)
        println("PHASE2_CORPUS|total=${cases.size}|conceptual=${conceptCases.size}|analysis=${compositionCases.size}|languages=${cases.map { it.language }.distinct().size}")

        cases.forEach { case ->
            val current = currentObservation(case)
            val indexCandidates = index.findCandidatesInText(case.text, case.language) +
                index.findCandidatesInText(case.text)
            val candidateIds = indexCandidates.map { it.canonicalId }.toSet()

            assertTrue(
                "${case.id}: matcher selection ${current.selectedIds} absent from index candidates $candidateIds",
                candidateIds.containsAll(current.selectedIds)
            )
            assertTrue(
                "${case.id}: blocked candidates ${current.blockedIds} absent from index candidates $candidateIds",
                candidateIds.containsAll(current.blockedIds)
            )
            current.selectedIds.forEach { id ->
                val concept = index.concepts.single { it.id == id }
                assertEquals("${case.id}: status for $id", current.statusById[id], concept.status)
            }

            if (case.analysis) {
                val diagnostics = service.analyzeWithDiagnostics(case.text, case.inputMode)
                diagnostics.tokens.filter { it.kind != NodeKind.COMPOSITE_INGREDIENT }
                    .forEach { token ->
                        val tokenCandidates = index.findCandidatesInText(token.matcherText ?: token.text) +
                            index.findCandidatesInText(token.matcherText ?: token.text, diagnostics.labelSections.language)
                        val ids = tokenCandidates.map { it.canonicalId }.toSet()
                        assertTrue(
                            "${case.id}/token:${token.order}: ${token.matchedIngredientIds} absent from $ids",
                            ids.containsAll(token.matchedIngredientIds)
                        )
                    }
                assertTrue("${case.id}: diagnostic must retain token order", diagnostics.tokens.zipWithNext().all { (a, b) -> a.order < b.order })
            }
        }
    }

    @Test
    fun contextsTracesOccurrencesAndVerdictsRemainExplicit() {
        val contexts = listOf(
            "chocolat" to "chocolate",
            "arôme chocolat" to "flavouring",
            "goût chocolat" to null,
            "extrait de chocolat" to null,
            "fraise" to "strawberry",
            "arôme fraise" to "flavouring",
            "café" to "coffee",
            "extrait de café" to null,
            "lait" to "milk",
            "arôme lait" to "flavouring",
            "lait végétal" to null,
            "miel" to "honey",
            "arôme miel" to "flavouring"
        )
        contexts.forEach { (text, expectedId) ->
            val result = matcher.match(IngredientToken(text, depth = 0, order = 0))
            val candidates = index.findCandidatesInText(text)
            if (expectedId != null) {
                assertTrue("$text should expose $expectedId", expectedId in result.ingredients.map { it.id })
                assertTrue("$text candidate parity", candidates.any { it.canonicalId == expectedId })
            } else {
                assertTrue("$text selected candidates must be indexed", candidates.map { it.canonicalId }
                    .containsAll(result.ingredients.map { it.id }))
            }
        }
        assertFalse("arôme chocolat must not become chocolate", matcher.match(token("arôme chocolat")).ingredients.any { it.id == "chocolate" })
        assertFalse("arôme fraise must not become strawberry", matcher.match(token("arôme fraise")).ingredients.any { it.id == "strawberry" })
        assertFalse("extrait de café must not become coffee", matcher.match(token("extrait de café")).ingredients.any { it.id == "coffee" })

        val repeated = service.analyzeWithDiagnostics("eau, eau, lait", InputMode.MANUAL_INGREDIENT_LIST)
        val waterOccurrences = repeated.tokens.filter { it.text.equals("eau", ignoreCase = true) }
        assertEquals(2, waterOccurrences.size)
        assertTrue(waterOccurrences[0].order != waterOccurrences[1].order)

        val nested = service.analyzeWithDiagnostics(
            "composition (eau, chocolat, ingrédient totalement inconnu)",
            InputMode.MANUAL_INGREDIENT_LIST
        )
        val childTokens = nested.tokens.filter { it.parentOrder != null }
        assertTrue(childTokens.isNotEmpty())
        assertTrue(childTokens.any { it.depth >= 1 })
        assertTrue(nested.result.unknown.any { it.contains("inconnu") })
        assertTrue(nested.tokens.any { token -> token.parentOrder?.let { token.order > it } == true })
        assertTrue(nested.verdictExplanation.uncertainIngredients.any { it.path.size >= 2 })

        val percentage = service.analyzeWithDiagnostics(
            "sauce (eau, farine de blé) 12,5 %",
            InputMode.MANUAL_INGREDIENT_LIST
        )
        assertTrue(percentage.tokens.any { it.quantityPercent != null })

        val traces = listOf(
            "Ingrédients : eau, sucre. Peut contenir : lait, œuf.",
            "Ingrediënten: water, suiker. Kan bevatten: melk, ei.",
            "Ingredients: water, sugar. May contain: milk, egg.",
            "Zutaten: Wasser, Zucker. Kann enthalten: Milch, Ei."
        )
        traces.forEach { text ->
            val withTrace = service.analyzeWithDiagnostics(text, InputMode.FULL_LABEL)
            val withoutTrace = service.analyzeWithDiagnostics(text.substringBefore('.').trim(), InputMode.FULL_LABEL)
            assertTrue("trace should be visible: $text", withTrace.crossContactWarnings.isNotEmpty())
            assertEquals("trace must not change verdict: $text", withoutTrace.result.verdict, withTrace.result.verdict)
            assertTrue("trace must not become a normal token: $text", withTrace.tokens.none { token ->
                token.text.contains("lait", true) || token.text.contains("milk", true) ||
                    token.text.contains("melk", true) || token.text.contains("milch", true)
            })
        }
    }

    @Test
    fun jvmBenchmarkSeparatesLoadingConstructionLookupAndAnalysis() {
        val files = assetPaths()
        val memoryBefore = usedMemory()
        val readTiming = measure("read-json", ::readJson)
        val json = readJson()
        val deserializeTiming = measure("deserialize-knowledge") {
            IngredientKnowledge.fromJson(json.ingredients, json.mappings, json.originRules)
        }
        val benchmarkKnowledge = IngredientKnowledge.fromJson(json.ingredients, json.mappings, json.originRules)
        val memoryAfterKnowledge = usedMemory()
        val indexTiming = measure("build-index") { KnowledgeIndex.from(benchmarkKnowledge) }
        val benchmarkIndex = KnowledgeIndex.from(benchmarkKnowledge)
        val memoryAfterIndex = usedMemory()
        val matcherTiming = measure("build-matcher") { IngredientMatcher(benchmarkKnowledge.ingredients) }
        val benchmarkService = IngredientAnalysisService(benchmarkKnowledge)
        val exactTiming = measure("lookup-exact") { benchmarkIndex.findByAlias("eau") }
        val multilingualTiming = measure("lookup-multilingual") {
            benchmarkIndex.findByAlias("chocoladearoma", LabelLanguage.DUTCH)
        }
        val eNumberTiming = measure("lookup-enumber") { benchmarkIndex.findByENumber("E 471") }
        val collisionTiming = measure("lookup-collision") { benchmarkIndex.findCandidatesInText("E471") }
        val simpleTiming = measure("analyze-simple") {
            benchmarkService.analyze("eau, sucre", InputMode.MANUAL_INGREDIENT_LIST)
        }
        val nestedTiming = measure("analyze-nested") {
            benchmarkService.analyzeWithDiagnostics(
                "sauce (eau, farine de blé, gélatine)",
                InputMode.MANUAL_INGREDIENT_LIST
            )
        }
        val multilingualAnalysisTiming = measure("analyze-multilingual") {
            benchmarkService.analyzeWithDiagnostics(
                "Ingredients EN: water, chocolate flavour, coffee extract",
                InputMode.FULL_LABEL
            )
        }
        val traceTiming = measure("analyze-traces") {
            benchmarkService.analyzeWithDiagnostics(
                "Ingrédients : eau, sucre. Peut contenir : lait, œuf.",
                InputMode.FULL_LABEL
            )
        }
        val diagnosticTiming = measure("generate-diagnostics") {
            benchmarkService.analyzeWithDiagnostics(
                "Ingrédients : sauce (eau, lait, ingrédient inconnu). Peut contenir : œuf.",
                InputMode.FULL_LABEL
            ).decision
        }

        println("PHASE2_FILES|ingredients=${files.ingredientsBytes}|mappings=${files.mappingsBytes}|originRules=${files.originRulesBytes}")
        println("PHASE2_LOGICAL|concepts=${benchmarkIndex.metrics.conceptCount}|forms=${benchmarkIndex.metrics.formCount}|aliasKeys=${benchmarkIndex.metrics.aliasKeyCount}|collisions=${benchmarkIndex.metrics.collisionKeyCount}|invalidTargets=${benchmarkIndex.metrics.invalidCanonicalIdCount}")
        listOf(
            "read-json" to readTiming,
            "deserialize-knowledge" to deserializeTiming,
            "build-index" to indexTiming,
            "build-matcher" to matcherTiming,
            "lookup-exact" to exactTiming,
            "lookup-multilingual" to multilingualTiming,
            "lookup-enumber" to eNumberTiming,
            "lookup-collision" to collisionTiming,
            "analyze-simple" to simpleTiming,
            "analyze-nested" to nestedTiming,
            "analyze-multilingual" to multilingualAnalysisTiming,
            "analyze-traces" to traceTiming,
            "generate-diagnostics" to diagnosticTiming
        ).forEach { (name, timing) -> println("PHASE2_TIMING|$name|${timing.rendered()}") }
        println("PHASE2_MEMORY|usedBefore=$memoryBefore|usedAfterKnowledge=$memoryAfterKnowledge|usedAfterIndex=$memoryAfterIndex|caveat=JVM heap estimate, GC/JIT dependent")
    }

    private data class JsonText(val ingredients: String, val mappings: String, val originRules: String)
    private data class FileSizes(val ingredientsBytes: Long, val mappingsBytes: Long, val originRulesBytes: Long)

    private fun currentObservation(case: CorpusCase): Observation {
        val resolution = knowledge.multilingualLexicon.resolve(case.text, case.language, knowledge.ingredients)
        val matchedText = resolution.correctedText
        val match = matcher.match(IngredientToken(matchedText, depth = 0, order = 0))
        return Observation(
            selectedIds = match.ingredients.map { it.id }.toSet(),
            statusById = match.ingredients.associate { it.id to it.status },
            blockedIds = match.blockedIngredientIds,
            resolution = match.resolution,
            unknown = if (match.resolution == MatchResolution.NONE) case.text else match.residualNormalized
        )
    }

    private data class Observation(
        val selectedIds: Set<String>,
        val statusById: Map<String, VeganStatus>,
        val blockedIds: List<String>,
        val resolution: MatchResolution,
        val unknown: String
    )

    private fun corpus(): List<CorpusCase> = listOf(
        CorpusCase("water", "eau", LabelLanguage.FRENCH),
        CorpusCase("sugar", "sucre", LabelLanguage.FRENCH),
        CorpusCase("salt", "sel", LabelLanguage.FRENCH),
        CorpusCase("wheat", "farine de blé", LabelLanguage.FRENCH),
        CorpusCase("milk", "lait", LabelLanguage.FRENCH),
        CorpusCase("egg", "œuf", LabelLanguage.FRENCH),
        CorpusCase("gelatin", "gélatine", LabelLanguage.FRENCH),
        CorpusCase("honey", "miel", LabelLanguage.FRENCH),
        CorpusCase("coffee", "café", LabelLanguage.FRENCH),
        CorpusCase("strawberry", "fraise", LabelLanguage.FRENCH),
        CorpusCase("chocolate", "chocolat", LabelLanguage.FRENCH),
        CorpusCase("e471", "E471", LabelLanguage.FRENCH),
        CorpusCase("e471-spaced", "E 471", LabelLanguage.FRENCH),
        CorpusCase("e471-functional-class", "émulsifiant : E471", LabelLanguage.FRENCH),
        CorpusCase("bare-number", "471", LabelLanguage.FRENCH),
        CorpusCase("unknown", "ingrédient totalement inconnu", LabelLanguage.FRENCH),
        CorpusCase("french-chocolate", "chocolat", LabelLanguage.FRENCH),
        CorpusCase("dutch-chocolate", "chocolade", LabelLanguage.DUTCH),
        CorpusCase("english-chocolate", "chocolate", LabelLanguage.ENGLISH),
        CorpusCase("german-chocolate", "Schokolade", LabelLanguage.GERMAN),
        CorpusCase("french-flavour", "arôme chocolat", LabelLanguage.FRENCH),
        CorpusCase("english-flavour", "chocolate flavour", LabelLanguage.ENGLISH),
        CorpusCase("dutch-flavour", "chocoladearoma", LabelLanguage.DUTCH),
        CorpusCase("german-flavour", "Schokoladenaroma", LabelLanguage.GERMAN),
        CorpusCase("french-extract", "extrait de café", LabelLanguage.FRENCH),
        CorpusCase("english-extract", "coffee extract", LabelLanguage.ENGLISH),
        CorpusCase("dutch-extract", "koffie-extract", LabelLanguage.DUTCH),
        CorpusCase("nested-sauce", "sauce (eau, farine de blé, gélatine)", LabelLanguage.FRENCH, true),
        CorpusCase("nested-e471", "sauce (eau, E471)", LabelLanguage.FRENCH, true),
        CorpusCase("nested-preparation", "préparation (sucre, arôme chocolat, café)", LabelLanguage.FRENCH, true),
        CorpusCase("nested-tofu", "tofu fumé (soja, nigari, chapelure)", LabelLanguage.FRENCH, true),
        CorpusCase("nested-three-states", "composition (eau, chocolat, ingrédient totalement inconnu)", LabelLanguage.FRENCH, true),
        CorpusCase("trace-fr", "Ingrédients : eau, sucre. Peut contenir : lait, œuf.", LabelLanguage.FRENCH, true, InputMode.FULL_LABEL),
        CorpusCase("trace-nl", "Ingrediënten: water, suiker. Kan bevatten: melk, ei.", LabelLanguage.DUTCH, true, InputMode.FULL_LABEL),
        CorpusCase("trace-en", "Ingredients: water, sugar. May contain: milk, egg.", LabelLanguage.ENGLISH, true, InputMode.FULL_LABEL),
        CorpusCase("trace-de", "Zutaten: Wasser, Zucker. Kann enthalten: Milch, Ei.", LabelLanguage.GERMAN, true, InputMode.FULL_LABEL)
    )

    private fun token(text: String) = IngredientToken(text, depth = 0, order = 0)

    private fun measure(name: String, operation: () -> Any?): Timing {
        repeat(5) { operation() }
        val samples = (1..20).map {
            val started = System.nanoTime()
            operation()
            System.nanoTime() - started
        }.sorted()
        return Timing(samples.first(), samples[samples.lastIndex / 2], samples.last(), samples.size)
    }

    private fun readJson(): JsonText {
        val root = repositoryRoot()
        return JsonText(
            root.resolve("app/src/main/assets/ingredients.json").toFile().readText(),
            root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json").toFile().readText(),
            root.resolve("app/src/main/assets/origin_qualifier_rules.json").toFile().readText()
        )
    }

    private fun assetPaths(): FileSizes {
        val root = repositoryRoot()
        return FileSizes(
            Files.size(root.resolve("app/src/main/assets/ingredients.json")),
            Files.size(root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json")),
            Files.size(root.resolve("app/src/main/assets/origin_qualifier_rules.json"))
        )
    }

    private fun loadCurrentKnowledge(): IngredientKnowledge {
        val json = readJson()
        return IngredientKnowledge.fromJson(json.ingredients, json.mappings, json.originRules)
    }

    private fun repositoryRoot(): Path = generateSequence(Path.of(System.getProperty("user.dir"))) { it.parent }
        .first { Files.exists(it.resolve("app/src/main/assets/ingredients.json")) }

    private fun usedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

}

private fun timingMicros(nanos: Long): Long = (nanos / 1_000.0).roundToLong()
