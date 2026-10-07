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
| `tools/import_eu_food_hygiene_regulation.py` | CELEX `02004R0853`, complément `02011R1169` | `--dry-run`, `--write`, `--check` | Uniquement les quatre dénominations longues de viande séparée mécaniquement; check exige un lot complet et conforme. |
| `tools/import_eu_cocoa_chocolate_directive.py` | CELEX `02000L0036` | `--dry-run`, `--write`, `--check` | Cacao et chocolat ; les catégories commerciales variables restent prudentes. |
| `tools/import_eu_flavourings_regulation.py` | CELEX `02008R1334` | `--dry-run`, `--write`, `--check` | Catégories d’arômes `UNCERTAIN` ; l’arôme n’est pas l’ingrédient évoqué. |
| `tools/extract_fic_reference.py` | CELEX `02011R1169` | `--write`, `--check` | Extractions TXT et extraits thématiques documentaires ; aucun import applicatif. |

Les options ci-dessus reflètent le code présent. Les anciens rapports peuvent décrire une commande exécutée à une date antérieure ; en cas de différence, l’interface du script courant et son contrôle non écrivant font foi.

## Préservation historique et compatibilité du lot 853/2004

`tools/validate_knowledge_history.py --check` contrôle en lecture seule une référence
indépendante extraite du commit `d1f02275508c9472b1fe55ac82d792d1257f6264`, avant
le lot 853/2004 : `tools/testdata/knowledge_history_d1f0227.json`. Son empreinte
SHA-256 sémantique est fixée dans le validateur. Les signatures portent sur les
surfaces lexicales (texte, langue, propriétaire, alias ou variante OCR), chaque
objet de mapping complet, ses preuves imbriquées et les corrections OCR. Les IDs
des 487 concepts historiques doivent rester disponibles. Le seul alias de concept
absent admis est exactement `cereals` / NL / `granen`, sans variante OCR.

La garde accepte des ajouts sans actualiser la référence; elle ne remplace pas les
contrôles de synchronisation, de collision, de statut ou les tests métier. Elle ne
garantit pas tous les champs des ingrédients canoniques. Les tests agricoles
l'appellent également avec `--stdin`, sur données en mémoire, avant de compter les
mappings. Une suppression coordonnée d'alias/mapping ne doit pas devenir acceptable
par le simple recalcul de ce nombre.

Pour une modification historique intentionnelle, faire une revue explicite du diff,
identifier une nouvelle révision de référence, recalculer les signatures et modifier
ensemble la référence et son empreinte fixée. Documenter ici et dans le rapport de
migration les surfaces, propriétaires, métadonnées ou exceptions changés et leur
raison. Ne pas actualiser automatiquement la référence depuis le corpus courant
pour faire passer un test; un ajout seul n'exige pas cette opération.

Le contrôle 853/2004 vérifie aussi la version 1 et un sous-ensemble typé prudent du
contrat du chargeur Kotlin, les langues, doublons et corrections OCR. Une correction
dont l'entrée normalisée est l'une des quatre dénominations ne peut pas changer sa
surface normalisée, quelle que soit la langue sélectionnée. Les corrections
indépendantes et celles qui conservent cette clé restent admises. Ce choix protège
les quatre formes approuvées; il ne certifie pas une nouvelle redirection, même vers
une autre forme du même concept. Ces redirections exigent une décision explicite.

Le validateur Python refuse les structures mal typées au lieu d'imiter les
conversions permissives de Kotlin. Par exemple, il refuse `schemaVersion: 1.9`
même si le `toInt()` actuel du chargeur donnerait 1. Les tests ciblés
`FoodHygieneLexiconCompatibilityTest` vérifient le chargeur/résolveur réel, les
corrections sûres et dangereuses, et la parité de normalisation de toutes les
surfaces actuelles. Une réussite de `--check` reste un contrôle statique du contrat
inspecté, pas une exécution du runtime ni une preuve de sécurité sémantique de toute
étiquette. En cas de changement de contrat Kotlin ou d'écart de normalisation,
mettre à jour les gardes et ces tests avant de revendiquer la compatibilité.
Les caractères non assignés par la version Unicode du Python utilisé (`Cn`) et
les surrogates isolés (`Cs`) sont refusés avant normalisation : ne pas supposer que
leur traitement correspond déjà à celui de la JVM. Une telle entrée exige un
contrôle Kotlin explicite et une extension revue du profil pris en charge.

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
