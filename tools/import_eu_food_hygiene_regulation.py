#!/usr/bin/env python3
"""Import only the four reviewed mechanically-separated-meat denominations.

Source-specific adapter for 853/2004, with local 1169/2011 label evidence.
Dry-run proposes a merge; check requires the complete existing batch; only
write changes editorial JSON. Assets/documentation use the normal builders.
"""
import argparse
import copy
import hashlib
import json
from pathlib import Path
import re
import unicodedata

from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
I = ROOT / 'knowledge/ingredients.json'
L = ROOT / 'knowledge/ingredient_aliases_multilingual.json'
S = ROOT / 'knowledge/sources.json'
CONCEPT = 'mechanically_separated_meat'
FORMS = {'FR': 'viandes séparées mécaniquement', 'NL': 'separatorvlees',
         'EN': 'mechanically separated meat', 'DE': 'Separatorenfleisch'}
PRIMARY_ID = 'eu-food-hygiene-regulation-853-2004-20260507'
LABEL_ID = 'eu-fic-regulation-1169-2011-20250401'
LANGUAGE_CODES = dict(zip(('FRENCH', 'DUTCH', 'ENGLISH', 'GERMAN', 'ITALIAN', 'SPANISH', 'POLISH'),
                          ('FR', 'NL', 'EN', 'DE', 'IT', 'ES', 'PL')))
# SHA-256 and offsets in pypdf's unmodified page text, reviewed against the PDF.
# Source casing is preserved separately from the four approved lexical forms.
PRIMARY = {
    'FR': ('302312dbf898ef8993b11057070691801c8c374d8de13b5947ff592a2309562e', 2215, 'viandes séparées mécaniquement'),
    'NL': ('30fa09a89bcede829396f29769812b5c4503f404c91a9acb2771b429a1356543', 2047, 'Separatorvlees'),
    'EN': ('125b70e276a8e21d9af7e72c6af773d19068896b126a54b777c08bd49fb7037d', 1951, 'Mechanically separated meat'),
    'DE': ('9874ff8be78fcd7d33a16d837f85cbe46ae7abcfd7d70d1c1dc9542deeb7602f', 2101, 'Separatorenfleisch'),
}
LABEL = {
    'FR': ('80fedf36e7930dddfb270871d68ff10864009fd3ac71c9d350876402c9d6a81e', 1764, 'Viandes séparées mécaniquement'),
    'NL': ('36dd5f61e3e9aeb95c2e51b4ff22ba90056639f726d4b30aae97c63c653c8ef3', 1563, 'separatorvlees'),
    'EN': ('6cdf4190d6fa4a99d2e7d3124611724ec4cf69159563f88b35dc3c50dd416c63', 1468, 'mechanically separated meat'),
    'DE': ('c2409d5cb83f784a8ac94ceeb8d4837da343189e024f5d1a58cc015f6bb62aa2', 1690, 'Separatorenfleisch'),
}
SPECS = (
    (PRIMARY_ID, '02004R0853', '20260507', '2026-05-07', '07.05.2026',
     '05-food-hygiene', 94, 16, 'Annex I, point 1.14', PRIMARY,
     'Regulation (EC) No 853/2004 — hygiene rules for food of animal origin',
     'Primary definition and animal-origin evidence for mechanically separated meat.'),
    (LABEL_ID, '02011R1169', '20250401', '2025-04-01', '01.04.2025',
     '00-general-food-labelling', 60, 49, 'Annex VII, Part B, point 18', LABEL,
     'Regulation (EU) No 1169/2011 — food information to consumers',
     'Complementary ingredient-designation evidence; the denomination is accompanied by animal species names.'),
)


def normalized(value):
    """Kotlin TextNormalizer: NFD, Unicode marks, ASCII words, initial E code.

    Python lower() and Kotlin lowercase() agree for the reviewed Latin forms.
    No stemming, compatibility folding or translation is used.
    """
    for character in value:
        if unicodedata.category(character) in ('Cn', 'Cs'):
            raise ValueError(f'unverified Unicode codepoint U+{ord(character):04X}; '
                             'validate with Kotlin before using this lexicon')
    value = unicodedata.normalize('NFD', value.lower().replace('œ', 'oe').replace('æ', 'ae'))
    value = ''.join(c for c in value if not unicodedata.category(c).startswith('M'))
    return re.sub(r'^e\s+(?=\d)', 'e', re.sub(r'[^a-z0-9]+', ' ', value).strip())


