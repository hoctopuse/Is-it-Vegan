# Manifest — normes sectorielles et produits agricoles UE

Ce dossier contient les PDF locaux des normes sectorielles utilisées par les importeurs réglementaires. Les fichiers restent des sources de référence et ne sont pas des assets Android. Les dates de consolidation sont celles portées dans les noms de fichiers et dans les rapports d’import.

## Sources présentes

| CELEX | Sujet | Consolidation | Langues | Pages par langue | Importeur applicatif |
|---|---|---|---|---:|---|
| `02000L0036` | Cacao et chocolat | 2013-11-18 | FR, NL, EN, DE | 12 | `tools/import_eu_cocoa_chocolate_directive.py` |
| `02001L0110` | Miel | 2026-06-14 | FR, NL, EN, DE | 10 | `tools/import_eu_honey_directive.py` |
| `02001L0112` | Jus, purées et nectars | 2026-06-14 | FR, NL, EN, DE | 17 | `tools/import_eu_fruit_juice_directive.py` |
| `02001L0113` | Confitures, gelées et marmelades | 2026-06-14 | FR, NL, EN, DE | 10 | `tools/import_eu_jams_directive.py` |
| `02001L0114` | Laits conservés | 2026-06-14 | FR, NL, EN, DE | 8 | `tools/import_eu_preserved_milk_directive.py` |
| `02013R1308` | Produits agricoles | 2026-08-18 | FR, NL, EN, DE | 219 | `tools/import_eu_agricultural_products_regulation.py` |

Chaque CELEX est présent dans les quatre langues. Les fichiers sont nommés :

```text
CELEX_<CELEX>-<YYYYMMDD>_<FR|NL|EN|DE>_TXT.pdf
```

## Rôle et limites

Les rapports réglementaires à la racine et les pages `docs/sources/` indiquent les annexes et pages examinées par chaque importeur. Une dénomination réglementaire sert à établir une identité ou une catégorie ; elle ne constitue pas à elle seule une preuve d’origine vegan pour une formulation commerciale.

Les catégories variables restent `UNCERTAIN` lorsque la composition ou le procédé ne sont pas déterminés par la source. Les poissons, crustacés et mollusques ne sont pas ajoutés par le flux `02013R1308`, dont le périmètre exclut la pêche et l’aquaculture.

## Hashes des PDF

Les hashes ci-dessous sont calculés sur les fichiers présents dans ce dossier.

| Fichier | SHA-256 |
|---|---|
| `CELEX_02000L0036-20131118_DE_TXT.pdf` | `cdcc5f4832d6333ca60b7d55273df3caecbff76a88de1011a77b98044f849b03` |
| `CELEX_02000L0036-20131118_EN_TXT.pdf` | `b65351eca1bfcec70325fa7c907e7a0f73021e0b6d81185ac2c167af453506ee` |
| `CELEX_02000L0036-20131118_FR_TXT.pdf` | `e1948a9a9f75ace85f282710b97abbf1b0c7c144f76012150591b9400d6277f1` |
| `CELEX_02000L0036-20131118_NL_TXT.pdf` | `6a050a60858a6369c0b3e6d84026b8429e4fe464c4ca59ed29d25ff8fc95dae3` |
| `CELEX_02001L0110-20260614_DE_TXT.pdf` | `5a4a137208ab0a6f7fbded9a108dc83b49a31cc185de15e21f42c7e0f69faea5` |
| `CELEX_02001L0110-20260614_EN_TXT.pdf` | `2f1e0a51b35ae8ce765ec22abacfb6fda571971c5c1f0b4f07860b4b56b16fd6` |
| `CELEX_02001L0110-20260614_FR_TXT.pdf` | `e22e34e4368bf7227d8e2434fd241503ee57765d66651685a16d68e60c1e860c` |
| `CELEX_02001L0110-20260614_NL_TXT.pdf` | `f88cbd8f565aecbce88f7ec020dd976d76c3ca818d908200f1ee2568cdb2e593` |
| `CELEX_02001L0112-20260614_DE_TXT.pdf` | `70b043836c30b67d6fdb6c4454e0b4f022cd928c5c1df8fb8193024fa983f153` |
| `CELEX_02001L0112-20260614_EN_TXT.pdf` | `b9a31c557e468c4a1ea2b9b6d7dcbd7501c24b52679ac6809ca6d04570ee0907` |
| `CELEX_02001L0112-20260614_FR_TXT.pdf` | `9e512d6fd7e9ca77a05cea81d99a42ad2c4620e7c31945b04f348a079db88bcf` |
| `CELEX_02001L0112-20260614_NL_TXT.pdf` | `aa126a12f3274c4da184cc02b6521e41fdda0d7dab63cef8dd7d07b7f70875f4` |
| `CELEX_02001L0113-20260614_DE_TXT.pdf` | `c7d8ef0c19ac4746e6c2facf5adcd51c7bdc7eb780b72d10353f4566add4dab6` |
| `CELEX_02001L0113-20260614_EN_TXT.pdf` | `e05bf441b8ec9704cd518ea4a71c7932ae0f1f54eaf0ed96361a539523d95561` |
| `CELEX_02001L0113-20260614_FR_TXT.pdf` | `b7e0785bcb9c917de2e4550770797ebbafe874f0b84f387f95fb027c5ed9126d` |
| `CELEX_02001L0113-20260614_NL_TXT.pdf` | `8956588875c36f3173cda7aea425e1f4cc6f3a2ea63ad3dfac0be4bbcbbe8ae3` |
| `CELEX_02001L0114-20260614_DE_TXT.pdf` | `9629aef4578c0098bbe6071cab624cb63bc8b27a6c9cbd440091ca73e45fd0cb` |
| `CELEX_02001L0114-20260614_EN_TXT.pdf` | `cba10f58c618773123a3830fc425ad7dcc8a2f7283790c196feaea78d11208c3` |
| `CELEX_02001L0114-20260614_FR_TXT.pdf` | `8ccd09a93c51cf7520a6b485ca24d32ea27ec9a8165527b4876e3c0e24b8d130` |
| `CELEX_02001L0114-20260614_NL_TXT.pdf` | `76b98b6adfdb7377140cd99105097f58f3954acb4618e289e83eec5cad248b8e` |
| `CELEX_02013R1308-20260818_DE_TXT.pdf` | `64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda` |
| `CELEX_02013R1308-20260818_EN_TXT.pdf` | `b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11` |
| `CELEX_02013R1308-20260818_FR_TXT.pdf` | `5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c` |
| `CELEX_02013R1308-20260818_NL_TXT.pdf` | `99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74` |

## Importation et contrôles

Les importeurs documentés acceptent `--check` lorsqu’il est implémenté. Les flux miel et lait conservé exposent `--dry-run` et `--write`; le dry-run sans changement est leur contrôle d’idempotence actuel. Après un import autorisé, `tools/build_ingredients.py` régénère les assets, puis `tools/build_multilingual_ingredient_mapping.py` et `tools/build_knowledge_docs.py` régénèrent leurs documents respectifs.
