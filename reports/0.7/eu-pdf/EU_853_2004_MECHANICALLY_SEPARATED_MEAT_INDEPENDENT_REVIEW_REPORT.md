# UE 853/2004 — revue indépendante du lot viande séparée mécaniquement

Date : 7 octobre 2026. Mode : lecture seule, à l'exception de la création de ce rapport. **Décision pour un commit : NO_GO.**

Les données actuellement présentes respectent le périmètre demandé et leurs preuves locales sont cohérentes. Aucun transfert de propriétaire ou changement du moteur n'a été trouvé. Le refus de commit porte sur deux protections insuffisantes : la perte d'une garde historique dans le test de cardinalité et les angles morts du contrôle d'import face à la résolution Kotlin. Le risque sémantique des mentions négatives reste une décision produit distincte. Aucune correction n'a été appliquée.

Cette décision ne conteste pas l'origine animale de la matière définie ni l'attestation des quatre dénominations. Elle est distincte du `GO_WITH_WARNINGS` d'intégration et ne constitue aucune autorisation d'import.

## 1. État observé et identification du diff

- Branche : `master...origin/master`; HEAD : `d1f02275508c9472b1fe55ac82d792d1257f6264`.
- Index vide de changements. Onze fichiers suivis modifiés, aucun fichier suivi supprimé. Diff : 410 insertions, une suppression. Aucun changement suivi dans les moteurs, enums, schémas, OCR, protections, configuration de build ou version.
- Non suivis observés : quatre PDF 853/2004; les trois livrables exploratoires préexistants; cinq fichiers nouveaux annoncés par l'intégration. Aucun n'est staged.
- Une empreinte des **406 fichiers suivis ou non suivis présents au début de cette revue**, hors fichiers ignorés, a été conservée en mémoire. La vérification finale retrouve ces 406 fichiers identiques; le seul ajout de la revue est ce rapport.
- Le manifeste temporaire préimport `%TEMP%/iiv_853_import_baseline.json` est encore disponible. Sa comparaison aux fichiers présents retrouve 401 entrées, onze changements et aucune disparition : les 390 autres entrées, dont les PDF et rapports exploratoires, sont identiques. Ce manifeste non versionné est un élément complémentaire, pas une preuve indépendante de son instant de création. Les comparaisons des données historiques à HEAD ont été réalisées séparément.

Fichiers suivis examinés dans le diff :

| Fichiers | Changement réellement observé |
|---|---|
| `knowledge/ingredients.json` | Un objet ajouté; 487 → 488 concepts |
| `knowledge/ingredient_aliases_multilingual.json` | Quatre entrées lexicales; 1 703 → 1 707 entrées; quatre mappings, 2 089 → 2 093 |
| `knowledge/sources.json` | Deux objets ajoutés; 21 → 23 sources |
| `app/src/main/assets/ingredients.json` | Un ingrédient ajouté; projection éditoriale conforme |
| `app/src/main/assets/ingredient_aliases_multilingual.json` | Copie identique octet pour octet au JSON éditorial |
| `tools/build_multilingual_ingredient_mapping.py` | Deux branches de groupe/relation, quatre lignes; anciens comportements conservés pour les anciens concepts |
| `app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt` | Remplacement du total figé par un total calculé et filtré |
| `docs/base-connaissances.md`, `docs/knowledge-pipeline.md` | Présentation du lot et ligne du nouvel outil |
| `docs/generated/base-connaissances-data.md`, `docs/generated/multilingual-ingredient-mapping.md` | Documentation correspondant au seul nouveau concept |

Cinq fichiers nouveaux de l'intégration inspectés : `tools/import_eu_food_hygiene_regulation.py`, `tools/tests/test_eu_food_hygiene_import.py`, `app/src/test/java/com/example/isitvegan/FoodHygieneRegulationImportTest.kt`, `docs/sources/eu-food-hygiene-regulation.md` et le rapport d'intégration audité. L'importeur et les deux tests concernés ont été lus intégralement.

Les PDF non suivis préexistants sont des sources déclarées intentionnellement, pas des ingrédients ajoutés. Les anciens CSV/rapports ne sont pas lus par le nouvel importeur. Leur présence dans `git status` ne justifie pas de les inclure automatiquement dans le commit du lot. Il n'y a actuellement aucune inclusion accidentelle dans l'index.

## 2. Méthode, commandes et effet des vérifications

Instructions consultées : `AGENTS.md`, skill `knowledge-import-validation`, références `source-adapters.md` et `import-workflow.md`; conventions de `docs/base-connaissances.md`, `docs/matching.md`, `docs/verdict.md`, `docs/knowledge-pipeline.md`. Le skill `android-validation` a été lu pour connaître le périmètre de validation; aucune tâche Android n'a été exécutée.

