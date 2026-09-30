# Import CELEX 02008R1334 — arômes (0.6.13.7)

## Décision

**GO**, sous réserve des validations consignées ci-dessous. L’import est additif, idempotent et n’a reclassifié aucun concept historique.

## Sources et périmètre

- Règlement (CE) n° 1334/2008, CELEX `02008R1334`, consolidation `2026-02-16`.
- PDF locaux primaires : `CELEX_02008R1334-20260216_{FR,NL,EN,DE}_TXT.pdf` dans `reference-input/eu-food-labelling/01-core-labelling-terms/`.
- Sections exploitées : article 3, paragraphe 2, points a) à j), pages PDF 4–5 dans chaque langue.
- La source `eu-flavourings-regulation-1334-2008-20260216` est ajoutée à `knowledge/sources.json`. Elle précise que la réglementation établit l’identité des catégories, pas la compatibilité vegan d’une formulation commerciale.

## Concepts et statuts

| Concept | Statut | Motif |
| --- | --- | --- |
| `flavouring` | UNCERTAIN | Catégorie générale pouvant provenir de diverses matières. |
| `flavouring_substance` | UNCERTAIN | Substance définie, sans origine de formulation établie. |
| `natural_flavouring` (enrichi) | UNCERTAIN | Une substance « naturelle » peut avoir une origine végétale, animale ou microbienne. |
| `flavouring_preparation` | UNCERTAIN | Article 3(d) prévoit des matières végétales, animales ou microbiologiques. |
| `thermal_process_flavouring` | UNCERTAIN | Procédé et matières premières variables. |
| `smoke_flavouring` | UNCERTAIN | Catégorie réglementaire distincte, non assimilée à une fumée réelle. |
| `flavour_precursor` | UNCERTAIN | Le précurseur produit l’arôme pendant la transformation. |
| `other_flavouring` | UNCERTAIN | Catégorie résiduelle de l’article 3(h). |
| `food_ingredient_with_flavouring_properties` | UNCERTAIN | Catégorie d’ingrédient, pas une origine déterminée. |

Les neuf concepts ont une `possibleOriginNote` structurée : `PLANT`, `ANIMAL`, `MICROBIAL`, variabilité `RAW_MATERIAL_AND_PROCESS`, source CELEX et confiance `REGULATORY_CATEGORY`. La note informe sans modifier le statut ou le verdict.

## Mappings réglementaires

40 mappings vérifiés ont été ajoutés, tous avec `mappingGroup: eu-flavourings-regulation`, `relation: REGULATORY_ALIAS`, `confidence: REVIEWED`.

| Langue | Nombre | Formes |
| --- | ---: | --- |
| FR | 10 | arôme/arômes ; substance et préparation aromatisantes ; substance aromatisante naturelle ; arôme thermique, de fumée, précurseur et autre arôme ; ingrédient aux propriétés aromatisantes |
| NL | 10 | aroma/aroma’s ; aromastof ; natuurlijke aromastof ; aromatiserend preparaat ; catégories thermiques, fumées et précurseurs correspondantes |
| EN | 10 | flavouring/flavourings ; flavouring substance/preparation ; natural flavouring substance ; thermal process, smoke, precursor et other flavouring |
| DE | 10 | Aroma/Aromen ; Aromastoff ; natürlicher Aromastoff ; Aromaextrakt ; thermisch gewonnenes Reaktionsaroma ; Raucharoma ; Aromavorstufe |

La différence allemande `Aromaextrakt` est conservée vers `flavouring_preparation`; elle n’est pas traduite artificiellement en un concept distinct. Le règlement ne définit pas un « concentré aromatique » autonome : il reste non importé. Il ne fournit pas non plus une identité réglementaire du fruit de vanille ; `vanilla` n’est donc pas créé à partir de mentions de vanillates en annexe.

## Contextes protégés

Le matcher exclut localement les candidats `strawberry`, `coffee` et la famille chocolat lorsqu’ils sont encadrés par un marqueur d’arôme ou de goût. Il exclut aussi `coffee` dans `extrait de café`. Cette règle ne retire aucun alias : elle empêche seulement qu’un ingrédient réel soit conclu à partir d’une dénomination aromatique.

Les protections 0.6.13.6 restent vérifiées : `arôme chocolat`, `arôme de chocolat`, `chocolate flavour`, `chocoladearoma` et `Schokoladenaroma` ne résolvent pas `chocolate`; `chocolat` continue à le résoudre. Les traces mentionnant un arôme restent hors verdict.

## Comptes, parité et idempotence

- Concepts : 471 → 479 (+8 ; `natural_flavouring` enrichi).
- Mappings multilingues : 1 956 → 1 996 (+40).
- `python tools/import_eu_flavourings_regulation.py --dry-run` annonce les 8 concepts et 40 alias avant écriture.
- Après écriture, `--check` retourne `changes=0`.
- `tools/build_ingredients.py` régénère les deux assets depuis `knowledge/`; l’asset multilingue est identique octet pour octet à la source éditoriale.

## Validations exécutées

- `python tools/import_eu_flavourings_regulation.py --check` : `changes=0`.
- `python tools/build_ingredients.py --check` : base et asset courants.
- `python tools/build_multilingual_ingredient_mapping.py --check` : mapping et documentation générée courants.
- `./gradlew :mutation-core:test testDebugUnitTest assembleDebug assembleDebugAndroidTest` : succès ; 44 rapports JVM applicatifs et 11 rapports `mutation-core`, sans échec ni erreur après réexécution.
- `git diff --check` : succès (seuls les avertissements de conversion LF/CRLF Git sont affichés).

## Limites

- Un arôme réglementaire ne prouve ni l’origine vegan, ni la présence de l’aliment dont il porte le goût.
- Les extraits et concentrés spécifiques ne sont pas assimilés à l’ingrédient réel sans identité et provenance propres.
- Les expressions marketing supplémentaires devront être ajoutées uniquement après observation documentée ; une erreur OCR isolée ne devient pas un alias.
- La vanille brute n’est pas ajoutée par cette passe : les PDF citent des dérivés comme les vanillates, qui ne constituent pas une source pour créer le concept botanique `vanilla`.
