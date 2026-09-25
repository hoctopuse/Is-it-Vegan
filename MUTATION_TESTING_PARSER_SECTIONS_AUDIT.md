# Audit des mutants PIT du parser et des sections

Date : 25 septembre 2026

Branche : `mutation-testing-pit`
Commit au début de l'audit : `f9ab6a0` (`Merge branch 'master' into mutation-testing-pit`)

Commit contenant ensuite le périmètre déjà audité : `c924a4a6676baa101a71e5172274edbd0b8f56ce` (`test: extend PIT to parser and label sections`)

## État Git initial

La branche vérifiée par `git branch --show-current` est bien `mutation-testing-pit`. L'état initial était :

```text
A  MUTATION_TESTING_PARSER_SECTIONS_REPORT.md
 M mutation-core/build.gradle.kts
```

Ces deux changements sont ceux de la campagne précédente : son rapport est déjà ajouté à l'index et l'extension de `targetClasses` est encore non indexée. Aucun fichier Kotlin, test, asset ou fichier du module `:app` n'était modifié. Le présent audit ne modifie aucun de ces changements préexistants.

Pendant l'audit, un changement Git externe à cette session d'agent a créé `c924a4a` avec exactement ces deux fichiers. Le reflog date ce commit du 25 septembre 2026 à 16:12:53 +0200. Les sources de production et les XML analysés sont inchangés ; le contenu audité est donc celui de `c924a4a`. L'agent d'audit n'a exécuté aucune commande de commit ou de push.

## Correction du décompte dédupliqué

Le score de 47 % reste un ordre de grandeur correct, mais le décompte « dédupliqué » du rapport de campagne contient un doublon démontré par les XML. `TextNormalizer` a été muté dans les groupes A et B ; ses trois mutants, dont deux survivants et un tué, ont été additionnés deux fois.

| Mesure | Rapport précédent | Union XML réellement dédupliquée |
|---|---:|---:|
| Mutants | 779 | **776** |
| Tués | 362 | **361** |
| Survivants | 289 | **287** |
| Non couverts | 127 | **127** |
| Timeout | 1 | **1** |
| Détectés au sens PIT | 363 | **362** |
| Score arrondi | 47 % | **47 %** |

Le tableau des survivants ci-dessous réconcilie les **289 entrées annoncées** : 287 mutants uniques et une dernière ligne signalant les deux doublons de `TextNormalizer`. Il ne prétend pas qu'il existe 289 mutants uniques.

L'ancienne campagne à 83 % concernait 157 mutants de `IngredientMatcher` et `VerdictEngine`. La présente union à 47 % porte sur 776 mutants uniques de huit autres classes. Les scores ne mesurent pas le même périmètre et leur écart n'est pas une régression.

## Sources et méthode

L'audit a lu intégralement les cinq rapports demandés, `docs/parsing.md`, `docs/sections.md`, `docs/langues.md` et `docs/tests.md`. Il a croisé :

- les XML des groupes A, B et C conservés sous `mutation-core/build/reports/pitest-groups` ;
- le XML et les pages HTML du dernier groupe sous `mutation-core/build/reports/pitest` ;
- le code source, les tests JVM du module et les tests Android qui documentent certains cas sans appartenir au classpath PIT ;
- le bytecode Java 17 avec `javap` pour les conditions ambiguës de la ligne 129 de `LabelSectionExtractor` ;
- une invocation en lecture seule des classes déjà compilées pour confirmer le cas des traces adjacentes.

Les XML donnent classe, méthode, ligne, mutateur, index bytecode, nombre de tests exécutés et test tueur lorsqu'il existe. Pour un survivant, PIT laisse `killingTest` vide et ne donne pas le nom des tests couvrants. La colonne « tests PIT » indique donc le nombre d'essais enregistré ; les suites associées sont déduites des appels présents dans `IngredientTreeParserTest`, `LabelSectionExtractorTest`, `ParserModulesTest`, `HierarchyAnalysisTest`, `Version059CompletionTest` et `DiagnosticStabilizationTest`. Une attribution nominative plus précise serait spéculative.

Chaque ligne des deux tableaux regroupe des mutants ayant la même conclusion. La colonne « nombre » garantit la traçabilité de tous les mutants. Les lignes synthétiques supérieures à la longueur du fichier source viennent du SMAP Kotlin et sont signalées comme telles.

## Synthèse par composant

