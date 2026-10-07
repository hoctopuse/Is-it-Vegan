# EU 1308/2013 — revue documentaire multilingue des viandes, v0.7

Date : **2026-10-06**. Révision de référence : **fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea** (`fc74f08`, `feat: enrich animal knowledge from EU 1308/2013`). Consolidations locales CELEX **02013R1308 du 18.08.2026**.

**Revue terminée, propositions non importées.** Chaque ligne du [CSV de revue](EU_1308_2013_MEAT_MULTILINGUAL_DOCUMENTARY_REVIEW.csv) porte `review_status=REVIEW_REQUIRED` et `applied=NO`. Les IDs proposés sont des pistes éditoriales, pas des concepts créés dans l'application.

## Comptes et unités

| Mesure | Compte exact | Sens |
|---|---:|---|
| Concepts déjà présents concernés | **4** | IDs distincts en base actuelle, déjà `NON_VEGAN` |
| Alias potentiels | **143** | Clés distinctes (cible proposée, langue, forme normalisée) |
| Dont formes absentes des alias canoniques et linguistiques exacts | **140** | Une reconnaissance partielle peut déjà exister |
| Dont métadonnées de langue seulement | **3** | viande FR, vlees NL, meat EN : déjà reconnus exactement |
| Concepts distincts proposés | **24** | Pistes sémantiques distinctes, aucun statut attribué aux concepts absents |
| Données réellement importées ou modifiées | **0** | Aucune mutation de connaissance |
| Groupes de revue | **46** | 37 viandes/espèces/morceaux/abats/préparations, 9 graisses |
| Lignes du CSV | **299** | 298 preuves distinctes langue/page/offset/forme ; Pig fat est réutilisé |
| Sondes Kotlin exécutées | **598** | 299 formes brutes et 299 lectures, chacune par matcher direct puis voie lexicale |

Les viandes représentent **132 clés d'alias** et **23 pistes de concepts** ; les graisses, **11 clés** et **1 piste** (`pork_backfat`). Ces sous-totaux sont disjoints. Par langue : FR **38**, NL **37**, EN **33**, DE **35**. **55 clés** demandent une validation explicite de contexte ; **8 clés** de fractions grasses sont de priorité basse avec usage alimentaire à confirmer. Ces deux nombres sont des sous-ensembles des 143 clés.

Le nombre d'alias n'est ni un quota ni le nombre de nouvelles reconnaissances. Sur les 143 lignes comptées : 108 `NONE`, 31 `PARTIAL_CONTEXTUAL`, 1 `COVERED` et 3 `EXACT` après la voie lexicale. `huile de saindoux` est déjà couverte sans alias complet ; une proposition améliorerait surtout la traçabilité. Les **10 formes d'espèce déjà alias de meat** restent à leur propriétaire actuel et sont exclues du compte d'alias nouveaux. Aucune réaffectation n'est autorisée ou exécutée.

Deux pistes (`other_equid_meat`, `meat_extract`) n'ont **aucune clé d'alias proposée** : le produit est documenté par une liste collective, mais un alias individuel d'étiquette demanderait une preuve supplémentaire. Elles restent incluses dans les 24 pistes de concepts, avec cette réserve.

## État Git et préservation

État réel au départ : `git status --short --branch` → `## master...origin/master`, sans modification suivie, staged ou non suivie ; `git diff --stat` vide. Le HEAD est le commit animal annoncé, et non celui de départ des rapports historiques. Les **383 fichiers suivis présents** ont été empreintés avant travaux et revérifiés octet pour octet avant écriture. Les rapports historiques et les PDF sont préservés. Les deux noms de sortie étaient absents avant ouverture exclusive.

Aucun importeur, builder, JSON éditorial, asset Android, moteur, test, document sous `docs/`, version ou historique Git n'a été modifié. Aucun commit/push. Seuls ce Markdown et son CSV sont créés dans le dépôt.

Consignes lues : [AGENTS.md](../../../AGENTS.md), [knowledge-import-validation](../../../.agents/skills/knowledge-import-validation/SKILL.md), [source-adapters.md](../../../.agents/skills/knowledge-import-validation/references/source-adapters.md), [import-workflow.md](../../../.agents/skills/knowledge-import-validation/references/import-workflow.md), [matching.md](../../../docs/matching.md), [documentation de source](../../../docs/sources/eu-agricultural-products-regulation.md) et manifeste sectoriel. La séparation acquisition/parsing/matching/verdict, la visibilité des inconnus, la distinction présence/traces, la revue OCR et les statuts déterministes restent les invariants de cette passe.

## Sources primaires et historiques

Les [rapport d'extraction large](EU_1308_2013_BROAD_CANDIDATE_EXTRACTION_REPORT.md) et [CSV large](EU_1308_2013_BROAD_CANDIDATES.csv) servent de pistes, confrontées ensuite aux PDF et à la base actuelle. Les [rapport du lot animal](EU_1308_2013_ANIMAL_AUTHORIZED_IMPORT_REPORT.md) et [résultats](EU_1308_2013_ANIMAL_AUTHORIZED_IMPORT_RESULTS.csv) documentent le lot déjà commité : 77 formes, dont 20 sur `animal_fat` et 9 sur `edible_offal`. Leurs décisions historiques ne sont pas une autorisation d'import pour cette revue.

Source actuelle : `eu-agricultural-products-regulation-1308-2013-20260818` dans `knowledge/sources.json`, Parlement européen/Conseil, consolidation 2026-08-18, quatre chemins locaux. Aucun téléchargement ni contrôle juridique de la version publiée en ligne. L'avertissement de consolidation présente le texte comme outil documentaire sans effet juridique. Aucun terme de licence, source ou attribution nouveaux n'a été inventé ou ajouté.

| PDF | Pages | SHA-256 recalculé, conforme au manifeste |
|---|---:|---|
| [CELEX_02013R1308-20260818_FR_TXT.pdf](../../../reference-input/eu-food-labelling/02-sector-product-standards/CELEX_02013R1308-20260818_FR_TXT.pdf) | 219 | `5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c` |
| [CELEX_02013R1308-20260818_NL_TXT.pdf](../../../reference-input/eu-food-labelling/02-sector-product-standards/CELEX_02013R1308-20260818_NL_TXT.pdf) | 219 | `99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74` |
| [CELEX_02013R1308-20260818_EN_TXT.pdf](../../../reference-input/eu-food-labelling/02-sector-product-standards/CELEX_02013R1308-20260818_EN_TXT.pdf) | 219 | `b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11` |
| [CELEX_02013R1308-20260818_DE_TXT.pdf](../../../reference-input/eu-food-labelling/02-sector-product-standards/CELEX_02013R1308-20260818_DE_TXT.pdf) | 219 | `64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda` |
## Méthode et signification des résultats

Les quatre PDF ont été extraits à nouveau avec `pypdf`, page par page (**876 pages**), en mémoire/temporaire hors dépôt. Identité CELEX/date et présence des pages vérifiées ; les 876 textes concordent avec les TXT historiques après retrait des seuls blancs de bord. Inspection ciblée des annexes I XV/XVII/XVIII/XX/XXIV, II V/VIII, IV A/B/C, VII I/V et IX, avec recherches dans tout le texte. **44 contrôles plain/layout** : pages 143, 145, 148, 149, 153, 154, 156, 172, 173, 185 et 186 dans les quatre versions ; inventaires de caractères hors espaces/césures souples identiques. Ce contrôle du même extracteur ne remplace pas une inspection visuelle ou une extraction indépendante.

`source_form` conserve une sous-chaîne **exacte** de la couche texte. `source_offset` est un index Python en caractères 0-based ; `pdf_page` est 1-based ; `context_excerpt` est un extrait exact de cette page. `source_reading` ne répare que les espaces/césures documentés. **15 lignes** possèdent une lecture distincte de la forme brute. Les tableaux montrent les formes lisibles ; les formes brutes, retours de ligne, rubriques et contextes par langue restent dans le CSV. Aucun singulier, pluriel, complément d'espèce, nom de recette ou traduction absents n'a été fabriqué.

Un sous-terme présent dans un libellé n'est pas automatiquement une dénomination réglementaire autonome. L'alignement repose sur la même rubrique/NC/définition/tableau. L'ordre des coupes bovines est une **inférence de correspondance à réviser**, pas une équivalence lexicographique certifiée. Dans les tableaux de veau, la langue du nom dépend du nom et du pays, pas de la seule langue du PDF. `Gallus domesticus` est marqué comme taxon **latin** imprimé dans la version EN ; aucune traduction chicken n'en est déduite.

Comparaison en lecture seule avec `knowledge/ingredients.json` (**482 concepts**), les alias historiques et mappings de `ingredient_aliases_multilingual.json`, et le code actuel `IngredientMatcher`, `TextNormalizer`, `MultilingualIngredientLexicon`, `OcrIngredientNormalizer` et le chemin lexical d'`IngredientAnalysisService`. L'application dépend de `:mutation-core` et sa façade Android délègue réellement au service JVM. Les champs de reconnaissance/statut du JSON éditorial et de l'asset ont la même valeur ; le fichier multilingue est identique octet pour octet. eNumber absent/chaîne vide sont traités comme au chargeur, sans modifier leurs représentations historiques.

Le matcher actuel a été compilé sans modification avec ses **24 fichiers Kotlin**, le compilateur **2.4.20 déjà en cache**, JBR local et cible JVM 17 vers un JAR temporaire. Le programme de sonde appelle le code réel :
```kotlin
val raw = matcher.match(IngredientToken(value, 0, 0))
val lex = knowledge.multilingualLexicon.resolve(value, language, knowledge.ingredients)
val canonical = knowledge.ingredients.firstOrNull { it.id == lex.canonicalId }
// Le programme conserve le suffixe à partir de '(' comme la voie actuelle.
val text = canonical?.name /* avec suffixe conservé */ ?: lex.correctedText
val current = matcher.match(IngredientToken(OcrIngredientNormalizer.forMatching(text), 0, 0))
```
Le snippet est un résumé ; les trois chemins de connaissance actuels sont chargés avec `IngredientKnowledge.fromJson`. Les résultats du matcher direct et de la voie lexicale sont conservés sur brut et lecture. Aucun verdict n'est calculé. `near_existing_aliases` est seulement un rapprochement textuel avec les alias de la famille : **le matcher ne fait ni fuzzy matching, ni traduction, ni stemming**.

Les colonnes `broad_exact_candidate_ids`, `broad_containing_candidate_ids` et `authorized_import_review_ids` donnent les liens aux revues historiques. **84 lignes** ont nécessité une recherche primaire ciblée, sans forme exacte ni candidat contenant dans le CSV large ; il pouvait y exister un terme voisin. Les faux sous-termes issus de segmentation, tel Sla dans un contexte de slachtafvallen, ne sont pas promus en ingrédients.

## Couverture actuelle

| ID existant | Statut déjà présent | Couverture utile vérifiée |
|---|---|---|
| `meat` | `NON_VEGAN` | viande/vlees/meat, bovin, porc, veau/veal et catégories collectives ; pas d'IDs séparés par espèce |
| `edible_offal` | `NON_VEGAN` | abats comestibles et foies de volaille importés |
| `animal_fat` | `NON_VEGAN` | saindoux/reuzel/lard EN/Schweineschmalz et graisses nommées par espèces |
| `poultry_meat_preparation` | `NON_VEGAN` | quatre formes de VII V.II.5 |

La correspondance **sémantique** à un ID existant ne signifie pas que le terme est reconnu. Kalfsvlees, Kalbfleisch, schapenvlees, sheepmeat, goatmeat, les coupes et la plupart des noms nus de volailles sont `NONE`. Leur statut propre dans l'application est **non disponible**, aucun concept nouveau n'étant créé. Le CSV distingue les statuts des familles existantes, les IDs réellement trouvés et les propositions sans statut.

Sur les 299 lignes, la voie lexicale appliquée à la lecture donne **55 EXACT, 1 COVERED, 46 PARTIAL_CONTEXTUAL et 197 NONE**. Le matcher direct sur forme brute donne **54 EXACT, 1 COVERED, 47 PARTIAL_CONTEXTUAL et 197 NONE**. Ces nombres portent sur les lignes, pas sur des ingrédients uniques ou des verdicts.

Trois confusions de portée :

- `edible meat offal` trouve **meat**, avec reste edible offal, sans reconnaître edible_offal : l'insertion de meat empêche le lookup de edible offal.

- `Bereiding op basis van vers pluimveevlees` trouve **meat**, pas la préparation complète.

- `Graisses de porc` trouve **meat** par porc, pas animal_fat ; le pluriel n'est pas l'alias graisse de porc.

Un `PARTIAL_CONTEXTUAL` conserve un inconnu selon le collecteur. Une forme proche n'est pas reconnue. `Pig fat, free of lean meat` trouve aussi meat dans une **négation de chair maigre** : une ligne douanière descriptive ne doit pas être traitée aveuglément comme liste d'ingrédients. Aucun de ces comportements n'a été modifié.

## Viandes, espèces, morceaux, abats et produits

« Correspondance » désigne un rattachement sémantique à une famille déjà présente ; les tableaux montrent séparément le résultat effectif. Toute cible nouvelle est **absente et sans statut attribué**. Les alias restent à réviser dans les usages végétaux, aromatiques, titres et traces. Un nom d'espèce ne prouve pas la présence de chair dans lait de chèvre ou oeufs d'oie.


### M01 — Viande générique

Nature : `MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Conserver meat pour la viande générique ; examiner seulement les formes manquantes, sans y ramener les nouveaux produits spécifiques.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viande` | 143 | EXACT → meat | métadonnées de langue seulement |
| FR | `Viandes` | 171 | NONE → aucun ID | forme absente, à revoir |
| NL | `Vlees` | 143 | EXACT → meat | métadonnées de langue seulement |
| EN | `Meat` | 143 | EXACT → meat | métadonnées de langue seulement |
| DE | `Fleisch` | 143 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XV/XVII ; annexe VII I.I. Détails par forme/langue : M0001 à M0005 du CSV.

Extrait primaire FR p.143 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> c  ⏎ Le secteur du tabac couvre les tabacs bruts ou non fabriqués et  les déchets de  ⏎ tabac relevant du code NC 2401.   ⏎ PARTIE XV   ⏎ Viande bovine   ⏎ Le secteur de la viande bovine couvre les produits énumérés dan s le tableau  ⏎ suivant:  ⏎ Code NC Description   ⏎ a) 0102 29 05 à  ⏎ 0102 29 99,  ⏎ 

