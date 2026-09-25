# Audit du code face aux mutants PIT restants

Date : 2026-09-25  
Branche : `mutation-testing-pit`  
Commit observé : `b199be5 test: strengthen mutation core coverage`

## Baseline et périmètre

La campagne actuelle cible uniquement `IngredientMatcher` et `VerdictEngine` dans
`:mutation-core`. Elle génère 157 mutants : 131 tués, 23 survivants et 3 non
couverts, soit un score de mutation de **83 %**. La couverture de lignes ciblées
reste de 289/291 (99 %). Les commandes `:mutation-core:test` et
`:mutation-core:pitest` étaient déjà validées par l'itération précédente ; elles
n'ont pas été relancées pendant cet audit strictement statique.

## Méthode

L'analyse croise `mutations.xml`, les pages HTML PIT, le code source des deux
classes, les tests existants et la table SMAP du bytecode Kotlin obtenue avec
`javap`. Cette dernière relie les lignes synthétiques PIT aux appels inline :

- 245 → `normalizedAliases.any` (source 45) ;
- 260 → `candidates.any` (source 76) ;
- 268 → `none` sur la plage d'un candidat (source 84) ;
- 282 → `blocked.all` (source 99) ;
- 284 → `ordered.any` (source 100) ;
- 306 → `all` sur le reliquat de caractères (source 146) ;
- 315 → `all` sur les mots de liaison (source 175) ;
- 318 → `selected.any` (source 178).

Les paires d'indices distantes de quatre montrent un schéma constant : PIT tue
la mutation de la condition métier ou de `isEmpty`, tandis que la garde de
chemin rapide générée par Kotlin survit. Pour des récepteurs qui sont toujours
des `List`/`Collection`, supprimer ce chemin rapide ne change que la manière
d'itérer, pas le résultat.

## Synthèse des 23 survivants

- **21 `MUTANT_EQUIVALENT_PROBABLE`** : gardes inline de collection, contrôles
  non-null Kotlin, bornes d'adjacence rendues redondantes par les frontières
  regex, ou comparaison de longueur redondante.
- **1 `CODE_PRODUCTION_CORRECT_TEST_MANQUANT`** : espace non expliqué autour
  d'une expression végétale protégée ; le comportement du code est cohérent,
  mais aucun test ne distingue espace et caractère sémantique à cet endroit.
- **1 `ANALYSE_INSUFFISANTE`** : condition de contrôle du `all` sur une chaîne,
  sans effet observable isolé démontré à partir du rapport seul.
- Aucun survivant ne justifie `CODE_PRODUCTION_SUSPECT`.

### Tableau individuel des survivants

