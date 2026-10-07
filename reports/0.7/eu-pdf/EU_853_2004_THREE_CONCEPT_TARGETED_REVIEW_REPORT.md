# UE 853/2004 — revue ciblée de trois concepts, v0.7

Date : 7 octobre 2026. Exploration documentaire et technique sans import. Ce rapport est le seul nouveau fichier du dépôt créé pour cette tâche.

**Verdict : GO_WITH_WARNINGS pour préparer une future intégration restreinte.** Proposer les quatre dénominations longues de `mechanically_separated_meat` à la prochaine décision d'intégration; différer les huit formes de `greaves` et `frog_legs` jusqu'à une preuve d'usage d'étiquette et une évaluation des contextes négatifs. Les trois matières ont une origine animale suffisamment établie pour envisager le statut ingrédient existant `NON_VEGAN`. Ce verdict ne valide aucun import et ne rend pas le lot des trois concepts prêt à intégrer.

`PROPOSER` signifie ici : formulation suffisamment étayée pour figurer dans un futur prompt borné, avec les validations restantes explicites. Cela ne signifie ni alias actif, ni couverture exacte déjà démontrée dans l'application. `DIFFÉRER` signifie qu'une preuve ou décision matérielle manque. `REJETER` signifie ne pas utiliser cette forme comme alias de ces trois concepts dans ce lot.

## État observé et périmètre

- HEAD réel : `d1f02275508c9472b1fe55ac82d792d1257f6264`.
- Branche : `master`, affichée `master...origin/master`; aucune opération réseau effectuée.
- Aucun changement suivi; index sans changement. `git diff --stat` et `git diff --cached --stat` étaient vides.
- Préexistants non suivis : `reference-input/eu-food-labelling/05-food-hygiene/` avec les quatre PDF, `EU_853_2004_MEAT_CANDIDATE_EXPLORATION_REPORT.md` et `EU_853_2004_MEAT_CANDIDATES.csv` dans ce répertoire de rapports. Aucun n'a été déplacé, supprimé ou réécrit.
- Une empreinte de chacun des 400 fichiers suivis ou non suivis préexistants a été enregistrée dans le répertoire temporaire Windows pour contrôler leur préservation. Les répertoires ignorés, notamment les sorties de build, ne font pas partie de cette mesure.
- Seulement `mechanically_separated_meat`, `greaves` et `frog_legs` examinés comme ajouts. Aucun élargissement aux catégories voisines ou aux espèces citées.

Instructions consultées : `AGENTS.md`, le skill `.agents/skills/knowledge-import-validation/SKILL.md`, ses références `source-adapters.md` et `import-workflow.md`, les invariants et conventions de `docs/base-connaissances.md`, ainsi que `docs/matching.md`, `docs/verdict.md` et `docs/knowledge-pipeline.md`. Les interdictions de cette passe priment sur les étapes d'import du workflow : aucun de ces outils n'a été exécuté.

Le [rapport exploratoire précédent](EU_853_2004_MEAT_CANDIDATE_EXPLORATION_REPORT.md) et son [CSV](EU_853_2004_MEAT_CANDIDATES.csv) ont été lus comme historique. Leurs classifications `CANDIDAT_IMPORTABLE` et résultats de sonde ne constituent pas une validation réexécutée ici. Les douze lignes correspondantes ont été rapprochées des passages primaires. Le contexte de la ligne FR `853-I1.14-FR-mechanically_separated_meat` contient notamment du texte allemand (« von Knochen… ») : ne pas reprendre ce champ comme citation française ou preuve d'import. Le CSV reste intact.

## Sources et contrôle PDF

Les quatre sources principales sont présentes sous `reference-input/eu-food-labelling/05-food-hygiene/`. L'identité visible est `02004R0853`, la consolidation `07.05.2026`, l'indice `029.001`; chaque PDF contient 94 pages. Les pages citées ci-dessous sont en base 1. Le nom de fichier, l'en-tête des pages inspectées, la langue, le nombre de pages et l'empreinte ont été vérifiés localement.

| Référence | PDF principal | Langue | Pages | SHA-256 |
|---|---|---|---:|---|
| H-FR | `CELEX_02004R0853-20260507_FR_TXT.pdf` | FR | 94 | `302312dbf898ef8993b11057070691801c8c374d8de13b5947ff592a2309562e` |
| H-NL | `CELEX_02004R0853-20260507_NL_TXT.pdf` | NL | 94 | `30fa09a89bcede829396f29769812b5c4503f404c91a9acb2771b429a1356543` |
| H-EN | `CELEX_02004R0853-20260507_EN_TXT.pdf` | EN | 94 | `125b70e276a8e21d9af7e72c6af773d19068896b126a54b777c08bd49fb7037d` |
| H-DE | `CELEX_02004R0853-20260507_DE_TXT.pdf` | DE | 94 | `9874ff8be78fcd7d33a16d837f85cbe46ae7abcfd7d70d1c1dc9542deeb7602f` |

