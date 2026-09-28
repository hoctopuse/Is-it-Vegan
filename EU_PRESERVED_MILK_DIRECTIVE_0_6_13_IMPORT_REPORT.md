# Import de la directive UE sur les laits conservés — 0.6.13

## Décision

**GO, sous réserve des validations listées ci-dessous.** CELEX `02001L0114` enrichit les concepts existants `milk` et `cream`; aucun concept, statut ou alias historique n’est supprimé. Les produits laitiers reconnus restent `VEGETARIAN`, donc incompatibles avec un verdict `VEGAN`.

## Sources examinées

Sources primaires locales, toutes consolidées le 14.06.2026 dans `reference-input/eu-food-labelling/02-sector-product-standards/` :

- `CELEX_02001L0114-20260614_FR_TXT.pdf`
- `CELEX_02001L0114-20260614_NL_TXT.pdf`
- `CELEX_02001L0114-20260614_EN_TXT.pdf`
- `CELEX_02001L0114-20260614_DE_TXT.pdf`

La version EUR-Lex de CELEX `02001L0114` consolidée le 14.06.2026 a servi de contrôle externe du titre, de la date et des annexes. Elle n’a pas remplacé les PDF pour l’extraction. La source éditoriale ajoutée, `eu-preserved-milk-directive-2001-114-20260614`, conserve CELEX, institution, URL, consolidation, langues, chemins locaux, rôle et limites.

## Extraction et jointure

L’annexe I, pages 4 et 5, définit les produits partiellement déshydratés (laits concentrés, évaporés, sucrés ou non) et totalement déshydratés (poudres entières, partiellement écrémées ou écrémées). L’annexe II, page 7, fournit les dénominations particulières telles que `evaporated milk`, `koffiemelk`, `lait demi-écrémé concentré` et les termes de crème en poudre.

`tools/import_eu_preserved_milk_directive.py` extrait le texte UTF-8 avec `pypdf`, retire seulement les espaces et césures ajoutés par l’extraction pour la vérification, puis conserve l’orthographe officielle dans `knowledge/`. Chaque alias est cherché dans le PDF de sa langue; les collisions, concepts absents, statut non `VEGETARIAN` et langues manquantes pour `milk` arrêtent l’import. La jointure est déterministe : `milk` reçoit les dénominations de lait, `cream` les quatre synonymes de crème en poudre que l’annexe II rattache explicitement au produit 2(a).

| Langue | Alias officiels vérifiés |
|---|---:|
| FR | 23 |
| NL | 20 |
| EN | 23 |
| DE | 23 |
| **Total** | **89** |

Exemples retenus : `lait concentré`, `lait en poudre entier`, `lait concentré sucré`; `geëvaporeerde volle melk`, `volle melkpoeder`, `gecondenseerde volle melk met suiker`; `condensed milk`, `evaporated milk`, `sweetened condensed milk`, `whole milk powder`; `Kondensmilch`, `Vollmilchpulver`, `Gezuckerte Kondensmilch`, `Magermilchpulver`.

### Alias exhaustifs par langue

| Langue | `milk` | `cream` |
|---|---|---|
| FR | lait ; lait partiellement déshydraté ; lait concentré riche en matières grasses ; lait concentré ; lait concentré partiellement écrémé ; lait concentré écrémé ; lait concentré sucré ; lait concentré sucré partiellement écrémé ; lait concentré sucré écrémé ; lait totalement déshydraté ; lait en poudre riche en matières grasses ; poudre de lait riche en matières grasses ; lait en poudre entier ; poudre de lait entier ; lait en poudre partiellement écrémé ; poudre de lait partiellement écrémé ; lait en poudre écrémé ; poudre de lait écrémé ; lait demi-écrémé concentré ; lait de mi-écrémé concentré non sucré ; lait demi-écrémé concentré sucré ; lait demi-écrémé en poudre | crème en poudre |
| NL | melk ; gedeeltelijk gedehydrateerde melk ; geëvaporeerde melk met hoog vetgehalte ; geëvaporeerde volle melk ; geëvaporeerde gedeeltelijk afgeroomde melk ; geëvaporeerde magere melk ; gecondenseerde volle melk met suiker ; gecondenseerde gedeeltelijk afgeroomde melk met suiker ; gecondenseerde magere melk met suiker ; geheel gedehydrateerde melk ; melkpoeder ; melkpoeder met hoog vetgehalte ; volle melkpoeder ; melkpoeder van gedeeltelijk afgeroomde melk ; magere melkpoeder ; geëvaporeerde halfvolle melk ; halfvolle koffiemelk ; halfvolle melkpoeder ; koffiemelk | roompoeder |
| EN | milk ; partly dehydrated milk ; condensed high-fat milk ; condensed milk ; condensed, partly skimmed milk ; condensed skimmed milk ; sweetened condensed milk ; sweetened condensed, partly skimmed milk ; sweetened condensed skimmed milk ; totally dehydrated milk ; milk powder ; dried high-fat milk ; high-fat milk powder ; dried whole milk ; whole milk powder ; dried partly skimmed milk ; partly skimmed-milk powder ; dried skimmed milk ; skimmed-milk powder ; evaporated milk ; evaporated semi-skimmed milk ; semi-skimmed milk powder ; dried semi-skimmed milk | ? |
| DE | Milch ; Eingedickte Milch ; Kondensmilch mit hohem Fettgehalt ; Kondensmilch ; kondensierte Vollmilch ; Teilentrahmte Kondensmilch ; Kondensmagermilch ; kondensierte Magermilch ; Gezuckerte Kondensmilch ; gezuckerte kondensierte Vollmilch ; Gezuckerte teilentrahmte Kondensmilch ; gezuckerte teilentrahmte kondensierte Milch ; Gezuckerte Kondensmagermilch ; gezuckerte kondensierte Magermilch ; Trockenmilch ; Milchpulver ; Milchpulver mit hohem Fettgehalt ; Vollmilchpulver ; Teilentrahmtes Milchpulver ; Magermilchpulver ; kondensierte Kaffeesahne | Rahmpulver ; Sahnepulver |

