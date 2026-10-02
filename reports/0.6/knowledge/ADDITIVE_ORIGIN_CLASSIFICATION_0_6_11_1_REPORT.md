# Qualification d’origine des additifs — 0.6.11.1

## Décision

**GO, sous réserve des validations consignées ci-dessous.** Cette passe modifie deux
statuts seulement. Elle conserve la qualification 0.6.11 de E901, E902, E903, E938,
E941 et E948 et ne déduit jamais une compatibilité vegan de la seule autorisation UE.

## Périmètre et audit

La revue a porté sur les 77 numéros et variantes prioritaires demandés. Parmi eux,
60 sont présents dans les 338 numéros E importables de la base applicative. Les 17
autres ne font pas partie de cette base : E441, E542, E913, E963, E1000, E101a,
E160a(i), E160a(ii), E161b(i), E161b(ii), E387, E430, E478, E483, E484, E910 et
E921. Aucun concept ou alias n’a été créé pour ces entrées absentes : une liste
spécialisée ne suffit pas à étendre le référentiel réglementaire.

Avant cette passe, les 311 numéros E encore `UNCERTAIN` après 0.6.11 ont été
comparés aux règles d’origine existantes. Les règles actives concernent des
qualificatifs explicitement attachés à E322 et E471 ; les règles E422, E626–E635 et
E640 restent en revue. Elles ne changent aucun statut générique. E270, déjà `VEGAN`,
n’a pas été modifié par cette passe.

| Numéro E | Concept | Avant | Décision | Après | Confiance | Justification et portée globale |
|---|---|---|---|---|---|---|
| E966 | `e966` | `UNCERTAIN` | À classer | `VEGETARIAN` | Élevée | La spécification UE définit le lactitol comme obtenu par hydrogénation catalytique du lactose. L’origine laitière est intrinsèque : globalement végétarienne selon la sémantique existante, jamais vegan. |
| E1105 | `e1105` | `UNCERTAIN` | À classer | `VEGETARIAN` | Élevée | La réglementation UE précise que le lysozyme est obtenu de blanc d’œuf de poule. L’origine œuf est intrinsèque : globalement végétarienne selon la sémantique existante, jamais vegan. |

Les deux décisions citent la spécification primaire `eu-reg-231-2012-specifications`
et les sources spécialisées `federation-vegane-e-additives` et
`vegan-easy-food-additives`. Il n’y a pas de contradiction sur l’origine de ces deux
additifs. Les sources secondaires n’ont servi qu’à la comparaison et ne fondent aucun
statut.

## Cas volontairement conservés incertains

Les sources spécialisées signalent une origine animale possible pour plusieurs
additifs, mais décrivent aussi des filières végétales, synthétiques ou dépendantes du
fabricant. Ils restent donc `UNCERTAIN` sans qualificatif local : E101, E104, E153,
E161b, E161g, E234, E304, E325–E327, E422, E431–E436, E442, E470a, E470b, E471,
E472a–E472f, E473–E477, E479b, E481, E482, E491–E495, E570, E572, E585, E627–E635,
E640, E920 et E1518.

Les listes divergent notamment sur la fréquence des origines animales des E627, E631,
E635 et E640. Cette divergence, ainsi que les alternatives explicitement signalées par
Vegan Easy, interdit une classification globale. E322, E422, E470a/E470b, E471,
E472a–E472f, E570 et E572 sont également des cas variables. E470b et E572 restent
des concepts distincts ; E960a et E960b restent distincts ; E345 et E345(i) restent
hors de la base applicative.

Aucune règle d’origine n’est ajoutée ou activée. Les règles existantes continuent de
résoudre seulement une occurrence assortie d’un qualificatif explicite (par exemple
soja, végétal, synthétique ou une origine animale déclarée). Elles ne reclassifient pas
le concept générique et les traces restent hors verdict.

## Sources et limites

