# Analyse des mutants PIT survivants et non couverts

Date : 2026-09-25  
Branche : `mutation-testing-pit`

## Périmètre et synthèse

Cette analyse est fondée sur `mutation-core/build/reports/pitest/mutations.xml`,
les rapports HTML PIT, les deux classes ciblées et les tests existants. Aucun
code, test, asset ou fichier Gradle n'a été modifié.

- score de mutation actuel : **68 %** ;
- couverture de lignes des classes mutées : **287/291 (99 %)** ;
- mutants générés : **157** ;
- tués : **106** ;
- survivants : **41** ;
- non couverts : **10** ;
- classes ciblées : `IngredientMatcher` et `VerdictEngine`.

La couverture de lignes élevée ne garantit pas que chaque décision soit
vérifiée. Les survivants du verdict révèlent surtout l'absence d'une matrice
directe et exhaustive des statuts. Ceux du matcher révèlent des cas limites de
composition, de résidu et de protection d'expressions. Plusieurs mutations
touchent cependant le bytecode produit par les fonctions Kotlin inline ; leur
ligne PIT dépasse parfois la dernière ligne du fichier source. Ces lignes sont
notées « synthétiques » ci-dessous et doivent être confirmées avant d'écrire un
test uniquement pour les tuer.

Priorités : **P0** = risque de verdict incorrect ; **P1** = risque métier
important ; **P2** = amélioration secondaire ; **Équivalent probable** = effet
observable improbable ou bytecode de garde, à confirmer avant tout test.

## Mutants survivants

Chaque ligne représente un mutant PIT distinct. L'index bytecode permet de
distinguer les mutations ayant la même ligne et la même description.

