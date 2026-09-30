package com.example.isitvegan

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeIndexTest {
    private val knowledge by lazy { loadCurrentKnowledge() }
    private val index by lazy { KnowledgeIndex.from(knowledge) }

    @Test
    fun currentJsonConvertsAllConceptsAndMappingsWithoutChangingIdentity() {
        assertEquals(479, knowledge.ingredients.size)
        assertEquals(479, index.metrics.conceptCount)
        assertEquals(knowledge.ingredients.map { it.id }, index.concepts.map { it.id })
        assertEquals(knowledge.ingredients.map { it.status }, index.concepts.map { it.status })
        assertEquals(1996, knowledge.multilingualLexicon.runtimeMappings().size)
        assertEquals(listOf("cereals"), index.invalidCanonicalIds)
    }

    @Test
    fun historicalAliasesLanguagesAndOcrVariantsRemainSearchable() {
        assertTrue(index.findByAlias("eau").any { it.canonicalId == "water" })
        assertTrue(index.findByAlias("sucre").any { it.canonicalAvailable })
        assertTrue(index.findByAlias("lait", LabelLanguage.FRENCH).any { it.canonicalId == "milk" })
        assertTrue(index.findByAlias("milk", LabelLanguage.ENGLISH).isNotEmpty())
        assertTrue(index.findByAlias("melk", LabelLanguage.DUTCH).isNotEmpty())
        assertTrue(index.findByAlias("Milch", LabelLanguage.GERMAN).isNotEmpty())
        assertTrue(knowledge.multilingualLexicon.runtimeMappings().any { it.language == LabelLanguage.ITALIAN })
        assertTrue(knowledge.multilingualLexicon.runtimeMappings().any { it.language == LabelLanguage.SPANISH })
        assertTrue(index.findByAlias("cereals", LabelLanguage.ENGLISH).isEmpty())
        assertTrue(index.findByAlias("granen", LabelLanguage.DUTCH).any { it.canonicalId == "cereals" })
        assertTrue(index.findByAlias("orge", LabelLanguage.FRENCH).any { it.provenance.isNotEmpty() })
        assertTrue(index.metrics.formCount > knowledge.ingredients.size)
        assertTrue(index.metrics.buildDurationNanos >= 0)
    }

    @Test
    fun invalidTargetsAndCollisionsAreVisibleWithoutDeduplication() {
        assertEquals(listOf("cereals"), index.invalidCanonicalIds)
        assertTrue(index.findByAlias("E471").isNotEmpty())
        assertTrue(index.collisionKeys().isNotEmpty())
        assertTrue(index.findByConcept("cereals").all { !it.canonicalAvailable })
        assertTrue(index.findByENumber("E 471").all { it.type == RuntimeFormType.E_NUMBER })
    }

    @Test
    fun occurrenceContractKeepsIdentityHierarchyStatusAndTraceSeparate() {
        val parent = RuntimeOccurrence(
            occurrenceId = "token:0",
            originalText = "préparation",
            normalizedText = "preparation",
            order = 0,
            state = RuntimeRecognitionState.RECOGNIZED,
            compositionPath = listOf("préparation")
        )
        val first = RuntimeOccurrence(
            occurrenceId = "token:1",
            originalText = "lait",
            normalizedText = "lait",
            order = 1,
            conceptId = "milk",
            depth = 1,
            parentOrder = 0,
            compositionPath = listOf("préparation", "lait"),
            percentage = java.math.BigDecimal("12.5"),
            state = RuntimeRecognitionState.RECOGNIZED_UNCERTAIN
        )
        val second = first.copy(occurrenceId = "token:2", order = 2)
        val trace = RuntimeOccurrence(
            occurrenceId = "trace:0",
            originalText = "lait",
            normalizedText = "lait",
            order = 3,
            state = RuntimeRecognitionState.UNKNOWN,
            trace = true
        )
        assertEquals(listOf("préparation", "lait"), first.compositionPath)
        assertEquals(2, setOf(first.occurrenceId, second.occurrenceId).size)
        assertTrue(trace.trace)
        assertFalse(trace.declaredPresence)
    }

    @Test
    fun matchingParityUsesTheExistingMatcherAndLeavesFlavourAndUnknownBoundariesIntact() {
        val matcher = IngredientMatcher(knowledge.ingredients)
        val corpus = listOf(
            "eau" to LabelLanguage.FRENCH,
            "sucre" to LabelLanguage.FRENCH,
            "lait" to LabelLanguage.FRENCH,
            "gélatine" to LabelLanguage.FRENCH,
            "E471" to LabelLanguage.FRENCH,
            "chocolat" to LabelLanguage.FRENCH,
            "arôme chocolat" to LabelLanguage.FRENCH,
            "extrait de café" to LabelLanguage.FRENCH,
            "fraise" to LabelLanguage.FRENCH,
            "chocoladearoma" to LabelLanguage.DUTCH,
            "Schokoladenaroma" to LabelLanguage.GERMAN
        )
        corpus.forEach { (text, language) ->
            val resolution = knowledge.multilingualLexicon.resolve(text, language, knowledge.ingredients)
            val token = IngredientToken(resolution.correctedText, depth = 0, order = 0)
            val currentIds = matcher.match(token).ingredients.map { it.id }.toSet()
            val indexedIds = (index.findCandidatesInText(text, language) + index.findCandidatesInText(text))
                .map { it.canonicalId }.toSet()
            assertTrue("$text: $currentIds vs $indexedIds", indexedIds.containsAll(currentIds))
        }
        assertTrue(index.findByAlias("texte totalement inconnu").isEmpty())
        assertTrue(knowledge.ingredients.first { it.id == "milk" }.status != VeganStatus.VEGAN)
    }

    @Test
    fun diagnosticHierarchyAndTraceRemainOnTheExistingPath() {
        val service = IngredientAnalysisService(knowledge)
        val diagnostics = service.analyzeWithDiagnostics(
            "Ingrédients: sucre, préparation (lait, ingrédient inconnu). Peut contenir: lait",
            InputMode.FULL_LABEL
        )
        assertTrue(diagnostics.tokens.any { it.parentOrder != null })
        assertTrue(diagnostics.tokens.any { it.unknown != null })
        assertTrue(diagnostics.crossContactWarnings.any { it.contains("lait", ignoreCase = true) })
        assertTrue(diagnostics.tokens.none { it.text.contains("Peut contenir", ignoreCase = true) })
        assertTrue(diagnostics.result.unknown.isNotEmpty())
    }

    private fun loadCurrentKnowledge(): IngredientKnowledge {
        val root = generateSequence(Path.of(System.getProperty("user.dir"))) { it.parent }
            .first { Files.exists(it.resolve("app/src/main/assets/ingredients.json")) }
        return IngredientKnowledge.fromJson(
            root.resolve("app/src/main/assets/ingredients.json").toFile().readText(),
            root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json").toFile().readText(),
            root.resolve("app/src/main/assets/origin_qualifier_rules.json").toFile().readText()
        )
    }
}
