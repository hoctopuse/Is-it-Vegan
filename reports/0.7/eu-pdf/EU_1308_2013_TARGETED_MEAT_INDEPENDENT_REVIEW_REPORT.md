# Contrôle indépendant — intégration ciblée des viandes UE 1308/2013

Date : 2026-10-07. **Verdict : NO_GO avant commit**, pour défauts de contrôle de l’importeur. Les données actuellement importées et leurs 17 preuves ont été vérifiées conformes ; aucun transfert d’alias ni ajout hors périmètre n’a été constaté. Cette revue ne corrige aucun problème.

## État réel et méthode

HEAD vérifié : `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`, branche `master`. Aucun fichier staged. Huit fichiers suivis modifiés : les deux JSON éditoriaux, les deux actifs Android correspondants, deux documents générés, l’importeur 1308 et le test ciblé. Sept rapports/CSV non suivis étaient déjà présents avant cette revue, dont le rapport d’intégration. Ce nouveau rapport est le seul fichier créé par le contrôle.

Instructions consultées : [AGENTS.md](../../../AGENTS.md), [knowledge-import-validation](../../../.agents/skills/knowledge-import-validation/SKILL.md), [source-adapters](../../../.agents/skills/knowledge-import-validation/references/source-adapters.md), [import-workflow](../../../.agents/skills/knowledge-import-validation/references/import-workflow.md), invariants de connaissance et documentation du matching. Rapports d’intégration, de décisions, de proposition et de revue documentaire consultés ; les preuves sont confrontées aux fichiers réels, pas au seul compte rendu.

Contrôle indépendant effectué avec des commandes Git et des scripts Python transmis sur stdin (`python -B -`) : lecture JSON/CSV, comparaison avec `git show HEAD:…`, recalcul des empreintes, extraction ciblée des PDF en mémoire et analyse AST de l’importeur. Aucun module de l’importeur n’a été importé ou exécuté. Aucun importeur, builder, Gradle ou PIT n’a été lancé dans cette revue. Les constats de comportement ci-dessous sont établis par inspection des branches, sans exécution ni manipulation des fichiers d’entrée.

## Constats par gravité

### P2 — 1. `--meat-species --check` réussit même si le lot entier manque

**Fichier :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), lignes **391–427 et 493–499** ; dispatch lignes **504–505**.

Si aucun des cinq concepts n’est présent, `present` est vide : la branche de vérification des concepts importés n’est pas exécutée. La fonction construit les cinq concepts et leurs mappings en mémoire, affiche `changes=1`, puis atteint la fin sans exception. Seul `args.write` déclenche une écriture ; **aucun test de `args.check` ne fait échouer cet état périmé**. Le programme se termine donc normalement (code de sortie 0) pour un lot absent, contrairement au mode historique, qui échoue lorsqu’une transformation est nécessaire.

**Cas concret à couvrir après correction :** les JSON du HEAD avant ajout des cinq concepts, avec les PDF valides, donnent un succès de processus sous `--meat-species --check` alors que l’import est nécessaire. Ce cas n’a pas été exécuté ici ; le chemin est entièrement visible dans le code.

**Impact :** un contrôle automatisé fondé sur le code de sortie peut accepter une base sans le lot attendu. Le test actuel, qui vérifie seulement le lot déjà présent, ne couvre pas cette branche. Défaut matériel de comportement à corriger avant commit.

### P2 — 2. Le contrôle du lot déjà présent ne vérifie pas toute la provenance déclarée

**Fichier :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), lignes **414–424**.

Les tuples comparés contiennent seulement `conceptId`, `language`, `normalizedForm`, `surfaceForm` et `sourceEvidence`. Les champs `source`, `relation`, `mappingGroup` et `confidence` des mappings sont ignorés. Par exemple, remplacer en mémoire le champ `source` d’un mapping du lot par un identifiant erroné ne changerait aucune valeur testée et laisserait le contrôle afficher `changes=0; imported batch is current`.

**Impact :** un mapping dont la source déclarée est incorrecte peut être déclaré conforme. Les 17 mappings réels ont été vérifiés indépendamment et leurs champs sont corrects à présent ; le défaut concerne la garantie du vérificateur, pas une corruption actuellement observée. Une assertion positive qui invoque ce même vérificateur ne valide pas ses cas de rejet.

### P2 — 3. La protection contre les collisions est incomplète par rapport au chargeur réel

**Fichiers :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), lignes **397–424, 434–456** ; [lexique Kotlin](../../../mutation-core/src/main/kotlin/com/example/isitvegan/MultilingualIngredientLexicon.kt), lignes **210–218**.