| ID | Classe / méthode | Ligne PIT, index | Mutation | Pourquoi elle survit et importance | Classification | Test ciblé proposé |
|---|---|---:|---|---|---|---|
| S01 | `IngredientMatcher.<init>` | 46, 576 | `>` devient `>=` dans la recherche d'un alias lié plus long | `startsWith("$alias ")` impose déjà au moins un caractère supplémentaire ; l'égalité de longueur ne peut donc pas changer le résultat. | Équivalent probable | Vérification métamorphique avec alias identiques, préfixes stricts et alias de même longueur ; ne conserver le test que si un comportement diffère. |
| S02 | `IngredientMatcher.<init>` | synth. 245, 542 | condition de boucle Kotlin niée pendant la construction/déduplication des alias | Les tests utilisent peu de doublons entre nom, alias et numéro E et n'observent pas directement la table normalisée. Effet exact à confirmer sur bytecode. | P2 / équivalent possible | Base minimale où nom, alias accentué normalisé et `eNumber` convergent, puis vérifier une seule correspondance canonique stable. |
| S03 | `IngredientMatcher.match` | 60, 118 | suppression de `checkNotNullExpressionValue` sur `substring(...).trimStart()` | Les API utilisées ne renvoient pas `null`; retirer la garde ne change normalement aucune sortie. | Équivalent probable | Aucun test dédié avant confirmation par inspection du bytecode/outil Kotlin PIT. |
| S04 | `IngredientMatcher.match` | 108, 982 | négation d'une condition de `ingredients.isEmpty() && blocked.isEmpty()` | Les tests ne verrouillent pas directement `NONE` pour une base vide et ne combinent pas séparément listes sélectionnée/bloquée. Une mauvaise résolution peut changer la collecte des inconnus. | P1 | Matcher un texte inconnu avec base vide puis avec une base sans alias correspondant ; vérifier ingrédients, blocages, résidu, `NONE` et inconnu. |
| S05 | `IngredientMatcher.match` | 117, 1048 | suppression de la garde non-null sur `ingredients.values.toList()` | La collection Kotlin produite est non nulle ; mutation sans effet métier attendu. | Équivalent probable | Aucun test spécifique ; confirmer comme bruit Kotlin. |
| S06 | `IngredientMatcher.protectedPair` | 125, 7 | `source.endExclusive <= animal.start` devient `<` | Le cas d'adjacence exacte n'est pas isolé. Avec les frontières regex, deux alias adjacents sans séparateur sont probablement impossibles à détecter séparément. | Équivalent probable | Construire des alias synthétiques adjacents et séparés par espace ; vérifier si l'adjacence est réellement atteignable depuis `match`. |
| S07 | `IngredientMatcher.protectedPair` | 128, 31 | `source.start >= animal.endExclusive` devient `>` | Même lacune sur l'adjacence dans l'ordre animal puis source. Les cas actuels couvrent surtout `beurre de cacao`, sans verrouiller la frontière d'indices. | Équivalent probable | Cas synthétique animal/source adjacent, puis `beurre de cacao`, `beurre d cacao`, `beurre van cacao` et séparateur non autorisé. |
| S08 | `IngredientMatcher.protectedPair` | 129, 45 | suppression de garde non-null sur le `substring(...).trim()` | `substring` et `trim` sont non nuls pour des indices valides. | Équivalent probable | Aucun test dédié avant confirmation. |
| S09 | `IngredientMatcher.isCovered` | 184, 359 | suppression de garde non-null sur le préfixe extrait | Garde Kotlin sur une chaîne non nulle ; aucune différence attendue. | Équivalent probable | Aucun test dédié. |
| S10 | `IngredientMatcher.isCovered` | 185, 377 | suppression de garde non-null sur le suffixe extrait | Même bruit probable que S09. | Équivalent probable | Aucun test dédié. |
| S11 | `IngredientMatcher.isCovered` | 187, 401 | négation de `suffix.isBlank() || derivedSourceSuffix.matches(suffix)` | Les tests couvrent des suffixes acceptés, mais pas la paire négative proche. Une expression dérivée pourrait être déclarée entièrement couverte malgré un suffixe sémantique. | P1 | Comparer `purée d'abricot`, `purée d'abricot moulue` et `purée d'abricot sucrée`; vérifier `COVERED` pour les deux premières seulement et le résidu/inconnu pour la troisième. |
| S12 | `IngredientMatcher.match` | synth. 260, 278 | condition Kotlin niée dans la sélection/filtration des candidats | Le test de priorité couvre un chemin heureux, mais pas plusieurs occurrences, candidats bloqués et candidats superposés dans le même token. | P1 | Token contenant deux occurrences et un alias chevauchant, puis vérifier exactement IDs, ordre, résidu et blocages. |
| S13 | `IngredientMatcher.match` | synth. 282, 834 | condition de boucle/collection niée lors du traitement des candidats | Deux tests atteignent le bloc sans assertion assez discriminante sur tous les états intermédiaires. | P1 | Cas `beurre de cacao extra` et vrai `beurre` dans le même token ; vérifier `blockedIngredientIds`, sélection et résidu. |
| S14 | `IngredientMatcher.match` | synth. 282, 838 | seconde condition au même point généré | Même faiblesse que S13, sur une autre branche du bytecode inline. | P1 | Même scénario que S13, avec ordre inversé et occurrences répétées. |
| S15 | `IngredientMatcher.match` | synth. 284, 874 | condition de parcours/filtrage niée | Les sorties actuelles ne distinguent pas toutes les branches de sélection après conflit. | P1 | Ajouter un scénario à trois candidats : animal protégé, source végétale, ingrédient indépendant ; vérifier que seul l'animal protégé est écarté. |
| S16 | `IngredientMatcher.hasNoSemanticRemainder` | synth. 301, 206 | condition de boucle niée pendant le marquage des plages expliquées | Les deux tests couvrants n'opposent pas assez clairement résidu vide, ponctuation seule et mot supplémentaire. | P1 | Table `beurre de cacao`, `(beurre de cacao)`, `beurre de cacao extra` ; vérifier respectivement couverture, couverture, conflit/partiel. |
| S17 | `IngredientMatcher.isCovered` | synth. 315, 205 | condition Kotlin niée dans `split/filter/all` des mots de liaison | Beaucoup de tests traversent ce code, mais leurs assertions finales ne séparent pas mot de liaison autorisé et mot sémantique. | P1 | Comparer `huile de colza`, `huile xyz colza` et `huile de colza et`; vérifier résolution et inconnu exacts. |
| S18 | `IngredientMatcher.isCovered` | synth. 316, 240 | condition de filtrage des fragments vides niée | Les cas actuels ne testent pas les résidus faits d'espaces multiples et de mots mélangés. | P2 | Résidus avec espaces répétés, uniquement `de/et`, puis `de mystère`; vérifier `COVERED` contre `PARTIAL_CONTEXTUAL`. |
| S19 | `IngredientMatcher.isCovered` | synth. 318, 268 | condition de `all { it in glueWords }` niée | Le chemin négatif proche n'est pas asserté avec assez de précision. | P1 | Base à un ingrédient et expressions ne différant que par `de` versus un nom inconnu ; vérifier résidu et collecte inconnue. |
| S20 | `VerdictEngine.assessVeganCompatibility` | 9, 56 | condition d'itération de `any` niée pour VEGETARIAN/NON_VEGAN | Les tests indirects combinent plusieurs statuts ; ils ne vérifient pas chaque position ni singleton. Une détection manquée donne potentiellement `VEGAN`. | P0 | Matrice directe : singleton VEGETARIAN, singleton NON_VEGAN, bloqueur en première/dernière position, avec et sans UNCERTAIN. |
| S21 | `VerdictEngine.assessVeganCompatibility` | 11, 132 | condition inline de `any(UNCERTAIN)` niée | Seuls deux scénarios complexes couvrent cette zone ; un singleton UNCERTAIN n'est pas verrouillé directement. | P0 | Appel direct avec `[UNCERTAIN]`, inconnu vide : résultat `UNCERTAIN`. |
| S22 | `VerdictEngine.assessVeganCompatibility` | 11, 151 | autre branche de contrôle de `any(UNCERTAIN)` | Même manque de matrice ; le mutant peut altérer entrée/sortie de boucle selon taille/position. | P0 | `[VEGAN, UNCERTAIN]` puis `[UNCERTAIN, VEGAN]`, tous deux `UNCERTAIN`. |
| S23 | `VerdictEngine.assessVeganCompatibility` | 11, 155 | condition de parcours de la collection niée | Les collections vide, singleton et multiéléments ne sont pas testées directement ensemble. | P0 | Cas `[]`, `[VEGAN]`, `[VEGAN, VEGAN]`, `[VEGAN, UNCERTAIN]` avec assertions exactes. |
| S24 | `VerdictEngine.assessVeganCompatibility` | 11, 164 | condition de comparaison au statut UNCERTAIN niée | Les tests couvrants possèdent d'autres causes possibles d'incertitude, masquant cette décision. | P0 | `[UNCERTAIN]` sans inconnus contre `[VEGAN]` sans inconnus ; sorties `UNCERTAIN` et `VEGAN`. |
| S25 | `VerdictEngine.assessVeganCompatibility` | 11, 167 | branche de sortie de `any` niée | Un seul test couvre ce point et son contexte peut rester incertain pour une autre raison. | P0 | `[VEGAN, UNCERTAIN, VEGAN]` sans inconnus, puis retirer UNCERTAIN et comparer. |
| S26 | `VerdictEngine.evaluateVegetarianCompatibility` | 41, 50 | condition de boucle du filtre qui retire UNCERTAIN niée | La méthode est surtout testée via l'orchestrateur ; aucun test unitaire ne verrouille la liste filtrée conceptuellement. | P0 | `[UNCERTAIN, VEGAN]`, `[UNCERTAIN, NON_VEGAN]` et `[UNCERTAIN]`; résultats VEGETARIAN, NON_VEGETARIAN, INCONCLUSIVE. |
| S27 | `VerdictEngine.evaluateVegetarianCompatibility` | 43, 126 | condition inline de `any(NON_VEGAN)` niée | Les trois appels couvrants ne forment pas une matrice d'ordre et de cardinalité. | P0 | NON_VEGAN singleton et placé au début/à la fin d'une liste vegan. |
| S28 | `VerdictEngine.evaluateVegetarianCompatibility` | 43, 145 | autre branche de parcours de `any(NON_VEGAN)` | Même cause que S27. | P0 | `[VEGAN, NON_VEGAN, VEGAN]` doit rester NON_VEGETARIAN. |
| S29 | `VerdictEngine.evaluateVegetarianCompatibility` | 44, 153 | négation de `considered.isEmpty()` | L'absence de valeur certaine n'est pas testée directement, notamment liste vide et liste tout UNCERTAIN. | P0 | `[]` et `[UNCERTAIN]` doivent produire INCONCLUSIVE ; `[VEGAN]` doit produire VEGETARIAN. |
| S30 | `VerdictEngine.assessVeganCompatibility` | synth. 50, 23 | garde `Collection.isEmpty` de `any` niée | Bytecode inline ; peut être redondant avec la sémantique de l'itérateur, mais une collection vide est métierment importante. | P0 / équivalent possible | Test direct `matched=[]`, `unknown=[]` => UNCERTAIN, et `unknown=[x]` => UNCERTAIN ; confirmer ensuite le mutant. |
| S31 | `VerdictEngine.assessVeganCompatibility` | synth. 53, 99 | condition `hasNext` de la première recherche niée | Les bloqueurs ne sont pas testés systématiquement selon leur position. | P0 | Permutations de `[VEGAN, VEGETARIAN]` et `[VEGAN, NON_VEGAN]`; toujours NOT_VEGAN. |
| S32 | `VerdictEngine.assessVeganCompatibility` | synth. 53, 103 | condition de correspondance VEGETARIAN/NON_VEGAN niée | Une autre valeur bloquante dans les scénarios actuels peut masquer la mutation. | P0 | Singletons de chacun des quatre statuts, sans inconnu. |
| S33 | `VerdictEngine.assessVeganCompatibility` | synth. 54, 116 | condition de sortie de boucle niée | Mutation de contrôle Kotlin potentiellement redondante, mais touche une règle de verdict critique. | P0 / équivalent possible | Cas à 0, 1 et 3 ingrédients avec bloqueur uniquement en dernière position. |
| S34 | `VerdictEngine.assessVeganCompatibility` | synth. 54, 141 | condition de boucle de la recherche UNCERTAIN niée | Les scénarios complexes fournissent parfois aussi des inconnus, ce qui masque l'effet. | P0 | UNCERTAIN en début/fin, sans aucune chaîne inconnue. |
| S35 | `VerdictEngine.evaluate` | synth. 59, 107 | condition inline de `any(NON_VEGAN)` niée | La priorité globale est testée, mais pas toutes les positions/singletons ; une autre cause peut préserver le même verdict. | P0 | Matrice directe de `evaluate` avec NON_VEGAN seul et entouré de VEGAN/UNCERTAIN, `excludeUncertain` vrai/faux. |
| S36 | `VerdictEngine.evaluate` | synth. 62, 179 | condition inline de `any(UNCERTAIN)` niée | Le test existant couvre un cas, sans paire négative ni permutation. | P0 | `[UNCERTAIN]`, `[VEGAN, UNCERTAIN]`, `[VEGAN]`, inconnus vides, avec `excludeUncertain=false`. |
| S37 | `VerdictEngine.evaluate` | synth. 65, 273 | condition inline de `any(VEGETARIAN)` niée | Le verdict VEGETARIAN est couvert dans une combinaison, pas pour chaque position. | P0 | VEGETARIAN singleton et placé aux deux extrémités avec VEGAN ; vérifier VEGETARIAN. |
| S38 | `VerdictEngine.evaluateVegetarianCompatibility` | synth. 69, 59 | garde de collection du filtre niée | Bytecode Kotlin du filtrage ; effet possiblement redondant, mais le cas vide manque. | P0 / équivalent possible | Liste vide, tout UNCERTAIN et liste mixte, appels directs. |
| S39 | `VerdictEngine.evaluateVegetarianCompatibility` | synth. 71, 93 | condition de boucle du filtre niée | Les éléments avant/après UNCERTAIN ne sont pas systématiquement testés. | P0 | Permuter UNCERTAIN avec VEGAN et NON_VEGAN, vérifier résultat invariant à l'ordre. |
| S40 | `VerdictEngine.evaluateVegetarianCompatibility` | synth. 71, 97 | condition `status != UNCERTAIN` niée | Risque de conserver les incertains ou d'écarter les certains ; les résultats indirects peuvent masquer ce changement. | P0 | `[UNCERTAIN, VEGAN]` => VEGETARIAN et `[UNCERTAIN, NON_VEGAN]` => NON_VEGETARIAN. |
| S41 | `VerdictEngine.evaluateVegetarianCompatibility` | synth. 72, 135 | condition de contrôle lors de la recherche NON_VEGAN niée | Un chemin Kotlin généré survit faute de matrice directe ; possible redondance à confirmer après tests métier. | P0 / équivalent possible | NON_VEGAN seul, puis au milieu de plusieurs valeurs certaines après filtrage d'UNCERTAIN. |

