# Rapport v0.7 — phase 3 : fournisseur de candidats indexé

## État initial

Le dépôt était sur `master`, à jour avec `origin/master`, au commit `264e216 docs: close 0.6.13.7 documentation cycle`. Les modifications non commités des phases précédentes ont été conservées : `MultilingualIngredientLexicon.kt`, `KnowledgeIndex.kt`, les tests de phases 1–2 et leurs rapports. Aucun fichier éditorial, asset ou fichier de version n’a été modifié.

## Fichiers créés

- `mutation-core/src/main/kotlin/com/example/isitvegan/IndexedCandidateProvider.kt` — façade expérimentale de recherche ;
- `mutation-core/src/test/kotlin/com/example/isitvegan/IndexedCandidateProviderTest.kt` — tests de fournisseur, comparateur et benchmark JVM ;
- `app/src/androidTest/java/com/example/isitvegan/Phase3IndexedCandidateInstrumentedTest.kt` — comparaison et benchmark Android ;
- `PRE_0_7_PHASE3_INDEXED_CANDIDATE_EXPERIMENT_REPORT.md` — présent rapport.

## Architecture de l’adaptateur

`IndexedCandidateProvider` encapsule `KnowledgeIndex` et expose quatre requêtes : alias exact, alias multilingue exact, numéro E et candidats lexicaux contenus dans un texte.

Chaque résultat conserve :

- l’identifiant canonique ;
- le `RuntimeConcept` lorsqu’il existe ;
- la `RuntimeForm` utilisée ;
- la langue, la forme normalisée, la relation, la provenance et la confiance ;
- le statut du concept disponible à titre informatif ;
- les collisions ;
- les cibles canoniques invalides.

Le fournisseur ne calcule aucun verdict et ne choisit pas entre des candidats. Il ne décide ni `EXACT`, ni `PARTIAL_CONTEXTUAL`, ni `BLOCKED_CONFLICT`.

Le comparateur de tests interroge le fournisseur puis appelle le `IngredientMatcher` inchangé sur le même texte. Il vérifie que les concepts sélectionnés et bloqués par le matcher sont présents dans les candidats indexés, puis compare les statuts et les diagnostics de l’analyse de référence.

## Responsabilités conservées dans le matcher

`IngredientMatcher` reste responsable des alias longs, priorités, frontières lexicales, protections arôme/goût/extrait, conflits, contextes multilingues et résolutions. `IngredientAnalysisService` reste responsable du parsing, des règles d’origine, des inconnus, des diagnostics, des traces et du verdict via les modèles existants.

Le chemin expérimental n’injecte pas une liste filtrée dans le matcher. Il s’agit volontairement d’un probe de comparaison ; il ne peut donc pas modifier le résultat de production.

## Corpus et parité

Le corpus réutilise les cas de la phase 2 et ajoute les alias colza FR/EN/NL/DE, `E471`, `E 471`, `471`, classes fonctionnelles, contextes protégés, `E470b`/`E572`, formes allemandes composées, compositions à trois niveaux, pourcentages, répétitions et traces adjacentes.

Les tests couvrent notamment :

- parents, enfants, profondeurs, `parentOrder`, ordre et pourcentages ;
- occurrences répétées ;
- inconnus et concepts `UNCERTAIN` ;
- traces contenant des alias connus, toujours exclues du verdict ;
- statuts et identifiants sélectionnés ;
- candidats bloqués ;
- chemins de diagnostic lorsque l’occurrence incertaine est représentée.

La parité vérifiée est une parité de disponibilité des candidats et de sélection par le matcher de référence. Elle n’est pas présentée comme une équivalence autonome de l’index.

## Différences constatées et limites

Les candidats lexicaux peuvent être plus larges que la sélection métier. C’est attendu pour `arôme chocolat`, `extrait de café`, `goût fraise` et `lait végétal` : le fournisseur expose des surfaces possibles, puis le matcher applique ses protections.

Les chaînes `raapzaadolie` et `Schokoladenaroma` ne sont pas des mappings exacts présents dans la base actuelle. Elles restent non reconnues dans les tests ; aucune alias éditoriale n’a été ajoutée pour les faire passer. Les formes présentes `Rapsöl` et `Schokoladenkuvertüre` sont vérifiées séparément.

