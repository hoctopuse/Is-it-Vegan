# Correction de la frontière entre sections de traces

Date : 25 septembre 2026
Branche : `master`
Commit de départ : `3d7c3f4` (`feat: stabilize analysis diagnostics`)

## Cause exacte

`TraceSection.start` est inclusif et `TraceSection.end` exclusif. La déduplication de `LabelSectionExtractor.extract` considérait pourtant qu’une section candidate chevauchait une section existante lorsque `candidate.start <= existing.end`. Pour deux intervalles adjacents, `candidate.start == existing.end` est vrai : la seconde section était donc supprimée.

Les candidats étant triés par position croissante, la correction minimale remplace cette comparaison par `candidate.start < existing.end`. Une intersection réelle reste détectée, tandis qu’une simple frontière commune ne l’est plus.

## Comportement avant et après

Entrée de régression :

```text
Ingrédients : eau. Peut contenir : lait.May contain traces of egg.
```

Avant la correction, le diagnostic ne conservait que `Peut contenir : lait.`. Le test ajouté échouait sur le nombre attendu de sections : une section était obtenue au lieu de deux.

Après la correction, `traceSections` contient les deux sections. La fin exclusive de la première est exactement égale au début inclusif de la seconde. Les textes concernant `lait` et `egg` restent dans le diagnostic de contamination croisée.

Le verdict principal reste `VEGAN`, identique à celui de l’entrée de référence `Ingrédients : eau.`. Ni `lait` ni `egg` ne sont analysés comme ingrédients déclarés, et `tracesExcludedFromVerdict` reste vrai.

## Tests ajoutés

- `exactlyAdjacentTraceSectionsArePreservedAndExcludedFromVerdict` vérifie les deux sections adjacentes, l’égalité `first.end == second.start`, leur contenu, le verdict inchangé et l’exclusion des traces du verdict et des tokens d’ingrédients.
- `trulyOverlappingTraceMarkersStillProduceOneTraceSection` vérifie qu’une vraie superposition de marqueurs de traces reste fusionnée en une seule section et n’ajoute aucun bloqueur animal.

Le test ciblé a d’abord été exécuté avant la correction : 9 tests exécutés, 1 échec sur le nouveau cas adjacent. Après la correction, toute la classe de test passe.

## Validations

| Commande | Résultat |
|---|---|
| `.\gradlew :mutation-core:test --tests com.example.isitvegan.LabelSectionExtractorTest` | Échec attendu avant correction, puis succès après correction |
| `.\gradlew :mutation-core:test` | Succès |
| `.\gradlew testDebugUnitTest` | Succès |
| `.\gradlew assembleDebug` | Succès |
| `git diff --check` | Succès |

La campagne PIT globale n’a pas été relancée, conformément à la demande.

## Fichiers modifiés

- `mutation-core/src/main/kotlin/com/example/isitvegan/LabelSectionExtractor.kt`
- `mutation-core/src/test/kotlin/com/example/isitvegan/LabelSectionExtractorTest.kt`
- `DIAGNOSTIC_TRACE_SECTION_BOUNDARY_FIX_REPORT.md`

Aucun alias, asset, fichier `ingredients.json`, règle de verdict, composant OCR, composant de segmentation linguistique ou fichier Gradle n’a été modifié. Aucun commit ni push n’a été effectué.
