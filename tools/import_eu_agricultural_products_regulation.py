#!/usr/bin/env python3
"""Add reviewed CELEX 02013R1308 product terms from four local PDFs only."""
from __future__ import annotations
import argparse, copy, hashlib, json, re, unicodedata
from pathlib import Path
from pypdf import PdfReader

ROOT=Path(__file__).resolve().parents[1]; BASE=ROOT/'reference-input/eu-food-labelling/02-sector-product-standards'
PDFS={x:BASE/f'CELEX_02013R1308-20260818_{x}_TXT.pdf' for x in ('FR','NL','EN','DE')}
I=ROOT/'knowledge/ingredients.json'; L=ROOT/'knowledge/ingredient_aliases_multilingual.json'; S=ROOT/'knowledge/sources.json'
SOURCE={'id':'eu-agricultural-products-regulation-1308-2013-20260818','title':'Regulation (EU) No 1308/2013 — common organisation of the markets in agricultural products','celex':'02013R1308','url':'https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02013R1308-20260818','language':'FR, NL, EN, DE','institution':'European Parliament and Council of the European Union','consolidationDate':'2026-08-18','localPdfPaths':[str(x.relative_to(ROOT)).replace('\\','/') for x in PDFS.values()],'role':'Primary local source for Annex I agricultural sector names and Annex VII product definitions.','limitations':'Article 1 excludes fishery and aquaculture products. Regulatory sectors identify agricultural goods but do not establish the composition of a general commercial preparation.'}
A={
'meat':{'FR':['viande bovine','viande de porc','viandes ovine et caprine','viande de volaille'],'NL':['rundvlees','varkensvlees','schapen- en geitenvlees','pluimveevlees'],'EN':['beef and veal','pigmeat','sheepmeat and goatmeat','poultrymeat'],'DE':['Rindfleisch','Schweinefleisch','Schaf- und Ziegenfleisch','Geflügelfleisch']},
'edible_offal':{'FR':["abats comestibles des animaux de l'espèce bovine"],'NL':['eetbare slachtafvallen van runderen'],'EN':['edible offal of bovine animals'],'DE':['Genießbare Schlachtnebenerzeugnisse von Rindern']},
'animal_fat':{'FR':['graisses de porc (y compris le saindoux)'],'NL':['varkensvet (reuzel daaronder begrepen)'],'EN':['pig fat (including lard)'],'DE':['Schweinefett (einschließlich Schweineschmalz)']},
'poultry_meat_preparation':{'FR':['préparation à base de viande de volaille'],'NL':['bereiding op basis van pluimveevlees'],'EN':['poultrymeat preparation'],'DE':['Geflügelfleischzubereitungen']},
'processed_fruit_vegetable_product':{'FR':['produits transformés à base de fruits et légumes'],'NL':['verwerkte groenten en fruit'],'EN':['processed fruit and vegetable products'],'DE':['Verarbeitungserzeugnisse aus Obst und Gemüse']},
'spreadable_fat':{'FR':['matières grasses tartinables'],'NL':['smeerbare vetten'],'EN':['spreadable fats'],'DE':['Streichfette']},
'egg':{'FR':["œufs de volailles de basse-cour, en coquille"],'NL':['eieren van pluimvee in de schaal'],'EN':['poultry eggs, in shell'],'DE':['Eier von Hausgeflügel in der Schale']},
'olive_oil':{'FR':["huile d'olive"],'NL':['olijfolie'],'EN':['olive oil'],'DE':['Olivenöl']},
}
NEW={'edible_offal':('Abats comestibles','NON_VEGAN','Parties comestibles d’animaux : non vegan.'),'animal_fat':('Graisse animale','NON_VEGAN','Graisse explicitement issue d’animaux : non vegan.'),'poultry_meat_preparation':('Préparation de viande de volaille','NON_VEGAN','Préparation explicitement à base de viande de volaille : non vegan.'),'processed_fruit_vegetable_product':('Produit transformé à base de fruits et légumes','UNCERTAIN','Catégorie réglementaire de produit transformé : lire la liste complète des ingrédients.'),'spreadable_fat':('Matière grasse tartinable','UNCERTAIN','Peut être végétale, animale ou mélangée selon la composition.')}
def n(x):
 x=unicodedata.normalize('NFKD',x.casefold());return re.sub(r'[^a-z0-9]+',' ',''.join(c for c in x if not unicodedata.combining(c))).strip()
