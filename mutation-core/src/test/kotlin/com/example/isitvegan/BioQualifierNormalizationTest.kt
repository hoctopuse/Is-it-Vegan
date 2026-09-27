package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BioQualifierNormalizationTest {
    private val database = listOf(
        ingredient("wheat_flour", "farine de ble", listOf("farine de ble", "wheat flour", "tarwebloem")),
        ingredient("rapeseed_oil", "huile de colza", listOf("huile de colza", "rapeseed oil")),
        ingredient("sugar", "sucre", listOf("sucre", "sugar", "sucre roux de canne")),
        ingredient("milk", "lait", listOf("lait", "milk"), VeganStatus.NON_VEGAN)
    )

    @Test fun supportedQualifiersAreRemovedOnlyForMatching() {
        assertEquals("farine de ble", OcrIngredientNormalizer.forMatching("farine de blé bio"))
        assertEquals("huile de colza", OcrIngredientNormalizer.forMatching("huile de colza biologique"))
        assertEquals("sucre roux de canne", OcrIngredientNormalizer.forMatching("sucre roux de canne bio"))
        assertEquals("tarwebloem", OcrIngredientNormalizer.forMatching("tarwebloem biologisch"))
        assertEquals("wheat flour", OcrIngredientNormalizer.forMatching("organic wheat flour"))
        assertEquals("farine de ble", OcrIngredientNormalizer.forMatching("Bio-Weizenmehl"))
    }

    @Test fun originalTextRemainsInDiagnostics() {
        val diagnostics = IngredientAnalysisService(IngredientKnowledge(database))
            .analyzeWithDiagnostics("farine de blé bio, huile de colza bio")
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertTrue(diagnostics.tokens.any { it.text == "farine de blé bio" })
        assertTrue(diagnostics.tokens.any { it.text == "huile de colza bio" })
        assertTrue(diagnostics.tokens.all { it.matcherText?.contains("bio") != true })
    }

    @Test fun unknownAndAnimalIngredientsKeepTheirExistingBehavior() {
        val service = IngredientAnalysisService(IngredientKnowledge(database))
        assertEquals(AnalysisVerdict.INCONCLUSIVE, service.analyze("ingrédient mystérieux bio").verdict)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, service.analyze("lait bio").verdict)
    }

    @Test fun tracesAreNotChangedByQualifierNormalization() {
        val result = IngredientAnalysisService(IngredientKnowledge(database))
            .analyzeWithDiagnostics("sucre bio. Peut contenir : lait bio")
        assertEquals(AnalysisVerdict.VEGAN, result.result.verdict)
        assertTrue(result.labelSections.traceSections.isNotEmpty())
    }

    private fun ingredient(
        id: String,
        name: String,
        aliases: List<String>,
        status: VeganStatus = VeganStatus.VEGAN
    ) = Ingredient(id, name, aliases, null, status, "test")
}
