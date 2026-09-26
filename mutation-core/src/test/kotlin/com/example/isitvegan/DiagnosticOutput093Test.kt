package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticOutput093Test {
    private val database = listOf(
        ingredient("bell_pepper", "poivron", "poivron"),
        ingredient("tomato", "tomate", "tomate"),
        ingredient("cashew", "noix de cajou", "noix de cajou"),
        ingredient("walnut", "noix", "noix"),
        ingredient("rapeseed_oil", "huile de colza", "huile de colza"),
        ingredient("vinegar", "vinaigre", "vinaigre"),
        ingredient("water", "eau", "eau"),
        ingredient("salt", "sel", "sel")
    )

    @Test fun contextualKnownIngredientsAreNotAlsoUnknown() {
        val diagnostics = analyze("poivron rouge, tomate séchée au soleil, mystère")

        assertEquals(
            setOf("bell_pepper", "tomato"),
            diagnostics.result.matched.map { it.id }.toSet()
        )
        assertEquals(listOf("mystère"), diagnostics.result.unknown)
        assertTrue(diagnostics.result.matched.none { ingredient ->
            diagnostics.result.unknown.any { unknown ->
                TextNormalizer.normalize(unknown).contains(TextNormalizer.normalize(ingredient.name))
            }
        })
        assertEquals(AnalysisVerdict.INCONCLUSIVE, diagnostics.result.verdict)
    }

    @Test fun realisticCompositionKeepsKnownAndUnknownListsConsistent() {
        val diagnostics = analyze(
            "poivron rouge 53%, noix de cajou 8,8%, noix 8,8%, huile de colza, " +
                "tomate séchée au soleil, sirop de grenade [concentré de grenade 50%, " +
                "sucre, acidifiant (E330), épices (paprika, cumin), ail, " +
                "piment (piment rouge, sel, vinaigre, eau, fibres d’agrumes)], sel, E202, E412, E415. " +
                "Peut contenir : lait, œuf."
        )

        assertTrue(diagnostics.result.matched.map { it.id }.containsAll(
            listOf("bell_pepper", "cashew", "walnut", "rapeseed_oil", "tomato", "vinegar", "water", "salt")
        ))
        assertTrue(diagnostics.result.unknown.isNotEmpty())
        assertFalse(diagnostics.result.unknown.any { it.contains("poivron rouge", true) })
        assertFalse(diagnostics.result.unknown.any { it.contains("tomate séchée", true) })
        assertTrue(diagnostics.crossContactWarnings.any { it.contains("lait", true) })
        assertTrue(diagnostics.result.unknown.none { it.contains("Peut contenir", true) })
    }

    @Test fun reportUsesPipeForStructuredListsAndEscapesSourcePipe() {
        val diagnostics = analyze("poivron rouge, tomate séchée au soleil, mystère, autre | inconnu")
        val report = DiagnosticReport.build(diagnostics, "0.6.9.3")

        assertTrue(report.contains("Reconnus : bell_pepper (VEGAN) |"))
        assertTrue(report.contains("Inconnus : "))
        assertTrue(report.contains("\\|"))
        assertFalse(report.contains("Reconnus : bell_pepper (VEGAN),"))
    }

    @Test fun tracesDoNotEnterStructuredIngredientLists() {
        val diagnostics = analyze("poivron rouge. Peut contenir : lait, œuf.")
        val report = DiagnosticReport.build(diagnostics, "0.6.9.3")

        assertTrue(diagnostics.result.unknown.isEmpty())
        assertTrue(diagnostics.crossContactWarnings.isNotEmpty())
        assertTrue(report.contains("Traces prises en compte dans le verdict : non"))
        assertFalse(report.substringAfter("Inconnus :").substringBefore("Éléments responsables").contains("lait"))
    }

    private fun analyze(text: String) = IngredientAnalysisService(IngredientKnowledge(database))
        .analyzeWithDiagnostics(text)

    private fun ingredient(id: String, name: String, alias: String) =
        Ingredient(id, name, listOf(alias), null, VeganStatus.VEGAN, "test")
}