`E470b` et `E572` restent deux concepts séparés avec le statut prudent `UNCERTAIN`. Les alias directs répétés restent conservés. `cereals` reste une cible sans concept canonique et est signalé comme invalide.

Le chemin « candidats indexés réellement filtrés puis matcher » n’est pas implémenté, car cela nécessiterait de modifier ou d’abstraire `IngredientMatcher`. Le probe appelle donc toujours le matcher complet de référence. Cette limite est volontaire et empêche toute modification implicite du comportement métier.

## Résultats JVM

Les mesures utilisent 5 échauffements et 20 répétitions séquentielles. L’index contient 479 concepts, 7 208 formes, 5 clés en collision et 1 cible canonique invalide.

| Opération | Min | Médiane | Max |
|---|---:|---:|---:|
| Alias exact | 15 µs | 15 µs | 90 µs |
| Alias multilingue | 11 µs | 11 µs | 21 µs |
| Numéro E | 16 µs | 18 µs | 29 µs |
| Récupération de candidats contenus | 5 667 µs | 9 959 µs | 16 928 µs |
| Candidats puis matcher | 6 654 µs | 7 276 µs | 9 973 µs |
| Analyse actuelle | 163 540 µs | 193 928 µs | 210 541 µs |
| Probe expérimental | 180 930 µs | 207 913 µs | 235 318 µs |
| Génération de diagnostics | 162 227 µs | 182 664 µs | 207 424 µs |

Le probe expérimental est plus lent que l’analyse actuelle, car il ajoute la récupération des candidats sans retirer le coût du matcher. Le gain de construction de l’index observé en phase 2 ne suffit donc pas à démontrer un gain de bout en bout.

## Résultats Android

Le test instrumenté a réussi sur Nokia G42 5G, Android 15, avec 5 échauffements et 10 répétitions.

| Opération | Min | Médiane | Max |
|---|---:|---:|---:|
| Alias exact | 364 µs | 371 µs | 434 µs |
| Alias multilingue | 386 µs | 403 µs | 509 µs |
| Numéro E | 319 µs | 328 µs | 422 µs |
| Récupération de candidats contenus | 182 664 µs | 183 388 µs | 186 572 µs |
| Candidats puis matcher | 212 232 µs | 213 581 µs | 216 807 µs |
| Analyse actuelle | 6 128 356 µs | 6 152 711 µs | 6 218 240 µs |
| Probe expérimental | 6 848 715 µs | 6 924 677 µs | 6 982 454 µs |
| Diagnostics | 6 161 816 µs | 6 208 920 µs | 6 271 652 µs |

Le PSS final observé était d’environ 138 911 KiB. Il s’agit d’une mesure globale du processus Android, sensible au GC et aux autres allocations ; aucune taille mémoire attribuée précisément à l’index n’est revendiquée.

## Vérifications exécutées

- `.\gradlew.bat :mutation-core:test` — succès ;
- `.\gradlew.bat testDebugUnitTest` — succès ;
- `.\gradlew.bat assembleDebug` — succès ;
- `.\gradlew.bat assembleDebugAndroidTest` — succès ;
- test instrumenté ciblé `Phase3IndexedCandidateInstrumentedTest` — succès ;
- `git diff --check` — succès.

Aucun importeur, générateur Python, build documentaire, PIT, commit ou push n’a été exécuté.

## Impact sur le verdict

Le verdict, les statuts, les inconnus, les incertains, les occurrences et les traces restent ceux du chemin actuel. Le fournisseur ne peut pas produire de verdict seul. Les tests échouent si une sélection, un statut, une hiérarchie, un pourcentage, une occurrence ou une exclusion de trace diverge du chemin de référence.

## Décision recommandée

`GO_FOR_EXPERIMENTAL_USE_ONLY`

L’adaptateur est suffisamment explicite et testable pour servir dans des comparaisons et benchmarks isolés. La parité des candidats avec le matcher de référence est démontrée sur le corpus ciblé, mais le probe complet est plus lent et ne constitue pas une nouvelle voie de sélection métier. Aucune intégration de production limitée n’est donc autorisée à ce stade.