### M02 — Bovin : viande et dénominations usuelles

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `beef_meat`, absent et sans statut attribué.

Un concept de viande bovine conserve l'espèce et porte les futurs noms complets. Plusieurs termes sont aujourd'hui alias de meat : aucune réaffectation proposée comme import immédiat.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Collision de propriété avec meat pour les alias déjà présents.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viande bovine` | 143 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| FR | `Viandes des animaux de l'espèce bovine` | 143 | NONE → aucun ID | forme absente, à revoir |
| NL | `Rundvlees` | 143 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| NL | `Vlees van runderen` | 143 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `beef` | 3 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| EN | `Meat of bovine animals` | 143 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Rindfleisch` | 143 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| DE | `Fleisch von Rindern` | 143 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XV, titre et NC 0201 ; art. 1(2)(o). Détails par forme/langue : M0006 à M0013 du CSV.

Extrait primaire FR p.143 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> c  ⏎ Le secteur du tabac couvre les tabacs bruts ou non fabriqués et  les déchets de  ⏎ tabac relevant du code NC 2401.   ⏎ PARTIE XV   ⏎ Viande bovine   ⏎ Le secteur de la viande bovine couvre les produits énumérés dan s le tableau  ⏎ suivant:  ⏎ Code NC Description   ⏎ a) 0102 29 05 à  ⏎ 0102 29 99,  ⏎ 0102 39

### M03 — Veau, catégorie V

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `veal_meat`, absent et sans statut attribué.

Produit vendu sous ces noms et destiné à l'alimentation humaine selon VII I.I. La distinction veau/bovin est utile ; ne pas inventer une traduction dans les tables de pays.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. veau/veal appartiennent déjà à meat ; kalfsvlees/Kalbfleisch sont des composés complets, sans matching par sous-mot.

**Alignement :** Même tableau V ; langue lexicale choisie selon le pays/nom, pas selon la seule langue du PDF.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `veau` | 172 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| FR | `viande de veau` | 172 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `Kalfsvlees` | 172 | NONE → aucun ID | forme absente, à revoir |
| EN | `Veal` | 172 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| DE | `Kalbfleisch` | 172 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII I.III.1.A, dénominations de vente V ; p.172 France/Pays-Bas/Irlande/Allemagne. Détails par forme/langue : M0014 à M0018 du CSV.

Extrait primaire FR p.172 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> lbfleisch   ⏎ Estonie Vasikaliha   ⏎ Irlande veal   ⏎ Grèce μοσχάρι γάλακτος   ⏎ Espagne ternera blanca, carne de ternera blanca   ⏎ France veau, viande de veau   ⏎ Croatie teletina   ⏎ Italie vitello, carne di vitello   ⏎ Chypre μοσχάρι γάλακτος   ⏎ Lettonie teļa gaļa   ⏎ Lituanie veršiena   ⏎ Luxembourg

### M04 — Jeune bovin, catégorie Z

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `beef_meat`, absent et sans statut attribué.

Sous-groupe réglementaire du bovin, sans créer un concept par catégorie de marché. Rattachement à beef_meat proposé à réviser avec métadonnées de sous-type Z ; rosé veal n'est pas un synonyme du veau V.

**Risques :** Risque de confusion V/Z et de propriété : rosé veal contient l'alias actuel veal ; préserver le nom source et l'âge documentaire, aucune fusion synonymique automatique.

**Alignement :** Même tableau Z ; les noms locaux divergent (jeune bovin / rosé veal), équivalence de catégorie seulement.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `jeune bovin` | 173 | NONE → aucun ID | forme absente, à revoir |
| FR | `viande de jeune bovin` | 173 | NONE → aucun ID | forme absente, à revoir |
| NL | `jongrundvlees` | 172 | NONE → aucun ID ; forme brute différente, voir CSV | forme absente, à revoir |
| NL | `rosé kalfsvlees` | 173 | NONE → aucun ID | forme absente, à revoir |
| EN | `rosé veal` | 173 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Jungrindfleisch` | 172 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII I.III.1.B, Z : Belgique/Allemagne p.172 ; France/Irlande/Pays-Bas p.173. Détails par forme/langue : M0019 à M0024 du CSV.

Extrait primaire FR p.173 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> 26 — 013.001 — 173   ⏎ Estonie noorloomaliha   ⏎ Irlande rosé veal   ⏎ Grèce νεαρό μοσχάρι   ⏎ Espagne Ternera, carne de ternera   ⏎ France jeune bovin, viande de jeune bovin   ⏎ Croatie mlada junetina   ⏎ Italie vitellone, carne di vitellone   ⏎ Chypre νεαρό μοσχάρι   ⏎ Lettonie jaunlopa gaļa   ⏎ Lituanie Jau

### M05 — Porc

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `pork_meat`, absent et sans statut attribué.

Une viande porcine spécifique couvre les noms d'espèce sans les assimiler au jambon, au sang ou au saindoux.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Alias sectoriels déjà détenus par meat.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viande de porc` | 145 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| FR | `Viandes des animaux de l'espèce porcine domestique` | 145 | NONE → aucun ID | forme absente, à revoir |
| NL | `Varkensvlees` | 145 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| NL | `Vlees van varkens` | 145 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `pigmeat` | 3 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| EN | `Meat of domestic swine` | 145 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Schweinefleisch` | 145 | EXACT → meat | déjà alias de meat ; propriété à préserver |
| DE | `Fleisch von Hausschweinen` | 145 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XVII, titre/NC ex0203 ; art.1(2)(q). Détails par forme/langue : M0025 à M0032 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>   ⏎ 02013R1308 — FR — 18.08.2026 — 013.001 — 145   ⏎ PARTIE XVII   ⏎ Viande de porc   ⏎ Le secteur de la viande de porc couvre les produits énumérés da ns le tableau  ⏎ suivant:  ⏎ Code NC Description   ⏎ a) ex 0103 Animaux vivants de l'espè

### M06 — Mélange documentaire ovins/caprins

