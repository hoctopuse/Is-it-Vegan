# Audit documentaire pré-0.7

Audit réalisé en lecture seule le 30 septembre 2026. Aucun document existant n’a été réécrit et aucune génération n’a été lancée.

## 1. Périmètre et sources examinées

Ont été examinés : `README.md`, `docs/index.md`, `docs/base-connaissances.md`, `docs/additifs.md`, `docs/generated/`, `docs/changelog.md`, `docs/guide-developpeur.md`, les pages `docs/sources/`, les README des outils, `SOURCE_MANIFEST.md`, les rapports de versions 0.6.9 à 0.6.13 et les dossiers `reference-input/eu-food-labelling/`.

Les contrôles non destructifs effectués sont les suivants :

- vérification des liens Markdown locaux ; aucun lien local cassé n’a été trouvé ;
- vérification des cibles Markdown déclarées dans `mkdocs.yml` ; toutes les cibles de navigation existent ;
- `python tools/build_ingredients.py --check` : succès ;
- `python tools/build_multilingual_ingredient_mapping.py --check` : succès ;
- `python tools/build_origin_rules.py --check` : succès ;
- `python tools/build_knowledge_docs.py --check` : échec, car `docs/generated/base-connaissances-data.md` est obsolète ;
- inspection des commandes et des options réellement exposées par les générateurs et importeurs.

## 2. Ce qu’un nouvel agent peut comprendre

La documentation identifie correctement `knowledge/ingredients.json` comme source de vérité et indique que l’asset Android est généré. Elle décrit aussi les rôles de `knowledge/sources.json`, de `knowledge/ingredient_aliases_multilingual.json` et de `knowledge/origin_qualifier_rules.json`.

Le document `docs/base-connaissances.md` explique de façon utile :

- le concept canonique, les alias et la séparation entre alias et statut ;
- les statuts `VEGAN`, `VEGETARIAN`, `NON_VEGAN` et `UNCERTAIN` ;
- les raisons, les sources et les notes d’origine ;
- la limite selon laquelle une source réglementaire décrit une dénomination ou une composition, sans constituer à elle seule une preuve d’origine vegan ;
- la différence entre arôme et ingrédient réel ;
- la différence entre trace et ingrédient déclaré ;
- le rôle des compositions et des règles d’origine ;
- les concepts proposés mais non encore couverts dans le backlog.

`docs/guide-developpeur.md` donne les commandes de génération des ingrédients et des règles d’origine, ainsi que les contrôles `--check`. Les pages de sources décrivent généralement les PDF locaux, le CELEX, les limites éditoriales et les effets du mode `--write`.

Les limites du verdict sont rappelées dans `docs/verdict.md`, `docs/matching.md` et `docs/parsing.md` : un inconnu ne devient pas vegan, les éléments incertains restent visibles, les traces restent hors du verdict et un produit composé conserve ses enfants.

## 3. Chaîne documentaire et commandes

Le flux principal est compréhensible :

`reference-input/` → importeur réglementaire → `knowledge/` → générateurs Python → `app/src/main/assets/` → runtime Android.

La documentation explique correctement que l’asset ne doit pas être modifié directement. Elle indique aussi que les descriptions complètes des sources restent dans `knowledge/sources.json` et que l’asset ne conserve que les identifiants utiles au runtime.

La couverture des commandes n’est toutefois pas homogène :

| Élément | État documentaire et constat vérifié |
|---|---|
| `build_ingredients.py` | Commandes normales et `--check` documentées ; contrôle réussi. |
| `build_origin_rules.py` | Commandes normales et `--check` documentées ; contrôle réussi. |
| `build_multilingual_ingredient_mapping.py` | Contrôle `--check` réussi, mais absent de la procédure centrale de `docs/base-connaissances.md` et de la liste de livraison de `docs/guide-developpeur.md`. Il apparaît seulement dans certaines pages de sources. |
| `build_knowledge_docs.py` | Commande et `--check` documentés ; le contrôle échoue actuellement parce que le document généré des connaissances est obsolète. Aucune régénération n’a été effectuée. |
| importeur des additifs | La page décrit l’écriture et les générateurs ; elle ne fournit pas un contrôle importeur `--check` équivalent. Elle documente l’idempotence attendue. |
| miel et lait | Les pages documentent `--dry-run` et `--write`, mais répètent `--dry-run` au lieu de fournir un contrôle distinct. |
| confitures et arômes | `--dry-run`, `--write` et `--check` sont documentés. |
| FIC | `--write` et `--check` sont documentés, avec extraction TXT et manifeste. |
| produits agricoles et cacao | L’importeur et `--write` sont décrits, mais la procédure opératoire ne donne pas une séquence complète `--dry-run`/`--check`/générateurs. |
| contrôle final | La liste de `docs/guide-developpeur.md` omet le générateur multilingue et le générateur de documentation. |

