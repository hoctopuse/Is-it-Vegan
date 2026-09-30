# Manifest — termes fondamentaux d’étiquetage UE

Ce dossier contient les PDF locaux utilisés pour les termes fondamentaux et les catégories d’arômes. Les PDF sont des sources de référence ; ils ne sont ni des assets Android ni des données applicatives.

## Sources présentes

| CELEX | Consolidation | Langues | Pages par langue | Usage constaté |
|---|---|---|---:|---|
| `02008R1334` | 2026-02-16 | FR, NL, EN, DE | 215 | Source locale de l’import des catégories d’arômes. |
| `02011R1169` | 2025-04-01 | FR, NL, EN, DE | 60 | Copie locale du FIC utilisée pour la traçabilité des termes fondamentaux. L’extraction FIC canonique et les TXT dérivés sont dans `00-general-food-labelling/`. |

Les huit fichiers présents sont :

| Fichier | SHA-256 |
|---|---|
| `CELEX_02008R1334-20260216_DE_TXT.pdf` | `b3def2c6eb5916c0f7d7091247a4863ec373b5911b8e9f5c28717f4c67c98254` |
| `CELEX_02008R1334-20260216_EN_TXT.pdf` | `4e34da0c99d7ebac7013d08e290aa301e50e75853652548ab3fbb43f2d783075` |
| `CELEX_02008R1334-20260216_FR_TXT.pdf` | `6a30a6390590a7e951b603156abdc7b133ab203b8a5473595c7eb43432957977` |
| `CELEX_02008R1334-20260216_NL_TXT.pdf` | `1e158ddb345c00cf2a4b66a6be87ba676df2301a08d0a953aadc3dc4e94651a1` |
| `CELEX_02011R1169-20250401_DE_TXT.pdf` | `c2409d5cb83f784a8ac94ceeb8d4837da343189e024f5d1a58cc015f6bb62aa2` |
| `CELEX_02011R1169-20250401_EN_TXT.pdf` | `6cdf4190d6fa4a99d2e7d3124611724ec4cf69159563f88b35dc3c50dd416c63` |
| `CELEX_02011R1169-20250401_FR_TXT.pdf` | `80fedf36e7930dddfb270871d68ff10864009fd3ac71c9d350876402c9d6a81e` |
| `CELEX_02011R1169-20250401_NL_TXT.pdf` | `36dd5f61e3e9aeb95c2e51b4ff22ba90056639f726d4b30aae97c63c653c8ef3` |

## Doublons FIC expliqués

Les quatre fichiers FIC de ce dossier sont des copies binaires des quatre PDF conservés dans `00-general-food-labelling/`. Ils sont présents ici pour conserver le regroupement historique des termes fondamentaux avec les sources de base. La source FIC canonique pour l’extraction, les TXT dérivés, les extraits thématiques et le manifeste détaillé reste `00-general-food-labelling/SOURCE_MANIFEST.md`.

Cette duplication ne constitue pas une seconde source réglementaire et ne doit pas être interprétée comme deux consolidations différentes. Les hashes permettent de vérifier l’identité des copies.

## Importation

Les PDF `02008R1334` sont utilisés par `tools/import_eu_flavourings_regulation.py`. Ce flux concerne les catégories d’arômes ; il n’importe pas un concept botanique ou alimentaire à partir d’un simple goût ou d’une mention d’arôme.

Les PDF `02011R1169` de ce dossier ne sont pas une entrée distincte d’un importeur applicatif. L’extraction FIC documentée est réalisée depuis le dossier 00 par `tools/extract_fic_reference.py` et reste documentaire.