Nature : `COLLECTIVE_SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Une ligne collective ne prouve pas un mélange d'ingrédients, ni une identité entre mouton et chèvre. Conserver sa reconnaissance historique ; ne pas attribuer le composé à une seule espèce.

**Risques :** Conjonction ou/ou-et, pluriels et ellipses : risque de fragment reconnu ou inconnu résiduel ; aucune reconstruction de Schaffleisch à partir de Schaf-.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viandes ovines et caprines` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `viandes ovine et caprine` | 145 | EXACT → meat | déjà reconnu ; aucun alias proposé |
| FR | `Viandes des animaux des espèces ovine et caprine` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Schapen- en geitenvlees` | 145 | EXACT → meat | déjà reconnu ; aucun alias proposé |
| NL | `Vlees van schapen of van geiten` | 145 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| EN | `Sheepmeat and goatmeat` | 145 | EXACT → meat | déjà reconnu ; aucun alias proposé |
| EN | `Meat of sheep or goats` | 145 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| DE | `Schaf- und Ziegenfleisch` | 145 | EXACT → meat | déjà reconnu ; aucun alias proposé |
| DE | `Fleisch von Schafen oder Ziegen` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XVIII, titre/NC 0204. Détails par forme/langue : M0033 à M0041 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> sons et similaires, de viandes et d'ab ats de toutes espèces, y  ⏎ compris les graisses de toute nature ou origine   ⏎ PARTIE XVIII   ⏎ Viandes ovines et caprines   ⏎ Le secteur des viandes ovine et caprine couvre les produits énu mérés dans le  ⏎ tableau suivant:   ⏎ Code NC Description   ⏎ a) 0104 10 30 Agneaux (jusq

### M07 — Viande ovine explicitement nommée

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `sheep_meat`, absent et sans statut attribué.

Noms complets utiles à reconnaître séparément ; pas de FR mouton, ni DE Schaffleisch attesté comme mot entier dans cette extraction.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. sheepmeat est actuellement proche du composé sheepmeat and goatmeat, sans stemming.

**Alignement :** Noms du secteur ovin : NL art.18(4)(c) p.13 et EN titre annexe I XVIII p.145 ; aucun alignement de nom individuel FR/DE.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| NL | `schapenvlees` | 13 | NONE → aucun ID | forme absente, à revoir |
| EN | `Sheepmeat` | 145 | NONE → aucun ID | forme absente, à revoir |

Aucune forme individuelle retenue pour FR, DE ; pas de traduction ajoutée.

Rubrique : Art.18(4)(c) p.13 NL ; annexe I XVIII EN. Détails par forme/langue : M0042 à M0043 du CSV.

Extrait primaire NL p.13 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ende bepalingen voor de betrokken producten, ond er  ⏎ meer inzake de bevleesdheids- en de vetbedekkingsklassen, en, i n de  ⏎ sector schapenvlees, aanvullende bepalingen inzake gewicht, vle es[U+00AD] ⏎ kleur en vetbedekking en de criteria voor de indeling van karka ssen  ⏎ van lichte lammeren;   ⏎ d) tot v

### M08 — Viande caprine explicitement nommée

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `goat_meat`, absent et sans statut attribué.

Mots complets réellement présents comme parties du titre collectif. Une viande caprine spécifique serait utile ; aucune traduction FR viande de chèvre n'est ajoutée.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Les composés collectifs existants ne reconnaissent pas automatiquement ces mots isolés.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| NL | `geitenvlees` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `goatmeat` | 145 | NONE → aucun ID | forme absente, à revoir |
| DE | `Ziegenfleisch` | 145 | NONE → aucun ID | forme absente, à revoir |

Aucune forme individuelle retenue pour FR ; pas de traduction ajoutée.

Rubrique : Annexe I XVIII, composés du titre. Détails par forme/langue : M0044 à M0046 du CSV.

Extrait primaire NL p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>  worst, vlees of slachtafvallen van alle so orten, met inbegrip van vet  ⏎ van alle soorten of oorsprong   ⏎ DEEL XVIII   ⏎ Schapen- en geitenvlees   ⏎ De in de onderstaande tabel opgenomen producten vallen onder de  sector  ⏎ schapen- en geitenvlees.   ⏎ GN-code Omschrijving   ⏎ a) 0104 10 30 Lammeren (

### M09 — Agneaux vivants

Nature : `LIVE_SPECIES`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Le texte atteste des animaux vivants jusqu'à un an, puis du classement de carcasses. Les alias agneau/lamb déjà dans meat ne sont pas une preuve source de viande d'agneau ici.

**Risques :** Ne pas inventer agneau/lamb singuliers, lamsvlees/Lammfleisch, ni créer un concept alimentaire à partir de la ligne d'animaux vivants.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Agneaux` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Lammeren` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `Lambs` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Lämmer` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XVIII NC 0104 10 30. Détails par forme/langue : M0047 à M0050 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ecteur des viandes ovine et caprine couvre les produits énu mérés dans le  ⏎ tableau suivant:   ⏎ Code NC Description   ⏎ a) 0104 10 30 Agneaux (jusqu'à l'âge d'un an)   ⏎ 0104 10 80 Animaux vivants de l'espèce ovine, autres que les re producteurs de race pure et les agneaux   ⏎ 0104 20 90 Animau

### M10 — Volaille générique

Nature : `MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Les états thermiques sont des variantes du même aliment, pas de nouveaux concepts. Ajouter un alias uniquement si sa forme n'est pas couverte ; reconnaître une base avec reste n'est pas une couverture complète.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viande de volaille` | 146 | EXACT → meat | déjà couvert |
| FR | `viande de volaille fraîche` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| FR | `viande de volaille congelée` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| FR | `viande de volaille surgelée` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `Pluimveevlees` | 146 | EXACT → meat | déjà couvert |
| NL | `Vers pluimveevlees` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `Bevroren pluimveevlees` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `Diepgevroren pluimveevlees` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `Poultrymeat` | 146 | EXACT → meat | déjà couvert |
| EN | `fresh poultrymeat` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `frozen poultrymeat` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `quick-frozen poultrymeat` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Geflügelfleisch` | 146 | EXACT → meat | déjà couvert |
| DE | `frisches Geflügelfleisch` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `gefrorenes Geflügelfleisch` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `tiefgefrorenes Geflügelfleisch` | 185 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |

Rubrique : Annexe I XX, titre ; annexe VII V.II.1-4 p.185. Détails par forme/langue : M0051 à M0066 du CSV.

Extrait primaire FR p.146 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ement conservés, même additi onnés de sucre ou d'autres  ⏎ édulcorants, autres qu'impropres à des usages alimentaires   ⏎ PARTIE XX   ⏎ Viande de volaille   ⏎ Le secteur de la viande de volaille couvre les produits énuméré s dans le tableau  ⏎ suivant:  ⏎ Code NC Description   ⏎ a) 0105 Coqs, poules, canards, o

### M11 — Coqs/poules — Gallus domesticus

Nature : `SPECIES_IN_FOOD_SCOPE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `chicken_meat`, absent et sans statut attribué.

La liste est explicitement alimentaire (pas seulement l'annexe I 0105 des animaux vivants). Concept de viande par espèce proposé ; noms nus conditionnels au contexte viande. Pas de chicken/poulet/Puten ni de singulier fabriqué. Gallus domesticus est une preuve taxonomique, pas un alias d'étiquette retenu.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Tous les pluriels, oeufs de poules/oies/canards et noms taxonomiques demandent une revue de portée avant import.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `coqs` | 185 | NONE → aucun ID | forme absente, à revoir |
| FR | `poules` | 185 | NONE → aucun ID | forme absente, à revoir |
| NL | `hanen` | 185 | NONE → aucun ID | forme absente, à revoir |
| NL | `kippen` | 185 | NONE → aucun ID | forme absente, à revoir |
| EN — taxon latin | `Gallus domesticus` | 185 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Hühner` | 185 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.I, liste des espèces de viandes/préparations/produits destinés à la commercialisation. Détails par forme/langue : M0067 à M0072 du CSV.

Extrait primaire FR p.185 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ase de viandes de volailles ou d'ab ats de volailles  ⏎ des espèces suivantes faisant l'objet d'une profession ou d'un commerce:  ⏎ — coqs et poules,   ⏎ — canards,   ⏎ — oies,   ⏎ — dindons et dindes,   ⏎ — pintades.   ⏎ Les présentes dispositions s'appliquent également à la viande d e volaille  ⏎ 

### M12 — Canards

Nature : `SPECIES_IN_FOOD_SCOPE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `duck_meat`, absent et sans statut attribué.

La liste est explicitement alimentaire (pas seulement l'annexe I 0105 des animaux vivants). Concept de viande par espèce proposé ; noms nus conditionnels au contexte viande. Pas de chicken/poulet/Puten ni de singulier fabriqué. Gallus domesticus est une preuve taxonomique, pas un alias d'étiquette retenu.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Tous les pluriels, oeufs de poules/oies/canards et noms taxonomiques demandent une revue de portée avant import.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `canards` | 185 | NONE → aucun ID | forme absente, à revoir |
| NL | `eenden` | 185 | NONE → aucun ID | forme absente, à revoir |
| EN | `ducks` | 185 | NONE → aucun ID | forme absente, à revoir |
| DE | `Enten` | 185 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.I, liste des espèces de viandes/préparations/produits destinés à la commercialisation. Détails par forme/langue : M0073 à M0076 du CSV.

Extrait primaire FR p.185 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> lailles ou d'ab ats de volailles  ⏎ des espèces suivantes faisant l'objet d'une profession ou d'un commerce:  ⏎ — coqs et poules,   ⏎ — canards,   ⏎ — oies,   ⏎ — dindons et dindes,   ⏎ — pintades.   ⏎ Les présentes dispositions s'appliquent également à la viande d e volaille  ⏎ saumurée couverte par l

### M13 — Oies

Nature : `SPECIES_IN_FOOD_SCOPE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `goose_meat`, absent et sans statut attribué.

La liste est explicitement alimentaire (pas seulement l'annexe I 0105 des animaux vivants). Concept de viande par espèce proposé ; noms nus conditionnels au contexte viande. Pas de chicken/poulet/Puten ni de singulier fabriqué. Gallus domesticus est une preuve taxonomique, pas un alias d'étiquette retenu.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Tous les pluriels, oeufs de poules/oies/canards et noms taxonomiques demandent une revue de portée avant import.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `oies` | 185 | NONE → aucun ID | forme absente, à revoir |
| FR | `oie` | 201 | NONE → aucun ID | forme absente, à revoir |
| NL | `ganzen` | 185 | NONE → aucun ID | forme absente, à revoir |
| NL | `gans` | 201 | NONE → aucun ID | forme absente, à revoir |
| EN | `geese` | 185 | NONE → aucun ID | forme absente, à revoir |
| EN | `goose` | 201 | NONE → aucun ID | forme absente, à revoir |
| DE | `Gänse` | 185 | NONE → aucun ID | forme absente, à revoir |
| DE | `Hafermastgans` | 201 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.I, liste des espèces alimentaires ; annexe IX, mention facultative oie à l’avoine p.201. Détails par forme/langue : M0077 à M0084 du CSV.

Extrait primaire FR p.185 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ab ats de volailles  ⏎ des espèces suivantes faisant l'objet d'une profession ou d'un commerce:  ⏎ — coqs et poules,   ⏎ — canards,   ⏎ — oies,   ⏎ — dindons et dindes,   ⏎ — pintades.   ⏎ Les présentes dispositions s'appliquent également à la viande d e volaille  ⏎ saumurée couverte par le code NC 

### M14 — Dindons/dindes

Nature : `SPECIES_IN_FOOD_SCOPE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `turkey_meat`, absent et sans statut attribué.

La liste est explicitement alimentaire (pas seulement l'annexe I 0105 des animaux vivants). Concept de viande par espèce proposé ; noms nus conditionnels au contexte viande. Pas de chicken/poulet/Puten ni de singulier fabriqué. Gallus domesticus est une preuve taxonomique, pas un alias d'étiquette retenu.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Tous les pluriels, oeufs de poules/oies/canards et noms taxonomiques demandent une revue de portée avant import.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `dindons` | 185 | NONE → aucun ID | forme absente, à revoir |
| FR | `dindes` | 185 | NONE → aucun ID | forme absente, à revoir |
| NL | `kalkoenen` | 185 | NONE → aucun ID | forme absente, à revoir |
| EN | `turkeys` | 185 | NONE → aucun ID | forme absente, à revoir |
| DE | `Truthühner` | 185 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.I, liste des espèces de viandes/préparations/produits destinés à la commercialisation. Détails par forme/langue : M0085 à M0089 du CSV.

Extrait primaire FR p.185 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> volailles  ⏎ des espèces suivantes faisant l'objet d'une profession ou d'un commerce:  ⏎ — coqs et poules,   ⏎ — canards,   ⏎ — oies,   ⏎ — dindons et dindes,   ⏎ — pintades.   ⏎ Les présentes dispositions s'appliquent également à la viande d e volaille  ⏎ saumurée couverte par le code NC 0210 99 39.  

### M15 — Pintades

Nature : `SPECIES_IN_FOOD_SCOPE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `guinea_fowl_meat`, absent et sans statut attribué.

La liste est explicitement alimentaire (pas seulement l'annexe I 0105 des animaux vivants). Concept de viande par espèce proposé ; noms nus conditionnels au contexte viande. Pas de chicken/poulet/Puten ni de singulier fabriqué. Gallus domesticus est une preuve taxonomique, pas un alias d'étiquette retenu.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict. Tous les pluriels, oeufs de poules/oies/canards et noms taxonomiques demandent une revue de portée avant import.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `pintades` | 185 | NONE → aucun ID | forme absente, à revoir |
| NL | `parelhoenders` | 185 | NONE → aucun ID | forme absente, à revoir |
| EN | `guinea fowls` | 185 | NONE → aucun ID | forme absente, à revoir |
| DE | `Perlhühner` | 185 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.I, liste des espèces de viandes/préparations/produits destinés à la commercialisation. Détails par forme/langue : M0090 à M0093 du CSV.

Extrait primaire FR p.185 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> suivantes faisant l'objet d'une profession ou d'un commerce:  ⏎ — coqs et poules,   ⏎ — canards,   ⏎ — oies,   ⏎ — dindons et dindes,   ⏎ — pintades.   ⏎ Les présentes dispositions s'appliquent également à la viande d e volaille  ⏎ saumurée couverte par le code NC 0210 99 39.   ⏎ II. Définitions   ⏎ 1) «v

### M16 — Viande de cheval

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `horse_meat`, absent et sans statut attribué.

Les lignes sont explicitement de viande, distinctes des chevaux vivants et des déchets de peaux. Concept équin utile sans dériver viande depuis le seul animal vivant.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viandes des animaux de l'espèce équine` | 156 | NONE → aucun ID | forme absente, à revoir |
| FR | `Viandes de cheval` | 156 | NONE → aucun ID | forme absente, à revoir |
| NL | `Vlees van paarden` | 156 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `Meat of horses` | 156 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `Horsemeat` | 156 | NONE → aucun ID | forme absente, à revoir |
| DE | `Fleisch von Pferden` | 156 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 2, NC ex0205/0210 99 10. Détails par forme/langue : M0094 à M0099 du CSV.

Extrait primaire FR p.156 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>  ⏎ nomenclature combinée.   ⏎ Section 2   ⏎ Code NC Description   ⏎ 0101 29 10 Chevaux vivants destinés à la boucherie (  α )  ⏎ ex 0205 00 Viandes des animaux de l'espèce équine, fraîches, ré frigérées ou congelées   ⏎ 0210 99 10 Viandes de cheval, salées ou en saumure ou bien séch ées  ⏎ 0511 99 10 Nerfs ou tendons; rognures et 

### M17 — Viandes asine et mulassière

Nature : `COLLECTIVE_SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `other_equid_meat`, absent et sans statut attribué.

Viandes explicitement désignées. Un groupe alimentaire pour équidés hors cheval suffit à ce stade ; aucun concept par variante âne/mulet/bardot et aucun alias d'animal nu.

**Risques :** Catégorie collective avec plusieurs espèces ; faible probabilité d'un tel libellé long sur étiquette. Priorité basse, ne pas en faire une traduction singulière inventée.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Viandes des animaux des espèces asine ou mulassière` | 148 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Vlees van ezels, van muildieren of van muilezels` | 148 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| EN | `Meat of asses, mules or hinnies` | 148 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| DE | `Fleisch von Eseln, Maultieren oder Mauleseln` | 148 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XXIV section 1, NC ex0205 00. Détails par forme/langue : M0100 à M0103 du CSV.

Extrait primaire FR p.148 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> de l'espèce porcine domestique   ⏎ ex 0203 29 – – autres:   ⏎ 0203 29 90 – – – autres que de l'espèce porcine domestique   ⏎ ex 0205 00 Viandes des animaux des espèces asine ou mulassière,  fraîches, réfrigérées ou congelées   ⏎ ▼B

### M18 — Gibier

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `game_meat`, absent et sans statut attribué.

Groupe alimentaire réel par origine sauvage, sans inventer cerf, sanglier ou lièvre ; la sous-position ne précise pas ces espèces.

**Risques :** wild/Wild sont aussi des adjectifs et game un mot non alimentaire : aucun alias nu global sans validation de contexte. Une viande de gibier n'est pas une espèce unique.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `gibier` | 154 | NONE → aucun ID | forme absente, à revoir |
| NL | `wild` | 154 | NONE → aucun ID | forme absente, à revoir |
| EN | `game` | 154 | NONE → aucun ID | forme absente, à revoir |
| DE | `Wild` | 154 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 1, NC 1602 90 31 sous préparations/conserves de viandes/abats. Détails par forme/langue : M0104 à M0107 du CSV.

Extrait primaire FR p.154 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ompris les préparations de sang de tous  animaux   ⏎ – – autres que des préparations de sang de tous animaux:   ⏎ 1602 90 31 – – – de gibier ou de lapin   ⏎ – – – autres:   ⏎ – – – – Autres que celles contenant de la viande ou des abats d e l'espèce porcine  ⏎ domestique:  ⏎ – – – – – autres que c

### M19 — Lapin

Nature : `SPECIES_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `rabbit_meat`, absent et sans statut attribué.

Nom sous une préparation de viande ou d'abats, utile sur étiquette. Distinct du groupe générique gibier ; ne pas déduire le lièvre.

**Risques :** Nom d'animal ou de viande dans une alternative végétale, un arôme, un titre ou une mention d'œufs/lait : exiger le contexte d'ingrédient réel ; aucune nouvelle règle de verdict.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `lapin` | 154 | NONE → aucun ID | forme absente, à revoir |
| NL | `konijn` | 154 | NONE → aucun ID | forme absente, à revoir |
| EN | `rabbit` | 154 | NONE → aucun ID | forme absente, à revoir |
| DE | `Kaninchen` | 154 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 1, NC 1602 90 31 sous préparations/conserves de viandes/abats. Détails par forme/langue : M0108 à M0111 du CSV.

Extrait primaire FR p.154 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> éparations de sang de tous  animaux   ⏎ – – autres que des préparations de sang de tous animaux:   ⏎ 1602 90 31 – – – de gibier ou de lapin   ⏎ – – – autres:   ⏎ – – – – Autres que celles contenant de la viande ou des abats d e l'espèce porcine  ⏎ domestique:  ⏎ – – – – – autres que celles conten

### M20 — Onglet / pilier du diaphragme

Nature : `MEAT_CUT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `beef_hanger`, absent et sans statut attribué.

Morceau comestible nommé et séparé de la seconde coupe ; le code douanier d'abats ne change pas sa nature musculaire. Correspondance intra-liste par ordre proposée, non un dictionnaire de synonymes certifié.

**Risques :** Nierenzapfen n'est pas Nieren (rognons) ; éviter de classer le morceau comme organe ou d'utiliser skirt hors du composé alimentaire.

**Alignement :** Même NC et ordre des deux morceaux ; alignement sémantique des sous-termes à réviser.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Onglets` | 143 | NONE → aucun ID | forme absente, à revoir |
| NL | `Longhaasjes` | 143 | NONE → aucun ID | forme absente, à revoir |
| EN | `Thick skirt` | 143 | NONE → aucun ID | forme absente, à revoir |
| DE | `Zwerchfellpfeiler` | 143 | NONE → aucun ID | forme absente, à revoir |
| DE | `Nierenzapfen` | 143 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XV NC 0206 10 95 / 0206 29 91 / 0210 99 51, premier élément de la liste. Détails par forme/langue : M0112 à M0116 du CSV.

Extrait primaire FR p.143 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> des des animaux de l'espèce bovine, fraîches ou réfrig érées  ⏎ 0202 Viandes des animaux de l'espèce bovine, congelées   ⏎ 0206 10 95 Onglets et hampes, frais ou réfrigérés   ⏎ 0206 29 91 Onglets et hampes, congelés   ⏎ 0210 20 Viandes des animaux de l'espèce bovine, salées ou en sa umure, séch

### M21 — Hampe / partie fine du diaphragme

Nature : `MEAT_CUT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `beef_skirt`, absent et sans statut attribué.

Seconde coupe musculaire nommée ; concept séparé d'onglet justifié par la partie anatomique, pas par une flexion.

**Risques :** omlopen est polysémique en NL ; hampe hors contexte alimentaire et skirt nu ne sont pas des alias sûrs.

**Alignement :** Même NC et ordre des deux morceaux ; alignement sémantique des sous-termes à réviser.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `hampes` | 143 | NONE → aucun ID | forme absente, à revoir |
| NL | `omlopen` | 143 | NONE → aucun ID | forme absente, à revoir |
| EN | `thin skirt` | 143 | NONE → aucun ID | forme absente, à revoir |
| DE | `Saumfleisch` | 143 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XV mêmes NC, second élément de la liste. Détails par forme/langue : M0117 à M0120 du CSV.

Extrait primaire FR p.143 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> maux de l'espèce bovine, fraîches ou réfrig érées  ⏎ 0202 Viandes des animaux de l'espèce bovine, congelées   ⏎ 0206 10 95 Onglets et hampes, frais ou réfrigérés   ⏎ 0206 29 91 Onglets et hampes, congelés   ⏎ 0210 20 Viandes des animaux de l'espèce bovine, salées ou en sa umure, séchées ou fum

### M22 — Jambons : morceau et produit conservé

Nature : `CUT_OR_PROCESSED_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `ham`, absent et sans statut attribué.

Même nom pour la pièce crue puis salée/conservée. Un groupe ham suffit : le règlement ne prouve ni jambon cuit, ni jambon sec, ni une recette. La viande porcine dépend ici de la rubrique parent.

**Risques :** Les lignes excluent le porc domestique : conserver cette restriction documentaire, ne pas faire de la NC un ingrédient. Alternatives végétales et polysémie de ham à traiter avant alias globaux.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Jambons` | 148 | NONE → aucun ID | forme absente, à revoir |
| FR | `Jambons et morceaux de jambons` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Hammen` | 148 | NONE → aucun ID | forme absente, à revoir |
| NL | `Hammen en delen daarvan` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `Hams` | 148 | NONE → aucun ID | forme absente, à revoir |
| EN | `Hams and cuts thereof` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schinken` | 148 | NONE → aucun ID | forme absente, à revoir |
| DE | `Schinken und Teile davon` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XXIV section 1 : ex0203 12 p.148 ; ex0210 11 p.149 ; ex1602 41 p.154. Détails par forme/langue : M0121 à M0128 du CSV.

Extrait primaire FR p.148 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> s:   ⏎ ex 0203 11 – – En carcasses ou demi-carcasses:   ⏎ 0203 11 90 – – – autres que de l'espèce porcine domestique   ⏎ ex 0203 12 – – Jambons, épaules et leurs morceaux, non désossés :  ⏎ 0203 12 90 – – – autres que de l'espèce porcine domestique   ⏎ ex 0203 19 – – autres:   ⏎ 0203 19 90 – – – au

### M23 — Épaules

Nature : `MEAT_CUT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Nom de pièce attesté dans la viande porcine, mais pas d'alias nu approuvé ni de nouvel aliment distinct nécessaire à cette passe.

**Risques :** Partie anatomique de nombreuses espèces et usages non alimentaires ; une forme qualifiée d'espèce serait préférable, sans la fabriquer.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `épaules` | 148 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `Épaules et leurs morceaux` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `schouders` | 148 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Schouders en delen daarvan` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `shoulders` | 148 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `Shoulders and cuts thereof` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schultern` | 148 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schultern und Teile davon` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XXIV ex0203 12 p.148 et ex1602 42 p.154. Détails par forme/langue : M0129 à M0136 du CSV.

Extrait primaire FR p.148 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> 203 11 – – En carcasses ou demi-carcasses:   ⏎ 0203 11 90 – – – autres que de l'espèce porcine domestique   ⏎ ex 0203 12 – – Jambons, épaules et leurs morceaux, non désossés :  ⏎ 0203 12 90 – – – autres que de l'espèce porcine domestique   ⏎ ex 0203 19 – – autres:   ⏎ 0203 19 90 – – – autres que 

### M24 — Poitrine de porc entrelardée

Nature : `MEAT_CUT_OR_SALTED_MEAT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `pork_belly`, absent et sans statut attribué.

Produit/morceau de viande et gras, à distinguer du saindoux. Le texte ne donne pas bacon ; aucun alias bacon ajouté. Les formes entre parenthèses restent explicitement liées à la poitrine.

**Risques :** Poitrine/belly nus sont anatomiques ; le parent porcin est indispensable. Le resolver coupe aux parenthèses : vérifier la différence entre brut et voie lexicale.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Poitrines (entrelardées)` | 149 | NONE → aucun ID | forme absente, à revoir |
| NL | `Buiken (buikspek)` | 149 | NONE → aucun ID | forme absente, à revoir |
| NL | `buikspek` | 149 | NONE → aucun ID | forme absente, à revoir |
| EN | `Bellies (streaky)` | 149 | NONE → aucun ID | forme absente, à revoir |
| DE | `Bäuche (Bauchspeck)` | 149 | NONE → aucun ID | forme absente, à revoir |
| DE | `Bauchspeck` | 149 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 1 ex0210 12, viande porcine salée/saumurée/séchée/fumée, hors porc domestique. Détails par forme/langue : M0137 à M0142 du CSV.

Extrait primaire FR p.149 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> – Jambons, épaules et leurs morceaux, non désossés :  ⏎ 0210 11 90 – – – autres que de l'espèce porcine domestique   ⏎ ex 0210 12 – – Poitrines (entrelardées) et leurs morceaux:   ⏎ 0210 12 90 – – – autres que de l'espèce porcine domestique   ⏎ ▼B

### M25 — Saucisses et saucissons de viande/abats/sang

Nature : `PROCESSED_MEAT`. Correspondance : `meat` (`NON_VEGAN`), `edible_offal` (`NON_VEGAN`). Proposition : `meat_sausage`, absent et sans statut attribué.

Produit transformé explicitement de viande, abats ou sang. Un groupe de charcuterie suffit ici ; pas de concept par pluriel ou variante de recette.

**Risques :** Les noms peuvent désigner des saucisses végétales sur étiquette ; la clause de composition source n'est pas présente dans un alias nu. Ne pas importer automatiquement avec statut animal hors contexte.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Saucisses` | 145 | NONE → aucun ID | forme absente, à revoir |
| FR | `saucissons` | 145 | NONE → aucun ID | forme absente, à revoir |
| NL | `Worst` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `Sausages` | 145 | NONE → aucun ID | forme absente, à revoir |
| DE | `Würste` | 145 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XVII NC 1601 00. Détails par forme/langue : M0143 à M0147 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> mest ique, salés ou en saumure, séchés ou  ⏎ fumés  ⏎ 1501 10  ⏎ 1501 20  ⏎ Graisses de porc (y compris le saindoux), autres   ⏎ c) 1601 00 Saucisses, saucissons et produits similaires, de via nde, d'abats ou de sang; préparations  ⏎ alimentaires à base de ces produits   ⏎ 1602 10 00 Préparations homog

### M26 — Sang animal alimentaire

Nature : `ANIMAL_TISSUE`. Correspondance : `meat` (`NON_VEGAN`), `edible_offal` (`NON_VEGAN`). Proposition : `animal_blood`, absent et sans statut attribué.

Tissu et matière alimentaire explicitement distingués de viande et abats dans les préparations ; ne pas fusionner sous meat ni inventer un boudin absent du texte.

**Risques :** blood orange/nom de couleur ou mention médicale ne prouve pas la présence de sang ; plusieurs rapprochements orthographiques possibles n'ont aucune valeur de reconnaissance.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `sang` | 145 | NONE → aucun ID | forme absente, à revoir |
| NL | `bloed` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `blood` | 145 | NONE → aucun ID | forme absente, à revoir |
| DE | `Blut` | 145 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XVII NC 1601/1602 10/1602 90 10, denrées à base de sang. Détails par forme/langue : M0148 à M0151 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> sses de porc (y compris le saindoux), autres   ⏎ c) 1601 00 Saucisses, saucissons et produits similaires, de via nde, d'abats ou de sang; préparations  ⏎ alimentaires à base de ces produits   ⏎ 1602 10 00 Préparations homogénéisées de viandes, d'abats ou de  sang   ⏎ 1602 20 90 Préparations 

### M27 — Abats, noms génériques et porcins

Nature : `EDIBLE_OFFAL`. Correspondance : `edible_offal` (`NON_VEGAN`). Proposition : aucun concept distinct.

Réutiliser edible_offal ; les organes alimentaires ne justifient pas un concept par espèce. Alias courts à revoir séparément des intitulés pharmaceutiques.

**Risques :** abats/offal sans comestible peuvent inclure des sous-produits non alimentaires ; une occurrence dans un nom plus long ne fait pas automatiquement un synonyme.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Abats comestibles` | 145 | EXACT → edible_offal | déjà couvert |
| FR | `Abats` | 145 | NONE → aucun ID | forme absente, à revoir |
| NL | `Eetbare slachtafvallen` | 145 | EXACT → edible_offal | déjà couvert |
| NL | `slachtafvallen` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `Edible offal` | 143 | EXACT → edible_offal | déjà couvert |
| EN | `offal` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `edible meat offal` | 145 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Genießbare Schlachtnebenerzeugnisse` | 145 | EXACT → edible_offal | déjà couvert |
| DE | `Schlachtnebenerzeugnisse` | 145 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XV p.143, XVII p.145 ; rubrique alimentaire hors fabrication pharmaceutique. Détails par forme/langue : M0152 à M0160 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> cteurs de race pure   ⏎ b) ex 0203 Viandes des animaux de l'espèce porcine domestique, fraîches, réfrigérées ou congelées   ⏎ ex 0206 Abats comestibles de l'espèce porcine domestique, autre s que pour la fabrication des produits  ⏎ pharmaceutiques, frais, réfrigérés ou congelés   ⏎ 0209 10 Lard sans part

### M28 — Foies et foies de volaille

Nature : `EDIBLE_OFFAL`. Correspondance : `edible_offal` (`NON_VEGAN`). Proposition : aucun concept distinct.

Foies alimentaires couverts sémantiquement par edible_offal ; ajouter les formes manquantes seulement. La recette foie gras n'est pas attestée et n'est pas proposée.

**Risques :** liver/Lebern polysémie possible ; ne pas confondre foie, huile de foie ou préparation à arôme. Aucun statut NON_VEGAN nouveau n'est écrit.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `foies` | 145 | NONE → aucun ID | forme absente, à revoir |
| FR | `Foies de volailles` | 146 | EXACT → edible_offal | déjà couvert |
| FR | `Foies d'oie ou de canards` | 147 | NONE → aucun ID | forme absente, à revoir |
| NL | `levers` | 145 | NONE → aucun ID | forme absente, à revoir |
| NL | `Levers van pluimvee` | 146 | EXACT → edible_offal | déjà couvert |
| NL | `Levers van ganzen of van eenden` | 147 | NONE → aucun ID ; forme brute différente, voir CSV | forme absente, à revoir |
| EN | `liver` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `Poultry livers` | 146 | EXACT → edible_offal | déjà couvert |
| EN | `Goose or duck livers` | 147 | NONE → aucun ID | forme absente, à revoir |
| DE | `Lebern` | 145 | NONE → aucun ID ; forme brute différente, voir CSV | forme absente, à revoir |
| DE | `Geflügellebern` | 146 | EXACT → edible_offal | déjà couvert |
| DE | `Gänse- oder Entenlebern` | 147 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XVII NC1602 20 90 p.145 ; XX NC0207/0210 p.146 et NC1602 20 10 p.147. Détails par forme/langue : M0161 à M0172 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> de ces produits   ⏎ 1602 10 00 Préparations homogénéisées de viandes, d'abats ou de  sang   ⏎ 1602 20 90 Préparations et conserves de foies de tous animaux a utres que d'oie ou de canard   ⏎ 1602 41 10  ⏎ 1602 42 10  ⏎ 1602 49 11 à  ⏎ 1602 49 50  ⏎ Autres préparations et conserves contenant de la v

### M29 — Rognons, langue et fressure : présentation des carcasses

Nature : `ANATOMY_CONTEXT`. Correspondance : `edible_offal` (`NON_VEGAN`). Proposition : aucun concept distinct.

La source atteste les organes, retirés ou gardés avec les carcasses ; elle ne suffit pas à approuver leurs noms nus comme ingrédients. Les foies alimentaires de M28 fournissent une preuve plus forte.

**Risques :** langue/tongue/tong/Zunge et hartslag sont polysémiques ; abats potentiels mais forme culinaire à sourcer avant alias.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `rognons` | 167 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `langue` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `fressure` | 169 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `nieren` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `tong` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `hartslag` | 169 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `kidneys` | 167 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `tongue` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `pluck` | 169 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Nieren` | 167 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Zunge` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Geschlinge` | 169 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe IV A.IV p.167 ; B.III p.168 ; C.IV p.169. Détails par forme/langue : M0173 à M0184 du CSV.

Extrait primaire FR p.167 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> métacarpiennes ou tarsométatarsiques;   ⏎ b) sans les organes contenus dans les cavités thoracique et abd ominale avec ou  ⏎ sans les rognons, la graisse de rognon, ainsi que la graisse de  bassin;   ⏎ c) sans les organes sexuels avec les muscles attenants, sans la  mamelle et la  ⏎ graisse mam

### M30 — Cuisse, dos, épaule : conformation

Nature : `ANATOMY_CONTEXT`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Vocabulaire anatomique dans un barème de marché, sans preuve de dénomination d'ingrédient. Aucun nouveau concept pour ces profils.

**Risques :** round/back/rug/dos trop génériques ; ne pas assimiler une conformation ou une classe S/E/U à un ingrédient.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `cuisse` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `dos` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `épaule` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `stomp` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `rug` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `schouder` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `round` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `back` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `shoulder` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Keule` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Rücken` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schulter` | 166 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe IV A.III.1, conformation de carcasses bovines. Détails par forme/langue : M0185 à M0196 du CSV.

Extrait primaire FR p.166 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> nformation, définie comme suit:   ⏎ Développement des profils de la carcasse, et notamment des part ies essen[U+00AD] ⏎ tielles de celle-ci (cuisse, dos, épaule)   ⏎ Classe de  ⏎ conformation Description  ⏎ S  ⏎ supérieure  ⏎ Tous les profils extrêmement convexes; développement  ⏎ musculaire exceptionnel ave

### M31 — Préparation de viande de volaille

Nature : `MEAT_PREPARATION`. Correspondance : `poultry_meat_preparation` (`NON_VEGAN`). Proposition : aucun concept distinct.

Concept déjà présent NON_VEGAN ; état frais et singulier/pluriel n'imposent pas un autre concept. Comparer l'alias complet et le résultat partiel du matcher.

**Risques :** Le produit transformé de II.7 est une autre définition ; ne pas le traiter comme un alias exact de la préparation II.5.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `préparation à base de viande de volaille` | 186 | EXACT → poultry_meat_preparation | déjà couvert |
| FR | `préparation à base de viande de volaille fraîche` | 186 | PARTIAL_CONTEXTUAL → poultry_meat_preparation | forme absente, à revoir |
| NL | `Bereiding op basis van pluimveevlees` | 186 | EXACT → poultry_meat_preparation | déjà couvert |
| NL | `Bereiding op basis van vers pluimveevlees` | 186 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `poultrymeat preparation` | 186 | EXACT → poultry_meat_preparation | déjà couvert |
| EN | `fresh poultrymeat preparation` | 186 | PARTIAL_CONTEXTUAL → poultry_meat_preparation | forme absente, à revoir |
| DE | `Geflügelfleischzubereitungen` | 186 | EXACT → poultry_meat_preparation | déjà couvert |
| DE | `frische Geflügelfleischzubereitung` | 186 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.II.5-6 p.186. Détails par forme/langue : M0197 à M0204 du CSV.

Extrait primaire FR p.186 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>   ⏎ 02013R1308 — FR — 18.08.2026 — 013.001 — 186   ⏎ 5) «préparation à base de viande de volaille»: viande de volail le, y compris  ⏎ la viande de volaille ayant été réduite en fragments, à laquell e ont été  ⏎ ajoutés des denrées alimentaires, des co