### `LabelLanguageSegmenter`

Les 38 mutations réelles de `scoreBlock` qui survivent modifient les coefficients, seuils ou signaux. Le code reste souvent sur le même bloc parce que seuls les blocs titrés sont comparés dès qu'un titre existe, et parce que certains bonus sont communs à tous les candidats. Le score et les signaux restent toutefois observables dans le diagnostic : ces mutants ne sont pas équivalents.

La documentation décrit l'ordre qualitatif, mais pas les valeurs `+40`, `+8`, `-20`, `-12`, `-50`, les plafonds ni leurs rapports. Leur statut principal est donc `REGLE_METIER_A_CLARIFIER`, confiance élevée. Une décision doit précéder tout test de seuil.

Les mutations des raisons de sélection, langues rejetées et égalités changent seulement le diagnostic pour les cas actuels. Le comportement voulu est documenté ; elles sont classées `TEST_MANQUANT`, sans recommandation d'écrire ces tests pendant cet audit.

### `OcrBlockLanguageClassifier`

La règle « au moins deux mots et strictement plus que la deuxième langue » est explicitement documentée. Les mutations du seuil, de l'égalité, de la priorité d'un titre distinctif et du troncage changent un comportement observable. Le code est cohérent avec le document ; leur catégorie est `TEST_MANQUANT`.

Une ambiguïté documentaire subsiste : `docs/langues.md` parle de quatre vocabulaires FR/EN/NL/DE, alors que le code contient aussi IT et PL. Ce n'est pas un défaut démontré par un mutant, mais la règle prise comme référence doit être clarifiée. Le titre `Ingredients` reste volontairement ambigu et dépend du contenu ; un titre isolé conserve la langue du marqueur et est déclaré tronqué.

### `LabelSectionExtractor`

Les principaux chemins ingrédients, présence réelle, traces, sections ignorées et marqueurs imbriqués sont exécutés. Un défaut réel est démontré dans la déduplication des traces : `TraceSection.end` est documenté comme exclusif, mais la ligne 129 traite l'égalité `candidate.start == existing.end` comme un chevauchement.

Entrée réaliste, notamment après OCR sans espace :

```text
Ingrédients : eau. Peut contenir : lait.May contain traces of egg.
```

Résultat actuel vérifié : une seule `TraceSection`, `Peut contenir : lait.`. La trace `egg` disparaît du diagnostic. Résultat attendu : deux sections adjacentes, toutes deux conservées. L'impact concerne le diagnostic et les avertissements de contamination ; le verdict doit rester celui de `eau`, car aucune trace ne doit y participer. Le mutant de borne à l'index bytecode 892 remplace précisément la comparaison inclusive qui devrait être exclusive. Il est classé `CODE_PRODUCTION_SUSPECT`, confiance élevée. Aucune correction n'est effectuée ici.

L'autre comparaison de la même expression, index 887, porte sur `existing.start <= candidate.end`. Comme les candidats sont triés et non vides, son égalité ne décrit pas le cas adjacent dans le sens utile ; son mutant de borne est un équivalent probable sous les invariants actuels.

### `IngredientTreeParser`

Les survivants fonctionnels couvrent les continuations de classes, parenthèses de composition, séparateurs, fins de phrase, sections internes, alternatives et propagation des métadonnées. Le code lu suit la documentation : progression monotone, virgule décimale protégée, séparation à profondeur zéro, récupération conservatrice et propagation des enfants.

Aucun survivant ne démontre qu'un ingrédient animal, inconnu ou enfant imbriqué disparaît du verdict. Les mutations changent néanmoins des arbres observables sur des entrées de frontière ; elles relèvent surtout de `TEST_MANQUANT`. Les contrôles non-null et chemins rapides compilés sont séparés comme `BYTECODE_KOTLIN_OU_DEFENSIF`.

### Dépendances directes

`FunctionalClassLexicon`, `OriginQualifierRuleSet`, `LabelLexicon` et `TextNormalizer` expliquent une part importante du volume. Les mutations métier concernent les préfixes de classes, titres OCR dégradés et qualifications attachées. Plusieurs cas sont couverts seulement dans `app/src/test`, hors du classpath de la tâche PIT JVM ; le statut PIT ne signifie donc pas nécessairement absence totale de test dans le dépôt.

## Tableau des 289 survivants annoncés

