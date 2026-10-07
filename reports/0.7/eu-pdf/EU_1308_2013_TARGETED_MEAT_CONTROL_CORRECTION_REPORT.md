# Correction des contrôles de l’import viande UE 1308/2013

Date : 2026-10-07. **Verdict : GO pour la correction des P2/P3 du contrôle indépendant.** Les cas négatifs sont exécutés et passent ; aucun défaut matériel restant identifié dans ce périmètre n’empêche de lever le NO_GO. Ce rapport complète les rapports historiques, sans les réécrire. Aucun commit.

## Préflight et périmètre

HEAD réel avant/après : `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`, branche `master`, index vide. Au départ : huit fichiers suivis modifiés (deux JSON de connaissance, deux actifs Android, deux documents générés, importeur, test Kotlin) et huit rapports/CSV non suivis dans ce dossier. Ces changements préexistants sont conservés.

Instructions consultées : `AGENTS.md`, skills `knowledge-import-validation` et `android-validation`, références `source-adapters.md` et `import-workflow.md`, invariants du projet et documentation du schéma/pipeline. Rapports d’intégration, contrôle indépendant et décisions consultés ; état réel du script, chargeur Kotlin, normaliseur, matcher, verdict et diagnostics inspecté. La comparaison est faite avec une empreinte au début de cette tâche, et non seulement avec le HEAD antérieur à l’import.

## Corrections et couverture

| Constat initial | Correction | Couverture exécutée |
|---|---|---|
| P2 : succès de `--check` sur un lot absent | Le lot absent déclenche un `SystemExit` non nul. Les lots partiels et divergents sont rejetés ; le lot complet retourne `changes=0`. | Processus CLI : absent, partiel, statut divergent et deux contrôles successifs conformes. |
| P2 : provenance partiellement comparée | Comparaison des objets mapping entiers, preuves imbriquées comprises. Une propriété supplémentaire inconnue échoue aussi : aucun champ futur n’est ignoré par une liste blanche de comparaison. | Valeur altérée puis champ supprimé pour les neuf propriétés de mapping, dont `source`, `relation`, `mappingGroup`, `confidence`. Même contrôle pour les sept propriétés de preuve ; champ futur supplémentaire rejeté. |
| P2 : collisions limitées à la langue et contournées après import | Index global des propriétaires canoniques et lexicaux ; recherche avant construction, même pour le lot déjà présent, puis sur le résultat construit en mémoire avant toute branche d’écriture. | Propriétaire concurrent EN de `geitenvlees` NL avant import et après import ; conflits de nom, alias, numéro E et variante OCR ; doublon lexical du même propriétaire ; alias orphelin créant un doublon ; synonyme de langue `dutch`. |
| P3 : dry-run silencieux | Le lot conforme affiche `changes=0; imported batch is current; no modification necessary`. Le lot absent affiche la proposition `changes=1` et ses 17 alias linguistiques. | Les deux états passent sans écriture. |
| P3 : matching insuffisamment asserté | Pour chaque forme : ID lexical attendu, liste exclusive du bon concept, `MatchResolution.EXACT`, résidu vide, aucun inconnu, `NON_VEGETARIAN`, ID responsable et bloqueur des diagnostics attendus. | Les **17 formes** passent dans le matcher et le pipeline Kotlin réels ; les cinq statuts restent `NON_VEGAN`. |

Implémentation : [importeur](../../../tools/import_eu_agricultural_products_regulation.py), fonctions `meat_runtime_normalized` (ligne 382), `meat_mapping` (392), `meat_collisions` (401), `meat_species_import` (439). Tests : [Python](../../../tools/tests/test_eu_meat_species_import.py) et [Kotlin](../../../app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt), méthode ciblée à partir de la ligne 56.

La normalisation de collision suit les opérations de `TextNormalizer.normalize` : minuscules, ligatures œ/æ, NFD, suppression de **toutes** les catégories Unicode M, séparateurs ASCII, espaces externes, puis code E initial. Elle n’utilise pas la normalisation d’import `n` ni le helper historique pour décider des collisions. Celui-ci conserve notamment son comportement antérieur. Des cas littéraux communs aux tests Python et Kotlin vérifient ligatures, accents, espaces, code E initial/interne et U+034F, marque à classe combinante zéro. Les codes et noms de langues acceptés par `labelLanguage` sont regroupés pour détecter les doublons.

La vérification est ciblée sur les surfaces du lot : elle ne remplace pas toute la validation structurelle Kotlin du lexique, ni les validations générales prévues pour un futur import. Le test Gradle charge effectivement les actifs actuels avec ce chargeur. Les évolutions du normaliseur ou des tables Unicode de Python/JVM devront être revalidées ; aucune garantie d’équivalence entre toutes les versions futures n’est revendiquée.

