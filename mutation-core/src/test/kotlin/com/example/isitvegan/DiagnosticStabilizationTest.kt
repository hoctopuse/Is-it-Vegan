package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticStabilizationTest {
    private val service = IngredientAnalysisService(
        IngredientKnowledge(
            listOf(
                ingredient("water", "eau", VeganStatus.VEGAN),
                ingredient("sugar", "sucre", VeganStatus.VEGAN),
                ingredient("milk", "lait", VeganStatus.VEGETARIAN),
                ingredient("gelatin", "gélatine", VeganStatus.NON_VEGAN),
                ingredient("e471", "E471", VeganStatus.UNCERTAIN)
            )
        )
    )

    @Test fun tracesAreStructuredButExplicitlyExcludedFromDecision() {
        val diagnostics = analyze(
            "Ingrédients : eau. Peut contenir : lait. May contain traces of gélatine."
        )

        assertEquals(2, diagnostics.labelSections.traceSections.size)
        assertTrue(diagnostics.crossContactWarnings.single().contains("lait"))
        assertTrue(diagnostics.crossContactWarnings.single().contains("gélatine"))
        assertEquals(listOf("water"), diagnostics.ingredientGroups.veganIngredientIds)
        assertTrue(diagnostics.ingredientGroups.vegetarianIngredientIds.isEmpty())
        assertEquals(VeganAssessment.VEGAN, diagnostics.decision.veganAssessment)
        assertTrue(diagnostics.decision.tracesExcludedFromVerdict)
    }

    @Test fun unknownAndKnownAnimalCauseAreExplicit() {
        val unknown = analyze("Ingrédients : eau, poudre mystérieuse.")
        val animal = analyze("Ingrédients : eau, gélatine.")

        assertEquals(listOf("poudre mystérieuse"), unknown.ingredientGroups.unknownIngredients)
        assertEquals(DecisionReason.UNKNOWN_INGREDIENT, unknown.decision.reason)
        assertTrue(unknown.decision.unknownPreventsVegan)
        assertEquals(listOf("gelatin"), animal.ingredientGroups.nonVegetarianIngredientIds)
        assertEquals(DecisionReason.KNOWN_VEGAN_BLOCKER, animal.decision.reason)
        assertEquals(listOf("gelatin"), animal.decision.responsibleIngredientIds)
        assertEquals(VeganAssessment.NOT_VEGAN, animal.decision.veganAssessment)
    }

    @Test fun nestedCompositionLanguageAndSectionRemainStructured() {
        val diagnostics = analyze("FR: Ingrédients : préparation 60 % (eau 80 %, lait 20 %), sucre.")
        val composite = diagnostics.tokens.first { it.nodeKind == IngredientNodeKind.COMPOSITE }
        val milk = diagnostics.tokens.first { "milk" in it.matchedIngredientIds }
        val ingredientSection = requireNotNull(diagnostics.labelSections.ingredientSection)

        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertTrue(diagnostics.labelSections.hasIngredientHeading)
        assertTrue(ingredientSection.rawText.contains("préparation"))
        assertTrue(ingredientSection.end > ingredientSection.start)
        assertEquals("FR", diagnostics.languageSegmentation.detectedMarker)
        assertEquals(2, composite.childCount)
        assertEquals(composite.order, milk.parentOrder)
        assertEquals("20", milk.quantityPercent?.stripTrailingZeros()?.toPlainString())
    }

    @Test fun diagnosticEnrichmentDoesNotChangeAnalysisResult() {
        val text = "Ingrédients : eau, E471, terme inconnu. Peut contenir : lait."
        val plain = service.analyze(text, InputMode.FULL_LABEL)
        val diagnostic = service.analyzeWithDiagnostics(text, InputMode.FULL_LABEL)

        assertEquals(plain, diagnostic.result)
        assertTrue(diagnostic.decision.tracesExcludedFromVerdict)
    }

    @Test fun emptyTruncatedAndIncoherentInputsHaveStableWarningCodes() {
        val empty = analyze("")
        val truncated = analyze(
            "FR: Ingrédients : riz (complet\nNL: Ingrediënten: eau, sucre."
        )
        val incoherent = analyze("Ingrédients : eau (sucre")

        assertEquals(listOf(DiagnosticInputWarning.EMPTY_INPUT), empty.inputWarnings)
        assertTrue(DiagnosticInputWarning.MANIFESTLY_TRUNCATED_BLOCK in truncated.inputWarnings)
        assertTrue(DiagnosticInputWarning.UNBALANCED_STRUCTURE in incoherent.inputWarnings)
    }

    @Test fun readableReportStatesDecisionAndTraceInvariants() {
        val diagnostics = analyze("Ingrédients : eau, terme inconnu. Peut contenir : lait.")
        val report = DiagnosticReport.build(diagnostics, "test")

        assertTrue(report.contains("Influence des traces sur le verdict : aucune"))
        assertTrue(report.contains("Raison de décision :"))
        assertTrue(report.contains("Un ingrédient inconnu empêche un verdict VEGAN : oui"))
        assertTrue(report.contains("Traces prises en compte dans le verdict : non"))
    }

    private fun analyze(text: String): AnalysisDiagnostics =
        service.analyzeWithDiagnostics(text, InputMode.FULL_LABEL)

    private fun ingredient(id: String, alias: String, status: VeganStatus) = Ingredient(
        id = id,
        name = alias,
        aliases = listOf(alias),
        status = status,
        reason = "test"
    )
}