Abréviations d'action : A = correction à étudier, B = règle à clarifier, C = test futur, D = exclusion PIT étroite éventuellement justifiable, E = aucune action. Une ligne groupée attribue exactement une catégorie principale à chaque mutant qu'elle compte.

| ID | Nombre | Localisation, lignes et mutation | Exécution / différence observable | Catégorie | Confiance | Action |
|---|---:|---|---|---|---|---|
| S01 | 3 | `FunctionalClassLexicon.matchPrefix`, 120/137, conditions niées | 32 tests ; peut changer le préfixe réglementaire le plus long et sa désignation. Le code suit la priorité documentée. | `TEST_MANQUANT` | moyenne | C |
| S02 | 10 | `IngredientTreeParser.additiveNodes` 442–448 et `extractStructuralMetadata` 472, OR/Elvis/condition niés | 6–34 tests ; propagation nano, proportions, alternatives et origine observable dans l'arbre/diagnostic. | `TEST_MANQUANT` | élevée | C |
| S03 | 15 | `functionalClassWithContinuations` 140–155, `startsFunctionalClass` 161–164 et condition réelle de `parseFunctionalClass` 390 | 2–34 tests ; peut absorber trop ou trop peu de désignations. Le code est conforme aux virgules de continuation documentées. | `TEST_MANQUANT` | moyenne | C |
| S04 | 6 | mêmes méthodes, suppressions `Intrinsics.checkNotNullExpressionValue` aux lignes 141/143/161/391/392/402 | Les API `substring`/`trim` sont non nulles ; aucun résultat métier distinct établi. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S05 | 13 | `inspectParentheses` 80/88 et `isCompositionParenthesis` 278/285/287 et lignes SMAP 549–558, bornes/conditions/arithmetic | 1–34 tests ; équilibre, sigles, mot unique, qualification et imbrication peuvent changer l'arbre. | `TEST_MANQUANT` | moyenne | C |
| S06 | 3 | `parseList` 119 et conditions réelles de `parseSegment` 180/539 | 1–32 tests ; arrêt de section ou création d'un parent composite potentiellement différents. | `TEST_MANQUANT` | moyenne | C |
| S07 | 1 | `parseSegment` 200, suppression d'une garde non-null sur `substring(...).trim()` | Valeur contractuellement non nulle. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S08 | 20 | `splitAtCurrentDepthWithSeparators` 304–306, `isSentenceBoundary` 327–329, `nextNonWhitespace` 379/381, `topLevelColon` 353/358 | 1–33 tests ; décimales, retours de ligne, point final et séparateurs peuvent produire des arbres différents. | `TEST_MANQUANT` | moyenne | C |
| S09 | 4 | `splitAtCurrentDepthWithSeparators` 313/317/322 et `topLevelGroups` 341, gardes `Intrinsics` | Suppression de contrôles sur des chaînes créées localement. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S10 | 3 | `replaceTopLevelAlternatives` 503/504/506, incrément/arithmetic/condition | 17–34 tests ; `et/ou` à profondeur zéro peut ne plus devenir séparateur. | `TEST_MANQUANT` | élevée | C |
| S11 | 1 | `replaceTopLevelAlternatives` 518, garde non-null sur `StringBuilder.toString()` | `toString()` ne renvoie pas `null`; résultat identique. | `MUTANT_EQUIVALENT_PROBABLE` | élevée | D |
| S12 | 8 | conditions réelles de `sectionParts` 366/367/370/373/374 | 2–34 tests ; distinction titre, `contient`, quantité et contenu vide observable. | `TEST_MANQUANT` | moyenne | C |
| S13 | 2 | `sectionParts` 371/372, gardes non-null sur les deux `substring` | Indices bornés par `topLevelColon`; aucun effet observable probable. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S14 | 2 | `LabelLanguageSegmenter.fallback`, lignes SMAP 305, garde/incrément de comptage | Chemin compilé de `count`; le texte complet et le score utile métier ne sont pas modifiés de façon isolée démontrée. | `BYTECODE_KOTLIN_OU_DEFENSIF` | moyenne | D |
| S15 | 3 | `findMarkers` 130 et lambda 116, chevauchement/répétition/contentStart | 6–7 tests ; peut conserver deux titres adjacents ou couper au mauvais caractère. | `TEST_MANQUANT` | moyenne | C |
| S16 | 2 | `findMarkers` 132 et `marketTags` ligne 217, gardes `Intrinsics` | Valeurs de `substring` et `uppercase` non nulles. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S17 | 2 | `preferredBlock` 154, conditions internes de `firstOrNull`/fallback | La méthode reçoit une liste non vide après les gardes de `segment`; les chemins donnent le même premier élément probable. | `MUTANT_EQUIVALENT_PROBABLE` | moyenne | D |
| S18 | 38 | `scoreBlock` 161–202, coefficients, multiplications, seuils et conditions | 2–16 tests ; score/signaux changent, et un classement serré peut changer. Valeurs numériques non spécifiées. | `REGLE_METIER_A_CLARIFIER` | élevée | B |
| S19 | 13 | `scoreBlock`, lignes SMAP 296/299/301/302, gardes de `count`/`any` inline | Conditions de parcours Kotlin, voisines des conditions métier réelles. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S20 | 10 | `segment` 93–107, égalités de score, raisons et langues rejetées | 16 tests ; sortie sélectionnée souvent identique, mais diagnostic observable différent. | `TEST_MANQUANT` | élevée | C |
| S21 | 8 | `segment` 88 et lignes SMAP 257–271, garde non-null/collections inline | Aucun comportement utilisateur distinct isolé ; bruit Kotlin dominant. | `BYTECODE_KOTLIN_OU_DEFENSIF` | moyenne | D |
| S22 | 9 | `LabelLexicon` 41/76/87/99/109/116/117/124/126, séparateurs, filtres OCR, certification et bornes produit | 1–34 tests ; reconnaissance observable de titres et frontières. | `TEST_MANQUANT` | moyenne | C |
| S23 | 1 | `LabelLexicon.headingMatch` 137, suppression du contrôle non-null de `groups[1]!!` | Le regex construit garantit le groupe ; l'exception et le résultat ne changent probablement pas. | `MUTANT_EQUIVALENT_PROBABLE` | élevée | D |
| S24 | 1 | initialisation `ingredientWordPattern`, lambda ligne 41 retournant `null` | Une initialisation statique mutée est difficile à attribuer après chargement de classe ; effet PIT non isolé. | `ANALYSE_INSUFFISANTE` | faible | E |
| S25 | 3 | `LabelSectionExtractor.depthAt` 310–312, borne et signe de profondeur | 5–20 tests ; un marqueur après une parenthèse fermée peut rester faussement imbriqué sous mutation. Code original correct. | `TEST_MANQUANT` | élevée | C |
| S26 | 1 | `extract` 129, index 892, `<=` changé en `<` | Cas adjacent confirmé : le mutant corrige la conservation de la seconde trace. | `CODE_PRODUCTION_SUSPECT` | élevée | A |
| S27 | 1 | `extract` 129, index 887, première borne `<=` changée en `<` | Avec candidats triés, l'égalité opposée ne décrit pas une section valide distincte. | `MUTANT_EQUIVALENT_PROBABLE` | moyenne | D |
| S28 | 13 | autres conditions métier de `extract` 102–150, dont index 887 nié | 1–33 tests ; sans titre, limites de trace/ignoré, langue et déduplication peuvent varier. | `TEST_MANQUANT` | moyenne | C |
| S29 | 7 | `extract` 99/120/138 et lignes SMAP 325/342/370, gardes non-null/collections | Contrôles Kotlin sans effet métier isolé. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S30 | 2 | `contentUntil` 284 et lambda de `joinToString` 162, gardes/retour non-null | Chaînes non nulles construites localement. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S31 | 8 | prédicats/offsets de `extract` et `findMarkers`, lignes 93/209/216/222/232/263/416 | 2–20 tests ; début/fin d'ingrédients, traces, `contient` et sections ignorées observables. | `TEST_MANQUANT` | moyenne | C |
| S32 | 1 | `findMarkers` 231, suppression de `checkNotNull` | Garde Kotlin sur un `MatchResult` déjà présent. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S33 | 1 | `hasEndOrMarketingMarker` 84 forcé à `true` | Le score linguistique reçoit une pénalité injustifiée ; sélection serrée potentiellement différente. | `TEST_MANQUANT` | moyenne | C |
| S34 | 4 | même méthode, lignes SMAP 318/319, gardes du `any` inline | Parcours Kotlin ; la condition regex voisine reste intacte. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S35 | 1 | `markerPriority` 274, retour remplacé par `0` | Les regex actuelles ne produisent pas de marqueurs de kinds différents au même début de façon observée ; ordre probablement inchangé. | `MUTANT_EQUIVALENT_PROBABLE` | moyenne | D |
| S36 | 5 | `normalizeTraceText` 189–192, préfixes et retour vide | 10 tests ; texte diagnostique de trace change sans toucher au verdict. | `TEST_MANQUANT` | élevée | C |
| S37 | 23 | conditions réelles de `OcrBlockLanguageClassifier.classify` 38–66 | 9–16 tests ; seuil de deux, égalité, titre explicite, troncage et métadonnées observables. | `TEST_MANQUANT` | élevée | C |
| S38 | 8 | `classify` 39 et lignes SMAP 91/93/99, garde non-null et boucles inline | Contrôles générés ou parcours de collections ; aucun effet isolé démontré. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S39 | 2 | `parenthesisBalance` 80/81, `+`/`-` inversés | Un bloc équilibré peut devenir tronqué sous mutation ; code original correct. | `TEST_MANQUANT` | élevée | C |
| S40 | 3 | `tokenize` 76, borne, condition et retour vide | Le seuil de longueur des mots change le vocabulaire dominant. | `TEST_MANQUANT` | élevée | C |
| S41 | 3 | `tokenize` 73 et ligne SMAP 102, gardes non-null/inline | Normalisation Java non nulle et contrôle de collection Kotlin. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S42 | 10 | comportements réels d'`OriginQualifierRuleSet` 84–198 : attachement, correspondance, protection et parenthèse finale | 1–34 tests ; qualification et statut effectif peuvent changer. Le code suit les règles chargées. | `TEST_MANQUANT` | moyenne | C |
| S43 | 11 | gardes non-null et lignes SMAP 525/530/531/538/540/541/548/549 | Boucles `forEach`/`any` compilées et valeurs de chaînes non nulles. | `BYTECODE_KOTLIN_OU_DEFENSIF` | élevée | D |
| S44 | 2 | `TextNormalizer.normalize` 8/11, suppressions de contrôles non-null | Les API `lowercase`, `Normalizer.normalize` et `replace` sont non nulles ; résultat inchangé. | `MUTANT_EQUIVALENT_PROBABLE` | élevée | D |
| S45 | 2 | doublons exacts de S44 provenant du groupe B | Ces entrées expliquent 289 annoncés contre 287 uniques ; ce ne sont pas de nouveaux mutants. | `MUTANT_EQUIVALENT_PROBABLE` | élevée | E |
| **Total annoncé** | **289** | **287 uniques + 2 doublons** |  |  |  |  |

