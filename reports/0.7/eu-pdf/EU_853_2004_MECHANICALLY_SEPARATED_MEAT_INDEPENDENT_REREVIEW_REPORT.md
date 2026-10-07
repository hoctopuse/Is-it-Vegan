# UE 853/2004 — seconde revue indépendante après corrections R1/R2

Date : 7 octobre 2026. **TECHNICAL_FIXES_VALIDATED. Décision pour un commit : NO_GO en attente d'une décision produit explicite sur R3.**

Les deux protections techniques initialement bloquantes sont rétablies et leurs contre-épreuves sont refusées. Aucun défaut matériel restant de R1/R2 n'a été trouvé dans le périmètre inspecté. Cette validation ne démontre pas la sécurité sémantique des mentions négatives. Elle ne vaut ni autorisation automatique de commit, ni nouvelle décision d'import.

## 1. État réel, fichiers examinés et méthode

- Branche initiale/finale : `master...origin/master` ; HEAD initial/final : `d1f02275508c9472b1fe55ac82d792d1257f6264`.
- Index sans changements. Onze fichiers suivis modifiés, vingt fichiers non suivis présents à l'ouverture, aucun suivi supprimé. Diff suivi : 464 insertions, une suppression.
- Inventaire SHA-256 en mémoire de **414 fichiers suivis/non suivis non ignorés** avant les vérifications. Ces 414 fichiers sont identiques à la fin ; seul ce rapport est ajouté, soit 415 fichiers. Les sorties ignorées de tests Gradle et les fixtures temporaires système ne font pas partie de cet inventaire.
- Le diff suivi et les fichiers nouveaux correspondent à l'intégration initiale et aux corrections décrites. Aucune modification de production, de configuration ou de connaissance supplémentaire n'a été découverte. Les rapports ont été confrontés aux JSON, à HEAD, aux scripts et aux tests, sans prendre leurs verdicts comme preuves.

Instructions lues : `AGENTS.md`, skill `knowledge-import-validation`, `source-adapters.md`, `import-workflow.md`, conventions/invariants de connaissance et verdict ; skill `android-validation` pour les tests ciblés. Aucun agent supplémentaire, accès réseau, installation ou téléchargement.

Fichiers examinés intégralement ou par analyse structurée complète pour les grands JSON :

- Les trois rapports demandés ; `docs/knowledge-pipeline.md`, les sections pertinentes de `docs/base-connaissances.md`, `docs/verdict.md` et `docs/sources/eu-food-hygiene-regulation.md`.
- `tools/validate_knowledge_history.py`, toute la référence `tools/testdata/knowledge_history_d1f0227.json`, `tools/tests/test_knowledge_history_guard.py`.
- `KnowledgeHistoryGuardTest.kt`, `KnowledgeValidationTestSupport.kt`, `AgriculturalProductsRegulationImportTest.kt`, `FoodHygieneLexiconCompatibilityTest.kt`, `FoodHygieneRegulationImportTest.kt` sous `app/src/test/java/com/example/isitvegan/`.
- `tools/import_eu_food_hygiene_regulation.py` et `tools/tests/test_eu_food_hygiene_import.py`, y compris les branches de mutation non exécutées.
- `MultilingualIngredientLexicon.kt`, `TextNormalizer.kt`, le parseur `MiniJson` dans `OriginQualifierRules.kt`, le chemin `VeganAnalyzer.kt`, `VerdictEngine.kt`, `LabelLanguageSegmenter.kt`, sous `mutation-core/src/main/kotlin/com/example/isitvegan/`.
- Les trois JSON éditoriaux, les deux actifs Android, leurs versions à HEAD ; le diff du builder de documentation et des documents générés, les règles de projection de `tools/build_ingredients.py`.
- Les huit PDF locaux utilisés par l'acquisition : 853/2004 consolidé au 07.05.2026 et 1169/2011 au 01.04.2025, FR/NL/EN/DE. Vérifications texte/hashes/pages/offsets, sans nouveau rendu visuel.

Cette revue ne peut pas recréer de manière indépendante tous les états intermédiaires non versionnés de l'intégration et de la correction. Elle vérifie l'état courant contre HEAD, les empreintes publiées et de nouvelles exécutions. Les anciens inventaires en mémoire et durées non journalisées ne sont pas une preuve indépendante de ces instants.

