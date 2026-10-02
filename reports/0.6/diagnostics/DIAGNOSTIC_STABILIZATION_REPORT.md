# Rapport de stabilisation du diagnostic

Date : 25 septembre 2026

## État initial

- Branche : `master`.
- Arbre de travail initial : propre (`git status --short` sans sortie).
- Les trois commandes de baseline passaient avant modification : `:mutation-core:test`, `testDebugUnitTest` et `assembleDebug`.
- Le pipeline séparait déjà OCR, nettoyage, segmentation linguistique, extraction des sections, parsing hiérarchique, matching et verdict.

## Informations déjà disponibles

- `OcrSession` conservait séparément la sortie originale ML Kit, le texte éditable, le texte nettoyé complet, le bloc sélectionné et les analyses précédentes.
- `OcrDiagnostics` exposait orientation EXIF, blocs/lignes, langues, scores, critères, sélection et avertissements.
- `LanguageSegmentation` exposait les blocs, langues, marqueurs, scores, signaux de sélection, offsets et troncature.
- `LabelSections` séparait composition, présence réelle, traces et sections ignorées.
- `AnalysisDiagnostics` exposait texte soumis, prétraitement, corrections OCR, arbre, tokens, pourcentages, qualificatifs, correspondances, inconnus et résultat.
- `AnalysisResult` exposait compatibilité vegan, verdict détaillé, statut végétarien secondaire, bloqueurs, incertains, inconnus et traces.

## Informations perdues ou ambiguës

- Un `AnalysisSnapshot` ne conservait que le rapport texte rendu ; le modèle `AnalysisDiagnostics` n’était plus accessible depuis l’historique structuré de la session OCR.
- Les raisons finales et les éléments responsables devaient être déduits de plusieurs champs.
- L’exclusion des traces du verdict était effective et testée, mais seulement implicite dans le rapport.
- Les entrées vides, tronquées ou structurellement incohérentes n’avaient pas de codes d’avertissement communs.
- Les sections ignorées ne portaient pas de motif structuré.
- La frontière de la composition et les frontières de chaque section de traces n’étaient pas toutes conservées ; le champ historique `traceSection` agrégeait plusieurs traces.

## Modèle de diagnostic retenu

Le cœur JVM fournit désormais des vues dérivées, sans nouvelle passe d’analyse :

- `DiagnosticInputWarning` pour entrée vide, bloc tronqué et structure incohérente ;
- `IngredientDiagnosticGroups` pour les identifiants vegan, végétariens, non végétariens et incertains, plus les inconnus ;
- `DecisionDiagnostic` pour verdicts, raison codée, responsables, inconnus bloquants et exclusion des traces ;
- `IgnoredSectionDiagnostic` pour le texte exclu et son motif ;
- `IngredientSection` et `traceSections` pour les bornes et textes de sections.

Les propriétés sont calculées depuis `AnalysisDiagnostics` et `AnalysisResult`. Elles ne participent pas au verdict. Les champs historiques `tracesText` et `traceSection` restent présents.

Côté Android, `AnalysisSnapshot.diagnostics` conserve optionnellement le modèle structuré déjà calculé. Le texte OCR brut reste porté par `OcrSession` et n’est pas dupliqué dans chaque instantané.

## Fichiers modifiés

- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/VeganAnalyzer.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/LabelSectionExtractor.kt`
- `mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticReport.kt`
- `app/src/main/java/com/example/isitvegan/OcrSession.kt`
- `app/src/main/java/com/example/isitvegan/OcrFirstScreen.kt`
- `mutation-core/src/test/kotlin/com/example/isitvegan/DiagnosticStabilizationTest.kt`
- `app/src/test/java/com/example/isitvegan/OcrSessionTest.kt`
- `docs/diagnostic.md`
- `docs/sections.md`
- `DIAGNOSTIC_STABILIZATION_REPORT.md`

`ingredients.json`, les alias et ML Kit n’ont pas été modifiés. La mise en page et le comportement visible Compose restent inchangés ; seul l’appel qui construit l’instantané transmet désormais le diagnostic structuré déjà calculé.

## Tests ajoutés

- Distinction structurée entre texte OCR brut, texte éditable et texte réellement soumis.
- Traces présentes avec toutes leurs frontières, mais absentes des groupes responsables et du verdict.
- Inconnu listé et marqué comme empêchant un verdict VEGAN.
- Ingrédient non végétarien identifié comme cause de `NOT_VEGAN`.
- Composition imbriquée, lien parent/enfant et pourcentage conservés.
- Langue, marqueur et section ingrédients exposés avec leurs bornes.
- Codes d’avertissement pour vide, troncature et parenthèses incohérentes.
- Égalité entre `analyze(...)` et `analyzeWithDiagnostics(...).result`.
- Rapport lisible explicitant raison, inconnus bloquants et exclusion des traces.

## Résultats des validations

Baseline avant modification :

- `./gradlew :mutation-core:test` : succès.
- `./gradlew testDebugUnitTest` : succès.
- `./gradlew assembleDebug` : succès.

Après modification :

- `./gradlew :mutation-core:test` : succès.
- `./gradlew testDebugUnitTest` : succès.
- `./gradlew assembleDebug` : succès.
- `./gradlew connectedDebugAndroidTest` : succès sur Nokia G42 5G, Android 15.

## Changements de comportement évités

- Aucun changement de priorité ou de règle dans `VerdictEngine`.
- Aucun changement du parsing, du matching, des qualifications d’origine ou de la collecte des inconnus.
- Aucun changement de sélection ML Kit, de nettoyage OCR ou d’alias.
- Aucun ajout de dépendance, de LLM ou de logique probabiliste.
- Aucun stockage persistant automatique : les diagnostics et textes restent en mémoire jusqu’à l’export explicitement demandé par l’utilisateur. Aucune image ni donnée binaire n’entre dans les modèles ou exports de diagnostic.

## Limites restantes

- Le recognizer ML Kit reste limité au script latin et ne détecte pas une rotation visuelle sans EXIF exploitable.
- Les raisons d’exclusion distinguent actuellement les sections hors composition comme une seule famille ; une taxonomie nutrition/conservation/marketing demanderait de typer les marqueurs existants.
- Le rapport d’analyse ne duplique volontairement pas le texte OCR brut : le lien complet OCR/analyse se consulte dans `OcrSession` et son export.
- Les offsets décrivent le texte soumis à chaque étape, pas les coordonnées pixel de l’image.

## Recommandations pour l’étape OCR multilingue suivante

1. Conserver `blockId` comme identité stable et `segmentId` comme identité d’offset locale.
2. Ajouter des fixtures multilingues couvrant plusieurs sections de traces et des changements de langue sans titre.
3. Si les marqueurs ignorés doivent être expliqués plus finement, typer les familles dans `LabelSectionExtractor` sans modifier les règles de verdict.
4. Comparer systématiquement texte ML Kit brut, texte nettoyé complet, bloc sélectionné et texte soumis à partir du modèle structuré avant toute évolution de sélection linguistique.
5. Garder les corrections déterministes et documenter chaque nouvelle correction avec une fixture OCR et un test de non-régression.
