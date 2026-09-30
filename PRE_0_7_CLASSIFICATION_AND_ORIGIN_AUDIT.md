# Audit éditorial des classifications et des origines avant 0.7

Date : 2026-09-30  
Périmètre : `knowledge/ingredients.json`, `knowledge/origin_qualifier_rules.json`, sources documentées et tests JVM associés.  
Mode : lecture seule. Aucun statut, fichier applicatif ou règle de verdict n'a été modifié.

## 1. Résumé exécutif

La base contient 479 concepts : 127 `VEGAN`, 12 `VEGETARIAN`, 10 `NON_VEGAN` et 330 `UNCERTAIN`. Les familles animales explicites sont classées `NON_VEGAN` ou `VEGETARIAN` selon la distinction du projet. Les familles d'origine variable, les arômes, les chocolats commerciaux et les produits composés portent généralement `UNCERTAIN` avec une note d'origine lorsque la variabilité est documentée.

Aucun statut démontrablement incorrect n'a été établi à partir des sources présentes. Les règles d'origine actives n'autorisent une résolution que pour une qualification attachée au même ingrédient. Les tests vérifient qu'un inconnu ne devient pas vegan, que les traces sont exclues du verdict, que les compositions conservent leurs enfants et que les arômes ne valent pas l'ingrédient évoqué.

Les avertissements concernent surtout la couverture documentaire : aucun concept `collagen` n'est présent, la couverture des produits de la mer reste limitée à `fish`, les vitamines ont des sources générales sans preuve de procédé pour chaque forme, et certaines collisions d'alias réglementaires restent résolues par un statut commun `UNCERTAIN`.

## 2. Tableau des anomalies par gravité

| Gravité | Famille / élément | Constat vérifiable | Catégorie |
|---|---|---|---|
| Haute | Aucune | Aucun statut démontrablement incorrect identifié dans les concepts examinés. | — |
| Moyenne | `e270` / acide lactique | Le statut par défaut est `VEGAN`; la règle active le rend non vegan seulement lorsqu'une origine laitière est syntaxiquement attachée. Cette convention est explicitement documentée, mais le libellé seul ne prouve pas l'origine de chaque fabrication. | Statut prudent mais acceptable ; risque de faux `VEGAN` si une origine commerciale est omise du libellé |
| Moyenne | `e470b` / `e572` | Les formes normalisées « sels de magnésium d'acides gras » sont partagées par deux concepts. Les deux sont `UNCERTAIN`, donc la collision ne produit pas de faux statut vegan ou non vegan dans l'état actuel. | Alias ambigu ; simple lacune de désambiguïsation |
| Moyenne | `cereals` | Groupe d'alias structuré reconnu sans concept canonique dans `ingredients.json`. Le générateur le documente comme non classifiable. | Concept trop générique ; risque de faux `VEGAN` évité par l'inconnu |
| Moyenne | `collagen` | Aucun concept ni alias dédié au collagène n'a été trouvé. | Source ou couverture manquante ; lacune documentaire |
| Moyenne | Produits de la mer | `fish` et des alias d'anchois sont présents, mais aucune famille structurée `seafood`, crustacés ou mollusques n'a été identifiée. | Couverture documentaire incomplète |
| Faible | 18 concepts d'additifs | Des alias directs répétés existent dans plusieurs entrées E. Les mappings structurés ne sont pas dupliqués et les statuts concernés sont généralement `UNCERTAIN`. | Alias dupliqué ; risque de maintenance |
| Faible | `vitamin_d`, `vitamin_b12` | Les deux concepts sont `UNCERTAIN`, avec une source générale et sans note structurée d'origine. | Statut incertain faute de preuve ; absence de note d'origine |
| Faible | `beeswax`, `cheese` | Les statuts `UNCERTAIN` reflètent respectivement la variation des critères végétariens et de la présure. | Statut prudent mais acceptable |

## 3. Concepts à vérifier en priorité

### Produits animaux et dérivés

`meat`, `edible_offal`, `animal_fat` et `poultry_meat_preparation` sont `NON_VEGAN`, avec des alias réglementaires explicites. `fish` est `NON_VEGAN`; `gelatin` est `NON_VEGAN`; `honey` et les produits laitiers explicites sont `VEGETARIAN`. Ces statuts sont cohérents avec les raisons éditoriales et ne sont pas de simples déductions à partir d'une ressemblance lexicale.

### Produits laitiers, œufs, miel, gélatine et collagène

`milk`, `lactose`, `whey`, `casein`, `butter`, `cream` et `milk_chocolate` sont `VEGETARIAN`. `egg` est `VEGETARIAN`. `honey` est `VEGETARIAN`. `gelatin` est `NON_VEGAN`. Le concept `beeswax` reste `UNCERTAIN`, car sa classification végétarienne dépend des critères éditoriaux retenus.

