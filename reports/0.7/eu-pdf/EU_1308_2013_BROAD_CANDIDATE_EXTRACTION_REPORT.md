# Extraction exploratoire large — règlement (UE) nº 1308/2013

Date du travail : 2026-10-05. CELEX : **02013R1308**. Consolidation locale : **18.08.2026**. Travail documentaire, sans import.

## Résultat et portée du pilote

Les quatre PDF ont été extraits entièrement, page par page : **876 pages**, soit 219 dans chaque langue FR/NL/EN/DE. Le CSV contient **11 286 occurrences référencées**, **3 870 identifiants de candidats exacts** et **2 288 propositions de regroupement**. Toutes les entrées sont **REVIEW_REQUIRED**, avec une confiance **SOURCE_TEXT_ONLY**. Ces propositions ne sont pas des concepts d’ingrédients validés et ne constituent pas une liste d’ajouts approuvés.

Le pilote couvre les secteurs et les noms au-delà de la liste positive de l’importeur : céréales, riz, sucres, semences, légumineuses, houblon, huiles, fruits/légumes, parties comestibles, préparations animales, produits laitiers, œufs, volaille, apiculture, vins, sous-produits et constituants. Aucun plafond de candidats n’a été dérivé des 44 formes codées en dur.

**Limite nette :** les 876 pages ont été extraites, indexées, contrôlées et soumises aux recherches lexicales ; la lecture lexicale assistée est approfondie sur les listes et définitions. Il ne s’agit pas d’une inspection visuelle exhaustive de chaque page ni d’une preuve que chaque nom utile a été découvert. Les descriptions réparties entre colonnes, césures dures et sens contextuels peuvent encore masquer des termes. Les préfixes de lignes NC ne sont pas une restitution complète de leurs catégories. Le pilote fournit une liste large pour revue, sans affirmer une exhaustivité lexicale.

## État Git et consignes

- Départ : git status --short --branch → « ## master...origin/master », sans changement suivi ou non suivi.
- HEAD : 6be60933df464dccb857b8d3287825011567344b.
- git worktree list --porcelain : un seul worktree, C:/Users/seb/AndroidStudioProjects/IsitVegan, branche refs/heads/master, même HEAD.
- git diff --stat au départ : vide. Aucun changement préexistant n’a été constaté.
- Consignes lues : AGENTS.md ; .agents/skills/knowledge-import-validation/SKILL.md ; intégralité de ses références source-adapters.md et import-workflow.md.
- Documents comparés : SOURCE_MANIFEST.md du dossier sectoriel, knowledge/sources.json, docs/sources/eu-agricultural-products-regulation.md et reports/0.6/knowledge/EU_AGRICULTURAL_PRODUCTS_REGULATION_0_6_13_4_IMPORT_REPORT.md.

Le rapport historique a servi de comparaison, pas de preuve des comptes, identités ou hashes courants. Aucun importeur, builder, test, asset, manifeste ou version n’a été modifié ou régénéré. Aucun commit ni push.

## Sources primaires vérifiées

Répertoire : reference-input/eu-food-labelling/02-sector-product-standards/.

| Langue | PDF | Octets | Pages | SHA-256 recalculé |
|---|---|---:|---:|---|
| FR | CELEX_02013R1308-20260818_FR_TXT.pdf | 2214937 | 219 | 5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c |
| NL | CELEX_02013R1308-20260818_NL_TXT.pdf | 2149221 | 219 | 99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74 |
| EN | CELEX_02013R1308-20260818_EN_TXT.pdf | 2105606 | 219 | b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11 |
| DE | CELEX_02013R1308-20260818_DE_TXT.pdf | 2159699 | 219 | 64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda |

Les quatre hashes correspondent au manifeste actuel ; ses 219 pages par langue sont confirmées. Chaque page primaire extraite contient le CELEX, la langue et la date 18.08.2026 dans l’en-tête. L’identité n’est donc pas déduite du seul nom de fichier.

knowledge/sources.json contient la source eu-agricultural-products-regulation-1308-2013-20260818, le CELEX 02013R1308, la consolidation 2026-08-18, les quatre langues et les quatre chemins exacts. Ce record ne remplace pas le contrôle des PDF. Leur première page indique que la consolidation est un outil documentaire sans effet juridique ; ce pilote est un inventaire lexical des fichiers locaux.

## Outils et méthode

Environnement exécuté : PowerShell, Python 3.14.7, pypdf 6.19.0. Aucun téléchargement, OCR, traduction automatique ou service distant. pypdf était déjà disponible ; fitz, pdfplumber, pypdfium2 et PIL n’étaient pas disponibles et n’ont pas été installés.

L’extraction de tools/import_eu_agricultural_products_regulation.py a été inspectée avant usage de la bibliothèque. tools/extract_fic_reference.py vise un autre règlement et écrit dans son dossier source ; tools/extract_eu_additives_reference.py cible seulement l’annexe II-B de 1333/2008. Ces scripts n’ont pas été exécutés. Seule la primitive PdfReader(...).pages[n].extract_text() a été réutilisée directement.

Les opérations documentaires ont été réalisées par snippets Python en mémoire, sans création de nouveau script ni import Python du module réglementaire. La constante A de ce dernier a été lue par analyse AST et ast.literal_eval ; main() n’a jamais été appelé.

1. Extraction intégrale conservée dans quatre TXT, avec blocs « ===== PAGE n ===== » et pagination PDF 1-based.
2. Contrôle de chaque page : identité, présence de texte, U+FFFD, contrôles invisibles anormaux et motifs usuels de mojibake.
3. Repérage des titres d’articles, annexes et parties ; collecte des 24 secteurs de l’article 1, libellés lexicaux de lignes NC dans toutes les parties de l’annexe I, dénominations entre guillemets, définitions et listes de produits.
4. Recherches de termes élémentaires repérés dans le texte primaire : denrées, matières premières, fractions comestibles, préparations, constituants, noms régionaux, noms taxonomiques imprimés et variétés. Une forme recherchée sans occurrence n’a pas été ajoutée pour compléter une langue.
5. Recherche des répétitions dans l’ensemble des pages, avec limites de mots et sans plafond d’occurrences.
6. Recherche souple des espaces/U+00AD insérés entre lettres ; maintien de la sous-chaîne brute réellement trouvée. Une césure dure n’est pas supprimée automatiquement. Les noms difficiles de l’appendice II ont été repris par leur forme littérale à référence de cellule.
7. Regroupement multilingue uniquement quand une définition numérotée, une entrée i–xvi, une cellule A/B/C 1–4, un secteur renvoyant à la même partie ou un tableau de dénominations de vente permet de l’étayer. Les autres propositions restent locales à une langue.
8. Comparaison en lecture seule avec les alias multilingues et avec les noms/alias globaux actuels. Aucun statut alimentaire ni relation taxonomique applicative n’a été attribué.

### Limites techniques de la collecte

La capture de termes entre guillemets utilise une fenêtre de 240 caractères. La recherche automatique des répétitions de libellés utilise les libellés jusqu’à 140 caractères ; les descriptions plus longues restent aux occurrences relevées et dans les TXT. Les lignes NC sont segmentées à leur première clause lexicale, avant certaines ponctuations ou conditions. Les fins de ligne incomplètes sont à revoir. Ces limites de méthode ne sont pas des plafonds de candidats.

La découverte repose sur les structures réglementaires et sur les noms relevés dans les listes primaires, puis des recherches littérales dans le document entier. Ce n’est pas un analyseur linguistique général capable de démontrer l’absence de tout nom non repéré. Il peut rester des sous-termes à isoler dans une catégorie composite et des noms dans une clause administrative.

### Lecture et déduplication du CSV

surface_form conserve une **sous-chaîne exacte de la couche texte extraite**, avec casse, accents, retours de ligne, espaces internes et césures. Elle n’est pas une transcription typographique certifiée des glyphes imprimés. surface_reading aide à lire/rechercher la forme ; elle ne remplace pas la preuve brute. Le PDF et sa page restent la référence primaire.

candidate_id identifie la combinaison forme exacte/langue/proposition et se répète lorsqu’il existe plusieurs occurrences. Chaque ligne possède son contexte et sa référence. source_offset est un offset Python en caractères, 0-based, dans le bloc de page du TXT. La validation vérifie exactement surface_form à cet offset. Les formes régionales ou ponctuations distinctes ne sont pas supprimées par normalisation.

language désigne la version linguistique du PDF. Un nom régional, latin ou étranger imprimé dans un tableau ne devient pas une traduction de cette langue. Les formes étrangères hors FR/NL/EN/DE des tableaux de pays n’ont pas été automatiquement converties en candidats pour les quatre langues.

Les propositions LEXICAL_FR/NL/EN/DE sont des regroupements lexicaux locaux. Les groupes legal_... et sector_I_... sont documentaires, fondés sur une référence primaire ; ils ne sont pas des identifiants approuvés pour la base. relation_to_concept décrit la relation proposée et evidence_method garde le chemin de collecte. Des répétitions d’un même noyau lexical au sein d’un nom plus long ne prouvent pas que ce nom composé est un synonyme.

coverage_status et existing_concept_ids comparent les alias de même langue après normalisation NFKD/ASCII inspirée du code existant. existing_global_concept_ids compare séparément les noms et alias de ingredients.json. Une absence exacte n’est pas une preuve d’absence de toute reconnaissance : pluriels, inflexions, artefacts de mise en page et composants déjà connus peuvent expliquer un écart. Aucun rapprochement sémantique ou fuzzy n’est compté comme couverture.

## Qualité de l’extraction et anomalies

| Langue | Caractères primaires extraits | Pages vides | U+FFFD | Contrôles anormaux | Césures U+00AD |
|---|---:|---:|---:|---:|---:|
| FR | 530721 | 0 | 0 | 0 | 732 |
| NL | 519363 | 0 | 0 | 0 | 1335 |
| EN | 478649 | 0 | 0 | 0 | 224 |
| DE | 520217 | 0 | 0 | 0 | 1866 |

Aucun motif usuel de mojibake recherché n’a été trouvé dans les pages. L’absence de ces motifs et de U+FFFD ne garantit pas l’absence de toute erreur d’encodage.

Un second mode pypdf, extraction_mode='layout', a été utilisé sur les pages 134, 137, 144, 170, 171, 172, 173, 182, 184, 194, 195 et 201 des quatre langues : **48 contrôles**. L’inventaire des caractères hors espaces et césures souples est identique entre les deux modes pour ces contrôles, mais leur ordre peut différer. Cela détecte certaines pertes ; cela ne certifie pas la géométrie ou le bon ordre des colonnes et n’utilise pas un moteur indépendant.

Anomalies et points de prudence observés :

- Espaces dans des mots, par exemple « riz loon zain » FR p.157, « Vinho Ver de » FR p.191 et de nombreux mots des lignes NC. Ils sont conservés dans surface_form.
- Les tableaux détachent parfois un libellé de son code ou répartissent le nom sur plusieurs lignes. « Trois quarts beurre », demi-margarine et les mélanges, notamment en allemand, nécessitent une reprise littérale. Les cellules A.1–C.4 de l’appendice II sont référencées séparément.
- **FR, annexe V p.170 :** la deuxième description contient « sans addition de jus de fruits », tandis que NL/EN/DE indiquent avec jus de fruits. La première description est sans jus dans les quatre versions. Aucun alignement automatique de cette deuxième description. La divergence persiste dans les inventaires plain/layout ; un contrôle visuel est nécessaire pour distinguer une divergence primaire d’un problème de restitution.
- **VII, partie VII, II.5, p.188 :** végétal/plantaardig/vegetable/pflanzlich peut admettre 2 % du total des graisses d’origine animale. Le qualificatif reste un terme d’origine à revoir ; il ne constitue pas une preuve de classification.
- **I, XXIV, ex1504, p.153 :** groupe des graisses/huiles de poissons ou mammifères marins avec exclusions. **ex2301, p.155 :** description collective comprenant poissons/crustacés/mollusques et sous-position de viande/abats. L’exclusion de la pêche/aquaculture à l’article 1 p.2 ne signifie pas absence de tous ces mots. Les occurrences de périmètre sont conservées avec prudence et ne justifient aucun alias alimentaire.
- Les formes kumis/koemis/koumiss/Kumys, viili/fil, smetana, fil, rjaženka et rūgušpiens sont conservées. La position d’une entrée laitière peut étayer une équivalence documentaire entre langues ; elle ne justifie pas la fusion de tous ces produits.
- Les graphies Vigna unguiculta et Vigna unguiculata restent distinctes, sans correction silencieuse ni création de relation taxonomique.

Aucun fichier manquant, hash divergent, texte de page vide ou U+FFFD n’a bloqué l’extraction. Les limites de mise en page et de contrôle visuel empêchent de valider automatiquement les candidats concernés pour un futur import. Les contrôles documentaires indépendants ont été poursuivis.

## Périmètre réellement examiné

| Pages PDF dans les quatre langues | Périmètre | Traitement réalisé |
|---|---|---|
| 1 | Identité et avertissement | Extraction et contrôle |
| 2–132 | Corps du règlement | Toutes les pages extraites et recherchées ; inspection ciblée des listes et passages lexicaux, notamment articles 1, 7, 11, 17, 23, 75, 78, 80–81. Pas de revue visuelle intégrale des clauses administratives |
| 133–156 | Annexe I, parties I–XXIV | Collecte transversale des lignes NC et noms, sans restriction à la liste A ; dépendances de sous-positions et préfixes à revoir |
| 157–163 | Annexe II, parties I–IX | Définitions du riz, sucre, houblon, produits vitivinicoles, œufs, volaille et apiculture ; distinctions entre qualités, produits et termes administratifs |
| 164–165 | Annexe III | Recherche des noms dans les qualités types ; méthodes de mesure non assimilées à des ingrédients |
| 166–169 | Annexe IV | Classement des carcasses, parties/coupes comestibles ; classes/conformation non prises comme ingrédients |
| 170 | Annexe V | Quatre descriptions de produits scolaires par langue ; divergence FR signalée |
| 171–189 | Annexe VII, parties I–VIII | Dénominations veau/jeune bovin, vins, laits et dénominations laitières, volaille, œufs, graisses tartinables, huiles d’olive |
| 190–193 | VII, appendice I | Zones viticoles : lieux non assimilés à des ingrédients ; Vinho Verde à revoir |
| 194–195 | VII, appendice II | Cellules A/B/C 1–4, compositions et césures |
| 196–200 | Annexe VIII | Constituants/opérations vitivinicoles, saccharose, acide tartrique, eau/alcool, sous-produits et noms Tokaji |
| 201 | Annexe IX | Catégories et noms/mentions de produits, dont oie à l’avoine ; adjectifs sensoriels isolés non pris comme ingrédients |
| 202–204 | Annexe X | Achats de betteraves : recherche lexicale, dispositions contractuelles écartées comme lexèmes d’ingrédients |
| 205–219 | Annexe XIV | Concordance : extraction/recherche, aucun concept créé à partir de numéros d’articles |

Les parties historiques IX/X et XV–XX de l’annexe I sont confirmées dans le texte : IX p.139–140, X p.140–141, XV p.143–144, XVI p.144, XVII p.145, XVIII p.145–146, XIX p.146, XX p.146–147. VII commence p.171 ; définitions jusqu’à p.189, appendices p.190–195.

La consolidation extraite passe de V à VII et de X à XIV, sans contenu autonome conservé des annexes VI et XI–XIII. Ce constat décrit ces fichiers, sans reconstruire des dispositions absentes.

## Importeur actuel : constat tiré du code

tools/import_eu_agricultural_products_regulation.py n’a été exécuté dans **aucun mode**.

| Concept ciblé par A | FR | NL | EN | DE |
|---|---:|---:|---:|---:|
| meat | 4 | 4 | 4 | 4 |
| edible_offal | 1 | 1 | 1 | 1 |
| animal_fat | 1 | 1 | 1 | 1 |
| poultry_meat_preparation | 1 | 1 | 1 | 1 |
| processed_fruit_vegetable_product | 1 | 1 | 1 | 1 |
| spreadable_fat | 1 | 1 | 1 | 1 |
| egg | 1 | 1 | 1 | 1 |
| olive_oil | 1 | 1 | 1 | 1 |
| Total | 11 | 11 | 11 | 11 |

- A contient 8 concepts et 44 formes. NEW décrit 5 ajouts possibles : edible_offal, animal_fat, poultry_meat_preparation, processed_fruit_vegetable_product, spreadable_fat.
- milk, cream, butter, whey, casein, lactose et cheese reçoivent aussi la source ; A n’ajoute pas de liste générale de termes laitiers.
- L’importeur concatène toutes les pages avec pypdf, puis normalise par casefold, NFKD, suppression des marques combinantes et conservation de a–z/0–9. compact supprime les espaces. Il vérifie A par recherche de sous-chaînes compactes dans le document entier, sans restituer les pages.
- Le filtre de découverte réel est la liste positive A. Aucun parcours général des catégories, cellules, fractions ou noms régionaux. Les notes d’origine possible des catégories variables sont des choix éditoriaux codés ; le pilote n’en attribue aucun statut.
- L’exclusion pêche/aquaculture figure dans SOURCE et la documentation ; le code ne découvre pas puis n’exclut pas dynamiquement tous les termes marins.
- Gardes historiques : au moins 459 concepts et 1864 mappings. Vérifications de collisions d’alias normalisés globaux/par langue ; clés de mappings concept/langue/normalizedForm ; ajouts conditionnels d’alias, mapping et source.
- La branche de traitement k == cheese dans la boucle sur A est inatteignable avec les clés actuelles de A ; elle ne prouve aucun changement courant de cheese.
- Modes CLI obligatoires et mutuellement exclusifs : --dry-run, --write, --check. Aucun mode implicite sans argument. --check échoue si l’état serait modifié ; --write écrit les trois JSON éditoriaux si un changement est calculé.
- Le code ne contrôle pas les SHA-256, les en-têtes de consolidation, les pages vides, U+FFFD ou la géométrie des tableaux. Ses seuils ne démontrent pas une couverture lexicale.

Les 44 formes ont été retrouvées directement dans les pages pour comparaison. La table détaillée et leurs références figurent dans les annexes calculées ci-dessous. Aucun mode --check, --dry-run ou --write d’un importeur n’a été lancé.

## Comparaison avec la base courante

À ce HEAD : **479 concepts**, **1668 entrées d’alias multilingues**, **1996 mappings structurés**. Les chiffres 459→464 et 1864→1906 du rapport historique ne décrivent pas les comptes actuels.

Les tableaux calculés ci-dessous distinguent formes brutes, clés lexicales de contrôle, candidats exacts, propositions et occurrences. Les clés lexicales de contrôle ignorent seulement casse, espaces et U+00AD : elles servent à lire les répétitions, sans normaliser accents ou variantes régionales. Les nombres ne sont pas des comptes d’ingrédients reconnus ou à intégrer.

Les écarts avec la couverture courante comprennent des formes simples absentes exactement, mais aussi pluriels, noms réglementaires, phrases composées et artefacts d’extraction. Ils ne permettent pas de calculer un taux général de reconnaissance de l’application.

## Candidats prioritaires à réviser

| Priorité | Exemples et preuve | Motif |
|---|---|---|
| 1 — Laits fermentés et noms régionaux | VII III p.182 : babeurre / karnemelk ou botermelk / buttermilk / Buttermilch ; kumis / koemis / koumiss / Kumys ; viili/fil, smetana, fil, rjaženka, rūgušpiens | Absences exactes de plusieurs formes actuelles ; correspondances de liste, produits et régionalismes à distinguer |
| 1 — Constituants et produits transformés | I p.134–136, II p.159 : maltodextrine/sirops, inuline/sirops ; lactose/sirops p.144 ; galactose VII IV p.184 | Substance, sirop, produit laitier, mélange et origine ne sont pas équivalents |
| 1 — Fractions animales et préparations | I XV–XX p.143–147, XXIV p.148–156 : onglets/hampes, foies, sang, boyaux, estomacs, graisses de volaille et pâtes farcies | Les quatre secteurs sous meat dans A ne décrivent pas toutes ces formes ; produits composés distincts |
| 1 — Apiculture | I XXII p.147 et II IX p.163 : gelée royale/royal jelly, propolis/Kittharz, pollen/Blütenpollen, cire/Bienenwachs | Absent de A ; absence exacte de plusieurs noms actuels ; pollen et produits apicoles ne sont pas automatiquement synonymes |
| 2 — Céréales, farines, amidons, semences, légumineuses | I I–V p.133–138 et XXIV p.151–153 : amidons/gluten, riz, haricots Adzuki/Bambara, niébé, pois d’Angole, graphies scientifiques | Aliment versus semence/fourrage ; identité et fautes primaires à revoir |
| 2 — Graisses, mélanges et huiles | I XXIV p.153–154 ; VII p.187–188 et appendice II p.194–195 : minarine, halvarine, demi-margarine, mélanges, stéarines, suif, huiles/fractions | Compositions variables ; mention végétale avec tolérance animale ; cellules A/B/C distinctes |
| 2 — Houblon et extraits | I VI p.138 ; II III p.159–160 : lupuline, poudres, poudres enrichies, extraits/mélanges | Les transformations ne sont pas des synonymes automatiques de houblon |
| 2 — Végétaux, épices et produits préparés | I IX/X/XXIV p.139–141 et 151–156 : aromatiques, pectines/pectinates, graines/noix, écorces, champignons/truffes, purées/confitures/jus | La catégorie transformée de A est beaucoup plus large que chaque constituant ou préparation |
| 2 — Produits vitivinicoles | II IV p.160–161 ; VII II p.175–181 ; VIII p.196–200 : catégories numérotées, retsina/résine, lies/marcs/piquette, Tokaji | Catégories et procédés ne prouvent pas l’absence d’auxiliaires animaux |
| 3 — Mentions de produit | IX p.201 : oie nourrie à l’avoine / haver vetgemeste gans / oats fed goose / Hafermastgans | Distinguer produit et alimentation de l’animal |

Les comparaisons exactes vérifiées sur les noms/alias globaux ne trouvent notamment pas babeurre, buttermilk, pollen, propolis, minarine, maltodextrine ou lupuline. Des termes comme orge et caseine ont en revanche des propriétaires existants. La table de comparaison détaillée fournit les identifiants actuels sans attribuer de statut.

## Ambiguïtés et éléments écartés

Les catégories génériques/composées restent dans le CSV avec revue explicite. Les noms liés à semences, fourrage, textile, plantes ornementales, tabac et vers à soie sont signalés comme usages de périmètre à revoir. Les qualités de riz ne sont pas confondues avec un ingrédient autonome. Noah/Othello/Isabelle/Jacquez/Clinton/Herbemont sont des variétés citées dans une restriction viticole de l’article 81, à revoir pour leur valeur alimentaire.

Les mots purement administratifs, marques de modification et renvois sont écartés comme noms d’ingrédients : contrats, marchés, appellations, mesures, classes isolées, numéros de code et pourcentages. Les fragments nus « autres/other/andere », « pour semence/for sowing », indications de poids et renvois de sous-position ne deviennent pas des alias autonomes. Le texte intégral de leur tableau est conservé dans les TXT.

Les sous-positions formulées seulement « de… / of… / van… / von… » et les descriptions d’usage pharmaceutique ou non consommable ne sont pas interprétées comme un nouveau nom complet. Les noms de matière dotés d’une valeur lexicale peuvent rester candidats, avec le contexte négatif ou restrictif. Les adjectifs sensoriels isolés de l’annexe IX, comme amer/intense/moyen/léger, ne sont pas repris comme ingrédients. fil, corn, clous/coques et produits entiers/séparés gardent des notes de polysémie.

Les recherches sans occurrence n’ont donné lieu à aucune traduction ajoutée : par exemple plusieurs formulations de beurre trois quarts, Vetproduct X %, esdoornsuiker, Dinkel ou miel artificiel ne sont pas les graphies trouvées. Les formes primaires réellement repérées comprennent Trois quarts beurre, Product met vet X %, Ahornsuiker, Spelz et succédanés du miel. Certaines difficultés provenaient de césures dures ou d’inflexions. Une recherche négative ne prouve pas l’absence du concept.

Les termes apparentés restent séparés : substance/sirop, poudre/extrait, semence/aliment, produit laitier/composé, vin/moût et formes régionales. Aucune relation taxonomique ni synonymie n’a été créée pour augmenter les nombres.

## Livrables et validations

Livrables attendus, uniquement sous reports/0.7/eu-pdf/ :

- EU_1308_2013_BROAD_CANDIDATE_EXTRACTION_REPORT.md ;
- EU_1308_2013_BROAD_CANDIDATES.csv ;
- EU_1308_2013_FULL_TEXT_FR.txt ;
- EU_1308_2013_FULL_TEXT_NL.txt ;
- EU_1308_2013_FULL_TEXT_EN.txt ;
- EU_1308_2013_FULL_TEXT_DE.txt.

Les TXT sont des pièces documentaires complémentaires permettant de vérifier les références et d’examiner les listes non réduites au CSV. Ils ne remplacent pas les PDF primaires.

Contrôles exécutés pendant la collecte et à la reprise : hashes/identités et pagination ; décodage UTF-8 ; CSV relu par csv.DictReader ; présence des champs ; valeurs de revue ; validation caractère par caractère de chaque surface_form à son offset de page. À la reprise, neuf références FR p.3 ont été précisées en Article 1 et les catégories de revue de 52 IDs harmonisées. Les formes brutes, IDs et nombres n’ont pas changé.

Les résultats définitifs des contrôles de fin, décomptes et références détaillées sont ajoutés ci-dessous après exécution. Aucun résultat Gradle ou métier n’est revendiqué : aucun test applicatif, builder, régénérateur ni importeur n’a été exécuté.

## Commandes réellement exécutées

- Git : git status --short --branch ; git rev-parse HEAD ; git worktree list --porcelain ; git diff --stat ; git status --short ; git diff --check.
- Recherche locale : rg --files avec globs ciblés AGENTS.md, source-adapters, import-workflow, PDF, manifestes et 1308 ; rg -n pour les identités/SHA-256 et PdfReader/extract_text dans tools/.
- Lecture : Get-Content des instructions, skill et références, importeur, deux extracteurs, manifeste, documentation source et rapport historique cités.
- Inventaire des sorties : Get-ChildItem -LiteralPath reports/0.7/eu-pdf.
- Python : importlib.util.find_spec pour les bibliothèques ; snippets passés à python - depuis PowerShell ; session python -u -i -q avec PYTHON_BASIC_REPL=1 pour contourner la limite de longueur Windows ; PdfReader, hashlib, AST, lectures JSON en UTF-8, recherches littérales, export/relecture CSV et assertions de références.
- Écriture documentaire : apply_patch pour le rapport, écritures Python des quatre TXT et du CSV et des annexes calculées du rapport.

Incidents sans effet sur les sources : un premier motif avec guillemets français a échoué via un pipe PowerShell en ASCII ; la reprise a fixé OutputEncoding en UTF-8. Une commande trop longue a été refusée par Windows avant création du processus ; les fragments en mémoire dans la session Python ont contourné cette limite. Les métadonnées dérivées des en-têtes TXT ont été corrigées et les pages revalidées contre les PDF. Quelques sorties console trop longues ont été tronquées ; elles ne servent pas de preuve d’exhaustivité. Après les interruptions de conversation, les fichiers produits ont été relus et contrôlés ; aucune session Python précédente n’était encore disponible à la reprise.

## Conclusion du pilote

La liste positive de l’importeur est bien plus étroite que le vocabulaire potentiel des quatre fichiers. Le pilote livre un inventaire large, des preuves locales exploitables et des propositions explicitement à revoir. Il ne démontre ni que chaque proposition doit être intégrée, ni que tous les termes utiles ont été trouvés. La revue visuelle des mises en page, la résolution des divergences linguistiques et le choix sémantique des alias restent nécessaires avant tout futur import. Aucun statut alimentaire n’est attribué dans cette passe.

## Annexe A — Décomptes calculés sur le CSV final

Une ligne du CSV est une occurrence littérale. Un candidate_id distingue langue, forme exacte et proposition de regroupement. Une proposition peut rester locale à une langue ; ces nombres ne sont pas des nombres d’ingrédients validés. Les clefs lexicales ignorent seulement la casse, les espaces et le trait d’union conditionnel U+00AD.

| Langue du PDF | Occurrences | Formes brutes distinctes | Clefs lexicales | candidate_id distincts | Propositions présentes |
|---|---:|---:|---:|---:|---:|
| FR | 2768 | 1005 | 661 | 1020 | 674 |
| NL | 2867 | 1000 | 614 | 1021 | 628 |
| EN | 3256 | 1016 | 665 | 1030 | 679 |
| DE | 2395 | 782 | 600 | 799 | 616 |

Total : 11286 occurrences, 3870 identifiants, 2288 propositions. Les formes identiques liées à des propositions différentes expliquent la différence entre formes brutes et identifiants.

| Catégorie de revue | Identifiants distincts | Occurrences | Propositions présentes |
|---|---:|---:|---:|
| COMPOSITION_TERM | 133 | 474 | 82 |
| EDIBLE_PART | 83 | 348 | 46 |
| INGREDIENT_TERM | 937 | 2272 | 727 |
| LAYOUT_FRAGMENT | 3 | 3 | 3 |
| ORIGIN_QUALIFIER | 7 | 42 | 4 |
| PROCESSED_PRODUCT | 186 | 430 | 146 |
| PRODUCT_CATEGORY | 865 | 3987 | 214 |
| QUALITY_DESCRIPTION | 77 | 97 | 47 |
| RAW_MATERIAL | 49 | 103 | 44 |
| REGIONAL_NAME | 57 | 59 | 41 |
| REGULATORY_CATEGORY | 1314 | 3233 | 912 |
| REGULATORY_SCOPE_TERM | 22 | 33 | 17 |
| SECTOR_SCOPE_REVIEW | 24 | 24 | 6 |
| SOURCE_TAXON_NAME | 89 | 157 | 76 |
| VARIETY_NAME | 24 | 24 | 24 |

103 regroupements fondés sur un emplacement réglementaire commun ; 103 disposent de formes dans les quatre PDF. Les 2185 propositions LEXICAL restent locales. Le regroupement documentaire n’atteste pas une synonymie utilisable par le moteur.

| Couverture exacte dans la langue, indiquée par le CSV | Identifiants | Occurrences |
|---|---:|---:|
| EXACT_CURRENT_LANGUAGE_ALIAS | 266 | 1608 |
| NO_EXACT_CURRENT_LANGUAGE_ALIAS | 3604 | 9678 |

## Annexe B — Toutes les propositions et leurs références

Le tableau déduplique les propositions pour la lecture. Chaque cellule linguistique donne le nombre d’identifiants, le nombre d’occurrences et toutes les pages PDF. Les formes exactes, variantes, contextes et relations sont dans le CSV ; aucune occurrence n’est éliminée de celui-ci. Les catégories restent des aides de revue.

| Proposition | Catégories | IDs | Occurrences | FR : IDs / occurrences / pages | NL : IDs / occurrences / pages | EN : IDs / occurrences / pages | DE : IDs / occurrences / pages |
|---|---|---:|---:|---|---|---|---|
| LEXICAL_DE: Abgeleitete Erzeugnisse | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: Adzukibohnen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Adzukibohnen ( Phaseolus oder Vigna angularis ) | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Ahornsirup | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 136 |
| LEXICAL_DE: Ahornzucker | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 136 |
| LEXICAL_DE: Aleppokiefernharz | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 176 |
| LEXICAL_DE: Algen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Amomen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Ananas | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 139, 152 |
| LEXICAL_DE: Andere Früchte | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Andere Schalenfrüchte | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 139, 152 |
| LEXICAL_DE: Andere Zucker | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: Andere gegorene Getränke (z. B. Apfelwein | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Andere Öle und ihre Fraktionen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 138 |
| LEXICAL_DE: Anderes Fleisch und andere genießbare Schlachtnebenerzeugn isse | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 149 |
| LEXICAL_DE: Anis- | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Apfelwein | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Aprikosen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Aprikosen/Marillen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Areka-Nüsse | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Arrowroot | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Avocadofrüchte | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 139, 152 |
| LEXICAL_DE: Babassuöl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Bambara-Erdnüsse | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Bambara-Erdnüsse oder Erderbsen ( Vigna subterranea oder Voandzeia subterranea ) | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Bananen | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 21 | — | — | — | 2 / 21 / 2, 33, 99, 102, 140–142, 152 |
| LEXICAL_DE: Bananensaft | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 141–142 |
| LEXICAL_DE: Basilikum | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Baumwollsamen | RAW_MATERIAL | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Baumwollsamenöl | RAW_MATERIAL | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Bienenwachs | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 147, 163 |
| LEXICAL_DE: Bienenzuchterzeugnisse | PRODUCT_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 3, 147, 163 |
| LEXICAL_DE: Birnen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Birnenwein | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Blasen | EDIBLE_PART | 1 | 1 | — | — | — | 1 / 1 / 150 |
| LEXICAL_DE: Blumenkohl | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Blut | INGREDIENT_TERM | 1 | 6 | — | — | — | 1 / 6 / 145, 154 |
| LEXICAL_DE: Blütenpollen | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 147, 163 |
| LEXICAL_DE: Bohnen der Art Vigna mungo (L.) Hepper oder Vigna radiata (L.) Wilczek | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Brand | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 177, 200 |
| LEXICAL_DE: Braunreis | PRODUCT_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 135, 157 |
| LEXICAL_DE: Brugnolen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Bruteier | PRODUCT_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 162, 186 |
| LEXICAL_DE: Buchweizen | RAW_MATERIAL | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Bäuche (Bauchspeck) und Teile davon | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 149 |
| LEXICAL_DE: Cajanus cajan | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Cannabis sativa | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Capsicum annuum | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Cargo-Reis | PRODUCT_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 135, 157 |
| LEXICAL_DE: Chicorée | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Cichorium intybus | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Clinton | VARIETY_NAME | 1 | 1 | — | — | — | 1 / 1 / 39 |
| LEXICAL_DE: Cornichons | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Curry | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Datteln | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 152 |
| LEXICAL_DE: Dicke Bohnen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Dost | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Durch Zusatz von Alkohol stummgemachter Most aus frischen W eintrau­ ben | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 160 |
| LEXICAL_DE: Därme | EDIBLE_PART | 1 | 1 | — | — | — | 1 / 1 / 150 |
| LEXICAL_DE: Eicheln | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 135, 155 |
| LEXICAL_DE: Eicheln und Rosskastanien | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Eier | INGREDIENT_TERM | 3 | 27 | — | — | — | 3 / 27 / 3, 17, 33, 37, 99, 102, 109, 122, 146–147, 162, 186–187, 201 |
| LEXICAL_DE: Eier in der Schale | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: Eier von Hausgeflügel in der Schale | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 146 |
| LEXICAL_DE: Eigelb | EDIBLE_PART | 1 | 4 | — | — | — | 1 / 4 / 146, 150, 162 |
| LEXICAL_DE: Erbsen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Erbsen ( Pisum sativum ) | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Erderbsen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Erdnussöl | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 153, 155 |
| LEXICAL_DE: Erdnussöl und seine Fraktionen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Erdnüsse | REGULATORY_CATEGORY | 1 | 5 | — | — | — | 1 / 5 / 137–138, 151–152 |
| LEXICAL_DE: Erzeugnisse gemäß der zusätzlichen Anmerkung 5 z u Kapitel 23 der Kombinierten | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Erzeugungsgebiet | PRODUCT_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 186–187 |
| LEXICAL_DE: Esel | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 148 |
| LEXICAL_DE: Esparsette | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 136, 153 |
| LEXICAL_DE: Essig | INGREDIENT_TERM | 1 | 11 | — | — | — | 1 / 11 / 138, 141, 155, 181 |
| LEXICAL_DE: Essigsäure | INGREDIENT_TERM | 1 | 12 | — | — | — | 1 / 12 / 138, 141, 155, 161, 181 |
| LEXICAL_DE: Ethanol | COMPOSITION_TERM | 1 | 1 | — | — | — | 1 / 1 / 199 |
| LEXICAL_DE: Ethylalkohol mit einem Alkoholgehalt von 80 % vol oder mehr | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 147 |
| LEXICAL_DE: Ethylalkohol und Branntwein mit beliebigem Alkoho lgehalt | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 147 |
| LEXICAL_DE: Extrakte und Säfte von Fleisch | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Feigen | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 139–140, 152 |
| LEXICAL_DE: Fermentierte Milcherzeugnisse mit Fruchtsaft, natürlich aroma tisiert oder nicht aromatisiert | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 170 |
| LEXICAL_DE: Fermentierte Milcherzeugnisse ohne Fruchtsaft, natürlich arom atisiert, | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 170 |
| LEXICAL_DE: Fermentierte oder nicht fermentierte Milcherzeugnisse mit Fruch tzusatz, natürlich aromatisiert oder nicht aromatisiert | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 170 |
| LEXICAL_DE: Fett von Rindern | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 144, 153 |
| LEXICAL_DE: Fett von Schafen oder Ziegen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 146 |
| LEXICAL_DE: Fette und Öle sowie deren Fraktionen | REGULATORY_CATEGORY | 2 | 4 | — | — | — | 2 / 4 / 153–154 |
| LEXICAL_DE: Fischen | REGULATORY_SCOPE_TERM | 2 | 4 | — | — | — | 2 / 4 / 150, 153, 155 |
| LEXICAL_DE: Flachs | INGREDIENT_TERM | 1 | 7 | — | — | — | 1 / 7 / 2, 5, 98, 139 |
| LEXICAL_DE: Flachs (Leinen) | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 139 |
| LEXICAL_DE: Flechsen und Sehnen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 156 |
| LEXICAL_DE: Fleisch | INGREDIENT_TERM, REGULATORY_CATEGORY | 4 | 65 | — | — | — | 4 / 65 / 10, 128, 143–150, 154–156, 171–175, 185 |
| LEXICAL_DE: Fleisch und Schlachtnebenerzeugnisse von Rindern | REGULATORY_CATEGORY | 2 | 4 | — | — | — | 2 / 4 / 143–144 |
| LEXICAL_DE: Fleisch und genießbare Schlachtnebenerzeugnisse | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 145–146, 149 |
| LEXICAL_DE: Fleisch und genießbare Schlachtnebenerzeugnisse von Hau sschweinen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Fleisch von Eseln | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 148 |
| LEXICAL_DE: Fleisch von Pferden | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 156 |
| LEXICAL_DE: Fleisch von Rindern | REGULATORY_CATEGORY | 2 | 5 | — | — | — | 2 / 5 / 143, 171, 173 |
| LEXICAL_DE: Fleisch von Schafen oder Ziegen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 145 |
| LEXICAL_DE: Fleisch von Schafen und Ziegen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Fleisch von Schweinen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 148–149 |
| LEXICAL_DE: Fruchtsäfte | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 141 |
| LEXICAL_DE: Fruchtsäfte und Gemüsesäfte | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 141 |
| LEXICAL_DE: Fructose | COMPOSITION_TERM | 2 | 7 | — | — | — | 2 / 7 / 134, 159 |
| LEXICAL_DE: Früchte | REGULATORY_CATEGORY | 1 | 25 | — | — | — | 1 / 25 / 138–141, 151–153, 155, 176 |
| LEXICAL_DE: Früchte (ausgenommen solche der Positionen 0801 bis 080 6) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Früchte der Gattung "Capsicum" mit brennendem Ge schmack | REGULATORY_CATEGORY | 2 | 3 | — | — | — | 2 / 3 / 141, 155 |
| LEXICAL_DE: Früchte der Gattungen " Capsicum" oder " Pimenta" | REGULATORY_CATEGORY | 3 | 6 | — | — | — | 3 / 6 / 140, 151–152 |
| LEXICAL_DE: Früchte und Nüsse | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 140 |
| LEXICAL_DE: Futterrüben | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 153 |
| LEXICAL_DE: Galactose | COMPOSITION_TERM | 1 | 1 | — | — | — | 1 / 1 / 184 |
| LEXICAL_DE: Ganze Erzeugnisse | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: Gartenbohnen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Gartenbohnen ( Phaseolus vulgaris ) | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Geflügelfett | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 146 |
| LEXICAL_DE: Geflügelfleisch | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 5 | 27 | — | — | — | 5 / 27 / 3, 33, 36, 99, 102, 109–110, 122, 146, 185–186, 201 |
| LEXICAL_DE: Geflügelfleischerzeugnis | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 186 |
| LEXICAL_DE: Geflügelfleischzubereitungen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 186 |
| LEXICAL_DE: Geflügelteile | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: Gelees | INGREDIENT_TERM | 2 | 2 | — | — | — | 2 / 2 / 141–142 |
| LEXICAL_DE: Gelée Royale | INGREDIENT_TERM | 1 | 3 | — | — | — | 1 / 3 / 147, 163 |
| LEXICAL_DE: Gelée Royale und Kittharz | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 147 |
| LEXICAL_DE: Gemüse | REGULATORY_CATEGORY | 5 | 63 | — | — | — | 5 / 63 / 2, 5, 16–17, 19–20, 33, 35, 75, 82, 85, 99, 101–102, 109–110, 121, 128, 130, 139–141, 151, 155 |
| LEXICAL_DE: Gemüsepaprika oder Paprika ohne brennenden Geschmack (Capsicum annuum) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Gemüsesäfte | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 141 |
| LEXICAL_DE: Genießbare Schlachtnebenerzeugnisse von Hausschweinen | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 145 |
| LEXICAL_DE: Genießbare Schlachtnebenerzeugnisse von Rindern | REGULATORY_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 143–144, 149 |
| LEXICAL_DE: Genießbare Schlachtnebenerzeugnisse von Schafen oder Ziegen | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 145–146 |
| LEXICAL_DE: Genießbare Waren tierischen Ursprungs | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 150 |
| LEXICAL_DE: Genießbares Mehl von Fleisch oder Schlachtnebenerzeu gnissen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 143 |
| LEXICAL_DE: Gerste | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 7–8, 12, 133 |
| LEXICAL_DE: Gerstenmehl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Geschlachtetes Geflügel | PRODUCT_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 162 |
| LEXICAL_DE: Geschmacksverstärker | INGREDIENT_TERM | 2 | 2 | — | — | — | 2 / 2 / 18, 21 |
| LEXICAL_DE: Getreidekeime | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Getreidekörner | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Getrennte Erzeugnisse | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: Getrocknete ausgelöste Hülsenfrüchte | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 151 |
| LEXICAL_DE: Getränke auf Milchbasis mit Kakao, Fruchtsaft oder natürlich aromatisiert | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 170 |
| LEXICAL_DE: Gewürznelken | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Glucose | COMPOSITION_TERM | 1 | 16 | — | — | — | 1 / 16 / 134–136, 144, 155–156, 159, 184 |
| LEXICAL_DE: Glucose und Glucosesirup | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 134 |
| LEXICAL_DE: Glucose- und Maltodextrinsirup | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 134, 136 |
| LEXICAL_DE: Glucosesirup | COMPOSITION_TERM | 1 | 8 | — | — | — | 1 / 8 / 134–135, 144, 155–156 |
| LEXICAL_DE: Grieben | INGREDIENT_TERM | 2 | 2 | — | — | — | 2 / 2 / 155 |
| LEXICAL_DE: Grobgrieß | REGULATORY_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 133, 135 |
| LEXICAL_DE: Grobgrieß und Feingrieß von Reis | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Grobgrieß und Feingrieß von Weizen | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 133 |
| LEXICAL_DE: Guaven | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 139, 152 |
| LEXICAL_DE: Gurken | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Gurken und Cornichons | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Hafer | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Hafermastgans | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 201 |
| LEXICAL_DE: Hafermehl | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Halvarine | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 188 |
| LEXICAL_DE: Hanf | INGREDIENT_TERM | 1 | 8 | — | — | — | 1 / 8 / 2, 98, 100, 139 |
| LEXICAL_DE: Hanf (Cannabis sativa L.) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Hanfsamen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 106, 152 |
| LEXICAL_DE: Hartweizen | COMPOSITION_TERM | 1 | 5 | — | — | — | 1 / 5 / 7–8, 12, 133 |
| LEXICAL_DE: Herbemont | VARIETY_NAME | 1 | 1 | — | — | — | 1 / 1 / 39 |
| LEXICAL_DE: Hinterviertel | EDIBLE_PART | 1 | 1 | — | — | — | 1 / 1 / 168 |
| LEXICAL_DE: Hirse | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Homogenisierte Zubereitungen aus Bananen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 142 |
| LEXICAL_DE: Homogenisierte Zubereitungen aus Fleisch | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Honig | INGREDIENT_TERM, PRODUCT_CATEGORY | 2 | 6 | — | — | — | 2 / 6 / 134, 147, 163 |
| LEXICAL_DE: Hopfen (Blütenzapfen) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 138 |
| LEXICAL_DE: Humulus lupulus | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 159 |
| LEXICAL_DE: Hunde- und Katzenfutter | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 135, 144, 155 |
| LEXICAL_DE: Hybrid-Körner-Sorghum | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 133, 137 |
| LEXICAL_DE: Hybriden von Zuckermais | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 137 |
| LEXICAL_DE: Hybridmais | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 133, 137 |
| LEXICAL_DE: Hülsenfrüchte | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 137, 139, 151 |
| LEXICAL_DE: Ingwer | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Inulin | COMPOSITION_TERM | 1 | 5 | — | — | — | 1 / 5 / 133–134, 152, 159 |
| LEXICAL_DE: Invertzucker | INGREDIENT_TERM | 3 | 4 | — | — | — | 3 / 4 / 134, 164–165 |
| LEXICAL_DE: Invertzuckercreme | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: Isabelle | VARIETY_NAME | 1 | 1 | — | — | — | 1 / 1 / 39 |
| LEXICAL_DE: Jacquez | VARIETY_NAME | 1 | 1 | — | — | — | 1 / 1 / 39 |
| LEXICAL_DE: Johannisbrot | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 119, 140, 153 |
| LEXICAL_DE: Johannisbrot (Carob) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Jojobaöl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Jungrindfleisch | INGREDIENT_TERM | 2 | 4 | — | — | — | 2 / 4 / 172–173 |
| LEXICAL_DE: Kaffee | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 156 |
| LEXICAL_DE: Kakao | REGULATORY_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 17, 144, 154, 170 |
| LEXICAL_DE: Kakaobohnen | RAW_MATERIAL | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Kakaoschalen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Kalbfleisch | INGREDIENT_TERM | 2 | 4 | — | — | — | 2 / 4 / 171–172 |
| LEXICAL_DE: Kanariensaat | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Kardamomen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Karfiol | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Karotten | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Karotten und Speisemöhren | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Kartoffeln | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 6 | — | — | — | 2 / 6 / 134, 140–141, 156 |
| LEXICAL_DE: Kaschu-Nüsse | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Keule | EDIBLE_PART | 1 | 4 | — | — | — | 1 / 4 / 166–168 |
| LEXICAL_DE: Kichererbsen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Kirschen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Kittharz | INGREDIENT_TERM | 1 | 3 | — | — | — | 1 / 3 / 147, 163 |
| LEXICAL_DE: Kleber von Weizen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: Klee | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 136, 153 |
| LEXICAL_DE: Kleie | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 134, 155 |
| LEXICAL_DE: Kleie und andere Rückstände | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 134, 155 |
| LEXICAL_DE: Knoblauch | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Knollensellerie | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Kohl | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Kohlrabi | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Kokosnüsse | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Kokosöl (Kopraöl) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Kolanüsse | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 139, 152 |
| LEXICAL_DE: Kolanüsse ( Cola spp.) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Konfitüren | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 141–142 |
| LEXICAL_DE: Konsummilch | PRODUCT_CATEGORY | 2 | 7 | — | — | — | 2 / 7 / 183–185 |
| LEXICAL_DE: Kopra | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Krebstieren | REGULATORY_SCOPE_TERM | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Kuhbohnen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Kuhbohnen ( Vigna unguiculata ) | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Kuhmilch | COMPOSITION_TERM | 2 | 6 | — | — | — | 2 / 6 / 7–8, 10, 37, 183 |
| LEXICAL_DE: Kurkuma | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Körner mit natürlichen Missbildungen | QUALITY_DESCRIPTION | 1 | 1 | — | — | — | 1 / 1 / 158 |
| LEXICAL_DE: Körner mit roten Rillen | QUALITY_DESCRIPTION | 1 | 2 | — | — | — | 1 / 2 / 158, 164 |
| LEXICAL_DE: Körner-Sorghum | INGREDIENT_TERM | 1 | 4 | — | — | — | 1 / 4 / 133, 137 |
| LEXICAL_DE: Küken | PRODUCT_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 162, 186 |
| LEXICAL_DE: Lactose | COMPOSITION_TERM | 1 | 6 | — | — | — | 1 / 6 / 134, 136, 144, 184 |
| LEXICAL_DE: Lactosesirup | COMPOSITION_TERM | 1 | 3 | — | — | — | 1 / 3 / 136, 144 |
| LEXICAL_DE: Lactuca sativa | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Lebendes Geflügel | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: Lebensmittelzubereitungen | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 134, 145 |
| LEXICAL_DE: Lebern | EDIBLE_PART | 2 | 4 | — | — | — | 2 / 4 / 145–146, 149 |
| LEXICAL_DE: Leinsamen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 138, 152 |
| LEXICAL_DE: Linsen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Loonzain-Reis | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 157 |
| LEXICAL_DE: Lorbeerblätter | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Lupinen | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 136, 153 |
| LEXICAL_DE: Lupulin | INGREDIENT_TERM | 1 | 4 | — | — | — | 1 / 4 / 36, 107, 138, 159 |
| LEXICAL_DE: Luzerne | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 10 | — | — | — | 2 / 10 / 136, 153 |
| LEXICAL_DE: Magermilch | INGREDIENT_TERM | 2 | 2 | — | — | — | 2 / 2 / 184 |
| LEXICAL_DE: Magermilchpulver | INGREDIENT_TERM | 2 | 6 | — | — | — | 2 / 6 / 6, 8, 10, 14 |
| LEXICAL_DE: Mais | REGULATORY_CATEGORY | 1 | 11 | — | — | — | 1 / 11 / 7–8, 12, 104, 133–134, 141 |
| LEXICAL_DE: Maltodextrin | PROCESSED_PRODUCT | 3 | 9 | — | — | — | 3 / 9 / 134–136, 144, 155–156 |
| LEXICAL_DE: Maltodextrin und Maltodextrinsirup | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: Maltodextrinsirup | PROCESSED_PRODUCT | 4 | 9 | — | — | — | 4 / 9 / 134–136, 144, 155–156 |
| LEXICAL_DE: Maltose | COMPOSITION_TERM | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: Malz | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Mangofrüchte | INGREDIENT_TERM | 2 | 2 | — | — | — | 2 / 2 / 139, 152 |
| LEXICAL_DE: Mangostanfrüchte | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 139, 152 |
| LEXICAL_DE: Maniok | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 133–134 |
| LEXICAL_DE: Marillen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Marmeladen | INGREDIENT_TERM | 2 | 3 | — | — | — | 2 / 3 / 141–142 |
| LEXICAL_DE: Meeressäugetieren | REGULATORY_SCOPE_TERM | 1 | 2 | — | — | — | 1 / 2 / 153, 155 |
| LEXICAL_DE: Mehl | REGULATORY_CATEGORY | 1 | 18 | — | — | — | 1 / 18 / 133, 136, 141–143, 149–150, 152–153, 155–156 |
| LEXICAL_DE: Mehl und Pellets von Fleisch | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 155 |
| LEXICAL_DE: Mehl und Pellets von Fleisch oder von Schlachtnebe nerzeugnissen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Mehl und Pellets von Luzerne | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 136, 153 |
| LEXICAL_DE: Mehl von Roggen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Mehl von anderem Getreide als Weizen oder Mengkorn | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Mehl von Ölsamen oder ölhaltigen Früchten | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Mehlbananen | PROCESSED_PRODUCT | 1 | 4 | — | — | — | 1 / 4 / 139, 142 |
| LEXICAL_DE: Melasse | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 108 |
| LEXICAL_DE: Melisse | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Melonen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 139–140 |
| LEXICAL_DE: Melonen (einschließlich Wassermelonen) und Papaya-Früchte | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Mengkorn | INGREDIENT_TERM | 1 | 4 | — | — | — | 1 / 4 / 133 |
| LEXICAL_DE: Met | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 155, 164 |
| LEXICAL_DE: Milch | INGREDIENT_TERM, PRODUCT_CATEGORY | 5 | 103 | — | — | — | 5 / 103 / 3, 5, 7, 16, 19, 36–37, 45, 67–71, 77–78, 82, 84, 91, 96–97, 99, 102, 109, 121–122, 144, 162, 182–185, 188, 194, 208 |
| LEXICAL_DE: Milcherzeugnisse | PRODUCT_CATEGORY | 4 | 49 | — | — | — | 4 / 49 / 3, 5, 36–37, 45, 67, 69, 71, 77–78, 82, 84, 91, 96–97, 99, 102, 109, 121–122, 135, 144, 155–156, 162, 170, 182–183 |
| LEXICAL_DE: Milchfett | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 162, 182–184, 194 |
| LEXICAL_DE: Milchstreichfett | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 194 |
| LEXICAL_DE: Minarine | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 188 |
| LEXICAL_DE: Mineralsalzen | COMPOSITION_TERM | 1 | 1 | — | — | — | 1 / 1 / 184 |
| LEXICAL_DE: Mischstreichfett | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 195 |
| LEXICAL_DE: Mischungen von getrockneten Früchten mit Bananen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 142 |
| LEXICAL_DE: Mlado vino portugizac | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 130 |
| LEXICAL_DE: Mohnsamen | RAW_MATERIAL | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Muskatblüte | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Muskatnüsse | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Mägen | EDIBLE_PART | 1 | 1 | — | — | — | 1 / 1 / 150 |
| LEXICAL_DE: Native Olivenöle | PRODUCT_CATEGORY | 2 | 3 | — | — | — | 2 / 3 / 188–189 |
| LEXICAL_DE: Naturkork | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 156 |
| LEXICAL_DE: Natürlicher Honig | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 147 |
| LEXICAL_DE: Nektarinen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Nierenzapfen | INGREDIENT_TERM | 2 | 6 | — | — | — | 2 / 6 / 143–144 |
| LEXICAL_DE: Noah | VARIETY_NAME | 1 | 1 | — | — | — | 1 / 1 / 39 |
| LEXICAL_DE: Oleomargarin | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Oleoresinen | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 151 |
| LEXICAL_DE: Oleostearin | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Oliven | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 2 | 16 | — | — | — | 2 / 16 / 90, 138, 140–141, 189 |
| LEXICAL_DE: Olivenöl | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 4 | 39 | — | — | — | 4 / 39 / 2, 5–6, 10, 33, 37, 82–83, 98, 109, 112, 138–139, 154–155, 188–189, 201 |
| LEXICAL_DE: Olivenöl - bestehend aus raffinierten Olivenölen und nativen Olivenölen | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 189 |
| LEXICAL_DE: Oregano | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Origanum vulgar | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Origanum vulgare | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Othello | VARIETY_NAME | 1 | 1 | — | — | — | 1 / 1 / 39 |
| LEXICAL_DE: Paddy-Reis | INGREDIENT_TERM | 1 | 4 | — | — | — | 1 / 4 / 135, 137, 157 |
| LEXICAL_DE: Palmherzen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 141 |
| LEXICAL_DE: Palmkernöl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Palmöl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Palmöl und seine Fraktionen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Papaya | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Paranüsse | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Pektinate | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Pektinstoffe | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Pellets von Reis | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 133, 135 |
| LEXICAL_DE: Pfeffer | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Pfeffer der Gattung " Piper" | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Pfefferminze | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Pfeilwurz | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Pferde | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 148, 156 |
| LEXICAL_DE: Pferdebohnen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Pfirsiche | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Pflanzen | REGULATORY_CATEGORY | 3 | 12 | — | — | — | 3 / 12 / 2, 25, 32–33, 80, 86, 99, 121, 143, 153 |
| LEXICAL_DE: Pflanzensäfte und Pflanzenauszüge von Hopfen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 138 |
| LEXICAL_DE: Pflanzliche Stoffe und pflanzliche Abfälle | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 135, 155 |
| LEXICAL_DE: Pflaumen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Phaseolus vulgaris | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Pilze | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 141 |
| LEXICAL_DE: Pilze und Trüffeln | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 141 |
| LEXICAL_DE: Pimenta | SOURCE_TAXON_NAME | 1 | 8 | — | — | — | 1 / 8 / 139–140, 151–152 |
| LEXICAL_DE: Piper | SOURCE_TAXON_NAME | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Pisum sativum | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Porree | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Puffbohnen | RAW_MATERIAL | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Puffbohnen (Dicke Bohnen) ( Vicia faba var. major ) | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 137, 151 |
| LEXICAL_DE: Quark | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 17, 144 |
| LEXICAL_DE: Quitten | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Raps- und Rübsenöl und Senföl sowie deren Fraktionen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Reisflocken | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Reiskörner | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Reismehl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Resinoiden | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 151 |
| LEXICAL_DE: Retsina | PRODUCT_CATEGORY, REGIONAL_NAME | 2 | 2 | — | — | — | 2 / 2 / 176 |
| LEXICAL_DE: Rettiche | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Rinder | PRODUCT_CATEGORY | 2 | 14 | — | — | — | 2 / 14 / 5, 7, 13, 15, 143, 148, 162, 166, 171 |
| LEXICAL_DE: Rindersperma | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 150 |
| LEXICAL_DE: Rindfleisch | REGULATORY_CATEGORY | 2 | 15 | — | — | — | 2 / 15 / 3, 7–9, 36, 99, 102, 109–110, 121–122, 143, 171, 173 |
| LEXICAL_DE: Roggen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 133 |
| LEXICAL_DE: Rohmilch | INGREDIENT_TERM | 3 | 25 | — | — | — | 3 / 25 / 67–71, 88, 184–185 |
| LEXICAL_DE: Rohreis | INGREDIENT_TERM | 2 | 18 | — | — | — | 2 / 18 / 5, 7–8, 12, 135, 137, 157, 164 |
| LEXICAL_DE: Rohzucker | COMPOSITION_TERM | 1 | 3 | — | — | — | 1 / 3 / 5, 165 |
| LEXICAL_DE: Rosmarin | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Rosskastanien | INGREDIENT_TERM | 2 | 2 | — | — | — | 2 / 2 / 135, 155 |
| LEXICAL_DE: Rote Rüben | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Rückstände aus der Stärkegewinnung und ähnliche Rücks tände | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 134 |
| LEXICAL_DE: Saccharose | COMPOSITION_TERM | 2 | 7 | — | — | — | 2 / 7 / 136, 159, 196, 198 |
| LEXICAL_DE: Safloröl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Safran | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 140, 152 |
| LEXICAL_DE: Sagomark | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 133, 152 |
| LEXICAL_DE: Salate | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Salate (Lactuca sativa) und Chicorée (Cichorium-Arten) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Salbei | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Salep | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 133 |
| LEXICAL_DE: Salz | COMPOSITION_TERM | 1 | 7 | — | — | — | 1 / 7 / 17–18, 22, 138, 140, 151 |
| LEXICAL_DE: Salzzusatz | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 187 |
| LEXICAL_DE: Samen | REGULATORY_CATEGORY | 2 | 4 | — | — | — | 2 / 4 / 106, 138, 153, 164 |
| LEXICAL_DE: Saumfleisch | EDIBLE_PART | 2 | 6 | — | — | — | 2 / 6 / 143–144 |
| LEXICAL_DE: Schaf- und Ziegenfleisch | REGULATORY_CATEGORY | 4 | 13 | — | — | — | 4 / 13 / 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 145 |
| LEXICAL_DE: Schafe | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 145, 168 |
| LEXICAL_DE: Schalen von Zitrusfrüchten oder von Melonen (einschl ießlich Wassermelonen) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Schalotten | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Schinken oder Schultern und Teile davon | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 148–149 |
| LEXICAL_DE: Schinken und Teile davon | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Schlachtkörper | EDIBLE_PART | 5 | 49 | — | — | — | 5 / 49 / 5–8, 13, 15, 166–169, 171 |
| LEXICAL_DE: Schlachtkörperhä lfte | EDIBLE_PART | 2 | 2 | — | — | — | 2 / 2 / 166, 168 |
| LEXICAL_DE: Schlachtnebenerzeugnisse | EDIBLE_PART | 6 | 29 | — | — | — | 6 / 29 / 143–147, 149–150, 154, 162, 171 |
| LEXICAL_DE: Schlehen | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Schmalzstearin | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Schmalzöl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Schultern und Teile davon | REGULATORY_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 148–149, 154 |
| LEXICAL_DE: Schwarzwurzeln | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Schwefeldioxid | INGREDIENT_TERM | 3 | 9 | — | — | — | 3 / 9 / 138, 140, 151 |
| LEXICAL_DE: Schweine | REGULATORY_CATEGORY | 2 | 5 | — | — | — | 2 / 5 / 6, 15, 148 |
| LEXICAL_DE: Schweinefett | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 145 |
| LEXICAL_DE: Schweinefett (einschließlich Schweineschmalz) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Schweinefleisch | REGULATORY_CATEGORY | 2 | 11 | — | — | — | 2 / 11 / 3, 7, 10, 36, 99, 102, 109–110, 122, 145 |
| LEXICAL_DE: Schweineschmalz | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Schweinespeck ohne magere Teile | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Seidenraupen | INGREDIENT_TERM | 1 | 4 | — | — | — | 1 / 4 / 3, 82, 147 |
| LEXICAL_DE: Senfsamen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Sesamsamen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Sojabohnen | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 138, 152 |
| LEXICAL_DE: Sojaöl | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 153, 155 |
| LEXICAL_DE: Sojaöl und seine Fraktionen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Sonnenblumenkerne | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 138, 152 |
| LEXICAL_DE: Sonnenblumenöl | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Speisemöhren | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Speiserüben | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Speisezwiebeln | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Spelz | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 133, 137 |
| LEXICAL_DE: Steckrüben | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 140, 153 |
| LEXICAL_DE: Sternanis | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Straucherbsen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Straucherbsen ( Cajanus cajan ) | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Streichfett | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 194 |
| LEXICAL_DE: Streichfette | REGULATORY_CATEGORY | 1 | 6 | — | — | — | 1 / 6 / 33, 37, 40, 45, 187, 194 |
| LEXICAL_DE: Stroh und Spreu von Getreide | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Stärke | REGULATORY_CATEGORY | 1 | 15 | — | — | — | 1 / 15 / 133–135, 141, 144, 152, 155–156, 158 |
| LEXICAL_DE: Stärke von Reis | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: Süßkartoffeln | INGREDIENT_TERM | 1 | 4 | — | — | — | 1 / 4 / 133, 139, 141 |
| LEXICAL_DE: Tafeltrauben | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 139, 142 |
| LEXICAL_DE: Talgöl | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Tange | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 153 |
| LEXICAL_DE: Tee | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Teigwaren | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Thymian | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 140, 152 |
| LEXICAL_DE: Tierische und pflanzliche Fette und Öle sowie deren Fra ktionen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: Tokaji eszenci a | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 176 |
| LEXICAL_DE: Tokaji fordítás | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 200 |
| LEXICAL_DE: Tokaji máslás | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 200 |
| LEXICAL_DE: Tokajská esencia | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 176 |
| LEXICAL_DE: Tokajský forditáš | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 200 |
| LEXICAL_DE: Tokajský mášláš | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 200 |
| LEXICAL_DE: Tomaten | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 139, 141 |
| LEXICAL_DE: Topinambur | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 133, 139 |
| LEXICAL_DE: Trauben | INGREDIENT_TERM | 3 | 30 | — | — | — | 3 / 30 / 47, 94, 142, 160, 175–176, 179, 181, 196–200 |
| LEXICAL_DE: Treber | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 134 |
| LEXICAL_DE: Trüffeln | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 141 |
| LEXICAL_DE: Vanille | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Verarbeitungserzeugnisse aus Obst und Gemüse | REGULATORY_CATEGORY | 3 | 12 | — | — | — | 3 / 12 / 2, 5, 17, 33, 99, 101–102, 109–110, 140 |
| LEXICAL_DE: Vigna angularis | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Vigna mungo | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Vigna radiata | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Vigna subterranea | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Vigna unguiculata | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Vinho Verde | REGIONAL_NAME | 1 | 1 | — | — | — | 1 / 1 / 191 |
| LEXICAL_DE: Vitaminen | COMPOSITION_TERM | 1 | 2 | — | — | — | 1 / 2 / 184 |
| LEXICAL_DE: Vitis vinifera | SOURCE_TAXON_NAME | 2 | 6 | — | — | — | 2 / 6 / 39, 47 |
| LEXICAL_DE: Voandzeia subterranea | SOURCE_TAXON_NAME | 1 | 2 | — | — | — | 1 / 2 / 137, 151 |
| LEXICAL_DE: Vogeleier | REGULATORY_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 146, 150, 162 |
| LEXICAL_DE: Vogeleier in der Schale | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 150 |
| LEXICAL_DE: Vollmilch | PROCESSED_PRODUCT | 1 | 6 | — | — | — | 1 / 6 / 184 |
| LEXICAL_DE: Waren tierischen Ursprungs | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 150 |
| LEXICAL_DE: Wassermelonen | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 139–140 |
| LEXICAL_DE: Weichtieren | REGULATORY_SCOPE_TERM | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Weichweizen | RAW_MATERIAL | 1 | 6 | — | — | — | 1 / 6 / 7–8, 12, 133 |
| LEXICAL_DE: Weichweizen und Mengkorn | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 133 |
| LEXICAL_DE: Weinblätter | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 141 |
| LEXICAL_DE: Weinstein | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 155 |
| LEXICAL_DE: Weinsäure | INGREDIENT_TERM | 1 | 3 | — | — | — | 1 / 3 / 176, 198 |
| LEXICAL_DE: Weintrauben | REGULATORY_CATEGORY | 6 | 32 | — | — | — | 6 / 32 / 4, 47, 90, 140, 142, 160–161, 175–179, 196, 198–200 |
| LEXICAL_DE: Weintrub/Weingeläger | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Weißzucker | COMPOSITION_TERM | 1 | 5 | — | — | — | 1 / 5 / 5, 10, 65, 164 |
| LEXICAL_DE: Wicken | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 136, 153 |
| LEXICAL_DE: Wirsingkohl | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Würste | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Yamswurzeln | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 141 |
| LEXICAL_DE: Zichorienwurzeln | REGULATORY_CATEGORY | 2 | 3 | — | — | — | 2 / 3 / 153, 156 |
| LEXICAL_DE: Ziegen | REGULATORY_CATEGORY | 2 | 15 | — | — | — | 2 / 15 / 128, 145–146, 149–150, 153–154 |
| LEXICAL_DE: Zimt | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Zimt und Zimtblüten | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Zimtblüten | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 152 |
| LEXICAL_DE: Zitrusfrüchte | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 139 |
| LEXICAL_DE: Zubereitungen aus Blut aller Tierarten | REGULATORY_CATEGORY | 2 | 3 | — | — | — | 2 / 3 / 145, 154 |
| LEXICAL_DE: Zubereitungen und haltbar gemachte Erzeugnisse aus L ebern aller Tierarten | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 145 |
| LEXICAL_DE: Zubereitungen von der zur Fütterung verwendeten Art | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 135, 144, 155 |
| LEXICAL_DE: Zucker und Melassen | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 134, 136 |
| LEXICAL_DE: Zuckerrohr | INGREDIENT_TERM | 1 | 5 | — | — | — | 1 / 5 / 64, 136, 153 |
| LEXICAL_DE: Zuckerrüben | INGREDIENT_TERM | 2 | 14 | — | — | — | 2 / 14 / 64, 136, 153, 159, 202, 204 |
| LEXICAL_DE: Zuckersirupe | REGULATORY_CATEGORY | 1 | 5 | — | — | — | 1 / 5 / 134, 136 |
| LEXICAL_DE: Zwerchfellpfeiler | INGREDIENT_TERM | 2 | 7 | — | — | — | 2 / 7 / 143–144, 168 |
| LEXICAL_DE: Zwerchfellpfeiler (Nierenzapfen) und Saumfleisch | REGULATORY_CATEGORY | 4 | 6 | — | — | — | 4 / 6 / 143–144 |
| LEXICAL_DE: als Pulver | REGULATORY_CATEGORY | 1 | 3 | — | — | — | 1 / 3 / 134, 138, 140 |
| LEXICAL_DE: als weißes | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: andere Stärke | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: andere Ölsamen und ölhaltige Früchte | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 138, 153 |
| LEXICAL_DE: anderes Gemüse | REGULATORY_CATEGORY | 2 | 8 | — | — | — | 2 / 8 / 139, 141, 151, 155 |
| LEXICAL_DE: anderes Gemüse und Mischungen von Gemüsen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: aus Maiskeimen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 134 |
| LEXICAL_DE: aus Milch stammendem Eiweiß | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 184 |
| LEXICAL_DE: bernsteinfarbene Körner | QUALITY_DESCRIPTION | 2 | 3 | — | — | — | 2 / 3 / 159, 164 |
| LEXICAL_DE: entalkoholisierter Wein | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 43, 143 |
| LEXICAL_DE: entrahmte Milch | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 184 |
| LEXICAL_DE: fettarme Milch | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 184 |
| LEXICAL_DE: fleckige Körner | QUALITY_DESCRIPTION | 1 | 2 | — | — | — | 1 / 2 / 158, 164 |
| LEXICAL_DE: frisch | PRODUCT_CATEGORY | 2 | 63 | — | — | — | 2 / 63 / 35, 133, 138–140, 142–143, 145–146, 148–153, 156, 162, 171, 186, 201 |
| LEXICAL_DE: frische Geflügelfleischzubereitung | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 186 |
| LEXICAL_DE: frisches Geflügelfleisch | PRODUCT_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 185–186 |
| LEXICAL_DE: ganze Körner | QUALITY_DESCRIPTION | 1 | 3 | — | — | — | 1 / 3 / 157–158, 164 |
| LEXICAL_DE: ganze oder halbe Tierkörper | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 148 |
| LEXICAL_DE: gebrochene Körner oder Bruchreis | QUALITY_DESCRIPTION | 1 | 1 | — | — | — | 1 / 1 / 158 |
| LEXICAL_DE: gefleckte Körner | QUALITY_DESCRIPTION | 1 | 2 | — | — | — | 1 / 2 / 158, 164 |
| LEXICAL_DE: gefrorenes Geflügelfleisch | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 185 |
| LEXICAL_DE: gelbe Körner | QUALITY_DESCRIPTION | 2 | 3 | — | — | — | 2 / 3 / 159, 164 |
| LEXICAL_DE: genießbare Mischungen und Zubereitungen von tierischen oder pflanzlichen Fetten und Ölen | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 154 |
| LEXICAL_DE: geschälter Reis ("Cargo-Reis" oder "Braunreis") | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: gestutzte Körner | QUALITY_DESCRIPTION | 1 | 1 | — | — | — | 1 / 1 / 158 |
| LEXICAL_DE: getrocknet | REGULATORY_CATEGORY | 6 | 44 | — | — | — | 6 / 44 / 133–134, 136, 138–140, 142–146, 149–150, 152–153, 156 |
| LEXICAL_DE: grüne Körner | QUALITY_DESCRIPTION | 1 | 1 | — | — | — | 1 / 1 / 158 |
| LEXICAL_DE: halbgeschliffener oder vollständig geschliffener Reis | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 135 |
| LEXICAL_DE: karamellisiert | INGREDIENT_TERM | 1 | 3 | — | — | — | 1 / 3 / 134, 136 |
| LEXICAL_DE: kreidige Körner | QUALITY_DESCRIPTION | 1 | 3 | — | — | — | 1 / 3 / 158, 164 |
| LEXICAL_DE: langkörniger Reis A und B | QUALITY_DESCRIPTION | 1 | 1 | — | — | — | 1 / 1 / 157 |
| LEXICAL_DE: nicht standardisierte Vollmilch | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 184 |
| LEXICAL_DE: oder Milcherzeugnisse enthalten | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 156, 183 |
| LEXICAL_DE: pflanzlich | ORIGIN_QUALIFIER | 1 | 1 | — | — | — | 1 / 1 / 188 |
| LEXICAL_DE: reinrassige Zuchttiere ( α ) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 148 |
| LEXICAL_DE: reinrassige Zuchttiere ( β ) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 148 |
| LEXICAL_DE: riso sbramato | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 157 |
| LEXICAL_DE: saure Milch | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 144 |
| LEXICAL_DE: saurer Rahm | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 144 |
| LEXICAL_DE: standardisierte Vollmilch | PROCESSED_PRODUCT | 1 | 2 | — | — | — | 1 / 2 / 184 |
| LEXICAL_DE: teilentrahmte Milch | INGREDIENT_TERM | 1 | 2 | — | — | — | 1 / 2 / 184 |
| LEXICAL_DE: tiefgefrorenes Geflügelfleisch | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 185 |
| LEXICAL_DE: ungenießbar oder ungenießbar gemacht ( δ ) | REGULATORY_CATEGORY | 1 | 4 | — | — | — | 1 / 4 / 150 |
| LEXICAL_DE: unmittelbar aus Milch oder Rahm herges tellt | PRODUCT_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 162 |
| LEXICAL_DE: wilder Majoran | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: zum industriellen Herstellen von ätherischen Ö len oder von Resinoiden ( γ ) | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 151 |
| LEXICAL_DE: Äpfel | INGREDIENT_TERM | 1 | 1 | — | — | — | 1 / 1 / 140 |
| LEXICAL_DE: Öldrass und Soapstock aus der Verarbeitung von Fetts toffen oder von tierischen oder pflanz­ | REGULATORY_CATEGORY | 2 | 2 | — | — | — | 2 / 2 / 154 |
| LEXICAL_DE: Ölkuchen und andere feste Rückstände aus der Gewinnu ng von Erdnussöl | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Ölkuchen und andere feste Rückstände aus der Gewinnu ng von Sojaöl | REGULATORY_CATEGORY | 1 | 1 | — | — | — | 1 / 1 / 155 |
| LEXICAL_DE: Ölkuchen und andere feste Rückstände aus der Gewinnung pflanzlicher Fette oder Öle | REGULATORY_CATEGORY | 1 | 2 | — | — | — | 1 / 2 / 134, 155 |
| LEXICAL_DE: Ölsäure | PROCESSED_PRODUCT | 2 | 7 | — | — | — | 2 / 7 / 189 |
| LEXICAL_DE: ätherischen Ölen | PROCESSED_PRODUCT | 1 | 1 | — | — | — | 1 / 1 / 151 |
| LEXICAL_EN: Acorns | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 135, 155 | — |
| LEXICAL_EN: Acorns and horse-chestnuts | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 135, 155 | — |
| LEXICAL_EN: Adzuki | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Aleppo pine resin | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 176 | — |
| LEXICAL_EN: Amber grains | QUALITY_DESCRIPTION | 2 | 2 | — | — | 2 / 2 / 159, 164 | — |
| LEXICAL_EN: Animal or vegetable fats and oils and their fractions | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Animal products | REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 150, 187, 195 | — |
| LEXICAL_EN: Apiculture products | PRODUCT_CATEGORY | 2 | 3 | — | — | 2 / 3 / 3, 147, 163 | — |
| LEXICAL_EN: Apples | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Apricots | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Areca nuts | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Argol | PROCESSED_PRODUCT | 2 | 2 | — | — | 2 / 2 / 155 | — |
| LEXICAL_EN: Asses | REGULATORY_CATEGORY | 2 | 6 | — | — | 2 / 6 / 148–149 | — |
| LEXICAL_EN: Avocados | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 139, 152 | — |
| LEXICAL_EN: Bambara beans | RAW_MATERIAL | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Bambara beans ( Vigna subterranea o r Voandzeia subterranea ) | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Banana juice | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 141–142 | — |
| LEXICAL_EN: Bananas | INGREDIENT_TERM | 3 | 23 | — | — | 3 / 23 / 2, 33, 99, 102, 140–142, 152 | — |
| LEXICAL_EN: Bananas preserved by sugar | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 141–142 | — |
| LEXICAL_EN: Bananas provisionally preserved | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 140, 142 | — |
| LEXICAL_EN: Barley | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 6 | — | — | 2 / 6 / 7–8, 12, 133 | — |
| LEXICAL_EN: Barley flour | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Basil | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Beans of the species Vigna mungo (L) Hepper or Vigna radiata (L) Wilczek | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: Beans of the species Vigna mungo (L.) Hepper or Vigna radiata (L.) Wilczek | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Beeswax | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 147, 163 | — |
| LEXICAL_EN: Beet pulp | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 64, 136 | — |
| LEXICAL_EN: Bellies (streaky) and cuts thereof | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 149 | — |
| LEXICAL_EN: Birds' eggs | REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 150, 162 | — |
| LEXICAL_EN: Blended spread | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 195 | — |
| LEXICAL_EN: Bovine animals | PRODUCT_CATEGORY | 5 | 40 | — | — | 5 / 40 / 5, 7–8, 10, 13, 143–144, 148–150, 153, 162, 166–167, 171–174 | — |
| LEXICAL_EN: Bovine semen | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 150 | — |
| LEXICAL_EN: Bran | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 57, 134, 155 | — |
| LEXICAL_EN: Brazil nuts | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Brewing or distilling dregs and waste | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 134 | — |
| LEXICAL_EN: Broad beans | RAW_MATERIAL | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Broad beans ( Vicia faba var. major ) and horse beans ( Vicia faba var. e quina and Vicia | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: Broad beans ( Vicia faba var. major) and horse beans ( Vicia faba var. equina | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 137, 151 | — |
| LEXICAL_EN: Broken grains or fragments | QUALITY_DESCRIPTION | 1 | 1 | — | — | 1 / 1 / 158 | — |
| LEXICAL_EN: Buckwheat | RAW_MATERIAL | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Butter and other fats and oils derived from milk | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: Cabbages | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Cajanus cajan | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Cane or beet sugar and chemically pure sucrose | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Cannabis sativa | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Capsicum annuum | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Caramel containing 50 % or more by weight of sucrose in the dry matter | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Carcass | EDIBLE_PART | 2 | 17 | — | — | 2 / 17 / 16, 128, 166–169 | — |
| LEXICAL_EN: Carcasses and half-carcasses | REGULATORY_CATEGORY | 2 | 5 | — | — | 2 / 5 / 148, 167, 169 | — |
| LEXICAL_EN: Carrots | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Cereal flours | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Cereal grains otherwise worked (for example | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Cereal groats | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Cereal straw and husks | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Chalky grains | QUALITY_DESCRIPTION | 2 | 3 | — | — | 2 / 3 / 158, 164 | — |
| LEXICAL_EN: Cheese and curd | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: Chickpeas | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Chickpeas (garbanzos) | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Chicks | PRODUCT_CATEGORY | 2 | 2 | — | — | 2 / 2 / 162, 186 | — |
| LEXICAL_EN: Chicory roots | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 153, 156 | — |
| LEXICAL_EN: Cichorium intybus | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Cinnamon | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 152 | — |
| LEXICAL_EN: Cinnamon and cinnamon-tree flowers | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Citrus fruit | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 139–140 | — |
| LEXICAL_EN: Clinton | VARIETY_NAME | 1 | 1 | — | — | 1 / 1 / 39 | — |
| LEXICAL_EN: Clipped grains | QUALITY_DESCRIPTION | 2 | 2 | — | — | 2 / 2 / 158, 164 | — |
| LEXICAL_EN: Cloves | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 152 | — |
| LEXICAL_EN: Cloves (whole fruit | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Cocoa beans | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Cocoa shells | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Coconut (copra) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Coconuts | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Coffee | REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 156 | — |
| LEXICAL_EN: Common wheat | RAW_MATERIAL | 3 | 6 | — | — | 3 / 6 / 7–8, 12, 133 | — |
| LEXICAL_EN: Common wheat and meslin seed | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Copra | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 152, 154 | — |
| LEXICAL_EN: Cotton seeds | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Cow peas | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Cow peas ( Vigna unguiculata ) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: Cow peas ( Vigna unguiculta ) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Cucumbers | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Cucumbers and gherkins | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Dairy spread | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 194 | — |
| LEXICAL_EN: Dates | INGREDIENT_TERM | 2 | 3 | — | — | 2 / 3 / 152, 199 | — |
| LEXICAL_EN: Derived products | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 162 | — |
| LEXICAL_EN: Dog or cat food | REGULATORY_CATEGORY | 1 | 3 | — | — | 1 / 3 / 135, 144, 155 | — |
| LEXICAL_EN: Dried | REGULATORY_CATEGORY | 3 | 64 | — | — | 3 / 64 / 2, 5, 133–134, 136–140, 142–146, 149–153, 156, 159–160, 177, 203 | — |
| LEXICAL_EN: Dried bananas | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 142 | — |
| LEXICAL_EN: Dried figs | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Dried grapes | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 140, 160, 177 | — |
| LEXICAL_EN: Dried leguminous vegetables | REGULATORY_CATEGORY | 3 | 4 | — | — | 3 / 4 / 137, 151–152 | — |
| LEXICAL_EN: Dried plantains | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Dried sweet peppers ( Capsicum annuum ) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Dried vegetables | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Durum wheat | COMPOSITION_TERM | 2 | 5 | — | — | 2 / 5 / 7–8, 12, 133 | — |
| LEXICAL_EN: Edible flours and meals of meat or meat offal | REGULATORY_CATEGORY | 3 | 3 | — | — | 3 / 3 / 143, 149–150 | — |
| LEXICAL_EN: Edible meat offal of bovine animals | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: Edible offal of bovine animals | REGULATORY_CATEGORY | 1 | 3 | — | — | 1 / 3 / 143, 149 | — |
| LEXICAL_EN: Edible offal of domestic swine | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Edible offal of sheep and goats | REGULATORY_CATEGORY | 1 | 3 | — | — | 1 / 3 / 145–146 | — |
| LEXICAL_EN: Edible products of animal origin | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 150 | — |
| LEXICAL_EN: Eggs for hatching | PRODUCT_CATEGORY | 2 | 4 | — | — | 2 / 4 / 162, 186 | — |
| LEXICAL_EN: Eggs in shell | PRODUCT_CATEGORY | 2 | 2 | — | — | 2 / 2 / 162 | — |
| LEXICAL_EN: Ethyl alcohol and other spirits | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 147 | — |
| LEXICAL_EN: Extracts and juices of meat | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Fat spreads | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 194 | — |
| LEXICAL_EN: Fats and oils and their fractions | REGULATORY_CATEGORY | 3 | 3 | — | — | 3 / 3 / 153–154 | — |
| LEXICAL_EN: Fats of bovine animals | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 144, 153 | — |
| LEXICAL_EN: Fats of sheep or goats | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 146 | — |
| LEXICAL_EN: Fermented milk products with fruit juice, naturally flavoured or non-flavoured | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 170 | — |
| LEXICAL_EN: Fermented milk products without fruit juice, naturally flavou red | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 170 | — |
| LEXICAL_EN: Fermented or non-fermented milk products with fruit, naturally flavoured or non-flavoured. | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 170 | — |
| LEXICAL_EN: Figs | INGREDIENT_TERM | 2 | 3 | — | — | 2 / 3 / 139–140, 152 | — |
| LEXICAL_EN: Flaked grains of rice | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Flavoured or coloured isoglucose syrups | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Flavoured or coloured lactose syrup | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: Flavoured or coloured sugar syrups | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 134, 136 | — |
| LEXICAL_EN: Flax | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 8 | — | — | 2 / 8 / 2, 5, 10, 98, 139 | — |
| LEXICAL_EN: Flour | REGULATORY_CATEGORY | 2 | 12 | — | — | 2 / 12 / 133, 135, 141–142, 152, 156 | — |
| LEXICAL_EN: Flours | REGULATORY_CATEGORY | 2 | 7 | — | — | 2 / 7 / 133, 143, 149–150, 153, 155 | — |
| LEXICAL_EN: Flours and meals of oil seeds or oleaginous fruits | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Food preparations | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 134, 145 | — |
| LEXICAL_EN: For the industrial manufacture of essential oi ls or resinoids ( γ ) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: For the manufacture of pharmaceutical products ( γ ) | REGULATORY_CATEGORY | 2 | 8 | — | — | 2 / 8 / 149 | — |
| LEXICAL_EN: Fresh bananas | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 142 | — |
| LEXICAL_EN: Fresh grape must with fermentation arrested by the addition of alcohol | PRODUCT_CATEGORY | 2 | 2 | — | — | 2 / 2 / 160, 199 | — |
| LEXICAL_EN: Fresh plantains | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Fresh table grapes | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Fruit | REGULATORY_CATEGORY | 3 | 78 | — | — | 3 / 78 / 2, 5, 16–17, 19–20, 33, 35, 75, 82, 85, 99, 101–102, 109–110, 121, 128, 130, 135, 138–141, 144, 152–153, 155, 160, 170, 176, 188 | — |
| LEXICAL_EN: Fruit and nuts | REGULATORY_CATEGORY | 1 | 3 | — | — | 1 / 3 / 140 | — |
| LEXICAL_EN: Fruit juices (excluding grape juice and grape must of s ubheadings 2009 61 and 2009 69 and | LAYOUT_FRAGMENT | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: Fruits of the genus Capsicum | REGULATORY_CATEGORY | 2 | 8 | — | — | 2 / 8 / 140, 151–152, 155 | — |
| LEXICAL_EN: Fruits of the genus Capsicum or of the genus Pimenta | REGULATORY_CATEGORY | 4 | 6 | — | — | 4 / 6 / 140, 151–152 | — |
| LEXICAL_EN: Gallus domesticus | SOURCE_TAXON_NAME | 2 | 3 | — | — | 2 / 3 / 146, 162, 185 | — |
| LEXICAL_EN: Ginger | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Glucose and glucose syrup | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 134 | — |
| LEXICAL_EN: Glucose syrup and maltodextrine syrup | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Goose or duck livers | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 147 | — |
| LEXICAL_EN: Grain sorghum | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 133, 137 | — |
| LEXICAL_EN: Grain sorghum hybrids | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Grains showing natural malformation | QUALITY_DESCRIPTION | 1 | 1 | — | — | 1 / 1 / 158 | — |
| LEXICAL_EN: Grains striated with red | QUALITY_DESCRIPTION | 2 | 2 | — | — | 2 / 2 / 158, 164 | — |
| LEXICAL_EN: Green grains | QUALITY_DESCRIPTION | 1 | 1 | — | — | 1 / 1 / 158 | — |
| LEXICAL_EN: Groats and meal of wheat | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 133 | — |
| LEXICAL_EN: Ground-nuts | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Groundnut oil | PROCESSED_PRODUCT | 2 | 2 | — | — | 2 / 2 / 153, 155 | — |
| LEXICAL_EN: Groundnut oil and its fractions | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Groundnuts | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 138, 152 | — |
| LEXICAL_EN: Guavas | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 139, 152 | — |
| LEXICAL_EN: Guts | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 150 | — |
| LEXICAL_EN: Hams | REGULATORY_CATEGORY | 1 | 4 | — | — | 1 / 4 / 148–149, 154 | — |
| LEXICAL_EN: Hams and cuts thereof | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Hemp seeds | RAW_MATERIAL | 2 | 2 | — | — | 2 / 2 / 106, 152 | — |
| LEXICAL_EN: Herbemont | VARIETY_NAME | 1 | 1 | — | — | 1 / 1 / 39 | — |
| LEXICAL_EN: Homogenised preparations of bananas | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 141–142 | — |
| LEXICAL_EN: Homogenised preparations of meat | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Hop cones | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Horsemeat | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 156 | — |
| LEXICAL_EN: Humulus lupulus | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 159 | — |
| LEXICAL_EN: Husked (brown) rice | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Hybrid maize (corn) seed | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Inulin | COMPOSITION_TERM, REGULATORY_CATEGORY | 2 | 7 | — | — | 2 / 7 / 133–134, 136, 152, 159 | — |
| LEXICAL_EN: Isabelle | VARIETY_NAME | 1 | 1 | — | — | 1 / 1 / 39 | — |
| LEXICAL_EN: Jacquez | VARIETY_NAME | 1 | 1 | — | — | 1 / 1 / 39 | — |
| LEXICAL_EN: Jams | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 141–142 | — |
| LEXICAL_EN: Jerusalem artichokes | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 133, 139 | — |
| LEXICAL_EN: Kidney beans | RAW_MATERIAL | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Kola nuts | PROCESSED_PRODUCT | 2 | 2 | — | — | 2 / 2 / 139, 152 | — |
| LEXICAL_EN: Kola nuts ( Cola spp.) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Lactose and lactose syrup | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: Lactuca sativa | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Lambs (up to one year old) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Lard stearin | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Leguminous vegetables | REGULATORY_CATEGORY | 3 | 5 | — | — | 3 / 5 / 137, 139, 151–152 | — |
| LEXICAL_EN: Lentils | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Lettuce | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Lettuce ( Lactuca sativa ) and chicory ( Cichorium spp.) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Linseed | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 138, 152 | — |
| LEXICAL_EN: Live bovine animals | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 148 | — |
| LEXICAL_EN: Live goats | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 145 | — |
| LEXICAL_EN: Live goats — pure-bred breeding animals | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Live horses | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 148, 156 | — |
| LEXICAL_EN: Live poultry | PRODUCT_CATEGORY | 1 | 2 | — | — | 1 / 2 / 146, 162 | — |
| LEXICAL_EN: Live sheep | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 145 | — |
| LEXICAL_EN: Live sheep — pure-bred breeding animals | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Live swine | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 145, 148 | — |
| LEXICAL_EN: Livers | REGULATORY_CATEGORY | 2 | 6 | — | — | 2 / 6 / 146–147, 149–150 | — |
| LEXICAL_EN: Locust beans | RAW_MATERIAL, REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 119, 140, 153 | — |
| LEXICAL_EN: Locust beans (carob) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Lucerne | REGULATORY_CATEGORY | 2 | 12 | — | — | 2 / 12 / 136, 153, 156 | — |
| LEXICAL_EN: Lucerne (alfalfa) meal and pellets | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Maize | RAW_MATERIAL, REGULATORY_CATEGORY | 2 | 14 | — | — | 2 / 14 / 7–8, 12, 104, 133–134, 137, 141, 155 | — |
| LEXICAL_EN: Maize (corn) flour | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Maize (corn) seed | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 133, 137 | — |
| LEXICAL_EN: Maize (corn) starch | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Malt | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Maltodextrine | PROCESSED_PRODUCT | 3 | 17 | — | — | 3 / 17 / 134–136, 144, 155–156 | — |
| LEXICAL_EN: Maltodextrine and maltodextrine syrup | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Manioc | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 133–134 | — |
| LEXICAL_EN: Manioc (cassava) starch | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Maple sugar | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Maple sugar and maple syrup | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Meal and pellets of lucerne artificially he at-dried | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Meat and edible meat offal | REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 145, 149 | — |
| LEXICAL_EN: Meat and edible meat offal of domestic swine | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Meat and edible offal | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 146 | — |
| LEXICAL_EN: Meat of asses | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 148 | — |
| LEXICAL_EN: Meat of bovine animals | REGULATORY_CATEGORY | 4 | 13 | — | — | 4 / 13 / 10, 143, 171–174 | — |
| LEXICAL_EN: Meat of domestic swine | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Meat of horses | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 156 | — |
| LEXICAL_EN: Meat of sheep and goats | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 145 | — |
| LEXICAL_EN: Meat of sheep or goats | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Meat of swine | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 148–149 | — |
| LEXICAL_EN: Melons | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 139–140 | — |
| LEXICAL_EN: Melons (including watermelons) and papaws (papayas) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Milk and cream | REGULATORY_CATEGORY | 3 | 4 | — | — | 3 / 4 / 144 | — |
| LEXICAL_EN: Milk-based drinks with cocoa, with fruit juice or naturally f lavoured | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 170 | — |
| LEXICAL_EN: Mixtures containing dried bananas | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 142 | — |
| LEXICAL_EN: Mlado vino portugizac | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 130 | — |
| LEXICAL_EN: Molasses | PROCESSED_PRODUCT | 2 | 4 | — | — | 2 / 4 / 108, 136 | — |
| LEXICAL_EN: Molasses resulting from the extraction or refining of sugar | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: Mushrooms | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: Mushrooms and truffles | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: Mustard seeds | RAW_MATERIAL | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Natural cork | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 156 | — |
| LEXICAL_EN: Natural honey | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 134, 147 | — |
| LEXICAL_EN: Noah | VARIETY_NAME | 1 | 1 | — | — | 1 / 1 / 39 | — |
| LEXICAL_EN: Nutmeg | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Oat flour | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Oats | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 133, 201 | — |
| LEXICAL_EN: Oil foots and dregs | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Oilcake and other solid residues | REGULATORY_CATEGORY | 3 | 6 | — | — | 3 / 6 / 134, 155 | — |
| LEXICAL_EN: Olive oil and its fractions | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Olive oils composed of refined olive oils and virgin olive oil s | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 189 | — |
| LEXICAL_EN: Olives | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 3 | 30 | — | — | 3 / 30 / 2, 5, 10, 33, 37, 82–83, 90, 98, 109, 112, 138, 140–141 | — |
| LEXICAL_EN: Olives (uncooked or cooked by steaming or boiling wa ter) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Olives dried | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Olives prepared or preserved by vinegar or acetic ac id | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Olives prepared or preserved otherwise than by vi negar or acetic acid | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 138 | — |
| LEXICAL_EN: Olives provisionally preserved (for example | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Onions | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Origanum vulgare | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Othello | VARIETY_NAME | 1 | 1 | — | — | 1 / 1 / 39 | — |
| LEXICAL_EN: Other dried leguminous vegetables | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Other fermented beverages (for example | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: Other fruit | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Other live animals | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 148 | — |
| LEXICAL_EN: Other meat and edible meat offal | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 149 | — |
| LEXICAL_EN: Other nuts | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 139, 152 | — |
| LEXICAL_EN: Other oils and their fractions | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Other oilseeds and oleaginous fruits | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Other prepared or preserved meat | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Other prepared or preserved meat containing bovine m eat or offal | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 143 | — |
| LEXICAL_EN: Other prepared or preserved meat containing bovine m eat or offal other than uncooked | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: Other prepared or preserved meat or meat offal o f sheep or goats | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 146 | — |
| LEXICAL_EN: Other prepared or preserved meat or meat offal of bo vine animals | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 143 | — |
| LEXICAL_EN: Other starches | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Other sugars | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Other vegetables | REGULATORY_CATEGORY | 1 | 4 | — | — | 1 / 4 / 139, 151 | — |
| LEXICAL_EN: Other vegetables and mixtures of vegetables | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: Other vegetables prepared or preserved otherwise than b y vinegar or acetic acid | REGULATORY_CATEGORY | 1 | 3 | — | — | 1 / 3 / 141, 155 | — |
| LEXICAL_EN: Palm oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Palm oil and its fractions | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Peas | INGREDIENT_TERM | 2 | 6 | — | — | 2 / 6 / 137, 151 | — |
| LEXICAL_EN: Peas ( Pisum sativum ) | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Pectic substances | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Pectic substances and pectinates | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Peel of citrus fruit or melons (including watermelon s) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Pellets of rice | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 133, 135 | — |
| LEXICAL_EN: Pepper | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Pepper of the genus Piper | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Phaseolus vulgaris | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Pig fat | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 145 | — |
| LEXICAL_EN: Pigeon peas | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Pigeon peas ( Cajanus cajan ) | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Pimenta | SOURCE_TAXON_NAME | 1 | 8 | — | — | 1 / 8 / 139–140, 151–152 | — |
| LEXICAL_EN: Pineapples | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 139, 152 | — |
| LEXICAL_EN: Piper | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Pisum sativum | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Plantains | INGREDIENT_TERM | 1 | 4 | — | — | 1 / 4 / 139, 142 | — |
| LEXICAL_EN: Plants and parts of plants (including seeds and fruits) of a kind used primarily in perfumery | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Poppy seeds | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Potato starch | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Potatoes | INGREDIENT_TERM | 3 | 9 | — | — | 3 / 9 / 133, 139–141, 156 | — |
| LEXICAL_EN: Poultry fat | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 146 | — |
| LEXICAL_EN: Preparations of a kind used in animal feeding | REGULATORY_CATEGORY | 1 | 3 | — | — | 1 / 3 / 135, 144, 155 | — |
| LEXICAL_EN: Preparations of blood of any animal | REGULATORY_CATEGORY | 3 | 3 | — | — | 3 / 3 / 145, 154 | — |
| LEXICAL_EN: Preparations or preserves of liver of any animal | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Protein concentrates obtained from lucerne juice and grass juice | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 136, 156 | — |
| LEXICAL_EN: Pure-bred breeding animals ( α ) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 148 | — |
| LEXICAL_EN: Pure-bred breeding animals ( β ) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 148 | — |
| LEXICAL_EN: Rape | REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 138, 152, 154, 196 | — |
| LEXICAL_EN: Residues of starch manufacture and similar residues | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 134 | — |
| LEXICAL_EN: Retsina | REGIONAL_NAME | 1 | 2 | — | — | 1 / 2 / 176 | — |
| LEXICAL_EN: Rice flour | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Rice groats and meal | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Rice in the husk (paddy or rough) | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 135, 137 | — |
| LEXICAL_EN: Rice starch | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Rolled grains of rice | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Royal jelly and propolis | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 147 | — |
| LEXICAL_EN: Rye | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 133 | — |
| LEXICAL_EN: Rye flour | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Saffron | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 140, 152 | — |
| LEXICAL_EN: Sausages | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 145 | — |
| LEXICAL_EN: Sausages and similar products | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Seeds | REGULATORY_CATEGORY | 2 | 24 | — | — | 2 / 24 / 2, 5, 98, 106, 137–138, 152–153, 204 | — |
| LEXICAL_EN: Seeds of anise | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Semi-milled or wholly milled rice | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 135 | — |
| LEXICAL_EN: Separated products | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 162 | — |
| LEXICAL_EN: Sesamum seeds | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Shoulders and cuts thereof | REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 148–149, 154 | — |
| LEXICAL_EN: Sinews or tendons | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 156 | — |
| LEXICAL_EN: Slaughtered poultry | PRODUCT_CATEGORY | 2 | 2 | — | — | 2 / 2 / 162 | — |
| LEXICAL_EN: Small red (Adzuki) beans ( Phaseolus o r Vigna angularis ) | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Soya beans | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 138, 152 | — |
| LEXICAL_EN: Soya-bean oil | PROCESSED_PRODUCT | 2 | 2 | — | — | 2 / 2 / 153, 155 | — |
| LEXICAL_EN: Soya-bean oil and its fractions | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: Spelt | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 133, 137 | — |
| LEXICAL_EN: Spotted grains | QUALITY_DESCRIPTION | 2 | 3 | — | — | 2 / 3 / 158, 164 | — |
| LEXICAL_EN: Stained grains | QUALITY_DESCRIPTION | 2 | 2 | — | — | 2 / 2 / 158, 164 | — |
| LEXICAL_EN: Starches | REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 134, 152 | — |
| LEXICAL_EN: Stuffed pasta | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: Sugar beet | INGREDIENT_TERM, REGULATORY_CATEGORY | 3 | 8 | — | — | 3 / 8 / 64, 116, 136, 153, 159, 202 | — |
| LEXICAL_EN: Sugar cane | INGREDIENT_TERM | 2 | 5 | — | — | 2 / 5 / 64, 136, 153 | — |
| LEXICAL_EN: Sunflower seeds | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 138, 152 | — |
| LEXICAL_EN: Sunflower-seed | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: Swedes | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | — | — | 2 / 3 / 140, 153 | — |
| LEXICAL_EN: Sweetcorn hybrids | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Tea | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Thick skirt | EDIBLE_PART | 2 | 6 | — | — | 2 / 6 / 143–144 | — |
| LEXICAL_EN: Thick skirt and thin skirt | REGULATORY_CATEGORY | 4 | 6 | — | — | 4 / 6 / 143–144 | — |
| LEXICAL_EN: Thyme | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 140, 152 | — |
| LEXICAL_EN: Tokaji eszencia | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 176 | — |
| LEXICAL_EN: Tokaji fordítás | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 200 | — |
| LEXICAL_EN: Tokaji máslás | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 200 | — |
| LEXICAL_EN: Tokajská esencia | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 176 | — |
| LEXICAL_EN: Tokajský forditáš | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 200 | — |
| LEXICAL_EN: Tokajský mášláš | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 200 | — |
| LEXICAL_EN: Tomatoes | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 139, 141 | — |
| LEXICAL_EN: Tomatoes prepared or preserved otherwise than by vinegar o r acetic acid | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: True hemp ( Cannabis sativa L.) raw or processed but | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: Undenatured ethyl alcohol of an alcoholic strengt h by volume of 80 % vol. or higher | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 147 | — |
| LEXICAL_EN: Vanilla | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: Veal | INGREDIENT_TERM | 2 | 22 | — | — | 2 / 22 / 3, 5, 7–9, 13–14, 36, 99, 102, 109–110, 121–122, 143, 162, 172–173 | — |
| LEXICAL_EN: Vegetable materials and vegetable waste | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 135, 155 | — |
| LEXICAL_EN: Vegetable saps and extracts of hops | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 138 | — |
| LEXICAL_EN: Vegetables | REGULATORY_CATEGORY | 6 | 67 | — | — | 6 / 67 / 2, 5, 16–17, 19–20, 33, 35, 75, 82, 85, 99, 101–102, 109–110, 121, 128, 130, 134, 137, 139–141, 151–152, 155 | — |
| LEXICAL_EN: Vegetables (uncooked or cooked by steaming or boili ng in water) frozen | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: Vegetables (uncooked or cooked by steaming or boiling i n water) | REGULATORY_CATEGORY | 2 | 2 | — | — | 2 / 2 / 140, 151 | — |
| LEXICAL_EN: Vegetables provisionally preserved (for example | REGULATORY_CATEGORY | 1 | 2 | — | — | 1 / 2 / 140, 151 | — |
| LEXICAL_EN: Vigna angularis | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Vigna mungo | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Vigna radiata | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Vigna subterranea | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Vigna unguiculata | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: Vigna unguiculta | SOURCE_TAXON_NAME | 1 | 1 | — | — | 1 / 1 / 137 | — |
| LEXICAL_EN: Vinho Verde | REGIONAL_NAME | 1 | 1 | — | — | 1 / 1 / 191 | — |
| LEXICAL_EN: Virgin olive oils | PRODUCT_CATEGORY | 4 | 5 | — | — | 4 / 5 / 188–189 | — |
| LEXICAL_EN: Vitis vinifera | SOURCE_TAXON_NAME | 2 | 6 | — | — | 2 / 6 / 39, 47 | — |
| LEXICAL_EN: Voandzeia subterranea | SOURCE_TAXON_NAME | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: Wheat gluten | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Wheat or meslin flour | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: Wheat starch | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: Whole grains | QUALITY_DESCRIPTION | 2 | 3 | — | — | 2 / 3 / 157–158, 164 | — |
| LEXICAL_EN: Whole products | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 162 | — |
| LEXICAL_EN: Wine of fresh grapes | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 142 | — |
| LEXICAL_EN: Yellow grains | QUALITY_DESCRIPTION | 2 | 3 | — | — | 2 / 3 / 158–159, 164 | — |
| LEXICAL_EN: acetic acid | COMPOSITION_TERM | 3 | 13 | — | — | 3 / 13 / 138, 141, 155–156, 161, 181 | — |
| LEXICAL_EN: added fat | INGREDIENT_TERM | 1 | 3 | — | — | 1 / 3 / 17–18, 22 | — |
| LEXICAL_EN: added salt | INGREDIENT_TERM | 1 | 3 | — | — | 1 / 3 / 17–18, 22 | — |
| LEXICAL_EN: added sugar | INGREDIENT_TERM | 3 | 16 | — | — | 3 / 16 / 18, 22, 140–141, 144, 146, 150, 162 | — |
| LEXICAL_EN: alfalfa | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 153 | — |
| LEXICAL_EN: algae | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: anhydrous milk fat | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 182 | — |
| LEXICAL_EN: anise | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: arrowroot | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: artificial flavour enhancers | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 18 | — |
| LEXICAL_EN: artificial honey | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: babassu oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: badian | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: bay leaves | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: beef and veal | REGULATORY_CATEGORY | 2 | 19 | — | — | 2 / 19 / 3, 5, 7–9, 13–14, 36, 99, 102, 109–110, 121–122, 143, 162 | — |
| LEXICAL_EN: birdsfoot | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 136, 153 | — |
| LEXICAL_EN: bladders | EDIBLE_PART | 1 | 1 | — | — | 1 / 1 / 150 | — |
| LEXICAL_EN: blood | INGREDIENT_TERM | 2 | 6 | — | — | 2 / 6 / 145, 154 | — |
| LEXICAL_EN: brown rice | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 157 | — |
| LEXICAL_EN: canary seed | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: capsicin | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: capsicum oleoresin | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: caramel | INGREDIENT_TERM | 2 | 3 | — | — | 2 / 3 / 134, 136 | — |
| LEXICAL_EN: caraway | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: cardamoms | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: carob | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: cashew nuts | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: cauliflowers | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: celeriac | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: cherries | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: chickling pea | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 136, 153 | — |
| LEXICAL_EN: chicory | INGREDIENT_TERM | 2 | 4 | — | — | 2 / 4 / 139, 153, 156 | — |
| LEXICAL_EN: cider | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: cinnamon-tree flowers | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: clover | INGREDIENT_TERM | 1 | 5 | — | — | 1 / 5 / 136, 153 | — |
| LEXICAL_EN: coriander | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: corn | INGREDIENT_TERM | 1 | 6 | — | — | 1 / 6 / 133–134, 137, 155 | — |
| LEXICAL_EN: cotton-seed oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: cow's milk | COMPOSITION_TERM | 1 | 4 | — | — | 1 / 4 / 7–8, 10 | — |
| LEXICAL_EN: crustaceans | REGULATORY_SCOPE_TERM | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: cumin | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: curcuma | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: curd | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 17, 144 | — |
| LEXICAL_EN: curdled milk | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 144 | — |
| LEXICAL_EN: curry | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: de-alcoholised wine | PROCESSED_PRODUCT | 2 | 4 | — | — | 2 / 4 / 43, 49, 143 | — |
| LEXICAL_EN: edible parts of plants | INGREDIENT_TERM | 3 | 7 | — | — | 3 / 7 / 141, 155 | — |
| LEXICAL_EN: egg yolks | EDIBLE_PART | 2 | 4 | — | — | 2 / 4 / 146, 150, 162 | — |
| LEXICAL_EN: eggs | INGREDIENT_TERM | 2 | 39 | — | — | 2 / 39 / 3, 33, 37, 99, 102, 109, 122, 146–147, 150, 162, 186–187, 201 | — |
| LEXICAL_EN: essential oils | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: ethanol | COMPOSITION_TERM | 1 | 2 | — | — | 1 / 2 / 199 | — |
| LEXICAL_EN: fennel | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: fish | REGULATORY_SCOPE_TERM | 3 | 5 | — | — | 3 / 5 / 150, 153, 155 | — |
| LEXICAL_EN: for sowing | REGULATORY_CATEGORY | 9 | 46 | — | — | 9 / 46 / 106, 133, 135, 137–138, 151–153 | — |
| LEXICAL_EN: fresh | PRODUCT_CATEGORY | 4 | 96 | — | — | 4 / 96 / 7, 10, 17, 35, 133, 138–140, 142–143, 145–146, 148–153, 156, 160–162, 171, 175, 177–179, 185–186, 196–201, 203 | — |
| LEXICAL_EN: fresh poultrymeat | PRODUCT_CATEGORY | 3 | 5 | — | — | 3 / 5 / 185–186 | — |
| LEXICAL_EN: fresh poultrymeat preparation | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 186 | — |
| LEXICAL_EN: frozen poultrymeat | PRODUCT_CATEGORY | 1 | 2 | — | — | 1 / 2 / 185 | — |
| LEXICAL_EN: fructose | COMPOSITION_TERM | 2 | 7 | — | — | 2 / 7 / 134, 159 | — |
| LEXICAL_EN: fruit jellies | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: fruit juices | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: fruit or nut purée | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: fruit stones | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: galactose | COMPOSITION_TERM | 1 | 1 | — | — | 1 / 1 / 184 | — |
| LEXICAL_EN: garbanzos | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: garlic | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: germ of cereals | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: gherkins | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: glucose | COMPOSITION_TERM | 2 | 24 | — | — | 2 / 24 / 134–136, 144, 155–156, 159, 184 | — |
| LEXICAL_EN: glucose syrup | COMPOSITION_TERM | 3 | 9 | — | — | 3 / 9 / 134–135, 144, 155–156 | — |
| LEXICAL_EN: greaves | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 155 | — |
| LEXICAL_EN: half-carcass | EDIBLE_PART | 1 | 2 | — | — | 1 / 2 / 166, 168 | — |
| LEXICAL_EN: halvarine | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 188 | — |
| LEXICAL_EN: hemp | INGREDIENT_TERM | 2 | 14 | — | — | 2 / 14 / 2, 5, 98, 100, 106, 139, 152 | — |
| LEXICAL_EN: hindquarter | EDIBLE_PART | 1 | 1 | — | — | 1 / 1 / 168 | — |
| LEXICAL_EN: honey | INGREDIENT_TERM | 2 | 10 | — | — | 2 / 10 / 134, 136, 147, 153, 163 | — |
| LEXICAL_EN: honey lotus | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 136, 153 | — |
| LEXICAL_EN: hop shoots | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 141 | — |
| LEXICAL_EN: horse beans | RAW_MATERIAL | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: horse-chestnuts | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 135, 155 | — |
| LEXICAL_EN: invert sugar | INGREDIENT_TERM | 1 | 4 | — | — | 1 / 4 / 134, 164–165 | — |
| LEXICAL_EN: jojoba oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: juniper berries | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: kale | INGREDIENT_TERM | 1 | 4 | — | — | 1 / 4 / 136, 139, 153 | — |
| LEXICAL_EN: kernels | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: kohlrabi | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: lactose | COMPOSITION_TERM | 3 | 10 | — | — | 3 / 10 / 17, 134, 136, 144, 184 | — |
| LEXICAL_EN: lactose syrup | COMPOSITION_TERM | 1 | 2 | — | — | 1 / 2 / 144 | — |
| LEXICAL_EN: lactose-free | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 17 | — |
| LEXICAL_EN: lard | PROCESSED_PRODUCT | 2 | 3 | — | — | 2 / 3 / 145, 153 | — |
| LEXICAL_EN: lard oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: leeks | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: liver | EDIBLE_PART | 1 | 5 | — | — | 1 / 5 / 145–146, 153, 169 | — |
| LEXICAL_EN: lupines | INGREDIENT_TERM | 1 | 3 | — | — | 1 / 3 / 153 | — |
| LEXICAL_EN: lupins | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 136 | — |
| LEXICAL_EN: lupulin | INGREDIENT_TERM | 1 | 4 | — | — | 1 / 4 / 36, 107, 138, 159 | — |
| LEXICAL_EN: mace | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: maltodextrine syrup | PROCESSED_PRODUCT | 2 | 8 | — | — | 2 / 8 / 134–135, 144, 155–156 | — |
| LEXICAL_EN: maltose | COMPOSITION_TERM | 1 | 1 | — | — | 1 / 1 / 134 | — |
| LEXICAL_EN: mangoes | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: mangolds | PROCESSED_PRODUCT | 2 | 2 | — | — | 2 / 2 / 153 | — |
| LEXICAL_EN: mangos | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: mangosteens | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 139, 152 | — |
| LEXICAL_EN: maple syrup | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 136 | — |
| LEXICAL_EN: marine mammals | REGULATORY_SCOPE_TERM | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: marmalades | INGREDIENT_TERM | 1 | 3 | — | — | 1 / 3 / 141–142 | — |
| LEXICAL_EN: mead | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: meat | INGREDIENT_TERM, PRODUCT_CATEGORY | 3 | 99 | — | — | 3 / 99 / 6–7, 10, 13, 16, 37, 143–150, 154–156, 168, 171–175, 186 | — |
| LEXICAL_EN: melissa | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: meslin | INGREDIENT_TERM | 1 | 4 | — | — | 1 / 4 / 133 | — |
| LEXICAL_EN: meslin flour | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: mild oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 201 | — |
| LEXICAL_EN: milk | INGREDIENT_TERM, PRODUCT_CATEGORY | 6 | 251 | — | — | 6 / 251 / 3, 5–8, 10, 14, 16–17, 19–20, 36–37, 45, 67–71, 77–78, 82, 84, 88, 91, 96–97, 99, 102, 109, 121–122, 128, 130, 135, 144, 155–156, 162, 170, 182–185, 187–188, 194–195, 208 | — |
| LEXICAL_EN: milk constituents | INGREDIENT_TERM | 2 | 3 | — | — | 2 / 3 / 144, 182, 194 | — |
| LEXICAL_EN: milk fat | INGREDIENT_TERM | 1 | 4 | — | — | 1 / 4 / 162, 182–183 | — |
| LEXICAL_EN: milk products | PRODUCT_CATEGORY | 10 | 53 | — | — | 10 / 53 / 3, 5, 16–17, 19, 36–37, 45, 67, 69, 71, 77–78, 82, 84, 91, 96, 99, 102, 109, 121–122, 135, 144, 155–156, 162, 170, 182–183, 194 | — |
| LEXICAL_EN: milk proteins | COMPOSITION_TERM | 1 | 1 | — | — | 1 / 1 / 184 | — |
| LEXICAL_EN: millet | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: minarine | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 188 | — |
| LEXICAL_EN: mineral salts | COMPOSITION_TERM | 1 | 1 | — | — | 1 / 1 / 184 | — |
| LEXICAL_EN: mint | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: molluscs | REGULATORY_SCOPE_TERM | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: nectarines | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: neutral alcohol of vinous origin | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 160 | — |
| LEXICAL_EN: new wine | INGREDIENT_TERM | 5 | 10 | — | — | 5 / 10 / 176, 179, 196–198 | — |
| LEXICAL_EN: non-standardised whole milk | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 184 | — |
| LEXICAL_EN: oats fed goose | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 201 | — |
| LEXICAL_EN: offal | EDIBLE_PART | 3 | 46 | — | — | 3 / 46 / 143–147, 149–150, 154–155, 162, 171, 173, 185 | — |
| LEXICAL_EN: oleic acid | COMPOSITION_TERM | 2 | 7 | — | — | 2 / 7 / 189 | — |
| LEXICAL_EN: oleo-oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: oleostearin | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: olive oil | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 5 | 48 | — | — | 5 / 48 / 2, 5–6, 10, 33, 37, 82–83, 98, 109, 112, 138–139, 154–155, 188–189, 201 | — |
| LEXICAL_EN: or milk products | REGULATORY_CATEGORY | 4 | 7 | — | — | 4 / 7 / 135, 144, 155–156, 183 | — |
| LEXICAL_EN: oregano | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: palm hearts | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 141 | — |
| LEXICAL_EN: palm kernel | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: papaws | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: papayas | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: peaches | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: peanut butter | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: pears | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: pectinates | COMPOSITION_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: perry | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 155 | — |
| LEXICAL_EN: pig fat (including lard) | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 145 | — |
| LEXICAL_EN: pigmeat | REGULATORY_CATEGORY | 3 | 13 | — | — | 3 / 13 / 3, 6–7, 10, 36, 99, 102, 109–110, 122, 145 | — |
| LEXICAL_EN: plums | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: pollen | PROCESSED_PRODUCT | 2 | 2 | — | — | 2 / 2 / 147, 163 | — |
| LEXICAL_EN: poultry cuts | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 162 | — |
| LEXICAL_EN: poultry eggs, in shell | REGULATORY_CATEGORY | 1 | 1 | — | — | 1 / 1 / 146 | — |
| LEXICAL_EN: poultry liver | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 146 | — |
| LEXICAL_EN: poultrymeat | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 6 | 35 | — | — | 6 / 35 / 3, 33, 36, 99, 109–110, 122, 146, 162, 185–186, 201 | — |
| LEXICAL_EN: poultrymeat preparation | PRODUCT_CATEGORY | 2 | 3 | — | — | 2 / 3 / 186 | — |
| LEXICAL_EN: poultrymeat product | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 186 | — |
| LEXICAL_EN: processed fruit and vegetable products | REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 2, 17, 33, 140 | — |
| LEXICAL_EN: propolis | PROCESSED_PRODUCT | 2 | 3 | — | — | 2 / 3 / 147, 163 | — |
| LEXICAL_EN: quick-frozen poultrymeat | PRODUCT_CATEGORY | 1 | 1 | — | — | 1 / 1 / 185 | — |
| LEXICAL_EN: quinces | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: radishes | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: raw milk | INGREDIENT_TERM | 4 | 40 | — | — | 4 / 40 / 67–71, 88, 96, 184–185 | — |
| LEXICAL_EN: raw sugar | COMPOSITION_TERM | 2 | 3 | — | — | 2 / 3 / 5, 165 | — |
| LEXICAL_EN: resinoids | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 151 | — |
| LEXICAL_EN: rosemary | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: round | EDIBLE_PART | 2 | 7 | — | — | 2 / 7 / 157, 166–168 | — |
| LEXICAL_EN: royal jelly | INGREDIENT_TERM | 2 | 3 | — | — | 2 / 3 / 147, 163 | — |
| LEXICAL_EN: safflower | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 154 | — |
| LEXICAL_EN: sage | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: sago | INGREDIENT_TERM | 1 | 3 | — | — | 1 / 3 / 133, 152 | — |
| LEXICAL_EN: sainfoin | INGREDIENT_TERM | 1 | 5 | — | — | 1 / 5 / 136, 153 | — |
| LEXICAL_EN: salad beetroot | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: salep | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 133 | — |
| LEXICAL_EN: salsify | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: salt | COMPOSITION_TERM | 1 | 4 | — | — | 1 / 4 / 17–18, 22, 187 | — |
| LEXICAL_EN: seaweeds | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: semi-skimmed milk | INGREDIENT_TERM | 1 | 3 | — | — | 1 / 3 / 184 | — |
| LEXICAL_EN: shallots | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: sharps | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 134, 155 | — |
| LEXICAL_EN: sheepmeat and goatmeat | REGULATORY_CATEGORY | 3 | 13 | — | — | 3 / 13 / 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 145 | — |
| LEXICAL_EN: silkworms | INGREDIENT_TERM | 2 | 3 | — | — | 2 / 3 / 3, 147 | — |
| LEXICAL_EN: skimmed milk | INGREDIENT_TERM | 1 | 11 | — | — | 1 / 11 / 6, 8, 10, 14, 184 | — |
| LEXICAL_EN: skimmed milk powder | PROCESSED_PRODUCT | 2 | 6 | — | — | 2 / 6 / 6, 8, 10, 14 | — |
| LEXICAL_EN: skimmed-milk | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 184 | — |
| LEXICAL_EN: sloes | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: spirit | INGREDIENT_TERM | 1 | 7 | — | — | 1 / 7 / 24, 49–50, 141–142 | — |
| LEXICAL_EN: spreadable fats | REGULATORY_CATEGORY | 2 | 6 | — | — | 2 / 6 / 33, 37, 40, 45, 187, 194 | — |
| LEXICAL_EN: standardised whole milk | PROCESSED_PRODUCT | 1 | 2 | — | — | 1 / 2 / 184 | — |
| LEXICAL_EN: stomachs | EDIBLE_PART | 1 | 1 | — | — | 1 / 1 / 150 | — |
| LEXICAL_EN: sucrose | INGREDIENT_TERM | 2 | 11 | — | — | 2 / 11 / 136, 159, 180–181, 196, 198 | — |
| LEXICAL_EN: sulphur dioxide | INGREDIENT_TERM | 3 | 6 | — | — | 3 / 6 / 138, 140, 151, 180–181 | — |
| LEXICAL_EN: sweet potatoes | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 4 | — | — | 2 / 4 / 133, 139, 141 | — |
| LEXICAL_EN: sweeteners | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 18 | — |
| LEXICAL_EN: table grapes | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 139, 142 | — |
| LEXICAL_EN: tallow oil | PROCESSED_PRODUCT | 1 | 1 | — | — | 1 / 1 / 153 | — |
| LEXICAL_EN: tartaric acid | COMPOSITION_TERM | 1 | 3 | — | — | 1 / 3 / 176, 198 | — |
| LEXICAL_EN: thin skirt | EDIBLE_PART | 2 | 6 | — | — | 2 / 6 / 143–144 | — |
| LEXICAL_EN: truffles | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: turmeric | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 152 | — |
| LEXICAL_EN: turnips | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 139 | — |
| LEXICAL_EN: vegetable | ORIGIN_QUALIFIER | 3 | 35 | — | — | 3 / 35 / 2, 17, 33, 135, 138–141, 153–155, 188, 194–195 | — |
| LEXICAL_EN: vegetable juices | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 141 | — |
| LEXICAL_EN: vetches | INGREDIENT_TERM | 1 | 5 | — | — | 1 / 5 / 136, 153 | — |
| LEXICAL_EN: vine leaves | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 141 | — |
| LEXICAL_EN: vinegar | INGREDIENT_TERM | 4 | 19 | — | — | 4 / 19 / 38, 40, 138, 141–142, 155–156, 181 | — |
| LEXICAL_EN: vitamins | COMPOSITION_TERM | 2 | 2 | — | — | 2 / 2 / 184 | — |
| LEXICAL_EN: watermelons | INGREDIENT_TERM | 2 | 2 | — | — | 2 / 2 / 139–140 | — |
| LEXICAL_EN: white pea beans | RAW_MATERIAL | 1 | 2 | — | — | 1 / 2 / 137, 151 | — |
| LEXICAL_EN: white sugar | COMPOSITION_TERM | 2 | 6 | — | — | 2 / 6 / 5, 10, 65, 164–165 | — |
| LEXICAL_EN: whole milk | PROCESSED_PRODUCT | 1 | 6 | — | — | 1 / 6 / 184 | — |
| LEXICAL_EN: wild marjoram | INGREDIENT_TERM | 1 | 1 | — | — | 1 / 1 / 140 | — |
| LEXICAL_EN: yams | INGREDIENT_TERM | 1 | 2 | — | — | 1 / 2 / 141 | — |
| LEXICAL_FR: Abats comestibles de l'espèce porcine domestique | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 145 | — | — | — |
| LEXICAL_FR: Abats comestibles des animaux de l'espèce bovine | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 143–144 | — | — | — |
| LEXICAL_FR: Abats comestibles des animaux des espèces bovine | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 149 | — | — | — |
| LEXICAL_FR: Abats comestibles des animaux des espèces ovine et c aprine | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 145–146 | — | — | — |
| LEXICAL_FR: Abricots | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Agglomérés sous forme de pellets de riz | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 133, 135 | — | — | — |
| LEXICAL_FR: Agrumes | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 139–140 | — | — | — |
| LEXICAL_FR: Alcool éthylique et eaux-de-vie dénaturés de tous titres | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 147 | — | — | — |
| LEXICAL_FR: Alcool éthylique non dénaturé d'un titre alcoomét rique volumique de 80 % vol. ou plus | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 147 | — | — | — |
| LEXICAL_FR: Aliments pour chiens ou chats | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 135, 144, 155 | — | — | — |
| LEXICAL_FR: Amidon de froment (blé) Code NC | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Amidon de maïs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Amidon de riz | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Amidons et fécules | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 134, 152 | — | — | — |
| LEXICAL_FR: Ananas | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: Animaux vivants de l'espèce bovine | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 143, 148, 162 | — | — | — |
| LEXICAL_FR: Animaux vivants de l'espèce caprine | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 145 | — | — | — |
| LEXICAL_FR: Animaux vivants de l'espèce ovine | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 145 | — | — | — |
| LEXICAL_FR: Animaux vivants de l'espèce porcine | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 145, 148 | — | — | — |
| LEXICAL_FR: Arachides | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 138, 152 | — | — | — |
| LEXICAL_FR: Arachides non grillées ni autrement cuites | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 138, 152 | — | — | — |
| LEXICAL_FR: Autres amidons et fécules | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Autres fruits | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Autres fruits à coques | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: Autres graines et fruits oléagineux | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 138, 153 | — | — | — |
| LEXICAL_FR: Autres huiles et leurs fractions | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 138 | — | — | — |
| LEXICAL_FR: Autres légumes | REGULATORY_CATEGORY | 2 | 5 | 2 / 5 / 139, 151 | — | — | — |
| LEXICAL_FR: Autres légumes préparés ou conservés autrement qu'au vi naigre ou à l'acide acétique | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 141, 155 | — | — | — |
| LEXICAL_FR: Autres légumes à cosse secs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 137 | — | — | — |
| LEXICAL_FR: Autres préparations et conserves contenant de la via nde ou des abats de l'espèce bovine | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 143–144 | — | — | — |
| LEXICAL_FR: Autres préparations et conserves contenant de la via nde ou des abats de l'espèce porcine | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 145 | — | — | — |
| LEXICAL_FR: Autres préparations et conserves de viande ou d'abat s de l'espèce bovine non cuits | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 143 | — | — | — |
| LEXICAL_FR: Autres préparations et conserves de viandes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Autres résidus provenant du traitement des corps gra s ou des cires animales ou végétales | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Autres sucres | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 134 | — | — | — |
| LEXICAL_FR: Autres viandes et abats comestibles | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 149 | — | — | — |
| LEXICAL_FR: Avocats | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: Avoine | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 133, 201 | — | — | — |
| LEXICAL_FR: Bananes confites au sucre | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 141–142 | — | — | — |
| LEXICAL_FR: Bananes conservées provisoirement | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 140, 142 | — | — | — |
| LEXICAL_FR: Bananes fraîches | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 142 | — | — | — |
| LEXICAL_FR: Bananes sèches | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 142 | — | — | — |
| LEXICAL_FR: Basilic | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Betteraves fourragères | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 153 | — | — | — |
| LEXICAL_FR: Betteraves à sucre | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 116, 153, 159 | — | — | — |
| LEXICAL_FR: Beurre d'arachide | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: Boissons à base de lait contenant du cacao, du jus de fruits ou aromatisées naturellement | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 170 | — | — | — |
| LEXICAL_FR: Boyaux | EDIBLE_PART | 1 | 1 | 1 / 1 / 150 | — | — | — |
| LEXICAL_FR: Cacao en fèves | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Cacao en fèves et brisures de fèves | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Café | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 156 | — | — | — |
| LEXICAL_FR: Cajanus cajan | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Cannabis sativa | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Cannelle | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Cannelle et fleurs de cannelier | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Cannes à sucre | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 5 | 2 / 5 / 134, 136, 153 | — | — | — |
| LEXICAL_FR: Capsicum annuum | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Carottes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Caroubes | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 119, 140, 153 | — | — | — |
| LEXICAL_FR: Champignons et truffes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: Chanvre | INGREDIENT_TERM | 4 | 14 | 4 / 14 / 2, 5, 98, 100, 106, 139, 152 | — | — | — |
| LEXICAL_FR: Chanvre ( Cannabis sativa L. ) brut ou travaillé mais non filé | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Chevaux | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 148, 156 | — | — | — |
| LEXICAL_FR: Chevaux vivants destinés à la boucherie ( α ) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 156 | — | — | — |
| LEXICAL_FR: Choux | INGREDIENT_TERM | 2 | 7 | 2 / 7 / 136, 139, 153 | — | — | — |
| LEXICAL_FR: Cichorium intybus | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Cire d'abeille | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 147 | — | — | — |
| LEXICAL_FR: Clinton | VARIETY_NAME | 1 | 1 | 1 / 1 / 39 | — | — | — |
| LEXICAL_FR: Concombres | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Concombres et cornichons | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Confitures | INGREDIENT_TERM | 2 | 3 | 2 / 3 / 141–142 | — | — | — |
| LEXICAL_FR: Coprah | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 152, 154 | — | — | — |
| LEXICAL_FR: Coques | INGREDIENT_TERM, REGULATORY_CATEGORY | 3 | 11 | 3 / 11 / 139–140, 152, 154, 156 | — | — | — |
| LEXICAL_FR: Cônes de houblon frais ou secs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 138 | — | — | — |
| LEXICAL_FR: Dattes | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 152 | — | — | — |
| LEXICAL_FR: Destinés à la fabrication de produits pharmace utiques ( γ ) | REGULATORY_CATEGORY | 3 | 8 | 3 / 8 / 149 | — | — | — |
| LEXICAL_FR: Doliques à œil noir | PROCESSED_PRODUCT | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Doliques à œil noir (Pois du Brésil | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Drêches | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 134 | — | — | — |
| LEXICAL_FR: Drêches et déchets de brasserie ou de distillerie | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 134 | — | — | — |
| LEXICAL_FR: Extraits et jus de viande | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Farine | REGULATORY_CATEGORY | 2 | 10 | 2 / 10 / 133, 135–136, 153, 156 | — | — | — |
| LEXICAL_FR: Farine d'avoine | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Farine d'orge | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Farine de maïs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Farine de riz | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Farine de seigle | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Farine et pellets de luzerne | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 136, 153 | — | — | — |
| LEXICAL_FR: Farines | REGULATORY_CATEGORY | 2 | 13 | 2 / 13 / 133, 141–143, 149–150, 152–153, 155 | — | — | — |
| LEXICAL_FR: Farines de céréales | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Farines de graines ou de fruits oléagineux | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Farines et poudres | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 143, 149–150 | — | — | — |
| LEXICAL_FR: Figues | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 139–140, 152 | — | — | — |
| LEXICAL_FR: Figues séchées | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Foies | REGULATORY_CATEGORY | 3 | 10 | 3 / 10 / 145–147, 149–150, 153 | — | — | — |
| LEXICAL_FR: Foies de volailles | INGREDIENT_TERM | 2 | 3 | 2 / 3 / 146, 150 | — | — | — |
| LEXICAL_FR: Froment (blé) tendre et méteil | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 133 | — | — | — |
| LEXICAL_FR: Fruits | REGULATORY_CATEGORY | 5 | 88 | 5 / 88 / 2, 5, 16–17, 19–20, 33, 35, 75, 82, 85, 99, 101–102, 109–110, 119, 121, 128, 130, 135, 138–141, 144, 152–153, 155, 170, 176 | — | — | — |
| LEXICAL_FR: Fruits conservés provisoirement (au moyen de gaz sulfur eux ou dans l'eau salée | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Fruits du genre Capsicum | REGULATORY_CATEGORY | 4 | 7 | 4 / 7 / 140–141, 152, 155 | — | — | — |
| LEXICAL_FR: Fruits et autres parties comestibles de plantes | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 141, 155 | — | — | — |
| LEXICAL_FR: Fruits séchés | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 140 | — | — | — |
| LEXICAL_FR: Fèves | RAW_MATERIAL | 2 | 6 | 2 / 6 / 137–138, 151–152, 154 | — | — | — |
| LEXICAL_FR: Fèves (Vicia faba var. major) | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Fèves (Vicia faba var. major) et févéroles ( Vicia faba var. equina e t Vicia faba var. | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: Fèves de soja | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 138, 152 | — | — | — |
| LEXICAL_FR: Fécule de manioc (cassave) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Fécule de pomme de terre | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Gelée royale et propolis | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 147 | — | — | — |
| LEXICAL_FR: Gingembre | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Girofles | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Girofles (antofles | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Glands de chêne | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 135, 155 | — | — | — |
| LEXICAL_FR: Glands de chêne et marrons d'Inde | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 135, 155 | — | — | — |
| LEXICAL_FR: Glucose et sirop de glucose | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 134 | — | — | — |
| LEXICAL_FR: Glucose et sirop de glucose contenant en poids à l 'état sec de 20 % inclus à 50 % exclus de | LAYOUT_FRAGMENT | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Gluten de froment | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Gluten de froment [blé] | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Goyaves | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: Graines | REGULATORY_CATEGORY | 2 | 21 | 2 / 21 / 106, 138, 147, 152–153 | — | — | — |
| LEXICAL_FR: Graines d'anis | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Graines d'œillette | RAW_MATERIAL | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Graines d'œillette ou de pavot | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Graines de chanvre | RAW_MATERIAL | 2 | 2 | 2 / 2 / 106, 152 | — | — | — |
| LEXICAL_FR: Graines de coton | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Graines de lin | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 138, 152 | — | — | — |
| LEXICAL_FR: Graines de moutarde | RAW_MATERIAL | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Graines de sésame | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Graines de tournesol | RAW_MATERIAL | 1 | 2 | 1 / 2 / 138, 152 | — | — | — |
| LEXICAL_FR: Grains ambrés | QUALITY_DESCRIPTION | 2 | 2 | 2 / 2 / 159, 164 | — | — | — |
| LEXICAL_FR: Grains brisés ou brisures | QUALITY_DESCRIPTION | 1 | 1 | 1 / 1 / 158 | — | — | — |
| LEXICAL_FR: Grains crayeux | QUALITY_DESCRIPTION | 2 | 3 | 2 / 3 / 158, 164 | — | — | — |
| LEXICAL_FR: Grains de céréales autrement travaillés (mondés | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Grains de riz aplatis | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Grains de riz ou flocons | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Grains entiers | QUALITY_DESCRIPTION | 3 | 3 | 3 / 3 / 157–158, 164 | — | — | — |
| LEXICAL_FR: Grains jaunes | QUALITY_DESCRIPTION | 2 | 3 | 2 / 3 / 158–159, 164 | — | — | — |
| LEXICAL_FR: Grains présentant des difformités naturelles | QUALITY_DESCRIPTION | 1 | 1 | 1 / 1 / 158 | — | — | — |
| LEXICAL_FR: Grains striés de rouge | QUALITY_DESCRIPTION | 2 | 2 | 2 / 2 / 158, 164 | — | — | — |
| LEXICAL_FR: Grains tachetés | QUALITY_DESCRIPTION | 2 | 3 | 2 / 3 / 158, 164 | — | — | — |
| LEXICAL_FR: Grains tachés | QUALITY_DESCRIPTION | 2 | 2 | 2 / 2 / 158, 164 | — | — | — |
| LEXICAL_FR: Grains verts | QUALITY_DESCRIPTION | 1 | 1 | 1 / 1 / 158 | — | — | — |
| LEXICAL_FR: Grains épointés | QUALITY_DESCRIPTION | 2 | 2 | 2 / 2 / 158, 164 | — | — | — |
| LEXICAL_FR: Graisse de volailles | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 146 | — | — | — |
| LEXICAL_FR: Graisse des animaux des espèces ovine et caprine | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 146 | — | — | — |
| LEXICAL_FR: Graisses de volaille | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 146 | — | — | — |
| LEXICAL_FR: Graisses des animaux de l'espèce bovine | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 144 | — | — | — |
| LEXICAL_FR: Graisses des animaux des espèces bovine | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Graisses et huiles animales ou végétales et leurs fract ions | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Graisses et huiles et leurs fractions | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 153 | — | — | — |
| LEXICAL_FR: Gruaux | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 133, 135 | — | — | — |
| LEXICAL_FR: Gruaux et semoules de froment (blé) | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 133 | — | — | — |
| LEXICAL_FR: Gruaux et semoules de riz | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Haricots Adzuki | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Haricots communs ( Phaseolus vulgaris ) | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Haricots des espèces Vigna mungo (L) Hepper ou Vigna radiata (L) Wilczek | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: Haricots des espèces Vigna mungo (L.) Hepper ou V igna radiata (L.) Wilczek | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 137 | — | — | — |
| LEXICAL_FR: Haricots «petits rouges» (haricots Adzuki) ( Phaseolus o u Vigna angularis ) | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Herbemont | VARIETY_NAME | 1 | 1 | 1 / 1 / 39 | — | — | — |
| LEXICAL_FR: Huile d'arachide | PROCESSED_PRODUCT | 2 | 2 | 2 / 2 / 153, 155 | — | — | — |
| LEXICAL_FR: Huile d'arachide et ses fractions | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Huile de palme | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Huile de palme et ses fractions | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Huile de soja | PROCESSED_PRODUCT | 2 | 2 | 2 / 2 / 153, 155 | — | — | — |
| LEXICAL_FR: Huile de soja et ses fractions | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Huiles de coco (huile de coprah) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Huiles de navette | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Huiles de tournesol | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Humulus lupulus | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 159 | — | — | — |
| LEXICAL_FR: Isabelle | VARIETY_NAME | 1 | 1 | 1 / 1 / 39 | — | — | — |
| LEXICAL_FR: Jacquez | VARIETY_NAME | 1 | 1 | 1 / 1 / 39 | — | — | — |
| LEXICAL_FR: Jambons | REGULATORY_CATEGORY | 2 | 5 | 2 / 5 / 148–149, 154 | — | — | — |
| LEXICAL_FR: Jambons et morceaux de jambons | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Jus de fruits ou de légumes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: Lactuca sativa | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Laitues | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Laitues (Lactuca sativa) et chicorées (Cichorium spp.) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Lard sans parties maigres et graisse de porc non fondue ou extraite d'une autre manière | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Lentilles | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Lies de vin | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 143, 155, 200 | — | — | — |
| LEXICAL_FR: Lies ou fèces d'huiles | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: Lin | INGREDIENT_TERM | 2 | 10 | 2 / 10 / 2, 5, 10, 98, 138–139, 152 | — | — | — |
| LEXICAL_FR: Lin brut ou travaillé mais non filé | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Liège naturel brut ou simplement préparé | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 156 | — | — | — |
| LEXICAL_FR: Luzerne | INGREDIENT_TERM | 2 | 12 | 2 / 12 / 136, 153, 156 | — | — | — |
| LEXICAL_FR: Légumes | REGULATORY_CATEGORY | 2 | 71 | 2 / 71 / 2, 5, 16–17, 19–20, 33, 35, 75, 82, 85, 99, 101–102, 109–110, 121, 128, 130, 137, 139–141, 151–152, 155 | — | — | — |
| LEXICAL_FR: Légumes conservés provisoirement (au moyen de gaz sulfu reux ou dans de l'eau salée | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 140, 151 | — | — | — |
| LEXICAL_FR: Légumes secs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Légumes à cosse | REGULATORY_CATEGORY | 2 | 5 | 2 / 5 / 137, 139, 151–152 | — | — | — |
| LEXICAL_FR: Légumes à cosse secs | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 137, 151–152 | — | — | — |
| LEXICAL_FR: Malt | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Maltodextrine | PROCESSED_PRODUCT | 3 | 17 | 3 / 17 / 134–136, 144, 155–156 | — | — | — |
| LEXICAL_FR: Maltodextrine et sirop de maltodextrine | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Matières pectines | COMPOSITION_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Matières pectines et pectinates | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Matières végétales et déchets végétaux | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 135, 155 | — | — | — |
| LEXICAL_FR: Maïs | RAW_MATERIAL, REGULATORY_CATEGORY | 2 | 15 | 2 / 15 / 7–8, 12, 104, 133–134, 137, 141, 155 | — | — | — |
| LEXICAL_FR: Maïs autre que de semence | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Maïs doux hybrides | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 137 | — | — | — |
| LEXICAL_FR: Maïs hybride de semence | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 137 | — | — | — |
| LEXICAL_FR: Melons | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 139–140 | — | — | — |
| LEXICAL_FR: Melons (y compris les pastèques) et papayes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Miel naturel | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 134, 147 | — | — | — |
| LEXICAL_FR: Mlado vino portugizac | REGIONAL_NAME | 1 | 1 | 1 / 1 / 130 | — | — | — |
| LEXICAL_FR: Moût de raisins frais muté à l'alcool | PRODUCT_CATEGORY | 3 | 3 | 3 / 3 / 160, 199 | — | — | — |
| LEXICAL_FR: Mélanges contenant des bananes séchées | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 142 | — | — | — |
| LEXICAL_FR: Nerfs ou tendons | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 156 | — | — | — |
| LEXICAL_FR: Niébé | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Noah | VARIETY_NAME | 1 | 1 | 1 / 1 / 39 | — | — | — |
| LEXICAL_FR: Noix d'arec | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: Noix de coco | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Noix muscades | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Oignons | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Olives | PROCESSED_PRODUCT | 2 | 31 | 2 / 31 / 2, 5, 10, 33, 37, 82–83, 90, 98, 109, 112, 138–141 | — | — | — |
| LEXICAL_FR: Olives conservées provisoirement (au moyen de gaz sulfu reux ou dans de l'eau salée | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 138 | — | — | — |
| LEXICAL_FR: Olives préparées ou conservées au vinaigre ou à l'ac ide acétique | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 138 | — | — | — |
| LEXICAL_FR: Olives préparées ou conservées autrement qu'au vi naigre ou à l'acide acétique | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 138 | — | — | — |
| LEXICAL_FR: Olives séchées | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 138 | — | — | — |
| LEXICAL_FR: Onglets | INGREDIENT_TERM | 2 | 6 | 2 / 6 / 143–144 | — | — | — |
| LEXICAL_FR: Onglets et hampes | REGULATORY_CATEGORY | 2 | 6 | 2 / 6 / 143–144 | — | — | — |
| LEXICAL_FR: Orge | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 6 | 2 / 6 / 7–8, 12, 133 | — | — | — |
| LEXICAL_FR: Origanum vulgare | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Othello | VARIETY_NAME | 1 | 1 | 1 / 1 / 39 | — | — | — |
| LEXICAL_FR: Pailles et balles de céréales brutes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Phaseolus vulgaris | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pimenta | SOURCE_TAXON_NAME | 2 | 8 | 2 / 8 / 139–140, 151–152 | — | — | — |
| LEXICAL_FR: Piments doux ou poivrons séchés ( Capsicum annuum ) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Piments du genre Capsicum ou du genre Pimenta | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 151 | — | — | — |
| LEXICAL_FR: Piper | SOURCE_TAXON_NAME | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Pisum sativum | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Plantains | INGREDIENT_TERM | 2 | 4 | 2 / 4 / 139, 142 | — | — | — |
| LEXICAL_FR: Plantains frais | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Plantains secs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Plantes | REGULATORY_CATEGORY | 3 | 16 | 3 / 16 / 2, 32–33, 99, 141, 143, 153, 155 | — | — | — |
| LEXICAL_FR: Pois ( Pisum sativum ) | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois Bambara | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois Bambara (Pois de terre) ( Vigna subterranea o u Voandzeia subterranea ) | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois chiches | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois d'Ambrevade | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois d'Ambrevade ou pois d'Angole ( Cajanus cajan ) | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois de terre | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Pois du Brésil | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Poitrines (entrelardées) et leurs morceaux | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 149 | — | — | — |
| LEXICAL_FR: Poivre | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Poivre (du genre Piper ) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Pollen | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 147, 163 | — | — | — |
| LEXICAL_FR: Pommes | REGULATORY_CATEGORY | 2 | 6 | 2 / 6 / 140–141, 156 | — | — | — |
| LEXICAL_FR: Pommes de terre | INGREDIENT_TERM, REGULATORY_CATEGORY | 3 | 5 | 3 / 5 / 140–141, 156 | — | — | — |
| LEXICAL_FR: Produits comestibles d'origine animale | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 150 | — | — | — |
| LEXICAL_FR: Produits d'origine animale | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 150 | — | — | — |
| LEXICAL_FR: Produits laitiers fermentés ou non fermentés contenant des frui ts, aromatisés naturellement ou non aromatisés | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 170 | — | — | — |
| LEXICAL_FR: Produits laitiers fermentés sans addition de jus de fruits, a romatisés naturel­ lement | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 170 | — | — | — |
| LEXICAL_FR: Produits laitiers fermentés sans addition de jus de fruits, a romatisés naturel­ lement ou non aromatisés | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 170 | — | — | — |
| LEXICAL_FR: Préparations alimentaires non dénommées ni comprises aille urs | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: Préparations de sang de tous animaux | REGULATORY_CATEGORY | 3 | 3 | 3 / 3 / 145, 154 | — | — | — |
| LEXICAL_FR: Préparations des types utilisés pour l'alimentation des an imaux | REGULATORY_CATEGORY | 3 | 3 | 3 / 3 / 135, 144, 155 | — | — | — |
| LEXICAL_FR: Préparations et conserves de foies de tous animaux a utres que d'oie ou de canard | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Préparations homogénéisées de bananes | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 141–142 | — | — | — |
| LEXICAL_FR: Préparations homogénéisées de viandes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Pâtes alimentaires farcies | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Pâtes alimentaires farcies (même cuites ou autrement préparées) contenant en poids plus de | LAYOUT_FRAGMENT | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Racines de chicorées | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 156 | — | — | — |
| LEXICAL_FR: Raisins de table | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 139, 142 | — | — | — |
| LEXICAL_FR: Raisins de table frais | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: Raisins secs | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 6 | 2 / 6 / 140, 160, 177 | — | — | — |
| LEXICAL_FR: Reproducteurs de race pure ( α ) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 148 | — | — | — |
| LEXICAL_FR: Reproducteurs de race pure ( β ) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 148 | — | — | — |
| LEXICAL_FR: Riz décortiqué (riz brun) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Riz en paille (riz paddy) | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 135, 137 | — | — | — |
| LEXICAL_FR: Riz semi-blanchi ou blanchi | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 135 | — | — | — |
| LEXICAL_FR: Rutabagas | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 140, 153 | — | — | — |
| LEXICAL_FR: Résidus d'amidonnerie et résidus similaires | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 134 | — | — | — |
| LEXICAL_FR: Safran | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 140, 152 | — | — | — |
| LEXICAL_FR: Sarrasin | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: Saucisses | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 145 | — | — | — |
| LEXICAL_FR: Seigle | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 133 | — | — | — |
| LEXICAL_FR: Sirops de sucre | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 134, 136 | — | — | — |
| LEXICAL_FR: Sons | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 134, 150, 155 | — | — | — |
| LEXICAL_FR: Sorgho à grains hybride | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 137 | — | — | — |
| LEXICAL_FR: Sperme de taureaux | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 150 | — | — | — |
| LEXICAL_FR: Stéarine solaire | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: Sucre et sirop d'érable | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 136 | — | — | — |
| LEXICAL_FR: Sucres et mélasses | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 134, 136 | — | — | — |
| LEXICAL_FR: Sucs et extraits de houblon | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 138 | — | — | — |
| LEXICAL_FR: Tartre brut | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 155 | — | — | — |
| LEXICAL_FR: Thym | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 140, 152 | — | — | — |
| LEXICAL_FR: Thé | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Tokaji eszencia | REGIONAL_NAME | 1 | 1 | 1 / 1 / 176 | — | — | — |
| LEXICAL_FR: Tokaji fordítás | REGIONAL_NAME | 1 | 1 | 1 / 1 / 200 | — | — | — |
| LEXICAL_FR: Tokaji máslás | REGIONAL_NAME | 1 | 1 | 1 / 1 / 200 | — | — | — |
| LEXICAL_FR: Tokajská esencia | REGIONAL_NAME | 1 | 1 | 1 / 1 / 176 | — | — | — |
| LEXICAL_FR: Tokajský forditáš | REGIONAL_NAME | 1 | 1 | 1 / 1 / 200 | — | — | — |
| LEXICAL_FR: Tokajský mášláš | REGIONAL_NAME | 1 | 1 | 1 / 1 / 200 | — | — | — |
| LEXICAL_FR: Tomates | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 139, 141 | — | — | — |
| LEXICAL_FR: Tomates préparées ou conservées autrement qu'au vinaigre o u à l'acide acétique | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: Tourteaux et autres résidus solides | REGULATORY_CATEGORY | 3 | 6 | 3 / 6 / 134, 155 | — | — | — |
| LEXICAL_FR: Vanille | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: Viandes de cheval | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 156 | — | — | — |
| LEXICAL_FR: Viandes des animaux de l'espèce bovine | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 143 | — | — | — |
| LEXICAL_FR: Viandes des animaux de l'espèce porcine | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 145, 148 | — | — | — |
| LEXICAL_FR: Viandes des animaux de l'espèce équine | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 156 | — | — | — |
| LEXICAL_FR: Viandes des animaux des espèces asine ou mulassière | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 148 | — | — | — |
| LEXICAL_FR: Viandes des animaux des espèces ovine et caprine | REGULATORY_CATEGORY | 1 | 3 | 1 / 3 / 145 | — | — | — |
| LEXICAL_FR: Viandes des animaux des espèces ovine et caprine dés ossées | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Viandes des animaux des espèces ovine et caprine non désossées | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Viandes et abats comestibles | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 145–146, 149 | — | — | — |
| LEXICAL_FR: Viandes et abats comestibles de l'espèce porcine domest ique | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: Vigna angularis | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Vigna mungo | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Vigna radiata | SOURCE_TAXON_NAME | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Vigna subterranea | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Vigna unguiculta | SOURCE_TAXON_NAME | 2 | 2 | 2 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: Vinho Verde | REGIONAL_NAME | 1 | 1 | 1 / 1 / 191 | — | — | — |
| LEXICAL_FR: Vitis vinifera | SOURCE_TAXON_NAME | 3 | 3 | 3 / 3 / 39, 47 | — | — | — |
| LEXICAL_FR: Voandzeia subterranea | SOURCE_TAXON_NAME | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: abats | EDIBLE_PART | 4 | 45 | 4 / 45 / 143–147, 149–150, 154–155, 162, 171, 185 | — | — | — |
| LEXICAL_FR: acide acétique | COMPOSITION_TERM | 2 | 13 | 2 / 13 / 138, 141, 155–156, 161, 181 | — | — | — |
| LEXICAL_FR: acide oléique | COMPOSITION_TERM | 1 | 7 | 1 / 7 / 189 | — | — | — |
| LEXICAL_FR: acide tartrique | COMPOSITION_TERM | 2 | 3 | 2 / 3 / 176, 198 | — | — | — |
| LEXICAL_FR: alcool neutre d'origine vinique | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 160 | — | — | — |
| LEXICAL_FR: algues | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: alpiste | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: amandes | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 119, 153 | — | — | — |
| LEXICAL_FR: amomes | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: anhydride sulfureux | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 180–181 | — | — | — |
| LEXICAL_FR: arrow-root | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: aulx | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: autres légumes et mélanges de légumes | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 155 | — | — | — |
| LEXICAL_FR: babassu | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: badiane | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: baies de genièvre | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: bananes | INGREDIENT_TERM | 3 | 25 | 3 / 25 / 2, 33, 99, 140–142, 152 | — | — | — |
| LEXICAL_FR: betteraves à salade | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: blé | INGREDIENT_TERM | 2 | 17 | 2 / 17 / 7–8, 12, 133–134 | — | — | — |
| LEXICAL_FR: bovins | PRODUCT_CATEGORY | 2 | 29 | 2 / 29 / 5, 7–8, 10, 13, 15, 162, 166–167, 171–174 | — | — | — |
| LEXICAL_FR: brugnons | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: caillé | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 17 | — | — | — |
| LEXICAL_FR: capsicine | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: carcasse | EDIBLE_PART | 4 | 15 | 4 / 15 / 16, 166–169 | — | — | — |
| LEXICAL_FR: cardamomes | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: carthame | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: carvi | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: cassave | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: cerises | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: champignons | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: chicorées | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 139, 156 | — | — | — |
| LEXICAL_FR: choux frisés | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: choux-fleurs | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: choux-raves | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: cidre | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 155 | — | — | — |
| LEXICAL_FR: clous | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: coings | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: constituants du lait | COMPOSITION_TERM | 1 | 2 | 1 / 2 / 182, 194 | — | — | — |
| LEXICAL_FR: coriandre | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: cornichons | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: cretons | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 155 | — | — | — |
| LEXICAL_FR: crustacés | REGULATORY_SCOPE_TERM | 1 | 1 | 1 / 1 / 155 | — | — | — |
| LEXICAL_FR: cuisse | EDIBLE_PART | 1 | 4 | 1 / 4 / 166–168 | — | — | — |
| LEXICAL_FR: cumin | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: curcuma | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: curry | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: céleris-raves | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: cœurs de palmier | PROCESSED_PRODUCT | 1 | 2 | 1 / 2 / 141 | — | — | — |
| LEXICAL_FR: d'un poids inférieur à 50 kg | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 148 | — | — | — |
| LEXICAL_FR: d'un poids égal ou supérieur à 50 kg | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 148 | — | — | — |
| LEXICAL_FR: dans la partie IX de la présente annexe | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: demi-carcasse | EDIBLE_PART | 1 | 2 | 1 / 2 / 166, 168 | — | — | — |
| LEXICAL_FR: destinées à l'ensemencement | REGULATORY_CATEGORY | 8 | 29 | 8 / 29 / 106, 137–138, 152 | — | — | — |
| LEXICAL_FR: destinés à la fabrication industrielle d'huile s essentielles ou de résinoïdes ( γ ) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: eau-de-vie | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 177, 200 | — | — | — |
| LEXICAL_FR: estomacs | EDIBLE_PART | 1 | 1 | 1 / 1 / 150 | — | — | — |
| LEXICAL_FR: exhausteurs de goût artificiels | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 18, 21 | — | — | — |
| LEXICAL_FR: fenouil | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: feuilles de laurier | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: feuilles de vigne | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 141 | — | — | — |
| LEXICAL_FR: fleurs de cannelier | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: foie | EDIBLE_PART | 1 | 1 | 1 / 1 / 169 | — | — | — |
| LEXICAL_FR: froment | INGREDIENT_TERM | 2 | 17 | 2 / 17 / 7–8, 12, 133–134 | — | — | — |
| LEXICAL_FR: froment (blé) dur | COMPOSITION_TERM | 3 | 5 | 3 / 5 / 7–8, 12, 133 | — | — | — |
| LEXICAL_FR: fructose | COMPOSITION_TERM | 2 | 7 | 2 / 7 / 134, 159 | — | — | — |
| LEXICAL_FR: fruits à coques | INGREDIENT_TERM | 1 | 5 | 1 / 5 / 139–140, 152 | — | — | — |
| LEXICAL_FR: féveroles | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 137 | — | — | — |
| LEXICAL_FR: févéroles | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: galactose | COMPOSITION_TERM | 1 | 1 | 1 / 1 / 184 | — | — | — |
| LEXICAL_FR: gelée royale | INGREDIENT_TERM | 2 | 3 | 2 / 3 / 147, 163 | — | — | — |
| LEXICAL_FR: gelées | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 141–142 | — | — | — |
| LEXICAL_FR: germes de céréales | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: germes de maïs | RAW_MATERIAL | 1 | 2 | 1 / 2 / 134, 155 | — | — | — |
| LEXICAL_FR: glucose | COMPOSITION_TERM | 4 | 24 | 4 / 24 / 134–136, 144, 155–156, 159, 184 | — | — | — |
| LEXICAL_FR: graisses | INGREDIENT_TERM | 3 | 18 | 3 / 18 / 17–18, 22, 134, 144–146, 153–155 | — | — | — |
| LEXICAL_FR: graisses ajoutées | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 17–18, 22 | — | — | — |
| LEXICAL_FR: graisses de porc (y compris le saindoux) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 145 | — | — | — |
| LEXICAL_FR: graisses et huiles | REGULATORY_SCOPE_TERM | 2 | 2 | 2 / 2 / 153 | — | — | — |
| LEXICAL_FR: grosses brisures | PRODUCT_CATEGORY | 1 | 2 | 1 / 2 / 158 | — | — | — |
| LEXICAL_FR: halvarine | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 188 | — | — | — |
| LEXICAL_FR: hampes | INGREDIENT_TERM | 1 | 6 | 1 / 6 / 143–144 | — | — | — |
| LEXICAL_FR: huile d'olive | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 5 | 44 | 5 / 44 / 2, 5–6, 10, 33, 37, 82–83, 98, 109, 138–139, 154–155, 189, 201 | — | — | — |
| LEXICAL_FR: huile d'olive -composée d'huiles d'olive raffiné es et d'huiles d'olive vierges | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 189 | — | — | — |
| LEXICAL_FR: huile de jojoba | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: huile de saindoux | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: huile de suif | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: huile douce | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 201 | — | — | — |
| LEXICAL_FR: huiles d'olive vierges | PRODUCT_CATEGORY | 3 | 5 | 3 / 5 / 188–189 | — | — | — |
| LEXICAL_FR: huiles essentielles | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: hydromel | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 155 | — | — | — |
| LEXICAL_FR: ignames | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 141 | — | — | — |
| LEXICAL_FR: interverti | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 134 | — | — | — |
| LEXICAL_FR: inuline | REGULATORY_CATEGORY | 2 | 8 | 2 / 8 / 133–134, 136, 152, 159 | — | — | — |
| LEXICAL_FR: jaunes d'œufs | EDIBLE_PART | 2 | 4 | 2 / 4 / 146, 150, 162 | — | — | — |
| LEXICAL_FR: jeune bovin | INGREDIENT_TERM | 1 | 6 | 1 / 6 / 172–173 | — | — | — |
| LEXICAL_FR: jus de bananes | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 141–142 | — | — | — |
| LEXICAL_FR: jus de fruits | INGREDIENT_TERM | 2 | 4 | 2 / 4 / 141, 170 | — | — | — |
| LEXICAL_FR: képhir | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 144 | — | — | — |
| LEXICAL_FR: lactose | COMPOSITION_TERM | 2 | 10 | 2 / 10 / 17, 134, 136, 144, 184 | — | — | — |
| LEXICAL_FR: lait | INGREDIENT_TERM, PRODUCT_CATEGORY | 6 | 175 | 6 / 175 / 3, 5–8, 10, 14, 16–17, 19–20, 36–37, 45, 67–71, 77–78, 82, 84, 88, 91, 96–97, 99, 102, 109, 121–122, 128, 144, 162, 170, 182–185, 187–188, 194, 208 | — | — | — |
| LEXICAL_FR: lait caillé | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 17 | — | — | — |
| LEXICAL_FR: lait cru | INGREDIENT_TERM | 7 | 40 | 7 / 40 / 67–71, 88, 96, 184–185 | — | — | — |
| LEXICAL_FR: lait de consommation | PRODUCT_CATEGORY | 3 | 7 | 3 / 7 / 17, 183–185 | — | — | — |
| LEXICAL_FR: lait de vache | COMPOSITION_TERM | 2 | 4 | 2 / 4 / 7–8, 10 | — | — | — |
| LEXICAL_FR: lait demi-écrémé | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 184 | — | — | — |
| LEXICAL_FR: lait entier | INGREDIENT_TERM | 3 | 5 | 3 / 5 / 184 | — | — | — |
| LEXICAL_FR: lait entier non normalisé | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 184 | — | — | — |
| LEXICAL_FR: lait entier normalisé | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 184 | — | — | — |
| LEXICAL_FR: lait écrémé | INGREDIENT_TERM | 2 | 7 | 2 / 7 / 6, 8, 10, 14, 184 | — | — | — |
| LEXICAL_FR: lait écrémé en poudre | PROCESSED_PRODUCT | 2 | 6 | 2 / 6 / 6, 8, 10, 14 | — | — | — |
| LEXICAL_FR: lupin | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 136, 153 | — | — | — |
| LEXICAL_FR: lupuline | INGREDIENT_TERM | 1 | 4 | 1 / 4 / 36, 107, 138, 159 | — | — | — |
| LEXICAL_FR: lévulose | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: macis | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: maltose | COMPOSITION_TERM | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: mammifères marins | REGULATORY_SCOPE_TERM | 1 | 2 | 1 / 2 / 153, 155 | — | — | — |
| LEXICAL_FR: mangoustans | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: mangues | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: manioc | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 133–134 | — | — | — |
| LEXICAL_FR: marcs de fruits | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 135, 155 | — | — | — |
| LEXICAL_FR: marjolaine vulgaire | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: marmelades | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 141–142 | — | — | — |
| LEXICAL_FR: marrons d'Inde | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 135, 155 | — | — | — |
| LEXICAL_FR: matière grasse laitière anhydre | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 182 | — | — | — |
| LEXICAL_FR: matière grasse laitière à tartiner | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 194 | — | — | — |
| LEXICAL_FR: matière grasse à tartiner | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 194 | — | — | — |
| LEXICAL_FR: matières grasses tartinables | REGULATORY_CATEGORY | 4 | 6 | 4 / 6 / 33, 37, 40, 45, 187, 194 | — | — | — |
| LEXICAL_FR: menthe | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: miel | INGREDIENT_TERM, PRODUCT_CATEGORY | 3 | 7 | 3 / 7 / 134, 147, 163 | — | — | — |
| LEXICAL_FR: millet | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: minarine | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 188 | — | — | — |
| LEXICAL_FR: mollusques | REGULATORY_SCOPE_TERM | 1 | 1 | 1 / 1 / 155 | — | — | — |
| LEXICAL_FR: mélasses | INGREDIENT_TERM | 4 | 7 | 4 / 7 / 108, 134, 136 | — | — | — |
| LEXICAL_FR: mélisse | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: méteil | INGREDIENT_TERM | 2 | 4 | 2 / 4 / 133 | — | — | — |
| LEXICAL_FR: navets | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: nectarines | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: noix de cajou | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: noix de cola | PROCESSED_PRODUCT | 1 | 2 | 1 / 2 / 139, 152 | — | — | — |
| LEXICAL_FR: noix de cola( Cola spp.) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: noix du Brésil | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: noyaux | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: oie nourrie à l'avoine | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 201 | — | — | — |
| LEXICAL_FR: oléomargarine | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: oléostéarine | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 153 | — | — | — |
| LEXICAL_FR: origan | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: palmiste | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 154 | — | — | — |
| LEXICAL_FR: papayes | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: parties de volailles | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 162 | — | — | — |
| LEXICAL_FR: pastèques | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 139–140 | — | — | — |
| LEXICAL_FR: patates douces | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 133, 139, 141 | — | — | — |
| LEXICAL_FR: pavot | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 152 | — | — | — |
| LEXICAL_FR: pectinates | COMPOSITION_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: pectines | COMPOSITION_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: pellicules | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 154, 156 | — | — | — |
| LEXICAL_FR: poireaux | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: poires | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: poiré | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 155 | — | — | — |
| LEXICAL_FR: pois d'Angole | PROCESSED_PRODUCT | 1 | 2 | 1 / 2 / 137, 151 | — | — | — |
| LEXICAL_FR: poissons | REGULATORY_SCOPE_TERM | 2 | 4 | 2 / 4 / 150, 153, 155 | — | — | — |
| LEXICAL_FR: poussins | PRODUCT_CATEGORY | 1 | 2 | 1 / 2 / 162, 186 | — | — | — |
| LEXICAL_FR: produit à base de viande de volaille | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 186 | — | — | — |
| LEXICAL_FR: produits apicoles | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 163 | — | — | — |
| LEXICAL_FR: produits dérivés | PRODUCT_CATEGORY | 2 | 4 | 2 / 4 / 37, 116, 162, 182 | — | — | — |
| LEXICAL_FR: produits entiers | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 162 | — | — | — |
| LEXICAL_FR: produits lait iers | PRODUCT_CATEGORY | 10 | 54 | 10 / 54 / 3, 5, 16–17, 19, 36–37, 45, 67, 69, 71, 77–78, 82, 84, 91, 96–97, 99, 102, 109, 121–122, 135, 144, 155–156, 162, 170, 182–183, 194 | — | — | — |
| LEXICAL_FR: produits séparés | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 162 | — | — | — |
| LEXICAL_FR: produits transformés à base de fruits et légumes | REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 2, 17, 140 | — | — | — |
| LEXICAL_FR: propolis | PROCESSED_PRODUCT | 1 | 3 | 1 / 3 / 147, 163 | — | — | — |
| LEXICAL_FR: protéines issues du lait | COMPOSITION_TERM | 1 | 1 | 1 / 1 / 184 | — | — | — |
| LEXICAL_FR: prunelles | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: prunes | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: préparation à base de viande de volaille | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 2 | 3 | 2 / 3 / 186 | — | — | — |
| LEXICAL_FR: préparation à base de viande de volaille fraîche | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 186 | — | — | — |
| LEXICAL_FR: purées | PROCESSED_PRODUCT | 1 | 3 | 1 / 3 / 141–142 | — | — | — |
| LEXICAL_FR: pâtes de fruits | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: pêches | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: quar tier arrière | EDIBLE_PART | 1 | 1 | 1 / 1 / 168 | — | — | — |
| LEXICAL_FR: racines de chicorée | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 153 | — | — | — |
| LEXICAL_FR: radis | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: remoulages | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 134, 155 | — | — | — |
| LEXICAL_FR: retsina | REGIONAL_NAME | 1 | 2 | 1 / 2 / 176 | — | — | — |
| LEXICAL_FR: riso sbramato | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 157 | — | — | — |
| LEXICAL_FR: riz brun | PRODUCT_CATEGORY | 1 | 2 | 1 / 2 / 135, 157 | — | — | — |
| LEXICAL_FR: riz cargo | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 157 | — | — | — |
| LEXICAL_FR: riz loon zain | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 157 | — | — | — |
| LEXICAL_FR: riz à grains long s A ou B | QUALITY_DESCRIPTION | 1 | 1 | 1 / 1 / 157 | — | — | — |
| LEXICAL_FR: romarin | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: résine de pin d'Alep | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 176 | — | — | — |
| LEXICAL_FR: résinoïdes | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 151 | — | — | — |
| LEXICAL_FR: saccharose | COMPOSITION_TERM | 3 | 11 | 3 / 11 / 136, 159, 180–181, 196, 198 | — | — | — |
| LEXICAL_FR: sagou | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 133, 152 | — | — | — |
| LEXICAL_FR: saindoux | PROCESSED_PRODUCT | 1 | 2 | 1 / 2 / 145, 153 | — | — | — |
| LEXICAL_FR: sainfoin | INGREDIENT_TERM | 1 | 5 | 1 / 5 / 136, 153 | — | — | — |
| LEXICAL_FR: salep | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 133 | — | — | — |
| LEXICAL_FR: salsifis | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: sang | INGREDIENT_TERM | 1 | 6 | 1 / 6 / 145, 154 | — | — | — |
| LEXICAL_FR: sans lactose | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 17 | — | — | — |
| LEXICAL_FR: saucissons | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 145 | — | — | — |
| LEXICAL_FR: sauge | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: sel | COMPOSITION_TERM | 1 | 6 | 1 / 6 / 17–18, 22, 102, 184, 187 | — | — | — |
| LEXICAL_FR: sel ajouté | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 17–18, 22 | — | — | — |
| LEXICAL_FR: sels minéraux | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 184 | — | — | — |
| LEXICAL_FR: sirop d'érable | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 136 | — | — | — |
| LEXICAL_FR: sirop de glucose | COMPOSITION_TERM | 4 | 8 | 4 / 8 / 134–135, 144, 155–156 | — | — | — |
| LEXICAL_FR: sirop de lactose | COMPOSITION_TERM | 2 | 2 | 2 / 2 / 144 | — | — | — |
| LEXICAL_FR: sirop de maltodextrine | PROCESSED_PRODUCT | 1 | 7 | 1 / 7 / 134–135, 144, 155–156 | — | — | — |
| LEXICAL_FR: sorgho | INGREDIENT_TERM | 2 | 3 | 2 / 3 / 104, 133, 137 | — | — | — |
| LEXICAL_FR: succédanés du miel | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 134 | — | — | — |
| LEXICAL_FR: sucre ajouté | COMPOSITION_TERM | 1 | 2 | 1 / 2 / 18, 22 | — | — | — |
| LEXICAL_FR: sucre blanc | COMPOSITION_TERM | 1 | 6 | 1 / 6 / 5, 10, 65, 164–165 | — | — | — |
| LEXICAL_FR: sucre brut | COMPOSITION_TERM | 1 | 5 | 1 / 5 / 5, 165 | — | — | — |
| LEXICAL_FR: sucre inverti | INGREDIENT_TERM | 1 | 4 | 1 / 4 / 134, 164–165 | — | — | — |
| LEXICAL_FR: sucres ajoutés | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 17 | — | — | — |
| LEXICAL_FR: sucres et mélasses caramélisés | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 134 | — | — | — |
| LEXICAL_FR: séchés | REGULATORY_CATEGORY | 2 | 23 | 2 / 23 / 2, 5, 133, 136, 140, 143–146, 149–150, 152–153 | — | — | — |
| LEXICAL_FR: tartre | INGREDIENT_TERM | 2 | 2 | 2 / 2 / 155 | — | — | — |
| LEXICAL_FR: topinambours | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 133, 139 | — | — | — |
| LEXICAL_FR: truffes | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 141 | — | — | — |
| LEXICAL_FR: trèfle | INGREDIENT_TERM | 1 | 5 | 1 / 5 / 136, 153 | — | — | — |
| LEXICAL_FR: veau | INGREDIENT_TERM | 1 | 7 | 1 / 7 / 171–173 | — | — | — |
| LEXICAL_FR: vers à soie | INGREDIENT_TERM | 2 | 5 | 2 / 5 / 3, 147 | — | — | — |
| LEXICAL_FR: vesces | INGREDIENT_TERM | 1 | 5 | 1 / 5 / 136, 153 | — | — | — |
| LEXICAL_FR: vessies | EDIBLE_PART | 1 | 1 | 1 / 1 / 150 | — | — | — |
| LEXICAL_FR: viande bovine | REGULATORY_CATEGORY | 7 | 21 | 7 / 21 / 3, 5, 7–9, 13–14, 36, 99, 102, 109–110, 121–122, 143, 162, 171 | — | — | — |
| LEXICAL_FR: viande d e volaille fraîche | PRODUCT_CATEGORY | 3 | 5 | 3 / 5 / 185–186 | — | — | — |
| LEXICAL_FR: viande de jeune bovin | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 172–173 | — | — | — |
| LEXICAL_FR: viande de porc | REGULATORY_CATEGORY | 3 | 13 | 3 / 13 / 3, 6–7, 10, 36, 99, 102, 109–110, 122, 145 | — | — | — |
| LEXICAL_FR: viande de veau | INGREDIENT_TERM | 1 | 3 | 1 / 3 / 171–172 | — | — | — |
| LEXICAL_FR: viande de volaille | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 6 | 33 | 6 / 33 / 3, 33, 36, 99, 109–110, 122, 146, 162, 185–186, 201 | — | — | — |
| LEXICAL_FR: viande de volaille congelée | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 185 | — | — | — |
| LEXICAL_FR: viande de volaille surgelée | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 185 | — | — | — |
| LEXICAL_FR: viandes | INGREDIENT_TERM, PRODUCT_CATEGORY | 4 | 52 | 4 / 52 / 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 143, 145–150, 154–156, 171, 174–175, 185 | — | — | — |
| LEXICAL_FR: viandes ovine et caprine | REGULATORY_CATEGORY | 4 | 11 | 4 / 11 / 3, 10, 36, 99, 102, 109–110, 122, 128, 145 | — | — | — |
| LEXICAL_FR: vin désalcoolisé | PROCESSED_PRODUCT | 1 | 1 | 1 / 1 / 143 | — | — | — |
| LEXICAL_FR: vinaigre | INGREDIENT_TERM | 4 | 17 | 4 / 17 / 40, 138, 141–142, 155–156, 181 | — | — | — |
| LEXICAL_FR: vitamines | COMPOSITION_TERM | 1 | 2 | 1 / 2 / 184 | — | — | — |
| LEXICAL_FR: volailles abattues | PRODUCT_CATEGORY | 1 | 2 | 1 / 2 / 162 | — | — | — |
| LEXICAL_FR: volailles vivantes | PRODUCT_CATEGORY | 1 | 3 | 1 / 3 / 162 | — | — | — |
| LEXICAL_FR: végétal | ORIGIN_QUALIFIER | 2 | 3 | 2 / 3 / 48, 121, 188 | — | — | — |
| LEXICAL_FR: yaourt | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 17 | — | — | — |
| LEXICAL_FR: Ânes | REGULATORY_CATEGORY | 2 | 2 | 2 / 2 / 148 | — | — | — |
| LEXICAL_FR: Écorces d'agrumes ou de melons (y compris de pastèqu es) | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 140 | — | — | — |
| LEXICAL_FR: Épaules et leurs morceaux | REGULATORY_CATEGORY | 2 | 4 | 2 / 4 / 148–149, 154 | — | — | — |
| LEXICAL_FR: Épeautre | INGREDIENT_TERM | 1 | 2 | 1 / 2 / 133, 137 | — | — | — |
| LEXICAL_FR: à 1702 30 90 | REGULATORY_CATEGORY | 1 | 2 | 1 / 2 / 155–156 | — | — | — |
| LEXICAL_FR: échalotes | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 139 | — | — | — |
| LEXICAL_FR: édulcorants ajoutés | INGREDIENT_TERM | 1 | 1 | 1 / 1 / 18 | — | — | — |
| LEXICAL_FR: éthanol | COMPOSITION_TERM | 2 | 2 | 2 / 2 / 199 | — | — | — |
| LEXICAL_FR: Œufs d'oiseaux | REGULATORY_CATEGORY | 3 | 5 | 3 / 5 / 146, 150, 162 | — | — | — |
| LEXICAL_FR: œufs | INGREDIENT_TERM | 2 | 43 | 2 / 43 / 3, 33, 37, 99, 102, 109, 122, 146, 150, 162, 186–187, 201 | — | — | — |
| LEXICAL_FR: œufs de volailles | INGREDIENT_TERM | 2 | 3 | 2 / 3 / 146, 162 | — | — | — |
| LEXICAL_FR: œufs de volailles de basse-cour, en coquille | REGULATORY_CATEGORY | 1 | 1 | 1 / 1 / 146 | — | — | — |
| LEXICAL_FR: œufs en coquille | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 162 | — | — | — |
| LEXICAL_FR: œufs frais | PRODUCT_CATEGORY | 1 | 1 | 1 / 1 / 186 | — | — | — |
| LEXICAL_FR: œufs à couver | PRODUCT_CATEGORY | 2 | 3 | 2 / 3 / 162, 186 | — | — | — |
| LEXICAL_NL: Aardappelen | INGREDIENT_TERM, REGULATORY_CATEGORY | 3 | 8 | — | 3 / 8 / 133, 140–141, 156 | — | — |
| LEXICAL_NL: Aardappelzetmeel | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Abrikozen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Advocaten | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Advocaten (avocado's) | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Afgeleide producten | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 162 | — | — |
| LEXICAL_NL: Afvallen van zetmeelfabrieken en dergelijke afvallen | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 134 | — | — |
| LEXICAL_NL: Ahornsuiker | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 136 | — | — |
| LEXICAL_NL: Als zaaigoed | REGULATORY_CATEGORY | 2 | 19 | — | 2 / 19 / 137–138 | — | — |
| LEXICAL_NL: Ananassen | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Ander fruit | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Ander vlees en andere eetbare slachtafvallen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 149 | — | — |
| LEXICAL_NL: Ander zetmeel | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Andere bereidingen en conserven | REGULATORY_CATEGORY | 1 | 7 | — | 1 / 7 / 143–145, 154 | — | — |
| LEXICAL_NL: Andere bereidingen en conserven van vlees of van sla chtafvallen van runderen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 143 | — | — |
| LEXICAL_NL: Andere gedroogde zaden van peulgroenten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 137 | — | — |
| LEXICAL_NL: Andere gegiste dranken (bijvoorbeeld appelwijn | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: Andere groenten | REGULATORY_CATEGORY | 1 | 8 | — | 1 / 8 / 139, 141, 151, 155 | — | — |
| LEXICAL_NL: Andere groenten en mengsels van groenten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: Andere noten | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Andere olie en fracties daarvan | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 138 | — | — |
| LEXICAL_NL: Andere oliehoudende zaden en vruchten | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 138, 153 | — | — |
| LEXICAL_NL: Andere suiker | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 134 | — | — |
| LEXICAL_NL: Anijszaad | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Appelen | REGULATORY_CATEGORY | 2 | 3 | — | 2 / 3 / 133, 140, 156 | — | — |
| LEXICAL_NL: Arecanoten | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Arecanoten (of betelnoten) | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Bambarabonen | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Bambarabonen (Vigna subterranea of Voandzeia subt erranea) | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Barnsteenkleurige korrels | QUALITY_DESCRIPTION | 2 | 2 | — | 2 / 2 / 159, 164 | — | — |
| LEXICAL_NL: Basilicum | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Bereiding op basis van pluimveevlees | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 186 | — | — |
| LEXICAL_NL: Bereiding op basis van vers pluimveevlees | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 186 | — | — |
| LEXICAL_NL: Bereidingen en conserven van levers van dieren van a lle soorten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: Bereidingen van bloed van dieren van alle soorten | REGULATORY_CATEGORY | 3 | 3 | — | 3 / 3 / 145, 154 | — | — |
| LEXICAL_NL: Bereidingen van de soort gebruikt voor het voederen van di eren | REGULATORY_CATEGORY | 3 | 3 | — | 3 / 3 / 135, 144, 155 | — | — |
| LEXICAL_NL: Bestemd voor de industriële vervaardiging van etherische oliën of van harsaroma's ( γ ) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 151 | — | — |
| LEXICAL_NL: Bestemd voor de vervaardiging van farmaceutisc he producten ( γ ) | REGULATORY_CATEGORY | 5 | 8 | — | 5 / 8 / 149 | — | — |
| LEXICAL_NL: Bevroren pluimveevlees | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 185 | — | — |
| LEXICAL_NL: Bijenwas | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 147, 163 | — | — |
| LEXICAL_NL: Boekweit | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Bonen van de soort Phaseolus vulgaris | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Bonen van de soort Vigna mungo (L) Hepper of Vigna radiata (L) Wilczek | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 151 | — | — |
| LEXICAL_NL: Bonen van de soort Vigna mungo (L.) Hepper of Vig na radiata (L.) Wilczek | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 137 | — | — |
| LEXICAL_NL: Bostel | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 134 | — | — |
| LEXICAL_NL: Bostel (bouwerijafval) en afvallen van branderijen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Broedeieren | PRODUCT_CATEGORY | 2 | 4 | — | 2 / 4 / 162, 186 | — | — |
| LEXICAL_NL: Buiken (buikspek) en delen daarvan | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 149 | — | — |
| LEXICAL_NL: Cacaobonen | RAW_MATERIAL | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Cacaodoppen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Cajanus cajan | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Cannabis sativa | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Capsicumsoorten bestemd voor de vervaardigin g van capsaïcine of van tincturen ( γ ) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 151 | — | — |
| LEXICAL_NL: Cichoreiwortels | REGULATORY_CATEGORY | 3 | 3 | — | 3 / 3 / 153, 156 | — | — |
| LEXICAL_NL: Cichorium intybus | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Citrusvruchten | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 139–140 | — | — |
| LEXICAL_NL: Clinton | VARIETY_NAME | 1 | 1 | — | 1 / 1 / 39 | — | — |
| LEXICAL_NL: Dadels | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 152 | — | — |
| LEXICAL_NL: Darmen | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: Diepgevroren pluimveevlees | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 185 | — | — |
| LEXICAL_NL: Dierlijke en plantaardige vetten en oliën | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Dranken op basis van melk met cacao, vruchtensap of natuurlij k gearomati­ seerd | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 170 | — | — |
| LEXICAL_NL: Droesem of bezinksel van olie | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Druiven | INGREDIENT_TERM | 2 | 58 | — | 2 / 58 / 4, 47, 90, 94, 135, 139, 142–143, 155, 160–161, 175–179, 181, 196–200 | — | — |
| LEXICAL_NL: Druiven voor tafelgebruik | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Druivenmost waarvan de gisting door de toevoeging van alcoh ol is gestuit | PRODUCT_CATEGORY | 3 | 3 | — | 3 / 3 / 160, 199–200 | — | — |
| LEXICAL_NL: Eetbaar meel en eetbaar poeder van vlees of van slac htafvallen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 143 | — | — |
| LEXICAL_NL: Eetbare koninginnengelei en propolis | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 147 | — | — |
| LEXICAL_NL: Eetbare producten van dierlijke oorsprong | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: Eetbare slachtafvallen van schapen en van geiten | REGULATORY_CATEGORY | 1 | 3 | — | 1 / 3 / 145–146 | — | — |
| LEXICAL_NL: Eetbare slachtafvallen van varkens (huisdieren) | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 145 | — | — |
| LEXICAL_NL: Eieren in de schaal | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 162 | — | — |
| LEXICAL_NL: Eikels | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 135, 155 | — | — |
| LEXICAL_NL: Eikels en wilde kastanjes | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 135, 155 | — | — |
| LEXICAL_NL: Erwten | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Erwten (Pisum sativum) | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Ethylalcohol | REGULATORY_CATEGORY | 3 | 11 | — | 3 / 11 / 3, 99, 109, 147 | — | — |
| LEXICAL_NL: Ethylalcohol en gedistilleerde dranken | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 147 | — | — |
| LEXICAL_NL: Extracten en sappen van vlees | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Ezels | REGULATORY_CATEGORY | 2 | 6 | — | 2 / 6 / 148–149 | — | — |
| LEXICAL_NL: Fokdieren van zuiver ras ( α ) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 148 | — | — |
| LEXICAL_NL: Fokdieren van zuiver ras ( β ) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 148 | — | — |
| LEXICAL_NL: Gallus domesticus | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 185 | — | — |
| LEXICAL_NL: Gebroken korrels | QUALITY_DESCRIPTION | 2 | 5 | — | 2 / 5 / 158 | — | — |
| LEXICAL_NL: Gedroogd | REGULATORY_CATEGORY | 5 | 45 | — | 5 / 45 / 133–134, 136, 138–140, 142–146, 149–150, 152–153, 156, 203 | — | — |
| LEXICAL_NL: Gedroogde groenten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Gedroogde vijgen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Gedroogde zaden van peulgroenten | REGULATORY_CATEGORY | 3 | 4 | — | 3 / 4 / 137, 151–152 | — | — |
| LEXICAL_NL: Gefermenteerde of niet-gefermenteerde zuivelproducten met fruit , natuurlijk ge­ aromatiseerd of niet-gearomatiseerd. | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 170 | — | — |
| LEXICAL_NL: Gefermenteerde zuivelproducten met vruchtensap, natuurlijk ge aromatiseerd of niet-gearomatiseerd | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 170 | — | — |
| LEXICAL_NL: Gefermenteerde zuivelproducten zonder vruchtensap, natuurlijk gearomati­ seerd | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 170 | — | — |
| LEXICAL_NL: Gehomogeniseerde bereidingen van bananen | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 141–142 | — | — |
| LEXICAL_NL: Gehomogeniseerde bereidingen van vlees | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: Gele korrels | QUALITY_DESCRIPTION | 3 | 3 | — | 3 / 3 / 158–159, 164 | — | — |
| LEXICAL_NL: Gember | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Geplette rijstkorrels | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 135 | — | — |
| LEXICAL_NL: Gerst | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 6 | — | 2 / 6 / 7–8, 12, 133 | — | — |
| LEXICAL_NL: Geslacht pluimvee | PRODUCT_CATEGORY | 2 | 2 | — | 2 / 2 / 162 | — | — |
| LEXICAL_NL: Gespikkelde korrels | QUALITY_DESCRIPTION | 3 | 3 | — | 3 / 3 / 158, 164 | — | — |
| LEXICAL_NL: Gevlekte korrels | QUALITY_DESCRIPTION | 2 | 2 | — | 2 / 2 / 158, 164 | — | — |
| LEXICAL_NL: Gevulde deegwaren | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: Glucose en glucosestroop | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 134 | — | — |
| LEXICAL_NL: Gries | REGULATORY_CATEGORY | 2 | 11 | — | 2 / 11 / 133, 135, 141–142, 152, 156 | — | — |
| LEXICAL_NL: Gries en griesmeel | REGULATORY_CATEGORY | 2 | 4 | — | 2 / 4 / 133, 135 | — | — |
| LEXICAL_NL: Gries en griesmeel van tarwe | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 133 | — | — |
| LEXICAL_NL: Groene korrels | QUALITY_DESCRIPTION | 1 | 1 | — | 1 / 1 / 158 | — | — |
| LEXICAL_NL: Groenten | REGULATORY_CATEGORY | 5 | 56 | — | 5 / 56 / 2, 5, 16–17, 33, 35, 82, 85, 99, 101–102, 109–110, 121, 130, 139–141, 151, 155 | — | — |
| LEXICAL_NL: Grondnoten | INGREDIENT_TERM | 1 | 3 | — | 1 / 3 / 138, 152 | — | — |
| LEXICAL_NL: Grondnotenolie | PROCESSED_PRODUCT | 2 | 2 | — | 2 / 2 / 153, 155 | — | — |
| LEXICAL_NL: Grondnotenolie en fracties daarvan | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Guaves | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 139, 152 | — | — |
| LEXICAL_NL: Halfwitte of volwitte rijst | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 135 | — | — |
| LEXICAL_NL: Hammen | REGULATORY_CATEGORY | 1 | 4 | — | 1 / 4 / 148–149, 154 | — | — |
| LEXICAL_NL: Hammen en delen daarvan | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Haver | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 133, 201 | — | — |
| LEXICAL_NL: Havermeel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Heel ei | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 162 | — | — |
| LEXICAL_NL: Hele en halve karkassen | REGULATORY_CATEGORY | 2 | 7 | — | 2 / 7 / 15, 148, 167, 169 | — | — |
| LEXICAL_NL: Hele korrels | QUALITY_DESCRIPTION | 2 | 2 | — | 2 / 2 / 158, 164 | — | — |
| LEXICAL_NL: Hennep | INGREDIENT_TERM | 2 | 11 | — | 2 / 11 / 2, 5, 98, 100, 106, 139 | — | — |
| LEXICAL_NL: Hennep (Cannabis sativa L.) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Hennepzaad | RAW_MATERIAL, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 106, 152 | — | — |
| LEXICAL_NL: Herbemont | VARIETY_NAME | 1 | 1 | — | 1 / 1 / 39 | — | — |
| LEXICAL_NL: Honden– en kattenvoer | REGULATORY_CATEGORY | 1 | 3 | — | 1 / 3 / 135, 144, 155 | — | — |
| LEXICAL_NL: Hopbellen | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 138, 159 | — | — |
| LEXICAL_NL: Humulus lupulus | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 159 | — | — |
| LEXICAL_NL: Hybriden van graansorgho | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 137 | — | — |
| LEXICAL_NL: Hybriden van maïs | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 137 | — | — |
| LEXICAL_NL: Hybriden van suikermaïs | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 137 | — | — |
| LEXICAL_NL: Isabelle | VARIETY_NAME | 1 | 1 | — | 1 / 1 / 39 | — | — |
| LEXICAL_NL: Jacquez | VARIETY_NAME | 1 | 1 | — | 1 / 1 / 39 | — | — |
| LEXICAL_NL: Jam | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 3 | — | 2 / 3 / 141–142 | — | — |
| LEXICAL_NL: Kalfsvlees | INGREDIENT_TERM | 2 | 5 | — | 2 / 5 / 8, 36, 171–173 | — | — |
| LEXICAL_NL: Kaneel | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Kaneel en kaneelknoppen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Karamel bevattende | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 136 | — | — |
| LEXICAL_NL: Katoenzaad | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Kekers | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Kekers (garbanzos) | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Koeienerwten | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Koeienerwten (Vigna unguiculata) | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Koffie | INGREDIENT_TERM | 2 | 4 | — | 2 / 4 / 156 | — | — |
| LEXICAL_NL: Kokosnoten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Kokosolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Kokosolie (kopraolie) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Kolanoten (Cola spp.) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Komkommers | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Komkommers en augurken | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Koolrapen | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 2 | 3 | — | 2 / 3 / 140, 153 | — | — |
| LEXICAL_NL: Koolzaad- | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Kopra | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Korrels die natuurlijke misvormingen vertonen | QUALITY_DESCRIPTION | 1 | 1 | — | 1 / 1 / 158 | — | — |
| LEXICAL_NL: Krijtachtige korrels | QUALITY_DESCRIPTION | 2 | 3 | — | 2 / 3 / 158, 164 | — | — |
| LEXICAL_NL: Kruidnagels | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Kuikens | PRODUCT_CATEGORY | 2 | 2 | — | 2 / 2 / 162, 186 | — | — |
| LEXICAL_NL: Lactuca sativa | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Levend pluimvee | PRODUCT_CATEGORY | 2 | 3 | — | 2 / 3 / 146, 162 | — | — |
| LEXICAL_NL: Levende geiten | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 145 | — | — |
| LEXICAL_NL: Levende paarden | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 148 | — | — |
| LEXICAL_NL: Levende runderen | REGULATORY_CATEGORY | 2 | 4 | — | 2 / 4 / 143, 148, 162 | — | — |
| LEXICAL_NL: Levende schapen | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 145 | — | — |
| LEXICAL_NL: Levende varkens | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 145, 148 | — | — |
| LEXICAL_NL: Lijnzaad | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 138, 152 | — | — |
| LEXICAL_NL: Linzen | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Longhaasjes | EDIBLE_PART | 3 | 6 | — | 3 / 6 / 143–144 | — | — |
| LEXICAL_NL: Longhaasjes en omlopen | REGULATORY_CATEGORY | 4 | 6 | — | 4 / 6 / 143–144 | — | — |
| LEXICAL_NL: Luzerne | INGREDIENT_TERM | 2 | 12 | — | 2 / 12 / 136, 153, 156 | — | — |
| LEXICAL_NL: Luzernemeel en luzerne in pellets met uitzonder ing van luzerne | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Maltodextrine | PROCESSED_PRODUCT | 4 | 10 | — | 4 / 10 / 134–136, 144, 155–156 | — | — |
| LEXICAL_NL: Maltodextrine en maltodextrinestroop | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Maniokwortel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Maniokzetmeel (cassave) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Maïs | RAW_MATERIAL, REGULATORY_CATEGORY | 2 | 10 | — | 2 / 10 / 7–8, 12, 104, 133, 137, 141 | — | — |
| LEXICAL_NL: Maïsmeel | RAW_MATERIAL | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Maïszetmeel | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Meel | REGULATORY_CATEGORY | 2 | 18 | — | 2 / 18 / 133, 136, 141–143, 149–150, 152–153, 155–156 | — | — |
| LEXICAL_NL: Meel van gerst | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Meel van granen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Meel van oliehoudende zaden en vruchten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Melangeproduct | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 195 | — | — |
| LEXICAL_NL: Melkvetproduct | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 194 | — | — |
| LEXICAL_NL: Meloenen | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 139–140 | — | — |
| LEXICAL_NL: Meloenen (watermeloenen daaronder begrepen) en papaja's | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Mengsels met gedroogde bananen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 142 | — | — |
| LEXICAL_NL: Met suiker gekonfijte bananen | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 141–142 | — | — |
| LEXICAL_NL: Mlado vino portugizac | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 130 | — | — |
| LEXICAL_NL: Mosterdzaad | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Mout | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Muskaatnoten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Natuurhoning | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 134, 147 | — | — |
| LEXICAL_NL: Natuurkurk | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 156 | — | — |
| LEXICAL_NL: Niet-eetbare koninginnengelei en propolis | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 147 | — | — |
| LEXICAL_NL: Niet-gekarameliseerd druivensap | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 160 | — | — |
| LEXICAL_NL: Niet-scherpsmakende pepers | REGULATORY_CATEGORY | 4 | 4 | — | 4 / 4 / 140, 151–152 | — | — |
| LEXICAL_NL: Noah | VARIETY_NAME | 1 | 1 | — | 1 / 1 / 39 | — | — |
| LEXICAL_NL: Olijfolie - bestande uit geraffineerde olijfolie en olijfolie van de eerste persing | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 189 | — | — |
| LEXICAL_NL: Ongegiste vruchtensappen (uitgezonderd druivensap en dr uivenmost van de onderverdelin­ | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 141 | — | — |
| LEXICAL_NL: Ongeschikt voor menselijke consumptie ( δ ) | REGULATORY_CATEGORY | 1 | 4 | — | 1 / 4 / 150 | — | — |
| LEXICAL_NL: Ontpunte korrels | QUALITY_DESCRIPTION | 2 | 2 | — | 2 / 2 / 158, 164 | — | — |
| LEXICAL_NL: Op andere wijze bewerkte granen (bijvoorbeeld gepeld | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Origanum vulgare | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Othello | VARIETY_NAME | 1 | 1 | — | 1 / 1 / 39 | — | — |
| LEXICAL_NL: Paddenstoelen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 141 | — | — |
| LEXICAL_NL: Paddenstoelen en truffels | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 141 | — | — |
| LEXICAL_NL: Palmolie | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Palmolie en fracties daarvan | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: Papaverzaad | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Pectinestoffen | COMPOSITION_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Pectinestoffen en pectinaten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Pellets van rijst | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 133, 135 | — | — |
| LEXICAL_NL: Peper | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Peper van het geslacht Piper | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Perskoeken en andere vaste afvallen | REGULATORY_CATEGORY | 3 | 6 | — | 3 / 6 / 134, 155 | — | — |
| LEXICAL_NL: Peulgroenten | REGULATORY_CATEGORY | 3 | 5 | — | 3 / 5 / 137, 139, 151–152 | — | — |
| LEXICAL_NL: Pezen en zenen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 156 | — | — |
| LEXICAL_NL: Phaseolus angularis | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Phaseolus vulgaris | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Pimenta | SOURCE_TAXON_NAME | 2 | 8 | — | 2 / 8 / 139–140, 151–152 | — | — |
| LEXICAL_NL: Piper | SOURCE_TAXON_NAME | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Pisum sativum | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Plantaardige zelfstandigheden en plantaardig afval | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 135, 155 | — | — |
| LEXICAL_NL: Plantains | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 4 | — | 2 / 4 / 139, 142 | — | — |
| LEXICAL_NL: Planten | REGULATORY_CATEGORY | 3 | 18 | — | 3 / 18 / 2, 23, 26–27, 29, 31–34, 99, 121–123, 143, 153 | — | — |
| LEXICAL_NL: Plantensappen en plantenextracten van hop | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 138 | — | — |
| LEXICAL_NL: Pluimveevlees | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 5 | 26 | — | 5 / 26 / 3, 36, 99, 102, 109–110, 122, 146, 185–186 | — | — |
| LEXICAL_NL: Pluimveevleesproduct | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 186 | — | — |
| LEXICAL_NL: Pollen | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 147, 163 | — | — |
| LEXICAL_NL: Producten van dierlijke oorsprong | REGULATORY_CATEGORY | 3 | 3 | — | 3 / 3 / 150, 167 | — | — |
| LEXICAL_NL: Producten voor menselijke consumptie | REGULATORY_CATEGORY | 3 | 3 | — | 3 / 3 / 134, 153–154 | — | — |
| LEXICAL_NL: Retsina | REGIONAL_NAME | 2 | 2 | — | 2 / 2 / 176 | — | — |
| LEXICAL_NL: Rijst (padie) | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 135, 137 | — | — |
| LEXICAL_NL: Rijstmeel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 135 | — | — |
| LEXICAL_NL: Rijstzetmeel | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 135 | — | — |
| LEXICAL_NL: Rode kool | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Rogge | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Roggemeel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: Roodgestreepte korrels | QUALITY_DESCRIPTION | 2 | 2 | — | 2 / 2 / 158, 164 | — | — |
| LEXICAL_NL: Rozijnen | INGREDIENT_TERM | 2 | 6 | — | 2 / 6 / 140, 160, 177 | — | — |
| LEXICAL_NL: Rozijnen en krenten | REGULATORY_CATEGORY | 2 | 6 | — | 2 / 6 / 140, 160, 177 | — | — |
| LEXICAL_NL: Rund- | REGULATORY_CATEGORY | 2 | 3 | — | 2 / 3 / 8, 36, 153 | — | — |
| LEXICAL_NL: Runderen | PRODUCT_CATEGORY | 3 | 47 | — | 3 / 47 / 5, 7–8, 10, 13, 37, 143–144, 148–150, 154, 162, 166–167, 171–174, 183 | — | — |
| LEXICAL_NL: Rundersperma | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: Rundervet | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 144 | — | — |
| LEXICAL_NL: Saffraan | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 140, 152 | — | — |
| LEXICAL_NL: Schapen- of geitenvet | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 146, 153 | — | — |
| LEXICAL_NL: Scherpsmakende vruchten van het geslacht Capsicu m | REGULATORY_CATEGORY | 3 | 4 | — | 3 / 4 / 141, 155 | — | — |
| LEXICAL_NL: Schillen van citrusvruchten en van meloenen (waterme loenen daaronder begrepen) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: Schouders en delen daarvan | REGULATORY_CATEGORY | 2 | 4 | — | 2 / 4 / 148–149, 154 | — | — |
| LEXICAL_NL: Sesamzaad | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Sint-Jansbrood | REGULATORY_CATEGORY | 1 | 3 | — | 1 / 3 / 119, 140, 153 | — | — |
| LEXICAL_NL: Sla | INGREDIENT_TERM | 3 | 6 | — | 3 / 6 / 6, 10–11, 139, 143–144 | — | — |
| LEXICAL_NL: Sla (Lactuca sativa) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Slachtpaarden ( α ) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 156 | — | — |
| LEXICAL_NL: Sojabonen | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 138, 152 | — | — |
| LEXICAL_NL: Sojaolie | PROCESSED_PRODUCT | 2 | 2 | — | 2 / 2 / 153, 155 | — | — |
| LEXICAL_NL: Sojaolie en fracties daarvan | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Spek (ander dan doorregen spek) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: Spelt | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 133, 137 | — | — |
| LEXICAL_NL: Stro en kaf van graangewassen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Struikerwten | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Struikerwten (Cajanus cajan) | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Suikerbieten | INGREDIENT_TERM | 4 | 22 | — | 4 / 22 / 64, 116, 136, 153, 159, 202–204 | — | — |
| LEXICAL_NL: Suikerriet | INGREDIENT_TERM, REGULATORY_CATEGORY | 3 | 7 | — | 3 / 7 / 64, 134, 136, 153 | — | — |
| LEXICAL_NL: Suikerstroop | REGULATORY_CATEGORY | 2 | 5 | — | 2 / 5 / 134, 136, 144 | — | — |
| LEXICAL_NL: Tarwegluten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Tarwezetmeel | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: Thee | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Tijm | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 140, 152 | — | — |
| LEXICAL_NL: Tokaji eszencia | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 176 | — | — |
| LEXICAL_NL: Tokaji fordítás | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 200 | — | — |
| LEXICAL_NL: Tokaji máslás | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 200 | — | — |
| LEXICAL_NL: Tokajská esencia | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 176 | — | — |
| LEXICAL_NL: Tokajský forditáš | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 200 | — | — |
| LEXICAL_NL: Tokajský mášláš | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 200 | — | — |
| LEXICAL_NL: Tomaten | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 139, 141 | — | — |
| LEXICAL_NL: Tuinbonen | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Tuinbonen (Vicia faba var. major) | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Uien | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Vanille | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: Varkensstearine | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: Vers pluimveevlees | PRODUCT_CATEGORY | 2 | 2 | — | 2 / 2 / 185–186 | — | — |
| LEXICAL_NL: Verse vijgen | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: Vetten en oliën | REGULATORY_CATEGORY | 3 | 5 | — | 3 / 5 / 153–154 | — | — |
| LEXICAL_NL: Vigna angularis | SOURCE_TAXON_NAME | 2 | 2 | — | 2 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Vigna mungo | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Vigna radiata | SOURCE_TAXON_NAME | 2 | 2 | — | 2 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Vigna subterranea | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Vigna unguiculata | SOURCE_TAXON_NAME | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Vinho Verde | REGIONAL_NAME | 1 | 1 | — | 1 / 1 / 191 | — | — |
| LEXICAL_NL: Vitis vinifera | SOURCE_TAXON_NAME | 2 | 4 | — | 2 / 4 / 39, 47 | — | — |
| LEXICAL_NL: Vlas | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 7 | — | 2 / 7 / 2, 5, 98, 139 | — | — |
| LEXICAL_NL: Vlees en eetbare slachtafvallen | REGULATORY_CATEGORY | 1 | 3 | — | 1 / 3 / 145–146, 149 | — | — |
| LEXICAL_NL: Vlees en eetbare slachtafvallen van varkens (huisdieren ) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: Vlees van ezels | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 148 | — | — |
| LEXICAL_NL: Vlees van paarden | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 156 | — | — |
| LEXICAL_NL: Vlees van runderen | REGULATORY_CATEGORY | 5 | 14 | — | 5 / 14 / 10, 143, 171–174 | — | — |
| LEXICAL_NL: Vlees van runderen bevroren | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 143 | — | — |
| LEXICAL_NL: Vlees van schapen en van geiten | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 145 | — | — |
| LEXICAL_NL: Vlees van schapen of van geiten | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: Vlees van varkens | REGULATORY_CATEGORY | 1 | 3 | — | 1 / 3 / 145, 148–149 | — | — |
| LEXICAL_NL: Vlokken van rijst | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 133, 135 | — | — |
| LEXICAL_NL: Voandzeia subterranea | SOURCE_TAXON_NAME | 2 | 2 | — | 2 / 2 / 137, 151 | — | — |
| LEXICAL_NL: Vogeleieren in de schaal | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: Vogeleieren uit de schaal en eigeel | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: Voorlopig verduurzaamde bananen | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 140, 142 | — | — |
| LEXICAL_NL: Vruchten | REGULATORY_CATEGORY | 5 | 32 | — | 5 / 32 / 17, 135, 138, 140–141, 144, 151–153, 155, 160, 176 | — | — |
| LEXICAL_NL: Vruchten en andere eetbare plantendelen | REGULATORY_CATEGORY | 2 | 3 | — | 2 / 3 / 141, 155 | — | — |
| LEXICAL_NL: Vruchten van de geslachten Capsicum en Pimen ta | REGULATORY_CATEGORY | 4 | 5 | — | 4 / 5 / 140, 151–152 | — | — |
| LEXICAL_NL: Wortelen | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 139 | — | — |
| LEXICAL_NL: Zaaigoed | REGULATORY_CATEGORY | 2 | 32 | — | 2 / 32 / 133, 137–138, 152 | — | — |
| LEXICAL_NL: Zachte tarwe | INGREDIENT_TERM | 2 | 6 | — | 2 / 6 / 7–8, 12, 133 | — | — |
| LEXICAL_NL: Zachte tarwe en mengkoren | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 133 | — | — |
| LEXICAL_NL: Zemelen | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 134, 155 | — | — |
| LEXICAL_NL: Zetmeel | REGULATORY_CATEGORY | 2 | 11 | — | 2 / 11 / 133–135, 144, 152, 155–156 | — | — |
| LEXICAL_NL: Zonnebloempitten | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 138, 152 | — | — |
| LEXICAL_NL: Zonnebloemzaad- | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: aardperen | REGULATORY_CATEGORY | 1 | 2 | — | 1 / 2 / 133, 139 | — | — |
| LEXICAL_NL: ach tervoet | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 168 | — | — |
| LEXICAL_NL: adzukibonen | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: ahornsuikerstroop | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 136 | — | — |
| LEXICAL_NL: algen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: amomen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: andijvie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: appelwijn | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: augurken | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: avocado's | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 139, 152 | — | — |
| LEXICAL_NL: azijn | INGREDIENT_TERM | 2 | 11 | — | 2 / 11 / 138, 141, 155, 181 | — | — |
| LEXICAL_NL: azijnzuur | INGREDIENT_TERM | 2 | 13 | — | 2 / 13 / 138, 141, 155–156, 161, 181 | — | — |
| LEXICAL_NL: babassunotenolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: bananen | INGREDIENT_TERM, REGULATORY_CATEGORY | 4 | 24 | — | 4 / 24 / 2, 5, 33, 99, 102, 140–142, 152 | — | — |
| LEXICAL_NL: bananensap | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 141–142 | — | — |
| LEXICAL_NL: bataten | INGREDIENT_TERM | 1 | 4 | — | 1 / 4 / 133, 139, 141 | — | — |
| LEXICAL_NL: betelnoten | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 139, 152 | — | — |
| LEXICAL_NL: blazen | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: bloed | INGREDIENT_TERM | 1 | 6 | — | 1 / 6 / 145, 154 | — | — |
| LEXICAL_NL: bloemkool | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: boerenkool | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: bonen van de soort Phaseolus angularis of Vig na angularis (adzukibonen) | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 137, 151 | — | — |
| LEXICAL_NL: cacaoschillen | RAW_MATERIAL | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: cashewnoten | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: cassave | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: consumptiemelk | PRODUCT_CATEGORY | 2 | 9 | — | 2 / 9 / 17, 183–185 | — | — |
| LEXICAL_NL: deeltjes | PRODUCT_CATEGORY | 1 | 4 | — | 1 / 4 / 158 | — | — |
| LEXICAL_NL: delen van pluim­ vee | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 162 | — | — |
| LEXICAL_NL: duivenbonen | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: durumtarwe | COMPOSITION_TERM | 1 | 4 | — | 1 / 4 / 7–8, 12 | — | — |
| LEXICAL_NL: eetbare slachtafvallen van runderen | REGULATORY_CATEGORY | 1 | 4 | — | 1 / 4 / 143–144, 149 | — | — |
| LEXICAL_NL: eieren | INGREDIENT_TERM | 4 | 33 | — | 4 / 33 / 3, 33, 37, 99, 102, 109, 122, 146–147, 162, 186–187, 201 | — | — |
| LEXICAL_NL: eieren van pluimvee in de schaal | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 146 | — | — |
| LEXICAL_NL: eigeel | EDIBLE_PART, PRODUCT_CATEGORY | 2 | 5 | — | 2 / 5 / 146, 150, 162 | — | — |
| LEXICAL_NL: esparcette | INGREDIENT_TERM | 1 | 4 | — | 1 / 4 / 136, 153 | — | — |
| LEXICAL_NL: ethanol | COMPOSITION_TERM | 1 | 1 | — | 1 / 1 / 199 | — | — |
| LEXICAL_NL: etherische oliën | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 151 | — | — |
| LEXICAL_NL: foelie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: fructose | COMPOSITION_TERM | 2 | 7 | — | 2 / 7 / 134, 159 | — | — |
| LEXICAL_NL: galactose | COMPOSITION_TERM | 1 | 1 | — | 1 / 1 / 184 | — | — |
| LEXICAL_NL: garbanzos | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: gedealcoholiseerde wijn | PROCESSED_PRODUCT | 2 | 2 | — | 2 / 2 / 49 | — | — |
| LEXICAL_NL: gestandaardiseerde volle melk | PROCESSED_PRODUCT | 1 | 2 | — | 1 / 2 / 184 | — | — |
| LEXICAL_NL: gestremde melk | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 144 | — | — |
| LEXICAL_NL: gierst | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: glucose | COMPOSITION_TERM | 2 | 16 | — | 2 / 16 / 134–136, 144, 155–156, 159, 184 | — | — |
| LEXICAL_NL: glucosestroop | COMPOSITION_TERM | 2 | 8 | — | 2 / 8 / 134–135, 144, 155–156 | — | — |
| LEXICAL_NL: graansorgho | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 133, 137 | — | — |
| LEXICAL_NL: groentesappen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 141 | — | — |
| LEXICAL_NL: half karkas | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 166 | — | — |
| LEXICAL_NL: halfvolle melk | PROCESSED_PRODUCT | 1 | 2 | — | 1 / 2 / 184 | — | — |
| LEXICAL_NL: halvarine | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 188 | — | — |
| LEXICAL_NL: halve karkassen | EDIBLE_PART | 1 | 8 | — | 1 / 8 / 15, 148, 167–169 | — | — |
| LEXICAL_NL: hanenkammetjes | INGREDIENT_TERM | 1 | 5 | — | 1 / 5 / 136, 153 | — | — |
| LEXICAL_NL: hars van de Aleppopijnboom | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 176 | — | — |
| LEXICAL_NL: harsaroma's | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 151 | — | — |
| LEXICAL_NL: haver vetgemeste gans | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 201 | — | — |
| LEXICAL_NL: heel karkas | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 166 | — | — |
| LEXICAL_NL: hele karkassen | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 168 | — | — |
| LEXICAL_NL: honing | INGREDIENT_TERM | 2 | 5 | — | 2 / 5 / 163 | — | — |
| LEXICAL_NL: honingdrank | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: hopscheuten | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 141 | — | — |
| LEXICAL_NL: inuline | COMPOSITION_TERM, REGULATORY_CATEGORY | 2 | 5 | — | 2 / 5 / 133–134, 152, 159 | — | — |
| LEXICAL_NL: invertsuiker | INGREDIENT_TERM | 1 | 4 | — | 1 / 4 / 134, 164–165 | — | — |
| LEXICAL_NL: jeneverbes | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: jeneverbessen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: jojobaolie | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: jongrundvlees | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 172 | — | — |
| LEXICAL_NL: kanariezaad | RAW_MATERIAL | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: kaneelknoppen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: kanen | INGREDIENT_TERM | 2 | 3 | — | 2 / 3 / 68, 155 | — | — |
| LEXICAL_NL: karamel | INGREDIENT_TERM | 2 | 3 | — | 2 / 3 / 134, 136 | — | — |
| LEXICAL_NL: kardemon | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: karkas | INGREDIENT_TERM | 3 | 7 | — | 3 / 7 / 15, 166–169 | — | — |
| LEXICAL_NL: karwijzaad | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: katoenzaadolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: kerrie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: kersen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: klaver | INGREDIENT_TERM | 1 | 5 | — | 1 / 5 / 136, 153 | — | — |
| LEXICAL_NL: knoflook | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: knolselderij | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: koemelk | COMPOSITION_TERM | 1 | 4 | — | 1 / 4 / 7–8, 10 | — | — |
| LEXICAL_NL: kolanoten | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: komijnzaad | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: koninginnengelei | INGREDIENT_TERM | 2 | 3 | — | 2 / 3 / 147, 163 | — | — |
| LEXICAL_NL: koolrabi | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: kopraolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: korianderzaad | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: krenten | INGREDIENT_TERM | 1 | 6 | — | 1 / 6 / 140, 160, 177 | — | — |
| LEXICAL_NL: kroten | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: kunsthoning | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: kurkuma | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: kweeperen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: lactose | COMPOSITION_TERM | 3 | 8 | — | 3 / 8 / 134, 136, 144, 184 | — | — |
| LEXICAL_NL: laurierblad | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: levers | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 9 | — | 2 / 9 / 145–147, 149–150 | — | — |
| LEXICAL_NL: levulose | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: lupine | INGREDIENT_TERM | 2 | 5 | — | 2 / 5 / 136, 153 | — | — |
| LEXICAL_NL: lupuline | INGREDIENT_TERM | 1 | 4 | — | 1 / 4 / 36, 107, 138, 159 | — | — |
| LEXICAL_NL: magen | EDIBLE_PART | 1 | 1 | — | 1 / 1 / 150 | — | — |
| LEXICAL_NL: magere melk | INGREDIENT_TERM | 1 | 3 | — | 1 / 3 / 184 | — | — |
| LEXICAL_NL: magere melkpoeder | INGREDIENT_TERM | 2 | 6 | — | 2 / 6 / 6, 8, 10, 14 | — | — |
| LEXICAL_NL: maltodextrinestroop | PROCESSED_PRODUCT | 3 | 7 | — | 3 / 7 / 134–135, 144, 155–156 | — | — |
| LEXICAL_NL: maltose | COMPOSITION_TERM | 1 | 1 | — | 1 / 1 / 134 | — | — |
| LEXICAL_NL: manga's | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 139, 152 | — | — |
| LEXICAL_NL: manggistans | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 139, 152 | — | — |
| LEXICAL_NL: marmelade | INGREDIENT_TERM | 1 | 3 | — | 1 / 3 / 141–142 | — | — |
| LEXICAL_NL: maïskiemen | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 134, 155 | — | — |
| LEXICAL_NL: mede | INGREDIENT_TERM | 2 | 6 | — | 2 / 6 / 31, 100, 105, 115, 129, 190 | — | — |
| LEXICAL_NL: melasse | INGREDIENT_TERM | 3 | 5 | — | 3 / 5 / 108, 136, 203 | — | — |
| LEXICAL_NL: melissa | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: melk | INGREDIENT_TERM, PRODUCT_CATEGORY | 6 | 147 | — | 6 / 147 / 3, 5, 7, 16–17, 19, 36–37, 45, 67–71, 77–78, 82, 84, 88, 91, 96–97, 99, 102, 109, 121–122, 144, 162, 170, 182–185, 188, 194, 208 | — | — |
| LEXICAL_NL: mengkoren | INGREDIENT_TERM | 1 | 4 | — | 1 / 4 / 133 | — | — |
| LEXICAL_NL: minarine | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 188 | — | — |
| LEXICAL_NL: minerale zouten | COMPOSITION_TERM | 1 | 1 | — | 1 / 1 / 184 | — | — |
| LEXICAL_NL: mosterdzaadolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 154 | — | — |
| LEXICAL_NL: munt | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: nectarines | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: niet-gestandaardiseerde volle melk | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 184 | — | — |
| LEXICAL_NL: oleomargarine | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: oleostearine | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: oliezuur | PROCESSED_PRODUCT | 2 | 7 | — | 2 / 7 / 189 | — | — |
| LEXICAL_NL: olijfolie | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 5 | 55 | — | 5 / 55 / 2, 5–6, 10, 33, 37, 82–83, 90, 98, 109, 112, 138–139, 154–155, 188–189, 201 | — | — |
| LEXICAL_NL: olijven | PROCESSED_PRODUCT, REGULATORY_CATEGORY | 3 | 28 | — | 3 / 28 / 90, 138–141, 188–189 | — | — |
| LEXICAL_NL: omlopen | EDIBLE_PART | 2 | 6 | — | 2 / 6 / 143–144 | — | — |
| LEXICAL_NL: oregano | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: paardenbonen | RAW_MATERIAL | 1 | 2 | — | 1 / 2 / 137, 151 | — | — |
| LEXICAL_NL: palmharten | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 141 | — | — |
| LEXICAL_NL: papaja's | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: paranoten | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: pectinaten | COMPOSITION_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: peren | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: perenwijn | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: perziken | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: pijlwortel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: plantaardig | ORIGIN_QUALIFIER | 1 | 3 | — | 1 / 3 / 135, 155, 188 | — | — |
| LEXICAL_NL: prei | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: producten van de bijenteelt | PRODUCT_CATEGORY | 2 | 3 | — | 2 / 3 / 3, 147, 163 | — | — |
| LEXICAL_NL: propolis | PROCESSED_PRODUCT | 1 | 3 | — | 1 / 3 / 147, 163 | — | — |
| LEXICAL_NL: pruimen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: radijs | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: rapen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: rauwe melk | INGREDIENT_TERM | 7 | 38 | — | 7 / 38 / 67–71, 88, 96, 184–185 | — | — |
| LEXICAL_NL: reuzel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: rozemarijn | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: rundvlees | REGULATORY_CATEGORY | 2 | 16 | — | 2 / 16 / 3, 5, 7–8, 13–14, 99, 102, 109–110, 121–122, 143, 171–172 | — | — |
| LEXICAL_NL: ruwe suiker | COMPOSITION_TERM | 2 | 3 | — | 2 / 3 / 5, 165 | — | — |
| LEXICAL_NL: ruwe wijnsteen | INGREDIENT_TERM, REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 155 | — | — |
| LEXICAL_NL: sacharose | INGREDIENT_TERM | 1 | 6 | — | 1 / 6 / 136, 159, 196 | — | — |
| LEXICAL_NL: sago | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 133, 152 | — | — |
| LEXICAL_NL: salepwortel | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 133 | — | — |
| LEXICAL_NL: salie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: schaaldieren | REGULATORY_SCOPE_TERM | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: schapen- en geitenvlees | REGULATORY_CATEGORY | 5 | 13 | — | 5 / 13 / 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 145 | — | — |
| LEXICAL_NL: schorseneren | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: sjalotten | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: slachtafvallen | EDIBLE_PART | 9 | 39 | — | 9 / 39 / 143–147, 149–150, 154–155, 162, 171 | — | — |
| LEXICAL_NL: sleepruimen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: slijpsel | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 134, 155 | — | — |
| LEXICAL_NL: smaakversterkers | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 18, 21 | — | — |
| LEXICAL_NL: smeerbare vetten | REGULATORY_CATEGORY | 2 | 2 | — | 2 / 2 / 45, 194 | — | — |
| LEXICAL_NL: spekolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: spliterwten | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 151 | — | — |
| LEXICAL_NL: spruitjes | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: steranijszaad | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: stomp | EDIBLE_PART | 1 | 4 | — | 1 / 4 / 166–168 | — | — |
| LEXICAL_NL: suiker | COMPOSITION_TERM | 7 | 76 | — | 7 / 76 / 2, 5, 10, 17, 22, 64–65, 91, 98, 102, 109, 131–132, 134, 136, 140–142, 144, 146, 150, 159, 162, 164–165, 179–181, 196–197, 202–204 | — | — |
| LEXICAL_NL: talkolie | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: toegevoegd vet | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 22 | — | — |
| LEXICAL_NL: toegevoegd zout | INGREDIENT_TERM | 2 | 3 | — | 2 / 3 / 17–18, 22 | — | — |
| LEXICAL_NL: toegevoegde suiker | INGREDIENT_TERM | 5 | 16 | — | 5 / 16 / 17, 22, 140–141, 144, 146, 150, 162 | — | — |
| LEXICAL_NL: truffels | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 141 | — | — |
| LEXICAL_NL: vallen onder de sector zijderupsen. | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 147 | — | — |
| LEXICAL_NL: varkenskarkas | EDIBLE_PART | 1 | 2 | — | 1 / 2 / 168 | — | — |
| LEXICAL_NL: varkensvet | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 145 | — | — |
| LEXICAL_NL: varkensvet (reuzel daaronder begrepen) | REGULATORY_CATEGORY | 1 | 1 | — | 1 / 1 / 145 | — | — |
| LEXICAL_NL: varkensvlees | REGULATORY_CATEGORY | 3 | 11 | — | 3 / 11 / 3, 10, 36, 99, 102, 109–110, 122, 145 | — | — |
| LEXICAL_NL: venkelzaad | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 152 | — | — |
| LEXICAL_NL: verk oopbenaming | PRODUCT_CATEGORY | 4 | 10 | — | 4 / 10 / 34, 37, 171–174, 200 | — | — |
| LEXICAL_NL: verse eieren | PRODUCT_CATEGORY | 1 | 1 | — | 1 / 1 / 186 | — | — |
| LEXICAL_NL: verwerkte groenten en fruit | REGULATORY_CATEGORY | 1 | 8 | — | 1 / 8 / 2, 5, 99, 101–102, 109–110 | — | — |
| LEXICAL_NL: vijgen | INGREDIENT_TERM | 1 | 3 | — | 1 / 3 / 139–140, 152 | — | — |
| LEXICAL_NL: vis | REGULATORY_SCOPE_TERM | 1 | 3 | — | 1 / 3 / 153, 155 | — | — |
| LEXICAL_NL: vitaminen | COMPOSITION_TERM | 1 | 2 | — | 1 / 2 / 184 | — | — |
| LEXICAL_NL: vlees | INGREDIENT_TERM, PRODUCT_CATEGORY | 4 | 81 | — | 4 / 81 / 10, 13, 16, 33, 37, 109, 143–150, 154–156, 162, 167–168, 171–175, 185–186, 201 | — | — |
| LEXICAL_NL: volle melk | PROCESSED_PRODUCT | 1 | 5 | — | 1 / 5 / 184 | — | — |
| LEXICAL_NL: vruchtengelei | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 141 | — | — |
| LEXICAL_NL: vruchtenmoes | INGREDIENT_TERM | 1 | 3 | — | 1 / 3 / 141–142 | — | — |
| LEXICAL_NL: vruchtenpasta | INGREDIENT_TERM | 2 | 3 | — | 2 / 3 / 141–142 | — | — |
| LEXICAL_NL: vruchtensappen | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 141 | — | — |
| LEXICAL_NL: watermeloenen | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 139–140 | — | — |
| LEXICAL_NL: weekdieren | REGULATORY_SCOPE_TERM | 1 | 1 | — | 1 / 1 / 155 | — | — |
| LEXICAL_NL: wijnsteenzuur | INGREDIENT_TERM | 1 | 3 | — | 1 / 3 / 176, 198 | — | — |
| LEXICAL_NL: wikke | INGREDIENT_TERM | 1 | 6 | — | 1 / 6 / 6, 136, 153 | — | — |
| LEXICAL_NL: wilde kastanjes | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 135, 155 | — | — |
| LEXICAL_NL: wilde marjolein | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 140 | — | — |
| LEXICAL_NL: witloof | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: witte kool | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 139 | — | — |
| LEXICAL_NL: witte suiker | COMPOSITION_TERM | 3 | 6 | — | 3 / 6 / 5, 10, 65, 164–165 | — | — |
| LEXICAL_NL: worst | INGREDIENT_TERM | 2 | 2 | — | 2 / 2 / 145 | — | — |
| LEXICAL_NL: wrongel | INGREDIENT_TERM | 1 | 2 | — | 1 / 2 / 17, 144 | — | — |
| LEXICAL_NL: zachte olie | PROCESSED_PRODUCT | 1 | 1 | — | 1 / 1 / 201 | — | — |
| LEXICAL_NL: zeewier | INGREDIENT_TERM | 1 | 1 | — | 1 / 1 / 153 | — | — |
| LEXICAL_NL: zeezoogdieren | REGULATORY_SCOPE_TERM | 1 | 2 | — | 1 / 2 / 153, 155 | — | — |
| LEXICAL_NL: zijderupsen | INGREDIENT_TERM | 2 | 7 | — | 2 / 7 / 3, 5, 82, 147 | — | — |
| LEXICAL_NL: zoetstoffen | INGREDIENT_TERM | 1 | 15 | — | 1 / 15 / 18, 140–141, 144, 146, 150, 162 | — | — |
| LEXICAL_NL: zout | COMPOSITION_TERM | 1 | 9 | — | 1 / 9 / 17–18, 22, 138, 140, 151, 187 | — | — |
| LEXICAL_NL: zuive lproducten | PRODUCT_CATEGORY | 11 | 53 | — | 11 / 53 / 3, 5, 16–17, 19, 36–37, 45, 67, 69, 71, 77–78, 82, 84, 91, 96–97, 99, 102, 109, 121–122, 135, 144, 155–156, 162, 170, 182–183, 194 | — | — |
| LEXICAL_NL: zwaveldioxide | INGREDIENT_TERM | 1 | 5 | — | 1 / 5 / 138, 140, 151, 180–181 | — | — |
| legal_II_III_1 | PRODUCT_CATEGORY | 7 | 95 | 2 / 38 / 2, 33, 36, 82, 106–107, 138, 141, 159–160 | 2 / 19 / 2, 33, 36, 106–107, 138, 159–160 | 2 / 24 / 2, 33, 36, 82, 106–107, 138, 159–160 | 1 / 14 / 2, 33, 36, 82, 107, 138, 159–160 |
| legal_II_III_2 | PRODUCT_CATEGORY | 7 | 28 | 1 / 7 / 36, 107, 159–160 | 2 / 7 / 36, 107, 159–160 | 2 / 7 / 36, 107, 159–160 | 2 / 7 / 36, 107, 159–160 |
| legal_II_III_3 | PRODUCT_CATEGORY | 8 | 11 | 2 / 3 / 36, 107, 159 | 2 / 3 / 36, 107, 159 | 3 / 3 / 36, 107, 159 | 1 / 2 / 36, 159 |
| legal_II_III_4 | PRODUCT_CATEGORY | 10 | 12 | 1 / 3 / 36, 107, 160 | 3 / 3 / 36, 107, 160 | 3 / 3 / 36, 107, 160 | 3 / 3 / 36, 107, 160 |
| legal_II_III_5 | PRODUCT_CATEGORY | 8 | 11 | 2 / 3 / 36, 107, 160 | 3 / 3 / 36, 107, 160 | 2 / 3 / 36, 107, 160 | 1 / 2 / 36, 160 |
| legal_II_II_A1 | PRODUCT_CATEGORY | 6 | 15 | 1 / 1 / 159 | 3 / 7 / 5, 10, 65, 159, 164–165 | 1 / 1 / 159 | 1 / 6 / 5, 10, 65, 159, 164 |
| legal_II_II_A2 | PRODUCT_CATEGORY | 5 | 10 | 1 / 1 / 159 | 2 / 4 / 5, 159, 165 | 1 / 1 / 159 | 1 / 4 / 5, 159, 165 |
| legal_II_II_A3 | COMPOSITION_TERM, PRODUCT_CATEGORY | 10 | 21 | 2 / 6 / 136, 159 | 3 / 5 / 136, 159 | 4 / 6 / 136, 159 | 1 / 4 / 136, 159 |
| legal_II_II_A4 | COMPOSITION_TERM, PRODUCT_CATEGORY | 6 | 8 | 2 / 2 / 136, 159 | 2 / 2 / 136, 159 | 1 / 2 / 136, 159 | 1 / 2 / 136, 159 |
| legal_II_IV_10 | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 10 | 24 | 3 / 6 / 143, 155, 161, 200 | 3 / 6 / 143, 155, 161, 200 | 2 / 6 / 143, 155, 161, 200 | 2 / 6 / 143, 155, 161, 200 |
| legal_II_IV_11 | PRODUCT_CATEGORY | 7 | 11 | 2 / 2 / 161, 199 | 2 / 3 / 161, 199 | 2 / 3 / 161, 199 | 1 / 3 / 161, 199 |
| legal_II_IV_12 | PRODUCT_CATEGORY | 8 | 14 | 3 / 3 / 161, 178 | 2 / 3 / 161, 178 | 2 / 3 / 161, 178 | 1 / 5 / 161, 178 |
| legal_II_IV_4 | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 15 | 64 | 5 / 23 / 142, 160–161, 175, 177–179, 196–200 | 4 / 18 / 142, 160–161, 175, 177–179, 196–200 | 4 / 19 / 142, 160–161, 175, 177–179, 196–200 | 2 / 4 / 142, 160, 198, 200 |
| legal_II_IV_6 | PRODUCT_CATEGORY | 12 | 72 | 4 / 18 / 38, 101–102, 142, 160, 199–200 | 3 / 19 / 38, 101–102, 141–142, 160, 199–200 | 2 / 19 / 38, 101–102, 141–142, 160, 199–200 | 3 / 16 / 38, 102, 141–142, 160, 199–200 |
| legal_II_IV_7 | PRODUCT_CATEGORY | 11 | 20 | 4 / 5 / 160, 199–200 | 2 / 6 / 38, 160, 199–200 | 2 / 6 / 38, 160, 199–200 | 3 / 3 / 160, 199–200 |
| legal_II_IV_8 | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 8 | 24 | 2 / 2 / 160, 200 | 2 / 8 / 143, 155, 160, 176, 200 | 3 / 6 / 143, 155, 160, 200 | 1 / 8 / 143, 155, 160, 200 |
| legal_II_IV_9 | PRODUCT_CATEGORY | 13 | 28 | 2 / 3 / 161, 177, 200 | 4 / 7 / 161, 177, 200 | 4 / 9 / 143, 155, 161, 177, 200 | 3 / 9 / 135, 143, 155, 161, 200 |
| legal_II_I_1a | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 7 | 55 | 2 / 18 / 5, 7–8, 12, 135, 137, 157, 164 | 2 / 17 / 5, 7–8, 12, 135, 137, 157, 164 | 2 / 16 / 5, 7–8, 12, 157, 164 | 1 / 4 / 135, 137, 157 |
| legal_II_I_1b | PRODUCT_CATEGORY, REGULATORY_CATEGORY | 7 | 11 | 2 / 3 / 135, 157 | 2 / 3 / 135, 157 | 2 / 2 / 157 | 1 / 3 / 135, 157 |
| legal_II_I_1c | PRODUCT_CATEGORY | 8 | 9 | 2 / 3 / 135, 157 | 2 / 2 / 157 | 2 / 2 / 157 | 2 / 2 / 157 |
| legal_II_I_1d | PRODUCT_CATEGORY | 9 | 20 | 2 / 5 / 157, 164 | 3 / 6 / 135, 157, 164 | 3 / 6 / 135, 157, 164 | 1 / 3 / 135, 157 |
| legal_II_I_2a | PRODUCT_CATEGORY | 7 | 11 | 2 / 3 / 157 | 2 / 3 / 157 | 2 / 3 / 157 | 1 / 2 / 157 |
| legal_II_I_2b | PRODUCT_CATEGORY, QUALITY_DESCRIPTION | 9 | 11 | 2 / 2 / 157 | 2 / 3 / 157 | 2 / 3 / 157 | 3 / 3 / 157 |
| legal_II_I_2c | PRODUCT_CATEGORY | 9 | 18 | 3 / 5 / 157 | 3 / 5 / 157 | 2 / 4 / 157 | 1 / 4 / 157 |
| legal_II_I_3 | INGREDIENT_TERM, PRODUCT_CATEGORY, REGULATORY_CATEGORY | 8 | 28 | 2 / 11 / 135, 154, 157–158 | 3 / 4 / 135, 157–158 | 2 / 3 / 135, 157–158 | 1 / 10 / 135, 157–158 |
| legal_VII_AppendixII_A1 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_A2 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_A3 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_A4 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_B1 | INGREDIENT_TERM, PRODUCT_CATEGORY, REGULATORY_CATEGORY | 10 | 26 | 3 / 8 / 153–154, 188, 194–195 | 3 / 7 / 154, 188, 194–195 | 2 / 6 / 154, 188, 194–195 | 2 / 5 / 188, 194–195 |
| legal_VII_AppendixII_B2 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_B3 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_B4 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 | 1 / 1 / 194 |
| legal_VII_AppendixII_C1 | INGREDIENT_TERM, PRODUCT_CATEGORY | 8 | 13 | 1 / 1 / 195 | 3 / 7 / 195 | 2 / 3 / 195 | 2 / 2 / 195 |
| legal_VII_AppendixII_C2 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 195 | 1 / 1 / 195 | 1 / 1 / 195 | 1 / 1 / 195 |
| legal_VII_AppendixII_C3 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 195 | 1 / 1 / 195 | 1 / 1 / 195 | 1 / 1 / 195 |
| legal_VII_AppendixII_C4 | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 195 | 1 / 1 / 195 | 1 / 1 / 195 | 1 / 1 / 195 |
| legal_VII_III_2a_i | INGREDIENT_TERM, PRODUCT_CATEGORY, REGULATORY_CATEGORY | 7 | 8 | 2 / 2 / 144, 182 | 2 / 2 / 144, 182 | 2 / 2 / 144, 182 | 1 / 2 / 144, 182 |
| legal_VII_III_2a_ii | INGREDIENT_TERM, PRODUCT_CATEGORY | 7 | 51 | 3 / 12 / 7, 10, 144, 162, 182, 184, 188 | 1 / 13 / 7, 10, 144, 162, 182, 184, 188 | 2 / 13 / 7, 10, 144, 162, 182, 184, 188 | 1 / 13 / 7, 10, 120, 144, 162, 182, 184, 188 |
| legal_VII_III_2a_iii | INGREDIENT_TERM, PRODUCT_CATEGORY | 12 | 61 | 3 / 16 / 5, 7–8, 10, 14, 141, 144, 162, 182, 188, 194 | 4 / 15 / 5, 7–8, 10, 14, 144, 162, 182, 188, 194 | 3 / 16 / 5, 7–8, 10, 14, 141, 144, 162, 182, 188, 194 | 2 / 14 / 5, 7–8, 10, 14, 144, 162, 182, 188, 194 |
| legal_VII_III_2a_iv | INGREDIENT_TERM, PRODUCT_CATEGORY | 9 | 10 | 2 / 2 / 144, 182 | 4 / 4 / 144, 182 | 2 / 2 / 144, 182 | 1 / 2 / 144, 182 |
| legal_VII_III_2a_ix | PRODUCT_CATEGORY | 4 | 10 | 1 / 2 / 144, 182 | 1 / 3 / 17, 144, 182 | 1 / 2 / 144, 182 | 1 / 3 / 17, 144, 182 |
| legal_VII_III_2a_v | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_vi | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_vii | PRODUCT_CATEGORY | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_viii | INGREDIENT_TERM, PRODUCT_CATEGORY | 8 | 40 | 1 / 8 / 10, 17, 88, 182 | 2 / 12 / 10, 17, 88, 144, 182 | 3 / 13 / 10, 17, 88, 144, 182 | 2 / 7 / 10, 17, 88, 144, 182 |
| legal_VII_III_2a_x | PRODUCT_CATEGORY | 4 | 7 | 1 / 1 / 182 | 1 / 2 / 144, 182 | 1 / 2 / 144, 182 | 1 / 2 / 144, 182 |
| legal_VII_III_2a_xi | REGIONAL_NAME | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_xii | REGIONAL_NAME | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_xiii | REGIONAL_NAME | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_xiv | PRODUCT_CATEGORY | 4 | 8 | 1 / 2 / 182 | 1 / 2 / 182 | 1 / 2 / 182 | 1 / 2 / 182 |
| legal_VII_III_2a_xv | REGIONAL_NAME | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_III_2a_xvi | REGIONAL_NAME | 4 | 4 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 | 1 / 1 / 182 |
| legal_VII_II_1 | PRODUCT_CATEGORY | 25 | 678 | 5 / 134 / 2, 24–25, 29, 34, 36, 38–40, 43–44, 46–49, 54, 56–57, 61, 63, 65–67, 88, 99, 109–110, 117, 142–143, 155–156, 160–161, 175–179, 181, 196–200 | 7 / 156 / 2, 23–25, 28, 31, 34, 36, 38, 40–41, 43–44, 46, 48–50, 54, 56–59, 61, 63, 65–67, 88, 90, 99, 101, 107, 109–110, 117, 142–143, 160–161, 175–179, 181, 196–200 | 7 / 284 / 2, 5, 23–25, 27–29, 34, 36, 38–41, 43–44, 46–52, 54–57, 59–63, 65–67, 88, 90, 94, 99, 101, 107, 109–110, 117–118, 130, 142–143, 155–156, 160–161, 175–179, 181, 190–193, 196–200 | 6 / 104 / 2, 24–25, 28, 34, 36, 38–40, 43, 46, 48–49, 51, 54, 57, 59, 65–67, 88, 94, 99, 101, 109–110, 117, 142–143, 160–161, 175–179, 181, 196, 198–200 |
| legal_VII_II_10 | PRODUCT_CATEGORY | 38 | 327 | 8 / 73 / 102, 160–161, 176–181, 196–200 | 11 / 94 / 38, 102, 141–142, 160–161, 175–181, 196–200 | 8 / 90 / 38, 102, 141–142, 160–161, 175–181, 196–200 | 11 / 70 / 34, 38, 102, 141–142, 160–161, 176–180, 196–200 |
| legal_VII_II_11 | PRODUCT_CATEGORY | 21 | 58 | 7 / 15 / 176–177, 179, 196–198, 200 | 6 / 20 / 142, 176–179, 196–198, 200 | 4 / 15 / 176, 178–179, 196–198, 200 | 4 / 8 / 177, 179, 200 |
| legal_VII_II_12 | PRODUCT_CATEGORY | 13 | 17 | 4 / 4 / 177, 179 | 4 / 6 / 177, 179, 200 | 2 / 3 / 179, 200 | 3 / 4 / 179, 200 |
| legal_VII_II_13 | PRODUCT_CATEGORY | 25 | 78 | 6 / 21 / 160, 177, 180–181, 196, 198, 200 | 7 / 26 / 38, 160, 177, 180–181, 196–198, 200 | 9 / 25 / 38, 160, 177, 180–181, 196–198, 200 | 3 / 6 / 177, 180, 200 |
| legal_VII_II_14 | PRODUCT_CATEGORY | 17 | 25 | 2 / 3 / 180, 197 | 6 / 9 / 180, 196–198, 200 | 7 / 10 / 180–181, 196–198, 200 | 2 / 3 / 180, 200 |
| legal_VII_II_15 | PRODUCT_CATEGORY | 6 | 8 | 2 / 2 / 181 | 2 / 2 / 181 | 1 / 2 / 181 | 1 / 2 / 181 |
| legal_VII_II_16 | PRODUCT_CATEGORY | 7 | 11 | 2 / 2 / 181 | 2 / 3 / 181, 200 | 2 / 3 / 181, 200 | 1 / 3 / 181, 200 |
| legal_VII_II_17 | PRODUCT_CATEGORY | 7 | 19 | 2 / 5 / 40, 142, 156, 181 | 2 / 5 / 40, 142, 156, 181 | 2 / 5 / 40, 142, 156, 181 | 1 / 4 / 40, 142, 181 |
| legal_VII_II_2 | PRODUCT_CATEGORY | 19 | 35 | 5 / 10 / 176, 179, 196–198 | 6 / 8 / 176, 196–198 | 6 / 10 / 176, 179, 196–198 | 2 / 7 / 176, 179, 196, 198 |
| legal_VII_II_3 | PRODUCT_CATEGORY | 8 | 16 | 2 / 2 / 176 | 3 / 5 / 41, 176, 199 | 2 / 3 / 176, 199 | 1 / 6 / 176–177, 199 |
| legal_VII_II_4 | PRODUCT_CATEGORY | 17 | 65 | 3 / 12 / 63, 161, 177–178, 198 | 5 / 12 / 41, 59, 177–178, 198–199 | 6 / 27 / 59, 63, 161, 177–179, 198–199 | 3 / 14 / 59, 63, 161, 177–178, 198–199 |
| legal_VII_II_5 | PRODUCT_CATEGORY | 10 | 26 | 2 / 4 / 178 | 2 / 8 / 59, 178 | 4 / 5 / 59, 178 | 2 / 9 / 59, 178 |
| legal_VII_II_6 | PRODUCT_CATEGORY | 9 | 12 | 2 / 2 / 178 | 3 / 4 / 59, 178 | 3 / 4 / 59, 178 | 1 / 2 / 178 |
| legal_VII_II_7 | PRODUCT_CATEGORY | 12 | 18 | 2 / 3 / 178, 198 | 4 / 5 / 59, 178, 198 | 3 / 5 / 59, 178, 198 | 3 / 5 / 59, 178, 198 |
| legal_VII_II_8 | PRODUCT_CATEGORY | 8 | 19 | 2 / 4 / 179 | 2 / 5 / 179, 199 | 2 / 5 / 179, 199 | 2 / 5 / 179, 199 |
| legal_VII_II_9 | PRODUCT_CATEGORY | 6 | 8 | 2 / 2 / 179 | 2 / 2 / 179 | 1 / 2 / 179 | 1 / 2 / 179 |
| legal_VII_I_V | PRODUCT_CATEGORY | 7 | 11 | 2 / 4 / 171–172 | 2 / 2 / 171–172 | 1 / 1 / 172 | 2 / 4 / 171–172 |
| legal_VII_I_Z | PRODUCT_CATEGORY | 6 | 8 | 2 / 3 / 172 | 2 / 2 / 172–173 | 1 / 1 / 173 | 1 / 2 / 172 |
| legal_VII_VIII_1a | PRODUCT_CATEGORY | 8 | 12 | 2 / 3 / 6, 189 | 2 / 3 / 6, 189 | 2 / 3 / 6, 189 | 2 / 3 / 6, 189 |
| legal_VII_VIII_1b | PRODUCT_CATEGORY | 17 | 51 | 3 / 12 / 6, 189 | 8 / 17 / 6, 188–189 | 3 / 13 / 6, 189 | 3 / 9 / 6, 189 |
| legal_VII_VIII_1c | PRODUCT_CATEGORY | 8 | 18 | 2 / 4 / 6, 189 | 1 / 2 / 189 | 3 / 6 / 6, 189 | 2 / 6 / 6, 189 |
| legal_VII_VIII_2 | PRODUCT_CATEGORY | 12 | 13 | 3 / 3 / 189 | 4 / 5 / 189 | 3 / 3 / 189 | 2 / 2 / 189 |
| legal_VII_VIII_4 | PRODUCT_CATEGORY | 9 | 10 | 2 / 3 / 189 | 2 / 2 / 189 | 3 / 3 / 189 | 2 / 2 / 189 |
| legal_VII_VIII_5 | PRODUCT_CATEGORY | 10 | 11 | 2 / 3 / 189 | 3 / 3 / 189 | 3 / 3 / 189 | 2 / 2 / 189 |
| legal_VII_VIII_6 | PRODUCT_CATEGORY | 11 | 32 | 2 / 8 / 189 | 3 / 6 / 189 | 4 / 8 / 189 | 2 / 10 / 188–189 |
| sector_I_I | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_II | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_III | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_IV | SECTOR_SCOPE_REVIEW | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_IX | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_V | SECTOR_SCOPE_REVIEW | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_VI | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_VII | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_VIII | SECTOR_SCOPE_REVIEW | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_X | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_XI | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_XII | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_XIII | SECTOR_SCOPE_REVIEW | 4 | 4 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 | 1 / 1 / 2 |
| sector_I_XIV | SECTOR_SCOPE_REVIEW | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XIX | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XV | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XVI | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XVII | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XVIII | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XX | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XXI | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XXII | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XXIII | SECTOR_SCOPE_REVIEW | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |
| sector_I_XXIV | REGULATORY_CATEGORY | 4 | 4 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 | 1 / 1 / 3 |

## Annexe C — Inventaire des 876 pages extraites

Une cellule contient caractères du bloc TXT (hors blancs de fin), occurrences candidates et repère juridique observé. Zéro occurrence signifie absence de résultat de la méthode, pas absence prouvée de vocabulaire utile. Toutes les pages figurent dans les quatre TXT. Un repère est une aide documentaire et peut désigner la continuation d’une section.

| Page PDF | FR : caractères / occurrences / repère | NL : caractères / occurrences / repère | EN : caractères / occurrences / repère | DE : caractères / occurrences / repère |
|---:|---|---|---|---|
| 1 | 2422 / 0 / Aucun repère candidat ; voir TXT | 2578 / 0 / Aucun repère candidat ; voir TXT | 2254 / 0 / Aucun repère candidat ; voir TXT | 2502 / 0 / Aucun repère candidat ; voir TXT |
| 2 | 1370 / 27 / Article 1 ; Article 1(2)(a) / Annex I / Part I ; Article 1(2)(b) / Annex I / Part II ; +11 autres repères (CSV) | 1377 / 24 / Article 1 ; Article 1(2)(a) / Annex I / Part I ; Article 1(2)(b) / Annex I / Part II ; +11 autres repères (CSV) | 1340 / 27 / Article 1 ; Article 1(2)(a) / Annex I / Part I ; Article 1(2)(b) / Annex I / Part II ; +11 autres repères (CSV) | 1371 / 23 / Article 1 ; Article 1(2)(a) / Annex I / Part I ; Article 1(2)(b) / Annex I / Part II ; +11 autres repères (CSV) |
| 3 | 1938 / 20 / Article 1 ; Article 1(2)(n) / Annex I / Part XIV ; Article 1(2)(s) / Annex I / Part XIX ; +9 autres repères (CSV) | 2043 / 21 / Article 1 ; Article 1(2)(n) / Annex I / Part XIV ; Article 1(2)(s) / Annex I / Part XIX ; +9 autres repères (CSV) | 1917 / 22 / Article 1 ; Article 1(2)(n) / Annex I / Part XIV ; Article 1(2)(s) / Annex I / Part XIX ; +9 autres repères (CSV) | 2097 / 20 / Article 1 ; Article 1(2)(n) / Annex I / Part XIV ; Article 1(2)(s) / Annex I / Part XIX ; +9 autres repères (CSV) |
| 4 | 2925 / 0 / Aucun repère candidat ; voir TXT | 2930 / 1 / Article 3 | 2663 / 0 / Aucun repère candidat ; voir TXT | 3025 / 1 / Article 3 |
| 5 | 1859 / 20 / Article 6 ; Article 7 | 1868 / 25 / Article 6 ; Article 7 | 1741 / 25 / Article 7 ; Article 6 | 1806 / 19 / Article 6 ; Article 7 |
| 6 | 1759 / 12 / Article 7 | 1669 / 10 / Article 7 | 1546 / 13 / Article 7 | 1700 / 11 / Article 7 |
| 7 | 2355 / 19 / Article 11 ; Article 10 ; Article 9 | 2232 / 13 / Article 11 ; Article 10 ; Article 9 | 2089 / 20 / Article 11 ; Article 10 ; Article 9 | 2248 / 19 / Article 11 ; Article 9 ; Article 10 |
| 8 | 1861 / 33 / Article 12 ; Article 13 ; Article 11 | 1786 / 21 / Article 12 ; Article 13 ; Article 11 | 1730 / 29 / Article 12 ; Article 13 ; Article 11 | 1875 / 20 / Article 12 ; Article 13 ; Article 11 |
| 9 | 2850 / 1 / Article 13 | 2777 / 0 / Aucun repère candidat ; voir TXT | 2380 / 2 / Article 13 | 2567 / 1 / Article 13 |
| 10 | 2198 / 19 / Article 17 | 2187 / 18 / Article 17 ; Article 16 | 2012 / 22 / Article 17 | 2166 / 13 / Article 17 |
| 11 | 2229 / 0 / Aucun repère candidat ; voir TXT | 2358 / 1 / Article 18 | 2069 / 0 / Aucun repère candidat ; voir TXT | 2480 / 0 / Aucun repère candidat ; voir TXT |
| 12 | 2445 / 9 / Article 19 | 2686 / 5 / Article 19 | 2269 / 6 / Article 19 | 2743 / 6 / Article 19 |
| 13 | 2548 / 2 / Article 19 | 2443 / 3 / Article 19 | 2299 / 5 / Article 19 | 2633 / 2 / Article 19 |
| 14 | 2141 / 5 / Article 20 | 2205 / 3 / Article 20 | 1989 / 6 / Article 20 | 2284 / 2 / Article 20 |
| 15 | 2270 / 3 / Article 20 | 2342 / 3 / Article 20 | 2206 / 0 / Aucun repère candidat ; voir TXT | 2455 / 9 / Article 20 |
| 16 | 2123 / 9 / Article 21 ; Article 23 | 1869 / 4 / Article 21 | 1847 / 11 / Article 21 ; Article 23 | 1965 / 3 / Article 21 ; Article 23 |
| 17 | 2432 / 25 / Article 23 | 2211 / 12 / Article 23 | 2228 / 23 / Article 23 | 2281 / 10 / Article 23 |
| 18 | 3383 / 7 / Article 23 | 3368 / 4 / Article 23 | 3087 / 6 / Article 23 | 3375 / 2 / Article 23 |
| 19 | 3018 / 7 / Article 23 | 2729 / 2 / Article 23 | 2686 / 8 / Article 23a | 3025 / 2 / Article 23a |
| 20 | 3340 / 9 / Article 23 | 3012 / 0 / Aucun repère candidat ; voir TXT | 2824 / 9 / Article 23a | 3245 / 3 / Article 23a |
| 21 | 2858 / 1 / Article 24 | 2928 / 1 / Article 24 | 2514 / 0 / Aucun repère candidat ; voir TXT | 2781 / 1 / Article 24 |
| 22 | 3072 / 5 / Article 24 | 3147 / 5 / Article 24 | 2644 / 4 / Article 24 | 2997 / 1 / Article 24 |
| 23 | 2290 / 0 / Aucun repère candidat ; voir TXT | 2232 / 2 / Article 61 | 2171 / 1 / Article 62 | 2220 / 0 / Aucun repère candidat ; voir TXT |
| 24 | 3903 / 1 / Article 62 | 3880 / 3 / Article 62 | 3489 / 5 / Article 62 | 3855 / 3 / Article 62 |
| 25 | 2928 / 2 / Article 63 | 2908 / 3 / Article 63 | 2564 / 3 / Article 63 | 2995 / 4 / Article 62 ; Article 63 |
| 26 | 2334 / 0 / Aucun repère candidat ; voir TXT | 2278 / 1 / Article 64 | 2150 / 0 / Aucun repère candidat ; voir TXT | 2409 / 0 / Aucun repère candidat ; voir TXT |
| 27 | 2621 / 0 / Aucun repère candidat ; voir TXT | 2485 / 1 / Article 64 | 2272 / 1 / Article 64 | 2472 / 0 / Aucun repère candidat ; voir TXT |
| 28 | 3108 / 0 / Aucun repère candidat ; voir TXT | 3028 / 3 / Article 65 ; Article 66 | 2854 / 1 / Article 65 | 3120 / 2 / Article 65 ; Article 66 |
| 29 | 3256 / 2 / Article 66 | 3244 / 1 / Article 67 | 2986 / 2 / Article 66 | 3288 / 0 / Aucun repère candidat ; voir TXT |
| 30 | 2226 / 0 / Aucun repère candidat ; voir TXT | 2064 / 0 / Aucun repère candidat ; voir TXT | 2094 / 0 / Aucun repère candidat ; voir TXT | 2147 / 0 / Aucun repère candidat ; voir TXT |
| 31 | 2149 / 0 / Aucun repère candidat ; voir TXT | 2156 / 3 / Article 70 ; Article 71 | 1955 / 0 / Aucun repère candidat ; voir TXT | 2082 / 0 / Aucun repère candidat ; voir TXT |
| 32 | 1820 / 1 / Article 73 | 1881 / 1 / Article 73 | 1702 / 0 / Aucun repère candidat ; voir TXT | 1884 / 1 / Article 73 |
| 33 | 2323 / 12 / Article 75 | 2250 / 8 / Article 75 | 2096 / 12 / Article 75 | 2304 / 10 / Article 75 |
| 34 | 2420 / 1 / Article 75 | 2405 / 3 / Article 75 | 2198 / 2 / Article 75 | 2553 / 3 / Article 75 |
| 35 | 3561 / 8 / Article 76 | 3469 / 4 / Article 76 | 3157 / 9 / Article 76 | 3575 / 5 / Article 76 |
| 36 | 2524 / 24 / Article 78 ; Article 77 | 2341 / 18 / Article 78 ; Article 77 | 2272 / 22 / Article 78 ; Article 77 | 2462 / 15 / Article 78 ; Article 77 |
| 37 | 2880 / 8 / Article 78 | 2853 / 7 / Article 78 | 2636 / 10 / Article 78 | 2946 / 7 / Article 78 |
| 38 | 2814 / 4 / Article 80 | 3011 / 9 / Article 80 | 2619 / 10 / Article 80 | 2810 / 6 / Article 80 |
| 39 | 2729 / 10 / Article 81 | 2643 / 7 / Article 81 | 2535 / 21 / Article 81 ; Article 80 | 2803 / 9 / Article 81 ; Article 80 |
| 40 | 3146 / 9 / Article 83 ; Article 82 ; Article 81 | 2969 / 5 / Article 81 ; Article 82 | 2892 / 17 / Article 83 ; Article 82 ; Article 81 | 3056 / 4 / Article 83 ; Article 82 |
| 41 | 2862 / 0 / Aucun repère candidat ; voir TXT | 2856 / 5 / Article 83 | 2532 / 1 / Article 84 | 2823 / 0 / Aucun repère candidat ; voir TXT |
| 42 | 2665 / 0 / Aucun repère candidat ; voir TXT | 2698 / 0 / Aucun repère candidat ; voir TXT | 2373 / 0 / Aucun repère candidat ; voir TXT | 2712 / 0 / Aucun repère candidat ; voir TXT |
| 43 | 2832 / 2 / Article 90 | 2723 / 3 / Article 90 | 2480 / 4 / Article 90 | 2777 / 3 / Article 90 |
| 44 | 2833 / 1 / Article 90 | 2792 / 1 / Article 90 | 2618 / 3 / Article 90a | 2806 / 0 / Aucun repère candidat ; voir TXT |
| 45 | 2520 / 3 / Article 91 | 2595 / 3 / Article 91 | 2360 / 4 / Article 91 | 2539 / 3 / Article 91 |
| 46 | 1980 / 1 / Article 91 | 2015 / 1 / Article 91 | 1794 / 1 / Article 91 | 2034 / 1 / Article 91 |
| 47 | 2416 / 3 / Article 93 | 2395 / 8 / Article 93 | 2250 / 5 / Article 93 | 2382 / 8 / Article 93 |
| 48 | 2234 / 3 / Article 94 | 2194 / 3 / Article 94 | 2098 / 4 / Article 94 | 2341 / 3 / Article 94 |
| 49 | 2160 / 1 / Article 95 | 2177 / 10 / Article 94 ; Article 95 | 2064 / 11 / Article 94 ; Article 95 | 2229 / 5 / Article 95 ; Article 94 |
| 50 | 2147 / 0 / Aucun repère candidat ; voir TXT | 2114 / 1 / Article 95 | 2037 / 3 / Article 100 ; Article 106a | 2144 / 0 / Aucun repère candidat ; voir TXT |
| 51 | 2267 / 0 / Aucun repère candidat ; voir TXT | 2263 / 0 / Aucun repère candidat ; voir TXT | 2072 / 4 / Article 107 ; Article 109 | 2183 / 1 / Article 109 |
| 52 | 2464 / 0 / Aucun repère candidat ; voir TXT | 2346 / 0 / Aucun repère candidat ; voir TXT | 2227 / 3 / Article 109 | 2380 / 0 / Aucun repère candidat ; voir TXT |
| 53 | 1728 / 0 / Aucun repère candidat ; voir TXT | 1693 / 0 / Aucun repère candidat ; voir TXT | 1513 / 0 / Aucun repère candidat ; voir TXT | 1783 / 0 / Aucun repère candidat ; voir TXT |
| 54 | 2912 / 1 / Article 113 | 2900 / 1 / Article 113 | 2727 / 2 / Article 113 ; Article 114 | 2978 / 1 / Article 113 |
| 55 | 2692 / 0 / Aucun repère candidat ; voir TXT | 2934 / 0 / Aucun repère candidat ; voir TXT | 2612 / 1 / Article 114 | 2714 / 0 / Aucun repère candidat ; voir TXT |
| 56 | 3511 / 1 / Article 116 | 3549 / 1 / Article 116 | 3187 / 1 / Article 116a | 3627 / 0 / Aucun repère candidat ; voir TXT |
| 57 | 2217 / 1 / Article 116 | 2288 / 1 / Article 116 | 2099 / 2 / Article 117 ; Article 116a | 2354 / 1 / Article 116a |
| 58 | 2221 / 0 / Aucun repère candidat ; voir TXT | 2375 / 1 / Article 119 | 2117 / 0 / Aucun repère candidat ; voir TXT | 2148 / 0 / Aucun repère candidat ; voir TXT |
| 59 | 1865 / 0 / Aucun repère candidat ; voir TXT | 1821 / 18 / Article 119 | 1771 / 22 / Article 119 | 1731 / 12 / Article 119 |
| 60 | 2642 / 0 / Aucun repère candidat ; voir TXT | 2617 / 0 / Aucun repère candidat ; voir TXT | 2468 / 1 / Article 120 | 2629 / 0 / Aucun repère candidat ; voir TXT |
| 61 | 2527 / 1 / Article 120 | 2415 / 2 / Article 120 | 2278 / 7 / Article 120 | 2543 / 0 / Aucun repère candidat ; voir TXT |
| 62 | 2433 / 0 / Aucun repère candidat ; voir TXT | 2358 / 0 / Aucun repère candidat ; voir TXT | 2127 / 1 / Article 122 | 2319 / 0 / Aucun repère candidat ; voir TXT |
| 63 | 2977 / 3 / Article 122 | 3013 / 1 / Article 122 | 2632 / 3 / Article 122 | 2857 / 1 / Article 122 |
| 64 | 2457 / 0 / Aucun repère candidat ; voir TXT | 2427 / 8 / Article 125 ; Article 123 | 2264 / 6 / Article 125 | 2302 / 5 / Article 125 |
| 65 | 3110 / 2 / Article 126 | 2918 / 7 / Article 126 ; Article 145 | 2895 / 7 / Article 126 ; Article 146 ; Article 145 | 3048 / 4 / Article 126 ; Article 145 |
| 66 | 3126 / 5 / Article 147 | 3223 / 5 / Article 147 | 2774 / 9 / Article 147 ; Article 147a | 3035 / 5 / Article 147a |
| 67 | 2450 / 28 / Article 147 ; Article 148 | 2518 / 26 / Article 147 ; Article 148 | 2350 / 29 / Article 147a ; Article 148 | 2702 / 12 / Article 147a ; Article 148 |
| 68 | 2463 / 14 / Article 148 | 2599 / 15 / Article 148 | 2357 / 14 / Article 148 | 2559 / 6 / Article 148 |
| 69 | 2658 / 16 / Article 148 ; Article 149 | 2809 / 16 / Article 148 ; Article 149 | 2542 / 18 / Article 148 ; Article 149 | 2716 / 9 / Article 149 ; Article 148 |
| 70 | 3198 / 16 / Article 149 | 3145 / 14 / Article 149 | 3025 / 16 / Article 149 | 3233 / 6 / Article 149 |
| 71 | 1773 / 12 / Article 151 | 1755 / 12 / Article 151 | 1696 / 14 / Article 151 | 1803 / 7 / Article 151 |
| 72 | 2201 / 0 / Aucun repère candidat ; voir TXT | 2136 / 0 / Aucun repère candidat ; voir TXT | 1906 / 0 / Aucun repère candidat ; voir TXT | 2203 / 0 / Aucun repère candidat ; voir TXT |
| 73 | 2251 / 0 / Aucun repère candidat ; voir TXT | 2153 / 0 / Aucun repère candidat ; voir TXT | 1980 / 0 / Aucun repère candidat ; voir TXT | 2099 / 0 / Aucun repère candidat ; voir TXT |
| 74 | 2908 / 0 / Aucun repère candidat ; voir TXT | 2805 / 0 / Aucun repère candidat ; voir TXT | 2623 / 0 / Aucun repère candidat ; voir TXT | 2810 / 0 / Aucun repère candidat ; voir TXT |
| 75 | 3174 / 2 / Article 152 | 3138 / 0 / Aucun repère candidat ; voir TXT | 3045 / 2 / Article 152 | 3079 / 1 / Article 152 |
| 76 | 3042 / 0 / Aucun repère candidat ; voir TXT | 3052 / 0 / Aucun repère candidat ; voir TXT | 2708 / 0 / Aucun repère candidat ; voir TXT | 3074 / 0 / Aucun repère candidat ; voir TXT |
| 77 | 2609 / 2 / Article 153 | 2319 / 2 / Article 153 | 2284 / 3 / Article 153 | 2545 / 2 / Article 153 |
| 78 | 2601 / 2 / Article 156 | 2486 / 2 / Article 156 | 2362 / 3 / Article 156 | 2562 / 2 / Article 156 |
| 79 | 3142 / 0 / Aucun repère candidat ; voir TXT | 3116 / 0 / Aucun repère candidat ; voir TXT | 2786 / 0 / Aucun repère candidat ; voir TXT | 3099 / 0 / Aucun repère candidat ; voir TXT |
| 80 | 2668 / 0 / Aucun repère candidat ; voir TXT | 2602 / 0 / Aucun repère candidat ; voir TXT | 2287 / 0 / Aucun repère candidat ; voir TXT | 2574 / 1 / Article 157 |
| 81 | 3070 / 0 / Aucun repère candidat ; voir TXT | 2742 / 0 / Aucun repère candidat ; voir TXT | 2741 / 0 / Aucun repère candidat ; voir TXT | 2860 / 0 / Aucun repère candidat ; voir TXT |
| 82 | 2049 / 21 / Article 160 ; Article 159 ; Article 161 | 1815 / 13 / Article 159 ; Article 160 ; Article 161 | 1804 / 24 / Article 159 ; Article 160 ; Article 161 | 1859 / 14 / Article 159 ; Article 160 ; Article 161 |
| 83 | 2590 / 4 / Article 162 | 2469 / 2 / Article 162 | 2432 / 4 / Article 162 | 2589 / 2 / Article 162 |
| 84 | 2433 / 6 / Article 163 | 2159 / 6 / Article 163 | 2192 / 9 / Article 163 | 2164 / 6 / Article 163 |
| 85 | 2949 / 2 / Article 164 | 2892 / 1 / Article 164 | 2662 / 2 / Article 164 | 2948 / 1 / Article 164 |
| 86 | 2311 / 0 / Aucun repère candidat ; voir TXT | 2341 / 0 / Aucun repère candidat ; voir TXT | 2040 / 0 / Aucun repère candidat ; voir TXT | 2451 / 1 / Article 164 |
| 87 | 2798 / 0 / Aucun repère candidat ; voir TXT | 2842 / 0 / Aucun repère candidat ; voir TXT | 2565 / 0 / Aucun repère candidat ; voir TXT | 2853 / 0 / Aucun repère candidat ; voir TXT |
| 88 | 3579 / 11 / Article 166 | 3399 / 12 / Article 166 | 3332 / 13 / Article 166a | 3354 / 5 / Article 166a |
| 89 | 2863 / 0 / Aucun repère candidat ; voir TXT | 2915 / 0 / Aucun repère candidat ; voir TXT | 2641 / 0 / Aucun repère candidat ; voir TXT | 2950 / 0 / Aucun repère candidat ; voir TXT |
| 90 | 2697 / 1 / Article 167 | 2851 / 7 / Article 167 | 2368 / 3 / Article 167a ; Article 167 | 2810 / 2 / Article 167a ; Article 167 |
| 91 | 2551 / 6 / Article 168 | 2548 / 8 / Article 168 | 2350 / 9 / Article 168 | 2719 / 6 / Article 168 |
| 92 | 2324 / 0 / Aucun repère candidat ; voir TXT | 2397 / 0 / Aucun repère candidat ; voir TXT | 2172 / 0 / Aucun repère candidat ; voir TXT | 2438 / 0 / Aucun repère candidat ; voir TXT |
| 93 | 2354 / 0 / Aucun repère candidat ; voir TXT | 2568 / 0 / Aucun repère candidat ; voir TXT | 2442 / 0 / Aucun repère candidat ; voir TXT | 2738 / 0 / Aucun repère candidat ; voir TXT |
| 94 | 2970 / 0 / Aucun repère candidat ; voir TXT | 2711 / 2 / Article 168 | 2491 / 1 / Article 172b | 2695 / 4 / Article 172b |
| 95 | 3032 / 0 / Aucun repère candidat ; voir TXT | 2911 / 0 / Aucun repère candidat ; voir TXT | 2773 / 0 / Aucun repère candidat ; voir TXT | 2928 / 0 / Aucun repère candidat ; voir TXT |
| 96 | 3328 / 4 / Article 173 | 3270 / 4 / Article 173 | 3009 / 5 / Article 173 | 3329 / 2 / Article 173 |
| 97 | 2170 / 2 / Article 174 | 2094 / 2 / Article 174 | 2033 / 2 / Article 174 | 1953 / 2 / Article 174 |
| 98 | 1779 / 5 / Article 176 | 1793 / 5 / Article 176 | 1591 / 6 / Article 176 | 1720 / 4 / Article 176 |
| 99 | 1813 / 15 / Article 176 | 1714 / 14 / Article 176 | 1618 / 15 / Article 176 | 1728 / 13 / Article 176 |
| 100 | 2373 / 1 / Article 177 | 2541 / 2 / Article 177 ; Article 178 | 2234 / 1 / Article 177 | 2444 / 1 / Article 177 |
| 101 | 2336 / 9 / Article 181 | 2351 / 8 / Article 181 | 2112 / 10 / Article 181 | 2179 / 7 / Article 181 |
| 102 | 2971 / 14 / Article 182 | 2955 / 14 / Article 182 | 2857 / 15 / Article 182 | 2931 / 13 / Article 182 |
| 103 | 2423 / 0 / Aucun repère candidat ; voir TXT | 2336 / 0 / Aucun repère candidat ; voir TXT | 2102 / 0 / Aucun repère candidat ; voir TXT | 2232 / 0 / Aucun repère candidat ; voir TXT |
| 104 | 2601 / 3 / Article 185 | 2779 / 2 / Article 185 | 2325 / 2 / Article 185 | 2718 / 2 / Article 185 |
| 105 | 2397 / 0 / Aucun repère candidat ; voir TXT | 2196 / 1 / Article 187 | 2112 / 0 / Aucun repère candidat ; voir TXT | 2270 / 0 / Aucun repère candidat ; voir TXT |
| 106 | 2542 / 11 / Article 189 ; Article 190 | 2400 / 4 / Article 189 ; Article 190 | 2141 / 13 / Article 189 ; Article 190 | 2224 / 2 / Article 189 |
| 107 | 2666 / 12 / Article 190 | 2615 / 11 / Article 190 ; Article 191 | 2527 / 10 / Article 190 ; Article 191 | 2717 / 6 / Article 190 |
| 108 | 2680 / 3 / Article 191 | 2656 / 3 / Article 191 | 2497 / 3 / Article 193a | 2531 / 1 / Article 193a |
| 109 | 2644 / 15 / Article 195 | 2390 / 15 / Article 195 | 2247 / 16 / Article 195 | 2357 / 12 / Article 195 |
| 110 | 2515 / 10 / Article 205 | 2323 / 8 / Article 205 | 2201 / 10 / Article 205 | 2287 / 8 / Article 205 |
| 111 | 2943 / 0 / Aucun repère candidat ; voir TXT | 2800 / 0 / Aucun repère candidat ; voir TXT | 2472 / 0 / Aucun repère candidat ; voir TXT | 2884 / 0 / Aucun repère candidat ; voir TXT |
| 112 | 3498 / 1 / Article 210 | 3152 / 1 / Article 210 | 2991 / 2 / Article 210 | 3418 / 1 / Article 210 |
| 113 | 2811 / 0 / Aucun repère candidat ; voir TXT | 2829 / 0 / Aucun repère candidat ; voir TXT | 2452 / 0 / Aucun repère candidat ; voir TXT | 2880 / 0 / Aucun repère candidat ; voir TXT |
| 114 | 2831 / 0 / Aucun repère candidat ; voir TXT | 2856 / 0 / Aucun repère candidat ; voir TXT | 2526 / 0 / Aucun repère candidat ; voir TXT | 2853 / 0 / Aucun repère candidat ; voir TXT |
| 115 | 2198 / 0 / Aucun repère candidat ; voir TXT | 2152 / 1 / Article 210 | 1946 / 0 / Aucun repère candidat ; voir TXT | 2186 / 0 / Aucun repère candidat ; voir TXT |
| 116 | 2186 / 2 / Article 214 ; Article 213 | 2054 / 1 / Article 214 | 1810 / 1 / Article 214 | 2067 / 0 / Aucun repère candidat ; voir TXT |
| 117 | 3079 / 4 / Article 216 | 3118 / 2 / Article 216 | 2859 / 5 / Article 216 | 3040 / 3 / Article 216 |
| 118 | 2373 / 0 / Aucun repère candidat ; voir TXT | 2284 / 0 / Aucun repère candidat ; voir TXT | 2090 / 1 / Article 216 | 2253 / 0 / Aucun repère candidat ; voir TXT |
| 119 | 1206 / 4 / Article 218 | 1267 / 1 / Article 218 | 1120 / 1 / Article 218 | 1111 / 1 / Article 218 |
| 120 | 3021 / 0 / Aucun repère candidat ; voir TXT | 2862 / 0 / Aucun repère candidat ; voir TXT | 2634 / 0 / Aucun repère candidat ; voir TXT | 2923 / 1 / Article 219 |
| 121 | 2357 / 6 / Article 220 ; Article 219 | 2139 / 7 / Article 220 ; Article 219 | 1979 / 7 / Article 220 | 2435 / 6 / Article 220 ; Article 219 |
| 122 | 2400 / 11 / Article 220 | 2291 / 10 / Article 220 | 2231 / 11 / Article 220 | 2437 / 9 / Article 220 |
| 123 | 3104 / 0 / Aucun repère candidat ; voir TXT | 2858 / 1 / Article 222 | 2588 / 0 / Aucun repère candidat ; voir TXT | 2937 / 0 / Aucun repère candidat ; voir TXT |
| 124 | 2426 / 0 / Aucun repère candidat ; voir TXT | 2360 / 0 / Aucun repère candidat ; voir TXT | 2279 / 0 / Aucun repère candidat ; voir TXT | 2464 / 0 / Aucun repère candidat ; voir TXT |
| 125 | 2503 / 0 / Aucun repère candidat ; voir TXT | 2322 / 0 / Aucun repère candidat ; voir TXT | 2200 / 0 / Aucun repère candidat ; voir TXT | 2409 / 0 / Aucun repère candidat ; voir TXT |
| 126 | 2326 / 0 / Aucun repère candidat ; voir TXT | 2442 / 0 / Aucun repère candidat ; voir TXT | 2098 / 0 / Aucun repère candidat ; voir TXT | 2331 / 0 / Aucun repère candidat ; voir TXT |
| 127 | 2252 / 0 / Aucun repère candidat ; voir TXT | 2074 / 0 / Aucun repère candidat ; voir TXT | 1955 / 0 / Aucun repère candidat ; voir TXT | 2120 / 0 / Aucun repère candidat ; voir TXT |
| 128 | 2336 / 5 / Article 225 | 2294 / 1 / Article 225 | 2127 / 5 / Article 225 | 2307 / 4 / Article 225 |
| 129 | 2186 / 0 / Aucun repère candidat ; voir TXT | 2209 / 1 / Article 227 | 2153 / 0 / Aucun repère candidat ; voir TXT | 2267 / 0 / Aucun repère candidat ; voir TXT |
| 130 | 2533 / 3 / Article 230 | 2391 / 2 / Article 230 | 2103 / 5 / Article 230 | 2311 / 2 / Article 230 |
| 131 | 2108 / 0 / Aucun repère candidat ; voir TXT | 2141 / 1 / Article 230 | 1950 / 0 / Aucun repère candidat ; voir TXT | 2087 / 0 / Aucun repère candidat ; voir TXT |
| 132 | 899 / 0 / Aucun repère candidat ; voir TXT | 925 / 1 / Article 232 | 817 / 0 / Aucun repère candidat ; voir TXT | 817 / 0 / Aucun repère candidat ; voir TXT |
| 133 | 2183 / 65 / Annex I / Part I ; Annex I / Part I / NC 1004 ; Annex I / Part I / NC 1102 90 10 ; +11 autres repères (CSV) | 2054 / 56 / Annex I / Part I ; Annex I / Part I / NC 1103 ; Annex I / Part I / NC 1103 11 ; +8 autres repères (CSV) | 1832 / 58 / Annex I / Part I ; Annex I / Part I / NC 1102 ; Annex I / Part I / NC 1104 ; +11 autres repères (CSV) | 2060 / 53 / Annex I / Part I ; Annex I / Part I / NC 1104 ; Annex I / Part I / NC 1103 ; +10 autres repères (CSV) |
| 134 | 2565 / 75 / Annex I / Part I / NC 1108 11 00 ; Annex I / Part I / NC 1108 12 00 ; Annex I / Part I ; +10 autres repères (CSV) | 2483 / 56 / Annex I / Part I / NC 1108 13 00 ; Annex I / Part I / NC 2303 ; Annex I / Part I / NC 2303 10 ; +12 autres repères (CSV) | 2408 / 62 / Annex I / Part I / NC 2302 ; Annex I / Part I / NC 2303 30 00 ; Annex I / Part I ; +13 autres repères (CSV) | 2545 / 55 / Annex I / Part I ; Annex I / Part I / NC 1702 30 ; Annex I / Part I / NC 1702 40 ; +10 autres repères (CSV) |
| 135 | 2113 / 39 / Annex I / Part II / NC 1103 20 50 ; Annex I / Part I ; Annex I / Part II ; +8 autres repères (CSV) | 2065 / 35 / Annex I / Part I / NC 2309 ; Annex I / Part I ; Annex I / Part I / NC 2308 00 40 ; +9 autres repères (CSV) | 1850 / 43 / Annex I / Part I ; Annex I / Part I / NC 2308 00 40 ; Annex I / Part II / NC 1104 19 91 ; +8 autres repères (CSV) | 1966 / 39 / Annex I / Part II ; Annex I / Part I ; Annex I / Part I / NC 2308 00 40 ; +9 autres repères (CSV) |
| 136 | 2018 / 44 / Annex I / Part III ; Annex I / Part IV ; Annex I / Part III / NC 2106 90 59 ; +1 autres repères (CSV) | 2119 / 47 / Annex I / Part III ; Annex I / Part IV ; Annex I / Part III / NC 1702 90 71 ; +2 autres repères (CSV) | 1808 / 53 / Annex I / Part III / NC 2303 20 ; Annex I / Part III / NC 1701 ; Annex I / Part III / NC 1702 90 71 ; +10 autres repères (CSV) | 1934 / 38 / Annex I / Part III ; Annex I / Part IV ; Annex I / Part IV / NC 1214 90 90 ; +2 autres repères (CSV) |
| 137 | 1601 / 56 / Annex I / Part V ; Annex I / Part V / NC 0713 39 00 ; Annex I / Part V / NC 0713 50 00 ; +8 autres repères (CSV) | 1351 / 73 / Annex I / Part V ; Annex I / Part V / NC 0713 35 00 ; Annex I / Part V / NC 0713 60 00 ; +9 autres repères (CSV) | 1305 / 61 / Annex I / Part V ; Annex I / Part V / NC 0713 34 00 ; Annex I / Part V / NC 0713 31 00 ; +9 autres repères (CSV) | 1346 / 39 / Annex I / Part V ; Annex I / Part V / NC 0713 34 00 ; Annex I / Part V / NC 0713 10 10 ; +7 autres repères (CSV) |
| 138 | 2342 / 53 / Annex I / Part V ; Annex I / Part V / NC 1202 30 00 ; Annex I / Part VII ; +12 autres repères (CSV) | 2270 / 49 / Annex I / Part V ; Annex I / Part V / NC 1205 90 00 ; Annex I / Part VII ; +4 autres repères (CSV) | 2087 / 56 / Annex I / Part VI ; Annex I / Part VII ; Annex I / Part V ; +14 autres repères (CSV) | 2215 / 40 / Annex I / Part VII ; Annex I / Part V ; Annex I / Part VI / NC 1210 ; +10 autres repères (CSV) |
| 139 | 2333 / 77 / Annex I / Part IX ; Annex I / Part IX / NC 0804 40 00 ; Annex I / Part VIII ; +13 autres repères (CSV) | 2390 / 74 / Annex I / Part IX ; Annex I / Part IX / NC 0804 40 00 ; Annex I / Part IX / NC 0804 30 00 ; +14 autres repères (CSV) | 2145 / 87 / Annex I / Part IX / NC 0804 40 00 ; Annex I / Part IX ; Annex I / Part VIII ; +13 autres repères (CSV) | 2408 / 83 / Annex I / Part IX ; Annex I / Part VIII ; Annex I / Part IX / NC 0804 20 10 ; +13 autres repères (CSV) |
| 140 | 3148 / 81 / Annex I / Part IX ; Annex I / Part X ; Annex I / Part X / NC 0811 ; +8 autres repères (CSV) | 3150 / 71 / Annex I / Part X ; Annex I / Part IX ; Annex I / Part IX / NC 0810 ; +13 autres repères (CSV) | 2818 / 87 / Annex I / Part IX / NC 0808 ; Annex I / Part IX ; Annex I / Part X ; +13 autres repères (CSV) | 3164 / 85 / Annex I / Part IX ; Annex I / Part IX / NC 0809 ; Annex I / Part X ; +9 autres repères (CSV) |
| 141 | 3663 / 83 / Annex I / Part X ; Annex I / Part X / NC 2003 ; Annex I / Part X / NC 2008 ; +2 autres repères (CSV) | 3517 / 81 / Annex I / Part X ; Annex I / Part X / NC 2009 ; Annex I / Part X / NC 2003 ; +1 autres repères (CSV) | 3250 / 83 / Annex I / Part X ; Annex I / Part X / NC 2008 ; Annex I / Part X / NC 2009 ; +4 autres repères (CSV) | 3433 / 57 / Annex I / Part X ; Annex I / Part X / NC 2009 ; Annex I / Part X / NC 2008 ; +2 autres repères (CSV) |
| 142 | 1711 / 35 / Annex I / Part XI / NC 2006 00 99 ; Annex I / Part XI / NC 0812 90 98 ; Annex I / Part XI / NC 0803 90 10 ; +6 autres repères (CSV) | 1782 / 43 / Annex I / Part XII ; Annex I / Part XI ; Annex I / Part XI / NC 2007 10 99 ; +5 autres repères (CSV) | 1612 / 45 / Annex I / Part XI ; Annex I / Part XI / NC 2006 00 99 ; Annex I / Part XI / NC 0812 90 98 ; +7 autres repères (CSV) | 1726 / 36 / Annex I / Part XI ; Annex I / Part XI / NC 2007 10 99 ; Annex I / Part XI / NC 0813 50 99 ; +1 autres repères (CSV) |
| 143 | 2242 / 48 / Annex I / Part XV / NC 0206 10 98 ; Annex I / Part XV ; Annex I / Part XV / NC 0210 99 90 ; +8 autres repères (CSV) | 2151 / 63 / Annex I / Part XV ; Annex I / Part XII ; Annex I / Part XV / NC 0210 99 90 ; +7 autres repères (CSV) | 2040 / 67 / Annex I / Part XV ; Annex I / Part XV / NC 0210 99 90 ; Annex I / Part XV / NC 0201 ; +7 autres repères (CSV) | 2367 / 57 / Annex I / Part XV ; Annex I / Part XV / NC 1602 50 10 ; Annex I / Part XV / NC 1602 90 61 ; +10 autres repères (CSV) |
| 144 | 2872 / 58 / Annex I / Part XV / NC 0210 99 59 ; Annex I / Part XVI ; Annex I / Part XV ; +1 autres repères (CSV) | 2962 / 78 / Annex I / Part XV ; Annex I / Part XVI ; Annex I / Part XVI / NC 2309 10 ; +1 autres repères (CSV) | 2612 / 95 / Annex I / Part XV ; Annex I / Part XVI / NC 0405 ; Annex I / Part XVI / NC 0406 ; +9 autres repères (CSV) | 3027 / 62 / Annex I / Part XV / NC 1502 10 90 ; Annex I / Part XV ; Annex I / Part XV / NC 0210 99 59 ; +2 autres repères (CSV) |
| 145 | 2817 / 58 / Annex I / Part XVII / NC 0206 ; Annex I / Part XVII ; Annex I / Part XVIII / NC 0206 80 99 ; +13 autres repères (CSV) | 2611 / 58 / Annex I / Part XVII ; Annex I / Part XVII / NC 1602 20 90 ; Annex I / Part XVII / NC 1602 90 10 ; +13 autres repères (CSV) | 2273 / 69 / Annex I / Part XVII ; Annex I / Part XVIII ; Annex I / Part XVII / NC 0206 ; +17 autres repères (CSV) | 2760 / 59 / Annex I / Part XVII ; Annex I / Part XVIII ; Annex I / Part XVII / NC 0210 ; +12 autres repères (CSV) |
| 146 | 1955 / 32 / Annex I / Part XVIII / NC 0206 90 99 ; Annex I / Part XVIII / NC 0210 99 85 ; Annex I / Part XX ; +3 autres repères (CSV) | 1905 / 28 / Annex I / Part XVIII / NC 0206 90 99 ; Annex I / Part XVIII / NC 0210 99 85 ; Annex I / Part XVIII ; +3 autres repères (CSV) | 1754 / 38 / Annex I / Part XVIII ; Annex I / Part XIX ; Annex I / Part XX ; +6 autres repères (CSV) | 1976 / 32 / Annex I / Part XIX ; Annex I / Part XVIII / NC 1502 90 90 ; Annex I / Part XVIII ; +3 autres repères (CSV) |
| 147 | 1959 / 20 / Annex I / Part XXI / NC 2207 20 00 ; Annex I / Part XXI / NC 2207 10 00 ; Annex I / Part XXII / NC 1521 90 ; +6 autres repères (CSV) | 2012 / 29 / Annex I / Part XXII ; Annex I / Part XXII / NC 0410 00 00 ; Annex I / Part XXI ; +8 autres repères (CSV) | 1872 / 21 / Annex I / Part XXII ; Annex I / Part XXI / NC 2207 20 00 ; Annex I / Part XX / NC 1602 20 10 ; +6 autres repères (CSV) | 2042 / 18 / Annex I / Part XXII ; Annex I / Part XXIII ; Annex I / Part XXI / NC 2207 10 00 ; +5 autres repères (CSV) |
| 148 | 1707 / 18 / Annex I / Part XXIV / NC 0102 ; Annex I / Part XXIV / NC 0103 ; Annex I / Part XXIV / NC 0101 ; +10 autres repères (CSV) | 1517 / 21 / Annex I / Part XXIV / NC 0101 30 00 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 0101 21 00 ; +10 autres repères (CSV) | 1438 / 23 / Annex I / Part XXIV / NC 0101 30 00 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 0203 11 ; +9 autres repères (CSV) | 1511 / 21 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0101 30 00 ; Annex I / Part XXIV / NC 0205 00 ; +9 autres repères (CSV) |
| 149 | 2004 / 28 / Annex I / Part XXIV / NC 0206 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 0206 29 10 ; +8 autres repères (CSV) | 2046 / 33 / Annex I / Part XXIV / NC 0208 ; Annex I / Part XXIV / NC 0206 29 10 ; Annex I / Part XXIV / NC 0206 10 10 ; +6 autres repères (CSV) | 1752 / 43 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0210 12 ; Annex I / Part XXIV / NC 0206 ; +8 autres repères (CSV) | 1959 / 24 / Annex I / Part XXIV / NC 0208 ; Annex I / Part XXIV / NC 0210 12 ; Annex I / Part XXIV ; +2 autres repères (CSV) |
| 150 | 2051 / 27 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0410 00 00 ; Annex I / Part XXIV / NC 0511 ; +5 autres repères (CSV) | 2176 / 29 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0410 00 00 ; Annex I / Part XXIV / NC 0408 11 ; +9 autres repères (CSV) | 1885 / 30 / Annex I / Part XXIV / NC 0511 ; Annex I / Part XXIV / NC 0407 ; Annex I / Part XXIV / NC 0408 ; +6 autres repères (CSV) | 2093 / 29 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0410 00 00 ; Annex I / Part XXIV / NC 0511 10 00 ; +9 autres repères (CSV) |
| 151 | 2487 / 55 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0713 50 00 ; Annex I / Part XXIV / NC 0713 33 ; +11 autres repères (CSV) | 2336 / 61 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0709 60 95 ; Annex I / Part XXIV / NC 0713 31 00 ; +10 autres repères (CSV) | 2210 / 75 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0713 31 00 ; Annex I / Part XXIV / NC 0713 50 00 ; +10 autres repères (CSV) | 2322 / 59 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0713 32 00 ; Annex I / Part XXIV / NC 0713 31 00 ; +10 autres repères (CSV) |
| 152 | 2593 / 100 / Annex I / Part XXIV / NC 1108 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 0906 ; +14 autres repères (CSV) | 2561 / 81 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0909 ; Annex I / Part XXIV / NC 0802 80 00 ; +12 autres repères (CSV) | 2298 / 101 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0906 ; Annex I / Part XXIV / NC 0907 ; +12 autres repères (CSV) | 2460 / 63 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0909 ; Annex I / Part XXIV / NC 0802 80 00 ; +14 autres repères (CSV) |
| 153 | 3148 / 76 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1214 90 10 ; Annex I / Part XXIV / NC 1212 ; +10 autres repères (CSV) | 3034 / 64 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1508 ; Annex I / Part XXIV / NC 1214 ; +8 autres repères (CSV) | 2737 / 83 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1213 00 00 ; Annex I / Part XXIV / NC 1504 ; +6 autres repères (CSV) | 2931 / 66 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1508 ; Annex I / Part XXIV / NC 1504 ; +8 autres repères (CSV) |
| 154 | 3061 / 43 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1801 00 00 ; Annex I / Part XXIV / NC 1802 00 00 ; +9 autres repères (CSV) | 3065 / 41 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1802 00 00 ; Annex I / Part XXIV / NC 1516 ; +9 autres repères (CSV) | 2674 / 44 / Annex I / Part XXIV / NC 1516 ; Annex I / Part XXIV / NC 1801 00 00 ; Annex I / Part XXIV / NC 1802 00 00 ; +8 autres repères (CSV) | 2936 / 37 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1602 ; Annex I / Part XXIV / NC 1801 00 00 ; +10 autres repères (CSV) |
| 155 | 3047 / 68 / Annex I / Part XXIV / NC 2309 10 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 2001 90 20 ; +8 autres repères (CSV) | 2913 / 63 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 2001 ; Annex I / Part XXIV / NC 2001 90 20 ; +3 autres repères (CSV) | 2800 / 69 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 2301 ; Annex I / Part XXIV / NC 2301 10 00 ; +3 autres repères (CSV) | 2862 / 57 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 2001 90 20 ; Annex I / Part XXIV / NC 2005 99 10 ; +10 autres repères (CSV) |
| 156 | 3683 / 35 / Annex I / Part XXIV / NC 0901 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 0101 29 10 ; +8 autres repères (CSV) | 3468 / 27 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 1212 94 00 ; Annex I / Part XXIV / NC 4501 ; +4 autres repères (CSV) | 3119 / 35 / Annex I / Part XXIV ; Annex I / Part XXIV / NC 0901 ; Annex I / Part XXIV / NC 0210 99 10 ; +5 autres repères (CSV) | 3253 / 23 / Annex I / Part XXIV / NC 0511 99 10 ; Annex I / Part XXIV ; Annex I / Part XXIV / NC 0205 00 ; +7 autres repères (CSV) |
| 157 | 2340 / 29 / Annex II / Part I ; Annex II / Part I / 1a ; Annex II / Part I / 1b ; +5 autres repères (CSV) | 2388 / 24 / Annex II / Part I / 1a ; Annex II / Part I / 1b ; Annex II / Part I / 1c ; +5 autres repères (CSV) | 2278 / 28 / Annex II / Part I ; Annex II / Part I / 1a ; Annex II / Part I / 1b ; +5 autres repères (CSV) | 2468 / 31 / Annex II / Part I ; Annex II / Part I / 1a ; Annex II / Part I / 1b ; +5 autres repères (CSV) |
| 158 | 2865 / 21 / Annex II / Part I ; Annex II / Part I / 3 | 2826 / 21 / Annex II / Part I ; Annex II / Part I / 3 | 2646 / 12 / Annex II / Part I ; Annex II / Part I / 3 | 2858 / 18 / Annex II / Part I ; Annex II / Part I / 3 |
| 159 | 3063 / 28 / Annex II / Part II ; Annex II / Part I ; Annex II / Part III ; +7 autres repères (CSV) | 2970 / 33 / Annex II / Part I ; Annex II / Part III ; Annex II / Part II ; +7 autres repères (CSV) | 2838 / 26 / Annex II / Part I ; Annex II / Part III ; Annex II / Part II ; +7 autres repères (CSV) | 3022 / 22 / Annex II / Part II ; Annex II / Part III ; Annex II / Part I ; +7 autres repères (CSV) |
| 160 | 2893 / 32 / Annex II / Part IV ; Annex II / Part III ; Annex II / Part III / 4 ; +5 autres repères (CSV) | 2945 / 35 / Annex II / Part IV ; Annex II / Part III ; Annex II / Part III / 4 ; +5 autres repères (CSV) | 2849 / 38 / Annex II / Part IV ; Annex II / Part III ; Annex II / Part III / 4 ; +5 autres repères (CSV) | 3114 / 25 / Annex II / Part IV ; Annex II / Part III ; Annex II / Part III / 4 ; +5 autres repères (CSV) |
| 161 | 2348 / 15 / Annex II / Part IV ; Annex II / Part IV / 10 ; Annex II / Part IV / 11 ; +2 autres repères (CSV) | 2429 / 15 / Annex II / Part IV ; Annex II / Part IV / 10 ; Annex II / Part IV / 11 ; +2 autres repères (CSV) | 2363 / 17 / Annex II / Part IV ; Annex II / Part IV / 10 ; Annex II / Part IV / 11 ; +2 autres repères (CSV) | 2620 / 15 / Annex II / Part IV ; Annex II / Part IV / 10 ; Annex II / Part IV / 11 ; +2 autres repères (CSV) |
| 162 | 2299 / 42 / Annex II / Part V ; Annex II / Part VIII ; Annex II / Part VII ; +1 autres repères (CSV) | 2077 / 39 / Annex II / Part VIII ; Annex II / Part VII ; Annex II / Part V ; +1 autres repères (CSV) | 2084 / 46 / Annex II / Part VII ; Annex II / Part V ; Annex II / Part VIII ; +1 autres repères (CSV) | 2144 / 31 / Annex II / Part VIII ; Annex II / Part VII ; Annex II / Part VI ; +1 autres repères (CSV) |
| 163 | 733 / 8 / Annex II / Part IX | 719 / 10 / Annex II / Part IX | 695 / 10 / Annex II / Part IX | 672 / 9 / Annex II / Part IX |
| 164 | 1849 / 18 / Annex III | 1938 / 24 / Annex III | 1753 / 18 / Annex III | 1998 / 19 / Annex III |
| 165 | 1161 / 6 / Annex III | 1211 / 12 / Annex III | 1157 / 4 / Annex III | 1209 / 5 / Annex III |
| 166 | 2047 / 8 / Annex IV | 2058 / 7 / Annex IV | 1860 / 8 / Annex IV | 2132 / 15 / Annex IV |
| 167 | 2828 / 6 / Annex IV | 2658 / 13 / Annex IV | 2542 / 7 / Annex IV | 2724 / 8 / Annex IV |
| 168 | 2313 / 8 / Annex IV | 2118 / 11 / Annex IV | 2022 / 13 / Annex IV | 2340 / 15 / Annex IV |
| 169 | 784 / 2 / Annex IV | 824 / 5 / Annex IV | 679 / 4 / Annex IV | 801 / 4 / Annex IV |
| 170 | 563 / 15 / Annex V / Category I / 3 ; Annex V ; Annex V / Category II ; +2 autres repères (CSV) | 544 / 8 / Annex V / Category I / 3 ; Annex V / Category II ; Annex V / Category I / 2 ; +2 autres repères (CSV) | 465 / 15 / Annex V / Category I / 2 ; Annex V / Category I / 1 ; Annex V / Category II ; +2 autres repères (CSV) | 523 / 8 / Annex V / Category I / 2 ; Annex V / Category I / 1 ; Annex V / Category II ; +2 autres repères (CSV) |
| 171 | 2798 / 23 / Annex VII / Part I ; Annex VII / Part I / III / V | 2788 / 30 / Annex VII / Part I ; Annex VII ; Annex VII / Part I / III / V | 2722 / 23 / Annex VII / Part I | 2751 / 18 / Annex VII / Part I ; Annex VII / Part I / III / V |
| 172 | 1098 / 15 / Annex VII / Part I ; Annex VII / Part I / III / V ; Annex VII / Part I / III / Z | 1101 / 9 / Annex VII / Part I ; Annex VII / Part I / III / V ; Annex VII / Part I / III / Z | 1062 / 5 / Annex VII / Part I ; Annex VII / Part I / III / V | 1102 / 11 / Annex VII / Part I ; Annex VII / Part I / III / V ; Annex VII / Part I / III / Z |
| 173 | 1722 / 9 / Annex VII / Part I | 1704 / 10 / Annex VII / Part I ; Annex VII / Part I / III / Z | 1615 / 11 / Annex VII / Part I ; Annex VII / Part I / III / Z | 1719 / 7 / Annex VII / Part I |
| 174 | 2668 / 7 / Annex VII / Part I | 2537 / 11 / Annex VII / Part I | 2473 / 9 / Annex VII / Part I | 2458 / 3 / Annex VII / Part I |
| 175 | 3466 / 6 / Annex VII / Part I ; Annex VII / Part II ; Annex VII / Part II / 1 | 3462 / 13 / Annex VII / Part II ; Annex VII / Part I ; Annex VII / Part II / 1 | 3411 / 11 / Annex VII / Part II ; Annex VII / Part I ; Annex VII / Part II / 1 | 3512 / 9 / Annex VII / Part I ; Annex VII / Part II ; Annex VII / Part II / 1 |
| 176 | 3274 / 26 / Annex VII / Part II ; Annex VII / Part II / 2 ; Annex VII / Part II / 3 | 3208 / 30 / Annex VII / Part II ; Annex VII / Part II / 2 ; Annex VII / Part II / 3 | 2919 / 30 / Annex VII / Part II ; Annex VII / Part II / 2 ; Annex VII / Part II / 3 | 3166 / 25 / Annex VII / Part II ; Annex VII / Part II / 2 ; Annex VII / Part II / 3 |
| 177 | 2251 / 30 / Annex VII / Part II ; Annex VII / Part II / 4 | 2395 / 38 / Annex VII / Part II ; Annex VII / Part II / 4 | 2336 / 29 / Annex VII / Part II ; Annex VII / Part II / 4 | 2477 / 25 / Annex VII / Part II ; Annex VII / Part II / 4 |
| 178 | 2334 / 26 / Annex VII / Part II ; Annex VII / Part II / 5 ; Annex VII / Part II / 6 ; +1 autres repères (CSV) | 2225 / 24 / Annex VII / Part II ; Annex VII / Part II / 5 ; Annex VII / Part II / 6 ; +1 autres repères (CSV) | 2149 / 32 / Annex VII / Part II ; Annex VII / Part II / 5 ; Annex VII / Part II / 6 ; +1 autres repères (CSV) | 2255 / 23 / Annex VII / Part II ; Annex VII / Part II / 5 ; Annex VII / Part II / 6 ; +1 autres repères (CSV) |
| 179 | 2759 / 34 / Annex VII / Part II ; Annex VII / Part II / 10 ; Annex VII / Part II / 11 ; +3 autres repères (CSV) | 2669 / 43 / Annex VII / Part II ; Annex VII / Part II / 10 ; Annex VII / Part II / 11 ; +3 autres repères (CSV) | 2494 / 45 / Annex VII / Part II ; Annex VII / Part II / 10 ; Annex VII / Part II / 11 ; +3 autres repères (CSV) | 2606 / 37 / Annex VII / Part II ; Annex VII / Part II / 10 ; Annex VII / Part II / 11 ; +3 autres repères (CSV) |
| 180 | 2510 / 17 / Annex VII / Part II ; Annex VII / Part II / 13 ; Annex VII / Part II / 14 | 2514 / 27 / Annex VII / Part II ; Annex VII / Part II / 13 ; Annex VII / Part II / 14 | 2384 / 21 / Annex VII / Part II ; Annex VII / Part II / 13 ; Annex VII / Part II / 14 | 2501 / 10 / Annex VII / Part II ; Annex VII / Part II / 13 ; Annex VII / Part II / 14 |
| 181 | 2124 / 21 / Annex VII / Part II ; Annex VII / Part II / 15 ; Annex VII / Part II / 16 ; +1 autres repères (CSV) | 2035 / 26 / Annex VII / Part II ; Annex VII / Part II / 15 ; Annex VII / Part II / 16 ; +1 autres repères (CSV) | 1970 / 22 / Annex VII / Part II ; Annex VII / Part II / 15 ; Annex VII / Part II / 16 ; +1 autres repères (CSV) | 2093 / 18 / Annex VII / Part II ; Annex VII / Part II / 15 ; Annex VII / Part II / 16 ; +1 autres repères (CSV) |
| 182 | 2292 / 37 / Annex VII / Part III ; Annex VII / Part III / 2(a)(i) ; Annex VII / Part III / 2(a)(ii) ; +14 autres repères (CSV) | 2410 / 38 / Annex VII / Part II ; Annex VII / Part III / 2(a)(i) ; Annex VII / Part III / 2(a)(ii) ; +14 autres repères (CSV) | 1954 / 44 / Annex VII / Part III ; Annex VII / Part III / 2(a)(i) ; Annex VII / Part III / 2(a)(ii) ; +14 autres repères (CSV) | 2300 / 34 / Annex VII / Part III ; Annex VII / Part III / 2(a)(i) ; Annex VII / Part III / 2(a)(ii) ; +14 autres repères (CSV) |
| 183 | 3005 / 17 / Annex VII / Part IV ; Annex VII / Part III | 2970 / 14 / Annex VII / Part II ; Annex VII / Part IV | 2795 / 19 / Annex VII / Part IV ; Annex VII / Part III | 2931 / 15 / Annex VII / Part IV ; Annex VII / Part III |
| 184 | 3169 / 47 / Annex VII / Part IV | 2901 / 50 / Annex VII / Part IV | 2718 / 58 / Annex VII / Part IV | 2901 / 51 / Annex VII / Part IV |
| 185 | 2870 / 27 / Annex VII / Part V ; Annex VII / Part IV | 2725 / 27 / Annex VII / Part V ; Annex VII / Part IV | 2518 / 31 / Annex VII / Part V ; Annex VII / Part IV | 2785 / 24 / Annex VII / Part V ; Annex VII / Part IV |
| 186 | 2538 / 28 / Annex VII / Part VI ; Annex VII / Part V | 2418 / 25 / Annex VII / Part V ; Annex VII / Part VI | 2242 / 34 / Annex VII / Part VI ; Annex VII / Part V | 2456 / 18 / Annex VII / Part VI ; Annex VII / Part V |
| 187 | 2651 / 15 / Annex VII / Part VII ; Annex VII / Part VI | 2565 / 12 / Annex VII / Part VI ; Annex VII / Part VII | 2357 / 16 / Annex VII / Part VII ; Annex VII / Part VI | 2630 / 14 / Annex VII / Part VI ; Annex VII / Part VII |
| 188 | 3242 / 12 / Annex VII / Part VII ; Annex VII / Part VIII | 3006 / 16 / Annex VII / Part VII ; Annex VII / Part VIII | 2654 / 17 / Annex VII / Part VIII ; Annex VII / Part VII | 2916 / 16 / Annex VII / Part VII ; Annex VII / Part VIII |
| 189 | 3545 / 63 / Annex VII / Part VIII ; Annex VII / Part VIII / 1a ; Annex VII / Part VIII / 1b ; +5 autres repères (CSV) | 3650 / 74 / Annex VII / Part VIII ; Annex VII / Part VIII / 1a ; Annex VII / Part VIII / 1b ; +5 autres repères (CSV) | 3002 / 68 / Annex VII / Part VIII ; Annex VII / Part VIII / 1a ; Annex VII / Part VIII / 1b ; +5 autres repères (CSV) | 3246 / 52 / Annex VII / Part VIII ; Annex VII / Part VIII / 1a ; Annex VII / Part VIII / 1b ; +5 autres repères (CSV) |
| 190 | 2231 / 0 / Aucun repère candidat ; voir TXT | 2076 / 1 / Annex VII / Appendix I | 2093 / 10 / Annex VII / Appendix I | 1971 / 0 / Aucun repère candidat ; voir TXT |
| 191 | 3006 / 1 / Annex VII / Appendix I | 3004 / 1 / Annex VII / Appendix I | 2827 / 5 / Annex VII / Appendix I | 2709 / 1 / Annex VII / Appendix I |
| 192 | 2544 / 0 / Aucun repère candidat ; voir TXT | 2534 / 0 / Aucun repère candidat ; voir TXT | 2391 / 5 / Annex VII / Appendix I | 2237 / 0 / Aucun repère candidat ; voir TXT |
| 193 | 1803 / 0 / Aucun repère candidat ; voir TXT | 1788 / 0 / Aucun repère candidat ; voir TXT | 1602 / 1 / Annex VII / Appendix I | 1494 / 0 / Aucun repère candidat ; voir TXT |
| 194 | 2769 / 20 / Annex VII / Appendix II ; Annex VII / Appendix II / A.1 ; Annex VII / Appendix II / A.2 ; +6 autres repères (CSV) | 2341 / 18 / Annex VII / Appendix II ; Annex VII / Appendix II / A.1 ; Annex VII / Appendix II / A.2 ; +6 autres repères (CSV) | 2179 / 32 / Annex VII / Appendix II ; Annex VII / Appendix II / A.1 ; Annex VII / Appendix II / A.2 ; +6 autres repères (CSV) | 2293 / 16 / Annex VII / Appendix II ; Annex VII / Appendix II / A.1 ; Annex VII / Appendix II / A.2 ; +6 autres repères (CSV) |
| 195 | 2158 / 6 / Annex VII / Appendix II ; Annex VII / Appendix II / C.1 ; Annex VII / Appendix II / C.2 ; +2 autres repères (CSV) | 1861 / 13 / Annex VII / Appendix II ; Annex VII / Appendix II / C.1 ; Annex VII / Appendix II / C.2 ; +2 autres repères (CSV) | 1651 / 17 / Annex VII / Appendix II ; Annex VII / Appendix II / C.1 ; Annex VII / Appendix II / C.2 ; +2 autres repères (CSV) | 1902 / 8 / Annex VII / Appendix II ; Annex VII / Appendix II / C.1 ; Annex VII / Appendix II / C.2 ; +2 autres repères (CSV) |
| 196 | 2502 / 30 / Annex VIII / Part I | 2332 / 35 / Annex VIII / Part I | 2188 / 46 / Annex VIII / Part I | 2339 / 16 / Annex VIII / Part I |
| 197 | 1780 / 16 / Annex VIII / Part I | 1825 / 24 / Annex VIII / Part I | 1763 / 32 / Annex VIII / Part I | 1823 / 4 / Annex VIII / Part I |
| 198 | 3464 / 36 / Annex VIII / Part I | 3118 / 48 / Annex VIII / Part I | 3145 / 50 / Annex VIII / Part I | 3327 / 31 / Annex VIII / Part I |
| 199 | 2692 / 18 / Annex VIII / Part II ; Annex VIII / Part I | 2671 / 20 / Annex VIII / Part II ; Annex VIII / Part I | 2648 / 31 / Annex VIII / Part I ; Annex VIII / Part II | 2718 / 19 / Annex VIII / Part I ; Annex VIII / Part II |
| 200 | 3247 / 36 / Annex VIII / Part II | 3187 / 47 / Annex VIII / Part II | 3021 / 44 / Annex VIII / Part II | 3413 / 43 / Annex VIII / Part II |
| 201 | 750 / 6 / Annex IX | 816 / 6 / Annex IX | 663 / 8 / Annex IX | 699 / 6 / Annex IX |
| 202 | 2379 / 0 / Aucun repère candidat ; voir TXT | 2526 / 10 / Annex X | 2123 / 1 / Annex X | 2341 / 5 / Annex X |
| 203 | 2727 / 0 / Aucun repère candidat ; voir TXT | 2909 / 8 / Annex X | 2460 / 4 / Annex X | 2841 / 0 / Aucun repère candidat ; voir TXT |
| 204 | 2488 / 0 / Aucun repère candidat ; voir TXT | 2410 / 4 / Annex X | 2116 / 1 / Annex X | 2315 / 1 / Annex X |
| 205 | 1247 / 0 / Aucun repère candidat ; voir TXT | 1098 / 0 / Aucun repère candidat ; voir TXT | 1051 / 0 / Aucun repère candidat ; voir TXT | 1144 / 0 / Aucun repère candidat ; voir TXT |
| 206 | 1216 / 0 / Aucun repère candidat ; voir TXT | 1142 / 0 / Aucun repère candidat ; voir TXT | 1037 / 0 / Aucun repère candidat ; voir TXT | 1183 / 0 / Aucun repère candidat ; voir TXT |
| 207 | 1054 / 0 / Aucun repère candidat ; voir TXT | 1011 / 0 / Aucun repère candidat ; voir TXT | 951 / 0 / Aucun repère candidat ; voir TXT | 1020 / 0 / Aucun repère candidat ; voir TXT |
| 208 | 1280 / 1 / Annex X | 1169 / 1 / Annex XIV | 958 / 1 / Annex XIV | 1033 / 1 / Annex XIV |
| 209 | 1059 / 0 / Aucun repère candidat ; voir TXT | 1028 / 0 / Aucun repère candidat ; voir TXT | 914 / 0 / Aucun repère candidat ; voir TXT | 929 / 0 / Aucun repère candidat ; voir TXT |
| 210 | 1663 / 0 / Aucun repère candidat ; voir TXT | 1505 / 0 / Aucun repère candidat ; voir TXT | 1149 / 0 / Aucun repère candidat ; voir TXT | 1300 / 0 / Aucun repère candidat ; voir TXT |
| 211 | 1772 / 0 / Aucun repère candidat ; voir TXT | 1503 / 0 / Aucun repère candidat ; voir TXT | 1309 / 0 / Aucun repère candidat ; voir TXT | 1441 / 0 / Aucun repère candidat ; voir TXT |
| 212 | 1624 / 0 / Aucun repère candidat ; voir TXT | 1474 / 0 / Aucun repère candidat ; voir TXT | 1178 / 0 / Aucun repère candidat ; voir TXT | 1251 / 0 / Aucun repère candidat ; voir TXT |
| 213 | 1865 / 0 / Aucun repère candidat ; voir TXT | 1557 / 0 / Aucun repère candidat ; voir TXT | 1340 / 0 / Aucun repère candidat ; voir TXT | 1810 / 0 / Aucun repère candidat ; voir TXT |
| 214 | 1426 / 0 / Aucun repère candidat ; voir TXT | 1311 / 0 / Aucun repère candidat ; voir TXT | 1169 / 0 / Aucun repère candidat ; voir TXT | 1208 / 0 / Aucun repère candidat ; voir TXT |
| 215 | 1152 / 0 / Aucun repère candidat ; voir TXT | 1114 / 0 / Aucun repère candidat ; voir TXT | 1071 / 0 / Aucun repère candidat ; voir TXT | 1112 / 0 / Aucun repère candidat ; voir TXT |
| 216 | 1310 / 0 / Aucun repère candidat ; voir TXT | 1196 / 0 / Aucun repère candidat ; voir TXT | 1127 / 0 / Aucun repère candidat ; voir TXT | 1204 / 0 / Aucun repère candidat ; voir TXT |
| 217 | 1171 / 0 / Aucun repère candidat ; voir TXT | 1084 / 0 / Aucun repère candidat ; voir TXT | 999 / 0 / Aucun repère candidat ; voir TXT | 1064 / 0 / Aucun repère candidat ; voir TXT |
| 218 | 1024 / 0 / Aucun repère candidat ; voir TXT | 1034 / 0 / Aucun repère candidat ; voir TXT | 898 / 0 / Aucun repère candidat ; voir TXT | 941 / 0 / Aucun repère candidat ; voir TXT |
| 219 | 865 / 0 / Aucun repère candidat ; voir TXT | 846 / 0 / Aucun repère candidat ; voir TXT | 756 / 0 / Aucun repère candidat ; voir TXT | 838 / 0 / Aucun repère candidat ; voir TXT |

## Annexe D — Comparaison des 44 formes codées dans A

La recherche de comparaison ci-dessous ignore accents, casse, ponctuation et espaces comme la normalisation du code. Les pages listées sont des repères de présence, pas une extraction exacte supplémentaire ni un alignement sémantique. Le CSV reste la référence des formes effectivement extraites et de leurs offsets.

| Concept de A | Langue | Forme codée | Pages où la chaîne normalisée est présente |
|---|---|---|---|
| meat | FR | viande bovine | 3, 5, 7–9, 13–14, 36, 99, 102, 109–110, 121–122, 143, 162, 171 |
| meat | FR | viande de porc | 3, 6–7, 10, 36, 99, 102, 109–110, 122, 145 |
| meat | FR | viandes ovine et caprine | 3, 10, 36, 99, 102, 109–110, 122, 128, 145 |
| meat | FR | viande de volaille | 3, 33, 36, 99, 109–110, 122, 146, 162, 185–186, 201 |
| meat | NL | rundvlees | 3, 5, 7–8, 13–14, 99, 102, 109–110, 121–122, 143, 162, 171–172 |
| meat | NL | varkensvlees | 3, 7, 10, 36, 99, 102, 109–110, 122, 145 |
| meat | NL | schapen- en geitenvlees | 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 145 |
| meat | NL | pluimveevlees | 3, 36, 99, 102, 109–110, 122, 146, 185–186 |
| meat | EN | beef and veal | 3, 5, 7–9, 13–14, 36, 99, 102, 109–110, 121–122, 143, 162 |
| meat | EN | pigmeat | 3, 6–7, 10, 36, 99, 102, 109–110, 122, 145 |
| meat | EN | sheepmeat and goatmeat | 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 145 |
| meat | EN | poultrymeat | 3, 33, 36, 99, 109–110, 122, 146, 162, 185–186, 201 |
| meat | DE | Rindfleisch | 3, 5, 7–9, 13–14, 36, 99, 102, 109–110, 121–122, 143, 162, 171–173 |
| meat | DE | Schweinefleisch | 3, 6–7, 10, 36, 99, 102, 109–110, 122, 145 |
| meat | DE | Schaf- und Ziegenfleisch | 3, 7, 10, 36, 99, 102, 109–110, 122, 128, 145 |
| meat | DE | Geflügelfleisch | 3, 33, 36, 99, 102, 109–110, 122, 146, 162, 185–186, 201 |
| edible_offal | FR | abats comestibles des animaux de l'espèce bovine | 143–144 |
| edible_offal | NL | eetbare slachtafvallen van runderen | 143–144, 149 |
| edible_offal | EN | edible offal of bovine animals | 143, 149 |
| edible_offal | DE | Genießbare Schlachtnebenerzeugnisse von Rindern | 143–144, 149 |
| animal_fat | FR | graisses de porc (y compris le saindoux) | 145 |
| animal_fat | NL | varkensvet (reuzel daaronder begrepen) | 145 |
| animal_fat | EN | pig fat (including lard) | 145 |
| animal_fat | DE | Schweinefett (einschließlich Schweineschmalz) | 145 |
| poultry_meat_preparation | FR | préparation à base de viande de volaille | 186 |
| poultry_meat_preparation | NL | bereiding op basis van pluimveevlees | 186 |
| poultry_meat_preparation | EN | poultrymeat preparation | 186 |
| poultry_meat_preparation | DE | Geflügelfleischzubereitungen | 186 |
| processed_fruit_vegetable_product | FR | produits transformés à base de fruits et légumes | 2, 17, 140 |
| processed_fruit_vegetable_product | NL | verwerkte groenten en fruit | 2, 5, 99, 101–102, 109–110 |
| processed_fruit_vegetable_product | EN | processed fruit and vegetable products | 2, 17, 33, 140 |
| processed_fruit_vegetable_product | DE | Verarbeitungserzeugnisse aus Obst und Gemüse | 2, 5, 17, 33, 99, 101–102, 109–110, 140 |
| spreadable_fat | FR | matières grasses tartinables | 33, 37, 40, 45, 187, 194 |
| spreadable_fat | NL | smeerbare vetten | 45, 194 |
| spreadable_fat | EN | spreadable fats | 33, 37, 40, 45, 187, 194 |
| spreadable_fat | DE | Streichfette | 33, 37, 40, 45, 144, 187, 194 |
| egg | FR | œufs de volailles de basse-cour, en coquille | 146, 162 |
| egg | NL | eieren van pluimvee in de schaal | 146, 162 |
| egg | EN | poultry eggs, in shell | 146, 162 |
| egg | DE | Eier von Hausgeflügel in der Schale | 146, 162 |
| olive_oil | FR | huile d'olive | 2, 5–6, 10, 33, 37, 82–83, 98, 109, 112, 138–139, 154–155, 189, 201 |
| olive_oil | NL | olijfolie | 2, 5–6, 10, 33, 37, 82–83, 90, 98, 109, 112, 138–139, 154–155, 188–189, 201 |
| olive_oil | EN | olive oil | 2, 5–6, 10, 33, 37, 82–83, 90, 98, 109, 112, 138–139, 154–155, 188–189, 201 |
| olive_oil | DE | Olivenöl | 2, 5–6, 10, 33, 37, 82–83, 90, 98, 109, 112, 138–139, 154–155, 188–189, 201 |

## Annexe E — Repères prioritaires et comparaison actuelle

Les lignes suivantes sélectionnent des formes du CSV pour orienter la revue. Les identifiants de connaissance sont ceux déjà présents, obtenus par comparaison exacte normalisée des noms/alias. Un champ vide n’est pas un verdict et ne prouve pas qu’aucun concept apparenté n’existe.

| Lecture proposée | Langue | Pages | Propriétaires exacts dans la langue | Propriétaires exacts globaux |
|---|---|---|---|---|
| babeurre | FR | 144, 182 | — | — |
| Buttermilk | EN | 144, 182 | — | — |
| pollen | FR | 147, 163 | — | — |
| Pollen | NL | 147, 163 | — | — |
| pollen | EN | 147, 163 | — | — |
| propolis | FR | 147, 163 | — | — |
| propolis | NL | 147, 163 | — | — |
| propolis | EN | 147, 163 | — | — |
| minarine | FR | 188 | — | — |
| minarine | NL | 188 | — | — |
| minarine | EN | 188 | — | — |
| Minarine | DE | 188 | — | — |
| Maltodextrine | FR | 134–136, 144, 155–156 | — | — |
| Maltodextrine | NL | 134–136, 144, 155–156 | — | — |
| Maltodextrine | EN | 134–136, 144, 155–156 | — | — |
| lupuline | FR | 36, 107, 138, 159 | — | — |
| lupuline | NL | 36, 107, 138, 159 | — | — |
| caseïne | NL | 182 | — | casein |
| Orge | FR | 7–8, 12, 133 | barley | barley |
| lactosérum | FR | 144, 182 | — | whey |
| saindoux | FR | 145, 153 | — | — |
| miel | FR | 134, 147, 163 | honey | honey |
| retsina | FR | 176 | — | — |
| Retsina | NL | 176 | — | — |
| Retsina | EN | 176 | — | — |
| Retsina | DE | 176 | — | — |
| Piquette | FR | 143, 155, 161, 200 | — | — |
| Piquette | NL | 143, 155, 161, 200 | — | — |
| Piquette | EN | 143, 155, 161, 200 | — | — |
| truffes | FR | 141 | — | — |
| inuline | FR | 133–134, 136, 152, 159 | inulin | inulin |
| inuline | NL | 133–134, 152, 159 | inulin | inulin |

## Annexe F — Vérifications de fin exécutées

Les assertions suivantes ont toutes réussi le 2026-10-05 :

- Les six livrables se décodent intégralement en UTF-8 strict.
- CSV relu : 22 colonnes uniques, dont les 15 requises, 11 286 lignes ; aucun champ requis vide et aucune colonne excédentaire.
- 11 286 références vérifiées : PDF local existant, langue, CELEX, date, page 1–219, emplacement juridique non vide, contexte littéral et surface exacte à son offset.
- 3 870 identifiants cohérents avec leur langue/forme/proposition et une catégorie de revue stable.
- Toutes les lignes : REVIEW_REQUIRED et SOURCE_TEXT_ONLY.
- Les 876 blocs de pages sont présents, non vides, sans U+FFFD ni contrôle anormal ; leurs en-têtes confirment CELEX, date et langue. Pagination des PDF relue avec pypdf : 219 chacun.
- Les quatre SHA-256 recalculés correspondent au manifeste local courant.
- HEAD inchangé ; un seul worktree ; diffs suivis et indexés vides.
- git ls-files --others --exclude-standard ne retourne que les six fichiers attendus sous reports/0.7/eu-pdf/.
- git status --short retourne uniquement ?? reports/0.7/eu-pdf/. Aucun changement préexistant n’existait au départ ; aucun fichier suivi n’a changé.
- git diff --check : code de sortie 0, aucune sortie. Les nouveaux documents Markdown ont aussi été contrôlés sans blancs de fin de ligne. Les TXT et formes CSV gardent les espaces primaires.

Ces contrôles valident la traçabilité technique ; ils ne remplacent pas la revue visuelle et sémantique décrite dans les limites.

Commandes de fin supplémentaires réellement exécutées : git diff --name-only ; git diff --cached --name-only ; git ls-files --others --exclude-standard ; git worktree list --porcelain ; relecture/assertions Python et nouveaux hashes SHA-256.

Incident de validation corrigé : le premier contrôle supposait le code langue dans les 130 premiers caractères de chaque page ; la page 1 contient un avertissement avant son en-tête. La vérification porte désormais sur la ligne d’identité complète, trouvée dans chacun des 876 blocs. Aucun contenu source n’a été modifié.

| Pièce documentaire (hors rapport lui-même) | Octets | SHA-256 final |
|---|---:|---|
| EU_1308_2013_BROAD_CANDIDATES.csv | 8443318 | 3435039abc03205f65250ccd9cb0c276c3b8a4a0e3dd8c0541bbee461c23947a |
| EU_1308_2013_FULL_TEXT_DE.txt | 551313 | f09b489123bd9e1863c3d0b873511c142bbeab80d81f490dc830eeaba1512fb0 |
| EU_1308_2013_FULL_TEXT_EN.txt | 499698 | 6fe574a768616d828bb6cf945a9c282873517098df1b03a24b4986116229f4de |
| EU_1308_2013_FULL_TEXT_FR.txt | 569989 | 8087e2aae9f98c183f83c362156f3035cde680ead4d91ff5d7ece121781fade1 |
| EU_1308_2013_FULL_TEXT_NL.txt | 542620 | 915dfcb2ee8f3f5978e0816d5f41c0cdda05b79905c73e7630334e41b158696e |
