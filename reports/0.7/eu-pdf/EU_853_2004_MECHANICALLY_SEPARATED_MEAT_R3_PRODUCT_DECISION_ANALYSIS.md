# Analyse et décision produit de R3 — viande séparée mécaniquement

Analyse initiale et décision produit : 7 octobre 2026. Mise à jour : consigner la décision explicite de Sébastien, responsable produit, de retenir **l'option A — conserver les quatre formes et accepter le risque R3 documenté**. Cette décision accepte le comportement actuel pour ce lot ; elle ne démontre pas sa sûreté sémantique générale et ne constitue pas une validation juridique.

Le risque est reproduit dans le pipeline courant. Il dépasse le seul exemple manuel : une négation placée après une composition peut déclencher `NON_VEGETARIAN` dans les modes texte d'étiquette et texte OCR. Une négation placée avant l'en-tête peut être écartée. Les exemples construits NL/EN/DE exposent également une absence de protection contextuelle. La fréquence et l'impact en conditions réelles restent inconnus.

## État observé lors de l'analyse initiale et périmètre

- Branche : `master`, affichée `master...origin/master` par Git local, sans interrogation réseau.
- HEAD initial et final : `5a3a352dbecf3af68fabe0b25cb2cced238d8517`.
- Sujet du commit : `fix(knowledge): add mechanically separated meat with runtime guards`.
- Départ : index et modifications suivies vides ; aucun fichier non suivi signalé.
- L'intégration et les corrections sont donc déjà présentes dans HEAD. L'état décrit par les anciennes revues avant commit n'est plus celui du dépôt. La décision produit désormais consignée accepte le lot courant avec son risque documenté ; aucune opération sur l'historique n'est proposée ou exécutée ici.
- Fin : seul ce nouveau rapport est non suivi ; index inchangé. Les 415 fichiers suivis/non ignorés présents au départ ont conservé exactement leurs empreintes SHA-256. Aucun changement des JSON, actifs, code, tests, sources, version ou rapports antérieurs.
- Aucun fichier fourni n'a été supprimé ou déplacé. Les seuls fichiers temporaires créés hors dépôt pour les sondes ont été nettoyés par leur propre contexte temporaire.

Les quatre surfaces sont toujours celles annoncées. Dans `knowledge/ingredients.json:8570`, la forme FR est le **nom canonique** du concept, les trois autres sont ses alias ; le statut est `NON_VEGAN`. Le test runtime confirme les quatre mappings, le bon propriétaire et la conversion vers `NON_VEGETARIAN`. Aucun sigle, espèce, greaves ou frog_legs n'est traité par cette tâche.

## Références confrontées au dépôt

Instructions consultées : `AGENTS.md`, skills `knowledge-import-validation` et `android-validation`, références `source-adapters.md` et `import-workflow.md`, invariants et documentation `docs/knowledge-pipeline.md`, `docs/matching.md`, `docs/parsing.md`, `docs/verdict.md`, `docs/diagnostic.md` et `docs/ocr.md`.

Rapports lus, dans `reports/0.7/eu-pdf/` :

- `EU_853_2004_MECHANICALLY_SEPARATED_MEAT_INTEGRATION_REPORT.md`
- `EU_853_2004_MECHANICALLY_SEPARATED_MEAT_INDEPENDENT_REVIEW_REPORT.md`
- `EU_853_2004_MECHANICALLY_SEPARATED_MEAT_REVIEW_CORRECTIONS_REPORT.md`
- `EU_853_2004_MECHANICALLY_SEPARATED_MEAT_INDEPENDENT_REREVIEW_REPORT.md`

Ces rapports servent à identifier R3 et les affirmations à vérifier. Les comportements ci-dessous proviennent de la lecture du code actuel et des exécutions de cette analyse, et non de leurs seuls verdicts. R1/R2 ne sont pas réaudités : leur validation technique dans la seconde revue reste distincte de la décision produit.

Fichiers de code et de test examinés :