L’idempotence est explicitement affirmée pour le flux additifs et implicitement recherchée par les contrôles des générateurs. Il manque une page centrale qui donne, importeur par importeur, les options effectivement acceptées et la définition vérifiable de l’idempotence.

## 4. Sources réglementaires et fichiers destinés aux futurs agents

Le dossier FIC contient quatre PDF locaux, quatre extractions TXT et un `SOURCE_MANIFEST.md` qui décrit CELEX, date de consolidation, outil, hashes, pages et extractions thématiques. Le dossier additifs contient quatre PDF, le CSV multilingue et un manifeste analogue.

Les PDF des dossiers 01 et 02 sont présents et leurs pages `docs/sources/` existent pour les arômes, le cacao, le miel, les jus, les confitures, le lait et les produits agricoles. Les chemins de PDF mentionnés par ces pages correspondent aux fichiers présents.

Le rôle des TXT légers est expliqué localement pour FIC comme une extraction UTF-8 reproductible, mais il n’existe pas de description centrale indiquant explicitement qu’ils sont destinés à une lecture rapide par de futurs agents, ni d’index global reliant chaque TXT à son importeur et à sa source primaire.

Le dossier `reference-input/eu-food-labelling/04-fortified-foods-and-supplements/` contient huit PDF locaux : quatre pour CELEX 02002L0046 et quatre pour CELEX 02006R1925. Aucun `SOURCE_MANIFEST.md`, aucune page `docs/sources/`, aucun importeur réglementaire correspondant et aucune procédure d’import n’ont été trouvés. Leur statut documentaire est donc celui de sources préparées dont l’import dans la base n’est pas démontré.

Les dossiers 01 et 02 ne possèdent pas non plus de manifeste propre. Le dossier 01 contient en outre des copies des quatre PDF FIC déjà présents dans le dossier 00. Cette organisation est traçable par les noms de fichiers, mais elle n’est pas expliquée à un nouvel agent.

Les éléments explicitement préparés mais différés sont identifiables dans les documents FIC et dans le backlog de `docs/base-connaissances.md` : certains allergènes ou catégories restent à examiner, et plusieurs concepts génériques ou nutriments sont proposés sans import confirmé. La documentation ne fournit pas une table unique “préparé / importé / volontairement différé”.

## 5. Versions, rapports et décomptes

La version applicative détectée dans `app/build.gradle.kts` est `0.6.13.7`.

Les rapports et le changelog des lots 0.6.9 à 0.6.13 présentent une progression cohérente des lots réglementaires : additifs, miel, lait, confitures, produits agricoles, cacao/chocolat et arômes. Les décomptes historiques sont généralement identifiés par leur lot et ne doivent pas être lus comme le compte courant.

Trois références générales restent en retard :

- `README.md` indique que la documentation couvre l’état livré jusqu’à `0.6.9.9` ;
- `docs/index.md` reprend `0.6.9.9` ;
- la description de `mkdocs.yml` mentionne `0.6.9.10`.

Ces valeurs contredisent la version applicative `0.6.13.7` et peuvent conduire un nouvel agent à sélectionner le mauvais rapport de référence. `docs/additifs.md` commence également par un cadrage 0.6.9.10 ; ce cadrage est interprétable comme historique, mais il n’est pas signalé assez nettement comme tel au regard de l’état courant.

Le contrôle de `build_knowledge_docs.py --check` ajoute une incohérence indépendante des numéros de version : le document généré ne correspond pas aux sources actuelles, alors que son en-tête indique qu’il ne doit pas être modifié à la main.

## 6. Liens, navigation et références absentes

