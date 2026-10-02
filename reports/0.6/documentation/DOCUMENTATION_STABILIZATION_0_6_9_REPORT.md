# Stabilisation de la documentation 0.6.9

Date : 2026-09-26  
Branche : `master`  
Périmètre : documentation Markdown, configuration MkDocs et rendu local du site uniquement.

## Sources consultées

- Consignes : `AGENTS.md`.
- Documentation et accueil : `README.md`, l’ensemble de `docs/`, `mkdocs.yml` et `.github/workflows/docs.yml`.
- Rapports de mutation : `MUTATION_TESTING_INSPECTION.md`, `MUTATION_TESTING_JVM_FEASIBILITY.md`, `MUTATION_TESTING_PIT_PROTOTYPE.md`, `MUTATION_CORE_PHASE1_REPORT.md`, `MUTATION_CORE_ARCHITECTURE_REPORT.md`.
- Rapports de diagnostic : `DIAGNOSTIC_STABILIZATION_REPORT.md`, `DIAGNOSTIC_TRACE_SECTION_BOUNDARY_FIX_REPORT.md`.
- Évolutions 0.6.9 : `VERDICT_EXPLANATION_0_6_9_REPORT.md`, `VERDICT_EXPLANATION_0_6_9_FIX_REPORT.md` et les rapports `FIX_0_6_9_1` à `FIX_0_6_9_8`.
- Revues : `REVIEW_0_6_9_7_UNCERTAIN_UNKNOWN_VISIBILITY.md`, `REVIEW_0_6_9_7_STRUCTURED_UNKNOWN_CONSISTENCY.md`, `REVIEW_0_6_9_8_NESTED_UNKNOWN_CONTEXT.md`.
- Décision d’architecture : `COMPOSITE_STATUS_PROPAGATION_AUDIT_0_6_9_9.md`.

Les affirmations ajoutées ont été confrontées aux pages existantes, aux rapports ci-dessus et aux composants actuels concernés (`IngredientTreeParser`, `VeganAnalyzer`, `VerdictEngine`, modèles de diagnostic, explication conditionnelle et extraction des sections).

## Inventaire et écarts corrigés

- Le README et l’accueil indiquaient encore 0.6.8, trois langues et une présentation incomplète des verdicts. Ils couvrent maintenant l’état livré jusqu’à 0.6.9.8, FR/NL/EN/DE, le fonctionnement hors cloud et la vérification du texte OCR.
- La distinction entre verdict principal, classification `VEGETARIAN` informative, traces et présence réelle est explicitée.
- Les pages parsing, matching, verdict, diagnostic et architecture décrivent maintenant les composites sans statut métier, les inconnus bruts/visibles/détaillés, le blocage conditionnel par un vrai inconnu et le contexte visuel parent → enfant.
- Les limites OCR et multilingues sont formulées comme limites réelles; la consolidation multilingue et la suggestion automatique de reprendre une photo restent explicitement non livrées.
- Une page `docs/changelog.md` rassemble les notes 0.6.9 puis 0.6.9.1 à 0.6.9.8. L’audit 0.6.9.9 y est distingué comme décision d’architecture, sans l’annoncer comme fonction utilisateur.
- `mkdocs.yml` référence les notes de version et ne présente plus le site comme documentation 0.6.5.

## Pages mises à jour

- `README.md`
- `mkdocs.yml`
- `docs/index.md`
- `docs/architecture.md`
- `docs/parsing.md`
- `docs/matching.md`
- `docs/verdict.md`
- `docs/diagnostic.md`
- `docs/ocr.md`
- `docs/langues.md`
- `docs/changelog.md` (nouvelle page)

## Limites et fonctionnalités futures

Les travaux suivants ne sont pas livrés et ne sont pas annoncés comme tels : consolidation de plusieurs langues dans une même composition, validation enrichie à partir de plusieurs blocs multilingues, et suggestion automatique de reprendre une photo avec un cadrage plus large. La documentation conseille seulement la correction du texte éditable et une nouvelle photo plus lisible lorsque nécessaire.

La génération locale crée `site/`, mais ce répertoire est ignoré par Git (`/site/`). La publication GitHub Pages reconstruira le site à partir des sources suivies par Git via `.github/workflows/docs.yml`; aucun artefact de site n’a donc été ajouté au diff.

## Contrôles effectués

- Inventaire des fichiers Markdown racine, de `docs/`, de la configuration MkDocs, du workflow Pages et du répertoire généré.
- Vérification des liens Markdown relatifs : succès.
- `./.venv/Scripts/python.exe -m mkdocs build --strict` : succès.
- Le build signale seulement la page générée préexistante `docs/generated/base-connaissances-data.md` hors navigation; aucune alerte de lien ou de configuration.
- `git diff --check` et `git diff --cached --check` : succès.
- `git status --short` et inspection du diff : contrôlés avant ce rapport final.

## État Git final

Les modifications attendues sont les pages documentaires listées ci-dessus, `mkdocs.yml` et ce rapport. Aucun fichier Kotlin/Java, test, Gradle fonctionnel, donnée, alias, asset applicatif, version, index Git, commit ou push n’a été modifié pendant ce travail.
