"""Historic reference checks on in-memory copies, including the real CLI."""
import copy
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / 'tools/validate_knowledge_history.py'
spec = importlib.util.spec_from_file_location('history', SCRIPT)
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)


class KnowledgeHistoryGuardTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.db = json.loads((ROOT / 'knowledge/ingredients.json').read_text(encoding='utf8'))
        cls.lex = json.loads((ROOT / 'knowledge/ingredient_aliases_multilingual.json').read_text(encoding='utf8'))

    def cli(self, db, lex):
        process = subprocess.run([sys.executable, '-B', '-X', 'utf8', str(SCRIPT), '--stdin'],
            input=json.dumps({'ingredients': db, 'lexicon': lex}), capture_output=True, text=True, encoding='utf8')
        return process.returncode, process.stdout + process.stderr

    def test_current_and_the_four_new_mappings_preserve_reference(self):
        guard.validate_history(self.db, self.lex)
        self.assertEqual(0, self.cli(self.db, self.lex)[0])

    def test_coordinated_lexical_and_mapping_deletion_fails(self):
        lex = copy.deepcopy(self.lex)
        lex['aliases'] = [x for x in lex['aliases'] if not(x['canonicalId'] == 'e100' and x['language'] == 'EN')]
        lex['mappings'] = [x for x in lex['mappings'] if not(x['conceptId'] == 'e100' and x['language'] == 'EN')]
        code, message = self.cli(self.db, lex)
        self.assertNotEqual(0, code)
        self.assertIn('historical lexical surfaces', message)

    def test_every_historical_mapping_property_is_protected(self):
        original = next(x for x in self.lex['mappings'] if x['conceptId'] == 'horse_meat')
        for field in original:
            with self.subTest(field=field):
                lex = copy.deepcopy(self.lex)
                row = next(x for x in lex['mappings'] if x == original)
                row[field] = 'WRONG'
                with self.assertRaisesRegex(ValueError, 'historical mapping objects'):
                    guard.validate_history(self.db, lex)
        lex = copy.deepcopy(self.lex)
        lex['mappings'][0]['unexpectedProperty'] = 'NEW'
        with self.assertRaisesRegex(ValueError, 'historical mapping objects'):
            guard.validate_history(self.db, lex)

    def test_changed_mapping_owner_fails_real_cli(self):
        lex = copy.deepcopy(self.lex)
        lex['mappings'][0]['conceptId'] = 'meat'
        code, message = self.cli(self.db, lex)
        self.assertNotEqual(0, code)
        self.assertIn('historical mapping objects', message)

    def test_unavailable_historical_concept_fails_even_if_its_rows_are_removed(self):
        db = [x for x in self.db if x['id'] != 'e100']
        code, message = self.cli(db, self.lex)
        self.assertNotEqual(0, code)
        self.assertIn('historical canonical concepts unavailable: e100', message)
        lex = copy.deepcopy(self.lex)
        lex['aliases'] = [x for x in lex['aliases'] if x['canonicalId'] != 'e100']
        lex['mappings'] = [x for x in lex['mappings'] if x['conceptId'] != 'e100']
        code, message = self.cli(db, lex)
        self.assertNotEqual(0, code)
        self.assertIn('historical canonical concepts unavailable: e100', message)

    def test_future_additions_and_extension_of_existing_alias_list_are_allowed(self):
        db, lex = copy.deepcopy((self.db, self.lex))
        concept = copy.deepcopy(db[0])
        concept.update(id='reviewed_future_test_concept', name='future test surface', aliases=['future test surface'])
        db.append(concept)
        lex['aliases'].append({'canonicalId': 'reviewed_future_test_concept', 'language': 'FR',
                              'aliases': ['future test surface'], 'ocrVariants': []})
        def mapping(owner, surface):
            return {'conceptId': owner, 'language': 'FR', 'surfaceForm': surface, 'normalizedForm': surface,
                    'mappingGroup': owner, 'relation': 'COMMON_LABEL_NAME', 'source': 'vegan-society', 'confidence': 'REVIEWED'}
        lex['mappings'].append(mapping(concept['id'], 'future test surface'))
        entry = next(x for x in lex['aliases'] if x['language'] == 'FR')
        entry['aliases'].append('future extra historical-owner surface')
        lex['mappings'].append(mapping(entry['canonicalId'], 'future extra historical-owner surface'))
        self.assertEqual(0, self.cli(db, lex)[0])

    def test_only_the_exact_cereals_nl_granen_exception_is_allowed(self):
        guard.validate_history(self.db, self.lex)
        for change in ('gran', 'language', 'other_absent_concept', 'extra_ocr', 'duplicate'):
            with self.subTest(change=change):
                lex = copy.deepcopy(self.lex)
                entry = next(x for x in lex['aliases'] if x['canonicalId'] == 'cereals')
                if change == 'gran': entry['aliases'] = ['gran']
                elif change == 'language': entry['language'] = 'EN'
                elif change == 'other_absent_concept': entry['canonicalId'] = 'new_unavailable'
                elif change == 'extra_ocr': entry['ocrVariants'] = ['grauen']
                else: lex['aliases'].append(copy.deepcopy(entry))
                with self.assertRaisesRegex(ValueError, 'only cereals/NL/granen'):
                    guard.validate_history(self.db, lex)

    def test_reference_cannot_be_silently_refreshed(self):
        reference = guard.load_reference()
        changed = copy.deepcopy(reference)
        changed['mappingSignatures'].pop()
        self.assertNotEqual(guard.PINNED_REFERENCE_SHA256, guard.signature(changed))
        with patch.object(Path, 'read_text', return_value=json.dumps(changed)):
            with self.assertRaisesRegex(ValueError, 'reference digest changed'):
                guard.load_reference()


if __name__ == '__main__':
    unittest.main()