Les commandes ci-dessous ont uniquement lu les fichiers ou calculé en mémoire. Les invocations Python utilisent `-B -X utf8`; aucun bytecode de dépôt n'est demandé. Aucune commande `--write`, aucun import effectif, aucune génération, installation, opération réseau ou modification de source n'a été effectué.

| Commande ou famille de commandes réellement exécutée | Effet et résultat |
|---|---|
| `git status --short --branch`, `git rev-parse HEAD`, `git diff --cached --stat`, `git diff --stat`, `git ls-files --others --exclude-standard` | État Git réel et inventaire initial; même HEAD et index à la fin |
| `git diff -- <fichiers>`, `git diff --name-only`, `git diff --numstat`, `git diff --cached --name-only` | Lecture du diff des onze fichiers; aucun diff staged |
| `git show HEAD:<fichier>` depuis les sondes Python | Lecture des versions historiques, sans checkout ni restauration |
| `rg`, `rg --files`, `Get-Content -Encoding UTF8`, `Get-ChildItem` ciblés | Instructions, rapports, CLI, données, code, emplacements et deux XML de tests existants |
| Scripts inline `@'…'@ \| python -B -X utf8 -` : inventaire SHA-256, audit JSON, PDF, cardinalité, sondes CLI et scan lexical | Aucun script enregistré, aucune fixture disque créée; résultats détaillés ci-dessous |
| `python -B -X utf8 tools/import_eu_food_hygiene_regulation.py --check` | Code 0; `changes=0; no modification necessary; batch is current` |
| Même outil avec `--dry-run` | Code 0; même message explicite; aucune proposition nouvelle ni régénération |
| `python -B -X utf8 tools/build_ingredients.py --check` | Code 0; structure et actifs courants; branche de vérification inspectée avant invocation |
| `python -B -X utf8 tools/build_multilingual_ingredient_mapping.py --check` | Code 0; document et parité courants; aucun appel de génération |
| `python -B -X utf8 tools/build_knowledge_docs.py --check` | Code 0; document courant; aucun appel de génération |
| `python -B -X utf8 tools/import_eu_agricultural_products_regulation.py --check` | Code 0; `changes=0`, contrôle historique général |
| Même outil avec `--animal-enrichment --check` | Code 0; `changes=0`, toutes additions à zéro |
| Même outil avec `--meat-species --check` | Code 0; `changes=0`, lot courant |
| `git diff --check` | Code 0; seuls avertissements Git de conversion LF/CRLF, sans erreur de whitespace |

Deux erreurs de sonde ont été corrigées sans toucher au dépôt : une projection d'actif initiale omettait la valeur par défaut `eNumber: ""`; un premier scan inline avait perdu les accents à travers le pipe PowerShell. La projection corrigée et le scan avec encodage UTF-8 explicite ont réussi. Leurs sorties initiales ne sont pas utilisées comme preuves de défaut des données ou du matcher.

### Sondes CLI sans fichiers temporaires

Le vrai `main()` du nouvel importeur a été appelé avec `sys.argv` et des contenus JSON substitués **en mémoire** à `Path.read_text` pour ses trois chemins éditoriaux. `Path.write_text` et `Path.write_bytes` étaient interdits par des mocks. Les huit preuves ont d'abord été recalculées sur les vrais PDF; seule cette acquisition vérifiée était mise en cache pour les cas négatifs. Le dispatcher et ses validations n'étaient pas substitués.

- **64 cas attendus validés** : courant/check, courant/dry-run, absent/check, absent/dry-run, partiel; chaque propriété de mapping manquante puis altérée; chacune des sept propriétés des deux preuves manquante puis altérée; champ supplémentaire dans concept, entrée lexicale, mapping, preuve ou source; collisions d'alias, variante OCR, nom canonique et surface de mapping, sur lot absent et présent.
- Les trois cas positifs retournent 0, les 61 cas négatifs retournent 1. Le dry-run absent propose exactement quatre entrées lexicales et quatre mappings.
- **Deux contre-épreuves supplémentaires** retournent à tort 0 : redirection OCR d'une surface approuvée et `schemaVersion: 2`; voir R2. Cela fait 66 appels au dispatcher du nouvel outil sur contenus en mémoire, en plus des deux commandes directes sur le dépôt.
- Deux appels au vrai dispatcher historique, également sans écriture et sur contenus en mémoire, acceptent une suppression coordonnée d'alias/mapping `e100/EN`; voir R1. Ils sont distincts des trois checks historiques directs sur les fichiers réels.