Répartition des 287 uniques : 1 `CODE_PRODUCTION_SUSPECT`, 38 `REGLE_METIER_A_CLARIFIER`, 165 `TEST_MANQUANT`, 8 `MUTANT_EQUIVALENT_PROBABLE`, 74 `BYTECODE_KOTLIN_OU_DEFENSIF` et 1 `ANALYSE_INSUFFISANTE`.

## Tableau des 127 mutants non couverts

Pour ces mutants, `numberOfTestsRun=0`. « Test futur » signifie qu'un comportement réaliste existe, pas qu'un test doit être ajouté pour améliorer un score.

| ID | Nombre | Localisation, lignes et mutation | Atteignabilité et analyse | Catégorie | Test futur / exclusion |
|---|---:|---|---|---|---|
| N01 | 2 | `FunctionalClassLexicon.contains` 130, retour vrai/condition niée | Méthode non appelée dans le dépôt ; simple façade de `match`. Fonctionnelle mais sans consommateur actuel. | `BRANCHE_NON_COUVERTE_JUSTIFIEE` | Test seulement si elle devient API utilisée ; exclusion de méthode envisageable après confirmation d'absence de consommateur externe. |
| N02 | 10 | `FunctionalClassLexicon.matchPrefix` 122–155, bornes, retours et gardes | Atteignable avec classes préfixées et désignation ; comportement réglementaire réel. | `TEST_MANQUANT` | Cas directs de préfixe long, désignation vide et classe inconnue ; aucune exclusion. |
| N03 | 47 | `IngredientTreeParser` : séries E 95, métadonnées 492, fermeture 81, composition 272–279, identifiants 522–524, classes 410–424, segments 174–223, alternatives 508–512, récupération 303, préfixes 164 | Entrées réalistes : série `E202-E262`, fermeture inattendue, AOP/origine, `contient`, classe sans désignation, alternative de niveau zéro et parenthèse manquante. Plusieurs existent dans les tests Android mais pas dans le classpath PIT JVM. | `TEST_MANQUANT` | Tests JVM futurs fondés sur règles documentées ; ne pas exclure. |
| N04 | 2 | `LabelLanguageSegmenter.scoreBlock` 161/162, certification biologique et pénalité `-80` | Réaliste ; un test Android existe pour la certification, mais PIT du module ne l'utilise pas. | `TEST_MANQUANT` | Porter ultérieurement l'observation au niveau JVM si cette règle reste voulue. |
| N05 | 2 | `LabelLanguageSegmenter.segment`, lignes SMAP 246/268, suppressions de `throwIndexOverflow`/`throwCountOverflow` | Débordement de compteur d'itérateur impossible avec une chaîne Java réaliste en mémoire. | `BYTECODE_KOTLIN_OU_DEFENSIF` | Aucun test ; exclusion ciblée des appels de garde Kotlin envisageable. |
| N06 | 31 | `LabelLexicon.findIngredientHeadings` 100/114–123/158/161 et `findProductLanguageBoundaries` 85 | Variantes OCR `ztaten`, néerlandais dégradé, séparateur et titres produit ; branches fonctionnelles et réalistes, certaines couvertes seulement dans `:app`. | `TEST_MANQUANT` | Cas JVM futurs de faux positif et liste suffisamment structurée ; aucune exclusion métier. |
| N07 | 1 | getter `LabelLexicon.ingredientHeadings` ligne 30 remplacé par liste vide | Le code interne lit le champ dans le même objet ; le getter public n'a pas de consommateur actuel. | `BRANCHE_NON_COUVERTE_JUSTIFIEE` | Aucun test tant que l'API n'est pas consommée ; exclusion de ce getter éventuellement justifiable. |
| N08 | 19 | `LabelSectionExtractor` : prédicats 93/102/108/109/142/143, certification 240/241, frontière produit 250/251, `traceEnd` 300/301, titre final 291/292 | Cas réalistes : sections successives, certification, titre produit OCR, ponctuation dans parenthèses et titre après une phrase. Plusieurs tests correspondants restent dans `:app`. | `TEST_MANQUANT` | Tests JVM futurs seulement pour les invariants retenus ; aucune exclusion globale. |
| N09 | 1 | `OcrBlockLanguageClassifier.classify`, ligne SMAP 93, suppression de `throwCountOverflow` | Artefact de comptage Kotlin ; nombre de tokens irréaliste pour atteindre le débordement. | `BYTECODE_KOTLIN_OU_DEFENSIF` | Aucun test ; exclusion ciblée envisageable. |
| N10 | 10 | `OriginQualifierRuleSet` 110/111/126/127/143/169/170/188/207 et ligne SMAP 534, conditions/retours | Atteignable avec règle, forme d'attachement, cible ou statut de base différents. Ces chemins dépendent des fixtures de règles. | `TEST_MANQUANT` | Cas futurs par matrice de règles injectées ; ne pas exclure. |
| N11 | 2 | `OriginQualifierRuleSet.extractAttached$lambda` 88 et `qualification` 143, retours `null` synthétiques | Kotlin impose des retours non nuls ; la mutation vise la plomberie de lambda plus que la décision métier. | `BYTECODE_KOTLIN_OU_DEFENSIF` | Aucun test dédié ; exclusion étroite envisageable après confirmation ASM. |
| **Total** | **127** |  |  |  |  |