| Zone | Fichiers / emplacements pertinents |
|---|---|
| Entrée Android active | `app/src/main/java/com/example/isitvegan/MainActivity.kt:28`, `OcrFirstScreen.kt:37`, `:89`, `:132`, `:180`, `:200`, `AndroidVeganAnalyzer.kt:21`, `AndroidIngredientKnowledgeLoader.kt:7` |
| Acquisition et édition OCR | `OcrProcessor.kt:78`, `OcrTextCleaner.kt:4`, `OcrSession.kt:15`, dans le même répertoire Android |
| Sélection, présence, parcours et verdict | `mutation-core/src/main/kotlin/com/example/isitvegan/VeganAnalyzer.kt:249`, `:261`, `:289`, `:350`, `:451`, `:484` |
| Sections et parsing | `LabelSectionExtractor.kt:95`, `:196`, `:283`, `LabelPreprocessor.kt:79`, `IngredientTreeParser.kt:297`, dans le répertoire Kotlin du core |
| Résolution | `MultilingualIngredientLexicon.kt:60`, `TextNormalizer.kt:6`, `OcrIngredientNormalizer.kt:4`, `IngredientMatcher.kt:137`, `:174`, `:204`, `:231`, `UnknownCollector.kt:13` |
| Décision et affichage | `VerdictEngine.kt:27`, `DiagnosticModels.kt:107`, `:120`, `:200` ; `app/src/main/java/com/example/isitvegan/VerdictExplanationFormatter.kt:40`, `:81`, `:102` |
| Test existant et connaissance effective | `app/src/test/java/com/example/isitvegan/FoodHygieneRegulationImportTest.kt:16`, `:25`, `:47`, `:80`, `:97` ; `knowledge/ingredients.json` et les trois actifs lus par le chargeur |

## Chemin réel et interprétation de R3

L'écran actuellement monté par MainActivity est OcrFirstScreen. Il permet la saisie de texte complet, la liste seule et le texte OCR éditable. L'analyse transmet le mode, la langue préférée et, dans certaines sélections OCR, l'identifiant de bloc au même service métier. La simple présence d'une chaîne dans un écran ne suffit donc pas à prédire son verdict : la sélection du bloc et des sections compte.

Pour une photo, OcrProcessor utilise ML Kit, conserve l'ordre natif de lecture, nettoie le texte et construit une sélection multilingue avant l'édition et l'analyse. Ces étapes d'acquisition n'ont pas été exécutées ici. Les observations en `OCR_LABEL` concernent du **texte directement injecté au service**, sans image, nettoyage OCR préalable, coordonnées, sélection explicite d'un bloc par l'utilisateur ni test de l'interface Android.

Dans le core, la segmentation et l'extraction des sections précèdent le parsing. Une liste explicite peut être analysée dans tous les modes ; le mode manuel permet aussi une liste sans en-tête. Certains textes suffisamment structurés sont reconnus comme listes implicites dans les modes étiquette/OCR. Un nom isolé ne devient pas automatiquement une composition dans ces deux modes.

Le lexique multilingue normalise les surfaces et applique ses corrections OCR selon la langue choisie. Le matcher général indexe également le nom et tous les alias des ingrédients, sans filtre linguistique sur cet index. TextNormalizer enlève notamment casse, accents et ponctuation ; il ne transforme pas le singulier en pluriel. Les frontières de mots et le choix des expressions longues empêchent plusieurs rapprochements textuels abusifs, mais ne donnent pas un sens positif ou négatif à une phrase.

Les protections contextuelles existantes ciblent certaines expressions et certains ingrédients. Il n'existe pas de protection générale de ce concept pour `sans`, `ne contient pas`, `zonder`, `no` ou `ohne`. Lorsque l'expression est reconnue au milieu d'un token, `PARTIAL_CONTEXTUAL` conserve à la fois l'ingrédient reconnu et un résidu. UnknownCollector conserve le token inconnu ; cette incertitude n'annule pas l'ingrédient reconnu. À l'inverse, `EXACT` dit que le token est couvert lexicalement, pas que la phrase exprime une présence réelle : C13 sépare `sans` du token exact suivant.

Les ingrédients reconnus sont ajoutés au résultat. VerdictEngine donne priorité à un statut ingrédient `NON_VEGAN` sur les inconnus : le verdict détaillé devient `NON_VEGETARIAN` lorsque la composition est analysable. Le diagnostic identifie alors `mechanically_separated_meat` comme responsable. Le moteur ne s'arrête pas au premier animal : la boucle poursuit l'analyse, les inconnus restent dans le résultat et `stoppedAtNonVegetarian` vaut false.

