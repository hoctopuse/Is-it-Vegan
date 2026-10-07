# UE 853/2004 — corrections des protections R1 et R2

Date : 7 octobre 2026. **Décision de correction : READY_FOR_INDEPENDENT_REVIEW.**

Les contre-épreuves de préservation historique et de compatibilité du lexique sont maintenant refusées, avec une couverture négative exécutée. Cette décision concerne les corrections techniques ; elle ne lève pas elle-même le NO_GO pour un commit. Une nouvelle revue indépendante et une décision produit distincte sur R3 restent nécessaires.

## 1. Préflight et périmètre préservé

Branche initiale et finale : `master` (`master...origin/master`). HEAD initial et final : `d1f02275508c9472b1fe55ac82d792d1257f6264`. Index sans changements au début et à la fin ; aucun commit, reset, checkout ou nettoyage.

Au départ : onze fichiers suivis modifiés et treize fichiers non suivis, dont les quatre PDF 853/2004, les rapports/CSV précédents et les fichiers de l'intégration. Les modifications retrouvées correspondent au périmètre de la revue indépendante : un concept, quatre entrées lexicales et mappings, deux sources, leurs projections et documentation, l'importeur et ses tests. Aucun écart bloquant n'a été trouvé avant correction. Les anciens objets de connaissance ont été comparés à HEAD ; les rapports ont servi d'historique, pas de preuve de l'état courant.

Instructions consultées : `AGENTS.md`, skill `knowledge-import-validation`, `source-adapters.md`, `import-workflow.md`, invariants et conventions de connaissance/verdict, puis skill `android-validation` pour le périmètre ciblé. Les deux rapports demandés, l'importeur complet, le chargeur `MultilingualIngredientLexicon`, `TextNormalizer` et leurs tests ont été examinés avant correction.

Un inventaire SHA-256 en mémoire couvrait les **407 fichiers présents, suivis et non suivis non ignorés**, au début de la tâche. À la fin, **403 sont identiques**, quatre sont intentionnellement modifiés et sept fichiers sont ajoutés, dont ce rapport : 414 fichiers au total. Aucun original n'est supprimé. Les répertoires ignorés de sortie Gradle sont exclus de cet inventaire ; les tests ciblés y ont produit leurs résultats ordinaires.

Les trois JSON éditoriaux, les deux actifs Android, les documents générés, tous les PDF, les anciens rapports/CSV, les sources et le builder préexistant restent identiques à l'état de départ. Aucun code de production Kotlin, matcher, moteur, OCR, règle, enum, schéma, configuration de build ou version n'est modifié. Les quatre formes et leur statut `NON_VEGAN`, leur provenance et les propriétaires historiques, notamment `meat`, restent inchangés.

## 2. Fichiers de cette correction

| Fichier | Intervention pendant cette tâche |
|---|---|
| `app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt:45` | Appel de la garde indépendante avant le contrôle de synchronisation et traitement explicite de l'exception historique |
| `tools/import_eu_food_hygiene_regulation.py:55`, `:134`, `:143`, `:212`, `:250` | Profil de compatibilité runtime, langues, corrections OCR protégées, parsing JSON strict et refus Unicode prudent avant toute annonce de conformité |
| `tools/tests/test_eu_food_hygiene_import.py:55`, `:198` | Extension des fixtures CLI et sept tests des modes sans écriture ; anciens tests conservés |
| `docs/knowledge-pipeline.md` | Garanties, limites, migration intentionnelle de la référence et profil de compatibilité documentés |
| `tools/validate_knowledge_history.py` | Nouvelle garde indépendante, uniquement `--check` et `--stdin` |
| `tools/testdata/knowledge_history_d1f0227.json` | Nouvelle référence historique issue du commit identifié, avec empreinte fixée |
| `tools/tests/test_knowledge_history_guard.py` | Huit tests Python de préservation, positifs et négatifs |
| `app/src/test/java/com/example/isitvegan/KnowledgeValidationTestSupport.kt` | Support de test : sérialisation des mutations en mémoire et invocation de la garde réelle |
| `app/src/test/java/com/example/isitvegan/KnowledgeHistoryGuardTest.kt` | Six tests Kotlin de la garde, sur données mutées en mémoire |
| `app/src/test/java/com/example/isitvegan/FoodHygieneLexiconCompatibilityTest.kt` | Six tests du chargeur/résolveur Kotlin réel et du dispatcher Python |
| Ce rapport | Nouveau livrable de correction, sans écrasement des rapports précédents |

