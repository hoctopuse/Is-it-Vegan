# Validation PIT ciblée de la correction des sections de traces

Date : 25 septembre 2026

Branche : `mutation-testing-pit`

Commit analysé : `48d2041a53400446a17e805676c11a429a5a877f` (`Merge branch 'master' into mutation-testing-pit`)

## État initial et merge vérifié

Le worktree était propre au début de la validation. `git branch --show-current` a retourné `mutation-testing-pit` et `git log -3 --oneline` a montré :

```text
48d2041 Merge branch 'master' into mutation-testing-pit
44cbc44 fix: preserve adjacent trace sections
a98ab73 docs: audit parser and label section mutations
```

Le merge `48d2041` a pour parents `a98ab73` et `44cbc44`. La commande `git merge-base --is-ancestor 44cbc44 HEAD` confirme que le commit de correction de `master` appartient bien à l'historique analysé.

## Correction et tests vérifiés

La déduplication de `LabelSectionExtractor.extract` utilise désormais :

```kotlin
candidate.start < existing.end
```

Cette comparaison respecte le contrat des intervalles semi-ouverts : le début est inclusif et la fin exclusive. Deux sections dont `first.end == second.start` sont adjacentes et restent distinctes. Une intersection réelle continue à être traitée comme un chevauchement.

Les tests présents et exécutés sont :

- `exactlyAdjacentTraceSectionsArePreservedAndExcludedFromVerdict`, qui exige deux sections adjacentes, vérifie leurs bornes et confirme que leur contenu ne rejoint ni les tokens d'ingrédients ni le verdict ;
- `trulyOverlappingTraceMarkersStillProduceOneTraceSection`, qui exige qu'une vraie superposition reste fusionnée en une section, avec un verdict `VEGAN` et `tracesExcludedFromVerdict == true`.

Validations JUnit :

| Commande | Résultat |
|---|---|
| `.\gradlew :mutation-core:test --tests com.example.isitvegan.LabelSectionExtractorTest` | Succès |
| Exécution forcée des deux méthodes de régression avec `--rerun-tasks` | 2 tests, 0 échec, 0 erreur |
| `.\gradlew :mutation-core:test` | Succès |

Le verdict reste celui de la composition déclarée. Les mentions `lait` et `egg` de l'exemple adjacent restent des informations de contamination croisée et sont toujours exclues du verdict.

## Configuration et ciblage PIT

La configuration conservée du module utilise :

- plugin Gradle PIT `info.solidsoft.pitest` 1.19.0 et moteur PIT 1.22.1 ;
- mutateurs PIT par défaut ;
- `targetTests = com.example.isitvegan.*` ;
- rapports HTML et XML non horodatés ;
- deux threads.

La tâche `:mutation-core:pitest` existe. Son aide expose `--targetTests`, mais aucune option `--targetClasses`. L'exécuter directement aurait donc analysé les dix classes actuellement déclarées dans `build.gradle.kts`.

Pour ne modifier aucun fichier Gradle et ne pas lancer la campagne globale, un script d'initialisation Gradle temporaire, créé hors du dépôt puis supprimé, a remplacé en mémoire `targetClasses` par le seul ensemble suivant :

```text
[com.example.isitvegan.LabelSectionExtractor]
```

La valeur a été affichée et vérifiée avant la campagne. La commande effective était équivalente à :

```powershell
.\gradlew :mutation-core:pitest --init-script <script-temporaire> --rerun-tasks --no-daemon --console=plain --no-configuration-cache
```

PIT a examiné 128 classes de test et exécuté 676 combinaisons test-mutant. La couverture de la seule classe mutée est de 292/308 lignes, soit 95 %. La durée interne PIT est de 11 secondes ; la tâche Gradle complète a pris 39 secondes. L'avertissement informatif sur l'absence du plugin commercial Arcmutate Kotlin est identique aux campagnes précédentes et n'a pas bloqué l'analyse.

## Mutant historique de la borne inclusive

Dans la campagne historique, le code contenait `candidate.start <= existing.end`. Le `ConditionalsBoundaryMutator` de la ligne 129, index bytecode 892 et bloc 177, remplaçait cette borne inclusive par une borne stricte. Ce mutant corrigeait artificiellement le défaut et avait le statut `SURVIVED`.

Après la correction, le code contient la borne stricte. Le mutant de même emplacement effectue l'opération inverse et réintroduit donc le comportement historique :

```text
candidate.start < existing.end
                 devient
candidate.start <= existing.end
```

Résultat actuel : `KILLED`. Le XML désigne explicitement comme test tueur :

```text
com.example.isitvegan.LabelSectionExtractorTest
    .exactlyAdjacentTraceSectionsArePreservedAndExcludedFromVerdict
```

Le mutant de négation au même index est également tué par ce test. Le défaut des sections adjacentes est donc corrigé et protégé par la régression ; il ne constitue plus un défaut actuel.

Le test de vraie superposition passe lors de son exécution forcée et confirme directement qu'une intersection réelle reste fusionnée. PIT ne le cite pas comme premier test tueur dans ce rapport : le XML ne conserve qu'un `killingTest` par mutant et la sélection/minimisation a attribué les mutants concernés à d'autres tests. Cette absence d'attribution nominative ne contredit pas son assertion fonctionnelle exécutée avec succès.