def pdf_path(spec, language):
    return ROOT / 'reference-input/eu-food-labelling' / spec[5] / f'CELEX_{spec[1]}-{spec[2]}_{language}_TXT.pdf'


def source_records():
    return [{
        'id': spec[0], 'title': spec[10], 'celex': spec[1],
        'url': f'https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:{spec[1]}-{spec[2]}',
        'language': 'FR, NL, EN, DE',
        'institution': 'European Parliament and Council of the European Union',
        'consolidationDate': spec[3],
        'localPdfPaths': [pdf_path(spec, lang).relative_to(ROOT).as_posix() for lang in FORMS],
        'role': spec[11],
        'limitations': 'Only four reviewed base denominations; no species-prefixed/compound-label coverage, acronym, flexion or general context protection is implied.',
        'use': 'Source: European Union / EUR-Lex. Consolidated text reused under CC BY 4.0; https://eur-lex.europa.eu/content/legal-notice/legal-notice.html?locale=en . Only reviewed terms are selected; classification and explanations are application editorial decisions, not part of the regulation.',
    } for spec in SPECS]


def source_evidence():
    result = {lang: [] for lang in FORMS}
    for spec in SPECS:
        for lang in FORMS:
            path = pdf_path(spec, lang)
            digest, offset, surface = spec[9][lang]
            if hashlib.sha256(path.read_bytes()).hexdigest() != digest:
                raise ValueError(f'source hash mismatch: {path.name}')
            reader = PdfReader(path)
            if len(reader.pages) != spec[6]:
                raise ValueError(f'source pagination mismatch: {path.name}')
            page = reader.pages[spec[7] - 1].extract_text() or ''
            if not page.strip() or '\ufffd' in page:
                raise ValueError(f'incomplete source extraction: {path.name}')
            if not any(spec[1] in line and lang in line and spec[4] in line for line in page.splitlines()):
                raise ValueError(f'source identity mismatch: {path.name}')
            if page[offset:offset + len(surface)] != surface or normalized(surface) != normalized(FORMS[lang]):
                raise ValueError(f'source wording/offset mismatch: {path.name}')
            point = r'(?m)^\s*1\.14\.' if spec[0] == PRIMARY_ID else r'(?m)^\s*18\.'
            if not re.search(point, page[:offset]):
                raise ValueError(f'source legal point missing: {path.name}')
            result[lang].append({
                'pdfFile': path.name, 'pdfPage': spec[7], 'legalLocation': spec[8],
                'sourceOffset': offset, 'surfaceForm': surface,
                'reviewId': f'853-I1.14-{lang}-{CONCEPT}' if spec[0] == PRIMARY_ID else f'1169-VIIB18-{lang}-{CONCEPT}',
                'sha256': digest,
            })
    return result


def expected_batch(evidence):
    entry = {'id': CONCEPT, 'name': FORMS['FR'],
             'aliases': [form for lang, form in FORMS.items() if lang != 'FR'],
             'status': 'NON_VEGAN',
             'reason': 'Viande obtenue mécaniquement à partir d’os couverts de chair ou de carcasses de volailles : matière animale non végétarienne.',
             'sources': [PRIMARY_ID, LABEL_ID]}
    aliases = [{'canonicalId': CONCEPT, 'language': lang, 'aliases': [form], 'ocrVariants': []}
               for lang, form in FORMS.items()]
    mappings = [{'surfaceForm': form, 'language': lang, 'conceptId': CONCEPT,
                 'mappingGroup': 'food-hygiene-regulation', 'relation': 'REGULATORY_ALIAS',
                 'normalizedForm': normalized(form), 'source': ', '.join(entry['sources']),
                 'confidence': 'REVIEWED', 'sourceEvidence': evidence[lang]}
                for lang, form in FORMS.items()]
    return entry, aliases, mappings


def runtime_language(value, location):
    if not isinstance(value, str):
        raise ValueError(f'{location}: language must be a supported string')
    code = LANGUAGE_CODES.get(value.upper(), value.upper())
    if code not in LANGUAGE_CODES.values():
        raise ValueError(f'{location}: unsupported runtime language {value!r}')
    return code


