#!/usr/bin/env python3
"""Read-only preservation guard; reference is pinned to the reviewed Git history.

Additions do not refresh the reference. An intentional historical migration must
review its diff, replace the reference from an identified revision, update the
pinned digest, and explain the changed guarantees in docs/knowledge-pipeline.md.
"""
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = ROOT / 'tools/testdata/knowledge_history_d1f0227.json'
PINNED_REFERENCE_SHA256 = '043a1c61b3226e88e6e1d251d9c7b10e268fa6ed0d6400a8710d9706e5289f1b'
ABSENT_ALIAS = {'canonicalId': 'cereals', 'language': 'NL',
                'aliases': ['granen'], 'ocrVariants': []}


def signature(value):
    text = json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(',', ':'), allow_nan=False)
    return hashlib.sha256(text.encode('utf8')).hexdigest()


def lexical_records(lexicon):
    for entry in lexicon['aliases']:
        for kind in ('aliases', 'ocrVariants'):
            for surface in entry.get(kind, []):
                yield {'canonicalId': entry['canonicalId'], 'language': entry['language'],
                       'kind': kind, 'surfaceForm': surface}


def load_reference():
    reference = json.loads(REFERENCE.read_text(encoding='utf8'))
    if signature(reference) != PINNED_REFERENCE_SHA256:
        raise ValueError('historical reference digest changed; an explicit reviewed migration is required')
    return reference


def require_signatures(label, expected, records):
    remaining = Counter(expected) - Counter(signature(value) for value in records)
    if remaining:
        raise ValueError(f'historical {label} missing or changed: {sum(remaining.values())} record(s)')


def validate_history(ingredients, lexicon):
    reference = load_reference()
    ids = [entry['id'] for entry in ingredients]
    available = set(ids)
    if len(ids) != len(available):
        raise ValueError('duplicate canonical concept ids')
    missing = set(reference['conceptIds']) - available
    if missing:
        raise ValueError(f'historical canonical concepts unavailable: {", ".join(sorted(missing))}')
    unavailable = [entry for entry in lexicon['aliases'] if entry['canonicalId'] not in available]
    # This is one exact historical exception, not a filter for any unavailable id.
    if unavailable != [ABSENT_ALIAS]:
        raise ValueError('unexpected unavailable canonical alias; only cereals/NL/granen is established')
    require_signatures('lexical surfaces', reference['lexicalSignatures'], lexical_records(lexicon))
    require_signatures('mapping objects', reference['mappingSignatures'], lexicon['mappings'])
    require_signatures('OCR corrections', reference['correctionSignatures'], lexicon['ocrCorrections'])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument('--check', action='store_true', help='Read current editorial files; never write')
    mode.add_argument('--stdin', action='store_true', help='Validate a JSON test payload from stdin; never write')
    args = parser.parse_args()
    try:
        if args.stdin:
            payload = json.load(sys.stdin)
            ingredients, lexicon = payload['ingredients'], payload['lexicon']
        else:
            ingredients = json.loads((ROOT / 'knowledge/ingredients.json').read_text(encoding='utf8'))
            lexicon = json.loads((ROOT / 'knowledge/ingredient_aliases_multilingual.json').read_text(encoding='utf8'))
        validate_history(ingredients, lexicon)
    except (ValueError, OSError, KeyError, TypeError) as error:
        parser.exit(1, f'ERROR: {error}\n')
    print('Historical knowledge preserved; approved additions are allowed; exception=cereals/NL/granen')


if __name__ == '__main__':
    main()