def compact(x):return re.sub(r'\s+','',n(x))
def text(p):return '\n'.join(x.extract_text() or '' for x in PdfReader(p).pages)

# This opt-in batch is the explicitly authorized v0.7 animal review.
# The CSV remains documentary evidence, never an open-ended import input.
ANIMAL_PDF_HASHES = {
    "FR": "5ea4e8f11d1ad1871ae780bbdc05ee4beff9a2283b8876be6bffaec4e73f4a4c",
    "NL": "99ee3d7c91c02249f6ca6ffca15e06cf2e9603ab9718919138dd2a981281bb74",
    "EN": "b5cc2ea8e761cc408cca7f8930de7b2c01c2609f88dcb5a6bdbc4882e4455d11",
    "DE": "64a45b265e50b848f0a1ae239b94a1feef40608b69c69c9e5e14f0ffb7328dda"
}
# concept, language, exact surface, PDF page, character offset, legal location, review ID
ANIMAL_REVIEW = [["whey","FR","lactosérum",182,1392,"Annex VII / Part III / 2(a)(i)","A289e9d1a136f"],
    ["whey","NL","wei",182,1495,"Annex VII / Part III / 2(a)(i)","A183557fc283e"],
    ["whey","EN","whey",182,1167,"Annex VII / Part III / 2(a)(i)","A60e5ccf268ea"],
    ["whey","DE","Molke",182,1396,"Annex VII / Part III / 2(a)(i)","A72019b963880"],
    ["buttermilk","FR","babeurre",182,1438,"Annex VII / Part III / 2(a)(iv)","Ada8f271ba175"],
    ["buttermilk","NL","karnemelk",182,1532,"Annex VII / Part III / 2(a)(iv)","Aa529996c2e06"],
    ["buttermilk","NL","botermelk",182,1545,"Annex VII / Part III / 2(a)(iv)","A4ff2656fc5da"],
    ["buttermilk","EN","buttermilk",182,1210,"Annex VII / Part III / 2(a)(iv)","Ae657dd855555"],
    ["buttermilk","DE","Buttermilch",182,1436,"Annex VII / Part III / 2(a)(iv)","A18f0b1e574d3"],
    ["casein","FR","caséines",182,1470,"Annex VII / Part III / 2(a)(vi)","A85544edb0b1f"],
    ["casein","NL","caseïne",182,1578,"Annex VII / Part III / 2(a)(vi)","A02f451e82730"],
    ["casein","EN","caseins",182,1246,"Annex VII / Part III / 2(a)(vi)","A6c531a4d338a"],
    ["casein","DE","Kaseine",182,1471,"Annex VII / Part III / 2(a)(vi)","A4df9d493272a"],
    ["milk","FR","lait cru",184,151,"Annex VII / Part IV / III.1(a)","Adc4bb6fba6d2"],
    ["milk","NL","rauwe melk",184,137,"Annex VII / Part IV / III.1(a)","Ac2ef1da4816c"],
    ["milk","EN","raw milk",184,143,"Annex VII / Part IV / III.1(a)","A51f27fce353d"],
    ["milk","DE","Rohmilch",184,121,"Annex VII / Part IV / III.1(a)","A39397e566562"],
    ["milk","FR","lait entier",184,263,"Annex VII / Part IV / III.1(b)","Af1b905bf492b"],
    ["milk","NL","volle melk",184,275,"Annex VII / Part IV / III.1(b)","A2173a9aeb1c6"],
    ["milk","EN","whole milk",184,257,"Annex VII / Part IV / III.1(b)","Ac0cbaec6a68d"],
    ["milk","DE","Vollmilch",184,241,"Annex VII / Part IV / III.1(b)","Ab6dc3e75fb82"],
    ["milk","FR","lait demi-écrémé",184,1039,"Annex VII / Part IV / III.1(c)","A7134bf4c221f"],
    ["milk","NL","halfvolle melk",184,902,"Annex VII / Part IV / III.1(c)","A8e8310a736ba"],
    ["milk","EN","semi-skimmed milk",184,874,"Annex VII / Part IV / III.1(c)","Ab4009a89bc12"],
    ["milk","DE","teilentrahmte Milch",184,879,"Annex VII / Part IV / III.1(c)","A8bb6aad63967"],
    ["milk","DE","fettarme Milch",184,900,"Annex VII / Part IV / III.1(c)","Ab493a2dc8414"],
    ["milk","FR","lait écrémé",184,1220,"Annex VII / Part IV / III.1(d)","A388133501653"],
    ["milk","NL","magere melk",184,1038,"Annex VII / Part IV / III.1(d)","A418fce16500c"],
    ["milk","EN","skimmed-milk",184,1004,"Annex VII / Part IV / III.1(d)","A20bb306b20e5"],
    ["milk","EN","skimmed milk",184,879,"Annex VII / Part IV / III.1(d)","A6b449c759611"],
    ["milk","DE","entrahmte Milch",184,883,"Annex VII / Part IV / III.1(d)","A69b7a95c50ab"],
    ["milk","DE","Magermilch",184,1083,"Annex VII / Part IV / III.1(d)","A67abd3e7fd64"],
    ["honey","FR","Miel naturel",147,1397,"Annex I / Part XXII / NC 0409","A2caa79f5e553"],
    ["honey","NL","Natuurhoning",147,1426,"Annex I / Part XXII / NC 0409","A35a54cfde886"],
    ["honey","EN","Natural honey",147,1346,"Annex I / Part XXII / NC 0409","Af1c92aa49e3d"],
    ["honey","DE","Natürlicher Honig",147,1483,"Annex I / Part XXII / NC 0409","Aae782f0a7271"],
    ["royal_jelly","FR","Gelée royale",147,1426,"Annex I / Part XXII / NC 0410 (edible)","A7da6efb7062f"],
    ["royal_jelly","NL","koninginnengelei",147,1463,"Annex I / Part XXII / NC 0410 (edible)","Abd979b4c4478"],
    ["royal_jelly","EN","Royal jelly",147,1376,"Annex I / Part XXII / NC 0410 (edible)","Abba71bd99577"],
    ["royal_jelly","DE","Gelée Royale",147,1517,"Annex I / Part XXII / NC 0410 (edible)","Ab1ad9606ca9c"],
    ["propolis","FR","propolis",147,1442,"Annex I / Part XXII / NC 0410 (edible)","A692ddafa9421"],
    ["propolis","NL","propolis",147,1483,"Annex I / Part XXII / NC 0410 (edible)","Aed2bcffb78e9"],
    ["propolis","EN","propolis",147,1392,"Annex I / Part XXII / NC 0410 (edible)","Aee51efe73b00"],
    ["propolis","DE","Kittharz",147,1534,"Annex I / Part XXII / NC 0410 (edible)","Af1c9d73d9d15"],
    ["egg","FR","jaunes d'œufs",146,927,"Annex I / Part XIX / NC 0408 (edible)","Adafa81bdcc53"],
    ["egg","NL","eigeel",146,882,"Annex I / Part XIX / NC 0408 (edible)","Adac7f8cc8e7c"],
    ["egg","EN","egg yolks",146,772,"Annex I / Part XIX / NC 0408 (edible)","A75570ef55abd"],
    ["egg","DE","Eigelb",146,947,"Annex I / Part XIX / NC 0408 (edible)","Af7431c6960d8"],
    ["edible_offal","FR","Abats comestibles",143,1861,"Annex I / Part XV / NC 0206 (edible)","Ad22b3d3ef8fc"],
    ["edible_offal","NL","Eetbare slachtafvallen",143,1803,"Annex I / Part XV / NC 0206 (edible)","A5a5ad06deef7"],
    ["edible_offal","EN","Edible offal",143,1714,"Annex I / Part XV / NC 0206 (edible)","A4363d135d25c"],
    ["edible_offal","DE","Genießbare Schlachtnebenerzeugnisse",143,1940,"Annex I / Part XV / NC 0206 (edible)","A58c0f3cbbc8f"],
    ["edible_offal","FR","Foies de volailles",146,1635,"Annex I / Part XX / (c)","Ab1dacfa67c8b"],
    ["edible_offal","NL","Levers van pluimvee",146,1589,"Annex I / Part XX / (c)","Af3a74cd38474"],
    ["edible_offal","EN","Poultry livers",146,1544,"Annex I / Part XX / (c)","A1cc3515d0fe8"],
    ["edible_offal","DE","Geflügelleber",146,1651,"Annex I / Part XX / (c)","A86b4de7910a1"],
    ["edible_offal","DE","Geflügellebern",146,1722,"Annex I / Part XX / (c)","A87d7c7e923ef"],
    ["animal_fat","FR","graisse de porc",145,581,"Annex I / Part XVII / NC 0209 and 1501","A29ea7e973802"],
    ["animal_fat","NL","Varkensvet",145,787,"Annex I / Part XVII / NC 0209 and 1501","A7c22426e62af"],
    ["animal_fat","EN","Pig fat",145,456,"Annex I / Part XVII / NC 0209 and 1501","A98b06cb91547"],
    ["animal_fat","DE","Schweinefett",145,534,"Annex I / Part XVII / NC 0209 and 1501","Af7702e07a8fb"],
    ["animal_fat","FR","saindoux",145,869,"Annex I / Part XVII / NC 1501","A77ca12c2e0cc"],
    ["animal_fat","NL","reuzel",145,799,"Annex I / Part XVII / NC 1501","A9c922c0af2bb"],
    ["animal_fat","EN","lard",145,711,"Annex I / Part XVII / NC 1501","A0cdd43811cdf"],
    ["animal_fat","DE","Schweineschmalz",145,852,"Annex I / Part XVII / NC 1501","Ac0bf87a8f6bc"],
    ["animal_fat","FR","Graisses de volaille",146,1930,"Annex I / Part XX / (e) / NC 1501","Abf8d004ce576"],
    ["animal_fat","NL","Vet van gevogelte",146,1737,"Annex I / Part XX / (e) / NC 1501","Ae13338bfb26d"],
    ["animal_fat","EN","Poultry fat",146,1611,"Annex I / Part XX / (e) / NC 1501","Ac71bfbed5422"],
    ["animal_fat","DE","Geflügelfett",146,1804,"Annex I / Part XX / (e) / NC 1501","Aea78fce14025"],
    ["animal_fat","FR","Graisses des animaux de l'espèce bovine",144,217,"Annex I / Part XV / NC 1502","Adba12345da26"],
    ["animal_fat","NL","Rundervet",144,213,"Annex I / Part XV / NC 1502","A6df3fba4a0e7"],
    ["animal_fat","EN","Fats of bovine animals",144,210,"Annex I / Part XV / NC 1502","A617e8de7ce15"],
    ["animal_fat","DE","Fett von Rindern",144,265,"Annex I / Part XV / NC 1502","A4bfa1ef450fc"],
    ["animal_fat","FR","Graisse des animaux des espèces ovine et caprine",146,351,"Annex I / Part XVIII / NC 1502","Aa7cc4fa90db1"],
    ["animal_fat","NL","Schapen- of geitenvet",146,335,"Annex I / Part XVIII / NC 1502","A96ad2cedeb95"],
    ["animal_fat","EN","Fats of sheep or goats",146,278,"Annex I / Part XVIII / NC 1502","A6dae67671904"],
    ["animal_fat","DE","Fett von Schafen oder Ziegen",146,367,"Annex I / Part XVIII / NC 1502","A5bf94d849f1b"]]
