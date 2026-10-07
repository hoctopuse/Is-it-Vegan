# Intégration ciblée — viande séparée mécaniquement, v0.7

Date : 7 octobre 2026. **Verdict d'intégration : GO_WITH_WARNINGS.** Un seul concept, `mechanically_separated_meat`, est intégré avec les quatre dénominations longues approuvées et le statut ingrédient `NON_VEGAN`. Les validations ciblées passent. Les négations et imitations contenant littéralement ces dénominations restent une limite constatée du matching, à examiner humainement avant commit; aucune protection nouvelle n'a été ajoutée.

## Préflight et environnement

- Branche observée : `master...origin/master`. HEAD de départ et de fin : `d1f02275508c9472b1fe55ac82d792d1257f6264`.
- État initial : aucun changement suivi, aucun changement indexé. Non suivis préexistants : les quatre PDF du répertoire `reference-input/eu-food-labelling/05-food-hygiene/` et les trois rapports/CSV `EU_853_2004_MEAT_CANDIDATE_EXPLORATION_REPORT.md`, `EU_853_2004_MEAT_CANDIDATES.csv`, `EU_853_2004_THREE_CONCEPT_TARGETED_REVIEW_REPORT.md`.
- Les 401 fichiers suivis et non suivis préexistants ont été empreintés avant modification, hors fichiers ignorés. Les trois JSON éditoriaux ont aussi été copiés dans le répertoire temporaire Windows pour la comparaison sémantique. Tous les changements préexistants et documents sources sont préservés.
- Instructions lues : `AGENTS.md`, skills `knowledge-import-validation` et `android-validation`, références `source-adapters.md` et `import-workflow.md`, invariants et conventions de `docs/base-connaissances.md`, documentation de matching, verdict et pipeline. Le [rapport de revue ciblée](EU_853_2004_THREE_CONCEPT_TARGETED_REVIEW_REPORT.md) a été consulté comme décision, puis les passages PDF ont été vérifiés à nouveau.
- Environnement : Windows/PowerShell, Python avec `pypdf 6.19.0`, Gradle wrapper 9.7.1. Java n'était pas exposé par la recherche initiale de commande; les commandes Gradle utilisent `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` pour leur processus, sans modifier la configuration du dépôt. JVM du wrapper : JetBrains 25.0.3.

Les trois contrôles de base (`build_ingredients.py --check`, `build_multilingual_ingredient_mapping.py --check`, `build_knowledge_docs.py --check`) passaient avant intégration. Il n'existait pas d'importeur dédié 853/2004. L'importeur agricole 1308/2013 et ses modes ont été inspectés, mais aucun de ses modes n'a été détourné pour ce lot.

## Sources effectivement contrôlées

Les huit PDF existent, avec le CELEX, la date et la langue attendus dans l'en-tête des passages. Contrôle actuel par extraction `pypdf`, nombre de pages, SHA-256 et position exacte dans le texte de page. Les offsets ci-dessous sont en caractères, base 0, dans l'extraction non modifiée; les pages PDF sont en base 1. La preuve conserve la casse source; seuls les quatre textes approuvés deviennent des surfaces lexicales.

Les quatre PDF 853 sont sous `reference-input/eu-food-labelling/05-food-hygiene/`, consolidation **07.05.2026**, 94 pages chacun. Les quatre PDF 1169 sont sous `reference-input/eu-food-labelling/00-general-food-labelling/`, consolidation **01.04.2025**, 60 pages chacun. Ces consolidations sont distinctes.

