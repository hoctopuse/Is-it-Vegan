package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodHygieneRegulationImportTest {
    private val root = generateSequence(File(requireNotNull(System.getProperty("user.dir"))).canonicalFile) {
        it.parentFile
    }.first { File(it, "settings.gradle.kts").isFile }
    private val knowledge = IngredientKnowledge.fromJson(
        root.resolve("app/src/main/assets/ingredients.json").readText(),
        root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json").readText(),
        root.resolve("app/src/main/assets/origin_qualifier_rules.json").readText()
    )
    private val service = IngredientAnalysisService(knowledge)
    private val cases = listOf(
        "viandes séparées mécaniquement" to LabelLanguage.FRENCH,
        "separatorvlees" to LabelLanguage.DUTCH,
        "mechanically separated meat" to LabelLanguage.ENGLISH,
        "Separatorenfleisch" to LabelLanguage.GERMAN
    )

    @Test fun fourApprovedFormsResolveExactlyAndIdentifyTheAnimalBlocker() {
        val concept = knowledge.ingredients.single { it.id == id }
        assertEquals(VeganStatus.NON_VEGAN, concept.status)
        assertEquals(cases.map { it.first }.toSet(), (concept.aliases + concept.name).toSet())
        assertTrue(knowledge.multilingualLexicon.validateAgainst(knowledge.ingredients).isValid)
        cases.forEach { (surface, language) ->
            assertEquals(surface, id, knowledge.multilingualLexicon.resolve(surface, language, knowledge.ingredients).canonicalId)
            val match = IngredientMatcher(knowledge.ingredients).match(IngredientToken(surface, 0, 0))
            assertEquals(surface, listOf(id), match.ingredients.map { it.id })
            assertEquals(surface, MatchResolution.EXACT, match.resolution)
            assertEquals(surface, "", match.residualNormalized)
            val diagnostics = service.analyzeWithDiagnostics(surface, InputMode.MANUAL_INGREDIENT_LIST)
            assertEquals(surface, listOf(id), diagnostics.result.matched.map { it.id })
            assertTrue(surface, diagnostics.result.unknown.isEmpty())
            assertEquals(surface, AnalysisVerdict.NON_VEGETARIAN, diagnostics.result.verdict)
            assertEquals(surface, listOf(id), diagnostics.decision.responsibleIngredientIds)
            assertEquals(surface, listOf(id), diagnostics.verdictExplanation.knownBlockingIngredients.map { it.ingredientId })
            assertEquals(surface, listOf(id), diagnostics.tokens.flatMap { it.matchedIngredientIds })
            assertTrue(surface, diagnostics.tokens.all { it.unknown == null })
        }
    }

    @Test fun historicalMeatAndNeighboringPlantTermsKeepTheirBehavior() {
        val matcher = IngredientMatcher(knowledge.ingredients)
        val before = IngredientMatcher(knowledge.ingredients.filterNot { it.id == id })
        val beforeService = IngredientAnalysisService(knowledge.copy(
            ingredients = knowledge.ingredients.filterNot { it.id == id }
        ))
        val neighbors = listOf("meat", "pigmeat", "minced meat", "meat preparations", "meat products",
            "poultrymeat preparation", "préparation à base de viande de volaille",
            "viandes hachées", "gehakt vlees", "Hackfleisch", "vegan meat", "plant meat",
            "viande végétale", "vegan gehakt", "tofu", "soja", "cretons", "Frogs' legs", "MSM", "VSM")
        neighbors.forEach { surface ->
            val old = before.match(IngredientToken(surface, 0, 0))
            val actual = matcher.match(IngredientToken(surface, 0, 0))
            assertEquals(surface, old.ingredients.map { it.id }, actual.ingredients.map { it.id })
            assertEquals(surface, old.resolution, actual.resolution)
            assertEquals(surface, old.residualNormalized, actual.residualNormalized)
            assertFalse(surface, actual.ingredients.any { it.id == id })
            val oldResult = beforeService.analyze(surface, InputMode.MANUAL_INGREDIENT_LIST)
            val currentResult = service.analyze(surface, InputMode.MANUAL_INGREDIENT_LIST)
            assertEquals(surface, oldResult.verdict, currentResult.verdict)
            assertEquals(surface, oldResult.matched.map { it.id }, currentResult.matched.map { it.id })
            assertEquals(surface, oldResult.unknown, currentResult.unknown)
        }
        val meat = matcher.match(IngredientToken("meat", 0, 0))
        assertEquals(listOf("meat"), meat.ingredients.map { it.id })
        assertEquals(MatchResolution.EXACT, meat.resolution)
        assertEquals("", meat.residualNormalized)
        assertEquals(listOf("meat"), service.analyzeWithDiagnostics("meat", InputMode.MANUAL_INGREDIENT_LIST).decision.responsibleIngredientIds)
        assertFalse(knowledge.ingredients.any { it.id in setOf("greaves", "frog_legs") })
    }

    @Test fun literalDenominationInsideNegationOrImitationRemainsAnExplicitLimit() {
        // Constructed counterexamples, not real labels: characterize the existing
        // lack of general context protection; do not pretend these are safe cases.
        listOf("sans viandes séparées mécaniquement", "vegan mechanically separated meat").forEach { surface ->
            val match = IngredientMatcher(knowledge.ingredients).match(IngredientToken(surface, 0, 0))
            assertEquals(surface, listOf(id), match.ingredients.map { it.id })
            assertEquals(surface, MatchResolution.PARTIAL_CONTEXTUAL, match.resolution)
            assertTrue(surface, match.residualNormalized.isNotEmpty())
            val diagnostics = service.analyzeWithDiagnostics(surface, InputMode.MANUAL_INGREDIENT_LIST)
            assertEquals(surface, AnalysisVerdict.NON_VEGETARIAN, diagnostics.result.verdict)
            assertEquals(surface, listOf(id), diagnostics.decision.responsibleIngredientIds)
        }
    }

    @Test fun animalPriorityKeepsUnknownDiagnosticsWithoutStoppingTheScan() {
        val diagnostics = service.analyzeWithDiagnostics(
            "separatorvlees, zzzingredient", InputMode.MANUAL_INGREDIENT_LIST
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, diagnostics.result.verdict)
        assertEquals(listOf(id), diagnostics.decision.responsibleIngredientIds)
        assertTrue(diagnostics.result.unknown.isNotEmpty())
        assertFalse(diagnostics.result.stoppedAtNonVegetarian)
    }

    @Test fun collisionNormalizerGoldenCasesUseTheRealKotlinNormalizer() {
        listOf("Œuf Æ" to "oeuf ae", " É-cole " to "e cole",
            "E  120" to "e120", "x E 120" to "x e 120",
            "SEPARAT\u034fORVLEES" to "separatorvlees").forEach { (raw, expected) ->
            assertEquals(raw, expected, TextNormalizer.normalize(raw))
        }
        val mappings = knowledge.multilingualLexicon.runtimeMappings().filter { it.canonicalId == id }
        assertEquals(4, mappings.size)
        assertTrue(mappings.all { it.normalizedForm == TextNormalizer.normalize(it.surfaceForm) })
        assertTrue(mappings.all { it.mappingGroup == "food-hygiene-regulation" && it.relation == "REGULATORY_ALIAS" && it.confidence == "REVIEWED" })
    }

    private companion object { const val id = "mechanically_separated_meat" }
}