| ID | Localisation et mutation exacte | Exemple réaliste ; résultat actuel → attendu | Analyse du comportement et code | Catégorie |
|---|---|---|---|---|
| S01 | `IngredientMatcher.<init>`, ligne 46, index 576 : borne `>` changée en `>=` pour un alias lié plus long. | Base `lait` + `lait de coco`; `lait de coco` → ingrédient végétal long, sans faux `milk`; identique attendu. | `startsWith("$alias ")` implique déjà une longueur strictement supérieure. Le code est correct et les deux bornes sont observablement identiques. | `MUTANT_EQUIVALENT_PROBABLE` |
| S02 | `<init>`, ligne synth. 245/index 542 : garde de chemin rapide de `normalizedAliases.any` niée. | Même base `lait`/`lait de coco`; résultat actuel → même résultat attendu. | Le récepteur est la `List` créée ligne 29. La mutation remplace le chemin rapide de collection par l'itération normale ; la condition métier voisine (index 546) est déjà tuée. | `MUTANT_EQUIVALENT_PROBABLE` |
| S03 | `hasNoSemanticRemainder`, ligne 146/index 301 : branche de `isWhitespace() || punctuation` niée. | `( beurre de cacao )` → devrait rester `COVERED`, avec `cocoa` sélectionné et `butter` bloqué. | Le code accepte correctement espaces et ponctuation hors des plages expliquées. Les tests couvrent la ponctuation accolée et les résidus sémantiques, mais pas la ponctuation espacée. La mutation peut refuser un espace pourtant neutre. | `CODE_PRODUCTION_CORRECT_TEST_MANQUANT` |
| S04 | `hasNoSemanticRemainder`, ligne synth. 306/index 317 : condition interne du `all` sur les caractères niée. | `beurre de cacao extra` → `BLOCKED_CONFLICT`, actuel et attendu ; `(beurre de cacao)` → `COVERED`, actuel et attendu. | Les chemins vrai et faux métier sont déjà assertés. Le rapport ne permet pas d'isoler une sortie différente de cette seconde garde compilée ; le code source est cohérent. | `ANALYSE_INSUFFISANTE` |
| S05 | `isCovered`, ligne synth. 315/index 205 : garde de collection de `all { it in glueWords }` niée. | `sel et` → `COVERED`, actuel et attendu. | La liste issue de `split(...).filter(...)` est une collection ; la condition métier voisine, index 209, est tuée par le test opposant `et` à `mystère`. | `MUTANT_EQUIVALENT_PROBABLE` |
| S06 | `isCovered`, ligne synth. 318/index 268 : garde de collection de `selected.any` niée. | `arôme naturel de vanille` → `COVERED`, actuel et attendu. | `selected` est une `List`. Le chemin rapide et l'itération ont la même valeur ; la condition voisine, index 272, est déjà tuée. | `MUTANT_EQUIVALENT_PROBABLE` |
| S07 | `isCovered`, ligne 184/index 359 : suppression de `checkNotNullExpressionValue` sur le préfixe extrait. | `purée d'abricot` → `COVERED`, actuel et attendu. | `String.substring(...).trim()` ne renvoie pas `null`. Retirer la garde Kotlin ne modifie aucune sortie ni exception attendue. | `MUTANT_EQUIVALENT_PROBABLE` |
| S08 | `isCovered`, ligne 185/index 377 : même suppression sur le suffixe extrait. | `purée d'abricot moulue` → `COVERED`, actuel et attendu. | Même justification que S07 ; le code est correct. | `MUTANT_EQUIVALENT_PROBABLE` |
| S09 | `match`, ligne synth. 260/index 278 : garde de collection de `candidates.any` niée dans la recherche d'une source protectrice. | `beurre de cacao` → `cocoa`, `butter` bloqué, `COVERED`; identique attendu. | `candidates` est la liste matérialisée ligne 57. La condition métier voisine, index 282, est tuée. | `MUTANT_EQUIVALENT_PROBABLE` |
| S10 | `match`, ligne synth. 282/index 834 : garde de collection de `blocked.all` niée. | `(beurre de cacao)` → `COVERED`; identique attendu. | `blocked` est une `List`; le `all` vide et non vide garde sa sémantique par itération. L'index 838 voisin est tué. | `MUTANT_EQUIVALENT_PROBABLE` |
| S11 | `match`, ligne synth. 284/index 874 : garde de collection de `ordered.any` niée. | `beurre de cacao sel` → `cocoa` et `salt`, sans `butter`; identique attendu. | `ordered` est une `List`. La recherche métier voisine, index 878, est tuée. | `MUTANT_EQUIVALENT_PROBABLE` |
| S12 | `match`, ligne 60/index 118 : suppression de la garde non-null sur `substring(...).trimStart()`. | `lait écrémé` → le suffixe `écrémé` est traité ; résultat actuel → même résultat attendu. | Les API `String` utilisées sont non nulles. Aucun effet observable. | `MUTANT_EQUIVALENT_PROBABLE` |
| S13 | `match`, ligne 117/index 1048 : suppression de la garde non-null sur `ingredients.values.toList()`. | `sel` → liste `[salt]`, actuel et attendu. | `Map.values.toList()` retourne une liste non nulle. Aucun risque métier. | `MUTANT_EQUIVALENT_PROBABLE` |
| S14 | `protectedPair`, ligne 125/index 7 : `source.endExclusive <= animal.start` devient `<`. | `cocoa butter` → `cocoa`, `butter` bloqué, `COVERED`; identique attendu. | Une égalité exigerait deux alias alphabétiques exactement adjacents sans caractère séparateur. Les regex à frontières de mots empêchent que les deux soient alors reconnus séparément. | `MUTANT_EQUIVALENT_PROBABLE` |
| S15 | `protectedPair`, ligne 128/index 31 : `source.start >= animal.endExclusive` devient `>`. | `beurre de cacao` → `cocoa`, `butter` bloqué, `COVERED`; identique attendu. | Même impossibilité d'adjacence exacte que S14. Le connecteur réel occupe au moins un caractère. | `MUTANT_EQUIVALENT_PROBABLE` |
| S16 | `protectedPair`, ligne 126/index 18 : suppression de garde non-null sur `substring(...).isBlank()`. | `cocoa butter` → protection valide, actuel et attendu. | `substring` est non nul pour les indices issus des correspondances regex. | `MUTANT_EQUIVALENT_PROBABLE` |
| S17 | `protectedPair`, ligne 129/index 45 : suppression de garde non-null sur `substring(...).trim()`. | `beurre de cacao` → connecteur `de` reconnu, actuel et attendu. | Même bruit Kotlin que S16. | `MUTANT_EQUIVALENT_PROBABLE` |
| S18 | `VerdictEngine.assessVeganCompatibility`, ligne synth. 50/index 23 : garde de collection du premier `any` niée. | `[VEGETARIAN]` → `NOT_VEGAN`, actuel et attendu. | `matched` est déclaré `List`. La mutation saute seulement l'optimisation `Collection.isEmpty`; l'itération donne la même réponse. L'index métier 27 est tué. | `MUTANT_EQUIVALENT_PROBABLE` |
| S19 | Même méthode, ligne synth. 53/index 99 : garde de collection du `any(UNCERTAIN)` niée. | `[UNCERTAIN]` → `UNCERTAIN`, actuel et attendu. | Même schéma ; la matrice teste vide, singleton et ordres, et l'index 103 est tué. | `MUTANT_EQUIVALENT_PROBABLE` |
| S20 | `VerdictEngine.evaluate`, ligne synth. 59/index 107 : garde de collection du `any(NON_VEGAN)` niée. | `[NON_VEGAN]` → `NON_VEGETARIAN`, actuel et attendu. | `considered` est toujours une `List`; seul le chemin rapide change. L'index 111 est tué. | `MUTANT_EQUIVALENT_PROBABLE` |
| S21 | `evaluate`, ligne synth. 62/index 179 : garde de collection du `any(UNCERTAIN)` niée. | `[UNCERTAIN]`, sans exclusion → `UNCERTAIN`, actuel et attendu. | Même équivalence de parcours ; l'index 183 est tué. | `MUTANT_EQUIVALENT_PROBABLE` |
| S22 | `evaluate`, ligne synth. 65/index 273 : garde de collection du `any(VEGETARIAN)` niée. | `[VEGAN, VEGETARIAN]` → `VEGETARIAN`, actuel et attendu. | Même équivalence ; l'index 277 est tué. | `MUTANT_EQUIVALENT_PROBABLE` |
| S23 | `evaluateVegetarianCompatibility`, ligne synth. 71/index 93 : garde de collection du `any(NON_VEGAN)` niée. | `[UNCERTAIN, NON_VEGAN]` → `NON_VEGETARIAN`, actuel et attendu. | Après `filter`, `considered` est une `List`; l'index 97 est tué et la matrice couvre les ordres et listes vides. | `MUTANT_EQUIVALENT_PROBABLE` |

