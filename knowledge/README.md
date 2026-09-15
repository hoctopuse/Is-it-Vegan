# Is It Vegan — base de connaissance

`ingredients.json` est la source éditoriale : chaque entrée comporte ses
synonymes français, néerlandais et anglais, son verdict et son niveau de
confiance. `tools/build_ingredients.py` la valide puis produit le fichier
embarqué par l'application.

Règles de décision :

- `VEGAN` : ingrédient végétal, minéral ou de synthèse sans origine animale
  raisonnablement attendue ;
- `NON_VEGAN` : ingrédient clairement non végétarien (chair, gélatine,
  carmin de cochenille, gomme-laque issue d'insectes) ;
- `VEGETARIAN` : ingrédient animal sans chair ni partie du corps de l'animal
  (lait, œuf, miel), donc non vegan ; ce verdict concerne uniquement la liste
  d'ingrédients et ne constitue jamais une certification ;
- `UNCERTAIN` : l'origine varie selon le procédé ou le fournisseur, ou le
  libellé ne permet pas de conclure.

Ne jamais transformer `UNCERTAIN` en `VEGAN` pour améliorer artificiellement
la couverture. Une entrée sans preuve ou sans revue reste à ajouter, pas à
deviner.

Les sources de travail sont décrites dans `sources.json`. Les données
Open Food Facts et le contenu de WebAdditifs ne sont pas importés : leurs
licences et leurs conditions d'utilisation doivent être respectées avant toute
réutilisation massive.
