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
    val fullTextSelected: Boolean = false
) {
    fun withOcrText(text: String): OcrSession = copy(rawOcrText = text, editableText = text)

    fun withOcrResult(result: OcrProcessingResult): OcrSession = copy(
        rawOcrText = result.rawText,
        editableText = result.editableText,
        ocrDiagnostics = result.diagnostics,
        fullOcrText = result.fullText,
        textOptions = result.textOptions,
        selectedOptionLanguage = result.selectedOptionLanguage,
        selectedOptionBlockId = result.selectedOptionBlockId,
        fullTextSelected = result.selectedOptionLanguage == null
    )

    fun withEditableText(text: String): OcrSession = copy(editableText = text)

    fun selectLanguage(language: LabelLanguage): OcrSession {
        val option = textOptions.firstOrNull { it.language == language } ?: return this
        return copy(
            editableText = option.text,
            analyses = emptyList(),
            selectedOptionLanguage = language,
            selectedOptionBlockId = option.blockId,
            fullTextSelected = false
        )
    }

    fun selectFullText(): OcrSession = copy(
        editableText = fullOcrText,
        analyses = emptyList(),
        selectedOptionLanguage = null,
        selectedOptionBlockId = null,
        fullTextSelected = true
    )

    fun addAnalysis(snapshot: AnalysisSnapshot): OcrSession =
        copy(analyses = analyses + snapshot)
}

data class AnalysisSnapshot(
    val submittedText: String,
    val diagnosticReport: String,
    val displayResult: String,
    val timestampMillis: Long
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
            appendLine("Blocs : ${diagnostic.blockCount}")
            appendLine("Lignes : ${diagnostic.lineCount}")
            appendLine("Zones détectées : ${diagnostic.detectedZones.ifEmpty { listOf("UNKNOWN") }.joinToString()}")
            diagnostic.detectedBlockDetails.forEach { appendLine("Bloc : $it") }
            appendLine("Langue sélectionnée : ${diagnostic.selectedLanguage ?: "UNKNOWN"}")
            appendLine("Identifiant du bloc sélectionné : ${diagnostic.selectedBlockId ?: "texte complet"}")
            appendLine("Ordre de préférence : ${diagnostic.languagePreference.ifEmpty { listOf("FR", "EN", "NL") }.joinToString(" → ")}")
            diagnostic.selectionReason?.takeIf { it.isNotBlank() }?.let { appendLine("Raison du choix : $it") }
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
                appendLine(analysis.submittedText.ifBlank { "(vide)" })
                appendLine("Résultat affiché")
                appendLine(analysis.displayResult)
                appendLine("Diagnostic détaillé")
                appendLine(analysis.diagnosticReport)
                if (index < session.analyses.lastIndex) appendLine()
            }
        }
        if (session.analyses.any { it.submittedText != session.editableText }) {
            appendLine()
            appendLine("ÉTAT")
            appendLine("Au moins un résultat correspond à une ancienne version du texte éditable.")
        }
    }
}
