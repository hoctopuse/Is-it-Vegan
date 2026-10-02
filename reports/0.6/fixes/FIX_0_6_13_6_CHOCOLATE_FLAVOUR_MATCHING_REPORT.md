# Correctif 0.6.13.6 — arômes de chocolat

## Cause

`IngredientMatcher` recherche les alias canoniques à l’intérieur de chaque token. Après l’ajout de l’alias `chocolat`, les tokens `arôme chocolat` et `chocolate flavour` contenaient donc une correspondance valide avec `chocolate`. Des protections équivalentes existaient déjà pour le miel, le lait et la pomme, mais pas pour la famille chocolat.

## Correction

Le matcher exclut désormais un candidat de la famille chocolat lorsque le texte situé immédiatement avant sa correspondance est un marqueur d’arôme ou de goût (`arôme`, `arôme de`, `arôme naturel de`, `arôme goût`, `goût`, `goût de`), ou lorsque le suffixe est `flavour`, `flavor` ou `aroma`.

La protection couvre `chocolate`, `milk_chocolate`, `white_chocolate`, `filled_chocolate`, `chocolate_confection` et `powdered_chocolate`. Elle ne supprime aucun alias et ne modifie aucun statut. Les composés néerlandais et allemands `chocoladearoma` et `Schokoladenaroma` restent sans correspondance avec le chocolat réel grâce aux frontières lexicales existantes.

Une décomposition contrôlée de `Bitterschokolade` en `bitter schokolade` permet à l’alias réglementaire allemand `Schokolade` de rester reconnu, sans ajouter de mapping ni modifier les données éditoriales.

## Couverture

Les tests refusent comme chocolat réel : `arôme chocolat`, `arôme de chocolat`, `arôme naturel de chocolat`, `arôme goût chocolat`, `goût chocolat`, `chocolate flavour`, `chocolate flavor`, `chocolate aroma`, `chocoladearoma` et `Schokoladenaroma`.

Ils conservent : `chocolat`, `chocolat noir`, `chocolate`, `dark chocolate`, `chocolade`, `pure chocolade`, `Schokolade`, `Bitterschokolade`, ainsi que les formes FR/NL/EN/DE de chocolat au lait et de chocolat blanc. Un chocolat présent uniquement dans une section de traces reste hors verdict.

## Validation et limites

Les 471 concepts, 1 956 mappings et tous les statuts sont inchangés. L’import CELEX 02000L0036 retourne `changes=0`, et les assets restent en parité avec `knowledge/`.

Les marqueurs sont volontairement limités à des formulations revues. Une nouvelle construction linguistique ou une erreur OCR isolée doit recevoir un test avant d’être ajoutée. La correction ne crée pas de concept générique d’arôme et ne modifie ni les traces, ni `VerdictEngine`, ni `VeganAnalyzer`.

Les suites JVM, `assembleDebug`, `assembleDebugAndroidTest`, les trois contrôles Python demandés et `git diff --check` réussissent. Décision : **GO**.