Un complément **local**, le règlement 1169/2011, a été consulté pour distinguer une définition d'hygiène d'une dénomination d'ingrédient. Il se trouve sous `reference-input/eu-food-labelling/00-general-food-labelling/`; sa consolidation est **01.04.2025**, distincte de celle du 853/2004. Il ne sert pas à inventer des expressions composées avec des espèces.

| Référence | PDF complémentaire | Langue | Pages | SHA-256 |
|---|---|---|---:|---|
| L-FR | `CELEX_02011R1169-20250401_FR_TXT.pdf` | FR | 60 | `80fedf36e7930dddfb270871d68ff10864009fd3ac71c9d350876402c9d6a81e` |
| L-NL | `CELEX_02011R1169-20250401_NL_TXT.pdf` | NL | 60 | `36dd5f61e3e9aeb95c2e51b4ff22ba90056639f726d4b30aae97c63c653c8ef3` |
| L-EN | `CELEX_02011R1169-20250401_EN_TXT.pdf` | EN | 60 | `6cdf4190d6fa4a99d2e7d3124611724ec4cf69159563f88b35dc3c50dd416c63` |
| L-DE | `CELEX_02011R1169-20250401_DE_TXT.pdf` | DE | 60 | `c2409d5cb83f784a8ac94ceeb8d4837da343189e024f5d1a58cc015f6bb62aa2` |

Découverte et repérage par `rg` et extraction avec `pypdf`. La couche texte introduit des espaces à l'intérieur des mots, des césures et des marqueurs d'amendement; elle ne doit pas être copiée sans contrôle dans un alias. Le rendu natif local `Windows.Data.Pdf` a permis une inspection visuelle malgré l'absence de pdftoppm, mutool, Ghostscript, PyMuPDF et pypdfium2. Les PNG sont des fichiers temporaires hors dépôt; aucune installation ni modification de politique d'exécution n'a été faite.

**20 pages distinctes rendues et inspectées visuellement :** 853/2004 pages 16, 18, 19 et 83, dans les quatre langues; 1169/2011 page 49, dans les quatre langues. Les douze formes principales et les cellules de dénomination du tableau complémentaire ont donc un contrôle visuel. Les passages complémentaires de 853/2004 page 50 n'ont été examinés que dans la couche texte; ils ne fondent aucun alias proposé. Ce n'est pas une revue visuelle exhaustive des 616 pages des huit PDF.

Aucune recherche externe, aucun téléchargement et aucun échantillon réel de liste d'ingrédients n'ont été examinés. Les consolidations locales sont des textes documentaires, dont les avertissements liminaires signalent qu'ils n'ont pas eux-mêmes d'effet juridique. Cette passe n'est pas une réévaluation de licence, de droits de réutilisation ou de droit applicable à une étiquette commerciale.

## 1. `mechanically_separated_meat`

### Définition et formes primaires

Annexe I, point 1.14, page PDF 16 : produit obtenu en retirant mécaniquement la viande qui reste sur des os après désossage ou sur des carcasses de volailles; le procédé détruit ou modifie la structure fibreuse des muscles. L'origine animale est explicite. Ce n'est pas un nom de procédé détaché de sa matière.

Les ID suivants sont ceux du CSV historique; la preuve citée est le PDF désormais vérifié, pas son champ de contexte.

| ID de revue | Source / emplacement | Forme exacte candidate | Court contexte source vérifié | Type de preuve |
|---|---|---|---|---|
| `853-I1.14-FR-mechanically_separated_meat` | H-FR, p.16, annexe I 1.14 | `viandes séparées mécaniquement` | Titre « viandes séparées mécaniquement ou VSM »; « destruction ou la modification de la structure fibreuse des muscles » | Définition de matière animale |
| `853-I1.14-NL-mechanically_separated_meat` | H-NL, p.16, annexe I 1.14 | `Separatorvlees` | « waardoor de spierweefselstructuur verloren gaat of verandert » | Définition de matière animale |
| `853-I1.14-EN-mechanically_separated_meat` | H-EN, p.16, annexe I 1.14 | `Mechanically separated meat` | « or ‘MSM’ »; « removing meat from flesh-bearing bones after boning or from poultry carcases » | Définition de matière animale |
| `853-I1.14-DE-mechanically_separated_meat` | H-DE, p.16, annexe I 1.14 | `Separatorenfleisch` | « die Struktur der Muskelfasern sich auflöst oder verändert wird » | Définition de matière animale |

Les quatre définitions sont parallèles. Les mots constituent des segments exacts du texte, sans guillemets, ponctuation définitoire ou « ou/or » ajouté à l'alias. La casse initiale NL/EN du titre est conservée dans cette table; sa minuscule correspondante est aussi attestée dans le tableau complémentaire.

