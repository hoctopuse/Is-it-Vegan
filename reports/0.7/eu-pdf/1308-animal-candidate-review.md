# EU 1308/2013 — première revue des candidats d’origine animale

**Objet :** préparer un petit lot de révision pour élargir le corpus d’ingrédients, à partir de l’extraction large du règlement 1308/2013.

**État :** revue éditoriale préliminaire, pas un import. Les PDF, les données actuelles et les commandes de validation du dépôt n’étaient pas disponibles dans cet espace. Les candidats ci-dessous doivent donc être confrontés au dépôt avant toute modification de la base.

## Décision de cadrage

Retenir les mots qui peuvent apparaître sur une étiquette comme ingrédient, produit animal, partie comestible ou dérivé. Écarter du premier lot les catégories douanières, les codes NC, les descriptions de marché, les termes d’élevage et les variantes purement réglementaires. Une mention de règlement ne prouve pas à elle seule qu’un terme doit être un concept distinct.

## Candidats prioritaires

| Famille | Candidats à vérifier | Traitement proposé | Indice dans l’extraction |
|---|---|---|---|
| Produits de la ruche | pollen, propolis, gelée royale | Concepts distincts si absents ; ajouter les formes FR/NL/EN/DE attestées sans fusionner les produits | Annex I Part XXII ; Annex II Part IX ; p. 147 et 163 |
| Lait et dérivés nommés | babeurre, lactosérum, caséines, butteroil, matière grasse laitière anhydre | Vérifier les concepts existants ; ajouter les alias linguistiques manquants ; garder les dérivés distincts du concept générique « lait » | Annex VII Part III 2(a)(i–vii), p. 182 ; Annex I Part XVI, p. 144 |
| Laits transformés | lait cru, lait entier, lait écrémé, poudres de lait | Alias ou variantes de concepts déjà présents si leur sens correspond ; ne pas créer un concept pour chaque teneur ou forme sans besoin d’usage | Annex VII Part IV, p. 184 |
| Viande et parties comestibles | abats, carcasse, demi-carcasse, foie de volaille, jaune d’œuf | Ajouter seulement les termes utiles à la reconnaissance d’étiquette ; distinguer l’animal, la partie et le produit | Annexes I, IV, VII ; notamment p. 143–150 et 171–174 |
| Graisses animales | graisse de porc, saindoux, graisses de volaille, graisses bovines/ovines | Vérifier les concepts génériques existants et ajouter les formes qui ont une portée d’ingrédient ; séparer des catégories de commerce | Annex I Parts XV, XVII, XVIII et XXIV, p. 143–154 |
| Miel | miel | Le concept existe déjà dans la couverture signalée par le CSV ; priorité aux alias et aux formes composées réellement présentes, sans dupliquer le concept | Annex I Part XXII ; consolidation dans le rapport d’extraction |
| Œufs | œufs, jaune d’œuf, œufs de volaille | Le concept générique « egg » existe déjà ; vérifier les alias et ne créer des concepts de parties que s’ils servent la reconnaissance | Annex I Part XIX ; Annex II Part VII ; Annex VII Part VI |

## Déjà couvert ou à ne pas compter comme concept nouveau

- Les identifiants globaux signalent déjà `milk`, `egg`, `meat`, `honey`, `beeswax`, `butter`, `whey`, `lactose` et `edible_offal` pour certaines lignes. Cela indique une couverture au niveau du concept, pas nécessairement une couverture des quatre langues.
- `NO_EXACT_CURRENT_LANGUAGE_ALIAS` signifie seulement qu’aucune correspondance exacte n’a été trouvée par l’extraction. Ce n’est pas une preuve d’absence du concept, ni une autorisation d’ajouter chaque variante.
- Les termes composés qui décrivent une catégorie légale, un code douanier, une carcasse entière, une denrée non comestible ou une matière première d’alimentation animale ne doivent pas gonfler artificiellement le compteur de concepts alimentaires.
- Les espaces et césures de PDF (« ra w milk », par exemple) sont des artefacts d’extraction à corriger avec le PDF, jamais des alias.

## Allergènes hors ligne

À traiter comme une petite extension du corpus existant : pour les ingrédients concernés, une information d’allergène issue de la réglementation d’étiquetage, consultable hors ligne et traduite avec les alias. Ce lot 1308 ne suffit pas à établir cette liste. La prochaine vérification devra partir du règlement 1169/2011, annexe II, et distinguer la présence d’un allergène de l’origine animale ou du statut vegan.

## Conditions avant import

1. Comparer chaque candidat et alias avec les fichiers de connaissance actuels.
2. Vérifier la forme et le contexte dans les PDF sources, surtout les tableaux et les césures.
3. Confirmer la licence, la provenance et le statut de source selon le workflow du dépôt.
4. Exécuter uniquement le mode de validation documenté de l’importeur correspondant, après inspection de son état Git et de ses effets.
5. Mesurer le gain en concepts distincts et alias par langue ; ne pas utiliser le nombre d’occurrences du PDF comme mesure de croissance.

## Source de travail et limites

Cette sélection s’appuie sur `EU_1308_2013_BROAD_CANDIDATES.csv` et le rapport d’extraction fournis. Le CSV recense des occurrences extraites, pas des concepts validés. Les quatre langues sont représentées, mais l’extraction ne remplace ni la vérification visuelle des PDF, ni la comparaison avec la base actuelle.