Ces sondes ne sont pas la réexécution des suites `unittest` du dépôt : elles ne créent aucune fixture sur disque et n'exercent jamais `--write`.

## 3. Données et sources vérifiées indépendamment

Les anciens objets sont conservés **intégralement et dans le même ordre** : tous les 487 ingrédients, les 21 sources, les 1 703 entrées lexicales et les 2 089 mappings de HEAD sont des préfixes identiques des listes actuelles. Les autres propriétés racines du lexique, dont les corrections OCR et la version, sont inchangées. Même contrôle pour les anciens objets de l'actif ingrédients.

Les 488 objets de l'actif ingrédients correspondent à la projection du builder : champs éditoriaux attendus, valeur par défaut d'E-number, et sources jointes dans `source`. L'actif lexical est identique au fichier éditorial. Aucun alias historique n'a été déplacé, dupliqué ou réattribué dans ce diff. `meat`, `pigmeat`, les cinq espèces déjà intégrées et les concepts voisins conservent leurs objets et mappings. Cela confirme le diff actuel, pas une garantie permanente apportée par le test modifié.

Le nouvel ingrédient est à `knowledge/ingredients.json:8570`; les quatre entrées lexicales commencent à `knowledge/ingredient_aliases_multilingual.json:14023`; les mappings commencent à `:36075`; les sources à `knowledge/sources.json:231` et `:249`.

| Langue | Forme lexicale | Preuve 853, p.16, I.1.14 : surface / offset | Preuve 1169, p.49, VII.B.18 : surface / offset |
|---|---|---|---|
| FR | `viandes séparées mécaniquement` | Même surface / 2215 | `Viandes séparées mécaniquement` / 1764 |
| NL | `separatorvlees` | `Separatorvlees` / 2047 | `separatorvlees` / 1563 |
| EN | `mechanically separated meat` | `Mechanically separated meat` / 1951 | `mechanically separated meat` / 1468 |
| DE | `Separatorenfleisch` | Même surface / 2101 | Même surface / 1690 |

La forme FR est le nom canonique; les trois autres sont ses alias. Le lexique a une entrée par langue, une surface par entrée et aucune variante OCR nouvelle. Statut : `NON_VEGAN`, conformément aux données existantes et à `docs/base-connaissances.md:61`. `NON_VEGETARIAN` reste un verdict détaillé, pas un statut d'ingrédient ajouté.

Chaque mapping contient les neuf propriétés existantes annoncées et deux objets `sourceEvidence` avec les sept propriétés existantes. Les quatre mappings portent le bon concept, la bonne langue, le groupe `food-hygiene-regulation`, la relation `REGULATORY_ALIAS`, la confiance `REVIEWED` et les deux identifiants de source. Les ID de preuve sont `853-I1.14-{LANG}-mechanically_separated_meat` et `1169-VIIB18-{LANG}-mechanically_separated_meat`.

Les chemins se retrouvent dans le registre des sources : `reference-input/eu-food-labelling/05-food-hygiene/` pour 853, et `reference-input/eu-food-labelling/00-general-food-labelling/` pour 1169. Empreintes recalculées, en-têtes CELEX/langue/date, pages et surfaces aux offsets vérifiés par `pypdf` :

| PDF | Pages | SHA-256 vérifié |
|---|---:|---|
| `CELEX_02004R0853-20260507_FR_TXT.pdf` | 94 | `302312dbf898ef8993b11057070691801c8c374d8de13b5947ff592a2309562e` |
| `CELEX_02004R0853-20260507_NL_TXT.pdf` | 94 | `30fa09a89bcede829396f29769812b5c4503f404c91a9acb2771b429a1356543` |
| `CELEX_02004R0853-20260507_EN_TXT.pdf` | 94 | `125b70e276a8e21d9af7e72c6af773d19068896b126a54b777c08bd49fb7037d` |
| `CELEX_02004R0853-20260507_DE_TXT.pdf` | 94 | `9874ff8be78fcd7d33a16d837f85cbe46ae7abcfd7d70d1c1dc9542deeb7602f` |
| `CELEX_02011R1169-20250401_FR_TXT.pdf` | 60 | `80fedf36e7930dddfb270871d68ff10864009fd3ac71c9d350876402c9d6a81e` |
| `CELEX_02011R1169-20250401_NL_TXT.pdf` | 60 | `36dd5f61e3e9aeb95c2e51b4ff22ba90056639f726d4b30aae97c63c653c8ef3` |
| `CELEX_02011R1169-20250401_EN_TXT.pdf` | 60 | `6cdf4190d6fa4a99d2e7d3124611724ec4cf69159563f88b35dc3c50dd416c63` |
| `CELEX_02011R1169-20250401_DE_TXT.pdf` | 60 | `c2409d5cb83f784a8ac94ceeb8d4837da343189e024f5d1a58cc015f6bb62aa2` |

