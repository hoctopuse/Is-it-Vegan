# Rapport d'amélioration des tests PIT

Date : 2026-09-25  
Branche : `mutation-testing-pit`

## Objectif et périmètre

Cette itération ajoute uniquement des tests JVM dans `:mutation-core`. Aucun
code de production, asset, fichier Gradle, rapport existant ou fichier de
`:app` n'a été modifié. Le travail s'est limité aux priorités P0 et aux P1
portant une règle métier observable.

## Tests ajoutés

### `VerdictEngineMutationTest`

Quatre tests unitaires directs ont été ajoutés :

| Test | Mutants principalement visés | Justification métier |
|---|---|---|
| `veganAssessmentClassifiesEveryStatusWithoutAnotherCauseMaskingIt` | S20–S25, S30, S32 | Chaque statut doit déterminer seul le bon verdict ; un inconnu ne doit pas produire VEGAN. |
| `veganAssessmentFindsDecisiveStatusesAtEitherEndOfTheList` | S20, S22–S25, S31, S33, S34 | Le verdict ne dépend pas de la position d'un bloqueur ou d'un ingrédient incertain. |
| `detailedVerdictKeepsPrecedenceAndExcludeUncertainSemantics` | S35–S37 | NON_VEGAN garde la priorité ; `excludeUncertain` retire seulement l'incertitude et conserve les bloqueurs certains. |
| `vegetarianCompatibilityFiltersOnlyUncertainAndIsOrderIndependent` | S26–S29, S38–S41 | Le verdict végétarien ignore UNCERTAIN, mais jamais un NON_VEGAN, quel que soit l'ordre. |

Après ce groupe, `:mutation-core:test --rerun-tasks` a réussi. PIT est passé de
106 à 122 mutants tués et de 68 % à 78 %. Les mutations métier directes de
`VerdictEngine` ont été tuées ; six conditions de boucle générées par les
fonctions Kotlin inline subsistaient malgré la matrice de comportements.

### `IngredientMatcherMutationTest`

Cinq tests ont été ajoutés :

| Test | Mutants principalement visés | Justification métier |
|---|---|---|
| `unknownExpressionRemainsUnmatchedWithOrWithoutUnrelatedDatabaseEntries` | S04 et branche vide associée | Un texte réellement inconnu doit rester `NONE` et être remonté comme inconnu, indépendamment d'entrées sans rapport. |
| `derivedIngredientAcceptsOnlyReviewedSuffixes` | S11 | Une expression dérivée reconnue peut absorber un suffixe autorisé, mais pas masquer « sucrée » ou un autre qualificatif sémantique. |
| `glueWordsDoNotHideARealUnknownRemainder` | S17–S19 | Les mots de liaison seuls sont acceptables ; un mot inconnu restant doit empêcher une couverture complète. |
| `englishCocoaButterProtectsButterOnlyWhenThePhraseIsContiguous` | N02, N03 et chemin source→animal | « cocoa butter » est une désignation végétale réelle ; un mot intercalé ne doit pas neutraliser un ingrédient animal. |
| `protectedPhraseAllowsPunctuationButRejectsSemanticRemainders` | S13–S16, N05, N06, N09, N10 | La ponctuation autour de « beurre de cacao » est neutre, tandis qu'un mot ou un ingrédient supplémentaire doit rester observable. |

Après ce groupe, `:mutation-core:test --rerun-tasks` a réussi. La campagne PIT
finale a tué 9 mutants supplémentaires par rapport au groupe P0.

## Résultats PIT avant et après

| Mesure | Avant | Après | Évolution |
|---|---:|---:|---:|
| Mutants générés | 157 | 157 | 0 |
| Tués | 106 | 131 | **+25** |
| Survivants | 41 | 23 | **-18** |
| Non couverts | 10 | 3 | **-7** |
| Score de mutation | 68 % | **83 %** | **+15 points** |
| Test strength | 72 % | 85 % | +13 points |
| Couverture de lignes | 287/291 (99 %) | 289/291 (99 %) | +2 lignes |
| Tests examinés par PIT | 116 | 118 | +2 classes de test |
| Durée PIT interne | 8–10 s | 10 s | stable |
| Durée Gradle finale | 20–23 s | 22 s | stable |

Les quatre tests P0 ont tué 16 mutants. Les cinq tests P1 en ont tué 9 de plus.
Aucune mutation n'est restée dans les sources ; PIT a travaillé dans ses
processus et rapports de build.

## Mutants restants

La campagne finale laisse **23 survivants** :

- 17 dans `IngredientMatcher` : construction/déduplication d'alias, conditions
  inline de sélection, parcours des résidus, frontières d'adjacence et cinq
  suppressions de gardes non-null Kotlin ;
- 6 dans `VerdictEngine` : conditions de contrôle synthétiques issues de
  `any`/`filter`, alors que les sorties métier sont maintenant couvertes pour
  les listes vides, singletons, permutations et priorités.

Il reste **3 mutants non couverts** :

- `IngredientMatcher.isCovered`, ligne 163/index 7 : retour forcé à `true`
  lorsque `selected` est vide, branche probablement inaccessible depuis
  `match` ;
- `IngredientMatcher.match`, ligne synthétique 268/index 497 : condition de
  collection Kotlin non atteinte ;
- `IngredientMatcher.protectedPair`, ligne 131/index 57 : retour final forcé à
  `true` pour un ordre/chevauchement de candidats probablement impossible avec
  les frontières regex.

## Équivalents probables

Les candidats les plus probables sont les suppressions de
`Intrinsics.checkNotNullExpressionValue` sur `substring`, `trim` et `toList`,
la frontière `length > alias.length` rendue redondante par
`startsWith("$alias ")`, et les frontières d'adjacence rendues inatteignables
par les regex de mots. Les six survivants synthétiques de `VerdictEngine` sont
également de forts candidats : la nouvelle matrice vérifie toutes les sorties
métier que ces boucles calculent.

Cette itération n'ajoute aucune exclusion PIT. L'équivalence reste à confirmer
par inspection mutation par mutation avant toute décision de configuration.

## Recommandations pour la prochaine itération

1. Examiner individuellement les survivants réels du matcher aux lignes
   synthétiques 260, 282, 284, 301, 306, 315 et 318 afin de relier chaque index
   bytecode à une branche observable.
2. Ajouter seulement les cas métier manquants trouvés lors de cette inspection,
   notamment plusieurs occurrences et candidats superposés dans un même token.
3. Confirmer l'inaccessibilité des trois mutants non couverts depuis l'API
   publique avant de les classer équivalents.
4. Ne pas ajouter de tests pour les gardes non-null ou les boucles Kotlin si
   aucun comportement utilisateur distinct ne peut être formulé.
5. Garder une future campagne parsing/quantités/langues séparée : ces classes ne
   font pas partie du périmètre PIT actuel.

## Validation

- `./gradlew :mutation-core:test --rerun-tasks` : succès après chaque groupe ;
- `./gradlew :mutation-core:pitest --rerun-tasks --no-daemon --console=plain` :
  succès après chaque groupe ;
- campagne finale : 157 générés, 131 tués, 23 survivants, 3 non couverts ;
- aucun commit ni push effectué.
