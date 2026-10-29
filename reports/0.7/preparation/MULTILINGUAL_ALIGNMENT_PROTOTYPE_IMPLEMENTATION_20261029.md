# v0.7 — Prototype de revue d’alignement des ingrédients multilingues

**Date :** 29 octobre 2026  
**Branche :** `master`  
**HEAD de départ :** `92a0837bbafff1e21e60d3bfca74d6a9e08a5bec`

## But et périmètre

Cette tranche relie le prototype pur `MultilingualIngredientAlignment.kt` aux sections d’ingrédients présentes dans les blocs OCR et ajoute une revue dans l’écran OCR. Elle cherche à exposer des preuves et des propositions vérifiables, sans modifier automatiquement le texte analysé ni le verdict courant.

Le travail reste un prototype prudent : il n’ajoute aucun mapping, alias métier, asset de connaissance ou numéro de version. Il ne lance ni benchmark ni test sur appareil.

## Comportement implémenté

Après segmentation des blocs OCR, chaque bloc titré comme section d’ingrédients est extrait et parsé avec les parseurs existants. Les occurrences conservent leur texte OCR, langue, identifiant de bloc, ordre, parenthèses, profondeur, concept reconnu et statut résolu lorsqu’il existe. Les termes non reconnus restent dans l’évaluation.

Les formes reconnues par les mappings canoniques existants peuvent produire une corroboration. Une proposition visant une forme inconnue n’est générée que si deux ancres canoniques partagées et uniques sont présentes dans le même ordre, avec exactement une occurrence de chaque côté dans l’intervalle. La position seule, la ressemblance approximative, les ancres réordonnées ou les intervalles multiples ne donnent pas de correction. Les cas ambigus sont signalés comme tels.

La proposition indique le texte source, le bloc et la langue correspondants ainsi que le concept canonique suggéré. Elle ne remplit pas l’identifiant ni le statut de l’occurrence inconnue. Dans l’écran OCR, l’utilisateur peut éditer le texte proposé, l’accepter explicitement ou l’ignorer. L’acceptation remplace une occurrence source unique dans le champ éditable seulement. Elle ne relance pas l’analyse : l’utilisateur doit ensuite demander une nouvelle analyse. Le résultat précédent et ses diagnostics restent donc inchangés jusque-là. Une proposition dont la source apparaît plusieurs fois dans le champ n’est pas présentée ni appliquée.

Les blocs SV/NO restent disponibles dans la session, mais aucune correction lexicale n’est proposée pour ces langues tant qu’il n’existe pas de mappings fiables. Leur détection ne constitue pas une prise en charge lexicale par le classifieur.

## Fichiers concernés

- `mutation-core/src/main/kotlin/com/example/isitvegan/MultilingualIngredientAlignment.kt` — génération conservatrice depuis les sections parsées, conservation des occurrences et évaluation des liens.
- `mutation-core/src/main/kotlin/com/example/isitvegan/VeganAnalyzer.kt` et `app/src/main/java/com/example/isitvegan/VeganAnalyzer.kt` — accès au générateur avec les données, mappings et règles déjà chargés.
- `app/src/main/java/com/example/isitvegan/OcrProcessor.kt`, `OcrModels.kt` et `OcrSession.kt` — transport des blocs/preuves, visibilité, refus et acceptation explicite.
- `app/src/main/java/com/example/isitvegan/MultilingualAlignmentReview.kt` et `OcrFirstScreen.kt` — carte de revue éditable intégrée au mode OCR.
- `app/src/main/res/values/strings.xml`, `values-en/strings.xml`, `values-nl/strings.xml`, `values-de/strings.xml` — libellés de la revue.
- `mutation-core/src/test/kotlin/com/example/isitvegan/MultilingualIngredientAlignmentTest.kt` et `MultilingualIngredientAlignmentGenerationTest.kt` — tests d’évaluation et de génération.
- `app/src/test/java/com/example/isitvegan/OcrSessionTest.kt` — acceptation, refus et occurrence répétée.
- `app/src/androidTest/java/com/example/isitvegan/MainScreenInstrumentedTest.kt` — test Compose de l’action explicite.
- `app/src/test/java/com/example/isitvegan/OcrMultilingualSelectionTest.kt` — tests préexistants de la tranche précédente concernant les marqueurs scandinaves, conservés et étendus.

