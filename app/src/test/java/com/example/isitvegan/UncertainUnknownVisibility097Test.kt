package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UncertainUnknownVisibility097Test {
    private val knowledge = IngredientKnowledge.fromJson(
        File("src/main/assets/ingredients.json").readText(),
        File("src/main/assets/ingredient_aliases_multilingual.json").readText(),
        File("src/main/assets/origin_qualifier_rules.json").readText()
    )

    @Test fun frenchOcrLabelKeepsUncertainAndTrueUnknownsVisibleAndBlocking() {
        val diagnostics = IngredientAnalysisService(knowledge).analyzeWithDiagnostics(REAL_OCR_LABEL)

        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
        assertTrue(diagnostics.verdictExplanation.uncertainIngredients.any {
            it.path.last().contains("arôme naturel", true)
        })
        listOf("68", "fenugrec", "cumin", "Die", "gomme de guai", "benOate de sodium", "extrait")
            .forEach { expected ->
                assertTrue("inconnu attendu : $expected", diagnostics.visibleUnknownIngredients.any {
                    it.contains(expected, true)
                })
                assertTrue(diagnostics.ingredientGroups.unknownIngredients.any { it.contains(expected, true) })
                assertTrue(diagnostics.decision.unknownIngredients.any { it.contains(expected, true) })
            }
        val visibleAggregates = diagnostics.visibleUnknownIngredients +
            diagnostics.ingredientGroups.unknownIngredients + diagnostics.decision.unknownIngredients
        assertFalse(visibleAggregates.any { it.contains("amidon modifié", true) })
        assertTrue(diagnostics.tokens.first { it.text.contains("amidon modifié", true) }
            .effectiveStatuses.let { statuses -> statuses.isNotEmpty() && statuses.all { it == VeganStatus.VEGAN } })
        listOf("purée de tomates mi-réduife", "vinaigre d’alcool", "graines d8 moutarde")
            .forEach { recognized ->
                assertFalse("reconnu affiché comme inconnu : $recognized", visibleAggregates.any {
                    it.contains(recognized, true)
                })
            }
        assertNull(diagnostics.verdictExplanation.conditionalVerdict)
        assertEquals(
            ConditionalVerdictReason.UNKNOWN_INGREDIENT_REMAINS,
            diagnostics.verdictExplanation.conditionalReason
        )
        assertTrue(diagnostics.crossContactWarnings.isEmpty())
    }

    private companion object {
        val REAL_OCR_LABEL = """INGREDIENTS: Eau, 68 purée de tomates mi-réduife 17%, vinaigre d'alcool, pomme, amidon modifié, curry 2,5% (Curcuma, Coriandre, fenugrec, graines d8 moutarde, poivre blanc, gingembre, graines de carvi, cumin, poivre de Jamaique, muscade, céleri, fenoul, Iveche, paprka en poudre, poivte uo Cayenne, cloUs de airofle), sel, sauce de soja (eau, graines de soja, Die, sel), épaississants (gomme de guai, gomme xanthane), conservateuts (sorbate de potassium, benOate de sodium), arôme naturel, extrait de piment."""
    }
}