**Distinctions à préserver :** au point 1.13, les viandes hachées sont de la viande désossée hachée en fragments, contenant moins de 1 % de sel. Au point 1.15, les préparations partent de viande fraîche, avec ajouts ou transformation insuffisante pour modifier à cœur la structure des fibres et faire disparaître les caractéristiques de viande fraîche. Ces trois définitions ne sont pas trois alias d'un même concept. Aucun alias de viande hachée ni de préparation n'est proposé ici.

### Dénomination d'ingrédient et limites d'usage

**Preuve supplémentaire : 1169/2011, annexe VII, partie B, point 18, page PDF 49**, tableau contrôlé dans les quatre langues. La colonne de dénomination prévoit :

| Source | Forme textuelle dans la colonne de dénomination | Contexte associé |
|---|---|---|
| L-FR | `Viandes séparées mécaniquement` | Avec le ou les noms de l'espèce ou des espèces animales dont elles proviennent |
| L-NL | `separatorvlees` | Précédé du ou des noms des espèces animales dont il provient |
| L-EN | `mechanically separated meat` | Avec le ou les noms des espèces animales dont il provient |
| L-DE | `Separatorenfleisch` | Précédé du ou des noms des espèces animales dont il provient |

Il existe donc une preuve locale de **dénomination dans la liste d'ingrédients**, et pas seulement une catégorie d'hygiène. Ce tableau ne démontre ni fréquence réelle ni succès du matcher sur toutes les étiquettes conformes. Il donne un modèle avec noms d'espèces, pas une liste de formulations composées attestées. Aucune expression espèce + matière n'est fabriquée dans ce rapport.

La forme de base NL/DE peut notamment apparaître dans une construction avec préfixe ou dans un mot composé; les bornes du matcher ne reconnaissent pas automatiquement une sous-chaîne au milieu d'un mot. Même avec un espace, un qualificatif d'espèce peut laisser un résidu ou résoudre un propriétaire historique supplémentaire. Il faudra tester des **formes complètes réellement attestées** avant de revendiquer cette couverture. La page 50 du 853/2004 contient des obligations d'information/cuisson pour certains produits; elle ne fournit pas une preuve supplémentaire de nom exact d'ingrédient.

**Décision : PROPOSER les quatre dénominations longues**, sous le concept distinct `mechanically_separated_meat`, avec statut ingrédient `NON_VEGAN`, à la préparation d'un lot limité. Leur formulation identifie expressément de la viande; c'est le meilleur gain de connaissance du trio. Conserver le propriétaire `meat` de son alias court anglais et laisser la nouvelle forme longue avoir sa propre identité après validation réelle.

**Différer `VSM` et `MSM`**, pourtant attestés dans les définitions FR/EN. Leur attestation réglementaire ne prouve pas que chaque sigle isolé rencontré dans une composition a ce sens. Aucun propriétaire local exact n'a été trouvé, mais aucun corpus ne permet d'écarter les homonymes de sigles. Le tableau d'ingrédients contrôlé prescrit les formulations longues, et aucune preuve d'étiquette réelle n'a été examinée pour ces sigles.

Risque résiduel : une formulation longue peut encore être citée dans une négation, une description d'imitation ou une mention descriptive hors composition. Il n'y a pas de protection contextuelle générale applicable à ce nouveau concept. Le benchmark futur doit distinguer ingrédient présent et simple mention. Le risque n'est pas annulé par la précision lexicale.

## 2. `greaves`

### Définition et séparation d'avec les graisses

Annexe I, point 7.6, page PDF 19 : résidus protéiniques de la fonte, après séparation partielle des graisses et de l'eau; la version DE précise les constituants solides provenant de la fonte du gras brut. Le point précédent 7.5 définit les graisses animales fondues, obtenues de viande, os compris, pour consommation humaine. La juxtaposition des définitions et le contexte de production de l'annexe III, section XII, distinguent **la fraction grasse** et **le résidu protéique**.

| ID de revue | Source / emplacement | Forme exacte candidate | Court contexte source vérifié | Type de preuve |
|---|---|---|---|---|
| `853-I7.6-FR-greaves` | H-FR, p.19, annexe I 7.6 | `cretons` | « résidus protéiniques de la fonte, après séparation partielle des graisses et de l'eau » | Définition de produit dérivé |
| `853-I7.6-NL-greaves` | H-NL, p.19, annexe I 7.6 | `Kanen` | « het eiwithoudende residu van het smeltproces » | Définition de produit dérivé |
| `853-I7.6-EN-greaves` | H-EN, p.19, annexe I 7.6 | `Greaves` | « the protein-containing residue of rendering » | Définition de produit dérivé |
| `853-I7.6-DE-greaves` | H-DE, p.19, annexe I 7.6 | `Grieben` | « eiweißhaltige feste Bestandteile »; « nach teilweiser Trennung von Fett und Wasser » | Définition de produit dérivé |