## Mutants non couverts

| ID | Classe / méthode | Ligne PIT, index | Mutation et raison probable de non-couverture | Priorité | Test recommandé |
|---|---|---:|---|---|---|
| N01 | `IngredientMatcher.protectedPair` | 126, 18 | garde non-null supprimée sur le `substring` du chemin « source avant animal ». Aucun test n'emprunte cet ordre. La garde elle-même est probablement sans effet. | Équivalent probable | Atteindre l'ordre source→animal avec une base synthétique, puis décider si la garde mérite d'être ignorée. |
| N02 | `IngredientMatcher.protectedPair` | 126, 23 | retour du chemin source→animal forcé à `true`. Ce chemin n'est jamais exercé. | P1 | Expression où une source végétale précède immédiatement un alias animal, avec espace seulement puis mot intermédiaire ; vérifier protection dans le premier cas seulement. |
| N03 | `IngredientMatcher.protectedPair` | 126, 23 | même retour forcé à `false`. | P1 | Même paire de tests que N02, avec assertion sur `blockedIngredientIds` et ingrédients retenus. |
| N04 | `IngredientMatcher.protectedPair` | 131, 57 | retour final forcé à `true`, correspondant aux candidats chevauchants ou aux ordres non reconnus. Le chemin paraît difficile, voire impossible, avec les frontières regex. | Équivalent probable | Chercher d'abord un couple d'alias chevauchants atteignable ; si aucun n'existe, documenter l'inaccessibilité plutôt qu'ajouter un test artificiel. |
| N05 | `IngredientMatcher.hasNoSemanticRemainder` | 146, 301 | condition `isWhitespace()` non couverte séparément dans le prédicat final. | P1 | Protection avec reste composé uniquement d'espaces, puis avec ponctuation et avec lettre ; vérifier `COVERED` seulement pour les deux premiers. |
| N06 | `IngredientMatcher.hasNoSemanticRemainder` | 146, 306 | condition `character in punctuation` non couverte séparément. | P1 | Cas entourés de `()`, `[]`, virgule, point et tiret, puis caractère non autorisé. |
| N07 | `IngredientMatcher.isCovered` | 163, 7 | retour `false` quand `selected` est vide forcé à `true`. Depuis `match`, les invariants semblent empêcher d'atteindre ce point avec une sélection vide. | Équivalent probable / inaccessible | Tenter un cas avec tous les candidats bloqués ; si une source vegan reste toujours sélectionnée, classer la branche inaccessible depuis l'API publique. |
| N08 | `IngredientMatcher.match` | synth. 268, 497 | condition de boucle du tri/sélection d'alias non exécutée, probablement une branche de collection vide ou terminale générée. | P2 / équivalent possible | Base vide, base avec alias absent, puis plusieurs alias de même longueur ; vérifier sortie stable et réexaminer la couverture bytecode. |
| N09 | `IngredientMatcher.hasNoSemanticRemainder` | synth. 306, 317 | condition de boucle générée non couverte pour une des plages expliquées. | P1 | Cas protégé où source et animal sont dans chaque ordre, séparés par connecteur, avec ponctuation en préfixe/suffixe. |
| N10 | `IngredientMatcher.hasNoSemanticRemainder` | synth. 306, 322 | incrément `+1` remplacé par `-1` dans la même boucle non parcourue. | P1 | Même test que N09 avec une plage de plusieurs caractères afin que l'incrément soit observable. |

