# Import Open Food Facts v0.7 — constat de cartographie

**Verdict : `BLOCKED`** — aucune source de données OFF locale ou snapshot OFF précisément documenté n'est disponible. Conformément à la règle d'arrêt de l'étape 1, aucun import, plan de mutation, générateur ni test n'a été exécuté.

## Dépôt et préconditions

Le dépôt ouvert est `C:\Users\Sebastien\StudioProjects\Is-it-Vegan`, branche `master`, remote `origin` : `https://github.com/hoctopuse/Is-it-Vegan.git`. `HEAD` initial : `b7bcbeaf2341d68ce4d424859bcb5ae6c1961339`. `git worktree list` montre ce worktree principal et `Is-it-Vegan-mutation-testing-pit` sur la branche `mutation-testing-pit`. L'état initial `git status --short --branch` est `## master...origin/master`, sans changement local. Le rapport `BENCHMARK_0_7_PRE_PHASE5_NOKIA_G42_BASELINE_REPORT.md` est présent et suivi par Git ; le seul commit entre la référence mesurée `9d98825d22caa9d5a8ac65406c65e424dd1ac195` et `HEAD` est `b7bcbea docs(perf): record pre-phase-5 Nokia baseline`.

## Cartographie des sources et du pipeline

Dans `knowledge/sources.json`, l'entrée `open-food-facts-docs` pointe vers `https://openfoodfacts.github.io/openfoodfacts-server/api/` et dit explicitement : « Vocabulary and future product-label test cases only. No Open Food Facts dataset is imported in this repository. » `knowledge/README.md` confirme que les données OFF ne sont pas importées. Cette URL décrit une API ; elle n'identifie aucun export ou snapshot d'ingrédients, aucune révision, date, licence de lot ou attribution propre à un jeu de données retenu. `reference-input/` contient des sources réglementaires UE et leurs manifestes, mais aucun fichier OFF. Aucun importeur OFF n'existe sous `tools/`. Le dépôt, fichier ou export OFF à importer reste donc **non identifié** ; sa licence et ses obligations de partage ou d'attribution restent **non vérifiées** pour l'usage envisagé. Aucun doublon ou collision **avec OFF** ne peut être calculé sans corpus OFF.

`knowledge/ingredients.json` est la source éditoriale des concepts (`id`, `name`, `aliases`, `status`, `reason`, `sources`, et métadonnées facultatives). `knowledge/ingredient_aliases_multilingual.json` sépare groupes d'alias (`canonicalId`, `language`, `aliases`, `ocrVariants`) et mappings révisés (`surfaceForm`, `normalizedForm`, `language`, `conceptId`, `mappingGroup`, `relation`, `source`, `confidence`). `knowledge/sources.json` définit les références de provenance. Les statuts actuels incluent `VEGAN`, `VEGETARIAN`, `NON_VEGAN` et `UNCERTAIN` ; le lexique multilingue n'attribue pas de classification. `knowledge/origin_qualifier_rules.json` porte les règles d'origine séparées.

Le flux documenté dans `docs/knowledge-pipeline.md` est `reference-input/` → importeur réglementaire → `knowledge/` → générateurs → `app/src/main/assets/`. Les assets sont générés et n'ont pas été édités. Les importeurs présents concernent les sources UE, pas OFF. Leurs modes de contrôle (`--dry-run` ou `--check`) et d'écriture (`--write`) sont documentés ; l'importeur d'arômes inspecté vérifie des seuils de base historiques, les collisions d'alias, les clés de mapping et les sources avant écriture, puis n'écrit que si son merge détecte un changement. Il ne constitue pas un importeur OFF et son idempotence n'a pas été exécutée dans cette campagne. Les générateurs `tools/build_ingredients.py`, `tools/build_multilingual_ingredient_mapping.py`, `tools/build_origin_rules.py` et `tools/build_knowledge_docs.py` disposent de contrôles `--check`, non lancés ici car l'étape 1 interdit l'exécution de scripts Python.

Les tests existants repérés comprennent `MultilingualIngredientMappingTest` pour la cohérence source/asset/runtime, `Phase2ParityBenchmarkTest` pour concepts, statuts, compositions et diagnostics, et `DiagnosticStabilizationTest` pour traces, inconnus, structure et diagnostics. Aucun nouveau cas OFF ne peut être construit sans forme source et décision éditoriale documentées.

## Comptes avant/après

Aucune donnée n'ayant été modifiée, les comptes avant et après sont identiques. Comptage direct en lecture seule des JSON éditoriaux :