Le concept proposé est le **résidu animal de fonte défini au point 7.6**, pas tout produit culinaire partageant un nom. Son statut attendu serait `NON_VEGAN`. Ne pas le fusionner avec `animal_fat`, ne pas donner ces surfaces à ce propriétaire par commodité, et ne pas fusionner les deux matières au titre d'un lot large.

`animal_fat` existe avec le nom « Graisse animale », statut `NON_VEGAN`, alias de graisses porcines, bovines, ovines/caprines et de volaille, notamment `saindoux`, `reuzel`, `lard`, `Schweineschmalz`. Ses alias et mappings ne contiennent aucune des quatre formes de cretons. `edible_offal` existe également, sans ces formes; rien dans les définitions examinées ne justifie de lui attribuer le résidu de fonte. Le concept distinct apporte une connaissance nouvelle, même s'il relève d'un futur lot de dérivés de fonte.

### Usage et décision

Les passages consultés définissent un produit alimentaire animal et encadrent sa production; ils ne montrent pas une liste d'ingrédients commerciale contenant chacune des formes. Leur usage comme composant est plausible, mais cette plausibilité n'est pas une preuve d'étiquette. La portée culinaire/régionale éventuelle de `cretons` n'a pas été vérifiée dans une autre source; elle ne doit pas être inférée de cette définition spécialisée.

Les quatre noms peuvent être repris dans une dénomination d'imitation végétale. Aucun mécanisme générique n'empêcherait un nouvel alias `NON_VEGAN` de se déclencher dans une telle expression. L'absence de collision dans le lexique actuel ne mesure pas ce risque.

**Décision : DIFFÉRER les quatre formes.** Déblocage : exemples locaux ou source autorisée établissant leur usage comme ingrédient, clarification de la portée hors catégorie réglementaire, et cas animaux/végétaux pertinents soumis au pipeline réel. Ne pas inventer une formulation « cretons animaux » ou un qualificatif de porc à partir de la catégorie collective pour rendre l'alias artificiellement sûr.

## 3. `frog_legs`

### Définition, produit et taxonomie

Annexe I, point 6.1, page PDF 18 : partie postérieure du corps coupée transversalement derrière les membres antérieurs, éviscérée et dépouillée. C'est une partie d'animal préparée pour consommation, suffisamment distincte d'une grenouille vivante ou d'un nom zoologique. Le statut attendu de la matière effectivement présente serait `NON_VEGAN`.

| ID de revue | Source / emplacement | Forme exacte candidate | Court contexte source vérifié | Type de preuve |
|---|---|---|---|---|
| `853-I6.1-FR-frog_legs` | H-FR, p.18, annexe I 6.1 | `cuisses de grenouille` | « partie postérieure du corps »; « éviscérée et dépouillée »; `Rana` | Définition de partie animale |
| `853-I6.1-NL-frog_legs` | H-NL, p.18, annexe I 6.1 | `Kikkerbilletjes` | « het achterste gedeelte »; « gestript en gevild »; `Rana` | Définition de partie animale |
| `853-I6.1-EN-frog_legs` | H-EN, p.18, annexe I 6.1 | `Frogs' legs` | « posterior part of the body »; « eviscerated and skinned »; le rendu affiche `RNA` | Définition de partie animale; anomalie taxonomique |
| `853-I6.1-DE-frog_legs` | H-DE, p.18, annexe I 6.1 | `Froschschenkel` | « hinteren Körperteile »; « ausgeweidet und enthäutet »; `Rana` | Définition de partie animale |

**`RNA` est visible dans le PDF anglais rendu lui-même**, au point 6.1; la couche texte reproduit cette anomalie. Il serait inexact de la décrire uniquement comme une erreur d'extraction et de remplacer silencieusement la citation par `Rana`. FR, NL et DE affichent `Rana`, avec la famille Ranidae. L'anomalie ne rend pas le nom anglais du produit illisible, mais elle doit rester signalée dans sa provenance. Aucun nom taxonomique n'est proposé comme alias.

**Autre limite du rapport historique :** annexe III, section XI, point 8, page PDF 83, vérifiée visuellement dans les quatre langues. Les exigences des points 1 à 5 sont également applicables aux cuisses de grenouille de `Pelophylax` (Ranidae) et de `Fejervarya`, `Limnonectes`, `Hoplobatrachus` (Dicroglossidae), pour consommation humaine. Ce passage étend des exigences d'hygiène; il ne faut pas le présenter comme une réécriture du point 6.1. La consolidation ne permet donc pas de résumer tout son champ par « uniquement Rana ». Le futur concept alimentaire doit préciser sa portée sans transformer cette énumération en alias ou en liste taxonomique exhaustive.

