package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HierarchyAnalysisTest {
    private val database = listOf(
        ingredient("wheat_flour", "farine", "farine de blé"),
        ingredient("water", "eau"),
        ingredient("rapeseed_oil", "huile de colza"),
        ingredient("sunflower_oil", "huile de tournesol"),
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
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "chapelure (farine de blé, eau, huile de colza, levure, sel)", database
        )
        val parent = diagnostics.tokens.first()
        assertEquals(NodeKind.COMPOSITE_INGREDIENT, parent.kind)
        assertNull(parent.parentOrder)
        assertEquals(listOf(0, 0, 0, 0, 0), diagnostics.tokens.drop(1).map { it.parentOrder })
        assertEquals(listOf(1, 1, 1, 1, 1), diagnostics.tokens.drop(1).map { it.depth })
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)

        val unspecified = VeganAnalyzer.analyze("chapelure", database)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, unspecified.verdict)
        assertEquals(listOf("chapelure"), unspecified.unknown)
    }

    @Test fun nestedHierarchyPreservesEveryParentRelationship() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
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
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "Farce (63 %) : eau. Cœur au tofu fumé 62,6 % : tofu. Enrobage 37.4 % : sel",
            database
        )
        val sections = diagnostics.tokens.filter { it.kind == NodeKind.SECTION_HEADING }
        assertEquals(listOf("Farce", "Cœur au tofu fumé", "Enrobage"), sections.map { it.text })
        assertTrue(sections.all { it.matcherText == null && it.unknown == null })
        assertFalse(diagnostics.tokens.any { it.text.contains("eau. Cœur") })
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun vegetableOilGroupEnrichesOnlyItsSimpleChildren() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "huiles végétales en proportion variable (colza, tournesol)", database
        )
        assertEquals(listOf("rapeseed_oil", "sunflower_oil"), diagnostics.result.matched.map { it.id })
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(
            listOf("huile de colza", "huile de tournesol"),
            diagnostics.tokens.drop(1).map { it.matcherText }
        )
        val report = DiagnosticReport.build(diagnostics, "0.5.7")
        assertTrue(report.contains("COMPOSITE_INGREDIENT | profondeur=0"))
        assertTrue(report.contains("parent=1"))
        assertTrue(report.contains("texte matcher=huile de colza"))
    }

    @Test fun preparationModifiersDisappearOnlyAfterARealMatch() {
        listOf(
            "protéines de soja réhydratées",
            "protéines de soja concentrées",
            "graines de soja dépelliculées",
            "tofu fumé au bois de hêtre"
        ).forEach { text ->
            val result = VeganAnalyzer.analyze(text, database)
            assertTrue(text, result.matched.isNotEmpty())
            assertTrue("$text -> ${result.unknown}", result.unknown.isEmpty())
        }
        assertEquals(listOf("protéines de mystère réhydratées"),
            VeganAnalyzer.analyze("protéines de mystère réhydratées", database).unknown)
    }

    @Test fun genericAliasesRequireACompleteKnownExpression() {
        val flax = VeganAnalyzer.analyze("farine de lin", database)
        assertFalse(flax.matched.any { it.id == "wheat_flour" })
        assertEquals(listOf("farine de lin"), flax.unknown)
        assertEquals(listOf("wheat_flour"), VeganAnalyzer.analyze("farine de blé", database).matched.map { it.id })
        assertEquals(listOf("milk"), VeganAnalyzer.analyze("lait", database).matched.map { it.id })
        assertEquals(listOf("coconut_milk"), VeganAnalyzer.analyze("lait de coco", database).matched.map { it.id })
        assertTrue(VeganAnalyzer.analyze("lait de chèvre", database).matched.isEmpty())
        assertEquals(listOf("rapeseed_oil"), VeganAnalyzer.analyze("huile de colza", database).matched.map { it.id })
        assertEquals(listOf("grana_padano"), VeganAnalyzer.analyze("Grana Padano AOP", database).matched.map { it.id })
    }

    @Test fun knownAnimalWordsInCompositeParentStillDetermineVerdict() {
        val result = VeganAnalyzer.analyze("sauce au fromage (eau, huile de colza)", database)
        assertEquals(AnalysisVerdict.VEGETARIAN, result.verdict)
        assertTrue(result.matched.any { it.id == "cheese" })
        assertTrue(result.unknown.isEmpty())
    }

    @Test fun unknownCompositeParentIsNeverMadeVeganByKnownChildren() {
        val result = VeganAnalyzer.analyze("farine de lin (eau)", database)
        assertFalse(result.matched.any { it.id == "wheat_flour" })
        assertEquals(listOf("farine de lin"), result.unknown)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
    }

    @Test fun hierarchyNeverConsumesATrailingTrace() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
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
