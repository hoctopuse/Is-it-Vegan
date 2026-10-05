# EU 1308/2013 — import animal autorisé v0.7

Date : 2026-10-05. HEAD de départ : 6be60933df464dccb857b8d3287825011567344b.

Cette passe exécute le lot explicitement autorisé par l’utilisateur après la revue documentaire. Le CSV de revue et les rapports précédents sont préservés ; leurs valeurs REVIEW_REQUIRED/applied=NO restent le constat historique de leur passe.

Lot : alias sur concepts existants avec décisions PROPOSED_ALIAS_EXISTING_CONCEPT ou LANGUAGE_METADATA_MISSING_CANONICAL_PRESENT, hors exclusions explicites ; nouveaux concepts buttermilk, royal_jelly et propolis. Pollen, toute cire d’abeille/E901, alias génériques crème/beurre, butteroil, matière grasse laitière anhydre et éléments exclus ne sont pas importés.

La convention de l’application classe royal_jelly et propolis comme VEGETARIAN, donc non vegan. Elle ne prétend pas définir une certification végétarienne universelle. Le règlement documente leurs noms et leur contexte apicole alimentaire ; il n’est pas présenté comme l’origine de cette convention. Le record de source éditoriale isitvegan-animal-products-convention-v0-7 distingue les deux responsabilités.

Les 77 formes/langues sélectionnées ont été vérifiées directement contre leur hash PDF, page, langue, sous-chaîne à l’offset et contexte. Les deux graphies skimmed-milk / skimmed milk partagent la même clé normalisée : une forme lexicale et un mapping avec les deux preuves sources, soit 76 clés. La variante reste conservée dans la revue et les preuves ; aucune forme régionale n’est supprimée.

L’ancien blocage d’exécution a été réévalué par une invocation normale de l’importeur après la nouvelle autorisation explicite. Le dry-run historique a été autorisé, lancé et terminé avec exit 0, changes=0. Aucun contournement ou remplacement de constantes à l’exécution.

L’importeur est étendu avec le mode explicite --animal-enrichment, sans modifier son mode historique. Le lot est fixé dans le code à partir des lignes vérifiées, et non ouvert à tout le CSV. Avant mutation : SHA-256 et identité des PDF ; preuves de chaque page ; collisions avec noms et alias sous normalisations importeur/runtime ; préservation des champs et données historiques ; cohérence des mappings. L’écriture reste réservée à --write.

Le dry-run du mode animal a réussi avec exit 0. Plan contrôlé : 3 concepts, 69 alias canoniques, 76 alias multilingues, 76 mappings et une source de convention. Tous les mappings correspondent au lot autorisé et à ses preuves ; aucun changement inattendu ni collision signalé. L’import et les validations finales sont consignés ci-dessous après leur exécution.

## Résultat de l’import

Import terminé : GO_WITH_WARNINGS, uniquement en raison de l’avertissement préexistant missingConcepts = ["cereals"]. Les commandes de validation de cette passe ont réussi après correction des difficultés de lancement/test détaillées ci-dessous. Aucun conflit d’alias ou changement imprévu n’a été retenu.

| Mesure | Avant | Après | Ajout réel |
| --- | ---: | ---: | ---: |
| Concepts du corpus de référence | 479 | 482 | 3 |
| Alias canoniques, memberships de chaînes | 2394 | 2463 | 69 |
| Alias linguistiques, memberships de chaînes | 1990 | 2066 | 76 |
| Entrées du lexique multilingue | 1668 | 1686 | 18 |
| Mappings de provenance déclarés | 1996 | 2072 | 76 |
| Sources éditoriales de convention | — | — | 1 |

Les memberships comptent une chaîne par concept, ou par concept et langue. Une entrée du lexique peut contenir plusieurs chaînes. Ces trois unités ne s’additionnent pas et ne mesurent pas autant de nouveaux ingrédients reconnus.

| Concept cible | Nouveaux alias canoniques | Nouveaux alias linguistiques | Nouveaux mappings |
| --- | ---: | ---: | ---: |
| animal_fat | 20 | 20 | 20 |
| milk | 18 | 18 | 18 |
| edible_offal | 9 | 9 | 9 |
| egg | 4 | 4 | 4 |
| honey | 4 | 4 | 4 |
| casein | 3 | 4 | 4 |
| whey | 1 | 4 | 4 |
| buttermilk | 5 | 5 | 5 |
| royal_jelly | 3 | 4 | 4 |
| propolis | 2 | 4 | 4 |
| Total | 69 | 76 | 76 |