Sans liste analysable, le verdict détaillé est null. Une présence extraite peut néanmoins rendre l'évaluation `NOT_VEGAN`. Le formateur Android prévoit alors un message d'absence de composition et une indication de présence déclarée, pas le même affichage qu'un verdict détaillé `NON_VEGETARIAN`. Cette distinction est essentielle pour C03 et C05. La détection du marqueur positif `contient` dans `ne contient pas` crée justement une présence extraite erronée dans C03/C21.

Enfin, `decision.responsibleIngredientIds` n'est pas toujours une liste de bloqueurs : pour une décision VEGAN il contient les ingrédients végétaux responsables ; pour NO_INGREDIENT_LIST il est vide. Il faut consulter la raison de décision et les références `knownBlockingIngredients`, et ne pas appeler tout ID responsable un coupable.

## Exécutions de l'analyse initiale et effets

Les tests et sondes ci-dessous ont été exécutés lors de l'analyse initiale. Ils n'ont pas été relancés pour la mise à jour documentaire après décision produit ; les vérifications de cette mise à jour sont consignées en fin de rapport.

| Commande / méthode exécutée | Résultat et effet |
|---|---|
| `git status --short --branch`, `git rev-parse HEAD`, inspection de l'index/diffs et `git log -1` | État initial propre, commit indiqué ci-dessus. Lecture seule ; aucune opération réseau. |
| `rg`, `rg --files`, `Get-Content` ciblés sur instructions, rapports, code et tests | Lecture locale des chemins réels ; aucune modification. Les sondes et la relecture du rapport utilisent explicitement UTF-8. |
| `.\gradlew.bat --offline :app:testDebugUnitTest --tests com.example.isitvegan.FoodHygieneRegulationImportTest` | **BUILD SUCCESSFUL**, environ 5 s ; 5 tests, 0 échec, 0 erreur, 0 ignoré. JAVA_HOME du processus : `C:\Program Files\Android\Android Studio\jbr`. Seuls des résultats de build/test ignorés sont écrits. Aucun générateur de connaissance exécuté. |
| Lecture de `app/build/test-results/testDebugUnitTest/TEST-com.example.isitvegan.FoodHygieneRegulationImportTest.xml` | Réexécution corroborée : timestamp `2026-10-07T17:59:34.425Z`, durée des tests 1,896 s. Il s'agit de cette exécution, pas d'un résultat ancien revendiqué comme nouveau. |
| Inspection ciblée des API avec `javap.exe` ; sonde orchestrée par `python -B -X utf8 -` | Fixture Java créée uniquement dans un répertoire temporaire système, appels du vrai code Kotlin sur les actifs courants, sans modification des tests du dépôt. |
| `java.exe -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 --class-path <mutation-core.jar>;<kotlin-stdlib-2.4.20.jar en cache> <temp>/R3Probe.java` | Retour 0 ; **97 observations** JSON décodées : C01–C31 × trois modes, plus C32 × deux modes × deux préférences linguistiques. Pas 97 assertions JUnit ni un benchmark d'étiquettes. Le source temporaire a été supprimé à la fermeture du contexte temporaire. |
| SHA-256 par Python de chaque fichier suivi/non ignoré, avant/après ; `git diff --check`, vérification finale Git | Les 415 fichiers initiaux sont identiques ; seul le rapport demandé est ajouté. Diff-check sans erreur ; aucun fichier indexé. |

La sonde charge les trois actifs `ingredients.json`, `ingredient_aliases_multilingual.json` et `origin_qualifier_rules.json` par `IngredientKnowledge.fromJson`, puis utilise `IngredientAnalysisService.analyzeWithDiagnostics(text, mode, UiLanguage, null)`. Elle relève disponibilité, sections, tokens, concepts reconnus, inconnus, verdict et IDs responsables. Un appel séparé au vrai `IngredientMatcher.match(IngredientToken(...))` relève la résolution et le résidu sur le texte utilisé pour matcher ; les résultats sur le texte brut entier sont distingués de ceux du pipeline.

Les 31 entrées exactes permettant de reproduire la sonde figurent ci-dessous. Préférence FR par défaut, sans identifiant de bloc imposé ; C32 teste FR et EN. Le JAR du core est celui disponible après la validation Gradle ciblée, qui a validé les tâches nécessaires à jour. Aucun résultat d'une approximation Python du matcher n'est utilisé.

