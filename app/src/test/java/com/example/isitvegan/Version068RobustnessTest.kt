package com.example.isitvegan

import java.io.File

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version068RobustnessTest {
    private val database = listOf(
        Ingredient("water", "eau", listOf("eau", "water", "water"), null, VeganStatus.VEGAN, "test"),
        Ingredient("salt", "sel", listOf("sel", "zout"), null, VeganStatus.VEGAN, "test"),
        Ingredient("milk", "lait", listOf("lait", "melk", "milk", "latte", "leche"), null, VeganStatus.NON_VEGAN, "test"),
        Ingredient("egg", "oeuf", listOf("oeuf", "ei", "egg", "uovo", "huevo"), null, VeganStatus.NON_VEGAN, "test"),
        Ingredient("sugar", "sucre", listOf("sucre", "suiker"), null, VeganStatus.VEGAN, "test")
    )

    @Test fun damagedDutchTitlesNeedSeparatorAndARealList() {
        listOf("ngrediënten", "Inoredienten", "Ingrediênten").forEach { title ->
            val sections = LabelSectionExtractor.extract(LabelLanguage.DUTCH, "$title: water, zout, suiker.")
            assertEquals(LabelLanguage.DUTCH, sections.language)
            assertEquals("water, zout, suiker.", sections.ingredientsText?.trim())
        }
        assertFalse(LabelLexicon.findIngredientHeadings("een grediënten verhaal: water, zout").any())
    }

    @Test fun degradedDutchTitlesNeedLineBoundaryAndStructuredContent() {
        assertFalse(LabelLexicon.findIngredientHeadings("texte ngredi\u00ebnten: water, zout, suiker").any())
        assertFalse(LabelLexicon.findIngredientHeadings("Inoredienten: water").any())
        assertFalse(LabelLexicon.findIngredientHeadings("Inoredienten: water.\nMarketing, address").any())
        listOf("ngrediÃ«nten", "Inoredienten", "IngrediÃªnten", "IngrediÃ«nten").forEach { title ->
            val decodedTitle = title.toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8)
            assertTrue(decodedTitle, LabelLexicon.findIngredientHeadings("\n\n$decodedTitle:\nwater, zout, suiker").isNotEmpty())
        }
    }

    @Test fun organicClaimIsNeverPreferredToStructuredList() {
        val text = "Ingrediënten uit biologische landbouw.\nIngrediënten: water, zout, suiker."
        val result = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)
        assertTrue(result.labelSections.ingredientsText.orEmpty().contains("water, zout"))
        assertFalse(result.labelSections.ingredientsText.orEmpty().contains("biologische landbouw"))
    }

    @Test fun allTraceLanguagesStayOutsideVerdictAndStopAtStorage() {
        val traces = listOf(
            "Peut contenir du lait et des oeufs.", "Kan sporen bevatten van melk en ei.",
            "Kann Spuren von Milch und Ei enthalten.", "May contain milk and egg.",
            "Può contenere tracce di latte e uovo.", "Puede contener trazas de leche y huevo."
        )
        traces.forEach { trace ->
            val result = VeganAnalyzer.analyzeWithDiagnostics("Ingredients: water, salt.\n$trace\nStorage: cool.", database, InputMode.FULL_LABEL)
            assertTrue(result.crossContactWarnings.isNotEmpty())
            assertTrue(result.result.matched.none { it.id == "milk" || it.id == "egg" })
            assertTrue(result.result.veganBlockers.isEmpty())
            assertFalse(result.preprocessedInput.contains("milk", true) || result.preprocessedInput.contains("lait", true))
        }
    }

    @Test fun tracePrefixesAreRemovedFromNormalizedDiagnosticText() {
        val cases = listOf(
            "Peut contenir : lait, oeufs." to "lait, oeufs",
            "Kan bevatten: melk en eieren." to "melk en eieren",
            "Kan sporen bevatten van melk en soja." to "melk en soja",
            "Kann Spuren enthalten: Milch und Eier." to "Milch und Eier",
            "Kann Spuren von Milch und Eiern enthalten." to "Milch und Eiern",
            "May contain traces of milk and eggs." to "milk and eggs",
            "Può contenere eventuali tracce di latte e uova." to "latte e uova",
            "Puede contener trazas de leche y huevos." to "leche y huevos"
        )
        cases.forEach { (trace, expected) ->
            val sections = LabelSectionExtractor.extract(
                LabelLanguage.UNKNOWN, "Ingredients: water, salt. $trace Storage: cool."
            )
            assertEquals(trace, expected, sections.traceSection?.normalizedText)
            assertFalse(trace, sections.ingredientsText.orEmpty().contains(expected, true))
        }
    }

    @Test fun multilingualTracesCutAnUnbalancedComposition() {
        val traces = listOf(
            "Peut contenir : lait, oeufs.", "Kan bevatten: melk en eieren.",
            "Kan sporen bevatten van melk en soja.", "Kann Spuren enthalten: Milch und Eier.",
            "Può contenere eventuali tracce di latte e uova."
        )
        traces.forEach { trace ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                "Ingredients: chocolat (sucre, cacao, sel. $trace", database, InputMode.FULL_LABEL
            )
            assertEquals(trace, 1, diagnostics.parenthesisStructure.missingClosings)
            assertFalse(trace, diagnostics.preprocessedInput.contains(trace, true))
            assertTrue(trace, diagnostics.crossContactWarnings.single().contains(trace.substringBefore(':').substringBefore('.'), true))
            assertTrue(trace, diagnostics.tokens.none { it.text.contains("lait", true) || it.text.contains("melk", true) || it.text.contains("Milch", true) || it.text.contains("latte", true) })
        }
    }

    @Test fun ambiguousOcrIsPreservedButBoundedFoodCorrectionsAreRecorded() {
        val corrected = LabelPreprocessor.preprocess("TOZijnen, sirop de qlucose, sujke")
        assertTrue(corrected.compositionText.contains("rozijnen"))
        assertTrue(corrected.compositionText.contains("sirop de glucose"))
        assertTrue(corrected.ocrCorrections.isNotEmpty())
        assertTrue(LabelPreprocessor.preprocess("0,2 se u").compositionText.contains("0,2 se u"))
    }

    @Test fun normalSugarIsNotReportedAsAnOcrCorrection() {
        assertTrue(LabelPreprocessor.preprocess("sucre").ocrCorrections.isEmpty())
    }

    @Test fun ocrCorrectionsAreCaseBoundedAndQlucoseNeedsSyrupContext() {
        val exact = LabelPreprocessor.preprocess("SUCre, sirop de qlucose")
        assertEquals("sucre, sirop de glucose", exact.compositionText)
        assertEquals(2, exact.ocrCorrections.size)
        listOf("sucre", "Sucre", "SUCRE", "qlucose").forEach { value ->
            val untouched = LabelPreprocessor.preprocess(value)
            assertEquals(value, untouched.compositionText)
            assertTrue(value, untouched.ocrCorrections.isEmpty())
        }
    }

    @Test fun everyReviewedOcrVariantIsDeterministicAndReported() {
        val cases = listOf(
            "TOZijnen" to "rozijnen",
            "qerousterde" to "geroosterde",
            "pisaehenoten" to "pistachenoten",
            "cranbery's" to "cranberry's",
            "sujke" to "suiker",
            "p\u00e5te" to "p\u00e2te",
            "l\u00e9cith\u00ednes" to "l\u00e9cithines",
            "sirop de qlucose" to "sirop de glucose"
        )
        cases.forEach { (raw, expected) ->
            val result = LabelPreprocessor.preprocess(raw)
            assertEquals(raw, expected, result.compositionText)
            assertTrue(raw, result.ocrCorrections.single().contains(raw))
        }
    }

    @Test fun correctionsNeverRewriteTraceStorageOrPreparationSections() {
        val text = "Ingrédients: SUCre, sirop de qlucose. Peut contenir: lécithínes. Conservation: sujke. Préparation: påte."
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)
        assertEquals("sucre, sirop de glucose.", diagnostics.preprocessedInput.trim())
        assertEquals(2, diagnostics.ocrCorrections.size)
        assertTrue(diagnostics.crossContactWarnings.single().contains("lécithínes"))
        assertFalse(diagnostics.preprocessedInput.contains("sujke") || diagnostics.preprocessedInput.contains("påte"))
    }

    @Test fun reviewedKnowledgeKeepsAmbiguousConceptsAndMapsPotassiumPhosphatesToE340() {
        val database = (MiniJson.parse(File("src/main/assets/ingredients.json").readText()) as List<*>).map { value ->
            val item = value as Map<*, *>
            Ingredient(item["id"] as String, item["name"] as String,
                (item["aliases"] as List<*>).filterIsInstance<String>(), item["eNumber"] as? String,
                VeganStatus.valueOf(item["status"] as String), item["reason"] as String)
        }
        val lexicon = MultilingualIngredientLexicon.load(
            File("src/main/assets/ingredient_aliases_multilingual.json").readText()
        )
        assertEquals("E340", database.single { it.id == "e340" }.eNumber)
        assertEquals(VeganStatus.UNCERTAIN, database.single { it.id == "vitamin_b12" }.status)
        assertEquals("e340", lexicon.resolve("kaliumfosfaten", LabelLanguage.DUTCH, database).canonicalId)
        assertEquals("glucose_syrup", lexicon.resolve("glucose-fructosestroop", LabelLanguage.DUTCH, database).canonicalId)
        assertEquals("sugar", lexicon.resolve("gekarameliseerde suikersiroop", LabelLanguage.DUTCH, database).canonicalId)
        listOf("natural_flavouring", "e322", "e422", "vitamin_d").forEach { id ->
            assertEquals(VeganStatus.UNCERTAIN, database.single { it.id == id }.status)
        }
    }

    @Test fun damagedChocolateBarKeepsAmbiguousIngredientsAndTracesOutOfVerdict() {
        val runtimeDatabase = runtimeDatabase()
        val lexicon = MultilingualIngredientLexicon.load(
            File("src/main/assets/ingredient_aliases_multilingual.json").readText()
        )
        val rules = OriginQualifierRuleSet.load(
            File("src/main/assets/origin_qualifier_rules.json").readText()
        ).rules
        val text = """
            IngrÃ©dients: pÃ©tales de riz, blÃ© complet, orge complÃ¨te, flocons d'avoine,
            chocolat noir (pÃ¢te de cacao, sucre, beurre de cacao, lÃ©cithines E322,
            riz et blÃ© extrudÃ©s (farine de riz, gluten de blÃ©, malt d'orge),
            sirop de glucose-fructose, pÃ©pites de chocolat, huile de tournesol,
            glycÃ©rol E422, sirop de sucre caramÃ©lisÃ©, arÃ´me naturel, sel.
            Peut contenir : lait, oeufs et fruits Ã  coque.
        """.trimIndent()
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            text.toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8),
            runtimeDatabase, InputMode.FULL_LABEL, rules = rules, multilingualLexicon = lexicon
        )
        assertEquals(1, diagnostics.parenthesisStructure.missingClosings)
        assertTrue(diagnostics.parenthesisStructure.recoveryApplied)
        assertTrue(diagnostics.ingredientTree.size > 4)
        assertFalse(diagnostics.preprocessedInput.contains("Peut contenir"))
        assertTrue(diagnostics.crossContactWarnings.single().contains("lait"))
        assertTrue(diagnostics.result.veganBlockers.none { it.id == "milk" || it.id == "egg" })
        assertEquals(VeganStatus.UNCERTAIN, runtimeDatabase.single { it.id == "e322" }.status)
        assertEquals(VeganStatus.UNCERTAIN, runtimeDatabase.single { it.id == "e422" }.status)
        assertEquals(VeganStatus.UNCERTAIN, runtimeDatabase.single { it.id == "natural_flavouring" }.status)
        assertTrue(diagnostics.result.matched.any { it.id == "e322" })
        assertTrue(diagnostics.result.matched.any { it.id == "e422" })
        assertTrue(diagnostics.result.matched.any { it.id == "natural_flavouring" })
        assertTrue(diagnostics.result.unknown.any { it.contains("chocolat noir", true) })
        assertTrue(diagnostics.result.verdict != AnalysisVerdict.NON_VEGETARIAN)
    }

    @Test fun balancedParenthesesRemainNestedAndRecoveryCreatesSiblingIngredients() {
        val balancedText = "chocolat 20 % (sucre, cacao), sel"
        val structure = IngredientTreeParser.inspectParentheses(balancedText)
        assertTrue(structure.balanced)
        assertFalse(structure.recoveryApplied)
        assertEquals(1, structure.maximumDepth)
        val balanced = IngredientTreeParser.parse(balancedText)
        assertEquals(listOf("chocolat", "sel"), balanced.map { it.rawText })
        assertEquals("20", balanced.first().quantityPercent?.stripTrailingZeros()?.toPlainString())
        assertEquals(listOf("sucre", "cacao"), balanced.first().children.map { it.rawText })
        val recovered = IngredientTreeParser.parse("chocolat (sucre, cacao, huile de tournesol, sel")
        assertTrue(recovered.size > 1)
        assertTrue(recovered.none { it.children.size >= 4 })
    }

    @Test fun sessionCountersKeepSelectedBlockAndSegmentConsistent() {
        val block = LanguageBlock(LabelLanguage.DUTCH, emptySet(), "Ingredi\u00ebnten: water, zout.", 0, 27, "NL")
        val result = OcrProcessingResult("raw", block.rawText,
            OcrDiagnostics(0, 1, 1, listOf("NL"), emptyList(), selectedLanguage = "NL",
                selectedBlockId = block.id, languageSegmentCount = 1, selectableBlockCount = 1,
                selectedBlockCount = 1, rejectedBlockCount = 0),
            textOptions = listOf(OcrTextOption(LabelLanguage.DUTCH, block.rawText, "NL", blockId = block.id, segmentId = block.segmentId)),
            selectedOptionLanguage = LabelLanguage.DUTCH, selectedOptionBlockId = block.id)
        val session = OcrSession().withOcrResult(result)
        assertEquals(block.id, session.selectedOptionBlockId)
        assertEquals(1, session.ocrDiagnostics?.selectedBlockCount)
        val export = OcrExportReport.build(session, "0.6.8")
        assertTrue(export.contains("Blocs s\u00e9lectionn\u00e9s : 1"))
        assertFalse(export.contains("Blocs s\u00e9lectionn\u00e9s : 0"))
        assertFalse(export.contains("Langue s\u00e9lectionn\u00e9e : UNKNOWN"))
        assertEquals(block.segmentId, session.textOptions.single().segmentId)
    }

    @Test fun missingClosingParenthesisDoesNotAbsorbTraceOrFollowingIngredients() {
        val text = "Ingredients: chocolat noir (sucre, cacao, pépites, huile de tournesol, glycérol, arôme naturel, sel. Peut contenir lait."
        val result = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)
        assertEquals(1, result.parenthesisStructure.missingClosings)
        assertTrue(result.parenthesisStructure.recoveryApplied)
        assertTrue(result.preprocessedInput.contains("huile de tournesol"))
        assertTrue(result.crossContactWarnings.single().contains("Peut contenir"))
        assertFalse(result.preprocessedInput.contains("Peut contenir"))
    }

    private fun runtimeDatabase(): List<Ingredient> =
        (MiniJson.parse(File("src/main/assets/ingredients.json").readText()) as List<*>).map { value ->
            val item = value as Map<*, *>
            Ingredient(
                id = item["id"] as String,
                name = item["name"] as String,
                aliases = (item["aliases"] as List<*>).filterIsInstance<String>(),
                eNumber = item["eNumber"] as? String,
                status = VeganStatus.valueOf(item["status"] as String),
                reason = item["reason"] as String,
                source = item["source"] as? String
            )
        }
}
