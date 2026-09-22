package com.example.isitvegan

/** In-memory state for one OCR work session. The URI is deliberately not part of this model. */
internal data class OcrSession(
    val rawOcrText: String? = null,
    val editableText: String = "",
    val analyses: List<AnalysisSnapshot> = emptyList(),
    val ocrDiagnostics: OcrDiagnostics? = null
) {
    fun withOcrText(text: String): OcrSession = copy(rawOcrText = text, editableText = text)

    fun withOcrResult(result: OcrProcessingResult): OcrSession = copy(
        rawOcrText = result.rawText,
        editableText = result.editableText,
        ocrDiagnostics = result.diagnostics
    )

    fun withEditableText(text: String): OcrSession = copy(editableText = text)

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
