# Référentiel des additifs UE

La version 0.6.9.10 complète la base applicative avec une référence réglementaire multilingue acquise localement. Elle améliore la reconnaissance des numéros E et des dénominations présentes sur les étiquettes ; elle ne transforme pas une autorisation réglementaire en preuve de compatibilité vegan.

## Sources de référence

La source réglementaire est le règlement (CE) n° 1333/2008, CELEX `02008R1333`, dans les textes consolidés au **18.08.2026**. Le dépôt conserve les quatre PDF utilisés pour la vérification :

- [PDF officiel français](https://github.com/hoctopuse/Is-it-Vegan/blob/master/reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_FR_TXT.pdf) ;
- [PDF officiel néerlandais](https://github.com/hoctopuse/Is-it-Vegan/blob/master/reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_NL_TXT.pdf) ;
- [PDF officiel anglais](https://github.com/hoctopuse/Is-it-Vegan/blob/master/reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_EN_TXT.pdf) ;
- [PDF officiel allemand](https://github.com/hoctopuse/Is-it-Vegan/blob/master/reference-input/eu-food-labelling/03-food-additives/CELEX_02008R1333-20260818_DE_TXT.pdf).

Le [CSV de référence multilingue](https://github.com/hoctopuse/Is-it-Vegan/blob/master/reference-input/eu-food-labelling/03-food-additives/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv) rassemble les 340 numéros E réglementaires retenus, les quatre dénominations disponibles, leur état de vérification et la provenance CELEX. Le [rapport d’acquisition](https://github.com/hoctopuse/Is-it-Vegan/blob/master/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_ACQUISITION_REPORT.md) décrit la constitution de cette référence ; le [rapport de complétion 0.6.9.10](https://github.com/hoctopuse/Is-it-Vegan/blob/master/EU_ADDITIVES_REFERENCE_0_6_9_10_COMPLETION_REPORT.md) décrit son import applicatif et ses limites.

Ces liens pointent vers les fichiers de provenance du dépôt. Les PDF et le CSV ne sont pas des assets Android et ne sont pas chargés à l’exécution.

La méthode de lecture de l’annexe II-B, la jointure exacte des numéros E, les exceptions `E345` / `E345(i)` et le cas structurel `E960b` sont documentés dans [Extraction de l’annexe II-B](sources/eu-additives-annex-ii-b.md).

## Ce que la base reconnaît

Les 338 lignes importables du CSV sont couvertes dans `app/src/main/assets/ingredients.json` : 250 nouvelles entrées ont été ajoutées et les 88 entrées déjà présentes ont conservé leurs classifications, raisons, sources, identifiants et alias métier. Les nouvelles entrées ont toutes le statut `UNCERTAIN`.

La base distingue deux questions :

1. **Reconnaissance** : un numéro E ou une dénomination officielle peut être associé à une entrée connue ;
2. **Compatibilité vegan** : le statut dépend des données d’origine et des règles documentées, pas de l’autorisation UE.

Ainsi, un additif reconnu dans cette référence peut rester `UNCERTAIN`. Cette prudence est volontaire : la réglementation décrit l’autorisation et les dénominations, mais ne documente pas nécessairement la matière première ni le procédé de fabrication. Les traces restent hors de la composition et hors du verdict.

Les numéros E utilisent le champ `eNumber` du schéma existant. Les formes `E330` et `E 330` sont reconnues ; un nombre nu comme `330` ne l’est pas. Les suffixes réglementaires sont conservés exactement, par exemple `E160b(i)`, `E322a`, `E960a` et `E960b`.

## Couverture et exceptions

La couverture est de **338/340** lignes :

- **E322a** est importé avec ses dénominations officielles FR, NL et EN. Le CSV indique qu’aucune dénomination DE officielle n’a été trouvée ; aucun nom allemand n’est inventé ;
- **E345** et **E345(i)** restent hors de la base. Le CSV les conserve comme deux clés distinctes et ne démontre pas une équivalence permettant une importation non ambiguë ;
- les autres lignes importables sont couvertes par un numéro E exact. Une ligne complète n’est pas pour autant une preuve de caractère vegan.

### Collision E470b / E572

Les quatre dénominations officielles de E470b se normalisent comme des alias historiques de E572. Les deux entrées sont `UNCERTAIN`.

Le matcher normalise les alias, les trie par longueur, puis couvre chaque portion de texte une seule fois. À position et longueur égales, l’ordre stable de la base fait actuellement retenir E572 pour le libellé commun seul. Ce choix ne produit pas de statut vegan ou non vegan : le résultat reste `UNCERTAIN`.

La présence d’un numéro explicite rétablit la distinction : `E572` sélectionne E572 ; `E470b` ajoute E470b à la correspondance du libellé commun. Les tests de sécurité vérifient les quatre dénominations communes dans ces trois formes. Aucun alias historique n’est réaffecté pour masquer cette collision.

## Suite prévue

## Qualification d’origine 0.6.11

La qualification ne part jamais de l’autorisation UE seule. Une classification globale n’est modifiée que lorsque la spécification primaire définit directement une matière animale, végétale ou élémentaire. Ainsi, E901 est non vegan car la spécification définit une cire provenant des rayons construits par l’abeille ; E902 et E903 sont vegan car elles sont définies comme cires de feuilles végétales ; E938, E941 et E948 sont des gaz élémentaires spécifiés.

La revue 0.6.11.1 ajoute E966 et E1105 comme `VEGETARIAN`. Le lactitol est défini comme obtenu par hydrogénation du lactose et le lysozyme comme obtenu de blanc d’œuf de poule : leur origine animale est établie, mais elle correspond à la définition végétarienne existante et ne permet pas un verdict vegan. Les sources spécialisées confirment ces deux cas, tandis que la spécification UE établit l’identité de la matière première.

Les additifs dont la matière première, le procédé ou le fournisseur peut varier restent `UNCERTAIN`, notamment E322, E422, E470a/E470b, E471, E472a–E472f, E570, E572, E627, E631, E635, E640 et E920. Une précision d’origine localement attachée, par exemple `lécithines (soja)`, est traitée par les règles d’origine existantes et ne reclassifie pas l’additif général. Les détails, les sources et les limites des deux revues figurent dans `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_REPORT.md` et `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_1_REPORT.md`.

## Notes d’origines possibles 0.6.11.2

Les concepts `UNCERTAIN` documentés peuvent porter une note structurée d’origines possibles. Elle est rendue dans les quatre langues de l’application et indique les alternatives attestées ainsi que la condition de variabilité. Elle informe sans modifier le statut ni le verdict. Elle n’est montrée que pour une occurrence restant incertaine : une règle locale explicite, telle que `lécithines (soja)`, garde priorité et ne reçoit donc pas la note générique. La note couvre E322, E422, E470a, E470b, E471, E572, E627, E631, E635 et E640. Les autres entrées incertaines restent sans note lorsqu’aucune origine variable assez précise n’est documentée.

La prochaine étape est une classification documentée des entrées `UNCERTAIN`, avec une source d’origine, une raison et des tests adaptés à chaque décision. Elle nécessitera une revue éditoriale séparée ; aucune classification automatique ne sera déduite du numéro E, du nom réglementaire ou de la seule présence dans la liste UE.