La page 83 EN atteste aussi `frogs’ legs`, apostrophe typographique, contre `Frogs' legs` au point 6.1. C'est une variante typographique attestée, pas un synonyme ou un changement de nombre; les deux deviennent `frogs legs` avec la normalisation actuelle. Une seule surface éditoriale représentative suffit; ne pas créer deux entrées équivalentes dans le même lexique.

### Usage et décision

Les quatre expressions désignent expressément une partie comestible et sont plus spécifiques que les noms nus de grenouilles ou d'espèces. Elles ne sont pas des noms d'élevage ou de procédé. Les passages prouvent un **produit alimentaire**; ils ne démontrent ni sa fréquence dans les listes d'ingrédients, ni l'utilisation effective des quatre expressions comme composant d'un produit composé.

Une mention de goût, une description négative ou une imitation peut encore citer ce nom. Aucune protection actuelle n'est dédiée à ces expressions. Leur spécificité anatomique réduit le risque lexical, mais ne démontre pas la maîtrise des contextes de substituts végétaux.

**Décision : DIFFÉRER les quatre formes avant intégration.** Déblocage : preuve d'usage sur des listes d'ingrédients, choix explicite de portée alimentaire à la lumière des deux passages, et contrôle de contextes négatifs. L'anomalie EN peut être documentée sans fabriquer une correction de source; une intégration future devra citer le produit tel qu'attesté et conserver la limite taxonomique.

## État lexical, collisions et comportement actuel

Les trois identifiants sont absents de `knowledge/ingredients.json`, des entrées d'alias et des mappings de `knowledge/ingredient_aliases_multilingual.json`. `knowledge/sources.json` ne contient pas de déclaration du 853/2004. Le futur lot devra déclarer sa source et sa consolidation; aucun identifiant de source n'est inventé ici.

Contrôle statique en lecture seule : recherche des noms canoniques, alias et E-numbers des ingrédients, des alias multilingues et `ocrVariants`, des `surfaceForm` de mapping et des corrections OCR liées aux formes étudiées. Comparaison normalisée interlangues, puis recherche de sous-expressions avec les bornes alphanumériques du matcher. Aucun propriétaire exact pour les douze surfaces, aucun alias existant plus long les contenant avec ces bornes, et aucune correction OCR directe pour ces formes. La seule sous-expression admissible trouvée est `meat`, propriétaire historique `meat`, dans la forme anglaise de viande séparée mécaniquement. Les formes du lot ne se heurtent pas entre elles après normalisation, hors les variantes d'apostrophe anglaises intentionnellement équivalentes.

La normalisation pertinente est celle de `TextNormalizer.kt:6` : minuscules, ligatures, NFD sans diacritiques, suites non `[a-z0-9]` remplacées par un espace. L'inspection de `IngredientMatcher.kt:170` et `:206` à `:251` confirme la sélection par longueur, les bornes de mots et le calcul du résidu. `MultilingualIngredientLexicon.kt:217` à `:235` vérifie les doublons par langue et les propriétaires incompatibles d'une même clé entre langues. Les mappings décrivent la provenance; une surface de mapping seule ne devient pas un alias runtime.

**Aucun appel JVM du matcher ou du pipeline n'a été réexécuté dans cette tâche.** Les résultats ci-dessous sont des conclusions statiques tirées des clés chargées et du code courant, cohérentes avec la sonde annoncée par le rapport historique, mais pas des tests d'exécution nouveaux. Les résidus sont normalisés. Pour un résultat `NONE` ou partiel, `UnknownCollector` conserve le token complet comme inconnu; ne pas confondre cette expression visible avec le seul résidu.

| Concept | Langue / forme source | Propriétaire exact | Résolution déduite sur token isolé | Concept reconnu / résidu |
|---|---|---|---|---|
| `mechanically_separated_meat` | FR `viandes séparées mécaniquement` | Aucun | `NONE` | Aucun / `viandes separees mecaniquement` |
| `mechanically_separated_meat` | NL `Separatorvlees` | Aucun | `NONE` | Aucun / `separatorvlees` |
| `mechanically_separated_meat` | EN `Mechanically separated meat` | Aucun | `PARTIAL_CONTEXTUAL` | `meat` / `mechanically separated` |
| `mechanically_separated_meat` | DE `Separatorenfleisch` | Aucun | `NONE` | Aucun / `separatorenfleisch` |
| `greaves` | FR `cretons` | Aucun | `NONE` | Aucun / `cretons` |
| `greaves` | NL `Kanen` | Aucun | `NONE` | Aucun / `kanen` |
| `greaves` | EN `Greaves` | Aucun | `NONE` | Aucun / `greaves` |
| `greaves` | DE `Grieben` | Aucun | `NONE` | Aucun / `grieben` |
| `frog_legs` | FR `cuisses de grenouille` | Aucun | `NONE` | Aucun / `cuisses de grenouille` |
| `frog_legs` | NL `Kikkerbilletjes` | Aucun | `NONE` | Aucun / `kikkerbilletjes` |
| `frog_legs` | EN `Frogs' legs` | Aucun | `NONE` | Aucun / `frogs legs` |
| `frog_legs` | DE `Froschschenkel` | Aucun | `NONE` | Aucun / `froschschenkel` |

