package com.example.isitvegan

internal object DiagnosticReport {
    fun build(diagnostics: AnalysisDiagnostics, versionName: String): String {
        val result = diagnostics.result
        return buildString {
            appendLine("Is It Vegan? — rapport de diagnostic")
            appendLine("Version : $versionName")
            appendLine()
            appendLine("ENTRÉE")
            appendLine(diagnostics.input.ifBlank { "(vide)" })
            appendLine()
            appendLine("COMPOSITION APRÈS PRÉTRAITEMENT")
            appendLine(diagnostics.preprocessedInput.ifBlank { "(vide)" })
            appendLine()
            appendLine("TRACES / CONTAMINATION CROISÉE")
            appendLine(diagnostics.crossContactWarnings.joinToString("\n").ifBlank { "(aucune)" })
            appendLine()
            appendLine("NOTES EXCLUES")
            appendLine(diagnostics.excludedNotes.joinToString("\n").ifBlank { "(aucune)" })
            appendLine()
            appendLine("ÉTAPES D'ANALYSE")
            if (diagnostics.tokens.isEmpty()) {
                appendLine("(aucun élément à analyser)")
            } else {
                diagnostics.tokens.forEachIndexed { index, token ->
                    val matches = token.matchedIngredientIds.ifEmpty { listOf("aucune") }
                    append("${index + 1}. ${token.kind} | profondeur=${token.depth} | ${token.text}")
                    token.parentOrder?.let { append(" | parent=${it + 1}") }
                    token.matcherText?.takeIf { it != token.text }
                        ?.let { append(" | texte matcher=$it") }
                    append(" | correspondances=${matches.joinToString(",")}")
                    token.unknown?.let { append(" | inconnu=$it") }
                    appendLine()
                }
            }
            appendLine()
            appendLine("RÉSULTAT")
            appendLine("Verdict : ${result.verdict}")
            appendLine("Verdict sans les incertains : ${result.verdictWithoutUncertain}")
            appendLine("Analyse arrêtée tôt : ${if (result.stoppedAtNonVegetarian) "oui" else "non"}")
            appendLine("Reconnus : ${result.matched.joinToString(", ") { "${it.id} (${it.status})" }.ifBlank { "aucun" }}")
            appendLine("Inconnus : ${result.unknown.joinToString(", ").ifBlank { "aucun" }}")
        }.trimEnd()
    }
}