À la création du lot, `owners` indexe globalement les noms et alias canoniques. Les alias multilingues sont en revanche placés uniquement dans `language_owners` et recherchés dans **la langue du candidat**. Le chargeur Kotlin refuse aussi une même surface normalisée détenue par des concepts différents **entre langues différentes**. Les deux protections ne sont donc pas équivalentes.

Exemple de chemin accepté à tort : un ancien alias lexical `geitenvlees` attribué à un autre concept en EN, sans copie de cette forme dans les alias canoniques. L’import du candidat NL ne le verrait ni dans `owners` ni dans la clé NL ; le JSON résultant aurait un conflit que le chargeur Kotlin rejetterait. De plus, lorsque les cinq concepts sont déjà présents, le retour anticipé de la ligne 424 précède toute recherche de collisions : un alias ajouté à un autre propriétaire peut passer le contrôle du lot.

**Impact :** dans ces états d’entrée, l’importeur peut valider/produire un lexique incompatible avec les règles réelles de chargement. **Aucune collision de ce type n’existe dans les données examinées** : toutes les 17 surfaces ont été auditées globalement, toutes langues confondues, contre noms, alias, variantes OCR et mappings. Ce constat demande un alignement des garanties de contrôle avant commit.

### P3 — 4. `--dry-run` devient silencieux lorsque le lot est déjà présent

**Fichier :** [importeur](../../../tools/import_eu_agricultural_products_regulation.py), lignes **422–424**.

Le message `changes=0` est supprimé si `args.dry_run` est vrai, puis la fonction retourne. Le mode ne modifie aucun fichier mais ne fournit aucun résultat explicite d’idempotence pour l’état importé. Limite de restitution non bloquante prise isolément.

### P3 — 5. Les tests positifs ne couvrent pas les garanties de rejet ni la résolution exacte

**Fichier :** [test ciblé](../../../app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt), lignes **52–85**.

Le nouveau test contient bien **17 cas sur cinq concepts**, pas seulement cinq expressions. Il vérifie le `canonicalId` du résolveur lexical, la présence du concept dans les ingrédients reconnus, `NON_VEGETARIAN`, puis le statut `NON_VEGAN` des cinq concepts. Ces assertions sur le pipeline Kotlin sont utiles : leurs valeurs attendues sont littérales et concordent avec les preuves indépendantes.

Il n’assert pas `IngredientMatch.resolution == EXACT`, l’absence de résidu/inconnu, l’exclusivité du concept reconnu ou l’identité du bloqueur dans les diagnostics. L’appel de `--check` à l’état courant n’éprouve pas un lot absent, une mauvaise source de mapping ou des collisions interlangues. Les tests positifs n’invalident donc pas les constats 1–3. L’assertion globale de 2089 mappings reste couplée à l’inventaire actuel.

## Données : conformités établies

Comparaison directe au HEAD :

- Les **seuls cinq nouveaux IDs** sont `bovine_meat`, `pork_meat`, `sheep_meat`, `goat_meat`, `horse_meat`. Tous sont `NON_VEGAN` ; aucun ancien ID n’est retiré.
- Les **482 concepts antérieurs sont identiques**, champs, ordre et listes d’alias compris. Voir les ajouts dans [ingredients.json](../../../knowledge/ingredients.json), lignes **8503–8569**.
- Tous les anciens éléments du lexique multilingue sont préservés, y compris les mappings, variantes OCR et corrections. Le registre `sources.json` est identique au HEAD.
- Les noms canoniques sont cinq formes EN attestées ; les listes canoniques portent 12 autres alias. Les 17 formes sont présentes dans les alias de langue et les mappings. Aucun nom actif supplémentaire, traduction, flexion ou forme courte non retenue n’a été ajouté.
- Répartition : **FR 3, NL 5, EN 6, DE 3**. Porc sans DE ; mouton sans FR/DE ; chèvre sans FR : cela correspond aux décisions.
- Les mappings possèdent les valeurs attendues de source, relation, groupe, confiance, forme normalisée et preuve. **0 doublon de clé de mapping**, **0 doublon de clé linguistique pour le lot**, **0 collision des surfaces du lot avec un autre propriétaire** selon la normalisation réelle.
- `pigmeat` a pour seul propriétaire `meat`. Tous les anciens alias de `meat`, notamment bovin/porc/veau et collectifs ovins-caprins, sont conservés sans duplication d’une clé complète vers un nouvel ID. Les sous-chaînes communes comme `meat`/`vlees` ne sont pas des collisions de clés : le matcher privilégie l’expression la plus longue.
- Aucun concept différé ou exclu n’a été ajouté. `animal_fat` et `poultry_meat_preparation` étaient déjà présents et sont identiques au HEAD ; « absents de l’import » ne signifie donc pas absents de toute la base.

