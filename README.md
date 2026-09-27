# Is It Vegan?

Application Android en Kotlin et Jetpack Compose qui analyse une liste d’ingrédients à partir d’une base embarquée. Elle accepte une étiquette saisie, une liste seule ou une photo traitée par l’OCR latin de ML Kit. Le flux photo inclut l’orientation EXIF, un recadrage en mémoire, la sélection prudente d’un bloc linguistique et un texte éditable à vérifier avant analyse.

La documentation couvre l’état livré jusqu’à **0.6.9.9**. L’interface et ses explications sont disponibles en français, néerlandais, anglais et allemand. Le moteur est déterministe : il ne prend aucune décision par IA générative, cloud ou service métier distant. Le modèle OCR et les données de classification sont embarqués ; les dépendances ML Kit et le manifeste fusionné sont détaillés dans la [documentation sur la vie privée](docs/vie-privee.md).

## Résultat et prudence

Le verdict principal affiché est l’un des suivants :

- **VEGAN** : tous les ingrédients considérés sont reconnus vegan ;
- **NON VEGAN** : un ingrédient non végétarien est identifié ;
- **INCERTAIN** : un ingrédient connu a une origine ou un statut variable ;
- **INCONCLUS** (`INCONCLUSIVE`) : un ingrédient réellement inconnu ou une analyse insuffisante empêche une conclusion complète.

`VEGETARIAN` est une classification détaillée informative : elle décrit les ingrédients connus comme végétariens mais incompatibles vegan. Elle ne transforme jamais le verdict vegan en résultat favorable. Les traces de contamination croisée sont affichées séparément et exclues du verdict. Une déclaration de présence réelle telle que « contient : lait » suit un parcours distinct des traces.

L’OCR peut mal lire une étiquette, surtout si elle est floue, tronquée, tournée, très dense ou composée de plusieurs langues. Vérifier et corriger le texte avant l’analyse reste indispensable. Si la composition n’est pas lisible dans son ensemble, reprendre une photo plus nette ou avec un cadrage plus large est la recommandation pratique ; cette aide est un conseil, pas une détection automatique livrée.

## Documentation

La documentation publiée sépare présentation utilisateur, fonctionnement, architecture développeur et [notes de version](docs/changelog.md). Elle peut être servie localement avec MkDocs Material :

```bash
python -m pip install -r requirements-docs.txt
python -m mkdocs serve
```

La publication GitHub Pages est préparée par [`.github/workflows/docs.yml`](.github/workflows/docs.yml). Le dépôt distant doit être configuré pour utiliser **GitHub Actions** comme source Pages.

## Compiler et tester

Prérequis : JDK 17, Android SDK 37 et un SDK Android permettant l’exécution sur API 30 ou plus.

Sous Windows :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebugAndroidTest
```

Avec un appareil ou un émulateur connecté :

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

L’architecture sépare l’application Android `:app` et le cœur Kotlin/JVM `:mutation-core`; les tests couvrent le parsing, le matching, les verdicts, les diagnostics et les régressions OCR. La base éditoriale se trouve dans [`knowledge/ingredients.json`](knowledge/ingredients.json). Voir le [guide développeur](docs/guide-developpeur.md) avant toute modification.
