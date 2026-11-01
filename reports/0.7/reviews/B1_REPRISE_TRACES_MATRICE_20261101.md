# B1 — reprise après une trace imbriquée

Date : 1 novembre 2026. HEAD : `23bf064f5b3375cfe12abb7fa86248f11f5a8d7e`.
Suite des rapports `SUIVI_AUDIT_ET_PLAN_ACTION_20261031.md` et `PREUVES_REPRISE_AUDIT_20261031.md`.

## État initial et reproduction

AGENTS.md et les skills android-validation/android-emulator-qa lus. Changements existants conservés, dont ExplicitPresenceRegressionTest.kt non suivi et les tests Android B1. Aucun commit, push, changement de version ou d’asset métier.

La commande suivante a forcé la recompilation des sources courantes avant modification de production ; les six sondes (deux textes × trois modes) ont été calculées avant les assertions :

```powershell
.\gradlew.bat :mutation-core:compileKotlin :app:testDebugUnitTest --tests com.example.isitvegan.ExplicitPresenceRegressionTest.bracketAndDashRegressionsWithCurrentRealKnowledge --rerun-tasks --console=plain
```

Échec attendu du test, avec les trois assets réels. Résultats identiques en MANUAL_INGREDIENT_LIST, FULL_LABEL et OCR_LABEL :

| Texte | Avant correction | Après correction |
|---|---|---|
| `Ingredients: eau [Peut contenir: noix], lait.` | Section `eau [` ; reconnus `[water]`, inconnus vides, trace `Peut contenir: noix`, VEGAN / VEGAN, sans ambiguïté signalée. | Lait conservé dans le texte extrait et reconnu ; VEGETARIAN / NOT_VEGAN ; noix uniquement dans la trace. |
| `Ingredients: eau (Peut contenir: noix) - lait.` | Section contenant `eau` et `- lait.` ; reconnus `[water]`, inconnus vides, trace `Peut contenir: noix`, VEGAN / VEGAN, sans ambiguïté signalée. | Frontière transformée en séparateur de parsing ; lait reconnu ; VEGETARIAN / NOT_VEGAN ; noix uniquement dans la trace. |

## Contrat et matrice

Le groupe n’appartient à la trace que si le marqueur suit immédiatement son ouverture, hors espaces. Un empilement typé vérifie parenthèses, crochets et enveloppes mixtes. Le masquage conserve la longueur et le texte brut ; une frontière reconnue devient un séparateur de parsing. Une portée non établie conserve le texte et bloque la confirmation.

`NestedTraceContinuationMatrixTest` exécute **2 496 lignes paramétrées** avec les assets réels :

- Parenthèses, crochets, enveloppes `([…])` et `[(…)]`, avec parenthèses dans le contenu de trace.
- Début, milieu et fin de composition ou de déclaration ; reprise par virgule, point-virgule, point, tirets `-`, `–`, `—`, retour à la ligne, espaces seuls, juxtaposition et `/`.
- Lait, E471, mystere et sucre ; les deux verdicts sont contrôlés, et l’inconnu reste visible lorsque l’analyse est possible.
- Composition et déclaration dans les trois modes ; liste sans titre en mode manuel.
- Délimiteurs manquants, discordants, croisés et surnuméraires : ambiguïté, texte conservé, aucune confirmation vegan.

Chaque ligne porte UNDERSTOOD ou AMBIGUOUS et contrôle extraction, reconnaissance, traces, inconnus, verdicts et cohérence des positions du texte brut. Les familles équilibrées comprises n’exposent pas de délimiteur isolé dans le texte extrait. Les témoins supplémentaires couvrent traces seules, groupe dans un composite, trace après un autre ingrédient dans le même groupe (portée non établie), déclarations alternées, E471, frontière « Barre chocolat » et simple retour à la ligne.

Une extension initiale du chemin manuel a fait échouer `CrossContactTest.compositionFollowingAWarningIsStillAnalyzed` : la gélatine après une trace autonome terminée par un point était perdue. Correction : le chemin manuel conserve le traitement historique des mentions autonomes par le prétraitement ; seuls les groupes imbriqués utilisent le nouveau masquage commun. Le témoin et les régressions de liste sans titre passent ensuite.

## Validation exécutée

Validation de la passe initiale B1 : tâches `:mutation-core:test`, `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest` avec les filtres ci-dessous ; **BUILD SUCCESSFUL**. La correction issue de la relecture est consignée séparément plus bas.

| Périmètre | Classes sélectionnées | Tests / échecs / erreurs / ignorés |
|---|---|---|
| Cœur JVM | LabelSectionExtractorTest, DiagnosticStabilizationTest, ParserModulesTest, OcrBoundarySafetyTest, OcrChosenTextRegressionTest, UnresolvedExplanationTest, VerdictExplanation069Test | 67 / 0 / 0 / 0 |
| Application JVM | NestedTraceContinuationMatrixTest, ExplicitPresenceRegressionTest, CrossContactTest, OcrSessionTest, UnresolvedRealKnowledgeTest, UncertainUnknownVisibility097Test | 2 541 / 0 / 0 / 0 |

Comptes lus dans les XML de cette exécution : matrice 2 496, ExplicitPresenceRegressionTest 15 ; total JVM ciblé **2 608**. La suite entière du dépôt n’a pas été relancée.

APK debug et APK AndroidTest courants installés avec `adb -s emulator-5556 install -r`. Émulateur : **B1_Test**, `sys.boot_completed=1`. Instrumentation ciblée :

