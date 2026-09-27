# Audit analytique PIT pré-0.7

## Décision

**NO-GO pour déclarer la baseline PIT prête comme seuil de qualité 0.7.** La campagne est techniquement valide, mais 277 mutations de comportement restent classées `TEST_GAP`, 63 autres sont `NO_COVERAGE_TEST_GAP`, et un mutant a expiré. Cette décision ne signale aucun défaut de production démontré.

L'audit ne conclut à aucun `PRODUCTION_DEFECT_DEMONSTRATED`. Les règles métier vérifiées dans le code et la documentation restent cohérentes : verdict principal prioritaire, traces exclues, inconnus et incertains visibles, et qualifications d'origine sans conversion d'un inconnu en vegan.

## Méthode et limites

Les 452 lignes de [MUTATION_TESTING_PRE_0_7_AUDIT_DETAILS.csv](MUTATION_TESTING_PRE_0_7_AUDIT_DETAILS.csv) reprennent chaque ligne de baseline sans déduplication. Les catégories sont fondées sur le statut PIT, les métadonnées de mutation, les sources métier et les tests existants. Un survivant ne démontre pas à lui seul une régression de production ; aucun mutant n'a été rejoué dans cet audit.

Les mutations Kotlin synthétiques, null-safety et protections de collections sont séparées en `EQUIVALENT_OR_KOTLIN_BYTECODE`. Cette étiquette indique une forte probabilité technique, sans prétendre démontrer l'équivalence sémantique.

## Synthèse chiffrée

| Groupe | Test gap | Non couvert | Bytecode Kotlin probable | Investigation | Défaut démontré |
|---|---:|---:|---:|---:|---:|
| Parser/prétraitement | 76 | 47 | 27 | 1 | 0 |
| Matching/origine | 29 | 7 | 10 | 0 | 0 |
| Segmentation/sections | 166 | 9 | 74 | 0 | 0 |
| Diagnostics/verdict | 6 | 0 | 0 | 0 | 0 |
| **Total** | **277** | **63** | **111** | **1** | **0** |

Les 122 mutants `NO_COVERAGE` de la baseline se répartissent entre 63 lacunes d'exécution utiles et 59 entrées de bytecode Kotlin probable. Le seul `TIMED_OUT` est `IngredientTreeParser.parseList`, ligne 106, `MathMutator`.

## Défauts démontrés

Aucun. La mission interdit d'ajouter ou d'exécuter un scénario mutant, et les sources/tests examinés ne prouvent pas qu'un comportement actuel viole une règle métier.

## Actions réellement recommandées

- Traiter le timeout `parseList` comme investigation isolée avant d'en faire un signal de qualité.
- Revoir les `TEST_GAP` en priorité dans `LabelSectionExtractor`, `LabelLanguageSegmenter` et `OcrBlockLanguageClassifier`, puis `IngredientTreeParser`.
- Ajouter ultérieurement des tests d'exécution pour les 63 `NO_COVERAGE_TEST_GAP` si ces chemins sont dans le périmètre fonctionnel 0.7.
- Examiner les 111 entrées Kotlin/bytecode avant de les intégrer à un éventuel seuil PIT.

Ces actions concernent le renforcement ou la qualification des tests. Elles ne recommandent aucune correction de production à ce stade.

## Zones à clarifier

- La valeur attendue des chemins de parsing/segmentation non couverts avant de décider s'ils font partie du contrat 0.7.
- La politique d'acceptation des mutants Kotlin synthétiques dans le score PIT.
- La raison du timeout de `parseList`, sans l'assimiler à un survivant ni à un défaut de production.

## Contrôles

- Baseline : 452 lignes ; audit : 452 lignes ; chaque signature baseline est présente une fois dans l'audit.
- Catégories : 277 `TEST_GAP`, 63 `NO_COVERAGE_TEST_GAP`, 111 `EQUIVALENT_OR_KOTLIN_BYTECODE`, 1 `NEEDS_DEEPER_INVESTIGATION`.
- Aucun PIT, test ou build relancé ; aucun changement de production appliqué.
