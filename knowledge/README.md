# Is It Vegan — base de connaissance

`ingredients.json` est la source éditoriale : chaque entrée comporte ses
synonymes français, néerlandais et anglais, son verdict et son niveau de
confiance. `tools/build_ingredients.py` la valide puis produit le fichier
embarqué par l'application.

Règles de décision :

- `VEGAN` : ingrédient végétal, minéral ou de synthèse sans origine animale
  raisonnablement attendue ;
- `NON_VEGAN` : ingrédient clairement non végétarien (chair, gélatine,
  présure animale, carmin de cochenille, gomme-laque issue d'insectes) ;
- `VEGETARIAN` : ingrédient animal sans chair ni partie du corps de l'animal
  (lait, œuf, miel), donc non vegan ; ce verdict concerne uniquement la liste
  d'ingrédients et ne constitue jamais une certification ;
- `UNCERTAIN` : l'origine varie selon le procédé ou le fournisseur, ou le
  libellé ne permet pas de conclure.

Ne jamais transformer `UNCERTAIN` en `VEGAN` pour améliorer artificiellement
la couverture. Une entrée sans preuve ou sans revue reste à ajouter, pas à
deviner.

Priorité du verdict présenté :

`NON_VEGETARIAN` > `UNCERTAIN` > `INCONCLUSIVE` > `VEGETARIAN` > `VEGAN`.

- l'analyse s'arrête au premier ingrédient `NON_VEGAN`, qui suffit à établir
  le verdict `NON_VEGETARIAN` ;
- les ingrédients `UNCERTAIN` sont conservés et l'analyse continue ;
- lorsqu'un verdict est `UNCERTAIN`, l'application indique également le
  verdict du reste de la composition après exclusion des ingrédients
  incertains : `VEGAN`, `VEGETARIAN` ou `INCONCLUSIVE` ;
- les ingrédients inconnus empêchent toujours de valider le reste comme vegan
  ou végétarien ;
- les mentions de contamination croisée (`Traces`, `peut contenir` ou
  `fabriqué dans un atelier`) sont exclues du verdict. Une mention de
  composition comme `contient : moutarde` reste analysée.

Les sources de travail sont décrites dans `sources.json`. Les données
Open Food Facts et le contenu de WebAdditifs ne sont pas importés : leurs
licences et leurs conditions d'utilisation doivent être respectées avant toute
réutilisation massive.

## Pipeline d'analyse hors ligne

Depuis la version 0.5.4, l'analyse est divisée en modules testables. La version
0.5.7 (code 13) reconstruit en plus la hiérarchie. Depuis la version 0.5.8
(code 15), le pipeline commence par `LabelLanguageSegmenter` :

1. `LabelLanguageSegmenter` découpe les blocs explicitement marqués FR, NL, EN
   ou DE, conserve leurs marqueurs de marché et sélectionne le bloc français
   exploitable en priorité. Sa détection est volontairement prudente : sans
   marqueur fiable, il conserve le texte complet comme bloc `UNKNOWN`. Il ne
   traduit pas, ne reconnaît aucun ingrédient et ne participe pas au verdict ;
2. `LabelSectionExtractor` isole à profondeur zéro la liste d'ingrédients, les
   mentions de présence réelle (`contient`, `bevat`, `contains`, `enthält`),
   les traces éventuelles et les sections d'étiquette non alimentaires. Les
   traces restent hors du verdict et une mention imbriquée dans un ingrédient
   composé ne découpe jamais sa composition ;
3. Depuis la version 0.5.6 (code 12), `LabelPreprocessor` renvoie un
   `PreprocessedLabel` : `compositionText`, `crossContactWarnings` et
   `excludedNotes`. Seule la composition passe ensuite dans `QuantityCleaner`,
   qui supprime les quantités sans altérer `E471`, `B12`, `D2` ou `oméga-3` ;
4. `IngredientTokenizer` découpe la liste, sépare aussi les éléments au point
   de niveau racine (sans couper les décimales), et produit des nœuds reliés à
   leur parent. Chaque nœud est un ingrédient, un ingrédient composite ou un
   titre de section ; les parenthèses et crochets imbriqués restent associés à
   leur véritable parent ;
5. `IngredientMatcher` privilégie les alias les plus longs afin que, par
   exemple, `lait de coco` masque correctement l'alias plus court `lait`. Un
   alias court qui est aussi le préfixe d'alias plus précis est refusé lorsqu'il
   est suivi de `de`, `d'`, `du`, `des`, `à` ou `au` et qu'aucun alias complet
   ne couvre l'expression. Ainsi `farine de lin` ne correspond pas à
   `wheat_flour`, tandis que `farine de blé` et `lait` seul restent valides ;
6. `UnknownCollector` conserve le résidu réellement non reconnu ;
7. `VerdictEngine` applique la priorité des verdicts et l'arrêt anticipé sur
   un ingrédient non végétarien.

Les titres tels que `Farce (63 %) :`, `Cœur au tofu fumé 62,6 % :` ou
`Enrobage 37,4 % :` organisent leurs enfants. Ils figurent dans le diagnostic,
mais ne passent ni dans le matcher ni dans les inconnus et n'influencent pas le
verdict. Un ingrédient composite conserve les correspondances présentes dans
son propre nom, puis analyse ses enfants. Son libellé structurel résiduel peut
être omis des inconnus lorsque sa sous-composition est explicite ; le même
libellé sans sous-composition reste inconnu.

La transmission de contexte est volontairement étroite : le groupe
`huiles végétales en proportion variable` permet seulement d'essayer
`huile de colza`, `huile de tournesol`, etc. pour ses enfants simples, avec les
alias déjà présents. Le texte enrichi est visible dans le diagnostic et aucune
combinaison générale de mots n'est inventée.

Une erreur de parsing crée un faux token, perd une relation ou choisit un alias
incorrect. Un ingrédient absent de `ingredients.json` reste au contraire un
inconnu légitime, même si la structure qui le contient est parfaitement
reconstruite. La couverture de la base et la qualité du parseur sont donc
testées séparément.

Les avertissements du fabricant (`Peut contenir`, `Traces :`, `Traces
éventuelles de`, `Fabriqué dans un atelier`) sont conservés dans leur ordre
d'apparition, sans doublons identiques après nettoyage d'affichage. Ils ne
passent jamais dans le tokenizer, le matcher, les inconnus ou le moteur de
verdict et ne déclenchent pas l'arrêt anticipé. `contient : lait` reste une
information de composition. Une entrée contenant seulement une trace donne
`INCONCLUSIVE`.

`AnalysisResult` conserve les avertissements et les notes exclues. L'interface
ajoute une section « TRACES SIGNALÉES » à la fin du résultat, quel que soit le
verdict, uniquement si des avertissements existent. Elle reproduit le texte du
fabricant et précise qu'il n'intervient pas dans le verdict.

Les notes reconnues (certification Rainforest Alliance et son pied de page,
`*Agriculture biologique`, `^concentré`, déclarations `Allergènes :`) sont
conservées dans `excludedNotes` pour le diagnostic uniquement. Aucun moteur
d'allergies ni classement des allergènes n'est effectué ; aucun conseil médical
ni garantie d'absence d'allergènes n'est fourni.

Le rapport de diagnostic distingue « COMPOSITION APRÈS PRÉTRAITEMENT » (avant
nettoyage des quantités), « TRACES / CONTAMINATION CROISÉE » et « NOTES EXCLUES »,
puis affiche pour chaque nœud son type, sa profondeur, son parent, son texte
original, le texte éventuellement enrichi pour le matcher, ses correspondances
et son résidu inconnu. Le fonctionnement reste hors ligne.