Les quatre premières lignes désignent des fichiers déjà modifiés ou non suivis avant cette tâche. Les sept dernières sont les nouveaux fichiers de cette correction. Le nombre de fichiers suivis modifiés reste onze ; aucun fichier n'a été ajouté à l'index.

## 3. R1 — préservation indépendante de l'historique

### Référence et garanties

La référence provient des deux JSON éditoriaux lus avec `git show d1f02275508c9472b1fe55ac82d792d1257f6264:<chemin>`, avant l'ajout 853/2004. Elle ne provient pas d'un total recalculé sur le corpus courant. Elle contient les 487 IDs canoniques, 2 090 signatures de surfaces lexicales, 2 089 signatures de mappings complets et 19 signatures de corrections OCR historiques.

Chaque signature est le SHA-256 d'un JSON UTF-8 avec clés triées, séparateurs déterministes et nombres non finis interdits. L'empreinte sémantique de l'ensemble de référence, fixée dans le validateur, est :

`043a1c61b3226e88e6e1d251d9c7b10e268fa6ed0d6400a8710d9706e5289f1b`

Les signatures lexicales portent sur propriétaire, langue, nature `aliases`/`ocrVariants` et texte exact. Un mapping est signé intégralement, y compris ses preuves imbriquées et toute propriété supplémentaire. La garde exige la présence de chaque occurrence historique par comparaison de multisets ; elle accepte de nouveaux enregistrements et l'extension d'une liste d'alias sans remplacer les signatures historiques. Elle protège également la disponibilité des IDs et les corrections OCR historiques. Elle ne certifie pas tous les autres champs des ingrédients canoniques : statut et comportement métier restent soumis aux autres contrôles.

L'unique entrée de concept indisponible admise est l'objet exact : `{"canonicalId":"cereals","language":"NL","aliases":["granen"],"ocrVariants":[]}`. Ce n'est ni un filtre général des concepts absents ni une permission pour `gran`, une autre langue ou une autre variante. La liste des entrées indisponibles doit être exactement cette exception unique.

Dans le test agricole, cette garde précède le décompte calculé. Le décompte vérifie la synchronisation des surfaces déclarées et mappings ; la référence vérifie leur identité et leur préservation. Les assertions antérieures de parité des actifs, unicité, langues et statut sont conservées. Les ajouts futurs n'exigent pas de changer un total figé.

### Couverture négative et évolution

Les tests Python et Kotlin suppriment ensemble l'entrée lexicale `e100/EN` et son mapping, changent le propriétaire d'un mapping historique, altèrent ses métadonnées/preuves, retirent `e100` des concepts disponibles, puis retirent aussi ses entrées lexicales/mappings. Tous ces états sont refusés. Les mutations portent uniquement sur des copies en mémoire. Les changements de propriété inconnue d'un mapping historique sont également refusés.

Les ajouts synthétiques légitimes d'un concept, d'une entrée lexicale et d'un mapping sont acceptés. Le test Python vérifie aussi l'ajout d'une surface à une liste existante. Le cas `cereals/NL/granen` actuel passe ; les variantes et un autre concept indisponible échouent. La référence dont le contenu est altéré échoue à son contrôle d'empreinte.

Pour une modification historique intentionnelle : faire examiner le diff, identifier une nouvelle révision de référence, mettre à jour ensemble signatures et empreinte fixée, documenter les changements précis et leur raison. Aucun rafraîchissement automatique depuis l'état courant n'est proposé. La procédure est documentée dans `docs/knowledge-pipeline.md`.

## 4. R2 — compatibilité du lexique avec le runtime

