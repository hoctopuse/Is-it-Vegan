# Rapport final de préparation à la 0.7

Audit de synthèse réalisé en lecture seule le 30 septembre 2026. Les cinq audits `PRE_0_7_*` existants et les rapports réglementaires importants présents à la racine ont été lus. Aucune campagne lourde n’a été relancée, aucun statut n’a été modifié et aucun fichier existant n’a été réécrit.

## 1. État final de la base

La base éditoriale contient 479 concepts dans `knowledge/ingredients.json` :

| Statut | Nombre |
|---|---:|
| `VEGAN` | 127 |
| `VEGETARIAN` | 12 |
| `NON_VEGAN` | 10 |
| `UNCERTAIN` | 330 |
| **Total** | **479** |

Les identifiants sont uniques et aucun concept historique n’a été perdu lors des derniers lots. Les 8 concepts d’arômes ajoutés dans le dernier lot portent le total de 471 à 479. Les mappings structurés passent de 1 956 à 1 996 sans perte détectée.

Les alias directs comportent 18 groupes de valeurs répétées dans des concepts d’additifs. Les mappings structurés ne présentent pas de doublon exact détecté. Le groupe `cereals` reste une cible d’alias structurée sans concept canonique ; il est documenté comme non classifiable et ne produit pas de statut implicite.

Les 24 concepts comportant une `possibleOriginNote` sont `UNCERTAIN` et leurs sources existent. Les règles d’origine ne résolvent qu’une qualification attachée à l’occurrence concernée. Les sources réglementaires sont utilisées pour l’identité, la dénomination ou la composition ; elles ne sont pas interprétées comme une preuve automatique d’origine vegan.

Les lacunes connues sont documentaires ou de couverture : aucun concept dédié au collagène, couverture limitée des crustacés et mollusques, et origine non documentée au niveau fabricant ou procédé pour plusieurs vitamines et nutriments.

## 2. État des assets

La chaîne `knowledge → asset` est cohérente après normalisation du schéma de sortie :

- 479 concepts dans la source et 479 dans `app/src/main/assets/ingredients.json` ;
- mêmes identifiants et même ordre ;
- `sources` éditorial est rendu en `source` dans l’asset ;
- les champs générés comme `eNumber` vide expliquent les différences textuelles brutes ;
- `knowledge/ingredient_aliases_multilingual.json` et son asset sont identiques par hash et par contenu JSON ;
- les règles d’origine sont à jour selon leur contrôle ;
- `build_ingredients.py --check` et `build_multilingual_ingredient_mapping.py --check` réussissent.

La parité sémantique et runtime est donc établie. La comparaison brute des deux fichiers d’ingrédients ne doit pas être utilisée comme critère d’égalité sans tenir compte du schéma généré.

## 3. État des importeurs

Les flux suivants sont présents et documentés par un rapport ou une page de source : additifs UE, mapping multilingue, jus/purées/nectars, confitures, produits agricoles, cacao/chocolat, lait conservé, miel, arômes et extraction FIC 1169/2011.

Les contrôles disponibles ont donné les résultats suivants :

| Flux | État constaté |
|---|---|
| Additifs | Dry-run implicite sans changement ; 338 entrées importables présentes. Le script n’expose pas `--check`. |
| Miel | `--dry-run` sans changement ; 53 alias joints. |
| Lait | `--dry-run` sans changement ; 89 alias vérifiés sur quatre langues. |
| Jus | `--check`, sans changement. |
| Confitures | `--check`, sans changement. |
| Produits agricoles | `--check`, sans changement. |
| Cacao/chocolat | `--check`, sans changement. |
| Arômes | `--check`, sans changement ; 8 concepts et 40 mappings vérifiés. |
| FIC | Extraction documentée avec `--write` et `--check`, sans import applicatif. |

Les importeurs à interface hétérogène constituent une réserve de maintenance, pas un défaut de données. L’idempotence est démontrée par `--check` lorsqu’il existe et par `--dry-run` pour les scripts qui ne proposent pas `--check`.

## 4. État des sources

Le dépôt contient 48 PDF réglementaires suivis et 11 fichiers TXT suivis. Les sources locales couvrent notamment :

- FIC 1169/2011 ;
- additifs 1333/2008 ;
- arômes 1334/2008 ;
- miel 2001/110 ;
- lait conservé 2001/114 ;
- jus 2001/112 ;
- confitures 2001/113 ;
- cacao et chocolat 2000/36 ;
- produits agricoles 1308/2013.

Les quatre TXT FIC et leurs extraits thématiques sont accompagnés d’un manifeste avec hashes, pages et limites. Le dossier additifs possède aussi un manifeste et un CSV multilingue.

