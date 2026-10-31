# Preuves de la reprise d’audit du 31 octobre 2026

Base : `ee499e53413769a42f8459587742409b24b7ee70`, branche master. [Suivi complet](SUIVI_AUDIT_ET_PLAN_ACTION_20261031.md) et [architecture](ARCHITECTURE_MOTEUR_CIBLE_20261031.md). Cette annexe consigne des observations, sans correction de sources/tests/configuration/assets/version.

## Inspection initiale et corrections intervenues

Commandes de lecture : `Get-Content AGENTS.md`, `git rev-parse HEAD`, `git status --short`, `git status --porcelain=v1 --untracked-files=all`, `git diff`, `git diff --cached`, `git ls-files --others --exclude-standard`, `git branch --show-current`, `git worktree list`, `git log -7 --oneline`, `git show --stat d97a862`. Statut/diffs/non-suivis vides au départ ; aucun bump en cours. Version actuelle déjà dans HEAD : `0.6.13.12`, code 64. Aucun accès au second worktree déclaré.

`d97a862` touche notamment `OcrFirstScreen`, `OcrSession`, `VerdictExplanationFormatter`, `DiagnosticModels`, service JVM, tests session/inconnus, deux nouvelles suites instrumentées et documentation. Inspection des gardes de génération et handlers d’invalidation, de l’état stale monotone jusqu’à nouvelle analyse, du filtre des inconnus et du rendu ; pas de réimplémentation.

Les chemins donnés dans les rapports sont ceux des sources actuelles. Les commandes `rg` ont ciblé source sets et fichiers Gradle/docs ; les sorties générées ont été consultées seulement pour résultats XML, classes et jar nécessaires aux reproductions. Aucun log de benchmark/PIT utilisé comme preuve fonctionnelle actuelle.

## Tests réellement exécutés

Commande unique exécutée depuis la racine avec le wrapper Windows :

```powershell
.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.UnresolvedExplanationTest --tests com.example.isitvegan.DiagnosticOutput093Test --tests com.example.isitvegan.OcrBoundarySafetyTest --tests com.example.isitvegan.OcrChosenTextRegressionTest --tests com.example.isitvegan.LabelSectionExtractorTest --tests com.example.isitvegan.HierarchyAnalysisTest --tests com.example.isitvegan.MultilingualIngredientAlignmentGenerationTest --tests com.example.isitvegan.DiagnosticStabilizationTest :app:testDebugUnitTest --tests com.example.isitvegan.OcrSessionTest --tests com.example.isitvegan.UnresolvedRealKnowledgeTest --tests com.example.isitvegan.UncertainUnknownVisibility097Test --tests com.example.isitvegan.OcrMultilingualSelectionTest --tests com.example.isitvegan.OcrTextCleanerTest --tests com.example.isitvegan.OcrNativeOrderRegressionTest --tests com.example.isitvegan.CrossContactTest --console=plain
```

Résultat : **BUILD SUCCESSFUL**, deux tâches de test exécutées, 27 tâches de préparation/compilation à jour ; les tests n’étaient pas simplement UP-TO-DATE. Comptes lus après exécution dans les XML `mutation-core/build/test-results/test/TEST-*.xml` et `app/build/test-results/testDebugUnitTest/TEST-*.xml` :

| Module | Classe | Tests | Échecs / erreurs / skips |
|---|---|---:|---|
| core | DiagnosticOutput093Test | 5 | 0 / 0 / 0 |
| core | DiagnosticStabilizationTest | 6 | 0 / 0 / 0 |
| core | HierarchyAnalysisTest | 9 | 0 / 0 / 0 |
| core | LabelSectionExtractorTest | 9 | 0 / 0 / 0 |
| core | MultilingualIngredientAlignmentGenerationTest | 9 | 0 / 0 / 0 |
| core | OcrBoundarySafetyTest | 6 | 0 / 0 / 0 |
| core | OcrChosenTextRegressionTest | 9 | 0 / 0 / 0 |
| core | UnresolvedExplanationTest | 3 | 0 / 0 / 0 |
| app | CrossContactTest | 13 | 0 / 0 / 0 |
| app | OcrMultilingualSelectionTest | 19 | 0 / 0 / 0 |
| app | OcrNativeOrderRegressionTest | 1 | 0 / 0 / 0 |
| app | OcrSessionTest | 15 | 0 / 0 / 0 |
| app | OcrTextCleanerTest | 5 | 0 / 0 / 0 |
| app | UncertainUnknownVisibility097Test | 1 | 0 / 0 / 0 |
| app | UnresolvedRealKnowledgeTest | 1 | 0 / 0 / 0 |
| **Total** | **56 core + 55 app** | **111** | **0 / 0 / 0** |

