# Extraction des sections

`LabelSectionExtractor` reçoit un `LanguageBlock` ou une paire langue/texte. Son rôle est de borner le contenu utile avant le parsing. Il ne consulte pas la base d’ingrédients et ne décide aucun verdict.

## Modèle produit

`LabelSections` expose :

- `ingredientsText` : composition située après un titre reconnu ou retenue sans titre après contrôle structurel ;
- `declaredContainsText` : présence réelle déclarée par « contient », `contains`, `bevat`, `enthält` ou `contiene` ;
- `tracesText` : avertissements de contamination croisée ;
- `ingredientSection` : limites début inclusif/fin exclusive et texte brut de la composition ;
- `traceSections` : limites et textes brut/normalisé de chaque avertissement de traces ;
- `ignoredSections` : sections non alimentaires ou bornes de fin ;
- le texte brut du bloc, la langue et les métadonnées du titre.

## Marqueurs et profondeur

Les marqueurs sont cherchés dans le texte, puis filtrés avec `depthAt`. Seuls ceux situés à profondeur zéro, hors parenthèses et crochets, peuvent ouvrir ou fermer une section. Une sous-composition comme `épices (contient : moutarde)` n’interrompt donc pas la liste principale.

À position identique, les traces ont priorité sur les titres, puis la présence réelle et les sections ignorées. Cette règle évite que le mot « contient » inclus dans « peut contenir » soit interprété comme une présence réelle.

## Composition et présence réelle

Après un titre d’ingrédients, la composition s’arrête au prochain titre d’ingrédients, à une trace, à une présence réelle ou à une section ignorée. Sans titre, les modes étiquette et OCR n’acceptent une composition que si plusieurs indices indépendants convergent : au moins trois séparateurs de premier niveau, quatre éléments compatibles avec le parseur, deux termes alimentaires bornés et un indice supplémentaire (pourcentage, structure équilibrée ou séquence dense). Les textes nutritionnels, marketing, courts ou limités aux traces restent `NO_INGREDIENT_LIST`.

Une section `contient : lait` est parsée séparément. Les petits déterminants français initiaux (`du`, `des`, `de la`, `de l’`) sont retirés avant parsing. Ses ingrédients reconnus contribuent au résultat et leurs identifiants sont ajoutés à `declaredPresenceIngredientIds`.

## Traces

Les préfixes couvrent notamment :

- « peut contenir [des traces de] » et « traces éventuelles de » ;
- `may contain [traces of]` ;
- `kan bevatten` et la forme néerlandaise avec « sporen van … bevatten » ;
- `kann enthalten` et la forme allemande avec « Spuren von … enthalten » ;
- `puede contener [trazas de]`.

Les formes OCR bornées `Pet conterir`, `Peut conterir`, `Peut conteir` et `Peut conteuir` sont reconnues en français. Les formes NL et DE admettent quelques mots entre `kan`/`kann` et `bevatten`/`enthalten`, ce qui conserve les listes de traces déformées sans analyser leurs allergènes.

Une trace s’étend sur plusieurs lignes si nécessaire. Une phrase d’allergènes terminée par `.`, `!` ou `?` est sa borne prioritaire ; sinon elle s’arrête à une section ignorée ou au prochain titre d’ingrédients. `LabelPreprocessor` possède en plus une détection de « fabriqué dans un atelier ». Les avertissements sont dédupliqués en conservant leur ordre.

Ils ne sont jamais envoyés à `IngredientTreeParser`, `IngredientMatcher`, `UnknownCollector` ou `VerdictEngine`. Une étiquette composée uniquement d’une trace produit donc `NO_INGREDIENT_LIST`, aucun ingrédient reconnu et aucun verdict détaillé.

## Sections ignorées

Les bornes couvrent des familles visibles dans le code :

- valeurs ou déclaration nutritionnelles ;
- préparation et mode d’emploi ;
- conservation, après ouverture et dates de durabilité (`A conserver`, `Bewaring`, `Aufbewahrung`, `Nach dem Öffnen`, `Best before`) ;
- dates de durabilité ;
- lot et quantité nette ;
- fabricant, distributeur, origine et importateur ;
- certification et marketing (`Rainforest Alliance`, `ra.org`, `Mehr unter`, `certifié`, `zertifiziert`, `gecertificeerd`) ;
- « open here » / « ouvrir ici ».

Les expressions existent dans plusieurs langues, surtout FR, EN, NL, DE et ES. Certaines nécessitent un deux-points ; les bornes de conservation/date peuvent aussi commencer une ligne ou suivre une fin de phrase.

## Prétraitement après extraction

`LabelPreprocessor` traite séparément la composition et la présence réelle. Il :

- remplace les espaces insécables ;
- extrait les traces encore présentes dans une saisie manuelle ;
- extrait certaines notes : allergènes, Rainforest Alliance, agriculture biologique, œufs de poules élevées au sol et `^concentré` ;
- rattache les lignes qui prolongent une note ;
- retire les appels de note attachés lorsqu’un pied de page correspondant existe ;
- applique des corrections OCR bornées de composition telles que `powron`, `olves nores`, `huie đove`, `doutble oncentré`, `ognon` et `toumesol`. Elles ne s’appliquent jamais au texte OCR brut.

Les classes fonctionnelles et les pourcentages restent dans `compositionText` pour que le parseur conserve leur structure. `QuantityCleaner` existe et est testé comme utilitaire, mais le chemin de production de `VeganAnalyzer` ne l’appelle pas en 0.6.5.

## Limites

La détection repose sur des expressions régulières et des profondeurs équilibrées. Une ponctuation OCR très dégradée ou une parenthèse non fermée peut déplacer une frontière. Les notes et sections ignorées sont une liste fermée ; un slogan inconnu peut donc rester dans le texte et devenir un inconnu plutôt que d’être supprimé arbitrairement.
## Stabilisation 0.6.5.1

Les marqueurs de traces français composés, par exemple `Peut contenir : des traces éventuelles de :`, sont consommés comme un seul préfixe. Les occurrences qui se chevauchent et les lignes identiques sont écartées avant l'analyse. Une section de traces porte sa frontière et son texte dans une même structure ; elle n'est jamais analysée comme une composition.
