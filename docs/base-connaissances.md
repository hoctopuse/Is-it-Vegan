# Base de connaissances

Le dépôt contient deux représentations liées :

- `knowledge/ingredients.json` est la source éditoriale ;
- `app/src/main/assets/ingredients.json` est l’asset généré et chargé par Android.

Le script `tools/build_ingredients.py` valide la source et produit l’asset. Les deux fichiers ne sont donc pas identiques octet pour octet : le champ éditorial `sources` est converti en chaîne `source` pour le runtime et le JSON est réindenté.

## Schéma éditorial réel

Chaque entrée de `knowledge/ingredients.json` contient :

| Champ | Type | Rôle |
|---|---|---|
| `id` | chaîne | identifiant stable et unique |
| `name` | chaîne | nom canonique affichable |
| `eNumber` | chaîne optionnelle | numéro E, lorsqu’il existe |
| `aliases` | tableau non vide | surfaces reconnues, principalement FR/EN/NL |
| `status` | enum | `VEGAN`, `VEGETARIAN`, `NON_VEGAN` ou `UNCERTAIN` |
| `reason` | chaîne | justification affichée et diagnostique |
| `sources` | tableau non vide | identifiants référencés dans `knowledge/sources.json` |

L’asset embarqué conserve `id`, `name`, `eNumber`, `aliases`, `status`, `reason` et joint les identifiants de `sources` dans le champ chaîne `source`. `VeganAnalyzer.loadDatabase` transforme cet asset en objets `Ingredient` avec `org.json.JSONArray`.

Lors de l’audit de la version 0.6.4.1, l’asset contient 101 entrées : 74 `VEGAN`, 8 `VEGETARIAN`, 6 `NON_VEGAN` et 13 `UNCERTAIN`. Ces nombres décrivent cette révision, pas une contrainte du schéma.

## Signification des statuts

| Statut | Sens dans le moteur |
|---|---|
| `VEGAN` | origine végétale, minérale, microbienne ou synthétique considérée compatible selon la donnée revue |
| `VEGETARIAN` | ingrédient animal sans classification non végétarienne, donc incompatible vegan |
| `NON_VEGAN` | chair, tissu, insecte ou ingrédient explicitement non végétarien selon la base |
| `UNCERTAIN` | origine, procédé ou formulation insuffisamment déterminés par l’étiquette |

Les noms peuvent être trompeurs : le champ `NON_VEGAN` représente dans le verdict détaillé une cause `NON_VEGETARIAN`, tandis que `VEGETARIAN` est lui aussi un bloqueur pour la compatibilité vegan. Consulter [le moteur de verdict](verdict.md).

## Sources

`knowledge/sources.json` décrit les identifiants cités : base des additifs de la Commission européenne, Vegan Society, Vegetarian Society et sources spécifiques. Le champ `use` précise leurs limites. Certaines références ne sont que des pistes de recherche et ne justifient pas à elles seules un statut.

Les qualifications d’origine suivent leur propre source éditoriale, `knowledge/origin_qualifier_rules.json`, copiée vers les assets par `tools/build_origin_rules.py`. Son schéma versionné contient sources, règles actives ou en revue et expressions protégées.

## Modifier la base

1. Modifier uniquement `knowledge/ingredients.json`.
2. Vérifier l’unicité des identifiants et des alias, le statut, la raison et les sources.
3. Générer l’asset :

   ```bash
   python tools/build_ingredients.py
   ```

4. Vérifier sans écrire :

   ```bash
   python tools/build_ingredients.py --check
   ```

5. Ajouter des tests de matching et de verdict pour les cas ambigus.

Le générateur refuse un alias appartenant à deux identifiants et un statut hors enum. Il ne vérifie pas automatiquement la qualité éditoriale d’une preuve : cette revue reste humaine.

Cette tâche documentaire ne modifie aucune donnée de `ingredients.json`.

