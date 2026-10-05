# OFF v0.7 — audit de licence, 47 formes et conception d’adaptateur

**Date de revue :** 2026-10-05. **Verdict : BLOCKED_LICENSE.** La conception peut être revue ; aucune forme OFF ne peut être intégrée ou redistribuée dans les données embarquées tant que la licence applicable à la taxonomie et les obligations de réutilisation ne sont pas établies. Les formes larges signalées ci-dessous exigent en outre une revue éditoriale.

## 1. Dépôt et portée

Dépôt réel : C:\Users\seb\AndroidStudioProjects\IsitVegan. Branche initiale et finale : master ; HEAD initial et final : 066c3ba56a49857617354908bc829d64e99ee966 ; remote origin : https://github.com/hoctopuse/Is-it-Vegan.git ; git worktree list : un seul worktree, celui du dépôt. git status --short --branch au départ et à la fin : « ## master...origin/master », sans ligne de changement. git diff --stat et git diff --check : aucune modification. Aucun commit ni push.

Le rapport local [OFF_IMPORT_V0_7_IMPLEMENTATION_AND_VALIDATION_REPORT.md](IsitVegan/reports/0.7/off-import/OFF_IMPORT_V0_7_IMPLEMENTATION_AND_VALIDATION_REPORT.md) est **suivi par Git** et décrit un état ancien au HEAD b7bcbea, bloqué faute de source OFF identifiée. Contrairement à l’état transmis dans la demande, il ne contient ni la liste des 47 formes, ni le commit OFF f10d80c, ni le hash annoncé. Le benchmark Nokia cité est également suivi par Git. Les affirmations historiques relatives à un rapport non suivi et à deux worktrees ne décrivent pas cet arbre. Les 47 formes ci-dessous ont été extraites directement du fichier OFF à la révision précisée, sans les reconstituer de mémoire.

Skill consulté : [knowledge-import-validation/SKILL.md](IsitVegan/.agents/skills/knowledge-import-validation/SKILL.md), et ses références réelles [source-adapters.md](IsitVegan/.agents/skills/knowledge-import-validation/references/source-adapters.md) et [import-workflow.md](IsitVegan/.agents/skills/knowledge-import-validation/references/import-workflow.md). L’acquisition spécifique OFF doit rester séparée des validateurs génériques. Une taxonomie fournit d’abord du vocabulaire et des relations, pas un verdict vegan.

## 2. Source OFF et licence