| PDF | Page / point | Offset | Forme exacte dans la preuve | SHA-256 |
|---|---|---:|---|---|
| `CELEX_02004R0853-20260507_FR_TXT.pdf` | 16 / annexe I 1.14 | 2215 | `viandes séparées mécaniquement` | `302312dbf898ef8993b11057070691801c8c374d8de13b5947ff592a2309562e` |
| `CELEX_02004R0853-20260507_NL_TXT.pdf` | 16 / annexe I 1.14 | 2047 | `Separatorvlees` | `30fa09a89bcede829396f29769812b5c4503f404c91a9acb2771b429a1356543` |
| `CELEX_02004R0853-20260507_EN_TXT.pdf` | 16 / annexe I 1.14 | 1951 | `Mechanically separated meat` | `125b70e276a8e21d9af7e72c6af773d19068896b126a54b777c08bd49fb7037d` |
| `CELEX_02004R0853-20260507_DE_TXT.pdf` | 16 / annexe I 1.14 | 2101 | `Separatorenfleisch` | `9874ff8be78fcd7d33a16d837f85cbe46ae7abcfd7d70d1c1dc9542deeb7602f` |
| `CELEX_02011R1169-20250401_FR_TXT.pdf` | 49 / annexe VII B 18 | 1764 | `Viandes séparées mécaniquement` | `80fedf36e7930dddfb270871d68ff10864009fd3ac71c9d350876402c9d6a81e` |
| `CELEX_02011R1169-20250401_NL_TXT.pdf` | 49 / annexe VII B 18 | 1563 | `separatorvlees` | `36dd5f61e3e9aeb95c2e51b4ff22ba90056639f726d4b30aae97c63c653c8ef3` |
| `CELEX_02011R1169-20250401_EN_TXT.pdf` | 49 / annexe VII B 18 | 1468 | `mechanically separated meat` | `6cdf4190d6fa4a99d2e7d3124611724ec4cf69159563f88b35dc3c50dd416c63` |
| `CELEX_02011R1169-20250401_DE_TXT.pdf` | 49 / annexe VII B 18 | 1690 | `Separatorenfleisch` | `c2409d5cb83f784a8ac94ceeb8d4837da343189e024f5d1a58cc015f6bb62aa2` |

Le point 1.14 établit le produit animal obtenu par retrait mécanique de viande des os ou carcasses de volailles, avec destruction/modification de la structure des fibres. Les points voisins 1.13 et 1.15 distinguent viande hachée et préparation de viande. Le point 18 du tableau d'ingrédients 1169/2011 donne la dénomination avec indication des espèces animales; il ne donne pas une liste de formulations composées à inventer. Aucun préfixe d'espèce n'a été créé.

Il n'y a pas eu de nouveau rendu PDF dans cette intégration. Les pages pertinentes avaient été contrôlées visuellement dans la revue précédente; les empreintes sont identiques. Cette passe refait la vérification du texte, des offsets et de l'identité des fichiers, sans revendiquer une nouvelle inspection visuelle, une revue exhaustive d'étiquettes ou une preuve de fréquence d'emploi.

Pour les conditions de réutilisation, la consultation de l'[avis juridique officiel EUR-Lex](https://eur-lex.europa.eu/content/legal-notice/legal-notice.html?locale=en) a confirmé la mention CC BY 4.0 pour les textes consolidés appartenant à l'UE, avec attribution et indication des changements. La première ouverture de l'URL sans locale renvoyait un contrôle JavaScript; le contenu officiel a été obtenu par la recherche ciblée. Cette consultation concernait uniquement la réutilisation; aucun autre PDF ou consolidation n'a été téléchargé. Les nouveaux enregistrements de source et la documentation attribuent Union européenne/EUR-Lex et distinguent la sélection et la classification éditoriales du texte réglementaire.

## Données ajoutées et provenance

| Langue | Surface lexicale ajoutée | Concept | Statut | Résolution runtime vérifiée | Résidu / inconnus | ID du bloqueur |
|---|---|---|---|---|---|---|
| FR | `viandes séparées mécaniquement` | `mechanically_separated_meat` | `NON_VEGAN` | `EXACT` | Vide / aucun | `mechanically_separated_meat` |
| NL | `separatorvlees` | `mechanically_separated_meat` | `NON_VEGAN` | `EXACT` | Vide / aucun | `mechanically_separated_meat` |
| EN | `mechanically separated meat` | `mechanically_separated_meat` | `NON_VEGAN` | `EXACT` | Vide / aucun | `mechanically_separated_meat` |
| DE | `Separatorenfleisch` | `mechanically_separated_meat` | `NON_VEGAN` | `EXACT` | Vide / aucun | `mechanically_separated_meat` |

La forme FR est le nom canonique; les trois autres sont dans `aliases` de l'ingrédient. Le lexique multilingue contient quatre entrées, une surface par langue, aucune variante OCR. Il s'agit donc de **quatre dénominations au total**, et non de quatre alias supplémentaires au nom canonique. Le corpus passe de 487 à 488 ingrédients et de 2 089 à 2 093 mappings.

Deux sources sont ajoutées sans modifier les anciennes :

- `eu-food-hygiene-regulation-853-2004-20260507` : définition et matière animale;
- `eu-fic-regulation-1169-2011-20250401` : dénomination d'ingrédient complémentaire.

