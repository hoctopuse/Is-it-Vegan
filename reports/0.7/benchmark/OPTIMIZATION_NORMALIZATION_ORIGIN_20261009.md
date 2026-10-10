# Optimisation cibl?e de la normalisation ? 9 octobre 2026

## Point de d?part et p?rim?tre

Commit de d?part complet : `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`.
Worktree de changement : `.benchmark-worktrees/normalize-origin-3ec32d3`, HEAD d?tach?, initialement vierge.
Worktree de contr?le des deux ?checs : `.benchmark-worktrees/normalize-origin-baseline-3ec32d3`, m?me commit, aucune modification de source.
Le d?p?t principal reste sur `master`, HEAD `528a9dbf26a917a0d008d0f05f4b429129d1d49a`, avec uniquement `.benchmark-worktrees/` non suivi ? l'inspection initiale et finale ; aucun diff staged/unstaged suivi. Les worktrees pr?/post-import existants sont conserv?s et n'ont pas ?t? modifi?s.

Instructions consult?es : AGENTS.md, skills android-validation et benchmark avec sa r?f?rence de workflow. Rapports locaux consult?s depuis le d?p?t principal : `PROTOCOL_REAL_LABEL_MULTILINGUAL_NOKIA.md`, `RESULTS_MEAT_IMPORT_ANALYSIS_NOKIA.md` et `SIMPLEPERF_SYNCHRONIZED_ATTEMPTS_20261008.md`. Les suffixes des pi?ces jointes ne sont pas n?cessaires : leurs versions locales existent.

## Changement

- `mutation-core/src/main/kotlin/com/example/isitvegan/TextNormalizer.kt` : les trois motifs inchang?s sont d?sormais des `private val Regex` de l'objet Kotlin. Publication unique et s?re de l'objet ; les op?rations replace cr?ent leurs matchers par appel. Aucun matcher mutable partag?. Ordre lowercase, substitutions ?/?, NFD, marques combinatoires, ponctuation, trim puis pr?fixe E inchang?.
- `mutation-core/src/main/kotlin/com/example/isitvegan/OriginQualifierRules.kt` : normalisation de l'entr?e une fois par extraction, de la cible une fois par it?ration de cible et des qualificatifs une fois par outcome de r?gle avant la boucle des cibles. Les listes locales conservent les cha?nes originales pour les diagnostics et l'ordre r?gle/cible/outcome/qualificatif. Les outcomes sans DIRECT_SUFFIX sont filtr?s dans le m?me ordre. Aucun cache persistant ou g?n?ral, aucune d?pendance ajout?e.
- `mutation-core/src/test/kotlin/com/example/isitvegan/NormalizationSemanticsTest.kt` : deux nouveaux tests de s?mantique, ex?cut?s ?galement avant modification du code m?tier.

Les r?gles charg?es depuis JSON sont priv?es et immuables. Les valeurs normalis?es d?pendent des cha?nes, pas de la cible ou du qualificatif courant lorsqu'elles sont sorties de leur boucle. La comparaison conserve exactement un espace entre cible normalis?e et qualificatif normalis? ; la cha?ne concat?n?e n'est pas renormalis?e. L'extraction parenth?tique, collectMatches, sameText, distinctBy, ambigu?t?s singleOrNull et fallback original ne sont pas modifi?s. Les appelants directs (parser, service/analyzer, matcher, lexiques, index et consommateurs de normalisation) ont ?t? inspect?s, sans changement.

Diff des deux fichiers de production : 21 lignes ajout?es, 9 supprim?es. Aucun verdict attendu existant n'a ?t? chang?.

## Validation r?ellement ex?cut?e

H?te Windows/PowerShell, wrapper Gradle du worktree ; JBR local Android Studio 25.0.3, toolchain JVM du module 17, SDK local. Initialisation commune :

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android/Sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

Depuis le worktree de changement, avant modification de production :

```powershell
.\gradlew.bat :mutation-core:test --tests '*ParserModulesTest' --tests '*IngredientTreeParserTest' --tests '*Version059CompletionTest' --tests '*Diagnostic*Test' :app:testDebugUnitTest --tests '*Version0591Test' --console=plain
.\gradlew.bat :mutation-core:test --tests '*NormalizationSemanticsTest' --console=plain
```

Les tests existants passent : 35 core + 11 app. Le nouveau test a initialement eu deux attentes erron?es successivement r?v?l?es par le code de r?f?rence : `E471 ((plant-based))` puis `E471 (plant-based` sont d?j? qualifi?s plant. Les logs/XML de ces ?checs de test ajout? sont conserv?s ; les assertions ont ?t? corrig?es selon les r?sultats du commit non optimis?, sans changement de production. Le dernier passage de r?f?rence des deux nouveaux tests r?ussit. Cela pr?serve m?me ces comportements tol?rants ; cette optimisation ne les corrige pas.

Apr?s optimisation :

```powershell
.\gradlew.bat :mutation-core:test --tests '*ParserModulesTest' --tests '*IngredientTreeParserTest' --tests '*Version059CompletionTest' --tests '*Diagnostic*Test' --tests '*NormalizationSemanticsTest' :app:testDebugUnitTest --tests '*Version0591Test' --console=plain
.\gradlew.bat :mutation-core:test --console=plain
.\gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin --console=plain
```