La validation intervient après lecture des JSON et **avant** l'annonce « batch is current », la proposition ou toute écriture. Elle vérifie explicitement la version numérique 1, les structures et champs typés, les langues reconnues par le chargeur, les clés d'alias normalisées, les doublons de corrections et leurs entrées/sorties non vides. Le parsing JSON refuse les clés dupliquées et constantes numériques étrangères au JSON.

Les codes et noms longs de langues correspondent aux conventions du chargeur actuel ; leur casse est acceptée, sans ajouter de nettoyage de langue absent du runtime. Une correction OCR dont l'entrée normalisée est l'une des quatre formes approuvées ne peut pas changer cette surface normalisée. Le contrôle s'applique dans **toute langue sélectionnable**, pas seulement la langue du PDF. Une redirection est refusée avec la langue, la surface et sa cible dans le message.

L'absence de collision entre alias n'est pas suffisante : la correction `separatorvlees` → `rundvlees` est acceptée structurellement par le chargeur, mais le vrai résolveur Kotlin renvoie alors `meat`. Le nouveau contrôle refuse cet état avant de déclarer le lot conforme. La correction indépendante `separatovlees` → `separatorvlees` et la correction conservant la clé normalisée `SEPARAT\u034fORVLEES` → `Separatorvlees` sont acceptées ; les quatre propriétaires restent corrects dans le vrai runtime.

Le profil Python est volontairement plus strict que certaines conversions permissives Kotlin : par exemple `1.9` est refusé, même si `toInt()` donnerait 1. Une redirection vers une autre surface du même concept est aussi refusée, faute de décision sur cette nouvelle transformation. Il n'y a aucune réparation silencieuse.

### Normalisation : preuve et limites

Un test compare la fonction Python au vrai `TextNormalizer` Kotlin pour **4 230 entrées** : toutes les surfaces alias/variantes OCR et mappings courantes, toutes les entrées/sorties des corrections OCR, plus cinq cas ciblés. Cette comparaison a été exécutée avec Python 3.14.7 / Unicode 16.0.0 et la JVM fournie par Android Studio (JDK 25.0.3).

Les caractères non assignés par la version Unicode Python (`Cn`) et surrogates isolés (`Cs`) sont refusés avant normalisation, au lieu de présumer une équivalence avec la JVM. Le cas `U+0378` est testé négativement dans les deux modes. La parité du corpus courant ne démontre pas l'équivalence universelle de toutes les versions Unicode et de toute entrée future ; un changement de contrat ou de répertoire pris en charge exige une nouvelle preuve Kotlin et une revue du profil. Le CLI Python ne lance pas lui-même Kotlin : une réussite de `--check` est un contrôle statique renforcé, complété ici par les tests runtime réellement exécutés.

## 5. Contre-épreuves observées avant et après

| Cas | Avant correction : preuve effectivement observée | Après correction : preuve effectivement observée |
|---|---|---|
| Suppression coordonnée de `e100/EN` et mapping | L'ancien total calculé donne 2 092 attendus et 2 092 présents ; il accepte cette perte | Garde : `historical lexical surfaces missing or changed: 1 record(s)` ; dispatcher non nul et tests Python/Kotlin négatifs réussis |
| `schemaVersion: 2` | Vrai dispatcher `--check` sur substitution en mémoire : code 0, lot déclaré courant | Code 1 : `unsupported runtime schemaVersion; expected numeric version 1` ; vrai chargeur Kotlin refuse aussi |
| OCR `separatorvlees` → `rundvlees` | Vrai dispatcher `--check` sur substitution en mémoire : code 0, lot déclaré courant | Code 1 : `OCR correction redirects approved surface NL/'separatorvlees' to 'rundvlees'` ; Kotlin montre le changement de propriétaire vers `meat` |
| Propriétaire, métadonnées/preuve historique altérés | Insuffisance du décompte constatée par inspection ; pas de nouvelle exécution avant correction revendiquée pour ces cas | Tests des objets signés : tous refusés, y compris propriété supplémentaire |
| Concept historique retiré, avec ou sans retrait coordonné des lignes | Insuffisance du filtre des concepts disponibles constatée par inspection | Tests Python/Kotlin : refus dans les deux situations |
| Ajout légitime / exception établie | Référence historique identifiée à HEAD | Tests positifs : ajouts admis et exception exacte conservée ; variantes d'exception refusées |