Autre commande : `python tools/build_ingredients.py --check` → connaissance valide et asset courant. Contrôle en lecture seule, pas de régénération. Ce contrôle n’est pas une preuve exhaustive des règles d’origine ou de couverture linguistique.

Tests Android **inspectés seulement** : `SessionSafetyInstrumentedTest` (13 @Test) et `OcrProgressInstrumentedTest` (11 @Test), plus références aux tests OCR/explication existants. Les scénarios contrôlent notamment callbacks hors ordre, effacement puis texte identique, changements langue/bloc, ancienne erreur, progression, photo de même URI, édition et retour au texte initial. Aucun résultat instrumenté de cette passe ; ne pas transférer les résultats d’un autre HEAD. Aucun assembleDebug/assembleDebugAndroidTest dédié ni suite entière lancé ici.

## Reproductions directes sans appareil et sans nouveau fichier de code/test

Après les tests, JShell a invoqué les **classes compilées existantes** avec les trois assets réels. Le jar app est utilisé seulement pour la sélection pure `OcrTextSelection`, sans initialisation d’Android. Java/JShell trouvés dans `C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin/`. Le cœur est compilé pour JVM 17, Kotlin stdlib 2.4.20 locale. Aucun téléchargement, compilation d’un moteur copié, modification de source ou harness persistant.

Préparation PowerShell et invocation de lecture reproductible (extraits Java équivalents aux commandes interactives exécutées, regroupés pour faciliter le rejeu) :

```powershell
$stdlib = Get-ChildItem "$env:USERPROFILE/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.4.20" -Recurse -Filter 'kotlin-stdlib-2.4.20.jar' | Select-Object -First 1 -ExpandProperty FullName
$auditClasspath = "mutation-core/build/classes/kotlin/main;app/build/intermediates/compile_app_classes_jar/debug/bundleDebugClassesToCompileJar/classes.jar;$stdlib"
@'
import com.example.isitvegan.*;
import java.nio.file.*;
var a = Path.of("app/src/main/assets");
var k = IngredientKnowledge.Companion.fromJson(Files.readString(a.resolve("ingredients.json")), Files.readString(a.resolve("ingredient_aliases_multilingual.json")), Files.readString(a.resolve("origin_qualifier_rules.json")));
var svc = new IngredientAnalysisService(k);
void probe(String s) { for (var mode : InputMode.values()) { var d=svc.analyzeWithDiagnostics(s,mode,UiLanguage.FR,null); System.out.println(mode+" | "+s+" | detailed="+d.getResult().getVerdict()+" | vegan="+d.getResult().getVeganAssessment()+" | presence="+d.getDeclaredPresenceText()+" | ids="+d.getResult().getMatched().stream().map(i->i.getId()).toList()+" | unknown="+d.getResult().getUnknown()+" | visible="+DiagnosticModelsKt.getVisibleUnknownIngredients(d)+" | traces="+d.getCrossContactWarnings()); } }
probe("Ingredients: eau. Contient: soja. Peut contenir: noix. Contient: lait.");
probe("Ingredients: eau. Contient: soja. Peut contenir: noix. Contient: gelatine.");
probe("Ingredients: eau. Contient: soja. Peut contenir: noix. Contient: mystere.");
probe("Ingredients: eau. Peut contenir: noix Contient: lait.");
probe("Ingredients: eau. Contient: lait.");
probe("Ingredients: eau. Contient: soja. Contient: lait.");
probe("Ingredients: eau. Peut contenir: noix. Contient: lait.");
probe("Ingredients: eau, sucre");
probe("Ingredients: eau, sucre, lait");
probe("Ingredients: eau inhabituelle. Peut contenir: lait.");
probe("Ingredients: lait, mystere");
probe("Ingredients: lait, E471");
var text="FR Ingr\u00e9dients: eau\nFR Ingr\u00e9dients: eau, sucre, sel, tomate, farine de bl\u00e9.".replace("\\u00e9","\u00e9");
var seg=LabelLanguageSegmenter.INSTANCE.segment(text,UiLanguage.FR);
var c=OcrTextSelection.INSTANCE.from(seg,text);
System.out.println("blocks="+seg.getBlocks().stream().map(b->b.getLanguage()+":"+b.getId()+":"+b.getSelectionScore()).toList()+" | selected="+c.getSelectedBlockId()+" | options="+c.getOptions().stream().map(o->o.getLanguage()+":"+o.getBlockId()).toList()+" | editableIsOption="+c.getOptions().stream().anyMatch(o->o.getText().equals(c.getEditableText())));
var repeated="FR Ingr\u00e9dients: eau, sucre\nFR Ingr\u00e9dients: eau, sucre\nNL Ingredi\u00ebnten: water, suiker".replace("\\u00e9","\u00e9").replace("\\u00eb","\u00eb");
var dup=LabelLanguageSegmenter.INSTANCE.segment(repeated,UiLanguage.FR);
System.out.println(dup.getBlocks().stream().map(b->b.getLanguage()+":"+b.getId()+":"+b.getSegmentId()).toList());
try {System.out.println(svc.generateMultilingualAlignment(dup.getBlocks()));} catch(Exception e) {System.out.println(e);}
System.out.println(k.getMultilingualLexicon().validateAgainst(k.getIngredients()).getUnavailableCanonicalIds());
/exit
'@ | jshell --class-path $auditClasspath -
```