def validate_runtime_lexicon(lex):
    """Conservative subset of MultilingualIngredientLexicon.load's input contract.

    Do not emulate its silent coercions/filtering of malformed lists or fields.
    Require the repository's typed schema; check normalized keys as the real
    loader does. Protected inputs cannot change normalized surface before resolve.
    The actual Kotlin loader/resolver and normalization parity are tested separately;
    this is not a certificate for arbitrary future runtime code or Unicode versions.
    """
    if not isinstance(lex, dict):
        raise ValueError('runtime lexicon root must be an object')
    version = lex.get('schemaVersion')
    if type(version) not in (int, float) or version != 1:
        raise ValueError('unsupported runtime schemaVersion; expected numeric version 1')
    for field in ('aliases', 'ocrCorrections', 'mappings'):
        if not isinstance(lex.get(field), list):
            raise ValueError(f'runtime lexicon {field} must be a list')
    def text(value, location):
        if not isinstance(value, str) or not value.strip():
            raise ValueError(f'{location}: nonblank string required')
        return value
    language_keys, owners = set(), {}
    for index, entry in enumerate(lex['aliases']):
        location = f'aliases[{index}]'
        if not isinstance(entry, dict):
            raise ValueError(f'{location}: object required')
        owner = text(entry.get('canonicalId'), location + '.canonicalId')
        lang = runtime_language(entry.get('language'), location)
        if not isinstance(entry.get('aliases'), list) or not entry['aliases']:
            raise ValueError(f'{location}.aliases: nonempty list required')
        if not isinstance(entry.get('ocrVariants'), list):
            raise ValueError(f'{location}.ocrVariants: list required')
        for surface in entry['aliases'] + entry['ocrVariants']:
            key = normalized(text(surface, location + '.surface'))
            if (lang, key) in language_keys:
                raise ValueError(f'duplicate runtime alias: {lang}/{key}')
            language_keys.add((lang, key))
            if key in owners and owners[key] != owner:
                raise ValueError(f'conflicting runtime alias across languages: {key}')
            owners[key] = owner
    for index, mapping in enumerate(lex['mappings']):
        location = f'mappings[{index}]'
        if not isinstance(mapping, dict):
            raise ValueError(f'{location}: object required')
        text(mapping.get('surfaceForm'), location + '.surfaceForm')
        text(mapping.get('conceptId'), location + '.conceptId')
        runtime_language(mapping.get('language'), location)
        for field in ('normalizedForm', 'mappingGroup', 'relation', 'source', 'confidence'):
            if mapping.get(field) is not None and not isinstance(mapping[field], str):
                raise ValueError(f'{location}.{field}: string or null required')
    correction_keys = set()
    protected = {normalized(form) for form in FORMS.values()}
    for index, correction in enumerate(lex['ocrCorrections']):
        location = f'ocrCorrections[{index}]'
        if not isinstance(correction, dict):
            raise ValueError(f'{location}: object required')
        lang = runtime_language(correction.get('language'), location)
        original = normalized(text(correction.get('from'), location + '.from'))
        target = normalized(text(correction.get('to'), location + '.to'))
        if (lang, original) in correction_keys:
            raise ValueError(f'duplicate runtime OCR correction: {lang}/{original}')
        correction_keys.add((lang, original))
        # Any supported selected language can process a protected surface, even
        # when it differs from the PDF's language. Do not just check NL/NL, etc.
        if original in protected and target != original:
            raise ValueError(f'{location}: OCR correction redirects approved surface '
                             f'{lang}/{correction["from"]!r} to {correction["to"]!r}')


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f'duplicate JSON key: {key}')
        result[key] = value
    return result


def strict_json(text):
    def invalid_constant(value):
        raise ValueError(f'non-JSON numeric constant: {value}')
    return json.loads(text, object_pairs_hook=unique_object, parse_constant=invalid_constant)


