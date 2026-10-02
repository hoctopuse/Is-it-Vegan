# Audit matching, OCR et diagnostics avant 0.7

Date : 2026-09-30  
Périmètre : matchers, parseurs, extracteurs, diagnostics, fixtures OCR, rapports de régression et tests existants.  
Mode : lecture seule. Aucun fichier de production ou de test n'a été modifié.

## 1. Comportements actuellement protégés

### Matching et arômes

- `arôme chocolat`, `arôme de chocolat`, `arôme naturel de chocolat`, `goût chocolat`, `chocolate flavour`, `chocolate flavor`, `chocolate aroma`, `chocoladearoma` et `Schokoladenaroma` ne sont pas traités comme le chocolat réel.
- `chocolat`, `chocolat noir`, `chocolate`, `dark chocolate`, `chocolade`, `pure chocolade`, `Schokolade` et `Bitterschokolade` restent reconnus comme les catégories réelles attendues.
- `arôme fraise` et `arôme de fraise` ne sont pas rattachés à `strawberry`; `fraise` est rattachée à `strawberry`.
- `extrait de café` ne devient pas `coffee`; `café` est rattaché à `coffee`.
- `arôme vanille` ne crée pas de correspondance avec `vanilla`, conformément à la règle documentaire qui distingue l'arôme de la matière première.
- La protection chocolat couvre aussi les catégories `milk_chocolate`, `white_chocolate`, `filled_chocolate`, `chocolate_confection` et `powdered_chocolate`.

Ces protections sont implémentées dans le matching contextuel et documentées par `FIX_0_6_13_6_CHOCOLATE_FLAVOUR_MATCHING_REPORT.md`, `docs/matching.md` et `docs/sources/eu-flavourings-regulation.md`.

### Alias, langues et formes composées

- Les mappings structurés FR/NL/EN/DE sont validés par `MultilingualIngredientMappingTest` et les tests d'import réglementaires.
- Les formes allemandes `Schokolade`, `Bitterschokolade` et `Schokoladenaroma` sont explicitement couvertes. `Bitterschokolade` est décomposée de manière contrôlée pour conserver la correspondance chocolat sans assimiler `Schokoladenaroma` au chocolat.
- Les formes néerlandaises et anglaises de chocolat et de chocolat au lait sont couvertes par `Version0681ChocolateLabelTest` et `CocoaChocolateDirectiveImportTest`.
- Les alias génériques exigent une expression complète connue ; `HierarchyAnalysisTest.genericAliasesRequireACompleteKnownExpression` protège contre les correspondances partielles trop courtes.
- Les numéros E sont conservés par le nettoyage OCR et le parsing ; `E471` est testé dans les chemins de matching, d'origine et de diagnostic.

### OCR et texte dégradé

- Les fixtures `app/src/androidTest/assets/ocr/` couvrent les textes simple, multilingue, imbriqué et les traces.
- `OcrLineBreak0692Test` couvre les expressions coupées entre lignes : `huile de`/`colza`, `glucose-`/`fructose`, `farine de`/`blé`, parenthèses continuées et séparation par virgule.
- Les réparations OCR sont bornées par langue et contexte ; elles ne réécrivent pas les sections de traces, de préparation ou de conservation.
- Les tests de correction OCR couvrent des fautes mineures comme `qlucose` dans un contexte sirop, des variantes de lécithine et des termes alimentaires revus.
- `OcrNativeOrderRegressionTest`, `OcrMultilingualSelectionTest`, `Version067OcrStabilizationTest` et `Version068RobustnessTest` couvrent l'ordre des blocs, les blocs multilingues, les titres dégradés et la sélection de la langue.

### Compositions, pourcentages et diagnostics

