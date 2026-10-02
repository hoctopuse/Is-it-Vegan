# Qualification d’origine des additifs — 0.6.11

## Décision

**GO.** La qualification est volontairement progressive : seules six entrées disposent d’une définition primaire assez précise pour une classification globale. La version 0.6.11 est permise après les validations listées ci-dessous.

## Audit et tableau de travail

317 additifs portant un numéro E étaient `UNCERTAIN` au début de la revue. Les règles actives d’origine couvrent déjà E322 et E471 seulement lorsque l’origine est syntaxiquement attachée ; les règles E422, E626–E635 et E640 restent en revue et ne modifient aucun statut global.

| Numéro E | ID | Statut initial | Décision | Statut final | Confiance | Source et raison |
|---|---|---|---|---|---|---|
| E901 | e901 | UNCERTAIN | À classer | NON_VEGAN | Élevée | La spécification UE le définit comme cire issue des rayons fabriqués par *Apis mellifera*. |
| E902 | e902 | UNCERTAIN | À classer | VEGAN | Élevée | Cire purifiée obtenue des feuilles de *Euphorbia antisyphilitica*. |
| E903 | e903 | UNCERTAIN | À classer | VEGAN | Élevée | Cire purifiée obtenue des feuilles et bourgeons de *Copernicia cerifera*. |
| E938 | e938 | UNCERTAIN | À classer | VEGAN | Élevée | Argon élémentaire avec formule et pureté réglementaires. |
| E941 | e941 | UNCERTAIN | À classer | VEGAN | Élevée | Azote élémentaire N₂ avec pureté réglementaire. |
| E948 | e948 | UNCERTAIN | À classer | VEGAN | Élevée | Oxygène élémentaire avec pureté réglementaire. |
| 311 autres E incertains | concepts distincts | UNCERTAIN | À conserver UNCERTAIN / À vérifier | UNCERTAIN | Insuffisante ou variable | Origine, procédé, matière première ou fournisseur non établis globalement. |

Décomptes après revue : **5 VEGAN**, **0 VEGETARIAN**, **1 NON_VEGAN**, **311 UNCERTAIN** parmi les 317 examinés.

## Sources et méthode

La source ajoutée est `eu-reg-231-2012-specifications` dans `knowledge/sources.json` : règlement (UE) n° 231/2012 de la Commission, version consolidée du 01.09.2022, publié par la Commission européenne en anglais. Son rôle est strictement l’identité définie des six additifs listés ; sa limite est explicite : il ne constitue pas une certification vegan ni une preuve pour les additifs à origine variable.

La hiérarchie appliquée est : spécification ou texte réglementaire primaire, source spécialisée, documentation fournisseur, publication technique identifiée, puis source secondaire uniquement comme piste. L’autorisation UE sans définition d’origine n’a déclenché aucune classification.

## Cas conservés et règles conditionnelles

E322, E471, E422, E570, E572, E470b, E627, E631, E635, E920 et les autres cas variables restent `UNCERTAIN`. E470b et E572 restent des concepts distincts ; E960a et E960b aussi. E345 et E345(i) restent absents de la base applicative.

Aucune règle d’origine n’a été ajoutée ou activée dans cette version. Les règles existantes continuent de résoudre uniquement une origine explicitement attachée : par exemple, une lécithine de soja peut devenir vegan dans cette occurrence sans reclassifier E322. Sans qualificatif local, le statut général reste celui de la base. Les traces restent hors verdict.

## Impact, limites et validations

Un libellé contenant E901 produit maintenant un verdict non végétarien ; un libellé ne contenant que E902, E903, E938, E941 ou E948 est vegan. Les autres nouveaux numéros E non documentés restent incertains.

Limite principale : la revue ne déduit pas l’origine à partir du nom commercial, de la seule fonction technologique ou de l’autorisation. Les 311 cas conservés exigent une preuve additive et, lorsque nécessaire, une preuve du procédé ou du fournisseur.

Fichiers modifiés : `knowledge/ingredients.json`, `knowledge/sources.json`, les assets régénérés, `EuAdditivesReference06910Test.kt`, `docs/additifs.md`, ce rapport et la version Android.

Validations prévues et exécutées avant la décision : contrôles des trois générateurs, tests JVM `:mutation-core:test` et `testDebugUnitTest`, assemblages debug, contrôle `git diff --check` et tests connectés si un appareil est disponible.
