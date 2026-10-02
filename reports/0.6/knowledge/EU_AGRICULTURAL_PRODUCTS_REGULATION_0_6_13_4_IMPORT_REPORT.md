# Import du règlement (UE) n° 1308/2013 — 0.6.13.4

## Source et périmètre

CELEX `02013R1308`, consolidation du 18.08.2026. Sources primaires :

- `CELEX_02013R1308-20260818_FR_TXT.pdf`
- `CELEX_02013R1308-20260818_NL_TXT.pdf`
- `CELEX_02013R1308-20260818_EN_TXT.pdf`
- `CELEX_02013R1308-20260818_DE_TXT.pdf`

Les parties IX, X et XV à XX de l’annexe I et les définitions de l’annexe VII ont été exploitées. L’article 1 exclut la pêche et l’aquaculture : aucun poisson, crustacé ou mollusque n’est ajouté par cette source.

## Résultat

Avant/après : 459 → 464 concepts ; 1 864 → 1 906 mappings structurés. L’import contient 44 alias réglementaires : 11 FR, 11 EN, 11 NL et 11 DE dans l’import brut. Deux formes `olive_oil`, `olijfolie` et `Olivenöl`, possédaient déjà une métadonnée historique : les deux métadonnées supplémentaires ont été supprimées, sans supprimer les alias. Le résultat contient donc 42 nouvelles métadonnées et une seule métadonnée par forme normalisée.

| Concept ajouté | Statut | Justification |
|---|---|---|
| `edible_offal` | `NON_VEGAN` | Partie comestible d’un animal. |
| `animal_fat` | `NON_VEGAN` | Graisse explicitement porcine/animale. |
| `poultry_meat_preparation` | `NON_VEGAN` | Préparation explicitement à base de volaille. |
| `processed_fruit_vegetable_product` | `UNCERTAIN` | Catégorie transformée dont la dénomination ne donne pas toute la composition. |
| `spreadable_fat` | `UNCERTAIN` | Peut contenir des matières grasses végétales, animales ou mélangées. |

Les concepts `meat`, `egg`, `olive_oil`, `milk`, `cream`, `butter`, `whey`, `casein`, `lactose` et `cheese` reçoivent la source réglementaire lorsque le règlement les couvre. Leurs statuts historiques sont conservés. Les catégories incertaines portent une note structurée d’origine possible.

Les imitateurs végétaux, arômes, matières premières, produits transformés et parties animales ne sont pas fusionnés. Les termes absents ou hors champ restent candidats à une source dédiée.

## Reproductibilité

`python tools/import_eu_agricultural_products_regulation.py --check` retourne `changes=0`. Les assets sont produits par `tools/build_ingredients.py`; le lexique source et son asset sont identiques. Les tests vérifient concepts obligatoires, quatre langues, unicité, non-régression et exclusion de la pêche.

## Validations finales

- `:mutation-core:test`, `testDebugUnitTest`, `assembleDebug` et `assembleDebugAndroidTest` : succès ;
- importeur, génération des ingrédients et mapping multilingue en mode `--check` : succès ;
- parité des identifiants, statuts et alias entre `knowledge/` et les assets : confirmée ;
- fichiers `.tmp_1308_*.txt` restants : 0 ;
- `git diff --check` : succès, avec avertissements de conversion LF/CRLF seulement ;
- version finale : `0.6.13.4`, code 56 ; décision : **GO**.
