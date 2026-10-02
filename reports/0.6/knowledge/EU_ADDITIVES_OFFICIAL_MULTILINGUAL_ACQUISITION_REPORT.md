# Acquisition du référentiel officiel multilingue UE des additifs

Date : 2026-09-27  
Branche : `master`  
Source recalculée : `reference-input/eu-food-labelling/03-food-additives/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv`

## Sources réglementaires

Le CSV provient des quatre éditions PDF EUR-Lex du règlement (CE) n° 1333/2008, CELEX `02008R1333`, consolidation `18.08.2026`, référence `066.001` : FR, NL, EN et DE. Les URL des éditions et la base des additifs de la Commission sont conservées dans chaque ligne.

## Validation du CSV

- Encodage lu : UTF-8.
- Les 16 en-têtes attendus sont présents et dans l’ordre prévu.
- Lignes de données : 340 ; `e_number` distincts : 340.

| Statut | Nombre |
|---|---:|
| `COMPLETE_4_LANGUAGES` | 337 |
| `PARTIAL_OFFICIAL` | 1 |
| `AMBIGUOUS_GROUP_OR_RANGE` | 2 |

La seule catégorie portée par le CSV est `Annex II Part B` pour les 340 lignes, dans la section `II-B`. Le fichier ne fournit pas de ventilation plus fine par famille de l’annexe I ; aucune n’est déduite ici.

## Écarts documentés

- `E960b` a été reconstruite dans le PDF FR comme continuation structurelle immédiatement après `E960a`, avec la note `structural_continuation_recovered`. Le libellé de `E960a` n’inclut plus le fragment `960b`.
- `E345` et `E345(i)` restent deux clés distinctes, toutes deux `AMBIGUOUS_GROUP_OR_RANGE` : les PDF ne démontrent pas explicitement leur équivalence.
- `E322a` est `PARTIAL_OFFICIAL` : FR, NL et EN sont attestés ; DE est vide avec `Absent from official PDF: DE`.

| Clé | FR | NL | EN | DE | Statut |
|---|---|---|---|---|---|
| `E160b(i)` | Bixine de rocou | Annatto bixine | Annatto bixin | Annatto Bixin | `COMPLETE_4_LANGUAGES` |
| `E322a` | Lécithine d’avoine | Haverlecithine | Oat lecithin | vide | `PARTIAL_OFFICIAL` |
| `E960a` | Glycosides de stéviol issus de Stevia | Steviolglycosiden uit Stevia | Steviol glycosides from Stevia | Steviolglycoside aus Stevia | `COMPLETE_4_LANGUAGES` |
| `E960b` | Glycosides de stéviol produits par fermentation | Steviolglycosiden uit fermentatie | Steviol glycosides from fermentation | Steviolglycoside aus Fermentation | `COMPLETE_4_LANGUAGES` |

## Conclusion

Le CSV est une source fiable pour préparer un futur import mécanique : les clés restent distinctes et les écarts sont explicitement marqués. Il ne contient aucun statut vegan, ne crée aucun alias applicatif et ne déclenche aucune modification de `ingredients.json`.

## Fichier modifié

- `EU_ADDITIVES_OFFICIAL_MULTILINGUAL_ACQUISITION_REPORT.md`