Le résultat partiel EN ne prouve ni l'identité de `mechanically_separated_meat`, ni la justesse sémantique de la mention. Le propriétaire historique `meat` et ses alias, notamment `pigmeat` et les noms courts, doivent rester en place. Un futur alias anglais plus long pourrait masquer sa plage courte et résoudre le nouveau concept; ce comportement devra être vérifié par test, sans transfert d'alias.

Le matcher ne réalise pas de flexion automatique. Aucun passage vérifié n'autorise ici des singuliers fabriqués, de nouveaux pluriels, un raccourci zoologique, un synonyme culinaire ou une correction OCR inventée. Une différence de casse ou d'apostrophe déjà normalisée ne mérite pas un alias supplémentaire. Les césures de ligne du PDF sont un artefact à contrôler, pas une variante à enregistrer.

Les protections sont limitées à des familles et situations révisées : `protectedAnimalIds` contient `butter`, `milk`, `cream` (`IngredientMatcher.kt:352`); d'autres filtres ciblent miel, lait, fruits, arômes, chocolat ou café. Il n'existe pas de règle universelle « vegan/végétal/sans » qui neutraliserait ces nouveaux concepts. Le modèle éditorial actuel ne fournit pas un champ d'exigence de contexte générale pour ces alias.

Exemples de **contre-épreuves construites, non observées sur des étiquettes** à ajouter à un futur benchmark : « sans viandes séparées mécaniquement », « cretons végétaliens », « vegan greaves », « plantaardige kanen », « vegane Grieben », ou une mention de goût/imitations de cuisses de grenouille. Ces expressions ne sont ni des aliases proposés ni des faits de marché. Sur un token contenant un futur alias animal, un résidu « vegan » ou une négation ne garantit pas l'annulation du bloqueur; la priorité du verdict rendrait un faux positif coûteux. Il faudra aussi vérifier le parsing et l'extraction de section, pas seulement la recherche de sous-chaînes.

## Statut, verdict et identification du responsable

Le schéma réel utilise `VEGAN`, `VEGETARIAN`, `NON_VEGAN`, `UNCERTAIN` pour les ingrédients (`ingredients.kt:3`; `docs/base-connaissances.md:38`). **`NON_VEGETARIAN` est un verdict détaillé, pas un statut à ajouter aux données.** Pour ces matières animales présentes en tant qu'ingrédients, la convention réutilisable est `NON_VEGAN`.

`VerdictEngine.kt:27` à `:28` donne priorité à un ingrédient considéré `NON_VEGAN` et renvoie `AnalysisVerdict.NON_VEGETARIAN`, même si des inconnus ou incertains sont présents. Les traces demeurent séparées du verdict. `DiagnosticModels.kt:115` expose `nonVegetarianIngredientIds`; les références de bloqueurs sont conservées dans `knownBlockingIngredients` (`:242`). Les API existantes permettent donc de contrôler l'ID responsable lors d'une future validation, sans modifier le moteur.

**Écart avec le contexte formulé dans la demande :** le verdict choisit prioritairement le bloqueur animal; l'analyse ne s'arrête cependant pas au premier token animal. `VeganAnalyzer.kt:455` renseigne actuellement `stoppedAtNonVegetarian = false`, et `docs/verdict.md:3` documente le parcours de tous les tokens pour conserver bloqueurs et inconnus. Le court-circuit de `any` dans l'agrégation n'est pas un arrêt anticipé du pipeline. Aucun changement de ce comportement ni mesure de rapidité n'est proposé ou exécuté ici.

## Tableau final de décision des formes

La langue du PDF et la langue lexicale sont identiques pour toutes les formes principales. Les références H/L renvoient aux fichiers et empreintes du tableau des sources. Les ID primaires sont conservés dans les sections précédentes.

