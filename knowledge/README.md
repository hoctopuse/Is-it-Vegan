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

Depuis la version 0.5.4, l'analyse est divisée en modules testables :

1. Depuis la version 0.5.6 (code 12), `LabelPreprocessor` renvoie un
   `PreprocessedLabel` : `compositionText`, `crossContactWarnings` et
   `excludedNotes`. Seule la composition passe ensuite dans `QuantityCleaner`,
   qui supprime les quantités sans altérer `E471`, `B12`, `D2` ou `oméga-3` ;
2. `IngredientTokenizer` découpe la liste en conservant la profondeur des
   parenthèses et crochets ;
3. `IngredientMatcher` privilégie les alias les plus longs afin que, par
   exemple, `lait de coco` masque correctement l'alias plus court `lait` ;
4. `UnknownCollector` conserve le résidu réellement non reconnu ;
5. `VerdictEngine` applique la priorité des verdicts et l'arrêt anticipé sur
   un ingrédient non végétarien.

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
puis affiche les tokens effectivement analysés. Le fonctionnement reste hors ligne.