ANIMAL_NEW = {
    'buttermilk': ('Babeurre', 'Produit exclusivement laitier : végétarien mais non vegan.'),
    'royal_jelly': ('Gelée royale', 'Convention de l’application Is It Vegan? : produit apicole considéré végétarien, mais non vegan. Ce choix ne constitue pas une règle universelle de certification.'),
    'propolis': ('Propolis', 'Convention de l’application Is It Vegan? : produit apicole considéré végétarien, mais non vegan. Ce choix ne constitue pas une règle universelle de certification.'),
}
ANIMAL_CONVENTION = {
    'id': 'isitvegan-animal-products-convention-v0-7',
    'title': 'Is It Vegan? v0.7 — convention éditoriale pour gelée royale et propolis',
    'institution': 'Is It Vegan? project',
    'revision': '0.7',
    'localDocumentationPath': 'reports/0.7/eu-pdf/EU_1308_2013_ANIMAL_AUTHORIZED_IMPORT_REPORT.md',
    'role': 'Explicit application convention: royal_jelly and propolis are VEGETARIAN, hence not vegan.',
    'limitations': 'Application convention, not a universal vegetarian certification rule; the regulation is evidence for names and apiculture context, not this classification.',
}
ANIMAL_EXISTING = {'whey', 'casein', 'milk', 'honey', 'egg', 'edible_offal', 'animal_fat'}