Le collagène n'a pas de concept présent. Cette absence empêche d'établir un faux statut, mais laisse une lacune de couverture pour un ingrédient animal explicite.

### Viande, abats, préparations et produits de la mer

Les concepts `meat`, `edible_offal`, `animal_fat` et `poultry_meat_preparation` sont `NON_VEGAN`. Les sources agricoles servent à documenter les dénominations, tandis que le statut animal est une décision éditoriale séparée. `fish` est `NON_VEGAN` et couvre `poisson`, `fish`, `anchois` et leurs formes néerlandaises.

Les catégories générales de produits de la mer, crustacés et mollusques ne sont pas représentées comme concepts distincts. Il s'agit d'une lacune documentaire et de couverture, pas d'un statut vegan incorrect.

### Additifs E

La majorité des additifs importés depuis la référence UE sont `UNCERTAIN`, ce qui respecte la règle selon laquelle l'autorisation réglementaire ne prouve pas l'origine vegan. Les entrées explicitement classées `VEGAN` ou `VEGETARIAN` disposent d'une raison éditoriale spécifique, par exemple les sels minéraux, le lactitol ou certains additifs déjà établis.

Les cas variables à surveiller sont `e322`, `e471`, `e422`, `e640`, `e470a`, `e470b`, `e572` et `e270`. Les notes d'origine sont présentes pour les cas variables documentés ; les règles actives ne couvrent directement que `e322`, `e471` et `e270`. Les règles candidates pour `e422`, les inosinate/nucléotides et `e640` restent `REVIEW_BEFORE_ENGINE_USE`, donc elles ne reclassent pas silencieusement les concepts.

`e322a` est `UNCERTAIN` sans note structurée d'origine. Son libellé est une lécithine d'avoine, mais la source UE d'identité ne suffit pas à établir le procédé ou l'origine commerciale ; le statut prudent est donc conservé comme acceptable.

### Arômes et préparations aromatisantes

`natural_flavouring`, `flavouring`, `flavouring_substance`, `flavouring_preparation`, `thermal_process_flavouring`, `smoke_flavouring`, `flavour_precursor`, `other_flavouring` et `food_ingredient_with_flavouring_properties` sont `UNCERTAIN` et portent des notes d'origine structurées. Les raisons indiquent explicitement qu'un arôme ne prouve pas la présence de l'aliment évoqué.

Les tests et la documentation distinguent `arôme fraise`, `extrait de café` et les ingrédients `fraise` ou `café`. Aucun alias de type `arôme de vanille` n'a été trouvé comme alias direct de `vanilla`. Ce comportement réduit le risque de faux `VEGAN` et de faux ingrédient réel par ressemblance.

### Fruits, purées, jus, nectars et confitures

`fruit_juice` et `fruit_puree` sont `VEGAN` dans le périmètre de leurs dénominations réglementaires de produits issus de fruits. `fruit_nectar` est `UNCERTAIN`, car la catégorie peut contenir du miel, des sucres ou des édulcorants. Les catégories de confitures, gelées, marmelade d'agrumes et crème de marrons sucrée sont distinctes des jus, purées, nectars et arômes ; `sweetened_chestnut_puree` est `VEGAN` selon la composition réglementaire documentée.

Cette classification reste dépendante de la correspondance exacte avec la catégorie réglementaire. Elle ne transforme pas une préparation commerciale générale ou une composition inconnue en produit vegan.

### Cacao, chocolat et produits agricoles composés

`cocoa` et `cocoa_butter` sont `VEGAN` selon leur identité végétale documentée. `chocolate` est `UNCERTAIN`, `milk_chocolate` est `VEGETARIAN`, et `filled_chocolate` ainsi que `chocolate_confection` sont `UNCERTAIN` avec note d'origine. La dénomination chocolat ne garantit pas une recette sans lait, œuf ou miel.

`processed_fruit_vegetable_product` et `spreadable_fat` sont `UNCERTAIN` avec notes d'origine `PLANT`/`ANIMAL`. La catégorie réglementaire de produit agricole composé ne masque donc pas une composition commerciale potentiellement animale.

### Vitamines, minéraux et nutriments

`vitamin_d` et `vitamin_b12` sont `UNCERTAIN`, ce qui est prudent en l'absence de preuve de l'origine et du procédé de fabrication de la forme commercialisée. Des additifs minéraux comme `e170` sont `VEGAN` avec une raison indiquant une désignation minérale, tandis que de nombreux sels minéraux importés restent `UNCERTAIN` par absence de preuve éditoriale d'origine.