### M32 — Produit transformé à base de volaille

Nature : `PROCESSED_MEAT`. Correspondance : `meat` (`NON_VEGAN`), `poultry_meat_preparation` (`NON_VEGAN`). Proposition : `poultry_meat_product`, absent et sans statut attribué.

II.7 distingue explicitement le produit à base de viande de la préparation II.5 : concept distinct justifié, sans importer le contenu de 853/2004 non fourni.

**Risques :** FR/EN contiennent une viande générique reconnue ; NL/DE sont des mots composés. Une correspondance partielle n'est pas une identité avec la préparation.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `produit à base de viande de volaille` | 186 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `Pluimveevleesproduct` | 186 | NONE → aucun ID | forme absente, à revoir |
| EN | `poultrymeat product` | 186 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Geflügelfleischerzeugnis` | 186 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe VII V.II.7 p.186. Détails par forme/langue : M0205 à M0208 du CSV.

Extrait primaire FR p.186 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> page et la manipulation réalisés dans l'usine pendant la p roduction  ⏎ de préparations à base de viande de volaille fraîche;   ⏎ 7) «produit à base de viande de volaille»: produit à base de vi ande tel que  ⏎ défini à l'annexe I, point 7.1, du règlement (CE) n  o 853/2004, pour  ⏎ lequel a été utilisée de la viande de vol

### M33 — Extraits de viande — preuve collective

Nature : `MEAT_DERIVATIVE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `meat_extract`, absent et sans statut attribué.

