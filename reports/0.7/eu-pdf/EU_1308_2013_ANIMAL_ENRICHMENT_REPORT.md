# EU 1308/2013 — revue préalable d’enrichissement animal v0.7

Date : 2026-10-05. Résultat : **ARRÊT AVANT IMPORT**. Aucun enrichissement n’a été appliqué. La liste ci-dessous est une proposition documentaire, pas des données validées ni une modification des statuts.

## Décision

L’enrichissement est utile, mais aucun importeur inspecté ne peut consommer cette nouvelle liste avec son interface actuelle. L’importeur 1308 ne lit ni CSV ni sélection externe : il traite seulement sa constante A de 8 concepts/44 formes. Les importeurs miel et lait conservé sont déjà synchronisés, avec `changes=0` dans leurs dry-runs. Relancer leurs écritures n’apporterait aucun des nouveaux noms retenus.

Le workflow prévoit, à son étape 8, l’exécution de l’importeur existant après examen de la transformation. Dans le périmètre demandé — changements de données — je n’ai ni étendu le code de cet importeur, ni injecté une autre liste à l’exécution, ni écrit les JSON à la main pour contourner cette étape. Une extension ciblée du flux 1308 est nécessaire pour le lot proposé ; elle reste hors du périmètre de cette exécution.

Des décisions éditoriales bloquent aussi certains sous-lots : cire d’abeille avec deux propriétaires historiques, rattachement des graisses laitières, portée du pollen, classification des produits apicoles nouveaux et contextes végétaux de noms génériques. Ces cas ne bloquent pas la préparation documentaire des autres propositions.

## Départ et conservation des fichiers

- HEAD : `6be60933df464dccb857b8d3287825011567344b`.
- `git status --short --branch` : branche master suivie par origin/master ; seul `?? reports/0.7/eu-pdf/` est présent.
- Un seul worktree : `C:/Users/seb/AndroidStudioProjects/IsitVegan`.
- Diffs suivis et indexés vides au départ.
- Les sept documents déjà présents dans le dossier de revue, dont `1308-animal-candidate-review.md`, sont des changements locaux préexistants et sont préservés.
- Une empreinte SHA-256 de chacun des 379 fichiers suivis ou non suivis existants a été prise avant toute écriture de cette passe. Les PDF sont vérifiés séparément contre le manifeste.

Consignes lues intégralement : AGENTS.md, [skill de validation](../../../.agents/skills/knowledge-import-validation/SKILL.md), [source-adapters.md](../../../.agents/skills/knowledge-import-validation/references/source-adapters.md) et [import-workflow.md](../../../.agents/skills/knowledge-import-validation/references/import-workflow.md). Le skill demande « Detect collisions and ambiguity before accepting aliases or mappings ». source-adapters prévoit l’arrêt avant mutation quand « mappings are ambiguous ». Cela concerne les sous-lots signalés, sans imposer une confirmation générale pour les lectures et les vérifications.

## Sources, droits de réutilisation et preuves

Source principale : quatre PDF locaux CELEX **02013R1308**, consolidation **18.08.2026**, 219 pages chacun, dans `reference-input/eu-food-labelling/02-sector-product-standards/`. Les identités, dates, langues, tailles, pagination et SHA-256 ont été contrôlés sur les fichiers courants. Le tableau calculé ci-dessous présente leurs empreintes, ainsi que celles des sources miel et lait conservé utilisées par les dry-runs voisins.