```powershell
adb -s emulator-5556 shell am instrument -w -r -e class 'com.example.isitvegan.MainScreenInstrumentedTest#interleavedDeclaredPresenceAndTracesReachTheRenderedResult,com.example.isitvegan.MainScreenInstrumentedTest#embeddedTraceResumptionKeepsMilkInRenderedVerdict' com.example.isitvegan.test/androidx.test.runner.AndroidJUnitRunner
```

Résultat effectif : **OK (2 tests)**, 24,485 secondes. Rendu des variantes lait/gélatine/inconnu entre déclarations et traces, puis des deux nouvelles reprises avec lait. Aucun appel ciblant le Nokia.

MkDocs : `.\.venv-docs\Scripts\mkdocs.exe build --strict` réussi. `git diff --check` sans erreur. Contenu UTF-8, caractères de remplacement, fin de fichier et espaces des nouveaux fichiers vérifiés séparément, car les non-suivis sont absents du diff normal.

## Fichiers et limites

Changements de cette passe : LabelSectionExtractor.kt, IngredientAnalysisService dans VeganAnalyzer.kt, ExplicitPresenceRegressionTest.kt, nouveau NestedTraceContinuationMatrixTest.kt, extension de MainScreenInstrumentedTest.kt, docs/architecture.md, docs/changelog.md et ce relevé. Les changements précédents, dont LabelSectionExtractorTest.kt, sont conservés.

Espaces seuls, juxtaposition ou signe inconnu avant un ingrédient : reprise volontairement non interprétée ; texte conservé et analyse suspendue. Le texte ne permet pas de prouver l’identité du produit ; les préambules simples et la frontière « Barre chocolat » restent des règles structurelles limitées. La matrice de reprise utilise des formulations françaises ; les témoins de verdict FR/NL/EN/DE existants sont rejoués. Pas d’acquisition photo réelle, de preuve générale sur toute prose, ni de validation instrumentée de chaque ligne de matrice.

## Correction issue de la relecture : brut des compositions implicites

Même HEAD, le 1 novembre 2026. Défaut reproduit avant correction par trois nouveaux tests de `LabelSectionExtractorTest` :

```powershell
.\gradlew.bat :mutation-core:test --tests 'com.example.isitvegan.LabelSectionExtractorTest.implicitComposition*' --console=plain
```

Résultat avant correction : **3 tests, 3 échecs**. Pour `eau, sucre [Peut contenir: noix], sel, farine, lait.`, `IngredientSection.rawText` contenait le texte masqué à la place de la sous-chaîne source. Le cas analogue avec parenthèses échouait aussi. Le témoin sans trace révélait en outre des positions incorrectes lorsque le texte commençait par des espaces ou un retour à la ligne.

Correction limitée à la construction de la section implicite : conservation de la longueur du texte extrait avant retrait des espaces initiaux, calcul de `start` et `end`, puis prélèvement du brut dans le texte source. Le texte masqué d’extraction et d’analyse reste identique. Aucun changement de règles de reconnaissance, d’assets métier ou de version.

Les trois tests couvrent crochets, parenthèses et absence de trace, chacun avec et sans espaces initiaux. Ils vérifient le brut exact, les positions et les sous-chaînes des traces ; dans les trois modes, les cinq ingrédients eau/sucre/sel/farine/lait restent reconnus, les traces restent séparées, sans inconnus ni ambiguïté ajoutée. La base de ces tests est synthétique ; les témoins applicatifs B1 utilisent les assets réels.

Après correction, la même commande ciblée réussit : **3 tests, 0 échec**.

Validation finale exécutée et attendue jusqu’à sa terminaison :

```powershell
.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.LabelSectionExtractorTest --tests com.example.isitvegan.DiagnosticStabilizationTest --tests com.example.isitvegan.ParserModulesTest --tests com.example.isitvegan.OcrBoundarySafetyTest --tests com.example.isitvegan.OcrChosenTextRegressionTest --tests com.example.isitvegan.UnresolvedExplanationTest --tests com.example.isitvegan.VerdictExplanation069Test :app:testDebugUnitTest --tests com.example.isitvegan.NestedTraceContinuationMatrixTest --tests com.example.isitvegan.ExplicitPresenceRegressionTest --tests com.example.isitvegan.CrossContactTest --tests com.example.isitvegan.OcrSessionTest --tests com.example.isitvegan.UnresolvedRealKnowledgeTest --tests com.example.isitvegan.UncertainUnknownVisibility097Test :app:assembleDebug --console=plain
```

**BUILD SUCCESSFUL en 3 min 5 s**. XML de cette exécution : **70 tests cœur + 2 541 tests application = 2 611 tests**, aucun échec, erreur ou ignoré. Les classes sont celles de la passe initiale ci-dessus ; `LabelSectionExtractorTest` passe de 12 à 15 tests. La matrice de **2 496 cas** est rejouée avec succès, ainsi que les 15 régressions de présence, les témoins de traces autonomes, de session, d’inconnus et d’incertitude. APK debug recompilé avec succès.

`git diff --check` réussi. Les trois fichiers non suivis nécessaires sont conservés et contrôlés séparément : décodage UTF-8 strict, absence de caractère de remplacement, d’espace final et de marqueur de conflit, présence de la fin de ligne finale. Aucun commit, push, installation ou appel à un appareil. La suite complète et MkDocs ne sont pas relancés pour cette correction ; les résultats Android et MkDocs plus haut restent ceux de la passe initiale, sans nouvelle preuve de runtime Android après cette correction des métadonnées de source. `docs/architecture.md` précise le contrat de sous-chaîne source pour les compositions implicites.