Produit dérivé distinct de la chair. Le texte fournit un libellé collectif : concept proposé, mais aucun alias extrait/extract nu ni expression reconstruite extrait de viande n'est retenu.

**Risques :** Une liste de deux produits n'est pas le nom de chacun. Extrait de plante, arôme ou bouillon ne peut être rattaché ici sans preuve complémentaire.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Extraits et jus de viande` | 154 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| NL | `Extracten en sappen van vlees` | 154 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| EN | `Extracts and juices of meat` | 154 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| DE | `Extrakte und Säfte von Fleisch` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XXIV section 1 NC ex1603 00. Détails par forme/langue : M0209 à M0212 du CSV.

Extrait primaire FR p.154 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> s contenant de la viande ou des abats  de l'espèce bovine:   ⏎ 1602 90 99 – – – – – – autres que d'ovins ou de caprins   ⏎ ex 1603 00 Extraits et jus de viande   ⏎ 1801 00 00 Cacao en fèves et brisures de fèves, bruts ou torréf iés  ⏎ 1802 00 00 Coques, pellicules (pelures) et autres déchets de ca cao  ⏎ ▼B

### M34 — Jus de viande

Nature : `MEAT_DERIVATIVE`. Correspondance : `meat` (`NON_VEGAN`). Proposition : `meat_juice`, absent et sans statut attribué.

