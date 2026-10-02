# Analyse d’architecture préliminaire — v0.7

Date : 30 septembre 2026  
Mode : lecture seule du dépôt cloné pour audit. Aucun script Python, build, test, importeur ou générateur n’a été exécuté pendant cette analyse. Aucun fichier applicatif ou éditorial n’a été modifié.

## 1. État vérifié

- Branche : `master`.
- Arbre de travail : propre au moment de l’inspection.
- HEAD réel : `264e216 docs: close 0.6.13.7 documentation cycle`.
- `versionCode` : `59`.
- `versionName` : `0.6.13.7`.
- Le rapport `PRE_0_7_REPOSITORY_STATE_AUDIT.md` mentionne encore `2229018`; il est donc antérieur au commit documentaire final et ne doit pas être considéré comme l’état Git courant.

## 2. Architecture observée

### Source de connaissance

La connaissance éditoriale est séparée du runtime :

```text
reference-input/
    ↓ importeurs et édition
knowledge/
    ingredients.json
    ingredient_aliases_multilingual.json
    sources.json
    origin rules
    ↓ générateurs
app/src/main/assets/
    ingredients.json
    ingredient_aliases_multilingual.json
    origin_qualifier_rules.json
```

Les tailles observées sont approximativement :

| Donnée | Taille |
|---|---:|
| Concepts éditoriaux | 226 kB |
| Mappings multilingues éditoriaux/runtime | 878 kB |
| Règles d’origine runtime | 15 kB |
| Total des trois assets runtime | 1,1 MB |

Ces tailles sont des tailles de fichier, pas des mesures de mémoire après désérialisation.

### Frontière Android/JVM

Le module `:mutation-core` contient le pipeline pur :

```text
texte
  → segmentation/sections
  → prétraitement OCR borné
  → arbre de composition
  → aplatissement des tokens
  → lexique multilingue contextualisé
  → matching
  → règles d’origine
  → verdict et diagnostics
```

`app` conserve le chargement Android des assets, l’OCR ML Kit, les images, l’UI Compose et la façade `VeganAnalyzer`. `AndroidIngredientKnowledgeLoader` est la frontière de chargement actuelle. `IngredientAnalysisService` reçoit une `IngredientKnowledge` immuable et reste utilisable sans Android ni réseau.

### Contrats actuels à préserver

- `Ingredient` porte l’identité, les alias locaux, le statut, la justification, la source et la note d’origine éventuelle.
- `MultilingualIngredientLexicon` porte séparément les mappings de langue et corrections OCR ; il ne porte pas la classification.
- `IngredientNode` porte la composition, les enfants, l’ordre indirect via le flattening, les pourcentages, classes fonctionnelles et qualifications.
- `AnalysisResult` porte les éléments reconnus, les inconnus, les traces, les présences déclarées et les résultats de verdict.
- `TokenDiagnostic` porte la provenance d’analyse d’une occurrence : texte, profondeur, parent, correction, langue, alias, concept, statuts et match.
- `VerdictEngine` est consommé par `AnalysisResult` et ne doit pas être modifié par opportunisme architectural.

## 3. Limites structurelles observées

1. `IngredientMatcher` reconstruit une liste d’alias normalisés et de regex à partir de la liste complète à chaque création d’instance. Le modèle est simple, mais le coût exact de création et de recherche n’a pas été mesuré.
2. Le matching principal et le lexique multilingue sont deux mécanismes distincts : le lexique résout d’abord un token exact dans une langue détectée, puis le matcher applique les alias classiques et les protections contextuelles.
3. `Ingredient` mélange identité éditoriale et surface de recherche locale, tandis que les mappings multilingues vivent dans un fichier séparé. Cette séparation protège la classification, mais rend la consolidation v0.7 délicate.
4. `AnalysisResult.matched` déduplique les concepts par identifiant, alors que les diagnostics conservent les occurrences. Une nouvelle structure de stockage ne doit pas confondre concept reconnu et occurrence reconnue.
5. Les assets sont chargés intégralement en mémoire via trois textes JSON puis désérialisés. C’est probablement acceptable pour les tailles actuelles, mais aucune mesure sur appareil n’a été réalisée.
6. La fixture de règles d’origine du module JVM est une copie contrôlée d’un asset ; une migration devra traiter explicitement ce risque de divergence.

