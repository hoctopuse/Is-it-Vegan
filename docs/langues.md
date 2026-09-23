# Segmentation des langues

La segmentation cherche des zones plausibles ; elle ne traduit rien et ne classe aucun ingrédient. Trois composants coopèrent : `LabelLexicon` décrit les titres, `LabelLanguageSegmenter` découpe et classe les blocs, puis `OcrBlockLanguageClassifier` confronte le titre au vocabulaire du contenu.

## Titres et marqueurs

`LabelLexicon.ingredientHeadings` reconnaît les familles suivantes, avec variantes singulier/pluriel et quelques formes OCR :

| Langue | Titres principaux |
|---|---|
| FR | `ingrédient`, `ingrédients`, `Sngredients`, `Ingrécients` |
| NL | `ingrediënt`, `ingrediënten`, `ingrediènten`, `ingredienten` |
| EN | `ingredient`, `ingredients` |
| DE | `zutat`, `zutaten` |
| ES | `ingrediente`, `ingredientes` |

Un titre peut être suivi de deux-points, d’un tiret ou d’un retour à la ligne. Un connecteur court permet des titres tels que « ingrédients de la sauce » ou « ingredients of the filling ». Les titres délimités peuvent apparaître au milieu d’une ligne ; c’est ce qui permet de couper `Ingredients: … Ingrédients: … Zutaten: …`.

Le segmentateur reconnaît aussi :

- noms de langues : Français, Nederlands, English, Deutsch, Español ;
- codes entre crochets : `[FR]`, `[NL]`, `[EN]`, `[DE]`, `[ES]` ;
- codes structurés FR, NL, EN, DE, IT, ES, PT, SE/SV, DK/DA, NO et FI ;
- combinaisons de marché telles que `FR/BE/LUX`, `BE-FR`, `NL-BE`, `EN-GB` et `SE DK NO`.

Les codes ne sont reconnus qu’en début de ligne ou après certains séparateurs (`.`, `;`, `|`) et avec une ponctuation de marqueur. `BE`, `CH`, `LU` et `LUX` enrichissent les `marketTags` d’un bloc FR ou NL ; ils ne deviennent pas des langues seuls. Le mot français « de » au milieu d’une phrase ne correspond donc pas au code allemand `DE`.

## Découpage

Tous les marqueurs et titres sont collectés, triés par position et dédupliqués lorsqu’ils se chevauchent. Chaque bloc s’étend de la fin de son marqueur jusqu’au marqueur suivant. Pour un titre d’ingrédients, le titre est conservé dans le bloc afin que l’extracteur de sections puisse le voir.

Sans marqueur fiable, un unique bloc `UNKNOWN` contient le texte complet avec `usedFallback = true`. Aucun contenu n’est supprimé.

## Attribution par le contenu

`OcrBlockLanguageClassifier` recherche le premier titre du bloc, puis compte les mots du contenu dans quatre petits vocabulaires : FR, EN, NL et DE. Les accents sont retirés uniquement pour ce comptage. Une langue de contenu gagne si elle possède au moins deux occurrences et strictement plus que la suivante.

Ainsi, `INGREDIENTS: eau, huile, sel, amidon` peut être corrigé de EN vers FR, tandis que `INGREDIENTS: water, coconut oil, salt` reste EN. Les titres distinctifs `Zutaten`, `Ingrediënten` et `Ingredientes` gardent leur langue même si le vocabulaire OCR est bruité. La langue suggérée par le titre et la langue normalisée sont conservées dans le diagnostic.

Cette logique a des limites explicites : un contenu court, spécialisé ou absent des quatre vocabulaires conserve la langue suggérée par son titre ou son marqueur. Le titre `Ingredients` est intrinsèquement ambigu entre un anglais correct et un français auquel l’OCR a retiré l’accent ; le contenu doit départager le cas.

## Qualité puis préférence

Chaque bloc reçoit : présence d’un titre, présence d’un séparateur, longueur utile et indice de troncature. Un bloc est manifestement tronqué s’il contient moins de huit caractères alphanumériques, se termine par un mot suivi d’un trait d’union ou présente un déséquilibre de parenthèses/crochets.

Le score de qualité additionne titre, séparateur, longueur utile, séparateurs de liste, fin cohérente et traces. La langue explicitement reconnue reste un critère affiché dans le diagnostic. Le score retire des points aux textes tronqués, de conservation ou marketing. Il est comparé avant la langue :

1. titre + séparateur + non tronqué ;
2. titre + non tronqué ;
3. titre, même tronqué ;
4. sans titre mais non tronqué ;
5. tronqué sans titre.

À score égal, l’ordre dépend de `UiLanguage` :

| Interface | Ordre prioritaire |
|---|---|
| FR | FR → EN → NL |
| EN | EN → FR → NL |
| NL | NL → EN → FR |

DE, IT, ES, PT, SE/SV, DK/DA, NO et FI suivent ces trois langues. La longueur utile puis l’ordre d’apparition départagent encore les égalités. Un bloc EN complet peut donc battre un bloc FR tronqué, même avec une interface française.

## Texte éditable et fallback

`OcrTextSelection` ne crée des options explicites que pour FR, EN, NL et DE, et seulement pour les blocs possédant un titre d’ingrédients. La première occurrence de chaque langue est conservée. Le bloc choisi devient le texte éditable ; l’option « Texte complet » reste disponible lorsque plusieurs blocs existent. Chaque bloc possède un identifiant stable, transmis au texte éditable, au diagnostic et à l’analyse afin qu’ils désignent le même contenu.

Si la segmentation utilise le fallback, le texte complet nettoyé est présenté. Changer de bloc remplace le texte éditable et vide les analyses précédentes. Le diagnostic conserve le marqueur brut, la langue finale, la correction éventuelle, les langues ignorées et la raison du choix.

Le moteur `VeganAnalyzer` segmente de nouveau son entrée. En mode étiquette ou OCR, il utilise le bloc retenu par le segmentateur. En mode liste manuelle, il cherche d’abord une section exploitable selon la préférence linguistique, puis prend la plus longue si nécessaire.
## Stabilisation 0.6.5.1

Les titres OCR limités `Ztaten`, `Ingediënten` et `ingredienti` sont reconnus uniquement lorsqu'un séparateur et une liste suffisamment structurée les suivent. Les identifiants de blocs dérivent du texte source normalisé, et non des offsets d'une segmentation ultérieure.