Sous-terme complet avec son complément viande réellement présent. Dérivé liquide à distinguer des extraits concentrés ; aucune invention jus de boeuf ou bouillon.

**Risques :** Les termes jus/juice seuls sont trop larges ; toute qualifier/arôme et viande alternative doivent être revus.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `jus de viande` | 154 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `sappen van vlees` | 154 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| EN | `juices of meat` | 154 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| DE | `Säfte von Fleisch` | 154 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 1 NC ex1603 00, second élément. Détails par forme/langue : M0213 à M0216 du CSV.

Extrait primaire FR p.154 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> de la viande ou des abats  de l'espèce bovine:   ⏎ 1602 90 99 – – – – – – autres que d'ovins ou de caprins   ⏎ ex 1603 00 Extraits et jus de viande   ⏎ 1801 00 00 Cacao en fèves et brisures de fèves, bruts ou torréf iés  ⏎ 1802 00 00 Coques, pellicules (pelures) et autres déchets de ca cao  ⏎ ▼B

### M35 — Farines/poudres comestibles de viande ou abats

Nature : `COLLECTIVE_MEAT_DERIVATIVE`. Correspondance : `meat` (`NON_VEGAN`), `edible_offal` (`NON_VEGAN`). Proposition : aucun concept distinct.

Catégorie alimentaire attestée, mais libellé collectif long et variantes de matière. Conserver en revue basse priorité avant de décider un nom d'ingrédient distinct.

**Risques :** Farine/meel/mehl et meals nus provoqueraient des collisions végétales ; risque élevé avec la segmentation NC du CSV large.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Farines et poudres, comestibles, de viande ou d'abats` | 143 | PARTIAL_CONTEXTUAL → meat ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| NL | `Eetbaar meel en eetbaar poeder van vlees of van slachtafvallen` | 143 | PARTIAL_CONTEXTUAL → meat ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| EN | `Edible flours and meals of meat or meat offal` | 143 | PARTIAL_CONTEXTUAL → meat | preuve seule ; aucun alias proposé |
| DE | `Genießbares Mehl von Fleisch oder Schlachtnebenerzeugnissen` | 143 | NONE → aucun ID ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XV NC0210 99 90 p.143 ; XXIV ex0210 p.149. Détails par forme/langue : M0217 à M0220 du CSV.

Extrait primaire FR p.143 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ovine, salées ou en sa umure, séchées ou fumées   ⏎ 0210 99 51 Onglets et hampes, salés ou en saumure, séchés ou fu més  ⏎ 0210 99 90 Farines et poudres, comestibles, de viande ou d'abat s  ⏎ 1602 50 10 Autres préparations et conserves de viande ou d'abat s de l'espèce bovine non cuits; mélanges  ⏎ de viande ou d'abats cuits et de viande ou

### M36 — Préparations et conserves multi-matières

Nature : `COLLECTIVE_PROCESSED_MEAT`. Correspondance : `meat` (`NON_VEGAN`), `edible_offal` (`NON_VEGAN`). Proposition : aucun concept distinct.

Une catégorie composite ne justifie pas un concept par ligne douanière ; les matières et produits spécifiques sont traités ailleurs.

**Risques :** Ne pas inférer une recette, ni faire d'une préparation de viandes/abats/sang un synonyme de la seule viande.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Préparations homogénéisées de viandes, d'abats ou de sang` | 145 | NONE → aucun ID ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| NL | `Gehomogeniseerde bereidingen van vlees, van slachtafvallen of van bloed` | 145 | PARTIAL_CONTEXTUAL → meat ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| EN | `Homogenised preparations of meat, meat offal or blood` | 145 | PARTIAL_CONTEXTUAL → meat ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| DE | `Homogenisierte Zubereitungen aus Fleisch, Schlachtnebenerzeugnissen oder Blut` | 145 | NONE → aucun ID ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XVII NC1602 10 00 ; XXIV ex1602 p.154. Détails par forme/langue : M0221 à M0224 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> saucissons et produits similaires, de via nde, d'abats ou de sang; préparations  ⏎ alimentaires à base de ces produits   ⏎ 1602 10 00 Préparations homogénéisées de viandes, d'abats ou de  sang   ⏎ 1602 20 90 Préparations et conserves de foies de tous animaux a utres que d'oie ou de canard   ⏎ 1602 41 10  ⏎ 1602 42 10  ⏎ 1602 49 11 à  ⏎ 1602 49 50  ⏎ Au

### M37 — Espèces de bétail : noms nus en contexte vivant/anatomique

Nature : `LIVE_SPECIES`. Correspondance : `meat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Attestation de l'espèce séparée de la désignation de viande. porc est déjà un alias de meat ; cela ne transforme pas ces mentions de bétail vivant ou carcasse en nouveaux ingrédients.

