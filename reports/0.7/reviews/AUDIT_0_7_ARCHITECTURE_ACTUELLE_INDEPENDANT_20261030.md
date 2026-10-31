# Is It Vegan? — Audit indépendant de l’architecture actuelle

Date : 30 octobre 2026. Base examinée : sources locales au HEAD `ff4e4af3fbc21f5dae2d26cb7d29d1e926a87178`.

## 1. Mandat, méthode et état du dépôt

Cet audit décrit le code présent et ses raccordements, sans proposer ni appliquer de changement de comportement. Les conclusions des sections 1 à 9 ont été établies avant toute lecture du plan v0.7. Aucun rapport antérieur ni page de documentation n’a servi à les établir. Seuls `AGENTS.md`, les instructions de session et le skill local `.agents/skills/android-validation/SKILL.md` ont été consultés comme instructions de travail. Aucun autre `AGENTS.md` n’a été trouvé dans les sous-arbres applicatifs inspectés ou aux emplacements parents vérifiés.

Inventaire initial, obtenu avec `Get-Location`, `git status --short`, `git status --porcelain=v1 --untracked-files=all`, `git diff`, `git diff --cached`, `git ls-files --others --exclude-standard`, `git branch --show-current`, `git rev-parse HEAD` et `git worktree list` :

| Élément | Observation |
|---|---|
| Dépôt | `C:\Users\Sebastien\StudioProjects\Is-it-Vegan` |
| Branche | `master` |
| HEAD | `ff4e4af3fbc21f5dae2d26cb7d29d1e926a87178` |
| Changements suivis, index et travail | Aucun au début de l’audit |
| Fichiers non suivis | Aucun au début de l’audit |
| Worktree principal | Dépôt ci-dessus, `ff4e4af`, `master` |
| Autre worktree déclaré | `C:/Users/Sebastien/StudioProjects/Is-it-Vegan-mutation-testing-pit`, `4aa6720`, branche `mutation-testing-pit` ; non inspecté et non modifié |
| Version déclarée dans Gradle | `0.6.13.11`, code `63` |

L’analyse repose sur les manifests, Gradle, l’inventaire des 47 fichiers Kotlin de production des deux modules et des lectures ciblées des sources, tests et scripts d’assets. L’inventaire compte 22 fichiers Kotlin de production dans `app/src/main` et 25 dans `mutation-core/src/main`. Ces nombres décrivent des fichiers présents, pas des fonctionnalités actives.

Vocabulaire de preuve : **F** = fait lisible dans le code/configuration ; **T** = test effectivement exécuté pendant cet audit ; **D** = déduction conditionnelle ; **R** = reproduction ou preuve supplémentaire nécessaire. Un test simplement présent est signalé comme tel. Aucun benchmark, PIT, test connecté, lancement d’application ou accès à un appareil n’a été effectué. Le Nokia n’a pas été ciblé. Aucun commit, push ou changement de code, test, asset, configuration ou version.