Limites/outils : une première tentative avec JShell n'a pas pu démarrer, cet outil/module étant absent du JBR. Une première capture Java a rencontré un décodage de sortie Windows incompatible ; elle n'est pas utilisée comme preuve. La sonde finale utilise des chaînes Unicode JSON échappées et une sortie UTF-8 explicite. Aucune installation n'a été faite.

Non exécutés : importeur, dry-run d'import, builder/générateur de connaissance, suite Gradle complète, PIT, tests R1/R2, instrumentation Android, Nokia, acquisition OCR sur image, revue de fréquence sur étiquettes, nouveau rendu PDF, recherche externe, téléchargement ou contrôle de licence.

Les cinq tests JUnit réellement passés contrôlent : les quatre formes exactes et leurs bloqueurs ; les voisins et le propriétaire historique `meat` ; les deux contre-exemples négation/imitation ; la priorité animale avec inconnus sans arrêt de lecture ; des normalisations ciblées. Le test caractérisant les contre-exemples **ne démontre pas leur sécurité sémantique**.

## Cas observés

Légende : M = `MANUAL_INGREDIENT_LIST`, F = `FULL_LABEL`, O = `OCR_LABEL` **sur texte injecté**. E = EXACT, P = PARTIAL_CONTEXTUAL, N = NONE ; MS = `mechanically_separated_meat`. NV = NON_VEGETARIAN, V = VEGAN, I = INCONCLUSIVE. ∅ = verdict détaillé null, NO_INGREDIENT_LIST ; ce n'est ni VEGAN ni une résolution NONE.

La résolution affichée est celle du token MS (ou meat) lorsqu'il existe ; les autres tokens peuvent être exacts ou inconnus. Chaque ligne C01–C31 correspond à trois observations réellement exécutées. Toutes les entrées sont **construites**, sans produit réel ni preuve d'usage/fréquence.

