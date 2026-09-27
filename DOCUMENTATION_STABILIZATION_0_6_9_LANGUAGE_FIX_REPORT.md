# Correction documentaire des langues UI 0.6.9

Date : 2026-09-26  
Branche : `master`  
Périmètre : `docs/guide-developpeur.md` et ce rapport uniquement.

## Incohérence corrigée

`docs/guide-developpeur.md` décrivait les ressources Android comme limitées à FR/EN/NL et demandait de maintenir « les trois fichiers ». Cette formulation contredisait le comportement livré : `UiLanguage.DE` existe et `app/src/main/res/values-de/strings.xml` complète les répertoires `values`, `values-en` et `values-nl`.

## Formulations remplacées

- L’arborescence du projet indique maintenant les ressources d’interface FR, NL, EN et DE.
- La section « Ressources et langues UI » liste les quatre fichiers `strings.xml` : français, anglais, néerlandais et allemand.
- La consigne « maintenir les trois fichiers » est remplacée par une obligation de cohérence des quatre ensembles : toute nouvelle chaîne utilisateur doit rester alignée en FR, NL, EN et DE.

La correction distingue les quatre langues de l’interface des langues plus larges détectées dans le texte OCR. Les pages `docs/index.md`, `docs/langues.md`, `docs/parsing.md` et `docs/sections.md` contiennent des mentions de langues OCR, de vocabulaires ou d’alias ; elles ne sont pas des affirmations obsolètes sur les ressources UI et n’ont pas été modifiées.

## Vérifications factuelles

- `UiLanguage.kt` contient `FR`, `EN`, `NL` et `DE`.
- Les répertoires présents sont `app/src/main/res/values`, `values-en`, `values-nl` et `values-de`.
- La recherche ciblée dans `README.md` et `docs/` ne trouve plus « trois langues », « trois fichiers » ou une liste FR/EN/NL appliquée aux ressources d’interface.

## Validations

- Liens Markdown relatifs : succès.
- `./.venv/Scripts/python.exe -m mkdocs build --strict` : succès.
- Le build conserve uniquement le message informatif attendu concernant `docs/generated/base-connaissances-data.md`, inclus par snippet mais hors navigation directe.
- `git diff --check` : succès.
- `git status --short` : contrôlé.

## État Git final

Le worktree contient les modifications documentaires préexistantes de stabilisation, auxquelles s’ajoutent la correction de `docs/guide-developpeur.md` et ce rapport. Aucun code Kotlin/Java, test, Gradle fonctionnel, donnée, alias, asset, version Android, workflow GitHub Pages, index Git, commit ou push n’a été modifié.
