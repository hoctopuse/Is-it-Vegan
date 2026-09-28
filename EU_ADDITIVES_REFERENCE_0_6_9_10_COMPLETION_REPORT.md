# Complétion du référentiel UE multilingue — 0.6.9.10

Date : 28 septembre 2026  
Branche : `master`  
Décision : **GO**  
Version effective : `0.6.9.10` (`versionCode = 46`)

## Sources et limites

L’import repose exclusivement sur les fichiers locaux suivants :

- `reference-input/eu-food-labelling/03-food-additives/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv` : 340 numéros E uniques, dont 337 lignes `COMPLETE_4_LANGUAGES`, une ligne `PARTIAL_OFFICIAL` et deux lignes `AMBIGUOUS_GROUP_OR_RANGE` ;
- `EU_ADDITIVES_OFFICIAL_MULTILINGUAL_ACQUISITION_REPORT.md` ;
- les quatre PDF CELEX 02008R1333 consolidés au 18.08.2026 présents dans `reference-input/eu-food-labelling/03-food-additives/` (FR, NL, EN et DE).

Aucune ressource Internet n’a été consultée pendant cette complétion. Les URL conservées dans le CSV sont des métadonnées de provenance déjà acquises. Les dénominations ont été importées telles qu’elles figurent dans le CSV : aucun synonyme, traduction, rôle fonctionnel ou correction OCR n’a été inventé.

La présence d’un additif dans la réglementation UE n’établit pas son origine vegan. Toutes les nouvelles entrées ont donc le statut `UNCERTAIN`. `INCONCLUSIVE` n’est pas utilisé comme statut d’ingrédient. Les classifications, raisons, sources, identifiants et alias métier préexistants ont été conservés ; seules les dénominations officielles absentes ont été ajoutées aux alias existants.

## Décompte avant et après

| Mesure | Avant | Après | Écart |
|---|---:|---:|---:|
| Entrées applicatives | 202 | 452 | +250 |
| Entrées avec numéro E | 92 | 342 | +250 |
| Alias | 972 | 2 176 | +1 204 |

Comparaison des 340 lignes de référence avec la base avant complétion :

- 250 entrées importables manquantes ont été ajoutées ;
- 88 entrées réglementaires étaient déjà présentes et ont été conservées, avec ajout de 271 dénominations officielles absentes ;
- 2 lignes ont été exclues : E345 et E345(i), toutes deux marquées `AMBIGUOUS_GROUP_OR_RANGE` ;
- les 250 nouvelles entrées apportent 933 alias officiels distincts ;
- les 338 lignes importables du CSV sont désormais couvertes par un numéro E exact dans la base.

La base contient en outre quatre numéros historiques absents du CSV consolidé actuel : E160b, E203, E572 et E960. Ils ont été conservés sans reclassification ni suppression.

## Cas particuliers

- **E160b(i)** : entrée propre et distincte de l’entrée historique E160b ; ses formes exactes `E160b(i)` et `E 160b(i)` sont reconnues.
- **E322a** : importé comme `UNCERTAIN` avec les seules dénominations officielles disponibles en français, néerlandais et anglais. Aucun nom allemand n’a été ajouté, conformément à la mention `Absent from official PDF: DE`.
- **E345 et E345(i)** : les deux clés restent distinctes dans le CSV et absentes de la base applicative. Les données disponibles sont complémentaires mais insuffisantes pour établir une fusion ou deux dénominations multilingues non ambiguës.
- **E960a et E960b** : importés comme deux entrées `UNCERTAIN` distinctes de l’entrée historique E960, dont la classification `VEGAN` n’a pas été modifiée.
- **E470b et E572** : les quatre dénominations officielles de E470b se normalisent comme les alias historiques de E572. E572 a le statut effectif `UNCERTAIN`, comme la nouvelle entrée E470b. Le matcher trie les alias par longueur décroissante puis couvre chaque portion de texte une seule fois. Pour deux alias normalisés identiques, de même longueur et à la même position, l’ordre stable de la base fait actuellement sélectionner E572, présent avant E470b. Un libellé seul correspond donc à E572 et produit `UNCERTAIN`, jamais `VEGAN` ou `NON_VEGAN`. Avec le numéro E572, la correspondance reste E572. Avec le numéro E470b, E572 couvre le libellé commun et la correspondance exacte du numéro ajoute E470b ; les deux entrées `UNCERTAIN` sont alors conservées et le verdict reste `UNCERTAIN`. Les numéros seuls distinguent exactement E572 de E470b. Aucun alias historique n’a été retiré ou réaffecté.

Les numéros sont portés par le champ `eNumber` du schéma existant. Le normaliseur reconnaît les formes sûres avec ou sans espace après `E`, y compris les suffixes exacts, et ne reconnaît pas un nombre nu tel que `330`.

## Validations

- test JVM ciblé `EuAdditivesReference06910Test` : succès ;
- couverture vérifiée : 340 lignes uniques, 338 importables présentes, 250 nouvelles entrées toutes `UNCERTAIN`, 92 classifications antérieures inchangées ;
- cas E160b(i), E322a, E960a, E960b, E345 et E345(i), collision E345/E345(i), formes `E330` / `E 330` / `330` et alias multilingues : succès ;
- test de sécurité E470b/E572 sur chacune des quatre dénominations officielles communes normalisées, seule puis accompagnée de chaque numéro E : succès ;
- `./gradlew :mutation-core:test` : succès ;
- `./gradlew testDebugUnitTest` : succès ;
- `./gradlew assembleDebug` : succès ;
- `./gradlew assembleDebugAndroidTest` : succès ;
- `git diff --check` : succès.

La commande `adb` n’est pas disponible dans la session ; les tests connectés n’ont donc pas été exécutés.

## Fichiers modifiés

- `app/src/main/assets/ingredients.json` ;
- `app/src/test/java/com/example/isitvegan/EuAdditivesReference06910Test.kt` ;
- `tools/import_eu_additives_reference.py` ;
- `docs/changelog.md` ;
- `EU_ADDITIVES_REFERENCE_0_6_9_10_IMPORT_REPORT.md` (marquage du rapport partiel comme historique) ;
- `EU_ADDITIVES_REFERENCE_0_6_9_10_COMPLETION_REPORT.md`.

Ni `VerdictEngine`, ni `VeganAnalyzer`, ni le pipeline OCR/ML Kit, ni le recadrage, ni la logique des traces n’ont été modifiés. Aucun commit ni push n’est effectué dans cette tâche.
