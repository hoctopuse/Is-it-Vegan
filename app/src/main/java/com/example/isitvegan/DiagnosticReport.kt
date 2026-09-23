package com.example.isitvegan

internal object DiagnosticReport {
    fun build(diagnostics: AnalysisDiagnostics, versionName: String): String {
        val result = diagnostics.result
        return buildString {
            appendLine("Is It Vegan? — rapport de diagnostic")
            appendLine("Version : $versionName")
            appendLine("Mode d’entrée : ${diagnostics.inputMode.displayName}")
            appendLine("Disponibilité : ${diagnostics.result.availability.displayName}")
            appendLine(
                "Fallback manuel : ${if (diagnostics.usedManualFallback) "oui" else "non"}"
            )
            appendLine("Portée : compatibilité vegan selon les ingrédients déclarés")
            diagnostics.availabilityReason?.let { appendLine("Raison : $it") }
            appendLine()
            appendLine("ENTRÉE")
            appendLine(diagnostics.input.ifBlank { "(vide)" })
            appendLine()
            appendLine("LANGUE / BLOC SÉLECTIONNÉ")
            appendLine("Langue sélectionnée : ${diagnostics.labelSections.language.displayName}")
            appendLine("Identifiant du bloc sélectionné : ${diagnostics.labelSections.selectedBlockId ?: "texte complet"}")
            val detectedLanguages = diagnostics.languageSegmentation.blocks
                .map { it.language.displayName }.distinct().joinToString(", ")
            appendLine("Blocs détectés : $detectedLanguages")
            appendLine("Segments détectés dans le texte soumis : $detectedLanguages")
            appendLine("Nombre de segments détectés dans le texte soumis : ${diagnostics.languageSegmentation.blocks.size}")
            appendLine("Nombre de blocs sélectionnables : ${diagnostics.languageSegmentation.blocks.size}")
            appendLine("Nombre de blocs sélectionnés : ${if (diagnostics.languageSegmentation.selectedBlockId == null) 0 else 1}")
            appendLine("Nombre de blocs rejetés : ${diagnostics.languageSegmentation.blocks.count { it.id != diagnostics.languageSegmentation.selectedBlockId }}")
            diagnostics.languageSegmentation.blocks.forEachIndexed { index, block ->
                appendLine(
                    "Bloc ${index + 1} (${block.id}, ${block.segmentId}) ${if (block.id == diagnostics.languageSegmentation.selectedBlockId) "retenu" else "non retenu"} : marqueur=${block.detectedMarker ?: "aucun"}, " +
                        "langue originale=${block.headingLanguage?.displayName ?: block.language.displayName}, " +
                        "langue normalisée=${block.language.displayName}, score=${block.selectionScore}, " +
                        "longueur utile=${block.usefulLength}, " +
                        "tronqué=${if (block.manifestlyTruncated) "oui" else "non"}"
                )
                val criteria = block.selectionSignals.joinToString("; ").ifBlank { "aucun" }
                appendLine("  Critères : $criteria")
                block.languageCorrectionReason?.let { appendLine("Correction : $it") }
            }
            appendLine("Marqueur sélectionné : ${diagnostics.languageSegmentation.detectedMarker ?: "aucun"}")
            appendLine(
                "Autres blocs ignorés : " + diagnostics.languageSegmentation.ignoredLanguages
                    .joinToString(", ") { it.displayName }
                    .ifBlank { "aucun" }
            )
            appendLine(
                "Blocs multilingues non sélectionnés : " + diagnostics.languageSegmentation.ignoredLanguages
                    .joinToString(", ") { it.displayName }
                    .ifBlank { "aucun" }
            )
            appendLine(
                "Fallback texte complet : " +
                    if (diagnostics.languageSegmentation.usedFallback) "oui" else "non"
            )
            appendLine("Raison de sélection : ${diagnostics.languageSegmentation.selectionReason}")
            appendLine(
                "Blocs sans titre rejetés : " +
                    diagnostics.languageSegmentation.rejectedUntitledLanguages
                        .joinToString(", ") { it.displayName }.ifBlank { "aucun" }
            )
            appendLine()
            appendLine("SECTIONS DÉTECTÉES")
            appendLine("Section ingrédients : ${if (diagnostics.labelSections.hasIngredientHeading) "détectée" else "non détectée"}")
            appendLine("Titre détecté : ${diagnostics.labelSections.ingredientHeadingText ?: "aucun"}")
            appendLine(
                "Séparateur du titre : " +
                    (diagnostics.labelSections.ingredientHeadingSeparator?.displayName ?: "aucun")
            )
            appendLine("Section présence réelle : ${if (diagnostics.labelSections.declaredContainsText != null) "détectée" else "non détectée"}")
            appendLine("Syntaxe de présence réelle : ${diagnostics.labelSections.declaredContainsSyntax ?: "aucune"}")
            appendLine("Section traces : ${if (diagnostics.labelSections.traceSection != null) "détectée" else "non détectée"}")
            diagnostics.labelSections.traceSection?.let {
                appendLine("Frontière traces : ${it.start}…${it.end}")
                appendLine("Texte traces : ${it.rawText}")
                appendLine("Traces normalisées : ${it.normalizedText}")
            }
            appendLine()
            appendLine("COMPOSITION APRÈS PRÉTRAITEMENT")
            appendLine(diagnostics.preprocessedInput.ifBlank { "(vide)" })
            appendLine("Présence réelle déclarée : ${diagnostics.declaredPresenceText ?: "(aucune)"}")
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
                    append("${index + 1}. ${token.nodeKind} | profondeur=${token.depth} | ${token.text}")
                    token.quantityPercent?.let {
                        append(" | quantité=${it.stripTrailingZeros().toPlainString().replace('.', ',')} %")
                    }
                    token.parentOrder?.let { append(" | parent=${it + 1}") }
                    token.functionalClass?.let { append(" | classe fonctionnelle originale=$it") }
                    token.functionalClassCanonical?.let { append(" | classe fonctionnelle canonique=$it") }
                    if (token.isNano) append(" | nano=oui (${token.nanoText})")
                    if (token.variableProportions) {
                        append(" | proportions variables=oui (${token.variableProportionsText})")
                    }
                    if (token.hasAlternatives) {
                        append(" | alternatives et/ou=oui (${token.alternativesText})")
                    }
                    token.originRuleId?.let { append(" | règle d’origine=$it") }
                    token.originOutcomeId?.let { append(" | résultat d’origine=$it") }
                    token.originQualifierText?.let { append(" ($it)") }
                    if (token.isDeclaredPresence) append(" | preuve de présence réelle=oui")
                    if (token.nodeKind == IngredientNodeKind.COMPOSITE) {
                        append(" | enfants=${token.childCount}")
                    }
                    token.matcherText?.takeIf { it != token.text }
                        ?.let { append(" | texte matcher=$it") }
                    token.correctedText?.let { append(" | texte corrigé=$it") }
                    token.multilingualAlias?.let { append(" | alias multilingue=$it") }
                    token.canonicalConceptId?.let { append(" | concept canonique=$it") }
                    token.canonicalConceptAvailable?.let { available ->
                        append(
                            " | disponibilité du concept=" + if (available) {
                                "présente dans ingredients.json"
                            } else {
                                "absente de ingredients.json"
                            }
                        )
                        if (!available) append(" | classification=non classifiable")
                    }
                    token.matchingLanguage?.let { append(" | langue matching=${it.displayName}") }
                    if (token.nodeKind == IngredientNodeKind.COMPOSITE) {
                        append(" | conteneur analysé | inconnus propres=aucun")
                    } else {
                        when (token.matchKind) {
                            MatchKind.NONE -> append(" | correspondances=aucune")
                            MatchKind.EXACT -> append(" | correspondances=${token.matchedIngredientIds.joinToString(",")}")
                            MatchKind.COVERED -> append(
                                " | correspondance couverte=${token.matchedIngredientIds.joinToString(",")}"
                            )
                            MatchKind.PARTIAL_CONTEXTUAL -> append(
                                " | correspondance contextuelle=${token.matchedIngredientIds.joinToString(",")}"
                            )
                            MatchKind.BLOCKED_CONFLICT -> {
                                append(" | correspondance contextuelle=${token.matchedIngredientIds.joinToString(",")}")
                                append(" | conflits bloqués=${token.blockedIngredientIds.joinToString(",")}")
                            }
                        }
                        token.unknown?.let { append(" | inconnu=$it") }
                        if (token.baseStatuses.isNotEmpty()) {
                            append(" | classification de base=${token.baseStatuses.joinToString(",")}")
                            append(" | classification effective=${token.effectiveStatuses.joinToString(",")}")
                        }
                        token.originResolution?.let { append(" | résolution par origine=$it") }
                    }
                    appendLine()
                }
            }
            appendLine()
            appendLine("CHARGEMENT DES RÈGLES D’ORIGINE")
            appendLine(diagnostics.originRuleErrors.joinToString("\n").ifBlank { "valide" })
            appendLine()
            appendLine("RÉSULTAT")
            if (result.availability == AnalysisAvailability.NO_INGREDIENT_LIST &&
                result.matched.isEmpty()
            ) {
                appendLine("Compatibilité vegan selon les ingrédients déclarés : non évaluée")
                appendLine("Classification détaillée : non calculée")
            } else {
                appendLine(
                    "Compatibilité vegan selon les ingrédients déclarés : " +
                        result.veganAssessment.displayName
                )
                appendLine("Classification détaillée : ${result.verdict}")
                appendLine("Verdict sans les incertains : ${result.verdictWithoutUncertain}")
            }
            appendLine(
                "Bloqueurs détectés : " + result.veganBlockers
                    .joinToString(", ") { it.id }.ifBlank { "aucun" }
            )
            appendLine(
                "Preuves de présence réelle : " + result.declaredPresenceIngredientIds
                    .joinToString(", ").ifBlank { "aucune" }
            )
            appendLine(
                "Origines explicitement non vegan : " + result.originNonVeganIngredientIds
                    .joinToString(", ").ifBlank { "aucune" }
            )
            appendLine("Analyse arrêtée tôt : ${if (result.stoppedAtNonVegetarian) "oui" else "non"}")
            appendLine("Reconnus : ${result.matched.joinToString(", ") { "${it.id} (${it.status})" }.ifBlank { "aucun" }}")
            appendLine("Inconnus : ${result.unknown.joinToString(", ").ifBlank { "aucun" }}")
        }.trimEnd()
    }

    private val VeganAssessment.displayName: String
        get() = when (this) {
            VeganAssessment.VEGAN -> "VEGAN"
            VeganAssessment.NOT_VEGAN -> "NON VEGAN"
            VeganAssessment.UNCERTAIN -> "INCERTAINE"
        }

    private val InputMode.displayName: String
        get() = when (this) {
            InputMode.MANUAL_INGREDIENT_LIST -> "liste manuelle"
            InputMode.FULL_LABEL -> "étiquette complète"
            InputMode.OCR_LABEL -> "étiquette OCR"
        }

    private val AnalysisAvailability.displayName: String
        get() = when (this) {
            AnalysisAvailability.INGREDIENT_LIST_ANALYZED -> "liste d’ingrédients analysée"
            AnalysisAvailability.NO_INGREDIENT_LIST -> "aucune liste d’ingrédients"
        }

    private val HeadingSeparator.displayName: String
        get() = when (this) {
            HeadingSeparator.COLON -> "deux-points"
            HeadingSeparator.DASH -> "tiret"
            HeadingSeparator.LINE_BREAK -> "retour à la ligne"
        }
}