Les trois premières lignes sont des contre-épreuves effectivement reproduites pendant cette tâche avant correction. Les autres observations avant correction sont de l'inspection, pas des tests rétrospectivement revendiqués.

## 6. Validations exécutées

| Commande / contrôle | Résultat réellement obtenu et effet |
|---|---|
| `python -B -X utf8 -m unittest discover -s tools/tests -p test_knowledge_history_guard.py -v` | 8 tests réussis ; dernier passage 1,537 s, après ajout du retrait coordonné des lignes d'un concept supprimé. Copies en mémoire, CLI `--stdin` |
| Sélection `unittest` du nouvel importeur, détaillée ci-dessous | 15 méthodes réussies, dernier passage 55,938 s. Fixtures temporaires, vrai dispatcher ; aucun `--write` exécuté |
| `gradlew.bat :app:testDebugUnitTest` avec les quatre filtres ci-dessous | BUILD SUCCESSFUL, 35 s ; 24 tests distincts : garde historique 6, compatibilité 6, agricole 7, hygiène 5 ; zéro échec, erreur ou test ignoré dans les XML inspectés |
| Même tâche filtrée uniquement sur `FoodHygieneLexiconCompatibilityTest` après le dernier renforcement Python | BUILD SUCCESSFUL, 14 s ; 6 tests, zéro échec/erreur/ignoré. XML final horodaté `2026-10-07T15:34:32.773Z` |
| `python -B -X utf8 tools/import_eu_food_hygiene_regulation.py --check` | Code 0 : `CELEX 02004R0853: changes=0; no modification necessary; batch is current` |
| Même outil avec `--dry-run` | Code 0, même message ; aucune proposition nouvelle, écriture ou régénération |
| `python -B -X utf8 tools/validate_knowledge_history.py --check` | Code 0 : `Historical knowledge preserved; approved additions are allowed; exception=cereals/NL/granen` |
| `git diff --check` et lecture du diff complet concerné | Code 0 ; seuls avertissements LF/CRLF habituels. Aucun changement indexé |
| Inventaires SHA-256 avant/après tâche et avant/après les trois commandes directes | Aucun changement hors des quatre fichiers autorisés ; les sept fichiers de connaissance/actifs/documents générés suivis séparément restent identiques |

Les quatre filtres du premier passage Gradle sont `com.example.isitvegan.KnowledgeHistoryGuardTest`, `com.example.isitvegan.FoodHygieneLexiconCompatibilityTest`, `com.example.isitvegan.AgriculturalProductsRegulationImportTest`, `com.example.isitvegan.FoodHygieneRegulationImportTest`, chacun passé avec `--tests`. `JAVA_HOME` a été défini dans le processus vers `C:\Program Files\Android\Android Studio\jbr`. Le dernier passage ne réexécute que la compatibilité ; il ne faut pas le présenter comme une nouvelle exécution des 24 tests.

Le test agricole exécute les checks ciblés des modes historiques `--animal-enrichment` et `--meat-species`. L'importeur historique n'est pas modifié. Le test hygiène existant vérifie les quatre résolutions `EXACT`, le statut, le verdict détaillé et le bloqueur, ainsi que les comportements voisins et la caractérisation R3 ; il est resté byte-identique.

### Sélection Python reproductible, sans appel en écriture

Les cinq méthodes préexistantes suivantes appellent aussi `--write` sur fixtures : elles sont conservées mais **non exécutées** pour respecter l'interdiction de cette tâche. Leurs cas check/dry-run nécessaires sont couverts par les nouvelles méthodes sans écriture.

```python
import unittest, sys
excluded = {
    'test_partial_or_orphan_batch_fails',
    'test_current_check_and_dry_run_are_idempotent',
    'test_write_only_appends_expected_batch_and_preserves_history',
    'test_missing_or_divergent_source_fails',
    'test_cross_language_canonical_ocr_and_mapping_collisions_fail',
}
suite = unittest.defaultTestLoader.loadTestsFromName(
    'tools.tests.test_eu_food_hygiene_import.FoodHygieneImportTest')
selected = unittest.TestSuite(t for t in suite if t._testMethodName not in excluded)
result = unittest.TextTestRunner(verbosity=2).run(selected)
sys.exit(0 if result.wasSuccessful() else 1)
```