## Comparaison avant et après

Le résultat historique provient du groupe B antérieur à la correction. Les valeurs ci-dessous sont filtrées sur `LabelSectionExtractor` seul, ce qui rend les volumes comparables.

| Mesure | Historique | Après correction | Écart |
|---|---:|---:|---:|
| Mutants générés | 146 | 146 | 0 |
| Mutants tués | 80 | 81 | +1 |
| Survivants | 47 | 46 | -1 |
| Non couverts | 19 | 19 | 0 |
| Couverture de lignes | 292/308 (95 %) | 292/308 (95 %) | 0 |
| Score de mutation exact | 80/146 (54,8 %) | 81/146 (55,5 %) | +0,7 point |
| Score PIT arrondi | 55 % | 55 % | 0 point affiché |
| Résistance des tests hors non-couverts | 80/127 (63,0 %) | 81/127 (63,8 %) | +0,8 point |

La campagne historique du groupe B complet durait 13 secondes dans PIT et 37 secondes dans Gradle, mais elle incluait aussi `LabelLexicon` et `TextNormalizer`. Sa durée ne peut pas être comparée directement aux 11/39 secondes de la présente campagne limitée à une classe.

Ces résultats ne doivent pas être comparés au score global historique de 47 %, qui concernait huit classes nouvelles dédupliquées. Ici, le périmètre contient exactement une classe et la variation attendue d'un seul mutant.

## Survivants du périmètre ciblé

Les 46 survivants actuels sont agrégés ci-dessous sans les reclasser comme défauts :

| Méthode compilée | Nombre | Lignes PIT concernées |
|---|---:|---|
| `contentUntil` | 1 | 284 |
| `depthAt` | 3 | 310, 311, 312 |
| `extract` | 21 | 99, 102, 103, 116, 117, 118, 120, 129, 133, 138, 142, 144, 148, 150, 325, 342, 370 |
| `extract$lambda$16$0` | 1 | 162 |
| `extract$lambda$2$0` | 1 | 93 |
| `findMarkers` | 3 | 263, 416 |
| `findMarkers$lambda$2$0` | 1 | 209 |
| `findMarkers$lambda$3$0` | 1 | 216 |
| `findMarkers$lambda$4$0` | 1 | 222 |
| `findMarkers$lambda$5$0` | 2 | 231, 232 |
| `hasEndOrMarketingMarker` | 5 | 84, 318, 319 |
| `markerPriority` | 1 | 274 |
| `normalizeTraceText` | 5 | 189, 190, 192 |
| **Total** | **46** | |

Le survivant de frontière à l'index 887 reste celui de la première comparaison `existing.start <= candidate.end`, déjà décrit historiquement comme probablement équivalent sous les invariants de tri et de sections non vides. La présente validation ne réinterprète pas ce mutant et ne conclut à aucun autre bug sans exemple métier observable.

## Mutants non couverts du périmètre ciblé

Les 19 mutants non couverts restent inchangés :

| Méthode compilée | Nombre | Lignes PIT concernées |
|---|---:|---|
| `extract` | 3 | 102, 142, 143 |
| `extract$lambda$2$0` | 2 | 93 |
| `extract$lambda$5$0` | 4 | 108, 109 |
| `findMarkers$lambda$6$0` | 3 | 240, 241 |
| `findMarkers$lambda$8` | 2 | 250, 251 |
| `traceEnd` | 2 | 300, 301 |
| `trimTrailingProductTitle` | 3 | 291, 292 |
| **Total** | **19** | |

Aucun de ces résultats n'est nouveau dans cette vérification et aucune modification de test, exclusion PIT ou correction supplémentaire n'est justifiée par le seul statut de mutation.

## Limites et recommandation

- Le ciblage d'une classe repose sur une surcharge Gradle temporaire, car la tâche n'expose pas `targetClasses` en option de ligne de commande. La cible réellement utilisée a toutefois été affichée avant l'exécution et le XML ne contient que `LabelSectionExtractor`.
- Le rapport PIT nomme un seul test tueur par mutant. Il prouve directement l'efficacité du test adjacent pour l'ancien défaut, mais ne fournit pas la liste exhaustive des tests capables de tuer chaque mutant.
- Les anciens rapports restent des observations historiques antérieures au correctif. Ils ne doivent pas être réécrits ; leur mention de ce défaut décrit l'état de l'époque.
- Le score arrondi reste à 55 % malgré un mutant tué supplémentaire. Le décompte exact, le test tueur et le périmètre d'une classe sont les indicateurs pertinents ici.

La suite de l'audit peut retirer le défaut des traces adjacentes de la liste des problèmes actuels. Les autres survivants doivent continuer à être examinés individuellement, avec un comportement métier observable avant toute conclusion. Il n'est pas recommandé de relancer la campagne globale ni d'ajouter des tests uniquement pour modifier un score.

## Intégrité du dépôt

Aucun code de production, test, fichier Gradle, asset, fichier `ingredients.json` ou rapport existant n'a été modifié pendant cette validation. Les sorties PIT sont des artefacts ignorés sous `mutation-core/build`. Le seul fichier créé à la racine est le présent rapport. Aucun commit ni push n'a été effectué.