Les dates visibles sont respectivement `07.05.2026` et `01.04.2025`. Les offsets sont en caractères, base 0 dans la couche texte non modifiée; pages PDF en base 1. Les définitions parallèles 1.13–1.15 ont été relues : hachage, séparation mécanique altérant les fibres et préparation de viande restent distincts. Le point 18 de 1169 atteste la dénomination d'ingrédient avec indication des espèces; aucune formule espèce + matière n'est inventée.

**Limites documentaires :** aucun nouveau rendu visuel PDF, aucune étude de fréquence ni échantillon réel d'étiquette dans cette revue. L'inspection visuelle de la revue préalable est un résultat historique, pas une inspection visuelle refaite ici. Les hashes correspondent à ses sources; la couche texte des passages a été vérifiée à nouveau. Espaces parasites et césures restent visibles dans cette couche, sans être ajoutés aux alias.

L'attribution et la distinction éditoriale existent effectivement dans les nouvelles sources (`use`, importeur:78) et dans `docs/sources/eu-food-hygiene-regulation.md:35–38`. Elles citent Union européenne/EUR-Lex, CC BY 4.0 et la sélection/classification comme décisions de l'application. Leur contenu est cohérent avec les conditions **rapportées** de l'avis juridique cité. Aucun exemplaire local de cet avis n'a été identifié et il n'a pas été consulté à nouveau en ligne : sa teneur et l'applicabilité juridique ne sont pas indépendamment certifiées par cette passe. Les définitions prouvent l'origine animale; elles ne prouvent pas la fréquence d'emploi ni la présence réelle de matière dans toute mention.

## 4. Importeur : contrats, contrôles et limites

`tools/import_eu_food_hygiene_regulation.py:1–219` a été lu intégralement. Il est borné à un concept et aux quatre constantes approuvées. Ses chemins de mutation sont exclusivement les trois JSON éditoriaux (`:18–21`, `:200–202`); il ne lance ni builder, sous-processus, export de rapport ni génération Android.

- `--dry-run`, `--check`, `--write` sont mutuellement exclusifs et obligatoires (`:205–210`).
- Lot absent : check non nul; dry-run propose en mémoire. Alias/mappings orphelins : refus avant mutation.
- Lot présent : égalité complète du concept, des quatre entrées lexicales, des quatre mappings, des preuves imbriquées et des deux sources; propriétés supplémentaires refusées. Un lot divergent n'est pas réparé. Les sources existantes du même ID doivent aussi correspondre exactement.
- Lot conforme : retour avant toute écriture, quel que soit le mode. Idempotence sans écriture confirmée pour check et dry-run; le chemin write conforme est également un retour anticipé par inspection.
- Acquisition : huit hashes, nombres de pages, en-têtes, surface à l'offset, normalisation et présence du point avant la surface (`:82–109`). Le hash figé lie aussi la preuve au bon contexte; le test du point seul serait moins fort sans ce hash.
- Mutation proposée : append uniquement, puis nouvelle vérification de collisions. Les anciennes listes sont comparées avant/après en mémoire (`:185–194`).

La normalisation du nouvel outil (`:53–61`) correspond à `mutation-core/src/main/kotlin/com/example/isitvegan/TextNormalizer.kt:6–15` pour les surfaces étudiées : minuscules, ligatures, NFD, retrait de toutes les marques Unicode, caractères ASCII et E-code initial. Les cinq cas partagés incluent une marque de classe combinatoire zéro. Les suites Python/Kotlin ont de vrais cas identiques; ce n'est pas une preuve universelle d'identité des bibliothèques Unicode pour tout alphabet.

Les collisions exactes interlangues et par langue des nouvelles clés sont examinées contre noms canoniques, alias, E-numbers, alias multilingues, variantes OCR et surfaces de mapping. Elles sont vérifiées même lorsque le lot existe. La comparaison des surfaces de mapping est plus stricte que le chemin lexical historique, qui n'utilise pas un mapping seul comme alias. Les collisions de sous-expression ne sont pas rejetées : `meat` dans la forme EN est une spécialisation attendue, et les bornes et la sélection du terme long appartiennent au matcher (`IngredientMatcher.kt:153–162`, `:198–251`).

Ce nouvel adaptateur reprend le modèle d'acquisition/transformation/validation/mutation prescrit; aucun importeur 1308 n'est utilisé pour extraire 853. La duplication de petites fonctions de provenance/normalisation est compréhensible au regard des outils spécifiques existants; elle impose toutefois de maintenir les cas partagés. Aucun besoin de refonte générale n'est identifié pour ce lot.

