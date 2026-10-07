# Proposition de lot - viandes UE 1308/2013 (v0.7)

Date : 2026-10-06. HEAD constate : `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`. Cette revue documentaire ne realise aucune importation. Toutes les lignes du [CSV de propositions](EU_1308_2013_MEAT_IMPORT_PROPOSALS.csv) sont `REVIEW_REQUIRED` et `applied=NO`. Le CSV de revue anterieure reste une source de pistes et de preuves, jamais un fichier d'import direct.

## Base de la proposition

Les sources sont les quatre PDF CELEX 02013R1308 locaux, consolidation du 18.08.2026, de 219 pages chacun. Le CSV relie chaque decision a l'ID de revue documentaire, au PDF, a la langue, a la forme exacte extraite, a la page PDF (base 1), a la rubrique, a l'offset caractere (base 0), au contexte et au SHA-256. L'extrait ne remplace pas la verification visuelle de la page.

Instructions lues : `AGENTS.md`, le skill `knowledge-import-validation` et ses references `source-adapters.md` et `import-workflow.md`, ainsi que les invariants de connaissance et la documentation de matching, base et verdict. Le schema, les alias multilingues, les statuts, l'importeur sectoriel et le builder ont ete inspectes en lecture seule. Aucun importeur, builder ou test n'a ete lance.

## A. Viandes par espece

Les concepts suivants sont proposes avec le statut ingredient existant `NON_VEGAN`. Les termes de l'espece deja possedes par `meat` restent sur `meat`; aucune reaffectation n'est proposee.

| ID envisage | Portee et preuves | Formes exactes a examiner | Etat/reconnaissance et decision |
|---|---|---|---|
| `bovine_meat` | Viande bovine, annexe I XV, PDF p.143 | FR `Viandes des animaux de l'espece bovine`; NL `Vlees van runderen`; EN `Meat of bovine animals`; DE `Fleisch von Rindern` | `meat` est `NON_VEGAN`; les formes courtes `Viande bovine`, `Rundvlees`, `beef`, `Rindfleisch` lui appartiennent deja et sont reconnues. Concept bovin separe propose; ne pas transferer les formes courtes. Risque: aroma, description ou alternative vegetale. |
| `veal_meat` | Viande de veau, annexe VII I.III.1.A, p.172 | FR `viande de veau`; NL `Kalfsvlees`; EN `Veal`; DE `Kalbfleisch` | `veau` et `Veal` sont deja reconnus sous `meat`; FR compose partiel, NL/DE absents. Concept separe propose; conserver les alias historiques. Les noms de vente V/Z et variantes par pays restent differes. |
| `pork_meat` | Viande porcine, annexe I XVII, p.145 | FR `Viandes des animaux de l'espece porcine domestique`; NL `Vlees van varkens`; EN `Meat of domestic swine`; DE `Fleisch von Hausschweinen` | Les formes courtes `Viande de porc`, `Varkensvlees`, `pork`/`pigmeat`, `Schweinefleisch` restent proprietes de `meat`. Proposer les formes longues uniquement, avec risque d'alternative/arome. |
| `sheep_meat` | Viande ovine, annexe I XVIII, p.148 | NL `schapenvlees`; EN `Sheepmeat` | Pas de forme autonome FR/DE dans les preuves examinees. Ne pas traduire ni fabriquer d'alias. |
| `goat_meat` | Viande caprine, annexe I XVIII, p.148 | NL `geitenvlees`; EN `goatmeat`; DE `Ziegenfleisch` | Pas de forme autonome FR dans les preuves examinees. Distinguer de lait de chevre. |
| `horse_meat` | Viande de cheval, annexe I XXIV section 2, p.156 | FR `Viandes de cheval`; NL `Vlees van paarden`; EN `Meat of horses`, `Horsemeat`; DE `Fleisch von Pferden` | Concept distinct propose. Risque d'usage descriptif du nom d'espece; seul le contexte d'ingredient justifie le match. |

### Volaille : candidats sous garde de contexte

L'annexe VII V.I, p.185, enumere des especes dans les regles de mise sur le marche de viande/preparations/produits de volaille. Les formes exactes disponibles figurent dans le CSV : coqs, poules, hanen, kippen, `Gallus domesticus` (taxon latin imprime dans le PDF EN), Huhner; canards, eenden, ducks, Enten; oies, ganzen, geese, Ganse; dindons/dindes, kalkoenen, turkeys, Truthuhner; pintades, parelhoenders, guinea fowls, Perlhuhner.

IDs candidats : `chicken_meat`, `duck_meat`, `goose_meat`, `turkey_meat`, `guinea_fowl_meat`. Ce sont des noms d'especes, pas des denominations explicites de chair dans chaque occurrence. Les alias sont context-required : ne pas les activer avant un benchmark sur les oeufs, le lait d'espece, les aromes, les descriptions et substituts vegetaux. `Gallus domesticus` n'est pas traduit en anglais.