## Synthèse des 3 mutants non couverts

Deux branches sont défensives ou générées et ne sont pas atteignables sous les
invariants de l'API et de la base actuelle. Le troisième expose une question de
diagnostic pour un alias végétal qui chevauche lexicalement un alias animal ;
le verdict reste correct dans les deux cas.

| ID | Localisation et mutation exacte | Exemple réaliste ; résultat actuel → attendu | Analyse du comportement et code | Catégorie |
|---|---|---|---|---|
| N01 | `isCovered`, ligne 163/index 7 : retour `false` forcé à `true` lorsque `selected` est vide. | `ingrédient mystère` → `NONE`, aucun ingrédient, texte inconnu ; identique attendu. | Depuis `match`, une sélection vide sans blocage retourne à la ligne 108 avant `isCovered`. Un blocage suppose une source végétale éligible, donc une sélection non vide avec la base actuelle ; `milk`, `butter` et `cream` sont tous `VEGETARIAN`. La garde défensive `false` est correcte. | `BRANCHE_NON_COUVERTE_JUSTIFIEE` |
| N02 | `match`, ligne synth. 268/index 497 : condition interne de `none { covered[it] }` niée. | Avec les alias `olive oil` et `oil`, `olive oil` → seul l'alias long est sélectionné, `EXACT`; c'est le résultat attendu. | Le comportement de chevauchement observable est déjà couvert par la priorité à l'alias long et l'autre condition de ce bloc (index 493) est tuée. L'index 497 correspond à un chemin de contrôle compilé sans branche métier distincte démontrée. Le code est correct au vu de l'invariant d'indices regex. | `BRANCHE_NON_COUVERTE_JUSTIFIEE` |
| N03 | `protectedPair`, ligne 131/index 57 : retour final `false` remplacé par `true` pour des plages qui se chevauchent. | Base hypothétique avec alias végétal exact `cocoa butter` et alias animal `butter` : actuel → source végétale `EXACT`, `blockedIngredientIds=[]`; mutant → source végétale `COVERED`, `blockedIngredientIds=[butter]`. Le verdict vegan reste identique. | La base actuelle représente `cocoa` séparément et supprime aussi plusieurs alias courts devant un alias lié plus long. Pour un futur alias végétal englobant, il faut décider si la trace doit signaler un faux candidat animal comme « bloqué » ou le laisser disparaître par priorité à l'alias long. Le code actuel est cohérent avec cette seconde lecture. | `REGLE_METIER_A_CLARIFIER` |