Source primaire fixée : [openfoodfacts/openfoodfacts-server, taxonomies/food/ingredients.txt au commit f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/taxonomies/food/ingredients.txt), acquise le 2026-10-05. Fichier téléchargé hors dépôt pour lecture : 2 799 878 octets ; SHA-256 recalculé : **232DFB2ED0D1C32B5CB46EFE46058B7A5D409502AA5365743779C522EB56BE1D**, identique au hash transmis. L’API GitHub donne pour ce chemin le blob Git d9dc2a043650cb937c5e6358e63254fc47ab81b0. L’historique du fichier au commit fixé montre au moins cinq modifications du 1er au 2 octobre 2026, dont le commit cible « taxonomy: foodture 6 (#14779) » ; aucun de ces libellés n’est une déclaration de licence. Cette vérification fixe le contenu, sans attester à elle seule les droits.

| Objet | Éléments primaires constatés | Conclusion |
|---|---|---|
| Code serveur Product Opener | [LICENSE](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/LICENSE), [COPYRIGHT](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/COPYRIGHT) et [README](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/README.md) présentent le programme sous AGPL-3.0 ou ultérieure. | Licence du logiciel explicitement indiquée ; ne vaut pas attribution démontrée à la taxonomie. |
| Base de produits | La [documentation officielle des licences de données](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/docs/api/tutorials/license-be-on-the-legal-side.md) indique ODbL pour la base, Database Contents License pour les contenus individuels et CC BY-SA pour les images. | Ces déclarations visent les données de produits et leurs contenus ; l’application précise à ingredients.txt n’y est pas établie. |
| Taxonomie d’ingrédients | [taxonomies/README.md](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/taxonomies/README.md) décrit un graphe orienté acyclique de termes et de parents. L’en-tête de ingredients.txt décrit des propriétés, sans notice de licence, titulaire ou SPDX. L’arbre Git à cette révision ne contient ni REUSE.toml, ni fichier .license adjacent, ni métadonnée REUSE spécifique ; la recherche de chemins de licence n’a trouvé que les notices générales du dépôt et des licences de médias sans rapport. | **NON_CLARIFIÉE** pour copie, adaptation et redistribution de cet extrait dans knowledge/ et dans l’application. |
| Contributions et autres sources | Le fichier contient des noms, traductions et propriétés alimentés par des contributions, ainsi que des champs faisant référence à Ciqual, Eurocode, Wikipedia ou Wikidata. | La portée des droits sur l’extrait et sur d’éventuelles contributions tierces reste à confirmer ; ces propriétés ne sont pas candidates à l’import. |

La [documentation API officielle](https://openfoodfacts.github.io/openfoodfacts-server/api/) et la [page de la taxonomie](https://github.com/openfoodfacts/openfoodfacts-server/blob/f10d80c51ac589021a4f2c7d39c0fc2a1c88e50f/taxonomies/README.md) n’apportent pas, dans les éléments consultés, une licence explicite pour ce fichier précis. Les pages wiki et conditions d’utilisation en ligne n’étaient pas consultables de façon fiable lors de cette revue ; leur contenu ne peut être présumé. Demander **par écrit à OFF**, avant import, la licence applicable à ingredients.txt à cette révision, la portée de la permission pour un sous-ensemble de traductions/synonymes dans une base éditoriale et des assets Android distribués hors ligne, les obligations d’attribution, de partage et de suivi de version, et les réserves éventuelles pour les contributions tierces. Aucun message n’a été envoyé.

## 3. Revue sémantique des 47 formes

Méthode : cinq blocs OFF identifiés par leur ligne anglaise racine ; seules les lignes en:, fr:, nl: et de: du **même bloc** ont été découpées sur « , ». Total : gelatin 1, milk 8, salt 23, water 11, honey 4 = **47**. Une recherche en lecture seule a comparé les formes via le normaliseur réel de build_multilingual_ingredient_mapping.py (casefold, décomposition NFKD, suppression des diacritiques, ponctuation vers espaces) aux noms, alias, variantes OCR et mappings locaux. Résultat : **17 déjà rattachées à la cible ; 30 absentes**. Aucune forme de ce lot n’a de propriétaire normalisé différent dans les données éditoriales actuelles. C’est un audit statique ciblé, pas une preuve d’absence de faux positifs dans le matcher ou en contexte OCR.

Dans le tableau : « E » = équivalent normalisé déjà présent chez la cible ; « Ø » = absent selon cette comparaison ; « C0 » = pas de collision normalisée avec un autre concept actuel. Pour les 17 E, REJETER signifie **rejeter l’ajout redondant**, pas invalider la forme historique. « tête » est la première forme d’une ligne linguistique OFF, « syn. » une autre forme de cette ligne ; ces rôles n’impliquent ni synonymie sûre dans l’application ni identité avec un parent. Aucun statut OFF vegan/vegetarian n’entre dans les décisions. Les risques cités portent sur un **matching exact en contexte ingrédient** ; le matching partiel, les arômes et les traces doivent être évalués séparément.

| # | Cible locale | Forme OFF (langue) | Local / collision | Rôle et sens dans le bloc OFF | Décision | Risque de faux positif / justification |
|---:|---|---|---|---|---|---|
| 01 | gelatin | gelatin (EN) | E / C0 | tête : gélatine | REJETER | Déjà reconnu ; ne pas assimiler « gelatine flavour » à la gélatine réelle. |
| 02 | milk | milk (EN) | E / C0 | tête : lait | REJETER | Déjà reconnu ; les boissons végétales et arômes au lait restent distincts. |
| 03 | milk | milk ingredients (EN) | Ø / C0 | syn. OFF : ensemble d’ingrédients issus du lait | REVUE_HUMAINE | Catégorie plus large que lait ; peut recouvrir plusieurs dérivés ou une mention d’allergène. |
| 04 | milk | Milch (DE) | E / C0 | tête : lait | REJETER | Déjà reconnu ; tester arôme et lait végétal en contexte. |
| 05 | milk | Milchbestandteile (DE) | Ø / C0 | syn. OFF : constituants du lait | REVUE_HUMAINE | Désigne des composants, pas nécessairement l’ingrédient lait lui-même. |
| 06 | milk | Lait (FR) | E / C0 | tête : lait | REJETER | Déjà reconnu ; préserver les exceptions de contexte. |
| 07 | milk | substances laitières (FR) | Ø / C0 | syn. OFF : ensemble de substances dérivées du lait | REVUE_HUMAINE | Terme collectif, sens réglementaire variable ; risque de confondre catégorie et ingrédient. |
| 08 | milk | melk (NL) | E / C0 | tête : lait | REJETER | Déjà reconnu ; tester les composés et les traces. |
| 09 | milk | melkbestanddelen (NL) | Ø / C0 | syn. OFF : constituants du lait | REVUE_HUMAINE | Catégorie de composants ; équivalence avec lait à décider humainement. |
| 10 | salt | salt (EN) | E / C0 | tête : sel | REJETER | Déjà reconnu ; éviter le matching dans un autre composé nommé. |
| 11 | salt | table salt (EN) | Ø / C0 | syn. OFF : sel de table | ALIAS_CANDIDAT | Faible si forme entière ; éviter sous-chaîne dans un mélange. |
| 12 | salt | common salt (EN) | Ø / C0 | syn. OFF : sel commun | ALIAS_CANDIDAT | Faible si forme entière ; « salt » isolé ne couvre pas tous les sels chimiques. |
| 13 | salt | cooking salt (EN) | Ø / C0 | syn. OFF : sel de cuisine | ALIAS_CANDIDAT | Faible si forme entière ; nom d’usage, composition à vérifier en contexte. |
| 14 | salt | dry salt (EN) | Ø / C0 | syn. OFF : sel sec | REVUE_HUMAINE | Forme générique pouvant qualifier d’autres sels ou procédés ; preuve insuffisante d’identité. |
| 15 | salt | edible salt (EN) | Ø / C0 | syn. OFF : sel alimentaire | ALIAS_CANDIDAT | Faible en liste d’ingrédients ; garder « edible » dans le match. |
| 16 | salt | edible common salt (EN) | Ø / C0 | syn. OFF : sel commun comestible | ALIAS_CANDIDAT | Faible mais formule inhabituelle ; conserver la phrase entière. |
| 17 | salt | food salt (EN) | Ø / C0 | syn. OFF : sel alimentaire | ALIAS_CANDIDAT | « food » seul est générique ; matcher l’expression complète. |
| 18 | salt | Speisesalz (DE) | E / C0 | tête : sel alimentaire | REJETER | Déjà reconnu ; ne pas élargir aux sels chimiques. |
| 19 | salt | Kochsalz (DE) | Ø / C0 | syn. OFF : sel de cuisine | ALIAS_CANDIDAT | Faible sur forme entière ; respecter les mots composés allemands. |
| 20 | salt | Tafelsalz (DE) | Ø / C0 | syn. OFF : sel de table | ALIAS_CANDIDAT | Faible sur forme entière ; éviter une correspondance par fragment. |
| 21 | salt | Salz (DE) | E / C0 | syn. OFF : sel | REJETER | Déjà reconnu ; terme générique à tester contre les sels spécifiques. |
| 22 | salt | sel (FR) | E / C0 | tête : sel | REJETER | Déjà reconnu ; contexte des sels chimiques à préserver. |
| 23 | salt | Sel alimentaire (FR) | Ø / C0 | syn. OFF : sel alimentaire | ALIAS_CANDIDAT | Faible sur forme entière ; éviter « sel » comme match partiel trompeur. |
| 24 | salt | sel de table (FR) | Ø / C0 | syn. OFF : sel de table | ALIAS_CANDIDAT | Faible sur forme entière ; préserver le composé. |
| 25 | salt | sel de cuisine (FR) | Ø / C0 | syn. OFF : sel de cuisine | ALIAS_CANDIDAT | Faible sur forme entière ; préserver le composé. |
| 26 | salt | sel sec (FR) | Ø / C0 | syn. OFF : sel sec | REVUE_HUMAINE | Adjectif générique ; peut décrire un état plutôt qu’un ingrédient stable. |
| 27 | salt | sel comestible (FR) | Ø / C0 | syn. OFF : sel alimentaire | ALIAS_CANDIDAT | Faible sur forme entière ; ne pas effacer le qualificatif. |
| 28 | salt | gros sel (FR) | Ø / C0 | syn. OFF : sel à gros grains | ALIAS_CANDIDAT | Granulométrie distincte mais même ingrédient présumé ; test de forme entière requis. |
| 29 | salt | zout (NL) | E / C0 | tête : sel | REJETER | Déjà reconnu ; préserver le contexte des sels composés. |
| 30 | salt | Keukenzout (NL) | Ø / C0 | syn. OFF : sel de cuisine | ALIAS_CANDIDAT | Faible sur forme entière ; pas d’inférence pour tous les sels. |
| 31 | salt | bakkerszout (NL) | Ø / C0 | syn. OFF : sel de boulangerie, souvent iodé | REVUE_HUMAINE | Variante enrichie en iode ; identité avec le sel local et statut des composants à examiner. |
| 32 | salt | kookzout (NL) | Ø / C0 | syn. OFF : sel de cuisson | ALIAS_CANDIDAT | Faible sur forme entière ; vérifier le contexte néerlandais. |
| 33 | water | water (EN) | E / C0 | tête : eau | REJETER | Déjà reconnu ; exclure les arômes et dénominations composées non équivalentes. |
| 34 | water | drinking water (EN) | Ø / C0 | syn. OFF : eau potable | ALIAS_CANDIDAT | Faible sur forme entière ; ne pas réduire à « drinking ». |
| 35 | water | tap water (EN) | Ø / C0 | syn. OFF : eau du robinet | ALIAS_CANDIDAT | Faible sur forme entière ; traitement éventuel conservé comme contexte. |
| 36 | water | Wasser (DE) | Ø / C0 | tête : eau | MAPPING_CANDIDAT | Traduction allemande directe ; tester aussi les noms composés. |
| 37 | water | Trinkwasser (DE) | Ø / C0 | syn. OFF : eau potable | ALIAS_CANDIDAT | Faible sur forme entière ; ne pas absorber un composé plus spécifique. |
| 38 | water | eau (FR) | E / C0 | tête : eau | REJETER | Déjà reconnu ; préserver les qualifications. |
| 39 | water | eau potable (FR) | Ø / C0 | syn. OFF : eau potable | ALIAS_CANDIDAT | Faible sur forme entière ; statut non emprunté à OFF. |
| 40 | water | eau du robinet (FR) | Ø / C0 | syn. OFF : eau du robinet | ALIAS_CANDIDAT | Faible sur forme entière ; préserver la locution. |
| 41 | water | eau osmosée (FR) | Ø / C0 | syn. OFF : eau traitée par osmose | ALIAS_CANDIDAT | Procédé distinct ; vérifier que le matcher garde la forme entière. |
| 42 | water | water (NL) | E / C0 | tête : eau | REJETER | Déjà reconnu ; collision interlangue EN/NL voulue vers le même concept. |
| 43 | water | opgiet (NL) | Ø / C0 | syn. OFF : terme d’opération ou de liquide de couverture | REJETER | Peut désigner une saumure ou un liquide versé, pas l’eau seule ; risque élevé. |
| 44 | honey | honey (EN) | E / C0 | tête : miel | REJETER | Déjà reconnu ; « honey flavour » n’établit pas la présence de miel. |
| 45 | honey | Honig (DE) | E / C0 | tête : miel | REJETER | Déjà reconnu ; arôme et goût restent distincts. |
| 46 | honey | miel (FR) | E / C0 | tête : miel | REJETER | Déjà reconnu ; « arôme de miel » n’est pas une preuve de miel. |
| 47 | honey | honing (NL) | E / C0 | tête : miel | REJETER | Déjà reconnu ; préserver les contextes de trace. |

Les exemples externes éclairent deux risques sans valider l’import : l’[autorité néerlandaise NVWA](https://www.nvwa.nl/onderwerpen/voedselveiligheid/etikettering-van-levensmiddelen/toegevoegd-water-in-lijst-van-ingredienten) distingue l’eau ajoutée comme liquide de couverture, et le [Voedingscentrum](https://www.voedingscentrum.nl/encyclopedie/zout-en-natrium.aspx) décrit le bakkerszout comme une forme iodée. La [notice canadienne](https://inspection.canada.ca/fr/etiquetage-aliment/etiquetage/avis-2026-02-11) illustre que les termes collectifs laitiers peuvent porter un sens réglementaire distinct du lait simple. Ces sources ne sont pas des preuves de licence OFF ni des sources de classification de l’application.

### Relations et revue humaine

Le bloc OFF milk contient « < en: dairy » et le bloc honey « < en: added sugar ». Décision pour chacun : **RELATION_SEPARÉE**, sous forme d’arête dirigée OFF enfant → parent, avec identifiants OFF distincts. « dairy » n’est ni un alias de milk ni nécessairement un concept canonique local ; « added sugar » ne rend pas honey synonyme de sugar. Les importer comme alias ferait perdre le sens du graphe et pourrait altérer les verdicts. Les catégories « milk ingredients », « Milchbestandteile », « substances laitières », « melkbestanddelen », « dry salt », « sel sec » et « bakkerszout » restent en **REVUE_HUMAINE** ; « opgiet » est **REJETER**. Toutes les décisions sont provisoires et bloquées par la licence.

## 4. Conception proportionnée d’un futur adaptateur OFF

**Acquisition.** Un adaptateur en lecture seule reçoit URL du dépôt OFF, chemin taxonomies/food/ingredients.txt, commit complet et SHA-256 attendu. Il refuse main sans commit, hash divergent, fichier manquant, syntaxe non reconnue ou licence non clarifiée. Il conserve le texte d’origine et un manifeste daté hors des assets. La lecture doit préserver la position du bloc et des lignes, la première forme de chaque langue, l’ordre des synonymes et toutes les arêtes « < ». Le pilote sélectionne explicitement les cinq identifiants OFF en:water, en:salt, en:milk, en:honey, en:gelatin ; il ne se fonde pas sur une recherche textuelle globale.

**Transformation.** Un export déterministe JSON/NDJSON de candidats, révisable avant toute écriture : un enregistrement par forme (surface exacte, langue FR/NL/EN/DE, rôle tête/synonyme, identifiant OFF, identifiant canonique local éventuel, type de relation proposée, décision et justification) et un enregistrement distinct par arête enfant/parent OFF. Les champs de provenance minimaux sont dépôt, chemin, commit, SHA-256, date d’acquisition, identifiant du terme OFF, langue, type de relation, source/licence vérifiée, concept local cible s’il est déterminé, décision éditoriale et confiance si le schéma le permet. Inclure le numéro de ligne/bloc, le texte de l’avis de licence et la version du format intermédiaire. Ne jamais convertir vegan:en ou vegetarian:en d’OFF en statut local.

**Validation.** Un validateur générique lit ces candidats et l’état éditorial actuel : schéma, langues permises, unicité des identifiants OFF et locaux, arêtes parentales sans auto-lien ni cycle attendu, doublons, collisions par casefold/trim dans ingredients.json et par normalisation NFKD du lexique, propriétaire actuel de chaque forme, existence des sources, preuve de licence, provenance et décisions humaines explicites. Il compare aussi IDs, statuts, raisons, alias, mappings et sources avant/après ; tout retrait, réaffectation ou changement de statut non prévu arrête le plan. Les formes larges ne deviennent pas automatiquement alias. Le validateur produit un diff de candidats sans écriture.

**Mutation, seulement après levée des blocages et validation du plan.** Un mode explicite de futur importeur ferait une fusion minimale dans knowledge/ingredients.json, knowledge/ingredient_aliases_multilingual.json et knowledge/sources.json. Il ne créerait ni statut ni verdict à partir d’OFF, conserverait les alias et concepts historiques, refuserait les collisions et n’écrirait rien si le résultat est identique. Les assets app/src/main/assets/ sont générés. Le schéma actuel des mappings (surfaceForm, normalizedForm, language, conceptId, mappingGroup, relation, source, confidence) peut documenter une forme révisée, mais ne représente pas nativement un nœud OFF et ses parents multiples. Aplatir le graphe en alias ferait perdre direction, hiérarchie, rôle tête/synonyme, possibilité de plusieurs parents et provenance d’arête. Un registre d’arêtes OFF ou une extension de schéma est une **décision d’architecture distincte** à spécifier et tester avant mutation ; les arêtes restent dans le format intermédiaire entre-temps.

**Vérification.** Exécuter les générateurs et tests ci-dessous après import autorisé, puis répéter l’aperçu et vérifier une sortie vide pour l’idempotence. Aucun appel OFF ne serait effectué à l’exécution Android ; knowledge/ demeure la source éditoriale, et VerdictEngine/VeganAnalyzer restent inchangés.

Interfaces réellement inspectées : l’importeur historique import_eu_flavourings_regulation.py a --check (échoue si l’import attendu manque), --dry-run (calcule sans écrire), --write (fusionne), mutuellement exclusifs. Il est propre à sa source UE, pas réutilisable tel quel pour OFF. build_ingredients.py, build_origin_rules.py et build_knowledge_docs.py utilisent --check pour valider la sortie générée ; sans option, ils écrivent. build_multilingual_ingredient_mapping.py expose --check, --write et --report, avec --check et --write exclusifs ; --report ne remplace pas un dry-run d’import. Aucun importeur OFF n’existe aujourd’hui sous tools/. L’interface du futur adaptateur n’est pas présumée implémentée.

## 5. Plan de validation du futur pilote — commandes prévues, non exécutées

Après clarification de licence et décisions éditoriales : lancer d’abord le **futur aperçu sans écriture** (commande à définir lors de l’implémentation), contrôler le manifeste source/hash et les 47 lignes avec les refus attendus. Rechercher les collisions selon les deux normaliseurs réels et les règles du matcher, les doublons de mapping et les formes identiques EN/NL, et comparer un instantané avant/après des IDs, statuts, raisons, alias, mappings, sources et comptes. Vérifier que les relations parentales restent distinctes et que les métadonnées de licence/provenance sont complètes.

Après écriture autorisée et revue du diff, commandes du dépôt à lancer depuis sa racine, selon les fichiers effectivement modifiés :

    python tools/build_ingredients.py --check
    python tools/build_multilingual_ingredient_mapping.py --check
    python tools/build_knowledge_docs.py --check
    python tools/build_origin_rules.py --check
    .\gradlew.bat :mutation-core:test
    .\gradlew.bat :app:testDebugUnitTest --tests com.example.isitvegan.MultilingualIngredientMappingTest
    git diff --check
    git status --short
    git diff --stat

La génération correspondante, si nécessaire, doit précéder les contrôles --check, après inspection des scripts ; build_ingredients.py sans option génère les assets, build_multilingual_ingredient_mapping.py --write génère le document. build_origin_rules.py --check n’est requis que si les règles d’origine changent ; aucun changement de ces règles n’est prévu. Les commandes Gradle exactes et les noms de tests seront revérifiés lors de l’implémentation selon le skill android-validation.

Les tests de comportement ciblés doivent couvrir matches positifs et négatifs FR/NL/EN/DE, formes complètes et imbriquées, termes génériques (sel/Salz/salt, water), composés et pourcentages, arômes/extraits/goûts versus ingrédient réel, lait végétal, présence « contient : lait » versus traces, inconnus restant inconclusifs, UNCERTAIN visible, répétitions, parents/enfants, ordre et chemins. Aucun statut historique ne doit changer. Après régénération, vérifier la parité knowledge/assets, la stabilité d’un second import (aucun diff), la taille des assets avant/après et seulement ensuite un benchmark Nokia G42 selon le [protocole de référence](IsitVegan/reports/0.7/benchmark/BENCHMARK_0_7_PRE_PHASE5_NOKIA_G42_BASELINE_REPORT.md). Ces validations **n’ont pas été lancées** dans la présente mission.

## 6. Limites et suites

La liste exacte a été retrouvée dans la source OFF, mais le rapport précédent local n’établit pas les comptes annoncés ; ils ont été recalculés. La comparaison des 17/30 porte sur des formes normalisées et sur un périmètre de cinq blocs, sans simulation du parser ni du matcher. Elle ne démontre pas l’innocuité des 30 formes absentes. Les avis de licence généraux, l’historique Git et la présence du fichier dans un dépôt AGPL ne prouvent pas les droits de redistribution de l’extrait. La licence de taxonomie est **NON_CLARIFIÉE**, d’où **BLOCKED_LICENSE**. Une réponse écrite d’OFF et la revue des termes collectifs/équivoques sont les conditions d’entrée d’un éventuel pilote.

Aucun fichier du dépôt, de knowledge/, de reference-input/, de tools/ ou des assets n’a été modifié ; aucun importeur, générateur, test JVM ou benchmark n’a été exécuté ; aucun commit ni push. Seul ce rapport a été écrit dans le répertoire parent du dépôt. Les téléchargements et scripts ponctuels de lecture ont été placés dans le dossier temporaire du système, hors dépôt.