| Cas | Entrée exacte (`↵` = retour à la ligne) | M : résolution / verdict | F : résolution / verdict | O : résolution / verdict | Preuve observée / limite |
|---|---|---|---|---|---|
| C01 | `viandes séparées mécaniquement` | E→MS / NV | non analysé / ∅ | non analysé / ∅ | Nom de base, aucun résidu. |
| C02 | `sans viandes séparées mécaniquement` | P→MS / NV | non analysé / ∅ | non analysé / ∅ | Résidu « sans » ; token entier inconnu, MS bloque. |
| C03 | `ne contient pas de viandes séparées mécaniquement` | P→MS / ∅ ; NOT_VEGAN | P→MS / ∅ ; NOT_VEGAN | P→MS / ∅ ; NOT_VEGAN | « contient » extrait une présence « pas de… » ; pas de liste complète. |
| C04 | `peut contenir des traces de viandes séparées mécaniquement` | non analysé / ∅ | non analysé / ∅ | non analysé / ∅ | Une trace séparée, aucun ingrédient MS. |
| C05 | `contient : viandes séparées mécaniquement` | E→MS / ∅ ; NOT_VEGAN | E→MS / ∅ ; NOT_VEGAN | E→MS / ∅ ; NOT_VEGAN | Présence explicite, composition absente. |
| C06 | `Ingrédients : eau, viandes séparées mécaniquement, sel.` | E→MS / NV | E→MS / NV | E→MS / NV | Composition positive construite, aucun inconnu. |
| C07 | `imitation végétale de viandes séparées mécaniquement` | P→MS / NV | non analysé / ∅ | non analysé / ∅ | Description construite ; résidu « imitation vegetale de ». |
| C08 | `Les viandes sont séparées puis traitées mécaniquement.` | N / I | non analysé / ∅ | non analysé / ∅ | Mots dispersés, aucune dénomination contiguë. |
| C09 | `Notice : le terme viandes séparées mécaniquement figure dans ce règlement.` | P→MS / NV | non analysé / ∅ | non analysé / ∅ | Citation reconnue dans le mode manuel, pas une preuve de présence. |
| C10 | `viande séparée mécaniquement` | P→meat / NV | non analysé / ∅ | non analysé / ∅ | Le singulier ne résout pas MS ; propriétaire historique meat. |
| C11 | `viandes-séparées-mécaniquement` | E→MS / NV | non analysé / ∅ | non analysé / ∅ | Normalisation des tirets ; pas d’ajout d’alias. |
| C12 | `viandes, séparées, mécaniquement` | N / I | non analysé / ∅ | non analysé / ∅ | Trois tokens N ; le matcher sur texte brut seul donnerait E→MS. |
| C13 | `sans : viandes séparées mécaniquement` | E→MS / NV | non analysé / ∅ | non analysé / ∅ | Deux tokens : « sans » N et dénomination E ; négation séparée. |
| C14 | `Ingrédients : eau, sucre. Sans viandes séparées mécaniquement.` | P→MS / NV | P→MS / NV | P→MS / NV | Négation après composition retenue ; résidu « sans ». |
| C15 | `Sans viandes séparées mécaniquement.↵Ingrédients : eau, sucre.` | E (végétaux) / V | E (végétaux) / V | E (végétaux) / V | Négation avant en-tête écartée, seuls eau/sucre. |
| C16 | `Ingrédients : eau, sucre. Peut contenir des traces de viandes séparées mécaniquement.` | E (végétaux) / V | E (végétaux) / V | E (végétaux) / V | Une trace exclue ; seuls eau/sucre. |
| C17 | `eau, sucre, sel, amidon, sans viandes séparées mécaniquement` | P→MS / NV | P→MS / NV | P→MS / NV | Liste implicite détectée aussi en F/O ; résidu « sans ». |
| C18 | `Imitation végétale de viandes séparées mécaniquement.↵Ingrédients : eau, sucre, soja.` | E (végétaux) / V | E (végétaux) / V | E (végétaux) / V | Description avant en-tête écartée. |
| C19 | `Ingrédients : eau, sucre. Imitation végétale de viandes séparées mécaniquement.` | P→MS / NV | P→MS / NV | P→MS / NV | Description après composition retenue. |
| C20 | `Ingrédients : eau, sans viandes séparées mécaniquement, sucre.` | P→MS / NV | P→MS / NV | P→MS / NV | Négation dans un élément de liste. |
| C21 | `Ingrédients : eau, ne contient pas de viandes séparées mécaniquement.` | P→MS / NV | P→MS / NV | P→MS / NV | Présence extraite « pas de… », plus token inconnu « ne ». |
| C22 | `separatorvlees` | E→MS / NV | non analysé / ∅ | non analysé / ∅ | Nom NL de base. |
| C23 | `mechanically separated meat` | E→MS / NV | non analysé / ∅ | non analysé / ∅ | Nom EN de base. |
| C24 | `Separatorenfleisch` | E→MS / NV | non analysé / ∅ | non analysé / ∅ | Nom DE de base. |
| C25 | `zonder separatorvlees` | P→MS / NV | non analysé / ∅ | non analysé / ∅ | Résidu « zonder ». |
| C26 | `no mechanically separated meat` | P→MS / NV | non analysé / ∅ | non analysé / ∅ | Résidu « no ». |
| C27 | `ohne Separatorenfleisch` | P→MS / NV | non analysé / ∅ | non analysé / ∅ | Résidu « ohne ». |
| C28 | `Ingrediënten: water, zonder separatorvlees.` | P→MS / NV | P→MS / NV | P→MS / NV | Négation NL dans composition explicite. |
| C29 | `Ingredients: water, no mechanically separated meat.` | P→MS / NV | P→MS / NV | P→MS / NV | Négation EN dans composition explicite. |
| C30 | `Zutaten: Wasser, ohne Separatorenfleisch.` | P→MS / NV | P→MS / NV | P→MS / NV | Négation DE ; Wasser inconnu dans cette sonde. |
| C31 | `viandes séparées↵mécaniquement` | N / I | non analysé / ∅ | non analysé / ∅ | Deux tokens N ; matcher brut E→MS ; acquisition OCR non exécutée. |

C32 ajoute quatre observations sur le texte bilingue construit :

`Ingrédients : eau, sans viandes séparées mécaniquement.↵Ingredients: water, sugar.`

| Préférence | FULL_LABEL | OCR_LABEL texte | Preuve / limite |
|---|---|---|---|
| FR | Bloc français ; P→MS, NV, résidu sans | Même résultat | ID responsable MS. |
| EN | Bloc anglais ; eau/sucre E, VEGAN | Même résultat | Aucun MS dans les tokens ; IDs responsables water/sugar ne sont pas des bloqueurs. |

Ces deux compositions contradictoires sont un stimulus artificiel de sélection, pas une étiquette examinée. Ce résultat démontre l'effet de la sélection linguistique, sans établir la qualité des choix automatiques sur des photos ou des blocs réels.

