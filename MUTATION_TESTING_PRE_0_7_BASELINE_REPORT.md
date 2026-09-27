# Campagne PIT complète de référence pré-0.7

## État et synthèse

- Branche : `mutation-testing-pit`
- HEAD : `1f39308f55f20a18b36a2fbdae611d9ac78fbb03`
- `master` est ancêtre de HEAD.
- Aucun PIT, test ou build n'a été relancé pour ce complément documentaire.
- Gradle `9.7.1`, plugin PIT Gradle `1.19.0`, moteur PIT `1.22.1`.
- PIT et minions : Temurin 17.0.20.1.
- Configuration suivie inchangée; aucun code, test, Gradle, donnée ou configuration modifié.

| Groupe | Mutants | Tués | Survivants | Non couverts | Timeouts | Score |
|---|---:|---:|---:|---:|---:|---:|
| Parser/prétraitement | 343 | 192 | 92 | 58 | 1 | 67,6 % |
| Matching/origine | 170 | 124 | 38 | 8 | 0 | 76,5 % |
| Segmentation/sections | 431 | 182 | 193 | 56 | 0 | 48,5 % |
| Diagnostics/verdict | 46 | 40 | 6 | 0 | 0 | 87,0 % |
| **Total dédupliqué** | **990** | **538** | **329** | **122** | **1** | **62,1 %** |

La baseline `:mutation-core:test` était verte. Aucun `UNKNOWN_ERROR`, `MEMORY_ERROR`, `RUN_ERROR` ou `NON_VIABLE` n'a été observé. Le CSV [MUTATION_TESTING_PRE_0_7_BASELINE_DETAILS.csv](MUTATION_TESTING_PRE_0_7_BASELINE_DETAILS.csv) est l'annexe exhaustive durable et machine-readable, produit exclusivement depuis les quatre XML archivés.

Contrôles du CSV : 452 enregistrements de données plus l'en-tête, dont 329 `SURVIVED`, 122 `NO_COVERAGE` et 1 `TIMED_OUT`. Les groupes sont disjoints selon la campagne.

## Contrôles finaux

- Aucun PIT, test ou build relancé.
- `git diff --check` vérifié.
- Seuls ce rapport et le CSV sont modifiés/ajoutés.
- Aucun commit ni push.
