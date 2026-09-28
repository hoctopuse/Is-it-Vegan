# Référentiel UE multilingue des additifs — 0.6.9.10

> Rapport historique du premier lot partiel de 60 entrées. Il est remplacé, pour l’état final de la version, par `EU_ADDITIVES_REFERENCE_0_6_9_10_COMPLETION_REPORT.md`.

Date : 2026-09-27  
Branche : `master`  
Version cible : `0.6.9.10` (`versionCode 46`)

## Objectif et règle de sécurité

Cet import améliore l’identification d’additifs réglementés, sans qualifier leur origine vegan. Les 60 entrées nouvelles ont toutes le statut `UNCERTAIN`. `INCONCLUSIVE` reste un verdict global et ne figure dans aucune entrée de `ingredients.json`.

Les classifications historiques n’ont pas été modifiées. Les contrôles ciblés verrouillent notamment `E120 = NON_VEGAN`, `E330 = VEGAN`, `E471 = UNCERTAIN` et `E904 = NON_VEGAN`.

## Sources consultées le 27 septembre 2026

- [Commission européenne — base des additifs](https://food.ec.europa.eu/food-safety/food-improvement-agents/additives/database_en) : la base décrit les additifs autorisés dans l’UE et renvoie à la liste de l’Union.
- [Your Europe — additifs alimentaires](https://europa.eu/youreurope/business/product-rules-compliance/food/additives/index_fr.htm) : contexte réglementaire et renvois vers les annexes.
- [Règlement (CE) n° 1333/2008, texte consolidé — annexe I et annexe II, partie B](https://eur-lex.europa.eu/legal-content/FR/TXT/HTML/?uri=CELEX%3A02008R1333-20200702) : numéros E, dénominations et familles fonctionnelles.
- [Règlement (UE) n° 1169/2011 — annexe VII, partie C](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX%3A32011R1169) : l’étiquetage associe une catégorie à un nom spécifique ou un numéro E.

Les dénominations françaises, néerlandaises, anglaises et allemandes ajoutées sont des formes réglementaires ou usuelles alignées sur ces références. L’autorisation UE ne constitue jamais une preuve de caractère vegan.

## Import réalisé

| Mesure | Avant | Après | Écart |
|---|---:|---:|---:|
| Entrées canoniques | 142 | 202 | +60 |
| Aliases | 732 | 972 | +240 |
| Nouveaux numéros E | 0 | 60 | +60 |

Chaque nouvelle entrée comporte un numéro E, une raison prudente, la source interne existante `eu-additives` et quatre aliases de dénomination : un pour FR, NL, EN et DE. La ventilation ajoutée est donc de 60 aliases par langue, soit 240 au total.

| Famille fonctionnelle réglementaire | Nouveaux E | Nombre |
|---|---|---:|
| Colorants | E100, E101, E140, E150a, E153, E160a, E160b, E162 | 8 |
| Conservateurs | E200, E203, E210, E211, E223, E224, E234, E235, E249, E250, E251, E252 | 12 |
| Acides, correcteurs d’acidité et antioxydants | E260, E280, E281, E282, E296, E304, E307, E310, E320, E321, E331, E333 | 12 |
| Épaississants, gélifiants, émulsifiants et stabilisants | E400, E401, E405, E407, E407a, E414, E416, E420, E432, E433, E450, E452, E460, E466, E476, E481 | 16 |
| Agents levants, exhausteurs, agents d’enrobage et gaz | E501, E504, E572, E575, E620, E622, E901, E903, E920, E938, E941, E948 | 12 |

## Comportement vérifié

- Le normaliseur existant réduit déjà `E 330` et `E330` à la même forme ; aucune modification du matcher n’était nécessaire.
- Les formes `E330`, `E 330`, `e330` et `e 330` sont reconnues. Le nombre nu `330` ne l’est pas.
- Une forme réglementaire telle que `émulsifiant : E433` reconnaît l’additif, sans utiliser `émulsifiant` seul comme alias.
- Des noms représentatifs FR, NL, EN et DE de chaque famille sont reconnus et conduisent à `UNCERTAIN` pour les nouveaux ajouts.
- Une trace telle que `Peut contenir : E433.` reste hors de la composition et ne modifie pas le verdict.

## Omissions et limites assumées

Il ne s’agit pas d’une transcription exhaustive de l’annexe II-B. Les sous-entrées très spécifiques, les plages ou groupes réglementaires, les additifs avec conditions d’emploi étroites et les dénominations nécessitant une désambiguïsation supplémentaire restent à examiner avant import. Sont notamment hors de ce lot les séries de colorants, sulfites, phosphates, amidons modifiés, polyols, édulcorants et gaz non listées ci-dessus.

La phase de qualification vegan prévue après cet enrichissement devra documenter séparément l’origine, le procédé et les éventuelles règles d’origine ; elle ne doit pas dériver une classification VEGAN de l’autorisation UE ni de la simple reconnaissance par numéro E.

## Fichiers modifiés

- `app/src/main/assets/ingredients.json`
- `app/src/test/java/com/example/isitvegan/EuAdditivesReference06910Test.kt`
- `app/build.gradle.kts`
- `docs/changelog.md`
- `EU_ADDITIVES_REFERENCE_0_6_9_10_IMPORT_REPORT.md`

## Validations

- JSON chargé par `MiniJson` dans le test de référence : succès.
- Contrôle automatique : identifiants uniques, numéros E non contradictoires et 60 nouveaux éléments tous `UNCERTAIN` : succès.
- `./gradlew testDebugUnitTest --tests com.example.isitvegan.EuAdditivesReference06910Test` : succès.
- `./gradlew :mutation-core:test` : succès.
- `./gradlew testDebugUnitTest` : succès.
- `./gradlew assembleDebug` : succès.
- `./gradlew assembleDebugAndroidTest` : succès.
- `adb devices` : aucun appareil disponible ; `connectedDebugAndroidTest` non exécuté.
- PIT : non exécuté, conformément au périmètre de cette étape.
- `git diff --check` et `git diff --cached --check` : succès.

État Git final : les cinq fichiers listés dans cette section sont les seules modifications. Aucun commit ni push n’est réalisé par cette tâche.
