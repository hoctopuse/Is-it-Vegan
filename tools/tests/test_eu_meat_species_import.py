"""CLI rejection tests on temporary inputs; never invoke an import in write mode."""
import copy
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
IMPORTER = ROOT / 'tools/import_eu_agricultural_products_regulation.py'
spec = importlib.util.spec_from_file_location('eu1308', IMPORTER)
eu = importlib.util.module_from_spec(spec)
spec.loader.exec_module(eu)

# Run the real CLI dispatcher and obtain real process exit codes. Only the PDF
# evidence extraction is cached across fixture processes, after a real extraction.
# Any importer write attempt fails, including writes of identical bytes.
LAUNCHER = '''
import importlib.util,json,sys
from pathlib import Path
from unittest.mock import patch
spec=importlib.util.spec_from_file_location('eu1308',sys.argv[1])
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
fixture=Path(sys.argv[2]);m.I=fixture/'ingredients.json';m.L=fixture/'lexicon.json';m.S=fixture/'sources.json'
evidence=json.loads((fixture/'evidence.json').read_text(encoding='utf8'))
m.meat_species_evidence=lambda:{tuple(key):record for key,record in evidence}
sys.argv=['importer','--meat-species',sys.argv[3]]
with patch.object(Path,'write_text',side_effect=AssertionError('forbidden importer write')), patch.object(Path,'write_bytes',side_effect=AssertionError('forbidden importer write')):
    m.main()
'''


class MeatSpeciesImportTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.evidence = eu.meat_species_evidence()
        cls.db = json.loads(eu.I.read_text(encoding='utf8'))
        cls.lex = json.loads(eu.L.read_text(encoding='utf8'))
        cls.sources = json.loads(eu.S.read_text(encoding='utf8'))

    def run_cli(self, db=None, lex=None, mode='--check'):
        with tempfile.TemporaryDirectory(prefix='eu1308-meat-test-') as directory:
            fixture = Path(directory)
            values = {'ingredients.json': self.db if db is None else db,
                      'lexicon.json': self.lex if lex is None else lex,
                      'sources.json': self.sources,
                      'evidence.json': list(self.evidence.items())}
            for name, value in values.items():
                (fixture / name).write_text(json.dumps(value), encoding='utf8')
            before = {p.name: p.read_bytes() for p in fixture.iterdir()}
            process = subprocess.run([sys.executable, '-B', '-X', 'utf8', '-c', LAUNCHER,
                                      str(IMPORTER), directory, mode],
                                     capture_output=True, text=True, encoding='utf8')
            self.assertEqual(before, {p.name: p.read_bytes() for p in fixture.iterdir()})
            return process.returncode, process.stdout + process.stderr

    def absent(self):
        db = [copy.deepcopy(x) for x in self.db if x['id'] not in eu.MEAT_SPECIES_NEW]
        lex = copy.deepcopy(self.lex)
        lex['aliases'] = [x for x in lex['aliases'] if x['canonicalId'] not in eu.MEAT_SPECIES_NEW]
        lex['mappings'] = [x for x in lex['mappings'] if x['conceptId'] not in eu.MEAT_SPECIES_NEW]
        return db, lex

    def assert_rejected(self, db=None, lex=None, mode='--check', message=None):
        code, output = self.run_cli(db, lex, mode)
        self.assertNotEqual(0, code, output)
        if message:
            self.assertIn(message, output)

    def test_absent_check_fails(self):
        self.assert_rejected(*self.absent(), message='batch is absent')

    def test_partial_check_fails(self):
        db = [x for x in self.db if x['id'] != 'goat_meat']
        self.assert_rejected(db=db, message='partial meat species batch')

    def test_conforming_check_is_idempotent(self):
        for _ in range(2):
            code, output = self.run_cli()
            self.assertEqual(0, code, output)
            self.assertIn('changes=0', output)

    def test_divergent_concept_fails(self):
        db = copy.deepcopy(self.db)
        next(x for x in db if x['id'] == 'goat_meat')['status'] = 'VEGAN'
        self.assert_rejected(db=db, message='concept differs')

    def test_mapping_properties_wrong_or_missing_fail(self):
        fields = ['conceptId', 'language', 'normalizedForm', 'surfaceForm', 'source',
                  'relation', 'mappingGroup', 'confidence', 'sourceEvidence']
        for field in fields:
            for missing in (False, True):
                with self.subTest(field=field, missing=missing):
                    lex = copy.deepcopy(self.lex)
                    mapping = next(x for x in lex['mappings'] if x['conceptId'] == 'goat_meat')
                    if missing:
                        del mapping[field]
                    else:
                        mapping[field] = [] if field == 'sourceEvidence' else 'INCORRECT'
                    self.assert_rejected(lex=lex)

    def test_all_nested_evidence_properties_checked(self):
        for field in ['pdfFile', 'pdfPage', 'legalLocation', 'sourceOffset',
                      'surfaceForm', 'reviewId', 'sha256']:
            for missing in (False, True):
                with self.subTest(field=field, missing=missing):
                    lex = copy.deepcopy(self.lex)
                    evidence = next(x for x in lex['mappings'] if x['conceptId'] == 'goat_meat')['sourceEvidence'][0]
                    if missing:
                        del evidence[field]
                    else:
                        evidence[field] = 'INCORRECT'
                    self.assert_rejected(lex=lex, message='mappings/provenance differ')

    def test_unknown_future_mapping_property_not_ignored(self):
        lex = copy.deepcopy(self.lex)
        next(x for x in lex['mappings'] if x['conceptId'] == 'goat_meat')['futureProvenance'] = 'unreviewed'
        self.assert_rejected(lex=lex, message='mappings/provenance differ')

    def conflict(self, lex, surface='geitenvlees', variants=False):
        lex['aliases'].append({'canonicalId': 'meat', 'language': 'EN',
                               'aliases': ['fixture'] if variants else [surface],
                               'ocrVariants': [surface] if variants else []})

    def test_cross_language_collision_before_import(self):
        db, lex = self.absent()
        self.conflict(lex)
        self.assert_rejected(db, lex, '--dry-run', 'meat alias collision')

    def test_collision_introduced_after_import(self):
        lex = copy.deepcopy(self.lex)
        self.conflict(lex)
        for mode in ('--check', '--dry-run'):
            self.assert_rejected(lex=lex, mode=mode, message='meat alias collision')

    def test_runtime_mark_normalization_and_ocr_collision(self):
        lex = copy.deepcopy(self.lex)
        # U+034F is a Unicode mark with combining class zero: combining() alone
        # does not reproduce Kotlin's removal of all \\p{M} characters.
        self.conflict(lex, 'GEITEN\u034fVLEES', variants=True)
        self.assert_rejected(lex=lex, message='meat alias collision')

    def test_canonical_name_alias_and_enumber_collisions(self):
        for field in ('name', 'aliases', 'eNumber'):
            with self.subTest(field=field):
                db = copy.deepcopy(self.db)
                owner = next(x for x in db if x['id'] == 'meat')
                owner[field] = ['geitenvlees'] if field == 'aliases' else 'geitenvlees'
                self.assert_rejected(db=db, message='meat alias collision')

    def test_duplicate_same_owner_language_alias_fails(self):
        lex = copy.deepcopy(self.lex)
        entry = next(x for x in lex['aliases'] if x['canonicalId'] == 'goat_meat')
        lex['aliases'].append(copy.deepcopy(entry))
        self.assert_rejected(lex=lex, message='duplicate meat language alias')

    def test_existing_orphan_alias_cannot_create_duplicate(self):
        db, lex = self.absent()
        lex['aliases'].append({'canonicalId': 'goat_meat', 'language': 'NL',
                               'aliases': ['geitenvlees'], 'ocrVariants': []})
        self.assert_rejected(db, lex, '--dry-run', 'duplicate meat language alias')

    def test_loader_language_synonyms_cannot_hide_duplicates(self):
        db, lex = self.absent()
        lex['aliases'].append({'canonicalId': 'goat_meat', 'language': 'dutch',
                               'aliases': ['geitenvlees'], 'ocrVariants': []})
        self.assert_rejected(db, lex, '--dry-run', 'duplicate meat language alias')

    def test_dry_run_proposed_change(self):
        code, output = self.run_cli(*self.absent(), mode='--dry-run')
        self.assertEqual(0, code, output)
        self.assertIn('changes=1', output)
        self.assertIn('"languageAliases": 17', output)

    def test_dry_run_current_explicit(self):
        code, output = self.run_cli(mode='--dry-run')
        self.assertEqual(0, code, output)
        self.assertIn('changes=0', output)
        self.assertIn('no modification necessary', output)

    def test_text_normalizer_golden_cases(self):
        for raw, expected in [('Œuf Æ', 'oeuf ae'), (' É-cole ', 'e cole'),
                              ('E  120', 'e120'), ('x E 120', 'x e 120'),
                              ('GEITEN\u034fVLEES', 'geitenvlees')]:
            self.assertEqual(expected, eu.meat_runtime_normalized(raw))


if __name__ == '__main__':
    unittest.main()
