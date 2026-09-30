# Audit documentaire FIC 1169/2011

## Décision

**GO documentaire, aucun import applicatif.** CELEX 02011R1169 est maintenant disponible sous forme d’extractions déterministes et d’extraits consultables. Le règlement établit les obligations d’information et les catégories d’allergènes ; il ne constitue pas, seul, une preuve suffisante pour créer ou reclassifier des ingrédients vegan.

## Sources et extraction

- CELEX : `02011R1169`, règlement (UE) n° 1169/2011 relatif à l’information des consommateurs sur les denrées alimentaires.
- Consolidation locale : `2025-04-01`.
- PDF primaires : `CELEX_02011R1169-20250401_{FR,NL,EN,DE}_TXT.pdf` dans `reference-input/eu-food-labelling/00-general-food-labelling/`.
- Générateur : `tools/extract_fic_reference.py`; bibliothèque d’extraction `pypdf`.
- Sorties : quatre TXT UTF-8 page par page, onze extraits thématiques et `SOURCE_MANIFEST.md` contenant hashes SHA-256, pages, caractères, date et limites.

Les TXT préservent un marqueur `===== PAGE n =====`, les titres, annexes, références d’articles et le texte des tableaux dans la mesure où il est extractible. Ils sont dérivés et ne remplacent jamais les PDF. Le contrôle `--check` recalcule toutes les sorties, refuse un caractère de remplacement Unicode et vérifie l’idempotence octet pour octet.

## Sections utiles

| Sujet | Référence FIC | Utilité prospective | Décision |
| --- | --- | --- | --- |
| Informations obligatoires | art. 9, 13–16 | bornes et libellés pour la segmentation OCR | documenter |
| Dénomination et liste d’ingrédients | art. 17–22 | sections, ordre et règles d’ingrédients composés | documenter ; étude dédiée avant changement parser |
| Ingrédients composés | art. 18 | éviter de confondre le nom d’un produit avec chacun de ses composants | documenter |
| Huiles et graisses végétales | annexe VII, partie A | candidats de formulation et de précision d’origine | candidat, ne pas importer depuis la seule catégorie |
| Protéines et amidons | annexe VII | vocabulaire technique, composition potentiellement variable | candidat `UNCERTAIN` |
| Arômes | annexe VII | contexte à exclure du matching d’un ingrédient réel | documenter pour règles contextuelles locales |
| Additifs et auxiliaires | art. 20, annexe VII | séparation étiquette / auxiliaire, compléter l’import additifs existant | documenter |
| Allergènes | annexe II, pages 36–37 EN | lexique et exceptions, sans moteur allergène automatique | inventaire seulement |
| Traces | aucune liste « may contain » normative dans les pages extraites | les mentions volontaires restent des avertissements hors verdict | conserver la logique actuelle |

## Inventaire des 14 allergènes réglementaires

Les formes ci-dessous sont les intitulés ou têtes de rubrique relevés dans l’annexe II locale. Elles désignent une catégorie réglementaire, pas nécessairement un ingrédient atomique. `relation` proposé : `RELATED_NOT_EQUIVALENT` pour la catégorie, puis alias vers un concept précis uniquement après revue.

| Catégorie | FR | NL | EN | DE | Concept / statut possible | Risque et décision |
| --- | --- | --- | --- | --- | --- | --- |
| Céréales à gluten | céréales contenant du gluten | glutenbevattende granen | cereals containing gluten | glutenhaltiges Getreide | `gluten`, VEGAN pour le gluten isolé | catégorie ≠ ingrédient ; documenter |
| Crustacés | crustacés | schaaldieren | crustaceans | Krebstiere | candidat `crustacean`, NON_VEGAN | catégorie large ; candidat |
| Œufs | œufs | eieren | eggs | Eier | `egg`, VEGETARIAN | concept déjà présent ; documenter |
| Poissons | poissons | vis | fish | Fische | `fish`, NON_VEGAN | concept déjà présent ; documenter |
| Arachides | arachides | aardnoten | peanuts | Erdnüsse | `peanut`, VEGAN | concept déjà présent ; documenter |
| Soja | soja | sojabonen | soybeans | Sojabohnen | `soy`, VEGAN | concept déjà présent ; documenter |
| Lait | lait (y compris lactose) | melk (met inbegrip van lactose) | milk (including lactose) | Milch (einschließlich Laktose) | `milk` / `lactose`, VEGETARIAN | exceptions annexe II ; pas de nouvel alias générique |
| Fruits à coque | fruits à coque | noten | nuts | Schalenfrüchte | catégorie `nut`, VEGAN seulement si fruit précis | catégorie ≠ fruit sec ; candidat |
| Céleri | céleri | selderij | celery | Sellerie | `celery`, VEGAN | concept déjà présent ; documenter |
| Moutarde | moutarde | mosterd | mustard | Senf | `mustard`, VEGAN | concept déjà présent ; documenter |
| Sésame | graines de sésame | sesamzaad | sesame seeds | Sesamsamen | candidat `sesame`, VEGAN | graine réelle vs trace ; candidat |
| Sulfites | anhydride sulfureux et sulfites | zwaveldioxide en sulfieten | sulphur dioxide and sulphites | Schwefeldioxid und Sulphite | additifs E220–E228, UNCERTAIN existants | seuil et déclaration ; documenter |
| Lupin | lupin | lupine | lupin | Lupinen | candidat `lupin`, VEGAN | terme botanique / produits composés ; candidat |
| Mollusques | mollusques | weekdieren | molluscs | Weichtiere | candidat `mollusc`, NON_VEGAN | catégorie large ; candidat |

