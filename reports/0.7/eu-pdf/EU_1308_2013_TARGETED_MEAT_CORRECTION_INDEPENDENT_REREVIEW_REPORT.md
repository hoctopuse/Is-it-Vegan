# Relecture indépendante des corrections du NO_GO viande UE

Date : 2026-10-07. **Verdict : GO dans le périmètre de cette relecture.** Les trois défauts P2 initiaux sont corrigés, couverts par des cas négatifs pertinents et validés à nouveau. Les deux P3 sont également résolus. Aucun défaut matériel restant trouvé ; le verdict du rapport de correction n’a pas été utilisé comme preuve.

## État réel et méthode

HEAD vérifié avant/après : `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`, branche `master`, aucun fichier staged. État initial : huit fichiers suivis modifiés (deux JSON éditoriaux, deux actifs Android, deux documents générés, importeur et test Kotlin) ; neuf rapports/CSV non suivis et le fichier de tests Python non suivi. Tous sont préservés. Seul ce nouveau rapport est ajouté par cette relecture ; aucune correction ni aucun commit.

Instructions relues : `AGENTS.md`, skill `knowledge-import-validation`, références `source-adapters.md` et `import-workflow.md`, invariants et schéma de `docs/base-connaissances.md`. Skill `android-validation` consulté pour le contrôle JVM ciblé. Rapports initial, correction, intégration et décisions consultés ; comparaison directe du code courant, tests, JSON, actifs et CSV. Les constats ci-dessous reposent sur ces fichiers et sur les nouvelles exécutions, pas sur les résultats annoncés dans les anciens rapports.

Une empreinte SHA-256 des **393 fichiers préexistants** suivis ou non suivis a été prise avant les contrôles. Des scripts de lecture ont comparé les données au HEAD et aux preuves CSV. Aucun import en écriture ni générateur n’a été exécuté. Gradle a écrit ses sorties habituelles de build/test ; aucune connaissance ni aucun actif Android n’a été écrit par les chemins inspectés.

## Constats classés et preuves

### P2 initial 1 — contrôle d’un lot absent/incomplet : RÉSOLU

**Fichiers/lignes :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), 439–485 ; [tests Python](../../../tools/tests/test_eu_meat_species_import.py), 22–32, 44–89.

La branche partielle lève `ValueError` avant construction ; le lot absent sous `args.check` déclenche `SystemExit` avec un message, donc un code non nul. Le lot complet vérifie concepts, alias et mappings avant d’afficher `changes=0` et de retourner. Le statut divergent est rejeté par les comparaisons du concept.

Les tests exécutent `m.main()` du module réel dans un sous-processus, avec `sys.argv` contenant `--meat-species` et le mode demandé. Seuls les chemins d’entrée sont redirigés vers un `TemporaryDirectory`, et l’extraction PDF est remplacée par une preuve mise en cache après une extraction réelle en `setUpClass`. Ce n’est ni un faux dispatcher ni une simple assertion sur un helper. Les codes de sortie sont effectivement assertés. Absent, partiel, divergent et conforme deux fois passent leurs assertions lors de cette relecture.

**Gravité restante : aucune. Impact :** `--check` ne peut plus accepter le lot entièrement absent dans ce chemin ; les lectures de l’état conforme n’écrivent pas.

### P2 initial 2 — provenance complète : RÉSOLU

**Fichiers/lignes :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), 392–398 et 472–481 ; [tests Python](../../../tools/tests/test_eu_meat_species_import.py), 91–122.

Les mappings attendus sont des objets entiers, triés seulement pour neutraliser l’ordre des enregistrements. La comparaison finale conserve tous les champs réels : concept, langue, formes, source, relation, groupe, confiance et liste des preuves. L’égalité des dictionnaires/listes compare aussi toutes les propriétés imbriquées et les propriétés supplémentaires inconnues. Aucun sous-ensemble de champs ne masque la provenance au moment de décider la conformité.

Les tests réexécutés altèrent puis suppriment chacun des neuf champs de mapping, notamment `source`, `relation`, `mappingGroup`, `confidence` et `sourceEvidence`. Ils altèrent/suppriment les sept propriétés de preuve (fichier, page, rubrique, offset, forme, ID de revue, empreinte) et ajoutent un champ inconnu au mapping. Tous sont rejetés. Certains champs structurels absents échouent plus tôt par exception ; le processus reste non nul, sans écriture. L’ajout d’un champ inconnu dans une preuve n’a pas de cas dédié, mais son rejet découle directement de la comparaison complète observée.

