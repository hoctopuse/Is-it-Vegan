# Référentiel UE des additifs

Prérequis : Python 3 et `pypdf` (`python -m pip install pypdf`). Le script ne requiert aucun accès réseau et ne touche jamais `ingredients.json`.

Depuis PowerShell :

```powershell
$env:PYTHONIOENCODING = 'utf-8'
.\.venv\Scripts\python.exe tools\extract_eu_additives_reference.py --dry-run
.\.venv\Scripts\python.exe tools\extract_eu_additives_reference.py --output EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv
```

Les quatre PDF sous `reference-input/eu-additives/` sont les valeurs par défaut. Les options `--fr`, `--nl`, `--en` et `--de` acceptent d’autres chemins.

Le CSV est UTF-8 et contient les 16 colonnes documentées dans son en-tête, avec les noms officiels FR/NL/EN/DE, le numéro E exact, le statut de vérification et la note associée. Les suffixes sont des clés distinctes : `E160b(i)`, `E160b(ii)` et `E960b` ne sont pas fusionnés.

`--dry-run` ne crée aucun CSV. `--output` écrit d’abord un fichier temporaire dans le répertoire cible, le relit avec le lecteur CSV standard, contrôle les clés, puis le remplace atomiquement. Le script échoue si l’annexe est absente, si la jointure est vide ou si des clés sont dupliquées.

Limites : la mise en page PDF peut produire des continuations structurées. Le seul cas actuellement reconstruit est `E960b` FR, strictement lorsqu’il suit `E960a` et existe dans les trois autres éditions. Les groupes, plages, clés non équivalentes (`E345` / `E345(i)`) et traductions absentes restent signalés, jamais déduits.