Les dossiers `01-core-labelling-terms/`, `02-sector-product-standards/` et `04-fortified-foods-and-supplements/` n’ont pas de `SOURCE_MANIFEST.md`. Le dossier 01 contient des doublons binaires des quatre PDF FIC du dossier 00, sans explication centrale de cette duplication.

Le dossier `04-fortified-foods-and-supplements/` contient huit PDF pour CELEX 02002L0046 et 02006R1925, mais aucun importeur, manifeste, page `docs/sources/` ou statut d’intégration n’a été identifié. Ces sources doivent être considérées comme préparées mais non démontrées comme importées.

## 5. État des statuts

Les classifications sont globalement prudentes et cohérentes avec les rapports réglementaires :

- les produits animaux explicites, la viande, le poisson, la gélatine, les abats et les graisses animales sont `NON_VEGAN` ;
- le lait, les œufs, le miel, le lactitol et le chocolat au lait sont `VEGETARIAN` ;
- les produits à origine variable, les arômes, le chocolat générique, les nectars, les produits agricoles composés et la majorité des additifs E restent `UNCERTAIN` ;
- les nouveaux additifs réglementaires n’ont pas été rendus vegan par leur seule autorisation UE ;
- E901, E966 et E1105 ont une justification d’origine suffisamment précise dans les rapports ;
- E470b/E572 restent distincts mais textuellement ambigus, avec un statut commun effectif `UNCERTAIN` ;
- les règles attachées à E322, E471 et E270 ne modifient qu’une occurrence qualifiée et ne reclassent pas le concept global ;
- les traces restent hors verdict.

Aucun statut démontrablement incorrect n’a été établi dans les audits. Les points à ne pas reclasser sans source supplémentaire sont les additifs variables, les vitamines D et B12, les produits composés agricoles, le chocolat générique, les arômes et les concepts génériques sans preuve de composition.

## 6. État du matching

Les protections suivantes sont couvertes par les tests et rapports :

- distinction entre arôme chocolat et chocolat réel ;
- distinction entre arôme fraise et fraise réelle ;
- distinction entre extrait de café et café réel ;
- protections FR/NL/EN/DE, notamment `chocoladearoma` et `Schokoladenaroma` ;
- frontières lexicales et alias génériques exigeant une expression complète ;
- numéros E avec et sans espace après `E`, sans reconnaissance d’un nombre nu ;
- maintien des formes composées allemandes testées ;
- conservation des parents, enfants, profondeurs, ordres, pourcentages et chemins de diagnostic.

Aucun bug démontré n’a été trouvé dans les campagnes ciblées. Les risques restants concernent la couverture combinatoire : nouveaux alias sous forme de sous-chaîne, formes allemandes hors chocolat, arômes avec fautes OCR et collisions d’alias E qui ne disposent pas tous d’un test négatif dédié.

## 7. État de l’OCR

Le corpus existant couvre les retours à la ligne, les traits d’union, les parenthèses continuées, les titres dégradés, les blocs multilingues, l’ordre natif des blocs, les listes sans titre et plusieurs fautes mineures contrôlées.

Les corrections OCR sont déterministes et bornées. Elles ne réécrivent pas le texte brut, ne franchissent pas les frontières des traces ou des zones marketing, et ne transforment pas une ressemblance en statut vegan.

Les tests manquants portent sur les combinaisons suivantes : arôme réel avec faute OCR, coupure de ligne et bloc multilingue ; troncature au début, au milieu et à la fin d’un nouvel alias ; formes allemandes composées des familles enrichies hors chocolat ; compositions à trois niveaux avec parent, enfant animal, enfant incertain, enfant inconnu et trace adjacente.

## 8. État des diagnostics

Les diagnostics conservent les inconnus, les éléments `UNCERTAIN`, les occurrences imbriquées, les pourcentages, les chemins parent → enfant et les occurrences répétées. Les vues conditionnelles ne transforment pas un inconnu en confirmation vegan.

Les traces sont extraites avant le parsing de la composition, affichées dans `crossContactWarnings` et exclues du moteur de verdict. Une liste sans titre n’est analysée que si ses indices structurels sont suffisants. Les textes nutritionnels, marketing, de stockage et les traces seules ne sont pas traités comme des listes d’ingrédients.

Les tests JVM ciblés de `mutation-core` et `app` ont réussi selon les audits précédents. Aucun défaut reproductible de diagnostic n’a été signalé. La couverture reste incomplète pour les troncatures et les compositions multilingues fortement dégradées.

## 9. État de la documentation

La documentation explique correctement la source de vérité, le rôle de `knowledge/`, la génération des assets, les statuts, les notes d’origine, les limites du verdict, la distinction arôme/ingrédient et la distinction trace/ingrédient. Les liens Markdown locaux et les cibles de navigation MkDocs ont été vérifiés.

Deux anomalies documentaires sont actuelles et vérifiables :

