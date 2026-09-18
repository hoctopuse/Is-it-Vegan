package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnknownCollectionHotfixTest {
    private val structuralDatabase = listOf(
        vegan("water", "eau"),
        vegan("soy", "soja", "protéines de soja"),
        vegan("vinegar", "vinaigre"),
        vegan("wheat", "blé")
    )

    @Test fun quantifiedCompositeContainerKeepsResolvedChildrenWithoutBecomingUnknown() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "morceaux végétaliens (36 %) [eau, protéines de soja, vinaigre]",
            structuralDatabase
        )

        val container = diagnostics.tokens.first()
        assertEquals(NodeKind.COMPOSITE_INGREDIENT, container.kind)
        assertTrue(container.compositionAfterQuantity)
        assertFalse(diagnostics.result.unknown.any { it.contains("morceaux végétaliens", true) })
        assertEquals(listOf("water", "soy", "vinegar"), diagnostics.result.matched.map { it.id })
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
    }

    @Test fun partialWheatMatchesKeepTheWholeExpressionUnknown() {
        listOf("amidon de BLÉ", "gluten de BLÉ").forEach { text ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, structuralDatabase)
            val token = diagnostics.tokens.single()

            assertEquals(MatchKind.PARTIAL_CONTEXTUAL, token.matchKind)
            assertEquals(listOf("wheat"), token.matchedIngredientIds)
            assertEquals(listOf(text), diagnostics.result.unknown)
            assertFalse(diagnostics.result.unknown.contains("amidon"))
            assertFalse(diagnostics.result.unknown.contains("gluten"))
            assertEquals(AnalysisVerdict.INCONCLUSIVE, diagnostics.result.verdict)
        }
    }

    @Test fun compositeParentsDoNotDuplicateUnknownChildrenOrKeepNestedContains() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "préparation d’herbes (épices (contient : MOUTARDE))",
            emptyList()
        )

        assertFalse(diagnostics.result.unknown.any { it.contains("préparation d’herbes", true) })
        assertFalse(diagnostics.result.unknown.any { it.contains("contient", true) })
        assertEquals(listOf("MOUTARDE"), diagnostics.result.unknown)
        assertEquals(1, diagnostics.result.unknown.distinct().size)
    }

    @Test fun aGenuinelyUnknownLeafRemainsUnknown() {
        val result = VeganAnalyzer.analyze("substance imaginaire xyz", structuralDatabase)

        assertEquals(listOf("substance imaginaire xyz"), result.unknown)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
    }

    @Test fun diagnosticDistinguishesContextualMatchesFromUnknownLeaves() {
        val contextual = VeganAnalyzer.analyzeWithDiagnostics("amidon de BLÉ", structuralDatabase)
        val contextualReport = DiagnosticReport.build(contextual, "0.5.8.2")
        val unknown = VeganAnalyzer.analyzeWithDiagnostics("substance imaginaire xyz", structuralDatabase)
        val unknownReport = DiagnosticReport.build(unknown, "0.5.8.2")

        assertTrue(contextualReport.contains("correspondance contextuelle=wheat"))
        assertTrue(contextualReport.contains("inconnu=amidon de BLÉ"))
        assertTrue(unknownReport.contains("correspondances=aucune"))
        assertTrue(unknownReport.contains("inconnu=substance imaginaire xyz"))
    }

    private fun vegan(id: String, vararg aliases: String) = Ingredient(
        id, aliases.first(), aliases.toList(), null, VeganStatus.VEGAN, "Test"
    )
}