Répartition : 119 `TEST_MANQUANT`, 3 `BRANCHE_NON_COUVERTE_JUSTIFIEE` et 5 `BYTECODE_KOTLIN_OU_DEFENSIF`.

## Analyse séparée du timeout

Le mutant unique est `IngredientTreeParser.parseList`, ligne 106, index bytecode 55 : `index += functionalGroup.second` devient `index -= functionalGroup.second`.

`functionalClassWithContinuations` ne renvoie un groupe que si au moins deux désignations ont été consommées ; `functionalGroup.second` est donc strictement positif. Le code original avance toujours vers la fin de `segments`. Avec le mutant, un groupe fonctionnel rencontré après un segment antérieur ramène l'index vers ce segment ; le parcours revient ensuite au même groupe et boucle. S'il est rencontré en première position, un index négatif peut plutôt provoquer une exception rapide. La forme exacte dépend de l'entrée choisie par PIT, ce qui explique le timeout observé.

Il n'existe pas de boucle dangereuse équivalente dans le code original : les trois branches de `parseList` font progresser `index` par un entier positif, `index++` ou `index = next` avec `next > index`. Une entrée réaliste ne peut pas rendre `functionalGroup.second` négatif ou nul. Le timeout est propre à la mutation artificielle ; aucune décision fonctionnelle ou correction technique n'est nécessaire. PIT l'a correctement compté comme mutation détectée.

