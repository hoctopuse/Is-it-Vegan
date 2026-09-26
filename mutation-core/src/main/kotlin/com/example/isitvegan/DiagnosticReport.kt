package com.example.isitvegan

object DiagnosticReport {
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
            appendLine(
                "Avertissements d’entrée : " + diagnostics.inputWarnings
                    .joinToString(" ; ") { it.displayName }.ifBlank { "aucun" }
            )
            appendLine()
            appendLine("LANGUE / BLOC SÉLECTIONNÉ")
            appendLine("Langue sélectionnée : ${diagnostics.labelSections.language.displayName}")
            appendLine("Identifiant du bloc sélectionné : ${diagnostics.labelSections.selectedBlockId ?: "texte complet"}")
            val selectedBlock = diagnostics.languageSegmentation.blocks
                .firstOrNull { it.id == diagnostics.languageSegmentation.selectedBlockId }
            appendLine("Identifiant du segment sélectionné : ${selectedBlock?.segmentId ?: "texte complet"}")
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
                        "frontière=${block.startIndex}…${block.endIndex}, longueur utile=${block.usefulLength}, " +
                        "tronqué=${if (block.manifestlyTruncated) "oui" else "non"}"
                )
                val criteria = block.selectionSignals.joinToString("; ").ifBlank { "aucun" }
                appendLine("  Critères : $criteria")
                if (block.id != diagnostics.languageSegmentation.selectedBlockId) {
                    val rejectionReason = when {
                        block.selectionSignals.any { it.contains("faux marqueur", true) } -> "faux marqueur"
                        block.manifestlyTruncated -> "bloc tronqué"
                        block.selectionSignals.any { it.contains("fragment court", true) } -> "bloc trop court"
                        selectedBlock != null && block.selectionScore < selectedBlock.selectionScore -> "qualité inférieure"
                        else -> "préférence linguistique entre blocs de qualité comparable"
                    }
                    appendLine("  Raison du rejet : $rejectionReason")
                }
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
            diagnostics.labelSections.ingredientSection?.let {
                appendLine("Frontière ingrédients : ${it.start}…${it.end}")
            }
            appendLine(
                "Séparateur du titre : " +
                    (diagnostics.labelSections.ingredientHeadingSeparator?.displayName ?: "aucun")
            )
            appendLine("Section présence réelle : ${if (diagnostics.labelSections.declaredContainsText != null) "détectée" else "non détectée"}")
            appendLine("Syntaxe de présence réelle : ${diagnostics.labelSections.declaredContainsSyntax ?: "aucune"}")
            appendLine("Section traces : ${if (diagnostics.labelSections.traceSection != null) "détectée" else "non détectée"}")
            diagnostics.labelSections.traceSections.forEachIndexed { index, section ->
                appendLine("Frontière traces ${index + 1} : ${section.start}…${section.end}")
                appendLine("Texte traces ${index + 1} : ${section.rawText}")
                appendLine("Traces normalisées ${index + 1} : ${section.normalizedText}")
            }
            appendLine("Sections produit ignorées : ${diagnostics.labelSections.ignoredSections.joinToString(" | ").ifBlank { "aucune" }}")
            diagnostics.ignoredSectionDiagnostics.forEach {
                appendLine("  Exclusion : ${it.text} | raison=${it.reason.displayName}")
            }
            appendLine()
            appendLine("COMPOSITION APRÈS PRÉTRAITEMENT")
            appendLine(diagnostics.preprocessedInput.ifBlank { "(vide)" })
            val ocrCorrections = diagnostics.ocrCorrections.joinToString(" ; ").ifBlank { "(aucune)" }
            val structure = diagnostics.parenthesisStructure
            appendLine("Structure OCR : balanced=${structure.balanced}; missingClosings=${structure.missingClosings}; unexpectedClosings=${structure.unexpectedClosings}; maximumDepth=${structure.maximumDepth}; recoveryApplied=${structure.recoveryApplied}")
            if (!structure.balanced) appendLine("Alerte structurelle : structure OCR incomplète ; les segments non fiables restent inconnus.")
            appendLine("Corrections OCR appliquées : $ocrCorrections")
            appendLine("Présence réelle déclarée : ${diagnostics.declaredPresenceText ?: "(aucune)"}")
            appendLine()
            appendLine("TRACES / CONTAMINATION CROISÉE")
            appendLine(diagnostics.crossContactWarnings.joinToString("\n").ifBlank { "(aucune)" })
            appendLine("Influence des traces sur le verdict : aucune (traces exclues de l’analyse)")
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
                appendLine("Végétarien selon les ingrédients reconnus, hors éléments incertains : ${result.vegetarianVerdictWithoutUncertain}")
            }
            appendLine(
                "Bloqueurs détectés : " + result.veganBlockers
                    .joinToString(", ") { it.id }.ifBlank { "aucun" }
            )
            val groups = diagnostics.ingredientGroups
            appendLine("Ingrédients vegan : ${groups.veganIngredientIds.joinToString().ifBlank { "aucun" }}")
            appendLine("Ingrédients végétariens non vegan : ${groups.vegetarianIngredientIds.joinToString().ifBlank { "aucun" }}")
            appendLine("Ingrédients non végétariens : ${groups.nonVegetarianIngredientIds.joinToString().ifBlank { "aucun" }}")
            appendLine("Ingrédients incertains : ${groups.uncertainIngredientIds.joinToString().ifBlank { "aucun" }}")
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
            val decision = diagnostics.decision
            appendLine("Éléments responsables du verdict : ${decision.responsibleIngredientIds.joinToString().ifBlank { "aucun identifiant connu" }}")
            appendLine("Raison de décision : ${decision.reason.displayName}")
            appendLine("Un ingrédient inconnu empêche un verdict VEGAN : ${if (decision.unknownPreventsVegan) "oui" else "non"}")
            appendLine("Traces prises en compte dans le verdict : ${if (decision.tracesExcludedFromVerdict) "non" else "oui"}")
            val explanation = diagnostics.verdictExplanation
            appendLine()
            appendLine("EXPLICATION CONDITIONNELLE 0.6.9")
            appendLine("Verdict principal conservé : ${explanation.mainVerdict ?: "non calculé"}")
            appendLine(
                "Ingrédients incertains exclus : " + explanation.uncertainIngredients
                    .joinToString(" ; ") { ingredient ->
                        "${ingredient.ingredientId}#${ingredient.occurrenceId} " +
                            "[${ingredient.path.joinToString(" → ")}]"
                    }.ifBlank { "aucun" }
            )
            appendLine(
                "Bloqueurs connus conservés : " + explanation.knownBlockingIngredients
                    .joinToString(", ") { it.ingredientId }.ifBlank { "aucun" }
            )
            appendLine("Résultat conditionnel hors incertains : ${explanation.conditionalVerdict ?: "non affiché"}")
            appendLine("Raison du résultat conditionnel : ${explanation.conditionalReason.displayName}")
            appendLine("Statut végétarien informatif : ${explanation.vegetarianStatus ?: "non calculé"}")
            appendLine(
                "Traces prises en compte dans le résultat conditionnel : " +
                    if (explanation.tracesExcludedFromConditionalVerdict) "non" else "oui"
            )
        }.trimEnd()
    }

    private val DiagnosticInputWarning.displayName: String
        get() = when (this) {
            DiagnosticInputWarning.EMPTY_INPUT -> "texte vide"
            DiagnosticInputWarning.MANIFESTLY_TRUNCATED_BLOCK -> "bloc linguistique manifestement tronqué"
            DiagnosticInputWarning.UNBALANCED_STRUCTURE -> "parenthèses ou crochets incohérents"
        }

    private val IgnoredSectionReason.displayName: String
        get() = when (this) {
            IgnoredSectionReason.OUTSIDE_INGREDIENT_COMPOSITION ->
                "section hors composition, exclue du parsing et du verdict"
        }

    private val DecisionReason.displayName: String
        get() = when (this) {
            DecisionReason.NO_INGREDIENT_LIST -> "aucune liste d’ingrédients exploitable"
            DecisionReason.EXPLICIT_NON_VEGAN_ORIGIN -> "origine explicitement incompatible avec un verdict vegan"
            DecisionReason.KNOWN_VEGAN_BLOCKER -> "au moins un ingrédient reconnu est incompatible avec un verdict vegan"
            DecisionReason.UNCERTAIN_INGREDIENT -> "au moins un ingrédient reconnu a un statut incertain"
            DecisionReason.UNKNOWN_INGREDIENT -> "au moins un ingrédient inconnu empêche de conclure VEGAN"
            DecisionReason.NO_RECOGNIZED_INGREDIENT -> "aucun ingrédient reconnu"
            DecisionReason.ALL_RECOGNIZED_INGREDIENTS_VEGAN -> "tous les ingrédients analysés sont reconnus vegan"
        }

    private val ConditionalVerdictReason.displayName: String
        get() = when (this) {
            ConditionalVerdictReason.UNCERTAIN_INGREDIENTS_EXCLUDED ->
                "les seuls ingrédients incertains ont été exclus"
            ConditionalVerdictReason.KNOWN_NON_VEGETARIAN_INGREDIENT_REMAINS ->
                "un ingrédient non végétarien connu reste bloquant"
            ConditionalVerdictReason.UNKNOWN_INGREDIENT_REMAINS ->
                "un ingrédient non identifié reste bloquant"
            ConditionalVerdictReason.NO_RELIABLE_ANALYSIS ->
                "aucune analyse fiable du reste de la composition"
            ConditionalVerdictReason.NO_UNCERTAIN_INGREDIENT ->
                "aucun ingrédient incertain à exclure"
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