**Risques :** Les espèces nommées peuvent qualifier lait, laine, cuir, oeufs ou élevage ; aucun alias nu nouveau ni statut alimentaire inféré.

**Alignement :** Noms des mêmes familles animales dans les définitions et sous-positions ; pas une équivalence de dénomination d'ingrédient.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `bovins` | 162 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `porc` | 168 | EXACT → meat | déjà reconnu ; aucun alias proposé |
| FR | `ovins` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `caprins` | 146 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Runderen` | 162 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `varkens` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Schapen` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `geiten` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `Bovine animals` | 162 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `swine` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `sheep` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `goats` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Rinder` | 162 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schweinen` | 149 | NONE → aucun ID ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| DE | `Schafe` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Ziegen` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe II V p.162 bovins ; annexe I XVII/XVIII p.145-146 ; annexe IV B/C p.168 ; XXIV ex0210 p.149. Détails par forme/langue : M0225 à M0240 du CSV.

Extrait primaire FR p.162 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> R1308 — FR — 18.08.2026 — 013.001 — 162   ⏎ PARTIE V   ⏎ Définitions applicables au secteur de la viande bovine   ⏎ ▼C2  ⏎ On entend par «bovins», les animaux vivants de l'espèce bovine des espèces  ⏎ domestiques des sous-positions 0102 21, ex 0102 31 00, 0102 90 20,  ⏎ ex 0102 29 10 à ex 0102 29 

## Graisses animales — section séparée du lot commité

Les 20 formes animal_fat du lot animal restent intactes. **Saindoux, reuzel, lard EN et Schweineschmalz sont déjà EXACT**, ainsi que les 12 formes de graisses bovines/ovines-caprines/volailles revues. Aucun concept saindoux nouveau n'est nécessaire.

La NC1503 p.153 atteste **huile de suif / talkolie / tallow oil / Talgöl**, et **huile de saindoux / spekolie / lard oil / Schmalzöl**. Ne pas tronquer ces noms en alias d'un produit générique suif/tallow/Talg/talk. Les formes intégrales restent des pistes d'alias de animal_fat, avec confirmation d'usage sur étiquette avant import. Aucun concept autonome proposé pour ces fractions.

La seule distinction proposée ici est **pork_backfat**, gras solide non fondu sans chair maigre, à distinguer du saindoux fondu et de la poitrine entrelardée. Le FR **Lard** se normalise comme l'alias EN **lard** déjà propriétaire animal_fat : collision de sens et de propriété, aucun alias FR lard nouveau proposé. NL Spek et DE Schweinespeck sont conditionnels ; les longues descriptions NC demeurent des preuves sans clés d'alias proposées. Spek ne désigne pas toujours du gras sans chair.


### F01 — Graisses porcines

Nature : `ANIMAL_FAT`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Couverture historique conservée. Le pluriel FR source est distinct de graisse de porc ; contrôler son lookup exact avant toute proposition.

**Risques :** Source animale explicite ; les graisses génériques sans espèce ne sont pas autorisées à devenir NON_VEGAN.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Graisses de porc` | 145 | PARTIAL_CONTEXTUAL → meat | forme absente, à revoir |
| NL | `Varkensvet` | 145 | EXACT → animal_fat ; forme brute différente, voir CSV | déjà couvert |
| EN | `Pig fat` | 145 | EXACT → animal_fat | déjà couvert |
| DE | `Schweinefett` | 145 | EXACT → animal_fat | déjà couvert |

Rubrique : Annexe I XVII NC1501 10/1501 20 p.145. Détails par forme/langue : M0241 à M0244 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> s  ⏎ ex 0210 Viandes et abats comestibles de l'espèce porcine domest ique, salés ou en saumure, séchés ou  ⏎ fumés  ⏎ 1501 10  ⏎ 1501 20  ⏎ Graisses de porc (y compris le saindoux), autres   ⏎ c) 1601 00 Saucisses, saucissons et produits similaires, de via nde, d'abats ou de sang; préparations  ⏎ alimentaires

### F02 — Saindoux

Nature : `RENDERED_ANIMAL_FAT`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Produit précis déjà couvert et importé dans le lot commité ; aucune séparation supplémentaire n'est nécessaire pour la reconnaissance actuelle.

**Risques :** Collision interlingue avec le FR Lard (gras non fondu) de F03 : même forme normalisée lard, sens et produit différents.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `saindoux` | 145 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| NL | `reuzel` | 145 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| EN | `lard` | 145 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| DE | `Schweineschmalz` | 145 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |

Rubrique : Annexe I XVII NC1501 10/1501 20 p.145. Détails par forme/langue : M0245 à M0248 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> estibles de l'espèce porcine domest ique, salés ou en saumure, séchés ou  ⏎ fumés  ⏎ 1501 10  ⏎ 1501 20  ⏎ Graisses de porc (y compris le saindoux), autres   ⏎ c) 1601 00 Saucisses, saucissons et produits similaires, de via nde, d'abats ou de sang; préparations  ⏎ alimentaires à base de ces produits

### F03 — Lard gras non fondu / spek

Nature : `UNRENDERED_ANIMAL_FAT`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : `pork_backfat`, absent et sans statut attribué.

Produit adipeux solide alimentaire distinct du saindoux fondu et de la poitrine entrelardée ; concept séparé possible si l'usage en ingrédient est confirmé. Une seule distinction de produit, pas un concept par langue.

**Risques :** BLOQUANT pour un alias FR lard nu : animal_fat possède déjà EN lard, index global sans langue. Spek seul englobe parfois poitrine ; les formes qualifiées restent préférables.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Lard sans parties maigres` | 145 | PARTIAL_CONTEXTUAL → animal_fat | preuve seule ; aucun alias proposé |
| FR | `Lard` | 145 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| NL | `Spek (ander dan doorregen spek)` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Spek` | 145 | NONE → aucun ID | forme absente, à revoir |
| EN | `Pig fat, free of lean meat` | 145 | PARTIAL_CONTEXTUAL → animal_fat / meat | preuve seule ; aucun alias proposé |
| EN | `Pig fat` | 145 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| DE | `Schweinespeck ohne magere Teile` | 145 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schweinespeck` | 145 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XVII NC0209 10, gras sans partie maigre et non fondu. Détails par forme/langue : M0249 à M0256 du CSV.

Extrait primaire FR p.145 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> espèce porcine domestique, autre s que pour la fabrication des produits  ⏎ pharmaceutiques, frais, réfrigérés ou congelés   ⏎ 0209 10 Lard sans parties maigres et graisse de porc non fondue  ou extraite d'une autre manière,  ⏎ frais, réfrigérés, congelés, salés ou en saumure, séchés ou fum és  ⏎ ex 0210 Viandes 

### F04 — Graisses bovines, ovines/caprines, volailles

Nature : `SPECIES_ANIMAL_FAT`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Les noms sont déjà dans animal_fat après fc74f08. Aucun déplacement ni multiplication de concepts par espèce ou flexion.

**Risques :** La catégorie de gras bovin/ovin ne permet pas de fabriquer le nom suif/tallow/Talg/talk.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Graisses des animaux de l'espèce bovine` | 144 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| FR | `Graisse des animaux des espèces ovine et caprine` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| FR | `Graisses de volaille` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| NL | `Rundervet` | 144 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| NL | `Schapen- of geitenvet` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| NL | `Vet van gevogelte` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| EN | `Fats of bovine animals` | 144 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| EN | `Fats of sheep or goats` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| EN | `Poultry fat` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| DE | `Fett von Rindern` | 144 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| DE | `Fett von Schafen oder Ziegen` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |
| DE | `Geflügelfett` | 146 | EXACT → animal_fat | déjà reconnu ; aucun alias proposé |

Rubrique : Annexe I XV NCex1502 10 90 p.144 ; XVIII ex1502 90 90 p.146 ; XX 1501 90 00 p.146. Détails par forme/langue : M0257 à M0268 du CSV.

Extrait primaire FR p.144 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>  comestibles des animaux de l'espèce bovine, sa lés ou en saumure, séchés ou fumés,  ⏎ autres que onglets et hampes   ⏎ ex 1502 10 90 Graisses des animaux de l'espèce bovine, autres q ue celles de la position 1503   ⏎ 1602 50 31 et  ⏎ 1602 50 95  ⏎ Autres préparations et conserves de viande ou d'abats de l'espè ce bovine, autre

### F05 — Huile de suif

Nature : `ANIMAL_FAT_FRACTION`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Produit nommé précisément : rattachement à animal_fat suffisant pour la reconnaissance. Pas de concept tallow séparé ni de réduction silencieuse du nom au seul suif.

**Risques :** Nom technique de fraction ; NC1503 seule ne garantit pas l'usage comme ingrédient humain. tallow oil comporte un espace artificiel dans la couche EN.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `huile de suif` | 153 | NONE → aucun ID | forme absente, à revoir |
| NL | `talkolie` | 153 | NONE → aucun ID | forme absente, à revoir |
| EN | `tallow oil` | 153 | NONE → aucun ID ; forme brute différente, voir CSV | forme absente, à revoir |
| DE | `Talgöl` | 153 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 1 NC1503 00 p.153. Détails par forme/langue : M0269 à M0272 du CSV.

Extrait primaire FR p.153 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>  l'exclusion des graisses d'os et de déchets (  γ )  ⏎ 1503 00 Stéarine solaire, huile de saindoux, oléostéarine, oléo margarine et huile de suif, non émul[U+00AD] ⏎ sionnées, ni mélangées ni autrement préparées   ⏎ ex 1504 Graisses et huiles et leurs fractions, de poissons ou d e mammifères marins, même 

### F06 — Huile de saindoux

Nature : `ANIMAL_FAT_FRACTION`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Alias complet possible pour animal_fat ; pas de concept distinct sans utilité d'étiquette mieux établie. Une base saindoux/lard reconnue n'est pas la couverture de l'expression complète.

**Risques :** Spek/Schmalz ne sont pas automatiquement des synonymes du produit fractionné ; ne pas inclure des huiles végétales sous la même logique.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `huile de saindoux` | 153 | COVERED → animal_fat | forme absente, à revoir |
| NL | `spekolie` | 153 | NONE → aucun ID | forme absente, à revoir |
| EN | `lard oil` | 153 | PARTIAL_CONTEXTUAL → animal_fat | forme absente, à revoir |
| DE | `Schmalzöl` | 153 | NONE → aucun ID | forme absente, à revoir |

Rubrique : Annexe I XXIV section 1 NC1503 00 p.153. Détails par forme/langue : M0273 à M0276 du CSV.

