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
            appendLine("LANGUE / BLOC SÉLECTIONNÉ")
            appendLine("Langue sélectionnée : ${diagnostics.labelSections.language.displayName}")
            appendLine("Blocs détectés : " + diagnostics.languageSegmentation.blocks
                .map { it.language.displayName }.distinct().joinToString(", "))
            appendLine("Marqueur sélectionné : ${diagnostics.languageSegmentation.detectedMarker ?: "aucun"}")
            appendLine(
                "Autres blocs ignorés : " + diagnostics.languageSegmentation.ignoredLanguages
                    .joinToString(", ") { it.displayName }
                    .ifBlank { "aucun" }
            )
            appendLine(
                "Fallback texte complet : " +
                    if (diagnostics.languageSegmentation.usedFallback) "oui" else "non"
            )
            appendLine()
            appendLine("SECTIONS DÉTECTÉES")
            appendLine("Section ingrédients : ${if (diagnostics.labelSections.hasIngredientHeading) "détectée" else "non détectée"}")
            appendLine("Section présence réelle : ${if (diagnostics.labelSections.declaredContainsText != null) "détectée" else "non détectée"}")
            appendLine("Section traces : ${if (diagnostics.labelSections.tracesText != null) "détectée" else "non détectée"}")
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
                    append("${index + 1}. ${token.kind} | profondeur=${token.depth} | ${token.text}")
                    token.parentOrder?.let { append(" | parent=${it + 1}") }
                    token.matcherText?.takeIf { it != token.text }
                        ?.let { append(" | texte matcher=$it") }
                    when (token.matchKind) {
                        MatchKind.NONE -> append(" | correspondances=aucune")
                        MatchKind.EXACT -> append(" | correspondances=${token.matchedIngredientIds.joinToString(",")}")
                        MatchKind.PARTIAL_CONTEXTUAL -> append(
                            " | correspondance contextuelle=${token.matchedIngredientIds.joinToString(",")}"
                        )
                    }
                    if (token.kind == NodeKind.COMPOSITE_INGREDIENT && token.unknown == null &&
                        token.matchKind != MatchKind.EXACT
                    ) {
                        append(" | conteneur analysé | inconnus propres=aucun")
                    }
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