## Vérification des invariants métier

| Invariant | Conclusion de l'audit |
|---|---|
| Une composition imbriquée transmet son statut | Démontré par les tests de diagnostic et de hiérarchie ; aucun défaut résiduel démontré. |
| Un ingrédient animal produit NON VEGAN | Démontré par `DiagnosticStabilizationTest` et les tests de verdict ; hors des survivants du nouveau périmètre. |
| Un inconnu n'est pas VEGAN silencieusement | Démontré par le diagnostic `UNKNOWN_INGREDIENT`; aucun mutant audité ne contourne le matcher/verdict. |
| Les traces n'influencent pas le verdict | Démontré. S26 peut perdre une trace du diagnostic, mais ne l'injecte pas dans le verdict. |
| Un `contient` interne ne coupe pas la composition | Le filtre de profondeur est cohérent et testé ; des frontières supplémentaires restent à tester, pas de défaut démontré. |
| Nutrition, conservation et marketing sont ignorés | Plusieurs scénarios sont testés ; N08 montre des variantes hors classpath PIT, pas un défaut confirmé. |
| Les pourcentages restent attachés au bon ingrédient | Démontré sur compositions imbriquées et décimales ; mutations de frontière à protéger ultérieurement. |
| Parenthèses et crochets conservent la hiérarchie | Démontré sur cas principaux ; cas de récupération et frontières restent classés `TEST_MANQUANT`. |
| Plusieurs langues peuvent coexister | Démontré ; les égalités et blocs courts restent une règle/observation à préciser. |
| Une langue inconnue ne détruit pas une section | Le fallback conserve tout le texte et l'extracteur peut retrouver un titre ; aucun défaut démontré. |
| Le diagnostic reflète le comportement sans changer le verdict | Démontré globalement, sauf perte de la seconde trace adjacente S26. |

