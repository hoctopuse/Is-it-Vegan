# Diagnostic

Le projet produit deux rapports texte complémentaires : un rapport d’analyse (`DiagnosticReport`) et un export de session OCR (`OcrExportReport`). Ils sont déclenchés par l’utilisateur via un `Intent.ACTION_SEND`.

## Diagnostic d’analyse

`DiagnosticReport.build` contient les sections suivantes :

| Section | Informations utiles |
|---|---|
| En-tête | version, mode d’entrée, disponibilité, fallback manuel et portée |
| Entrée | texte exact soumis à `VeganAnalyzer` |
| Langue/bloc | identifiant, langues originale/normalisée, score, critères, longueur, troncature, corrections et fallback |
| Sections | titre, séparateur, présence réelle, traces et frontière détectée |
| Composition | texte après `LabelPreprocessor` et présence réelle séparée |
| Traces/notes | avertissements de contamination et notes exclues |
| Étapes | un enregistrement par nœud/token |
| Règles d’origine | validité ou erreurs de chargement JSON |
| Résultat | compatibilité vegan, classification détaillée, verdict sans incertains, bloqueurs, connus et inconnus |

Pour chaque token, regarder en priorité :

- `profondeur` et `parent` pour vérifier l’arbre ;
- `texte matcher` lorsqu’une expression protégée est remplacée par un alias contrôlé ;
- `correspondances`, `correspondance couverte` ou `contextuelle` ;
- `conflits bloqués` lorsqu’un alias animal court n’a volontairement pas été retenu ;
- `inconnu`, qui conserve la feuille complète ;
- `classification de base` et `classification effective` pour une règle d’origine.

Un conteneur composite affiche « conteneur analysé » et aucun inconnu propre. Il faut inspecter ses enfants pour comprendre le résultat.

## Diagnostic OCR

En 0.6.8, le rapport d’analyse expose aussi l’équilibre des parenthèses, le nombre de fermetures manquantes ou inattendues, la profondeur maximale et l’application éventuelle de la récupération conservatrice. Les corrections OCR listées concernent uniquement la composition extraite ; elles ne réécrivent ni les traces, ni la conservation, ni les instructions. Les traces normalisées restent affichées séparément et sont exclues du verdict.

## Matching multilingue 0.6.6

Pour chaque alias linguistique retenu, le diagnostic d’analyse affiche le texte OCR, la correction éventuelle, l’alias, le concept canonique, sa disponibilité dans `ingredients.json` et la langue de matching. Un concept proposé mais absent est affiché comme non classifiable et reste inconnu. L’identifiant canonique ne porte aucun statut : la classification continue de provenir exclusivement de `ingredients.json`.

`OcrDiagnostics` et `OcrExportReport` exposent :

- rotation EXIF transmise à ML Kit ;
- nombre de blocs géométriques ML Kit, de segments linguistiques et de lignes ;
- zones détectées ;
- identifiant, marqueur brut, langues originale et normalisée, score et critères de chaque bloc ;
- langue sélectionnée et ordre de préférence ;
- raison du choix ;
- avertissements d’orientation, d’ordre ou de fallback.

L’orientation affichée vaut 0 degré pour un bitmap de recadrage déjà orienté. Pour une image entière, elle correspond à la rotation donnée à `InputImage`. Une valeur indéterminée signifie que l’EXIF n’a pas fourni de rotation exploitable.

L’avertissement sur l’ordre natif signifie que la reconstruction issue des rectangles différait de `Text.text`. Le pipeline a alors gardé le texte natif de ML Kit, conformément au comportement de la version 0.6.5.

## Texte brut, éditable et instantanés

L’export OCR place dans des sections distinctes :

1. le texte brut original ML Kit ;
2. le texte éditable actuel ;
3. chaque analyse avec le texte exact soumis, son résultat affiché et son diagnostic détaillé.

Si le texte actuel diffère d’un instantané, une section `ÉTAT` l’indique. L’image et son contenu binaire ne sont jamais insérés dans ce rapport.

L’identifiant `block-*` désigne la source sélectionnée et reste transmis à l’analyse, même après une édition du texte. L’identifiant secondaire `segment-*` décrit uniquement les offsets de la segmentation courante. Les deux ne sont jamais présentés comme un même comptage géométrique ML Kit.

## Méthode de débogage

Pour un résultat surprenant, suivre cet ordre :

1. vérifier le texte brut pour distinguer erreur ML Kit et post-traitement ;
2. vérifier le bloc et la langue retenus ;
3. vérifier `ingredientsText`, les traces et les sections ignorées ;
4. vérifier l’arbre et la profondeur des tokens ;
5. vérifier le type de correspondance et le résidu inconnu ;
6. comparer statuts de base/effectifs ;
7. appliquer la table de priorité du verdict.

Ce parcours localise généralement l’erreur dans une seule frontière : OCR, segmentation, extraction, parsing, base ou agrégation.