Les tests négatifs utilisent des JSON temporaires et le véritable dispatcher CLI dans des sous-processus, dont le code de sortie est asserté. Seule l’extraction PDF est mise en cache après une extraction réelle au début de la campagne. Chaque appel interdit `Path.write_text` et `Path.write_bytes` et compare intégralement ses fichiers temporaires avant/après. Aucun test n’appelle `--write`.

## Validations réellement exécutées

| Commande / contrôle | Résultat |
|---|---|
| `python -B -m unittest discover -s tools/tests -p test_eu_meat_species_import.py -v` | Passage final : **17 tests, OK**, 30,707 s, avec sous-cas négatifs d’altération/suppression. |
| `.\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.AgriculturalProductsRegulationImportTest` | **BUILD SUCCESSFUL** ; XML final : **7 tests, 0 échec, 0 erreur, 0 ignoré**, horodatage `2026-10-07T09:22:07.221Z`. |
| `python -B tools/import_eu_agricultural_products_regulation.py --meat-species --check` | Code 0, `changes=0`, lot conforme. Réexécuté après le dernier ajustement de collision. |
| Même commande avec `--dry-run` à la place de `--check` | Code 0, `changes=0`, message explicite d’absence de modification. |
| `python -B tools/import_eu_agricultural_products_regulation.py --animal-enrichment --check` | Code 0, `changes=0`, toutes les additions à zéro. |
| Même commande historique avec `--dry-run` | Code 0, `changes=0`, proposition vide conservée. |
| Comparaison AST avec le script empreinté au début | Seule fonction préexistante modifiée : `meat_species_import`. Toutes les anciennes constantes, fonctions historiques, extraction de preuve et dispatcher CLI sont identiques. |
| Empreintes SHA-256 avant/après ; `git diff --check`, HEAD et index | Seuls les deux fichiers de code/test préexistants autorisés changent ; aucune erreur d’espacement, HEAD/index conservés. |

Le premier passage Python a rencontré une erreur d’encodage dans le harnais sur la sortie accentuée du dry-run sous Windows. Le sous-processus utilise désormais `-X utf8`, cohérent avec le décodage du test. Les campagnes suivantes passent. Le test Gradle a été réexécuté après ajout des cas communs de normalisation ; la dernière campagne Python inclut le contrôle des synonymes de langue.

## Fichiers modifiés et préservation

Changements propres à cette tâche :

- `tools/import_eu_agricultural_products_regulation.py` : contrôles du seul lot viande et helpers dédiés.
- `tools/tests/test_eu_meat_species_import.py` : nouveau fichier de couverture positive/négative.
- `app/src/test/java/com/example/isitvegan/AgriculturalProductsRegulationImportTest.kt` : assertions renforcées et cas de normalisation.
- Ce nouveau rapport.

L’empreinte initiale couvre **391 fichiers préexistants** suivis ou présents dans le dossier de rapports. Les deux JSON `knowledge/ingredients.json` et `knowledge/ingredient_aliases_multilingual.json`, les deux actifs Android associés, tous les autres actifs suivis et les deux documents générés sont **identiques octet pour octet à leur état initial**. Les anciens rapports/CSV, sources et règles restent intacts. Les cinq concepts, 17 formes, statuts, mappings et propriétaires historiques sont donc préservés ; aucune correction de données n’a été nécessaire.

Aucun fichier du moteur, matcher, enums, schéma ou protections contextuelles n’a changé. Aucun import en écriture, builder ou régénération n’a été lancé. Les fichiers temporaires du harnais ne sont pas une voie d’import parallèle et ne changent jamais la base du dépôt.

## Limites et décision

Non exécutés : suite Gradle complète, PIT, tests sur Nokia/autre appareil, builder/génération, import en écriture, rendu visuel PDF, revue exhaustive d’étiquettes, nouvelle enquête de licence ou vérification distante de consolidation. L’extraction réelle utilisée par les contrôles reste celle des PDF locaux de la consolidation déclarée `2026-08-18`, avec vérification des empreintes, pages et offsets existante.

La reconnaissance exacte des 17 formulations démontre le comportement demandé sur ces entrées ; elle ne prouve pas leur justesse sémantique dans toute étiquette, négation ou description de substitut. Les risques documentaires et de faux positifs déjà décrits dans les décisions demeurent, sans élargissement des alias ni changement des protections.

**GO : les défauts P2/P3 motivant le NO_GO sont corrigés et leur couverture est validée. Aucun point matériel restant identifié dans cette correction n’impose de maintenir ce NO_GO.** Le commit reste à la charge de l’utilisateur.