def collisions(db, lex):
    owners, language_counts = {}, {}
    for entry in db:
        for term in [entry['name']] + entry.get('aliases', []) + [entry.get('eNumber')]:
            if term:
                owners.setdefault(normalized(term), set()).add(entry['id'])
    for entry in lex['aliases']:
        language = runtime_language(entry['language'], 'collision alias')
        for term in entry.get('aliases', []) + entry.get('ocrVariants', []):
            key = normalized(term)
            owners.setdefault(key, set()).add(entry['canonicalId'])
            pair = language, key
            language_counts[pair] = language_counts.get(pair, 0) + 1
    for mapping in lex['mappings']:
        owners.setdefault(normalized(mapping['surfaceForm']), set()).add(mapping['conceptId'])
    for lang, form in FORMS.items():
        key = normalized(form)
        if owners.get(key, set()) - {CONCEPT}:
            raise ValueError(f'alias collision: {lang}/{form}; owners={sorted(owners[key])}')
        if language_counts.get((lang, key), 0) > 1:
            raise ValueError(f'duplicate language alias: {lang}/{form}')


def integrate(args):
    evidence = source_evidence()
    expected_entry, expected_aliases, expected_mappings = expected_batch(evidence)
    db, lex, sources = [strict_json(path.read_text(encoding='utf8')) for path in (I, L, S)]
    validate_runtime_lexicon(lex)  # Before current-batch success and any mutation.
    if len({entry['id'] for entry in db}) != len(db):
        raise ValueError('duplicate ingredient ids')
    if len({entry['id'] for entry in sources}) != len(sources):
        raise ValueError('duplicate source ids')
    collisions(db, lex)  # Always, including already-present batches, before write.
    reviewed_sources = source_records()
    for expected in reviewed_sources:
        actual = [entry for entry in sources if entry['id'] == expected['id']]
        if actual and actual != [expected]:
            raise ValueError(f'divergent source record: {expected["id"]}')
    existing_entry = [entry for entry in db if entry['id'] == CONCEPT]
    existing_aliases = [entry for entry in lex['aliases'] if entry['canonicalId'] == CONCEPT]
    existing_mappings = [entry for entry in lex['mappings'] if entry['conceptId'] == CONCEPT]
    if existing_entry:
        # Full object equality includes extra/new properties and nested evidence.
        order = lambda entry: entry.get('language', '')
        if (existing_entry != [expected_entry] or
                sorted(existing_aliases, key=order) != sorted(expected_aliases, key=order) or
                sorted(existing_mappings, key=order) != sorted(expected_mappings, key=order) or
                any(expected not in sources for expected in reviewed_sources)):
            raise ValueError('incomplete or divergent reviewed batch/provenance')
        print('CELEX 02004R0853: changes=0; no modification necessary; batch is current')
        return
    if existing_aliases or existing_mappings:
        raise ValueError('orphan batch aliases/mappings; refusing partial import')
    if args.check:
        raise SystemExit('mechanically separated meat batch is absent; changes required')
    before = copy.deepcopy((db, lex, sources))
    db.append(expected_entry)
    lex['aliases'].extend(expected_aliases)
    lex['mappings'].extend(expected_mappings)
    added_sources = [source for source in reviewed_sources if source not in sources]
    sources.extend(added_sources)
    if (db[:-1] != before[0] or lex['aliases'][:-4] != before[1]['aliases'] or
            lex['mappings'][:-4] != before[1]['mappings'] or sources[:len(before[2])] != before[2]):
        raise ValueError('historical knowledge modified')
    collisions(db, lex)
    print('CELEX 02004R0853: changes=1; concepts=1; denominations=4; mappings=4; '
          f'sources={len(added_sources)}')
    if args.dry_run:
        print(json.dumps({'concept': expected_entry, 'aliases': expected_aliases,
                          'mappings': expected_mappings, 'sources': added_sources}, ensure_ascii=False, indent=2))
    if args.write:
        for path, value in zip((I, L, S), (db, lex, sources)):
            path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf8')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument('--dry-run', action='store_true', help='Validate and show proposal without writing')
    mode.add_argument('--check', action='store_true', help='Require the complete conforming batch; never write')
    mode.add_argument('--write', action='store_true', help='Append only the reviewed batch to three editorial JSON files')
    args = parser.parse_args()
    try:
        integrate(args)
    except (ValueError, OSError, KeyError, TypeError) as error:
        parser.exit(1, f'ERROR: {error}\n')


if __name__ == '__main__':
    main()
