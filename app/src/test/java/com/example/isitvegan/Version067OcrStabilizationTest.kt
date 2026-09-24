package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version067OcrStabilizationTest {
    private val database = listOf(
        ingredient("rice_flour", "farine de riz"),
        ingredient("sunflower_oil", "huile de tournesol"),
        ingredient("salt", "sel"),
        ingredient("rice", "riz complet"),
        ingredient("barley", "orge perlé"),
        ingredient("pepper", "poivron"),
        ingredient("soy", "soja"),
        Ingredient("milk", "lait", listOf("lait", "milk", "melk", "Milch", "latte", "leche"), null, VeganStatus.NON_VEGAN, "test")
    )

    @Test fun organicCertificationClaimCannotDisplaceARealIngredientList() {
        val text = """
            Ingrédients : riz complet, orge perlé, poivron.
            Ingrédients issus de l'agriculture biologique.
            Peut contenir des traces de moutarde.
            NL: Ingrediënten: rijst, gerst, paprika.
        """.trimIndent()

        val segmentation = LabelLanguageSegmenter.segment(text, UiLanguage.FR)
        val sections = LabelSectionExtractor.extract(segmentation.blocks.first { it.id == segmentation.selectedBlockId })

        assertEquals(LabelLanguage.FRENCH, segmentation.selectedLanguage)
        assertTrue(sections.ingredientsText.orEmpty().contains("riz complet"))
        assertFalse(sections.ingredientsText.orEmpty().contains("agriculture biologique", true))
        assertTrue(segmentation.blocks.first().selectionSignals.none { it.contains("titre d’ingrédients") && it.contains("biologique") })
    }

    @Test fun longerDutchListWinsOverAnUntitledFrenchOrganicClaim() {
        val text = """
            FR: Ingrédients issus de l'agriculture biologique.
            NL: Ingrediënten: rijst, gerst, paprika, zout, zonnebloemolie.
        """.trimIndent()

        val segmentation = LabelLanguageSegmenter.segment(text, UiLanguage.FR)

        assertEquals(LabelLanguage.DUTCH, segmentation.selectedLanguage)
        assertTrue(segmentation.selectedText.contains("zonnebloemolie"))
        assertTrue(segmentation.blocks.first().selectionSignals.any { it.contains("certification biologique") })
    }

    @Test fun allOrganicClaimsAreRejectedAndQualityBeatsLanguagePreference() {
        val falseClaims = listOf(
            "FR: Ingrédients d'origine biologique.",
            "FR: Ingrédients provenant de l'agriculture biologique.",
            "NL: Ingrediënten uit de biologische landbouw.",
            "EN: Ingredients from organic farming.",
            "DE: Zutaten aus ökologischem Landbau."
        )
        falseClaims.forEach { claim ->
            val real = "NL: Ingrediënten: rijst, gerst, paprika, zout, zonnebloemolie."
            val segmentation = LabelLanguageSegmenter.segment("$claim\n$real", UiLanguage.FR)
            assertEquals(claim, LabelLanguage.DUTCH, segmentation.selectedLanguage)
            assertTrue(claim, segmentation.blocks.first().selectionSignals.any { it.contains("faux marqueur") })
        }

        val qualityWins = LabelLanguageSegmenter.segment(
            "FR: Ingrédients: riz, sel.\nNL: Ingrediënten: rijst, gerst, paprika, zout, zonnebloemolie, suiker.",
            UiLanguage.FR
        )
        val truncated = LabelLanguageSegmenter.segment(
            "FR: Ingrédients: riz (complet\nNL: Ingrediënten: rijst, gerst, zout.",
            UiLanguage.FR
        )
        assertEquals(LabelLanguage.DUTCH, qualityWins.selectedLanguage)
        assertTrue(qualityWins.selectionReason.contains("qualité", true))
        assertEquals(LabelLanguage.DUTCH, truncated.selectedLanguage)
        assertTrue(truncated.blocks.first().manifestlyTruncated)
    }

    @Test fun multilineTraceSectionIsExcludedUntilItsCompletedSentenceInAllLanguages() {
        val notices = listOf(
            "Peut contenir des traces d'autres céréales contenant du gluten (seigle),\nd'autres fruits à coque (noix de cajou),\nde lait et de soja.",
            "Kan sporen bevatten van andere granen met gluten (rogge),\nnoten, melk en soja.",
            "Kann Spuren enthalten von glutenhaltigem Getreide (Roggen),\nNüssen, Milch und Soja.",
            "May contain traces of cereals containing gluten (rye),\nnuts, milk and soy.",
            "Può contenere tracce di cereali con glutine (segale),\nlatte e soia.",
            "Puede contener trazas de cereales con gluten (centeno),\nleche y soja."
        )
        notices.forEach { notice ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                "Ingrédients : farine de riz, huile de tournesol, sel. $notice\nEn dépit des contrôles effectués...",
                database,
                InputMode.FULL_LABEL
            )
            assertEquals(notice, VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
            assertTrue(notice, diagnostics.result.veganBlockers.isEmpty())
            assertTrue(notice, diagnostics.labelSections.tracesText.orEmpty().contains("lait", true) ||
                diagnostics.labelSections.tracesText.orEmpty().contains("milk", true) ||
                diagnostics.labelSections.tracesText.orEmpty().contains("melk", true) ||
                diagnostics.labelSections.tracesText.orEmpty().contains("Milch", true) ||
                diagnostics.labelSections.tracesText.orEmpty().contains("latte", true) ||
                diagnostics.labelSections.tracesText.orEmpty().contains("leche", true))
            assertFalse(notice, diagnostics.tokens.any { it.text.contains("lait", true) || it.text.contains("milk", true) })
        }
    }

    @Test fun productTitleAfterCompletedIngredientSentenceIsNotParsed() {
        listOf(
            "B8 L0ER CH Nouilles à base de farine de riz.",
            "Muesli avec des fruits secs.",
            "Mélange de noix.",
            "Barre de céréales aux fruits rouges."
        ).forEach { title ->
            val text = "Ingrédients : 99,2 % farine de riz,\n0,6 % huile de tournesol,\n0,2 % sel.\n$title"
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)

            assertEquals(title, VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
            assertFalse(title, diagnostics.preprocessedInput.contains(title.substringBefore(' '), true))
            assertFalse(title, diagnostics.labelSections.ingredientsText.orEmpty().contains(title.substringBefore(' '), true))
        }
    }

    @Test fun boundedOcrCorrectionsJoinOnlyKnownIngredientExpressions() {
        val raw = "0,6h hulle de\ntournesol, 0,2 se cu"
        val corrected = LabelPreprocessor.preprocess(raw)

        assertTrue(corrected.compositionText.contains("0,6 % huile de tournesol"))
        assertTrue(corrected.compositionText.contains("0,2 se cu"))
        assertTrue(corrected.ocrCorrections.any { it.contains("hulle de") })
        assertTrue(corrected.ocrCorrections.any { it.contains("0,6h") && it.contains("0,6 %") })
        assertEquals(raw, raw)
    }

    @Test fun knownLineBreakExpressionsAndCompositeFruitMixRemainStructured() {
        val normalized = LabelPreprocessor.preprocess(
            "farine de\nriz, huile de\ntournesol, extrait riche en\ntocophérols"
        )
        val tree = IngredientTreeParser.parse(
            "45 % mélange de fruits secs (raisins sultanines, raisins secs, chips de banane, dattes, ananas confit, chips de coco, papaye confite), flocons d'orge complets"
        )

        assertTrue(normalized.compositionText.contains("farine de riz"))
        assertTrue(normalized.compositionText.contains("huile de tournesol"))
        assertTrue(normalized.compositionText.contains("extrait riche en tocophérols"))
        assertEquals(2, tree.size)
        assertEquals(7, tree.first().children.size)
    }

    @Test fun nestedMuesliKeepsEveryReadableChildWithoutInventingMissingOnes() {
        val expected = listOf(
            "raisins sultanines", "raisins secs", "banane", "dattes", "ananas", "coco", "papaye",
            "orge", "avoine", "malt", "graines de courge", "amandes", "noix de pécan", "noisettes"
        )
        val tree = IngredientTreeParser.parse("45 % mélange (${expected.joinToString(", ")})")

        assertEquals(expected, tree.single().children.map { it.rawText })
        val damaged = IngredientTreeParser.parse("45 % mélange (raisins secs, xx7?, dattes)")
        assertEquals(listOf("raisins secs", "xx7?", "dattes"), damaged.single().children.map { it.rawText })
    }

    @Test fun marketingAndSafetyLinesNeverEnterCompositionAndTraceDuplicatesCollapse() {
        val label = """
            Ingrédients : farine de riz, huile de tournesol, sel.
            Conditionné sous atmosphère protectrice.
            À conserver à l'abri de la chaleur et de l'humidité.
            Non ouvert.
            Certifié biologique.
            Rainforest Alliance.
            En dépit des contrôles effectués.
        """.trimIndent()
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(label, database, InputMode.FULL_LABEL)
        val excluded = listOf("Conditionné", "conserver", "Non ouvert", "Certifié", "Rainforest", "contrôles")
        excluded.forEach { value ->
            assertFalse(value, diagnostics.preprocessedInput.contains(value, true))
            assertFalse(value, diagnostics.tokens.any { it.text.contains(value, true) })
        }

        val duplicated = LabelSectionExtractor.extract(
            LabelLanguage.FRENCH,
            "Ingrédients: farine de riz. Peut contenir: lait et soja. Peut contenir : lait et soja."
        )
        assertEquals(1, duplicated.tracesText.orEmpty().lines().size)
        assertEquals("lait et soja", duplicated.traceSection?.normalizedText)
    }

    @Test fun diagnosticReportsStableBlockSegmentAndRejectionReasons() {
        val text = "FR: Ingrédients d'origine biologique.\nNL: Ingrediënten: rijst, gerst, zout."
        val segmentation = LabelLanguageSegmenter.segment(text, UiLanguage.FR)
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            text,
            database,
            InputMode.OCR_LABEL,
            preferredLanguage = UiLanguage.FR,
            selectedBlockId = segmentation.selectedBlockId
        )
        val report = DiagnosticReport.build(diagnostics, "0.6.7")

        assertTrue(report.contains("Identifiant du bloc sélectionné : ${segmentation.selectedBlockId}"))
        assertTrue(report.contains("Identifiant du segment sélectionné :"))
        assertTrue(report.contains("Raison du rejet :"))
        assertTrue(report.contains("Raison de sélection :"))
    }

    private fun ingredient(id: String, alias: String) = Ingredient(
        id, alias, listOf(alias), null, VeganStatus.VEGAN, "test"
    )
}
