# Relecture indépendante — optimisation du matcher v0.7 phase 4

## 1. Décision globale

L’optimisation de `IngredientMatcher` est logiquement équivalente au calcul historique de `hasLongerLinkedAlias` pour la connaissance actuelle. Aucun changement de sélection, de statut, de diagnostic ou de verdict n’est introduit par le diff examiné.

La phase 5 peut commencer. Deux réserves restent à conserver : les temps d’analyse complète des phases 3 et 4 ne reposent pas sur un corpus identique, et une partie des tests de phase 4 compare deux instances du même code optimisé. La preuve principale de non-régression repose donc sur le test exhaustif du prédicat historique, le diff de production et les corpus antérieurs.

## 2. Résumé de la correction

Avant la phase 4, chaque entrée d’alias évaluait :

```text
il existe un alias plus long
ET il commence par « alias court + espace »
ET le reste correspond à de|d|du|des|a|au
```

Le nouveau code construit une fois l’ensemble des préfixes concernés. Pour chaque alias long, il examine chaque séparateur, extrait le préfixe et le suffixe, puis retient le préfixe uniquement s’il appartient lui-même à l’ensemble des alias normalisés et si le suffixe satisfait la regex historique. La création des `AliasEntry` remplace ensuite le parcours global par une appartenance à cet ensemble.

L’ensemble reste local au constructeur et n’est plus modifié après sa création.

## 3. Vérification de l’équivalence logique

### Préfixe valide

Le contrôle `base in aliases` est indispensable et présent. Il reproduit la condition historique dans laquelle `normalizedAlias` provenait nécessairement de la liste des alias valides. Un simple fragment situé avant un espace n’est donc pas ajouté s’il n’existe pas comme alias normalisé.

### Relation préfixe/suffixe

Pour un alias historique `A` et un alias plus long `B`, l’ancien prédicat acceptait `B` si `B.startsWith("$A ")`. Le nouveau calcul trouve exactement le séparateur placé après `A`, produit le même préfixe `A` et le même reste après l’espace. La condition de longueur est implicite : un séparateur suivi d’un suffixe produit nécessairement une chaîne plus longue que le préfixe.

La regex reste exactement :

```text
^(?:de|d|du|des|a|au)(?:\s|$).*
```

Les six connecteurs et leurs frontières sont inchangés. Le test `linkedAliasPrecomputationIsEquivalentToTheFormerPairwisePredicate` recalcule l’ancien prédicat sur les 2 485 alias normalisés actuels et compare les ensembles complets.

### Normalisation, accents et alias courts

Les deux chemins utilisent la même liste produite par `TextNormalizer.normalize`, suivie de `filter(String::isNotBlank)` et `distinct()`. Les accents, espaces et variantes normalisées ont donc le même comportement. Aucun nouvel alias éditorial n’est créé. Les alias courts ne peuvent être ajoutés que s’ils figurent déjà dans cette liste et s’ils disposent réellement d’un alias long conforme.

### Ordre, collisions et regex de matching

La construction des entrées reste fondée sur les mêmes noms, alias et E-numbers, avec la même normalisation et le même `distinct()` par concept. Le tri final reste `sortedByDescending { it.value.length }`. La regex de frontière de chaque alias reste :

```text
(?<![a-z0-9])<alias échappé>(?![a-z0-9])
```

Les collisions et leur ordre relatif ne sont donc pas modifiés par cette correction. `E470b` et `E572` conservent leurs entrées et concepts distincts.

### Règles contextuelles

Les protections miel, lait, pomme, chocolat, fraise et café ne sont pas modifiées. Les regex arôme/goût/extrait, `knownIngredientCompounds` et le cas allemand restent identiques. Le changement intervient uniquement lors du calcul initial du booléen `hasLongerLinkedAlias`.

### Structures et verdict

Le parser, le tokenizer, `IngredientAnalysisService`, `VerdictEngine` et `VeganAnalyzer` ne sont pas modifiés par la phase 4. Les parents, enfants, profondeurs, ordres, pourcentages, chemins et occurrences répétées sont donc transmis comme auparavant. Les inconnus, concepts `UNCERTAIN` et traces restent traités par le chemin de référence existant.

## 4. Vérification des tests

Les tests ciblés suivants ont été exécutés ensemble :

```text
Phase4MatcherProfileTest
Phase2ParityBenchmarkTest
IndexedCandidateProviderTest
```

Résultat : succès, 21 secondes, aucune erreur.

La couverture observée comprend :

- `E471`, `E 471` et `471` ;
- arôme chocolat, arôme de chocolat, extrait de café, goût fraise et lait végétal ;
- `Schokolade`, `Schokoladenaroma` et d’autres formes allemandes dans les corpus antérieurs ;
- `E470b` et `E572` ;
- inconnus et concepts incertains ;
- compositions imbriquées, trois niveaux, profondeurs, parents, pourcentages et chemins ;
- occurrences répétées ;
- traces FR/NL/EN/DE et exclusion du verdict ;
- texte long contenant de nombreux alias ;
- comparaison exhaustive de l’ancien et du nouveau calcul des préfixes liés sur la base actuelle.