## 2. R1 — référence historique indépendante : corrigé

### Origine et intégrité vérifiées

Une sonde indépendante a lu les deux JSON **directement avec `git show` au commit annoncé**, reconstruit les enregistrements et signatures sans appeler le calcul de signatures du validateur, puis comparé l'objet de référence entier. Égalité exacte :

| Ensemble historique | Nombre |
|---|---:|
| IDs de concepts | 487 |
| Surfaces lexicales, alias et variantes OCR | 2 090 |
| Objets complets de mapping | 2 089 |
| Objets de correction OCR | 19 |

Toutes les signatures sont des SHA-256 valides. L'empreinte sémantique recalculée de la référence est bien `043a1c61b3226e88e6e1d251d9c7b10e268fa6ed0d6400a8710d9706e5289f1b`, identique à celle fixée dans `tools/validate_knowledge_history.py:17`. Les métadonnées de référence, dont révision et version de format, participent à cette empreinte. Une altération sémantique sans mise à jour délibérée du digest est refusée. L'empreinte est sémantique : reformatage, ordre des clés JSON et fins de ligne ne sont pas protégés octet pour octet ; elle n'est pas une signature d'authenticité résistante à une modification concertée du code et de la référence.

### Propriétés réellement garanties

`lexical_records` (`:27`) signe propriétaire, langue, nature alias/variante OCR et surface exacte. `require_signatures` (`:42`) compare des multisets de signatures, pas seulement des tailles. Les mappings sont signés comme objets complets : tous leurs champs, champs supplémentaires et preuves imbriquées participent au contrôle. Les corrections historiques complètes et la disponibilité des IDs sont également préservées (`:48–64`). La référence est indépendante du corpus courant ; des ajouts peuvent être acceptés sans changer l'attendu.

L'exception d'indisponibilité est l'objet exact unique `cereals/NL/granen`, sans variante OCR (`:18–19`, `:56–60`). Les cas `gran`, autre langue, autre concept absent, variante OCR supplémentaire, duplication et suppression de l'exception sont refusés. Aucune filtration générale des concepts devenus indisponibles n'est utilisée.

Dans `AgriculturalProductsRegulationImportTest.kt:47`, la garde est effectivement invoquée avant le décompte. Le helper (`KnowledgeValidationTestSupport.kt:33–42`) transmet les JSON à un **processus réel** `validate_knowledge_history.py --stdin` et exige son succès. Le contrôle de cardinalité qui suit reste un contrôle de synchronisation ; sa protection historique provient désormais de la référence, pas de son attendu recalculé. Les anciennes assertions de parité, unicité, langues, statuts et importeurs restent présentes.

Les tests Kotlin mutent effectivement les données chargées en mémoire et invoquent ce helper : suppression coordonnée, propriétaire/métadonnées/preuve altérés, concept retiré avec ou sans ses lignes, ajout admis et exception bornée. Les cas positifs vérifient que la garde est opérationnelle ; les tests Python et sondes complémentaires vérifient aussi les messages d'erreur, évitant de confondre une indisponibilité de Python avec une détection correcte.

### Contre-épreuves indépendantes réexécutées

Treize appels supplémentaires au vrai CLI `--stdin`, uniquement en mémoire : **deux succès attendus et onze refus attendus**.

| Mutation | Résultat |
|---|---|
| Corpus courant avec les quatre ajouts | Code 0 |
| Retrait simultané de `e100/EN` et mapping | Code 1, `historical lexical surfaces missing or changed: 1 record(s)` |
| Propriétaire historique modifié | Code 1, `historical mapping objects missing or changed` |
| SHA-256 d'une preuve imbriquée historique altéré | Code 1, même contrôle d'objet complet |
| Propriété supplémentaire sur un mapping historique | Code 1 |
| Langue lexicale historique changée ; alias déplacé en variante OCR | Code 1 dans les deux cas |
| `e100` absent ; puis absent avec toutes ses lignes retirées | Code 1 dans les deux cas, ID indisponible indiqué |
| Exception `cereals` retirée ou dupliquée ; autre concept indisponible | Code 1 dans les trois cas |
| Changement de statut de `olive_oil` sur une copie en mémoire | Code 0, conformément à la limite annoncée de cette garde |

