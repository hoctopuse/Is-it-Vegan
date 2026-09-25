# Rapport PIT — parser et sections d’étiquette

Date : 25 septembre 2026  
Branche : `mutation-testing-pit`  
Commit analysé : `f9ab6a0286c20b2ae65e57b4d7121426279ca89f` (`f9ab6a0 Merge branch 'master' into mutation-testing-pit`)

## État initial et baseline

L’arbre de travail était propre au début de la campagne. Les commandes demandées ont toutes réussi avant la modification de la configuration PIT :

- `./gradlew :mutation-core:test` : succès en 4 s ;
- `./gradlew testDebugUnitTest` : succès en 8 s ;
- `./gradlew assembleDebug` : succès en 3 s.

La tâche `:mutation-core:pitest` était disponible dans `:mutation-core:tasks --all`. Les tests existants associés au parser et aux sections ont aussi été exécutés explicitement avec `--rerun-tasks` : `IngredientTreeParserTest`, `LabelSectionExtractorTest`, `ParserModulesTest`, `HierarchyAnalysisTest`, `Version059CompletionTest` et `DiagnosticStabilizationTest`. Cette exécution ciblée a réussi en 8 s. Aucun test n’a été ajouté ou modifié.

## Environnement et configuration PIT

- Gradle : 9.7.1 ;
- lanceur Gradle : Oracle JDK 27 (`27+35-2325`) ;
- daemon Gradle : Eclipse Temurin JDK 25 (`25.0.3+9-LTS`) ;
- compilation Kotlin, commande PIT et minions : Eclipse Temurin JDK 17 (`17.0.20.1+1`) via `jvmToolchain(17)` ;
- plugin Gradle PIT : `info.solidsoft.pitest` 1.19.0 ;
- moteur PIT indiqué dans les rapports : 1.22.1 ;
- mutateurs : jeu par défaut PIT ;
- tests ciblés par PIT : `com.example.isitvegan.*` ;
- sorties : HTML et XML, répertoire non horodaté ;
- deux threads ;
- aucun seuil, filtre métier ou exclusion supplémentaire.

La seule modification de configuration conservée étend `targetClasses`. Les cibles historiques `VerdictEngine` et `IngredientMatcher` restent présentes. Les nouvelles cibles sont :

- `IngredientTreeParser`, `FunctionalClassLexicon`, `OriginQualifierRuleSet`, `TextNormalizer` ;
- `LabelSectionExtractor`, `LabelLexicon` ;
- `LabelLanguageSegmenter`, `OcrBlockLanguageClassifier`.

Les dépendances ont été limitées aux composants JVM directement appelés par les trois classes prioritaires et contenant une logique observable. `IngredientTokenizer`, consommateur du parser, n’a pas été ajouté. Aucun fichier de production, test, asset ou du module `:app` n’a été modifié.

## Méthode de campagne

Trois campagnes distinctes ont été exécutées avec `./gradlew :mutation-core:pitest --rerun-tasks --no-daemon --console=plain`. PIT a examiné les 128 tests du package configuré à chaque campagne. Les rapports de travail ont été copiés après chaque groupe dans `mutation-core/build/reports/pitest-groups/group-a`, `group-b` et `group-c`. Ces fichiers sont des artefacts sous `build/` et ne font pas partie des changements Git.

Les groupes B et C se chevauchent volontairement : le segmenter appelle le lexique et `LabelSectionExtractor.hasEndOrMarketingMarker`. Les totaux des groupes ne doivent donc pas être additionnés sans dédupliquer ces classes.

## Résultats par groupe

| Groupe | Classes réellement mutées | Lignes | Mutants | Tués | Survivants | Non couverts | Autres | Score PIT | Résistance des tests | Durée PIT / Gradle |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| A | `IngredientTreeParser`, `FunctionalClassLexicon`, `OriginQualifierRuleSet`, `TextNormalizer` | 561/666 (84 %) | 400 | 216 | 112 | 71 | 1 timeout | 217/400 (54 %) | 217/329 (66 %) | 31 s / 56 s |
| B | `LabelSectionExtractor`, `LabelLexicon`, `TextNormalizer` | 391/418 (94 %) | 208 | 97 | 60 | 51 | 0 | 97/208 (47 %) | 97/157 (62 %) | 13 s / 37 s |
| C | `LabelLanguageSegmenter`, `OcrBlockLanguageClassifier`, `LabelLexicon`, `LabelSectionExtractor` | 666/705 (94 %) | 376 | 145 | 175 | 56 | 0 | 145/376 (39 %) | 145/320 (45 %) | 25 s / 49 s |

