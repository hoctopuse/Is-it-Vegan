# Correction 0.6.9.3 — cohérence des diagnostics

Date : 2026-09-26  
Branche : `master`  
Version : `0.6.9.3` / `versionCode 39`

## Incohérences observées

Des correspondances contextuelles valides comme `poivron rouge` et `tomate
séchée au soleil` étaient classifiées vegan mais restaient ajoutées aux
inconnus. Les exports utilisaient aussi la virgule pour séparer des listes dont
les valeurs pouvaient elles-mêmes contenir des virgules.

## Corrections implémentées

Les qualificatifs déterministes `rouge`, `sèche au soleil` et `séchée au
soleil` sont désormais traités comme des restes lexicaux couverts par une
correspondance connue. Ils ne produisent donc plus d’inconnu. Les résidus
arbitraires, par exemple `mystère au lait`, restent inconnus et continuent de
bloquer la conclusion vegan.

Les listes structurées du rapport détaillé et de l’explication affichée
utilisent maintenant ` | `. Les virgules internes aux ingrédients et aux
sous-compositions sont conservées. Un `|` fourni par une donnée est échappé en
`\|` dans ces listes afin de ne pas casser leur séparation. Les textes OCR brut
et éditable ne sont pas modifiés.

Les listes de vegan, végétariens non vegan, non vegan, incertains, bloqueurs,
inconnus, responsables du verdict, langues, blocs, traces et notes suivent ce
format lorsqu’elles sont produites par les formatters concernés. Les traces
restent exclues du verdict et des ingrédients analysés.

## Tests ajoutés ou adaptés

`DiagnosticOutput093Test` couvre les correspondances contextuelles, un terme
réellement inconnu, la composition réaliste issue des exports OCR, les traces,
les listes structurées, les virgules internes et l’échappement de `|`.
Les assertions historiques de rapports ont été adaptées au nouveau séparateur.

## Validations

- `:mutation-core:test` : succès ;
- `testDebugUnitTest` : succès ;
- `assembleDebug` : succès ;
- `assembleDebugAndroidTest` : succès ;
- `git diff --check` : succès ;
- tests connectés : non exécutés, `adb` n’est pas disponible dans l’environnement.

Les règles de `VerdictEngine`, `VeganAnalyzer`, les statuts métier, les alias,
les assets et `ingredients.json` n’ont pas été modifiés.

## Limites conservées

Cette version ne traite pas la consolidation multilingue, le choix automatique
de langue, les scores OCR, la fusion de blocs, les corrections OCR générales,
ni l’enrichissement de la base. Ces sujets restent reportés à la 0.7.

Aucun commit ni push n’a été effectué.