1. `python tools/build_knowledge_docs.py --check` échoue car `docs/generated/base-connaissances-data.md` est obsolète. Cette constatation est postérieure aux anciens rapports qui indiquaient un contrôle réussi lors de leur propre exécution.
2. `README.md`, `docs/index.md` et la description de `mkdocs.yml` indiquent encore 0.6.9.x, alors que `app/build.gradle.kts` indique `0.6.13.7`.

Le runbook central ne liste pas tous les importeurs, les options réellement acceptées, le générateur multilingue et le générateur de documentation. Les exemples miel/lait répètent `--dry-run`. Le rôle des TXT légers et le statut des sources fortifiées ne sont pas centralisés.

## 10. Anomalies bloquantes

Aucune anomalie bloquante pour commencer la conception de la 0.7 n’a été démontrée dans la base, les assets, les importeurs, le matching, l’OCR ou les diagnostics.

Les défauts suivants seraient bloquants pour déclarer une version 0.6.13 entièrement documentée et livrable sans réserve, mais ils ne bloquent pas l’ouverture d’un chantier 0.7 :

- la documentation générée ne passe pas son `--check` ;
- les points d’entrée documentaires ne sont pas alignés sur `0.6.13.7` ;
- le statut d’intégration des sources fortifiées n’est pas établi.

## 11. Anomalies non bloquantes

- alias directs répétés dans 18 concepts d’additifs ;
- groupe `cereals` sans concept canonique, explicitement non classifiable ;
- interface `--check` non homogène entre importeurs ;
- absence de manifestes dans trois dossiers de sources ;
- doublons PDF FIC dans deux dossiers ;
- manque de tests combinatoires pour OCR, alias réglementaires et troncatures ;
- absence de concept dédié au collagène et couverture partielle des produits de la mer ;
- sources d’origine insuffisantes pour plusieurs vitamines et additifs variables.

## 12. Tests manquants

Les tests à prévoir avant de conclure à une couverture 0.7 complète sont :

- matrice réel / arôme / extrait pour chocolat, café et fraise en FR/NL/EN/DE ;
- mêmes cas avec fautes OCR mineures, coupures de ligne et blocs multilingues ;
- tests négatifs de sous-chaînes, alias trop courts, composés allemands non prévus et collisions E ;
- couverture des nouveaux additifs avec numéro E, nom officiel, forme OCR et classe fonctionnelle ;
- compositions à trois niveaux avec pourcentages, enfants animaux, incertains, inconnus et traces ;
- troncatures au début, au milieu et à la fin d’un alias ;
- test de parité et d’idempotence du futur modèle de composition et de toute migration de stockage ;
- mesures de performance sur chargement, recherche, matching et diagnostic avec les 479 concepts et 1 996 mappings.

## 13. Changements à geler avant la 0.7

Avant toute évolution de modèle, les invariants suivants doivent être considérés comme gelés :

- `knowledge/ingredients.json` reste la source éditoriale des concepts et statuts ;
- les assets Android restent générés et ne sont jamais édités directement ;
- la parité source → asset et les mappings FR/NL/EN/DE restent contrôlés ;
- `VEGAN`, `VEGETARIAN`, `NON_VEGAN` et `UNCERTAIN` gardent leur sémantique actuelle ;
- un inconnu ou un `UNCERTAIN` ne devient jamais vegan par défaut ;
- les traces restent hors du verdict ;
- les arômes, extraits et goûts ne deviennent pas automatiquement l’ingrédient évoqué ;
- la hiérarchie parent/enfants, les pourcentages, `parentOrder` et les chemins de diagnostic sont conservés ;
- aucune modification opportuniste de `VerdictEngine`, `VeganAnalyzer`, du matcher ou du pipeline OCR ne doit être introduite dans la branche 0.6.13 ;
- les collisions E470b/E572 restent prudentes tant qu’une source ou règle de contexte ne permet pas une distinction sûre.

## 14. Changements à reporter en 0.7

Les sujets suivants doivent rester hors de la branche 0.6.13 et être traités comme sujets de conception 0.7 :

- ajout de nouveaux concepts de collagène, crustacés, mollusques, sésame, lupin ou nutriments sans sources dédiées ;
- déduplication ou réaffectation d’alias sans analyse de priorité du matcher ;
- activation des règles d’origine encore marquées `REVIEW_BEFORE_ENGINE_USE` ;
- correction OCR probabiliste ou élargissement non borné des fautes reconnues ;
- ajout d’un concept canonique pour `cereals` ;
- import applicatif des sources fortifiées avant décision éditoriale ;
- changement du format JSON ou du moteur de recherche sans corpus de parité et stratégie de migration.

## 15. Périmètre recommandé de la 0.7

Le périmètre recommandé est un chantier de consolidation et de performance, sans reclassification massive par défaut.

