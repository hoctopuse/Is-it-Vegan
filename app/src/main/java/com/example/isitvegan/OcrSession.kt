package com.example.isitvegan

/** In-memory state for one OCR work session. The URI is deliberately not part of this model. */
internal data class OcrSession(
    val rawOcrText: String? = null,
    val editableText: String = "",
    val analyses: List<AnalysisSnapshot> = emptyList(),
    val ocrDiagnostics: OcrDiagnostics? = null,
    val fullOcrText: String = "",
    val textOptions: List<OcrTextOption> = emptyList(),
    val selectedOptionLanguage: LabelLanguage? = null,
    val selectedOptionBlockId: String? = null,
    val selectedOptionSegmentId: String? = null,
    val fullTextSelected: Boolean = false,
    val languageBlocks: List<LanguageBlock> = emptyList(),
    val multilingualAlignment: MultilingualAlignmentEvaluation? = null,
    val dismissedAlignmentProposals: Set<String> = emptySet(),
    val alignmentUnavailable: Boolean = false,
    val resultStale: Boolean = false
) {
    fun withOcrText(text: String): OcrSession = copy(
        rawOcrText = text,
        editableText = text,
        languageBlocks = emptyList(),
        multilingualAlignment = null,
        dismissedAlignmentProposals = emptySet(),
        alignmentUnavailable = false,
        resultStale = analyses.isNotEmpty()
    )

    fun withOcrResult(result: OcrProcessingResult): OcrSession = copy(
        rawOcrText = result.rawText,
        editableText = result.editableText,
        ocrDiagnostics = result.diagnostics,
        fullOcrText = result.fullText,
        textOptions = result.textOptions,
        selectedOptionLanguage = result.selectedOptionLanguage,
        selectedOptionBlockId = result.selectedOptionBlockId,
        selectedOptionSegmentId = result.selectedOptionSegmentId,
        fullTextSelected = result.selectedOptionLanguage == null,
        languageBlocks = result.languageBlocks,
        multilingualAlignment = result.multilingualAlignment,
        dismissedAlignmentProposals = emptySet(),
        alignmentUnavailable = result.alignmentUnavailable,
        resultStale = analyses.isNotEmpty()
    )

    fun withEditableText(text: String): OcrSession = copy(
        editableText = text,
        multilingualAlignment = null,
        dismissedAlignmentProposals = emptySet(),
        resultStale = resultStale || (text != editableText && analyses.isNotEmpty())
    )

    fun dismissAlignmentProposal(proposal: IngredientAlignmentResult): OcrSession =
        copy(dismissedAlignmentProposals = dismissedAlignmentProposals + proposal.proposalKey)

    /** Applies only a unique, explicitly accepted replacement to the editable text. */
    fun acceptAlignmentProposal(
        proposal: IngredientAlignmentResult,
        replacementText: String
    ): OcrSession? {
        if (replacementText.isBlank()) return null
        val source = proposal.first.rawText
        if (source.isBlank() || source == replacementText) return null
        val range = proposalRange(proposal) ?: return null
        return copy(
            editableText = editableText.replaceRange(range.first, range.last + 1, replacementText),
            multilingualAlignment = null,
            dismissedAlignmentProposals = emptySet(),
            resultStale = resultStale || analyses.isNotEmpty()
        )
    }

    /** Bind only to the unchanged selected OCR block and a unique source span, including overlaps. */
    fun proposalRange(proposal: IngredientAlignmentResult): IntRange? {
        if (proposal.kind != AlignmentKind.CORRECTION_PROPOSAL || fullTextSelected ||
            proposal.proposalKey in dismissedAlignmentProposals ||
            multilingualAlignment?.links?.contains(proposal) != true ||
            multilingualAlignment.occurrences.count { it == proposal.first } != 1 ||
            selectedOptionBlockId != proposal.first.blockId || selectedOptionLanguage != proposal.first.language
        ) return null
        val option = textOptions.singleOrNull { it.blockId == proposal.first.blockId && it.language == proposal.first.language } ?: return null
        val block = languageBlocks.singleOrNull { it.id == proposal.first.blockId && it.language == proposal.first.language } ?: return null
        if (editableText != option.text) return null
        val source = proposal.first.rawText
        if (source.isBlank()) return null
        val sourceStart = block.rawText.indexOf(source)
        if (sourceStart < 0 || block.rawText.indexOf(source, sourceStart + 1) >= 0) return null
        val start = option.text.indexOf(source)
        if (start < 0 || option.text.indexOf(source, start + 1) >= 0) return null
        return start until (start + source.length)
    }

    fun visibleAlignmentProposals(): List<IngredientAlignmentResult> =
        multilingualAlignment?.links.orEmpty().filter { proposal ->
            proposalRange(proposal) != null
        }

    fun hasStaleResult(): Boolean = analyses.isNotEmpty() &&
        (resultStale || analyses.last().submittedText != editableText)

    fun hasFullTextChoice(): Boolean = rawOcrText != null && fullOcrText.isNotBlank()

    fun selectLanguage(language: LabelLanguage): OcrSession {
        val option = textOptions.singleOrNull { it.language == language } ?: return this
        return selectOption(option.segmentId)
    }

    fun selectOption(segmentId: String): OcrSession {
        val option = textOptions.singleOrNull { it.segmentId == segmentId } ?: return this
        return copy(
            editableText = option.text,
            analyses = emptyList(),
            resultStale = false,
            selectedOptionLanguage = option.language,
            selectedOptionBlockId = option.blockId,
            selectedOptionSegmentId = option.segmentId,
            ocrDiagnostics = ocrDiagnostics?.copy(
                selectedLanguage = option.language.displayName,
                selectedBlockId = option.blockId,
                selectedSegmentId = option.segmentId,
                selectedBlockCount = 1,
                rejectedBlockCount = (ocrDiagnostics.languageSegmentCount - 1).coerceAtLeast(0)
            ),
            fullTextSelected = false
        )
    }

    fun selectFullText(): OcrSession = if (!hasFullTextChoice()) this else copy(
        editableText = fullOcrText,
        analyses = emptyList(),
        resultStale = false,
        selectedOptionLanguage = null,
        selectedOptionBlockId = null,
        selectedOptionSegmentId = null,
        ocrDiagnostics = ocrDiagnostics?.copy(
            selectedLanguage = null,
            selectedBlockId = null,
            selectedSegmentId = null,
            selectedBlockCount = 0,
            rejectedBlockCount = 0
        ),
        fullTextSelected = true
    )

    fun addAnalysis(snapshot: AnalysisSnapshot): OcrSession =
        copy(analyses = analyses + snapshot, resultStale = false)

}

