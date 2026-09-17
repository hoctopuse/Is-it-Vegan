package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserModulesTest {
    @Test fun quantityCleanerDropsValuesButKeepsIdentifiers() {
        val cleaned = QuantityCleaner.clean(
            "2,9% CAJOU, E 471, vitamine B 12, D 2, oméga 3, 250 ml"
        )
        assertFalse(cleaned.contains("2,9"))
        assertFalse(cleaned.contains("250"))
        assertTrue(cleaned.contains("E471"))
        assertTrue(cleaned.contains("B12"))
        assertTrue(cleaned.contains("D2"))
        assertTrue(cleaned.contains("oméga-3"))
    }

    @Test fun preprocessorRemovesTracesFootnotesAndFunctionalHeadings() {
        val preprocessed = LabelPreprocessor.preprocess(
            """Ingrédients: cacao¹ 2,9%, stabilisant (pectines), E 471. """ +
                "Peut contenir du lait. ¹Rainforest Alliance Certified. Find out more."
        )
        val cleaned = QuantityCleaner.clean(preprocessed.compositionText)
        assertFalse(cleaned.contains("2,9"))
        assertFalse(cleaned.contains("stabilisant", ignoreCase = true))
        assertFalse(cleaned.contains("Peut contenir", ignoreCase = true))
        assertFalse(cleaned.contains("Rainforest", ignoreCase = true))
        assertTrue(cleaned.contains("cacao"))
        assertTrue(cleaned.contains("pectines"))
        assertTrue(cleaned.contains("E471"))
    }

    @Test fun tokenizerRetainsNestedDepth() {
        val tokens = IngredientTokenizer.tokenize("ricotta (lait, sel), farine de blé")
        assertEquals(
            listOf(
                "ricotta" to 0,
                "lait" to 1,
                "sel" to 1,
                "farine de blé" to 0
            ),
            tokens.map { it.text to it.depth }
        )
    }

    @Test fun matcherGivesPriorityToTheLongestOverlappingAlias() {
        val database = listOf(
            ingredient("milk", "lait", VeganStatus.VEGETARIAN),
            ingredient("coconut_milk", "lait de coco", VeganStatus.VEGAN)
        )
        val match = IngredientMatcher(database).match(IngredientToken("lait de coco", 0, 0))
        assertEquals(listOf("coconut_milk"), match.ingredients.map { it.id })
        assertEquals("", match.residualNormalized)
    }

    @Test fun matcherFindsKnownIngredientsInsideReviewedModifiers() {
        val database = listOf(
            ingredient("grana", "Grana Padano AOP", VeganStatus.NON_VEGAN),
            ingredient("rapeseed", "huile de colza", VeganStatus.VEGAN),
            ingredient("peas", "pois", VeganStatus.VEGAN)
        )
        val matcher = IngredientMatcher(database)
        val grana = matcher.match(IngredientToken("flocons de Grana Padano AOP", 0, 0))
        val rapeseed = matcher.match(IngredientToken("huile de colza non hydrogénée", 0, 1))
        val peaProtein = matcher.match(IngredientToken("protéine de pois", 0, 2))
        assertEquals(listOf("grana"), grana.ingredients.map { it.id })
        assertEquals(listOf("rapeseed"), rapeseed.ingredients.map { it.id })
        assertEquals(listOf("peas"), peaProtein.ingredients.map { it.id })
        assertNull(UnknownCollector.collect(grana))
        assertNull(UnknownCollector.collect(rapeseed))
        assertNull(UnknownCollector.collect(peaProtein))
    }

    @Test fun unknownCollectorKeepsOnlyMeaningfulResidualWords() {
        val database = listOf(ingredient("milk", "lait", VeganStatus.VEGETARIAN))
        val match = IngredientMatcher(database).match(IngredientToken("mystère au lait", 0, 0))
        assertEquals("mystere", UnknownCollector.collect(match))
    }

    @Test fun verdictEngineKeepsTheExistingPrecedence() {
        val vegan = ingredient("sugar", "sucre", VeganStatus.VEGAN)
        val vegetarian = ingredient("milk", "lait", VeganStatus.VEGETARIAN)
        val uncertain = ingredient("e471", "E471", VeganStatus.UNCERTAIN)
        val animal = ingredient("gelatin", "gélatine", VeganStatus.NON_VEGAN)
        assertEquals(AnalysisVerdict.VEGAN, VerdictEngine.evaluate(listOf(vegan), emptyList()))
        assertEquals(AnalysisVerdict.VEGETARIAN,
            VerdictEngine.evaluate(listOf(vegan, vegetarian), emptyList()))
        assertEquals(AnalysisVerdict.UNCERTAIN,
            VerdictEngine.evaluate(listOf(vegan, uncertain), emptyList()))
        assertEquals(AnalysisVerdict.NON_VEGETARIAN,
            VerdictEngine.evaluate(listOf(uncertain, animal), listOf("mystère")))
    }

    private fun ingredient(id: String, alias: String, status: VeganStatus) =
        Ingredient(id, alias, listOf(alias), null, status, "Test")
}
