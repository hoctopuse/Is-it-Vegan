"""Targeted CLI tests on temporary editorial JSON; no repository writes."""
import copy
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
IMPORTER = ROOT / 'tools/import_eu_food_hygiene_regulation.py'
spec = importlib.util.spec_from_file_location('eu853', IMPORTER)
eu = importlib.util.module_from_spec(spec)
spec.loader.exec_module(eu)
LAUNCHER = '''
import importlib.util,json,sys
from pathlib import Path
from unittest.mock import patch
spec=importlib.util.spec_from_file_location('eu853',sys.argv[1])
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
fixture=Path(sys.argv[2]);m.I=fixture/'ingredients.json';m.L=fixture/'lexicon.json';m.S=fixture/'sources.json'
evidence=json.loads((fixture/'evidence.json').read_text(encoding='utf8'))
m.source_evidence=lambda:evidence
mode=sys.argv[3];sys.argv=['importer',mode]
if mode=='--write':
    m.main()
else:
    with patch.object(Path,'write_text',side_effect=AssertionError('forbidden CLI write')), patch.object(Path,'write_bytes',side_effect=AssertionError('forbidden CLI write')):
        m.main()
'''


class FoodHygieneImportTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.evidence = eu.source_evidence()  # Verify all eight real PDFs once.
        cls.entry, cls.aliases, cls.mappings = eu.expected_batch(cls.evidence)
        cls.db = [x for x in json.loads(eu.I.read_text(encoding='utf8')) if x['id'] != eu.CONCEPT]
        cls.lex = json.loads(eu.L.read_text(encoding='utf8'))
        cls.lex['aliases'] = [x for x in cls.lex['aliases'] if x['canonicalId'] != eu.CONCEPT]
        cls.lex['mappings'] = [x for x in cls.lex['mappings'] if x['conceptId'] != eu.CONCEPT]
        cls.sources = [x for x in json.loads(eu.S.read_text(encoding='utf8'))
                       if x['id'] not in {eu.PRIMARY_ID, eu.LABEL_ID}]

    def conforming(self):
        db, lex, sources = copy.deepcopy((self.db, self.lex, self.sources))
        db.append(copy.deepcopy(self.entry))
        lex['aliases'].extend(copy.deepcopy(self.aliases))
        lex['mappings'].extend(copy.deepcopy(self.mappings))
        sources.extend(eu.source_records())
        return db, lex, sources

    def run_cli(self, values, mode='--check', raw_lexicon=None):
        with tempfile.TemporaryDirectory(prefix='eu853-test-') as directory:
            fixture = Path(directory)
            for name, value in zip(('ingredients.json', 'lexicon.json', 'sources.json'), values):
                (fixture / name).write_text(json.dumps(value), encoding='utf8')
            if raw_lexicon is not None:
                (fixture / 'lexicon.json').write_text(raw_lexicon, encoding='utf8')
            (fixture / 'evidence.json').write_text(json.dumps(self.evidence), encoding='utf8')
            before = {p.name: p.read_bytes() for p in fixture.iterdir()}
            process = subprocess.run([sys.executable, '-B', '-X', 'utf8', '-c', LAUNCHER,
                                      str(IMPORTER), directory, mode],
                                     capture_output=True, text=True, encoding='utf8')
            after = {p.name: p.read_bytes() for p in fixture.iterdir()}
            if mode != '--write' or process.returncode != 0:
                self.assertEqual(before, after)
            final = tuple(json.loads((fixture / name).read_text(encoding='utf8'))
                          for name in ('ingredients.json', 'lexicon.json', 'sources.json'))
            return process.returncode, process.stdout + process.stderr, final, before == after

    def rejected(self, values, mode='--check'):
        code, output, _, unchanged = self.run_cli(values, mode)
        self.assertNotEqual(0, code, output)
        self.assertTrue(unchanged)

    def test_absent_check_fails_without_write(self):
        self.rejected((self.db, self.lex, self.sources))

    def test_partial_or_orphan_batch_fails(self):
        db, lex, sources = self.conforming()
        lex['aliases'] = lex['aliases'][:-1]
        self.rejected((db, lex, sources))
        self.rejected((self.db, lex, sources), '--write')

    def test_current_check_and_dry_run_are_idempotent(self):
        for mode in ('--check', '--check', '--dry-run', '--write'):
            code, output, _, unchanged = self.run_cli(self.conforming(), mode)
            self.assertEqual(0, code, output)
            self.assertIn('changes=0', output)
            self.assertIn('no modification necessary', output)
            self.assertTrue(unchanged)

    def test_absent_dry_run_proposes_only_approved_forms(self):
        code, output, _, unchanged = self.run_cli((self.db, self.lex, self.sources), '--dry-run')
        self.assertEqual(0, code, output)
        self.assertTrue(unchanged)
        proposal = json.loads(output.split('\n', 1)[1])
        self.assertEqual('mechanically_separated_meat', proposal['concept']['id'])
        self.assertEqual({'FR', 'NL', 'EN', 'DE'}, {x['language'] for x in proposal['aliases']})
        self.assertEqual({'viandes séparées mécaniquement', 'separatorvlees',
                          'mechanically separated meat', 'Separatorenfleisch'},
                         {s for x in proposal['aliases'] for s in x['aliases']})
        self.assertEqual(4, len(proposal['mappings']))

    def test_write_only_appends_expected_batch_and_preserves_history(self):
        code, output, values, _ = self.run_cli((self.db, self.lex, self.sources), '--write')
        self.assertEqual(0, code, output)
        self.assertEqual(self.conforming(), values)
        self.assertEqual(self.db, values[0][:-1])
        self.assertEqual(self.lex['aliases'], values[1]['aliases'][:-4])
        self.assertEqual(self.lex['mappings'], values[1]['mappings'][:-4])

    def test_divergent_concept_or_extra_alias_fails(self):
        for field, value in [('status', 'VEGAN'), ('name', 'meat'), ('aliases', ['MSM'])]:
            with self.subTest(field=field):
                values = self.conforming()
                values[0][-1][field] = value
                self.rejected(values)

    def test_all_mapping_fields_missing_or_altered_fail(self):
        for field in self.mappings[0]:
            for missing in (True, False):
                with self.subTest(field=field, missing=missing):
                    values = self.conforming()
                    mapping = values[1]['mappings'][-4]
                    if missing:
                        del mapping[field]
                    else:
                        mapping[field] = [] if field == 'sourceEvidence' else 'WRONG'
                    self.rejected(values)

    def test_nested_evidence_and_unknown_fields_fail(self):
        for field in self.evidence['FR'][0]:
            for index in (0, 1):
                with self.subTest(field=field, index=index):
                    values = self.conforming()
                    del values[1]['mappings'][-4]['sourceEvidence'][index][field]
                    self.rejected(values)
        for nested in (False, True):
            values = self.conforming()
            mapping = values[1]['mappings'][-4]
            (mapping['sourceEvidence'][0] if nested else mapping)['unexpected'] = 'NEW'
            self.rejected(values)

    def test_missing_or_divergent_source_fails(self):
        values = self.conforming()
        values[2].pop()
        self.rejected(values)
        values = self.conforming()
        values[2][-1]['consolidationDate'] = 'WRONG'
        self.rejected(values, '--write')

    def test_cross_language_canonical_ocr_and_mapping_collisions_fail(self):
        for present in (False, True):
            for kind in ('alias', 'ocr', 'canonical', 'mapping'):
                with self.subTest(present=present, kind=kind):
                    values = self.conforming() if present else copy.deepcopy((self.db, self.lex, self.sources))
                    surface = 'SEPARAT\u034fORVLEES'
                    if kind in ('alias', 'ocr'):
                        values[1]['aliases'].append({'canonicalId': 'olive_oil', 'language': 'EN',
                            'aliases': [surface] if kind == 'alias' else ['unrelated olive oil'],
                            'ocrVariants': [surface] if kind == 'ocr' else []})
                    elif kind == 'canonical':
                        values[0][0]['name'] = surface
                    else:
                        values[1]['mappings'][0]['surfaceForm'] = surface
                    self.rejected(values, '--write')

    def test_same_language_duplicate_fails(self):
        values = self.conforming()
        values[1]['aliases'].append(copy.deepcopy(values[1]['aliases'][-1]))
        self.rejected(values)

    def test_runtime_normalization_golden_cases(self):
        for raw, expected in [('Œuf Æ', 'oeuf ae'), (' É-cole ', 'e cole'),
                              ('E  120', 'e120'), ('x E 120', 'x e 120'),
                              ('SEPARAT\u034fORVLEES', 'separatorvlees')]:
            self.assertEqual(expected, eu.normalized(raw))

    def test_acquisition_rejects_changed_pdf_and_wrong_offset(self):
        with tempfile.TemporaryDirectory(prefix='eu853-source-') as directory:
            path = Path(directory) / 'wrong.pdf'
            path.write_bytes(b'not the approved PDF')
            with patch.object(eu, 'pdf_path', return_value=path):
                with self.assertRaisesRegex(ValueError, 'hash mismatch'):
                    eu.source_evidence()
        wrong = copy.deepcopy(eu.PRIMARY)
        digest, offset, surface = wrong['FR']
        wrong['FR'] = (digest, offset + 1, surface)
        specs = ((*(eu.SPECS[0][:9]), wrong, *eu.SPECS[0][10:]), eu.SPECS[1])
        with patch.object(eu, 'SPECS', specs):
            with self.assertRaisesRegex(ValueError, 'wording/offset mismatch'):
                eu.source_evidence()

    def test_runtime_schema_versions_fail_in_both_read_only_modes(self):
        for mode in ('--check', '--dry-run'):
            for version in (2, None, '1', True, 1.9):
                with self.subTest(mode=mode, version=version):
                    values = self.conforming()
                    values[1]['schemaVersion'] = version
                    code, output, _, unchanged = self.run_cli(values, mode)
                    self.assertNotEqual(0, code)
                    self.assertIn('schemaVersion', output)
                    self.assertTrue(unchanged)

    def test_ocr_redirects_of_all_approved_inputs_fail_before_current_success(self):
        for mode in ('--check', '--dry-run'):
            for language, source in [('DUTCH', 'separatorvlees'), ('EN', eu.FORMS['EN']),
                                     ('FR', eu.FORMS['FR']), ('GERMAN', eu.FORMS['DE']),
                                     ('DE', eu.FORMS['FR'])]:
                with self.subTest(mode=mode, language=language, source=source):
                    values = self.conforming()
                    values[1]['ocrCorrections'].append({'language': language, 'from': source, 'to': 'rundvlees'})
                    code, output, _, unchanged = self.run_cli(values, mode)
                    self.assertNotEqual(0, code)
                    self.assertIn('OCR correction redirects approved surface', output)
                    self.assertTrue(unchanged)

    def test_safe_ocr_corrections_and_current_batch_do_not_write(self):
        for mode in ('--check', '--dry-run'):
            for correction in (None, {'language': 'dutch', 'from': 'separatovlees', 'to': 'separatorvlees'},
                               {'language': 'NL', 'from': 'SEPARAT\u034fORVLEES', 'to': 'Separatorvlees'}):
                with self.subTest(mode=mode, correction=correction):
                    values = self.conforming()
                    if correction: values[1]['ocrCorrections'].append(correction)
                    code, output, _, unchanged = self.run_cli(values, mode)
                    self.assertEqual(0, code, output)
                    self.assertIn('no modification necessary', output)
                    self.assertTrue(unchanged)

    def test_runtime_bad_shapes_languages_and_duplicate_corrections_fail(self):
        for mode in ('--check', '--dry-run'):
            for case in ('alias_language', 'mapping_language', 'blank_correction', 'correction_language',
                         'duplicate_correction', 'correction_type', 'alias_type', 'mapping_type'):
                with self.subTest(mode=mode, case=case):
                    values = self.conforming()
                    lex = values[1]
                    if case == 'alias_language': lex['aliases'][0]['language'] = 'XX'
                    elif case == 'mapping_language': lex['mappings'][0]['language'] = 'XX'
                    elif case == 'blank_correction': lex['ocrCorrections'][0]['to'] = ''
                    elif case == 'correction_language': lex['ocrCorrections'][0]['language'] = 'XX'
                    elif case == 'duplicate_correction': lex['ocrCorrections'].append(copy.deepcopy(lex['ocrCorrections'][0]))
                    elif case == 'correction_type': lex['ocrCorrections'][0]['to'] = 42
                    elif case == 'alias_type': lex['aliases'][0]['aliases'] = 'not a list'
                    else: lex['mappings'][0]['surfaceForm'] = 42
                    self.rejected(values, mode)

    def test_partial_source_and_collisions_fail_in_read_only_modes(self):
        for mode in ('--check', '--dry-run'):
            for case in ('partial', 'source', 'alias', 'ocrVariant', 'canonical', 'mapping'):
                with self.subTest(mode=mode, case=case):
                    values = self.conforming()
                    surface = 'SEPARAT\u034fORVLEES'
                    if case == 'partial': values[1]['aliases'].pop()
                    elif case == 'source': values[2][-1]['consolidationDate'] = 'WRONG'
                    elif case in ('alias', 'ocrVariant'):
                        values[1]['aliases'].append({'canonicalId': 'olive_oil', 'language': 'EN',
                            'aliases': [surface] if case == 'alias' else ['another olive oil'],
                            'ocrVariants': [surface] if case == 'ocrVariant' else []})
                    elif case == 'canonical': values[0][0]['name'] = surface
                    else: values[1]['mappings'][0]['surfaceForm'] = surface
                    self.rejected(values, mode)

    def test_duplicate_json_keys_are_rejected_like_the_runtime_parser(self):
        values = self.conforming()
        raw = json.dumps(values[1])[:-1] + ',"schemaVersion":1}'
        for mode in ('--check', '--dry-run'):
            code, output, _, unchanged = self.run_cli(values, mode, raw_lexicon=raw)
            self.assertNotEqual(0, code)
            self.assertIn('duplicate JSON key', output)
            self.assertTrue(unchanged)

    def test_unverified_unicode_cannot_silently_evade_the_ocr_guard(self):
        for mode in ('--check', '--dry-run'):
            values = self.conforming()
            values[1]['ocrCorrections'].append({'language': 'NL', 'from': 'SEPARAT\u0378ORVLEES', 'to': 'rundvlees'})
            code, output, _, unchanged = self.run_cli(values, mode)
            self.assertNotEqual(0, code)
            self.assertIn('unverified Unicode codepoint U+0378', output)
            self.assertTrue(unchanged)


if __name__ == '__main__':
    unittest.main()