## 4. Alias à protéger

- `lait de coco`, `lait de soja` et `lait d'amande` doivent rester attachés à leurs concepts végétaux respectifs ; le mot `lait` seul reste attaché à `milk`.
- Les expressions d'arôme et d'extrait doivent rester des catégories aromatisantes ou des inconnus lorsqu'elles évoquent un goût, sans devenir l'ingrédient réel. Les cas documentés sont `arôme fraise`, `extrait de café` et les arômes de chocolat.
- Les expressions `huile végétale` et `huiles végétales (...)` sont explicitement protégées par `origin_qualifier_rules.json`; leur parenthèse ne doit pas résoudre un autre additif comme `e471`.
- Les qualifications `E471 (origine végétale)`, `E471 (origine animale)`, `lécithines (soja)` et `lécithines (œuf)` ne doivent produire un effet que lorsqu'elles qualifient le même ingrédient.
- `cereals` doit rester non classifiable tant qu'il ne possède pas de concept canonique ; il ne doit pas devenir un alias vegan générique.
- Les formes partagées par `e470b` et `e572` doivent rester traitées comme ambiguës tant qu'une distinction contextuelle ou un numéro E explicite n'est pas disponible.

## 5. Statuts à ne pas modifier sans source supplémentaire

- `UNCERTAIN` pour `e322`, `e471`, `e422`, `e640`, `e470a`, `e470b`, `e572`, `cheese`, `beeswax`, `fruit_nectar`, `chocolate`, `filled_chocolate`, `chocolate_confection`, les arômes, les graisses tartinables et les produits agricoles composés.
- `UNCERTAIN` pour `vitamin_d`, `vitamin_b12` et les nutriments dont l'origine de fabrication n'est pas précisée.
- `VEGAN` pour `e270` sans qualificatif laitier doit rester lié à la règle active documentée ; aucune généralisation à d'autres acides ou additifs n'est établie.
- `VEGAN` pour les jus, purées, catégories de confiture et beurre de cacao doit rester limité aux dénominations et compositions réglementaires représentées par les concepts concernés.
- `VEGETARIAN` pour le lait, les œufs, le miel, le lactitol et le chocolat au lait ne doit pas être traité comme un synonyme de `VEGAN`.
- `NON_VEGAN` pour viande, poisson, gélatine, cire d'abeille E901, abats, graisse animale et préparations de volaille ne doit pas être abaissé sur la base d'une source de dénomination réglementaire seule.

## 6. Sources manquantes

- Source d'identité et de classification dédiée au collagène et à ses hydrolysats.
- Couverture réglementaire ou éditoriale dédiée aux crustacés, mollusques et catégories générales de produits de la mer.
- Sources fabricant ou spécifications de procédé pour les formes de vitamine D, vitamine B12 et autres nutriments dont l'origine peut varier.
- Source plus précise pour la classification végétarienne de la cire d'abeille, si le projet doit dépasser le statut `UNCERTAIN`.
- Source primaire ou spécification fabricant pour distinguer les formes commerciales partageant les alias E470b/E572.

Les sources réglementaires existantes documentent principalement l'identité, les dénominations ou les compositions réglementaires. Elles ne constituent pas, à elles seules, une preuve d'origine vegan.

## 7. Recommandations pour le modèle de données 0.7

- Conserver la séparation entre statut de base, note d'origine, règle d'origine active et preuve documentaire.
- Ajouter un champ ou une structure de provenance permettant de distinguer identité réglementaire, origine démontrée, origine variable et simple lacune documentaire.
- Représenter explicitement les concepts génériques non classifiables comme `cereals`, sans leur attribuer un statut implicite.
- Ajouter une relation de contexte pour les alias ambigus, les arômes, les extraits et les catégories composées ; l'alias seul ne doit pas créer une équivalence sémantique.
- Conserver les enfants des compositions et leur chemin parent/enfant dans les données de diagnostic afin qu'un parent générique ne masque jamais un enfant animal ou incertain.
- Ajouter une couverture de sources et de tests pour le collagène, les produits de la mer et les vitamines avant toute reclassification.
- Conserver les traces dans une section séparée du modèle d'analyse ; elles ne doivent pas participer au verdict principal.

## 8. Verdict

**READY_WITH_WARNINGS**

Les classifications inspectées sont globalement cohérentes et prudentes. Les tests ciblés JVM de hiérarchie, traces, inconnus, qualifications d'origine et verdicts passent. Aucun faux `VEGAN` ou faux `NON_VEGAN` démontré n'a été identifié dans les données examinées. La préparation 0.7 reste conditionnée à la conservation des protections de contexte et à la documentation des lacunes de couverture signalées, sans modification de statut dans cet audit.