La [notice officielle EUR-Lex](https://eur-lex.europa.eu/content/legal-notice/legal-notice.html?locale=en), retrouvée dans l’index web officiel le 2026-10-05, indique que les textes consolidés sont sous CC BY 4.0, avec mention de la source et des modifications. L’ouverture directe a rencontré la protection JavaScript du site ; la notice indexée est la preuve consultée. Je n’applique pas par extrapolation la licence générale du portail Publications Office aux PDF. La notice spécifique EUR-Lex couvre ici les textes consolidés ; aucune exception particulière n’a été repérée dans les passages examinés. Cette vérification ne constitue pas un avis juridique.

Attribution de la liste : Union européenne, Parlement européen et Conseil, règlement (UE) nº 1308/2013, CELEX 02013R1308, consolidation du 18.08.2026, versions FR/NL/EN/DE. Modifications documentaires : sélection de sous-termes, regroupement par emplacement juridique, comparaison au corpus et notes de revue ; aucune modification des PDF. Les records de source existants sont conservés. Une future intégration devra maintenir cette attribution et documenter la sélection dans sa provenance.

Le rapport animal préliminaire indique lui-même qu’il n’avait pas accès au dépôt. Le CSV large contient des hypothèses. Ils ont servi de pistes ; aucune décision n’est fondée sur le seul champ `NO_EXACT_CURRENT_LANGUAGE_ALIAS`.

## Méthode de revue

1. Lecture des règles, du rapport animal, du rapport d’extraction et des trois importeurs concernés, y compris leurs gardes, transformations et modes.
2. Chargement en lecture seule des données et sources actuelles ; comparaison des noms, alias canoniques, alias par langue et variantes OCR. Les relations historiques et statuts ne sont pas modifiés.
3. Nouvelle extraction directe des pages pertinentes des PDF avec pypdf, indépendamment des TXT historiques : I XV–XXIV p.143–154, II VII–IX p.162–163, IV p.166 et VII III–IV p.182/184. Inspection lexicale ciblée, pas certification visuelle intégrale.
4. Sélection manuelle de noms alimentaires simples, sans code NC, description commerciale complète ni reconstruction de mot cassé. Les familles correspondent aux préoccupations de la revue animale et aux dérivés voisins directement attestés.
5. Vérification de chaque forme retenue comme sous-chaîne littérale de sa page primaire, avec offset et contexte. La forme proposée conserve casse et graphie attestées. Aucune traduction de langue manquante n’est inventée.
6. Alignement par item réglementaire, définition ou ligne NC identique ; une coordination comme « Schapen- of geitenvet » n’est pas décomposée en traductions supplémentaires.
7. Séparation des propriétaires exacts et des mots déjà disponibles dans les expressions. La normalisation NFKD/ASCII de l’importeur et celle de TextNormalizer (NFD, œ→oe, æ→ae) sont distinguées.

Le CSV de cette passe conserve les références vers les candidate_id précédents lorsqu’un terme complet correspondant existe ; un sous-terme relevé directement peut n’avoir aucun ID précédent. Cela n’en fait pas une traduction inventée : sa preuve est la nouvelle occurrence PDF.

### Limite de la mesure de couverture

Les nombres d’alias potentiels ci-dessous sont des écarts de vocabulaire exact, pas un nombre de nouveaux verdicts ni de nouvelles reconnaissances effectives. Le matcher utilise déjà les noms canoniques, alias et sous-termes ; il applique aussi des règles de contexte et de résidu. Un lait entier peut donc déjà être partiellement reconnu via « lait ». Les champs runtime du CSV reprennent statiquement la normalisation et les limites de mots du code pour signaler ces possibilités ; ce ne sont pas des résultats d’exécution de l’application.

Une mesure réelle du gain après import nécessiterait des essais ciblés avant/après sur les mêmes expressions, dans chaque langue, comprenant les contextes végétaux, arômes, traces et mots inconnus. Aucun gain de reconnaissance ni test métier n’est revendiqué dans cette passe sans import.

## Décisions sémantiques importantes

- **Babeurre** : item IV de VII III 2(a), noms FR/NL/EN/DE attestés. Le produit est distinct du lait générique ; aucune entrée correspondante n’est présente dans les noms/alias examinés. `karnemelk` et `botermelk` sont explicitement reliés par « of ». Nouveau concept proposé, pas créé.
- **Lactosérum, caséines, lait, jaune d’œuf, abats et graisses animales** : concepts existants. Proposer les noms manquants et leurs mappings ; ne pas créer un concept par pluriel, taux de graisse, animal ou présentation. Le jaune singulier FR existe déjà dans egg ; le pluriel et les formes NL/EN/DE sont examinés séparément.
- **Butteroil / matière grasse laitière anhydre** : items V et VII distincts. Le lexique DE de butter possède déjà `Butterreinfett`, absent de ses alias canoniques. Cela impose une décision sur le périmètre existant avant de créer une matière grasse laitière nouvelle ou de tout fusionner sous butter. La seule proximité de composition ne prouve pas une synonymie.
- **Crème, room, Rahm, beurre, boter** : laitier dans l’item source, mais alias génériques sensibles aux produits végétaux et contextes aromatiques. Le matcher protège déjà butter/milk/cream dans certains contextes ; aucune nouvelle règle n’est ajoutée. La sécurité d’alias supplémentaires doit être démontrée dans les quatre langues avant écriture.
- **Cire d’abeille** : beeswax et e901 existent tous deux. Leurs noms français normalisés se recouvrent ; leurs statuts historiques diffèrent. `Bienenwachs` est déjà un alias canonique de e901. Il ne faut ni compter ce mot comme nouveau ni le réassigner automatiquement à beeswax. Le validateur multilingue ne détecte pas cette collision de noms canoniques : son résultat « collisions: 0 » porte sur les alias par langue qu’il parcourt.
- **Gelée royale / propolis** : la ligne NC 0410 est explicitement comestible, la ligne NC 0511 explicitement non comestible. Seule la première est utilisée pour proposer des noms alimentaires. La définition II IX confirme le regroupement ; `Kittharz` est bien le nom allemand attesté. Un nom nouveau ne valide pas à lui seul le choix entre les classifications alimentaires du projet.
- **Pollen** : apicole dans la source, mais le nom générique ne garantit pas une collecte par les abeilles. Pas d’alias « bee pollen » ajouté sans occurrence correspondante, pas de statut animal attribué au pollen végétal.
- **Graisses / foies** : sous-termes alimentaires attestés rattachables aux concepts animal_fat et edible_offal existants. Les lignes pharmaceutiques, les codes et leurs exceptions restent exclus. Le français `lard` n’est pas fusionné avec le saindoux ; les quatre sous-termes du saindoux sont pris dans la parenthèse de NC 1501.
- **Miel / poudres de lait** : honey/milk existent et les importeurs voisins sont synchronisés. Les formes déjà importées ne sont pas comptées comme nouveaux concepts. Les quatre noms de miel naturel restent des noms proposés d’un concept existant. Aucun lot allergènes 1169/2011 n’est ajouté depuis ce règlement.

## Éléments écartés

Le CSV inclut une petite table d’exclusion traçable : carcasse, demi-carcasse, gelée royale/propolis non comestibles, jaunes d’œufs impropres à l’alimentation, sous-positions pharmaceutiques, vers à soie, aliments pour chiens/chats, descriptions NC collectives et une césure néerlandaise. Ces entrées ont `EXCLUDED_FROM_PROPOSAL` et aucun alias proposé.

Les codes, descriptions douanières complètes, matières industrielles et mots reconstruits de césures sont exclus des ajouts. Un nom simple présent dans une ligne NC n’est pas automatiquement une catégorie douanière : sa valeur lexicale et son contexte alimentaire sont examinés séparément. La sélection n’est pas une nouvelle extraction exhaustive des 3 870 candidats du pilote.

## Fonctionnement et validations des outils

| Outil | Modes inspectés | Effets / limites |
|---|---|---|
| import_eu_agricultural_products_regulation.py | --dry-run / --check / --write obligatoirement exclusifs | A/NEW codés en dur ; pas d’entrée de candidats ; sous-chaînes normalisées dans tous les PDF ; écrit trois JSON avec --write ; --check signifie synchronisation actuelle, pas revue générale |
| import_eu_honey_directive.py | --dry-run / --write, exactement un | 53 noms fixes de honey ; contrôle statut/source/collisions ; peut remplacer la reason de honey ; --check absent |
| import_eu_preserved_milk_directive.py | --dry-run / --write, exactement un | Noms fixes de milk/cream ; vérifie les PDF, statuts, collisions et source ; peut remplacer la reason de milk ; --check absent |
| build_ingredients.py | --check ou génération sans --check | Schéma et collisions d’alias canoniques littéraux ; parité assets ingrédients/alias |
| build_multilingual_ingredient_mapping.py | --check / --write / --report | Métadonnées, alias/langues, collisions multilingues, document généré et parité ; le mode --check seul n’échoue pas sur tous les avertissements du rapport |
| build_knowledge_docs.py | --check ou génération sans --check | Parité de la documentation générée, aucune mutation en --check |

Les fichiers de ces outils ont été lus avant exécution. Aucun mode --write ni régénérateur n’a été exécuté. Aucun importeur n’a été modifié ou importé comme module pour substituer ses constantes.

**Refus automatique :** la tentative `python tools/import_eu_agricultural_products_regulation.py --dry-run` a été refusée avant création du processus. Le contrôle automatique d’approbation invoque l’interdiction de tous les modes de cet importeur dans la passe documentaire précédente, malgré la nouvelle demande conditionnelle d’enrichissement. Ce dry-run n’est donc pas déclaré exécuté ni réussi. Aucun contournement ou exécution indirecte n’a eu lieu. Les comparaisons du code et les validations génériques indépendantes ont été poursuivies.

## Suite nécessaire pour une intégration

Un lot de seuls alias sur concepts existants peut être isolé des nouveaux concepts et cas ambigus. Avant import, il faudrait étendre de façon ciblée le flux 1308 pour accepter ce lot vérifié et contrôler ses pages/identités, sans reprendre toutes les catégories de marché. Cette modification de script sort du périmètre données de cette passe. Les droits d’exécution de l’importeur doivent aussi être clarifiés après le refus automatique.

Ensuite : dry-run avec comparaison exacte avant/après ; aucune suppression, réassignation, modification de statut/reason existant ; validation des mappings ; write documenté ; builders et parité ; essais métier ciblés et mesure de couverture réelle ; idempotence et diff final. Les nouveaux produits apicoles et les frontières des graisses laitières demandent leur décision éditoriale séparée.

Les résultats calculés et les vérifications finales figurent ci-dessous.

## Tableau des familles examinées

Chaque forme ci-dessous est attestée telle quelle dans le PDF indiqué, sans césure interne. Les formes d’une même famille suivent le même emplacement réglementaire. Les propositions de rattachement restent à revoir.

| Famille | Concept existant associé / proposition | FR | NL | EN | DE | Page PDF commune | Traitement |
|---|---|---|---|---|---|---:|---|
| whey | whey | lactosérum | wei | whey | Molke | 182 | LANGUAGE_METADATA_MISSING_CANONICAL_PRESENT, PROPOSED_ALIAS_EXISTING_CONCEPT |
| cream | cream | crème | room | cream | Rahm | 182 | CONTEXT_REVIEW |
| butter | butter | beurre | boter | butter | Butter | 182 | CONTEXT_REVIEW |
| buttermilk | Nouveau à revoir : buttermilk | babeurre | karnemelk; botermelk | buttermilk | Buttermilch | 182 | NEW_CONCEPT_REVIEW |
| butteroil | butter | butteroil | boterolie | butteroil | Butteroil | 182 | CONCEPT_BOUNDARY_REVIEW |
| casein | casein | caséines | caseïne | caseins | Kaseine | 182 | LANGUAGE_METADATA_MISSING_CANONICAL_PRESENT, PROPOSED_ALIAS_EXISTING_CONCEPT |
| anhydrous_milk_fat | butter | matière grasse laitière anhydre | watervrij melkvet | anhydrous milk fat | wasserfreies Milchfett | 182 | CONCEPT_BOUNDARY_REVIEW |
| raw_milk | milk | lait cru | rauwe melk | raw milk | Rohmilch | 184 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| whole_milk | milk | lait entier | volle melk | whole milk | Vollmilch | 184 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| semi_skimmed_milk | milk | lait demi-écrémé | halfvolle melk | semi-skimmed milk | teilentrahmte Milch; fettarme Milch | 184 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| skimmed_milk | milk | lait écrémé | magere melk | skimmed-milk; skimmed milk | entrahmte Milch; Magermilch | 184 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| natural_honey | honey | Miel naturel | Natuurhoning | Natural honey | Natürlicher Honig | 147 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| beeswax | beeswax | Cire d'abeille | Bijenwas | Beeswax | Bienenwachs | 147 | LANGUAGE_METADATA_MISSING_CANONICAL_PRESENT, OWNER_COLLISION_REVIEW |
| royal_jelly | Nouveau à revoir : royal_jelly | Gelée royale | koninginnengelei | Royal jelly | Gelée Royale | 147 | NEW_CLASSIFICATION_REVIEW |
| propolis | Nouveau à revoir : propolis | propolis | propolis | propolis | Kittharz | 147 | NEW_CLASSIFICATION_REVIEW |
| pollen | Nouveau à revoir : pollen | Pollen | Pollen | Pollen | Blütenpollen | 147 | ORIGIN_SCOPE_REVIEW |
| egg_yolk | egg | jaunes d'œufs | eigeel | egg yolks | Eigelb | 146 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| edible_offal | edible_offal | Abats comestibles | Eetbare slachtafvallen | Edible offal | Genießbare Schlachtnebenerzeugnisse | 143 | LANGUAGE_METADATA_MISSING_CANONICAL_PRESENT, PROPOSED_ALIAS_EXISTING_CONCEPT |
| poultry_liver | edible_offal | Foies de volailles | Levers van pluimvee | Poultry livers | Geflügelleber; Geflügellebern | 146 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| pig_fat | animal_fat | graisse de porc | Varkensvet | Pig fat | Schweinefett | 145 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| lard | animal_fat | saindoux | reuzel | lard | Schweineschmalz | 145 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| poultry_fat | animal_fat | Graisses de volaille | Vet van gevogelte | Poultry fat | Geflügelfett | 146 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| bovine_fat | animal_fat | Graisses des animaux de l'espèce bovine | Rundervet | Fats of bovine animals | Fett von Rindern | 144 | PROPOSED_ALIAS_EXISTING_CONCEPT |
| sheep_goat_fat | animal_fat | Graisse des animaux des espèces ovine et caprine | Schapen- of geitenvet | Fats of sheep or goats | Fett von Schafen oder Ziegen | 146 | PROPOSED_ALIAS_EXISTING_CONCEPT |

## Décomptes de la proposition

101 formes/langues sur 24 familles examinées ; 10 exemples d’exclusion documentés. Toutes les 111 lignes ont applied=NO. Le CSV comporte 30 colonnes en UTF-8. Les lignes proposées sont REVIEW_REQUIRED ; les exclusions EXCLUDED_FROM_PROPOSAL.

| Décision | Lignes | FR | NL | EN | DE |
|---|---:|---:|---:|---:|---:|
| ARTIFACT_EXTRACTION | 1 | 0 | 1 | 0 | 0 |
| CONCEPT_BOUNDARY_REVIEW | 8 | 2 | 2 | 2 | 2 |
| CONTEXT_REVIEW | 8 | 2 | 2 | 2 | 2 |
| LANGUAGE_METADATA_MISSING_CANONICAL_PRESENT | 7 | 2 | 3 | 2 | 0 |
| NEW_CLASSIFICATION_REVIEW | 8 | 2 | 2 | 2 | 2 |
| NEW_CONCEPT_REVIEW | 5 | 1 | 2 | 1 | 1 |
| ORIGIN_SCOPE_REVIEW | 4 | 1 | 1 | 1 | 1 |
| OUT_OF_SCOPE | 7 | 6 | 1 | 0 | 0 |
| OWNER_COLLISION_REVIEW | 2 | 1 | 0 | 0 | 1 |
| PROPOSED_ALIAS_EXISTING_CONCEPT | 59 | 13 | 13 | 15 | 18 |
| REGULATORY_CATEGORY_EXCLUDED | 2 | 0 | 0 | 2 | 0 |

Sous-lot potentiel sur concepts existants hors ambiguïtés explicitement séparées : 66 formes/langues, dont 59 sans propriétaire global exact selon la normalisation de l’importeur et 7 déjà présentes globalement, mais sans alias de langue exact.
Parmi les 59 formes globales absentes, 22 contiennent déjà un mot/alias courant susceptible d’être reconnu et 0 ont un propriétaire canonique exact avec la normalisation runtime. Cela interdit de présenter 59 comme un nombre de nouvelles reconnaissances. Les propositions ne sont pas encore un lot validé.

| Langue | Sous-lot potentiel sur concepts existants | Formes globales exactes absentes | Avec mot/alias courant déjà présent (parmi ces formes absentes) |
|---|---:|---:|---:|
| FR | 15 | 13 | 7 |
| NL | 16 | 13 | 4 |
| EN | 17 | 15 | 7 |
| DE | 18 | 18 | 4 |

Gain effectivement appliqué : **0 concept, 0 alias canonique, 0 alias multilingue, 0 mapping, 0 changement de statut**. Aucun quota visé.

## Sources vérifiées pour cette passe

| PDF | Octets | Pages | SHA-256 conforme au manifeste courant |
|---|---:|---:|---|
| CELEX_02013R1308-20260818_FR_TXT.pdf | 2214937 | 219 | 5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c |
| CELEX_02013R1308-20260818_NL_TXT.pdf | 2149221 | 219 | 99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74 |
| CELEX_02013R1308-20260818_EN_TXT.pdf | 2105606 | 219 | b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11 |
| CELEX_02013R1308-20260818_DE_TXT.pdf | 2159699 | 219 | 64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda |
| CELEX_02001L0110-20260614_FR_TXT.pdf | 240096 | 10 | e22e34e4368bf7227d8e2434fd241503ee57765d66651685a16d68e60c1e860c |
| CELEX_02001L0110-20260614_NL_TXT.pdf | 237013 | 10 | f88cbd8f565aecbce88f7ec020dd976d76c3ca818d908200f1ee2568cdb2e593 |
| CELEX_02001L0110-20260614_EN_TXT.pdf | 238553 | 10 | 2f1e0a51b35ae8ce765ec22abacfb6fda571971c5c1f0b4f07860b4b56b16fd6 |
| CELEX_02001L0110-20260614_DE_TXT.pdf | 227909 | 10 | 5a4a137208ab0a6f7fbded9a108dc83b49a31cc185de15e21f42c7e0f69faea5 |
| CELEX_02001L0114-20260614_FR_TXT.pdf | 210939 | 8 | 8ccd09a93c51cf7520a6b485ca24d32ea27ec9a8165527b4876e3c0e24b8d130 |
| CELEX_02001L0114-20260614_NL_TXT.pdf | 213056 | 8 | 76b98b6adfdb7377140cd99105097f58f3954acb4618e289e83eec5cad248b8e |
| CELEX_02001L0114-20260614_EN_TXT.pdf | 207235 | 8 | cba10f58c618773123a3830fc425ad7dcc8a2f7283790c196feaea78d11208c3 |
| CELEX_02001L0114-20260614_DE_TXT.pdf | 213037 | 8 | 9629aef4578c0098bbe6071cab624cb63bc8b27a6c9cbd440091ca73e45fd0cb |

## Commandes de validation réellement tentées / exécutées

| Commande | Exécution / résultat |
|---|---|
| python tools/import_eu_agricultural_products_regulation.py --dry-run | Refusée avant lancement par le contrôle automatique ; aucun résultat de l’importeur. |
| python tools/import_eu_honey_directive.py --dry-run | Exécutée, exit 0 : CELEX 02001L0110 Annex I: 4 languages, 53 joined aliases, 0 ambiguous aliases changes=0 |
| python tools/import_eu_preserved_milk_directive.py --dry-run | Exécutée, exit 0 : CELEX 02001L0114 Annexes I-II: {'FR': 23, 'NL': 20, 'EN': 23, 'DE': 23}, 0 ambiguous aliases changes=0 |
| python tools/build_ingredients.py --check | Exécutée, exit 0 : Knowledge base is valid and app\src\main\assets\ingredients.json is current. |
| python tools/build_multilingual_ingredient_mapping.py --check --report | Exécutée, exit 0 : { "concepts": 422, "aliases": { "FR": 494, "EN": 445, "NL": 520, "DE": 470, "IT": 34, "ES": 33 }, "missingConcepts": [ "cereals" ], "collisions": 0, "placeholders": [], "assetParity": true, "declaredMappings": 1996 } Multilingual mapping is current: docs\generated\multilingual-ingredient-mapping.md |
| python tools/build_knowledge_docs.py --check | Exécutée, exit 0 : Knowledge documentation is current: docs\generated\base-connaissances-data.md |

Baseline validée : 479 concepts canoniques ; 1 668 entrées d’alias multilingues ; 1 996 mappings déclarés. Le rapport du builder multilingue parcourt 422 concepts, compte FR 494 / NL 520 / EN 445 / DE 470 / IT 34 / ES 33 formes (alias et variantes OCR), indique assetParity=true, aucun placeholder et aucune collision par langue. Il signale cereals comme concept absent : avertissement préexistant, non restauré ou reclassé.

Ces validations sont celles de l’état courant, sans import. Les checks de builders ont des rôles différents ; leur succès ne valide pas les nouvelles propositions ni toutes les collisions canoniques. Pas de contrôle d’origine supplémentaire : origin_qualifier_rules.json reste inchangé. Pas de build Android ni tests métier : aucune donnée ou logique applicative modifiée.

## Commandes et outils documentaires exécutés

- Git : status --short --branch, rev-parse HEAD, worktree list --porcelain, diff --stat, ls-files et ls-files --others --exclude-standard pour les empreintes de conservation ; contrôles finaux ci-dessous.
- PowerShell : Get-Content -Encoding UTF8 des instructions, importeurs, builders et extraits ciblés du matcher ; recherches rg ciblées des consignes, sources, modes et cas protégés.
- Python via snippets python - : lectures JSON/CSV, SHA-256, AST en lecture seule de A, PdfReader/extract_text sur les preuves ciblées, comparaisons de vocabulaire, export et assertions du CSV. Aucun import du module d’import 1308.
- Web : ouverture et recherche de la notice officielle de réutilisation EUR-Lex ; source juridique primaire des candidats toujours locale.
- Écritures : uniquement le nouveau rapport et le nouveau CSV sous reports/0.7/eu-pdf/. Les documents de la passe précédente restent intacts.

Incidents documentaires corrigés : diagnostic importlib.util nécessitant un import explicite ; guillemet erroné dans un snippet de préparation corrigé avant exécution de l’export ; sorties exploratoires longues tronquées à l’écran. Les vérifications finales relisent les fichiers et les PDF, sans dépendre des sorties tronquées.

### Déduplication et limites des compteurs

Les 66 formes/langues littérales du sous-lot potentiel donnent 65 clefs de mapping distinctes concept/langue/normalisation. Les 59 formes/langues absentes globalement donnent 58 formes globales normalisées et 58 clefs concept/langue distinctes. Ces variantes ne sont pas comptées comme des concepts différents. Par exemple skimmed-milk et skimmed milk sont deux graphies attestées, mais une même clef normalisée.

La lecture AST de A et la comparaison statique de ses 44 formes aux alias canoniques, alias par langue et mappings actuels ne trouvent aucun manque. Cette vérification est une lecture des données et du code ; elle ne simule pas main(), ne vérifie pas tous ses effets et ne remplace pas le dry-run refusé. La liste fixe ne contient pas les nouvelles formes du sous-lot proposé.

## Vérifications finales exécutées

- CSV UTF-8 strict : 111 lignes, 30 colonnes uniques, 111 review_id distincts, aucun champ de source essentiel vide ni colonne excédentaire.
- Chaque ligne est contrôlée contre son PDF courant : hash, langue, CELEX, consolidation, page 1–219, surface exacte à l’offset, extrait de contexte littéral, référence juridique et justification.
- Toutes les 101 formes proposées sont continues, sans U+00AD ni retour de ligne ; aucune forme corrigée de césure n’est proposée. Les 10 exclusions conservent leur preuve brute.
- Toutes les lignes restent applied=NO ; aucun statut alimentaire proposé ou attribué dans le CSV.
- Les 379 fichiers existants au départ ont été rehashés : aucune modification ni disparition, y compris les sept documents locaux préexistants. Seuls les deux nouveaux livrables de cette passe sont apparus.
- HEAD identique ; diffs suivis et indexés toujours vides ; git diff --check retourne 0. Le nouveau Markdown est également contrôlé sans blancs de fin de ligne.
- Aucun changement à knowledge/, aux assets, aux moteurs, à l’OCR, aux tests, aux scripts/importeurs, aux versions ou aux documents générés ; aucune écriture d’import, aucun commit/push.

Commandes finales supplémentaires exécutées : git diff --name-only ; git diff --cached --name-only ; git diff --check ; git ls-files ; git ls-files --others --exclude-standard ; relecture et assertions Python du CSV contre les pages PDF ; comparaison des empreintes avant/après ; git status --short.

Livrables nouveaux : EU_1308_2013_ANIMAL_ENRICHMENT_REPORT.md et EU_1308_2013_ANIMAL_ENRICHMENT_REVIEW.csv, uniquement sous reports/0.7/eu-pdf/.

CSV final : 95320 octets ; SHA-256 a669e8a82d4581d9e6648f93fc960f31f61556f55cd9812eaeb6258a04db2a1d.