Alias historiques de `meat` a conserver, sans duplication ni transfert : formes pour boeuf/beef, veau/veal, porc/pork, agneau/lamb, viande bovine, Rundvlees, Varkensvlees, Rindfleisch, Schweinefleisch et formulations collectives ovine/caprine et volaille. Les lignes correspondantes sont marquees `RETAIN_HISTORICAL_OWNER` dans le CSV.

## B. Derives

| Concept | Preuve et decision proposee |
|---|---|
| `poultry_meat_preparation` (existant, `NON_VEGAN`) | Annexe VII V.II.5-6, p.185-186. Les formes generiques des quatre langues sont deja couvertes. Proposer uniquement les formes qualifiees fraiche/vers/fresh/frische; elles sont actuellement partielles ou absentes. |
| `poultry_meat_product` (nouveau) | Annexe VII V.II.7, p.186 : FR `produit a base de viande de volaille`, NL `Pluimveevleesproduct`, EN `poultrymeat product`, DE `Geflugelfleischerzeugnis`. Distinct de preparation selon la rubrique. Le renvoi a 853/2004 et la composition precise n'ont pas ete verifies; decision avant import requise. |
| `animal_blood` (nouveau) | Annexe I XVII, p.145, rubrique de preparations/denrees a base de sang. Le CSV contient les formes source `sang`, `bloed`, `blood`, `Blut` dans ce contexte. Ne pas importer ces mots nus : blood orange et expressions seraient des collisions. Garder uniquement les syntagmes qualifies par animaux dans une future liste d'alias. |
| `pork_cuts` (nouveau, groupe) | Annexes I XXIV sections 1, p.148-149 et produits prepares p.154. Groupe limite a jambon et poitrine : `Jambons`, `Hammen`, `Hams`, `Schinken`; `Poitrines (entrelardees)`, `Buiken (buikspek)`, `Bellies (streaky)`, `Baeuche (Bauchspeck)`. Un concept groupe evite un concept par coupe. Risque important d'alternatives vegetales. Le terme `bacon` n'est pas atteste dans le texte examine; ne pas l'inventer. |
| `meat_sausage` (nouveau, conditionnel) | Annexe I XVII, p.145 : `Saucisses`, `saucissons`, `Worst`, `Sausages`, `Wurste`. La rubrique est une categorie de produits carnes, mais les noms nus servent aussi aux substituts vegetaux. Conserver comme candidat, sans alias actif avant garde de contexte et benchmark. |
| `edible_offal` (existant, `NON_VEGAN`) | Abats comestibles, annexe I XV p.143; foies de volaille/oie/canard, annexe I XX p.146. Garder le concept existant, proposer seulement les formes completes qualifiees absentes et utiles. Ne pas creer un concept par organe; attention a liver oil. |
| Jus et extraits de viande (differe) | Annexe I XXIV section 1, p.154, rubrique collective `Extraits et jus de viande` et equivalences au CSV. Cette ligne douaniere ne prouve pas une denomination autonome ou son usage comme ingredient. Pas de nouvel ID sans preuve d'etiquette. |

Les reins, langue, pluck et autres elements issus d'une presentation/conformation de carcasse ne sont pas proposes comme ingredients. Farines/repas de viande, melanges homogenises et collectifs douaniers restent differes : leur rubrique ne prouve pas la composition d'un ingredient d'etiquette.

## C. Graisses animales (lot distinct)

`animal_fat` existe deja au statut `NON_VEGAN`. FR `saindoux`, NL `reuzel`, EN `lard`, DE `Schweineschmalz` sont deja reconnus : ne pas creer de concept saindoux. Les graisses porcines NL/EN/DE, ainsi que les graisses bovines, ovines/caprines et de volaille du lot deja commite, restent inchangees. Proposer la forme plurielle FR `Graisses de porc` comme alias de `animal_fat`; le matcher la reconnaissait seulement partiellement via `meat`.

La forme FR `Lard` de la rubrique de gras non fondu (p.145) entre en collision normalisee avec l'alias EN `lard`, deja proprietaire de `animal_fat`, mais le contexte la distingue du saindoux. Ne pas la transferer. `Spek`, `Schweinespeck` et formes longues sont differes en attendant une decision de propriete et une preuve d'usage ingredient. Les huiles/fractions `huile de suif`, `talkolie`, `tallow oil`, `Talgoel` (p.153) sont techniques; l'usage comme ingredient alimentaire n'est pas prouve. `huile de saindoux` est deja couverte et `lard oil` partiel; aucun nouveau concept d'huile n'est justifie ici.

## Statut ingredient et verdict : point bloquant verifie