1. `eu-reg-231-2012-specifications` — règlement (UE) nº 231/2012, spécifications
   consolidées au 01.09.2022, Commission européenne, EN. Source primaire utilisée
   lorsque sa définition établit directement l’origine ; ce n’est pas une
   certification vegan.
2. `federation-vegane-e-additives` — Fédération végane, FR. Guide spécialisé,
   utilisé pour corroborer les cas à origine animale et repérer les cas variables ;
   non exhaustif et non spécifique à un fabricant.
3. `vegan-easy-food-additives` — Vegan Easy, EN. Guide spécialisé, utilisé pour
   corroborer E966/E1105 et les alternatives de fabrication ; non réglementaire et
   non spécifique à un produit.
4. `les-additifs-alimentaires-vegetarien` — Les Additifs Alimentaires, FR. Source
   secondaire de comparaison ; datation et méthode insuffisantes pour fonder un
   statut seule.

La hiérarchie appliquée est : spécification réglementaire primaire, guide spécialisé,
documentation fabricant, publication technique identifiable, puis piste secondaire.
Une autorisation UE ou une fonction technologique seule n’a entraîné aucun statut.

## Décomptes

| Mesure | Avant 0.6.11.1 | Après 0.6.11.1 |
|---|---:|---:|
| Numéros E `UNCERTAIN` | 311 | 309 |
| Concepts `VEGAN` | 119 | 119 |
| Concepts `VEGETARIAN` | 8 | 10 |
| Concepts `NON_VEGAN` | 7 | 7 |
| Concepts `UNCERTAIN` | 318 | 316 |

Les deux diminutions correspondent uniquement à E966 et E1105. Les six décisions
0.6.11 précédentes sont inchangées.

## Impact et tests

E966 et E1105 rendent une occurrence reconnue végétarienne, donc non compatible avec
un verdict vegan. Les additifs variables restent incertains sans origine locale.
`EuAdditivesReference06910Test` vérifie ces statuts depuis les chemins de production,
la synchronisation `knowledge/` vers l’asset, les formes de numéros E, les suffixes,
les exclusions E345/E345(i), la séparation E470b/E572 et le maintien des cas variables.

Validations exécutées avec succès :

- `python tools/build_ingredients.py --check`
- `python tools/build_origin_rules.py --check`
- `python tools/build_knowledge_docs.py --check`
- `python tools/build_ingredients.py`, `python tools/build_origin_rules.py` et
  `python tools/build_knowledge_docs.py`, puis les trois contrôles `--check` à
  nouveau ; la seconde vérification ne produit aucune différence.
- `./gradlew :mutation-core:test`
- `./gradlew testDebugUnitTest` (incluant le test source → asset → runtime renforcé)
- `./gradlew assembleDebug`
- `./gradlew assembleDebugAndroidTest`
- `./gradlew connectedDebugAndroidTest` sur Nokia G42 5G, Android 15
- `git diff --check`

Aucun changement n’a été apporté à `VerdictEngine`, `VeganAnalyzer`, au pipeline
OCR/ML Kit ou au traitement des traces.

## Fichiers de cette passe

- `knowledge/ingredients.json`
- `knowledge/sources.json`
- `app/src/main/assets/ingredients.json` (généré)
- `app/src/test/java/com/example/isitvegan/EuAdditivesReference06910Test.kt`
- `docs/additifs.md`
- `docs/base-connaissances.md`
- `docs/changelog.md`
- `docs/generated/base-connaissances-data.md` (généré)
- `app/build.gradle.kts`
- `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_REPORT.md` (rapport de la passe conservée)
- `ADDITIVE_ORIGIN_CLASSIFICATION_0_6_11_1_REPORT.md`

## Suite à examiner

Les 309 numéros E incertains exigent une preuve par additif, et pour les cas variables
une preuve d’origine ou de procédé rattachée au produit. Les 17 numéros prioritaires
absents doivent d’abord être vérifiés contre le périmètre réglementaire éditorial avant
toute éventuelle extension de la base.