## Validation exécutée

Avec le JBR fourni par Android Studio (JDK 25.0.3), les validations ciblées suivantes ont réussi :

```powershell
.\gradlew.bat :mutation-core:test `
  --tests com.example.isitvegan.MultilingualIngredientAlignmentTest `
  --tests com.example.isitvegan.MultilingualIngredientAlignmentGenerationTest `
  :app:testDebugUnitTest `
  --tests com.example.isitvegan.OcrSessionTest `
  --tests com.example.isitvegan.OcrMultilingualSelectionTest `
  --tests com.example.isitvegan.OcrLanguageZonesTest `
  :app:compileDebugAndroidTestKotlin --no-daemon --console=plain
```

Résultats JUnit conservés dans les sorties Gradle locales :

| Classe | Réussites | Échecs |
|---|---:|---:|
| `MultilingualIngredientAlignmentTest` | 10 | 0 |
| `MultilingualIngredientAlignmentGenerationTest` | 7 | 0 |
| `OcrSessionTest` | 10 | 0 |
| `OcrMultilingualSelectionTest` | 8 | 0 |
| `OcrLanguageZonesTest` | 6 | 0 |

Après un dernier garde-fou qui évite de construire le matcher si moins de deux langues ont un titre d’ingrédients, les deux classes du cœur ont été relancées seules :

```powershell
.\gradlew.bat :mutation-core:test `
  --tests com.example.isitvegan.MultilingualIngredientAlignmentTest `
  --tests com.example.isitvegan.MultilingualIngredientAlignmentGenerationTest `
  --no-daemon --console=plain
```

Cette exécution a également réussi (`BUILD SUCCESSFUL`). `git diff --check` n’a signalé aucune erreur. Le test Compose `multilingualCorrectionReachesTheCallerOnlyAfterExplicitAccept` a été compilé via `compileDebugAndroidTestKotlin`, mais n’a pas été exécuté.

## Limites et risques résiduels

- Aucun parcours Compose n’a été exécuté sur émulateur ou téléphone ; l’affichage décrit ici est vérifié par le code et la compilation, pas par une observation à l’exécution.
- L’évaluation est déclenchée après segmentation OCR et peut construire un matcher si plusieurs blocs ont un titre d’ingrédients. Son coût n’a pas été mesuré.
- Aucun résultat OCR de référence n’est archivé pour `5087.jpg`. La photo locale est un artefact de benchmark non suivi et n’a pas été copiée, embarquée ou utilisée par cette tranche. Les erreurs synthétiques citées antérieurement ne prouvent donc pas ce que ML Kit a réellement produit pour cette image.
- Les corrections restent limitées aux langues pour lesquelles les mappings existants et les ancres parser/matcher offrent des preuves. Il n’y a ni fuzzy matching ni alignement automatique des listes réordonnées.
- Les tests confirment que le statut animal/incertain reste attaché aux occurrences et que l’acceptation ne change que le champ éditable. La nouvelle analyse après acceptation n’a pas été exercée dans un test instrumenté exécuté.
- La détection des codes SV/NO n’implique pas que leurs titres, classifieur ou sélecteur d’interface soient pris en charge.

## État du dépôt et actions hors périmètre

Le dépôt était sur `master`, au HEAD indiqué ci-dessus. Les changements préexistants de la tranche de prototype ont été préservés ; les fichiers listés ci-dessus ont été modifiés ou ajoutés dans le worktree. Aucun commit/push, installation, commande ADB, benchmark, changement de version, asset métier ou règle de connaissance n’a été effectué.

## Suite proposée

Avant d’élargir les règles d’alignement, exécuter le test Compose ciblé dans un environnement Android et ajouter des fixtures OCR déterministes issues de blocs réels, avec leur provenance. Puis vérifier le parcours complet : proposition affichée, édition/acceptation, nouvelle action « Analyser », résultat mis à jour et incertitudes toujours visibles. Les langues SV/NO nécessitent une décision et des mappings validés séparément.
