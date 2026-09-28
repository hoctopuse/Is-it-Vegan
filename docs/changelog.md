# Notes de version

Ces notes résument les capacités livrées. Elles ne remplacent ni le diagnostic d’une étiquette, ni les rapports d’audit et de correction à la racine du dépôt.

## 0.6.11.2

- Ajoute des notes localisées d’origines possibles aux additifs incertains documentés, sans modifier leurs statuts ni les verdicts ; les règles d’origine locales restent prioritaires.

## 0.6.11.1

- Qualifie E966 (lactitol) et E1105 (lysozyme) comme `VEGETARIAN` : leur origine laitière ou œuf est documentée par les spécifications UE et corroborée par des sources spécialisées ; les cas à origine variable restent `UNCERTAIN`.

## 0.6.11

- Qualifie E901 comme non vegan et E902, E903, E938, E941 et E948 comme vegan à partir des spécifications UE ; les autres origines non établies restent incertaines.

## 0.6.10

- Synchronise le référentiel UE des additifs depuis `knowledge/`, génère les assets Android et documente l’extraction de l’annexe II-B.

## 0.6.9.10

- Le référentiel local couvre les 338 lignes importables sur 340 de la référence officielle UE acquise pour l’annexe II-B, par leur numéro E et leurs dénominations françaises, néerlandaises, anglaises et allemandes disponibles.
- Les nouveaux additifs importés restent tous `UNCERTAIN` : l’autorisation UE et une dénomination réglementaire ne suffisent pas à conclure sur leur origine vegan.
- E345 et E345(i) restent hors de la base applicative, car la référence les conserve comme deux clés distinctes sans démontrer leur équivalence ni fournir quatre dénominations non ambiguës.
- E322a reste explicitement partiel (FR/NL/EN, sans nom DE officiel) ; la collision textuelle E470b/E572 est conservée avec des numéros E explicites distincts et un comportement prudent `UNCERTAIN`.

## 0.6.9.9

- Lorsque le verdict principal est `UNCERTAIN` ou `INCONCLUSIVE`, l’interface peut afficher avant les listes un résultat informatif fondé uniquement sur les ingrédients reconnus dont le statut effectif est établi.
- Cet encart conserve le verdict principal, laisse visibles les inconnus et les incertains, et ajoute une note indiquant qu’ils empêchent toujours la confirmation globale.
- Les traces, la présence réelle et les nœuds composites ne sont pas transformés en statut métier pour cet encart.

## 0.6.9.8

- Le rendu des vrais inconnus imbriqués conserve le contexte visuel parent → enfant.
- Ce contexte est uniquement une présentation : le parent composite ne devient pas un ingrédient classé et le verdict ne change pas.

## 0.6.9.7

- Les vrais inconnus restent visibles avec un verdict `UNCERTAIN`.
- Les listes d’inconnus visibles sont alignées entre l’interface, les diagnostics structurés et le rapport texte.
- Une correspondance contextuelle effectivement vegan n’est plus présentée comme inconnue dans les vues agrégées; sa trace détaillée reste disponible.

## 0.6.9.6

- La présentation des inconnus devient une liste d’occurrences lisible, sans perdre leurs répétitions ni leur contexte de composition.

## 0.6.9.5

- Une composition sans titre explicite peut être analysée seulement lorsque des indices structurels suffisamment nombreux la confirment.
- Les textes marketing, nutritionnels, trop courts ou composés uniquement de traces restent sans liste d’ingrédients exploitable.

## 0.6.9.4

- L’état `NO_INGREDIENT_LIST` est explicite lorsqu’aucune liste d’ingrédients exploitable n’est reconnue.
- Une déclaration autonome de présence réelle reste distincte d’une composition complète et des traces.

## 0.6.9.3

- Le rapport de diagnostic stabilise la représentation des sections, des tokens, des inconnus, des traces et des raisons de décision.

## 0.6.9.2

- Le prétraitement OCR gère de manière bornée des retours à la ligne observés dans les expressions d’ingrédients.
- Les corrections restent limitées et ne réécrivent ni les traces ni le texte brut OCR.

## 0.6.9.1

- La normalisation des qualificatifs biologiques est isolée du matching des ingrédients et des traces.

## 0.6.9

- L’explication structurée « hors ingrédients incertains » est ajoutée au diagnostic et à l’interface.
- Elle réutilise le moteur de verdict, ne modifie jamais le verdict principal et reste absente lorsqu’un vrai inconnu ou un bloqueur connu demeure.
- Les occurrences incertaines, leurs chemins dans la composition et les traces séparées sont conservés.

## Décision d’architecture postérieure

L’audit `COMPOSITE_STATUS_PROPAGATION_AUDIT_0_6_9_9.md`, conservé à la racine du dépôt, documente une décision de conception, pas une fonctionnalité utilisateur : les statuts des enfants ne sont pas propagés vers les nœuds `COMPOSITE`. Le verdict global agrège déjà les feuilles et additifs via `VerdictEngine`.

## Non livré

La consolidation de plusieurs langues d’une même étiquette, l’usage des blocs multilingues comme aide de validation enrichie et une suggestion automatique de reprendre une photo plus large restent des travaux futurs. Aucune de ces capacités n’est annoncée comme disponible.
