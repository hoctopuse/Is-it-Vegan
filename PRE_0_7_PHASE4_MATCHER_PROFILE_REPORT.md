# Pré-0.7 — phase 4 : profilage du matcher

## État initial

Branche de référence : `master`, version applicative inchangée `0.6.13.7` / code `59`.
L’arbre était déjà volontairement non propre à l’ouverture : les fichiers des phases 1 à 3, dont `KnowledgeIndex`, `IndexedCandidateProvider`, leurs tests et rapports, étaient non commités ; la modification préexistante de `MultilingualIngredientLexicon.kt` a été conservée. Aucun commit, push, reset, générateur Python, importeur, donnée éditoriale ou asset n’a été modifié.

Les références lues sont les rapports de préparation et phases 1–3, le matcher, le service d’analyse et les tests/benchmarks existants.

## Fichiers de cette phase

- Modifié : `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt`.
- Créés : `mutation-core/src/test/kotlin/com/example/isitvegan/Phase4MatcherProfileTest.kt`, `app/src/androidTest/java/com/example/isitvegan/Phase4MatcherProfileInstrumentedTest.kt` et ce rapport.

Les autres fichiers visibles dans `git status` étaient déjà présents avant cette phase.

## Méthode

`MatcherProfileCollector` est une instrumentation optionnelle, inactive en production par défaut et sans journal applicatif. Elle mesure préparation des alias, compilation de regex, recherche, filtrage contextuel, sélection, ainsi que le nombre de regex, candidats, blocages et sélections. Les mesures JVM utilisent 5 échauffements et 20 répétitions ; Android utilise 5 échauffements et 10 répétitions, séquentiellement.

Les étapes de lecture/décodage sont mesurées séparément par asset et `MiniJson`. Les étapes de prétraitement, parsing, lexique, règles d’origine, matching, diagnostics et verdict sont également mesurées isolément. Les données Android ont été recueillies sur Nokia G42 5G, Android 15.

## Étape dominante et optimisation retenue

Le profil JVM avant changement attribuait 459 ms des 534 ms de construction instrumentée à `hasLongerLinkedAlias` : pour chaque forme, le constructeur reparcourait toutes les formes normalisées. La compilation des regex représentait 18 ms.

Cette recherche quadratique a été remplacée par un ensemble immuable de préfixes d’alias liés, construit une fois en parcourant les séparateurs. Le prédicat historique est conservé exactement : un préfixe n’est retenu que s’il est lui-même un alias et que son suffixe satisfait la même expression `de/d/du/des/a/au`. Le test compare les deux définitions sur les 2 485 alias normalisés actuels.

Cette optimisation est localisée dans `IngredientMatcher`, réversible, et ne modifie ni l’ordre des alias, ni les regex, ni le filtrage, ni le choix des candidats. `KnowledgeIndex` et `IndexedCandidateProvider` ne sont pas appelés par le chemin de production.

Les pistes non retenues sont le branchement de l’index mémoire, un cache de résultats, une modification des protections contextuelles et une réécriture des regex : les mesures ne les justifient pas dans cette phase et elles auraient un périmètre de parité plus large.

## Résultats JVM

Après optimisation : 2 490 entrées d’alias, 2 485 alias normalisés. La construction médiane passe de **163,177 ms** avant à **16,373 ms** après. Les vérifications liées passent de 459,340 ms à 0,665 ms dans l’échantillon instrumenté ; les regex restent environ 20,352 ms.

Médianes après optimisation : matching exact 229 µs ; contexte protégé 562 µs ; forme allemande 361 µs ; texte long 8,043 ms ; inconnu 2,061 ms ; parsing imbriqué 16,157 ms ; analyse simple 24,902 ms ; analyse imbriquée 34,146 ms ; analyse multilingue 29,930 ms ; traces 24,954 ms ; texte long 80,221 ms ; diagnostics 16,385 ms.

La recherche contenue demeure le coût dominant du matcher par appel (329,948 ms cumulés pour 171 appels et 425 790 regex testées). Le filtrage arôme/goût/extrait et les frontières sont inclus dans la recherche/filtrage : 1,825 ms et 9,815 ms cumulés respectivement, donc ils ne justifient pas une optimisation supplémentaire dans cette phase.

## Résultats Android

Les mesures Android sont des médianes en µs : lecture ingrédients 3 989, mappings 14 880, règles 426 ; parsing JSON ingrédients 21 235, mappings 95 036, règles 1 341 ; désérialisation complète `IngredientKnowledge` 1 019 337 ; construction matcher 1 112 065.

Matching : exact 27 186, multilingue 15 803, E-number 15 614, texte contenant plusieurs alias 24 393, contexte protégé 16 264, inconnu 17 800. Parsing de composition imbriquée : **1 708 594** ; résolution lexicale 76 281 ; règle d’origine 155 743.

Analyse complète : simple 1 996 621 ; imbriquée 3 321 405 ; multilingue 2 687 894 ; traces 2 176 752 ; texte long 8 584 631 ; inconnu 1 626 271 ; diagnostics 1 196 769 ; verdict mesuré avec analyse préalable 1 982 938. Le parsing de composition est donc l’étape dominante des cas structurés après la réduction du coût de construction. Le chemin indexé de phase 3 n’est pas utilisé ni évalué ici.

La PSS est passée de 123 347 KB à 138 484 KB ; elle est globale au processus et dépend du GC, donc uniquement indicative.

Le benchmark phase 3 sur le même appareil donnait environ 3,55 s de construction du matcher et 6,15 s d’analyse diagnostique pour son libellé de référence. Les corpus complets ne sont pas identiques : ces chiffres confirment la baisse du constructeur, mais ne constituent pas une comparaison d’analyse totale à total strictement identique.

## Parité et non-régression

Le nouveau test compare chaque résultat du matcher instrumenté au matcher non instrumenté sur le corpus : E471/E 471/471, contextes arôme/extrait/goût/lait végétal, allemand, collisions E470b/E572, inconnus, répétitions et profondeur. La propriété de pré-calcul est comparée à la définition historique sur tous les alias. La suite `:mutation-core:test` inclut les corpus de parité des phases 2 et 3 : concepts, statuts, diagnostics, parents/enfants, pourcentages, ordre, chemins, occurrences, traces et verdicts.

Aucune différence fonctionnelle n’a été détectée. Les verdicts, statuts, inconnus, `UNCERTAIN`, traces, diagnostics, règles d’arôme/goût/extrait et moteur de verdict ne sont pas modifiés.

## Limites et suite

Les chronométrages sont sensibles au JIT, au GC, à la fréquence CPU et à la PSS globale. L’instrumentation explique des étapes mais ne remplace pas un profileur système. Les tests Android sont volontairement coûteux : chaque point applique le protocole complet.

Phase 5 devrait cibler séparément `IngredientTreeParser` et la désérialisation `IngredientKnowledge`, avec le même contrat de parité ; aucune intégration de `KnowledgeIndex`, migration d’assets ou changement de règles n’est recommandée.

## Conclusion

`OPTIMIZATION_READY_FOR_REVIEW`