## 5. Constats par gravité

### R1 — P2, bloquant : la cardinalité calculée n'assure plus la garde historique perdue

**Emplacement :** `app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt:44–58`; compléments `:30–37` et `:60–63`.

L'ancien `assertEquals(2089, mappings.size)` détectait une variation de total à cette révision. Son nombre figé devait évoluer avec un ajout autorisé; le workflow demande d'ailleurs de ne pas en faire un seuil universel. Cependant, son remplacement calcule l'attendu à partir des mêmes données actuelles et des concepts actuellement disponibles. Il conserve une vérification de synchronisation, mais perd la détection d'une suppression coordonnée.

**Contre-épreuve exécutée en mémoire :** retirer l'entrée lexicale entière `e100/EN`, surface `Curcumin`, et son mapping. Le total calculé et le total réel deviennent tous deux **2092**; l'assertion de cardinalité et l'unicité passent. Aucun alias vide n'est introduit. Les deux checks historiques `--animal-enrichment --check` et `--meat-species --check`, appelés via leurs vrais dispatchers sur cette variante, retournent aussi `changes=0`. Aucune donnée du dépôt n'a été retirée. La classe Kotlin complète n'a pas été réexécutée sur cette variante; le constat porte sur les assertions modifiées et les deux CLI effectivement sondés.

Le filtrage suit une vraie convention existante : `cereals/NL/granen` n'a pas de concept canonique et ne possède pas de mapping. Ce cas est présent avant le lot et documenté dans `docs/generated/multilingual-ingredient-mapping.md:446`. Il explique 2094 surfaces lexicales contre 2093 mappings; il ne justifie pas de considérer automatiquement toute future disparition de concept comme une nouvelle exception acceptable.

**Ce qui reste garanti :** parité source/actif; égalité du nombre de mappings avec les surfaces des concepts disponibles; unicité des triplets; quatre langues pour cinq catégories agricoles; checks des lots agricoles; statuts et présence d'une liste de concepts historiques; assertions réelles sur les 17 formes des cinq espèces.

**Ce qui est perdu ou non garanti :** garde sur la baisse coordonnée alias/mapping; identité du catalogue historique global; comparaison terme par terme des propriétaires et métadonnées historiques; liste contrôlée des concepts indisponibles. Une substitution conservant le nombre peut également échapper à cette seule cardinalité. L'ancien total ne garantissait déjà pas les propriétaires terme par terme; il ne faut pas lui attribuer cette propriété.

**Impact :** aucun historique perdu dans le diff actuel, mais une protection existante est affaiblie sans garde de préservation durable de remplacement. Les fixtures Python partent aussi du corpus présent : préserver ce corpus dans une fixture ne prouve pas qu'il n'a pas déjà perdu une entrée.

**Recommandation :** garder le contrôle dynamique de synchronisation, mais rétablir une référence indépendante de préservation des mappings/alias historiques et de leurs propriétaires, compatible avec les ajouts autorisés. Borner explicitement l'exception `cereals`, puis ajouter un cas négatif de suppression coordonnée et de concept devenu indisponible. Ne pas simplement transformer 2089 en seuil universel.

### R2 — P2, bloquant : `--check` peut valider un lot incompatible avec la résolution Kotlin

**Emplacement :** `tools/import_eu_food_hygiene_regulation.py:128–151`, `:157–180`; `MultilingualIngredientLexicon.kt:68–89`, `:119–127`; `VeganAnalyzer.kt:353–364` dans `mutation-core/src/main/kotlin/com/example/isitvegan/`.

**Contre-épreuve OCR exécutée en mémoire :** ajouter à `ocrCorrections` l'objet `{"language":"NL","from":"separatorvlees","to":"rundvlees"}`. Le vrai dispatcher `--check` retourne **0**, `changes=0; … batch is current`. Les collisions ne parcourent pas `ocrCorrections`; l'égalité complète ne couvre que les entrées du lot et ses sources.

Le chargeur réel applique précisément ces corrections **avant** de rechercher l'alias. La correction est structurellement valide et `rundvlees` est détenu par `meat` dans le lexique NL. Par lecture des API, un appel `resolve("separatorvlees", DUTCH, database)` prendrait donc ce propriétaire et le pipeline utiliserait son nom canonique. Cette redirection n'a pas été exécutée dans une nouvelle JVM; elle découle directement du branchement `resolve` et du propriétaire relevé. Les quatre tests métier positifs détecteraient cette dérive s'ils étaient relancés, mais le check d'import seul l'accepte et aucun test Python existant ne couvre ce cas.