## 4. Comparaison des architectures candidates

### A. JSON actuel + index mémoire construit au chargement

Le format éditorial et les assets restent JSON. Au chargement, une couche `KnowledgeIndex` précompile les alias, les variantes par langue, les E-numbers, les frontières et les protections nécessaires au matcher.

**Avantages**

- changement limité et réversible ;
- compatibilité directe avec le pipeline `knowledge → assets` ;
- fonctionnement offline inchangé ;
- conservation naturelle des alias, sources, contextes et diagnostics ;
- possibilité de garder un chemin JSON de référence pour la parité.

**Coûts et risques**

- consommation mémoire supérieure à la seule représentation JSON ;
- duplication partielle des chaînes et structures ;
- nécessité de définir précisément la clé d’index et la priorité des alias ;
- le fichier JSON reste chargé avant la construction de l’index.

**Appréciation** : meilleure première étape technique si les mesures montrent que le temps de recherche ou de création du matcher est le problème réel.

### B. Tables de correspondance précompilées dans les assets

Les générateurs produisent, en plus des concepts et mappings éditoriaux, un asset runtime déjà indexé : alias normalisé → candidats, variantes par langue, suffixes contextuels et métadonnées de provenance.

**Avantages**

- temps de démarrage et construction du matcher potentiellement réduits ;
- index déterministe et vérifiable avant intégration ;
- peut rester compact si les références utilisent des identifiants numériques ;
- compatible offline et avec un fallback JSON.

**Coûts et risques**

- format généré supplémentaire à maintenir ;
- risque de désynchronisation entre concepts et index ;
- migration plus sensible pour les règles contextuelles et les alias conflictuels ;
- diagnostics devant retrouver la forme, la langue, le concept et la source d’origine.

**Appréciation** : option intéressante après stabilisation du modèle cible et benchmark ; probablement préférable à une base relationnelle pour un corpus de cette taille.

### C. SQLite/Room embarqué

Les concepts, formes, langues, relations, contextes et sources sont stockés dans une base SQLite incluse dans l’APK, éventuellement via Room.

**Avantages**

- modèle relationnel naturel pour les formes multilingues, sources et relations parent/enfant ;
- index SQL explicites ;
- migrations de schéma standardisées ;
- lecture partielle possible si le corpus augmente fortement.

**Coûts et risques**

- ajout d’une dépendance et d’une frontière de chargement plus complexe ;
- coût de requêtes et de mapping Kotlin à mesurer ;
- recherche lexicale contextualisée et priorités de matching moins naturelles en SQL que dans une structure mémoire ;
- migration des diagnostics et des occurrences plus complexe ;
- aucune nécessité démontrée à la taille actuelle.

**Appréciation** : architecture de réserve pour une croissance importante ou des mises à jour de connaissance indépendantes de l’application, pas le choix initial recommandé.

### D. Format binaire ou index spécialisé autonome

Un format compact dédié est généré pour le runtime, avec un lecteur Kotlin spécialisé.

**Avantages** : démarrage potentiellement rapide, taille contrôlée, structure adaptée au matcher.

**Coûts et risques** : outil de génération et lecteur supplémentaires, débogage difficile, migration moins transparente, parité plus difficile à auditer et réversibilité faible.

**Appréciation** : prématuré avant d’avoir démontré une limite avec JSON et index mémoire.

## 5. Comparaison synthétique

