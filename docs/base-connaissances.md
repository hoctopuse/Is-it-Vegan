# Base de connaissances

Cette page est le point de préparation des évolutions de données. Les tableaux générés reflètent les fichiers réellement chargés ou édités par le projet ; ils ne constituent pas une seconde base maintenue à la main.

## Rôle de chaque source

| Source | Responsabilité |
|---|---|
| `knowledge/ingredients.json` | Concepts canoniques, statuts, raisons et références éditoriales. |
| `app/src/main/assets/ingredients.json` | Copie générée et chargée hors ligne par Android. |
| `knowledge/ingredient_aliases_multilingual.json` | Alias par langue et variantes OCR ; aucun statut vegan. |
| `knowledge/origin_qualifier_rules.json` | Origine explicitement attachée à certains ingrédients et additifs. |
| Cette page | Vue documentaire, propositions et backlog. |

Un **concept** est l’identifiant canonique, par exemple `sunflower_oil`. Un **alias** est une surface reconnue dans une langue, par exemple `zonnebloemolie`. Un alias peut être reconnu alors que son concept est absent : le diagnostic le signale comme non classifiable et le verdict reste prudent.

Les `UNKNOWN` désignent un texte sans correspondance classifiable. Un concept absent est plus précis : le terme et l’ID proposé sont connus, mais aucun statut n’est disponible dans `ingredients.json`.

Les traces (`Peut contenir`, `Kan sporen bevatten`, `Kann Spuren enthalten`, etc.) forment une section distincte. Elles sont affichées comme information de contamination croisée et ne modifient jamais le verdict des ingrédients réellement déclarés.

Le dépôt contient deux représentations liées :

- `knowledge/ingredients.json` est la source éditoriale ;
- `app/src/main/assets/ingredients.json` est l’asset généré et chargé par Android.

Le script `tools/build_ingredients.py` valide la source et produit l’asset. Les deux fichiers ne sont donc pas identiques octet pour octet : le champ éditorial `sources` est converti en chaîne `source` pour le runtime et le JSON est réindenté.

## Schéma éditorial réel

Chaque entrée de `knowledge/ingredients.json` contient :

| Champ | Type | Rôle |
|---|---|---|
| `id` | chaîne | identifiant stable et unique |
| `name` | chaîne | nom canonique affichable |
| `eNumber` | chaîne optionnelle | numéro E, lorsqu’il existe |
| `aliases` | tableau non vide | surfaces reconnues, principalement FR/EN/NL |
| `status` | enum | `VEGAN`, `VEGETARIAN`, `NON_VEGAN` ou `UNCERTAIN` |
| `reason` | chaîne | justification affichée et diagnostique |
| `sources` | tableau non vide | identifiants référencés dans `knowledge/sources.json` |
| `possibleOriginNote` | objet optionnel, seulement `UNCERTAIN` | origines codées, condition de variabilité, sources et niveau de confiance ; rendu localisé au runtime |

L’asset embarqué conserve `id`, `name`, `eNumber`, `aliases`, `status`, `reason` et joint les identifiants de `sources` dans le champ chaîne `source`. Il conserve aussi `possibleOriginNote` lorsqu’elle existe. `AndroidIngredientKnowledgeLoader` lit les trois assets sans les transformer, puis le lecteur JSON JVM de `IngredientKnowledge.fromJson` construit les objets `Ingredient`, le lexique multilingue et les règles d’origine.

## Données générées

Les sections suivantes sont générées par `python tools/build_knowledge_docs.py` depuis la base éditoriale, le lexique multilingue et les règles d’origine.

--8<-- "generated/base-connaissances-data.md"

## Mapping multilingue verifiable

Le mapping generated source -> asset -> runtime est disponible dans [Mapping multilingue des ingredients](generated/multilingual-ingredient-mapping.md). Il liste les formes originales, la normalisation, le groupe de mapping, la relation, la source du concept et les alertes; le lexique JSON reste l unique source editoriale.

## Signification des statuts

| Statut | Sens dans le moteur |
|---|---|
| `VEGAN` | origine végétale, minérale, microbienne ou synthétique considérée compatible selon la donnée revue |
| `VEGETARIAN` | ingrédient animal sans classification non végétarienne, donc incompatible vegan |
| `NON_VEGAN` | chair, tissu, insecte ou ingrédient explicitement non végétarien selon la base |
| `UNCERTAIN` | origine, procédé ou formulation insuffisamment déterminés par l’étiquette |

Les noms peuvent être trompeurs : le champ `NON_VEGAN` représente dans le verdict détaillé une cause `NON_VEGETARIAN`, tandis que `VEGETARIAN` est lui aussi un bloqueur pour la compatibilité vegan. Consulter [le moteur de verdict](verdict.md).

## Sources

`knowledge/sources.json` décrit les identifiants cités : base des additifs de la Commission européenne, Vegan Society, Vegetarian Society et sources spécifiques. Le champ `use` précise leurs limites. Certaines références ne sont que des pistes de recherche et ne justifient pas à elles seules un statut.

