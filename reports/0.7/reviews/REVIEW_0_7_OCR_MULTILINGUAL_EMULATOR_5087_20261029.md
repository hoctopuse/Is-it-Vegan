# Validation OCR multilingue sur émulateur — photo 5087

Le 29 octobre 2026, le parcours réel a été observé sur `emulator-5554`, `sdk_gphone16k_x86_64`, Android 17 / API 37, application debug 0.6.13.8 (versionCode 60). Dépôt `C:\Users\seb\AndroidStudioProjects\IsitVegan`, branche `master`, HEAD `6c3d7796cfcb8fa747ff7a7ccfafda4a97907b42` ; état Git initial propre, diffs indexé et non indexé vides. Le rapport d'inventaire antérieur est désormais commité à ce HEAD. Les instructions `AGENTS.md`, les skills `android-validation` et `android-emulator-qa`, ainsi que `docs/ocr.md`, `docs/langues.md` et `docs/tests.md` ont cadré la validation.

La photo est présente à `app/src/androidTest/assets/benchmark/real-labels/5087.jpg` (419 514 octets, SHA-256 `0035C77089F01B0D1D20553B8BE3848AE6ECB05A6EA4E59025010BDF907083A9`) ; elle est suivie à ce HEAD. Elle a été copiée dans la galerie de l'émulateur pour l'import, sans copie dans les assets de production ni modification du fichier d'origine. Cette copie de galerie reste sur l'émulateur.

## Exécution

La première commande `.\gradlew.bat :app:assembleDebug --console=plain` a échoué avant compilation : téléchargement du JDK 25 depuis Foojay inaccessible. Une seconde exécution avec `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` et ce JDK 25 en tête du `PATH` a réussi (`BUILD SUCCESSFUL`, 38 tâches à jour). L'APK debug construite a été installée avec `adb install -r`, puis `MainActivity` lancée. Dans l'interface française : mode OCR, choisir la photo dans le sélecteur Android, image entière sans recadrage, « EXTRAIRE LE TEXTE », puis « ANALYSER ». L'import et ML Kit ont réussi. L'arbre UI a été relevé avec `uiautomator` ; le premier dump a renvoyé `null root node`, résolu après réveil et déverrouillage de l'émulateur.

## Observations

Le texte brut ML Kit est affiché en lecture seule. Il contient des fragments parasites à gauche, le haut français tronqué et dégradé, puis des lignes néerlandaises, allemandes, anglaises, suédoises et norvégiennes, et enfin une partie des valeurs nutritionnelles. Extraits bruts représentatifs, sans correction :

```text
olcoxe ructose, Farine de RLE, Acidiharn Cts (E100,É131,E163), Sirope
GEAROMATISEERD GEGELIFIEERDE SUIKERWAREN. INGREDIENTE.
Suiker Glucose-Fructosestroop, TARWEBLOEM, Voedingszuren (Appelzuur
GUMMIBONBONS MIT FRUCHTGESCHMACK. ZUTATEN: Zucker, GlukoSe
Fruktosesirup, WEIZENMEHL, Säuerungsmittel (Äpfelsäure, Citronensāure)
Dextrose, Pflanzliches 0l (Palm), Aromen, Farbstoffe (E100, E131, E163)
FLAVOURED JELLY GUMS. INGREDIENTS: Sugar, GluCOse-fructose sma
WHEAT flour, Acids (Malicaid, Citricacid), Dextrose, Vegetable oil (Palm), Flavourin
SMAKSATT GELÉGODIS. INGREDIENSER: Socker, Glukos-fruktossiza
GELEFIGURER MED FRUKTSMAK. INGREDIENSER: Sukker, Glukst
```

Ce sont des extraits et non une transcription exhaustive du résultat brut. Dans l'interface, les options de bloc proposées étaient **DE**, **EN** et **Texte complet** ; le texte éditable initial et l'analyse correspondaient au bloc allemand. La segmentation complète et les identifiants internes de tous les blocs n'ont pas été relevés ; ne pas confondre les langues discernables dans le texte brut avec des blocs effectivement sélectionnables. La fin du bloc éditable incluait également le titre `FLAVOURED JELLY GUMS.` de la section suivante.

Texte effectivement présenté dans le champ avant le premier clic d'analyse, sans édition :

```text
ZUTATEN: Zucker, GlukoSe
Fruktosesirup, WEIZENMEHL, Säuerungsmittel (Äpfelsäure, Citronensāure)
Dextrose, Pflanzliches 0l (Palm), Aromen, Farbstoffe (E100, E131, E163)
Karamellisierter Zuckersirup, Färbenden Lebensmittel (Holunderbeerextrat
Saflor, Süßkartoffel), Antioxidationsmittel (Ascorbinsäure, E306).
FLAVOURED JELLY GUMS.
```

