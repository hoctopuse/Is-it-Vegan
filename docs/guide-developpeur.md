# Guide développeur

## Prérequis

Versions déclarées dans le dépôt :

| Élément | Version |
|---|---|
| Gradle Wrapper | 9.7.1 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.4.20 |
| Java source/target | 17 |
| compileSdk / targetSdk | 37 / 37 |
| minSdk | 30 |
| Compose BOM | 2026.09.00 |
| ML Kit Text Recognition | 16.0.1 |
| AndroidX ExifInterface | 1.4.2 |

Le dépôt ne fixe pas une version précise d’Android Studio. Utiliser une version compatible avec AGP 9.4.1, JDK 17 et Android SDK 37. Le JDK embarqué d’Android Studio convient généralement.

## Structure

```text
app/
  src/main/java/com/example/isitvegan/   code Kotlin
  src/main/res/                           ressources Compose/Android FR, EN, NL
  src/main/assets/                        base et règles embarquées générées
  src/test/                               tests JVM
  src/androidTest/                        tests Android et fixtures OCR
knowledge/                                sources éditoriales JSON
tools/                                    validateurs/générateurs Python
docs/                                     documentation MkDocs
.github/workflows/docs.yml                publication GitHub Pages
```

`MainActivity` lance actuellement `OcrFirstScreen`. Le composable `IsItVeganScreen` présent dans `MainActivity.kt` est hérité et n’est pas référencé.

## Compilation

Windows PowerShell :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

Linux/macOS :

```bash
./gradlew assembleDebug
```

La variante release n’active actuellement pas la minification.

## Tests

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest
```

La dernière commande exige un appareil ou émulateur visible par ADB. Voir la [stratégie de tests](tests.md) pour cibler une suite.

## Documentation locale

```powershell
python -m pip install -r requirements-docs.txt
python -m mkdocs serve
```

Validation stricte utilisée par la CI :

```powershell
python -m mkdocs build --strict
```

Le workflow `Documentation` construit le site sur les changements de `master` touchant les sources documentaires, puis publie l’artefact avec les actions officielles GitHub Pages. Il ne peut fonctionner que si le dépôt GitHub utilise GitHub Actions comme source Pages ; ce réglage distant n’est pas effectué par le code.

## Modifier une règle de traitement

1. Identifier la bonne frontière : langue, section, parsing, matching ou verdict.
2. Ajouter d’abord un cas minimal à la suite ciblée.
3. Modifier le composant responsable sans enrichir silencieusement une autre couche.
4. Exécuter le test ciblé, puis `testDebugUnitTest`.
5. Si Android, Compose, EXIF ou ML Kit sont concernés, construire et exécuter les tests instrumentés pertinents.
6. Mettre à jour `/docs` si le contrat, le flux ou le format change.

Exemples : un nouveau titre va dans `LabelLexicon`, une nouvelle borne d’étiquette dans `LabelSectionExtractor`, une nouvelle classe fonctionnelle dans `FunctionalClassLexicon`, et une nouvelle priorité globale dans `VerdictEngine`.

## Modifier un ingrédient

La source de vérité est `knowledge/ingredients.json`, jamais l’asset directement.

```powershell
python tools/build_ingredients.py
python tools/build_ingredients.py --check
```

Chaque entrée exige un identifiant unique, un nom, au moins un alias, un statut valide, une raison et au moins une source. Ajouter ou changer un statut exige une revue éditoriale et des tests de matching/verdict.

Les descriptions de sources sont dans `knowledge/sources.json`. Elles ne sont pas copiées dans l’APK ; l’asset contient seulement leurs identifiants joints.

## Modifier une qualification d’origine

Éditer `knowledge/origin_qualifier_rules.json`, puis :

```powershell
python tools/build_origin_rules.py
python tools/build_origin_rules.py --check
```

Une règle `REVIEW_BEFORE_ENGINE_USE` est ignorée. Pour activer une règle, vérifier les attaches syntaxiques, les statuts vegan et végétarien, les sources et les conflits, puis ajouter des tests dans les suites de version/origine.

## Ressources et langues UI

Les textes externalisés sont dans :

- `app/src/main/res/values/strings.xml` — français ;
- `app/src/main/res/values-en/strings.xml` — anglais ;
- `app/src/main/res/values-nl/strings.xml` — néerlandais.

Certains messages plus anciens restent codés en dur dans `OcrFirstScreen` et dans le composable hérité. Lors d’une modification UI, éviter d’en ajouter et maintenir les trois fichiers de ressources.

## Contrôles avant livraison

```powershell
python tools/build_ingredients.py --check
python tools/build_origin_rules.py --check
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebugAndroidTest
git diff --check
git status --short
```

Ajouter `connectedDebugAndroidTest` lorsque l’environnement le permet. Vérifier aussi le manifeste fusionné si une dépendance, une permission ou la promesse hors ligne change.

