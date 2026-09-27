# Revue documentaire — stabilisation 0.6.9

Date : 2026-09-26  
Branche revue : `master`  
Périmètre : lecture seule, hors création de ce rapport.

## Décision finale : NO-GO

Le site se construit et la presque totalité des affirmations ajoutées correspond au code et aux rapports 0.6.9–0.6.9.8. Une incohérence documentaire importante empêche toutefois un GO pour le commit et la publication : `docs/guide-developpeur.md` affirme encore que les ressources Android sont uniquement FR/EN/NL et demande de maintenir « les trois fichiers », alors que `UiLanguage` contient aussi `DE` et que `app/src/main/res/values-de/strings.xml` est présent et utilisé.

Ce défaut ne bloque pas techniquement GitHub Pages, mais il contredit le README, l’accueil du site et le comportement livré. Il doit être corrigé avant publication documentaire.

## Affirmations validées

| Sujet | État | Preuves |
|---|---|---|
| Traitement métier local, sans verdict par IA/cloud | Validé avec la nuance réseau documentée | `docs/vie-privee.md` : aucun client HTTP, endpoint, LLM ou traduction; le traitement dépend des données et du modèle OCR embarqués. Le README parle correctement d’absence de décision par cloud et renvoie vers cette nuance. |
| Verdicts et rôle de `VEGETARIAN` | Validé | `AnalysisVerdict` et `VeganAssessment` dans `VeganAnalyzer.kt`; priorité dans `VerdictEngine.kt`. `VEGETARIAN` est une classification détaillée et un bloqueur de compatibilité vegan, sans résultat vegan favorable. |
| Traces séparées | Validé | `LabelSectionExtractor`, `LabelPreprocessor`, `crossContactWarnings`, `DecisionDiagnostic.tracesExcludedFromVerdict`; `CrossContactTest` et les rapports 0.6.9. Les traces ne deviennent pas des tokens du verdict. |
| Présence réelle déclarée | Validé | `VeganAnalyzer.kt` construit des `presenceTokens` et `declaredPresenceIngredientIds`; `docs/sections.md` décrit la section distincte `contient`. |
| FR/NL/EN/DE | Validé dans le code, contradictoire dans le guide développeur | `UiLanguage.kt` contient FR/EN/NL/DE; répertoires `values`, `values-en`, `values-nl`, `values-de`; ressources conditionnelles et traces DE présentes. Contradiction relevée ci-dessous. |
| OCR éditable et limites | Validé | `OcrSession`/`OcrFirstScreen`, `docs/ocr.md`, ressources `check_text`; les limites de photo sont formulées comme recommandations manuelles. |
| Arbre, composites, parenthèses, crochets, quantités, qualificatifs | Validé | `IngredientTreeParser.kt`, `IngredientTokenizer.kt`, `IngredientTreeParserTest`; `COMPOSITE` est structurel, les feuilles/additifs seuls sont classés. |
| Inconnus bruts, visibles et détail | Validé | `AnalysisResult.unknown`, `visibleUnknownTokens`, `visibleUnknownIngredients`, `ingredientGroups` et `decision`; correctifs et revue 0.6.9.7. |
| Conditionnel hors incertains | Validé | `verdictWithoutUncertain` rappelle `VerdictEngine`; `DiagnosticModels.toVerdictExplanation()` rend `conditionalVerdict` nul avec `UNKNOWN_INGREDIENT_REMAINS`; `VerdictExplanation069Test` et `UncertainUnknownVisibility097Test`. |
| Contexte parent → enfant | Validé | `VerdictExplanationFormatter.renderUnknownIngredients()` remonte `parentOrder`; revue et rapport 0.6.9.8 confirment que le parent reste hors des données métier. |
| Absence de propagation de statut vers `COMPOSITE` | Validé | Branche précoce `COMPOSITE_INGREDIENT` dans `VeganAnalyzer.kt`, `IngredientTreeParser`/`IngredientTokenizer`, et décision `COMPOSITE_STATUS_PROPAGATION_AUDIT_0_6_9_9.md`. |

## Incohérence à corriger

### Important — ressources allemandes omises du guide développeur

`docs/guide-developpeur.md` contient deux affirmations obsolètes :

- l’arborescence annonce `ressources Compose/Android FR, EN, NL` ;
- la section « Ressources et langues UI » liste trois répertoires et demande de maintenir « les trois fichiers ».

Le code contient pourtant `values-de`, `UiLanguage.DE`, les chaînes allemandes de verdict conditionnel et de traces, ainsi que les choix de blocs DE. Le README, `docs/index.md`, `docs/langues.md` et les tests instrumentés présentent déjà FR/NL/EN/DE. Le guide doit être aligné sur quatre ressources avant commit.

## Limites et futur 0.7

Conforme : README, accueil, OCR, langues et changelog indiquent que la consolidation de plusieurs langues, l’exploitation des blocs comme aide de validation enrichie et la suggestion automatique de reprendre une photo plus large ne sont pas livrées. Ils recommandent seulement de vérifier/corriger le texte ou de reprendre une image plus lisible. Aucune fonctionnalité future n’est présentée comme disponible.

## Changelog, navigation et page générée

- `docs/changelog.md` est cohérent avec 0.6.9 puis 0.6.9.1 à 0.6.9.8; l’audit 0.6.9.9 est correctement présenté comme décision d’architecture.
- `mkdocs.yml` contient une entrée « Notes de version » et toutes les pages documentaires de premier niveau utiles.
- Les liens Markdown relatifs sont valides.
- `docs/generated/base-connaissances-data.md` est volontairement hors `nav`: il est inclus dans `docs/base-connaissances.md` par le snippet `--8<-- "generated/base-connaissances-data.md"`. Le message informatif MkDocs est donc attendu, sans page utilisateur oubliée.

## État de la publication GitHub Pages

**GO technique.** Le workflow `docs.yml` prépare un runner vierge avec `actions/setup-python@v6` et Python 3.12, met en cache sur le fichier existant `requirements-docs.txt`, installe `mkdocs-material==9.6.20`, exécute `python -m mkdocs build --strict`, puis publie `site` via `actions/upload-pages-artifact@v3`. Il ne référence pas `.venv`.

Le build local strict avec l’environnement disponible réussit. La balise `!!python/name:pymdownx.superfences.fence_code_format` de `mkdocs.yml` est attendue par MkDocs Material; une lecture YAML générique ne la construit pas, mais `mkdocs build --strict` la traite correctement.

## Contrôles exécutés

- Lecture des consignes, rapports de stabilisation, rapports/revues 0.6.9–0.6.9.8, audit 0.6.9.9, README, configuration, workflow et pages modifiées.
- Inspection ciblée des sources de parsing, analyse, verdict, diagnostics, sections, langues, formatter et ressources Android.
- Vérification des liens Markdown relatifs : succès.
- `./.venv/Scripts/python.exe -m mkdocs build --strict` : succès; seul message informatif attendu sur la page générée hors navigation.
- Validation statique YAML du workflow et de ses chemins : succès.
- `git diff --check`, `git diff --cached --check`, `git diff`, `git diff --stat` et `git status --short` : exécutés.

## État Git final

Le worktree contient les modifications documentaires de stabilisation existantes, `docs/changelog.md`, `DOCUMENTATION_STABILIZATION_0_6_9_REPORT.md` et ce rapport de revue. Aucun fichier Kotlin/Java, test, donnée, asset, version, index Git, commit ou push n’a été modifié pendant cette revue. Seul `REVIEW_DOCUMENTATION_STABILIZATION_0_6_9.md` a été créé.
