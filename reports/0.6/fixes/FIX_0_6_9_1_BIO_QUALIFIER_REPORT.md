# Correction 0.6.9.1 — qualificatifs biologiques

Date : 2026-09-26  
Branche : `master`

## Problème observé

Des ingrédients connus devenaient inconnus lorsque leur désignation contenait
un qualificatif biologique (`bio`, `biologique`, `biologisch` ou `organic`).

## Correction

`OcrIngredientNormalizer.forMatching` retire désormais ces qualificatifs d’une
copie normalisée utilisée uniquement pour le matching. Les suffixes sont pris
en charge dans les quatre langues ; les formes préfixées séparées par un espace
ou un tiret sont également prises en charge (`organic wheat flour`,
`Bio-Weizenmehl`). Le texte source du token reste inchangé dans les diagnostics
et dans l’affichage.

Le matching réutilise toujours `IngredientMatcher` et les mêmes alias et règles
de verdict. Aucun changement n’a été apporté à `VerdictEngine`, `VeganAnalyzer`,
aux traces, au parser, à ML Kit ou à la base d’ingrédients.

## Tests ajoutés

`BioQualifierNormalizationTest` couvre les exemples français, néerlandais,
anglais et allemand, la conservation du texte original, les ingrédients
réellement inconnus, les ingrédients animaux et l’exclusion des traces.

## Validations

- `:mutation-core:test` : succès ;
- `testDebugUnitTest` : succès ;
- `assembleDebug` : succès ;
- `assembleDebugAndroidTest` : succès ;
- `git diff --check` : succès.

## Version

- `versionName` : `0.6.9` → `0.6.9.1` ;
- `versionCode` : `36` → `37`.

## Limites connues

Cette évolution ne traite pas les retours à la ligne OCR, les corrections OCR,
la consolidation multilingue ni les qualificatifs non listés. Le qualificatif
seul reste inconnu et ne rend aucun ingrédient vegan par défaut.

Aucun commit ni push n’a été effectué. Aucun asset, alias, `ingredients.json` ou
règle de verdict n’a été modifié.
