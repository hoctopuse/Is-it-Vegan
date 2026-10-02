# Revalidation finale de la phase 4 v0.7

## Décision

`NO_GO_PHASE5`

La logique de l’optimisation est confirmée équivalente au prédicat historique sur la base actuelle, les tests dédiés passent et la mesure indépendante confirme un gain sur la préparation du prédicat. La phase 5 reste toutefois bloquée par l’état Git : le working tree contient encore des modifications de production locales qui ne correspondent pas exactement au commit examiné. La validation ciblée exécutée porte donc sur cet état local enrichi, tandis que le commit `21454d9` a été vérifié par inspection historique.

## État initial

Les vérifications initiales ont identifié :

- branche `master`, à jour avec `origin/master` ;
- commit examiné : `21454d9 perf(v0.7): optimize linked alias matcher preparation` ;
- modifications locales de `PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md` ;
- modifications locales de `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt` ;
- modifications locales de `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt` ;
- rapports non suivis `REVIEW_0_7_PHASE4_POST_IMPLEMENTATION_REVIEW_REPORT.md` et `REVIEW_0_7_PHASE4_SEQUENCE_CORRECTION_REPORT.md`.

Ces modifications n’ont pas été supprimées, réinitialisées ni écrasées. Les modifications de production existaient avant cette revalidation ; les seules modifications effectuées pendant cette mission concernent le test JVM dédié et le présent rapport.

`PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT(1).md` n’existe pas. Le seul rapport de profilage présent sous ce nom est `PRE_0_7_PHASE4_MATCHER_PROFILE_REPORT.md`. `REVIEW_0_7_PHASE4_MATCHER_OPTIMIZATION_REPORT.md` reste invalide comme preuve, car il est antérieur à l’implémentation réelle.

## Prédicat vérifié

Le prédicat historique est :

```text
B.length > A.length
B.startsWith("$A ")
suffix(B après "$A ") correspond à ^(?:de|d|du|des|a|au)(?:\s|$).*
```

Le pré-calcul inspecté parcourt chaque espace de chaque alias long, extrait la base et le suffixe, vérifie que la base appartient à l’ensemble des alias normalisés, puis applique la même regex. Les deux chemins utilisent la même normalisation, le même filtrage des blancs, `distinct()`, les mêmes séparateurs d’espace et les mêmes six connecteurs.

L’ordre des `AliasEntry`, le tri décroissant par longueur, les regex de frontières, les doublons intra-concept, les collisions inter-concepts et les règles contextuelles n’ont pas été modifiés par le diff du commit. Les protections arôme, goût, extrait, lait végétal, E-numbers, allemand et inconnus restent inchangées.

## Tests ajoutés et parité

Le fichier suivant a été ajusté pendant cette revalidation :

`mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt`

Les ajouts couvrent explicitement chacun des six connecteurs : `de`, `d`, `du`, `des`, `a` et `au`. Pour chaque connecteur, le test vérifie un cas positif, une frontière valide, un suffixe invalide et un préfixe ressemblant à un alias mais absent comme alias complet. Les cas négatifs vérifient aussi que l’alias court n’est pas marqué à tort.

Le test vérifie désormais explicitement `2485` alias normalisés pour la base actuelle. Il compare l’ensemble pairwise historique à l’ensemble pré-calculé sans filtrage intermédiaire. Les cas existants couvrent également casse, espaces normalisés, E-numbers, tirets, parenthèses, arôme, goût, extrait, lait végétal, inconnus, collisions et occurrences répétées. Les corpus phase 2/3 couvrent en complément FR/NL/EN/DE, formes allemandes, compositions, parents, enfants, pourcentages, traces et diagnostics.

La comparaison d’ensembles sur le même domaine démontre les valeurs positives et négatives du prédicat pour les 2 485 alias. Le matcher profilé et le matcher sans collecteur restent égaux sur le corpus de résultats. Cette dernière comparaison contrôle l’instrumentation ; elle n’est pas présentée comme une comparaison de deux algorithmes historiques distincts.

## Résultats exécutés

Le test ciblé exécuté est passé :

```text
.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.Phase4MatcherProfileTest --console=plain
BUILD SUCCESSFUL
5 tests
```

La suite JVM complète du module est passée :

```text
.\gradlew.bat :mutation-core:test --console=plain
BUILD SUCCESSFUL
```