Précisions sur les résidus et l'attribution :

- C01/C06/C11/C22–C24 : token MS exact, résidu vide, sans inconnu pour cette reconnaissance.
- C02/C14/C17/C20 : résidu `sans`, token négatif entier dans les inconnus, MS reconnu et responsable du verdict NV. C07/C19 : résidu `imitation vegetale de` ; C25–C30 : `zonder`, `no` ou `ohne`.
- C03 : présence extraite `pas de viandes séparées mécaniquement`, résidu `pas de`, MS reconnu mais verdict détaillé null et IDs responsables vides. C21 ajoute une vraie section de composition : NV avec MS responsable, inconnus `ne` et le token `pas de…`.
- C04/C16 : une alerte de traces, pas de MS considéré comme ingrédient. C04 n'a pas de composition ; C16 en a une, uniquement végétale dans cet exemple. La correspondance brute de la phrase de traces ne doit pas être substituée au résultat du pipeline.
- C12/C31 : le matcher brut normaliserait ponctuation/retour à la ligne en espaces et trouverait l'expression exacte ; le parser du pipeline la scinde, d'où absence de MS. Le résidu de chaque fragment et les inconnus restent présents. Ce résultat ne couvre pas le nettoyage préalable d'une photo.
- Le test historique confirme séparément `meat` → propriétaire `meat`, EXACT, résidu vide et ID responsable meat en manuel. C10 confirme une reconnaissance partielle de ce propriétaire, pas une nouvelle variante singulière de MS.

## Faits, inférences et inconnues

| Nature | Conclusion |
|---|---|
| Établi | Les quatre formes positives résolvent le bon concept. La preuve réglementaire et la dénomination d'ingrédient ne prouvent pas que toute occurrence textuelle exprime sa présence. |
| Établi | Une présence reconnue dans un token partiel peut bloquer malgré la négation inconnue. Même un token exact peut provenir d'une phrase négative scindée. |
| Établi | Le faux positif est reproduit sur du texte manuel et dans les deux modes d'étiquette testés, selon position et structure. Il n'est pas limité à la forme française. |
| Établi | Dans ces sondes à texte identique, F et O donnent les mêmes résultats. Les traces testées restent séparées. Une description avant en-tête peut être écartée. |
| Établi par code, pas par UI exécutée | Le formateur affiche les chemins des bloqueurs. Sa section dédiée aux inconnus est limitée aux verdicts UNCERTAIN/INCONCLUSIVE ; les inconnus conservés au niveau métier ne garantissent donc pas un avertissement visible dans un verdict animal. |
| Inférence plausible | Une photo qui mélange une allégation avec une composition, perd une limite de section ou altère la négation peut exposer ce risque. Aucune acquisition réelle n'a établi sa probabilité. |
| Inconnu | Fréquence des allégations exactes, distribution des parcours utilisés, erreurs OCR sur ces mentions, effet du choix manuel de bloc, lecture effective des diagnostics par l'utilisateur et taux de faux positifs réels. |
| Inconnu | Usage réel de ces expressions dans des imitations végétales. Les exemples d'imitation sont des contre-épreuves construites, pas une preuve qu'un tel produit existe. |

## Gravité et portée produit

L'impact potentiel est matériel : l'application peut attribuer une origine animale explicite à un texte qui affirme son absence, afficher un verdict animal et désigner un coupable incorrect. Cela peut conduire à rejeter un produit compatible et diminuer la confiance dans l'explication. Aucun impact sanitaire ni aucune fréquence ne sont quantifiés ici. Sébastien accepte explicitement cette **fausse attribution de présence** pour ce lot ; le succès lexical ne devient pas pour autant une preuve de présence effective.

La gravité d'une occurrence est plus élevée qu'une simple reconnaissance partielle affichée comme incertaine : la priorité animale produit une conclusion déterminée. La portée dépend de la sélection du texte, de la position des allégations et des parcours. Les protections de section et de traces atténuent certains cas ; elles ne neutralisent pas C14/C17/C19/C21/C28–C30. L'édition du texte et l'explication du bloqueur donnent des moyens de contrôle humain, sans garantie de correction ou de compréhension.

L'ajout du concept explique la nouvelle attribution précise MS dans ces contre-exemples, mais ne constitue pas une évaluation de tous les risques préexistants. C10 et le test des voisins montrent qu'il existe aussi des comportements historiques. Différer ce lot ne prouve donc pas que toutes les négations de viande deviennent sûres.