Le schema ingrédient n'accepte pas `NON_VEGETARIAN`. `VeganStatus` et le validateur JSON n'autorisent que `VEGAN`, `VEGETARIAN`, `NON_VEGAN`, `UNCERTAIN`. L'enum de sortie `AnalysisVerdict` est distinct et contient `NON_VEGETARIAN`. Le moteur actuel transforme un ingredient `NON_VEGAN` en verdict detaille `NON_VEGETARIAN`; l'affichage principal dit « NON VEGAN ». Les diagnostics gardent `knownBlockingIngredients`, avec l'ingredient detecte et son chemin.

Il n'y a donc pas de blocage structurel a l'import de viandes : utiliser `NON_VEGAN` pour la chair animale conserve les invariants et conduit au verdict detaille `NON_VEGETARIAN`. Des concepts par espece rendent l'ingredient coupable plus precis dans les diagnostics. Ne pas ajouter de statut ni changer le moteur dans ce lot. Un besoin futur de statut ingredient distinct demanderait un mandat separe, puis une migration schema/asset/builder, une adaptation du moteur et de l'affichage, et des tests de non-regression. Cette sequence n'est pas necessaire a la proposition presente.

## Proprietaires, collisions et risques

Le CSV expose pour chaque preuve le proprietaire actuel, le statut et le resultat du matcher. `EXACT` est deja reconnu; `PARTIAL_CONTEXTUAL` ne reconnait qu'une partie et peut laisser un inconnu; `NONE` n'est pas reconnu. Il n'y a ni matching flou, ni traduction, ni stemming. Les alias historiques deja sous `meat` ne changent pas de proprietaire. Les futurs alias nouveaux devront etre controles contre les cles canoniques et multilingues avant toute importation.

Risques a couvrir au benchmark : viande nommee dans une alternative vegetale; arôme ou mention « saveur poulet/bacon »; oeufs de poule/oie/canard; lait de chevre; saucisse/jambon vegetaux; jus ou extrait vegetal. Tester aussi les mentions descriptives et les ingredients positifs avec preuve explicite de chair. Une reconnaissance erronee ne doit pas cacher les inconnus ni changer le verdict par heuristique.

## Differes et exclus

**Differes :** categorie de jeune bovin/veau rosé (classes V/Z et noms par pays); collectifs ovine/caprine; lapin et gibier uniquement dans une categorie collective de preparations/conserves NC 1602; autres equides (ane/mulet/bardot) uniquement en ligne collective commerciale; alias nus de jambon/poitrine/saucisse sans garde; jus/extraits; huiles/fractions de suif; gras non fondu dont le proprietaire collisionne; liver seul si le sens reste ambigu.

**Exclus :** animaux vivants (dont agneaux); concepts deduits d'une ligne douaniere collective; « bacon » absent de la source; pieces anatomiques attestees seulement comme parties de carcasse; traductions et variantes grammaticales inventees; game meat deduit de « game or rabbit » dans une rubrique de produits prepares/conserves.

## Limites et validations

L'extraction documentaire precedente a traite les quatre PDF locaux via pypdf et plusieurs controles plain/layout avec le meme extracteur. Cela ne remplace pas une inspection visuelle independante et peut manquer tableaux, colonnes, cesures et libelles en image. Les offsets sont ceux du texte extrait, non du fichier PDF. Les pages/rubriques doivent etre revérifiees visuellement avant import. Le rapport anterieur signale des extraits Markdown parfois mal ancres; utiliser en priorite page, offset et contexte du CSV documentaire.

Etat Git au depart : HEAD `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`, aucun changement suivi; les deux rapports documentaires non suivis deja presents ont ete preserves. Aucun importeur, builder, test, modification de connaissance, moteur, statut, version ou commit dans cette passe.

## Comptes exacts

- Concepts distincts nouveaux proposes : **15** (`animal_blood`, `bovine_meat`, `chicken_meat`, `duck_meat`, `goat_meat`, `goose_meat`, `guinea_fowl_meat`, `horse_meat`, `meat_sausage`, `pork_cuts`, `pork_meat`, `poultry_meat_product`, `sheep_meat`, `turkey_meat`, `veal_meat`).
- Concepts existants concernes : **4** (`meat`, `edible_offal`, `animal_fat`, `poultry_meat_preparation`).
- Alias candidats vers concepts existants : **13** cles uniques.
- Lignes explicitement preservant un proprietaire historique `meat` : **10**; aucun transfert.
- Lignes de familles/alias differes : **8**.
- Lignes CSV source-liees : **105**, toutes `REVIEW_REQUIRED`, `applied=NO`; donnees appliquees : **0**.

Les decisions a prendre avant tout import sont : accepter ou reduire les concepts par espece (notamment les especes de volaille sous garde); accepter le groupement jambon/poitrine et le concept saucisse conditionnel; confirmer les syntagmes complets pour sang et produit de volaille; fixer le benchmark de faux positifs; resoudre la collision `Lard`/`lard` et l'usage alimentaire des fractions grasses. Aucun point ne requiert `NON_VEGETARIAN` comme statut ingredient.