def runtime_normalized(value):
    value = unicodedata.normalize('NFD', value.lower().replace('œ', 'oe').replace('æ', 'ae'))
    value = re.sub(r'[^a-z0-9]+', ' ', ''.join(c for c in value if not unicodedata.combining(c))).strip()
    return re.sub(r'^e\\s+(?=\\d)', 'e', value)


def animal_evidence():
    """Verify every literal on its specific primary page before planning mutation."""
    pages = {}
    for language, path in PDFS.items():
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        if digest != ANIMAL_PDF_HASHES[language]:
            raise ValueError(f'animal source hash mismatch: {path.name}')
        reader = PdfReader(path)
        if len(reader.pages) != 219:
            raise ValueError(f'animal source pagination mismatch: {path.name}')
        for number in {row[3] for row in ANIMAL_REVIEW if row[1] == language}:
            value = reader.pages[number - 1].extract_text() or ''
            if not value.strip() or '\ufffd' in value:
                raise ValueError(f'incomplete animal extraction: {language}/{number}')
            if not any('02013R1308' in line and '18.08.2026' in line and language in line
                       for line in value.splitlines()):
                raise ValueError(f'animal source identity mismatch: {language}/{number}')
            pages[language, number] = value
    grouped = {}
    for concept, language, surface, number, offset, location, review_id in ANIMAL_REVIEW:
        if concept not in ANIMAL_EXISTING | set(ANIMAL_NEW):
            raise ValueError(f'concept outside authorized animal batch: {concept}')
        if not surface or any(char in surface for char in ('\xad', '\n', '\r')):
            raise ValueError(f'extraction artifact in animal alias: {surface!r}')
        value = pages[language, number]
        if value[offset:offset + len(surface)] != surface:
            raise ValueError(f'animal page wording mismatch: {language}/{number}/{surface}')
        key = concept, language, n(surface)
        evidence = {
            'pdfFile': PDFS[language].name, 'pdfPage': number,
            'legalLocation': location, 'sourceOffset': offset, 'surfaceForm': surface,
            'reviewId': review_id, 'sha256': ANIMAL_PDF_HASHES[language],
        }
        if key not in grouped:
            grouped[key] = {'surface': surface, 'evidence': []}
        grouped[key]['evidence'].append(evidence)
    for concept in ANIMAL_NEW:
        if {key[1] for key in grouped if key[0] == concept} != set(PDFS):
            raise ValueError(f'missing language for new animal concept: {concept}')
    return grouped


