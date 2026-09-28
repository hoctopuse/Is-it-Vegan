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

La prochaine étape est une classification documentée des entrées `UNCERTAIN`, avec une source d’origine, une raison et des tests adaptés à chaque décision. Elle nécessitera une revue éditoriale séparée ; aucune classification automatique ne sera déduite du numéro E, du nom réglementaire ou de la seule présence dans la liste UE.