## Comparaison des options examinées

La comparaison est conservée comme historique d'analyse. **A est retenue ; B, C et D ne sont pas retenues pour ce lot.** Les conséquences des alternatives ci-dessous ne constituent pas des travaux demandés ni des conditions nouvelles de la décision prise.

| Choix | Avantage | Inconvénient / risque | Conséquence sur le lot et conditions |
|---|---|---|---|
| **A — Conserver les quatre formes et accepter/documenter R3 — RETENUE** | Conserve immédiatement la couverture exacte et la granularité de l'ingrédient. Aucun changement technique supplémentaire du lot. | Maintient les faux positifs démontrés, y compris les exemples texte d'étiquette et NL/EN/DE. Documentation et tests de caractérisation ne les corrigent pas. Fréquence réelle inconnue. | Acceptation explicite par Sébastien le 7 octobre 2026 du comportement actuel et du risque documenté pour ce lot. Les formes sont conservées ; ce choix ne règle pas la réserve juridique. |
| **B — Différer seulement la forme FR** | Peut réduire une exposition française si la surface est réellement désactivée dans le runtime. Conserve une partie de la couverture multilingue. | **Retirer uniquement le mapping FR ne suffit pas** : le nom canonique FR est lui-même une clé globale du matcher (`IngredientMatcher.kt:137`). Les négations NL/EN/DE restent concernées. Aucun gain quantifié. | Nécessite une portée distincte approuvée, une décision sur la clé canonique et une revue des données, importeur et tests aujourd'hui fondés sur les quatre formes. Cette analyse ne modifie ni ne choisit cette représentation. B n'est pas une solution démontrée de R3. |
| **C — Différer l'ensemble du concept** | Évite d'accepter maintenant les nouvelles attributions MS alors que le contexte reste insuffisamment établi. | Perd la reconnaissance précise du lot, limite le benchmark et peut laisser des reconnaissances historiques génériques. | Le lot étant déjà dans HEAD, sa désactivation/reprise exige une tâche explicitement autorisée et revue ; aucun retrait ou changement d'historique n'est réalisé ici. Reporter la validation produit du concept et préciser les conditions de reprise. |
| **D — Demander une évolution contextuelle distincte avant acceptation produit** | Traite la cause du risque et permet un contrat testable sur présence, négation, imitation et traces, dans les parcours pertinents. | Travail et délai supplémentaires ; risque de faux négatifs et de régressions à mesurer. Une correction générale n'est pas acquise. | Cette option aurait subordonné l'acceptation à une évolution séparée, hors ligne et déterministe, avec tests et revue indépendante. Elle n'est pas retenue ; aucune évolution du matcher, parsing, OCR ou verdict n'est autorisée par le présent rapport. |

L'analyse initiale recommandait D au vu des contre-épreuves multilingues et des modes texte d'étiquette. **D est retirée comme recommandation active après la décision explicite de Sébastien de retenir A.** Les observations techniques et la comparaison restent conservées ; aucun report de forme ou de concept, ni aucune évolution contextuelle, n'est demandé par cette décision.

Le risque est accepté pour le lot courant, sans correction technique ni retrait du corpus.

## Données complémentaires utiles à un éventuel réexamen

Aucun échantillon d'étiquettes réelles n'a été étudié et aucune statistique d'usage n'a été fournie ou observée pour cette décision. Les éléments suivants restent utiles à un éventuel réexamen ; ils ne sont pas des conditions préalables ajoutées à l'option A retenue :

1. Étiquettes complètes authentiques avec photos et transcription, langues, position de l'allégation par rapport à la composition, identifiants de cas et vérité de composition vérifiable ; inclure des présences positives, des absences déclarées et des textes sans rapport.
2. Fréquences avec dénominateur : occurrence des formes et des négations sur l'échantillon, parcours manuel/étiquette/OCR réellement utilisés, et sélection de langue/bloc. Les 97 observations construites ne peuvent pas servir à estimer ces fréquences.
3. Pour l'OCR : comparer photo, texte reconnu, texte nettoyé, texte retenu après sélection et texte édité réellement soumis. Contrôler ordre, limites de sections et conservation de la négation ; cette passe n'a pas exécuté ce parcours.
4. Évaluation utilisateur de la fausse exclusion d'un produit, de la compréhension du bloqueur et de la capacité à repérer/corriger l'erreur. Le responsable fixe l'acceptabilité d'un verdict conservateur et les critères de réexamen.
5. Si une évolution contextuelle est demandée ultérieurement : un périmètre séparé couvrant positifs, négatifs, mentions végétales, inconnus, traces, listes absentes, blocs multilingues, ponctuation et OCR. Préserver les régressions historiques et vérifier qu'une mention « sans » seule ne devient pas automatiquement une preuve VEGAN pour un produit entier. Aucun tel travail n'est décidé ici ; implémentation et tests nécessiteraient leur propre approbation.