## Classification

`milk` et `cream` restent `VEGETARIAN`. Leur origine laitière est animale au sens du projet; elle reste compatible avec la classification végétarienne retenue, mais bloque le verdict vegan. La directive établit l’identité réglementaire, et non une certification vegan : la classification reste justifiée par les sources éditoriales existantes.

Les alias de l’annexe II `crème en poudre`, `roompoeder`, `Rahmpulver` et `Sahnepulver` enrichissent le concept existant `cream`; aucun concept `cream-powder` n’est créé. Toutes les variantes de teneur en matière grasse restent des alias des concepts existants, pas des doublons.

## Termes exclus et limites

Ne sont pas importés comme alias `milk` : arôme/goût de lait, boissons végétales, lait de soja, formulations marketing et la simple expression `contient du lait`. Cette dernière conserve le traitement existant de présence déclarée, distinct d’un alias d’ingrédient. Une mention `sans lactose` ne modifie aucune origine laitière.

Beurre, fromage, yaourt, lactosérum, caséine, caséinates, lactose isolé, protéines de lait, lait infantile et desserts lactés ne sont pas ajoutés par ce flux. Ils exigent une source ou directive dédiée. La directive ne permet pas de classer un produit composé au-delà de ses ingrédients réellement déclarés.

## Décomptes

| Mesure | Avant | Après |
|---|---:|---:|
| Concepts | 452 | 452 |
| Concepts `VEGAN` | 119 | 119 |
| Concepts `VEGETARIAN` | 10 | 10 |
| Concepts `NON_VEGAN` | 7 | 7 |
| Concepts `UNCERTAIN` | 316 | 316 |
| Alias canoniques `milk` | 6 | 88 |
| Alias canoniques `cream` | 7 | 11 |
| Source CELEX 02001L0114 | 0 | 1 |

## Validations

- `python tools/import_eu_preserved_milk_directive.py --dry-run`, `--write`, puis second `--dry-run` : `changes=0` après écriture.
- Test JVM ciblé `PreservedMilkDirectiveImportTest` : reconnaissance FR/NL/EN/DE, laits en poudre, concentrés, évaporés et sucrés; statut, verdict, exclusions de contexte, traces, parité source/asset et idempotence.
- Générateurs et contrôles `--check` : réussis après régénération; second import à `changes=0`.
- `:mutation-core:test`, `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest` et `git diff --check` : réussis.
- `adb devices` ne liste aucun appareil; aucun test connecté n’a donc été exécutable.

## Fichiers modifiés

- `knowledge/ingredients.json`, `knowledge/ingredient_aliases_multilingual.json`, `knowledge/sources.json`
- `app/src/main/assets/ingredients.json`, `app/src/main/assets/ingredient_aliases_multilingual.json` (générés)
- `tools/import_eu_preserved_milk_directive.py`
- `mutation-core/src/main/kotlin/com/example/isitvegan/IngredientMatcher.kt`
- `app/src/test/java/com/example/isitvegan/PreservedMilkDirectiveImportTest.kt`
- `docs/base-connaissances.md`, `docs/changelog.md`, `docs/generated/base-connaissances-data.md`, `docs/sources/eu-preserved-milk-directive.md`, `mkdocs.yml`
- `app/build.gradle.kts`, `EU_PRESERVED_MILK_DIRECTIVE_0_6_13_IMPORT_REPORT.md`
