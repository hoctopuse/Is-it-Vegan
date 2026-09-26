# Correction 0.6.9.2 — retours à la ligne OCR

Date : 2026-09-26  
Branche : `master`

## Problème observé

Certains retours à la ligne OCR coupaient une expression connue, par exemple
`huile de` puis `colza`, ce qui produisait plusieurs ingrédients inconnus.
Le cas `glucose-` puis `fructose` perdait également la structure attendue
pendant le nettoyage du texte.

## Composants corrigés

- `LabelPreprocessor` reconnaît maintenant quelques expressions lexicales
  déterministes coupées par une ligne : huile de colza, farine de blé, sirop de
  glucose-fructose et acide citrique. Le cas huile de tournesol existant reste
  inchangé.
- `OcrTextCleaner` protège explicitement `glucose-\nfructose` afin de conserver
  le trait d’union avant le prétraitement.

La fusion est appliquée uniquement à ces expressions revues. Les retours à la
ligne entre deux ingrédients séparés par une virgule, entre langues, dans les
sections de traces, ou dans des textes non reconnus restent conservés.
Les sections déclarées, les traces et leurs frontières ne sont pas modifiées.

## Tests ajoutés

`OcrLineBreak0692Test` couvre les expressions françaises, le trait d’union,
les parenthèses continuées, les frontières par virgule, une liste multilingue,
la séparation des traces et le nettoyage OCR. Les tests existants du matching
bio sont également conservés et passent.

## Validations

- `:mutation-core:test` : succès ;
- `testDebugUnitTest` : succès ;
- `assembleDebug` : succès ;
- `assembleDebugAndroidTest` : succès ;
- `git diff --check` : succès.

Le verdict principal, `VerdictEngine`, `VeganAnalyzer`, le matching bio et le
traitement des traces n’ont pas été modifiés.

## Version

- `versionName` : `0.6.9.1` → `0.6.9.2` ;
- `versionCode` : `37` → `38`.

## Limites restantes

La correction ne réalise pas de fusion générale des lignes OCR, de correction
probabiliste, de consolidation multilingue ni de reconstruction fondée sur le
contexte visuel. Les expressions inconnues ou les frontières ambiguës restent
inchangées.

Aucun commit ni push n’a été effectué. Aucun asset, alias, `ingredients.json` ou
règle de verdict n’a été modifié.