## Audit des risques métier demandés

- **Verdicts VEGAN/NON_VEGAN/INCERTAIN et arrêt après détection animale** : les
  six survivants sont des chemins rapides du bytecode. Les matrices actuelles
  couvrent statuts isolés, listes vides, permutations, inconnus, priorité du
  bloqueur et `excludeUncertain`. Aucun défaut n'est indiqué.
- **Normalisation, matching et listes vides** : S03 justifie éventuellement un
  test d'espaces neutres. Les autres cas restants ne changent pas le matching
  observable sous les invariants actuels.
- **Compositions imbriquées, pourcentages, origine, sections « peut contenir »,
  traces et multilingue** : aucune mutation résiduelle ne vise ces composants,
  car la campagne PIT ne cible que deux classes. Les tests d'architecture lus
  couvrent ces sujets, mais le score de 83 % ne permet pas de conclure sur leur
  résistance aux mutations.

## Défauts potentiels et règles ambiguës

Aucun `CODE_PRODUCTION_SUSPECT` n'est confirmé. Il n'existe donc aucune
correction de production justifiée par cette campagne.

La seule règle ambiguë est N03 : `blockedIngredientIds` doit-il mémoriser un
alias animal entièrement inclus dans un alias végétal exact ? Cette décision
affecte la résolution (`EXACT` ou `COVERED`) et la trace diagnostique, mais pas
le verdict final avec la priorité actuelle à l'alias long. Une décision
fonctionnelle est nécessaire avant d'ajouter un test ou de modifier le code.

## Mutants équivalents probables

S01, S02, S05–S23 (hors S03/S04) sont des équivalents probables. Les raisons
sont vérifiables dans le bytecode : récepteur toujours `Collection`, valeur
Kotlin contractuellement non nulle, ou égalité de borne rendue impossible par
la regex. Une exclusion PIT pourrait être étudiée plus tard pour les
`VoidMethodCallMutator` visant `Intrinsics.checkNotNullExpressionValue` et pour
les lignes synthétiques des fonctions inline. Une exclusion globale du
`NegateConditionalsMutator` masquerait de vraies mutations et n'est pas
recommandée.

S04 reste hors de cette liste tant que son instruction exacte et son effet ne
sont pas confirmés par une exécution isolée ou une inspection ASM plus fine.

## Tests encore éventuellement utiles

Un futur test métier peut comparer `(beurre de cacao)`, `( beurre de cacao )`
et `(beurre de cacao extra)`. Il doit vérifier ingrédients, blocages, résolution
et inconnu. Ce test vise S03 et exprime une règle réelle : espaces et
ponctuation ne doivent pas transformer une désignation végétale connue.

Un test pour N03 n'est utile qu'après clarification de la trace attendue. Aucun
test ne devrait être ajouté pour les contrôles non-null ou les chemins rapides
de collection.

## Recommandations

### A. Correction de production

Aucune correction à entreprendre sur la base de ces 26 mutants. Si une
régression observable apparaît, elle doit être reproduite indépendamment de
PIT avant tout changement.

### B. Clarification métier

Décider si un alias végétal exact englobant un mot animal doit produire une
entrée dans `blockedIngredientIds` et une résolution `COVERED` (N03), ou être
traité simplement comme le meilleur alias `EXACT`.

### C. Ajout futur de tests

Priorité secondaire : ajouter le cas d'espaces autour d'une expression
protégée (S03). Confirmer S04 mutation par mutation avant d'écrire un test ; ne
pas déduire un manque fonctionnel du seul statut `SURVIVED`.

### D. Exclusion ou absence d'action

- Ne rien faire pour S01, S02, S05–S23 : leur score n'apporte pas
  d'information métier supplémentaire.
- Conserver N01 et N02 comme branches justifiées tant que les invariants de la
  base et des candidats restent valides.
- Si le bruit gêne durablement la lecture, envisager une exclusion étroite des
  gardes Kotlin non-null et documenter la version du compilateur concernée.

## Limites

L'audit est statique et n'a exécuté ni test ni mutant isolé. PIT donne une
description générique pour les conditions niées ; l'identification des gardes
inline repose sur le XML, les paires d'indices, SMAP et le bytecode compilé.
S04 reste donc classé prudemment. Les exemples utilisant une base hypothétique
servent à rendre les effets observables sans affirmer qu'ils existent dans
`ingredients.json`. Enfin, les classes de parsing, hiérarchie, quantités,
origine, langues, sections et diagnostics sont hors du périmètre de mutation
actuel.