Répartition des 76 alias linguistiques et mappings : FR 18, NL 19, EN 18, DE 21.

Les trois nouveaux concepts sont VEGETARIAN ; une présence reconnue produit donc un résultat non vegan selon les invariants existants, sans changement de moteur. Pour buttermilk, la justification est son origine laitière. Pour royal_jelly et propolis, cette classification relève expressément de la convention autorisée de l’application.

## Sources et preuves vérifiées

Les quatre PDF locaux correspondent au CELEX 02013R1308, consolidation du 18.08.2026. Les tailles et empreintes correspondent au manifeste local courant, indépendamment des rapports historiques.

| Langue | Fichier | Octets | Pages PDF | SHA-256 |
| --- | --- | ---: | ---: | --- |
| FR | CELEX_02013R1308-20260818_FR_TXT.pdf | 2214937 | 219 | 5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c |
| NL | CELEX_02013R1308-20260818_NL_TXT.pdf | 2149221 | 219 | 99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74 |
| EN | CELEX_02013R1308-20260818_EN_TXT.pdf | 2105606 | 219 | b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11 |
| DE | CELEX_02013R1308-20260818_DE_TXT.pdf | 2159699 | 219 | 64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda |

Répertoire : reference-input/eu-food-labelling/02-sector-product-standards. Les références de page sont les numéros PDF, base 1.