Les liens Markdown locaux vérifiés sont valides. Les entrées de navigation de `mkdocs.yml` pointent vers des fichiers présents, y compris les pages des sources réglementaires et les documents générés.

Aucune référence locale aux PDF des flux déjà documentés n’est absente. Les deux observations de chemin nécessitant une clarification sont documentaires :

- le CSV des additifs est sous `reference-input/eu-food-labelling/03-food-additives/`, mais certaines formulations le présentent comme un fichier de sortie sans toujours rappeler le chemin depuis la racine ;
- certaines commandes inline ont été détectées comme des chaînes ressemblant à des chemins par un contrôle automatique ; le script correspondant existe, ce n’est pas un lien cassé.

La lacune réelle concerne surtout les sources fortifiées : les fichiers existent sans chaîne documentaire complète, manifeste ni preuve d’import.

## 7. Anomalies prioritaires

| Priorité | Constat | Impact pour un nouvel agent |
|---|---|---|
| P0 | `build_knowledge_docs.py --check` échoue et signale une documentation générée obsolète. | Les tableaux publiés dans `docs/generated/` ne sont pas une représentation fiable de l’état courant. |
| P0 | README, index MkDocs et description MkDocs affichent des versions 0.6.9.x alors que l’application est en 0.6.13.7. | Mauvais point de départ et mauvaise lecture des rapports. |
| P1 | Aucun runbook central ne couvre tous les importeurs, options, générateurs, contrôles et ordre d’exécution. | Risque d’exécution partielle ou d’utilisation d’une option inexistante. |
| P1 | Les pages miel/lait répètent `--dry-run` et plusieurs pages ne documentent pas `--check` ou `--dry-run`. | Procédure difficile à reproduire et distinction incomplète entre simulation, validation et écriture. |
| P1 | Les sources 04 fortifiées n’ont ni manifeste, ni page, ni importeur identifié. | Impossible de déterminer depuis la documentation si elles sont importées, différées ou seulement archivées. |
| P1 | Les dossiers réglementaires 01 et 02 n’ont pas de manifeste propre ; les doublons FIC ne sont pas expliqués. | Traçabilité moins directe et risque de confusion entre copie de référence et source active. |
| P2 | Le rôle des TXT légers et leur usage par les futurs agents n’est pas centralisé. | Lecture et reprise de l’audit plus lentes. |
| P2 | Les décomptes historiques ne sont pas regroupés dans une table avec leur périmètre et leur date. | Risque de comparer un compte de lot à un compte courant. |

## 8. Corrections documentaires recommandées, sans les appliquer

1. Mettre à jour les trois points d’entrée de version (`README.md`, `docs/index.md`, `mkdocs.yml`) et marquer explicitement les sections historiques.
2. Rendre `docs/generated/` cohérent avec `knowledge/`, après validation de la politique de régénération ; ne pas éditer le fichier généré manuellement.
3. Ajouter un runbook unique couvrant les importeurs réglementaires, leurs options réelles, l’ordre `dry-run → check → write → build`, les générateurs et les tests associés.
4. Ajouter `build_multilingual_ingredient_mapping.py` et `build_knowledge_docs.py` à la procédure centrale de contrôle.
5. Corriger les exemples répétés ou incomplets des pages miel, lait, produits agricoles et cacao.
6. Documenter le statut des huit PDF de `04-fortified-foods-and-supplements/` : importé, différé ou archivage préparatoire, avec manifeste et source de vérité correspondants si ce flux est retenu.
7. Ajouter un index des sources locales indiquant, par CELEX et par langue, le PDF, le TXT éventuel, le manifeste, l’importeur, le rapport et le statut d’intégration.
8. Décrire explicitement les TXT légers comme artefacts de lecture rapide reproductibles, avec leur relation aux PDF et aux sources réglementaires.
9. Publier un tableau de décomptes datés et bornés par lot afin de séparer les statistiques historiques du compte courant.

## 9. Conclusion

La documentation est suffisamment riche pour comprendre les principes de la base, les limites du verdict et les flux principaux. Les liens et la navigation sont cohérents, et plusieurs contrôles de génération passent. La documentation générée obsolète, les versions générales en retard, l’absence de runbook complet et le statut non documenté des sources fortifiées empêchent toutefois de considérer le dépôt comme entièrement autonome pour un nouvel agent.

**documentation suffisante avec réserves**