Les quatre mappings utilisent les champs existants `surfaceForm`, `language`, `conceptId`, `mappingGroup`, `relation`, `normalizedForm`, `source`, `confidence`, `sourceEvidence`. Groupe `food-hygiene-regulation`, relation existante `REGULATORY_ALIAS`, confiance `REVIEWED`. Chaque mapping contient deux preuves, soit huit objets avec les propriétés déjà utilisées dans le lot viande précédent : `pdfFile`, `pdfPage`, `legalLocation`, `sourceOffset`, `surfaceForm`, `reviewId`, `sha256`. Les noms de fichiers donnent langue et consolidation, également déclarées dans le registre des sources. Les différences de casse d'une preuve ne créent pas de nouveaux alias.

Aucun enum, schéma ou champ de données nouveau. Le builder de documentation des mappings a reçu deux branches propres à cette source pour afficher le groupe et la relation déclarés; son flux, ses options et les anciennes branches sont conservés. Il ne devient pas un moteur de matching.

## Outil retenu et résultats attendus avant écriture

Nouvel adaptateur borné : `tools/import_eu_food_hygiene_regulation.py`. Acquisition des huit PDF, hash/date/langue/pagination/texte/offset/point, transformation en lot constant, contrôle des propriétaires et de la provenance, puis mutation exclusivement sous `--write`.

| Mode | Contrat inspecté | Résultat avant import | Résultat sur le lot intégré |
|---|---|---|---|
| `--dry-run` | Proposition en mémoire, aucun builder ni écriture | Code 0, `changes=1`, un concept, quatre surfaces/mappings, deux sources | Code 0, `changes=0; no modification necessary; batch is current` |
| `--check` | Exige un lot déjà complet et conforme; ne simule pas un import | Code 1, lot absent, changements nécessaires | Code 0, `changes=0`, lot courant |
| `--write` | Après validation, ajoute seulement aux trois JSON éditoriaux; ne régénère pas les actifs | Exécuté une seule fois sur le dépôt, code 0; lot ajouté | Idempotence de write vérifiée sur fixture conforme, sans seconde écriture sur le dépôt |

Les contrôles de collision utilisent NFD et suppression de toutes les marques Unicode, ligatures et bornes normalisées compatibles avec le chargeur Kotlin pour les formes étudiées, sans stemming. Ils couvrent noms canoniques, alias, E-numbers, langues, variantes OCR et propriétaires de mappings. Ils s'exécutent aussi sur un lot présent, avant écriture. Des cas de normalisation partagés sont vérifiés par Python et le vrai normaliseur Kotlin.

Un lot présent incomplet ou divergent est refusé plutôt que réparé silencieusement. Les concepts, entrées lexicales, mappings et preuves sont comparés par objet complet : champs supplémentaires inclus. Aucun alias historique n'est déplacé. Les tests CLI utilisent les vrais `main` et arguments sur fichiers temporaires; seule l'acquisition déjà contrôlée des PDF est substituée dans ces fixtures. Les modes sans écriture interdisent aussi `Path.write_text` et `Path.write_bytes` dans le processus de test.

## Validations réellement exécutées

Toutes les commandes Python ci-dessous ont été lancées avec `python -B -X utf8` (aucune génération de bytecode du dépôt demandée).