| Élément | Avant | Après |
|---|---:|---:|
| Concepts | 479 | 479 |
| Chaînes d'alias dans les concepts | 2 394 | 2 394 |
| Groupes d'alias multilingues | 1 668 | 1 668 |
| Chaînes d'alias dans ces groupes | 1 990 | 1 990 |
| Variantes OCR dans ces groupes | 7 | 7 |
| Mappings révisés | 1 996 | 1 996 |

| Langue des mappings | Avant | Après |
|---|---:|---:|
| FR | 494 | 494 |
| NL | 520 | 520 |
| EN | 445 | 445 |
| DE | 470 | 470 |
| IT | 34 | 34 |
| ES | 33 | 33 |

Le benchmark antérieur rapporte 2 485 alias normalisés distincts dans le matcher au commit mesuré. Ce chiffre est **repris du rapport antérieur**, non remesuré ici ; les 2 394 chaînes d'alias du JSON éditorial sont un autre compte.

## Décisions, limites et suite nécessaire

Aucun sous-ensemble OFF, champ, langue, concept, alias ou statut n'a été retenu. Aucun doublon exact, quasi-doublon, collision ou faux positif OFF n'a été évalué. Aucune donnée produit, label, catégorie, trace ou classification déclarative OFF n'a été utilisée comme preuve de véganisme. Aucune validation d'idempotence ou de non-régression après import n'est possible puisqu'aucun import n'a eu lieu. Les tests JVM, le `--check` des générateurs et toute validation Android n'ont pas été exécutés. Les invariants métier, `VerdictEngine`, `VeganAnalyzer`, l'OCR, le matcher et les diagnostics n'ont pas été modifiés.

Pour reprendre, il faut fournir **un fichier OFF local délimité aux données d'ingrédients utiles**, ou un snapshot précisément identifié permettant de l'obtenir, avec : son URL d'origine exacte, sa date ou révision vérifiable, sa licence applicable et ses obligations d'attribution/partage, les champs et langues d'origine, et un manifeste ou hash du fichier. Une simple URL de documentation d'API ne suffit pas. La licence et la provenance devront être vérifiées avant tout plan d'import ou script. Il faudra ensuite établir les collisions et le plan éditorial sur ce fichier précis ; aucune classification vegan ne sera déduite de labels de produits.

**Fichiers modifiés :** ce rapport uniquement. Retour arrière : supprimer ce rapport non committé ; aucune base, aucun asset et aucun code n'ont à restaurer. Aucun commit ni push n'a été fait.

## Commandes exécutées et résultats

Toutes les commandes ci-dessous sont des lectures locales ; aucune commande Python, téléchargement, importeur, générateur, Gradle ou ADB n'a été exécutée.

| Commande | Résultat |
|---|---|
| `Get-Location` ; `git status --short --branch` ; `git rev-parse HEAD` ; `git branch --show-current` ; `git remote -v` ; `git worktree list` | Dépôt, branche, SHA et arbre propre confirmés. |
| `git log --oneline 9d98825d22caa9d5a8ac65406c65e424dd1ac195..HEAD` ; `git ls-files --error-unmatch BENCHMARK_0_7_PRE_PHASE5_NOKIA_G42_BASELINE_REPORT.md` ; `Test-Path BENCHMARK_0_7_PRE_PHASE5_NOKIA_G42_BASELINE_REPORT.md` | Commit documentaire unique ; rapport suivi et présent. |
| `rg --files reference-input knowledge docs` et `rg -n -i "open food facts\|openfoodfacts\|\\bOFF\\b\|odbl"` sur le dépôt ; lecture de `knowledge/sources.json` et `knowledge/README.md` | Aucun corpus OFF local ; référence API documentaire seulement. |
| `Get-Content docs/knowledge-pipeline.md -Encoding UTF8 -TotalCount 95` ; lectures ciblées de `knowledge/ingredients.json`, `knowledge/ingredient_aliases_multilingual.json`, `tools/import_eu_flavourings_regulation.py` ; `rg` ciblés sur `tools/` et les tests | Schéma, importeurs, générateurs et tests cartographiés sans exécution. |
| `Get-Content ... -Raw -Encoding UTF8 \| ConvertFrom-Json` sur les deux JSON éditoriaux, puis comptage PowerShell | 479 concepts, 1 996 mappings, autres comptes détaillés ci-dessus. |
| `git status --short --branch` après la cartographie | Arbre encore propre avant création de ce rapport. |
