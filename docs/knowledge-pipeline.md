# Pipeline de connaissances et importeurs

Cette page décrit le flux documentaire et les options réellement disponibles dans l’état `0.6.13.7`. Les sources éditoriales restent dans `knowledge/`. Les assets Android sont produits par les générateurs et ne doivent pas être édités directement.

## Ordre général

Le flux attendu est :

```text
reference-input/ → importeur réglementaire → knowledge/ → générateurs → app/src/main/assets/
```

Un importeur peut modifier les fichiers de `knowledge/` seulement avec son option `--write`. Les générateurs produisent les assets ou la documentation dérivée. Un contrôle `--check` ne doit pas écrire ; un `--dry-run` est le contrôle non écrivant des importeurs qui ne proposent pas `--check`.

## Générateurs

| Script | Options disponibles | Entrées et sorties |
|---|---|---|
| `tools/build_ingredients.py` | sans option : génération ; `--check` : contrôle | `knowledge/ingredients.json` et `knowledge/ingredient_aliases_multilingual.json` vers les deux assets Android. |
| `tools/build_multilingual_ingredient_mapping.py` | `--write`, `--check`, `--report` | Lexique multilingue vers `docs/generated/multilingual-ingredient-mapping.md`. |
| `tools/build_origin_rules.py` | sans option : génération ; `--check` : contrôle | Règles d’origine vers l’asset correspondant. |
| `tools/build_knowledge_docs.py` | sans option : génération ; `--check` : contrôle | Base éditoriale vers `docs/generated/base-connaissances-data.md`. |

Contrôle documentaire recommandé après une modification autorisée de `knowledge/` :

```powershell
python tools/build_ingredients.py --check
python tools/build_multilingual_ingredient_mapping.py --check
python tools/build_origin_rules.py --check
python tools/build_knowledge_docs.py --check
```

Les documents générés portent leur propre avertissement de ne pas être modifiés manuellement. Leur contenu doit être recalculé par le générateur.

## Importeurs réglementaires

| Script | Source principale | Options réellement disponibles | État du flux |
|---|---|---|---|
| `tools/import_eu_additives_reference.py` | CSV et PDF CELEX `02008R1333` | sans option : dry-run ; `--write` | 338 clés importables présentes ; les deux clés ambiguës E345/E345(i) restent exclues. |
| `tools/import_eu_honey_directive.py` | CELEX `02001L0110` | `--dry-run`, `--write` | Alias du concept `honey` ; aucun importeur `--check`. |
| `tools/import_eu_preserved_milk_directive.py` | CELEX `02001L0114` | `--dry-run`, `--write` | Alias de `milk` et `cream` ; aucun importeur `--check`. |
| `tools/import_eu_fruit_juice_directive.py` | CELEX `02001L0112` | `--dry-run`, `--write`, `--check` | Jus et purées vegan dans le périmètre réglementaire ; nectars incertains. |
| `tools/import_eu_jams_directive.py` | CELEX `02001L0113` | `--dry-run`, `--write`, `--check` | Confitures, gelées, marmelades et crème de marrons réglementaires. |
| `tools/import_eu_agricultural_products_regulation.py` | CELEX `02013R1308` | `--dry-run`, `--write`, `--check` | Produits agricoles ; pêche et aquaculture hors périmètre. |
| `tools/import_eu_cocoa_chocolate_directive.py` | CELEX `02000L0036` | `--dry-run`, `--write`, `--check` | Cacao et chocolat ; les catégories commerciales variables restent prudentes. |
| `tools/import_eu_flavourings_regulation.py` | CELEX `02008R1334` | `--dry-run`, `--write`, `--check` | Catégories d’arômes `UNCERTAIN` ; l’arôme n’est pas l’ingrédient évoqué. |
| `tools/extract_fic_reference.py` | CELEX `02011R1169` | `--write`, `--check` | Extractions TXT et extraits thématiques documentaires ; aucun import applicatif. |

Les options ci-dessus reflètent le code présent. Les anciens rapports peuvent décrire une commande exécutée à une date antérieure ; en cas de différence, l’interface du script courant et son contrôle non écrivant font foi.

## Sources préparées mais non intégrées

`reference-input/eu-food-labelling/04-fortified-foods-and-supplements/` contient les huit PDF des CELEX `02002L0046` et `02006R1925`, en FR/NL/EN/DE. Aucun importeur correspondant n’est présent dans `tools/`, et aucun de ces PDF n’est déclaré comme source d’un lot applicatif actuel. Ils sont donc documentés comme sources préparées mais non importées.

Les concepts `vitamin_d` et `vitamin_b12` déjà présents dans la base ne sont pas attribués à une importation de ce dossier. Leur statut `UNCERTAIN` reste inchangé. Aucune couverture des vitamines, minéraux ou compléments ne doit être déduite de la seule présence des PDF.

Les manifestes de `reference-input/eu-food-labelling/00-general-food-labelling/`, `01-core-labelling-terms/`, `02-sector-product-standards/`, `03-food-additives/` et `04-fortified-foods-and-supplements/` indiquent les fichiers, leurs hashes lorsqu’ils sont disponibles et leur rôle documentaire.

## Validation de la documentation

Depuis la racine du dépôt :

```powershell
python tools/build_knowledge_docs.py --check
python tools/build_multilingual_ingredient_mapping.py --check
python tools/build_ingredients.py --check
python -m mkdocs build --strict
git diff --check
```

Les liens Markdown locaux doivent pointer vers des fichiers suivis ou vers des URL explicitement externes. Un changement de source réglementaire doit conserver son CELEX, sa consolidation, sa langue, son chemin local et son rôle ; il ne doit pas être présenté comme un import applicatif sans rapport et test correspondants.
