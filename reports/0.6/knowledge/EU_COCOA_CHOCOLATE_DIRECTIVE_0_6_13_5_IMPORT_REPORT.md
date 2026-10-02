# Import de la directive 2000/36/CE — 0.6.13.5

## Sources et extraction

Source primaire : CELEX `02000L0036`, directive 2000/36/CE, consolidation du 18.11.2013. Les quatre PDF locaux utilisés sont :

- `CELEX_02000L0036-20131118_FR_TXT.pdf`
- `CELEX_02000L0036-20131118_NL_TXT.pdf`
- `CELEX_02000L0036-20131118_EN_TXT.pdf`
- `CELEX_02000L0036-20131118_DE_TXT.pdf`

L’importeur vérifie les dénominations dans l’annexe I, sections A(1) à A(10), à partir de l’extraction UTF-8 des PDF. Une jointure est acceptée uniquement lorsque chaque forme est présente dans son PDF de langue. Aucun terme n’est traduit automatiquement.

## Résultat

La base passe de 464 à 471 concepts. Le lexique passe de 1 906 à 1 956 mappings structurés : 55 dénominations réglementaires sont examinées (FR 14, NL 14, EN 13, DE 14), dont deux métadonnées préexistantes `cacaoboter` et `Kakaobutter` sont réaffectées de `cocoa` à `cocoa_butter`. Elles conservent leur forme et ne créent pas de doublon. Trois formes du concept historique `cocoa` existaient déjà ; l’augmentation nette est donc 50 mappings.

| Concept | Statut | Justification |
|---|---|---|
| `cocoa_butter` | `VEGAN` | Matière grasse issue de fèves de cacao. |
| `powdered_chocolate` | `VEGAN` | Définition : cacao en poudre et sucres. |
| `chocolate` | `UNCERTAIN` | Une recette commerciale peut contenir du lait ou d’autres ingrédients animaux. |
| `milk_chocolate` | `VEGETARIAN` | La définition impose du lait ou un produit laitier. |
| `white_chocolate` | `VEGETARIAN` | La définition impose du lait ou un produit laitier. |
| `filled_chocolate` | `UNCERTAIN` | La garniture peut contenir lait, œuf ou miel. |
| `chocolate_confection` | `UNCERTAIN` | La composition d’un bonbon ou d’une praline n’est pas déterminée par sa dénomination. |

Le concept historique `cocoa` est enrichi, reste `VEGAN` et conserve sa distinction avec `cocoa_butter` et les catégories de chocolat. Les concepts de pâte ou masse de cacao ne sont pas créés séparément : l’annexe I ne leur donne pas une dénomination de vente autonome dans les quatre langues. Les arômes et les mentions de goût ne sont pas ajoutés comme équivalents du chocolat réel.

## Contrôles, limite bloquante et décision

`python tools/import_eu_cocoa_chocolate_directive.py --check` retourne `changes=0` après écriture. Les assets sont produits par `tools/build_ingredients.py`; le lexique source et son asset sont identiques. Les tests de statuts, quatre langues, unicité, non-régression et parité passent.

La directive ne garantit pas la recette réelle d’un chocolat commercial : le chocolat générique, les fourrages et les pralines restent prudents. Elle ne justifie pas de classer un arôme chocolat comme chocolat réel. Les marques, dénominations marketing et variantes non attestées restent hors import.

Le NO-GO initial provoqué par `arôme chocolat` a été levé par le correctif contextuel 0.6.13.6, documenté dans `FIX_0_6_13_6_CHOCOLATE_FLAVOUR_MATCHING_REPORT.md`. Le test obligatoire et les validations complètes passent désormais, sans modification des statuts ni des mappings.