**Gravité restante : aucune. Impact :** les champs auparavant ignorés ne permettent plus de déclarer conforme un mapping corrompu ; une nouvelle propriété nécessite une décision explicite du vérificateur.

### P2 initial 3 — collisions et retour anticipé : RÉSOLU

**Fichiers/lignes :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), 382–389, 401–436, 454, 527 et 532–534 ; [normaliseur Kotlin](../../../mutation-core/src/main/kotlin/com/example/isitvegan/TextNormalizer.kt), 6–17 ; [chargeur Kotlin](../../../mutation-core/src/main/kotlin/com/example/isitvegan/MultilingualIngredientLexicon.kt), 175–184 et 188–218 ; [tests Python](../../../tools/tests/test_eu_meat_species_import.py), 124–170.

L’index de propriétaires est global, sans séparation par langue, et comprend noms canoniques, alias, numéros E, alias linguistiques et variantes OCR. Les candidats du lot sont aussi indexés entre eux. L’index lexical compte séparément les doublons de même langue/surface, y compris du même propriétaire. Les codes et noms de langues acceptés par le chargeur sont regroupés, avec conversion en majuscules.

Le contrôle est appelé **avant** la branche du lot présent, donc avant son retour anticipé, et une seconde fois sur le résultat construit en mémoire, **avant** la branche d’écriture. Le défaut initial de contrôle après import est donc supprimé, et une collision de construction n’atteint pas les écritures.

Les opérations de normalisation reproduisent la séquence Kotlin : minuscules, ligatures œ/æ, NFD, suppression de toutes les marques Unicode M, remplacement des caractères hors ASCII alphanumérique, trim et code E initial. Le helper historique est laissé intact. Les cas communs exécutés en Python et Kotlin couvrent ligatures, accents, espaces, code E initial/interne et U+034F, que `combining()` seul aurait manqué.

Les cas négatifs réexécutés couvrent un alias `geitenvlees` EN détenu par `meat` face au candidat NL, avant import puis après import (`--check` et `--dry-run`), noms/alias/numéros E concurrents, variante OCR avec marque Unicode, doublon du même propriétaire, alias orphelin créant un doublon et synonyme de langue `dutch`. Ils sont tous rejetés avec les diagnostics de collision attendus.

**Gravité restante : aucune pour les P2 examinés. Impact :** les collisions interlangues initialement manquées sont désormais bloquées, y compris sur un lot déjà importé. Ce contrôle ciblé ne prétend pas remplacer toutes les règles structurelles Kotlin ou garantir les tables Unicode de toutes les versions futures ; le chargement Kotlin du corpus actuel est effectivement réexécuté dans le test Gradle.

### P3 initial 4 — dry-run silencieux : RÉSOLU

**Fichiers/lignes :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), 482–483 et 528–534 ; [tests Python](../../../tools/tests/test_eu_meat_species_import.py), 172–182.

Le message du lot conforme est désormais inconditionnel : `changes=0; imported batch is current; no modification necessary`. Le lot absent en dry-run annonce `changes=1` et la proposition de 17 alias linguistiques. Les deux tests passent. Seul `args.write` mène à `write_text` ; aucun appel au builder/régénérateur n’est présent. Dans les sous-processus de test, `Path.write_text`/`write_bytes` sont interdits et les fichiers temporaires sont comparés avant/après.

**Gravité restante : aucune. Impact :** l’utilisateur dispose d’un résultat explicite sans modification ni génération.

### P3 initial 5 — matching et diagnostics : RÉSOLU

**Fichier/lignes :** [test Kotlin](../../../app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt), 56–103.

Les 17 valeurs attendues sont des formes et IDs littéraux. Les assertions passent par `multilingualLexicon.resolve`, puis le véritable `IngredientMatcher.match` sur la forme entière : liste exclusive du concept attendu, `MatchResolution.EXACT` et résidu vide. Le pipeline réel `service.analyzeWithDiagnostics` vérifie la liste exclusive, zéro inconnu, `NON_VEGETARIAN`, `decision.responsibleIngredientIds`, `verdictExplanation.knownBlockingIngredients` et les IDs reconnus dans les tokens. Les cinq statuts ingrédient `NON_VEGAN` sont également assertés.

Les attendus ne sont pas calculés par l’importeur ni déduits du résultat testé. L’API des tokens ne publie pas le résidu du matcher ; celui-ci est donc asserté directement sur `IngredientMatch`, tandis que les inconnus sont contrôlés sur le pipeline. Les 17 cas passent réellement pendant cette relecture.

