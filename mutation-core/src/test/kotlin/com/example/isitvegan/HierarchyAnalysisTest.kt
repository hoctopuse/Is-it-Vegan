package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HierarchyAnalysisTest {
    private val originRules = OriginQualifierRuleSet.load(
        requireNotNull(javaClass.getResource("/origin_qualifier_rules.json")) {
            "Fixture origin_qualifier_rules.json absente"
        }.readText()
    ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules

    private val database = listOf(
        ingredient("wheat_flour", "farine", "farine de blé"),
        ingredient("water", "eau"),
        ingredient("rapeseed_oil", "huile de colza"),
        ingredient("sunflower_oil", "huile de tournesol"),
        ingredient("vegetable_oil", "huile végétale", "huiles végétales"),
        ingredient("yeast", "levure"),
        ingredient("salt", "sel"),
        ingredient("soy", "soja", "graines de soja", "protéine de soja"),
        ingredient("tofu", "tofu"),
        ingredient("corn", "maïs", "flocons de maïs"),
        ingredient("cheese", "fromage", status = VeganStatus.VEGETARIAN),
        ingredient("milk", "lait", status = VeganStatus.VEGETARIAN),
        ingredient("coconut_milk", "lait de coco"),
        ingredient("grana_padano", "Grana Padano AOP", status = VeganStatus.NON_VEGAN)
    )

    @Test fun compositeParentKeepsChildrenAndDoesNotBecomeAFakeUnknown() {
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "chapelure (farine de blé, eau, huile de colza, levure, sel)", database
        )
        val parent = diagnostics.tokens.first()
        assertEquals(NodeKind.COMPOSITE_INGREDIENT, parent.kind)
        assertNull(parent.parentOrder)
        assertEquals(listOf(0, 0, 0, 0, 0), diagnostics.tokens.drop(1).map { it.parentOrder })
        assertEquals(listOf(1, 1, 1, 1, 1), diagnostics.tokens.drop(1).map { it.depth })
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)

        val unspecified = CoreTestAnalyzer.analyze("chapelure", database)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, unspecified.verdict)
        assertEquals(listOf("chapelure"), unspecified.unknown)
    }

    @Test fun nestedHierarchyPreservesEveryParentRelationship() {
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "chapelure aux flocons de maïs [farine de blé, flocons de maïs, levure, sel, épices (curcuma, paprika)]",
            database
        )
        val breadcrumb = diagnostics.tokens.single { it.text.startsWith("chapelure") }
        val spices = diagnostics.tokens.single { it.text == "épices" }
        val turmeric = diagnostics.tokens.single { it.text == "curcuma" }
        val paprika = diagnostics.tokens.single { it.text == "paprika" }
        assertEquals(NodeKind.COMPOSITE_INGREDIENT, breadcrumb.kind)
        assertEquals(NodeKind.COMPOSITE_INGREDIENT, spices.kind)
        assertEquals(breadcrumb.text, diagnostics.tokens.single { it.order == spices.parentOrder }.text)
        assertEquals(spices.text, diagnostics.tokens.single { it.order == turmeric.parentOrder }.text)
        assertEquals(spices.text, diagnostics.tokens.single { it.order == paprika.parentOrder }.text)
        assertEquals(2, turmeric.depth)
        assertTrue(diagnostics.result.unknown.containsAll(listOf("curcuma", "paprika")))
    }

    @Test fun percentageSectionsAreDiagnosticOnlyAndPointSeparatesThem() {
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "Farce (63 %) : eau. Cœur au tofu fumé 62,6 % : tofu. Enrobage 37.4 % : sel",
            database
        )
        val sections = diagnostics.tokens.filter { it.kind == NodeKind.SECTION_HEADING }
        assertEquals(listOf("Farce", "Cœur au tofu fumé", "Enrobage"), sections.map { it.text })
        assertTrue(sections.all { it.matcherText == null && it.unknown == null })
        assertFalse(diagnostics.tokens.any { it.text.contains("eau. Cœur") })
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun protectedVegetableOilDesignationDoesNotAnalyzeItsParenthesis() {
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "huiles végétales en proportion variable (colza, tournesol)",
            database,
            rules = originRules
        )
        assertEquals(listOf("vegetable_oil"), diagnostics.result.matched.map { it.id })
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(IngredientNodeKind.LEAF, diagnostics.ingredientTree.single().kind)
        assertEquals("huile végétale", diagnostics.tokens.single().matcherText)
        val report = DiagnosticReport.build(diagnostics, "0.5.7")
        assertTrue(report.contains("LEAF | profondeur=0"))
        assertTrue(report.contains("texte matcher=huile végétale"))
        assertFalse(report.contains("parent=1"))
    }

    @Test fun preparationModifiersDisappearOnlyAfterARealMatch() {
        listOf(
            "protéines de soja réhydratées",
            "protéines de soja concentrées",
            "graines de soja dépelliculées",
            "tofu fumé au bois de hêtre"
        ).forEach { text ->
            val result = CoreTestAnalyzer.analyze(text, database)
            assertTrue(text, result.matched.isNotEmpty())
            assertTrue("$text -> ${result.unknown}", result.unknown.isEmpty())
        }
        assertEquals(listOf("protéines de mystère réhydratées"),
            CoreTestAnalyzer.analyze("protéines de mystère réhydratées", database).unknown)
    }

    @Test fun genericAliasesRequireACompleteKnownExpression() {
        val flax = CoreTestAnalyzer.analyze("farine de lin", database)
        assertFalse(flax.matched.any { it.id == "wheat_flour" })
        assertEquals(listOf("farine de lin"), flax.unknown)
        assertEquals(listOf("wheat_flour"), CoreTestAnalyzer.analyze("farine de blé", database).matched.map { it.id })
        assertEquals(listOf("milk"), CoreTestAnalyzer.analyze("lait", database).matched.map { it.id })
        assertEquals(listOf("coconut_milk"), CoreTestAnalyzer.analyze("lait de coco", database).matched.map { it.id })
        assertTrue(CoreTestAnalyzer.analyze("lait de chèvre", database).matched.isEmpty())
        assertEquals(listOf("rapeseed_oil"), CoreTestAnalyzer.analyze("huile de colza", database).matched.map { it.id })
        assertEquals(listOf("grana_padano"), CoreTestAnalyzer.analyze("Grana Padano AOP", database).matched.map { it.id })
    }

    @Test fun compositeParentWordsDoNotDetermineVerdict() {
        val result = CoreTestAnalyzer.analyze("sauce au fromage (eau, huile de colza)", database)
        assertEquals(AnalysisVerdict.VEGAN, result.verdict)
        assertFalse(result.matched.any { it.id == "cheese" })
        assertTrue(result.unknown.isEmpty())
    }

    @Test fun unknownCompositeParentDelegatesToKnownChildren() {
        val result = CoreTestAnalyzer.analyze("farine de lin (eau)", database)
        assertFalse(result.matched.any { it.id == "wheat_flour" })
        assertTrue(result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, result.verdict)
    }

    @Test fun hierarchyNeverConsumesATrailingTrace() {
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "Enrobage 37,4 % : chapelure (farine de blé, eau, sel). Peut contenir du lait",
            database
        )
        assertEquals(listOf("Peut contenir du lait"), diagnostics.crossContactWarnings)
        assertFalse(diagnostics.tokens.any { it.text.contains("lait") })
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
    }

    private fun ingredient(
        id: String,
        vararg aliases: String,
        status: VeganStatus = VeganStatus.VEGAN
    ) = Ingredient(id, aliases.first(), aliases.toList(), null, status, "Test")
}