Cette sélection a été exécutée par script inline PowerShell vers `python -B -X utf8 -`. Les fixtures JSON sont créées dans le répertoire temporaire système ; le dispatcher ne peut appeler `Path.write_text`/`write_bytes` dans les modes sans écriture, et les contenus avant/après sont comparés. Les huit PDF réels sont vérifiés une fois par préparation de suite avant mise en cache des preuves pour les fixtures. Les tests Kotlin substituent le JSON lexical en mémoire et réutilisent les preuves des mappings inchangés ; ils testent le vrai chargeur/résolveur et dispatcher, pas une nouvelle acquisition PDF dans chaque cas.

### Empreintes des données et sorties préservées

Ces empreintes sont identiques à l'entrée de la tâche et après les commandes finales :

| Fichier | SHA-256 |
|---|---|
| `knowledge/ingredients.json` | `301f375dcd19efe9e275dcab0f8e34d732a15d7e2008a78ae924021867884291` |
| `knowledge/ingredient_aliases_multilingual.json` | `3952c31957690a2d998dc2a2bae740e01d434824fe31ea01fd555d1aa52992e2` |
| `knowledge/sources.json` | `a289977b6d857288f10476322b0c81f8c70c27c7388d166667975016da21b798` |
| `app/src/main/assets/ingredients.json` | `78dccb4b02dbfc14921fcf40c62228fbf5dcdb8294b63a1c2dcdae05fd1a3825` |
| `app/src/main/assets/ingredient_aliases_multilingual.json` | `3952c31957690a2d998dc2a2bae740e01d434824fe31ea01fd555d1aa52992e2` |
| `docs/generated/base-connaissances-data.md` | `2de288d0247b73e573ec9e5de8702dd8341dfb23ab2fc4a86af35b514a8dbc5e` |
| `docs/generated/multilingual-ingredient-mapping.md` | `21cd3a00256e5f384bc5ae0eef510e98766c201c1a11c74993e54bdc52aa3ac0` |

## 7. Limites et R3

`sans viandes séparées mécaniquement` peut toujours produire `NON_VEGETARIAN` avec le concept animal comme bloqueur. La caractérisation existante réussit ; cela démontre le comportement actuel, **pas** sa sécurité sémantique. R3 est distinct de la préservation R1 et de la compatibilité R2. Il demande une décision produit séparée et des exemples adaptés de listes réelles, mentions négatives et produits d'imitation. Aucun alias, parsing, règle de négation, matcher, moteur ou portée OCR n'a été changé pour le masquer. Le comportement actuel ne doit pas être décrit comme un arrêt au premier animal : le test existant conserve `stoppedAtNonVegetarian == false`.

Pas d'import `--write`, builder, génération ou régénération, même sur fixtures ; pas de suite Gradle complète, PIT, téléphone Nokia, benchmark, installation ou téléchargement. Les tests peuvent créer leurs fichiers temporaires et sorties de compilation habituels, sans modifier les sources de connaissance. Aucune nouvelle revue visuelle PDF, étude exhaustive d'étiquettes ou réévaluation de licence n'est revendiquée. Les builders n'ont pas été nécessaires : données et actifs sont byte-identiques, la parité et les tests métier ciblés ont été exécutés.

Une vérification finale en lecture seule a rencontré une expiration du contrôle automatique d'autorisation avant démarrage ; une relance a ensuite réussi. Cela n'est ni un échec de validation du corpus ni une preuve de risque de la commande.

**READY_FOR_INDEPENDENT_REVIEW** : les protections R1/R2 disposent de contre-épreuves pertinentes et validées, avec leurs limites explicites. La nouvelle revue devra examiner la référence historique, son évolution, le profil strict de validation, les preuves runtime et les résultats. Elle devra maintenir R3 visible avant toute décision de commit ; aucune autorisation de commit ou d'élargissement du corpus n'est donnée ici.