**Autre contre-épreuve exécutée en mémoire :** changer seulement la propriété racine `schemaVersion` de 1 à 2. Même succès du vrai check. Le chargeur Kotlin exige explicitement 1 et rejetterait le lexique avant analyse. La version réelle reste 1; aucune de ces deux altérations n'existe dans les fichiers courants.

**Impact :** le check confirme l'égalité de certains objets, pas leur compatibilité complète avec la voie de résolution actuelle. Les tests de variantes OCR (`ocrVariants`) ne couvrent pas les règles de redirection (`ocrCorrections`). Les cas négatifs pertinents manquent, malgré une bonne couverture des propriétés de mapping/provenance.

**Recommandation :** valider la version de lexique prise en charge et contrôler les corrections susceptibles de détourner les quatre formes vers un autre concept; ajouter des cas négatifs passant par le vrai dispatcher. Conserver une validation par le vrai lexique Kotlin si une équivalence fiable ne peut être établie en Python. Aucun changement du moteur/matcher n'est nécessaire pour corriger ces protections d'outil.

### R3 — P2, risque produit à trancher : nouvelle attribution animale dans une négation française

**Emplacement :** `FoodHygieneRegulationImportTest.kt:79–90`; `IngredientMatcher.kt:205–251`, `:352`; `VerdictEngine.kt:27–34`; rapport d'intégration:112.

Le test caractérise volontairement `sans viandes séparées mécaniquement` et `vegan mechanically separated meat` comme `PARTIAL_CONTEXTUAL`, avec résidu, verdict `NON_VEGETARIAN` et responsable `mechanically_separated_meat`. Il ne les teste pas comme contextes sûrs. Le diagnostic est cohérent avec la correspondance lexicale mais désigne une présence animale que la mention négative française ne démontre pas.

Un scan statique normalisé avec les véritables bornes alphanumériques compare HEAD et le corpus courant : aucune clé candidate ancienne pour la phrase FR; la nouvelle dénomination est désormais candidate. Pour la phrase EN, `meat` était déjà candidat à HEAD; le lot change principalement la précision du coupable, et n'est pas la cause initiale de ce risque anglais. Le token isolé `meat` conserve exactement son propriétaire avant/après.

La négation française constitue donc une nouvelle exposition sémantique, pas uniquement une limite ancienne réidentifiée. Ces chaînes sont des contre-exemples construits, pas des étiquettes observées. On ne peut ni chiffrer leur fréquence, ni conclure que toute étiquette complète contenant cette mention est mal analysée : les assertions utilisent `MANUAL_INGREDIENT_LIST`; l'extraction `FULL_LABEL`/OCR peut écarter des mentions hors composition et n'a pas été réévaluée ici.

**Impact :** lorsqu'une telle expression arrive comme token analysé, l'animal connu gagne même sur le résidu inconnu. Un résultat lexical exact sur les quatre noms isolés ne résout pas ce problème de présence. Aucune protection générale de négation/imitations n'est disponible pour ce concept.

**Recommandation :** décider explicitement de l'acceptabilité produit et obtenir des exemples locaux de compositions/mentions négatives avec modes d'entrée pertinents. Si cette exposition est jugée incompatible, différer l'activation dans une tâche autorisée; ne pas modifier le matcher dans cette revue. R1/R2 suffisent déjà au NO_GO technique; un éventuel accord sur ce risque ne les lève pas.

### R4 — P3, non bloquant : écritures éditoriales non transactionnelles

**Emplacement :** `tools/import_eu_food_hygiene_regulation.py:200–202`, `:214–215`.

Les trois fichiers sont écrits successivement. Une erreur d'E/S après la première écriture peut laisser un lot partiel; le code capture l'erreur mais ne restaure pas les fichiers. Le check suivant refuserait ce lot plutôt que le réparer silencieusement. Aucun échec de ce type ni nouvelle écriture n'a été provoqué dans cette revue. Ce risque existe aussi dans plusieurs importeurs historiques.

**Recommandation :** documenter la procédure de récupération d'une écriture partielle et, dans une tâche dédiée, envisager une préparation des sorties avant remplacement. Cela ne doit pas servir à élargir ce lot ou à corriger les données courantes qui sont conformes.

## 6. Force des tests et preuves de leur exécution

### Python

`tools/tests/test_eu_food_hygiene_import.py:1–198` utilise le vrai `main`, un sous-processus Python et des chemins éditoriaux temporaires. Les modes sans écriture interdisent `Path.write_text`/`write_bytes` dans le processus CLI; les octets des fixtures sont comparés. Les tests write sont sur fixtures, pas sur le corpus. Les pages PDF réelles sont vérifiées une fois, puis cette acquisition est substituée dans les processus des fixtures.