Les ajouts synthétiques d'un concept/alias/mapping et l'extension d'une liste existante sont également admis par les suites réexécutées. La garde **ne protège pas les statuts, noms, raisons, sources ou alias du JSON canonique** par signature : elle protège ses IDs et les données lexicales/mappings décrites. Ces autres propriétés nécessitent les contrôles de données et métier. Le contrôle des IDs ne doit pas être présenté comme une protection complète du sens des concepts. Une nouvelle correction OCR additionnelle n'est pas interdite par R1 uniquement parce qu'elle est nouvelle.

La procédure de migration intentionnelle (`docs/knowledge-pipeline.md:72`) exige une révision identifiée, revue du diff, mise à jour conjointe référence/digest et documentation. Elle ne permet pas un rafraîchissement automatique pour faire passer les tests. Le CLI de la garde ne possède aucun mode de génération ou d'écriture.

## 3. R2 — compatibilité runtime : corrigé dans le profil documenté

### Contrats inspectés

`validate_runtime_lexicon` est appelé dans `integrate` (`tools/import_eu_food_hygiene_regulation.py:254`), **avant** toute déclaration de lot courant et avant mutation. Le parsing strict (`:212–224`) refuse les clés JSON dupliquées, y compris imbriquées, comme `MiniJson` (`OriginQualifierRules.kt:399–415`). Les langues correspondent aux sept langues réellement chargeables par le lexique, avec leurs noms longs et codes ; ce n'est pas l'ensemble plus large de l'enum `LabelLanguage`.

La version doit être numériquement 1, sans booléen, chaîne, valeur absente ou autre valeur. Le runtime exige un `Number` dont `toInt()` vaut 1 (`MultilingualIngredientLexicon.kt:119`) : **Python est volontairement plus strict**, notamment pour `1.9`. Il refuse aussi les listes et champs mal typés que Kotlin peut filtrer ou convertir silencieusement. Ces divergences sont prudentes et documentées, pas une prétendue équivalence totale des parseurs.

Le chargeur Kotlin retire le suffixe parenthétique, cherche une correction dans la langue choisie sur la base normalisée, applique au plus la première correction puis cherche l'alias (`:64–90`). Le pipeline utilise ensuite son ID et nom canonique (`VeganAnalyzer.kt:353–365`). Le contrôle Python ne tente pas de reconstruire tout ce pipeline : il interdit toute correction dont l'entrée normalisée est l'une des quatre bases et dont la sortie normalisée diffère, dans toutes les langues chargeables. Les identités normalisées et corrections d'autres entrées restent permises. Aucune chaîne de corrections n'est présumée, aucun stemming ou synonymie n'est ajouté.

Le test `FoodHygieneLexiconCompatibilityTest.kt:78` charge réellement le lexique contenant `separatorvlees → rundvlees` et observe le propriétaire `meat` dans le résolveur Kotlin. Les deux modes Python refusent ensuite ce lexique. Les tests positifs (`:57`, `:91`) vérifient dans le même vrai résolveur les quatre propriétaires, le typo indépendant et l'identité comportant `U+034F`. Ils ne calculent pas leur attendu depuis l'importeur. Les cas `schemaVersion: 2`, langue inconnue, correction vide/dupliquée et clé JSON dupliquée sont également exercés contre le vrai chargeur.

Les fixtures Python utilisent de vrais fichiers temporaires et le vrai dispatcher `main()`. Elles remplacent uniquement l'acquisition après vérification des huit PDF. Le helper Kotlin substitue le JSON lexical en mémoire et reprend les preuves des mappings courants ; ce helper ne constitue pas une nouvelle preuve d'acquisition PDF. Les modes sans écriture interdisent `Path.write_text`/`write_bytes` ; les fixtures comparent aussi leur contenu avant/après. L'outil ne répare pas les lots divergents : égalité complète du lot/provenance, refus non nul, absence d'annonce « current ».

### Sondes indépendantes supplémentaires

**80 appels** au vrai dispatcher sur JSON substitués en mémoire : **6 succès et 74 refus attendus**, avec acquisition fraîche des huit PDF puis cache de ces seules preuves.

