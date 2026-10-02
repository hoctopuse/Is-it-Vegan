# Audit de la chaîne knowledge et import avant 0.7

Date : 2026-09-30  
Mode : lecture seule. Aucun asset n'a été régénéré. Aucun commit, push, reset ou nettoyage destructif n'a été exécuté.

## 1. Sources de vérité

- Source éditoriale des concepts : `knowledge/ingredients.json`.
- Source éditoriale des alias et mappings : `knowledge/ingredient_aliases_multilingual.json`.
- Registre des sources : `knowledge/sources.json`.
- Assets Android inspectés : `app/src/main/assets/ingredients.json` et `app/src/main/assets/ingredient_aliases_multilingual.json`.
- Références réglementaires locales utilisées par les importeurs : `reference-input/`.
- L'arbre Git était propre avant l'audit, à l'exception du rapport précédent déjà non suivi `PRE_0_7_REPOSITORY_STATE_AUDIT.md`.

## 2. Chaîne knowledge → asset

La chaîne déclarée est :

1. édition dans `knowledge/` ;
2. génération des assets par `tools/build_ingredients.py` et du mapping documentaire par `tools/build_multilingual_ingredient_mapping.py` ;
3. consommation Android depuis `app/src/main/assets/`.

`knowledge/ingredients.json` et son asset contiennent chacun 479 concepts dans le même ordre et avec les mêmes identifiants. La comparaison binaire et structurelle brute n'est pas exacte : la source contient `sources` sous forme de liste, tandis que l'asset contient `source` sous forme de chaîne et ajoute `eNumber` vide. Après normalisation de ces champs générés, les 479 entrées sont équivalentes.

Les deux fichiers `ingredient_aliases_multilingual.json` sont identiques par comparaison JSON et par hash.

## 3. Importeurs

Importeurs réglementaires présents :

- `import_eu_additives_reference.py` ;
- `import_eu_honey_directive.py` ;
- `import_eu_preserved_milk_directive.py` ;
- `import_eu_fruit_juice_directive.py` ;
- `import_eu_jams_directive.py` ;
- `import_eu_agricultural_products_regulation.py` ;
- `import_eu_cocoa_chocolate_directive.py` ;
- `import_eu_flavourings_regulation.py`.

Les vérifications disponibles ont été exécutées séquentiellement. Les résultats sans changement sont :

| Importeur | Commande | Résultat |
|---|---|---|
| Additifs | exécution sans `--write`, mode dry-run implicite | `imported=0`, `already_present=338`, `aliases_added_to_existing=0` |
| Miel | `--dry-run` | `changes=0`, 53 alias joints |
| Lait conservé | `--dry-run` | `changes=0`, FR 23, NL 20, EN 23, DE 23 |
| Jus | `--check` | `changes=0`, 26 alias |
| Confitures | `--check` | `changes=0`, 4 concepts, 30 alias, 4 langues |
| Produits agricoles | `--check` | `changes=0`, 5 concepts, 44 alias |
| Cacao/chocolat | `--check` | `changes=0`, 7 concepts, 55 alias examinés |
| Arômes | `--check` | `changes=0`, 8 concepts, 40 mappings |

Trois importeurs n'acceptent pas l'option `--check` : additifs accepte seulement `--write` et le dry-run implicite ; miel et lait acceptent `--dry-run` ou `--write`. Leurs dry-runs ont passé sans changement. Il s'agit d'une différence d'interface de script, pas d'un défaut de données.

## 4. Générateurs

Générateurs présents et contrôlés :

- `tools/build_ingredients.py --check` : succès ; l'asset `ingredients.json` est courant.
- `tools/build_multilingual_ingredient_mapping.py --check` : succès ; le document de mapping est courant.
- `tools/build_origin_rules.py --check` : succès ; l'asset des règles d'origine est courant.
- `tools/build_knowledge_docs.py --check` : échec contrôlé avec `Knowledge documentation is stale; run: python tools/build_knowledge_docs.py`.

Le dernier échec concerne `docs/generated/base-connaissances-data.md`, qui ne correspond pas au rendu calculé à partir des données actuelles. Aucun rendu n'a été écrit pour préserver la lecture seule.

## 5. Parité

- Concepts : 479 dans `knowledge/`, 479 dans l'asset.
- Identifiants de concepts : uniques dans `knowledge/ingredients.json`, aucun identifiant manquant dans l'asset.
- Alias structurés : 1 668 groupes dans `knowledge/`, même contenu dans l'asset.
- Mappings structurés : 1 996 dans `knowledge/`, sans doublon exact ni doublon sémantique sur `(surfaceForm, language, conceptId)`.
- Les cibles `conceptId` des mappings existent toutes dans les concepts.
- Une cible `canonicalId` d'alias structuré, `cereals`, n'a pas de concept canonique correspondant. Le générateur documentaire le traite explicitement comme concept générique reconnu mais absent et non classifiable.
- Langues des mappings : FR 494, NL 520, EN 445, DE 470 ; des mappings supplémentaires existent en IT 34 et ES 33.
- Langues des groupes d'alias : FR 410, NL 420, EN 385, DE 399 ; des groupes supplémentaires existent en IT 27 et ES 27.
- Les quatre langues FR/NL/EN/DE sont donc conservées pour les mappings et les alias réglementaires vérifiés.