Les négatifs couvrent des champs variés de mapping, les preuves et champs supplémentaires, les sources, collisions interlangues/noms/OCR/mappings et lots présents/absents. L'égalité d'objets complets est plus forte qu'une liste partielle de propriétés. Les attendus conformes proviennent toutefois de `expected_batch` du même importeur; ils ne sont pas une preuve indépendante de justesse réglementaire. Le test de proposition encode séparément les quatre formes autorisées, et les PDF ont été vérifiés indépendamment dans cette revue.

Les fixtures lisent le dépôt actuel et exigent les huit vrais PDF : elles ne sont pas hermétiques. Elles ne masquent pas cette dépendance dans leur setup (`:38–45`), mais peuvent recopier une perte historique déjà survenue. Les règles de correction OCR et la version racine n'ont pas les cas négatifs constatés nécessaires.

**Non réexécutées ici :** les suites de 13 tests nouveaux et de 17 tests historiques. Elles créent des fichiers temporaires et certains tests nouveaux exercent write sur fixtures; cela ne respecte pas la restriction de cette revue à une seule création de fichier. Les nombres de tests sont corroborés par leur code, mais leurs durées et réussites historiques ne disposent pas d'un journal Python cité et conservé permettant de les certifier indépendamment. Les 64 sondes attendues et les contre-épreuves de cette revue sont des validations supplémentaires réellement exécutées, pas une prétendue relance de ces suites.

### Kotlin

`FoodHygieneRegulationImportTest.kt:13–45` charge les trois actifs réels et utilise le vrai lexique, matcher et service d'analyse. Les quatre formes et langues sont codées indépendamment de l'importeur. Assertions réelles : statut `NON_VEGAN`, surfaces exactement approuvées, propriétaire par langue, singleton du bon concept, `EXACT`, résidu vide, absence d'inconnus, `NON_VEGETARIAN`, responsable de décision, bloqueur d'explication et IDs des tokens. Ce n'est pas un mock du verdict ni une simple comparaison de JSON.

Le voisinage (`:48–76`) compare avec/sans le nouveau concept, sans réattribuer préparations, hachage, substituts, sigles et formes exclues. Ce témoin est construit à partir des données courantes en retirant le nouveau concept; il montre l'effet de cet ajout, pas la conservation historique à lui seul. Les entrées multilingues du nouveau concept restent dans le témoin, mais les expressions voisines testées ne les utilisent pas.

Le test `separatorvlees, zzzingredient` (`:93–100`) vérifie priorité animale, inconnu conservé et `stoppedAtNonVegetarian=false`. Le moteur de production confirme ce comportement : parcours de tous les tokens, puis agrégation; aucun arrêt au premier animal n'est revendiqué. Le diagnostic conserve bien l'ID du responsable.

**XML existants effectivement lus**, dans `app/build/test-results/testDebugUnitTest/` :

| Classe | Horodatage XML | Tests | Échecs / erreurs / ignorés |
|---|---|---:|---|
| `FoodHygieneRegulationImportTest` | `2026-10-07T14:26:50.388Z` | 5 | 0 / 0 / 0 |
| `AgriculturalProductsRegulationImportTest` | `2026-10-07T14:26:41.669Z` | 7 | 0 / 0 / 0 |

Noms des cinq tests nouveaux et des sept tests agricoles présents dans ces XML; cohérence avec les sources lues et le rapport. Cela confirme l'existence du résultat final annoncé, pas une nouvelle exécution ni la compilation fraîche du code pendant cette revue. Les deux échecs intermédiaires rapportés ne sont pas prouvés par ces XML finaux; ils restent déclaratifs.

La commande ciblée est présente et cohérente avec les classes : `.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.FoodHygieneRegulationImportTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest`. **Elle n'a pas été exécutée par cette revue**, car Gradle écrit ses sorties. Aucun Gradle complet, PIT, appareil/Nokia, benchmark, assembleDebug ou nouveau test JVM n'a été lancé.

## 7. Tableau des affirmations du rapport audité