- Cibl?s : BUILD SUCCESSFUL, 48 tests r?ussis ; comparaison des noms et statuts JUnit avant/apr?s identique.
- Suite core compl?te : 112 tests, 110 r?ussis, 2 ?checs (suite non verte).
- Compilation Kotlin app et androidTest : BUILD SUCCESSFUL. Aucun test instrument? ex?cut? et aucune APK install?e.

Les deux ?checs ont ?t? v?rifi?s uniquement sur le worktree de contr?le vierge, sans investigation ni correction de leurs causes :

```powershell
.\gradlew.bat :mutation-core:test --tests '*KnowledgeIndexTest.currentJsonConvertsAllConceptsAndMappingsWithoutChangingIdentity' --tests '*Phase4MatcherProfileTest.linkedAliasPrecomputationIsEquivalentToTheFormerPairwisePredicate' --console=plain
```

R?sultats identiques avant/apr?s :

| Test | ?chec pr?existant |
|---|---|
| KnowledgeIndexTest.currentJsonConvertsAllConceptsAndMappingsWithoutChangingIdentity | expected 482, actual 488 |
| Phase4MatcherProfileTest.linkedAliasPrecomputationIsEquivalentToTheFormerPairwisePredicate | expected 2485, actual 2574 |

Attention : la commande de suite compl?te demand?e a aussi ex?cut? le test JVM de profilage `Phase4MatcherProfileTest.profilesMatcherStagesAndKeepsInstrumentedResultsIdentical`, qui contient des chronom?trages internes. Ils ne constituent pas un benchmark autoris? de cette optimisation et ne sont ni exploit?s, ni compar?s, ni rapport?s comme gain. Aucune campagne de mesure apr?s optimisation n'a ?t? lanc?e. Pour une future validation purement fonctionnelle, s?parer ce test chronom?tr? de la suite utilis?e, sans modifier ici ses sources.

Cas v?rifi?s : accents pr?compos?s/d?compos?s, ligatures, casse, ponctuation, blancs/tabulations/sauts de ligne, cha?nes vides, nombres apr?s E en t?te uniquement, parenth?ses simples/doubles/incompl?tes, qualificatifs FR/EN/NL/DE et ES via les tests existants, absence de qualificatif, suffixe suppl?mentaire, Vegetable oil (Palm) sans invention d'une r?solution, verdicts/diagnostics/traces et m?tadonn?es structur?es via les tests m?tier existants. Le test compare l'objet complet de qualification entre variantes, y compris phrase d?tect?e et raison.

## Artefacts et garde-fous

Logs et XML conserv?s sous `mutation-core/build/optimization-validation/` du worktree de changement : `baseline.log`, `semantics-initial.xml`, `semantics-second.xml`, logs semantics-baseline*, `after-targeted.log`, `full-core.log`, `baseline-known-failures.log`, `compile.log`, dossiers `baseline-xml/`, `after-targeted-xml/`, `full-core-xml/` et `validation-summary.json`. Ils sont g?n?r?s/ignor?s et non ajout?s ? Git.

Relecture du diff complet et `git diff --check` r?ussies. Le contr?le des chemins knowledge/, app/src/main/assets/, app/src/androidTest/ et des scripts/protocoles benchmark ne montre aucun changement. Seuls deux fichiers de production, un nouveau test JVM et ce rapport sont modifi?s/ajout?s dans le worktree isol?. Aucun changement de donn?es, r?gle, segmentation, version, test instrument?, script Simpleperf ou protocole. Aucune photo copi?e ; aucun OCR/ML Kit ex?cut?. Aucun appel ADB, aucune capture, installation, commit ou push dans ce chantier. Les worktrees restent disponibles.

## Comparaison Nokia ult?rieure

Le gain reste une hypoth?se : moins de compilations Regex et de normalisations r?p?t?es, sans gain quantifi?. Les tests JVM et la compilation ne mesurent pas les performances Android.

Pour une phase ult?rieure autoris?e, reprendre le protocole existant, copier uniquement le m?me harness texte dans un worktree de r?f?rence ? 3ec32d3 et dans le worktree optimis?, v?rifier les diff et hashes du harness/assets et identifier explicitement la r?vision plus le patch non commit?. Ne copier aucune photo. Utiliser le m?me Nokia AQ5003H044Q32600084, runner, build debug, corpus corrig?/variantes, premi?re invocation document?e, 5 ?chauffements et 10 mesures warm. Relever conditions thermiques/batterie/USB, bruts et XML, assertions fonctionnelles identiques, construction matcher/extraction/parsing/analyse compl?te. Refaire les deux passages dans des conditions comparables plut?t que d?duire un gain des anciens profils. Un profil synchronis? UI identique peut ensuite v?rifier la r?duction du hotspot ; ses pourcentages inclusifs ne sont pas additifs et ne deviennent pas des dur?es exactes.

Conclusion : optimisation minimale appliqu?e, parit? des tests cibl?s v?rifi?e ; la suite core reste limit?e par deux ?checs d?montr?s pr?existants. Aucun gain de performance mesur? ou revendiqu?.