L'option A est retenue sans ces mesures : la fréquence réelle reste inconnue et les faux positifs construits sont acceptés, sans promesse de sécurité sémantique démontrée.

## Décision produit actée et réserves séparées

**Le 7 octobre 2026, Sébastien, responsable produit, retient l'option A : conserver les quatre formes et accepter le risque R3 documenté.** Cette décision a été communiquée explicitement dans la demande de mise à jour du présent rapport.

Les formes conservées sont exclusivement :

- FR : `viandes séparées mécaniquement` ;
- NL : `separatorvlees` ;
- EN : `mechanically separated meat` ;
- DE : `Separatorenfleisch`.

Sébastien estime que les cas concernés sont **probablement rares**. Il s'agit de son appréciation produit, **pas d'une fréquence mesurée** : aucun échantillon d'étiquettes réelles n'a été étudié. Les 97 observations construites ne permettent pas d'estimer cette fréquence, ni le comportement complet de tous les parcours OCR.

Le responsable accepte le comportement actuel pour ce lot, y compris les faux positifs construits où une formulation négative comme `sans viandes séparées mécaniquement` peut conduire à `NON_VEGETARIAN`, ainsi que les autres limites documentées dans ce rapport. Le lot est conservé dans son état actuel ; aucune désactivation pendant une étude ni évolution contextuelle préalable n'est demandée.

Cette acceptation règle la décision produit R3 pour ce lot. Elle ne corrige pas le faux positif, ne démontre pas la présence animale dans ces phrases et ne prouve pas la sûreté sémantique générale du matcher ou de l'application. Les cas reproduits, leurs limites et les inconnues restent valables.

R1/R2 sont des protections techniques sur la préservation historique et la compatibilité runtime. Leur validation rapportée ne tranche ni la vérité de présence de l'ingrédient dans une phrase ni l'acceptabilité de R3. Aucun travail sur R1/R2 n'est revendiqué ici.

La réserve juridique relative à l'avis cité, à l'attribution et à la déclaration de changement reste indépendante. Aucun contrôle de licence, avis juridique ou nouvelle validation des PDF n'a été réalisé par cette analyse. Un choix produit n'emporte pas résolution de cette réserve.

## Vérification de la mise à jour documentaire du 7 octobre 2026

- Préflight : `git status --short --branch`, `git rev-parse HEAD`, `git diff --stat`, `git diff --cached --stat` et `git ls-files --others --exclude-standard`. Branche master, HEAD inchangé à `5a3a352dbecf3af68fabe0b25cb2cced238d8517`, index et modifications suivies vides ; seul le présent rapport était déjà non suivi.
- Relecture de `AGENTS.md` et du rapport avant modification. Seul ce rapport a été modifié pour consigner la décision ; comparaison SHA-256 des 416 fichiers suivis/non ignorés avant et après, avec les 415 autres fichiers inchangés.
- Relecture intégrale du rapport actualisé et examen de son diff complet par rapport à sa copie initiale en mémoire, car un fichier non suivi n'apparaît pas dans le diff Git ordinaire. Vérification des diffs suivis et indexés, puis `git diff --check` sans erreur.
- Aucun test, sonde runtime, importeur, builder ou générateur relancé. Les résultats techniques présentés plus haut restent ceux de l'analyse initiale ; aucune validation nouvelle du comportement ou de la licence n'est revendiquée.
- État final : HEAD et index inchangés ; seul ce rapport reste non suivi. Aucun autre rapport, code, test, donnée, actif, source ou version n'a changé.

Le rapport actualisé consigne une décision produit et peut être publié à ce titre. Il ne constitue pas une validation juridique, une autorisation de commit automatique, un nouvel import ou une autorisation de modifier les règles. Aucun commit créé.
