package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrSessionTest {
    @Test fun restoringEditedFullTextPreservesExactSourceAndClearsEveryOldAnalysis() {
        val source = "[FR] eau, sucre\n[NL] water, suiker\n"
        val editedText = "INGREDIENTS: lait"
        val edited = OcrSession(rawOcrText = source, fullOcrText = source,
            editableText = source, fullTextSelected = true)
            .addAnalysis(AnalysisSnapshot(source, "source report", "old result", 1L))
            .withEditableText(editedText)
            .addAnalysis(AnalysisSnapshot(editedText, "edited report", "VÉGÉTARIEN", 2L))
        assertEquals(2, edited.analyses.size)
        assertTrue(edited.hasFullTextChoice())
        val restored = edited.selectFullText()
        assertEquals(source, restored.editableText)
        assertEquals(source, restored.fullOcrText)
        assertEquals(source, restored.rawOcrText)
        assertTrue(restored.fullTextSelected)
        assertTrue(restored.analyses.isEmpty())
        assertFalse(restored.hasStaleResult())
    }

    @Test fun exportDoesNotInferFullTextFromMissingAnalysisIdentityOrCurrentChoiceFromInitialReason() {
        val text = "Ingrédients: eau"
        val option = OcrTextOption(LabelLanguage.FRENCH, text, "FR", blockId = "fr-content", segmentId = "fr-local")
        val session = OcrSession(rawOcrText = text, fullOcrText = text, textOptions = listOf(option),
            ocrDiagnostics = OcrDiagnostics(0, 1, 1, emptyList(), emptyList(),
                selectedLanguage = "IT", selectionReason = "motif automatique IT", languageSegmentCount = 2))
            .selectOption(option.segmentId)
            .addAnalysis(AnalysisSnapshot(text, "rapport", "résultat", 1L))
        val report = OcrExportReport.build(session, "test")
        assertTrue(report.contains("Langue sélectionnée : FR"))
        assertTrue(report.contains("Raison du choix automatique initial : motif automatique IT"))
        assertTrue(report.contains("Identifiant du bloc analysé : non établi pour le texte soumis"))
        assertFalse(report.contains("Identifiant du bloc analysé : texte complet"))
    }

    @Test fun revertingAnOcrEditStillRequiresReanalysis() {
        val text = "Ingrédients: eau"
        val initial = OcrSession(editableText = text).addAnalysis(AnalysisSnapshot(text, "diag", "VEGAN", 1L))
        val reverted = initial.withEditableText("$text, lait").withEditableText(text)
        assertTrue(reverted.hasStaleResult())
        assertTrue(OcrExportReport.build(reverted, "test").contains("ancienne version"))
        assertFalse(reverted.addAnalysis(AnalysisSnapshot(text, "new", "VEGAN", 2L)).hasStaleResult())
    }

    @Test fun reviewUnavailableForUnloadedDatabaseOrGenerationFailure() {
        var generated = false
        var logged: Exception? = null
        val unloaded = attemptMultilingualReview(false, {
            generated = true
            MultilingualAlignmentEvaluation(emptyList(), emptyList())
        }, { logged = it })
        assertEquals(null to true, unloaded)
        assertFalse(generated)
        assertEquals(null, logged)

        val failure = IllegalStateException("technical detail")
        val failed = attemptMultilingualReview(true, { throw failure }, { logged = it })
        assertEquals(null to true, failed)
        assertEquals(failure, logged)
    }

    @Test fun unacceptedOrRefusedProposalDoesNotChangeTextOrPriorVerdict() {
        val proposal = proposal()
        val oldAnalysis = AnalysisSnapshot("INGREDIENTS: WHEAT lour", "diag", "INCONCLUSIVE", 1L)
        val initial = OcrSession().withOcrResult(
            OcrProcessingResult(
                "INGREDIENTS: WHEAT lour", "INGREDIENTS: WHEAT lour",
                OcrDiagnostics(0, 2, 2, listOf("EN", "NL"), emptyList()),
                multilingualAlignment = MultilingualAlignmentEvaluation(listOf(proposal.first, proposal.second), listOf(proposal))
            )
        ).addAnalysis(oldAnalysis)

        val refused = initial.dismissAlignmentProposal(proposal)

        assertEquals(initial.editableText, refused.editableText)
        assertEquals(listOf(oldAnalysis), refused.analyses)
        assertTrue(refused.visibleAlignmentProposals().isEmpty())
    }

    @Test fun acceptedCorrectionOnlyEditsTheUniqueFieldTextAndRequiresReanalysis() {
        val proposal = proposal()
        val oldAnalysis = AnalysisSnapshot("INGREDIENTS: WHEAT lour", "diag", "INCONCLUSIVE", 1L)
        val initial = boundSession(proposal).addAnalysis(oldAnalysis)

        val accepted = initial.acceptAlignmentProposal(proposal, "WHEAT flour")

        assertEquals("INGREDIENTS: WHEAT flour", accepted?.editableText)
        assertEquals(listOf(oldAnalysis), accepted?.analyses)
        assertEquals("INCONCLUSIVE", accepted?.analyses?.single()?.displayResult)
        assertTrue(accepted?.visibleAlignmentProposals().orEmpty().isEmpty())
        assertEquals(null, initial.acceptAlignmentProposal(proposal, ""))
        assertTrue(accepted!!.hasStaleResult())
        val reanalyzed = accepted.addAnalysis(AnalysisSnapshot(accepted.editableText, "new", "VEGAN", 2L))
        assertFalse(reanalyzed.hasStaleResult())
    }

    @Test fun aProposalCannotReplaceRepeatedTextOccurrences() {
        val proposal = proposal()
        val session = OcrSession(editableText = "WHEAT lour, WHEAT lour")

        assertEquals(null, session.acceptAlignmentProposal(proposal, "WHEAT flour"))
        assertEquals("WHEAT lour, WHEAT lour", session.editableText)
    }

    @Test fun bindingRejectsChangedBlockFullTextAndManualEdits() {
        val proposal = proposal()
        val initial = boundSession(proposal)
        assertEquals("WHEAT lour", initial.editableText.substring(initial.proposalRange(proposal)!!))
        assertEquals(null, initial.copy(selectedOptionBlockId = "other").acceptAlignmentProposal(proposal, "WHEAT flour"))
        assertEquals(null, initial.selectFullText().acceptAlignmentProposal(proposal, "WHEAT flour"))
        assertEquals(null, initial.withEditableText("INGREDIENTS: WHEAT lour, milk").acceptAlignmentProposal(proposal, "WHEAT flour"))
        assertEquals(null, initial.copy(editableText = "INGREDIENTS: WHEAT lour, milk").acceptAlignmentProposal(proposal, "WHEAT flour"))
    }

    @Test fun bindingRejectsRepeatedAndOverlappingOccurrences() {
        val original = proposal()
        val overlapping = original.copy(first = original.first.copy(rawText = "aa"))
        val session = boundSession(overlapping, "Ingredients: aaa")
        assertEquals(null, session.proposalRange(overlapping))
        assertEquals(null, session.acceptAlignmentProposal(overlapping, "other"))
        val repeated = boundSession(original, "WHEAT lour, WHEAT lour")
        assertEquals(null, repeated.proposalRange(original))
    }

    @Test fun acceptancePreservesQualifierOutsideTheBoundSpan() {
        val proposal = proposal()
        val session = boundSession(proposal, "INGREDIENTS: WHEAT lour (whole)")
        val accepted = session.acceptAlignmentProposal(proposal, "WHEAT flour")
        assertEquals("INGREDIENTS: WHEAT flour (whole)", accepted?.editableText)
    }

    @Test fun rawTextStaysSeparateFromEditableText() {
        val original = "INGRÉDIENTS\neau, lait"
        val edited = OcrSession().withOcrText(original).withEditableText("INGRÉDIENTS\neau, sucre")
        assertEquals(original, edited.rawOcrText)
        assertEquals("INGRÉDIENTS\neau, sucre", edited.editableText)
        assertNotSame(edited.rawOcrText, edited.editableText)
    }

    @Test fun reconstructedTextAndRawMlKitTextStayDistinct() {
        val result = OcrProcessingResult(
            rawText = "NL water\nFR eau",
            editableText = "FR eau\n\nNL water",
            diagnostics = OcrDiagnostics(90, 2, 2, listOf("FR", "NL"), emptyList())
        )
        val session = OcrSession().withOcrResult(result)

        assertEquals("NL water\nFR eau", session.rawOcrText)
        assertEquals("FR eau\n\nNL water", session.editableText)
        assertEquals(90, session.ocrDiagnostics?.orientationDegrees)
    }

    @Test fun exportIncludesOcrDiagnosticWithoutImageData() {
        val session = OcrSession().withOcrResult(
            OcrProcessingResult(
                "brut", "éditable",
                OcrDiagnostics(90, 8, 34, listOf("FR", "NL", "UNKNOWN"), listOf("ordre visuel incertain"))
            )
        )
        val export = OcrExportReport.build(session, "0.6.2")

        assertTrue(export.contains("Rotation EXIF transmise à ML Kit : 90°"))
        assertTrue(export.contains("Blocs géométriques ML Kit : 8"))
        assertTrue(export.contains("Lignes : 34"))
        assertTrue(export.contains("Zones détectées : FR, NL, UNKNOWN"))
        assertFalse(export.contains("data:image"))
    }

    @Test fun analysesKeepExactSubmittedSnapshots() {
        val first = AnalysisSnapshot("lait", "diagnostic lait", "NON VEGAN", 1L)
        val second = AnalysisSnapshot("eau, sucre", "diagnostic vegan", "VEGAN", 2L)
        val session = OcrSession().withOcrText("lait").withEditableText("eau, sucre")
            .addAnalysis(first).addAnalysis(second)
        assertEquals(listOf("lait", "eau, sucre"), session.analyses.map { it.submittedText })
    }

    @Test fun exportSeparatesRawEditedAndEveryResult() {
        val session = OcrSession("brut ML Kit", "texte corrigé", listOf(
            AnalysisSnapshot("brut ML Kit", "diag 1", "résultat 1", 1L),
            AnalysisSnapshot("texte corrigé", "diag 2", "résultat 2", 2L)
        ))
        val export = OcrExportReport.build(session, "0.6.0")
        assertTrue(export.indexOf("TEXTE BRUT OCR") < export.indexOf("TEXTE ÉDITABLE ACTUEL"))
        assertTrue(export.contains("brut ML Kit"))
        assertTrue(export.contains("texte corrigé"))
        assertTrue(export.contains("ANALYSE 1"))
        assertTrue(export.contains("ANALYSE 2"))
        assertTrue(export.contains("diag 1"))
        assertTrue(export.contains("diag 2"))
        assertTrue(export.contains("ancienne version"))
    }

    @Test fun editedTextIsTheValuePassedToExistingPipeline() {
        val database = listOf(
            Ingredient("milk", "lait", listOf("lait"), null, VeganStatus.NON_VEGAN, "origine animale", null),
            Ingredient("water", "eau", listOf("eau"), null, VeganStatus.VEGAN, "minéral", null)
        )
        val edited = "INGRÉDIENTS\neau"
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(edited, database, InputMode.FULL_LABEL)
        assertEquals(edited, diagnostics.input)
        assertTrue(diagnostics.result.matched.none { it.id == "milk" })
    }

    @Test fun analysisSnapshotKeepsStructuredDiagnosticsWithoutImageData() {
        val database = listOf(
            Ingredient("water", "eau", listOf("eau"), null, VeganStatus.VEGAN, "minéral", null)
        )
        val raw = "INGRÉDIENTS\n eau"
        val editable = "Ingrédients : eau"
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            editable, database, InputMode.OCR_LABEL
        )
        val session = OcrSession().withOcrResult(
            OcrProcessingResult(
                rawText = raw,
                editableText = editable,
                diagnostics = OcrDiagnostics(90, 1, 2, listOf("FR"), emptyList())
            )
        ).addAnalysis(
            AnalysisSnapshot(editable, "rapport", "VEGAN", 1L, diagnostics = diagnostics)
        )

        assertEquals(raw, session.rawOcrText)
        assertEquals(editable, session.editableText)
        assertEquals(editable, session.analyses.single().diagnostics?.input)
        assertEquals(90, session.ocrDiagnostics?.orientationDegrees)
        assertFalse(OcrExportReport.build(session, "test").contains("data:image"))
    }

    private fun proposal(): IngredientAlignmentResult {
        val unknown = IngredientOccurrence(
            "en:1", "en-block", LabelLanguage.ENGLISH, "WHEAT lour", 1
        )
        val known = IngredientOccurrence(
            "nl:1", "nl-block", LabelLanguage.DUTCH, "tarwebloem", 1,
            canonicalId = "wheat_flour", status = VeganStatus.VEGAN
        )
        return MultilingualIngredientAlignment.evaluate(
            listOf(unknown, known),
            listOf(AlignmentLink(unknown.id, known.id, AlignmentBasis.EXPLICIT_TRANSLATION))
        ).links.single()
    }

    private fun boundSession(proposal: IngredientAlignmentResult, text: String = "INGREDIENTS: WHEAT lour"): OcrSession {
        val block = LanguageBlock(LabelLanguage.ENGLISH, emptySet(), text, 0, text.length, "EN", id = "en-block")
        val option = OcrTextOption(LabelLanguage.ENGLISH, text, "EN", blockId = block.id, segmentId = block.segmentId)
        return OcrSession(rawOcrText = text, fullOcrText = text, editableText = text, selectedOptionBlockId = block.id,
            selectedOptionLanguage = LabelLanguage.ENGLISH, textOptions = listOf(option), languageBlocks = listOf(block),
            multilingualAlignment = MultilingualAlignmentEvaluation(listOf(proposal.first, proposal.second), listOf(proposal)))
    }
}