| Critère | JSON actuel | Index mémoire | Asset précompilé | SQLite/Room |
|---|---:|---:|---:|---:|
| Réversibilité | Très forte | Forte | Moyenne à forte | Moyenne |
| Compatibilité offline | Oui | Oui | Oui | Oui |
| Conservation provenance/contexte | Forte | Forte | Forte si conçue explicitement | Forte |
| Complexité de migration | Nulle | Faible | Moyenne | Forte |
| Recherche d’alias | Linéaire/regex actuel | Indexée | Pré-indexée | Index SQL, logique hybride |
| Risque de désynchronisation | Faible | Moyen | Moyen à fort | Moyen |
| Pertinence à 479 concepts | Suffisante | À mesurer | À mesurer | Non démontrée |
| Mise à jour indépendante des assets | Faible | Faible | Faible | Possible |

## 6. Recommandation provisoire

Ne pas migrer immédiatement vers Room/SQLite et ne pas créer un format binaire autonome.

La trajectoire la plus sûre est :

1. définir les contrats v0.7 indépendamment du stockage ;
2. conserver JSON comme représentation éditoriale et format de compatibilité ;
3. introduire une couche de modèle runtime explicite distinguant concept, forme, occurrence, contexte et provenance ;
4. construire un index mémoire optionnel à partir de cette connaissance ;
5. comparer le comportement de référence JSON et de l’index sur un corpus de parité ;
6. ne produire un asset précompilé que si les mesures démontrent un bénéfice réel ;
7. ne considérer SQLite/Room qu’en cas de besoin futur de taille, de mises à jour partielles ou de requêtes structurées que l’index mémoire ne couvre pas.

Cette recommandation ne modifie ni `VerdictEngine`, ni `VeganAnalyzer`, ni le verdict. Elle cible la préparation de la connaissance et du matching, avec un chemin de repli JSON permanent durant la migration.

## 7. Modèle cible à étudier avant choix du stockage

Le modèle v0.7 devrait séparer au minimum :

- `KnowledgeConcept` : identité canonique, statut, raison, sources, notes d’origine ;
- `KnowledgeForm` : surface, langue, forme normalisée, type de forme, concept cible, provenance et confiance éditoriale ;
- `KnowledgeContextRule` : arôme, extrait, qualification, frontière ou contexte de blocage ;
- `IngredientOccurrence` : occurrence du texte analysé, position, langue détectée, parent, chemin, confiance de reconnaissance et résultat de matching ;
- `CompositionNode` : parent/enfants, ordre, pourcentage et métadonnées réglementaires ;
- `UnknownOccurrence` : texte non rattaché, raison et emplacement, sans statut implicite ;
- `CrossContactOccurrence` : trace séparée du flux de verdict.

Le stockage choisi devra permettre de reconstruire exactement les diagnostics actuels. Une déduplication de concepts ne doit jamais dédupliquer les occurrences.

## 8. Plan de mesure requis avant décision

Aucun résultat de performance ne doit être inventé à partir des tailles de fichiers. La prochaine mission de benchmark devra mesurer, sur l’appareil cible et sur JVM :

- temps de lecture des assets ;
- temps de désérialisation ;
- temps de construction du matcher/index ;
- mémoire avant/après chargement et après indexation ;
- recherche d’un alias exact, d’un alias multilingue, d’un E-number et d’un alias conflictuel ;
- analyse manuelle, étiquette complète, composition imbriquée et OCR multilingue ;
- génération des diagnostics ;
- taille finale des assets et impact APK ;
- parité des résultats `AnalysisResult` et des diagnostics occurrence par occurrence.

Les mesures devront être reproductibles, exécutées séquentiellement, et ne devront modifier ni les sources éditoriales ni les assets de production.

## 9. Décision demandée avant implémentation

La proposition à valider est : **contrats v0.7 explicites + JSON de compatibilité + index mémoire expérimental, sans changement du verdict et sans migration irréversible**.

Après validation, une mission séparée pourra produire un benchmark puis une proposition de schéma détaillée. Aucune implémentation de ce rapport n’est incluse dans cette branche distante clonée.
