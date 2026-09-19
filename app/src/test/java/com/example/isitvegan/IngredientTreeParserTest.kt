package com.example.isitvegan

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientTreeParserTest {
    private val database = listOf(
        vegan("oats", "flocons d’avoine"),
        vegan("wheat_flour", "farine de blé", "farine"),
        vegan("sunflower_oil", "huile de tournesol"),
        vegan("flax", "graines de lin"),
        vegan("water", "eau"),
        vegan("sugar", "sucre"),
        vegan("mustard", "moutarde"),
        vegan("vinegar", "vinaigre"),
        vegan("onion", "oignons"),
        vegan("rapeseed_oil", "huile de colza"),
        vegan("salt", "sel"),
        vegan("soy", "soja"),
        vegan("tofu", "tofu"),
        vegan("nigari", "nigari"),
        vegan("breadcrumb", "chapelure"),
        vegan("corn", "flocons de maïs"),
        Ingredient("milk", "lait", listOf("lait"), null, VeganStatus.VEGETARIAN, "Test"),
        Ingredient("e471", "E471", listOf("E471"), "E471", VeganStatus.UNCERTAIN, "Test")
    )

    @Test fun regressionLabelBuildsFourRootsAndOnlyUnknownLeaves() {
        val text = "FR — Ingrédients : galettes de céréales 62 % [flocons d’avoine, farine de BLÉ, huile de tournesol, graines de lin], sauce 25 % [eau, tomates, sucre, épices (contient : MOUTARDE), vinaigre], oignons frits 8 % [oignons, huile de colza, farine de BLÉ], sel."
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)
        val roots = diagnostics.ingredientTree

        assertEquals(listOf("galettes de céréales", "sauce", "oignons frits", "sel"), roots.map { it.rawText })
        assertEquals(listOf(IngredientNodeKind.COMPOSITE, IngredientNodeKind.COMPOSITE, IngredientNodeKind.COMPOSITE, IngredientNodeKind.LEAF), roots.map { it.kind })
        assertEquals(BigDecimal("62"), roots[0].quantityPercent)
        assertEquals(BigDecimal("25"), roots[1].quantityPercent)
        assertEquals(BigDecimal("8"), roots[2].quantityPercent)
        assertEquals(17, diagnostics.tokens.size)
        val mustard = diagnostics.tokens.single { it.text.equals("MOUTARDE", true) }
        assertEquals(2, mustard.depth)
        listOf("galettes de céréales", "sauce", "oignons frits").forEach { parent ->
            assertFalse(parent, diagnostics.result.unknown.any { it.equals(parent, true) })
        }
        assertEquals(listOf("tomates"), diagnostics.result.unknown)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, diagnostics.result.verdict)
        val report = DiagnosticReport.build(diagnostics, "0.5.9")
        assertTrue(report.contains("COMPOSITE | profondeur=0 | galettes de céréales | quantité=62 % | enfants=4"))
        assertFalse(report.contains("inconnu=galettes de céréales"))
        assertFalse(report.contains("inconnu=sauce"))
        assertFalse(report.contains("inconnu=oignons frits"))
    }

    @Test fun qualificationParenthesisStaysInsideOneLeaf() {
        val tree = IngredientTreeParser.parse("huile de colza (non hydrogénée), sel")
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "huile de colza (non hydrogénée), sel",
            database
        )

        assertEquals(2, tree.size)
        assertTrue(tree.all { it.kind == IngredientNodeKind.LEAF })
        assertEquals("huile de colza (non hydrogénée)", tree.first().rawText)
        assertEquals(listOf("rapeseed_oil", "salt"), diagnostics.result.matched.map { it.id })
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun nestedQuantitiesStayOnTheirOwnCompositeNodes() {
        val roots = IngredientTreeParser.parse(
            "cœur de tofu fumé 62,6 % (tofu 95 % [soja, eau, nigari]), enrobage 37,4 % [chapelure, flocons de maïs]"
        )

        assertEquals(2, roots.size)
        assertEquals(BigDecimal("62.6"), roots[0].quantityPercent)
        assertEquals(BigDecimal("95"), roots[0].children.single().quantityPercent)
        assertEquals(BigDecimal("37.4"), roots[1].quantityPercent)
        assertEquals(IngredientNodeKind.COMPOSITE, roots[0].kind)
        assertEquals(IngredientNodeKind.COMPOSITE, roots[0].children.single().kind)
        assertEquals(3, roots[0].children.single().children.size)
        assertEquals(2, roots[1].children.size)
    }

    @Test fun additiveKeepsFunctionalClassOutsideMatcherText() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "eau, huile de colza, émulsifiant : E471, sel",
            database
        )
        val additive = diagnostics.ingredientTree.single { it.kind == IngredientNodeKind.ADDITIVE }
        val token = diagnostics.tokens.single { it.nodeKind == IngredientNodeKind.ADDITIVE }

        assertEquals("émulsifiant", additive.functionalClass)
        assertEquals("E471", additive.rawText)
        assertEquals("E471", token.matcherText)
        assertFalse(diagnostics.result.unknown.any { it.contains("émulsifiant", true) })
        assertTrue(diagnostics.result.matched.any { it.id == "e471" })
        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
    }

    @Test fun multilingualFunctionalClassesCreateAdditiveNodes() {
        listOf(
            "émulsifiant : E471" to "E471",
            "émulsifiant: mono- et diglycérides d’acides gras" to "mono- et diglycérides d’acides gras",
            "emulsifier: E471" to "E471",
            "emulgator: E471" to "E471"
        ).forEach { (text, designation) ->
            val node = IngredientTreeParser.parse(text).single()
            assertEquals(text, IngredientNodeKind.ADDITIVE, node.kind)
            assertEquals(text, designation, node.rawText)
            assertTrue(text, node.functionalClass!!.isNotBlank())
        }
    }

    @Test fun animalLeafInsideCompositeStillDeterminesVerdict() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "sauce [eau, crème (LAIT), sel], farine",
            database
        )

        assertEquals(IngredientNodeKind.COMPOSITE, diagnostics.ingredientTree.first().kind)
        assertEquals(2, diagnostics.tokens.single { it.text == "LAIT" }.depth)
        assertTrue(diagnostics.result.matched.any { it.id == "milk" })
        assertEquals(AnalysisVerdict.VEGETARIAN, diagnostics.result.verdict)
        assertFalse(diagnostics.result.unknown.any { it.equals("sauce", true) || it.equals("crème", true) })
    }

    @Test fun tracesNeverEnterTheIngredientTree() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "eau, sel. Peut contenir : lait.",
            database
        )

        assertEquals(listOf("eau", "sel"), diagnostics.tokens.map { it.text })
        assertFalse(diagnostics.result.matched.any { it.id == "milk" })
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertEquals(listOf("Peut contenir : lait."), diagnostics.crossContactWarnings)
    }

    private fun vegan(id: String, vararg aliases: String) = Ingredient(
        id, aliases.first(), aliases.toList(), null, VeganStatus.VEGAN, "Test"
    )
}