- `IngredientTreeParser` conserve les parents, enfants, profondeurs, ordres, `parentOrder` et pourcentages.
- Un parent composite n'est pas envoyé comme un ingrédient autonome au matcher ; les feuilles déterminent le verdict.
- Les enfants animaux, incertains et inconnus restent visibles dans l'arbre et déterminent leur effet respectif.
- Les parenthèses qualificatives, les classes fonctionnelles et les parenthèses d'origine ne sont pas transformées abusivement en composites.
- Les pourcentages restent attachés au nœud qui les porte et ne modifient pas le statut métier.
- Les diagnostics reconstruisent le chemin parent → enfant d'un inconnu pour l'affichage sans ajouter le parent aux inconnus métier.

### Traces, listes sans titre et inconnus

- Les traces sont extraites avant le parsing de la composition, restent dans `crossContactWarnings` et ne sont pas envoyées au matcher ou au verdict.
- Les marqueurs français, néerlandais, allemands et anglais de traces sont couverts, y compris les sections adjacentes et les marqueurs qui se chevauchent réellement.
- Une liste sans titre n'est retenue que si les indices structurels sont suffisants ; un texte marketing, nutritionnel, de stockage ou une trace seule n'est pas traité comme une composition.
- Un ingrédient inconnu conserve son effet bloquant ; un élément `UNCERTAIN` reste incertain. Les vues conditionnelles ne transforment pas ces éléments en confirmation globale vegan.

## 2. Régressions possibles

| Catégorie | Zone | Constat |
|---|---|---|
| Bug démontré | Aucune dans les campagnes exécutées | Les tests ciblés matching/OCR/diagnostics passent et aucun échec reproductible n'a été observé. |
| Risque de faux positif | Nouveaux alias réglementaires | L'augmentation de la base accroît mécaniquement le risque qu'un alias court ou une sous-chaîne soit reconnu dans un mot plus long. Les frontières et le tri des alias protègent les cas existants, mais les nouveaux alias n'ont pas tous un test de non-correspondance. |
| Risque de faux positif | Arômes hors formes revues | La protection repose sur des marqueurs immédiats et des constructions connues. Une nouvelle forme linguistique ou OCR, par exemple un composé non couvert, peut encore atteindre l'ingrédient réel si elle passe les frontières lexicales. |
| Risque de faux négatif | OCR fortement dégradé | Les corrections sont volontairement limitées aux variantes revues. Une faute nouvelle sur un alias réglementaire peut rester inconnue plutôt que d'être corrigée. |
| Risque de faux négatif | Formes allemandes non réglementaires | Les tests couvrent plusieurs composés de chocolat, mais pas une matrice complète des composés allemands pour les autres familles enrichies. |
| Couverture de test absente | Arôme + OCR | Les scénarios café/fraise sont testés en saisie manuelle ; leur combinaison avec les fautes OCR, les retours à la ligne et les blocs multilingues n'est pas couverte de manière dédiée. |
| Couverture de test absente | Texte partiellement tronqué | Les parenthèses non fermées et les frontières de langue sont couvertes, mais pas un corpus systématique de troncatures au milieu des nouveaux alias FR/NL/EN/DE. |
| Couverture de test absente | Nouveaux additifs E | E471, E322 et quelques cas historiques sont couverts ; il n'existe pas de test de non-collision pour chaque famille d'alias E importée. |
| Comportement volontaire | Alias inconnus ou trop courts | Ils restent inconnus ou sont ignorés tant qu'une expression complète n'est pas reconnue. Cette prudence réduit les faux positifs et peut produire des faux négatifs sur des libellés très abrégés. |
| Comportement volontaire | Traces | Les mentions « peut contenir » et équivalents restent hors verdict, même si elles contiennent lait, œuf ou gélatine. |
| Amélioration réservée à la 0.7 | Matrice de corpus enrichi | Une campagne combinant chaque famille réglementaire avec OCR, langue, composition imbriquée et troncature n'existe pas encore. |
| Amélioration hors périmètre | Correction OCR probabiliste générale | Les correctifs actuels sont déterministes et bornés ; une correction probabiliste nécessiterait une autre validation et un autre périmètre. |