Extrait primaire FR p.153 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> ication de produits pour l'alimentation  ⏎ humaine, à l'exclusion des graisses d'os et de déchets (  γ )  ⏎ 1503 00 Stéarine solaire, huile de saindoux, oléostéarine, oléo margarine et huile de suif, non émul[U+00AD] ⏎ sionnées, ni mélangées ni autrement préparées   ⏎ ex 1504 Graisses et huiles et leurs fractio

### F07 — Stéarines/oléomargarine : ligne technique

Nature : `TECHNICAL_FAT_FRACTION`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Termes techniques gardés comme preuve documentaire. Aucun alias ni concept alimentaire proposé sans étude d'usage et de composition ; Stéarine solaire est conservé sans correction.

**Risques :** NC et noms industriels ne prouvent pas un ingrédient usuel ; ne pas confondre oléomargarine avec toute margarine végétale.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Stéarine solaire` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `oléostéarine` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `oléomargarine` | 153 | NONE → aucun ID ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| NL | `Varkensstearine` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `oleostearine` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `oleomargarine` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `Lard stearin` | 153 | PARTIAL_CONTEXTUAL → animal_fat | preuve seule ; aucun alias proposé |
| EN | `oleostearin` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `oleo-oil` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Schmalzstearin` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Oleostearin` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Oleomargarin` | 153 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XXIV section 1 NC1503 00 p.153. Détails par forme/langue : M0277 à M0288 du CSV.

Extrait primaire FR p.153 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> autres que la fabrication de produits pour l'alimentation  ⏎ humaine, à l'exclusion des graisses d'os et de déchets (  γ )  ⏎ 1503 00 Stéarine solaire, huile de saindoux, oléostéarine, oléo margarine et huile de suif, non émul[U+00AD] ⏎ sionnées, ni mélangées ni autrement préparées   ⏎ ex 1504 Graisses et huil

### F08 — Graisses autour des organes / panne

Nature : `ANATOMICAL_FAT`. Correspondance : `animal_fat` (`NON_VEGAN`). Proposition : aucun concept distinct.

Présentation des carcasses : preuve anatomique uniquement. Une attestation culinaire supplémentaire serait nécessaire avant alias d'ingrédient.

**Risques :** panne a un sens courant non alimentaire ; Nierenfettgewebe est coupé par une césure PDF. Ne pas prendre tout dépôt de gras pour un produit d'étiquette.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `graisse de rognon` | 167 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| FR | `panne` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `niervet` | 167 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `kidney fat` | 167 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `flare fat` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Nierenfettgewebe` | 167 | NONE → aucun ID ; forme brute différente, voir CSV | preuve seule ; aucun alias proposé |
| DE | `Flomen` | 168 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe IV A.IV p.167 ; B.III p.168 ; C.IV p.169. Détails par forme/langue : M0289 à M0295 du CSV.

Extrait primaire FR p.167 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

> es ou tarsométatarsiques;   ⏎ b) sans les organes contenus dans les cavités thoracique et abd ominale avec ou  ⏎ sans les rognons, la graisse de rognon, ainsi que la graisse de  bassin;   ⏎ c) sans les organes sexuels avec les muscles attenants, sans la  mamelle et la  ⏎ graisse mammaire.   ⏎ V. Classement

### F09 — Graisses animales ou végétales : catégorie mixte

Nature : `MIXED_ORIGIN_CATEGORY`. Correspondance : aucun statut applicable à la catégorie entière. Proposition : aucun concept distinct.

Catégorie alternative/mixte ; aucun statut existant applicable à toute la ligne, aucun alias animal_fat proposé.

**Risques :** Ne pas transformer la possibilité animale ou la catégorie de marché en présence animale certaine.

**Alignement :** Rubriques/entrées réglementaires parallèles ; seules les formes explicitement listées sont retenues dans chaque langue.

| Langue | Forme lisible attestée | Page PDF | Reconnaissance actuelle (lecture) | Revue de l'alias |
|---|---|---:|---|---|
| FR | `Graisses et huiles animales ou végétales` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| NL | `Dierlijke en plantaardige vetten en oliën` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| EN | `Animal or vegetable fats and oils` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |
| DE | `Tierische und pflanzliche Fette und Öle` | 154 | NONE → aucun ID | preuve seule ; aucun alias proposé |

Rubrique : Annexe I XXIV section 1 NCex1516 p.154. Détails par forme/langue : M0296 à M0299 du CSV.

Extrait primaire FR p.154 (retours de ligne matérialisés ; les autres langues ont leur contexte exact dans le CSV) :

>  de jojoba de la sous-position  ⏎ 1515 90 11) et leurs fractions, fixes, même raffinées, mais non  chimiquement modifiées   ⏎ ex 1516 Graisses et huiles animales ou végétales et leurs fract ions, partiellement ou totalement  ⏎ hydrogénées, interestérifiées, réestérifiées ou élaïdinisées, m ême raffinées, mais non autre[U+00AD] ⏎ ment

## Absences et limites

Les recherches dans les 876 pages toléraient espaces internes et césures souples, avec limites de mots. Elles n'ont pas retrouvé ces noms entiers dans la couche texte :

- FR : bœuf/boeuf, mouton, chèvre, agneau au singulier, poulet, bacon, foie gras, viande hachée, sanglier, cerf, lièvre ;

- NL : lamsvlees, kippenvlees, bacon, talk/talg en mots autonomes ;

- EN : bacon, mutton, lamb au singulier, chicken, venison, hare, wild boar, meat extract en expression contiguë, minced meat ;

- DE : Bacon, Schaffleisch, Lammfleisch, Puten/Putenfleisch, Wildschwein, Pferdefleisch, Talg en mot autonome.

Ces absences sont limitées aux fichiers et à l'extraction. Les **pluriels** Agneaux/Lammeren/Lambs/Lämmer, les noms bovins/caprins, Gallus domesticus et des composés existent. Aucun terme absent n'est inventé pour compléter quatre langues. Des alias historiques agneau/lamb, poulet/chicken, bœuf/beef et pork sont déjà dans meat : leur présence en base ne prouve pas leur attestation dans ce règlement.

Carcasses/demi-carcasses, animaux vivants, classes, poids/âges, codes NC seuls et lignes industrielles ne sont pas proposés comme ingrédients. L'anatomie d'IV reste du contexte : épaules, rognons, langue, fressure et graisses de rognon demandent un usage culinaire attesté avant alias nus. Les coupes nommées dans les listes de viande et les produits alimentaires de VII V sont examinés séparément.

Les sous-positions hors espèce porcine domestique ne prouvent ni sanglier, ni jambon de porc domestique dans un produit particulier. Les farines/poudres et conserves collectives documentent des familles, pas une recette. Les fractions techniques NC1503 ne deviennent pas automatiquement des ingrédients courants. Une catégorie graisses animales **ou** végétales ne reçoit pas NON_VEGAN.

La découverte reste lexicale et ciblée, sans inspection visuelle exhaustive, second moteur indépendant, nomenclature externe complète ou corpus réel d'étiquettes. Des termes peuvent rester masqués par des césures dures, images, colonnes ou clauses non identifiées. Les 44 contrôles layout ne certifient pas les glyphes ni la géométrie. V/Z est gardé dans les preuves et relations ; une simple liste d'alias n'encode pas l'âge.

Les **24 pistes ne sont pas un lot à créer tel quel**. Noms nus d'espèces, alignements de coupes, catégories collectives et fractions de gras demandent encore une revue éditoriale et des preuves d'usage. Les protections du matcher n'ont pas été élargies aux alternatives végétales de viande : un futur lot doit examiner ces contextes.

## Validation réellement exécutée

Lecture : git status/rev-parse/log/diff, rg ciblé, consignes, rapports, CSV, JSON et code Kotlin. Snippets Python locaux pour `PdfReader(...).pages[n].extract_text()`, 44 extractions `extraction_mode='layout'`, recherches, comparaisons JSON et empreintes.

Compilation locale : `java -cp <jars locaux> org.jetbrains.kotlin.cli.jvm.K2JVMCompiler @compiler.args` avec `-no-stdlib -no-reflect -classpath <stdlib;annotations> -jvm-target 17 -d <JAR temporaire>`, les 24 sources inchangées et un programme de sonde. `ProbeKt` reçoit les trois chemins de connaissance et les 598 entrées langue/texte en base64 sur stdin. Compilation et sondes : **exit 0, 598 réponses structurées**. Aucun téléchargement, outil installé ou configuration modifiée.

Contrôles : formes exactes à leurs offsets, extraits présents, hashes/manifeste, IDs et statuts actuels, lookup canonique/linguistique/structuré, voisins textuels, propriétaires et collisions. Parité des champs utilisés pour reconnaissance/statut avec l'asset Android. eNumber absent/chaîne vide est une différence de représentation historique équivalente au chargement, pas une erreur corrigée.

**Aucun test Gradle, build APK ou test appareil exécuté** : périmètre documentaire. Les sondes du matcher ne valident ni parsing, détection de langue, traces, OCR, verdict end-to-end ou régression générale. Aucun importeur ni builder exécuté, même en dry-run.

Le CSV UTF-8 comporte un ID de revue unique par ligne. Il sépare :

- preuves primaires, langue PDF/langue lexicale, CELEX/date/hash, forme brute/lecture, page/rubrique/offset/contexte et liens aux revues historiques ;

- correspondance sémantique/statut existant, lookup exact et voisins textuels, propriétaires en collision ;

- matcher direct et voie lexicale sur brut/lecture, IDs et reste pour chaque passage ;

- décision, concept distinct sans statut, alias examiné, clé comptée, préparation/priorité, risques/justification, REVIEW_REQUIRED et NO.

Seules les clés `POTENTIAL_NEW_SURFACE` et `POTENTIAL_LANGUAGE_METADATA_ONLY` entrent dans les 143 alias. `suggested_alias` peut aussi conserver une forme déjà détenue par meat : consulter `alias_disposition` et `potential_alias_key` avant tout comptage. **Ce CSV ne doit pas être passé directement à un importeur.**


SHA-256 CSV : `c0a4fb87ef954306e5269853e05a5d3699b8919bb114c3fd15bd18fece43b70a` ; **62 colonnes**. Contrôle final exécuté avec succès : 299 lignes, 298 preuves distinctes, 62 colonnes, 299 IDs uniques, 299 formes/contextes revérifiés, clés et comptes recalculés, liens locaux valides, UTF-8 strict sans caractère de remplacement. Aucune collision de propriétaire entre les clés proposées ; les collisions avec les alias historiques restent explicites dans la revue. Les 383 fichiers de départ sont identiques octet pour octet. Git : aucun diff suivi/staged, HEAD fc74f08 inchangé, exactement les deux nouveaux livrables non suivis.


## Compte final

**4 concepts déjà présents ; 143 alias potentiels (140 formes absentes + 3 métadonnées de langue) ; 24 pistes de concepts distincts (23 viandes/produits + 1 gras solide). Zéro donnée importée.** Toutes les propositions restent à réviser, avec les limites de preuve et d'extraction indiquées.