- Pour chacun des deux modes `--check`/`--dry-run` : les quatre formes redirigées vers `rundvlees` dans les sept langues FR/NL/EN/DE/IT/ES/PL sont refusées, soit 56 cas négatifs.
- Les cinq versions `2`, `null`, `"1"`, `true`, `1.9` sont refusées dans les deux modes.
- La redirection `SEPARAT\u034fORVLEES → rundvlees`, `U+0378` et un surrogate isolé `U+D800` sont refusés dans les deux modes.
- Le lot courant, `separatovlees → separatorvlees` et `SEPARAT\u034fORVLEES → Separatorvlees` sont acceptés dans les deux modes.
- Une clé racine `schemaVersion` dupliquée est refusée dans les deux modes.

Messages observés : `unsupported runtime schemaVersion; expected numeric version 1`, `OCR correction redirects approved surface NL/'separatorvlees' to 'rundvlees'`, `unverified Unicode codepoint U+0378`, `unverified Unicode codepoint U+D800`. Tous les refus empêchent l'annonce du lot courant. Aucun appel d'écriture n'est autorisé dans ces sondes.

### Normalisation : couverture et limites exactes

Python et Kotlin appliquent, pour les formes testées : minuscules, développement de `œ/æ`, NFD, retrait des marques Unicode, regroupement des caractères hors ASCII alphanumérique en espaces, trim, fusion du préfixe E-number initial. `U+034F` est bien supprimé comme marque malgré sa classe combinatoire nulle.

Le test `FoodHygieneLexiconCompatibilityTest.kt:124` compare réellement les sorties Python au **vrai `TextNormalizer` Kotlin**, sur 4 230 entrées. Ce nombre est recalculé indépendamment : surfaces alias/variantes OCR, surfaces de mappings, entrées/sorties des corrections, cinq cas ciblés. Il comprend des répétitions : il ne désigne pas 4 230 connaissances ni caractères distincts. Python 3.14.7 / Unicode 16.0.0 ; JVM Android Studio utilisée pour les tests locaux.

Le refus `Cn`/`Cs` (`importeur:61–64`) empêche certains cas non vérifiés de passer silencieusement. Il ne prouve pas une équivalence universelle de toutes les lettres assignées, versions Unicode, règles de casse, chaînes futures ou runtimes Android. La comparaison porte sur le corpus courant et les cas explicitement exercés dans la JVM locale. Un changement du corpus ou du contrat doit réexécuter ces tests et analyser les nouveaux caractères. Une réussite du CLI seul ne certifie ni l'ensemble du runtime ni la sécurité sémantique d'une étiquette ; la documentation (`docs/knowledge-pipeline.md:86–98`) conserve correctement cette limite.

## 4. Données et état d'intégration

Comparaison structurée exhaustive à HEAD : les 487 anciens ingrédients, 21 sources, 1 703 entrées lexicales, 2 089 mappings et les corrections/propriétés racines antérieures sont des préfixes ou objets identiques. État courant : 488 concepts, 23 sources, 1 707 entrées lexicales, 2 093 mappings. Seuls ajouts de connaissance : le concept autorisé, quatre entrées et mappings, deux sources. Le propriétaire de `meat`, ses alias, les cinq espèces historiques et les concepts voisins restent inchangés.

Les deux actifs sont cohérents : projection complète des ingrédients selon les champs/defaults du builder inspecté ; lexique éditorial et Android identiques octet pour octet. Les sept SHA-256 publiés dans le rapport de correction pour JSON, actifs et documents générés correspondent aux fichiers actuels. Aucun diff de production Kotlin, OCR, matcher, règle, enum, moteur ou version n'existe.

| Langue | Forme lexicale exclusive | Preuve 853 : p.16 / I.1.14, offset | Preuve 1169 : p.49 / VII.B.18, offset |
|---|---|---:|---:|
| FR | `viandes séparées mécaniquement` | 2215 | 1764 |
| NL | `separatorvlees` | 2047 | 1563 |
| EN | `mechanically separated meat` | 1951 | 1468 |
| DE | `Separatorenfleisch` | 2101 | 1690 |

Les huit empreintes, paginations (94 et 60), en-têtes de langue/date/CELEX, surfaces source et offsets sont revérifiés via l'acquisition et les checks. Les différences de casse source sont conservées dans les preuves. Les mappings complets et sources sont contrôlés par égalité. Aucun sigle, espèce, flexion, `greaves` ou `frog_legs` n'est ajouté aux données. Leur présence dans les tests d'exclusion n'est pas un alias.