## Mutants équivalents probables

Les candidats les plus solides sont S01, car `startsWith("$alias ")` implique
strictement une chaîne plus longue, ainsi que S03, S05, S08, S09, S10 et N01,
qui retirent des contrôles non-null insérés par Kotlin autour d'API non nulles.
S06 et S07 semblent neutralisés par les frontières de mots des regex. N04 et
N07 paraissent inaccessibles depuis `match` selon les invariants actuels.

Les lignes synthétiques marquées « équivalent possible » ne doivent pas être
exclues sur la seule base du numéro de ligne. Il faut d'abord exécuter les tests
métier proposés, puis examiner le bytecode restant. PIT ne prouve pas
l'équivalence.

## Faiblesses importantes et recommandations par domaine

### Verdict vegan, non vegan et incertain

La faiblesse principale est l'absence de tests unitaires directs en table pour
les trois fonctions de `VerdictEngine`. Les tests d'intégration combinent
souvent plusieurs causes du même verdict ; une condition mutée peut donc être
masquée par une autre. Priorité **P0** : couvrir chaque statut seul, toutes les
priorités, les listes vides, les inconnus, `excludeUncertain`, puis permuter la
position du statut décisif.

### Normalisation et matching

Priorité **P1** : distinguer explicitement résidu vide, mots de liaison seuls et
résidu sémantique ; vérifier ensemble `resolution`, IDs retenus,
`blockedIngredientIds`, résidu et sortie d'`UnknownCollector`. Les expressions
dérivées doivent avoir une paire positive/négative ne différant que par le
suffixe.