## Défauts potentiels de production

Un seul cas est démontré : S26, perte d'une seconde section de traces exactement adjacente. Le risque est une information de contamination incomplète dans le diagnostic ou l'interface. L'entrée, le résultat actuel et le résultat attendu sont détaillés plus haut. La mutation révèle une différence observable parce qu'elle aligne la comparaison sur la convention de fin exclusive déjà documentée. Aucune correction ne doit être implémentée pendant cet audit.

Les autres survivants ne justifient pas une correction de production. En particulier, le timeout ne révèle pas une non-termination du code original.

## Règles métier ambiguës

- Les valeurs exactes et rapports des poids de `scoreBlock` ne sont pas spécifiés. Il est démontré que les mutants changent scores et signaux ; leur effet souhaité sur les classements serrés reste incertain.
- La documentation linguistique annonce quatre vocabulaires, le code en possède six. Il faut choisir la référence avant de figer des tests.
- Le diagnostic distingue « qualité unique » d'une égalité de score, mais la notion de « qualités comparables » repose actuellement sur l'égalité exacte. Une tolérance éventuelle serait une décision produit.
- `markerPriority` paraît sans effet avec les regex actuelles à position identique ; une future extension des marqueurs pourrait rendre cette priorité fonctionnelle.

## Mutants équivalents probables et bytecode Kotlin

Les huit équivalents probables uniques reposent sur des invariants inspectés : retour non nul de `StringBuilder.toString`, liste non vide de `preferredBlock`, ordre des traces pour la première borne, absence actuelle de collision de marker kinds et API Java/Kotlin non nulles de `TextNormalizer`. Le score faible n'est jamais utilisé comme preuve d'équivalence.

Les 74 survivants et 5 non couverts classés `BYTECODE_KOTLIN_OU_DEFENSIF` sont surtout des suppressions d'`Intrinsics.checkNotNull*`, gardes de fonctions inline, branches SMAP de `any`/`count`/`forEach` et protections de dépassement d'itérateur. Une exclusion PIT pourrait être étudiée uniquement pour des signatures générées précises et une version de compilateur documentée. Exclure `NegateConditionalsMutator`, `MathMutator` ou des méthodes métier entières masquerait de vrais signaux et n'est pas justifié.

