# Is It Vegan?

Application Android en Kotlin et Jetpack Compose qui analyse une liste d’ingrédients et évalue sa compatibilité vegan à partir d’une base embarquée. Elle accepte une étiquette saisie, une liste seule ou une photo traitée par l’OCR latin de ML Kit. Le flux photo comprend l’orientation EXIF, un recadrage en mémoire, la sélection prudente d’un bloc linguistique et un texte éditable avant analyse.

La version documentée est **0.6.8** (`versionCode 34`). L’interface est disponible en français, anglais et néerlandais. Le moteur distingue la compatibilité vegan, la classification végétarienne détaillée, les ingrédients incertains, les éléments inconnus et les traces de contamination croisée.

Le moteur d’analyse, la base d’ingrédients et le modèle OCR utilisé sont embarqués. Le code applicatif ne contacte aucun service métier distant. Les dépendances ML Kit ajoutent toutefois des composants de transport et des permissions réseau au manifeste final ; les limites exactes sont détaillées dans la [documentation sur la vie privée](docs/vie-privee.md).

## Documentation

La documentation technique complète se trouve dans [`/docs`](docs/index.md). Elle peut être servie localement avec MkDocs Material :

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

La base éditoriale se trouve dans [`knowledge/ingredients.json`](knowledge/ingredients.json). Voir le [guide développeur](docs/guide-developpeur.md) avant toute modification.