def animal_import(args):
    grouped = animal_evidence()
    db = json.loads(I.read_text(encoding='utf8'))
    lex = json.loads(L.read_text(encoding='utf8'))
    sources = json.loads(S.read_text(encoding='utf8'))
    before = copy.deepcopy((db, lex, sources))
    ids = {entry['id']: entry for entry in db}
    if len(ids) != len(db) or not ANIMAL_EXISTING <= set(ids):
        raise ValueError('missing or duplicated existing animal concept')
    primary = next((entry for entry in sources if entry['id'] == SOURCE['id']), None)
    if primary != SOURCE:
        raise ValueError('conflicting or missing primary 1308 source record')
    conventions = [entry for entry in sources if entry['id'] == ANIMAL_CONVENTION['id']]
    if conventions and (len(conventions) != 1 or conventions[0] != ANIMAL_CONVENTION):
        raise ValueError('conflicting animal application convention')
    if not conventions:
        sources.append(copy.deepcopy(ANIMAL_CONVENTION))
    added_concepts = []
    for concept, (name, reason) in ANIMAL_NEW.items():
        if concept not in ids:
            provenance = [SOURCE['id']]
            if concept in {'royal_jelly', 'propolis'}:
                provenance.append(ANIMAL_CONVENTION['id'])
            entry = {'id': concept, 'name': name, 'aliases': [], 'status': 'VEGETARIAN',
                     'reason': reason, 'sources': provenance}
            db.append(entry)
            ids[concept] = entry
            added_concepts.append(concept)
        elif ids[concept]['status'] != 'VEGETARIAN':
            raise ValueError(f'conflicting classification for animal concept: {concept}')
    # Check both importer and runtime normalization, including canonical names.
    owners = {}
    language_owners = {}
    for entry in db:
        for surface in [entry['name']] + entry['aliases']:
            for normalize in (n, runtime_normalized):
                owners.setdefault((normalize.__name__, normalize(surface)), set()).add(entry['id'])
    for entry in lex['aliases']:
        for surface in entry['aliases'] + entry['ocrVariants']:
            for normalize in (n, runtime_normalized):
                key = normalize.__name__, entry['language'], normalize(surface)
                language_owners.setdefault(key, set()).add(entry['canonicalId'])
    keys = {(entry['conceptId'], entry['language'], entry['normalizedForm']) for entry in lex['mappings']}
    if len(keys) != len(lex['mappings']):
        raise ValueError('pre-existing duplicate mapping keys; refusing animal import')
    additions = {'concepts': added_concepts, 'canonicalAliases': [], 'languageAliases': [],
                 'mappings': [], 'sources': []}
    for (concept, language, normalized), record in grouped.items():
        surface = record['surface']
        for normalize in (n, runtime_normalized):
            global_key = normalize.__name__, normalize(surface)
            language_key = normalize.__name__, language, normalize(surface)
            if (owners.get(global_key, set()) | language_owners.get(language_key, set())) - {concept}:
                raise ValueError(f'animal alias collision: {concept}/{language}/{surface}')
            owners.setdefault(global_key, set()).add(concept)
            language_owners.setdefault(language_key, set()).add(concept)
        entry = ids[concept]
        if SOURCE['id'] not in entry['sources']:
            entry['sources'].append(SOURCE['id'])
        if not any(n(alias) == normalized for alias in entry['aliases']):
            entry['aliases'].append(surface)
            additions['canonicalAliases'].append({'conceptId': concept, 'surfaceForm': surface})
        matches = [item for item in lex['aliases']
                   if item['canonicalId'] == concept and item['language'] == language]
        if len(matches) > 1:
            raise ValueError(f'duplicate animal lexicon entry: {concept}/{language}')
        if not matches:
            le = {'canonicalId': concept, 'language': language, 'aliases': [], 'ocrVariants': []}
            lex['aliases'].append(le)
        else:
            le = matches[0]
        if not any(n(alias) == normalized for alias in le['aliases'] + le['ocrVariants']):
            le['aliases'].append(surface)
            additions['languageAliases'].append({'conceptId': concept, 'language': language,
                                                'surfaceForm': surface})
        key = concept, language, normalized
        if key not in keys:
            mapping = {'surfaceForm': surface, 'language': language, 'conceptId': concept,
                       'mappingGroup': 'agricultural-products-regulation',
                       'relation': 'REGULATORY_ALIAS', 'normalizedForm': normalized,
                       'source': SOURCE['id'], 'confidence': 'REVIEWED',
                       'sourceEvidence': record['evidence']}
            lex['mappings'].append(mapping)
            keys.add(key)
            additions['mappings'].append(mapping)
    # Guard all historical fields, mappings and aliases, not a fixed past count.
    for old in before[0]:
        current = ids[old['id']]
        for key, value in old.items():
            if key in {'aliases', 'sources'}:
                if current[key][:len(value)] != value:
                    raise ValueError(f'historical animal data modified: {old["id"]}/{key}')
            elif current[key] != value:
                raise ValueError(f'historical animal field modified: {old["id"]}/{key}')
    for field, value in before[1].items():
        if field == 'aliases':
            for old, current in zip(value, lex[field]):
                if any(current[key] != old[key] for key in old if key != 'aliases'):
                    raise ValueError('historical multilingual field modified')
                if current['aliases'][:len(old['aliases'])] != old['aliases']:
                    raise ValueError('historical multilingual aliases modified')
        elif field == 'mappings':
            if lex[field][:len(value)] != value:
                raise ValueError('historical mappings modified')
        elif lex[field] != value:
            raise ValueError(f'historical lexicon field modified: {field}')
    if sources[:len(before[2])] != before[2]:
        raise ValueError('historical sources modified')
    count = sum(len(item['aliases']) + len(item['ocrVariants']) for item in lex['aliases'])
    # cereals is a historical unavailable concept and has no declared mappings.
    unavailable = set(item['canonicalId'] for item in lex['aliases']) - set(ids)
    unavailable_count = sum(len(item['aliases']) + len(item['ocrVariants']) for item in lex['aliases']
                            if item['canonicalId'] in unavailable)
    if len(lex['mappings']) != count - unavailable_count:
        raise ValueError('animal mapping metadata does not cover available aliases')
    additions['sources'] = [entry['id'] for entry in sources[len(before[2]):]]
    changed = (db, lex, sources) != before
    counts = {key: len(value) for key, value in additions.items()}
    print(f'CELEX 02013R1308 animal batch: changes={int(changed)}; additions={json.dumps(counts)}')
    if args.dry_run:
        print(json.dumps(additions, ensure_ascii=False, indent=2))
    if args.check and changed:
        raise SystemExit('authorized animal import is stale; run --animal-enrichment --write')
    if args.write and changed:
        for path, value in ((I, db), (L, lex), (S, sources)):
            path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf8')