Les [actifs ingrédients](../../../app/src/main/assets/ingredients.json) concordent avec la projection éditoriale attendue, champ par champ. Le [lexique Android](../../../app/src/main/assets/ingredient_aliases_multilingual.json) est identique octet pour octet au lexique éditorial.

Les deux [documents](../../../docs/generated/base-connaissances-data.md) [générés](../../../docs/generated/multilingual-ingredient-mapping.md) contiennent les cinq concepts et les 17 formes avec leurs statuts/langues corrects. En retirant uniquement les lignes/blocs des cinq nouveaux concepts, leur texte redevient exactement celui du HEAD. Cette comparaison n’a nécessité aucune régénération.

## Vérification des 17 preuves

Lecture indépendante des quatre PDF locaux sous `reference-input/eu-food-labelling/02-sector-product-standards/`, extraction `pypdf` en mémoire. Chaque page utilisée porte CELEX, date et langue attendus. Les empreintes recalculées concordent avec les deux CSV et les mappings :

| PDF / langue | Pages | SHA-256 recalculé |
|---|---:|---|
| FR | 219 | `5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c` |
| NL | 219 | `99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74` |
| EN | 219 | `b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11` |
| DE | 219 | `64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda` |

Pour **chaque ligne** ci-dessous : la sous-chaîne à l’offset indiqué correspond mot pour mot à la forme importée ; fichier PDF, langue, page, rubrique, offset, ID et empreinte correspondent aux CSV de proposition et de revue documentaire. La page est en base 1, l’offset en caractères Python base 0.

| ID de revue | Concept | Langue | Forme exacte | Page PDF | Offset |
|---|---|---|---|---:|---:|
| M0007 | `bovine_meat` | FR | Viandes des animaux de l'espèce bovine | 143 | 946 |
| M0009 | `bovine_meat` | NL | Vlees van runderen | 143 | 874 |
| M0011 | `bovine_meat` | EN | Meat of bovine animals | 143 | 929 |
| M0013 | `bovine_meat` | DE | Fleisch von Rindern | 143 | 835 |
| M0026 | `pork_meat` | FR | Viandes des animaux de l'espèce porcine domestique | 145 | 302 |
| M0028 | `pork_meat` | NL | Vlees van varkens | 145 | 277 |
| M0030 | `pork_meat` | EN | Meat of domestic swine | 145 | 268 |
| M0042 | `sheep_meat` | NL | schapenvlees | 13 | 1366 |
| M0043 | `sheep_meat` | EN | Sheepmeat | 145 | 1490 |
| M0044 | `goat_meat` | NL | geitenvlees | 145 | 1742 |
| M0045 | `goat_meat` | EN | goatmeat | 145 | 1504 |
| M0046 | `goat_meat` | DE | Ziegenfleisch | 145 | 1868 |
| M0095 | `horse_meat` | FR | Viandes de cheval | 156 | 2719 |
| M0096 | `horse_meat` | NL | Vlees van paarden | 156 | 2556 |
| M0097 | `horse_meat` | EN | Meat of horses | 156 | 2293 |
| M0098 | `horse_meat` | EN | Horsemeat | 156 | 2347 |
| M0099 | `horse_meat` | DE | Fleisch von Pferden | 156 | 2432 |

Rubriques et contexte vérifiés :

- M0007/9/11/13 : annexe I XV, NC 0201 ; libellés viande bovine fraîche/réfrigérée.
- M0026/28/30 : annexe I XVII, ex0203 ; viande porcine domestique fraîche/réfrigérée/congelée. Le sous-terme NL est suivi de `(huisdieren)` dans la source ; il n’a pas été complété dans l’alias.
- M0042 : article 18(4)(c), p.13 NL, secteur de viande ovine. M0043 : annexe I XVIII, p.145 EN, titre `Sheepmeat and goatmeat`.
- M0044/45/46 : annexe I XVIII, p.145, composés de titres ovins/caprins. La forme individuelle caprine est une sous-chaîne réelle, sans traduction reconstruite.
- M0095–99 : annexe I XXIV section 2, p.156. La première occurrence FR retenue M0095 se trouve à NC 0210 99 10 (salée/saumure/séchée) ; les autres occurrences citées couvrent ex0205 00 ou 0210 99 10. La rubrique collective de provenance cite les deux codes, sans prétendre que chaque forme appartient aux deux occurrences.

Les preuves confirment le vocabulaire et le contexte réglementaire de viande. Elles ne démontrent pas que toute apparition de ces chaînes dans un texte commercial correspond à un ingrédient réellement présent.

## Importeur et effets de bord

La liste `MEAT_SPECIES_REVIEW` (lignes **132–150**) est bornée aux cinq IDs et aux 17 formes contrôlées ; le nombre et les langues approuvés concordent avec la revue. Le contrôle des PDF vérifie empreintes, 219 pages, identité de page et sous-chaîne exacte à l’offset. Il ne valide pas automatiquement le sens de la rubrique : cette partie a été examinée dans les CSV et le contexte extrait.