## 6. Idempotence

Tous les importeurs disposant de `--check` ou d'un mode dry-run ont indiqué zéro changement. Les générateurs d'assets et de mapping passent leurs contrôles d'actualité. L'import additifs en dry-run confirme 338 lignes déjà présentes et aucune nouvelle modification.

L'idempotence n'est pas directement exposée par une option `--check` pour les importeurs additifs, miel et lait ; leur dry-run est le contrôle disponible et n'a produit aucune modification.

## 7. Statistiques réelles

### Concepts et statuts

| Statut | Nombre |
|---|---:|
| `VEGAN` | 127 |
| `VEGETARIAN` | 12 |
| `NON_VEGAN` | 10 |
| `UNCERTAIN` | 330 |
| Total | 479 |

Aucun autre statut n'est présent.

### Alias et notes d'origine

- Alias portés directement par les concepts : 2 394 valeurs, dont 2 367 valeurs uniques.
- 18 concepts comportent au moins un alias local répété : `e422`, `e440`, `e100`, `e160b`, `e234`, `e235`, `e307`, `e320`, `e407`, `e420`, `e432`, `e433`, `e450`, `e452`, `e460`, `e466`, `e575` et `e938`.
- Notes `possibleOriginNote` : 24 concepts ; les 24 ont le statut `UNCERTAIN`.
- Tous les `sourceIds` des notes d'origine existent dans `knowledge/sources.json`.
- Aucun placeholder détecté parmi les concepts, alias, mappings ou sources (`TODO`, `TBD`, `placeholder`, `example.com`, etc.).

Les comptes annoncés dans les rapports récents suivent la progression réelle : 452 concepts après le lot lait/miel, 459 après les confitures, 464 après l'agriculture, 471 après cacao/chocolat et 479 après les arômes ; les mappings correspondants progressent de 1 834 à 1 864, 1 906, 1 956 puis 1 996.

## 8. Anomalies

- `build_knowledge_docs.py --check` signale un document généré obsolète. C'est une incohérence entre les données actuelles et un artefact documentaire généré, sans écriture effectuée pendant l'audit.
- Les 18 concepts listés en section 7 contiennent des alias directs répétés. Les mappings structurés ne reproduisent pas cette duplication.
- Le groupe d'alias structuré `cereals` n'a pas de concept canonique. Le comportement est documenté comme non classifiable ; il ne s'agit pas d'une perte d'identifiant de concept.
- `knowledge/sources.json` contient 20 sources ; deux identifiants (`les-additifs-alimentaires-vegetarien` et `open-food-facts-docs`) ne sont pas utilisés par les champs `sources` des concepts actuels, mais aucun identifiant référencé par un concept ou une note n'est absent.
- La comparaison brute des `ingredients.json` n'est pas une parité exacte de fichier, mais la différence correspond au schéma de sortie du générateur et disparaît après normalisation.

## 9. Concepts ou alias potentiellement perdus

La comparaison avec `HEAD^` ne montre aucune perte d'identifiant conceptuel : 471 concepts existaient dans le parent, les 8 concepts d'arômes suivants ont été ajoutés, et aucun identifiant historique n'a disparu :

`flavour_precursor`, `flavouring`, `flavouring_preparation`, `flavouring_substance`, `food_ingredient_with_flavouring_properties`, `other_flavouring`, `smoke_flavouring`, `thermal_process_flavouring`.

La comparaison des clés de mappings normalisées avec `HEAD^` ne montre aucune perte ; le total passe de 1 956 à 1 996. Les aliases FR/NL/EN/DE des lots réglementaires sont présents dans les structures correspondantes. Les répétitions locales signalées sont des doublons de valeur, pas des concepts ou mappings supprimés.

## 10. Verdict final

**READY_WITH_WARNINGS**

La chaîne de connaissance et d'import est idempotente pour les contrôles disponibles, les données applicatives sont cohérentes après normalisation du format généré, les statuts sont valides, les sources référencées existent, et aucun concept historique ni mapping normalisé n'a été perdu. Le document de connaissances généré est toutefois obsolète, et des doublons d'alias directs ainsi qu'un groupe générique sans concept canonique restent présents.

## 11. Corrections recommandées, sans les appliquer

- Régénérer `docs/generated/base-connaissances-data.md` avec `tools/build_knowledge_docs.py`, puis relancer son `--check`.
- Dédupliquer les alias directs des 18 concepts concernés, après confirmation que la déduplication ne change pas les règles de priorité du matcher.
- Décider explicitement si le groupe `cereals` doit rester un groupe non classifiable documenté ou recevoir un concept canonique.
- Ajouter une option `--check` homogène aux importeurs additifs, miel et lait, ou documenter formellement leur dry-run comme interface d'idempotence.
- Ajouter des contrôles automatisés couvrant la parité brute attendue du schéma généré et les cibles `canonicalId` absentes.