Pour le groupe A, PIT compte le timeout comme mutation détectée dans son score, d’où 217 mutations détectées alors que le statut XML contient 216 `KILLED` et 1 `TIMED_OUT`.

### Détail par classe

| Classe | Lignes | Mutants | Tués/détectés | Survivants | Non couverts | Score PIT |
|---|---:|---:|---:|---:|---:|---:|
| `IngredientTreeParser` | 352/382 (92 %) | 321 | 188, dont 1 timeout | 86 | 47 | 59 % |
| `FunctionalClassLexicon` | 113/135 (84 %) | 17 | 2 | 3 | 12 | 12 % |
| `OriginQualifierRuleSet` | 88/141 (62 %) | 59 | 26 | 21 | 12 | 44 % |
| `TextNormalizer` | 8/8 (100 %) | 3 | 1 | 2 | 0 | 33 % |
| `LabelSectionExtractor` | 292/308 (95 %) | 146 | 80 | 47 | 19 | 55 % |
| `LabelLexicon` | 91/102 (89 %) | 59 | 16 | 11 | 32 | 27 % |
| `LabelLanguageSegmenter` | 204/214 (95 %) | 119 | 37 | 78 | 4 | 31 % |
| `OcrBlockLanguageClassifier` | 79/81 (98 %) | 52 | 12 | 39 | 1 | 23 % |

`TextNormalizer`, `LabelLexicon` et `LabelSectionExtractor` produisent les mêmes résultats dans les groupes où ils sont répétés. Après déduplication des classes nouvelles, le périmètre ajouté représente 779 mutants : 362 tués, 289 survivants, 127 non couverts et 1 timeout, soit 363/779 mutations détectées (47 % arrondi). Ce calcul est une synthèse des campagnes par groupes, pas le résultat d’une quatrième campagne globale.

## Mutants survivants

La liste individuelle exhaustive reste dans les fichiers `mutations.xml`. Les concentrations suivantes donnent les zones utiles pour l’audit sans assimiler un survivant à un défaut :

- groupe A : 86 survivants dans `IngredientTreeParser`, principalement `functionalClassWithContinuations` (12), `isCompositionParenthesis` (10), `sectionParts` (10), `splitAtCurrentDepthWithSeparators` (10), `additiveNodes` (9) et `isSentenceBoundary` (9). `OriginQualifierRuleSet` en compte 21, `FunctionalClassLexicon` 3 et `TextNormalizer` 2 ;
- groupe B : 47 survivants dans `LabelSectionExtractor`, dont 22 dans `extract`, 5 dans `hasEndOrMarketingMarker`, 5 dans `normalizeTraceText`, 3 dans `findMarkers` et 3 dans `depthAt`. `LabelLexicon` en compte 11 et `TextNormalizer` 2 ;
- groupe C : 78 survivants dans `LabelLanguageSegmenter`, dont 51 dans `scoreBlock` et 18 dans `segment`. `OcrBlockLanguageClassifier` en compte 39, dont 31 dans `classify` et 6 dans `tokenize`. Les 58 survivants des deux composants partagés sont identiques à ceux du groupe B.

Les mutations survivantes comprennent des négations de conditions et des changements de bornes ou d’arithmétique, mais aussi des suppressions de contrôles non-null générés par Kotlin et des branches de lambdas/collections susceptibles d’être équivalentes. Aucune équivalence n’a été déclarée sans démonstration.

## Mutants non couverts

- groupe A : 71, concentrés dans `IngredientTreeParser.parseSegment` (15), `FunctionalClassLexicon.matchPrefix` (10), `IngredientTreeParser.replaceTopLevelAlternatives` (10), `parseFunctionalClass` (6), ainsi que plusieurs chemins de qualification d’origine ;
- groupe B : 51, dont 32 dans `LabelLexicon`, surtout les variantes compilées de `findIngredientHeadings`, et 19 dans `LabelSectionExtractor`, notamment les fins de traces, le titre produit final et certaines sections ignorées ;
- groupe C : 56, soit les 51 précédents plus 4 dans `LabelLanguageSegmenter` et 1 dans `OcrBlockLanguageClassifier`.

Une part des entrées `NO_COVERAGE` correspond à du bytecode Kotlin synthétique (`throwIndexOverflow`, `throwCountOverflow`, gardes non-null et lambdas). Les autres indiquent des variantes de syntaxe ou de bornage non exercées par les tests actuels. La campagne ne tranche pas entre branche défensive, règle non représentée et manque d’observation.