Attribution : Union européenne, règlement (UE) nº 1308/2013, texte consolidé EUR-Lex du 18.08.2026, CELEX 02013R1308. Modifications apportées ici : sélection de termes, rattachement éditorial à des concepts et métadonnées linguistiques ; les PDF ne sont pas modifiés. La vérification de licence du rapport précédent, consulté intégralement, repose sur la [notice officielle EUR-Lex](https://eur-lex.europa.eu/content/legal-notice/legal-notice.html?locale=en), indexée et vérifiée le 2026-10-05 : textes consolidés sous CC BY 4.0 avec attribution et indication des modifications. L’ouverture directe avait rencontré la protection JavaScript ; cette limite de la preuve de licence reste documentée.

Cette passe vérifie le lot retenu, pas une nouvelle extraction exhaustive de tout le règlement. Pypdf et l’extraction déjà examinée dans l’importeur ont servi à retrouver exactement les sous-chaînes et leurs offsets. Les pages utilisées sont 143, 144, 145, 146, 147, 182 et 184 : annexe I, secteurs laitiers, œufs, apiculture, viandes/abats et graisses ; annexe VII, dénominations laitières. Identité, langue et date, pagination de 219 pages, pages sélectionnées non vides et absence de caractères de remplacement ont été contrôlées. Les équivalences multilingues restent celles des rubriques primaires vérifiées.

Le [CSV de résultats](EU_1308_2013_ANIMAL_AUTHORIZED_IMPORT_RESULTS.csv) contient 77 lignes de formes attestées, avec ID de revue, concept cible, langue, PDF, page, rubrique, offset, contexte exact et SHA-256. Chaque ligne est liée à une preuve source du mapping importé. Le cas EN skimmed-milk / skimmed milk explique les 77 lignes pour 76 mappings effectifs ; il est signalé COVERED_NORMALIZED_VARIANT_WITH_SOURCE_EVIDENCE. Les graphies exactes et les deux occurrences ne sont pas effacées. Le statut précédent REVIEW_REQUIRED reste historique ; authorization_basis enregistre séparément USER_AUTHORIZED_V0_7_SCOPE. Les alias canoniques partagés entre langues sont indiqués comme tels et comptés une seule fois.

| Nouveau concept | FR | NL | EN | DE |
| --- | --- | --- | --- | --- |
| buttermilk | babeurre | karnemelk ; botermelk | buttermilk | Buttermilch |
| royal_jelly | Gelée royale | koninginnengelei | Royal jelly | Gelée Royale |
| propolis | propolis | propolis | propolis | Kittharz |

Aucune traduction manquante n’a été fabriquée. NO_EXACT_CURRENT_LANGUAGE_ALIAS n’a pas été traité comme une preuve d’absence de concept : le rattachement des alias a pris en compte les noms et alias existants ainsi que leurs normalisations. Les catégories douanières englobantes, produits non comestibles, césures PDF et lignes exclues de la revue ne sont pas intégrés.

## Workflow, changements et non-régression

Les instructions AGENTS.md, le skill knowledge-import-validation et ses références source-adapters.md et import-workflow.md ont été lus avant modification. Le skill android-validation a servi au choix des tests ciblés. Les importeurs, builders, données et tests pertinents ont été inspectés avant leur exécution.

L’autorisation actuelle permet l’import ; l’interdiction de tous les modes était propre à la passe documentaire antérieure. Le premier dry-run normal, sans escalade ni contournement, a réussi. Ensuite le dry-run du lot fermé a produit les nombres ci-dessus et ses propositions complètes ont été comparées aux 77 lignes sélectionnées avant le lancement normal de --write. Aucune écriture directe dans les JSON.

Le diff complet a été inspecté ; les comparaisons sémantiques avant/après ont vérifié :
- les seuls nouveaux identifiants sont buttermilk, royal_jelly et propolis ;
- les statuts, raisons, noms, alias antérieurs et références de source des concepts existants sont conservés ;
- les champs OCR, alias linguistiques, mappings et sources historiques sont conservés ; les ajouts ne remplacent aucun propriétaire existant ;
- seuls milk, whey, casein, egg, honey, edible_offal et animal_fat reçoivent les alias existants autorisés ; honey reçoit aussi la référence réglementaire correspondante ;
- beeswax et E901 restent inchangés, ainsi que les exclusions de cette passe ;
- les assets sont ceux des builders documentés, avec parité vérifiée ;
- le moteur de verdict, l’OCR, les règles d’origine, la version, les manifestes et les builders sont inchangés.

Le changement de tests vérifie les formes importées et actualise trois attentes de comptage. Il ne modifie aucun comportement de production. Les mappings nouveaux portent les preuves exactes dans sourceEvidence ; la source isitvegan-animal-products-convention-v0-7 n’est attribuée qu’aux deux produits apicoles nouveaux.

## Commandes et validations exécutées

Inventaire des commandes substantielles exécutées, avec regroupement des répétitions de lecture/inspection : git status --short --branch ; git rev-parse HEAD ; git worktree list ; git diff et git diff --stat ; git show HEAD:<fichier> pour les comparaisons ; rg et Get-Content pour instructions, code, rapports et tests. Des scripts Python temporaires transmis à stdin, sans fichier de script créé, ont lu CSV/JSON, calculé les SHA-256, vérifié les pages avec pypdf et comparé les snapshots et les données avant/après. Ils ont uniquement créé les deux livrables documentaires de cette passe ; les mutations JSON ont été effectuées par l’importeur.

Commandes de workflow réellement exécutées :
~~~text
python tools/import_eu_agricultural_products_regulation.py --dry-run
python tools/import_eu_agricultural_products_regulation.py --animal-enrichment --dry-run
python tools/import_eu_agricultural_products_regulation.py --animal-enrichment --write
python tools/build_ingredients.py
python tools/build_multilingual_ingredient_mapping.py --write
python tools/build_knowledge_docs.py
python tools/build_ingredients.py --check
python tools/build_multilingual_ingredient_mapping.py --check --report
python tools/build_knowledge_docs.py --check
python tools/import_eu_agricultural_products_regulation.py --animal-enrichment --check
python tools/import_eu_agricultural_products_regulation.py --animal-enrichment --dry-run
git diff --check
~~~

Résultats : chaque commande ci-dessus termine avec code 0. Le dernier --check et le dry-run répété indiquent changes=0 et zéro ajout sur toutes les dimensions : idempotence vérifiée sans seconde écriture. La vérification multilingue trouve collisions=0, placeholders=[], assetParity=true et declaredMappings=2072. Son indicateur concepts=426 ne désigne pas le total du corpus de référence de 482. Le seul missingConcepts, cereals, est identique au diagnostic de départ et n’est pas masqué ni corrigé hors périmètre.

Tests ciblés avec le wrapper :
~~~text
.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest --tests com.example.isitvegan.MultilingualIngredientMappingTest --tests com.example.isitvegan.DairyKnowledgeNonRegressionTest --tests com.example.isitvegan.Version066MultilingualMatchingTest --tests com.example.isitvegan.Version066TraceLanguageTest :mutation-core:test --tests com.example.isitvegan.KnowledgeIndexTest
.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest
~~~

Le lancement initial sous le JAVA_HOME ambiant JDK 27 a échoué avant les tests, lors de la résolution réseau du daemon JDK 25. Le JBR 25.0.3 déjà installé dans C:\Program Files\Android\Android Studio\jbr a ensuite été choisi seulement pour les commandes PowerShell (variable JAVA_HOME de processus). Aucune configuration du dépôt n’a été modifiée.

Le premier test ajouté sur les traces supposait à tort que Wasser était connu : ce mot allemand est absent des alias historiques de water. L’assertion a été corrigée pour comparer le même préfixe avec/sans trace et vérifier le maintien de sa reconnaissance, de ses inconnus et de son verdict. Aucun alias hors lot n’a été ajouté. Après correction, la commande combinée a réussi : 14 tests app et 6 tests mutation-core, soit 20 tests distincts, sans échec. Une assertion de résolution linguistique a ensuite été renforcée ; sa compilation initiale a nécessité une comparaison explicite du booléen nullable. Le dernier lancement ciblé a réussi avec les 6 tests AgriculturalProductsRegulationImportTest, dont la résolution explicite des 77 formes dans leur langue.

Ces tests couvrent le rattachement au bon concept, la disponibilité du canonique, les résultats non vegan des nouveaux produits, les classifications beeswax/E901 inchangées, la convention apicole, les traces et les contextes végétaux protégés. La suite complète, assembleDebug et les tests sur appareil n’ont pas été exécutés : aucun code de production Android, verdict ou OCR n’est modifié. Les succès mentionnés sont ceux des commandes effectivement exécutées.

## État Git et limites finales

État de départ : branche master suivait origin/master ; aucun diff suivi ni staged ; seul reports/0.7/eu-pdf/ était non suivi, avec neuf fichiers préexistants. Un seul worktree, sur la même branche et le HEAD cité en tête.

Les empreintes des 381 fichiers présents dans le snapshot initial ont été comparées. Les neuf rapports préexistants sont conservés octet pour octet, et tous les fichiers initiaux hors des onze modifications autorisées sont inchangés. Deux fichiers ont été créés : ce rapport et le CSV de résultats. Aucun commit ni push ; HEAD inchangé.

Les onze fichiers suivis modifiés sont :
- tools/import_eu_agricultural_products_regulation.py ;
- knowledge/ingredients.json, knowledge/ingredient_aliases_multilingual.json, knowledge/sources.json ;
- app/src/main/assets/ingredients.json et ingredient_aliases_multilingual.json ;
- docs/generated/base-connaissances-data.md et multilingual-ingredient-mapping.md ;
- les tests AgriculturalProductsRegulationImportTest.kt, MultilingualIngredientMappingTest.kt et mutation-core KnowledgeIndexTest.kt.

Le rapport final de git status garde reports/0.7/eu-pdf/ non suivi, avec ses neuf fichiers anciens et ces deux nouveaux ; les onze modifications suivies correspondent exactement à cette liste. Les annonces LF/CRLF de Git sont informatives ; git diff --check ne trouve aucune erreur de whitespace.

La couverture mesurable est celle du lot de 77 formes attestées et des 76 nouvelles clés linguistiques. Les alias déjà reconnus canoniquement améliorent surtout l’étiquetage linguistique et la traçabilité ; ils ne sont pas tous des reconnaissances inédites. Cette passe ne revendique ni exhaustivité documentaire, ni validation universelle de certification, ni correction des lacunes étrangères au lot autorisé.

Contrôle final du CSV : UTF-8 strict valide, 21 colonnes, 77 IDs de revue uniques, 76 clés de mapping ; chaque référence PDF et offset exact a été revérifié sur 28 couples langue/page. Les 77 preuves sourceEvidence correspondent aux 77 lignes. Aucun caractère de remplacement dans les deux livrables. CSV : 52 161 octets, SHA-256 ef4957c74fab6b44d1eb4326f7113175e3bcfd6c6db493661314821d3004ce52. Dernier état Git : HEAD inchangé, aucun changement staged, onze fichiers suivis modifiés et uniquement les deux nouveaux livrables dans le snapshot, neuf rapports historiques inchangés.

Un contrôle de snapshot avait dépassé la longueur maximale d’une commande Windows (erreur 206, avant lancement) ; il a été raccourci en comparant les empreintes en mémoire après leur lecture. Le contrôle final décrit ci-dessus a réussi. Ce diagnostic ne concerne pas le contrôle automatique d’autorisation de l’importeur.
