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

- l'analyse parcourt toute la composition afin de conserver tous les bloqueurs
  et toutes les incertitudes dans le diagnostic ;
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
0.5.9.1 (code 20) distingue une liste manuelle d'une étiquette complète ou
issue d'un OCR, et représente explicitement l'absence de liste d'ingrédients.
La version 0.5.9 (code 19) fait de `IngredientTreeParser` la source de vérité de la
structure et conserve les pourcentages comme métadonnées. La version 0.5.8.3
(code 18) applique la même reconnaissance des marchés BE, LU et LUX aux
marqueurs français et néerlandais. La version 0.5.8.2
(code 17) distingue les correspondances exactes des correspondances
contextuelles partielles et conserve l'expression complète dans les inconnus.
La version 0.5.8.1 (code 16) accepte aussi les marqueurs de bloc suivis d'un
tiret long ou court.
Depuis la version 0.5.8
(code 15), le pipeline commence par `LabelLanguageSegmenter` :

1. `LabelLanguageSegmenter` découpe les blocs explicitement marqués FR, NL, EN,
   DE ou ES, conserve leurs marqueurs de marché et sélectionne le bloc français
   exploitable en priorité. Sa détection est volontairement prudente : sans
   marqueur fiable, il conserve le texte complet comme bloc `UNKNOWN`. Il ne
   traduit pas, ne reconnaît aucun ingrédient et ne participe pas au verdict ;
2. `LabelSectionExtractor` isole à profondeur zéro la liste d'ingrédients, y
   compris les titres courts complétés comme `Ingrédients de la sauce :`, les
   mentions de présence réelle (`contient`, `bevat`, `contains`, `enthält`),
   les traces éventuelles et les sections d'étiquette non alimentaires. Les
   traces restent hors du verdict et une mention imbriquée dans un ingrédient
   composé ne découpe jamais sa composition ;
3. Depuis la version 0.5.6 (code 12), `LabelPreprocessor` renvoie un
   `PreprocessedLabel` : `compositionText`, `crossContactWarnings` et
   `excludedNotes`. Les classes fonctionnelles et les quantités restent dans
   la composition afin que le parseur puisse les structurer ;
4. `IngredientTreeParser` construit les nœuds `LEAF`, `COMPOSITE` et
   `ADDITIVE`. Les virgules et points-virgules ne séparent qu'à la profondeur
   courante, les pourcentages deviennent des `BigDecimal`, et une parenthèse
   qualificative telle que `(non hydrogénée)` reste attachée à sa feuille. Les
   classes fonctionnelles au singulier ou au pluriel restent du contexte et
   leurs désignations sont les seuls nœuds envoyés au matcher.
   `IngredientTokenizer` n'est plus qu'un adaptateur d'aplatissement pour les
   contrats internes historiques ;
5. `IngredientMatcher` privilégie les alias les plus longs afin que, par
   exemple, `lait de coco` masque correctement l'alias plus court `lait`. Un
   alias court qui est aussi le préfixe d'alias plus précis est refusé lorsqu'il
   est suivi de `de`, `d'`, `du`, `des`, `à` ou `au` et qu'aucun alias complet
   ne couvre l'expression. Ainsi `farine de lin` ne correspond pas à
   `wheat_flour`, tandis que `farine de blé` et `lait` seul restent valides. Il
   distingue une correspondance exacte, une expression couverte par des
   qualificatifs contrôlés, une correspondance contextuelle partielle et un
   conflit bloqué. Ce dernier protège notamment `beurre de cacao` du beurre
   laitier ;
6. `UnknownCollector` conserve l'expression complète lorsqu'un alias n'en
   couvre qu'une partie. Seules les feuilles et désignations d'additifs lui
   sont transmises : un conteneur composé est jugé exclusivement par ses
   enfants et ne crée jamais d'inconnu parent redondant ;
7. `VerdictEngine` conserve la classification détaillée historique et calcule
   séparément la compatibilité vegan. Un ingrédient végétarien ou non vegan
   connu donne ainsi `NOT_VEGAN`, même si un autre ingrédient reste incertain.

Les titres tels que `Farce (63 %) :`, `Cœur au tofu fumé 62,6 % :` ou
`Enrobage 37,4 % :` organisent leurs enfants. Ils figurent dans le diagnostic,
mais ne passent ni dans le matcher ni dans les inconnus et n'influencent pas le
verdict. Le nom d'un ingrédient composite n'est envoyé ni au matcher ni au
collecteur d'inconnus : seules ses feuilles participent au verdict. Un libellé
sans sous-composition reste au contraire une feuille ordinaire.

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

## Cadre d'étiquetage utilisé en 0.5.9.1

Les règles structurelles s'appuient sur le
[règlement (UE) nº 1169/2011 consolidé au 1er avril 2025](https://eur-lex.europa.eu/eli/reg/2011/1169/2025-04-01/eng),
principalement ses articles 18 à 21 et ses annexes II et VII. Ce règlement
organise l'information alimentaire ; il ne définit pas le véganisme. Le moteur
utilise donc ces règles pour localiser et structurer le texte déclaré, puis
applique séparément sa base de connaissances vegan.

Trois modes d'entrée sont disponibles :

- `MANUAL_INGREDIENT_LIST` accepte une liste saisie sans titre et autorise le
  fallback historique sur le texte complet ;
- `FULL_LABEL` exige un titre d'ingrédients reconnu avant de construire
  l'arbre ;
- `OCR_LABEL` applique la même prudence à un texte provenant d'un OCR.

Une étiquette complète ou OCR sans section reconnue produit
`NO_INGREDIENT_LIST`. Cet état ne valide ni n'invalide le caractère vegan et
ne cherche pas à décider si l'absence de liste bénéficie légalement d'une
exemption de l'article 19. Une mention autonome `Contient :` reste analysée
comme présence déclarée ; `Peut contenir :` reste une trace hors verdict.

Le parseur conserve `(nano)`, les proportions variables et les alternatives
`et/ou` comme métadonnées. Il n'invente ni quantité ni ordre relatif. Un
ingrédient composé déclaré à moins de 2 % sans sous-composition reste une
feuille ordinaire et n'est jamais supposé résolu.

Les déclarations d'origine sont reconnues par un lexique multilingue séparé.
Elles ne changent un statut que pour les identifiants inscrits dans un registre
interne limité ; en 0.5.9.1, seul `e471` est autorisé. Une origine végétale le
résout comme vegan, une origine animale comme non vegan, tandis qu'une origine
absente ou microbienne conserve son statut de base incertain. Le diagnostic
affiche le statut de base, le statut effectif et la raison de la résolution.

L'évaluation porte sur la « compatibilité vegan selon les ingrédients
déclarés ». L'article 20 et l'annexe VII permettent dans certains cas
l'omission de constituants, auxiliaires, additifs de transfert, supports ou
détails d'ingrédients composés. Cette limite est signalée sans rendre chaque
résultat automatiquement incertain.

Le projet ne possède pas de moteur réglementaire complet des allergènes de
l'annexe II. Il ne tente donc pas d'interpréter ses exceptions, notamment
l'exception relative à l'acide béhénique d'une pureté minimale de 85 % utilisé
dans certains émulsifiants E470a, E471 et E477. Cette disposition ne crée
aucune règle vegan. Avant la 0.6, restent notamment à traiter la validation
juridique des exemptions, les exceptions détaillées de l'annexe II et une
gestion OCR plus riche ; elles resteront séparées du classement vegan.