### Consolidation multilingue

Examiner un modèle où chaque forme possède une langue, une forme normalisée, une relation, une provenance, une confiance et un contexte éventuel. Préserver les alias historiques, les variantes OCR et les quatre langues FR/NL/EN/DE sans imposer une traduction automatique.

### Modèle de composition

Examiner une représentation explicite des nœuds, feuilles, pourcentages, parenthèses, classes fonctionnelles, qualificatifs et occurrences répétées. Le modèle doit préserver l’ordre et les frontières de sections.

### Règles parent/enfants

Définir les règles de propagation et de non-propagation : le parent structure la composition, les enfants portent les preuves de statut, et aucun parent générique ne masque un enfant animal, incertain ou inconnu.

### Reconnu, incertain et inconnu

Stabiliser une séparation de données explicite entre correspondance reconnue, concept reconnu mais d’origine incertaine, et texte non rattaché à un concept. Cette séparation doit rester visible dans les diagnostics et le verdict.

### Index de recherche ou base embarquée

Évaluer un index de recherche ou une base embarquée uniquement à partir de mesures comparant le chargement JSON actuel, une structure indexée et une éventuelle base locale. Le choix doit préserver les alias, les contextes, les langues, la provenance et le fonctionnement hors ligne.

### Performance

Mesurer la taille mémoire, le temps de chargement, le coût de normalisation, la recherche d’alias, le parsing imbriqué et la génération des diagnostics sur le corpus réel. Aucun seuil de performance ne peut être fixé à partir des seuls comptes actuels.

### Maintien des importeurs et des assets

Conserver un pipeline reproductible où les importeurs écrivent seulement dans `knowledge/`, où les générateurs produisent les assets et où chaque lot possède un rapport, une source locale, un contrôle d’idempotence et un test de non-régression.

### Migration JSON vers une structure plus performante

Étudier une migration par étapes : schéma cible, export de compatibilité, double lecture temporaire, comparaison des résultats, migration des assets, mesure de performance, puis suppression éventuelle de l’ancien chemin. Aucune migration ne doit être engagée avant que la parité des 479 concepts, des mappings et des diagnostics soit mesurée.

## 16. Risques de régression

Les risques principaux de la 0.7 sont :

- perdre des alias historiques lors d’une consolidation multilingue ;
- transformer une forme réglementaire ou un arôme en ingrédient réel par normalisation excessive ;
- propager un statut du parent vers un enfant ou l’inverse ;
- supprimer la distinction entre reconnu, incertain et inconnu dans un index ;
- faire entrer les traces dans le verdict pendant une migration du parser ;
- modifier la priorité entre alias concurrents, en particulier E470b/E572 ;
- perdre les chemins parent → enfant, les pourcentages ou les occurrences répétées ;
- désynchroniser `knowledge/`, les assets et les documents générés ;
- réduire les performances ou augmenter la mémoire sur appareil lors du remplacement du JSON ;
- interpréter des sources réglementaires préparées comme des données déjà importées.

## 17. Ordre recommandé des futures itérations

1. Stabiliser les contrats actuels : parité, statuts, traces, hiérarchie, diagnostics et provenance.
2. Rendre la documentation générée et les points d’entrée de version cohérents avec `0.6.13.7`.
3. Établir une matrice de corpus et de tests de non-régression pour les langues, arômes, OCR, compositions et additifs.
4. Formaliser le schéma 0.7 de provenance, contexte, composition et états reconnu/incertain/inconnu.
5. Mesurer les performances du pipeline actuel sur le corpus réel.
6. Comparer une structure indexée ou une base embarquée avec le JSON actuel, sans changer encore le comportement métier.
7. Prototyper la migration avec double lecture et comparaison complète des résultats.
8. Décider seulement ensuite de la structure de production, de la migration des assets et du retrait éventuel de l’ancien format.
9. Traiter séparément les nouvelles familles documentaires et les reclassifications, avec sources supplémentaires et tests dédiés.

## 18. Verdict global

La base de connaissances est cohérente, les assets sont en parité sémantique, les importeurs disponibles sont idempotents selon leurs interfaces respectives, les sources réglementaires principales sont présentes, et aucun bug démontré n’a été trouvé dans le matching, l’OCR ou les diagnostics. Les statuts restent prudents et les protections critiques sont couvertes.

La transition ne doit pas être présentée comme une clôture parfaite de la série 0.6.13 : la documentation générée est obsolète, les métadonnées générales de version sont en retard, plusieurs tests combinatoires manquent et les sources fortifiées ne sont pas intégrées de façon traçable. Ces points ne justifient pas de modification corrective opportuniste dans la branche 0.6.13 ; ils doivent être traités dans l’ordre recommandé.

**GO_WITH_WARNINGS**