### Hiérarchie et compositions

Priorité **P1** : tester les expressions protégées dans les deux ordres
source/animal, avec connecteur autorisé, connecteur refusé, mot supplémentaire,
ponctuation et ingrédient indépendant. Ces tests doivent passer par l'API
publique du matcher ou du service, sans appeler les méthodes privées.

### Parsing

Aucun mutant de `IngredientTreeParser` n'a été produit dans cette campagne,
car la classe n'était pas ciblée. Les recommandations de composition ci-dessus
traversent le parser lorsqu'elles sont écrites au niveau service, mais ne
mesurent pas sa résistance mutationnelle. Priorité **P2** : campagne PIT dédiée
ultérieure, limitée et séparée.

### Quantités et pourcentages

Aucun mutant de `QuantityCleaner` ou du parsing des pourcentages n'est présent.
Les tests existants ne peuvent donc pas être évalués à partir de ces 51
résultats. Priorité **P2** : ne proposer une amélioration qu'après une campagne
ciblant ces classes.

### Langues et sections

Aucun mutant de segmentation ou d'extraction de sections n'est présent. Les
tests correspondants servent seulement de tests couvrants indirects du
matcher/verdict. Priorité **P2** : future campagne distincte, sans déduire une
faiblesse de cette campagne.

### Diagnostics