| Affirmation importante | Statut de revue | Preuve et limite |
|---|---|---|
| Un concept, exactement quatre formes FR/NL/EN/DE, `NON_VEGAN` | **CONFIRMÉ** | Diff JSON et projection d'actif; formes et définition locales |
| Aucun ajout de sigle, espèce, flexion, `greaves`, `frog_legs` | **CONFIRMÉ** | Aucun ajout de données correspondant; leur mention dans tests/docs d'exclusion n'est pas un alias |
| Historique et propriétaire `meat` préservés | **CONFIRMÉ** | Comparaison exhaustive des anciens objets à HEAD; sources et lexique append uniquement |
| Huit preuves, bonnes consolidations/pages/offsets/hashes | **CONFIRMÉ** | Extraction et SHA-256 nouveaux sur les huit PDF; contrôle visuel antérieur seulement |
| Preuve de dénomination d'ingrédient dans 1169 | **CONFIRMÉ** | Point 18, quatre langues, avec mention des espèces; aucune fréquence démontrée |
| Attribution et distinction éditoriale cohérentes avec l'avis cité | **PARTIELLEMENT CONFIRMÉ** | Textes d'attribution observés; avis en ligne et applicabilité non revérifiés |
| Check refuse absence, lot partiel et provenance divergente | **CONFIRMÉ** | Sondes du vrai dispatcher, comparaison complète et codes non nuls |
| Propriétés supplémentaires des objets du lot refusées | **CONFIRMÉ** | Code et négatifs sur les cinq types d'objets; ce n'est pas une validation de toutes les propriétés racines du lexique |
| Contrôles de collision compatibles avec le runtime | **PARTIELLEMENT CONFIRMÉ** | Normalisation/alias/OCR variants/noms contrôlés; corrections OCR omises, R2 |
| Modes sans écriture et état courant explicitement annoncés | **CONFIRMÉ** | Commandes directes, guards en mémoire et vérification finale des empreintes |
| Write est minimal et idempotent | **PARTIELLEMENT CONFIRMÉ** | Chemins et retour anticipé inspectés; résultat du diff conforme; aucune écriture ou simulation write dans cette revue, risque R4 |
| Nouveau compte de mappings compatible avec croissance et concept indisponible | **CONFIRMÉ** | Total 2093 et exception historique `cereals`; la protection historique perdue est détaillée en R1 |
| 13 nouveaux tests Python et 17 historiques ont réussi | **NON CONFIRMÉ** | Implémentations présentes; aucune relance de ces suites ou journal de réussite indépendant disponible ici |
| Résultat Gradle final : 5 + 7 tests sans échec | **PARTIELLEMENT CONFIRMÉ** | Deux XML finaux concordants réellement lus; aucune relance Gradle |
| Les quatre formes ont des assertions EXACT/résidu/inconnus/verdict/bloqueur réels | **CONFIRMÉ** | API et assertions inspectées; XML de réussite existant, sans nouvelle sonde JVM |
| Faux positifs construits reconnus et rapportés honnêtement | **CONFIRMÉ** | Test explicite et explication; risque FR nouvellement exposé, EN historiquement lié aussi à `meat` |
| L'analyse ne s'arrête pas au premier animal | **CONFIRMÉ** | `VeganAnalyzer.kt:455`, moteur, test et XML; aucun gain de performance revendiqué |
| `GO_WITH_WARNINGS` suffit à accepter le commit | **NON CONFIRMÉ** | Jugement de cette revue : R1/R2 nécessitent une correction de protections avant commit |

## 8. Décision et actions recommandées, sans correction appliquée

**NO_GO pour le commit de l'ensemble actuel.** Aucune anomalie des quatre données intégrées ou des preuves n'a été trouvée, mais le lot inclut un importeur et une modification de test dont les protections doivent être renforcées.

1. Remplacer la protection historique perdue par une garde indépendante compatible avec la croissance; couvrir négativement une suppression coordonnée et une indisponibilité canonique nouvelle. Conserver l'exception historique identifiée sans généraliser le filtrage à toute perte future.
2. Fermer les angles morts de compatibilité du nouvel check : version du lexique et corrections OCR détournant les quatre formes. Couvrir les deux contre-épreuves et vérifier le bon propriétaire via la vraie API Kotlin.
3. Décider séparément du risque de négation/imitations. Ne pas présenter les tests qui caractérisent le faux positif comme une validation de sécurité sémantique; compléter avec des cas locaux dans les modes de composition/étiquette réellement employés, dans une future tâche autorisée.
4. Avant commit, contrôler l'attribution à partir de l'avis juridique applicable et sélectionner explicitement les fichiers; ne pas inclure les PDF/rapports non suivis préexistants par simple sélection globale.

Les recommandations ne demandent ni transfert d'alias ni changement des statuts, du matcher ou du moteur dans cette revue. Les données courantes peuvent être préservées pendant une correction d'outillage ultérieure.

Contrôle final : même HEAD et index; les 406 fichiers de départ restent présents et identiques octet pour octet, notamment JSON, actifs, code, tests, documentation, huit PDF et rapports existants. Seul ce rapport a été créé. Aucun commit.
