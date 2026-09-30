# Présentation

Cette documentation décrit l’état livré du dépôt **Is It Vegan? jusqu’à 0.6.13.7**, avant les travaux fonctionnels 0.7. L’application Android aide à lire une étiquette alimentaire et à évaluer les ingrédients déclarés. Elle propose trois modes d’entrée : étiquette complète, liste d’ingrédients seule et photo avec OCR.

Le produit poursuit trois objectifs techniques :

- conserver les données et le calcul métier sur l’appareil, sans décision par IA générative ni service cloud ;
- séparer la lecture de l’étiquette, sa structure syntaxique et la classification vegan ;
- rester prudent : un texte non reconnu ou une origine variable ne devient jamais vegan par défaut.

## Résultats présentés

Deux vues du résultat coexistent dans le modèle :

- `VeganAssessment` répond à la question de compatibilité vegan : `VEGAN`, `NOT_VEGAN` ou `UNCERTAIN` ;
- `AnalysisVerdict` conserve une classification plus détaillée : `VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`, `UNCERTAIN` ou `INCONCLUSIVE`.

L’écran actif, `OcrFirstScreen`, affiche principalement :

- **VEGAN** si tous les ingrédients analysés sont reconnus vegan ;
- **NON VEGAN** lorsqu’un ingrédient non végétarien est identifié ;
- **VÉGÉTARIEN** comme information détaillée lorsque les ingrédients animaux reconnus restent compatibles avec ce régime ; ce statut reste incompatible vegan ;
- **INCERTAIN** lorsqu’un ingrédient connu possède un statut variable ;
- **INCONCLUS** lorsque du texte réellement analysable reste inconnu.

Une étiquette sans section d’ingrédients exploitable produit `NO_INGREDIENT_LIST` et aucune conclusion. Une déclaration autonome telle que « contient : lait » peut néanmoins fournir une preuve de présence réelle, exposée séparément dans le diagnostic.

## Ingrédients, allergènes et traces

La composition déclarée alimente le parseur, le matcher et le moteur de verdict. Les avertissements « peut contenir », « may contain », « kan bevatten », « kann enthalten » et formulations voisines sont conservés comme risques de contamination croisée, puis exclus du calcul. Une mention de présence réelle comme « contient : lait » reste une information de composition.

Les déclarations d’allergènes reconnues sont conservées dans les notes exclues du diagnostic. Le dépôt ne contient pas de moteur réglementaire complet des allergènes et ne prétend pas établir l’absence d’allergènes.

## Langues

L’interface utilisateur, les verdicts, les explications conditionnelles et les traces existent en **FR**, **NL**, **EN** et **DE**. Le choix est mémorisé dans `SharedPreferences`.

La portée linguistique du traitement est plus large mais inégale :

- les titres d’ingrédients sont reconnus en français, néerlandais, anglais, allemand et espagnol ;
- les marqueurs structurés comprennent aussi l’italien, le portugais, le suédois, le danois, le norvégien et le finnois ;
- la reclassification par vocabulaire est implémentée pour FR, EN, NL et DE ;
- la base embarquée contient surtout des alias français, anglais et néerlandais.

La reconnaissance d’un bloc allemand ou espagnol ne garantit donc pas que tous ses ingrédients existent dans la base.

## Principes de conception observés

- Le texte brut ML Kit est conservé sans modification dans `OcrSession.rawOcrText`.
- Le texte éditable est nettoyé de manière déterministe, reste modifiable et doit être vérifié avant analyse.
- L’ordre natif de `Text.text` fourni par ML Kit est la source du texte analysé. La reconstruction géométrique sert actuellement à détecter et signaler les divergences.
- Les conteneurs composites structurent l’arbre mais seuls leurs enfants terminaux sont classés.
- Les alias, statuts et raisons viennent de fichiers JSON versionnés, jamais d’un service distant ou d’une correction générative.
- Les traitements lourds d’image et d’analyse sont envoyés sur `Dispatchers.IO` ou `Dispatchers.Default` pour éviter de bloquer l’interface.

## Limites et travaux non livrés

L’OCR reste sensible au flou, à l’éclairage, au recadrage, aux petits caractères, aux tableaux et aux étiquettes multilingues. Lorsque la composition est incomplète ou douteuse, l’utilisateur doit corriger le texte ou reprendre une photo plus lisible, idéalement avec un cadrage plus large. L’application ne suggère pas encore automatiquement cette reprise.

La consolidation de plusieurs langues d’une même étiquette n’est pas livrée : le choix de bloc sert à sélectionner le texte à analyser, et les autres blocs multilingues restent une aide de validation dans le diagnostic. Ces sujets sont des travaux futurs, sans calendrier annoncé.

Commencer par [l’architecture générale](architecture.md), puis suivre le [pipeline OCR](ocr.md), le [pipeline d’analyse](parsing.md) ou les [notes de version](changelog.md).
