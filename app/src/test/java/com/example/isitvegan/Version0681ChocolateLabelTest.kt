package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version0681ChocolateLabelTest {
    private val database = runtimeDatabase()
    private val lexicon = MultilingualIngredientLexicon.load(
        File("src/main/assets/ingredient_aliases_multilingual.json").readText()
    )

    @Test fun cocoaButterIsMatchedAsCocoaWithoutDairyButterBlocker() {
        val result = VeganAnalyzer.analyzeWithDiagnostics(
            "sucre, pâte de cacao, beurre de cacao, noisettes, huile de tournesol", database,
            InputMode.MANUAL_INGREDIENT_LIST, multilingualLexicon = lexicon
        )
        assertTrue(result.result.matched.any { it.id == "cocoa" })
        assertFalse(result.result.veganBlockers.any { it.id == "butter" })
        assertEquals(AnalysisVerdict.VEGAN, result.result.verdict)
    }

    @Test fun reviewedLineWrapsJoinOnlyKnownExpressions() {
        val milk = LabelPreprocessor.preprocess("LAIT écrémé en\npoudre, sucre")
        val whey = LabelPreprocessor.preprocess("lactosérum en\npoudre (de\nLAIT), sucre")
        assertTrue(milk.compositionText.contains("LAIT écrémé en poudre"))
        assertTrue(whey.compositionText.contains("lactosérum en poudre (de LAIT)"))
        assertTrue(LabelPreprocessor.preprocess("sucre\nhuile de tournesol").compositionText.contains("sucre\nhuile"))
        assertEquals("beurre de cacao", LabelPreprocessor.preprocess("beurre de\ncacao").compositionText)
        assertEquals("beurre, cacao", LabelPreprocessor.preprocess("beurre, cacao").compositionText)
    }

    @Test fun wrappedCocoaButterFollowsTheFullPipelineWithoutAButterBlocker() {
        val inline = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients : sucre, beurre de cacao, pâte de cacao, huile de tournesol, noisettes, sel.",
            database, InputMode.FULL_LABEL, multilingualLexicon = lexicon
        )
        val wrapped = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients : sucre, beurre de\ncacao, pâte de cacao, huile de tournesol, noisettes, sel.",
            database, InputMode.FULL_LABEL, multilingualLexicon = lexicon
        )
        listOf(inline, wrapped).forEach { analysis ->
            assertTrue(analysis.preprocessedInput.contains("beurre de cacao"))
            assertTrue(analysis.tokens.any { it.text.equals("beurre de cacao", true) })
            assertTrue(analysis.result.matched.any { it.id == "cocoa" })
            assertFalse(analysis.result.veganBlockers.any { it.id == "butter" })
            assertEquals(AnalysisVerdict.VEGAN, analysis.result.verdict)
        }
    }

    @Test fun secondaryVegetarianClassificationIgnoresOnlyUncertainIngredients() {
        fun assess(text: String) = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL).result
        assertEquals(AnalysisVerdict.VEGETARIAN, assess("Ingredients: milk, E322.").vegetarianVerdictWithoutUncertain)
        assertEquals(AnalysisVerdict.VEGETARIAN, assess("Ingredients: sugar, natural flavouring.").vegetarianVerdictWithoutUncertain)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, assess("Ingredients: gelatin, E322.").vegetarianVerdictWithoutUncertain)
        assertEquals(AnalysisVerdict.VEGETARIAN, assess("Ingredients: sugar. May contain milk.").vegetarianVerdictWithoutUncertain)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, assess("Ingredients: unknown fragment.").vegetarianVerdictWithoutUncertain)
    }

    @Test fun productInformationDoesNotAlsoContainTheFollowingTrace() {
        val sections = LabelSectionExtractor.extract(LabelLanguage.FRENCH,
            "Ingrédients: sucre. Cacao : 33 % minimum dans le chocolat. PEUT CONTENIR LAIT. GB Melkchocolade")
        assertTrue(sections.ignoredSections.any { it.contains("33 % minimum") })
        assertFalse(sections.ignoredSections.any { it.contains("PEUT CONTENIR", true) })
        assertTrue(sections.tracesText.orEmpty().contains("PEUT CONTENIR", true))
    }

    @Test fun contextualOcrRepairsDoNotApplyOutsideTheirFunctionalContext() {
        val repaired = LabelPreprocessor.preprocess("poudre à lever : ES03, émulsifiant (lécithines de SQA, EA76)")
        assertTrue(repaired.compositionText.contains("E503"))
        assertTrue(repaired.compositionText, repaired.compositionText.contains("E476"))
        assertTrue(repaired.compositionText.contains("lécithines de SOJA"))
        assertEquals("ES03, EA76", LabelPreprocessor.preprocess("ES03, EA76").compositionText)
    }

    @Test fun soyOcrRepairsRequireLecithinOrEmulsifierContext() {
        listOf("lécithines de S0JA", "lécithines de S0YA", "lécithines de SQJA", "émulsifiant (lécithines de S0JA)").forEach { raw ->
            val corrected = LabelPreprocessor.preprocess(raw)
            assertTrue(raw, corrected.compositionText.contains("SOJA"))
        }
        assertEquals("S0JA", LabelPreprocessor.preprocess("S0JA").compositionText)
        val trace = VeganAnalyzer.analyzeWithDiagnostics("Ingredients: sugar. May contain S0JA.", database, InputMode.FULL_LABEL)
        assertTrue(trace.crossContactWarnings.isNotEmpty())
        assertFalse(trace.preprocessedInput.contains("SOJA"))
    }

    @Test fun explicitSoyOriginResolvesOnlyThisE322Occurrence() {
        val rules = OriginQualifierRuleSet.load(File("src/main/assets/origin_qualifier_rules.json").readText()).rules
        listOf("lécithines de SOJA", "lécithines de S0JA", "lécithines de S0YA", "lécithines de SQJA", "émulsifiant (lécithines de SOJA)").forEach { text ->
            val analysis = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.MANUAL_INGREDIENT_LIST, rules = rules)
            assertEquals(text, VeganStatus.VEGAN, analysis.result.matched.single { it.id == "e322" }.status)
        }
        val bare = VeganAnalyzer.analyzeWithDiagnostics("lécithines", database, InputMode.MANUAL_INGREDIENT_LIST, rules = rules)
        assertEquals(VeganStatus.UNCERTAIN, bare.result.matched.single { it.id == "e322" }.status)
    }

    @Test fun observedChocolateOcrRepairsStayInIngredientSections() {
        val analysis = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients: SICTe, LAIT érémé en poudre, BEURRE CORCentié. Conservation: Sure.",
            database, InputMode.FULL_LABEL, multilingualLexicon = lexicon
        )
        assertTrue(analysis.preprocessedInput.contains("sucre"))
        assertTrue(analysis.preprocessedInput, analysis.preprocessedInput.contains("LAIT écrémé"))
        assertTrue(analysis.preprocessedInput.contains("BEURRE concentré"))
        assertFalse(analysis.preprocessedInput.contains("Conservation"))
    }

    @Test fun dutchAndGermanChocolateAliasesKeepTheirExistingStatuses() {
        val dutch = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrediënten: suiker, cacaoboter, cacaomassa, magere melkpoeder, weipoeder van melk, SOJALECITHINEN, zonnebloemolie.",
            database, InputMode.FULL_LABEL, multilingualLexicon = lexicon
        )
        val german = VeganAnalyzer.analyzeWithDiagnostics(
            "Zutaten: Zucker, Kakaobutter, Kakaomasse, Magermilchpulver, Molkenpulver aus Milch, Butterreinfett, Sojalecithine, Speisesalz.",
            database, InputMode.FULL_LABEL, multilingualLexicon = lexicon
        )
        assertTrue(dutch.result.matched.map { it.id }.containsAll(listOf("cocoa", "milk", "whey", "e322")))
        assertTrue(german.result.matched.map { it.id }.containsAll(listOf("cocoa", "milk", "whey", "butter", "e322")))
        assertTrue(dutch.result.matched.single { it.id == "e322" }.status == VeganStatus.UNCERTAIN)
        assertEquals(VeganAssessment.NOT_VEGAN, german.result.veganAssessment)
    }

    @Test fun cocoaSolidsAndFollowingLanguageTitlesAreExcluded() {
        val text = """
            Ingrédients: sucre, beurre de cacao, arôme.
            Cacao : 33 % minimum dans le chocolat au lait du pays alpin.
            NL BISCUITS BEDEKT MET MELKCHOCOLADE
            Ingrediënten: suiker, melk.
        """.trimIndent()
        val analysis = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL, multilingualLexicon = lexicon)
        assertTrue(analysis.preprocessedInput.contains("beurre de cacao"))
        assertFalse(analysis.preprocessedInput.contains("33 % minimum"))
        assertFalse(analysis.preprocessedInput.contains("BISCUITS BEDEKT"))
        assertFalse(analysis.preprocessedInput.contains("melk"))
        assertTrue(analysis.result.unknown.none { it.contains("BISCUITS", true) })
        assertTrue(analysis.languageSegmentation.blocks.size >= 2)
    }

    @Test fun germanAndItalianProductTitleBoundariesDoNotCreateIngredients() {
        listOf("E) KEKSE ÜBERZOGEN MIT\nZutaten: Zucker, Milch.", "ENIKEDAO AL LATTE\nIngredienti: zucchero, latte.")
            .forEach { title ->
                val analysis = VeganAnalyzer.analyzeWithDiagnostics(
                    "Ingrédients: sucre, cacao, arôme.\n$title", database, InputMode.FULL_LABEL,
                    multilingualLexicon = lexicon
                )
                assertFalse(analysis.preprocessedInput.contains("Zucker"))
                assertFalse(analysis.preprocessedInput.contains("zucchero"))
                assertTrue(analysis.result.unknown.none { it.contains("KEKSE", true) || it.contains("ENIKEDAO", true) })
            }
    }

    @Test fun tracesWithMilkAndEggNeverAlterTheIngredientVerdict() {
        val base = VeganAnalyzer.analyzeWithDiagnostics("Ingredients: sugar, cocoa butter.", database, InputMode.FULL_LABEL)
        val traced = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingredients: sugar, cocoa butter. Kan sporen bevatten melk en ei. NL BISCUITS BEDEKT MET MELKCHOCOLADE",
            database, InputMode.FULL_LABEL
        )
        assertEquals(base.result.verdict, traced.result.verdict)
        assertTrue(traced.crossContactWarnings.isNotEmpty())
        assertTrue(traced.result.matched.none { it.id == "milk" || it.id == "egg" })
    }

    @Test fun incompleteParenthesisStopsBeforeNextLanguage() {
        val sections = LabelSectionExtractor.extract(
            LabelLanguage.FRENCH,
            "Ingrédients: chocolat (sucre, cacao, arôme. E) KEKSE ÜBERZOGEN MIT Zutaten: Zucker, Milch."
        )
        val composition = LabelPreprocessor.preprocess(sections.ingredientsText.orEmpty()).compositionText
        assertEquals(composition, 1, IngredientTreeParser.inspectParentheses(composition).missingClosings)
        assertTrue(IngredientTreeParser.parse(composition).none { it.children.size > 3 })
        assertFalse(composition.contains("KEKSE"))
    }

    private fun runtimeDatabase(): List<Ingredient> =
        (MiniJson.parse(File("src/main/assets/ingredients.json").readText()) as List<*>).map { value ->
            val item = value as Map<*, *>
            Ingredient(item["id"] as String, item["name"] as String,
                (item["aliases"] as List<*>).filterIsInstance<String>(), item["eNumber"] as? String,
                VeganStatus.valueOf(item["status"] as String), item["reason"] as String)
        }
}