Le test métier réexécuté charge les actifs réels et vérifie, dans chaque langue : propriétaire, statut `NON_VEGAN`, `EXACT`, singleton du concept, résidu vide, aucun inconnu, verdict détaillé `NON_VEGETARIAN`, ID de décision et des bloqueurs/tokens. Le token historique `meat` reste `EXACT` avec son propre ID. Le voisinage est comparé avec/sans le concept ; ce témoin ne remplace pas la référence historique ni une preuve de sûreté de tout substitut.

## 5. Commandes exécutées, résultats et effets

| Vérification réellement exécutée pendant cette revue | Résultat et effet |
|---|---|
| `git status --short --branch`, `git rev-parse HEAD`, `git diff --cached --stat`, `git diff --stat`, `git diff -- <fichiers>` | État initial/final et inspection des onze diffs ; index vide |
| `rg`, `rg --files`, `Get-Content -Encoding UTF8` ciblés | Lecture des instructions, rapports, code, tests et documentation |
| Scripts inline `python -B -X utf8 -`, `git show <HEAD>:<JSON>` | Référence reconstruite indépendamment, audit de données/actifs, hashes, 13 sondes CLI R1 et 80 sondes du vrai dispatcher R2 ; aucune fixture dans le dépôt |
| `python -B -X utf8 -m unittest discover -s tools/tests -p test_knowledge_history_guard.py -v` | **8 tests OK**, 2,125 s ; mutations en mémoire |
| Sélection sans écriture de `FoodHygieneImportTest`, ci-dessous | **15 tests OK**, 58,566 s ; fixtures système temporaires, acquisition PDF réelle puis cache, writes CLI interdits |
| `gradlew.bat --offline :app:testDebugUnitTest` avec les quatre filtres ci-dessous | **BUILD SUCCESSFUL en 31 s**, **24 tests**, zéro échec/erreur/ignoré ; sorties Gradle ignorées habituelles, aucune génération de connaissance |
| `python -B -X utf8 tools/import_eu_food_hygiene_regulation.py --check` et `--dry-run` | Codes 0, `CELEX 02004R0853: changes=0; no modification necessary; batch is current` ; aucune écriture |
| `python -B -X utf8 tools/validate_knowledge_history.py --check` | Code 0, historique préservé et exception explicite |
| `git diff --check`, `git diff --cached --name-only` | Code 0 ; uniquement avertissements LF/CRLF, aucun staging |
| Inventaires SHA-256 avant/après | Les 414 fichiers de départ restent présents et identiques ; seul ce rapport est nouveau |

Gradle utilise `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` pour le processus, sans changer la configuration. Filtres `--tests` : `com.example.isitvegan.KnowledgeHistoryGuardTest`, `com.example.isitvegan.FoodHygieneLexiconCompatibilityTest`, `com.example.isitvegan.AgriculturalProductsRegulationImportTest`, `com.example.isitvegan.FoodHygieneRegulationImportTest`. Le test agricole exécute les checks réels `--animal-enrichment` et `--meat-species` de l'importeur historique ; ils réussissent. Cet importeur est inchangé. La tâche ciblée réutilise les compilations à jour et exécute les tests ; aucune suite complète ou PIT.

XML **nouveaux**, effectivement lus dans `app/build/test-results/testDebugUnitTest/` :

| Classe | Tests | Horodatage UTC | Échec / erreur / ignoré |
|---|---:|---|---|
| AgriculturalProductsRegulationImportTest | 7 | 2026-10-07T17:25:42.546Z | 0 / 0 / 0 |
| FoodHygieneLexiconCompatibilityTest | 6 | 2026-10-07T17:25:53.314Z | 0 / 0 / 0 |
| FoodHygieneRegulationImportTest | 5 | 2026-10-07T17:26:03.718Z | 0 / 0 / 0 |
| KnowledgeHistoryGuardTest | 6 | 2026-10-07T17:26:05.310Z | 0 / 0 / 0 |

Avant cette relance, seul le XML du dernier passage compatibilité du rapport de correction était disponible : six tests sans échec, `2026-10-07T15:34:32.773Z`. Il corrobore ce passage historique, pas les 24 résultats antérieurs ni leurs durées. Les réussites actuelles ci-dessus sont de **nouvelles exécutions**. Les sorties Python historiques n'ont pas de journal durable identifié permettant d'authentifier leurs durées déclarées ; la fonctionnalité et les nombres sont maintenant reproduits.

