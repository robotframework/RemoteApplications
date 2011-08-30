import unittest

from robot.utils.asserts import assert_raises_with_msg

import RemoteApplications


class TestMultipleKeywordsWithSameName(unittest.TestCase):

    def test_two_libraries_contain_keywords_with_different_names(self):
        lib1 = Library('lib1', ['foo', 'bar'])
        lib2 = Library('lib2', ['zip', 'zap'])
        expected = ['bar', 'foo', 'zap', 'zip']
        self._test_returned_kw_names([lib1, lib2], expected)

    def test_two_libraries_contain_keywords_with_same_name(self):
        lib1 = Library('lib1', ['foo', 'bar'])
        lib2 = Library('lib2', ['foo', 'zap'])
        expected = ['bar', 'foo', 'lib1.foo', 'lib2.foo', 'zap']
        self._test_returned_kw_names([lib1, lib2], expected)

    def _test_returned_kw_names(self, libs, expected):
        remote_app = RemoteApplications.RemoteApplication()
        remote_app._libs = libs
        self.assertEquals(expected, remote_app.get_keyword_names())


class TestRunningMultipleKeywordsWithSameName(unittest.TestCase):

    def setUp(self):
        self._remote_app = RemoteApplications.RemoteApplication()
        lib1 = Library('lib1', ['foo', 'bar'])
        lib2 = Library('lib2', ['foo', 'zap'])
        self._remote_app._libs = [lib1, lib2]
        self._remote_app.get_keyword_names()

    def test_running_kw_with_single_kw(self):
        self._remote_app.run_keyword('bar', [])
        self._remote_app.run_keyword('zap', [])

    def test_run_duplicate_kw_with_short_name(self):
        msg = "Keyword 'foo' available from multiple remote libraries. Use: 'lib1.foo' or 'lib2.foo'"
        assert_raises_with_msg(RuntimeError, msg, 
                               self._remote_app.run_keyword, 'foo', [])

    def test_run_duplicate_kw_with_long_name(self):
        self._remote_app.run_keyword('lib1.foo', [])
        self._remote_app.run_keyword('lib2.foo', [])


class Library:

    def __init__(self, name, keyword_names):
        self.keyword_names = keyword_names
        self.name = name

    def get_keyword_names(self):
        return self.keyword_names

    def run_keyword(self, name, args):
        pass

if __name__ == '__main__':
    unittest.main()
