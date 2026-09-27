# Stratégie de tests

Le projet sépare les tests du cœur JVM (`mutation-core/src/test`), les autres tests JVM (`app/src/test`) et les tests instrumentés Android (`app/src/androidTest`). Les composants structurels sont conçus avec des fonctions pures afin que la majorité du pipeline puisse être testée sans ML Kit, image ou appareil.

## Tests JVM

Commande complète :

```powershell
.\gradlew.bat testDebugUnitTest
```

Tests du cœur partagé :

```powershell
.\gradlew.bat :mutation-core:test
```

Les suites principales sont regroupées ci-dessous.

| Domaine | Classes représentatives | Couverture |
|---|---|---|
| OCR structurel | `OcrTextReconstructorTest`, `OcrTextCleanerTest`, `OcrNativeOrderRegressionTest` | lignes/blocs, colonnes, fallback d’ordre, nettoyage prudent, rotation simulée |
| Image et recadrage | `OcrCropGeometryTest`, `OcrImageSizingTest`, `OcrThreadingTest` | hitboxes, limites, taille minimale, `ContentScale.Fit`, sous-échantillonnage, threads de travail |
| Session OCR | `OcrSessionTest`, `OcrMultilingualSelectionTest` | séparation brut/éditable, sélection de bloc, texte complet, invalidation et export |
| Langues | `LabelLanguageSegmenterTest`, `LabelLanguageHotfixTest`, `LabelLanguageSegmentationHotfixTest`, `OcrLanguageZonesTest`, `Version064LanguageTest` | marqueurs, marchés, priorité UI, titres sur une ligne, ambiguïtés et fallback |
| Sections | `LabelSectionExtractorTest`, `CrossContactTest` | ingrédients, présence réelle, traces, nutrition, conservation et notes |
| Régression 0.6.5 | `Version065OcrPipelineTest` | titres OCR, score bloc complet, identité partagée, traces FR/DE/NL, conservation, séries E et pont allemand |
| Parsing | `IngredientTreeParserTest`, `HierarchyAnalysisTest`, `ParserModulesTest` | parenthèses, crochets, niveaux, pourcentages, classes fonctionnelles et additifs |
| Matching/verdict | `VeganAnalyzerTest`, `UnknownCollectionHotfixTest`, suites `Version05…Test` | alias longs, expressions protégées, inconnus, règles d’origine et priorité du verdict |
| Explication 0.6.9 | `VerdictExplanation069Test` | résultat conditionnel vegan/végétarien, bloqueurs, occurrences identiques ordonnées, chemins imbriqués, traces et stabilité du verdict principal |

Les noms historiques de certaines suites correspondent à la version où le comportement a été introduit. Ils restent des tests de régression du code actuel.

## Cas représentatifs

### Multilingue

`OcrMultilingualSelectionTest` utilise une même étiquette contenant `INGREDIENTEN`, un titre ambigu `INGREDIENTS` suivi de vocabulaire français et `ZUTATEN`. Il vérifie la correction EN → FR par le contenu, le choix FR avec une interface française et la disponibilité séparée des autres blocs.

`Version064LanguageTest` vérifie qu’un bloc EN complet gagne face à un bloc FR tronqué et que le mot français « de » ne devient pas un marqueur allemand.

### Sous-ingrédients et pourcentages

`IngredientTreeParserTest` construit notamment :

```text
cœur de tofu fumé 62,6 % (tofu 95 % [soja, eau, nigari])
```

Le test vérifie les deux niveaux composites et l’affectation des deux pourcentages. D’autres cas couvrent les crochets, les titres de sections pondérées et la règle selon laquelle un parent composite ne devient pas un inconnu.

### Traces et allergènes

`CrossContactTest` compare le verdict avant et après ajout de formulations de traces. Il vérifie leur ordre, leur déduplication et leur exclusion des tokens. Les lignes « Allergènes : » sont conservées comme notes diagnostiques sans être interprétées comme moteur d’allergies.

### Additifs

`ParserModulesTest` protège `E 471`, `B 12` et les identifiants voisins lors du nettoyage des quantités. `IngredientTreeParserTest` vérifie les formes `émulsifiant : E471`, `emulsifier: E471` et `emulgator: E471`. Les tests `Version0511Test` couvrent les qualifications d’origine attachées à E322 et E471.

### Erreurs OCR

`OcrTextCleanerTest` conserve exactement `11,9 %`, `E471`, `PETIT-LAIT`, les parenthèses imbriquées et les mots inconnus. Il vérifie les coupures avec trait d’union et la correction très bornée de `ingrédlents` en titre. `OcrNativeOrderRegressionTest` protège une étiquette tournée où ML Kit a déjà produit un ordre exploitable.

## Tests instrumentés

Construire l’APK de tests :

```powershell
.\gradlew.bat assembleDebugAndroidTest
```

Exécuter sur un appareil ou émulateur connecté :

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Suites actuelles :

- `MainScreenInstrumentedTest` : lancement réel de `MainActivity`, modes d’entrée, défilement, résultat, traces et invalidation ;
- `OcrFixturesInstrumentedTest` : quatre images dans `app/src/androidTest/assets/ocr` réellement reconnues par le modèle latin embarqué ;
- `OcrTextCleanerInstrumentedTest` : expressions Unicode exécutées par le moteur regex Android, notamment les majuscules accentuées ;
- `RealLabelsInstrumentedTest` : étiquettes réalistes, hiérarchies, verdicts et traces avec l’asset embarqué ;
- `Version0511InstrumentedTest` : présence de données attendues dans la base embarquée.
- `VerdictExplanationInstrumentedTest` : terminologie conditionnelle, rendu NON VEGAN avec incertains, traces et chemin explicatif en FR, NL, EN et DE.

Les fixtures OCR possèdent un `.png` et un `.txt` de référence pour `simple`, `multilingual`, `nested` et `may_contain`. Seule cette suite dépend réellement de ML Kit ; les tests de reconstruction utilisent des `OcrDocument` simulés.

## Validation des données

Les scripts éditoriaux ont leur propre mode de vérification :

```powershell
python tools/build_ingredients.py --check
python tools/build_origin_rules.py --check
```

Ces commandes détectent notamment un asset périmé, un statut inconnu, un alias partagé ou des règles d’origine contradictoires.

## Choisir le bon niveau

- Changement de regex/nettoyage : test JVM et cas Android si le moteur regex peut différer.
- Changement de géométrie : test JVM avec coordonnées simulées ; test Compose seulement pour le parcours UI.
- Changement de parser/matcher/verdict : tests JVM ciblés, puis suite unitaire complète.
- Changement de ML Kit, manifeste ou ressources Android : `assembleDebugAndroidTest` et tests connectés lorsque l’appareil est disponible.
