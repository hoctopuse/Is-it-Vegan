# CELEX 02001L0112 fruit juice import — 0.6.13.2

## Sources

Primary PDFs: CELEX_02001L0112-20260614 FR, NL, EN and DE. Annex I pages 8-9 defines fruit juice, juice from concentrate, concentrate, water extraction, dehydrated/powdered juice, puree and nectar.

## Imported concepts

- `fruit_juice` VEGAN: 18 aliases (FR 5, NL 3, EN 5, DE 3; generic forms overlap in existing aliases).
- `fruit_puree` VEGAN: 6 aliases (FR 2, NL 1, EN 2, DE 1).
- `fruit_nectar` UNCERTAIN: FR/NL/EN/DE, because the Directive permits water plus honey, sugars and/or sweeteners.

Fruits remain separate from juice, puree and nectar. Existing dried-fruit and nut concepts were audited: raisin, date, cranberry, almond, cashew, walnut, hazelnut, pistachio, macadamia, Brazil nut and pecan exist. Unattested requested dried variants remain candidates rather than silently imported.

## Evidence

`--check` returns `changes=0`; knowledge/assets parity passes; the multilingual mapping has 1,834 structured records. Targeted JVM test passes, including FR/NL/EN/DE juice mapping, statuses and the exclusion of `arome de pomme` as `apple`.

## Limits

The full requested Gradle suite, source documentation page and exhaustive individual-fruit import remain to be completed before release approval.