La carte « Lecture multilingue à vérifier » affichait les concordances `Zucker ↔ Sugar`, `WEIZENMEHL ↔ WHEAT flour`, `Ascorbinsäure ↔ Ascorbic acid` et `E306 ↔ E306)`. Elle affichait aussi « Alignement ambigu : aucune correction proposée » et de nombreuses paires, par exemple `Dextrose (DE) ↔ E100 (EN)` et `Malicaid (EN) ↔ Äpfelsäure (DE)` ; il s'agit de pistes ambiguës, pas d'équivalences établies. Aucun conflit ni proposition de correction n'a été observé dans cette revue. La liste très longue d'ambiguïtés rend l'accès au champ et au bouton d'analyse coûteux en défilements ; aucun temps ni benchmark n'a été mesuré.

Le résultat initial affiché était **INCERTAIN**, avec « Selon les ingrédients au statut établi : VEGAN » et un avertissement que les incertains ou inconnus empêchent une confirmation globale. Éléments affichés comme incertains : `Säuerungsmittel → Äpfelsäure`, `Aromen` (origines possibles végétale, animale, fermentaire), et `Farbstoffe → E100`, `E131`, `E163`. Inconnus affichés : `GlukoSe`, `Fruktosesirup`, `Dextrose`, `Pflanzliches 0l` avec `Palm`, `Karamellisierter Zuckersirup`, `Färbenden Lebensmittel` avec `Holunderbeerextrat Saflor` et `Süßkartoffel`. Ces classifications rapportent l'affichage observé, sans conclure à la nature réelle du produit ni à la cause individuelle de chaque inconnu.

Après édition manuelle, insertion volontaire de `, XYZTEST` entre `Färbenden` et `Lebensmittel`, la revue multilingue a disparu et le résultat précédent a été masqué au profit de « Le texte a changé depuis la dernière analyse » et de l'instruction de relancer l'analyse. Après nouveau clic, le résultat **INCERTAIN** est revenu et `XYZTEST Lebensmittel` figurait parmi les inconnus. `XYZTEST` est une donnée simulée ajoutée pour tester la transition ; elle ne figure pas sur la photo et n'est pas une erreur OCR.

## Comparaison et limites

À la lecture de la photo, le titre allemand `ZUTATEN`, `WEIZENMEHL` et les codes E100, E131 et E163 sont visibles ; le brut les contient. L'allemand imprimé `Pflanzliches Öl` est devenu `Pflanzliches 0l` dans le brut et le champ : confusion OCR observée entre lettre et chiffre. L'anglais imprimé `Malic acid` apparaît `Malicaid` ; l'allemand `Holunderbeerextrakt` apparaît `Holunderbeerextrat`. Les bords, reflets et la partie française coupée rendent d'autres transcriptions incertaines ; aucune identité complète des compositions traduites n'est supposée. Les options DE/EN et la revue ambiguë observées sont compatibles avec les limites décrites dans `docs/ocr.md` et `docs/langues.md` ; cette seule image ne valide pas la couverture linguistique générale.

Les tests instrumentés existants `MainScreenInstrumentedTest` montent notamment une proposition synthétique ou injectent du texte ; ils ne couvrent pas l'import de cette photo, ML Kit, la segmentation, la revue et la réanalyse ensemble. Aucun test instrumenté existant couvrant ce parcours complet n'a été trouvé ni exécuté. Prévoir un test instrumenté de session avec image de test autorisée, conservation de la sortie brute et assertions sur les transitions plutôt que sur une transcription ML Kit intégralement fixe ; ajouter des fixtures déterministes pour les erreurs observées `0l`, `Malicaid`, le titre suivant et l'affichage des nombreuses ambiguïtés, puis vérifier inconnus, traces et réanalyse. Couvrir séparément les blocs FR/NL/EN/DE et ne pas inférer une correction à partir d'ancres seules.

Commandes utilisées : `git status`, `git diff --stat`, `git diff --cached --stat`, `git rev-parse HEAD`, `git ls-files`, `Get-FileHash`, `rg` ciblé ; wrapper Gradle ci-dessus ; `adb devices -l`, `getprop`, `push`, diffusion au MediaScanner, `install -r`, `am start`, `input`, `uiautomator dump` et inspection ciblée du logcat. Pas de benchmark, Nokia, campagne PIT, commit ou push. Les observations sont celles d'une seule exécution sur émulateur ; les autres blocs et leurs verdicts, les diagnostics complets et le comportement sur téléphone physique restent à vérifier.