## 3. Tests existants pertinents

### Arômes, langues et matching

- `app/src/test/java/com/example/isitvegan/FlavouringsRegulationImportTest.kt` : arôme chocolat, arôme fraise, extrait de café, arôme vanille et séparation des traces.
- `app/src/test/java/com/example/isitvegan/CocoaChocolateDirectiveImportTest.kt` : formes chocolat FR/NL/EN/DE et exclusion des arômes chocolat.
- `app/src/test/java/com/example/isitvegan/Version0681ChocolateLabelTest.kt` : chocolat, beurre de cacao, formes néerlandaises et allemandes, OCR chocolat, E322 et traces.
- `app/src/test/java/com/example/isitvegan/Version066MultilingualMatchingTest.kt` et `MultilingualIngredientMappingTest.kt` : mappings structurés et langues.
- `mutation-core/src/test/kotlin/com/example/isitvegan/HierarchyAnalysisTest.kt` : alias génériques, parenthèses protégées, contexte parent et enfants.

### OCR et parsing

- `OcrLineBreak0692Test.kt` : retours à la ligne, traits d'union et parenthèses.
- `Version066OcrNormalizationTest.kt`, `Version067OcrStabilizationTest.kt` et `Version068RobustnessTest.kt` : fautes bornées, titres dégradés, textes multilingues, troncatures et compositions endommagées.
- `OcrMultilingualSelectionTest.kt`, `OcrNativeOrderRegressionTest.kt`, `OcrTextReconstructorTest.kt` et `OcrLanguageZonesTest.kt` : reconstruction et sélection des blocs.
- `IngredientTreeParserTest.kt` et `LabelSectionExtractorTest.kt` : pourcentages, classes fonctionnelles, imbrication, titres et sections de traces.

### Diagnostics et verdicts

- `CrossContactTest.kt`, `Version068RobustnessTest.kt` et `DiagnosticStabilizationTest.kt` : séparation des traces et invariants du verdict.
- `UncertainUnknownVisibility097Test.kt`, `UnknownCollectionHotfixTest.kt` et `DiagnosticOutput093Test.kt` : inconnus, éléments incertains, visibilité et diagnostics.
- `VerdictExplanation069Test.kt` : blocage par inconnu ou `NON_VEGAN`, résultat conditionnel et occurrences imbriquées.
- Les rapports `FIX_0_6_13_6_CHOCOLATE_FLAVOUR_MATCHING_REPORT.md`, `FIX_0_6_9_2_OCR_LINE_BREAK_REPORT.md`, `FIX_0_6_9_8_NESTED_UNKNOWN_CONTEXT_REPORT.md` et `COMPOSITE_STATUS_PROPAGATION_AUDIT_0_6_9_9.md` servent de corpus documentaire de régression.

Les campagnes JVM ciblées exécutées pendant cet audit ont réussi :

- `:mutation-core:test` sur les tests de hiérarchie, parsing, sections, traces, origine et explication ;
- `:app:testDebugUnitTest` sur chocolat, arômes, multilingue, OCR, lignes coupées, langues, inconnus et traces.

## 4. Tests manquants

- Une matrice explicite `réel / arôme / extrait` pour chocolat, café et fraise dans chacune des langues FR/NL/EN/DE.
- Les mêmes cas après faute OCR mineure, coupure de ligne et suppression d'un séparateur.
- Des tests négatifs pour les nouveaux alias réglementaires : sous-chaîne dans un mot plus long, alias trop court, composé allemand non prévu et alias partagé entre deux E-numbers.
- Une matrice E-number couvrant les nouveaux additifs importés avec `E<number>`, nom officiel, forme OCR et forme dans une classe fonctionnelle.
- Des compositions à trois niveaux combinant pourcentage au parent, pourcentage à l'enfant, enfant animal, enfant incertain, enfant inconnu et trace adjacente.
- Des textes tronqués au début, au milieu et à la fin d'un alias dans chaque bloc OCR linguistique.
- Une vérification dédiée que les arômes placés dans une composition réelle restent des feuilles `UNCERTAIN` sans créer le fruit, le café ou le chocolat correspondant.