Le référentiel réglementaire multilingue UE, sa couverture, ses exceptions et la distinction entre reconnaissance et compatibilité vegan sont détaillés dans [Référentiel des additifs UE](additifs.md) et [l’extraction de l’annexe II-B](sources/eu-additives-annex-ii-b.md). La présence d’un additif dans la liste UE ne constitue pas une preuve d’origine vegan.

La même séparation s’applique aux normes sectorielles : la directive miel UE relie les dénominations réglementaires au concept `honey`, tandis que son statut `VEGETARIAN` est une décision éditoriale distincte. Voir [Directive UE sur le miel](sources/eu-honey-directive.md).

La directive UE sur les laits conservés applique le même principe : CELEX 02001L0114 documente les dénominations de lait concentré, évaporé et en poudre jointes aux concepts existants `milk` et `cream`; leurs statuts `VEGETARIAN` restent des décisions éditoriales. Voir [Directive UE sur les laits conservés](sources/eu-preserved-milk-directive.md).

CELEX 02001L0113 documente les catégories strictement définies de confiture,
gelée, marmelade d’agrumes et crème de marrons sucrée. Ces catégories restent
distinctes de `fruit_juice`, `fruit_puree`, `fruit_nectar`, des fruits frais,
des arômes et des préparations commerciales générales. Voir [Directive UE sur les confitures](sources/eu-jams-directive.md).

Le [règlement UE sur les produits agricoles](sources/eu-agricultural-products-regulation.md) ajoute des dénominations réglementaires pour les viandes, abats, graisses animales, œufs, huiles et catégories transformées. Les produits de la pêche et de l’aquaculture sont hors de son champ.

Les qualifications d’origine suivent leur propre source éditoriale, `knowledge/origin_qualifier_rules.json`, copiée vers les assets par `tools/build_origin_rules.py`. Son schéma versionné contient sources, règles actives ou en revue et expressions protégées. Une source réglementaire peut établir l’identité d’un additif sans certifier son caractère vegan ; les sources spécialisées servent à confirmer une origine ou à conserver l’incertitude lorsque les procédés varient. La revue 0.6.11.1 est documentée dans `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_1_REPORT.md`.

La [directive UE cacao et chocolat](sources/eu-cocoa-chocolate-directive.md) distingue le cacao, le beurre de cacao et les catégories de chocolat. Elle établit des dénominations et une composition réglementaire ; elle ne confirme pas qu’une recette commerciale de chocolat ou de praline soit vegan.

## Modifier la base

1. Modifier uniquement `knowledge/ingredients.json`.
2. Vérifier l’unicité des identifiants et des alias, le statut, la raison et les sources.
3. Générer l’asset et les tableaux documentaires :

   ```bash
   python tools/build_ingredients.py
   python tools/build_knowledge_docs.py
   ```

4. Vérifier sans écrire :

   ```bash
   python tools/build_ingredients.py --check
   python tools/build_origin_rules.py --check
   python tools/build_knowledge_docs.py --check
   ```

5. Ajouter des tests de matching et de verdict pour les cas ambigus.

Le générateur refuse un alias appartenant à deux identifiants et un statut hors enum. Il ne vérifie pas automatiquement la qualité éditoriale d’une preuve : cette revue reste humaine.

## Propositions d’ajout

Cette table est un backlog éditorial. Une ligne n’est pas une instruction de modifier les données : la décision exige une raison, une source et un test réel.

| ID proposé | Nom FR | Alias connus | Statut proposé | Source à vérifier | Décision |
|---|---|---|---|---|---|
| `cereals` | Céréales non précisées | céréales, granen, cereals | Aucun statut | Cas d’étiquette réel | Concept générique |
| `e422` | Glycérol | glycerol, glycerine | `UNCERTAIN` | Règle d’origine existante | Déjà couvert |
| `natural_flavouring` | Arôme naturel | arôme naturel, natural flavouring | `UNCERTAIN` | Formulation et fabricant | À conserver incertain |
| `vitamin_d` | Vitamine D | vitamin D, cholecalciferol | `UNCERTAIN` | Origine précise / fabricant | À conserver incertain |
| `e322` | Lécithines sans origine | lecithin, lecithinen | `UNCERTAIN` | Origine explicitement rattachée | Déjà couvert |
| Additif ou arôme ambigu | À qualifier | Étiquette complète | Aucun statut implicite | Source primaire ou fabricant | À vérifier |

Les décisions possibles sont : `À vérifier`, `À ajouter`, `Déjà couvert`, `Alias uniquement`, `Concept générique`, `À conserver incertain` et `À ne pas ajouter`.

## Bloc à transmettre pour préparer les prochains ajouts

```text
CONCEPTS À AJOUTER
- id :
  nom français :
  alias FR :
  alias NL :
  alias DE :
  alias EN :
  alias IT :
  alias ES :
  statut proposé :
  raison :
  source :
  test réel :

CONCEPTS RECONNUS MAIS NON CLASSIFIABLES
- concept :
  alias observé :
  langue :
  raison :

CORRECTIONS OCR
- langue :
  texte OCR :
  correction :
  concept :

ORIGINES À AJOUTER
- texte :
  origine :
  langue :
  classification attendue :

CAS À TESTER
- étiquette :
  langue :
  verdict attendu :
  traces :
  erreur actuelle :
```