def main():
 p=argparse.ArgumentParser();g=p.add_mutually_exclusive_group(required=True);g.add_argument('--dry-run',action='store_true');g.add_argument('--write',action='store_true');g.add_argument('--check',action='store_true');p.add_argument('--animal-enrichment',action='store_true',help='Import only the authorized v0.7 animal review batch');a=p.parse_args()
 if a.animal_enrichment:
  animal_import(a);return
 ts={lang:compact(text(path)) for lang,path in PDFS.items()}; missing=[f'{k}/{lang}/{x}' for k,v in A.items() for lang,xs in v.items() for x in xs if compact(x) not in ts[lang]]
 if missing:raise ValueError('unverified local-PDF aliases: '+str(missing))
 db=json.loads(I.read_text(encoding='utf8'));lex=json.loads(L.read_text(encoding='utf8'));sources=json.loads(S.read_text(encoding='utf8'))
 if len(db)<459 or len(lex.get('mappings',[]))<1864:raise ValueError('historical knowledge is missing; refusing destructive import')
 ids={x['id'] for x in db}; owners={n(z):x['id'] for x in db for z in x.get('aliases',[])}; lexowners={(x['language'],n(z)):x['canonicalId'] for x in lex['aliases'] for z in x.get('aliases',[])+x.get('ocrVariants',[])}; keys={(x['conceptId'],x['language'],x['normalizedForm']) for x in lex.get('mappings',[])}; changed=False
 for k,langs in A.items():
  if k not in ids:
   name,status,reason=NEW[k]; entry={'id':k,'name':name,'aliases':[],'status':status,'reason':reason,'sources':[SOURCE['id']]}
   if k in {'processed_fruit_vegetable_product','spreadable_fat'}:entry['possibleOriginNote']={'origins':['PLANT','ANIMAL'],'variability':'MANUFACTURER','sourceIds':[SOURCE['id']],'confidence':'REGULATORY_CATEGORY'}
   db.append(entry);ids.add(k);changed=True
  entry=next(x for x in db if x['id']==k)
  if k=='cheese' and entry['status']=='UNCERTAIN': entry['status']='VEGETARIAN';entry['reason']='Produit laitier d’origine animale : végétarien mais non vegan.';changed=True
  if SOURCE['id'] not in entry['sources']:entry['sources'].append(SOURCE['id']);changed=True
  for lang,xs in langs.items():
   le=next((x for x in lex['aliases'] if x['canonicalId']==k and x['language']==lang),None)
   if not le:le={'canonicalId':k,'language':lang,'aliases':[],'ocrVariants':[]};lex['aliases'].append(le);changed=True
   for x in xs:
    if owners.get(n(x),k)!=k or lexowners.get((lang,n(x)),k)!=k:raise ValueError(f'alias collision: {lang}/{x}')
    if x not in entry['aliases']:entry['aliases'].append(x);changed=True
    if x not in le['aliases']:le['aliases'].append(x);changed=True
    r={'surfaceForm':x,'language':lang,'conceptId':k,'mappingGroup':'agricultural-products-regulation','relation':'REGULATORY_ALIAS','normalizedForm':n(x),'source':SOURCE['id'],'confidence':'REVIEWED'}; key=(k,lang,r['normalizedForm'])
    if key not in keys:lex.setdefault('mappings',[]).append(r);keys.add(key);changed=True
 for k in ('milk','cream','butter','whey','casein','lactose','cheese'):
  e=next(x for x in db if x['id']==k)
  if SOURCE['id'] not in e['sources']:e['sources'].append(SOURCE['id']);changed=True
 if not any(x['id']==SOURCE['id'] for x in sources):sources.append(SOURCE);changed=True
 print(f'CELEX 02013R1308: {len(NEW)} concepts, {sum(len(x) for v in A.values() for x in v.values())} aliases, changes={int(changed)}')
 if a.check and changed:raise SystemExit('agricultural regulation import is stale; run --write')
 if a.write and changed:
  I.write_text(json.dumps(db,ensure_ascii=False,indent=2)+'\n',encoding='utf8');L.write_text(json.dumps(lex,ensure_ascii=False,indent=2)+'\n',encoding='utf8');S.write_text(json.dumps(sources,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':main()