**Gravité restante : aucune. Impact :** une résolution partielle, un mauvais concept ou un mauvais bloqueur fait désormais échouer le test ciblé.

## Préservation du périmètre et mode historique

- Comparaison au HEAD : seuls les cinq IDs autorisés sont nouveaux ; les 482 concepts historiques et les préfixes historiques du lexique sont identiques. Les 17 mappings actuels ont été confrontés indépendamment au CSV de proposition : ID/concept, langue, forme exacte, fichier, page, rubrique, offset et empreinte concordent. Source, relation, groupe et confiance sont vérifiés contre des attendus littéraux. Répartition FR 3, NL 5, EN 6, DE 3 ; aucun élargissement observé.
- Comparaison source/actifs : cinq concepts, statuts, noms, alias, raisons et source concordent ; le lexique éditorial et l’actif sont identiques octet pour octet.
- Le manifeste local conservé au début de la correction (`meat_correction_snapshot.json`, hors dépôt) a aussi été relu : seuls importeur et test Kotlin diffèrent parmi ses 391 fichiers. Cela corrobore la préservation pendant la correction, mais ce manifeste temporaire n’est pas une révision Git historique. La conformité actuelle a été vérifiée séparément contre le HEAD et le CSV.
- Aucun diff de matcher, moteur, enums, schéma, règles contextuelles ou builders. Les fonctions et constantes historiques de l’importeur concordent avec le HEAD par AST ; cette comparaison est seulement un élément de preuve.
- Le dispatch réel ([importeur](../../../tools/import_eu_agricultural_products_regulation.py), 536–543) refuse les deux lots simultanément et appelle directement `animal_import` lorsque seul `--animal-enrichment` est choisi. Les nouveaux helpers ne sont alors pas appelés. `--animal-enrichment --check` et `--dry-run` ont été **réexécutés**, succès et proposition vide. Le test Gradle réexécute aussi le check historique et les cas historiques de matching de sa classe. Ces résultats ne constituent pas une validation globale de tous les modes ni de toute l’application.

## Validations nouvellement exécutées

| Commande / lecture | Résultat de cette relecture |
|---|---|
| `python -B -m unittest discover -s tools/tests -p test_eu_meat_species_import.py -v` | **17 tests, OK**, 52,953 s, avec sous-cas négatifs réellement exécutés. |
| `.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest --rerun-tasks` | **BUILD SUCCESSFUL**, 49 s. XML neuf : **7 tests, 0 échec, 0 erreur, 0 ignoré**, `2026-10-07T09:38:43.782Z`. Le filtre reste limité à cette classe ; les tâches nécessaires de compilation/build sont réexécutées. |
| `python -B tools/import_eu_agricultural_products_regulation.py --meat-species --check` | Code 0, lot actuel conforme, `changes=0`. |
| Même script, `--meat-species --dry-run` | Code 0, aucun changement nécessaire explicitement annoncé. |
| Même script, `--animal-enrichment --check` | Code 0, `changes=0`, additions toutes à zéro. |
| Même script, `--animal-enrichment --dry-run` | Code 0, `changes=0`, proposition vide. |
| Lectures JSON/CSV, comparaison au HEAD, actifs, AST et dispatch ; `git diff --check` | Conforme ; aucune erreur d’espacement. |
| Empreintes, HEAD, index et état Git avant/après | **393 fichiers préexistants inchangés** ; seul ce rapport est ajouté. |

Les résultats anciens des rapports ne sont pas comptés comme tests nouvellement exécutés. Aucun nouveau test ad hoc n’a été ajouté ni aucun fichier de code/test corrigé pendant la relecture.

## Limites et décision

Non exécutés : suite Gradle complète, PIT, Nokia/autre appareil, import en écriture, builder ou régénération, rendu visuel PDF, revue exhaustive d’étiquettes, réévaluation de licence, vérification distante de consolidation. Les checks et la campagne Python ont bien refait la vérification locale des preuves PDF existante ; la comparaison CSV a été effectuée indépendamment de leur construction dans l’importeur.

Les risques sémantiques déjà documentés pour descriptions, négations et substituts restent distincts de la conformité du contrôle. Une reconnaissance exacte n’est pas une preuve universelle de présence animale. La portée ciblée et les limites Unicode futures sont documentées, sans anomalie concrète trouvée dans le corpus courant.

**GO : le NO_GO initial peut être levé pour les défauts P2/P3 examinés.** Aucun blocage matériel restant identifié dans ce périmètre. Les données et le moteur sont conservés ; le commit reste à la charge de l’utilisateur.
