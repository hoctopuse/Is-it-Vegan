package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrMultilingualSelectionTest {
    @Test fun damagedHeadingCannotRemoveMilkFromConfirmedGermanAnalysis() {
        val text = "ZUTATEN: Zucker\nMILCH. INGREDIENTE.\nSuiker, tarwebloem, fructosestroop.\nINGREDIENTS: sugar, milk"
        val segmentation = LabelLanguageSegmenter.segment(text, UiLanguage.DE)
        val selection = OcrTextSelection.from(segmentation, text)
        val german = selection.options.single { it.language == LabelLanguage.GERMAN }
        val english = selection.options.single { it.language == LabelLanguage.ENGLISH }
        val database = listOf(
            Ingredient("sugar", "Zucker", listOf("sugar", "sucre"), status = VeganStatus.VEGAN, reason = "test"),
            Ingredient("milk", "MILCH", listOf("milk"), status = VeganStatus.VEGETARIAN, reason = "test")
        )
        val result = VeganAnalyzer.analyzeWithDiagnostics(german.text, database, InputMode.OCR_LABEL, preferredLanguage = UiLanguage.DE, selectedBlockId = german.blockId)
        assertEquals(text, segmentation.originalText)
        assertTrue(german.text.contains("MILCH."))
        assertTrue(german.text.contains("INGREDIENTE."))
        assertTrue(english.text.contains("sugar, milk"))
        assertTrue(result.input.contains("MILCH."))
        assertTrue(result.result.matched.any { it.id == "milk" })
        assertEquals(VeganAssessment.NOT_VEGAN, result.result.veganAssessment)
        assertTrue(result.result.verdict != AnalysisVerdict.VEGAN)
    }

    @Test fun uppercaseMilkBeforeNextHeadingRemainsInGermanAnalysis() {
        val text = "ZUTATEN: Zucker.\nMILCH. INGREDIENTS: sugar, milk"
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(text), text)
        val german = selection.options.single { it.language == LabelLanguage.GERMAN }.text
        val english = selection.options.single { it.language == LabelLanguage.ENGLISH }.text
        val database = listOf(
            Ingredient("sugar", "Zucker", listOf("sugar"), status = VeganStatus.VEGAN, reason = "test"),
            Ingredient("milk", "MILCH", listOf("milk"), status = VeganStatus.VEGETARIAN, reason = "test")
        )
        val germanResult = VeganAnalyzer.analyzeWithDiagnostics(german, database, InputMode.OCR_LABEL, preferredLanguage = UiLanguage.DE)
        val englishResult = VeganAnalyzer.analyzeWithDiagnostics(english, database, InputMode.OCR_LABEL, preferredLanguage = UiLanguage.EN)
        assertEquals("ZUTATEN: Zucker.\nMILCH.", german)
        assertEquals("INGREDIENTS : sugar, milk", english)
        assertTrue(germanResult.result.matched.any { it.id == "milk" })
        assertEquals(VeganAssessment.NOT_VEGAN, germanResult.result.veganAssessment)
        assertTrue(germanResult.result.verdict != AnalysisVerdict.VEGAN)
        assertTrue(englishResult.result.matched.any { it.id == "milk" })
        assertEquals(VeganAssessment.NOT_VEGAN, englishResult.result.veganAssessment)
        assertTrue(englishResult.result.verdict != AnalysisVerdict.VEGAN)
    }

    @Test fun observed5087HeadingLossAndAdjacentProductTitleRemainBounded() {
        // The report contains the first two Dutch lines ending at "Appelzuur".
        // The next Dutch line is reconstructed; the German paragraph comes from the
        // report's editable field and the two English lines from its raw excerpts.
        val text = """
            GEAROMATISEERD GEGELIFIEERDE SUIKERWAREN. INGREDIENTE.
            Suiker Glucose-Fructosestroop, TARWEBLOEM, Voedingszuren (Appelzuur
            Cltreenzuur), Dextrose, Plantaardige olie (Palm).
            GUMMIBONBONS MIT FRUCHTGESCHMACK. ZUTATEN: Zucker, GlukoSe
            Fruktosesirup, WEIZENMEHL, Säuerungsmittel (Äpfelsäure, Citronensāure)
            Dextrose, Pflanzliches 0l (Palm), Aromen, Farbstoffe (E100, E131, E163)
            Karamellisierter Zuckersirup, Färbenden Lebensmittel (Holunderbeerextrat
            Saflor, Süßkartoffel), Antioxidationsmittel (Ascorbinsäure, E306).
            FLAVOURED JELLY GUMS. INGREDIENTS: Sugar, GluCOse-fructose sma
            WHEAT flour, Acids (Malicaid, Citricacid), Dextrose, Vegetable oil (Palm), Flavourin
        """.trimIndent()
        val cleaned = OcrTextCleaner.clean(text)
        val segmentation = LabelLanguageSegmenter.segment(cleaned, UiLanguage.FR)
        val selection = OcrTextSelection.from(segmentation, cleaned)
        assertEquals(setOf(LabelLanguage.GERMAN, LabelLanguage.ENGLISH), selection.options.map { it.language }.toSet())
        val german = selection.options.single { it.language == LabelLanguage.GERMAN }.text
        val english = selection.options.single { it.language == LabelLanguage.ENGLISH }.text
        assertEquals(LabelLanguage.GERMAN, segmentation.selectedLanguage)
        assertEquals(cleaned, segmentation.originalText)
        assertTrue(cleaned.contains("INGREDIENTE."))
        assertTrue(german.contains("Antioxidationsmittel"))
        // Without reliable title evidence, the prefix remains in the previous block.
        assertTrue(german.contains("FLAVOURED JELLY GUMS"))
        assertFalse(english.contains("FLAVOURED JELLY GUMS."))
        assertTrue(Regex("INGREDIENTS\\s*:").containsMatchIn(english))
    }

    @Test fun incompleteHeadingsAndRunningListsAreNotInventedOrCut() {
        val incomplete = "INGREDIENTE.\nSuiker, water, tarwebloem"
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(incomplete), incomplete)
        assertTrue(selection.options.isEmpty())
        assertEquals(incomplete, selection.editableText)
        val running = "ZUTATEN: Zucker,\nSWEET POTATO. INGREDIENTS: sugar, water"
        val blocks = LabelLanguageSegmenter.segment(running).blocks
        assertTrue(blocks.first().rawText.contains("SWEET POTATO."))
        assertTrue(blocks.last().rawText.contains("INGREDIENTS:"))
    }

    @Test fun observed5087DutchExcerptRemainsAvailableWithoutAProbableOption() {
        val observedExcerpt = """
            GEAROMATISEERD GEGELIFIEERDE SUIKERWAREN. INGREDIENTE.
            Suiker Glucose-Fructosestroop, TARWEBLOEM, Voedingszuren (Appelzuur
            GUMMIBONBONS MIT FRUCHTGESCHMACK. ZUTATEN: Zucker, GlukoSe
        """.trimIndent()
        val segmentation = LabelLanguageSegmenter.segment(observedExcerpt, UiLanguage.DE)
        val options = OcrTextSelection.from(segmentation, observedExcerpt).options
        assertTrue(options.none { it.language == LabelLanguage.DUTCH })
        assertEquals(LabelLanguage.GERMAN, segmentation.selectedLanguage)
        assertEquals(observedExcerpt, segmentation.originalText)
        assertTrue(segmentation.originalText.contains("TARWEBLOEM"))
        assertTrue(observedExcerpt.contains("INGREDIENTE."))
    }

    @Test fun damagedTitleCandidatesNeedDutchListAndNeighborBoundaryEvidence() {
        val negatives = listOf(
            "INGREDIENTE.\nSuiker, water, olie: heerlijke reclame.\nZUTATEN: Zucker, Milch" to "tekst commercial",
            "INGREDIENTE.\nSuiker, water, sugar, flour, milk.\nZUTATEN: Zucker, Milch" to "indices contradictoires",
            "INGREDIENTE.\nSuiker, tarwebloem, fructosestroop,\nZUTATEN: Zucker, Milch" to "liste tronquée",
            "INGREDIENTE.\nSuiker, tarwebloem, fructosestroop.\nConserver au sec.\nINGREDIENTS: sugar, milk" to "titre voisin d'une autre section",
            "INGREDIENTE.\nSuiker, suiker, suiker.\nZUTATEN: Zucker, Milch" to "répétitions"
        )
        negatives.forEach { (text, reason) ->
            assertTrue(reason, LabelLanguageSegmenter.segment(text).blocks.none { it.headingConfidence == HeadingConfidence.PROBABLE })
        }
        val missingNeighborHeading = "INGREDIENTE.\nSuiker, fructosestroop, tarwebloem, voedingszuren.\n[EN]"
        assertFalse(LabelLanguageSegmenter.segment(missingNeighborHeading).blocks.any { it.headingConfidence == HeadingConfidence.PROBABLE })
    }

    @Test fun editedSelectedTextKeepsUnknownAndDoesNotReuseOldBlockIdentity() {
        val full = "ZUTATEN: Zucker, MILCH.\nINGREDIENTS: sugar, milk"
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(full, UiLanguage.DE), full)
        val session = OcrSession(fullOcrText = full, textOptions = selection.options).selectLanguage(LabelLanguage.GERMAN)
        val oldId = session.selectedOptionBlockId!!
        val edited = session.withEditableText(session.editableText + ", XYZTEST")
        val database = listOf(
            Ingredient("sugar", "Zucker", listOf("sucre"), status = VeganStatus.VEGAN, reason = "test"),
            Ingredient("milk", "MILCH", listOf("milk"), status = VeganStatus.VEGETARIAN, reason = "test")
        )
        val result = VeganAnalyzer.analyzeWithDiagnostics(edited.editableText, database, InputMode.OCR_LABEL, preferredLanguage = UiLanguage.DE, selectedBlockId = oldId)
        assertEquals(edited.editableText, result.input)
        assertTrue(result.result.unknown.any { it.contains("XYZTEST", true) })
        assertTrue(result.result.matched.any { it.id == "milk" })
        assertTrue(result.languageSegmentation.selectedBlockId != oldId)
        assertEquals(result.languageSegmentation.selectedBlockId, result.labelSections.selectedBlockId)
    }

    @Test fun mismatchedNlIdentityIsNotAssignedToResegmentedGermanText() {
        val text = "PRODUKT ZUTATEN: Zucker, MILCH. INGREDIENTE.\nSuiker, tarwebloem, fructosestroop, XYZTEST."
        val result = VeganAnalyzer.analyzeWithDiagnostics(text, emptyList(), InputMode.OCR_LABEL, preferredLanguage = UiLanguage.DE, selectedBlockId = "nl-untrusted")
        assertEquals(LabelLanguage.GERMAN, result.languageSegmentation.selectedLanguage)
        assertTrue(result.languageSegmentation.selectedBlockId != "nl-untrusted")
        assertEquals(result.languageSegmentation.selectedBlockId, result.labelSections.selectedBlockId)
        assertTrue(result.ambiguousOcrText)
        assertEquals(text, result.labelSections.rawText)
        assertEquals(null, result.result.verdict)
    }

    @Test fun confirmedDutchBlockTakesPrecedenceOverEarlierProbableDutchCandidate() {
        val text = "INGREDIENTE.\nSuiker, fructosestroop, tarwebloem.\nINGREDIENTEN: water, zout.\nZUTATEN: Zucker, Milch"
        val segmentation = LabelLanguageSegmenter.segment(text)
        val confirmed = segmentation.blocks.single { it.language == LabelLanguage.DUTCH }
        val hypothetical = confirmed.copy(id = "unverified-nl", headingConfidence = HeadingConfidence.PROBABLE)
        val dutch = OcrTextSelection.from(segmentation.copy(blocks = listOf(hypothetical) + segmentation.blocks), text)
            .options.single { it.language == LabelLanguage.DUTCH }
        assertEquals(HeadingConfidence.CONFIRMED, dutch.headingConfidence)
        assertEquals(confirmed.id, dutch.blockId)
        assertTrue(dutch.text.contains("INGREDIENTEN:"))
    }

    @Test fun probableBlocksRemainUnselectableEvenIfSuppliedByAnotherCaller() {
        val text = "ZUTATEN: Zucker, Milch"
        val segmentation = LabelLanguageSegmenter.segment(text)
        val hypothetical = segmentation.blocks.single().copy(language = LabelLanguage.DUTCH, headingConfidence = HeadingConfidence.PROBABLE)
        val selection = OcrTextSelection.from(segmentation.copy(blocks = listOf(hypothetical) + segmentation.blocks), text)
        assertTrue(selection.options.none { it.language == LabelLanguage.DUTCH })
        assertTrue(selection.options.any { it.language == LabelLanguage.GERMAN })
    }

    @Test fun ambiguousOrDamagedUppercasePrefixesStayInPreviousBlockAcrossLanguages() {
        val cases = listOf(
            "ZUTATEN: Zucker.\nMILCH UND EIER. INGREDIENTS: sugar, milk" to LabelLanguage.GERMAN,
            "ZUTATEN: Zucker.\nFLAVOURED JELLY GUM5. INGREDIENTS: sugar, milk" to LabelLanguage.GERMAN,
            "Ingrédients: sucre.\nLAIT EN POUDRE. INGREDIENTS: sugar, milk" to LabelLanguage.FRENCH,
            "Ingrediënten: suiker.\nMELK POEDER. ZUTATEN: Zucker, Milch" to LabelLanguage.DUTCH,
            "INGREDIENTS: sugar.\nMILK POWDER. Ingrédients: sucre, lait" to LabelLanguage.ENGLISH
        )
        cases.forEach { (text, language) ->
            val prefix = text.substringAfter('\n').substringBefore(". ") + "."
            val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(text), text)
            val previous = selection.options.single { it.language == language }.text
            assertTrue("$language: $previous", previous.contains(prefix))
        }
    }

    private val fixture = """
        INGREDIENTEN: water, geraffineerde ongeharde kokosolie 29%,
        zetmelen (mais, aardappel) 11%, zout, gemodificeerd tapiocazetmeel,
        zuurteregelaar (glucono-delta-lacton), aardappeleiwit,
        verdikkingsmiddel (agar-agar), natuurlijk aroma, olijfextract.

        INGREDIENTS: eau, huile de coco raffinée non hydrogénée 29%,
        amidons (mais, pomme de terre) 11%, sel, amidon de tapioca modifié,
        correcteur d’acidité (glucono-delta-lactone), protéines de pomme de terre,
        épaississant (agar-agar), arôme naturel, extrait d’olive.

        ZUTATEN: Wasser, raffiniertes ungehärtetes Kokosnussöl 29%,
        Stärke (Mais, Kartoffel), Salz, modifizierte Tapiokastärke,
        Säureregulator (Glucono-delta-lacton), Kartoffeleiweiß,
        Verdickungsmittel (Agar-Agar), natürliches Aroma, Olivenextrakt.
    """.trimIndent()

    @Test fun contentReclassifiesAmbiguousIngredientsHeadingAsFrench() {
        val segmentation = LabelLanguageSegmenter.segment(fixture, UiLanguage.FR)

        assertEquals(listOf(LabelLanguage.DUTCH, LabelLanguage.FRENCH, LabelLanguage.GERMAN),
            segmentation.blocks.map { it.language })
        assertEquals(LabelLanguage.FRENCH, segmentation.selectedLanguage)
        assertEquals("INGREDIENTS", segmentation.detectedMarker)
        assertNotNull(segmentation.blocks[1].languageCorrectionReason)
    }

    @Test fun editableTextContainsOnlySelectedBlockAndFullTextRemainsAvailable() {
        val full = OcrTextCleaner.clean(fixture)
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(full, UiLanguage.FR), full)

        assertEquals(LabelLanguage.FRENCH, selection.selectedLanguage)
        assertTrue(selection.editableText.contains("eau, huile de coco"))
        assertFalse(selection.editableText.contains("geraffineerde"))
        assertFalse(selection.editableText.contains("Wasser"))
        assertTrue(full.contains("geraffineerde"))
        assertEquals(setOf(LabelLanguage.DUTCH, LabelLanguage.FRENCH, LabelLanguage.GERMAN),
            selection.options.map { it.language }.toSet())
    }

    @Test fun scandinavianMarkersAreDetectedButTheirBlocksAreNotSelectableOptions() {
        listOf(
            "SE: Ingredienser: Socker, VETEMJOL, Dextros." to LabelLanguage.SWEDISH,
            "NO: Ingredienser: Sukker, HVETEMEL, Dextrose." to LabelLanguage.NORWEGIAN
        ).forEach { (text, expectedLanguage) ->
            val segmentation = LabelLanguageSegmenter.segment(text)
            val selection = OcrTextSelection.from(segmentation, OcrTextCleaner.clean(text))

            assertEquals(expectedLanguage, segmentation.blocks.single().language)
            assertEquals(expectedLanguage, selection.selectedLanguage)
            assertTrue(segmentation.blocks.single().hasIngredientHeading.not())
            assertTrue(selection.options.isEmpty())
        }
    }

    @Test fun scandinavianCodeBeforeProductTitleDoesNotCurrentlyStartALanguageBlock() {
        listOf(
            "SE SMaksatt gelegodis. Ingredienser: Socker, VETEMJOL, Dextros.",
            "NO Gelefigurer med fruktsmak. Ingredienser: Sukker, HVETEMEL, Dextrose."
        ).forEach { text ->
            val segmentation = LabelLanguageSegmenter.segment(text)

            assertTrue(segmentation.usedFallback)
            assertEquals(LabelLanguage.UNKNOWN, segmentation.selectedLanguage)
        }
    }

    @Test fun analysisKeepsTheContentAssignedLanguageAndSelectedSection() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            text = fixture,
            database = emptyList(),
            inputMode = InputMode.FULL_LABEL,
            preferredLanguage = UiLanguage.FR
        )

        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertTrue(diagnostics.labelSections.ingredientsText.orEmpty().contains("eau, huile de coco"))
        assertFalse(diagnostics.labelSections.ingredientsText.orEmpty().contains("geraffineerde"))
        assertFalse(diagnostics.labelSections.ingredientsText.orEmpty().contains("Wasser"))
    }

    @Test fun selectedBlockCanBeChangedAndFullTextInvalidatesPreviousAnalysis() {
        val full = OcrTextCleaner.clean(fixture)
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(full, UiLanguage.FR), full)
        val result = OcrProcessingResult(
            rawText = fixture,
            editableText = selection.editableText,
            diagnostics = OcrDiagnostics(0, 3, 12, listOf("NL", "FR", "DE"), emptyList()),
            fullText = full,
            textOptions = selection.options,
            selectedOptionLanguage = selection.selectedLanguage,
            selectedOptionBlockId = selection.selectedBlockId
        )
        val analyzed = OcrSession().withOcrResult(result)
            .addAnalysis(AnalysisSnapshot("ancien", "diagnostic", "résultat", 1L))
        val german = analyzed.selectLanguage(LabelLanguage.GERMAN)
        val complete = german.addAnalysis(AnalysisSnapshot("ancien", "diagnostic", "résultat", 2L)).selectFullText()

        assertTrue(german.editableText.contains("Wasser"))
        assertTrue(german.analyses.isEmpty())
        assertEquals(german.selectedOptionBlockId, german.ocrDiagnostics?.selectedBlockId)
        assertEquals("DE", german.ocrDiagnostics?.selectedLanguage)
        assertEquals(full, complete.editableText)
        assertTrue(complete.fullTextSelected)
        assertTrue(complete.analyses.isEmpty())
        assertEquals(null, complete.ocrDiagnostics?.selectedBlockId)
    }

    @Test fun safeNoiseAndTraceBoundariesStayOutsideComposition() {
        val cleaned = OcrTextCleaner.cleanSelectedBlock("INGREDIENTS: eau, sel\nOUVRIR ICI\nImportateur: Exemple")
        val sections = LabelSectionExtractor.extract(
            LabelLanguage.FRENCH,
            "Ingrédients: eau, sel. Peut contenir lait. Importateur: Exemple"
        )

        assertFalse(cleaned.contains("OUVRIR ICI"))
        assertEquals("eau, sel.", sections.ingredientsText?.trim())
        assertTrue(sections.tracesText.orEmpty().contains("Peut contenir lait"))
        assertFalse(sections.tracesText.orEmpty().contains("Importateur"))
    }

    @Test fun nonVeganBlockerStillWinsOverUnknownsAndTraces() {
        val nonVegan = Ingredient(
            id = "gelatin",
            name = "gélatine",
            aliases = listOf("gélatine"),
            status = VeganStatus.NON_VEGAN,
            reason = "animal"
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, VerdictEngine.evaluate(listOf(nonVegan), listOf("mystère")))
        assertEquals(AnalysisVerdict.UNCERTAIN,
            VerdictEngine.evaluate(
                listOf(
                    Ingredient(
                        id = "x",
                        name = "x",
                        aliases = listOf("x"),
                        status = VeganStatus.UNCERTAIN,
                        reason = "incertain"
                    )
                ),
                emptyList()
            )
        )
        assertEquals(AnalysisVerdict.INCONCLUSIVE, VerdictEngine.evaluate(emptyList(), listOf("mystère")))
    }
}
