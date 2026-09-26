package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VerdictExplanation069Test {
    private val database = listOf(
        ingredient("water", "eau", VeganStatus.VEGAN),
        ingredient("oil", "huile de colza", VeganStatus.VEGAN),
        ingredient("milk", "lait", VeganStatus.VEGETARIAN),
        ingredient("gelatin", "gélatine", VeganStatus.NON_VEGAN),
        ingredient("flavour", "arôme", VeganStatus.UNCERTAIN),
        ingredient("spices", "épices", VeganStatus.UNCERTAIN),
        ingredient("extract", "extrait végétal non identifié", VeganStatus.UNCERTAIN)
    )
    private val service = IngredientAnalysisService(IngredientKnowledge(database))

    @Test fun uncertainCompositionExposesAVeganConditionalVerdict() {
        val diagnostics = analyze("eau, huile de colza, arôme")
        val explanation = diagnostics.verdictExplanation

        assertEquals(AnalysisVerdict.UNCERTAIN, explanation.mainVerdict)
        assertEquals(listOf("flavour"), explanation.uncertainIngredients.map { it.ingredientId })
        assertEquals(AnalysisVerdict.VEGAN, explanation.conditionalVerdict)
        assertEquals(
            ConditionalVerdictReason.UNCERTAIN_INGREDIENTS_EXCLUDED,
            explanation.conditionalReason
        )
    }

    @Test fun vegetarianKnownRemainderIsReportedAsConditionalInformation() {
        val explanation = analyze("eau, lait, arôme").verdictExplanation

        assertEquals(AnalysisVerdict.UNCERTAIN, explanation.mainVerdict)
        assertEquals(AnalysisVerdict.VEGETARIAN, explanation.conditionalVerdict)
        assertEquals(AnalysisVerdict.VEGETARIAN, explanation.vegetarianStatus)
        assertEquals(listOf("milk"), explanation.knownBlockingIngredients.map { it.ingredientId })
    }

    @Test fun knownNonVegetarianIngredientBlocksTheConditionalResult() {
        val explanation = analyze("eau, gélatine, arôme").verdictExplanation

        assertEquals(AnalysisVerdict.NON_VEGETARIAN, explanation.mainVerdict)
        assertEquals(listOf("gelatin"), explanation.knownBlockingIngredients.map { it.ingredientId })
        assertEquals(listOf("flavour"), explanation.uncertainIngredients.map { it.ingredientId })
        assertNull(explanation.conditionalVerdict)
        assertEquals(
            ConditionalVerdictReason.KNOWN_NON_VEGETARIAN_INGREDIENT_REMAINS,
            explanation.conditionalReason
        )
    }

    @Test fun multipleKnownBlockersAndUncertainOccurrencesRemainSeparated() {
        val diagnostics = analyze("gélatine, gélatine, arôme, épices")
        val explanation = diagnostics.verdictExplanation

        assertEquals(AnalysisVerdict.NON_VEGETARIAN, diagnostics.result.verdict)
        assertEquals(
            listOf("gelatin", "gelatin"),
            explanation.knownBlockingIngredients.map { it.ingredientId }
        )
        assertEquals(
            listOf("flavour", "spices"),
            explanation.uncertainIngredients.map { it.ingredientId }
        )
        assertNull(explanation.conditionalVerdict)
    }

    @Test fun explicitNonVeganOriginKeepsItsMainVerdictAndBlocksConditionalResult() {
        val rules = OriginQualifierRuleSet.load(
            File("src/test/resources/origin_qualifier_rules.json").readText()
        ).rules
        val originDatabase = database + ingredient("e471", "E471", VeganStatus.UNCERTAIN)
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "E471 d’origine animale, arôme",
            originDatabase,
            rules = rules
        )
        val explanation = diagnostics.verdictExplanation

        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
        assertEquals(VeganAssessment.NOT_VEGAN, diagnostics.result.veganAssessment)
        assertEquals(listOf("e471"), diagnostics.result.originNonVeganIngredientIds)
        assertEquals(listOf("e471"), explanation.knownBlockingIngredients.map { it.ingredientId })
        assertTrue(explanation.uncertainIngredients.any { it.ingredientId == "flavour" })
        assertNull(explanation.conditionalVerdict)
        assertEquals(
            ConditionalVerdictReason.KNOWN_NON_VEGETARIAN_INGREDIENT_REMAINS,
            explanation.conditionalReason
        )
    }

    @Test fun everyUncertainIngredientIsPreserved() {
        val explanation = analyze("eau, arôme, épices, extrait végétal non identifié").verdictExplanation

        assertEquals(
            listOf("flavour", "spices", "extract"),
            explanation.uncertainIngredients.map { it.ingredientId }
        )
        assertEquals(AnalysisVerdict.VEGAN, explanation.conditionalVerdict)
        assertEquals(
            ConditionalVerdictReason.UNCERTAIN_INGREDIENTS_EXCLUDED,
            explanation.conditionalReason
        )
    }

    @Test fun identicalUncertainOccurrencesArePreservedInSourceOrder() {
        val explanation = analyze("eau, arôme, arôme").verdictExplanation

        assertEquals(listOf("flavour", "flavour"), explanation.uncertainIngredients.map { it.ingredientId })
        assertEquals(listOf(listOf("arôme"), listOf("arôme")), explanation.uncertainIngredients.map { it.path })
        assertEquals(2, explanation.uncertainIngredients.map { it.occurrenceId }.distinct().size)
        assertEquals(AnalysisVerdict.VEGAN, explanation.conditionalVerdict)
    }

    @Test fun identicalOccurrencesUnderIdenticalPathsRemainDistinct() {
        val explanation = analyze("préparation [arôme], préparation [arôme]").verdictExplanation

        assertEquals(2, explanation.uncertainIngredients.size)
        assertEquals(
            listOf(
                listOf("préparation", "arôme"),
                listOf("préparation", "arôme")
            ),
            explanation.uncertainIngredients.map { it.path }
        )
        assertEquals(2, explanation.uncertainIngredients.map { it.occurrenceId }.distinct().size)
    }

    @Test fun occurrencesUnderDifferentPathsRemainDistinctAndOrdered() {
        val explanation = analyze("préparation [arôme], garniture [arôme]").verdictExplanation

        assertEquals(
            listOf(
                listOf("préparation", "arôme"),
                listOf("garniture", "arôme")
            ),
            explanation.uncertainIngredients.map { it.path }
        )
        assertEquals(listOf("flavour", "flavour"), explanation.uncertainIngredients.map { it.ingredientId })
    }

    @Test fun nestedUncertainIngredientKeepsItsFullPath() {
        val explanation = analyze(
            "préparation végétale [eau, arôme], huile de colza"
        ).verdictExplanation

        assertEquals(
            listOf("préparation végétale", "arôme"),
            explanation.uncertainIngredients.single().path
        )
        assertEquals(AnalysisVerdict.VEGAN, explanation.conditionalVerdict)
    }

    @Test fun tracesAreExcludedFromTheConditionalCalculation() {
        val diagnostics = analyze("eau, arôme. Peut contenir : lait, gélatine")
        val explanation = diagnostics.verdictExplanation

        assertEquals(AnalysisVerdict.VEGAN, explanation.conditionalVerdict)
        assertTrue(explanation.tracesExcludedFromConditionalVerdict)
        assertTrue(diagnostics.crossContactWarnings.single().contains("lait"))
        assertTrue(explanation.knownBlockingIngredients.isEmpty())
    }

    @Test fun explanationDoesNotChangeTheMainVerdict() {
        listOf(
            "eau, arôme",
            "eau, lait, arôme",
            "eau, gélatine, arôme",
            "eau, mot inconnu"
        ).forEach { text ->
            val plain = service.analyze(text)
            val diagnostics = analyze(text)
            assertEquals(text, plain.verdict, diagnostics.result.verdict)
            assertEquals(text, plain.verdict, diagnostics.verdictExplanation.mainVerdict)
        }
    }

    @Test fun unknownIngredientIsNeverRemovedByTheConditionalCalculation() {
        val explanation = analyze("eau, arôme, poudre mystérieuse").verdictExplanation

        assertNull(explanation.conditionalVerdict)
        assertEquals(ConditionalVerdictReason.UNKNOWN_INGREDIENT_REMAINS, explanation.conditionalReason)
    }

    @Test fun emptyOrUninterpretableInputHasNoConditionalResult() {
        listOf("", "Ingrédients:", "25 %").forEach { text ->
            val explanation = analyze(text).verdictExplanation
            assertNull(text, explanation.conditionalVerdict)
            assertEquals(text, ConditionalVerdictReason.NO_RELIABLE_ANALYSIS, explanation.conditionalReason)
        }
    }

    @Test fun diagnosticReportExportsTheStructuredConditionalExplanation() {
        val report = DiagnosticReport.build(analyze("eau, arôme"), "0.6.9")

        assertTrue(report.contains("EXPLICATION CONDITIONNELLE 0.6.9"))
        assertTrue(report.contains("Verdict principal conservé : UNCERTAIN"))
        assertTrue(report.contains("flavour#token:"))
        assertTrue(report.contains("[arôme]"))
        assertTrue(report.contains("Résultat conditionnel hors incertains : VEGAN"))
        assertTrue(report.contains("Traces prises en compte dans le résultat conditionnel : non"))
    }

    private fun analyze(text: String): AnalysisDiagnostics = service.analyzeWithDiagnostics(text)

    private fun ingredient(
        id: String,
        name: String,
        status: VeganStatus
    ) = Ingredient(id, name, listOf(name), null, status, "raison $id")
}
