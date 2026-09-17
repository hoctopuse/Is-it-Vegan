package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class VeganAnalyzerTest {
    private val database = listOf(
        ingredient("sucre", "sucre", VeganStatus.VEGAN),
        ingredient("farine", "farine de blé", VeganStatus.VEGAN),
        ingredient("lait", "lait", VeganStatus.VEGETARIAN),
        ingredient("coco", "lait de coco", VeganStatus.VEGAN),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN, "E471"),
        ingredient("e627", "guanylate de sodium", VeganStatus.UNCERTAIN, "E627")
    )

    @Test fun allIngredientsMustBeCovered() {
        val result = VeganAnalyzer.analyze("Ingrédients: sucre, farine de blé", database)
        assertEquals(2, result.matched.size)
        assertEquals(emptyList<String>(), result.unknown)
        assertEquals(listOf("mystère"), VeganAnalyzer.analyze("sucre, mystère", database).unknown)
    }

    @Test fun nestedIngredientsAndOrigins() {
        val result = VeganAnalyzer.analyze("sucre (lait de coco; E 471)", database)
        assertEquals(listOf("sucre", "coco", "e471"), result.matched.map { it.id })
        assertEquals(emptyList<String>(), result.unknown)
        assertEquals(VeganStatus.VEGETARIAN,
            VeganAnalyzer.analyze("lait", database).matched.single().status)
    }

    @Test fun eNumbersMayContainASpace() {
        val result = VeganAnalyzer.analyze("sucre, E 627", database)
        assertEquals(listOf("sucre", "e627"), result.matched.map { it.id })
        assertEquals(VeganStatus.UNCERTAIN, result.matched.last().status)
        assertEquals(emptyList<String>(), result.unknown)
    }

    @Test fun percentageSectionHeadingsAreNotIngredients() {
        val result = VeganAnalyzer.analyze(
            "Farce (63%): sucre 23,8%. Pâte (37%): farine de blé 28,5%",
            database
        )
        assertEquals(listOf("sucre", "farine"), result.matched.map { it.id })
        assertEquals(emptyList<String>(), result.unknown)
        assertEquals(AnalysisVerdict.VEGAN, result.verdict)
    }

    @Test fun decimalPercentagesAreNotIngredientSeparators() {
        val result = VeganAnalyzer.analyze(
            "sucre 23,8%, farine de blé 28.5%; lait 35 %",
            database
        )
        assertEquals(listOf("sucre", "farine", "lait"), result.matched.map { it.id })
        assertEquals(emptyList<String>(), result.unknown)
    }

    @Test fun longestKnownPhraseWinsInsideTheFullPipeline() {
        val result = VeganAnalyzer.analyze(
            "lait de coco, lait",
            database
        )
        assertEquals(listOf("coco", "lait"), result.matched.map { it.id })
        assertEquals(AnalysisVerdict.VEGETARIAN, result.verdict)
        assertEquals(emptyList<String>(), result.unknown)
    }

    @Test fun verdictsRespectUnknownAndAmbiguousIngredients() {
        val samples = listOf(
            "sucre, farine de blé" to AnalysisVerdict.VEGAN,
            "sucre, lait" to AnalysisVerdict.VEGETARIAN,
            "sucre, lait, mystère" to AnalysisVerdict.INCONCLUSIVE,
            "sucre, lait, E471" to AnalysisVerdict.UNCERTAIN,
            "sucre, gélatine, lait" to AnalysisVerdict.NON_VEGETARIAN
        )
        val entries = database + ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        samples.forEach { (text, expected) ->
            assertEquals(text, expected, VeganAnalyzer.analyze(text, entries).verdict)
        }
    }

    @Test fun uncertainVerdictExplainsTheRemainder() {
        val veganRemainder = VeganAnalyzer.analyze("sucre, E471, farine de blé", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, veganRemainder.verdict)
        assertEquals(AnalysisVerdict.VEGAN, veganRemainder.verdictWithoutUncertain)
        assertEquals(listOf("e471"), veganRemainder.uncertainIngredients.map { it.id })
        assertEquals(emptyList<String>(), veganRemainder.vegetarianIngredients.map { it.id })

        val vegetarianRemainder = VeganAnalyzer.analyze("farine de blé, lait, E471, sucre", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, vegetarianRemainder.verdict)
        assertEquals(AnalysisVerdict.VEGETARIAN, vegetarianRemainder.verdictWithoutUncertain)
        assertEquals(listOf("e471"), vegetarianRemainder.uncertainIngredients.map { it.id })
        assertEquals(listOf("lait"), vegetarianRemainder.vegetarianIngredients.map { it.id })
    }

    @Test fun unknownIngredientsPreventValidationOfTheRemainder() {
        val result = VeganAnalyzer.analyze("sucre, E471, mystère", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdictWithoutUncertain)
    }

    @Test fun tracesAndAllergenNotesAreNotIngredients() {
        val sample = """Eau, graines de soja*, présure (nigari), chlorure de calcium. *Agriculture biologique.
            **Allergènes :** soja
            **Traces :** lait, gélatine""".trimIndent()
        val entries = database + ingredient("eau", "eau", VeganStatus.VEGAN) +
            ingredient("soja", "graines de soja", VeganStatus.VEGAN) +
            ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        val result = VeganAnalyzer.analyze(sample, entries)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
        assertEquals(listOf("eau", "soja"), result.matched.map { it.id })
        assertEquals(listOf("présure", "nigari", "chlorure de calcium."), result.unknown)
    }

    @Test fun inlineCrossContactNotesDoNotAffectTheVerdict() {
        val entries = database + ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        val result = VeganAnalyzer.analyze(
            "sucre, E471, farine de blé. Peut contenir des traces de gélatine et de lait",
            entries
        )
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.VEGAN, result.verdictWithoutUncertain)
        assertEquals(listOf("e471"), result.uncertainIngredients.map { it.id })
        assertEquals(false, result.stoppedAtNonVegetarian)
    }

    @Test fun animalIngredientDominatesUnknownsAndAmbiguity() {
        val entries = database + ingredient("porc", "viande de porc", VeganStatus.NON_VEGAN) +
            ingredient("jaune", "jaune d’œuf", VeganStatus.VEGETARIAN)
        val result = VeganAnalyzer.analyze(
            "viande de porc (64%) (origine: Belgique); jaune d’OEUF; E471; ingrédient inconnu",
            entries
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertEquals(listOf("porc"), result.matched.map { it.id })
        assertEquals(true, result.stoppedAtNonVegetarian)
        assertEquals(emptyList<String>(), result.unknown)
    }

    @Test fun emptyOrNonIngredientTextCannotBeDeclaredVegan() {
        listOf("", "   \n", "Ingrédients:", "Peut contenir du lait", "25 %").forEach { text ->
            val result = VeganAnalyzer.analyze(text, database)
            assertEquals(text, AnalysisVerdict.INCONCLUSIVE, result.verdict)
            assertEquals(text, emptyList<Ingredient>(), result.matched)
        }
    }

    @Test fun uncertainIngredientAloneHasNoValidatedRemainder() {
        val result = VeganAnalyzer.analyze("E471", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdictWithoutUncertain)
    }

    @Test fun animalIngredientAtTheEndOverridesEarlierUnknownAndUncertainIngredients() {
        val entries = database + ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        val result = VeganAnalyzer.analyze("mystère, E471, lait, gélatine", entries)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdictWithoutUncertain)
        assertEquals(listOf("e471", "lait", "gelatine"), result.matched.map { it.id })
        assertEquals(listOf("mystère"), result.unknown)
        assertEquals(false, result.stoppedAtNonVegetarian)
    }

    @Test fun containsIsCompositionWhileMayContainIsCrossContact() {
        val composition = VeganAnalyzer.analyze("sucre (contient : lait)", database)
        val traces = VeganAnalyzer.analyze("sucre. Peut contenir du lait", database)
        assertEquals(AnalysisVerdict.VEGETARIAN, composition.verdict)
        assertEquals(listOf("sucre", "lait"), composition.matched.map { it.id })
        assertEquals(emptyList<String>(), composition.unknown)
        assertEquals(AnalysisVerdict.VEGAN, traces.verdict)
        assertEquals(listOf("sucre"), traces.matched.map { it.id })
        assertEquals(emptyList<String>(), traces.unknown)
    }

    @Test fun repeatedIngredientsAreReportedOnceInOrderOfAppearance() {
        val result = VeganAnalyzer.analyze("lait, sucre (lait), sucre", database)
        assertEquals(listOf("lait", "sucre"), result.matched.map { it.id })
        assertEquals(emptyList<String>(), result.unknown)
        assertEquals(AnalysisVerdict.VEGETARIAN, result.verdict)
    }

    @Test fun aliasesDoNotMatchInsideOtherWordsOrAdditiveNumbers() {
        listOf("laitue", "E4710").forEach { text ->
            val result = VeganAnalyzer.analyze(text, database)
            assertEquals(text, emptyList<Ingredient>(), result.matched)
            assertEquals(text, listOf(text), result.unknown)
            assertEquals(text, AnalysisVerdict.INCONCLUSIVE, result.verdict)
        }
    }

    @Test fun diagnosticsExposePreprocessingAndEveryProcessedToken() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients: sucre 25 %, E 471, mystère. Peut contenir du lait",
            database
        )
        assertEquals("sucre 25 %, E 471, mystère. ", diagnostics.preprocessedInput)
        assertEquals(listOf("sucre", "E471", "mystère."), diagnostics.tokens.map { it.text })
        assertEquals(listOf("sucre"), diagnostics.tokens[0].matchedIngredientIds)
        assertEquals(listOf("e471"), diagnostics.tokens[1].matchedIngredientIds)
        assertEquals("mystère.", diagnostics.tokens[2].unknown)
        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
    }

    @Test fun diagnosticReportContainsInputStepsAndBothVerdicts() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics("sucre, E471", database)
        val report = DiagnosticReport.build(diagnostics, "0.5.5")
        assertEquals(true, report.contains("Version : 0.5.5"))
        assertEquals(true, report.contains("ENTRÉE\nsucre, E471"))
        assertEquals(true, report.contains("correspondances=sucre"))
        assertEquals(true, report.contains("correspondances=e471"))
        assertEquals(true, report.contains("Verdict : UNCERTAIN"))
        assertEquals(true, report.contains("Verdict sans les incertains : VEGAN"))
    }

    private fun ingredient(id: String, alias: String, status: VeganStatus, number: String? = null) =
        Ingredient(id, alias, listOf(alias), number, status, "Test")
}