Le test phase 4 compare le matcher profilé et le matcher sans collecteur sur un corpus ciblé. Cette comparaison valide l’absence d’effet de l’instrumentation, mais les deux instances utilisent l’algorithme optimisé. La preuve avant/après proprement dite vient du test exhaustif du prédicat et de l’inspection du diff.

Les assertions structurelles les plus fortes se trouvent dans les tests des phases 2 et 3. Le cas répété de phase 4 correspond à plusieurs appels indépendants ; la conservation de deux occurrences dans une même analyse est vérifiée dans les corpus antérieurs.

## 5. Vérification du périmètre

- `KnowledgeIndex` n’est référencé par aucun chemin applicatif de `app/src/main` ou par `IngredientAnalysisService`.
- `IndexedCandidateProvider` n’est utilisé que par ses tests et benchmarks expérimentaux.
- `IngredientAnalysisService` continue de construire directement `IngredientMatcher(database)`.
- `MatcherProfileCollector` vaut `null` par défaut. Les appels à `System.nanoTime()` liés au profil sont conditionnés par sa présence.
- Aucun log n’est ajouté dans le code de production. Les `println` sont dans les tests JVM et `Log.i` dans `androidTest`.
- Aucun cache global n’est ajouté. Le seul état mutable est celui d’un collecteur explicitement injecté ; il n’existe pas dans le chemin par défaut.
- Aucun JSON, asset, fichier de `knowledge/`, importeur, générateur ou fichier de version n’apparaît dans le diff.
- `versionCode = 59` et `versionName = "0.6.13.7"` restent inchangés.
- Les modifications préexistantes de `MultilingualIngredientLexicon` appartiennent aux phases précédentes et ne sont pas altérées par la phase 4.

## 6. Anomalies et avertissements

### Comparaison de performance non homogène

Les constructions de matcher JVM avant/après utilisent la même opération, 5 échauffements et 20 répétitions. Les mesures Android de construction utilisent également 5 échauffements et 10 répétitions sur le même appareil et la même connaissance ; elles sont raisonnablement comparables.

En revanche, les analyses complètes des phases 3 et 4 n’emploient pas toutes le même libellé. Le chiffre phase 3 d’environ 6,15 s ne doit pas être comparé directement aux mesures « simple », « imbriquée », « multilingue » ou « traces » de phase 4. Le rapport de phase 4 le signale correctement. Il ne démontre donc pas un ratio avant/après homogène pour l’analyse complète.

Les mesures JVM et Android sont bien séparées. Les temps de construction, matching, parsing, diagnostics et analyse complète sont également présentés séparément.

### Attribution des sous-étapes

Le pré-calcul de l’ensemble `longerLinkedAliasBases` est compris dans le temps total de construction, mais n’est pas inclus dans `longerLinkedAliasCheckNanos`, qui mesure uniquement les consultations de l’ensemble pendant la création des entrées. L’étiquette « linkedAliasChecks » après optimisation ne représente donc pas tout le coût du nouveau mécanisme.

Le temps `candidateSearchNanos` comprend le balayage et les regex de frontières. `selectionAndResolutionNanos` ne mesure pas les frontières. La formulation du rapport associant les valeurs cumulées de filtrage et sélection aux frontières doit être lue avec prudence.

### Surface d’API

Le `companion object` de `IngredientMatcher` n’est plus privé afin d’exposer la fonction `internal` au test. Les constantes du companion, qui n’ont pas de modificateur explicite, deviennent ainsi visibles publiquement au niveau Kotlin. Cela n’altère pas le comportement, mais élargit inutilement l’API du module. `MatcherProfileCollector` et `MatcherProfileSnapshot` sont également publics alors qu’ils servent au profilage.

Cette exposition ne bloque pas la phase 5, mais elle devrait être réduite lors d’un nettoyage : constantes privées, fonction d’équivalence `internal`, et types de profilage `internal` si les tests Android permettent toujours leur accès.

## 7. Risques résiduels

- Le test d’équivalence est exhaustif pour la base actuelle, pas pour toutes les chaînes théoriquement possibles. La démonstration par inspection couvre toutefois la transformation générale.
- Le collecteur n’est pas conçu pour être partagé entre threads. Il est absent du chemin par défaut et les benchmarks sont séquentiels.
- Les chronométrages restent sensibles au JIT, au GC et à la fréquence CPU ; la PSS Android reste globale au processus.
- L’optimisation réduit la construction mais ne traite ni le balayage de toutes les regex par token ni le coût du parser. Ces sujets doivent rester séparés.

## 8. Recommandations

1. Autoriser la phase 5 sur le parser et la désérialisation, avec un corpus avant/après strictement identique pour chaque conclusion de performance.
2. Conserver le test exhaustif du prédicat comme garde de non-régression.
3. Ajouter, lors d’un nettoyage ciblé, un petit test synthétique couvrant explicitement chacun des six connecteurs et un faux préfixe absent de la liste d’alias.
4. Réduire la visibilité des constantes du companion et des types de profilage sans modifier leur comportement.
5. Ne pas brancher `KnowledgeIndex` ou `IndexedCandidateProvider` dans le chemin de production sur la base de cette optimisation.

## 9. Décision finale

`GO_WITH_WARNINGS`