| Concept | Langue | Alias ou forme examinée | Preuve | Décision | Motif / condition restante |
|---|---|---|---|---|---|
| `mechanically_separated_meat` | FR | `viandes séparées mécaniquement` | H-FR p.16 I.1.14; L-FR p.49 VII.B.18 | **PROPOSER** | Matière animale et dénomination d'ingrédient attestées; futur contrôle des formes complètes et contextes |
| `mechanically_separated_meat` | NL | `Separatorvlees` | H-NL p.16 I.1.14; `separatorvlees` L-NL p.49 VII.B.18 | **PROPOSER** | Même preuve; attention aux préfixes/composés et bornes du matcher |
| `mechanically_separated_meat` | EN | `Mechanically separated meat` | H-EN p.16 I.1.14; `mechanically separated meat` L-EN p.49 VII.B.18 | **PROPOSER** | Même preuve; préserver `meat`, vérifier résolution complète du nouveau concept |
| `mechanically_separated_meat` | DE | `Separatorenfleisch` | H-DE p.16 I.1.14; L-DE p.49 VII.B.18 | **PROPOSER** | Même preuve; attention aux préfixes/composés et bornes du matcher |
| `mechanically_separated_meat` | FR | `VSM` | H-FR p.16 I.1.14 | **DIFFÉRER** | Sigle attesté; preuve d'usage et maîtrise des homonymes manquantes |
| `mechanically_separated_meat` | EN | `MSM` | H-EN p.16 I.1.14 | **DIFFÉRER** | Sigle attesté; preuve d'usage et maîtrise des homonymes manquantes |
| `greaves` | FR | `cretons` | H-FR p.19 I.7.6 | **DIFFÉRER** | Résidu animal défini; usage comme ingrédient, portée et contextes végétaux à vérifier |
| `greaves` | NL | `Kanen` | H-NL p.19 I.7.6 | **DIFFÉRER** | Même manque de preuve d'usage et de contrôle des contextes |
| `greaves` | EN | `Greaves` | H-EN p.19 I.7.6 | **DIFFÉRER** | Même manque de preuve d'usage et de contrôle des contextes |
| `greaves` | DE | `Grieben` | H-DE p.19 I.7.6 | **DIFFÉRER** | Même manque de preuve d'usage et de contrôle des contextes |
| `frog_legs` | FR | `cuisses de grenouille` | H-FR p.18 I.6.1; p.83 III.XI.8 | **DIFFÉRER** | Partie animale spécifique; preuve d'usage et portée alimentaire à décider |
| `frog_legs` | NL | `Kikkerbilletjes` | H-NL p.18 I.6.1; p.83 III.XI.8 | **DIFFÉRER** | Même manque de preuve d'usage et de validation des contextes |
| `frog_legs` | EN | `Frogs' legs` | H-EN p.18 I.6.1 | **DIFFÉRER** | Nom lisible; anomalie `RNA` documentée, usage et portée à valider |
| `frog_legs` | EN | `frogs’ legs` | H-EN p.83 III.XI.8 | **DIFFÉRER** | Variante attestée; même clé `frogs legs`, ne pas dupliquer l'entrée précédente |
| `frog_legs` | DE | `Froschschenkel` | H-DE p.18 I.6.1; p.83 III.XI.8 | **DIFFÉRER** | Même manque de preuve d'usage et de validation des contextes |
| `frog_legs` | Taxonomie, hors alias linguistique | `Rana`, `Ranidae`, `Pelophylax`, `Fejervarya`, `Limnonectes`, `Hoplobatrachus`, `Dicroglossidae` | H-FR/NL/DE p.18; H-FR/NL/EN/DE p.83 | **REJETER** comme alias | Noms zoologiques, pas dénominations des parties consommées |
| `frog_legs` | Anomalie EN | `RNA` | H-EN p.18, texte et rendu | **REJETER** | Ne pas propager l'anomalie comme nom taxonomique ou alias |
| `frog_legs` | FR/NL/EN/DE | Noms génériques d'animaux et noms d'espèces seuls | Aucun passage retenu comme dénomination d'ingrédient de ce concept | **REJETER** dans ce lot | Animal vivant, description ou zoologie ne prouvent pas la présence des cuisses |
| Les trois | FR/NL/EN/DE | Autres nombres, synonymes culinaires, raccourcis et formulations espèce + matière non attestés | Aucune preuve exacte retenue | **REJETER** dans ce lot | Ne pas fabriquer d'alias; toute autre forme exige sa propre preuve |

Par rapport au rapport initial : contrôle visuel désormais effectué; preuve de dénomination d'ingrédient renforcée pour la viande séparée mécaniquement grâce au 1169/2011 local; cretons et cuisses de grenouille remis en attente d'usage/contextes; anomalie EN localisée au rendu source; portée « uniquement Rana » abandonnée comme résumé de toute la consolidation. Aucun historique n'est réécrit.

## Validations exécutées et limites

**Exécuté :** `git status --short --branch`, `git rev-parse HEAD`, diff suivi et staged; recherches `rg`/`rg --files`; lecture des instructions, rapports, importeur, données et code ciblés; calcul SHA-256 et nombre de pages des huit PDF; extraction locale des points cités; rendu et inspection visuelle des 20 pages listées; scan statique normalisé des propriétaires et sous-expressions. Une sortie console d'extraction a rencontré un problème d'encodage cp1252; le passage a été relu avec `python -X utf8`, sans modifier la source. Cela n'est pas un échec de validation du corpus.