## Erreurs, avertissements et incompatibilités

Les trois tâches Gradle ont terminé avec `BUILD SUCCESSFUL`. Aucune incompatibilité Java, Kotlin, Gradle ou PIT n’a été observée.

Le groupe A a émis `Minion exited abnormally due to TIMED_OUT` pour un mutant de `IngredientTreeParser.parseList`, ligne source 106, remplaçant une addition entière par une soustraction. PIT a poursuivi et produit un rapport complet. Ce timeout isolé n’a pas été contourné et le code n’a pas été modifié.

Chaque campagne affiche l’avertissement informatif indiquant que le projet Kotlin n’utilise pas le plugin commercial Arcmutate Kotlin. La campagne précédente présentait déjà ce message ; il ne bloque ni la couverture ni la génération des mutants. Aucun plugin ou dépendance n’a été ajouté.

## Comparaison avec la campagne précédente

La campagne précédente ciblait `IngredientMatcher` et `VerdictEngine` : 157 mutants, 131 tués, 23 survivants et 3 non couverts, soit 83 % avec 289/291 lignes couvertes (99 %).

Le nouveau périmètre dédupliqué ajoute 779 mutants et atteint 47 % de mutations détectées avec des couvertures de lignes allant de 84 à 94 % selon le groupe. La baisse de score ne constitue pas une régression : les classes, la quantité de logique conditionnelle et les règles observées sont différentes. Elle montre surtout que l’exécution de lignes est élevée alors que de nombreuses variations de bornes, priorités et pondérations restent observablement identiques pour les assertions existantes.

## Comportements à auditer

Les observations suivantes sont suspectes au sens d’un besoin d’audit, sans diagnostic de défaut :

- le choix linguistique conserve son résultat sous de nombreuses mutations des poids `+40`, `+8`, `-20`, `-12`, `-50`, des multiplicateurs et des seuils de `scoreBlock`. La règle de classement peut être robuste par domination d’un signal, insuffisamment spécifiée, ou insuffisamment observée ;
- le classificateur conserve souvent son résultat lorsque changent le seuil de deux mots, la victoire stricte sur le second, la priorité du titre explicite ou les critères de troncature. Ces frontières méritent une revue fonctionnelle avant toute décision ;
- l’extracteur conserve souvent ses sorties après mutation des frontières entre ingrédients, présence déclarée, traces et sections ignorées, ainsi que de la priorité et de la profondeur des marqueurs ;
- le parser conserve souvent son arbre après mutation des continuations de classes fonctionnelles, des fins de phrase, des parenthèses de composition et des séparateurs de profondeur courante.

Ces constats peuvent correspondre à des mutants équivalents, des invariants induits par les regex, des règles ambiguës, des assertions ne vérifiant que le verdict final ou un défaut de production. Aucun cas n’a été corrigé et aucun test n’a été ajouté pour augmenter le score.

## Limites et recommandations

- Les groupes se chevauchent ; seule la synthèse dédupliquée permet d’estimer le volume ajouté. Une campagne globale sur les dix classes configurées inclurait aussi les 157 mutants historiques et n’a pas été relancée, car les groupes fournissent les mesures demandées sans dupliquer l’analyse.
- Les numéros de ligne PIT peuvent pointer vers des lignes synthétiques Kotlin ; l’audit suivant doit croiser XML, HTML, source et, si nécessaire, SMAP/bytecode comme lors de la campagne précédente.
- Les tests PIT restent volontairement larges (`com.example.isitvegan.*`) afin de conserver les interactions existantes ; 128 tests ont été examinés dans chaque groupe.
- L’audit suivant devrait commencer par les mutations arithmétiques et de seuil de `LabelLanguageSegmenter.scoreBlock`, puis les frontières de `OcrBlockLanguageClassifier.classify`, `LabelSectionExtractor.extract` et les séparateurs/continuations de `IngredientTreeParser`. Pour chaque mutant, il faut d’abord établir le comportement observable attendu et vérifier l’équivalence possible avant d’envisager un changement de code ou de test.
- Les 127 mutants non couverts doivent être séparés entre bytecode synthétique, branches défensives et variantes fonctionnelles réelles avant toute recommandation.

Cette itération reste une observation : aucun survivant ou mutant non couvert n’a été corrigé, et aucune conclusion automatique sur la nécessité d’un test n’est formulée.
