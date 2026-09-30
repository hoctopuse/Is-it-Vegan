package com.example.isitvegan

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.roundToLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IndexedCandidateProviderTest {
    private val knowledge by lazy { loadKnowledge() }
    private val index by lazy { KnowledgeIndex.from(knowledge) }
    private val provider by lazy { IndexedCandidateProvider(index) }
    private val matcher by lazy { IngredientMatcher(knowledge.ingredients) }
    private val service by lazy { IngredientAnalysisService(knowledge) }

    @Test
    fun exactMultilingualENumberAndProvenanceQueriesExposeCandidatesOnly() {
        assertContains(provider.findExactAlias("huile de colza"), "rapeseed_oil")
        assertContains(provider.findExactAlias("rapeseed oil"), "rapeseed_oil")
        assertTrue(provider.findMultilingualAlias("raapzaadolie", LabelLanguage.DUTCH).candidates.isEmpty())
        assertContains(provider.findMultilingualAlias("Rapsöl", LabelLanguage.GERMAN), "rapeseed_oil")
        assertContains(provider.findENumber("E471"), "e471")
        assertContains(provider.findENumber("E 471"), "e471")
        assertTrue(provider.findENumber("471").candidates.isEmpty())
        assertTrue(provider.findExactAlias("huile de colza").candidates.all { it.form.normalizedForm.isNotBlank() })
        assertTrue(provider.findMultilingualAlias("Rapsöl", LabelLanguage.GERMAN)
            .candidates.any { it.form.provenance.isNotEmpty() })
        assertFalse(provider.findExactAlias("alias totalement inconnu").collision)
        assertTrue(provider.findExactAlias("alias totalement inconnu").candidates.isEmpty())
    }

    @Test
    fun collisionsInvalidTargetsAndGermanFormsRemainVisible() {
        val e470b = provider.findExactAlias("E470b")
        val e572 = provider.findExactAlias("E572")
        assertContains(e470b, "e470b")
        assertContains(e572, "e572")
        assertEquals(
            setOf(VeganStatus.UNCERTAIN),
            (e470b.candidates + e572.candidates).mapNotNull { it.status }.toSet()
        )
        assertTrue(provider.findMultilingualAlias("Schokoladenaroma", LabelLanguage.GERMAN).candidates.isEmpty())
        assertContains(provider.findMultilingualAlias("Schokoladenkuvertüre", LabelLanguage.GERMAN), "chocolate")

        val cereals = provider.findMultilingualAlias("granen", LabelLanguage.DUTCH)
        assertContains(cereals, "cereals")
        assertTrue(cereals.invalidCanonicalIds.contains("cereals"))
        assertTrue(cereals.candidates.any { !it.canonicalAvailable && it.status == null })
        assertTrue(provider.findExactAlias("E470b").candidates.map { it.canonicalId }.distinct().isNotEmpty())
    }

    @Test
    fun comparatorKeepsMatcherSelectionDiagnosticsAndVerdictAsReference() {
        val cases = listOf(
            "eau" to LabelLanguage.FRENCH,
            "huile de colza" to LabelLanguage.FRENCH,
            "rapeseed oil" to LabelLanguage.ENGLISH,
            "raapzaadolie" to LabelLanguage.DUTCH,
            "Rapsöl" to LabelLanguage.GERMAN,
            "E471" to LabelLanguage.FRENCH,
            "471" to LabelLanguage.FRENCH,
            "arôme chocolat" to LabelLanguage.FRENCH,
            "arôme de chocolat" to LabelLanguage.FRENCH,
            "chocolate flavour" to LabelLanguage.ENGLISH,
            "Schokoladenaroma" to LabelLanguage.GERMAN,
            "extrait de café" to LabelLanguage.FRENCH,
            "goût fraise" to LabelLanguage.FRENCH,
            "lait végétal" to LabelLanguage.FRENCH,
            "produit au miel" to LabelLanguage.FRENCH,
            "E470b" to LabelLanguage.FRENCH,
            "E572" to LabelLanguage.FRENCH,
            "ingrédient totalement inconnu" to LabelLanguage.FRENCH
        )
        cases.forEach { (text, language) -> compareToken(text, language) }

        val labels = listOf(
            "sauce (eau, huile de colza, gélatine)",
            "préparation (sucre, arôme chocolat, café)",
            "composition (eau, chocolat, ingrédient totalement inconnu)",
            "parent (enfant (eau, chocolat), lait) 12,5 %",
            "eau, eau, E471"
        )
        labels.forEach { text ->
            val diagnostics = service.analyzeWithDiagnostics(text, InputMode.MANUAL_INGREDIENT_LIST)
            compareDiagnostics(diagnostics)
        }

        val withTrace = service.analyzeWithDiagnostics(
            "Ingrédients : eau, sucre. Peut contenir : lait, œuf.",
            InputMode.FULL_LABEL
        )
        val withoutTrace = service.analyzeWithDiagnostics(
            "Ingrédients : eau, sucre.",
            InputMode.FULL_LABEL
        )
        compareDiagnostics(withTrace)
        assertEquals(withoutTrace.result.verdict, withTrace.result.verdict)
        assertTrue(withTrace.crossContactWarnings.isNotEmpty())
        assertTrue(withTrace.tokens.none { it.text.contains("lait", true) || it.text.contains("milk", true) })
    }

    @Test
    fun jvmPerformanceSeparatesIndexedLookupMatcherAndFullAnalysis() {
        val exact = measure { provider.findExactAlias("huile de colza") }
        val multilingual = measure { provider.findMultilingualAlias("Schokoladenaroma", LabelLanguage.GERMAN) }
        val eNumber = measure { provider.findENumber("E 471") }
        val candidates = measure { provider.findTextCandidates("arôme chocolat") }
        val matcherPass = measure {
            provider.findTextCandidates("arôme chocolat")
            matcher.match(IngredientToken("arôme chocolat", depth = 0, order = 0))
        }
        val currentAnalysis = measure {
            service.analyzeWithDiagnostics(
                "Ingrédients : sauce (eau, lait, ingrédient inconnu). Peut contenir : œuf.",
                InputMode.FULL_LABEL
            )
        }
        val experimentalProbe = measure {
            val diagnostics = service.analyzeWithDiagnostics(
                "Ingrédients : sauce (eau, lait, ingrédient inconnu). Peut contenir : œuf.",
                InputMode.FULL_LABEL
            )
            diagnostics.tokens.filter { it.kind != NodeKind.COMPOSITE_INGREDIENT }.forEach {
                provider.findTextCandidates(it.matcherText ?: it.text)
            }
            diagnostics
        }
        val diagnostics = measure {
            service.analyzeWithDiagnostics(
                "Ingrédients : parent (eau, chocolat, ingrédient inconnu). Peut contenir : lait.",
                InputMode.FULL_LABEL
            ).decision
        }
        println("PHASE3_JVM_LOGICAL|concepts=${index.metrics.conceptCount}|forms=${index.metrics.formCount}|collisions=${index.metrics.collisionKeyCount}|invalidTargets=${index.metrics.invalidCanonicalIdCount}")
        listOf(
            "exact" to exact,
            "multilingual" to multilingual,
            "enumber" to eNumber,
            "candidate-retrieval" to candidates,
            "candidate-plus-matcher" to matcherPass,
            "current-analysis" to currentAnalysis,
            "experimental-probe" to experimentalProbe,
            "diagnostics" to diagnostics
        ).forEach { (name, timing) -> println("PHASE3_JVM_TIMING|$name|${timing.rendered()}") }
    }

    private fun compareToken(text: String, language: LabelLanguage) {
        val resolution = knowledge.multilingualLexicon.resolve(text, language, knowledge.ingredients)
        val matcherText = resolution.correctedText
        val current = matcher.match(IngredientToken(matcherText, depth = 0, order = 0))
        val indexedCandidates = candidateResults(text, language).flatMap { it.candidates }
        val allCandidates = indexedCandidates.map { it.canonicalId }.toSet()
        assertTrue("$text selected candidates: $allCandidates", allCandidates.containsAll(current.ingredients.map { it.id }))
        assertTrue("$text blocked candidates: $allCandidates", allCandidates.containsAll(current.blockedIngredientIds))
        current.ingredients.forEach { ingredient ->
            assertEquals(ingredient.status, indexedCandidates.first { it.canonicalId == ingredient.id }.status)
        }
    }

    private fun compareDiagnostics(diagnostics: AnalysisDiagnostics) {
        val leafTokens = diagnostics.tokens.filter {
            it.kind != NodeKind.COMPOSITE_INGREDIENT && it.kind != NodeKind.SECTION_HEADING
        }
        leafTokens.forEach { token ->
            val indexedCandidates = candidateResults(token.matcherText ?: token.text, token.matchingLanguage)
                .flatMap { it.candidates }
            val ids = indexedCandidates.map { it.canonicalId }.toSet()
            assertTrue("token ${token.order} candidates", ids.containsAll(token.matchedIngredientIds))
            assertTrue("token ${token.order} blocked", ids.containsAll(token.blockedIngredientIds))
            token.matchedIngredientIds.forEach { id ->
                val indexedStatus = indexedCandidates.first { it.canonicalId == id }.status
                val statusIndex = token.matchedIngredientIds.indexOf(id)
                assertEquals(token.effectiveStatuses.getOrNull(statusIndex), indexedStatus)
            }
        }
        assertTrue(diagnostics.tokens.zipWithNext().all { (first, second) -> first.order < second.order })
        if (diagnostics.tokens.any { it.parentOrder != null }) {
            assertTrue(diagnostics.tokens.any { it.depth > 0 })
            if (diagnostics.verdictExplanation.uncertainIngredients.isNotEmpty()) {
                assertTrue(diagnostics.verdictExplanation.uncertainIngredients.any { it.path.size >= 2 })
            }
        }
        assertEquals(true, diagnostics.decision.tracesExcludedFromVerdict)
    }

    private data class Timing(val min: Long, val median: Long, val max: Long, val repetitions: Int) {
        fun rendered() = "min=${micros(min)}us median=${micros(median)}us max=${micros(max)}us n=$repetitions"
    }

    private fun measure(operation: () -> Any?): Timing {
        repeat(5) { operation() }
        val values = (1..20).map {
            val start = System.nanoTime()
            operation()
            System.nanoTime() - start
        }.sorted()
        return Timing(values.first(), values[values.lastIndex / 2], values.last(), values.size)
    }

    private fun assertContains(result: IndexedCandidateResult, canonicalId: String) {
        assertTrue(
            "${result.originalText} candidates=${result.candidates.map { it.canonicalId }}",
            result.candidates.any { it.canonicalId == canonicalId }
        )
    }

    private fun candidateResults(
        text: String,
        language: LabelLanguage?
    ): List<IndexedCandidateResult> = buildList {
        if (language != null) add(provider.findTextCandidates(text, language))
        add(provider.findTextCandidates(text))
    }

    private fun loadKnowledge(): IngredientKnowledge {
        val root = generateSequence(Path.of(System.getProperty("user.dir"))) { it.parent }
            .first { Files.exists(it.resolve("app/src/main/assets/ingredients.json")) }
        return IngredientKnowledge.fromJson(
            root.resolve("app/src/main/assets/ingredients.json").toFile().readText(),
            root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json").toFile().readText(),
            root.resolve("app/src/main/assets/origin_qualifier_rules.json").toFile().readText()
        )
    }
}

private fun micros(nanos: Long): Long = (nanos / 1_000.0).roundToLong()
