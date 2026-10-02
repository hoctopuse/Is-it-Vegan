# Rapport de clôture documentaire 0.6.13.7

Date : 30 septembre 2026  
Périmètre : documentation, rapports générés et traçabilité des sources uniquement.

Cette passe n’a modifié ni la logique métier, ni les tests métier, ni les données éditoriales, ni les assets, ni les statuts, alias ou versions applicatives. Les rapports existants ont été conservés.

## Fichiers modifiés

Fichiers suivis modifiés :

- `README.md` ;
- `docs/index.md` ;
- `docs/additifs.md` ;
- `docs/generated/base-connaissances-data.md` ;
- `mkdocs.yml`.

Nouveaux documents ou manifests :

- `docs/knowledge-pipeline.md` ;
- `reference-input/eu-food-labelling/01-core-labelling-terms/SOURCE_MANIFEST.md` ;
- `reference-input/eu-food-labelling/02-sector-product-standards/SOURCE_MANIFEST.md` ;
- `reference-input/eu-food-labelling/04-fortified-foods-and-supplements/SOURCE_MANIFEST.md` ;
- `DOCUMENTATION_0_6_13_7_CLOSURE_REPORT.md`.

Les cinq rapports `PRE_0_7_*` déjà présents ont été laissés inchangés. Le statut Git actuel montre ces rapports comme fichiers non suivis issus des audits précédents, ainsi que les nouveaux documents de cette passe ; aucune modification métier suivie n’est présente.

## Corrections documentaires réalisées

### Documentation générée

`python tools/build_knowledge_docs.py` a été exécuté pour régénérer `docs/generated/base-connaissances-data.md`. Le contrôle `--check` réussit maintenant et confirme que le document est courant.

Le générateur a uniquement actualisé la documentation dérivée. Les fichiers `knowledge/ingredients.json`, `knowledge/ingredient_aliases_multilingual.json` et les assets Android n’ont pas été régénérés ou modifiés dans cette passe.

### Version documentaire

Les points d’entrée généraux indiquent maintenant `0.6.13.7` :

- `README.md` ;
- `docs/index.md` ;
- description de `mkdocs.yml`.

`docs/additifs.md` distingue désormais explicitement le lot historique 0.6.9.10 de l’état courant 0.6.13.7. Les références 0.6.9.x conservées dans `docs/verdict.md`, `docs/diagnostic.md`, `docs/tests.md` et le changelog décrivent des fonctionnalités ou rapports historiques ; elles n’ont pas été renommées artificiellement.

### Manifests des sources

Les trois dossiers auparavant dépourvus de manifeste disposent maintenant d’un inventaire :

- `01-core-labelling-terms` : quatre PDF d’arômes CELEX 02008R1334 et quatre copies FIC CELEX 02011R1169, avec consolidation, pages, hashes et rôle distinct ;
- `02-sector-product-standards` : 24 PDF couvrant cacao/chocolat, miel, jus, confitures, lait conservé et produits agricoles, avec importeur associé, pages et hashes ;
- `04-fortified-foods-and-supplements` : huit PDF couvrant CELEX 02002L0046 et 02006R1925, avec hashes, pages et état explicitement non importé.

Les manifestes expliquent que les quatre PDF FIC du dossier 01 sont des copies binaires de ceux du dossier 00. Le dossier 00 reste la source FIC canonique pour l’extraction TXT, les extraits thématiques et le manifeste détaillé ; le dossier 01 conserve le regroupement historique des termes fondamentaux.

### Pipeline centralisé

`docs/knowledge-pipeline.md` centralise :

- la chaîne `reference-input → importeur → knowledge → générateurs → assets` ;
- les générateurs et leurs options réellement disponibles ;
- les importeurs réglementaires, leurs sources et leurs options ;
- la différence entre `--check`, `--dry-run` et `--write` ;
- les sources fortifiées présentes mais non importées ;
- les commandes de validation documentaire.

La page ne présente pas les PDF fortifiés comme une couverture applicative. Elle précise aussi que `vitamin_d` et `vitamin_b12` déjà présents ne proviennent pas d’un import identifié de ce dossier.

## Sources toujours non intégrées

Les huit PDF de `reference-input/eu-food-labelling/04-fortified-foods-and-supplements/` restent documentaires uniquement :

- CELEX `02002L0046`, quatre langues, 14 pages par langue ;
- CELEX `02006R1925`, quatre langues, 20 pages par langue.

Aucun importeur correspondant n’existe dans `tools/`, aucun mapping ou statut n’a été ajouté à partir de ces PDF, et aucun asset ne les référence comme résultat d’import. Leur étude est reportée à une décision éditoriale ultérieure.

Les catégories FIC candidates, le collagène, les crustacés, les mollusques et plusieurs origines de vitamines ou nutriments restent également hors couverture applicative lorsqu’aucun importeur ou source de classification suffisant n’est identifié.

## Validations exécutées

Les commandes ont été exécutées séquentiellement depuis la racine du dépôt :

| Validation | Résultat |
|---|---|
| `python tools/build_knowledge_docs.py --check` | Succès : documentation des connaissances courante. |
| `python tools/build_multilingual_ingredient_mapping.py --check` | Succès : mapping documentaire courant. |
| `python tools/build_ingredients.py --check` | Succès : base valide et asset courant. |
| Vérification des liens Markdown locaux | Succès : aucun lien local manquant détecté. |
| Vérification des cibles `mkdocs.yml` | Succès : toutes les cibles de navigation existent, y compris `knowledge-pipeline.md`. |
| `git diff --check` | Succès ; seuls les avertissements standard LF/CRLF sont affichés. |
| `python -m mkdocs build --strict` | Non exécuté par MkDocs : le module Python `mkdocs` n’est pas installé dans l’environnement (`No module named mkdocs`). |

Aucune installation de dépendance n’a été tentée et aucun fichier de build n’a été produit par l’échec préalable de MkDocs.

## Avertissements restants

- La validation MkDocs stricte reste à exécuter dans un environnement disposant des dépendances documentaires du dépôt.
- Les importeurs additifs, miel et lait n’ont pas une option `--check` homogène ; leur interface documentée reste respectivement dry-run implicite ou `--dry-run`.
- Les PDF fortifiés sont manifestés mais non importés.
- Les doublons binaires FIC sont conservés volontairement et expliqués ; ils ne représentent pas deux sources réglementaires distinctes.
- Les rapports historiques conservent leurs numéros de version d’origine afin de préserver la traçabilité des lots.

## État final de la passe

La documentation générée est maintenant cohérente avec les sources courantes, les points d’entrée indiquent 0.6.13.7, les sources réglementaires possèdent une traçabilité accrue et les sources préparées mais non importées sont explicitement séparées de la couverture applicative. Aucun changement de code métier ou de données de classification n’a été effectué.

