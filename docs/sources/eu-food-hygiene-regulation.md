# Viande séparée mécaniquement — CELEX 02004R0853

Le lot v0.7 ajoute uniquement `mechanically_separated_meat`, avec le statut
ingrédient existant `NON_VEGAN`. Le verdict détaillé correspondant est
`NON_VEGETARIAN`; le diagnostic conserve l'identifiant du bloqueur. L'analyse
parcourt tous les tokens afin de conserver aussi les inconnus.

Les quatre formes sont `viandes séparées mécaniquement` (FR), `separatorvlees`
(NL), `mechanically separated meat` (EN), `Separatorenfleisch` (DE).
L'annexe I, point 1.14, des quatre PDF locaux 853/2004 consolidés au 07.05.2026,
page PDF 16, définit la matière animale. L'annexe VII, partie B, point 18,
des quatre PDF locaux 1169/2011 consolidés au 01.04.2025, page PDF 49,
atteste la dénomination d'ingrédient avec indication des espèces animales.
Chaque mapping conserve les deux preuves, leur forme source, page, point,
offset dans l'extraction et SHA-256. Les différences de casse des titres
restent dans la preuve; elles ne créent pas de variantes lexicales.

`tools/import_eu_food_hygiene_regulation.py` est borné à ce concept et ces
quatre formes : `--dry-run` propose sans écrire, `--check` exige le lot complet
et conforme, `--write` ajoute les données éditoriales après validation.
Les actifs et documents générés passent ensuite par les builders habituels.
Un lot incomplet/divergent ou une collision doit échouer avant écriture.

L'alias court `meat` reste au concept historique `meat`. Les viandes hachées,
préparations et produits à base de viande restent distincts. Aucun sigle
`MSM`/`VSM`, nom d'espèce, composé, flexion, creton ou cuisse de grenouille
n'est ajouté. Les quatre formes de base ne garantissent pas la reconnaissance
des dénominations complètes avec espèce en préfixe ou mot composé.

Une mention négative ou d'imitation contenant littéralement une forme du lot
peut déclencher un bloqueur animal : le matcher n'a pas de protection générale
pour ces contextes. Cette limite est caractérisée dans les tests et le rapport;
la présence lexicale n'est pas une preuve sémantique universelle.

Sources : Union européenne / EUR-Lex. Les conditions de réutilisation des
textes consolidés sont données par l'[avis juridique EUR-Lex](https://eur-lex.europa.eu/content/legal-notice/legal-notice.html?locale=en)
(CC BY 4.0, attribution et indication des changements). La sélection des quatre
termes et la classification sont des décisions éditoriales de l'application.