data class AnalysisSnapshot(
    val submittedText: String,
    val diagnosticReport: String,
    val displayResult: String,
    val timestampMillis: Long,
    val selectedBlockId: String? = null,
    /** Structured counterpart of diagnosticReport, retained in memory for this OCR session only. */
    val diagnostics: AnalysisDiagnostics? = null
)

internal object OcrExportReport {
    fun build(session: OcrSession, versionName: String): String = buildString {
        appendLine("Is It Vegan? — export OCR")
        appendLine("Version : $versionName")
        appendLine("Les données de cette session sont conservées en mémoire uniquement.")
        appendLine()
        session.ocrDiagnostics?.let { diagnostic ->
            appendLine("DIAGNOSTIC OCR")
            appendLine(
                "Rotation EXIF transmise à ML Kit : " +
                    (diagnostic.orientationDegrees?.let { "$it°" } ?: "indéterminée")
            )
            appendLine("Blocs géométriques ML Kit : ${diagnostic.blockCount}")
            appendLine("Segments linguistiques détectés : ${diagnostic.languageSegmentCount}")
            appendLine("Blocs sélectionnables : ${diagnostic.selectableBlockCount}")
            appendLine("Blocs sélectionnés : ${diagnostic.selectedBlockCount}")
            appendLine("Blocs rejetés : ${diagnostic.rejectedBlockCount}")
            appendLine("Lignes : ${diagnostic.lineCount}")
            appendLine("Zones détectées : ${diagnostic.detectedZones.ifEmpty { listOf("UNKNOWN") }.joinToString()}")
            diagnostic.detectedBlockDetails.forEach { appendLine("Bloc : $it") }
            appendLine("Langue sélectionnée : ${diagnostic.selectedLanguage ?: "UNKNOWN"}")
            appendLine("Identifiant du bloc sélectionné : ${diagnostic.selectedBlockId ?: if (session.fullTextSelected) "texte complet" else "non établi"}")
            appendLine("Identifiant du segment sélectionné (segmentation OCR initiale) : ${diagnostic.selectedSegmentId ?: "aucun"}")
            appendLine("Ordre de préférence : ${diagnostic.languagePreference.ifEmpty { listOf("FR", "EN", "NL") }.joinToString(" → ")}")
            diagnostic.selectionReason?.takeIf { it.isNotBlank() }?.let { appendLine("Raison du choix automatique initial : $it") }
            appendLine("Avertissements : ${diagnostic.warnings.ifEmpty { listOf("aucun") }.joinToString(" ; ")}")
            appendLine()
        }
        appendLine("TEXTE BRUT OCR (sortie originale ML Kit)")
        appendLine(session.rawOcrText?.ifBlank { "(aucun texte OCR)" } ?: "(aucun texte OCR)")
        appendLine()
        appendLine("TEXTE ÉDITABLE ACTUEL")
        appendLine(session.editableText.ifBlank { "(vide)" })
        appendLine()
        if (session.analyses.isEmpty()) {
            appendLine("ANALYSES")
            appendLine("(aucune analyse)")
        } else {
            session.analyses.forEachIndexed { index, analysis ->
                appendLine("ANALYSE ${index + 1} — texte exact soumis")
                appendLine("Identifiant du bloc analysé : ${analysis.selectedBlockId ?: "non établi pour le texte soumis"}")
                appendLine(analysis.submittedText.ifBlank { "(vide)" })
                appendLine("Résultat affiché")
                appendLine(analysis.displayResult)
                appendLine("Diagnostic détaillé")
                appendLine(analysis.diagnosticReport)
                if (index < session.analyses.lastIndex) appendLine()
            }
        }
        if (session.hasStaleResult() || session.analyses.any { it.submittedText != session.editableText }) {
            appendLine()
            appendLine("ÉTAT")
            appendLine("Au moins un résultat correspond à une ancienne version du texte éditable.")
        }
    }
}