Les espèces de fruits à coque nommées (amande, noisette, noix, cajou, pécan, noix du Brésil, pistache, macadamia) doivent rester des concepts distincts quand elles sont déjà reconnues. Elles ne sont ni des synonymes de « fruit séché » ni une permission de fusionner un allergène et une trace.

## Candidats de données et de règles

| Terme source / zone | Langues | Concept proposé | Relation | Statut possible | Confiance | Décision |
| --- | --- | --- | --- | --- | --- | --- |
| « huiles végétales » / « graisses végétales », annexe VII A | 4 | `vegetable_oil` / candidat `vegetable_fat` | `REGULATORY_ALIAS` après revue | VEGAN seulement pour l’huile explicitement végétale | élevée pour le libellé, insuffisante pour une préparation | documenter ; ne pas généraliser « matière grasse » |
| « protéines » / « amidon », annexe VII | 4 | catégorie technique | `RELATED_NOT_EQUIVALENT` | UNCERTAIN | élevée | laisser candidat : origine et procédé variables |
| « arôme » / « flavouring », annexe VII | 4 | catégorie d’exclusion contextuelle | `EXCLUDED_CONTEXT` | UNCERTAIN | élevée | documenter ; ne pas rattacher à l’ingrédient mentionné |
| « ingrédients composés », art. 18 | 4 | structure de liste | `RELATED_NOT_EQUIVALENT` | aucun | élevée | documenter pour parser futur |
| « pays d’origine / lieu de provenance », art. 26 | 4 | provenance de produit | `RELATED_NOT_EQUIVALENT` | aucun | élevée | documenter : ne prouve pas l’origine d’un ingrédient |
| annexe II, catégories allergènes | 4 | concepts existants ou candidats ci-dessus | `RELATED_NOT_EQUIVALENT` | selon le concept précis | élevée | ne pas importer le lexique global sans règles anti-traces |
| « peut contenir » / contamination croisée | formulations d’étiquette, pas une dénomination normative de l’annexe II | section de trace existante | `EXCLUDED_CONTEXT` | aucun | moyenne | conserver hors verdict et hors ingrédients |

## Doublons, absences et classification

La base contient déjà notamment `gluten`, `egg`, `fish`, `peanut`, `soy`, `milk`, `celery`, `mustard` et `vegetable_oil`. Aucun statut existant n’a été modifié. Les concepts absents ou trop génériques (`crustacean`, `mollusc`, `sesame`, `lupin`, catégorie `nut`) restent des candidats : le règlement les signale comme allergènes, pas comme preuve suffisante de leur représentation exacte dans le schéma actuel.

Les catégories explicitement animales peuvent orienter une future revue vers `NON_VEGAN`; le lait et l’œuf vers `VEGETARIAN`; une matière végétale explicitement identifiée vers `VEGAN`; une catégorie, un arôme, une protéine, un amidon ou une préparation variable vers `UNCERTAIN`. Aucune de ces orientations n’est appliquée automatiquement par cette passe.

## OCR, traces et consolidation multilingue

Les pages d’articles 9, 17–22 et annexes II, III, VI et VII constituent les meilleures sources de futurs marqueurs de section. Les quatre sorties permettent de comparer les termes par page et d’exposer les différences linguistiques sans choisir arbitrairement une traduction. Les mots-clés d’extraits sont volontairement larges : une page peut être sélectionnée pour plusieurs thèmes, et l’absence d’extrait sur les traces reflète que FIC ne fournit pas une liste normative exhaustive de formulations « peut contenir ».

Le travail futur devra séparer : ingrédient déclaré, composant d’un ingrédient composé, catégorie allergène, avertissement de contamination et nom marketing. Il ne devra ni transformer une trace en ingrédient ni faire d’un allergène une preuve automatique de statut vegan.

## Coût et recommandations

Un futur import limité aux concepts déjà existants est estimé à 20–40 mappings revus par langue et 1–2 jours de revue/test. Un modèle complet de catégories d’allergènes et d’ingrédients composés demanderait environ 80–150 mappings, une évolution explicite du parseur et une validation de corpus OCR ; il est hors périmètre de cette passe documentaire. Commencer par une revue des candidats `sesame`, `lupin`, `crustacean` et `mollusc` avec une source d’identité et de classification distincte.

## Limites

- Les tableaux PDF restent une extraction textuelle, pas un format tabulaire juridiquement certifié.
- Le règlement organise l’information au consommateur ; il ne garantit pas la composition de chaque produit commercial.
- Les exceptions de l’annexe II exigent une analyse contextuelle avant toute reconnaissance.
- Aucune donnée applicative, asset Android, statut ou règle de verdict n’a été modifié.