| Commande / contrôle | Résultat observé |
|---|---|
| `git status --short --branch`, `git rev-parse HEAD`, diff suivi/indexé | Préflight propre pour les fichiers suivis; non suivis préexistants identifiés |
| `python … tools/build_ingredients.py --check` | Code 0 avant et après; actifs courants |
| `python … tools/build_multilingual_ingredient_mapping.py --check` | Code 0 avant et après; documentation et parité lexicales courantes |
| `python … tools/build_knowledge_docs.py --check` | Code 0 avant et après; documentation courante |
| `python … -m unittest discover -s tools/tests -p test_eu_food_hygiene_import.py -v` | **13 tests, OK**, 31,282 s, avant mutation des données du dépôt |
| `python … tools/import_eu_food_hygiene_regulation.py --check` avant import | Code 1 attendu pour lot absent, sans modification des données/actifs |
| `python … tools/import_eu_food_hygiene_regulation.py --dry-run` avant import | Code 0, proposition bornée; trois JSON éditoriaux et deux actifs inchangés |
| `python … tools/import_eu_food_hygiene_regulation.py --write` | Code 0; ajout d'un concept et du lot autorisé |
| `python … tools/build_ingredients.py` | Code 0; génération normale des deux actifs; 488 ingrédients |
| `python … tools/build_multilingual_ingredient_mapping.py --write` | Code 0; document généré par son outil |
| `python … tools/build_knowledge_docs.py` | Code 0; document généré par son outil |
| `python … tools/build_origin_rules.py --check` | Code 0; règles et actif courants, aucune régénération d'origine |
| `python … tools/import_eu_food_hygiene_regulation.py --check` et `--dry-run` après import | Codes 0, aucune modification nécessaire; empreintes identiques des trois JSON, deux actifs et deux documents générés avant/après ces modes |
| `.\gradlew.bat --version` | Code 0; wrapper et JVM disponibles |
| `.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.FoodHygieneRegulationImportTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest` | Exécution finale **BUILD SUCCESSFUL** : 5 + 7 = **12 tests, 0 échec, 0 erreur, 0 ignoré** |
| `python … -m unittest discover -s tools/tests -p test_eu_meat_species_import.py -v` | **17 tests, OK**, 28,903 s; non-régression ciblée de l'importeur historique |
| Importeur agricole : `--check`, `--animal-enrichment --check`, `--meat-species --check` | Codes 0; chaque mode annonce `changes=0`; aucune écriture |
| Audit JSON et empreintes, comparaison des actifs historiques à HEAD | Tous les anciens ingrédients, sources, alias, mappings et corrections OCR conservés; seuls ajouts annoncés; actifs historiques préservés |
| `git diff --check`, diff indexé | Aucune erreur de whitespace; index inchangé |

La commande Gradle a été exécutée trois fois, pour des motifs concrets. Premier essai : les quatre tests initiaux du nouveau lot passaient, mais le test agricole échouait sur un total global figé (`2089` au lieu de `2093`). Deuxième essai après remplacement du total : écart `2094` contre `2093`, car un alias historique vise un concept indisponible et n'a pas de mapping déclaré. L'assertion finale suit la convention du builder : compter les surfaces/variantes OCR dont le concept canonique existe, puis vérifier l'unicité des clés comme auparavant. Les anciens contrôles de langue, propriétaires et importeur restent présents. Aucune donnée ancienne n'a été ajoutée pour satisfaire cette assertion.

XML final : `FoodHygieneRegulationImportTest`, horodatage `2026-10-07T14:26:50.388Z`, 5 tests; `AgriculturalProductsRegulationImportTest`, `2026-10-07T14:26:41.669Z`, 7 tests. Seul le dernier résultat est présenté comme réussite finale; les deux échecs intermédiaires ne sont pas masqués.

Les 13 tests Python couvrent lot absent, partiel/orphelin, conforme, divergence de concept/alias/source, tous les champs de mapping absents/altérés, propriétés manquantes des deux preuves, propriétés supplémentaires inconnues, collisions avant et après import, entre langues/noms/alias/OCR/mappings, doublons, dry-run proposé/courant, append et idempotence sur fixtures, hash/offset source incorrect et cas de normalisation. Ce ne sont pas seulement des comparaisons AST.

Les tests métier chargent les **actifs Android réels** et appellent le lexique, `IngredientMatcher`, `IngredientAnalysisService.analyzeWithDiagnostics` et le moteur réel. Les quatre cas vérifient le concept, `EXACT`, résidu vide, aucun inconnu, `NON_VEGAN`, `NON_VEGETARIAN`, ID de décision, ID des bloqueurs connus et tokens diagnostiques. L'ID responsable est `mechanically_separated_meat` dans les quatre langues.

## Propriétaires, voisinage et limites constatées

Le concept et l'alias anglais `meat` restent intégralement au propriétaire historique **`meat`**. Le token isolé `meat` garde une résolution `EXACT`, résidu vide et diagnostic responsable `meat`. La forme anglaise longue résout désormais uniquement `mechanically_separated_meat`; elle ne transfère pas la propriété du mot court. `pigmeat` et tous les autres alias historiques sont inchangés.