`DiagnosticReport` n'était pas ciblé. Pour les futurs tests **P0/P1**, vérifier
d'abord les objets métier (`VeganAssessment`, `AnalysisVerdict`, match et
inconnus) ; ajouter une assertion de diagnostic seulement lorsqu'elle protège
une information destinée à l'utilisateur, afin d'éviter des tests couplés au
formatage.

## Ordre conseillé pour une future campagne

1. **P0 — Verdict direct** : table exhaustive de `assessVeganCompatibility`,
   `evaluate` et `evaluateVegetarianCompatibility`, incluant permutations et
   listes vides.
2. **P1 — Expressions protégées** : source/animal dans les deux ordres,
   connecteurs, résidu sémantique et ponctuation.
3. **P1 — Couverture du matcher** : mots de liaison, suffixes dérivés, candidats
   superposés/répétés et assertions complètes sur la résolution.
4. Rejouer exactement la campagne à deux classes et comparer les identifiants
   ligne/index, pas seulement le score global.
5. Examiner manuellement les survivants Kotlin restants et documenter les
   équivalents probables avant toute exclusion PIT.
6. Seulement ensuite, envisager des campagnes séparées pour parsing,
   quantités, langues/sections et diagnostics.

## Limites

L'analyse est statique : aucun mutant individuel n'a été injecté dans le code
source et aucun test proposé n'a été implémenté. Les numéros de ligne
synthétiques proviennent des fonctions Kotlin inline et ne correspondent pas à
des lignes réelles des fichiers. L'effet exact de ces mutants demanderait une
exécution mutation par mutation ou une désactivation expérimentale des autres
mutants, à réaliser lors d'une future phase autorisée. La campagne ne permet
aucune conclusion sur les classes non ciblées. Les qualifications « équivalent
probable » restent des hypothèses argumentées, pas des preuves formelles.

## État Git

L'arbre était propre au début de l'analyse. Le seul changement attendu à la fin
est ce rapport. Aucun commit ni push n'a été effectué.