**Non exécuté :** importeur, y compris `--check`/`--dry-run`; builder, générateur ou régénération; test Python d'importeur; sonde JVM nouvelle; pipeline de matching; tests Gradle ciblés ou complets; PIT; contrôle sur téléphone; benchmark; vérification de licence; étude exhaustive d'étiquettes ou des occurrences des huit PDF. Aucune réussite de ces validations n'est revendiquée. Les chiffres de précision ou de performance du futur lot restent inconnus.

Contrôle final de préservation : les 400 fichiers de l'empreinte initiale sont toujours présents et identiques octet pour octet, notamment les JSON de connaissance, les actifs Android, le code, les PDF et les rapports préexistants. Le seul fichier supplémentaire est ce rapport; diff suivi et staged toujours vides, `git diff --check` sans erreur. Relecture du rapport et contrôle UTF-8 effectués; le tableau final contient 19 lignes de décision, comprenant les douze formes principales, les sigles, la variante typographique et les groupes d'exclusions. Ce contrôle de préservation ne remplace pas les validations applicatives non exécutées.

L'importeur `tools/import_eu_agricultural_products_regulation.py:536` à `:544` a été **inspecté seulement**. Ses modes `--dry-run`, `--write`, `--check` et son dispatcher `--animal-enrichment`/`--meat-species` concernent le 1308/2013; le dernier lot est borné aux cinq espèces déjà intégrées. Il ne constitue pas un adaptateur 853/2004 pour ces trois candidats. Aucun importeur dédié à cette source n'a été repéré dans les outils recherchés. Ne pas détourner ce mode pour contourner la revue d'un nouveau lot.

## Étapes nécessaires pour un futur lot — non exécutées

1. Refaire le préflight Git et les empreintes des sources. Borner explicitement le futur périmètre : recommandation actuelle, `mechanically_separated_meat` avec quatre formes longues seulement; les deux autres concepts restent différés. Établir les exigences de réutilisation et la déclaration de source avant toute mutation.
2. Rassembler des étiquettes ou preuves d'usage autorisées et exactes, surtout pour les deux concepts différés. Décider la portée de `frog_legs` sans zoologie transformée en alias, et celle de `greaves` sans fusion avec `animal_fat`. Ne pas compléter les langues ou les flexions par déduction.
3. Préparer le traitement spécifique à la source conformément à `source-adapters.md`; conserver PDF, consolidation, langue, page, point, forme et empreinte dans les champs de provenance déjà utilisés. Les mappings devront avoir concept, langue, surface, normalisation, source, relation, groupe, confiance et preuves cohérentes. Réutiliser le modèle existant; aucun nouveau statut ou attribut contextuel.
4. Examiner le véritable CLI du futur outil; vérifier sur entrées temporaires les modes sans écriture, lot absent/partiel/divergent, provenance altérée, collisions interlangues et avec noms/alias/OCR, idempotence et préservation des propriétaires. Un `--check` ne doit pas être assimilé à une validation de proposition. Aucune de ces commandes n'est exécutée dans cette passe.
5. Tester par les API réelles chaque forme retenue : bon ID, `EXACT`, résidu vide, aucun inconnu ajouté, statut `NON_VEGAN`, verdict détaillé `NON_VEGETARIAN`, diagnostic contenant le bon ID de bloqueur. Ajouter les contextes négatifs et les noms complets d'espèces seulement s'ils sont attestés. Si une résolution n'est pas exacte, documenter le blocage; ne pas modifier le matcher pour faire passer ce lot.
6. Lors d'une future intégration explicitement demandée, appliquer le workflow du dépôt : contrôles appropriés de `build_ingredients.py --check`, `build_multilingual_ingredient_mapping.py --check` et `build_knowledge_docs.py --check`, après inspection des outils; parité des actifs et non-régression historique. Les règles d'origine restent hors périmètre, donc aucune raison actuelle d'exécuter leur builder. Sélectionner les seuls tests métier nécessaires; aucune suite complète ou mutation par défaut.
7. Relire le diff et contrôler que les alias courts de `meat`, les graisses et tous les propriétaires historiques sont préservés. Distinguer couverture des quatre formes de base, couverture des étiquettes complètes et risque de mention descriptive. Documenter les tests exécutés et leurs limites dans le rapport de ce futur lot.

La préparation peut donc avancer sur les formulations longues de viande séparée mécaniquement. Les décisions bloquantes restantes pour les cretons et les cuisses de grenouille sont la preuve d'usage d'ingrédient et l'acceptabilité des contextes négatifs; les sigles restent en attente. **GO_WITH_WARNINGS concerne cette préparation uniquement, sans autorisation d'import automatique.**
