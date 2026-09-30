# Rapport d’implémentation v0.7 — phase 1

## Objet

Cette phase ajoute une fondation runtime expérimentale pour la connaissance embarquée. Le JSON actuel reste le chemin de compatibilité et `KnowledgeIndex` n’est pas branché sur l’analyse de production.

## État Git initial

Les vérifications demandées ont été exécutées avant toute modification :

- branche `master`, synchronisée avec `origin/master` ;
- dernier commit : `264e216 docs: close 0.6.13.7 documentation cycle` ;
- version de référence : `0.6.13.7`, code `59` ;
- le dépôt contenait déjà le fichier non suivi `PRE_0_7_ARCHITECTURE_ANALYSIS.md` ; il a été conservé intact.

## Modèle runtime retenu

`KnowledgeIndex.kt` introduit quatre contrats distincts :

- `RuntimeConcept` conserve l’identité canonique, le statut métier, la justification, le numéro E, les sources, la note d’origine et les alias historiques ;
- `RuntimeForm` représente une surface de recherche et porte sa langue, son type, sa provenance, sa confiance, sa relation et la disponibilité de son concept cible ;
- `RuntimeOccurrence` représente une occurrence positionnelle indépendante du concept. Il conserve texte, correction, ordre, langue, hiérarchie, chemin, pourcentage, contexte, confiance, présence déclarée et marqueur de trace ;
- `KnowledgeIndex` fournit des recherches par alias normalisé, langue, numéro E et concept, conserve les candidats concurrents et expose les cibles canoniques invalides.

Les états `RECOGNIZED`, `RECOGNIZED_UNCERTAIN` et `UNKNOWN` sont explicites. La classification reste portée par `RuntimeConcept`; un mapping ne peut donc pas créer son propre statut.

Les listes et index exposés sont copiés ou encapsulés à la construction. Aucune opération publique ne modifie l’index après sa création. Les métriques incluent temps de construction, concepts, formes, clés d’alias, collisions et cibles invalides.

## Compatibilité avec le chemin actuel

Le flux est :

```text
JSON actuel → IngredientKnowledge → RuntimeConcept/RuntimeForm → KnowledgeIndex
```

`MultilingualIngredientLexicon` expose maintenant deux snapshots de lecture :

- les entrées historiques `aliases`, y compris les variantes OCR ;
- les mappings structurés `mappings`, avec `surfaceForm`, `conceptId`, langue, groupe, relation, source et confiance.

La résolution existante du lexique n’est pas remplacée par les mappings structurés. Le matcher de production continue d’utiliser `IngredientMatcher` avec la base existante. La recherche de candidats de l’index sert uniquement aux tests de parité et aux futurs adaptateurs.

## Invariants préservés

- 479 concepts sont convertis avec leurs identifiants et statuts exacts ;
- les mappings structurés FR/NL/EN/DE ainsi que les entrées IT/ES présentes sont conservés ;
- les 1 996 mappings actuels sont chargés ;
- `cereals` reste une cible sans concept canonique et est signalé comme invalide ;
- les alias historiques et variantes OCR ne sont ni supprimés ni fusionnés ;
- les collisions restent accessibles comme plusieurs candidats ;
- les occurrences identiques restent distinguées par leur identifiant positionnel ;
- les parents, enfants, profondeurs, ordres, chemins et pourcentages sont représentables ;
- les traces disposent d’un champ séparé dans `RuntimeOccurrence` et le chemin de production continue de les exclure du verdict ;
- les concepts `UNCERTAIN`, les inconnus, les arômes, goûts et extraits ne sont pas reclassés ;
- aucun asset généré n’a été édité.

## Fichiers modifiés ou créés

- `mutation-core/src/main/kotlin/com/example/isitvegan/MultilingualIngredientLexicon.kt` — exposition en lecture des entrées historiques et parsing des mappings structurés ;
- `mutation-core/src/main/kotlin/com/example/isitvegan/KnowledgeIndex.kt` — contrats runtime et index mémoire expérimental ;
- `mutation-core/src/test/kotlin/com/example/isitvegan/KnowledgeIndexTest.kt` — tests de conversion, validation, langues, OCR, collisions, occurrences, hiérarchie, traces et parité ;
- `PRE_0_7_PHASE1_IMPLEMENTATION_REPORT.md` — présent rapport.

Le fichier préexistant `PRE_0_7_ARCHITECTURE_ANALYSIS.md` est resté non modifié.

## Vérifications exécutées

- `.\gradlew.bat :mutation-core:test --tests com.example.isitvegan.KnowledgeIndexTest` — succès ;
- `.\gradlew.bat :mutation-core:test` — succès ;
- `git diff --check` — succès.

Les scripts Python, importeurs, générateurs de connaissances et build documentaire n’ont pas été exécutés, conformément au périmètre demandé.

## Limites connues

L’index ne reproduit pas encore toute la sélection contextuelle du matcher : il énumère les candidats contenus et conserve les collisions, tandis que `IngredientMatcher` garde les règles de priorité, de contexte et de protection. La parité démontrée dans cette phase vérifie que les candidats du matcher sont disponibles dans l’index sur le corpus ciblé ; elle ne constitue pas encore une autorisation de remplacer le matcher.

La mesure de temps de construction est exposée mais aucun gain de performance n’est revendiqué. La taille mémoire réelle sur appareil et le coût de chargement Android restent à mesurer.

## Éléments volontairement non modifiés

`VerdictEngine`, `VeganAnalyzer`, `IngredientTreeParser`, le pipeline OCR, la sélection multilingue OCR, `AndroidIngredientKnowledgeLoader`, les assets Android, les données éditoriales et les statuts métier n’ont pas été modifiés.

## Décision à prendre avant la phase 2

Avant toute intégration dans le chemin de production, il faut définir un corpus de parité complet et comparer, pour chaque occurrence, les candidats, la priorité, le type de résolution, les diagnostics, les chemins hiérarchiques, les inconnus et les traces. La phase suivante devra aussi mesurer le coût réel de construction et de consultation sur appareil. Aucune migration d’asset, intégration par défaut, SQLite ou Room ne doit être engagée sur la seule base de cette phase.