La sélection Python exécutée charge `tools.tests.test_eu_food_hygiene_import.FoodHygieneImportTest`, puis exclut ces cinq méthodes contenant des appels `--write` : `test_partial_or_orphan_batch_fails`, `test_current_check_and_dry_run_are_idempotent`, `test_write_only_appends_expected_batch_and_preserves_history`, `test_missing_or_divergent_source_fails`, `test_cross_language_canonical_ocr_and_mapping_collisions_fail`. Elle appelle `unittest.TextTestRunner(verbosity=2)` sur les quinze restantes. Les branches sans écriture pertinentes sont couvertes par les méthodes supplémentaires ; aucun appel `--write`, même sur fixture, n'a été exécuté.

Non exécutés : builders, régénération, import en écriture, suite Python comportant des writes, suite Gradle complète, PIT, instrumentation/Nokia, benchmark, étude exhaustive d'étiquettes, nouveau rendu PDF, audit de licence. Les tests ciblés ont été inspectés avant invocation ; leurs écritures se limitent aux fixtures temporaires et sorties ignorées autorisées, pas aux fichiers suivis.

## 6. Affirmations du rapport de corrections

| Affirmation | Statut | Preuve / réserve |
|---|---|---|
| Référence réellement issue du commit annoncé et indépendante du courant | CONFIRMÉ | Reconstruction autonome des listes et du digest depuis Git ; objet entier identique |
| Signatures couvrant propriétaire/langue/surface/type, mapping complet et preuves | CONFIRMÉ | Code, reconstruction, mutants et tests réexécutés |
| Suppressions coordonnées, réattributions et indisponibilités détectées | CONFIRMÉ | Suites Python/Kotlin et sondes CLI indépendantes |
| Ajouts autorisés sans renouveler l'attendu | CONFIRMÉ | Tests positifs, référence inchangée et quatre nouveaux mappings courants acceptés |
| Exception `cereals/NL/granen` exacte et unique | CONFIRMÉ | Objet exact et plusieurs variantes négatives, dont disparition/duplication |
| Garde ne certifiant pas tous les champs canoniques | CONFIRMÉ | Sonde de statut canonique acceptée, limite explicitement documentée |
| Versions/langues/JSON/corrections divergentes refusés avant annonce courante | CONFIRMÉ | Inspection des dispatchers, suites et 80 sondes ; profil strict volontairement distinct des coercitions Kotlin |
| Mauvais propriétaire dû à OCR démontré dans le vrai Kotlin, corrections sûres admises | CONFIRMÉ | Tests runtime réellement exécutés, attendu indépendant de l'importeur |
| Parité de normalisation de 4 230 entrées, sans certification universelle | CONFIRMÉ | Test réel et nombre recompté ; JVM locale et corpus courant uniquement |
| Modes sans écriture et absence de réparation silencieuse | CONFIRMÉ | Code, interdictions de write dans les fixtures/sondes, hashes de tous les fichiers initiaux |
| Historique, quatre formes, provenance, statuts, actifs et production préservés | CONFIRMÉ | État courant : comparaison complète à HEAD, projection/parité et sept empreintes publiées ; les instants intermédiaires de correction ne sont pas tous reconstruisibles |
| 403/407 fichiers préexistants identiques pendant la correction | PARTIELLEMENT CONFIRMÉ | Inventaire final de 414 retrouvé ; sept hashes et diff cohérents, inventaire initial historique non durable |
| 8/15 tests Python et 24 puis 6 Kotlin exécutés avec les durées annoncées | PARTIELLEMENT CONFIRMÉ | Code/nombres et dernier XML historique cohérents ; pas de preuve durable des anciennes durées complètes ; nouvelles exécutions réussies |
| R3 ouvert et caractérisation non assimilée à sûreté | CONFIRMÉ | Test et pipeline réexécutés ; rapport le maintient explicitement |
| Aucune nouvelle vérification juridique revendiquée | CONFIRMÉ | Correction:148 ; attribution présente, avis juridique non réévalué ici |

## 7. Constats par gravité et conditions restantes