Pour les termes voisins (viande hachée, préparations, produits, préparation de volaille, noms d'imitation végétale, tofu/soja, sigles et formes des deux concepts différés), la classe ciblée compare le matcher **et** les résultats du pipeline avec/sans le nouvel ingrédient. Les IDs, résolutions, résidus, verdicts et inconnus restent identiques; aucun n'est réattribué au nouveau concept. Cela ne certifie pas que les anciens résultats sur toute imitation sont sémantiquement corrects : par exemple un contexte avec le mot historique `meat` peut déjà être bloquant.

**Risque réellement caractérisé :** les contre-exemples construits `sans viandes séparées mécaniquement` et `vegan mechanically separated meat` sont reconnus partiellement avec un résidu contextuel, mais le pipeline produit néanmoins `NON_VEGETARIAN` et le bloqueur `mechanically_separated_meat`. Ces mentions ne prouvent pas la présence de chair. La première expose notamment un nouveau déclenchement lexical FR; ce n'est pas une garantie de non-régression sémantique universelle. Les tests documentent cette limite et ne constituent pas une approbation de ce résultat pour une étiquette réelle. Aucune modification de matcher ou règle n'était autorisée. Une revue humaine doit apprécier ce risque de mentions hors composition/négatives/imitations; les tests de voisinage ne l'annulent pas.

Les formes avec espèce en préfixe, mots composés, flexions ou graphies non approuvées ne sont pas déclarées couvertes. Aucun échantillon réel d'étiquette n'a été évalué pour mesurer précision/fréquence. Une reconnaissance exacte de la forme de base n'est pas une preuve du sens de toute mention contenant ces mots.

Le verdict donne priorité à un ingrédient `NON_VEGAN`, y compris avec un inconnu après lui. L'analyse **ne s'arrête pas au premier animal** : le test `separatorvlees, zzzingredient` conserve l'inconnu, renvoie `NON_VEGETARIAN` et garde `stoppedAtNonVegetarian=false`. Le moteur, ses enums et les diagnostics de production sont inchangés; aucune revendication de performance nouvelle.

## Fichiers touchés et état final

Onze fichiers suivis modifiés :

- `knowledge/ingredients.json` : un concept;
- `knowledge/ingredient_aliases_multilingual.json` : quatre entrées lexicales et quatre mappings;
- `knowledge/sources.json` : deux références;
- `app/src/main/assets/ingredients.json`, `app/src/main/assets/ingredient_aliases_multilingual.json` : sorties des builders;
- `tools/build_multilingual_ingredient_mapping.py` : groupe/relation documentaires propres à la nouvelle source;
- `app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt` : assertion de cardinalité compatible avec la croissance et les concepts indisponibles;
- `docs/base-connaissances.md`, `docs/knowledge-pipeline.md` : lot et CLI documentés;
- `docs/generated/base-connaissances-data.md`, `docs/generated/multilingual-ingredient-mapping.md` : régénération normale.

Cinq fichiers nouveaux, laissés non suivis :

- `tools/import_eu_food_hygiene_regulation.py`;
- `tools/tests/test_eu_food_hygiene_import.py`;
- `app/src/test/java/com/example/isitvegan/FoodHygieneRegulationImportTest.kt`;
- `docs/sources/eu-food-hygiene-regulation.md`;
- ce rapport.

État final : même branche et HEAD, onze fichiers suivis modifiés, cinq nouveaux fichiers en plus des non suivis préexistants, aucun changement staged. Sur les 401 fichiers de départ, onze sont modifiés dans le périmètre annoncé et 390 restent identiques octet pour octet. Les sources PDF et trois livrables précédents sont inchangés. Aucun commit, suppression, déplacement ou changement d'historique.

**Explicitement exclus et inchangés :** alias `VSM`/`MSM`, noms d'espèces, formes composées, synonymes et flexions; concepts `greaves` et `frog_legs`; matcher, moteur, enums, schéma, OCR, règles d'origine/protections contextuelles et version. Les seuls changements de code de production applicatif sont donc inexistants; le code changé appartient aux outils documentaires/d'import et tests.

Non exécutés : suite Gradle complète, PIT, benchmark, test appareil/Nokia, assembleDebug, campagne exhaustive de faux positifs, nouvelle revue visuelle PDF. Aucune réussite de ces contrôles n'est revendiquée.

**GO_WITH_WARNINGS** : le lot demandé, sa provenance, ses propriétaires et ses quatre résolutions exactes sont validés par les contrôles ciblés. La revue humaine restante porte surtout sur le risque contextuel documenté, l'attribution/provenance et le diff des outils/tests avant commit. Le verdict n'étend ni le corpus autorisé ni les garanties de couverture.