Les chemins ci-dessous sont relatifs à la racine. Pour alléger les références, **A/** désigne `app/src/main/java/com/example/isitvegan/`, **C/** désigne `mutation-core/src/main/kotlin/com/example/isitvegan/`. Les noms de tests se retrouvent dans les répertoires de tests inventoriés en section 7.

## 2. Résumé de l’architecture observée

L’application active est un écran Compose avec trois modes d’entrée, un état de session en mémoire et une façade Android vers un service JVM déterministe. La séparation `app -> mutation-core` est réelle : le cœur est appelé en production, malgré son nom historique. L’OCR ML Kit acquiert du texte ; il ne décide pas du verdict. L’analyse est déclenchée explicitement après sélection ou édition du texte.

```text
Manifest MAIN/LAUNCHER
  MainActivity.onCreate
    chargement IO des trois assets -> façade Android VeganAnalyzer
    OcrFirstScreen -> OcrFirstScreenContent
      saisie Étiquette / Liste seule -------------------------------+
      Photo Picker -> aperçu / recadrage -> OcrProcessor             |
        ML Kit latin -> texte natif -> OcrTextCleaner                |
        segmentation -> choix du texte -> OcrSession.editableText ---+
        revue multilingue -> information / édition explicite         |
      bouton analyse -> OcrThreading.cpu                             |
        IngredientAnalysisService.analyzeWithDiagnostics <-----------+
          segmentation / sections -> prétraitement -> arbre / tokens
          lexique multilingue -> matching -> règles d’origine
          AnalysisResult / diagnostics / explication
        VerdictExplanationFormatter -> résultat affiché
        AnalysisSnapshot -> export texte Android ACTION_SEND
```

Forces principales : cœur indépendant d’Android, données embarquées, séparation traces/présence, diagnostics riches, conservation du texte OCR natif et des textes soumis, garde-fous récents sur le texte OCR choisi, absence de correction multilingue silencieuse. Limites principales : transformations sans cartographie complète vers la source, identité de bloc fondée sur son contenu, sélection réduite à une option par langue, états UI multiples, validation asynchrone sans identité de session, divergences entre inconnus décisionnels et affichés.

## 3. Modules, dépendances et données effectives

### 3.1 Frontières de compilation et d’exécution

| Zone | Responsabilité réellement établie | Dépendances / limites |
|---|---|---|
| `:app` | Activity, Compose, préférences de langue, image/EXIF/recadrage, ML Kit, choix OCR, session, rendu localisé et partage Android | Dépend de `:mutation-core`, Compose Material3, Activity Compose, Core/Lifecycle, ExifInterface et ML Kit. Pas de ViewModel ni de stockage de session dans le parcours inspecté. |
| `:mutation-core` | JSON embarqué vers modèles, segmentation linguistique, extraction de sections, arbre, tokenisation, lexiques, matching, origine, résultats et diagnostics, génération de revue multilingue | Bibliothèque Java/Kotlin JVM 17 ; seule dépendance explicitement ajoutée : JUnit en test. Aucune dépendance Android ni PIT déclarée dans son Gradle courant. |
| `tools/build_ingredients.py` | Validation éditoriale et génération des assets ingrédients/alias | Outil hors parcours applicatif ; non appelé par les fichiers Gradle inspectés. Exécuté ici uniquement avec `--check`. |
| Tests et outils de recherche | Profils de matcher, mesures, index expérimental, scénarios métier | Leur présence ne démontre ni adoption en production ni performances utilisateur. Aucun test de mesure lancé. |

Preuves : [settings.gradle.kts](../../../settings.gradle.kts), [app/build.gradle.kts](../../../app/build.gradle.kts), [mutation-core/build.gradle.kts](../../../mutation-core/build.gradle.kts), [catalogue de versions](../../../gradle/libs.versions.toml). SDK minimal 30, cible/compilation 37, Java 17, AGP déclaré 9.4.1, Kotlin 2.4.20, BOM Compose 2026.09.00, ML Kit `com.google.mlkit:text-recognition:16.0.1`. Il s’agit des versions déclarées, pas d’un audit de résolution complète des dépendances transitives. Le type release ne minifie pas.

Le [manifest principal](../../../app/src/main/AndroidManifest.xml) expose `MainActivity` comme entrée lanceur. Il ne déclare ni permission Internet ni activité de capture caméra. Le parcours photo établi est `PickVisualMedia(ImageOnly)` : sélection d’une image fournie par le système, sans implémentation de prise de vue directe établie. Le manifest debug ajoute `showWhenLocked` et `turnScreenOn`. La dépendance ML Kit est la variante embarquée ; aucune décision métier distante n’est appelée dans le chemin inspecté. Le fonctionnement complet hors ligne d’une installation neuve n’a pas été rejoué sur Android.

### 3.2 Sources chargées

`A/AndroidIngredientKnowledgeLoader.load` ouvre exactement trois assets :

| Asset de production | Inventaire JSON courant | Utilisation |
|---|---|---|
| `ingredients.json` | 488 ingrédients, IDs uniques dans le comptage local | Noms, alias, E-numbers, statuts, raisons, source et éventuelles notes d’origine |
| `ingredient_aliases_multilingual.json` | 1 707 entrées `aliases`, 19 corrections OCR, 2 093 `mappings` | `resolve` utilise les alias/corrections historiques ; les mappings structurés sont exposés aux outils d’index, pas au résolveur courant |
| `origin_qualifier_rules.json` | 7 règles déclarées : 3 `ACTIVE`, 4 `REVIEW_BEFORE_ENGINE_USE` ; 1 expression protégée | Seules les règles actives sont chargées pour résolution ; les rubriques documentaires ou non résolveuses ne sont pas automatiquement des décisions moteur |

`knowledge/ingredients.json` n’est pas lu par l’application. Il est transformé par `tools/build_ingredients.py` : notamment `sources` devient `source`. Les 488 enregistrements éditoriaux diffèrent donc structurellement de l’asset ; cette différence n’établit pas une dérive. **T : `python tools/build_ingredients.py --check` réussit**, vérifiant la projection courante et la copie des alias. Ce contrôle n’atteste pas toute la sémantique des règles d’origine.

Les entrées historiques d’alias se répartissent ainsi : NL 430, FR 419, DE 407, EN 397, IT 27, ES 27. Les mappings structurés : NL 545, FR 516, DE 495, EN 470, IT 34, ES 33. Aucun alias historique PL ou scandinave dans ce fichier. Un ID visé par les alias, `cereals`, manque à la base des 488 ingrédients ; `resolve` expose `canonicalAvailable=false`, et le service ne peut pas lui attribuer une classification canonique absente. Cela ne signifie pas que toutes ses formes seront nécessairement inconnues : le matcher peut encore reconnaître un autre alias connu. `validateAgainst` existe mais n’est pas appelé par le loader Android.

### 3.3 Présent dans le module, sans appel métier de production établi

Les recherches d’appels dans `app/src` et `mutation-core/src` trouvent `KnowledgeIndex.from` et `IndexedCandidateProvider` dans les tests ordinaires et les tests de mesure/profilage, pas dans `IngredientAnalysisService.runAnalysis` ni la façade Android. Le matcher actif reste `IngredientMatcher(database)`.

`MatcherProfileCollector` est optionnel et utilisé par les tests de profilage ; la production ne lui passe pas de collecteur. `LabelLanguageSegmenter.probableDutchHeadings` est une fonction de recherche appelée par `ProbableDutchHeadingResearchTest`, absente de `findMarkers`. Les branches de présentation `PROBABLE` ne prouvent donc pas la génération active de tels blocs.

`MainActivity.kt` contient encore `IsItVeganScreen`, un ancien écran synchrone avec son propre export. `onCreate` appelle `OcrFirstScreen`. Les constats de parcours actif de cet audit ne reposent pas sur cet ancien écran.

## 4. Parcours actifs de bout en bout

### 4.1 Démarrage, saisie et lancement d’analyse

**F.** `MainActivity.onCreate` initialise `databaseReady` à partir du singleton `A/VeganAnalyzer`. Un `LaunchedEffect(Unit)` charge la base sur `Dispatchers.IO`, sous `runCatching`. La façade publie un `IngredientAnalysisService` dans un champ `@Volatile`, avec chargement synchronisé. Les trois textes JSON sont parsés par `IngredientKnowledge.fromJson` et le parseur JVM `MiniJson`.

L’écran actif est affiché pendant ce chargement, avec analyse désactivée tant que `databaseReady` est faux. En cas d’exception de chargement, cet état reste faux ; aucune erreur dédiée ni relance n’est proposée par ce code. Les préférences de langue sont lues dans SharedPreferences (`A/UiLanguage.kt`) ; elles constituent une persistance distincte des sessions.

`OcrFirstScreenContent` commence en `FULL_LABEL`. `FULL_LABEL` et `MANUAL_INGREDIENT_LIST` partagent le champ `ingredients`. `OCR_LABEL` utilise `session.editableText`. Au clic, `analyse(text)` capture texte, mode, langue et éventuel ID de bloc, puis appelle la façade sur `Dispatchers.Default` via `OcrThreading.cpu`. Le rendu et la construction du diagnostic texte se font dans ce même travail CPU. La publication vérifie que mode et texte courant correspondent encore à la demande. L’erreur d’analyse est convertie en message localisé sous la même condition.

### 4.2 Photo, EXIF, géométrie et OCR

La sélection d’image remet la session à zéro et conserve l’URI dans l’état Compose. `LaunchedEffect(sourceUri)` prépare l’aperçu sur IO. `OcrImageInputFactory` lit les dimensions et l’EXIF, décode avec échantillonnage puis limite l’aperçu à 1 024 px et l’image OCR à 2 048 px. L’aperçu utilise RGB_565, l’OCR ARGB_8888. Les orientations miroir sont appliquées aux pixels ; sinon la rotation est transmise à `InputImage`.

`OcrCropDialog` utilise `OcrCropGeometry` pour les gestes et un rectangle normalisé dans l’image déjà orientée. `OcrBitmapCropper.crop` convertit ces fractions en pixels bornés. Le recadrage OCR est refait depuis la source décodée/orientée, et non depuis le petit aperçu. La validation d’un cadre réinitialise la session et demande une nouvelle extraction.

`OcrProcessor` possède un `SupervisorJob + Dispatchers.IO`. Il prépare l’image puis appelle le reconnaisseur latin ML Kit. En succès, il construit un `OcrDocument` avec blocs, lignes, éléments, bounding boxes et ordres ML Kit, puis exécute sur Default la reconstruction géométrique et le post-traitement.

Point essentiel : **le texte éditable provient de `recognized.text` nettoyé, dans l’ordre natif ML Kit**. Le texte reconstruit géométriquement est nettoyé séparément puis comparé ; sa différence produit un avertissement, sans remplacer le texte natif. Les warnings détaillés de `OcrTextReconstructor` et le document géométrique lui-même ne sont pas transmis dans `OcrProcessingResult`.

Les erreurs de préparation IO/runtime/mémoire ont des messages distincts. Une `RuntimeException` de post-traitement déclenche un résultat de secours avec texte ML Kit brut éditable et `usedRawFallback=true`. Le bitmap préparé est recyclé au complete de la tâche ; le processeur est fermé au dispose de la composition. Ces chemins sont lisibles, pas validés ici sur Android.

### 4.3 Nettoyage, blocs, choix et session

`OcrTextCleaner.clean` conserve les lignes ordinaires, corrige certaines césures, espaces, séparateurs et le titre OCR français `ingrédlents`. `cleanSelectedBlock` coupe avant certains parasites tels que `open here` ou `importateur` en début de ligne. Il n’existe pas de journal exhaustif de ces transformations ni de table d’offsets brut/nettoyé.

`LabelLanguageSegmenter.segment` reconnaît des marqueurs/codes et titres, construit des `LanguageBlock`, appelle `OcrBlockLanguageClassifier.classify`, les note par qualité puis choisit un bloc. Le score inclut titre, séparateur, longueur utile, structure de liste et pénalité de troncature. La préférence UI départage ensuite. `startIndex/endIndex` concernent le texte passé au segmenteur, ici déjà nettoyé ; `rawText` de bloc est en outre trimé.

`OcrTextSelection.from` garde les blocs avec titre confirmé et langue dans FR/EN/NL/DE/IT/ES/PL, puis **une seule option par langue : la première**. L’éditable initial peut cependant venir de `segmentation.selectedText` si le bloc retenu n’a pas d’option correspondante. `OcrTextBlockChoices` n’affiche ses choix, dont « texte complet », que si `options.size > 1`.

`OcrSession` conserve texte natif, texte complet nettoyé, texte édité, options/blocs, diagnostic OCR, revue et snapshots d’analyse. L’URI et les bitmaps ne font pas partie de ce modèle. `withEditableText` invalide la revue mais conserve les analyses. `hasStaleResult` compare le dernier texte soumis à l’éditable. Les changements de bloc ou passage au texte complet effacent l’historique d’analyse ; une nouvelle photo, un cadre ou la saisie manuelle OCR recréent la session. Les données restent en mémoire Compose (`remember`), sans restauration déclarée de session.

### 4.4 Sections, parsing, lexique et origine

Dans `IngredientAnalysisService.runAnalysis`, une nouvelle segmentation est réalisée sur le texte réellement soumis. En OCR, `selectOcrSections` **ne resélectionne pas une traduction** : il extrait dans ce texte exact et refuse une conclusion si préambule non autorisé, plusieurs titres/blocs, délimiteurs non équilibrés ou marqueur de langue imbriqué. Un ID ancien n’autorise pas un texte ambigu. L’ID source n’est conservé que s’il égale `stableBlockId(text)` et si le texte est sûr. Les tests ciblés confirment ces garde-fous.

Les autres modes passent par `selectSections`. `FULL_LABEL` utilise la sélection segmentée. Le mode liste seule permet un fallback sans titre et a sa propre préférence de sections. Il ne faut donc pas supposer que tout le texte collé en mode liste seule sera toujours analysé si ce texte contient plusieurs blocs linguistiques.

`LabelSectionExtractor.extract` sépare ingrédients, présence déclarée (`contient`, etc.), traces et sections ignorées. Il peut reconnaître une liste implicite sans titre sur signaux structurels convergents : au moins trois séparateurs de premier niveau, quatre éléments parsés, deux termes alimentaires de sa liste bornée et un signal supplémentaire (%/groupe/densité). Les indices alimentaires sont surtout FR/EN. Ce mécanisme est appelé en production ; ce n’est pas seulement un prototype.

`LabelPreprocessor.preprocess` retire notices/traces, applique des corrections OCR déterministes et joint une liste d’expressions connues coupées sur deux lignes. `IngredientTreeParser.parse` construit composites, feuilles et additifs, avec pourcentages, classes fonctionnelles, nano, proportions variables, alternatives et qualifications d’origine. `IngredientTokenizer.flatten` attribue ordre, profondeur et parent. Les noms composites/sections restent diagnostiques et ne sont pas matchés comme feuilles.

Chaque feuille suit : expression protégée éventuelle -> `MultilingualIngredientLexicon.resolve` dans la langue extraite -> nom canonique disponible -> `OcrIngredientNormalizer.forMatching` -> qualification d’origine -> `IngredientMatcher.match` -> `OriginQualifierRuleSet.resolve` -> `UnknownCollector.assess`. Le matcher choisit les alias les plus longs, protège certains contextes végétaux et conserve le résidu. Les règles d’origine ne s’appliquent qu’aux IDs et statuts de base attendus ; elles ne classent pas librement tout qualificatif. Les diagnostics conservent statuts de base/effectifs et IDs de règle/outcome.

### 4.5 Verdict, rendu, diagnostic et export

`found` agrège les ingrédients par ID avec conservation du statut le plus défavorable ; les inconnus sont dédoublonnés par normalisation. Les tokens diagnostiques conservent les occurrences et leurs parents, sans offsets vers l’image.

Il existe plusieurs lectures du résultat :

- `veganAssessment` : présence d’un `VEGETARIAN` ou `NON_VEGAN` => `NOT_VEGAN` ; sinon incertitude/inconnu/absence => `UNCERTAIN` ; sinon `VEGAN`. Une origine explicitement non vegan force aussi `NOT_VEGAN`.
- `verdict` détaillé : `NON_VEGAN` connu => `NON_VEGETARIAN` ; puis incertain => `UNCERTAIN` ; puis inconnu/vide => `INCONCLUSIVE` ; puis végétarien => `VEGETARIAN` ; sinon `VEGAN`. En absence de liste, ce verdict est null.
- Résultats conditionnels et classification végétarienne secondaire : explicitement calculés en mettant certaines catégories de côté, avec explication structurée.

L’écran actif appelle `VerdictExplanationFormatter.render` avec `includeAnalysisContext=false` par défaut : il affiche principalement la classification détaillée, les bloqueurs, incertains, inconnus visibles et conditions. L’enveloppe explicite de compatibilité vegan présente dans l’ancien écran n’est pas activée ici. `VEGETARIAN` n’est jamais transformé en `VEGAN`, mais la multiplicité des sorties mérite un contrat explicite lors de v0.7, notamment pour lait + incertain ou lait + inconnu.

Les traces sont passées séparément à `crossContactWarnings` et ajoutées à la fin du rendu ; elles n’entrent pas dans `VerdictEngine`. La présence déclarée est parsée séparément et rejoint les ingrédients effectifs. Sans liste exploitable, la présence peut être signalée mais aucun verdict de liste complète n’est produit.

`DiagnosticReport.build` sérialise entrée, segmentation, sections, arbre/tokens, corrections métier, origine et décision. `AnalysisSnapshot` conserve texte exact, diagnostic, rendu, timestamp, ID et diagnostic structuré. `OcrExportReport.build` ajoute texte natif, éditable courant et analyses conservées. L’export actif est `ACTION_SEND text/plain`, sans image ni URI dans le rapport construit. Cela ouvre le partage Android ; aucune transmission distante automatique n’est établie.

### 4.6 Revue multilingue : raccordement et limite de génération

`OcrProcessor` appelle bien `VeganAnalyzer.generateMultilingualAlignment(segmentation.blocks)` si la base est chargée. L’évaluation est stockée en session et affichée par `MultilingualAlignmentReview`. Une indisponibilité ou une exception de génération est isolée de l’OCR et signalée. Il n’y a pas de recalcul automatique après édition ni lorsque la base finit de charger après une extraction ayant trouvé la revue indisponible.

`MultilingualIngredientAlignment.generate` produit des occurrences, relie les concepts uniques partagés, puis marque **AMBIGUOUS** les éléments isolés entre ancres. Les liens qu’il crée utilisent `CANONICAL_ID` ou `AMBIGUOUS`, jamais `EXPLICIT_TRANSLATION`. Or `evaluateLink` n’autorise une `CORRECTION_PROPOSAL` inconnue-vers-connue qu’avec `EXPLICIT_TRANSLATION`. **D forte : aucune correction proposée n’est générée par ce parcours courant**, même si l’évaluateur, l’éditeur et l’acceptation explicite sont présents et testables avec des évaluations construites.

Le test `parsedSectionsProduceExactCorroborationAndAUniqueAnchoredCorrectionProposal` a un nom trompeur : son assertion actuelle exige `AMBIGUOUS`, sans ID proposé. Ce test a été exécuté avec succès. Les tests UI de bouton d’acceptation injectent une proposition ; ils ne prouvent pas sa génération par l’OCR réel.

Une proposition injectable ne modifie le champ qu’après acceptation explicite et liaison stricte : bloc/langue sélectionnés, texte inchangé, occurrence source unique jusque dans les chevauchements. L’acceptation édite le texte, invalide la revue et requiert une nouvelle analyse. L’alignement n’est pas un argument du calcul de verdict et ne fusionne pas les verdicts des traductions.

## 5. Limites et risques par priorité

Priorités : **P1** = cohérence de décision/provenance à sécuriser avant extension ; **P2** = robustesse, explicabilité ou couverture à traiter ; **P3** = dette d’observabilité/maintenance. Aucun incident terrain nouveau n’est prétendu. Chaque cas distingue mécanisme établi et impact à reproduire.

### P1-1 — Résultat manuel conservé après modification

**F :** `A/OcrFirstScreen.kt`, `OcrFirstScreenContent`, champ `ingredients.onValueChange` : seul `diagnostics` est remis à null ; `result` reste inchangé. La carte ne consulte `session.hasStaleResult()` qu’en mode OCR. **Condition :** analyser en Étiquette ou Liste seule, puis modifier le champ. **Conséquence déduite :** ancien verdict encore affiché sans avertissement d’obsolescence, même si le prochain clic utilisera le nouveau texte. C’est un écart statique clair à l’invariant d’identification des anciens résultats ; le parcours visuel exact reste **R**, sans instrumentation exécutée. Les tests UI présents couvrent l’édition/réanalyse OCR et le changement de mode, pas une assertion équivalente trouvée pour l’édition manuelle après résultat.

### P1-2 — Retour asynchrone non lié à l’identité de session

**F :** même fichier, `analyse`, handlers Effacer, photo, mode, saisie manuelle et callbacks OCR ; `A/OcrProcessor.process`. La publication d’analyse vérifie mode + égalité de texte, pas session/URI/bloc/langue. Le callback OCR applique `session.withOcrResult(ocr)` à la session courante sans jeton de requête. Effacer et les chips de mode restent utilisables pendant les calculs ; le bouton analyse n’est pas bloqué par `extracting`. **Conditions :** extraction suivie d’effacement avant callback ; ou analyse A, changement de session puis même texte/mode dans B avant le retour ; ou changement de langue pendant analyse. **D :** texte ancien réinjecté, diagnostic/rendu d’un ancien contexte publié ou historique enrichi dans la mauvaise session. Le rejet texte différent fonctionne et limite le risque. **R :** interleavings à reproduire avec contrôle de délais ; aucun test de courses session A/B trouvé dans les tests inspectés. `OcrThreadingTest` vérifie un dispatcher, pas ce contrat.

### P1-3 — Inconnus décisionnels et inconnus visibles différents

**F/T :** `C/DiagnosticModels.kt`, `visibleUnknownTokens`, exclut les tokens de présence déclarée et ceux dont les statuts effectifs sont tous VEGAN, sauf préfixe numérique conservé. `C/UnknownCollector.kt` garde pourtant les feuilles `PARTIAL_CONTEXTUAL` dans `result.unknown`, qui bloque le verdict. `DiagnosticOutput093Test.effectiveVeganContextualMatchIsExcludedFromAggregatedUnknownLists` confirme ce comportement sur `tomate inhabituelle` avec une base synthétique. **Condition :** match vegan partiel avec résidu sémantique non couvert. **Conséquence :** possible conclusion INCONCLUSIVE avec liste visible des inconnus ne montrant pas le résidu responsable ; groupes et décision agrégée utilisent aussi le filtre. Le diagnostic token reste disponible. Ce n’est pas un faux verdict VEGAN démontré ; c’est une limite d’explication et une tension avec la visibilité des inconnus. **R :** reproduire un cas isolé avec assets réels et UI. `UncertainUnknownVisibility097Test` réussit mais couvre une composition donnée, pas l’équivalence générale des listes.

### P1-4 — Sélection d’une liste partielle sans preuve de complétude

**F :** `C/LabelLanguageSegmenter.kt`, `scoreBlock/preferredBlock` et `OcrBlockLanguageClassifier.classify` ; `A/OcrTextSelection.from`. Les scores et `manifestlyTruncated` sont heuristiques, pas une preuve de fin réelle de liste. Une liste tronquée après un ingrédient complet peut rester équilibrée et être reconnue. **Condition :** photo/recadrage/OCR ne contenant que le début d’une liste, avec un titre et des ingrédients connus. **D :** verdict portant seulement sur le fragment fourni, sans moyen structurel de déceler un ingrédient animal absent de l’acquisition. Les garde-fous multi-sections ne résolvent pas cette absence. **R :** corpus photo avec fins perdues ; aucun faux verdict terrain nouveau établi. Préserver la demande de vérification et exprimer le périmètre de la conclusion.

### P2-1 — Géométrie et provenance ne traversent pas toute la chaîne

**F :** `A/OcrProcessor.toOcrDocument/processPrepared`, `OcrModels`, `OcrSession`; `C/LanguageBlock`, `IngredientNode`, `IngredientOccurrence`. La géométrie existe temporairement, puis seuls textes et résumés sont conservés. Pas de lien token -> élément ML Kit -> zone de photo ; `rawText` d’un nœud est déjà nettoyé/normalisé. `stableBlockId` est un hash 32 bits du contenu normalisé, pas une identité d’occurrence. **Conditions :** besoin d’expliquer un mot à l’écran, blocs identiques répétés, ou collision de hash. **D :** impossibilité de reconstruire exactement la preuve image depuis l’export ; blocs de même contenu peuvent partager l’ID et des occurrences `${block.id}:${order}`. `evaluate` rejette les IDs d’occurrences dupliqués, pouvant rendre la revue indisponible. Pas de collision réelle observée ; **R** pour son impact utilisateur. Les tests de session prouvent la conservation de textes, pas une cartographie géométrique.

### P2-2 — Transformations OCR irréversibles et frontières heuristiques

**F :** `A/OcrTextCleaner.clean/cleanSelectedBlock` : césure minuscule réunie sans trait d’union, majuscules avec trait conservé, séparateurs répétés contractés, coupe avant certains débuts de ligne. `C/LabelPreprocessor.preprocess` retire notices et peut prolonger une note aux lignes suivantes ; ses corrections ne journalisent pas celles du cleaner. `C/LabelSectionExtractor.findMarkers/trimTrailingProductTitle` et `LabelLexicon.findProductLanguageBoundaries` contiennent des motifs très spécifiques : titres de biscuits/chocolat, `ENIKEDAO AL LATTE`, débuts de titres « nouilles/muesli/mélange/barre ». **Conditions :** texte alimentaire ressemblant à un marqueur, ponctuation OCR altérée, note suivie de composition sans séparation. **D :** coupure ou rattachement erroné, source difficile à expliquer automatiquement. Les tests `OcrTextCleanerTest`, `OcrLineBreak0692Test`, `OcrNativeOrderRegressionTest` et les tests de frontières protègent des exemples, pas toutes les ambiguïtés. Seuls les tests de frontières mentionnés en section 7 ont été relancés. **R :** exemples hors corpus ; ne pas généraliser ces motifs en garantie de segmentation.

### P2-3 — Une option par langue, texte complet parfois inaccessible par le choix

**F/T :** `A/OcrTextSelection.from` filtre puis groupe par langue et prend le premier bloc, alors que `segment` peut avoir préféré un bloc ultérieur de meilleure qualité. `A/OcrFirstScreen.OcrTextBlockChoices` retourne avant d’afficher « texte complet » si zéro ou une option. **Conditions :** deux listes de même langue ; ou une seule langue sélectionnable avec d’autres fragments non sélectionnables. **D :** option visible différente du bloc initial, impossible de choisir chaque occurrence, impossibilité d’utiliser ce contrôle pour revenir au texte nettoyé complet. Le brut reste affiché et le champ reste éditable, ce qui atténue sans remplacer le choix. Les 19 tests `OcrMultilingualSelectionTest` réussissent ; ils n’établissent pas un sélecteur par occurrence. **R :** reproduction UI de ces configurations.

### P2-4 — Parenthèses, composites et répétitions : conservation partielle

**F :** `C/IngredientTreeParser.isCompositionParenthesis`, `inspectParentheses`, `splitAtCurrentDepthWithSeparators`, `runAnalysis`. Le parseur utilise des heuristiques de composition (un mot, majuscules, %, groupes), protège certaines expressions et récupère des fermetures manquantes. Le mode OCR ajoute un contrôle typé des délimiteurs ; l’inspection générale compte surtout les profondeurs. Les parents composites sont ignorés pour le matching ; seuls les enfants décident. Les ingrédients finaux sont dédoublonnés par ID, inconnus par normalisation ; les tokens gardent ordre/parent, mais sans spans source. **Conditions :** parent inconnu avec sous-composition connue mais incomplète, groupe qualificatif ambigu, répétitions portant des origines différentes. **D :** portée du verdict limitée à la sous-composition interprétée ; contexte d’occurrence non représenté dans l’agrégat final. `HierarchyAnalysisTest` teste explicitement la délégation d’un parent inconnu à des enfants connus ; ce comportement est voulu par les tests, pas un bug nouvellement démontré. **R :** exactitude pour compositions incomplètes et parenthèses mal typées hors OCR. Ne pas durcir ces règles sans décision métier et tests.

### P2-5 — Traces et présence : règle saine, frontières dépendantes du texte

**F/T :** `C/LabelSectionExtractor.extract/findMarkers/traceEnd`, `LabelPreprocessor.extractNotices`, `runAnalysis`. Les traces sont hors verdict et la présence déclarée est réelle. L’extracteur prend la première section ingrédients et le premier marqueur `CONTAINS`; la fin d’une trace dépend d’une ponctuation ou de certains marqueurs. **Conditions :** plusieurs déclarations distinctes « contient » séparées, disparition d’un point, notice de traces imbriquée ou frontière non reconnue. **D :** fusion, inclusion de prose ou exclusion d’une portion qu’il faudrait analyser ; la direction de l’erreur dépend du texte. `DiagnosticStabilizationTest` et `OcrChosenTextRegressionTest` réussissent pour traces séparées, lait et ambiguïtés connues. `CrossContactTest`, `Version066TraceLanguageTest`, `LabelSectionExtractorTest` existent. **R :** transitions trace -> présence et plusieurs déclarations sans ponctuation ; aucun incident nouveau affirmé.

### P2-6 — Couverture linguistique non uniforme et deux formats de mappings

**F :** `C/LabelLexicon`, `LabelLanguageSegmenter`, `OcrBlockLanguageClassifier`, `MultilingualIngredientLexicon.resolve/load`, `A/OcrTextSelection`. Codes détectés, titres reconnus, vocabulaire classifiant, options UI et alias n’ont pas le même périmètre. FR/NL/EN/DE sont les langues UI ; IT/ES ont peu d’alias ; PL est sélectionnable mais absent du lexique historique ; codes scandinaves ne signifient pas analyse lexicale complète. Le résolveur cherche un alias entier dans la langue fournie et ignore les `mappings` structurés. **Conditions :** texte mixte, mot non mappé, langue choisie par vocabulaire dominant, mise à jour limitée aux mappings. **D :** inconnus supplémentaires ou mapping attendu non actif ; disponibilité de `cereals` non garantie. Les tests multilingues et de mappings sont présents ; `OcrMultilingualSelectionTest` réussit sur ses cas. **R :** taux de couverture sur corpus FR/NL/EN/DE et langues supplémentaires ; ne pas déduire ce taux des nombres d’entrées.

### P2-7 — Contrats distincts entre analyse principale et revue

**F/T :** `C/MultilingualIngredientAlignment.parseOccurrences/sourceItemText` parse directement l’extrait et reconstruit le texte source par recherche de sous-chaîne. Il ne reprend pas toute la chaîne `LabelPreprocessor -> OcrIngredientNormalizer` du service. En cas d’échec de recherche, il retourne le texte parsé comme source de secours. La revue n’agrège pas les décisions, et ses propositions ne sont pas générées actuellement. **Conditions :** pourcentages/corrections/répétitions/parenthèses rendant le texte parsé différent de la surface, traduction partielle ou ordre modifié. **D :** différence de reconnaissance entre revue et résultat principal ; provenance textuelle approximative possible. Les neuf tests de génération relancés protègent notamment répétitions, listes partielles, ordre et absence de correction sur seules ancres. **R :** parité détaillée revue/analyse sur assets réels ; un test injecté de `EXPLICIT_TRANSLATION` ne suffit pas.

### P2-8 — Cycle de vie et échec de chargement

**F :** `MainActivity.onCreate`, `A/OcrFirstScreenContent`, `OcrProcessor.close`. Les sessions, champs et analyses sont en `remember`; pas de `rememberSaveable`, ViewModel, SavedStateHandle ni persistance de session dans ce chemin. Le singleton de connaissance peut survivre à une recréation d’Activity, pas à un arrêt de processus. Le scope UI est annulé au dispose, et le scope OCR est annulé à la fermeture ; cela ne démontre pas l’annulation de la tâche ML Kit déjà soumise ni tous les chemins de libération bitmap. Le chargement en échec laisse l’analyse désactivée sans diagnostic visible. **Conditions :** recréation/process death, sortie pendant calcul, asset invalide ou inaccessible. **D :** perte de travail ou attente non expliquée ; risque de ressource transitoire à vérifier lors d’annulation. **R :** rotation/recréation, arrêt/reprise et erreurs forcées. Aucun test de restauration/session lifecycle trouvé dans les tests inspectés ; aucune fuite mesurée.

### P3-1 — Diagnostic exporté incomplet ou contexte mélangé

**F :** `A/OcrProcessor`, `OcrSession.withOcrResult/selectLanguage/selectFullText`, `OcrExportReport.build`, `OcrFirstScreenContent.analyse`. `selectableBlockCount` est initialement le nombre de blocs segmentés, pas le nombre réel d’options filtrées. Les warnings géométriques sont perdus. Le rapport n’exporte pas toute la revue ni les coordonnées. Les analyses manuelles sont ajoutées à la même session que l’OCR, alors que l’éditable exporté reste celui de la session OCR. `withOcrResult` conserve les analyses précédentes si la session n’a pas été réinitialisée. **Conditions :** alternance modes manuels/OCR, réextraction dans une même session, blocs non sélectionnables. **D :** compteurs trompeurs ou export mélangeant des acquisitions/contextes, bien que les textes exacts des snapshots restent distingués. Les tests d’export de `OcrSessionTest` réussissent sur leurs états construits ; **R** pour exports de transitions UI réelles.

### P3-2 — Points de coût CPU/mémoire à examiner, sans mesure de lenteur

**F :** `C/runAnalysis` recrée `IngredientMatcher(database)` à chaque analyse ; son constructeur normalise les alias et compile leurs regex. `match` parcourt les alias pour chaque token. `MultilingualIngredientLexicon.resolve` parcourt des listes et renormalise des formes ; la recherche du concept dans la base est linéaire. L’alignement compare les paires de blocs et recrée aussi un matcher. `A/OcrProcessor` construit une reconstruction géométrique pourtant non utilisée pour l’éditable ; `OcrTextReconstructor` comporte des recherches de rangées/colonnes et sous-listes. Les deux nettoyages sont effectués. Les snapshots sans limite explicite retiennent texte, diagnostic sérialisé et objets structurés ; l’export se construit dans le callback UI. Décodage, mise à l’échelle, orientation et crop peuvent faire coexister plusieurs bitmaps malgré les dimensions bornées et le recyclage.

**Conditions :** longues étiquettes, nombreux alias/blocs, historique de session important, image volumineuse. **D :** allocations et coût croissant possibles. Aucun temps utilisateur, jank, pic mémoire ou fuite n’a été mesuré ici ; aucun benchmark lancé. Les dispatchers et dimensions bornées sont des protections observées. L’index expérimental n’est pas une optimisation active du matcher. La priorité réelle de ces coûts reste **R**.

## 6. Forces à préserver

1. Déterminisme et décisions locales : la même connaissance et le même texte suivent des règles explicites ; aucun service génératif dans le verdict.
2. Dépendance `app -> mutation-core` et capacité à tester le service métier sur JVM sans Android.
3. Distinction disponibilité de liste, compatibilité vegan, classification détaillée et résultats conditionnels ; ne jamais confondre végétarien et vegan.
4. Traces exclues et présence déclarée prise en compte ; inconnus et incertitudes bloquent une confirmation complète selon les contrats actuels.
5. Texte exact du champ soumis en OCR, refus des entrées multi-listes ambiguës, vérification d’identité après édition, et absence de remplacement automatique par une traduction.
6. Brut ML Kit inspectable, édition explicite et snapshots avec texte exact, diagnostic structuré et rendu réellement calculé.
7. Arbre/parents, qualifications d’origine limitées aux cibles attendues et protections contre les matches animaux abusifs dans des expressions végétales.
8. Revue multilingue séparée de la décision, refus des inférences fondées uniquement sur position/ancres et liaison stricte d’une éventuelle édition acceptée.
9. IO et calcul métier déportés, aperçu borné, EXIF/miroirs pris en compte et recyclage explicite de bitmaps.

## 7. Tests examinés et validation réellement exécutée

### 7.1 Inventaire de présence et nature

Comptage statique des fichiers Kotlin et occurrences `@Test` :

| Source set | Fichiers | Occurrences `@Test` | Interprétation |
|---|---:|---:|---|
| `mutation-core/src/test` | 22 | 148 | Tests JVM, incluant aussi profils/mesures ; pas 148 tests exécutés ici |
| `app/src/test` | 48 | 328 | Tests JVM locaux, métier/façade et fonctions OCR pures |
| `app/src/androidTest` | 11 | 51 | Instrumentation Android/Compose/OCR et aussi mesures ; aucun exécuté ici |

Ces nombres ne sont pas un taux de couverture ni un inventaire exhaustif de cas JUnit paramétrés.

| Parcours / risque | Tests présents utiles | Niveau et limite |
|---|---|---|
| Texte OCR choisi, ambiguïté et frontières | `OcrChosenTextRegressionTest`, `OcrBoundarySafetyTest`, `OcrMultilingualSelectionTest` | JVM, relancés ; entrées texte et modèles, pas photographie réelle |
| Brut / nettoyage / ordre / géométrie | `OcrTextCleanerTest`, `OcrNativeOrderRegressionTest`, `OcrTextReconstructorTest`, `OcrCropGeometryTest`, `OcrImageSizingTest`, `OcrThreadingTest` | JVM présents ; certains tests reproduisent une chaîne pure et non l’appel Android complet |
| Parsing / hiérarchie / sections | `IngredientTreeParserTest`, `HierarchyAnalysisTest`, `ParserModulesTest`, `LabelSectionExtractorTest`, `ImplicitIngredientList095Test`, `NoIngredientList094Test` | JVM présents ; règles et corpus bornés |
| Inconnus, origine, explication, traces | `DiagnosticStabilizationTest`, `DiagnosticOutput093Test`, `UncertainUnknownVisibility097Test`, `PossibleOriginNoteTest`, `CrossContactTest`, `VerdictExplanation069Test` | Trois premiers groupes de diagnostic/visibilité relancés selon détail ci-dessous ; autres présents |
| Sessions et revue | `OcrSessionTest`, `MultilingualIngredientAlignmentTest`, `MultilingualIngredientAlignmentGenerationTest` | Modèles/évaluations construits ; session et génération relancées, pas l’ensemble |
| UI et transitions | `MainScreenInstrumentedTest`, `OcrChosenTextInstrumentedTest`, `VerdictExplanationInstrumentedTest` | Présents : champ édité, stale OCR/réanalyse, mode, erreur, incertains, traces ; quelques tests injectent écran/service/évaluation, à distinguer du lanceur réel |
| Android / OCR | `OcrFixturesInstrumentedTest`, `OcrTextCleanerInstrumentedTest`, `RealLabelsInstrumentedTest`, `OcrChosenTextInstrumentedTest` | Le premier appelle le vrai `OcrProcessor` sur quatre fixtures et vérifie des mots essentiels ; les tests RealLabels appellent essentiellement le moteur sur du texte, malgré leur emplacement instrumenté |
| Index / performance | `KnowledgeIndexTest`, `IndexedCandidateProviderTest`, `Phase2ParityBenchmarkTest`, `Phase4MatcherProfileTest`, variantes instrumentées et benchmark multilingue | Preuves sur composants/mesures seulement ; aucun lancé, aucune adoption runtime déduite |

### 7.2 Commandes exécutées et résultats

Toutes lancées depuis le dépôt principal via le wrapper Windows ; les tâches de test ont bien été exécutées, les compilations étaient majoritairement `UP-TO-DATE`.

```powershell
.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.OcrBoundarySafetyTest --tests com.example.isitvegan.OcrChosenTextRegressionTest --tests com.example.isitvegan.MultilingualIngredientAlignmentGenerationTest --tests com.example.isitvegan.DiagnosticStabilizationTest --console=plain
.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.OcrSessionTest --tests com.example.isitvegan.UncertainUnknownVisibility097Test --tests com.example.isitvegan.OcrMultilingualSelectionTest --console=plain
.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.DiagnosticOutput093Test --console=plain
python tools/build_ingredients.py --check
```

| Groupe | Cas exécutés | Résultat |
|---|---:|---|
| `OcrBoundarySafetyTest` | 6 | Réussite, aucun échec/erreur/skip |
| `OcrChosenTextRegressionTest` | 9 | Réussite, aucun échec/erreur/skip |
| `MultilingualIngredientAlignmentGenerationTest` | 9 | Réussite, aucun échec/erreur/skip |
| `DiagnosticStabilizationTest` | 6 | Réussite, aucun échec/erreur/skip |
| `OcrSessionTest` | 14 | Réussite, aucun échec/erreur/skip |
| `UncertainUnknownVisibility097Test` | 1 | Réussite, aucun échec/erreur/skip |
| `OcrMultilingualSelectionTest` | 19 | Réussite, aucun échec/erreur/skip |
| `DiagnosticOutput093Test` | 5 | Réussite, aucun échec/erreur/skip |
| Projection/copie des assets | Contrôle `--check` | Réussite |

Total : **69 tests JVM**, 0 échec constaté. Les XML des deux exécutions core ont été examinés à leur issue ; la seconde exécution ciblée remplace les résultats de tâche précédents sur disque. Aucun échec n’a été corrigé. Ni suite complète, ni `assembleDebug`, ni compilation instrumentée dédiées n’ont été lancées. Les dépendances de compilation réutilisées par les tests ne constituent pas une preuve d’exécution UI ou de packaging release.

### 7.3 Preuves encore manquantes

- Édition manuelle après résultat : disparition ou identification explicite de l’ancien verdict.
- Analyse lente / extraction en vol puis effacement, nouvelle session, texte identique, autre bloc et changement de langue ; injection contrôlée de retours dans le désordre.
- Recréation d’Activity, arrêt de processus, annulation pendant décodage/ML Kit et absence de chargement réussi.
- Photo réelle : picker -> EXIF/miroir -> recadrage -> OCR -> sélection -> édition -> verdict -> partage, avec inspection du texte exact à chaque frontière.
- Même langue répétée avec meilleur bloc ultérieur ; zéro/une option et accès au texte complet.
- Résidu inconnu masqué par le filtre d’affichage, présence déclarée inconnue, liste partielle à fin perdue et groupes parenthétiques hors corpus.
- Parité et provenance de chaque occurrence entre revue multilingue et analyse ; aucun parcours de génération de correction lexicale explicite établi actuellement.
- Couverture linguistique sur un corpus représentatif et preuve de fonctionnement après installation hors ligne ; aucune conclusion quantitative à partir du seul lexique.

## 8. Recommandations ordonnées pour la reprise v0.7

1. **Fixer le contrat texte/contexte/résultat avant d’enrichir les langues.** Définir une identité de session et de requête, incluant texte, mode, langue et sélection ; tester les retours tardifs. Généraliser l’identification des résultats obsolètes aux modes manuels. Garder ces changements dans un chantier séparé avec tests de transition ; cet audit ne les applique pas.
2. **Arbitrer la visibilité des inconnus et les sorties de verdict.** Documenter précisément la différence entre inconnu décisionnel, résidu et élément affiché ; garantir une explication visible de chaque obstacle à une confirmation. Verrouiller aussi les couples lait + inconnu/incertain et la lecture vegan/végétarienne sans modifier implicitement les invariants.
3. **Conserver les preuves avant de réécrire la segmentation.** Définir des occurrences distinctes du contenu, des spans entre versions du texte et un lien optionnel vers géométrie/acquisition. Conserver séparément texte ML Kit, transformations, bloc choisi et texte soumis. Prévoir le cas de blocs identiques et de plusieurs blocs de même langue.
4. **Rendre le choix de texte complet et de bloc cohérent.** Vérifier que le bloc initial est représenté parmi les options, permettre de distinguer les occurrences de même langue, auditer les coupes et la complétude. Ne pas laisser une heuristique de qualité devenir une preuve de liste complète.
5. **Clarifier la revue multilingue livrée.** Assumer aujourd’hui corroborations/ambiguïtés et inspection d’occurrences. Si des corrections sont voulues, ajouter une source de liens explicites vérifiables et une validation de provenance ; ne pas réactiver la déduction par seule position. Choisir quels prétraitements partagent revue et analyse, et tester leurs divergences.
6. **Rationaliser les contrats de données.** Distinguer alias actifs et mappings structurés ; contrôler cibles absentes/collisions et générés en CI. Conserver les données éditoriales comme référence, sans transformer automatiquement une cible absente ou une origine variable en statut certain.
7. **Renforcer les preuves Android ciblées.** Traiter les scénarios de section 7.3 sur émulateur autorisé ou autre cible explicitement choisie, sans dépendre du Nokia. Séparer UI injectée, moteur sur texte et acquisition photo réelle dans les comptes rendus.
8. **Mesurer seulement après sécurisation fonctionnelle, dans un mandat distinct.** Si nécessaire, étudier la réutilisation du matcher/normalisations, le coût de reconstruction et la rétention d’historique. Ne pas adopter l’index expérimental sur la seule base de son nom, de sa compilation ou de tests de candidats ; exiger parité du service appelé en production.
9. **Réduire la dette de maintenance et d’observabilité.** Clarifier le rôle de l’ancien écran, les noms de tests devenus trompeurs, les compteurs réellement sélectionnables, les erreurs de chargement et les règles de conservation de l’historique/export.

## 9. Conclusion indépendante avant comparaison documentaire

Le socle métier partagé est effectivement branché sur l’application. L’index et certaines capacités de correction multilingue ne le sont pas comme mécanismes de décision courants. Le parcours OCR conserve le texte choisi et refuse plusieurs situations ambiguës ; les tests ciblés le confirment. La base est embarquée et ses assets générés sont à jour selon l’outil local.

La reprise v0.7 doit prioriser la cohérence de session et l’explicabilité du texte considéré : obsolescence manuelle, retours tardifs, inconnus affichés et décisionnels, identité/provenance des occurrences et sélection de listes partielles. L’audit ne démontre ni lenteur mesurée, ni fuite mémoire, ni fiabilité photo globale. Les constats statiques restent distincts des reproductions encore nécessaires.

## 10. Comparaison documentaire après clôture de l’analyse indépendante

Source comparée seulement après rédaction des sections précédentes : [V0_7_MULTILINGUAL_ARCHITECTURE_AND_DEVELOPMENT_PLAN_20261030.md](../preparation/V0_7_MULTILINGUAL_ARCHITECTURE_AND_DEVELOPMENT_PLAN_20261030.md). Ce plan cite le HEAD `6c34424a7e8e07116c7a0516f0425724b488ffc9`, différent du HEAD audité ; ses preuves historiques ne sont pas transférées automatiquement à l’état présent.

**Accords.** Les deux lectures établissent le cœur JVM actif, l’ordre natif ML Kit conservé, la géométrie non propagée jusqu’à la session, l’identité de contenu insuffisante pour les occurrences, l’index non adopté, la piste NL probable non raccordée et l’absence de correction lexicale générée par le parcours photo. Le besoin de provenance continue et de révisions de session est confirmé. La conservation additive du document OCR proposée en T1 est cohérente avec une limite observée ; son implémentation n’est pas livrée au HEAD audité.

**Réserves et compléments.** La synthèse du plan sur les résultats périmés et la visibilité des inconnus doit être lue avec les limites P1-1 et P1-3 : les protections OCR ne couvrent pas l’édition manuelle, et certains inconnus décisionnels sont retirés des listes visibles. Cet audit place ces deux contrats et les retours tardifs avant l’extension fonctionnelle. Il précise aussi l’écart possible entre bloc initial et première option d’une langue, l’absence de choix « texte complet » avec au plus une option et le défaut d’erreur visible au chargement. La vue de compatibilité vegan du formatteur n’est pas activée par l’appel de l’écran courant ; il faut distinguer capacités du formatteur et affichage effectif.

**Non confirmé ici.** Les rejeux historiques sur émulateurs, échecs historiques de suite complète et gains de profilage mentionnés dans le plan ne sont ni reproduits ni réfutés. Les 69 tests ciblés de cet audit ne les résolvent pas. Les modèles proposés (`SourceSpan`, `VerifiedComposition`, opérations de revue), la complétude globale visée et le découpage T1–T7 restent des propositions à arbitrer et valider, pas des faits de l’architecture actuelle. Aucune mesure Nokia ni preuve photo complète n’est ajoutée par cet audit.