| Gravité / état | Emplacement | Preuve observée et impact |
|---|---|---|
| **P2 — R3 ouvert, décision produit requise avant commit** | `FoodHygieneRegulationImportTest.kt:79–90`, `VerdictEngine.kt:27–28` | `sans viandes séparées mécaniquement` produit encore `NON_VEGETARIAN`, responsable `mechanically_separated_meat`, avec résolution partielle/résidu. Le test documente un faux positif construit ; il ne prouve aucune présence animale ni acceptabilité produit |
| P2 initial — R1 corrigé | `validate_knowledge_history.py:16–64`, `AgriculturalProductsRegulationImportTest.kt:45–66` | Référence indépendante, préservation par objets/surfaces et exception bornée ; négatifs validés. Aucun blocage technique restant identifié sur R1 |
| P2 initial — R2 corrigé dans le profil documenté | `import_eu_food_hygiene_regulation.py:143–224`, `:254`, `FoodHygieneLexiconCompatibilityTest.kt:57–151` | Schéma et OCR désormais contrôlés avant succès ; vrai runtime exercé. Ne vaut pas une certification générale de tout futur lexique |
| P3 — limite Unicode/runtime documentée | `import_eu_food_hygiene_regulation.py:55–67`, `FoodHygieneLexiconCompatibilityTest.kt:124`, `docs/knowledge-pipeline.md:86–98` | 4 230 entrées en JVM locale ; `Cn/Cs` refusés. Aucun écart observé sur le lot courant, aucune équivalence universelle ou validation sur appareil revendiquée |
| P3 — réserve juridique conservée | `docs/sources/eu-food-hygiene-regulation.md:35–38`, `import_eu_food_hygiene_regulation.py:84`, première revue:107 | Attribution UE/EUR-Lex, CC BY 4.0 citée et distinction éditoriale présentes. L'avis applicable et sa teneur n'ont pas été revérifiés ; cohérence éditoriale observable, licence non certifiée par cette revue |
| P3 — R4 initial non transactionnel, inchangé | `import_eu_food_hygiene_regulation.py:297–299` | Écritures séquentielles des trois JSON sous write, sans transaction. Branche non exécutée ; risque initial non bloquant, hors corrections R1/R2 et hors usage de cette revue |

R3 demande une décision produit explicite et documentée sur l'acceptabilité de l'exposition nouvelle française, avec des exemples pertinents et les modes d'entrée réellement utilisés. Aucun accord de ce type n'a été identifié dans les rapports fournis. Les exemples actuels sont construits en `MANUAL_INGREDIENT_LIST` ; ni leur fréquence ni le traitement de toutes les étiquettes complètes/OCR ne sont établis. Le token anglais historique `meat` pouvait déjà être bloquant dans l'imitation anglaise. Aucun changement du matcher ou du moteur n'est recommandé ni appliqué dans cette revue.

La réserve juridique de la première revue reste entière : vérifier l'attribution contre l'avis applicable avant commit, ou disposer de sa validation éditoriale identifiée ; ne pas présenter cette revue comme une vérification de licence. Le rapport de correction annonce correctement l'absence de réévaluation. L'analyse parcourt les tokens et conserve les inconnus ; `stoppedAtNonVegetarian=false` reste testé, sans prétention d'arrêt au premier animal.

## 8. Conclusions séparées

1. **TECHNICAL_FIXES_VALIDATED** : R1 et R2 répondent aux défauts initiaux, avec référence indépendante, négatifs pertinents, vrais dispatchers et runtime Kotlin exercés. Les contrôles de synchronisation antérieurs restent présents. Les limites annoncées ne doivent pas être transformées en garanties plus larges.
2. **NO_GO pour un commit à ce stade** : le blocage technique R1/R2 est résolu, mais l'acceptabilité produit de R3 demeure explicitement ouverte. Consigner une décision sur ce risque avant une décision de commit ; conserver aussi la réserve d'attribution/juridique et sélectionner les fichiers du lot individuellement.

Aucun fichier non suivi préexistant, notamment PDF ou ancien rapport, n'a été ajouté à l'index ni inclus automatiquement dans un commit. Aucune correction n'a été appliquée. Les 414 fichiers initiaux sont préservés ; le seul fichier créé est ce rapport, relu après rédaction. Aucun commit.