Par inspection, `--dry-run` et `--check` n’écrivent aucun fichier. `--write` ne vise que `knowledge/ingredients.json` et `knowledge/ingredient_aliases_multilingual.json`, après les contrôles précédant l’écriture. Il n’appelle aucun builder ni rapporteur. Les actifs et docs sont des résultats des étapes séparées annoncées dans le rapport d’intégration. Lorsque le lot existant concorde, `--write` retourne sans réécrire, ce qui établit l’idempotence nominale par lecture.

Les fonctions historiques `n`, `compact`, `text`, `runtime_normalized`, `animal_evidence` et `animal_import` sont identiques au HEAD par comparaison AST, ainsi que tous les anciens blocs de constantes. Le dispatch historique reste inchangé lorsque le nouveau drapeau est absent ; combiner les deux lots est explicitement refusé. Aucun effet de bord sur l’ancien lot n’a été identifié par inspection. Ses sorties n’ont pas été réexécutées pendant cette revue.

## Verdict, diagnostics et limites

Le matcher, le moteur, les enums, les règles d’origine et les règles d’arrêt sont inchangés dans le diff. Lecture de [VerdictEngine](../../../mutation-core/src/main/kotlin/com/example/isitvegan/VerdictEngine.kt), lignes **26–28** : un ingrédient `NON_VEGAN` donne `AnalysisVerdict.NON_VEGETARIAN`. Aucune donnée ne porte un statut ingredient `NON_VEGETARIAN`.

Les [diagnostics](../../../mutation-core/src/main/kotlin/com/example/isitvegan/DiagnosticModels.kt), lignes **166–213**, conservent ID, nom, chemin, statut et raison du bloqueur ; le code générique s’applique aux nouveaux IDs. Le test ajouté ne contrôle pas ces diagnostics spécifiquement, mais aucun changement du mécanisme n’est observé.

Risque résiduel explicitement accepté par les décisions : des expressions comme « substitute for Meat of bovine animals » ou « sans Viandes de cheval » contiennent la formulation entière tout en ne prouvant pas sa présence. Les cinq nouveaux IDs ne font partie ni de `protectedAnimalIds` ni de `flavourQualifiedIngredientIds` ([matcher](../../../mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt), lignes **206–215, 352, 393**). Une correspondance peut rester partielle tout en ajoutant un bloqueur `NON_VEGAN`, prioritaire dans le verdict. Ces exemples sont des risques déduits du code, pas des sondes exécutées ici. Aucun benchmark exhaustif d’étiquettes n’est annoncé ou démontré.

Le rapport d’intégration distingue correctement les contrôles annoncés des limites : pas de nouveau rendu visuel PDF, pas de revue exhaustive d’étiquettes, pas de réévaluation de licence. Cette revue a réextrait les pages et les empreintes, sans rendu visuel indépendant ni vérification en ligne/licence. Elle n’accorde aucune garantie sémantique à une reconnaissance textuelle exacte.

L’artefact de test ciblé déjà présent indique **7 tests, 0 échec, 0 erreur, 0 ignoré**, horodatage `2026-10-07T08:41:47.718Z` (`app/build/test-results/testDebugUnitTest/TEST-com.example.isitvegan.AgriculturalProductsRegulationImportTest.xml`). Cela corrobore une exécution antérieure ; ce n’est pas une nouvelle validation exécutée par cette revue et cela ne couvre pas les états négatifs de l’importeur.

## Synthèse des validations

**Effectué :** état Git/HEAD/staging/diff, comparaison JSON et anciens propriétaires au HEAD, vérification globale des clés normalisées du lot, contrôle des 17 liens CSV/PDF avec empreintes/pages/offsets/contextes, comparaison des actifs et ajouts documentaires, lecture et comparaison AST de l’importeur historique, revue des tests/verdict/diagnostics, consultation du résultat de test existant, `git diff --check`.

**Non effectué :** exécution d’un importeur (y compris `--check`/`--dry-run`), builder, génération, nouveau test Gradle/JVM, suite complète, mutation, nouveau rendu PDF, benchmark d’étiquettes, réévaluation de licence ou vérification de la consolidation en ligne.

Préservation finale : les empreintes des **390 fichiers préexistants** suivis ou présents dans ce dossier de rapports sont identiques avant/après le contrôle. Le HEAD et l’index restent inchangés ; seul ce nouveau rapport a été ajouté.

**NO_GO** porte sur les défauts matériels de vérification décrits en 1–3. Les données importées examinées sont conformes. Le commit doit attendre la correction des contrôles et leur couverture négative ; cette revue a laissé tous les fichiers concernés et les rapports précédents intacts.