## 5. Exemples minimaux à ajouter en 0.7

Ces exemples sont des cas de test proposés, non exécutés et non ajoutés dans cette branche :

- `Ingrédients : arôme chocolat, café, fraise.` : reconnaître l'arôme comme incertain et `café`/`fraise` comme ingrédients réels selon les tokens distincts.
- `Ingredients: chocolate flavour, coffee, strawberry.` : même distinction en anglais.
- `Zutaten: Schokoladenaroma, Kaffee, Erdbeere.` : ne pas classer l'arôme comme chocolat ; conserver les deux ingrédients réels.
- `Ingrediënten: chocoladearoma, koffie-extract, aardbei.` : ne pas convertir l'arôme ou l'extrait en ingrédients réels.
- `Ingrédients : émulsifiant : E471, arôme naturel de fraise, sel.` : E471 reste incertain, l'arôme reste incertain et aucun `strawberry` implicite n'est créé.
- `Ingrédients : sauce (eau, farine de blé 40 %, gélatine), sel. Peut contenir : lait.` : la gélatine bloque, le parent reste structurel et la trace n'ajoute pas un second bloqueur.
- `Ingrédients : chocolat (sucre, cacao, lécithines (soja),` suivi d'une trace et d'un titre de stockage : le texte tronqué ne doit pas absorber la trace ni le stockage.
- Un même libellé multilingue contenant `arôme chocolat` en FR, `chocolate flavour` en EN, `chocoladearoma` en NL et `Schokoladenaroma` en DE, avec fautes OCR mineures contrôlées.

## 6. Éléments à ne surtout pas corriger dans la branche 0.6.13

- Ne pas remplacer les protections d'arôme par une correspondance lexicale générale qui ferait d'un goût ou d'un extrait l'ingrédient réel.
- Ne pas propager un statut du parent composite vers ses enfants ou inversement ; seuls les descendants analysables alimentent le verdict.
- Ne pas envoyer les traces dans `IngredientTreeParser`, `IngredientMatcher`, `UnknownCollector` ou `VerdictEngine`.
- Ne pas transformer un inconnu ou un `UNCERTAIN` en `VEGAN` pour améliorer le taux de correspondance.
- Ne pas élargir les corrections OCR au-delà des formes revues sans test de contexte et de non-correspondance.
- Ne pas fusionner les langues ou reconstruire un bloc OCR multilingue en une composition unique si les frontières de bloc ne le justifient pas.
- Ne pas supprimer les pourcentages, `parentOrder`, les occurrences répétées ou les chemins parent → enfant des diagnostics.
- Ne pas résoudre les alias E470b/E572 par un choix arbitraire : leur statut commun `UNCERTAIN` évite actuellement un impact de verdict.
- Ne pas interpréter une autorisation réglementaire, un alias de dénomination ou une proximité de texte comme une preuve d'origine vegan.

## 7. Verdict final

**READY_WITH_WARNINGS**

L'enrichissement n'a pas révélé de régression démontrée dans les tests exécutés. Le matching chocolat/arômes, les langues FR/NL/EN/DE, l'OCR borné, les compositions imbriquées, les additifs E, les inconnus, les éléments incertains et les traces sont actuellement protégés. Les avertissements portent sur la couverture combinatoire encore limitée pour café/fraise sous OCR, les nouveaux alias réglementaires, les formes allemandes hors chocolat et les textes partiellement tronqués. Ces extensions relèvent de la 0.7 ; aucune correction ne doit être appliquée à la branche 0.6.13 sur la base de cet audit seul.