## Tests futurs éventuellement utiles

Ces propositions protègent un comportement formulable ; elles ne visent pas un pourcentage :

- deux traces adjacentes, séparées ou non par un espace, avec vérification des bornes exclusives et maintien du verdict ;
- deux blocs titrés de scores proches, égalité exacte, interface FR/EN/NL, bloc tronqué et titre `Ingredients` ambigu ;
- classificateur avec 1 puis 2 mots dominants, égalité entre langues, titre distinctif isolé, parenthèses équilibrées et déséquilibrées ;
- marqueur top-level après une parenthèse fermée, `contient` imbriqué et section vide ;
- alternatives de niveau zéro, série de numéros E, fermeture inattendue, fin décimale, classe fonctionnelle suivie d'une nouvelle classe et récupération de parenthèse manquante ;
- matrice minimale de règles d'origine injectées couvrant cible, statut de base et forme d'attachement.

Aucun test ne devrait être ajouté pour les contrôles non-null, `throwIndexOverflow`, `throwCountOverflow` ou les chemins rapides de collection sans sortie distincte.

## Limites

- PIT ne nomme pas les tests couvrants pour les survivants ; seul leur nombre est disponible. Les associations de suites sont donc établies par recherche statique.
- Les tests Android ne participent pas à la tâche PIT du module JVM. Certains statuts `SURVIVED` ou `NO_COVERAGE` décrivent une limite de classpath, pas une absence de test dans tout le dépôt.
- Les lignes SMAP peuvent regrouper plusieurs conditions. Sauf pour S26/S27 inspectés au bytecode, les conditions impossibles à distinguer sont classées prudemment.
- Aucun mutant n'a été relancé isolément. Le seul comportement dynamique vérifié est celui des traces adjacentes, sur les classes compilées existantes.
- Les catégories `TEST_MANQUANT` signifient « code cohérent avec la règle disponible et différence observable formulable ». Elles ne prescrivent pas automatiquement un nouveau test.

## Recommandations

### A. Correction de production à étudier

Étudier S26 : remplacer la notion de chevauchement inclusif par une intersection de demi-intervalles cohérente avec `end` exclusif. Reproduire d'abord le cas adjacent dans une future phase autorisant code et tests. Ne rien corriger dans le présent audit.

### B. Clarification d'une règle métier

Spécifier les coefficients ou au minimum les relations d'ordre attendues de `scoreBlock`, décider si l'égalité exacte suffit à définir des qualités comparables, et aligner la documentation sur les langues réellement comptées par le classificateur.

### C. Tests à ajouter ultérieurement

Priorité : trace adjacente, égalités/seuils linguistiques, fermeture de parenthèse suivie d'un marqueur top-level, alternatives/classes fonctionnelles et variantes actuellement testées seulement dans `:app`. Ajouter un test uniquement après avoir formulé la sortie métier ou diagnostique attendue.

### D. Exclusion PIT éventuellement justifiable

Envisager plus tard des exclusions étroites pour `Intrinsics.checkNotNull*`, `throwIndexOverflow`, `throwCountOverflow` et certains chemins inline démontrés équivalents. Conserver toutes les mutations de conditions, bornes, calculs et retours des méthodes métier.

### E. Aucune action

Aucune action pour le timeout artificiel, les deux doublons de comptage, les équivalents probables tant que le bruit reste acceptable, et les getters non utilisés tant qu'ils ne deviennent pas une API consommée.

## Conclusion probante

Est démontré : le doublon de comptage, le timeout artificiel sans risque original, la perte d'une trace adjacente et l'existence de nombreuses mutations Kotlin sans sortie métier distincte. Est probable : l'équivalence des huit mutants identifiés et l'absence d'autre défaut de production dans les scénarios inspectés. Restent incertains : les coefficients linguistiques souhaités, quelques conditions regroupées par SMAP et l'effet exact de certains chemins couverts seulement côté Android.

Aucun code, test, asset, fichier Gradle ou rapport existant n'a été modifié par l'audit. Le seul fichier créé par l'audit est le présent rapport. Le commit concurrent `c924a4a` a enregistré les deux changements qui existaient déjà au relevé initial ; aucun commit ni push n'a été effectué par l'agent d'audit.
