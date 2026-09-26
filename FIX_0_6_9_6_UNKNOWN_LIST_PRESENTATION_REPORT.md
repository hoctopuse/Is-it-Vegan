# Correctif 0.6.9.6 — présentation des ingrédients non identifiés

Date : 2026-09-26  
Branche : `master`

## Problème initial

L’écran utilisait une chaîne aplatie séparée par `|` pour les inconnus. Chaque entrée répétait la même explication et le lien parent/enfant des sous-compositions disparaissait.

## Rendu corrigé

Le formatter commun des deux parcours Compose produit désormais, uniquement pour un verdict `INCONCLUSIVE` :

- une puce par occurrence simple ;
- un parent composite affiché une seule fois, avec son pourcentage disponible ;
- les enfants inconnus sous ce parent, avec leur propre pourcentage disponible ;
- une unique explication générale après la liste.

Les occurrences sont prises dans `AnalysisDiagnostics.tokens`, avec leurs `order`, `parentOrder` et quantités. Aucun contexte n’est reconstruit depuis le texte affiché. Les ingrédients reconnus ne sont pas ajoutés à la liste. Les séparateurs `|` restent inchangés dans les exports et diagnostics structurés, mais ne sont plus utilisés dans le rendu utilisateur concerné.

Les traces restent dans leur section dédiée, affichées sur des lignes séparées, et restent exclues du verdict comme du résultat conditionnel.

## Localisation

Les nouvelles clés de puce, enfant, pourcentage et explication générale sont présentes en français, néerlandais, anglais et allemand. Aucun texte utilisateur n’a été ajouté en dur dans Kotlin.

## Tests

- `NoIngredientListLocalizationTest` vérifie la présence des nouvelles clés dans les quatre ressources.
- `VerdictExplanationInstrumentedTest` couvre les puces, le parent `sirop de grenade`, l’enfant `concentré de grenade`, les pourcentages, l’absence de `|` et l’explication unique dans les quatre langues.

## Limites conservées

Cette version ne modifie pas la propagation des statuts des composites : un composé inconnu ne devient pas reconnu parce que ses enfants le sont. Elle ne modifie ni `VerdictEngine`, ni `VeganAnalyzer`, ni les règles de verdict, les données d’ingrédients, la détection implicite, la segmentation linguistique ou le traitement OCR.

## Version

- `versionName` : `0.6.9.6`
- `versionCode` : `42`

## Validations

- `testDebugUnitTest` : réussi.
- `assembleDebug` : réussi.
- `assembleDebugAndroidTest` : réussi.
- `git diff --check` : réussi.
- Tests connectés : non exécutés ; `adb` est indisponible dans l’environnement.

## Fichiers modifiés

- `app/build.gradle.kts`
- `app/src/main/java/com/example/isitvegan/VerdictExplanationFormatter.kt`
- `app/src/main/res/values*/strings.xml`
- `app/src/test/java/com/example/isitvegan/NoIngredientListLocalizationTest.kt`
- `app/src/androidTest/java/com/example/isitvegan/VerdictExplanationInstrumentedTest.kt`
- `FIX_0_6_9_6_UNKNOWN_LIST_PRESENTATION_REPORT.md`

Aucun commit ni push n’a été effectué.