Les chaînes sans accent `Ingredients` sont volontaires et reconnues par le moteur. Les escapes Unicode du cas FR permettent de conserver le titre français dans le pipe PowerShell. Une première tentative avec accents littéraux a reçu des `?` par encodage du pipe : ces sorties ont été rejetées, pas utilisées comme constats. Rejeu en ASCII/escapes a établi les résultats ci-dessous. Une première recherche javap dans l’ancien chemin tmp des classes app n’a rien trouvé ; utilisation du jar courant produit par AGP ensuite réussie. Aucun échec de test ni de build à corriger.

| Reproduction | Entrée et observation réelle | Portée de preuve et action |
|---|---|---|
| **R1 — présence après trace perdue** | `Ingredients: eau. Contient: soja. Peut contenir: noix. Contient: lait.` : **les trois modes** donnent detailed=VEGAN, vegan=VEGAN, matched `[water, soy]`, présence `soja.`, unknown `[]`, traces `[Peut contenir: noix.]`. Même constat confirmé avec titre français accentué transmis par escapes. | Défaut moteur réellement appelé par app, pas UI rejouée. Le texte contient explicitement lait mais il n’atteint pas le résultat. P2-5 promu P1, lot B1. |
| **R2 — autres présences perdues** | Même patron avec `gelatine` puis `mystere` : en FULL_LABEL, VEGAN, `[water, soy]`, inconnus/visibles vides. | Écarte une particularité au lait ; animal et inconnu peuvent être omis. Ces deux variantes ont été exécutées en FULL_LABEL seulement, le script de rejeu proposé élargit aux trois modes. Attendus de régression : gélatine non végétarienne ; mystere INCONCLUSIVE et visible. |
| **R3 — frontière sans point** | `Ingredients: eau. Peut contenir: noix Contient: lait.` : les trois modes donnent VEGETARIAN/NOT_VEGAN, `[water,milk]`, mais traces contiennent aussi `Contient: lait.`. | Ici lait n’est pas perdu pour la décision ; contamination du texte de trace, liée au même contrat de frontières. B1. |
| **Témoins R1/R3** | `Ingredients: eau. Contient: lait.` : VEGETARIAN/NOT_VEGAN dans les trois modes. `... Contient: soja. Contient: lait.` sans trace intercalée : lait conservé en FULL_LABEL. `Ingredients: eau. Peut contenir: noix. Contient: lait.` avec une seule déclaration : lait conservé en FULL_LABEL. | Ne pas conclure que toutes les déclarations répétées ou toute présence après une trace sont perdues ; l’entrelacement après une première présence déclenche la perte démontrée. |
| **R4 — option différente de l’initiale** | `FR Ingrédients: eau\nFR Ingrédients: eau, sucre, sel, tomate, farine de blé.` : blocs FRENCH `block-54c06005` score -22 et `block-22b18f2` score 39 ; selected=second ; options contient uniquement premier ; `editableIsOption=false`. | Sélection pure réelle de l’app. L’inaccessibilité du bouton « texte complet » avec ≤1 option est prouvée par retour anticipé de la fonction Compose, pas par capture d’écran. B2. |
| **R5 — occurrences dupliquées/revue** | Deux blocs FR identiques `Ingrédients: eau, sucre` suivis de `NL Ingrediënten: water, suiker` : IDs FR identiques `block-751803cd`, segmentId distincts `segment-2-27` et `segment-29-54`, NL `block-b92d8e40`. Génération lève `java.lang.IllegalArgumentException: Ingredient occurrence ids must be unique.` | Deux blocs de même langue seuls donnent revue vide (garde de langues), donc pas d’exception dans ce sous-cas. L’ajout d’une autre langue déclenche la preuve utile. OcrProcessor isole l’erreur et expose indisponibilité selon inspection ; rendu non rejoué. B5a coordonné B2. |
| **R6 — fragment en apparence complet** | `Ingredients: eau, sucre` → VEGAN dans les trois modes ; avec `, lait` → VEGETARIAN/NOT_VEGAN. | Limite d’acquisition, pas détection d’une fin perdue possible à partir du seul texte. B3/B6, ne pas déclarer les listes courtes invalides par principe. |
| **R7 — inconnus corrigés** | `Ingredients: eau inhabituelle. Peut contenir: lait.` en FULL_LABEL : INCONCLUSIVE, unknown `[eau inhabituelle]`, visible `[inhabituelle]`, matched `[water]`. J réel séparé confirme lait uniquement dans traces. | Confirme correction P1-3 avec assets réels, sans prétendre à une preuve de tous les cas ni du rendu Android. |
| **R8 — doubles sorties de verdict** | En FULL_LABEL : `Ingredients: lait, mystere` → detailed INCONCLUSIVE, vegan NOT_VEGAN, visible `[mystere]`, milk VEGETARIAN. `Ingredients: lait, E471` → detailed UNCERTAIN, vegan NOT_VEGAN, milk VEGETARIAN + e471 UNCERTAIN. | Contrat distinct actuel, pas fausse conversion du lait en vegan. Le rendu actif privilégie le détaillé ; arbitrage B3, sans changer implicitement les invariants. |
| **R9 — cible canonique absente** | `k.multilingualLexicon.validateAgainst(k.ingredients).unavailableCanonicalIds` → `[cereals]`. | Dette réelle de données/validation ; n’établit pas que chaque forme de cereals est inconnue, le matcher peut reconnaître autre chose. B7, aucun import/modification réalisé. |

Le code concerné par R1–R3 est `LabelSectionExtractor.extract` : premier marqueur CONTAINS, `declaredContains` arrêté à TRACES/IGNORED/INGREDIENTS ; pas de collecte des présences ultérieures. `traceSections` ne prend pas CONTAINS comme borne maximale, `traceEnd` s’appuie aussi sur la ponctuation. Le service prétraite ensuite seulement `sections.declaredContainsText`. Ceci explique les résultats sans modifier le cœur pour les observer.

## Limites et conservation

Pas de photo réelle, installation neuve hors ligne, rotation/process death, ressources ML Kit sous annulation, export système ou runtime UI exécuté. Pas de mesure de couverture linguistique, collision de hash entre contenus différents, latence/jank/mémoire, adoption d’index ni compatibilité PIT actuelle prétendue. Les preuves statiques, JVM et UI future sont distinguées dans le tableau de suivi.

Contrôle final : HEAD et version inchangés ; seuls les trois nouveaux rapports de cette reprise sont non suivis. Aucun fichier suivi modifié, aucun fichier ajouté à l’index ; les rapports historiques sont conservés. Aucun commit/push.