`testDebugUnitTest` a exécuté 287 tests et en a échoué 6. Les six erreurs proviennent des importeurs éditoriaux et contiennent toutes `ModuleNotFoundError: No module named 'pypdf'`. Aucun test du matcher n’est en échec. Cette cause est environnementale et hors périmètre ; `pypdf` n’a pas été installé et aucun importeur n’a été modifié.

`git diff --check` passe. Aucun script Python, importeur ou générateur n’a été exécuté directement pendant cette revalidation.

## Mesure indépendante

Le test exécuté a utilisé la même base, les 2 485 alias normalisés, 5 échauffements et 20 mesures séquentielles pour les deux chemins :

| Préparation | Min | Médiane | Max |
|---|---:|---:|---:|
| Prédicat historique pairwise | 109 065 µs | 122 144 µs | 141 340 µs |
| Pré-calcul optimisé | 796 µs | 919 µs | 1 281 µs |

La réduction mesurée est `(122144 - 919) / 122144 = 99,25 %`. Le chiffre antérieur `(111966 - 870) / 111966 = 99,22 %` est mathématiquement cohérent, mais il appartient à une mesure précédente. Ces chiffres concernent uniquement la préparation du prédicat. Ils ne démontrent ni gain global de construction du matcher, ni gain Android, ni gain mémoire, ni amélioration générale de l’application.

Le test rapporte aussi une construction complète actuelle médiane de 38 828 µs, une recherche exacte médiane de 752 µs, une recherche multilingue de 1 441 µs, une analyse simple de 41 330 µs, une analyse imbriquée de 72 571 µs, une analyse multilingue de 56 645 µs et une analyse avec traces de 36 876 µs. Ces valeurs sont des profils JVM de l’état local et ne sont pas utilisées pour établir un ratio avant/après global.

Les limites habituelles restent applicables : JIT, GC, fréquence CPU, cache de fichiers et bruit de `System.nanoTime()`. La médiane et les répétitions réduisent l’effet des valeurs isolées sans constituer un benchmark multi-machine.

## Android

`adb` n’est pas disponible et aucun appareil n’a été accessible. Aucune mesure Android n’a été exécutée ou réutilisée comme preuve. Les résultats JVM ci-dessus ne sont pas des résultats appareil. L’absence Android est donc un avertissement distinct, mais elle s’ajoute à l’état Git non propre pour la décision finale.

## Invariants et périmètre métier

L’inspection du diff confirme l’absence de modification de `VerdictEngine`, `VeganAnalyzer`, `IngredientAnalysisService`, parser, tokenizer, pipeline OCR, sélection multilingue OCR, `KnowledgeIndex`, `IndexedCandidateProvider`, assets, données éditoriales, importeurs et version applicative.

Les statuts `VEGAN`, `VEGETARIAN`, `NON_VEGAN` et `UNCERTAIN`, les inconnus, les traces, les compositions, les parents/enfants, les pourcentages, les occurrences, les diagnostics, les résultats conditionnels et les protections arôme/goût/extrait restent sur le chemin historique. `KnowledgeIndex` et `IndexedCandidateProvider` ne sont pas branchés en production. Aucun asset n’a été régénéré ou modifié.

## Réserves restantes

La parité du prédicat est maintenant démontrée avec une assertion de taille et six connecteurs explicites. La suite `mutation-core` passe. L’échec `pypdf` est isolé aux importeurs et aucun défaut matcher n’est masqué.

La réserve bloquante restante est l’arbre de travail : le code de production local diffère encore du commit examiné. Sans supprimer ces modifications, il n’est pas possible de prétendre avoir exécuté la validation finale sur un checkout exactement égal à `21454d9`. Une validation finale positive devra être faite sur un arbre propre ou sur une copie exacte du commit, sans écraser les changements locaux.

## Conclusion

La phase 4 est fonctionnellement cohérente et son optimisation centrale est confirmée sur la base actuelle. La revalidation apporte les six cas synthétiques manquants et une mesure indépendante reproductible dans l’état local. Elle ne donne pas encore l’autorisation de phase 5, car l’état Git ne permet pas une clôture exacte du commit de référence.

`KnowledgeIndex` reste hors production, aucune migration d’asset n’est autorisée et aucune modification du verdict n’est autorisée.
