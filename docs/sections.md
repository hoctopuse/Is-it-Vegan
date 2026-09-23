# Extraction des sections

`LabelSectionExtractor` reçoit un `LanguageBlock` ou une paire langue/texte. Son rôle est de borner le contenu utile avant le parsing. Il ne consulte pas la base d’ingrédients et ne décide aucun verdict.

## Modèle produit

`LabelSections` expose :

- `ingredientsText` : composition située après un titre reconnu ;
- `declaredContainsText` : présence réelle déclarée par « contient », `contains`, `bevat`, `enthält` ou `contiene` ;
- `tracesText` : avertissements de contamination croisée ;
- `ignoredSections` : sections non alimentaires ou bornes de fin ;
- le texte brut du bloc, la langue et les métadonnées du titre.

## Marqueurs et profondeur

Les marqueurs sont cherchés dans le texte, puis filtrés avec `depthAt`. Seuls ceux situés à profondeur zéro, hors parenthèses et crochets, peuvent ouvrir ou fermer une section. Une sous-composition comme `épices (contient : moutarde)` n’interrompt donc pas la liste principale.

À position identique, les traces ont priorité sur les titres, puis la présence réelle et les sections ignorées. Cette règle évite que le mot « contient » inclus dans « peut contenir » soit interprété comme une présence réelle.

## Composition et présence réelle

Après un titre d’ingrédients, la composition s’arrête au prochain titre d’ingrédients, à une trace, à une présence réelle ou à une section ignorée. Si aucun titre n’existe, le texte précédant une trace ou une section ignorée reste disponible pour le mode manuel ; les modes étiquette et OCR exigent toutefois un vrai titre avant de l’analyser.

Une section `contient : lait` est parsée séparément. Les petits déterminants français initiaux (`du`, `des`, `de la`, `de l’`) sont retirés avant parsing. Ses ingrédients reconnus contribuent au résultat et leurs identifiants sont ajoutés à `declaredPresenceIngredientIds`.

## Traces

Les préfixes couvrent notamment :

- « peut contenir [des traces de] » et « traces éventuelles de » ;
- `may contain [traces of]` ;
- `kan bevatten` et la forme néerlandaise avec « sporen van … bevatten » ;
- `kann enthalten` et la forme allemande avec « Spuren von … enthalten » ;
- `puede contener [trazas de]`.

Une trace s’étend jusqu’à une section ignorée ou au prochain titre d’ingrédients. `LabelPreprocessor` possède en plus une détection de « fabriqué dans un atelier ». Les avertissements sont dédupliqués en conservant leur ordre.

Ils ne sont jamais envoyés à `IngredientTreeParser`, `IngredientMatcher`, `UnknownCollector` ou `VerdictEngine`. Une étiquette composée uniquement d’une trace produit donc `NO_INGREDIENT_LIST`, aucun ingrédient reconnu et aucun verdict détaillé.

## Sections ignorées

Les bornes couvrent des familles visibles dans le code :

- valeurs ou déclaration nutritionnelles ;
- préparation et mode d’emploi ;
- conservation et après ouverture ;
- dates de durabilité ;
- lot et quantité nette ;
- fabricant, distributeur, origine et importateur ;
- certification ;
- « open here » / « ouvrir ici ».

Les expressions existent dans plusieurs langues, surtout FR, EN, NL, DE et ES. Certaines nécessitent un deux-points ; les bornes de conservation/date peuvent aussi commencer une ligne ou suivre une fin de phrase.

## Prétraitement après extraction

`LabelPreprocessor` traite séparément la composition et la présence réelle. Il :

- remplace les espaces insécables ;
- extrait les traces encore présentes dans une saisie manuelle ;
- extrait certaines notes : allergènes, Rainforest Alliance, agriculture biologique, œufs de poules élevées au sol et `^concentré` ;
- rattache les lignes qui prolongent une note ;
- retire les appels de note attachés lorsqu’un pied de page correspondant existe ;
- applique une seule correction OCR lexicale historique et bornée : `formage grana` vers `fromage grana`.

Les classes fonctionnelles et les pourcentages restent dans `compositionText` pour que le parseur conserve leur structure. `QuantityCleaner` existe et est testé comme utilitaire, mais le chemin de production de `VeganAnalyzer` ne l’appelle pas en 0.6.4.1.

## Limites

La détection repose sur des expressions régulières et des profondeurs équilibrées. Une ponctuation OCR très dégradée ou une parenthèse non fermée peut déplacer une frontière. Les notes et sections ignorées sont une liste fermée ; un slogan inconnu peut donc rester dans le texte et devenir un inconnu plutôt que d’être supprimé arbitrairement.

